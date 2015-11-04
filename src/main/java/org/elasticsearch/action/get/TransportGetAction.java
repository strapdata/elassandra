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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.utils.FBUtilities;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.RoutingMissingException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.single.shard.TransportShardSingleOperationAction;
import org.elasticsearch.cassandra.SchemaService;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.ShardIterator;
import org.elasticsearch.cluster.routing.operation.plain.Preference;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Performs the get operation.
 */
public class TransportGetAction extends TransportShardSingleOperationAction<GetRequest, GetResponse> {

    private final IndicesService indicesService;
    private final SchemaService elasticSchemaService;
    private final boolean realtime;

    @Inject
    public TransportGetAction(Settings settings, ClusterService clusterService, TransportService transportService, IndicesService indicesService, ThreadPool threadPool, ActionFilters actionFilters,
            SchemaService elasticSchemaService) {
        super(settings, GetAction.NAME, threadPool, clusterService, transportService, actionFilters);
        this.indicesService = indicesService;
        this.elasticSchemaService = elasticSchemaService;

        this.realtime = settings.getAsBoolean("action.get.realtime", true);
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.GET;
    }

    @Override
    protected boolean resolveIndex() {
        return true;
    }

    @Override
    protected ShardIterator shards(ClusterState state, InternalRequest request) {
        return clusterService.operationRouting().getShards(clusterService.state(), request.concreteIndex(), request.request().type(), request.request().id(), request.request().routing(),
                request.request().preference());
    }

    @Override
    protected void resolveRequest(ClusterState state, InternalRequest request) {
        if (request.request().realtime == null) {
            request.request().realtime = this.realtime;
        }
        IndexMetaData indexMeta = state.getMetaData().index(request.concreteIndex());
        if (request.request().realtime && // if the realtime flag is set
                request.request().preference() == null && // the preference flag
                                                          // is not already set
                indexMeta != null && // and we have the index
                IndexMetaData.isIndexUsingShadowReplicas(indexMeta.settings())) { // and
                                                                                  // the
                                                                                  // index
                                                                                  // uses
                                                                                  // shadow
                                                                                  // replicas
            // set the preference for the request to use "_primary"
            // automatically
            request.request().preference(Preference.PRIMARY.type());
        }
        // update the routing (request#index here is possibly an alias)
        request.request().routing(state.metaData().resolveIndexRouting(request.request().routing(), request.request().index()));
        // Fail fast on the node that received the request.
        if (request.request().routing() == null && state.getMetaData().routingRequired(request.concreteIndex(), request.request().type())) {
            throw new RoutingMissingException(request.concreteIndex(), request.request().type(), request.request().id());
        }
    }

    @Override
    protected GetResponse shardOperation(GetRequest request, ShardId shardId) throws ElasticsearchException {
        /*
        IndexService indexService = indicesService.indexService(request.index());

        Collection<String> columns;
        if (request.fields() != null) {
            columns = new ArrayList<String>(request.fields().length);
            for (String field : request.fields()) {
                int i = field.indexOf('.');
                String colName = (i > 0) ? field.substring(0, i ) : field;
                if (!columns.contains(colName))
                    columns.add(colName);
            }
        } else {
            columns = elasticSchemaService.mappedColumns(indexService.index().getName(), request.type());
        }

        // TODO: add param consistencyLevel
        try {
            request.f
            UntypedResultSet result = elasticSchemaService.fetchRow(request.index(), request.type(), columns, request.id() );
            if (!result.isEmpty()) {
                Map<String, Object> rowAsMap = elasticSchemaService.rowAsMap(result.one());
                Map<String, GetField> rowAsFieldMap = null;
                BytesReference source = null;
                
                
                if (request.fields() != null) {
                    elasticSchemaService.flattenGetField(request.fields(), "", rowAsMap, rowAsFieldMap);
                } else {
                    if (request.q)
                    //source = new BytesArray(FBUtilities.json(rowAsMap).getBytes("UTF-8"));
                    source = XContentFactory.contentBuilder(XContentType.JSON).map(rowAsMap).bytes();
                }
                
                GetResult getResult = new GetResult(request.index(), request.type(), request.id(), 0L, true, source, rowAsFieldMap);
                return new GetResponse(getResult);
            }
        } catch (org.apache.cassandra.exceptions.ConfigurationException | org.apache.cassandra.exceptions.SyntaxException e) {
            logger.warn("Cassandra probably not yet up to date: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ElasticsearchException(e.getMessage(), e);
        }
        return new GetResponse(new GetResult(request.index(), request.type(), request.id(), 0L, false, null, null));
        */
        IndexService indexService = indicesService.indexServiceSafe(shardId.getIndex());
        IndexShard indexShard = indexService.shardSafe(shardId.id());

        if (request.refresh() && !request.realtime()) {
            indexShard.refresh("refresh_flag_get");
        }

        GetResult result = indexShard.getService().get(request.type(), request.id(), request.fields(),
                request.realtime(), request.version(), request.versionType(), request.fetchSourceContext(), request.ignoreErrorsOnGeneratedFields());
        return new GetResponse(result);
    }

    @Override
    protected GetRequest newRequest() {
        return new GetRequest();
    }

    @Override
    protected GetResponse newResponse() {
        return new GetResponse();
    }
}
