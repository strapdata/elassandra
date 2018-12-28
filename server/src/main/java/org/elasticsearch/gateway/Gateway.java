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

import org.apache.logging.log4j.message.ParameterizedMessage;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.zen.ElectMasterService;
import org.elasticsearch.indices.IndicesService;

import java.util.Map;

public class Gateway extends AbstractComponent {

    private final ClusterService clusterService;

    private final TransportNodesListGatewayMetaState listGatewayMetaState;

    private final int minimumMasterNodes;
    private final IndicesService indicesService;

    public Gateway(Settings settings, ClusterService clusterService,
                   TransportNodesListGatewayMetaState listGatewayMetaState,
                   IndicesService indicesService) {
        super(settings);
        this.indicesService = indicesService;
        this.clusterService = clusterService;
        this.listGatewayMetaState = listGatewayMetaState;
        this.minimumMasterNodes = ElectMasterService.DISCOVERY_ZEN_MINIMUM_MASTER_NODES_SETTING.get(settings);
    }

    public void performStateRecovery(final GatewayStateRecoveredListener listener) throws GatewayException {
        ClusterState.Builder builder = ClusterState.builder(clusterService.state());

        MetaData metadata = null;
        try {
            metadata = clusterService.loadGlobalState();
            logger.info("Successfull cluster state recovery from CQL schema version={}/{}", metadata.clusterUUID(), metadata.version());
            listener.onSuccess( builder.metaData(metadata).build() );
            return;
        } catch (Exception e) {
            metadata = clusterService.state().metaData();
            if (metadata.clusterUUID().equals("_na_")) {
                metadata = MetaData.builder(metadata).clusterUUID(clusterService.localNode().getId()).build();
            }
            logger.info("New cluster state metadata version={}/{}", metadata.clusterUUID(), metadata.version());
            listener.onSuccess( builder.metaData(metadata).build() );
        }
    }

    private void logUnknownSetting(String settingType, Map.Entry<String, String> e) {
        logger.warn("ignoring unknown {} setting: [{}] with value [{}]; archiving", settingType, e.getKey(), e.getValue());
    }

    private void logInvalidSetting(String settingType, Map.Entry<String, String> e, IllegalArgumentException ex) {
        logger.warn(
            (org.apache.logging.log4j.util.Supplier<?>)
                () -> new ParameterizedMessage("ignoring invalid {} setting: [{}] with value [{}]; archiving",
                    settingType,
                    e.getKey(),
                    e.getValue()),
            ex);
    }

    public interface GatewayStateRecoveredListener {
        void onSuccess(ClusterState build);

        void onFailure(String s);
    }
}
