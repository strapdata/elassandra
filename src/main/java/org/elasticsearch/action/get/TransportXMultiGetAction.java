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

package org.elasticsearch.action.get;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.AtomicArray;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportXMultiGetAction extends HandledTransportAction<XMultiGetRequest, XMultiGetResponse> {

    private final ClusterService clusterService;

    private final TransportShardXMultiGetAction shardAction;

    @Inject
    public TransportXMultiGetAction(Settings settings, ThreadPool threadPool, TransportService transportService, ClusterService clusterService, TransportShardXMultiGetAction shardAction, ActionFilters actionFilters) {
        super(settings, XMultiGetAction.NAME, threadPool, transportService, actionFilters);
        this.clusterService = clusterService;
        this.shardAction = shardAction;
    }

    @Override
    public XMultiGetRequest newRequestInstance(){
        return new XMultiGetRequest();
    }

    @Override
    protected void doExecute(final XMultiGetRequest request, final ActionListener<XMultiGetResponse> listener) {
        ClusterState clusterState = clusterService.state();

        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

        final AtomicArray<XMultiGetItemResponse> responses = new AtomicArray<>(request.items.size());

        Map<ShardId, XMultiGetShardRequest> shardRequests = new HashMap<>();
        for (int i = 0; i < request.items.size(); i++) {
            XMultiGetRequest.Item item = request.items.get(i);
            if (!clusterState.metaData().hasConcreteIndex(item.index())) {
                responses.set(i, new XMultiGetItemResponse(null, new XMultiGetResponse.Failure(item.index(), item.type(), item.id(), "[" + item.index() + "] missing")));
                continue;
            }
            item.routing(clusterState.metaData().resolveIndexRouting(item.routing(), item.index()));
            String concreteSingleIndex = clusterState.metaData().concreteSingleIndex(item.index(), item.indicesOptions());
            if (item.routing() == null && clusterState.getMetaData().routingRequired(concreteSingleIndex, item.type())) {
                responses.set(i, new XMultiGetItemResponse(null, new XMultiGetResponse.Failure(concreteSingleIndex, item.type(), item.id(),
                        "routing is required for [" + concreteSingleIndex + "]/[" + item.type() + "]/[" + item.id() + "]")));
                continue;
            }
            ShardId shardId = clusterService.operationRouting()
                    .getShards(clusterState, concreteSingleIndex, item.type(), item.id(), item.routing(), null).shardId();
            XMultiGetShardRequest shardRequest = shardRequests.get(shardId);
            if (shardRequest == null) {
                shardRequest = new XMultiGetShardRequest(request, shardId.index().name(), shardId.id());
                shardRequests.put(shardId, shardRequest);
            }
            shardRequest.add(i, item);
        }

        if (shardRequests.size() == 0) {
            // only failures..
            listener.onResponse(new XMultiGetResponse(responses.toArray(new XMultiGetItemResponse[responses.length()])));
        }

        final AtomicInteger counter = new AtomicInteger(shardRequests.size());

        for (final XMultiGetShardRequest shardRequest : shardRequests.values()) {
            shardAction.execute(shardRequest, new ActionListener<XMultiGetShardResponse>() {
                @Override
                public void onResponse(XMultiGetShardResponse response) {
                    for (int i = 0; i < response.locations.size(); i++) {
                        responses.set(response.locations.get(i), new XMultiGetItemResponse(response.responses.get(i), response.failures.get(i)));
                    }
                    if (counter.decrementAndGet() == 0) {
                        finishHim();
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    // create failures for all relevant requests
                    String message = ExceptionsHelper.detailedMessage(e);
                    for (int i = 0; i < shardRequest.locations.size(); i++) {
                        XMultiGetRequest.Item item = shardRequest.items.get(i);
                        responses.set(shardRequest.locations.get(i), new XMultiGetItemResponse(null,
                                new XMultiGetResponse.Failure(shardRequest.index(), item.type(), item.id(), message)));
                    }
                    if (counter.decrementAndGet() == 0) {
                        finishHim();
                    }
                }

                private void finishHim() {
                    listener.onResponse(new XMultiGetResponse(responses.toArray(new XMultiGetItemResponse[responses.length()])));
                }
            });
        }
    }
}
