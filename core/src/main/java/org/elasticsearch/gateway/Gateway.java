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

package org.elasticsearch.gateway;

import java.nio.file.Path;

import org.apache.lucene.util.IOUtils;
import org.elassandra.NoPersistedMetaDataException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.NodeEnvironment;

/**
 *
 */
public class Gateway extends AbstractComponent implements ClusterStateListener {

    private final ClusterService clusterService;

    private final NodeEnvironment nodeEnv;
    private final String initialMeta;
    private final ClusterName clusterName;

    @Inject
    public Gateway(Settings settings, ClusterService clusterService, NodeEnvironment nodeEnv, ClusterName clusterName) {
        super(settings);
        this.clusterService = clusterService;
        this.nodeEnv = nodeEnv;
        this.clusterName = clusterName;

        clusterService.addLast(this);

        // we define what is our minimum "master" nodes, use that to allow for recovery
        this.initialMeta = settings.get("gateway.initial_meta", settings.get("gateway.local.initial_meta", settings.get("discovery.zen.minimum_master_nodes", "1")));
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
        } catch (NoPersistedMetaDataException |ActionRequestValidationException e) {
            logger.trace("Failed to read metadata from table comment", e);
            metadata = clusterService.state().metaData();
            if (metadata.uuid().equals("_na_")) {
                metadata = MetaData.builder(metadata).clusterUUID(clusterService.localNode().id()).build();
            }
        }

        listener.onSuccess( clusterService.updateNumberOfShards(builder.metaData(metadata).build()) );
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

    @Override
    public void clusterChanged(final ClusterChangedEvent event) {
        // order is important, first metaState, and then shardsState
        // so dangling indices will be recorded
        //metaState.clusterChanged(event);
    }

    public interface GatewayStateRecoveredListener {
        void onSuccess(ClusterState build);

        void onFailure(String s);
    }
}
