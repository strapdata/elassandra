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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.transport.TransportAddress;

import com.google.common.collect.Lists;

/**
 * For each newRoute(), returns all local ranges and randomly pickup ranges from available nodes (may be unbalanced).
 * 
 * @author vroyer
 *
 */
public class RandomSearchStrategy extends AbstractSearchStrategy {
    
    public class RandomRouter extends Router {
        Random rnd = new Random();
        
        public RandomRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
            super(index, ksName, shardStates, clusterState);
        }
        
        @Override
        public Route newRoute(@Nullable String preference, TransportAddress src) {
            DiscoveryNode pivotNode = localNode;
            if (this.greenShards.get(pivotNode) == null && greenShards.size() > 0) {
                pivotNode = greenShards.keySet().iterator().next();
            }
            BitSet pivotBitset = this.greenShards.get(pivotNode);
            final Map<DiscoveryNode, BitSet> selectedShards = new HashMap<DiscoveryNode, BitSet>();
            selectedShards.put(pivotNode, pivotBitset);
            
            List<DiscoveryNode> randomAvailableNodes = Lists.newArrayList(greenShards.keySet());
            randomAvailableNodes.remove(pivotNode);
            Collections.shuffle(randomAvailableNodes, rnd);
            
            
            BitSet coverBitmap = (BitSet)pivotBitset.clone();
            int i = 0;
            while (coverBitmap.cardinality() != tokens.size() && i < tokens.size()) {
                int x = coverBitmap.nextClearBit(i);
                DiscoveryNode choice = null;
                for(Iterator<DiscoveryNode> it = randomAvailableNodes.iterator(); it.hasNext(); ) {
                    DiscoveryNode node = it.next();
                    if (this.greenShards.get(node).get(x)) {
                        // choose the first (could choose the wider token_range, or the one having the less dropped mutations)
                        if (choice == null)
                            choice = node;
                        it.remove();
                    }
                }
                if (choice != null) {
                    BitSet choiceBitset = this.greenShards.get(choice);
                    choiceBitset.andNot(coverBitmap);
                    selectedShards.put(choice, choiceBitset);
                    coverBitmap.or(choiceBitset);
                } 
                i++;
            }
        
            return new Route()  {
                @Override
                public Map<DiscoveryNode, BitSet> selectedShards() {
                    return selectedShards;
                }
            };
        }

    }

    @Override
    public Router newRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
        return new RandomRouter(index, ksName, shardStates, clusterState);
    }
    
}
