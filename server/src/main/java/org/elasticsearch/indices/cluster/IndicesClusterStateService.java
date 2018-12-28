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

package org.elasticsearch.indices.cluster;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.apache.lucene.store.LockObtainFailedException;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.shard.CassandraShardStateListener;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateApplier;
import org.elasticsearch.cluster.action.index.NodeMappingRefreshAction;
import org.elasticsearch.cluster.action.shard.ShardStateAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData.State;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.RecoverySource;
import org.elasticsearch.cluster.routing.RecoverySource.Type;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.AbstractRunnable;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.env.ShardLockObtainFailedException;
import org.elasticsearch.gateway.GatewayService;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexComponent;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.seqno.GlobalCheckpointTracker;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardRelocatedException;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.index.shard.PrimaryReplicaSyncer.ResyncTask;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.shard.ShardNotFoundException;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.recovery.PeerRecoveryTargetService;
import org.elasticsearch.indices.recovery.RecoveryFailedException;
import org.elasticsearch.indices.recovery.RecoveryState;
import org.elasticsearch.search.SearchService;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.elasticsearch.indices.cluster.IndicesClusterStateService.AllocatedIndices.IndexRemovalReason.CLOSED;
import static org.elasticsearch.indices.cluster.IndicesClusterStateService.AllocatedIndices.IndexRemovalReason.DELETED;
import static org.elasticsearch.indices.cluster.IndicesClusterStateService.AllocatedIndices.IndexRemovalReason.FAILURE;

public class IndicesClusterStateService extends AbstractLifecycleComponent implements ClusterStateApplier {

    final AllocatedIndices<? extends Shard, ? extends AllocatedIndex<? extends Shard>> indicesService;
    private final ClusterService clusterService;
    private final ThreadPool threadPool;
    //private final PeerRecoveryTargetService recoveryTargetService;
    //private final ShardStateAction shardStateAction;
    private final NodeMappingRefreshAction nodeMappingRefreshAction;

    private static final ShardStateAction.Listener SHARD_STATE_ACTION_LISTENER = new ShardStateAction.Listener() {
    };

    // a list of shards that failed during recovery
    // we keep track of these shards in order to prevent repeated recovery of these shards on each cluster state update
    final ConcurrentMap<ShardId, ShardRouting> failedShardsCache = ConcurrentCollections.newConcurrentMap();
    //private final RepositoriesService repositoriesService;

    private final FailedShardHandler failedShardHandler = new FailedShardHandler();

    private final boolean sendRefreshMapping;
    private final List<IndexEventListener> buildInIndexListener;
    //private final PrimaryReplicaSyncer primaryReplicaSyncer;
    //private final Consumer<ShardId> globalCheckpointSyncer;

    @Inject
    public IndicesClusterStateService(Settings settings,
                                      IndicesService indicesService,
                                      ClusterService clusterService,
                                      ThreadPool threadPool,
                                      //PeerRecoveryTargetService recoveryTargetService,
                                      //ShardStateAction shardStateAction,
                                      NodeMappingRefreshAction nodeMappingRefreshAction,
                                      //RepositoriesService repositoriesService,
                                      SearchService searchService,
                                      //SyncedFlushService syncedFlushService,
                                      CassandraShardStateListener cassandraShardStateListener
                                      //PeerRecoverySourceService peerRecoverySourceService,
                                      //SnapshotShardsService snapshotShardsService,
                                      //PrimaryReplicaSyncer primaryReplicaSyncer,
                                      //GlobalCheckpointSyncAction globalCheckpointSyncAction
                                      ) {
        this(settings, (AllocatedIndices<? extends Shard, ? extends AllocatedIndex<? extends Shard>>) indicesService,
                clusterService, threadPool,
                nodeMappingRefreshAction, searchService, cassandraShardStateListener);
    }

    // for tests
    IndicesClusterStateService(Settings settings,
                               AllocatedIndices<? extends Shard, ? extends AllocatedIndex<? extends Shard>> indicesService,
                               ClusterService clusterService,
                               ThreadPool threadPool,
                               //PeerRecoveryTargetService recoveryTargetService,
                               //ShardStateAction shardStateAction,
                               NodeMappingRefreshAction nodeMappingRefreshAction,
                               //RepositoriesService repositoriesService,
                               SearchService searchService,
                               //SyncedFlushService syncedFlushService,
                               CassandraShardStateListener cassandraShardStateListener) {
        super(settings);
        this.buildInIndexListener =
                Arrays.asList(
                        searchService,
                        //syncedFlushService,
                        cassandraShardStateListener);
        this.indicesService = indicesService;
        this.clusterService = clusterService;
        this.threadPool = threadPool;
        //this.recoveryTargetService = recoveryTargetService;
        //this.shardStateAction = shardStateAction;
        this.nodeMappingRefreshAction = nodeMappingRefreshAction;
        //this.repositoriesService = repositoriesService;
        //this.primaryReplicaSyncer = primaryReplicaSyncer;
        //this.globalCheckpointSyncer = globalCheckpointSyncer;
        this.sendRefreshMapping = this.settings.getAsBoolean("indices.cluster.send_refresh_mapping", true);
    }

    @Override
    protected void doStart() {
        // Doesn't make sense to manage shards on non-master and non-data nodes
        if (DiscoveryNode.isDataNode(settings) || DiscoveryNode.isMasterNode(settings)) {
            clusterService.addHighPriorityApplier(this);
        }
    }

    @Override
    protected void doStop() {
        if (DiscoveryNode.isDataNode(settings) || DiscoveryNode.isMasterNode(settings)) {
            clusterService.removeApplier(this);
        }
    }

    @Override
    protected void doClose() {
    }

    @Override
    public synchronized void applyClusterState(final ClusterChangedEvent event) {
        if (!lifecycle.started()) {
            return;
        }

        final ClusterState state = event.state();

        // we need to clean the shards and indices we have on this node, since we
        // are going to recover them again once state persistence is disabled (no master / not recovered)
        // TODO: feels hacky, a block disables state persistence, and then we clean the allocated shards, maybe another flag in blocks?
        /*
        if (state.blocks().disableStatePersistence()) {
            for (AllocatedIndex<? extends Shard> indexService : indicesService) {
                indicesService.removeIndex(indexService.index(), NO_LONGER_ASSIGNED,
                    "cleaning index (disabled block persistence)"); // also cleans shards
            }
            return;
        }
        */
        //updateFailedShardsCache(state);

        deleteIndices(event); // also deletes shards of deleted indices

        removeClosedIndices(event); // also removes shards of removed indices

        //failMissingShards(state);

        //removeShards(state);   // removes any local shards that doesn't match what the master expects

        updateIndices(event); // can also fail shards, but these are then guaranteed to be in failedShardsCache

        //createIndices(state);
        applyNewIndices(event);

        createOrUpdateShards(state);
    }

    /**
     * Deletes indices (with shard data).
     *
     * @param event cluster change event
     */
    private void deleteIndices(final ClusterChangedEvent event) {
        final ClusterState previousState = event.previousState();
        final ClusterState state = event.state();
        final String localNodeId = state.nodes().getLocalNodeId();
        assert localNodeId != null;

        for (Index index : event.indicesDeleted()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[{}] cleaning index, no longer part of the metadata", index);
            }
            AllocatedIndex<? extends Shard> indexService = indicesService.indexService(index);
            final IndexSettings indexSettings;
            if (indexService != null) {
                indexSettings = indexService.getIndexSettings();
                indicesService.removeIndex(index, DELETED, "index no longer part of the metadata");
            } else if (previousState.metaData().hasIndex(index.getName())) {
                // The deleted index was part of the previous cluster state, but not loaded on the local node
                final IndexMetaData metaData = previousState.metaData().index(index);
                indexSettings = new IndexSettings(metaData, settings);
                indicesService.deleteUnassignedIndex("deleted index was not assigned to local node", metaData, state);
            } else {
                // The previous cluster state's metadata also does not contain the index,
                // which is what happens on node startup when an index was deleted while the
                // node was not part of the cluster.  In this case, try reading the index
                // metadata from disk.  If its not there, there is nothing to delete.
                // First, though, verify the precondition for applying this case by
                // asserting that the previous cluster state is not initialized/recovered.
                assert previousState.blocks().hasGlobalBlock(GatewayService.STATE_NOT_RECOVERED_BLOCK);
                final IndexMetaData metaData = indicesService.verifyIndexIsDeleted(index, event.state());
                if (metaData != null) {
                    indexSettings = new IndexSettings(metaData, settings);
                } else {
                    indexSettings = null;
                }
            }
            if (indexSettings != null) {
                threadPool.generic().execute(new AbstractRunnable() {
                    @Override
                    public void onFailure(Exception e) {
                        logger.warn(
                            (Supplier<?>) () -> new ParameterizedMessage("[{}] failed to complete pending deletion for index", index), e);
                    }

                    @Override
                    protected void doRun() throws Exception {
                        try {
                            // we are waiting until we can lock the index / all shards on the node and then we ack the delete of the store
                            // to the master. If we can't acquire the locks here immediately there might be a shard of this index still
                            // holding on to the lock due to a "currently canceled recovery" or so. The shard will delete itself BEFORE the
                            // lock is released so it's guaranteed to be deleted by the time we get the lock
                            indicesService.processPendingDeletes(index, indexSettings, new TimeValue(30, TimeUnit.MINUTES));
                        } catch (LockObtainFailedException exc) {
                            logger.warn("[{}] failed to lock all shards for index - timed out after 30 seconds", index);
                        } catch (InterruptedException e) {
                            logger.warn("[{}] failed to lock all shards for index - interrupted", index);
                        }
                    }
                });
            }
        }
    }

    /**
     * Removes indices that have no shards allocated to this node. This does not delete the shard data as we wait for enough
     * shard copies to exist in the cluster before deleting shard data (triggered by {@link org.elasticsearch.indices.store.IndicesStore}).
     *
     * @param event the cluster changed event
     */
    private void removeClosedIndices(final ClusterChangedEvent event) {
        final ClusterState state = event.state();
        final String localNodeId = state.nodes().getLocalNodeId();
        assert localNodeId != null;

        for (AllocatedIndex<? extends Shard> indexService : indicesService) {
            Index index = indexService.index();
            if (state.metaData().index(index) == null || state.metaData().index(index).getState() == State.CLOSE) {
                final AllocatedIndices.IndexRemovalReason reason = CLOSED;
                logger.debug("{} removing index, [{}]", index, reason);
                indicesService.removeIndex(index, reason, "removing index (no shards allocated)");
            }
        }
    }

    /**
     * Removes shards that are currently loaded by indicesService but have disappeared from the routing table of the current node.
     * This method does not delete the shard data.
     *
     * @param state new cluster state
     */
    private void removeShards(final ClusterState state) {
        final String localNodeId = state.nodes().getLocalNodeId();
        assert localNodeId != null;

        // remove shards based on routing nodes (no deletion of data)
        RoutingNode localRoutingNode = state.getRoutingNodes().node(localNodeId);
        for (AllocatedIndex<? extends Shard> indexService : indicesService) {
            for (Shard shard : indexService) {
                ShardRouting currentRoutingEntry = shard.routingEntry();
                ShardId shardId = currentRoutingEntry.shardId();
                ShardRouting newShardRouting = localRoutingNode == null ? null : localRoutingNode.getByShardId(shardId);
                if (newShardRouting == null) {
                    // we can just remove the shard without cleaning it locally, since we will clean it in IndicesStore
                    // once all shards are allocated
                    logger.debug("{} removing shard (not allocated)", shardId);
                    indexService.removeShard(shardId.id(), "removing shard (not allocated)");
                } else if (newShardRouting.isSameAllocation(currentRoutingEntry) == false) {
                    logger.debug("{} removing shard (stale allocation id, stale {}, new {})", shardId,
                        currentRoutingEntry, newShardRouting);
                    indexService.removeShard(shardId.id(), "removing shard (stale copy)");
                } else if (newShardRouting.initializing() && currentRoutingEntry.active()) {
                    // this can happen if the node was isolated/gc-ed, rejoins the cluster and a new shard with the same allocation id
                    // is assigned to it. Batch cluster state processing or if shard fetching completes before the node gets a new cluster
                    // state may result in a new shard being initialized while having the same allocation id as the currently started shard.
                    logger.debug("{} removing shard (not active, current {}, new {})", shardId, currentRoutingEntry, newShardRouting);
                    indexService.removeShard(shardId.id(), "removing shard (stale copy)");
                }
            }
        }
    }

    /**
     * In Elassandra, new index creation involve a new INITIALIZING shardRouting attached to the index and recovered.
     * @param event
     */
    private void applyNewIndices(final ClusterChangedEvent event) {
        ClusterState state = event.state();
        for (IndexMetaData indexMetaData : event.state().metaData()) {
            AllocatedIndex<? extends Shard> indexService = indicesService.indexService(indexMetaData.getIndex());
            if (indexService == null && indexMetaData.getState() == State.OPEN) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[{}] creating index", indexMetaData.getIndex());
                }
                try {
                    indexService = indicesService.createIndex(indexMetaData, buildInIndexListener);
                    indexService.updateMapping(indexMetaData);
                } catch (Throwable e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("[{}][{}] failed to create index", indexMetaData.getIndex(),  indexMetaData.getIndexUUID(), e);
                    }
                }
            }

            DiscoveryNodes nodes = event.state().nodes();
            RoutingTable routingTable = event.state().routingTable();

            if (indexService != null && indexService.getShardOrNull(0) == null && indexMetaData.getState() == State.OPEN) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}][{}] creating new shard INITIALIZING", indexMetaData.getIndex(), 0);
                    }
                    ShardRouting shardRouting = new ShardRouting(new ShardId(indexMetaData.getIndex(), 0), clusterService.localNode().getId(), null, true,
                            ShardRoutingState.INITIALIZING,
                            RecoverySource.StoreRecoverySource.EMPTY_STORE_INSTANCE,
                            IndexRoutingTable.UNASSIGNED_INFO_INDEX_CREATED,
                            ShardRouting.DUMMY_ALLOCATION_ID, 0,
                            AbstractSearchStrategy.EMPTY_RANGE_TOKEN_LIST);
                    createShard(nodes, routingTable, shardRouting, state);

                    //IndexShard indexShard = indexService.createShard(shardRouting);
                    //indexShard.shardRouting(shardRouting);
                    //indexShard.addFailedEngineListener(failedEngineHandler);
                    //this.clusterService.recoverShard(indexMetaData.getIndex());  // post-applied shard recovery by CassandraIndicesClusterStateService
                } catch (Throwable e) {
                    logger.error("Unexpected error", e);
                    final String failShardReason;
                    if (indexService == null) {
                        failShardReason = "failed to create index";
                    } else {
                        failShardReason = "failed to update mapping for index";
                        indicesService.removeIndex(indexMetaData.getIndex(), FAILURE, "removing index (mapping update failed)");
                    }
                }
            }
        }
    }

    private void updateIndices(ClusterChangedEvent event) {
        if (!event.metaDataChanged()) {
            return;
        }
        final ClusterState state = event.state();
        for (AllocatedIndex<? extends Shard> indexService : indicesService) {
            final Index index = indexService.index();
            final IndexMetaData currentIndexMetaData = indexService.getIndexSettings().getIndexMetaData();
            final IndexMetaData newIndexMetaData = state.metaData().index(index);
            assert newIndexMetaData != null : "index " + index + " should have been removed by deleteIndices";
            if (ClusterChangedEvent.indexMetaDataChanged(currentIndexMetaData, newIndexMetaData)) {
                indexService.updateMetaData(newIndexMetaData);
                try {
                    indexService.updateMapping(newIndexMetaData);
                } catch (Exception e) {
                    indicesService.removeIndex(indexService.index(), FAILURE, "removing index (mapping update failed)");
                }
            }
        }
    }

    private void createOrUpdateShards(final ClusterState state) {
        RoutingNode localRoutingNode = state.getRoutingNodes().node(state.nodes().getLocalNodeId());
        if (localRoutingNode == null) {
            return;
        }

        DiscoveryNodes nodes = state.nodes();
        RoutingTable routingTable = state.routingTable();

        for (final ShardRouting shardRouting : localRoutingNode) {
            ShardId shardId = shardRouting.shardId();
            if (shardId.getId() == 0) {
                AllocatedIndex<? extends Shard> indexService = indicesService.indexService(shardId.getIndex());
                assert indexService != null : "index " + shardId.getIndex() + " should have been created by createIndices";
                Shard shard = indexService.getShardOrNull(shardId.id());
                if (shard == null) {
                    assert shardRouting.initializing() : shardRouting + " should have been removed by failMissingShards";
                    createShard(nodes, routingTable, shardRouting, state);
                } else {
                    updateShard(nodes, shardRouting, shard, routingTable, state);
                }
            }
        }
    }

    private Shard createShard(DiscoveryNodes nodes, RoutingTable routingTable, ShardRouting shardRouting, ClusterState state) {
        assert shardRouting.initializing() : "only allow shard creation for initializing shard but was " + shardRouting;

        DiscoveryNode sourceNode = null;
        if (shardRouting.recoverySource().getType() == Type.PEER)  {
            sourceNode = findSourceNodeForPeerRecovery(logger, routingTable, nodes, shardRouting);
            if (sourceNode == null) {
                logger.trace("ignoring initializing shard {} - no source node can be found.", shardRouting.shardId());
                return null;
            }
        }

        try {
            logger.debug("{} creating shard", shardRouting.shardId());
            RecoveryState recoveryState = new RecoveryState(shardRouting, nodes.getLocalNode(), sourceNode);
            return indicesService.createShard(shardRouting, recoveryState, new RecoveryListener(shardRouting),
               failedShardHandler);
        } catch (Exception e) {
            failAndRemoveShard(shardRouting, true, "failed to create shard", e, state);
            return null;
        }
    }

    private void updateShard(DiscoveryNodes nodes, ShardRouting shardRouting, Shard shard, RoutingTable routingTable,
                             ClusterState clusterState) {
        final ShardRouting currentRoutingEntry = shard.routingEntry();
        assert currentRoutingEntry.isSameAllocation(shardRouting) :
            "local shard has a different allocation id but wasn't cleaning by removeShards. "
                + "cluster state: " + shardRouting + " local: " + currentRoutingEntry;

        try {
            shard.moveToStart();
        } catch (Exception e) {
            failAndRemoveShard(shardRouting, true, "failed updating shard routing entry", e, clusterState);
            return;
        }

        final IndexShardState state = shard.state();
        if (shardRouting.initializing() && (state == IndexShardState.STARTED || state == IndexShardState.POST_RECOVERY)) {
            // the master thinks we are initializing, but we are already started or on POST_RECOVERY and waiting
            // for master to confirm a shard started message (either master failover, or a cluster event before
            // we managed to tell the master we started), mark us as started
            if (logger.isTraceEnabled()) {
                logger.trace("{} master marked shard as initializing, but shard has state [{}], resending shard started to {}",
                    shardRouting.shardId(), state, nodes.getMasterNode());
            }
        }
    }

    /**
     * Finds the routing source node for peer recovery, return null if its not found. Note, this method expects the shard
     * routing to *require* peer recovery, use {@link ShardRouting#recoverySource()} to check if its needed or not.
     */
    private static DiscoveryNode findSourceNodeForPeerRecovery(Logger logger, RoutingTable routingTable, DiscoveryNodes nodes,
                                                               ShardRouting shardRouting) {
        DiscoveryNode sourceNode = null;
        if (!shardRouting.primary()) {
            ShardRouting primary = routingTable.shardRoutingTable(shardRouting.shardId()).primaryShard();
            // only recover from started primary, if we can't find one, we will do it next round
            if (primary.active()) {
                sourceNode = nodes.get(primary.currentNodeId());
                if (sourceNode == null) {
                    logger.trace("can't find replica source node because primary shard {} is assigned to an unknown node.", primary);
                }
            } else {
                logger.trace("can't find replica source node because primary shard {} is not active.", primary);
            }
        } else if (shardRouting.relocatingNodeId() != null) {
            sourceNode = nodes.get(shardRouting.relocatingNodeId());
            if (sourceNode == null) {
                logger.trace("can't find relocation source node for shard {} because it is assigned to an unknown node [{}].",
                    shardRouting.shardId(), shardRouting.relocatingNodeId());
            }
        } else {
            throw new IllegalStateException("trying to find source node for peer recovery when routing state means no peer recovery: " +
                shardRouting);
        }
        return sourceNode;
    }

    private class RecoveryListener implements PeerRecoveryTargetService.RecoveryListener {

        private final ShardRouting shardRouting;

        private RecoveryListener(ShardRouting shardRouting) {
            this.shardRouting = shardRouting;
        }

        @Override
        public void onRecoveryDone(RecoveryState state) {
            logger.info("shard [{}][{}] recovered source=[{}] ", state.getShardId().getIndexName(), state.getShardId().getId() ,state.getRecoverySource());

            // add for elassandra
            try {
               IndexService indexService = clusterService.indexServiceSafe(shardRouting.shardId().getIndex());
               Shard shard = indexService.getShardOrNull(0);
               if (shard != null)
                   shard.moveToStart();
            } catch (Exception e) {
                logger.error("Shard [{}] routing updated failed",e, shardRouting.shardId());
            }
        }

        @Override
        public void onRecoveryFailure(RecoveryState state, RecoveryFailedException e, boolean sendShardFailure) {
            handleRecoveryFailure(shardRouting, sendShardFailure, e);
        }
    }

    private synchronized void handleRecoveryFailure(ShardRouting shardRouting, boolean sendShardFailure, Exception failure) {
        failAndRemoveShard(shardRouting, sendShardFailure, "failed recovery", failure, clusterService.state());
    }

    private void failAndRemoveShard(ShardRouting shardRouting, boolean sendShardFailure, String message, @Nullable Exception failure,
                                    ClusterState state) {
        try {
            AllocatedIndex<? extends Shard> indexService = indicesService.indexService(shardRouting.shardId().getIndex());
            if (indexService != null) {
                indexService.removeShard(shardRouting.shardId().id(), message);
            }
        } catch (ShardNotFoundException e) {
            // the node got closed on us, ignore it
        } catch (Exception inner) {
            inner.addSuppressed(failure);
            logger.warn(
                (Supplier<?>) () -> new ParameterizedMessage(
                    "[{}][{}] failed to remove shard after failure ([{}])",
                    shardRouting.getIndexName(),
                    shardRouting.getId(),
                    message),
                inner);
        }
    }

    private class FailedShardHandler implements Consumer<IndexShard.ShardFailure> {
        @Override
        public void accept(final IndexShard.ShardFailure shardFailure) {
            final ShardRouting shardRouting = shardFailure.routing;
            logger.error("Shard recovery failure reson=[{}]", shardFailure.reason);
        }
    }

    public interface Shard {

        /**
         * Returns the shard id of this shard.
         */
        ShardId shardId();

        /**
         * Returns the latest cluster routing entry received with this shard.
         */
        ShardRouting routingEntry();

        void moveToStart();

        /**
         * Returns the latest internal shard state.
         */
        IndexShardState state();

        /**
         * Returns the recovery state associated with this shard.
         */
        RecoveryState recoveryState();

        /**
         * Updates the shard state based on an incoming cluster state:
         * - Updates and persists the new routing value.
         * - Updates the primary term if this shard is a primary.
         * - Updates the allocation ids that are tracked by the shard if it is a primary.
         *   See {@link GlobalCheckpointTracker#updateFromMaster(long, Set, IndexShardRoutingTable, Set)} for details.
         *
         * @param shardRouting                the new routing entry
         * @param primaryTerm                 the new primary term
         * @param primaryReplicaSyncer        the primary-replica resync action to trigger when a term is increased on a primary
         * @param applyingClusterStateVersion the cluster state version being applied when updating the allocation IDs from the master
         * @param inSyncAllocationIds         the allocation ids of the currently in-sync shard copies
         * @param routingTable                the shard routing table
         * @throws IndexShardRelocatedException if shard is marked as relocated and relocation aborted
         * @throws IOException                  if shard state could not be persisted
         */
        void updateShardState(ShardRouting shardRouting,
                              long primaryTerm,
                              BiConsumer<IndexShard, ActionListener<ResyncTask>> primaryReplicaSyncer,
                              long applyingClusterStateVersion,
                              Set<String> inSyncAllocationIds,
                              IndexShardRoutingTable routingTable,
                              Set<String> pre60AllocationIds) throws IOException;
    }

    public interface AllocatedIndex<T extends Shard> extends Iterable<T>, IndexComponent {

        /**
         * Returns the index settings of this index.
         */
        IndexSettings getIndexSettings();

        /**
         * Updates the meta data of this index. Changes become visible through {@link #getIndexSettings()}
         */
        void updateMetaData(IndexMetaData indexMetaData);

        /**
         * Checks if index requires refresh from master.
         */
        boolean updateMapping(IndexMetaData indexMetaData) throws IOException;

        public MapperService mapperService();

        /**
         * Returns shard with given id.
         */
        @Nullable T getShardOrNull(int shardId);

        /**
         * Removes shard with given id.
         */
        void removeShard(int shardId, String message);
    }

    public interface AllocatedIndices<T extends Shard, U extends AllocatedIndex<T>> extends Iterable<U> {

        /**
         * Creates a new {@link IndexService} for the given metadata.
         *
         * @param indexMetaData          the index metadata to create the index for
         * @param builtInIndexListener   a list of built-in lifecycle {@link IndexEventListener} that should should be used along side with
         *                               the per-index listeners
         * @throws ResourceAlreadyExistsException if the index already exists.
         */
        U createIndex(IndexMetaData indexMetaData,
                      List<IndexEventListener> builtInIndexListener) throws IOException;

        /**
         * Verify that the contents on disk for the given index is deleted; if not, delete the contents.
         * This method assumes that an index is already deleted in the cluster state and/or explicitly
         * through index tombstones.
         * @param index {@code Index} to make sure its deleted from disk
         * @param clusterState {@code ClusterState} to ensure the index is not part of it
         * @return IndexMetaData for the index loaded from disk
         */
        IndexMetaData verifyIndexIsDeleted(Index index, ClusterState clusterState);


        /**
         * Deletes an index that is not assigned to this node. This method cleans up all disk folders relating to the index
         * but does not deal with in-memory structures. For those call {@link #removeIndex(Index, IndexRemovalReason, String)}
         */
        void deleteUnassignedIndex(String reason, IndexMetaData metaData, ClusterState clusterState);

        /**
         * Removes the given index from this service and releases all associated resources. Persistent parts of the index
         * like the shards files, state and transaction logs are kept around in the case of a disaster recovery.
         * @param index the index to remove
         * @param reason the reason to remove the index
         * @param extraInfo extra information that will be used for logging and reporting
         */
        void removeIndex(Index index, IndexRemovalReason reason, String extraInfo);

        /**
         * Returns an IndexService for the specified index if exists otherwise returns <code>null</code>.
         */
        @Nullable U indexService(Index index);

        /**
         * Creates shard for the specified shard routing and starts recovery,
         */
        T createShard(ShardRouting shardRouting, RecoveryState recoveryState,
                      PeerRecoveryTargetService.RecoveryListener recoveryListener,
                      Consumer<IndexShard.ShardFailure> onShardFailure) throws IOException;

        /**
         * Returns shard for the specified id if it exists otherwise returns <code>null</code>.
         */
        default T getShardOrNull(ShardId shardId) {
            U indexRef = indexService(shardId.getIndex());
            if (indexRef != null) {
                return indexRef.getShardOrNull(shardId.id());
            }
            return null;
        }

        void processPendingDeletes(Index index, IndexSettings indexSettings, TimeValue timeValue)
            throws IOException, InterruptedException, ShardLockObtainFailedException;

        public enum IndexRemovalReason {
            /**
             * Shard of this index were previously assigned to this node but all shards have been relocated.
             * The index should be removed and all associated resources released. Persistent parts of the index
             * like the shards files, state and transaction logs are kept around in the case of a disaster recovery.
             */
            NO_LONGER_ASSIGNED,
            /**
             * The index is deleted. Persistent parts of the index  like the shards files, state and transaction logs are removed once
             * all resources are released.
             */
            DELETED,

            /**
             * The index have been closed. The index should be removed and all associated resources released. Persistent parts of the index
             * like the shards files, state and transaction logs are kept around in the case of a disaster recovery.
             */
            CLOSED,

            /**
             * Something around index management has failed and the index should be removed.
             * Persistent parts of the index like the shards files, state and transaction logs are kept around in the
             * case of a disaster recovery.
             */
            FAILURE
        }
    }
}
