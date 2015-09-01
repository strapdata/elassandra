/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cluster.routing.allocation.allocator;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

import org.elasticsearch.cluster.routing.MutableShardRouting;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.allocation.FailedRerouteAllocation;
import org.elasticsearch.cluster.routing.allocation.RoutingAllocation;
import org.elasticsearch.cluster.routing.allocation.StartedRerouteAllocation;
import org.elasticsearch.cluster.routing.allocation.decider.AllocationDecider;
import org.elasticsearch.cluster.routing.allocation.decider.Decision;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.elasticsearch.cluster.routing.ShardRoutingState.INITIALIZING;
import static org.elasticsearch.cluster.routing.ShardRoutingState.STARTED;

/**
 * A {@link ShardsAllocator} that tries to balance shards across nodes in the
 * cluster such that each node holds approximatly the same number of shards. The
 * allocations algorithm operates on a cluster ie. is index-agnostic. While the
 * number of shards per node might be balanced across the cluster a single node
 * can hold mulitple shards from a single index such that the shard of an index
 * are not necessarily balanced across nodes. Yet, due to high-level
 * {@link AllocationDecider decisions} multiple instances of the same shard
 * won't be allocated on the same node.
 * <p>
 * During {@link #rebalance(RoutingAllocation) re-balancing} the allocator takes
 * shards from the <tt>most busy</tt> nodes and tries to relocate the shards to
 * the least busy node until the number of shards per node are equal for all
 * nodes in the cluster or until no shards can be relocated anymore.
 * </p>
 */
public class CassandraShardsAllocator extends AbstractComponent implements ShardsAllocator {

    @Inject
    public CassandraShardsAllocator(Settings settings) {
        super(settings);
    }

    /**
     * Applies changes on started nodes based on the implemented algorithm. For example if a 
     * shard has changed to {@link ShardRoutingState#STARTED} from {@link ShardRoutingState#RELOCATING} 
     * this allocator might apply some cleanups on the node that used to hold the shard.
     * @param allocation all started {@link ShardRouting shards}
     */
    @Override
    public void applyStartedShards(StartedRerouteAllocation allocation) { /* ONLY FOR GATEWAYS */
    }

    /**
     * Applies changes on failed nodes based on the implemented algorithm. 
     * @param allocation all failed {@link ShardRouting shards}
     */
    @Override
    public void applyFailedShards(FailedRerouteAllocation allocation) { /* ONLY FOR GATEWAYS */
    }

    /**
     * Assign all unassigned shards to nodes 
     * 
     * @param allocation current node allocation
     * @return <code>true</code> if the allocation has changed, otherwise <code>false</code>
     */
    @Override
    public boolean allocateUnassigned(RoutingAllocation allocation) {
        boolean changed = false;
        RoutingNodes routingNodes = allocation.routingNodes();
        /* 
         * 1. order nodes by the number of shards allocated on them least one first (this takes relocation into account)
         *    ie. if a shard is relocating the target nodes shard count is incremented.
         * 2. iterate over the unassigned shards
         *    2a. find the least busy node in the cluster that allows allocation for the current unassigned shard
         *    2b. if a node is found add the shard to the node and remove it from the unassigned shards
         * 3. iterate over the remaining unassigned shards and try to allocate them on next possible node
         */
        // order nodes by number of shards (asc) 
        //RoutingNode[] nodes = sortedNodesLeastToHigh(allocation);

        Iterator<MutableShardRouting> unassignedIterator = routingNodes.unassigned().iterator();

        // allocate all the unassigned shards above to alives nodes.
        for (Iterator<MutableShardRouting> it = routingNodes.unassigned().iterator(); it.hasNext();) {
            MutableShardRouting shard = it.next();
            // go over the nodes and try and allocate the remaining ones
            for (RoutingNode routingNode : allocation.routingNodes()) {
                changed = true;
                allocation.routingNodes().assign(shard, routingNode.nodeId());
                it.remove();
                break;
            }
        }
        return changed;

    }

    /**
     * Rebalancing number of shards on all nodes
     *   
     * @param allocation current node allocation
     * @return <code>true</code> if the allocation has changed, otherwise <code>false</code>
     */
    @Override
    public boolean rebalance(RoutingAllocation allocation) {
        return false;
    }

    /**
     * Moves a shard from the given node to other node.
     * 
     * @param shardRouting the shard to move
     * @param node A node containing the shard
     * @param allocation current node allocation
     * @return <code>true</code> if the allocation has changed, otherwise <code>false</code>
     */
    @Override
    public boolean move(MutableShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        return false;
    }

}
