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

import org.elasticsearch.cluster.node.DiscoveryNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Sub query nodes in the same rack if possible, from the coordinator's rack if its shard is green.
 * 
 * @author vroyer
 *
 */
public class RackAwareSearchStrategy extends RandomSearchStrategy {
    
    @Override
    public void sortNodes(DiscoveryNode pivotNode, List<DiscoveryNode> greenNodes, Random random) {
        Collections.sort(greenNodes, new RackComparator(pivotNode.getAttributes().get("rack"), random));
    }
    
    
    static class RackComparator implements Comparator<DiscoveryNode> {
        String rack;
        Random random;
        
        public RackComparator(String rack, Random random) {
            this.rack = rack;
            this.random = random;
        }
        
        @Override
        public int compare(DiscoveryNode o1, DiscoveryNode o2) {
            if (rack.equals(o1.getAttributes().get("rack"))) {
                return (rack.equals(o2.getAttributes().get("rack"))) 
                        ? random.nextInt(2)*2  -1 // load balance subqueries in the rack
                        : -1;
            }
            if (rack.equals(o2.getAttributes().get("rack"))) {
                return 1;
            }
            return random.nextInt(2)*2  -1; // random node outside the rack
        } 
    }
}
