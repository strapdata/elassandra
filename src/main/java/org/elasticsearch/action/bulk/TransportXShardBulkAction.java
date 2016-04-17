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

package org.elasticsearch.action.bulk;

import java.io.IOException;

import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.RoutingMissingException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.update.UpdateHelper;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Performs the index operation.
 */
public class TransportXShardBulkAction extends TransportShardBulkAction {

    
    public static final String ACTION_NAME = BulkAction.NAME + "[s]";

   
    @Inject
    public TransportXShardBulkAction(Settings settings, TransportService transportService, ClusterService clusterService,
            IndicesService indicesService, ThreadPool threadPool, ShardStateAction shardStateAction,
            MappingUpdatedAction mappingUpdatedAction, UpdateHelper updateHelper, ActionFilters actionFilters,
            IndexNameExpressionResolver indexNameExpressionResolver) {
        super( settings,  transportService,  clusterService,
                 indicesService,  threadPool,  shardStateAction,
                 mappingUpdatedAction,  updateHelper,  actionFilters,
                 indexNameExpressionResolver);
    }

    @Override
    protected void shardOperationOnReplica(ShardId shardId, BulkShardRequest request) {
        
    }

    @Override
    protected WriteResult shardIndexOperation(BulkShardRequest request, IndexRequest indexRequest, ClusterState clusterState, IndexShard indexShard, boolean processed) throws Throwable {

        // validate, if routing is required, that we got routing
        MappingMetaData mappingMd = clusterState.metaData().index(request.index()).mappingOrDefault(indexRequest.type());
        if (mappingMd != null && mappingMd.routing().required()) {
            if (indexRequest.routing() == null) {
                throw new RoutingMissingException(request.index(), indexRequest.type(), indexRequest.id());
            }
        }

        if (!processed) {
            indexRequest.process(clusterState.metaData(), mappingMd, allowIdGeneration, request.index());
        }

        Long writetime = new Long(1);
        clusterService.insertDocument(indicesService, indexRequest, clusterState, indexRequest.timestamp());

        assert indexRequest.versionType().validateVersionForWrites(indexRequest.version());

        IndexResponse indexResponse = new IndexResponse(request.index(), indexRequest.type(), indexRequest.id(), writetime, true);
        return new WriteResult(indexResponse, null);

        //return executeIndexRequestOnPrimary(request, indexRequest, indexShard);
    }

    @Override
    protected WriteResult<DeleteResponse> shardDeleteOperation(BulkShardRequest request, DeleteRequest deleteRequest, IndexShard indexShard)  {
        Engine.Delete delete = indexShard.prepareDelete(deleteRequest.type(), deleteRequest.id(), deleteRequest.version(), deleteRequest.versionType(), Engine.Operation.Origin.PRIMARY);
        indexShard.delete(delete);
        // update the request with the version so it will go to the replicas
        deleteRequest.versionType(delete.versionType().versionTypeForReplicationAndRecovery());
        deleteRequest.version(delete.version());

        assert deleteRequest.versionType().validateVersionForWrites(deleteRequest.version());

        try {
            String mappedKeyspace = this.clusterService.state().metaData().index(request.index()).keyspace();
            clusterService.deleteRow((mappedKeyspace == null) ? request.index() : mappedKeyspace, deleteRequest.type(), deleteRequest.id(), request.consistencyLevel().toCassandraConsistencyLevel());
            DeleteResponse deleteResponse = new DeleteResponse(request.index(), deleteRequest.type(), deleteRequest.id(), delete.version(), true);
            return new WriteResult(deleteResponse, null);
        } catch (RequestExecutionException | RequestValidationException | IOException e) {
            logger.error("[{}].[{}] failed to delete id = [{}]", e, request.index(), deleteRequest.type(), deleteRequest.id());
            throw new ElasticsearchException(e.getMessage(),e);
        }
    }
}
