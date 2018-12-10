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
import com.google.common.collect.ImmutableListMultimap;

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
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.logging.ServerLoggers;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Listen for cassandra schema changes and elasticsearch cluster state changes.
 */
public class SchemaListener extends MigrationListener implements ClusterStateListener
{
    final ClusterService clusterService;
    final Logger logger;

    MetaData metaData = null;
    ImmutableListMultimap.Builder<String, IndexMetaData> indexMetaDataBuilder; // indexName -> List of IndexMetaData with a single mapping

    public SchemaListener(Settings settings, ClusterService clusterService) {
        this.clusterService = clusterService;
        this.clusterService.addListener(this);
        this.logger = ServerLoggers.getLogger(getClass(), settings);
    }

    /**
     * Not called when resulting from a cluster state update invloving a CQL update (see inhibited MigrationListeners in MigrationManager.announce()).
     */
    @Override
    public void onBeginTransaction()
    {
        indexMetaDataBuilder = ImmutableListMultimap.builder();
        metaData = null;
    }

    /**
     * Not called when resulting from a cluster state update invloving a CQL update (see inhibited MigrationListeners in MigrationManager.announce()).
     * Rebuild MetaData from per transaction updated extensions and submit a clusterState update task.
     */
    @Override
    public void onEndTransaction()
    {
        final MetaData metaData2 = metaData;
        final ImmutableListMultimap<String, IndexMetaData> newIndexMetaData = (indexMetaDataBuilder == null) ? null : indexMetaDataBuilder.build();

        if (metaData != null || newIndexMetaData != null) {
            clusterService.submitStateUpdateTask("cql-schema-mapping-update", new ClusterStateUpdateTask() {
                @Override
                public ClusterState execute(ClusterState currentState) {
                    ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);
                    ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());

                    MetaData newMetadata = (metaData2 == null) ? currentState.metaData() : metaData2;
                    MetaData.Builder metaDataBuilder = MetaData.builder(newMetadata);
                    for(String indexName : newIndexMetaData.keySet()) {
                        // merge all IndexMetadata for single type to a multi-typed IndexMetaData (for baward compatibility with version 5)
                        IndexMetaData indexMetaData = metaDataBuilder.get(indexName);
                        if (indexMetaData == null)
                            indexMetaData = newIndexMetaData.get(indexName).get(0);

                        IndexMetaData.Builder indexMetaDataBuilder = new IndexMetaData.Builder(indexMetaData);
                        for(IndexMetaData imd : newIndexMetaData.get(indexName)) {
                            for(ObjectCursor<MappingMetaData> m : imd.getMappings().values())
                                indexMetaDataBuilder.putMapping(m.value);
                        }
                        metaDataBuilder.put(indexMetaDataBuilder);
                    }

                    // update blocks
                    if (newMetadata.settings().getAsBoolean("cluster.blocks.read_only", false))
                        blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);

                    // update indices block.
                    for (IndexMetaData indexMetaData : newMetadata)
                        blocks.updateBlocks(indexMetaData);

                    return newStateBuilder.metaData(metaDataBuilder).blocks(blocks).build();
                }

                @Override
                public void onFailure(String source, Exception t) {
                    logger.error("unexpected failure during [{}]", t, source);
                }
            });
        }

        indexMetaDataBuilder = null;
        metaData = null;
    }

    @Override
    public void onCreateColumnFamily(KeyspaceMetadata ksm, CFMetaData cfm)
    {
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
    public void onUpdateColumnFamily(KeyspaceMetadata ksm, CFMetaData cfm, boolean affectsStatements)
    {
        logger.trace("{}.{}", ksm.name, cfm.cfName);
        if (isElasticAdmin(ksm.name, cfm.cfName)) {
            updateElasticsearchMetaData(ksm, cfm);
        } else {
            updateElasticsearchMapping(ksm, cfm);
        }
    }

    /**
     * Update number of shards if replication map changed
     */
    @Override
    public void onUpdateKeyspace(final String ksName)
    {
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
    public void onDropKeyspace(String ksName)
    {
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
                try {
                    IndexMetaData indexMetaData = clusterService.getIndexMetaDataFromExtension(e.getValue());
                    if (indexMetaDataBuilder != null)
                        indexMetaDataBuilder.put(indexMetaData.getIndex().getName(), indexMetaData);

                    if (hasSecondaryIndex)
                        indexMetaData.getMappings().forEach( m -> SchemaManager.typeToCfName(ksm.name, m.value.type()) );
                } catch (IOException e1) {
                    logger.error("Failed to parse mapping in {}.{} extension {}", ksm.name, cfm.cfName, e.getKey());
                }
            }
        }
    }

    void updateElasticsearchMetaData(KeyspaceMetadata ksm, CFMetaData cfm) {
        try {
            metaData =  clusterService.readMetaData(cfm);
        } catch (IOException e) {
            logger.error("Failed to parse metadata from elastic_admin.metadata table", e);
        }
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        for(ElasticSecondaryIndex esi : ElasticSecondaryIndex.elasticSecondayIndices.values())
            esi.clusterChanged(event);
    }
}