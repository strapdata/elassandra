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

package org.elasticsearch.action.update;

import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.TransportDeleteAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.TransportIndexAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 */
public class TransportXUpdateAction extends TransportUpdateAction {

    private final ClusterService clusterService;
    private final IndicesService indicesService;
    private final UpdateHelper updateHelper;
    
    @Inject
    public TransportXUpdateAction(Settings settings, ThreadPool threadPool, ClusterService clusterService, TransportService transportService,
                                 TransportIndexAction indexAction, TransportDeleteAction deleteAction, TransportCreateIndexAction createIndexAction,
                                 UpdateHelper updateHelper, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                                 IndicesService indicesService, AutoCreateIndex autoCreateIndex) {
        super( settings,  threadPool,  clusterService,  transportService,
                 indexAction,  deleteAction,  createIndexAction,
                 updateHelper,  actionFilters,  indexNameExpressionResolver,
                 indicesService,  autoCreateIndex);
        this.clusterService = clusterService;
        this.indicesService = indicesService;
        this.updateHelper = updateHelper;
    }

    @Override
    protected void shardOperation(final UpdateRequest request, final ActionListener<UpdateResponse> listener, final int retryCount) {
        IndexService indexService = indicesService.indexServiceSafe(request.concreteIndex());
        IndexShard indexShard = indexService.shardSafe(request.shardId());
        UpdateResponse update;
        WriteConsistencyLevel wcl = (request.consistencyLevel() == WriteConsistencyLevel.DEFAULT) ? WriteConsistencyLevel.ALL: request.consistencyLevel();
        try {
            final UpdateHelper.Result result = updateHelper.prepare(request, indexShard);
            switch (result.operation()) {
                case UPSERT:
                    IndexRequest upsertRequest = new IndexRequest((IndexRequest)result.action(), request);
                    upsertRequest.consistencyLevel(wcl);
                    clusterService.updateDocument(indicesService, upsertRequest, clusterService.state().metaData().index(request.index()));
                    update = new UpdateResponse(request.index(), request.type(), request.id(), new Long(1), true);
                    update.setShardInfo(clusterService.shardInfo(request.index(), wcl.toCassandraConsistencyLevel()));
                    if (request.fields() != null && request.fields().length > 0) {
                        final BytesReference upsertSourceBytes = upsertRequest.source();
                        Tuple<XContentType, Map<String, Object>> sourceAndContent = XContentHelper.convertToMap(upsertSourceBytes, true);
                        update.setGetResult(updateHelper.extractGetResult(request, request.concreteIndex(), 1L, sourceAndContent.v2(), sourceAndContent.v1(), upsertSourceBytes));
                    } else {
                        update.setGetResult(null);
                    }
                    listener.onResponse(update);
                    break;
                case INDEX:
                    IndexRequest indexRequest = new IndexRequest((IndexRequest)result.action(), request);
                    indexRequest.consistencyLevel(wcl);
                    // we fetch it from the index request so we don't generate the bytes twice, its already done in the index request
                    clusterService.insertDocument(indicesService, indexRequest, clusterService.state().metaData().index(indexRequest.index()));
                    final BytesReference indexSourceBytes = indexRequest.source();
                    update = new UpdateResponse(request.index(), request.type(), request.id(), new Long(1), true);
                    update.setShardInfo(clusterService.shardInfo(request.index(), wcl.toCassandraConsistencyLevel()));
                    update.setGetResult(updateHelper.extractGetResult(request, request.concreteIndex(), 1L, result.updatedSourceAsMap(), result.updateSourceContentType(), indexSourceBytes));
                    listener.onResponse(update);
                    break;
                case DELETE:
                    DeleteRequest deleteRequest = new DeleteRequest((DeleteRequest)result.action(), request);
                    deleteRequest.consistencyLevel(wcl);
                    clusterService.deleteRow(request.index(), request.type(), request.id(), wcl.toCassandraConsistencyLevel());
                    update = new UpdateResponse(request.index(), request.type(), request.id(), new Long(1), true);
                    update.setShardInfo(clusterService.shardInfo(request.index(), wcl.toCassandraConsistencyLevel()));
                    update.setGetResult(updateHelper.extractGetResult(request, request.concreteIndex(), 1L, result.updatedSourceAsMap(), result.updateSourceContentType(), null));
                    listener.onResponse(update);
                    break;
                case NONE:
                    update = result.action();
                    IndexService indexServiceOrNull = indicesService.indexService(request.concreteIndex());
                    if (indexServiceOrNull !=  null) {
                        IndexShard shard = indexService.shard(request.shardId());
                        if (shard != null) {
                            shard.indexingService().noopUpdate(request.type());
                        }
                    }
                    listener.onResponse(update);
                    break;
                default:
                    throw new IllegalStateException("Illegal operation " + result.operation());
            }
        } catch(Exception e) {
            listener.onFailure(e);
        }
    }
    
}