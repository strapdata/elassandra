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

package org.elasticsearch.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;

/**
 *
 */
public class DiscoveryService extends AbstractLifecycleComponent<DiscoveryService> {

    public static final String SETTING_INITIAL_STATE_TIMEOUT = "discovery.initial_state_timeout";
    public static final String SETTING_DISCOVERY_SEED = "discovery.id.seed";

    private final TimeValue initialStateTimeout;
    private final Discovery discovery;
    private final DiscoverySettings discoverySettings;

    @Inject
    public DiscoveryService(Settings settings, DiscoverySettings discoverySettings, Discovery discovery) {
        super(settings);
        this.discoverySettings = discoverySettings;
        this.discovery = discovery;
        this.initialStateTimeout = settings.getAsTime(SETTING_INITIAL_STATE_TIMEOUT, TimeValue.timeValueSeconds(30));
    }

    public ClusterBlock getNoMasterBlock() {
        return discoverySettings.getNoMasterBlock();
    }

    @Override
    protected void doStart() {
        //initialStateListener = new InitialStateListener();
        //discovery.addListener(initialStateListener);
        discovery.start();
        logger.info(discovery.nodeDescription());
    }
    
    @Override
    protected void doStop() {
        discovery.stop();
    }

    @Override
    protected void doClose() {
        discovery.close();
    }

    public DiscoveryNode localNode() {
        return discovery.localNode();
    }

    public String nodeDescription() {
        return discovery.nodeDescription();
    }
    
    public static String generateNodeId(Settings settings) {
        String seed = settings.get(DiscoveryService.SETTING_DISCOVERY_SEED);
        if (seed != null) {
            return Strings.randomBase64UUID(new Random(Long.parseLong(seed)));
        }
        return Strings.randomBase64UUID();
    }
    
    
    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void putShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        this.discovery.putShardRoutingState(index, shardRoutingState);
    }

    
    public Map<UUID, ShardRoutingState> getShardRoutingStates(String index) {
         return this.discovery.getShardRoutingStates(index);
    }
    
    public void removeIndexShardState(final String index) throws JsonGenerationException, JsonMappingException, IOException {
        this.discovery.putShardRoutingState(index, null);
    }
    
    /**
     * Publish cluster metadata uuid and version in gossip state.
     * @param clusterState
     */
    public void publishX2(final ClusterState clusterState) {
        this.discovery.publishX2(clusterState);
    }
    
    /**
     * Publish local routingShard state in gossip state.
     * @param clusterState
     */
    public void publishX1(final ClusterState clusterState) {
        this.discovery.publishX1(clusterState);
    }

    public boolean awaitMetaDataVersion(long version, TimeValue ackTimeout) throws Exception  {
        return this.discovery.awaitMetaDataVersion(version, ackTimeout);
    }
    
    public void connectToNodes() {
        this.discovery.connectToNodes();
    }
}
