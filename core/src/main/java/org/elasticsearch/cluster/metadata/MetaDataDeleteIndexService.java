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

package org.elasticsearch.cluster.metadata;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Schema;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexClusterStateUpdateRequest;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.snapshots.SnapshotsService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Deletes indices.
 */
public class MetaDataDeleteIndexService extends AbstractComponent {

    private final ClusterService clusterService;

    private final AllocationService allocationService;

    @Inject
    public MetaDataDeleteIndexService(Settings settings, ClusterService clusterService, AllocationService allocationService) {
        super(settings);
        this.clusterService = clusterService;
        this.allocationService = allocationService;
    }

    public void deleteIndices(final DeleteIndexClusterStateUpdateRequest request,
            final ActionListener<ClusterStateUpdateResponse> listener) {
        if (request.indices() == null || request.indices().length == 0) {
            throw new IllegalArgumentException("Index name is required");
        }

        clusterService.submitStateUpdateTask("delete-index " + Arrays.toString(request.indices()),
            new AckedClusterStateUpdateTask<ClusterStateUpdateResponse>(Priority.URGENT, request, listener) {

            @Override
            protected ClusterStateUpdateResponse newResponse(boolean acknowledged) {
                return new ClusterStateUpdateResponse(acknowledged);
            }
            
            @Override
            public boolean doPresistMetaData() {
                return true;
            }

            @Override
            public ClusterState execute(final ClusterState currentState) {
                return deleteIndices(currentState, Sets.newHashSet(request.indices()));
            }
        });
    }

    /**
     * Delete some indices from the cluster state.
     */
    public ClusterState deleteIndices(ClusterState currentState, Set<Index> indices) {
        final MetaData meta = currentState.metaData();
        final Set<IndexMetaData> metaDatas = indices.stream().map(i -> meta.getIndexSafe(i)).collect(toSet());
        // Check if index deletion conflicts with any running snapshots
        SnapshotsService.checkIndexDeletion(currentState, metaDatas);
        //RoutingTable.Builder routingTableBuilder = RoutingTable.builder(clusterService, currentState.routingTable());
        MetaData.Builder metaDataBuilder = MetaData.builder(meta);
        ClusterBlocks.Builder clusterBlocksBuilder = ClusterBlocks.builder().blocks(currentState.blocks());

        final IndexGraveyard.Builder graveyardBuilder = IndexGraveyard.builder(metaDataBuilder.indexGraveyard());
        final int previousGraveyardSize = graveyardBuilder.tombstones().size();
        Multimap<IndexMetaData,String> unindexedTables = HashMultimap.create();
        for (final Index index : indices) {
            String indexName = index.getName();
            logger.info("{} deleting index", index);
            //routingTableBuilder.remove(indexName);
            clusterBlocksBuilder.removeIndexBlocks(indexName);
            metaDataBuilder.remove(indexName);
            
            final IndexMetaData indexMetaData = currentState.metaData().index(index);
            // record keyspace.table having useless 2i 
            for(ObjectCursor<MappingMetaData> type:indexMetaData.getMappings().values()) 
                if (!MapperService.DEFAULT_MAPPING.equals(type.value.type()))
                    unindexedTables.put(indexMetaData, ClusterService.typeToCfName(type.value.type()));
        }
        
        // add tombstones to the cluster state for each deleted index
        final IndexGraveyard currentGraveyard = graveyardBuilder.addTombstones(indices).build(settings);
        metaDataBuilder.indexGraveyard(currentGraveyard); // the new graveyard set on the metadata
        logger.trace("{} tombstones purged from the cluster state. Previous tombstone size: {}. Current tombstone size: {}.",
            graveyardBuilder.getNumPurged(), previousGraveyardSize, currentGraveyard.getTombstones().size());
        
        MetaData newMetaData = metaDataBuilder.build();
        ClusterBlocks blocks = clusterBlocksBuilder.build();

        // remove keyspace.table still having ES indices from the unindexedTables
        for(ObjectCursor<IndexMetaData> index:newMetaData.indices().values()) {
            for(ObjectCursor<MappingMetaData> type:index.value.getMappings().values()) 
                unindexedTables.remove(index.value.keyspace(), type.value);
        }
        
        logger.debug("unindexed tables={}", unindexedTables);
        boolean clusterDropOnDelete = currentState.metaData().settings().getAsBoolean(ClusterService.SETTING_CLUSTER_DROP_ON_DELETE_INDEX, Boolean.getBoolean("es.drop_on_delete_index"));
        for(IndexMetaData imd : unindexedTables.keySet()) {
            if (Schema.instance.getKeyspaceInstance(imd.keyspace()) != null) {
                // keyspace still exists.
                if (imd.getSettings().getAsBoolean(IndexMetaData.SETTING_DROP_ON_DELETE_INDEX, clusterDropOnDelete)) {
                    int tableCount = 0;
                    for(CFMetaData tableOrView : Schema.instance.getKeyspaceInstance(imd.keyspace()).getMetadata().tablesAndViews()) {
                        if (tableOrView.isCQLTable())
                            tableCount++;
                    }
                    if (tableCount == unindexedTables.get(imd).size()) {
                        // drop keyspace instead of droping all tables.
                        try {
                            MetaDataDeleteIndexService.this.clusterService.dropIndexKeyspace(imd.keyspace());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // drop tables
                        for(String table : unindexedTables.get(imd))
                            MetaDataDeleteIndexService.this.clusterService.dropTable(imd.keyspace(), table);
                    }
                } else {
                    // drop secondary indices
                    for(String table : unindexedTables.get(imd))
                        MetaDataDeleteIndexService.this.clusterService.dropSecondaryIndex(imd.keyspace(), table);
                }
            }
        }
        
        // update snapshot restore entries
        ImmutableOpenMap<String, ClusterState.Custom> customs = currentState.getCustoms();
        /*
        final RestoreInProgress restoreInProgress = currentState.custom(RestoreInProgress.TYPE);
        if (restoreInProgress != null) {
            RestoreInProgress updatedRestoreInProgress = RestoreService.updateRestoreStateWithDeletedIndices(restoreInProgress, indices);
            if (updatedRestoreInProgress != restoreInProgress) {
                ImmutableOpenMap.Builder<String, ClusterState.Custom> builder = ImmutableOpenMap.builder(customs);
                builder.put(RestoreInProgress.TYPE, updatedRestoreInProgress);
                customs = builder.build();
            }
        }
        */
        
        // Update the routing table without deleted indices.
        RoutingTable newRoutingTable = RoutingTable.build(this.clusterService, ClusterState.builder(currentState).metaData(newMetaData).blocks(blocks).customs(customs).build());
        return ClusterState.builder(currentState).metaData(newMetaData).blocks(blocks).routingTable(newRoutingTable).customs(customs).build();
        /*
        return allocationService.reroute(
                ClusterState.builder(currentState)
                    .routingTable(routingTableBuilder.build())
                    .metaData(newMetaData)
                    .blocks(blocks)
                    .customs(customs)
                    .build(),
                "deleted indices [" + indices + "]");
        */
    }
}
