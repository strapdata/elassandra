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

import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexClusterStateUpdateRequest;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.AutoCreateIndex;
import org.elasticsearch.action.support.replication.TransportShardReplicationOperationAction;
import org.elasticsearch.cassandra.ElasticSchemaService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataCreateIndexService;
import org.elasticsearch.cluster.routing.ShardIterator;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Performs the index operation.
 * <p/>
 * <p>
 * Allows for the following settings:
 * <ul>
 * <li><b>autoCreateIndex</b>: When set to <tt>true</tt>, will automatically
 * create an index if one does not exists. Defaults to <tt>true</tt>.
 * <li><b>allowIdGeneration</b>: If the id is set not, should it be generated.
 * Defaults to <tt>true</tt>.
 * </ul>
 */
public class TransportIndexAction extends TransportShardReplicationOperationAction<IndexRequest, IndexRequest, IndexResponse> {

    private final AutoCreateIndex autoCreateIndex;

    private final boolean allowIdGeneration;

    // private final TransportCreateIndexAction createIndexAction;
    private final MetaDataCreateIndexService createIndexService;

    private final MappingUpdatedAction mappingUpdatedAction;

    @Inject
    public TransportIndexAction(Settings settings, TransportService transportService, ClusterService clusterService, MetaDataCreateIndexService createIndexService, IndicesService indicesService,
            ThreadPool threadPool, ShardStateAction shardStateAction, MappingUpdatedAction mappingUpdatedAction, ElasticSchemaService elasticSchemaManager, ActionFilters actionFilters) {
        super(settings, IndexAction.NAME, transportService, clusterService, indicesService, threadPool, shardStateAction, actionFilters, elasticSchemaManager);
        // this.createIndexAction = createIndexAction;
        this.createIndexService = createIndexService;
        this.mappingUpdatedAction = mappingUpdatedAction;
        this.autoCreateIndex = new AutoCreateIndex(settings);
        this.allowIdGeneration = settings.getAsBoolean("action.allow_id_generation", true);
    }

    @Override
    protected void doExecute(final IndexRequest request, final ActionListener<IndexResponse> listener) {
        // if we don't have a master, we don't have metadata, that's fine, let
        // it find a master using create index API
        if (autoCreateIndex.shouldAutoCreate(request.index(), clusterService.state())) {
            request.beforeLocalFork(); // we fork on another thread...
            /*
             * CreateIndexRequest createIndexRequest = new
             * CreateIndexRequest(request);
             * createIndexRequest.index(request.index());
             * createIndexRequest.mapping(request.type());
             * createIndexRequest.cause("auto(index api)");
             * createIndexRequest.masterNodeTimeout(request.timeout());
             * createIndexAction.execute(createIndexRequest, new
             * ActionListener<CreateIndexResponse>() {
             * 
             * @Override public void onResponse(CreateIndexResponse result) {
             * innerExecute(request, listener); }
             * 
             * @Override public void onFailure(Throwable e) { if
             * (ExceptionsHelper.unwrapCause(e) instanceof
             * IndexAlreadyExistsException) { // we have the index, do it try {
             * innerExecute(request, listener); } catch (Throwable e1) {
             * listener.onFailure(e1); } } else { listener.onFailure(e); } } });
             */

            // no more request to master node, we can create index.
            ClusterBlockException blockedException = clusterService.state().blocks().indexBlockedException(ClusterBlockLevel.METADATA, request.index());
            if (blockedException != null) {
                throw blockedException;
            }

            CreateIndexClusterStateUpdateRequest createIndexRequest = new CreateIndexClusterStateUpdateRequest(request, "auto(index api)", request.index()).ackTimeout(request.timeout())
                    .masterNodeTimeout(request.timeout());

            createIndexService.createIndex(createIndexRequest, new ActionListener<ClusterStateUpdateResponse>() {
                @Override
                public void onResponse(ClusterStateUpdateResponse result) {
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

    @Override
    protected boolean resolveIndex() {
        return true;
    }

    @Override
    protected boolean resolveRequest(ClusterState state, InternalRequest request, ActionListener<IndexResponse> indexResponseActionListener) {
        MetaData metaData = clusterService.state().metaData();

        MappingMetaData mappingMd = null;
        if (metaData.hasIndex(request.concreteIndex())) {
            mappingMd = metaData.index(request.concreteIndex()).mappingOrDefault(request.request().type());
        }
        request.request().process(metaData, mappingMd, allowIdGeneration, request.concreteIndex());
        return true;
    }

    private void innerExecute(final IndexRequest request, final ActionListener<IndexResponse> listener) {
        super.doExecute(request, listener);
    }

    @Override
    protected boolean checkWriteConsistency() {
        return true;
    }

    @Override
    protected IndexRequest newRequestInstance() {
        return new IndexRequest();
    }

    @Override
    protected IndexRequest newReplicaRequestInstance() {
        return new IndexRequest();
    }

    @Override
    protected IndexResponse newResponseInstance() {
        return new IndexResponse();
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.INDEX;
    }

    @Override
    protected ShardIterator shards(ClusterState clusterState, InternalRequest request) {
        return clusterService.operationRouting().indexShards(clusterService.state(), request.concreteIndex(), request.request().type(), request.request().id(), request.request().routing());
    }

    @Override
    protected PrimaryResponse<IndexResponse, IndexRequest> shardOperationOnPrimary(ClusterState clusterState, PrimaryOperationRequest shardRequest) throws Throwable {
        final IndexRequest request = shardRequest.request;

        Long writetime = new Long(0);
        Boolean applied = new Boolean(true);
        String id = elasticSchemaService.insertDocument(indicesService, request, clusterState, writetime, applied);
        request.versionType(request.versionType().versionTypeForReplicationAndRecovery());
        IndexResponse response = new IndexResponse(shardRequest.shardId.getIndex(), request.type(), id, writetime, applied);

        return new PrimaryResponse<>(shardRequest.request, response, null);
    }

    @Override
    protected void shardOperationOnReplica(ReplicaOperationRequest shardRequest) {
        // should never happend
        assert false;
    }
}
