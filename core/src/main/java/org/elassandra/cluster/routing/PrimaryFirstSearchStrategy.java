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

import java.net.InetAddress;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.cassandra.dht.Murmur3Partitioner.LongToken;
import org.apache.cassandra.db.Keyspace;
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
            super(index, ksName, shardStates, clusterState, false);
            
            if (!StorageService.instance.isJoined() || !Keyspace.isInitialized()) {
                // temporary fake routing table in order to start local shards before cassandra services.
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
            
            if (logger.isTraceEnabled())
                logger.trace("index={} keyspace={} greenShards={} yellowShards={} redShards={}", index, ksName, greenShards, yellowShards, redShards);
            
            this.route = new Router.Route() {
                @Override
                public Map<DiscoveryNode, BitSet> selectedShards() {
                    return greenShards;
                }
            };
        }
        
        private void clearReplicaRange(InetAddress endpoint, int tokenIndex, Token token, ClusterState clusterState) {
            for(DiscoveryNode node : this.tokenToNodes.get(token)) {
                if (this.greenShards.get(node) != null) {
                    if (logger.isTraceEnabled())
                        logger.trace("clear bit={} for token={} node={}", tokenIndex, token, node);
                    this.greenShards.get(node).set(tokenIndex, false);
                } else {
                    if (logger.isTraceEnabled())
                        logger.trace("uuid={} for replica={} node found", node.uuid(), node.getInetAddress());
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
