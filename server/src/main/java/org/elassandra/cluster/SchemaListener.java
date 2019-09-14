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

package org.elassandra.cluster;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.service.MigrationListener;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.logging.log4j.Logger;
import org.elassandra.index.ElasticSecondaryIndex;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.logging.ServerLoggers;
import org.elasticsearch.common.settings.Settings;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Listen for cassandra schema changes and elasticsearch cluster state changes.
 */
public class SchemaListener extends MigrationListener implements ClusterStateListener
{
    final ClusterService clusterService;
    final Logger logger;

    // record per transaction changes (
    boolean record = false;
    MetaData recordedMetaData = null;
    ListMultimap<String, IndexMetaData> recordedIndexMetaData = ArrayListMultimap.create(); // indexName -> List of IndexMetaData with a single mapping

    public SchemaListener(Settings settings, ClusterService clusterService) {
        this.clusterService = clusterService;
        this.clusterService.addListener(this);
        this.logger = ServerLoggers.getLogger(getClass(), settings);
    }

    /**
     * Not called when resulting from a cluster state update involving a CQL update (see inhibited MigrationListeners in MigrationManager.announce()).
     */
    @Override
    public void onBeginTransaction() {
        record = true;
        recordedIndexMetaData.clear();
        recordedMetaData = null;
    }

    /**
     * Rebuild MetaData from per transaction updated extensions and submit a clusterState update task.
     */
    @Override
    public void onEndTransaction() {
        if (recordedMetaData != null || !recordedIndexMetaData.isEmpty()) {
            final ClusterState currentState = this.clusterService.state();
            final ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);
            final ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());
            final MetaData sourceMetaData = (recordedMetaData == null) ? currentState.metaData() : recordedMetaData;
            final MetaData.Builder metaDataBuilder = MetaData.builder(sourceMetaData);
            if (recordedMetaData == null) {
                // add collected mappings coming from a CQL update (table schema restoration)
                recordedIndexMetaData.keySet().forEach( i -> clusterService.mergeIndexMetaData(metaDataBuilder, i, recordedIndexMetaData.get(i)));
            } else {
                // add all mappings from table extensions
                clusterService.mergeWithTableExtensions(metaDataBuilder);
            }

            // update virtualized index mapping
            final MetaData.Builder metaDataBuilder2 = clusterService.addVirtualIndexMappings(metaDataBuilder.build());
            
            // update blocks
            if (sourceMetaData.settings().getAsBoolean("cluster.blocks.read_only", false))
                blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);

            // update indices block.
            for (IndexMetaData indexMetaData : sourceMetaData)
                blocks.updateBlocks(indexMetaData);

            // summit the new clusterState to the MasterService for a local update.
            // keep the metadata.clusterUuid from the coordinator node.
            clusterService.submitStateUpdateTask("cql-schema-mapping-update", new ClusterStateUpdateTask(Priority.URGENT) {
                @Override
                public ClusterState execute(ClusterState currentState) {
                    ClusterState newClusterState = newStateBuilder.incrementVersion().metaData(metaDataBuilder2).blocks(blocks).build();
                    newClusterState = ClusterState.builder(newClusterState)
                            .routingTable(RoutingTable.build(SchemaListener.this.clusterService, newClusterState))
                            .build();
                    return newClusterState;
                }

                @Override
                public void onFailure(String source, Exception t) {
                    logger.error("unexpected failure during [{}]", t, source);
                }

                @Override
                public SchemaUpdate schemaUpdate() {
                    return SchemaUpdate.UPDATE;
                }
            });
        }
        record = false;
        recordedIndexMetaData.clear();
        recordedMetaData = null;
    }

    @Override
    public void onCreateColumnFamily(KeyspaceMetadata ksm, CFMetaData cfm) {
        if (!record)
            return;

        logger.trace("{}.{}", ksm.name, cfm.cfName);
        if (!isElasticAdmin(ksm.name, cfm.cfName)) {
            updateElasticsearchMapping(ksm, cfm);
        }
    }

    /**
     * if elastic_admin.metadata.extensions.get('version') greater than curent.metadata.version
     * then trigger 2i mapping update and a clusterState update.
     */
    @Override
    public void onUpdateColumnFamily(KeyspaceMetadata ksm, CFMetaData cfm, boolean affectsStatements) {
        if (!record)
            return;

        logger.trace("{}.{}", ksm.name, cfm.cfName);
        if (isElasticAdmin(ksm.name, cfm.cfName)) {
            recordedMetaData =  clusterService.readMetaData(cfm);
        } else {
            updateElasticsearchMapping(ksm, cfm);
        }
    }

    /**
     * Update number of shards if replication map changed
     */
    @Override
    public void onUpdateKeyspace(final String ksName) {
        logger.trace("{}", ksName);
        MetaData metadata = this.clusterService.state().metaData();
        for(ObjectCursor<IndexMetaData> imdCursor : metadata.indices().values()) {
            if (ksName.equals(imdCursor.value.keyspace())) {
                KeyspaceMetadata ksm = Schema.instance.getKSMetaData(ksName);
                if (ksm.params != null && ksm.params.replication != null && ksm.params.replication.klass.isAssignableFrom(NetworkTopologyStrategy.class)) {
                    String localDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(FBUtilities.getBroadcastAddress());
                    try {
                        Integer rf = Integer.parseInt(ksm.params.replication.asMap().get(localDc));
                        if (!rf.equals(imdCursor.value.getNumberOfReplicas()+1)) {
                            // submit a cluster state update task to update number of replica shards.
                            // TODO: update index replication_map ?
                            logger.debug("Submit numberOfReplicas update  for indices based on keyspace [{}]", ksName);
                            clusterService.submitNumberOfShardsAndReplicasUpdate("update-shard-replicas", ksName);
                        }
                    } catch(NumberFormatException e) {

                    }
                }
            }
        }
    }

    // TODO: drop associated indices
    @Override
    public void onDropKeyspace(String ksName) {
        logger.trace("{}", ksName);
    }

    boolean isElasticAdmin(String ksName, String cfName) {
        return (clusterService.getElasticAdminKeyspaceName().equals(ksName) &&
                ClusterService.ELASTIC_ADMIN_METADATA_TABLE.equals(cfName));
    }

    void updateElasticsearchMapping(KeyspaceMetadata ksm, CFMetaData cfm) {
        boolean hasSecondaryIndex = cfm.getIndexes().has(SchemaManager.buildIndexName(cfm.cfName));
        for(Map.Entry<String, ByteBuffer> e : cfm.params.extensions.entrySet()) {
            if (clusterService.isValidTypeExtension(e.getKey())) {
                    IndexMetaData indexMetaData = clusterService.getIndexMetaDataFromExtension(e.getValue());
                    if (recordedIndexMetaData != null)
                        recordedIndexMetaData.put(indexMetaData.getIndex().getName(), indexMetaData);

                    if (hasSecondaryIndex)
                        indexMetaData.getMappings().forEach( m -> SchemaManager.typeToCfName(ksm.name, m.value.type()) );
            }
        }
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        for(ElasticSecondaryIndex esi : ElasticSecondaryIndex.elasticSecondayIndices.values())
            esi.clusterChanged(event);
    }
}