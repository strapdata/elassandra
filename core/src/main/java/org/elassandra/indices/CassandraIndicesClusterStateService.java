/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
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
package org.elassandra.indices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.gateway.CassandraGatewayService;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData.State;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexShardAlreadyExistsException;
import org.elasticsearch.index.aliases.IndexAliasesService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.MapperService.MergeReason;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardRecoveryException;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.shard.ShardNotFoundException;
import org.elasticsearch.index.shard.StoreRecoveryService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;

import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * Pre-applied cluster state listener
 */
public class CassandraIndicesClusterStateService extends AbstractLifecycleComponent<CassandraIndicesClusterStateService> implements ClusterStateListener {

    private final IndicesService indicesService;
    private final ClusterService clusterService;
    private final ThreadPool threadPool;


    // a map of mappings type we have seen per index due to cluster state
    // we need this so we won't remove types automatically created as part of the indexing process
    private final ConcurrentMap<Tuple<String, String>, Boolean> seenMappings = ConcurrentCollections.newConcurrentMap();

    private final Object mutex = new Object();
    
    private final FailedEngineHandler failedEngineHandler = new FailedEngineHandler();
    
    @Inject
    public CassandraIndicesClusterStateService(Settings settings, IndicesService indicesService, 
            ClusterService clusterService, ThreadPool threadPool) {
        super(settings);
        this.indicesService = indicesService;
        this.clusterService = clusterService;
        this.threadPool = threadPool;
    }

    @Override
    protected void doStart() {
        clusterService.addFirst(this);
    }

    @Override
    protected void doStop() {
        clusterService.remove(this);
    }

    @Override
    protected void doClose() {
    }

    @Override
    public void clusterChanged(final ClusterChangedEvent event) {
        if (!indicesService.changesAllowed()) {
            return;
        }

        if (!lifecycle.started()) {
            return;
        }

        synchronized (mutex) {
            // we need to clean the shards and indices we have on this node, since we
            // are going to recover them again once state persistence is disabled (no master / not recovered)
            // TODO: this feels a bit hacky here, a block disables state persistence, and then we clean the allocated shards, maybe another flag in blocks?
            if (event.state().blocks().disableStatePersistence() && !event.state().blocks().hasGlobalBlock(CassandraGatewayService.NO_CASSANDRA_RING_BLOCK.id())) {
                for (IndexService indexService : indicesService) {
                    String index = indexService.index().getName();
                    for (Integer shardId : indexService.shardIds()) {
                        logger.debug("[{}][{}] removing shard (disabled block persistence)", index, shardId);
                        try {
                            indexService.removeShard(shardId, "removing shard (disabled block persistence)");
                        } catch (Throwable e) {
                            logger.warn("[{}] failed to remove shard (disabled block persistence)", e, index);
                        }
                    }
                    removeIndex(index, "cleaning index (disabled block persistence)");
                }
                return;
            }


            applyDeletedIndices(event);
            applyNewIndices(event);
            applyMappings(event);
            applyAliases(event);
            
            applyCleanedIndices(event);
            applySettings(event);
        }
    }
    
    private void applyCleanedIndices(final ClusterChangedEvent event) {
        // handle closed indices, since they are not allocated on a node once they are closed
        // so applyDeletedIndices might not take them into account
        for (IndexService indexService : indicesService) {
            String index = indexService.index().getName();
            IndexMetaData indexMetaData = event.state().metaData().index(index);
            if (indexMetaData != null && indexMetaData.getState() == IndexMetaData.State.CLOSE) {
                try {
                    logger.debug("[{}] removing shards (index is closed)", index);
                    indexService.close("removing shard (index is closed)", false);
                } catch (Throwable e) {
                    logger.warn("[{}] failed to remove shard (index is closed)", e, index);
                }
            }
        }
        for (IndexService indexService : indicesService) {
            String index = indexService.index().getName();
            if (indexService.shardIds().isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[{}] cleaning index (no shards allocated)", index);
                }
                // clean the index
                removeIndex(index, "removing index (no shards allocated)");
            }
        }
    }

    private void applyDeletedIndices(final ClusterChangedEvent event) {
        final ClusterState previousState = event.previousState();
        final String localNodeId = event.state().nodes().localNodeId();
        assert localNodeId != null;

        for (IndexService indexService : indicesService) {
            IndexMetaData indexMetaData = event.state().metaData().index(indexService.index().name());
            if (indexMetaData != null) {
                if (!indexMetaData.isSameUUID(indexService.indexUUID())) {
                    logger.debug("[{}] mismatch on index UUIDs between cluster state and local state, cleaning the index so it will be recreated", indexMetaData.getIndex());
                    deleteIndex(indexMetaData.getIndex(), "mismatch on index UUIDs between cluster state and local state, cleaning the index so it will be recreated");
                }
            }
        }

        for (String index : event.indicesDeleted()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[{}] cleaning index, no longer part of the metadata", index);
            }
            final Settings indexSettings;
            final IndexService idxService = indicesService.indexService(index);
            if (idxService != null) {
                indexSettings = idxService.indexSettings();
                deleteIndex(index, "index no longer part of the metadata");
            } else {
                final IndexMetaData metaData = previousState.metaData().index(index);
                assert metaData != null;
                indexSettings = metaData.getSettings();
                indicesService.deleteClosedIndex("closed index no longer part of the metadata", metaData, event.state());
            }
        }


    }



    private boolean applyNewIndices(final ClusterChangedEvent event) {
        boolean newShards = false;
        for (IndexMetaData indexMetaData : event.state().metaData()) {
            IndexService indexService = indicesService.indexService(indexMetaData.getIndex());
            if (indexService == null && indexMetaData.getState() == State.OPEN) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[{}] creating index", indexMetaData.getIndex());
                }
                try {
                    indexService = indicesService.createIndex(indexMetaData.getIndex(), indexMetaData.getSettings(), event.state().nodes().localNode().id());
                } catch (Throwable e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("[{}][{}] failed to create index", indexMetaData.getIndex(),  indexMetaData.getIndexUUID());
                    }
                }
            } 
            if (indexService != null && indexService.shard(0) == null && indexMetaData.getState() == State.OPEN) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}][{}] creating shard", indexMetaData.getIndex(), 0);
                    }
                    ShardRouting shardRouting = new ShardRouting(indexMetaData.getIndex(), 0, clusterService.localNode().id(), true, 
                            ShardRoutingState.INITIALIZING, 
                            event.state().metaData().version(), 
                            IndexRoutingTable.UNASSIGNED_INFO_INDEX_CREATED, 
                            AbstractSearchStrategy.EMPTY_RANGE_TOKEN_LIST);
                    IndexShard indexShard = indexService.createShard(shardRouting);
                    indexShard.shardRouting(shardRouting);
                    indexShard.addFailedEngineListener(failedEngineHandler);
                    newShards = true;
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("[{}][{}] creating shard", shardRouting.index(), shardRouting.shardId().getId());
                    }
                    
                    // try to recover if index was existing but has no shards.
                    indexShard.recoverFromStore(shardRouting, new StoreRecoveryService.RecoveryListener() {
                        @Override
                        public void onRecoveryDone() {
                            logger.debug("[{}][{}] recovery done, shard state={}", shardRouting.index(),0, indexShard.state());
                            indexShard.moveToStart();
                        }

                        @Override
                        public void onIgnoreRecovery(String reason) {
                            logger.warn("[{}][{}] recovery ignored", shardRouting.index(),0);
                        }

                        @Override
                        public void onRecoveryFailed(IndexShardRecoveryException e) {
                            logger.warn("[{}][{}] recovery failed", e, shardRouting.index(),0);
                        }
                    });
                    
                } catch (IndexShardAlreadyExistsException e) {
                    // ignore this, the method call can happen several times
                } catch (Throwable e) {
                    logger.error("Unexpected error", e);
                    failAndRemoveShard(indexService, true, "failed to create shard", e);
                }
            }
            
        }
        return newShards;
    }

     
    private void applySettings(ClusterChangedEvent event) {
        if (!event.metaDataChanged()) {
            return;
        }
        for (IndexMetaData indexMetaData : event.state().metaData()) {
            if (!indicesService.hasIndex(indexMetaData.getIndex())) {
                // we only create / update here
                continue;
            }
            // if the index meta data didn't change, no need check for refreshed settings
            if (!event.indexMetaDataChanged(indexMetaData)) {
                continue;
            }
            String index = indexMetaData.getIndex();
            IndexService indexService = indicesService.indexService(index);
            if (indexService == null) {
                // already deleted on us, ignore it
                continue;
            }
            IndexSettingsService indexSettingsService = indexService.injector().getInstance(IndexSettingsService.class);
            indexSettingsService.refreshSettings(indexMetaData.getSettings());
        }
    }


    private void applyMappings(ClusterChangedEvent event) {
        // go over and update mappings
        for (IndexMetaData indexMetaData : event.state().metaData()) {
            if (!indicesService.hasIndex(indexMetaData.getIndex())) {
                // we only create / update here
                continue;
            }
            List<String> typesToRefresh = new ArrayList<>();
            String index = indexMetaData.getIndex();
            IndexService indexService = indicesService.indexService(index);
            if (indexService == null) {
                // got deleted on us, ignore (closing the node)
                return;
            }
            try {
                MapperService mapperService = indexService.mapperService();
                // first, go over and update the _default_ mapping (if exists)
                if (indexMetaData.getMappings().containsKey(MapperService.DEFAULT_MAPPING)) {
                    boolean requireRefresh = processMapping(index, mapperService, MapperService.DEFAULT_MAPPING, indexMetaData.mapping(MapperService.DEFAULT_MAPPING).source());
                    if (requireRefresh) {
                        typesToRefresh.add(MapperService.DEFAULT_MAPPING);
                    }
                }

                // go over and add the relevant mappings (or update them)
                for (ObjectCursor<MappingMetaData> cursor : indexMetaData.getMappings().values()) {
                    MappingMetaData mappingMd = cursor.value;
                    String mappingType = mappingMd.type();
                    CompressedXContent mappingSource = mappingMd.source();
                    if (mappingType.equals(MapperService.DEFAULT_MAPPING)) { // we processed _default_ first
                        continue;
                    }
                    boolean requireRefresh = processMapping(index, mapperService, mappingType, mappingSource);
                    if (requireRefresh) {
                        typesToRefresh.add(mappingType);
                    }
                }
            } catch (Throwable t) {
                // if we failed the mappings anywhere, we need to fail the shards for this index, note, we safeguard
                // by creating the processing the mappings on the master, or on the node the mapping was introduced on,
                // so this failure typically means wrong node level configuration or something similar
                failAndRemoveShard(indexService, true, "failed to update mappings", t);
            }
        }
    }

    private boolean processMapping(String index, MapperService mapperService, String mappingType, CompressedXContent mappingSource) throws Throwable {
        if (!seenMappings.containsKey(new Tuple<>(index, mappingType))) {
            seenMappings.put(new Tuple<>(index, mappingType), true);
        }

        // refresh mapping can happen for 2 reasons. The first is less urgent, and happens when the mapping on this
        // node is ahead of what there is in the cluster state (yet an update-mapping has been sent to it already,
        // it just hasn't been processed yet and published). Eventually, the mappings will converge, and the refresh
        // mapping sent is more of a safe keeping (assuming the update mapping failed to reach the master, ...)
        // the second case is where the parsing/merging of the mapping from the metadata doesn't result in the same
        // mapping, in this case, we send to the master to refresh its own version of the mappings (to conform with the
        // merge version of it, which it does when refreshing the mappings), and warn log it.
        boolean requiresRefresh = false;
        try {
            if (!mapperService.hasMapping(mappingType)) {
                if (logger.isDebugEnabled() && mappingSource.compressed().length < 512) {
                    logger.debug("[{}] adding mapping [{}], source [{}]", index, mappingType, mappingSource.string());
                } else if (logger.isTraceEnabled()) {
                    logger.trace("[{}] adding mapping [{}], source [{}]", index, mappingType, mappingSource.string());
                } else {
                    logger.debug("[{}] adding mapping [{}] (source suppressed due to length, use TRACE level if needed)", index, mappingType);
                }
                // we don't apply default, since it has been applied when the mappings were parsed initially
                mapperService.merge(mappingType, mappingSource, MergeReason.MAPPING_UPDATE, true);
                if (!mapperService.documentMapper(mappingType).mappingSource().equals(mappingSource)) {
                    logger.debug("[{}] parsed mapping [{}], and got different sources\noriginal:\n{}\nparsed:\n{}", index, mappingType, mappingSource, mapperService.documentMapper(mappingType).mappingSource());
                    requiresRefresh = true;
                }
            } else {
                DocumentMapper existingMapper = mapperService.documentMapper(mappingType);
                if (!mappingSource.equals(existingMapper.mappingSource())) {
                    // mapping changed, update it
                    if (logger.isDebugEnabled() && mappingSource.compressed().length < 512) {
                        logger.debug("[{}] updating mapping [{}], source [{}]", index, mappingType, mappingSource.string());
                    } else if (logger.isTraceEnabled()) {
                        logger.trace("[{}] updating mapping [{}], source [{}]", index, mappingType, mappingSource.string());
                    } else {
                        logger.debug("[{}] updating mapping [{}] (source suppressed due to length, use TRACE level if needed)", index, mappingType);
                    }
                    // we don't apply default, since it has been applied when the mappings were parsed initially
                    mapperService.merge(mappingType, mappingSource, MergeReason.MAPPING_UPDATE, true);
                    if (!mapperService.documentMapper(mappingType).mappingSource().equals(mappingSource)) {
                        requiresRefresh = true;
                        logger.debug("[{}] parsed mapping [{}], and got different sources\noriginal:\n{}\nparsed:\n{}", index, mappingType, mappingSource, mapperService.documentMapper(mappingType).mappingSource());
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("[{}] failed to add mapping [{}], source [{}]", e, index, mappingType, mappingSource);
            throw e;
        }
        return requiresRefresh;
    }

    private boolean aliasesChanged(ClusterChangedEvent event) {
        return !event.state().metaData().equalsAliases(event.previousState().metaData()) ||
                !event.state().routingTable().equals(event.previousState().routingTable());
    }

    private void applyAliases(ClusterChangedEvent event) {
        // check if aliases changed
        if (aliasesChanged(event)) {
            // go over and update aliases
            for (IndexMetaData indexMetaData : event.state().metaData()) {
                String index = indexMetaData.getIndex();
                IndexService indexService = indicesService.indexService(index);
                if (indexService == null) {
                    // we only create / update here
                    continue;
                }
                IndexAliasesService indexAliasesService = indexService.aliasesService();
                indexAliasesService.setAliases(indexMetaData.getAliases());
            }
        }
    }

    private void removeIndex(String index, String reason) {
        try {
            indicesService.removeIndex(index, reason);
        } catch (Throwable e) {
            logger.warn("failed to clean index ({})", e, reason);
        }
        clearSeenMappings(index);

    }

    private void clearSeenMappings(String index) {
        // clear seen mappings as well
        for (Tuple<String, String> tuple : seenMappings.keySet()) {
            if (tuple.v1().equals(index)) {
                seenMappings.remove(tuple);
            }
        }
    }

    private void deleteIndex(String index, String reason) {
        try {
            indicesService.deleteIndex(index, reason);
        } catch (Throwable e) {
            logger.warn("failed to delete index ({})", e, reason);
        }
        // clear seen mappings as well
        clearSeenMappings(index);

    }

    private void failAndRemoveShard(IndexService indexService, boolean sendShardFailure, String message, @Nullable Throwable failure) {
        if (indexService.hasShard(0)) {
            try {
                indexService.removeShard(0, message);
            } catch (ShardNotFoundException e) {
                // the node got closed on us, ignore it
            } catch (Throwable e1) {
                logger.warn("[{}][{}] failed to remove shard after failure ([{}])", e1, indexService.index(), 0, message);
            }
        }
    }

    
    private class FailedEngineHandler implements Engine.FailedEngineListener {
        @Override
        public void onFailedEngine(final ShardId shardId, final String reason, final @Nullable Throwable failure) {
            ShardRouting shardRouting = null;
            final IndexService indexService = indicesService.indexService(shardId.index().name());
            if (indexService != null) {
                IndexShard indexShard = indexService.shard(shardId.id());
                if (indexShard != null) {
                    shardRouting = indexShard.routingEntry();
                }
            }
            if (shardRouting == null) {
                logger.warn("[{}][{}] engine failed, but can't find index shard. failure reason: [{}]", failure,
                        shardId.index().name(), shardId.id(), reason);
                return;
            }
            threadPool.generic().execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (mutex) {
                        failAndRemoveShard(indexService, true, "engine failure, reason [" + reason + "]", failure);
                    }
                }
            });
        }
    }

}
