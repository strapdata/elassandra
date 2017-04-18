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

/**
 * Post applied cluster state service to update gossip X1 shards state.
 */
import java.io.IOException;

import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesLifecycle;

public class CassandraShardStateListener extends IndicesLifecycle.Listener {
    ESLogger logger = Loggers.getLogger(CassandraShardStateListener.class);
    
    private final ClusterService clusterService;
    private final DiscoveryService discoveryService;
    
    @Inject
    public CassandraShardStateListener(ClusterService clusterService, DiscoveryService discoveryService) {
        this.clusterService = clusterService;
        this.discoveryService = discoveryService;
    }
    
    @Override
    public void beforeIndexShardCreated(ShardId shardId, Settings indexSettings) {
        try {
            discoveryService.putShardRoutingState(shardId.getIndex(), ShardRoutingState.INITIALIZING);
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
            discoveryService.putShardRoutingState(indexShard.shardId().getIndex(), ShardRoutingState.STARTED);
            clusterService.submitStateUpdateTask("shard-started-update-routing", new ClusterStateUpdateTask() {
                @Override
                public ClusterState execute(ClusterState currentState) {
                    return ClusterState.builder(currentState).incrementVersion().build();
                }

                @Override
                public void onFailure(String source, Throwable t) {
                    logger.error("unexpected failure during [{}]", t, source);
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
            discoveryService.putShardRoutingState(indexShard.shardId().index().name(), ShardRoutingState.UNASSIGNED);
        } catch (IOException e) {
            logger.error("Unexpected error", e);
        }
    }

}
