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

package org.elassandra.action.admin.indices.restore;

import java.util.ArrayList;
import java.util.List;

import org.apache.cassandra.service.StorageService;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elasticsearch.action.ActionWriteResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.replication.TransportReplicationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 *
 */
public class TransportShardRestoreAction extends TransportReplicationAction<ShardRestoreRequest, ShardRestoreRequest, ActionWriteResponse> {

    public static final String NAME = RestoreAction.NAME + "[s]";

    @Inject
    public TransportShardRestoreAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                       IndicesService indicesService, ThreadPool threadPool,
                                       MappingUpdatedAction mappingUpdatedAction, ActionFilters actionFilters,
                                       IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, NAME, transportService, clusterService, indicesService, threadPool, mappingUpdatedAction,
                actionFilters, indexNameExpressionResolver, ShardRestoreRequest.class, ShardRestoreRequest.class, ThreadPool.Names.FLUSH);
    }

    @Override
    protected ActionWriteResponse newResponseInstance() {
        return new ActionWriteResponse();
    }

    @Override
    protected Tuple<ActionWriteResponse, ShardRestoreRequest> shardOperationOnPrimary(MetaData metaData, ShardRestoreRequest shardRequest) throws Throwable {
        IndexService indexService = indicesService.indexServiceSafe(shardRequest.shardId().getIndex());
        List<String> tables = new ArrayList<String>();
        IndexMetaData indexMetaData = metaData.index(shardRequest.shardId().getIndex());
        for(ObjectCursor<MappingMetaData> it : indexMetaData.getMappings().values()) {
            MappingMetaData mapping = it.value;
            String table = InternalCassandraClusterService.typeToCfName(mapping.type());
            tables.add(table);
        }
        
        // TODO: TRUNCATE + restore SSTables and Lucene files
        
        for(int i=0; i < tables.size(); i++)
            //StorageService.instance.forceKeyspaceCleanup(shardRequest.getRequest().jobs(), indexService.keyspace(), tables.toArray(new String[tables.size()]));
        
        logger.trace("index=[{}] restore request executed on keyspace=[{}] tables={}", shardRequest.shardId().getIndex(), indexService.keyspace(), tables);
        return new Tuple<>(new ActionWriteResponse(), shardRequest);
    }

    @Override
    protected void shardOperationOnReplica(ShardRestoreRequest request) {
    }

    @Override
    protected boolean checkWriteConsistency() {
        return false;
    }

    @Override
    protected ClusterBlockLevel globalBlockLevel() {
        return ClusterBlockLevel.METADATA_WRITE;
    }

    @Override
    protected ClusterBlockLevel indexBlockLevel() {
        return ClusterBlockLevel.METADATA_WRITE;
    }

    @Override
    protected boolean shouldExecuteReplication(Settings settings) {
        return false;
    }
}
