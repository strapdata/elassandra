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
package org.elassandra.cluster.routing;

import java.net.InetAddress;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.cassandra.dht.Murmur3Partitioner.LongToken;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.transport.TransportAddress;

import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * return primary ranges of all nodes (and some replica for unreachable nodes).
 * 
 * @author vroyer
 *
 */
public class PrimaryFirstSearchStrategy extends AbstractSearchStrategy {

    public Router newRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
        return new PrimaryFirstRouter(index, ksName, shardStates, clusterState);
    }
    
    public class PrimaryFirstRouter extends Router {
        Route route;
        
        public PrimaryFirstRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
            super(index, ksName, shardStates, clusterState);
            
            if (!StorageService.instance.isJoined()) {
                // temporary fake routing table in order to start local shards before cassandra services.
                Range<Token> ring = new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken(Long.MAX_VALUE));
                this.tokens.add(ring);
                BitSet singletonBitSet = new BitSet(1);
                singletonBitSet.set(0, true);
                this.greenShards.put(localNode, singletonBitSet);
                this.route = new Router.Route() {
                    @Override
                    public Map<DiscoveryNode, BitSet> selectedShards() {
                        return greenShards;
                    }
                    
                };
                return;
            }
            
            // remove replica ranges from bitset of greenShards for available shards.
            for(DiscoveryNode node : greenShards.keySet()) {
                Collection<Range<Token>> primaryRanges = StorageService.instance.getPrimaryRangeForEndpointWithinDC(ksName, node.getInetAddress());
                Range<Token> wrappedRange = null;        
                for(Range<Token> range : primaryRanges) {
                    if (range.isWrapAround()) {
                        wrappedRange = range;
                        break;
                    }
                }
                if (wrappedRange != null) {
                    primaryRanges.remove(wrappedRange);
                    primaryRanges.add( new Range<Token>(new LongToken((Long) wrappedRange.left.getTokenValue()), new LongToken(Long.MAX_VALUE)) );
                    primaryRanges.add( new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken((Long) wrappedRange.right.getTokenValue())));
                }
                // unset all primary range bits on started replica shards.
                for(Range<Token> range : primaryRanges) {
                    int idx = this.tokens.indexOf(range);
                    if (this.rangeToEndpointsMap.get(range) != null) {
                        for(InetAddress replica : this.rangeToEndpointsMap.get(range)) {
                            UUID uuid = StorageService.instance.getHostId(replica);
                            if (uuid != null && !node.uuid().equals(uuid) && ShardRoutingState.STARTED.equals(shardStates.get(uuid))) {
                                DiscoveryNode n = clusterState.nodes().get( uuid.toString() );
                                if (this.greenShards.get(n) != null)
                                    this.greenShards.get(n).set(idx, false);
                            }
                        }
                    }
                }
            }
            
            if (logger.isTraceEnabled())
                logger.trace("index={} keyspace={} greenShards={} yellowShards={} redShards={}", index, ksName, greenShards, yellowShards, redShards);
            
            this.route = new Router.Route() {
                @Override
                public Map<DiscoveryNode, BitSet> selectedShards() {
                    return greenShards;
                }
            };
        }

        @Override
        public Route newRoute(String preference, TransportAddress src) {
            return this.route;
        }

    }
}
