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

package org.elasticsearch.action.index;

import java.util.Collections;

import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaDataCreateIndexService;
import org.elasticsearch.cluster.routing.PlainShardIterator;
import org.elasticsearch.cluster.routing.ShardIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Performs the index operation.
 * <p/>
 * <p>Allows for the following settings:
 * <ul>
 * <li><b>autoCreateIndex</b>: When set to <tt>true</tt>, will automatically create an index if one does not exists.
 * Defaults to <tt>true</tt>.
 * <li><b>allowIdGeneration</b>: If the id is set not, should it be generated. Defaults to <tt>true</tt>.
 * </ul>
 */
public class TransportXIndexAction extends TransportIndexAction {

    private final AutoCreateIndex autoCreateIndex;
    private final boolean allowIdGeneration;
    //private final TransportCreateIndexAction createIndexAction;
    private final MetaDataCreateIndexService createIndexService;
    
    private final ClusterService clusterService;
    
    @Inject
    public TransportXIndexAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                IndicesService indicesService, ThreadPool threadPool,
                                TransportCreateIndexAction createIndexAction, MappingUpdatedAction mappingUpdatedAction,
                                ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                                AutoCreateIndex autoCreateIndex, MetaDataCreateIndexService createIndexService) {
        super( settings,  transportService,  clusterService,
                 indicesService,  threadPool,
                 createIndexAction,  mappingUpdatedAction,
                 actionFilters,  indexNameExpressionResolver,
                 autoCreateIndex);
        this.createIndexService = createIndexService;
        this.autoCreateIndex = autoCreateIndex;
        this.allowIdGeneration = settings.getAsBoolean("action.allow_id_generation", true);
        this.clusterService = clusterService;
    }

    /**
     * Fake shard iterator to execute locally only for elassandra.
     */
    @Override
    protected ShardIterator shards(ClusterState clusterState, InternalRequest request) {
    	ShardRouting primaryShardRouting  = new ShardRouting(request.concreteIndex(), 0, clusterService.localNode().id(), true, ShardRoutingState.STARTED);
    	return new PlainShardIterator(primaryShardRouting.shardId(), Collections.singletonList(primaryShardRouting));
    }
    
    @Override
    protected Tuple<IndexResponse, IndexRequest> shardOperationOnPrimary(ClusterState clusterState, PrimaryOperationRequest shardRequest) throws Throwable {
        final IndexRequest request = shardRequest.request;

        Long version = new Long(1);
        IndexResponse response;
        clusterService.insertDocument(indicesService, request, clusterState, request.timestamp());
        request.versionType(request.versionType().versionTypeForReplicationAndRecovery());
        response = new IndexResponse(shardRequest.shardId.getIndex(), request.type(), request.id(), version, true);

        return new Tuple<>(response, shardRequest.request);
    }
    
    @Override
    protected void shardOperationOnReplica(ShardId shardId, IndexRequest request) {
        
    }

}
