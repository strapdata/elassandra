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
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.schema.SchemaKeyspace;
import org.apache.cassandra.transport.Event;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elassandra.cluster.SchemaManager;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexClusterStateUpdateRequest;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.RestoreInProgress;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.snapshots.SnapshotsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Deletes indices.
 */
public class MetaDataDeleteIndexService {

    private static final Logger logger = LogManager.getLogger(MetaDataDeleteIndexService.class);

    private final Settings settings;
    private final ClusterService clusterService;

    private final AllocationService allocationService;

    @Inject
    public MetaDataDeleteIndexService(Settings settings, ClusterService clusterService, AllocationService allocationService) {
        this.settings = settings;
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
            public SchemaUpdate schemaUpdate() {
                return SchemaUpdate.UPDATE;
            }

            @Override
            public ClusterState execute(ClusterState currentState) throws Exception {
                throw new UnsupportedOperationException();
            }

            @Override
                public ClusterState execute(final ClusterState currentState, Collection<Mutation> mutations, Collection<Event.SchemaChange> events) {
                return deleteIndices(currentState, Sets.newHashSet(request.indices()), mutations, events);
            }
        });
    }

    // for testing purposes only
    public ClusterState deleteIndices(ClusterState currentState, Set<Index> indices) {
        return deleteIndices(currentState, indices, new ArrayList<Mutation>(), new ArrayList<Event.SchemaChange>());
    }

    // collected keyspaces and tables for deleted indices
    class KeyspaceRemovalInfo {
        String keyspace;
        Set<IndexMetaData> indices = new HashSet<>();
        Set<String> droppableTables = new HashSet<>();
        Set<String> unindexableTables = new HashSet<>();
        boolean droppableKeyspace = true;

        public KeyspaceRemovalInfo(String keyspace) {
            this.keyspace = keyspace;
        }

        void addRemovedIndex(IndexMetaData indexMetaData, boolean clusterDropOnDelete) {
            assert this.keyspace.equals(indexMetaData.keyspace()) : "Keyspace does not match";
            indices.add(indexMetaData);
            for(ObjectCursor<MappingMetaData> type : indexMetaData.getMappings().values()) {
                if (!MapperService.DEFAULT_MAPPING.equals(type.value.type())) {
                    String tableName = SchemaManager.typeToCfName(indexMetaData.keyspace(), type.value.type());
                    unindexableTables.add(tableName);
                    if (indexMetaData.getSettings().getAsBoolean(IndexMetaData.SETTING_DROP_ON_DELETE_INDEX, clusterDropOnDelete))
                        droppableTables.add(tableName);
                }
            }
            if (!indexMetaData.getSettings().getAsBoolean(IndexMetaData.SETTING_DROP_ON_DELETE_INDEX, clusterDropOnDelete))
                droppableKeyspace = false;
        }

        void removeUsedTables(IndexMetaData indexMetaData) {
            assert this.keyspace.equals(indexMetaData.keyspace()) : "Keyspace does not match";
            for(ObjectCursor<MappingMetaData> type: indexMetaData.getMappings().values()) {
                String tableName = SchemaManager.typeToCfName(indexMetaData.keyspace(), type.value.type());
                droppableTables.remove(tableName);
                unindexableTables.remove(tableName);
                droppableKeyspace = false;
            }
        }

        void drop(Collection<Mutation> mutations, Collection<Event.SchemaChange> events) {
            logger.debug("drop keyspaces={} droppableKeyspace={} droppableTables={} unindexableTables={}",
                    keyspace, droppableKeyspace, droppableTables, unindexableTables);

            if (droppableKeyspace) {
                MetaDataDeleteIndexService.this.clusterService.getSchemaManager().dropIndexKeyspace(keyspace, mutations, events);
                return;
            }

            KeyspaceMetadata ksm = SchemaManager.getKSMetaDataCopy(keyspace);
            if (ksm == null)
                return;

            for(String table : droppableTables) {
                ksm = MetaDataDeleteIndexService.this.clusterService.getSchemaManager().dropTable(ksm, table, mutations, events);
                unindexableTables.remove(table);
            }
            for(String table : unindexableTables) {
                MetaDataDeleteIndexService.this.clusterService.getSchemaManager().dropSecondaryIndex(ksm, table, mutations, events);
            }

            // remove table extensions from CQL schema
            HashMultimap<String, IndexMetaData> tableExtensionToRemove = HashMultimap.create();
            for(IndexMetaData indexMetaData : indices) {
                for(ObjectCursor<MappingMetaData> type: indexMetaData.getMappings().values()) {
                    String table = SchemaManager.typeToCfName(indexMetaData.keyspace(), type.value.type());
                    if (!droppableTables.contains(table) && !unindexableTables.contains(table))
                        tableExtensionToRemove.put(table, indexMetaData);
                }
            }
            Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
            for(String table : tableExtensionToRemove.keySet()) {
                CFMetaData cfm = ksm.getTableOrViewNullable(table);
                MetaDataDeleteIndexService.this.clusterService.getSchemaManager().removeTableExtensionToMutationBuilder(cfm, tableExtensionToRemove.get(table), builder);
            }
            if (!builder.isEmpty())
                mutations.add(builder.build());
        }
    }

    /**
     * Delete some indices from the cluster state.
     */
    public ClusterState deleteIndices(ClusterState currentState, Set<Index> indices, Collection<Mutation> mutations, Collection<Event.SchemaChange> events) {
        final MetaData meta = currentState.metaData();
        final Set<IndexMetaData> metaDatas = indices.stream().map(i -> meta.getIndexSafe(i)).collect(toSet());
        // Check if index deletion conflicts with any running snapshots
        SnapshotsService.checkIndexDeletion(currentState, metaDatas);
        //RoutingTable.Builder routingTableBuilder = RoutingTable.builder(currentState.routingTable());
        MetaData.Builder metaDataBuilder = MetaData.builder(meta).setClusterUuid();
        ClusterBlocks.Builder clusterBlocksBuilder = ClusterBlocks.builder().blocks(currentState.blocks());

        final IndexGraveyard.Builder graveyardBuilder = IndexGraveyard.builder(metaDataBuilder.indexGraveyard());
        final int previousGraveyardSize = graveyardBuilder.tombstones().size();

        final boolean clusterDropOnDelete = currentState.metaData().settings().getAsBoolean(ClusterService.SETTING_CLUSTER_DROP_ON_DELETE_INDEX, Boolean.getBoolean("es.drop_on_delete_index"));
        final Map<String, KeyspaceRemovalInfo> removalInfoMap = new HashMap<>();
        RoutingTable.Builder routingTableBuilder = new RoutingTable.Builder(currentState.routingTable());
        for (final Index index : indices) {
            String indexName = index.getName();
            logger.info("{} deleting index", index);
            //routingTableBuilder.remove(indexName);
            clusterBlocksBuilder.removeIndexBlocks(indexName);
            metaDataBuilder.remove(indexName);
            routingTableBuilder.remove(index.getName());

            final IndexMetaData indexMetaData = currentState.metaData().index(index);
            KeyspaceRemovalInfo kri = removalInfoMap.computeIfAbsent(indexMetaData.keyspace(),  k -> { return new KeyspaceRemovalInfo(indexMetaData.keyspace()); });
            kri.addRemovedIndex(indexMetaData, clusterDropOnDelete);
        }

        // add tombstones to the cluster state for each deleted index
        final IndexGraveyard currentGraveyard = graveyardBuilder.addTombstones(indices).build(settings);
        metaDataBuilder.indexGraveyard(currentGraveyard); // the new graveyard set on the metadata
        logger.trace("{} tombstones purged from the cluster state. Previous tombstone size: {}. Current tombstone size: {}.",
            graveyardBuilder.getNumPurged(), previousGraveyardSize, currentGraveyard.getTombstones().size());

        MetaData newMetaData = metaDataBuilder.build();
        ClusterBlocks blocks = clusterBlocksBuilder.build();

        // remove keyspacse and tables still having ES indices from the unindexedTables and unindexedKeyspaces
        for(ObjectCursor<IndexMetaData> index : newMetaData.indices().values()) {
            final IndexMetaData imd = index.value;
            removalInfoMap.computeIfPresent(index.value.keyspace(), (String k, KeyspaceRemovalInfo v) -> { v.removeUsedTables(imd); return v; });
        }

        if (this.clusterService != null)    // to avoid NPE on test
            removalInfoMap.values().forEach( v -> v.drop(mutations, events) );

        // update snapshot restore entries
        ImmutableOpenMap<String, ClusterState.Custom> customs = currentState.getCustoms();
        return ClusterState.builder(currentState).metaData(newMetaData).blocks(blocks).routingTable(routingTableBuilder.build()).customs(customs).build();
    }
}
