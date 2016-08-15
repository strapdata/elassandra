/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.cassandra.shard;

import java.io.IOException;

import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesLifecycle;

public class CassandraShardStateService extends IndicesLifecycle.Listener {
	ESLogger logger = Loggers.getLogger(CassandraShardStateService.class);
	
	private final ClusterService clusterService;
	private final DiscoveryService discoveryService;
	
	@Inject
	public CassandraShardStateService(ClusterService clusterService, DiscoveryService discoveryService) {
		this.clusterService = clusterService;
		this.discoveryService = discoveryService;
	}
    
    /**
     * Called after the index shard has been started.
     */
    public void afterIndexShardStarted(IndexShard indexShard) {
    	try {
			discoveryService.putShardRoutingState(indexShard.shardId().getIndex(), ShardRoutingState.STARTED);
			updateRoutingTable("shard ["+indexShard.shardId().getIndex()+"][0] started", indexShard.shardId().getIndex());
		} catch (IOException e) {
			logger.error("Unexpected error", e);
		}
    }
    
    /**
     * Called before the index shard gets closed.
     *
     * @param indexShard The index shard
     */
    public void beforeIndexShardClosed(ShardId shardId, @Nullable IndexShard indexShard, Settings indexSettings) {
    	try {
			discoveryService.putShardRoutingState(indexShard.shardId().index().name(), ShardRoutingState.UNASSIGNED);
			updateRoutingTable("shard ["+indexShard.shardId().getIndex()+"][0] closed", indexShard.shardId().getIndex());
		} catch (IOException e) {
			logger.error("Unexpected error", e);
		}
    }

    private void updateRoutingTable(String source, String index) {
        clusterService.submitStateUpdateTask(source,Priority.NORMAL, new ClusterStateUpdateTask() {

            @Override
            public ClusterState execute(ClusterState currentState) {
            	logger.debug("updating routing table [{}]", source);
            	RoutingTable newRoutingTable = RoutingTable.build(clusterService, currentState,index);
                return ClusterState.builder(currentState).incrementVersion().routingTable(newRoutingTable).build();
            }


            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }

        });
    }
    
}
