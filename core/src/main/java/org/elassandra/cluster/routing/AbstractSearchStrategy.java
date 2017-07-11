/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
 * Contains some code from Elasticsearch (http://www.elastic.co)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elassandra.cluster.routing;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.dht.Murmur3Partitioner.LongToken;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.TokenMetadata;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Only support Murmur3 Long Token.
 * SearchStrategy is per index
 * SearchStrategy.Router is updated each time node join/leave/start/stop the cluster.
 * SearchStrategy.Router.Route is per query route.
 * @author vroyer
 *
 */
public abstract class AbstractSearchStrategy {
    public static Logger logger = Loggers.getLogger(AbstractSearchStrategy.class);
    
    public static final Collection<Range<Token>> EMPTY_RANGE_TOKEN_LIST = ImmutableList.<Range<Token>>of();
    
    public static final Token TOKEN_MIN = new LongToken(Long.MIN_VALUE);
    public static final Token TOKEN_MAX = new LongToken(Long.MAX_VALUE);
    public static final Range<Token> FULL_RANGE_TOKEN = new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken(Long.MAX_VALUE));

    public abstract Router newRouter(final Index index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState);
    
    // per index router, updated on each cassandra ring change.
    public abstract class Router {
        final Index index;
        final String ksName;
        final long version;
        final DiscoveryNode localNode;
        final Map<UUID, ShardRoutingState> shardStates;
        
        protected Multimap<Token,DiscoveryNode> tokenToNodes = ArrayListMultimap.create();
        protected Map<DiscoveryNode, BitSet> greenShards;            // available   node to bitset of ranges => started primary.
        protected Map<DiscoveryNode, BitSet> redShards;            // unavailable node to bitset of orphan ranges => unassigned primary
        protected List<DiscoveryNode> yellowShards;                 // unassigned replica
        protected List<Token> tokens;
        protected boolean isConsistent = true;

        protected final TokenMetadata metadata;
        protected final AbstractReplicationStrategy strategy;
        
        public Router(final Index index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState, boolean includeReplica) 
        {
            this.index = index;
            this.ksName = ksName;
            this.version = clusterState.version();
            this.localNode = clusterState.nodes().getLocalNode();
            this.shardStates = shardStates;
            
            if (Keyspace.isInitialized() && StorageService.instance.isJoined()) {
                // only available when keyspaces are initialized and node joined
                //this.rangeToEndpointsMap = StorageService.instance.getRangeToAddressMapInLocalDC(ksName);
                this.strategy = Keyspace.open(ksName).getReplicationStrategy();
                this.metadata = StorageService.instance.getTokenMetadata().cloneOnlyTokenMap();
                for(DiscoveryNode node : clusterState.nodes()) {
                    for(Token token : this.metadata.getTokens(node.getInetAddress())) 
                        this.tokenToNodes.put(token, node);
                }
            } else {
                this.strategy = null;
                this.metadata = null;
            }
            
            this.tokens = new ArrayList<Token>(this.tokenToNodes.keys());
            this.tokens.add(TOKEN_MAX);
            Collections.sort(tokens);
            if (logger.isTraceEnabled())
                logger.trace("index=[{}] keyspace=[{}] ordered tokens={}",index, ksName, this.tokens);
            
            int i=0;
            this.greenShards = new HashMap<DiscoveryNode, BitSet>();
            for(Token token: tokens) {
                if (TOKEN_MIN.equals(token))
                    continue;

                // greenshard = available node -> token range bitset, 
                boolean orphanRange = true;
                for(InetAddress endpoint :  (this.metadata == null) ? Collections.singletonList(localNode.getInetAddress()) : this.strategy.calculateNaturalEndpoints(token, this.metadata)) {
                    UUID uuid = StorageService.instance.getHostId(endpoint);
                    DiscoveryNode node =  (uuid == null) ? clusterState.nodes().findByInetAddress(endpoint) : clusterState.nodes().get(uuid.toString());
                    if (node != null && node.status() == DiscoveryNode.DiscoveryNodeStatus.ALIVE) {
                        if (ShardRoutingState.STARTED.equals(shardStates.get(node.uuid()))) {
                            orphanRange = false;
                            BitSet bs = greenShards.get(node);
                            if (bs == null) {
                                bs = new BitSet(tokens.size() - 1);
                                greenShards.put(node, bs);
                            }
                            bs.set(i);
                            if (!includeReplica)
                                break;
                        }
                    }
                }
                
                // redshards = unavailable node->token range bitset, 
                if (orphanRange) {
                    isConsistent = false;
                    if (redShards == null) 
                        redShards = new HashMap<DiscoveryNode, BitSet>();
                    for(DiscoveryNode node : tokenToNodes.get(token)) {
                        BitSet bs = redShards.get(node);
                        if (bs == null) {
                            bs = new BitSet(tokens.size() - 1);
                            redShards.put(node, bs);
                        }
                        bs.set(i);
                    }
                }
                i++;
            }
            
            // yellow shards = unavailable nodes hosting token range available somewhere else in greenShards.
            for(DiscoveryNode node : clusterState.nodes()) {
                if (!this.greenShards.containsKey(node) && (this.redShards == null || !this.redShards.containsKey(node))) {
                    if (this.yellowShards == null) {
                        this.yellowShards  = new ArrayList<DiscoveryNode>();
                    }
                    this.yellowShards.add(node);
                }
            }
            
            if (logger.isTraceEnabled())
                logger.trace("index=[{}] keyspace=[{}] isConsistent={} greenShards={} redShards={} yellowShards={}",index, ksName, this.isConsistent, this.greenShards, this.redShards, this.yellowShards);
        }
        
        public abstract Route newRoute(@Nullable String preference, TransportAddress src);

        public boolean isConsistent() {
            return this.isConsistent;
        }
        
        public ShardRoutingState getShardRoutingState(DiscoveryNode node) {
            ShardRoutingState srs = this.shardStates.get(node.uuid());
            return (srs==null) ? ShardRoutingState.UNASSIGNED : srs;
        }
        
        public Collection<Range<Token>> getTokenRanges(BitSet bs) {
            List<Range<Token>> l = new ArrayList<Range<Token>>();
            int i = 0;
            while (i >= 0 && i < bs.length()) {
                int left = bs.nextSetBit(i);
                int right = bs.nextClearBit(left);
                l.add(new Range<Token>( (left == 0) ? TOKEN_MIN : tokens.get(left -1), tokens.get(right - 1)));
                i = right;
            }
            logger.trace("tokens={} bitset={} ranges={}", tokens, bs, l);
            return  l;
        }
        
        public abstract class Route {
            List<IndexShardRoutingTable> shardRouting = null;
            
            public Route() {
                shardRouting = buildShardRouting();
            }
        
            /**
             * Should returns selected shards token range bitset covering 100% of the cassandra ring.
             * @return
             */
            public abstract Map<DiscoveryNode, BitSet> selectedShards();
            
            public List<IndexShardRoutingTable> getShardRouting() {
                return this.shardRouting;
            }
            
            List<IndexShardRoutingTable> buildShardRouting() {
                List<IndexShardRoutingTable> isrt = new ArrayList<IndexShardRoutingTable>(selectedShards().size() + ((Router.this.redShards!=null) ? Router.this.redShards.size() : 0) );
                int i = 1;
                boolean todo = true;
                for(DiscoveryNode node : selectedShards().keySet()) {
                    int  shardId = (localNode.getId().equals(node.getId())) ? 0 : i;
                    
                    // started primary shards (green)
                    ShardRouting primaryShardRouting = new ShardRouting(new ShardId(index, shardId), node.getId(), true, 
                            ShardRoutingState.STARTED, null, 
                            Router.this.getTokenRanges(selectedShards().get(node)));
                    
                    if (todo && Router.this.yellowShards != null) {
                        // add all unassigned remote replica shards (yellow) on the first IndexShardRoutingTable.
                        todo = false;
                        List<ShardRouting> shards = new ArrayList<ShardRouting>(1+Router.this.yellowShards.size());
                        shards.add(primaryShardRouting);
                        for(DiscoveryNode node2 : Router.this.yellowShards) {
                            UnassignedInfo info = null;
                            if (ShardRoutingState.UNASSIGNED.equals(Router.this.getShardRoutingState(node2)))
                                info = IndexRoutingTable.UNASSIGNED_INFO_UNAVAILABLE;
                            if (node2.status() != DiscoveryNodeStatus.ALIVE)
                                info = IndexRoutingTable.UNASSIGNED_INFO_NODE_LEFT;
                            
                            // unassigned secondary shards (yellow)
                            ShardRouting replicaShardRouting = new ShardRouting(new ShardId(index, shardId), node2.getId(), false, 
                                    Router.this.getShardRoutingState(node2), info, EMPTY_RANGE_TOKEN_LIST);
                            shards.add(replicaShardRouting);
                        }
                        isrt.add( new IndexShardRoutingTable(new ShardId(index,shardId), shards) );
                    } else {
                        isrt.add( new IndexShardRoutingTable(new ShardId(index,shardId), primaryShardRouting) );
                    }
                    
                    if (shardId != 0)
                        i++;
                }
                
                if (Router.this.redShards != null) {
                    for(DiscoveryNode node : Router.this.redShards.keySet()) {
                        int  shardId = (localNode.getId().equals(node.getId())) ? 0 : i;
                        UnassignedInfo info = null;
                        if (ShardRoutingState.UNASSIGNED.equals(Router.this.getShardRoutingState(node)))
                            info = IndexRoutingTable.UNASSIGNED_INFO_UNAVAILABLE;
                        if (node.status() != DiscoveryNodeStatus.ALIVE)
                            info = IndexRoutingTable.UNASSIGNED_INFO_NODE_LEFT;
                        
                        // add one unassigned primary shards (red) for orphan token ranges.
                        ShardRouting primaryShardRouting = new ShardRouting(new ShardId(index, shardId), node.getId(), true, 
                                Router.this.getShardRoutingState(node),
                                info,
                                Router.this.getTokenRanges(Router.this.redShards.get(node)));
                        isrt.add( new IndexShardRoutingTable(new ShardId(index,shardId), primaryShardRouting) );
                        if (shardId != 0)
                            i++;
                    }
                }
                
                // shuffle shards to distribute fetch requests on first shard.
                Collections.shuffle(isrt);
                return isrt;
            }
             
        }
    };

    public static Class<AbstractSearchStrategy> getSearchStrategyClass(String cls) throws ConfigurationException
    {
        String className = cls.contains(".") ? cls : "org.elassandra.cluster.routing." + cls;
        Class<AbstractSearchStrategy> searchClass = FBUtilities.classForName(className, "search strategy");
        if (!AbstractSearchStrategy.class.isAssignableFrom(searchClass))
        {
            throw new ConfigurationException(String.format((Locale)null, "Specified search strategy class (%s) is not derived from AbstractSearchStrategy", className));
        }
        return searchClass;
    }

}
