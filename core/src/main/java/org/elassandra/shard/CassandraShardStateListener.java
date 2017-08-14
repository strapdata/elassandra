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

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;

/**
 * Post applied cluster state service to update gossip X1 shards state.
 */
import java.io.IOException;

public class CassandraShardStateListener extends AbstractComponent implements IndexEventListener {
    
    private final ClusterService clusterService;
    
    @Inject
    public CassandraShardStateListener(Settings settings, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
    }
    
    @Override
    public void beforeIndexShardCreated(ShardId shardId, Settings indexSettings) {
        try {
            clusterService.putShardRoutingState(shardId.getIndexName(), ShardRoutingState.INITIALIZING);
        } catch (IOException e) {
            logger.error("Unexpected error", e);
        }
    }
    
    /**
     * Called after the index shard has been started.
     */
    @Override
    public void afterIndexShardStarted(IndexShard indexShard) {
        try {
            clusterService.putShardRoutingState(indexShard.shardId().getIndexName(), ShardRoutingState.STARTED);
            clusterService.submitStateUpdateTask("shard-started-update-routing", new ClusterStateUpdateTask() {
                @Override
                public ClusterState execute(ClusterState currentState) {
                    RoutingTable routingTable = RoutingTable.build(clusterService, currentState);
                    return ClusterState.builder(currentState).incrementVersion().routingTable(routingTable).build();
                }

                @Override
                public void onFailure(String source, Exception e) {
                    logger.error("unexpected failure during [{}]", e, source);
                }
            });
        } catch (IOException e) {
            logger.error("Unexpected error", e);
        }
    }
    
    /**
     * Called before the index shard gets closed.
     *
     * @param indexShard The index shard
     */
    @Override
    public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, Settings indexSettings) {
        try {
            clusterService.putShardRoutingState(indexShard.shardId().getIndexName(), ShardRoutingState.UNASSIGNED);
        } catch (IOException e) {
            logger.error("Unexpected error", e);
        }
    }

}
