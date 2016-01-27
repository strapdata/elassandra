/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
 * Contains some code from Elasticsearch (http://www.elastic.co)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.cassandra.gateway;

import java.nio.file.Path;

import org.apache.lucene.util.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cassandra.NoPersistedMetaDataException;
import org.elasticsearch.cassandra.SchemaService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.cassandra.discovery.CassandraDiscovery;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.gateway.Gateway;
import org.elasticsearch.gateway.GatewayException;
//import org.elasticsearch.index.gateway.local.LocalIndexGatewayModule;

/**
 * Recover metadata from cassandra schema.
 */
public class CassandraGateway extends AbstractComponent implements ClusterStateListener {

    private final ClusterService clusterService;
    private final NodeEnvironment nodeEnv;

    @Inject
    public CassandraGateway(Settings settings, ClusterName clusterName, ClusterService clusterService, NodeEnvironment nodeEnv, CassandraDiscovery cassandraDiscovery) {
        super(settings);
        this.clusterService = clusterService;
        this.nodeEnv = nodeEnv;
    }


    

    public void performStateRecovery(final GatewayStateRecoveredListener listener) throws GatewayException {
        logger.debug("performing metadata recovery from cassandra");
        ClusterState.Builder builder = ClusterState.builder(clusterService.state());

        MetaData metadata;
        try {
            /*
             * Recovery performed from comment because elatic_admin keyspace won't be available before replaying commmit logs.
             */
            metadata = clusterService.readMetaDataAsComment();
        } catch (NoPersistedMetaDataException e) {
            metadata = clusterService.state().metaData();
            if (metadata.uuid().equals("_na_")) {
                metadata = MetaData.builder(metadata).clusterUUID(clusterService.localNode().id()).build();
            }
        }

        builder.metaData(metadata);
        listener.onSuccess(builder.build());
    }


    public void reset() throws Exception {
        try {
            Path[] dataPaths = nodeEnv.nodeDataPaths();
            logger.trace("removing node data paths: [{}]", dataPaths);
            IOUtils.rm(dataPaths);
        } catch (Exception ex) {
            logger.debug("failed to delete shard locations", ex);
        }
    }

    public interface GatewayStateRecoveredListener {
        void onSuccess(ClusterState build);

        void onFailure(String s);
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        // TODO Auto-generated method stub
        
    }

}
