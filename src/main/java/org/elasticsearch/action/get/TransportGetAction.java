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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.utils.FBUtilities;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.RoutingMissingException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.single.shard.TransportShardSingleOperationAction;
import org.elasticsearch.cassandra.SchemaService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.ShardIterator;
import org.elasticsearch.cluster.routing.operation.plain.Preference;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesService;
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
        IndexService indexService = indicesService.indexService(request.index());

        List<String> columns;
        if (request.fields() != null) {
            columns = new ArrayList<String>(request.fields().length);
            for (String field : request.fields()) {
                int i = field.indexOf('.');
                String colName = (i > 0) ? field.substring(0, i - 1) : field;
                if (!columns.contains(colName))
                    columns.add(colName);
            }
        } else {
            columns = elasticSchemaService.cassandraMappedColumns(indexService.index().getName(), request.type());
        }

        // TODO: add param consistencyLevel
        try {
            UntypedResultSet result = elasticSchemaService.fetchRow(request.index(), request.type(), request.id(), columns);
            if (!result.isEmpty()) {
                Map<String, Object> rowAsMap = elasticSchemaService.rowAsMap(result.one(), null, indexService.mapperService(), new String[] { request.type() });
                Map<String, GetField> rowAsFieldMap = elasticSchemaService.flattenGetField(request.fields(), "", rowAsMap, new HashMap<String, GetField>());

                GetResult getResult = new GetResult(request.index(), request.type(), request.id(), 0L, true, new BytesArray(FBUtilities.json(rowAsMap).getBytes("UTF-8")), rowAsFieldMap);
                return new GetResponse(getResult);
            }
        } catch (org.apache.cassandra.exceptions.ConfigurationException | org.apache.cassandra.exceptions.SyntaxException e) {
            logger.warn("Cassandra probably not yet up to date: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ElasticsearchException(e.getMessage(), e);
        }
        return new GetResponse(new GetResult(request.index(), request.type(), request.id(), 0L, false, null, null));
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
