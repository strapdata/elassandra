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

package org.elasticsearch.action.delete;

import java.io.IOException;
import java.util.Collections;

import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
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
 * Performs the delete operation.
 */
public class TransportXDeleteAction extends TransportDeleteAction {

    
    @Inject
    public TransportXDeleteAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                 IndicesService indicesService, ThreadPool threadPool,
                                 TransportCreateIndexAction createIndexAction, ActionFilters actionFilters,
                                 IndexNameExpressionResolver indexNameExpressionResolver, MappingUpdatedAction mappingUpdatedAction,
                                 AutoCreateIndex autoCreateIndex) {
        super( settings,  transportService,  clusterService,
                 indicesService,  threadPool,
                 createIndexAction,  actionFilters,
                 indexNameExpressionResolver,  mappingUpdatedAction,
                 autoCreateIndex);
    }

    /**
     * Fake shard iterator to execute locally only for elassandra.
     */
    @Override
    protected ShardIterator shards(ClusterState clusterState, InternalRequest request) {
    	ShardRouting primaryShardRouting  = new ShardRouting(request.concreteIndex(), 0, clusterService.localNode().id(), true, ShardRoutingState.STARTED);
    	return new PlainShardIterator(primaryShardRouting.shardId(), Collections.singletonList(primaryShardRouting));
    }
    
    /**
     * Modified to delete document in cassandra.
     */
    @Override
    protected Tuple<DeleteResponse, DeleteRequest> shardOperationOnPrimary(ClusterState clusterState, PrimaryOperationRequest shardRequest)  {
        DeleteRequest request = shardRequest.request;
        try {
            clusterService.deleteRow(request.index(), request.type(), request.id(), request.consistencyLevel().toCassandraConsistencyLevel());
            DeleteResponse response = new DeleteResponse(shardRequest.shardId.getIndex(), request.type(), request.id(), 0L, true);
            return new Tuple<>(response, shardRequest.request);
        } catch (RequestExecutionException | RequestValidationException | IOException e) {
            throw new ElasticsearchException(e.getMessage(),e);
        }
    }
    
    @Override
    protected void shardOperationOnReplica(ShardId shardId, DeleteRequest request) {
        
    }
}
