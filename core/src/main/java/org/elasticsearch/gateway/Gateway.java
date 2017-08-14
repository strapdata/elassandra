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
import org.elassandra.NoPersistedMetaDataException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateApplier;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.indices.IndicesService;

import java.util.Map;
import java.util.function.Supplier;

public class Gateway extends AbstractComponent implements ClusterStateApplier {

    private final ClusterService clusterService;

    public Gateway(Settings settings, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
        //clusterService.addLowPriorityApplier(this);
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
            logger.trace("Cannot read metadata from table comment", e);
            metadata = clusterService.state().metaData();
            if (metadata.clusterUUID().equals("_na_")) {
                metadata = MetaData.builder(metadata).clusterUUID(clusterService.localNode().getId()).build();
            }
        }

        listener.onSuccess( clusterService.updateNumberOfShards(builder.metaData(metadata).build()) );
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

    @Override
    public void applyClusterState(final ClusterChangedEvent event) {
        // order is important, first metaState, and then shardsState
        // so dangling indices will be recorded
        //metaState.applyClusterState(event);
    }

    public interface GatewayStateRecoveredListener {
        void onSuccess(ClusterState build);

        void onFailure(String s);
    }
}
