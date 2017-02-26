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

import java.util.Collections;
import java.util.Map;

import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.RoutingMissingException;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.TransportDeleteAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.TransportIndexAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.action.support.TransportActions;
import org.elasticsearch.action.support.single.instance.TransportInstanceSingleOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.routing.PlainShardIterator;
import org.elasticsearch.cluster.routing.ShardIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 */
public class TransportUpdateAction extends TransportInstanceSingleOperationAction<UpdateRequest, UpdateResponse> {

    protected final TransportDeleteAction deleteAction;
    protected final TransportIndexAction indexAction;
    protected final AutoCreateIndex autoCreateIndex;
    protected final TransportCreateIndexAction createIndexAction;
    protected final UpdateHelper updateHelper;
    protected final IndicesService indicesService;

    @Inject
    public TransportUpdateAction(Settings settings, ThreadPool threadPool, ClusterService clusterService, TransportService transportService,
                                 TransportIndexAction indexAction, TransportDeleteAction deleteAction, TransportCreateIndexAction createIndexAction,
                                 UpdateHelper updateHelper, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                                 IndicesService indicesService, AutoCreateIndex autoCreateIndex) {
        super(settings, UpdateAction.NAME, threadPool, clusterService, transportService, actionFilters, indexNameExpressionResolver, UpdateRequest.class);
        this.indexAction = indexAction;
        this.deleteAction = deleteAction;
        this.createIndexAction = createIndexAction;
        this.updateHelper = updateHelper;
        this.indicesService = indicesService;
        this.autoCreateIndex = autoCreateIndex;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.INDEX;
    }

    @Override
    protected UpdateResponse newResponse() {
        return new UpdateResponse();
    }

    @Override
    protected boolean retryOnFailure(Throwable e) {
        return TransportActions.isShardNotAvailableException(e);
    }

    @Override
    protected void resolveRequest(ClusterState state, UpdateRequest request) {
        resolveAndValidateRouting(state.metaData(), request.concreteIndex(), request);
    }

    public static void resolveAndValidateRouting(MetaData metaData, String concreteIndex, UpdateRequest request) {
        request.routing((metaData.resolveIndexRouting(request.routing(), request.index())));
        // Fail fast on the node that received the request, rather than failing when translating on the index or delete request.
        if (request.routing() == null && metaData.routingRequired(concreteIndex, request.type())) {
            throw new RoutingMissingException(concreteIndex, request.type(), request.id());
        }
    }

    @Override
    protected void doExecute(final UpdateRequest request, final ActionListener<UpdateResponse> listener) {
        // if we don't have a master, we don't have metadata, that's fine, let it find a master using create index API
        if (autoCreateIndex.shouldAutoCreate(request.index(), clusterService.state())) {
            createIndexAction.execute(new CreateIndexRequest(request).index(request.index()).cause("auto(update api)").masterNodeTimeout(request.timeout()), new ActionListener<CreateIndexResponse>() {
                @Override
                public void onResponse(CreateIndexResponse result) {
                    innerExecute(request, listener);
                }

                @Override
                public void onFailure(Throwable e) {
                    if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {
                        // we have the index, do it
                        try {
                            innerExecute(request, listener);
                        } catch (Throwable e1) {
                            listener.onFailure(e1);
                        }
                    } else {
                        listener.onFailure(e);
                    }
                }
            });
        } else {
            innerExecute(request, listener);
        }
    }

    private void innerExecute(final UpdateRequest request, final ActionListener<UpdateResponse> listener) {
        super.doExecute(request, listener);
    }

    @Override
    protected ShardIterator shards(ClusterState clusterState, UpdateRequest request) {
        if (request.shardId() != -1) {
            return clusterState.routingTable().index(request.concreteIndex()).shard(request.shardId()).primaryShardIt();
        }
        ShardIterator shardIterator = clusterService.operationRouting()
                .indexShards(clusterState, request.concreteIndex(), request.type(), request.id(), request.routing());
        ShardRouting shard;
        while ((shard = shardIterator.nextOrNull()) != null) {
            if (shard.primary()) {
                return new PlainShardIterator(shardIterator.shardId(), Collections.singletonList(shard));
            }
        }
        return new PlainShardIterator(shardIterator.shardId(), Collections.<ShardRouting>emptyList());
    }

    @Override
    protected void shardOperation(final UpdateRequest request, final ActionListener<UpdateResponse> listener) {
        shardOperation(request, listener, 0);
    }

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
