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
package org.elassandra.shard;

import com.carrotsearch.hppc.cursors.ObjectCursor;

import org.elassandra.cluster.service.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gateway.GatewayService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Block cassandra until all local shards are started before playing commit logs or bootstrapping.
 * @author vroyer
 *
 */
public class CassandraShardStartedBarrier extends AbstractComponent  {
    
    final CountDownLatch latch = new CountDownLatch(1);
    final ClusterService clusterService;
    
    public CassandraShardStartedBarrier(Settings settings, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
    }

    /**
     * Block until all local shards are started.
     */
    public void blockUntilShardsStarted() {
        try {
            logger.debug("Waiting latch={}", latch.getCount());
            if (latch.await(600, TimeUnit.SECONDS))
                logger.debug("All local shards ready to index.");
            else 
                logger.error("Some local shards not ready to index, clusterState = {}", clusterService.state());
        } catch (InterruptedException e) {
            logger.error("Interrupred before all local shards are ready to index", e);
        }
        
    }

    public boolean isReadyToIndex(ClusterState clusterState) {
        boolean readyToIndex;
        if (clusterState.blocks().hasGlobalBlock(GatewayService.STATE_NOT_RECOVERED_BLOCK)) {
            readyToIndex = false;
        } else {
            readyToIndex = true;
            DiscoveryNode localNode = clusterState.nodes().getLocalNode();
            RoutingTable routingTable = clusterState.routingTable();
            for(ObjectCursor<IndexMetaData> cursor : clusterState.metaData().indices().values()) {
                IndexMetaData indexMetaData = cursor.value;
                IndexRoutingTable indexRoutingTable;
                ShardRouting shardRouting;
                if (indexMetaData.getState() == IndexMetaData.State.OPEN &&
                   ( (indexRoutingTable=routingTable.index(indexMetaData.getIndex())) == null ||
                         (shardRouting = indexRoutingTable.primaryShardRouting(localNode.getId())) == null ||
                         !shardRouting.started() 
                   )) {
                    readyToIndex = false;
                    break;
                }
            }
        }
        if (readyToIndex && latch.getCount() > 0) {
            clusterService.removeShardStartedBarrier();
            latch.countDown();
        }
        logger.debug("readyToIndex={} latch={} state={}",readyToIndex, latch.getCount(), clusterState);
        return readyToIndex;
    }
    
}
