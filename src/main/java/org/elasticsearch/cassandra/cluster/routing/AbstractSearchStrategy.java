/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.cassandra.cluster.routing;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.cassandra.dht.Murmur3Partitioner.LongToken;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
import org.apache.lucene.util.CollectionUtil;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.UnassignedInfo;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.shard.ShardId;

import com.google.common.collect.ImmutableList;

/**
 * Only support Murmur3 Long Token.
 * SearchStrategy is per index
 * SearchStrategy.Router is updated each time node join/leave/start/stop the cluster.
 * SearchStrategy.Router.Route is per query route.
 * @author vroyer
 *
 */
public abstract class AbstractSearchStrategy {
    public static ESLogger logger = Loggers.getLogger(AbstractSearchStrategy.class);
    
    public static final Collection<Range<Token>> EMPTY_RANGE_TOKEN = ImmutableList.<Range<Token>>of();

    public abstract Router newRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState);
    
    // per index router, updated on each cassandra ring change.
    public abstract class Router {
    	final String index;
    	final String ksName;
    	final long version;
    	final DiscoveryNode localNode;
    	final Map<UUID, ShardRoutingState> shardStates;
    	Map<Range<Token>, List<InetAddress>> rangeToEndpointsMap; // range to endpoints map
    	Map<DiscoveryNode, BitSet> greenShards;			// available   node to bitset of ranges => started primary.
    	Map<DiscoveryNode, BitSet> redShards;			// unavailable node to bitset of orphan ranges => unassigned primary
    	List<DiscoveryNode> yellowShards;		 		// unassigned replica
    	List<Range<Token>> tokens;
    	boolean isConsistent = true;
    	final Route route;
    	
    	public Router(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) 
    	{
    		this.index = index;
    		this.ksName = ksName;
    		this.version = clusterState.version();
            this.localNode = clusterState.nodes().localNode();
            this.shardStates = shardStates;
            this.rangeToEndpointsMap = StorageService.instance.getRangeToAddressMapInLocalDC(ksName);
            if (logger.isTraceEnabled())
            	logger.trace("rangeToEndpointsMap={}", this.rangeToEndpointsMap);
            
            Range<Token> wrappedAround = null;		
            for(Range<Token> rt : rangeToEndpointsMap.keySet()) {
            	if (rt.isWrapAround()) {
            		wrappedAround = rt;
            		break;
            	}
            }
            if (wrappedAround != null) {
            	List<InetAddress> endpoints =  this.rangeToEndpointsMap.remove(wrappedAround);
            	this.rangeToEndpointsMap.put( new Range<Token>(new LongToken((Long) wrappedAround.left.getTokenValue()), new LongToken(Long.MAX_VALUE)) , endpoints );
            	this.rangeToEndpointsMap.put( new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken((Long) wrappedAround.right.getTokenValue())) , endpoints );
            }
            this.tokens = new ArrayList<Range<Token>>(rangeToEndpointsMap.keySet());
            Collections.sort(tokens);
            if (logger.isTraceEnabled())
            	logger.trace("ordered tokens={}", this.tokens);
            
            int i=0;
            this.greenShards = new HashMap<DiscoveryNode, BitSet>();
            for(Range<Token> rt: tokens) {
            	boolean orphanRange = true;
            	for(InetAddress endpoint : rangeToEndpointsMap.get(rt)) {
            		UUID uuid = StorageService.instance.getHostId(endpoint);
            		assert uuid != null;
            		DiscoveryNode node = clusterState.nodes().get( uuid.toString() );
            		assert node != null;
            		if (ShardRoutingState.STARTED.equals(shardStates.get(uuid))) {
            			orphanRange = false;
	            		BitSet bs = greenShards.get(node);
	            		if (bs == null) {
	            			bs = new BitSet(tokens.size());
	            			greenShards.put(node, bs);
	            		}
	            		bs.set(i);
            		}
            	}
            	if (orphanRange) {
            		isConsistent = false;
            		if (redShards == null) 
            			redShards = new HashMap<DiscoveryNode, BitSet>();
            		for(InetAddress endpoint : rangeToEndpointsMap.get(rt)) {
            			UUID uuid = StorageService.instance.getHostId(endpoint);
            			assert uuid != null;
            			DiscoveryNode node = clusterState.nodes().get(uuid.toString());
            			assert node != null;
            			BitSet bs = redShards.get(node);
            			if (bs == null) {
	            			bs = new BitSet(tokens.size());
	            			redShards.put(node, bs);
	            		}
            			bs.set(i);
            		}
            	}
            	i++;
            }
	        for(DiscoveryNode node : clusterState.nodes()) {
	        	if (this.greenShards.get(node) == null && (this.redShards == null || this.redShards.get(node)==null)) {
	        		if (this.yellowShards == null) {
	        			this.yellowShards  = new ArrayList<DiscoveryNode>();
	        		}
	        		this.yellowShards.add(node);
	        	}
	        }
            if (!this.isConsistent)
            	logger.debug("Unavailable index={} keyspace={} ranges={}", index, ksName, redShards);
            this.route = new Route();
    	}
    	
    	public Route newRoute(@Nullable String preference, TransportAddress src) {
        	return this.route;
        }

        public boolean isConsistent() {
        	return isConsistent;
        }
        
    	public ShardRoutingState getShardRoutingState(DiscoveryNode node) {
    		ShardRoutingState srs = this.shardStates.get(node.uuid());
    		return (srs==null) ? ShardRoutingState.UNASSIGNED : srs;
    	}
    	
    	public Collection<Range<Token>> getTokenRanges(BitSet bitset) {
    		List<Range<Token>> l = new ArrayList<Range<Token>>();
    		int j=0;
    		Range<Token> last = null;
    		for(int i=0; i < tokens.size() && j < bitset.cardinality(); i++) {
    			if (bitset.get(i)) {
    				j++;
    				Range<Token> t = tokens.get(i);
    				if (last == null) {
    					last = t;
    				} else {
    					if (last.right.compareTo(t.left) == 0) {
    						last = new Range<Token>(last.left, t.right);
    					} else {
    						l.add(last);
    						last = t;
    					}
    				}			
    			}
    		}
    		if (last != null & !l.contains(last))
    			l.add(last);
    		return l;
    	}
        
    	public class Route {
    		public Route() {
    		}
    	
    		public Map<DiscoveryNode, BitSet> selectedShards() {
    			return Router.this.greenShards;
    		}
    		
			public List<IndexShardRoutingTable> getShardRouting() {
				List<IndexShardRoutingTable> isrt = new ArrayList<IndexShardRoutingTable>(selectedShards().size() + ((Router.this.redShards!=null) ? Router.this.redShards.size() : 0) );
				int i = 1;
				boolean first = true;
				for(DiscoveryNode node : selectedShards().keySet()) {
					int  shardId = (localNode.id().equals(node.id())) ? 0 : i;
					
					// started primary shards (green)
					ShardRouting primaryShardRouting = new ShardRouting(index, shardId, node.id(), true, 
							ShardRoutingState.STARTED, version, null, 
							Router.this.getTokenRanges(selectedShards().get(node)));
					
					if (!first || Router.this.yellowShards == null) {
						isrt.add( new IndexShardRoutingTable(new ShardId(index,shardId), primaryShardRouting) );
					} else {
						// add all unassigned secondary replica (yellow) on the first IndexShardRoutingTable.
						first = false;
						List<ShardRouting> shards = new ArrayList<ShardRouting>(1+Router.this.yellowShards.size());
						shards.add(primaryShardRouting);
						for(DiscoveryNode node2 : Router.this.yellowShards) {
							UnassignedInfo info = null;
							if (ShardRoutingState.UNASSIGNED.equals(Router.this.getShardRoutingState(node2)))
								info = IndexRoutingTable.UNASSIGNED_INFO_UNAVAILABLE;
							if (node2.status() != DiscoveryNodeStatus.ALIVE)
								info = IndexRoutingTable.UNASSIGNED_INFO_NODE_LEFT;
							
							// unassigned secondary shards (yellow)
							ShardRouting replicaShardRouting = new ShardRouting(index, shardId, node.id(), false, 
									Router.this.getShardRoutingState(node2), version, info, EMPTY_RANGE_TOKEN);
							shards.add(replicaShardRouting);
						}
						isrt.add( new IndexShardRoutingTable(new ShardId(index,shardId), shards) );
					}
					if (shardId != 0)
						i++;
				}
				
				if (Router.this.redShards != null) {
					for(DiscoveryNode node : Router.this.redShards.keySet()) {
						int  shardId = (localNode.id().equals(node.id())) ? 0 : i;
						UnassignedInfo info = null;
						if (ShardRoutingState.UNASSIGNED.equals(Router.this.getShardRoutingState(node)))
							info = IndexRoutingTable.UNASSIGNED_INFO_UNAVAILABLE;
						if (node.status() != DiscoveryNodeStatus.ALIVE)
							info = IndexRoutingTable.UNASSIGNED_INFO_NODE_LEFT;
						
						// add one unassigned primary shards (red) for orphan token ranges.
						ShardRouting primaryShardRouting = new ShardRouting(index, shardId, node.id(), true, 
								Router.this.getShardRoutingState(node), version,
								info,
								Router.this.getTokenRanges(Router.this.redShards.get(node)));
						isrt.add( new IndexShardRoutingTable(new ShardId(index,shardId), primaryShardRouting) );
						if (shardId != 0)
							i++;
					}
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("route = {}", isrt);
				}
				return isrt;
			}
    		 
    	}
    };

    
    // utility
    /*
    public static Collection<Range<Token>> sortAndMergeRange(Collection<Range<Token>> ranges) {
        ArrayList<Range<Token>> optimizedRange = new ArrayList<Range<Token>>();
        for (Range<Token> range : ranges) {
            if (range.isWrapAround()) {
                optimizedRange.add(new Range<Token>(new LongToken((Long) range.left.getTokenValue()), new LongToken(Long.MAX_VALUE)));
                optimizedRange.add(new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken((Long) range.right.getTokenValue())));
            } else {
                optimizedRange.add(range);
            }
        }
        CollectionUtil.introSort(optimizedRange);
        if (logger.isTraceEnabled())
        	logger.trace("sort in={} out={}", ranges, optimizedRange);
        int i = 0;
        while (i < optimizedRange.size() - 1) {
            Range<Token> range1 = optimizedRange.get(i);
            Range<Token> range2 = optimizedRange.get(i + 1);
            if (range1.right.equals(range2.left)) {
                optimizedRange.set(i + 1, new Range<Token>(range1.left, range2.right));
                optimizedRange.remove(i);
            } else {
                i++;
            }
        }
        if (logger.isTraceEnabled())
        	logger.trace("merge out={}", optimizedRange);
        return optimizedRange;
    }
    */
}
