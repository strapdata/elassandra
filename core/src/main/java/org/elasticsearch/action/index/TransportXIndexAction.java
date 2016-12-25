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

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaDataCreateIndexService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.tasks.Task;
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
        this.clusterService = clusterService;
    }

   
    @Override
    protected void innerExecute(Task task, final IndexRequest request, final ActionListener<IndexResponse> listener) {
        try {
            final ClusterState state = clusterService.state();
            ClusterBlockException blockException = state.blocks().globalBlockedException(globalBlockLevel());
            if (blockException != null)
                throw blockException;
           
            final String concreteIndex = resolveIndex() ? indexNameExpressionResolver.concreteSingleIndex(state, request) : request.index();
            blockException = state.blocks().indexBlockedException(indexBlockLevel(), concreteIndex);
            if (blockException != null)
                throw blockException;
            
            // request does not have a shardId yet, we need to pass the concrete index to resolve shardId
            resolveRequest(state.metaData(), concreteIndex, request);
            
            clusterService.insertDocument(indicesService, request, state.metaData().index(concreteIndex));
            request.versionType(request.versionType().versionTypeForReplicationAndRecovery());
            IndexResponse response = new IndexResponse(concreteIndex, request.type(), request.id(), new Long(1), true);
            response.setShardInfo(clusterService.shardInfo(concreteIndex, request.consistencyLevel().toCassandraConsistencyLevel()));
            listener.onResponse(response);
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

}
