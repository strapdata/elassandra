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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cassandra.ElasticSchemaService;
import org.elasticsearch.cluster.CassandraClusterState;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ProcessedClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.threadpool.ThreadPool;

/**
 *
 */
public class GatewayService extends AbstractLifecycleComponent<GatewayService> implements ClusterStateListener {

	public static final ClusterBlock STATE_NOT_RECOVERED_BLOCK = new ClusterBlock(1, "state not recovered / initialized", true, true, RestStatus.SERVICE_UNAVAILABLE, ClusterBlockLevel.ALL);
	public static final ClusterBlock NO_CASSANDRA_RING_BLOCK = new ClusterBlock(12, "no cassandra ring", true, true, RestStatus.SERVICE_UNAVAILABLE, ClusterBlockLevel.NONE);

    public static final TimeValue DEFAULT_RECOVER_AFTER_TIME_IF_EXPECTED_NODES_IS_SET = TimeValue.timeValueMinutes(5);

    private final Gateway gateway;

    private final ThreadPool threadPool;

    //private final AllocationService allocationService;

    private final ClusterService clusterService;

    private final DiscoveryService discoveryService;
    private final ElasticSchemaService elasticSchemaService;
    
    private final TimeValue recoverAfterTime;
    private final int recoverAfterNodes;
    private final int expectedNodes;
    private final int recoverAfterDataNodes;
    private final int expectedDataNodes;
    private final int recoverAfterMasterNodes;
    private final int expectedMasterNodes;


    private final AtomicBoolean recovered = new AtomicBoolean();
    private final AtomicBoolean scheduledRecovery = new AtomicBoolean();

    @Inject
    public GatewayService(Settings settings, Gateway gateway, AllocationService allocationService, ClusterService clusterService, 
    		DiscoveryService discoveryService, ThreadPool threadPool, ElasticSchemaService elasticSchemaService) {
        super(settings);
        this.gateway = gateway;
        //this.allocationService = allocationService;
        this.clusterService = clusterService;
        this.discoveryService = discoveryService;
        this.elasticSchemaService = elasticSchemaService;
        this.threadPool = threadPool;
        // allow to control a delay of when indices will get created
        this.expectedNodes = componentSettings.getAsInt("expected_nodes", -1);
        this.expectedDataNodes = componentSettings.getAsInt("expected_data_nodes", -1);
        this.expectedMasterNodes = componentSettings.getAsInt("expected_master_nodes", -1);

        TimeValue defaultRecoverAfterTime = null;
        if (expectedNodes >= 0 || expectedDataNodes >= 0 || expectedMasterNodes >= 0) {
            defaultRecoverAfterTime = DEFAULT_RECOVER_AFTER_TIME_IF_EXPECTED_NODES_IS_SET;
        }

        this.recoverAfterTime = componentSettings.getAsTime("recover_after_time", defaultRecoverAfterTime);
        this.recoverAfterNodes = componentSettings.getAsInt("recover_after_nodes", -1);
        this.recoverAfterDataNodes = componentSettings.getAsInt("recover_after_data_nodes", -1);
        // default the recover after master nodes to the minimum master nodes in the discovery
        this.recoverAfterMasterNodes = componentSettings.getAsInt("recover_after_master_nodes", settings.getAsInt("discovery.zen.minimum_master_nodes", -1));

        // Add the not recovered as initial state block, we don't allow anything until
        this.clusterService.addInitialStateBlock(STATE_NOT_RECOVERED_BLOCK);
        
        // block to avoid to persist metadata while cassandra is not ready.
        this.clusterService.addInitialStateBlock(NO_CASSANDRA_RING_BLOCK);
    }

    /**
     * release the NO_CASSANDRA_RING_BLOCK and update routingTable since the node'state = NORMAL (i.e a member of the ring) 
     * (may be update when replaying the cassandra logs)
     */
    public void enableMetaDataPersictency() {
    	logger.trace("releasing the cassandra ring block...");
    	
        clusterService.submitStateUpdateTask("gateway-cassandra-ring-ready", new ProcessedClusterStateUpdateTask() {
            @Override
            public CassandraClusterState execute(CassandraClusterState currentState) {
            	
            	
                // remove the block, since we recovered from gateway
                ClusterBlocks.Builder blocks = ClusterBlocks.builder()
                        .blocks(currentState.blocks())
                        .removeGlobalBlock(NO_CASSANDRA_RING_BLOCK);

                RoutingTable newRoutingTable = RoutingTable.builder(clusterService, currentState).build();
                
                // update the state to reflect 
                CassandraClusterState updatedState = CassandraClusterState.builder(currentState)
                        .blocks(blocks)
                        .routingTable(newRoutingTable)
                        .build();
                
                return updatedState;
            }

            @Override
            public boolean doPresistMetaData() {
            	// nothing to persist, we just recover from cassandra schema.
            	return false;
            }
            
            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }

            @Override
            public void clusterStateProcessed(String source, CassandraClusterState oldState, CassandraClusterState newState) {
                logger.info("cassandra ring block released");
            }
        });
    }
    
    @Override
    protected void doStart() throws ElasticsearchException {
        gateway.start();
        clusterService.addLast(this);
        // if we received initial state, see if we can recover within the start phase, so we hold the
        // node from starting until we recovered properly
        if (discoveryService.initialStateReceived()) {
            CassandraClusterState clusterState = clusterService.state();
            if (clusterState.nodes().localNodeMaster() && clusterState.blocks().hasGlobalBlock(STATE_NOT_RECOVERED_BLOCK)) {
                checkStateMeetsSettingsAndMaybeRecover(clusterState, false);
            }
        } else {
            logger.error("should wait on start for (possibly) reading state from gateway");
            assert false;
        }
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        clusterService.remove(this);
        gateway.stop();
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        gateway.close();
    }

    @Override
    public void clusterChanged(final ClusterChangedEvent event) {
        if (lifecycle.stoppedOrClosed()) {
            return;
        }
        if (event.localNodeMaster() && event.state().blocks().hasGlobalBlock(STATE_NOT_RECOVERED_BLOCK)) {
            checkStateMeetsSettingsAndMaybeRecover(event.state(), true);
        }
    }

    
    protected void checkStateMeetsSettingsAndMaybeRecover(CassandraClusterState state, boolean asyncRecovery) {
        DiscoveryNodes nodes = state.nodes();
        if (state.blocks().hasGlobalBlock(discoveryService.getNoMasterBlock())) {
            logger.debug("not recovering from gateway, no master elected yet");
        } else if (recoverAfterNodes != -1 && (nodes.masterAndDataNodes().size()) < recoverAfterNodes) {
            logger.debug("not recovering from gateway, nodes_size (data+master) [" + nodes.masterAndDataNodes().size() + "] < recover_after_nodes [" + recoverAfterNodes + "]");
        } else if (recoverAfterDataNodes != -1 && nodes.dataNodes().size() < recoverAfterDataNodes) {
            logger.debug("not recovering from gateway, nodes_size (data) [" + nodes.dataNodes().size() + "] < recover_after_data_nodes [" + recoverAfterDataNodes + "]");
        } else if (recoverAfterMasterNodes != -1 && nodes.masterNodes().size() < recoverAfterMasterNodes) {
            logger.debug("not recovering from gateway, nodes_size (master) [" + nodes.masterNodes().size() + "] < recover_after_master_nodes [" + recoverAfterMasterNodes + "]");
        } else {
            boolean enforceRecoverAfterTime;
            String reason;
            if (expectedNodes == -1 && expectedMasterNodes == -1 && expectedDataNodes == -1) {
                // no expected is set, honor the setting if they are there
                enforceRecoverAfterTime = true;
                reason = "recovery_after_time was set to [" + recoverAfterTime + "]";
            } else {
                // one of the expected is set, see if all of them meet the need, and ignore the timeout in this case
                enforceRecoverAfterTime = false;
                reason = "";
                if (expectedNodes != -1 && (nodes.masterAndDataNodes().size() < expectedNodes)) { // does not meet the expected...
                    enforceRecoverAfterTime = true;
                    reason = "expecting [" + expectedNodes + "] nodes, but only have [" + nodes.masterAndDataNodes().size() + "]";
                } else if (expectedDataNodes != -1 && (nodes.dataNodes().size() < expectedDataNodes)) { // does not meet the expected...
                    enforceRecoverAfterTime = true;
                    reason = "expecting [" + expectedDataNodes + "] data nodes, but only have [" + nodes.dataNodes().size() + "]";
                } else if (expectedMasterNodes != -1 && (nodes.masterNodes().size() < expectedMasterNodes)) { // does not meet the expected...
                    enforceRecoverAfterTime = true;
                    reason = "expecting [" + expectedMasterNodes + "] master nodes, but only have [" + nodes.masterNodes().size() + "]";
                }
            }
            performStateRecovery(asyncRecovery, enforceRecoverAfterTime, reason);
        }
    }

    private void performStateRecovery(boolean asyncRecovery, boolean enforceRecoverAfterTime, String reason) {
    	CountDownLatch recoverLatch = new CountDownLatch(1);
        final Gateway.GatewayStateRecoveredListener recoveryListener = new GatewayRecoveryListener(recoverLatch);

        if (enforceRecoverAfterTime && recoverAfterTime != null) {
            if (scheduledRecovery.compareAndSet(false, true)) {
                logger.info("delaying initial state recovery for [{}]. {}", recoverAfterTime, reason);
                threadPool.schedule(recoverAfterTime, ThreadPool.Names.GENERIC, new Runnable() {
                    @Override
                    public void run() {
                        if (recovered.compareAndSet(false, true)) {
                            logger.info("recovery_after_time [{}] elapsed. performing state recovery...", recoverAfterTime);
                            gateway.performStateRecovery(recoveryListener);
                        }
                    }
                });
            }
        } else {
            if (recovered.compareAndSet(false, true)) {
                if (asyncRecovery) {
                    threadPool.generic().execute(new Runnable() {
                        @Override
                        public void run() {
                            gateway.performStateRecovery(recoveryListener);
                        }
                    });
                } else {
                    logger.trace("performing state recovery...");
                    gateway.performStateRecovery(recoveryListener);
                }
            }
        }
        try {
			recoverLatch.await();
		} catch (InterruptedException e) {
			logger.error("Waiting recovery interrupted", e);
		}
    }

    class GatewayRecoveryListener implements Gateway.GatewayStateRecoveredListener {

        private final CountDownLatch latch;

        GatewayRecoveryListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(final CassandraClusterState recoveredState) {
            logger.trace("successful state recovery, importing cluster state...");
            clusterService.submitStateUpdateTask("gateway-recover-state", new ProcessedClusterStateUpdateTask() {
                @Override
                public CassandraClusterState execute(CassandraClusterState currentState) {
                    assert currentState.metaData().indices().isEmpty();

                    // remove the block, since we recovered from gateway
                    ClusterBlocks.Builder blocks = ClusterBlocks.builder()
                            .blocks(currentState.blocks())
                            .blocks(recoveredState.blocks())
                            .removeGlobalBlock(STATE_NOT_RECOVERED_BLOCK);

                    MetaData.Builder metaDataBuilder = MetaData.builder(recoveredState.metaData());
                    // automatically generate a UID for the metadata if we need to
                    //metaDataBuilder.generateUuidIfNeeded();

                    if (recoveredState.metaData().settings().getAsBoolean(MetaData.SETTING_READ_ONLY, false) || currentState.metaData().settings().getAsBoolean(MetaData.SETTING_READ_ONLY, false)) {
                        blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);
                    }

                    for (IndexMetaData indexMetaData : recoveredState.metaData()) {
                        metaDataBuilder.put(indexMetaData, false);
                        blocks.addBlocks(indexMetaData);
                    }

                    // update the state to reflect the new metadata and routing
                    CassandraClusterState updatedState = CassandraClusterState.builder(currentState)
                            .blocks(blocks)
                            .metaData(metaDataBuilder)
                            .build();

                    // initialize all index routing tables with the one local primary shard active only
                    RoutingTable.Builder routingTableBuilder = RoutingTable.builder(clusterService, updatedState);
                    return CassandraClusterState.builder(updatedState).routingTable(routingTableBuilder).build();
                }

                @Override
                public boolean doPresistMetaData() {
                	return false;
                }
                
                @Override
                public void onFailure(String source, Throwable t) {
                    logger.error("unexpected failure during [{}]", t, source);
                }

                @Override
                public void clusterStateProcessed(String source, CassandraClusterState oldState, CassandraClusterState newState) {
                    logger.info("recovered [{}] indices into cluster_state", newState.metaData().indices().size());
                    latch.countDown();
                }
            });
        }

        @Override
        public void onFailure(String message) {
            recovered.set(false);
            scheduledRecovery.set(false);
            // don't remove the block here, we don't want to allow anything in such a case
            logger.info("metadata state not restored, reason: {}", message);
        }
    }

    // used for testing
    public TimeValue recoverAfterTime() {
        return recoverAfterTime;
    }

}
