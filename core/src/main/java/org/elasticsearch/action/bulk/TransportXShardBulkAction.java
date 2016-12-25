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
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.RoutingMissingException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.update.UpdateHelper;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.engine.DocumentAlreadyExistsException;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Performs the index operation.
 */
public class TransportXShardBulkAction extends TransportShardBulkAction {

    @Inject
    public TransportXShardBulkAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                    IndicesService indicesService, ThreadPool threadPool, 
                                    MappingUpdatedAction mappingUpdatedAction, UpdateHelper updateHelper, ActionFilters actionFilters,
                                    IndexNameExpressionResolver indexNameExpressionResolver) {
        super( settings,  transportService,  clusterService,
                 indicesService,  threadPool, 
                 mappingUpdatedAction, updateHelper,  actionFilters,
                 indexNameExpressionResolver);
    }

    @Override
    protected WriteResult shardIndexOperation(BulkShardRequest request, IndexRequest indexRequest, MetaData metaData, IndexShard indexShard, boolean processed) throws Throwable {

        // validate, if routing is required, that we got routing
        MappingMetaData mappingMd = metaData.index(request.index()).mappingOrDefault(indexRequest.type());
        if (mappingMd != null && mappingMd.routing().required()) {
            if (indexRequest.routing() == null) {
                throw new RoutingMissingException(request.index(), indexRequest.type(), indexRequest.id());
            }
        }

        if (!processed) {
            indexRequest.process(metaData, mappingMd, allowIdGeneration, request.index());
        }

        Long writetime = new Long(1);
        clusterService.insertDocument(indicesService, indexRequest, metaData.index(request.index()));

        assert indexRequest.versionType().validateVersionForWrites(indexRequest.version());

        IndexResponse indexResponse = new IndexResponse(request.index(), indexRequest.type(), indexRequest.id(), writetime, true);
        return new WriteResult(indexResponse, null);

        //return executeIndexRequestOnPrimary(request, indexRequest, indexShard);
    }

    protected UpdateResult shardUpdateOperation(MetaData metaData, BulkShardRequest bulkShardRequest, UpdateRequest updateRequest, IndexShard indexShard) {
        UpdateHelper.Result translate = updateHelper.prepare(updateRequest, indexShard);
        switch (translate.operation()) {
            case UPSERT:
            case INDEX:
                IndexRequest indexRequest = translate.action();
                try {
                    WriteResult result = shardIndexOperation(bulkShardRequest, indexRequest, metaData, indexShard, false);
                    return new UpdateResult(translate, indexRequest, result);
                } catch (Throwable t) {
                    t = ExceptionsHelper.unwrapCause(t);
                    boolean retry = false;
                    if (t instanceof VersionConflictEngineException || (t instanceof DocumentAlreadyExistsException && translate.operation() == UpdateHelper.Operation.UPSERT)) {
                        retry = true;
                    }
                    return new UpdateResult(translate, indexRequest, retry, t, null);
                }
            case DELETE:
                DeleteRequest deleteRequest = translate.action();
                
                try {
                    String mappedKeyspace = this.clusterService.state().metaData().index(deleteRequest.index()).keyspace();
                    clusterService.deleteRow((mappedKeyspace == null) ? deleteRequest.index() : mappedKeyspace, deleteRequest.type(), deleteRequest.id(), deleteRequest.consistencyLevel().toCassandraConsistencyLevel());
                    DeleteResponse deleteResponse = new DeleteResponse(deleteRequest.index(), deleteRequest.type(), deleteRequest.id(), deleteRequest.version(), true);
                    WriteResult<DeleteResponse> result = new WriteResult<DeleteResponse>(deleteResponse, null);
                    return new UpdateResult(translate, deleteRequest, result);
                } catch (RequestExecutionException | RequestValidationException | IOException e) {
                    logger.error("[{}].[{}] failed to delete id = [{}]", e, deleteRequest.index(), deleteRequest.type(), deleteRequest.id());
                    return new UpdateResult(translate, deleteRequest, false, e, null);
                }
                
            case NONE:
                UpdateResponse updateResponse = translate.action();
                indexShard.indexingService().noopUpdate(updateRequest.type());
                return new UpdateResult(translate, updateResponse);
            default:
                throw new IllegalStateException("Illegal update operation " + translate.operation());
        }
    }
    
}
