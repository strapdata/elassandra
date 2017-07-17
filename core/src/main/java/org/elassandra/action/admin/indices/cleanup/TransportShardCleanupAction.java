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

package org.elassandra.action.admin.indices.cleanup;

import com.carrotsearch.hppc.cursors.ObjectCursor;

import org.apache.cassandra.service.StorageService;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.support.replication.TransportReplicationAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class TransportShardCleanupAction extends TransportReplicationAction<ShardCleanupRequest, ShardCleanupRequest, ReplicationResponse> {

    public static final String NAME = CleanupAction.NAME + "[s]";

    @Inject
    public TransportShardCleanupAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                       IndicesService indicesService, ThreadPool threadPool,
                                       ActionFilters actionFilters,
                                       IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, NAME, transportService, clusterService, indicesService, threadPool,
                actionFilters, indexNameExpressionResolver, ShardCleanupRequest::new, ShardCleanupRequest::new, ThreadPool.Names.FLUSH);
    }

    @Override
    protected ReplicationResponse newResponseInstance() {
        return new ReplicationResponse();
    }

    @Override
    protected PrimaryResult shardOperationOnPrimary(ShardCleanupRequest shardRequest, IndexShard primary) throws IOException, ExecutionException, InterruptedException {
        IndexService indexService = indicesService.indexServiceSafe(shardRequest.shardId().getIndex());
        List<String> tables = new ArrayList<String>();
        MetaData metaData = clusterService.state().metaData();
        IndexMetaData indexMetaData = metaData.index(shardRequest.shardId().getIndex());
        for(ObjectCursor<MappingMetaData> it : indexMetaData.getMappings().values()) {
            MappingMetaData mapping = it.value;
            String table = org.elasticsearch.cluster.service.ClusterService.typeToCfName(mapping.type());
            tables.add(table);
        }
        
        // Cassandra flush and rebuild_index for all mapped tables.
        
        for(int i=0; i < tables.size(); i++)
            StorageService.instance.forceKeyspaceCleanup(shardRequest.getRequest().jobs(), indexService.keyspace(), tables.toArray(new String[tables.size()]));
        
        logger.trace("index=[{}] cleanup request executed on keyspace=[{}] tables={} with jobs={}", shardRequest.shardId().getIndex(), indexService.keyspace(), tables, shardRequest.getRequest().jobs());
        return new PrimaryResult(shardRequest, new ReplicationResponse());
    }

    @Override
    protected ReplicaResult shardOperationOnReplica(ShardCleanupRequest request, IndexShard replica) {
        return new ReplicaResult();
    }

    @Override
    protected boolean shouldExecuteReplication(IndexMetaData indexMetaData) {
        return false;
    }
}
