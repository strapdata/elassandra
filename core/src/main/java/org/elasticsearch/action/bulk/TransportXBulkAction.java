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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.DocumentRequest;
import org.elasticsearch.action.RoutingMissingException;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.TransportDeleteAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.action.update.TransportUpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.AtomicArray;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.google.common.collect.Maps;

/**
 *
 */
public class TransportXBulkAction extends TransportBulkAction {

    
    @Inject
    public TransportXBulkAction(Settings settings, ThreadPool threadPool, TransportService transportService, ClusterService clusterService,
                               TransportXShardBulkAction shardBulkAction, TransportCreateIndexAction createIndexAction,
                               ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                               AutoCreateIndex autoCreateIndex) {
        super( settings,  threadPool,  transportService,  clusterService,
                 shardBulkAction,  createIndexAction,
                 actionFilters,  indexNameExpressionResolver,
                 autoCreateIndex);
    }

    protected void executeBulk(final BulkRequest bulkRequest, final long startTime, final ActionListener<BulkResponse> listener, final AtomicArray<BulkItemResponse> responses ) {
        final ClusterState clusterState = clusterService.state();
        // TODO use timeout to wait here if its blocked...
        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.WRITE);

        final ConcreteIndices concreteIndices = new ConcreteIndices(clusterState, indexNameExpressionResolver);
        MetaData metaData = clusterState.metaData();
        for (int i = 0; i < bulkRequest.requests.size(); i++) {
            ActionRequest request = bulkRequest.requests.get(i);
            //the request can only be null because we set it to null in the previous step, so it gets ignored
            if (request == null) {
                continue;
            }
            DocumentRequest documentRequest = (DocumentRequest) request;
            if (addFailureIfIndexIsUnavailable(documentRequest, bulkRequest, responses, i, concreteIndices, metaData)) {
                continue;
            }
            String concreteIndex = concreteIndices.resolveIfAbsent(documentRequest);
            if (request instanceof IndexRequest) {
                IndexRequest indexRequest = (IndexRequest) request;
                MappingMetaData mappingMd = null;
                if (metaData.hasIndex(concreteIndex)) {
                    mappingMd = metaData.index(concreteIndex).mappingOrDefault(indexRequest.type());
                }
                try {
                    indexRequest.process(metaData, mappingMd, allowIdGeneration, concreteIndex);
                } catch (ElasticsearchParseException | RoutingMissingException e) {
                    BulkItemResponse.Failure failure = new BulkItemResponse.Failure(concreteIndex, indexRequest.type(), indexRequest.id(), e);
                    BulkItemResponse bulkItemResponse = new BulkItemResponse(i, "index", failure);
                    responses.set(i, bulkItemResponse);
                    // make sure the request gets never processed again
                    bulkRequest.requests.set(i, null);
                }
            } else if (request instanceof DeleteRequest) {
                try {
                    TransportDeleteAction.resolveAndValidateRouting(metaData, concreteIndex, (DeleteRequest)request);
                } catch(RoutingMissingException e) {
                    BulkItemResponse.Failure failure = new BulkItemResponse.Failure(concreteIndex, documentRequest.type(), documentRequest.id(), e);
                    BulkItemResponse bulkItemResponse = new BulkItemResponse(i, "delete", failure);
                    responses.set(i, bulkItemResponse);
                    // make sure the request gets never processed again
                    bulkRequest.requests.set(i, null);
                }
            } else if (request instanceof UpdateRequest) {
                try {
                    TransportUpdateAction.resolveAndValidateRouting(metaData, concreteIndex, (UpdateRequest)request);
                } catch(RoutingMissingException e) {
                    BulkItemResponse.Failure failure = new BulkItemResponse.Failure(concreteIndex, documentRequest.type(), documentRequest.id(), e);
                    BulkItemResponse bulkItemResponse = new BulkItemResponse(i, "update", failure);
                    responses.set(i, bulkItemResponse);
                    // make sure the request gets never processed again
                    bulkRequest.requests.set(i, null);
                }
            } else {
                throw new AssertionError("request type not supported: [" + request.getClass().getName() + "]");
            }
        }

        // first, go over all the requests and create a ShardId -> Operations mapping
        Map<ShardId, List<BulkItemRequest>> requestsByShard = Maps.newHashMap();

        for (int i = 0; i < bulkRequest.requests.size(); i++) {
            ActionRequest request = bulkRequest.requests.get(i);
            if (request instanceof IndexRequest) {
                IndexRequest indexRequest = (IndexRequest) request;
                String concreteIndex = concreteIndices.getConcreteIndex(indexRequest.index());
                ShardId shardId = clusterService.operationRouting().indexShards(clusterState, concreteIndex, indexRequest.type(), indexRequest.id(), indexRequest.routing()).shardId();
                List<BulkItemRequest> list = requestsByShard.get(shardId);
                if (list == null) {
                    list = new ArrayList<>();
                    requestsByShard.put(shardId, list);
                }
                list.add(new BulkItemRequest(i, request));
            } else if (request instanceof DeleteRequest) {
                DeleteRequest deleteRequest = (DeleteRequest) request;
                String concreteIndex = concreteIndices.getConcreteIndex(deleteRequest.index());
                ShardId shardId = clusterService.operationRouting().indexShards(clusterState, concreteIndex, deleteRequest.type(), deleteRequest.id(), deleteRequest.routing()).shardId();
                List<BulkItemRequest> list = requestsByShard.get(shardId);
                if (list == null) {
                    list = new ArrayList<>();
                    requestsByShard.put(shardId, list);
                }
                list.add(new BulkItemRequest(i, request));
            } else if (request instanceof UpdateRequest) {
                UpdateRequest updateRequest = (UpdateRequest) request;
                String concreteIndex = concreteIndices.getConcreteIndex(updateRequest.index());
                ShardId shardId = clusterService.operationRouting().indexShards(clusterState, concreteIndex, updateRequest.type(), updateRequest.id(), updateRequest.routing()).shardId();
                List<BulkItemRequest> list = requestsByShard.get(shardId);
                if (list == null) {
                    list = new ArrayList<>();
                    requestsByShard.put(shardId, list);
                }
                list.add(new BulkItemRequest(i, request));
            }
        }

        if (requestsByShard.isEmpty()) {
            listener.onResponse(new BulkResponse(responses.toArray(new BulkItemResponse[responses.length()]), buildTookInMillis(startTime)));
            return;
        }

        final AtomicInteger counter = new AtomicInteger(requestsByShard.size());
        for (Map.Entry<ShardId, List<BulkItemRequest>> entry : requestsByShard.entrySet()) {
            final ShardId shardId = entry.getKey();
            final List<BulkItemRequest> requests = entry.getValue();
            BulkShardRequest bulkShardRequest = new BulkShardRequest(bulkRequest, shardId, bulkRequest.refresh(), requests.toArray(new BulkItemRequest[requests.size()]));
            bulkShardRequest.consistencyLevel(bulkRequest.consistencyLevel());
            bulkShardRequest.timeout(bulkRequest.timeout());
            shardBulkAction.execute(bulkShardRequest, new ActionListener<BulkShardResponse>() {
                @Override
                public void onResponse(BulkShardResponse bulkShardResponse) {
                    for (BulkItemResponse bulkItemResponse : bulkShardResponse.getResponses()) {
                        // we may have no response if item failed
                        if (bulkItemResponse.getResponse() != null) {
                            bulkItemResponse.getResponse().setShardInfo(bulkShardResponse.getShardInfo());
                        }
                        responses.set(bulkItemResponse.getItemId(), bulkItemResponse);
                    }
                    if (counter.decrementAndGet() == 0) {
                        finishHim();
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    // create failures for all relevant requests
                    for (BulkItemRequest request : requests) {
                        if (request.request() instanceof IndexRequest) {
                            IndexRequest indexRequest = (IndexRequest) request.request();
                            responses.set(request.id(), new BulkItemResponse(request.id(), indexRequest.opType().toString().toLowerCase(Locale.ENGLISH),
                                    new BulkItemResponse.Failure(concreteIndices.getConcreteIndex(indexRequest.index()), indexRequest.type(), indexRequest.id(), e)));
                        } else if (request.request() instanceof DeleteRequest) {
                            DeleteRequest deleteRequest = (DeleteRequest) request.request();
                            responses.set(request.id(), new BulkItemResponse(request.id(), "delete",
                                    new BulkItemResponse.Failure(concreteIndices.getConcreteIndex(deleteRequest.index()), deleteRequest.type(), deleteRequest.id(), e)));
                        } else if (request.request() instanceof UpdateRequest) {
                            UpdateRequest updateRequest = (UpdateRequest) request.request();
                            responses.set(request.id(), new BulkItemResponse(request.id(), "update",
                                    new BulkItemResponse.Failure(concreteIndices.getConcreteIndex(updateRequest.index()), updateRequest.type(), updateRequest.id(), e)));
                        }
                    }
                    if (counter.decrementAndGet() == 0) {
                        finishHim();
                    }
                }

                private void finishHim() {
                    listener.onResponse(new BulkResponse(responses.toArray(new BulkItemResponse[responses.length()]), buildTookInMillis(startTime)));
                }
            });
        }
    }

}
