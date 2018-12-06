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
import org.apache.cassandra.db.SystemKeyspace;
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
 * Listen for cassandra schema and elasticsearch cluster state changes.
 */
public class SchemaListener extends MigrationListener implements ClusterStateListener
{
    final ClusterService clusterService;
    final Logger logger;

    MetaData metaData = null;
    ImmutableListMultimap.Builder<String, MappingMetaData> mappingsBuilder; // indexName -> mappings

    public SchemaListener(Settings settings, ClusterService clusterService) {
        this.clusterService = clusterService;
        this.clusterService.addListener(this);
        this.logger = ServerLoggers.getLogger(getClass(), settings);
    }

    @Override
    public void onBeginTransaction()
    {
        mappingsBuilder = ImmutableListMultimap.builder();
        metaData = null;
    }

    /**
     * Rebuild MetaData from per transaction updated extensions and submit a clusterState update task.
     */
    @Override
    public void onEndTransaction()
    {
        if (metaData != null && !metaData.clusterUUID().equals(SystemKeyspace.getLocalHostId().toString())) {
            final MetaData newMetadata = metaData;
            final ImmutableListMultimap<String, MappingMetaData> newMappings = (mappingsBuilder == null) ? null : mappingsBuilder.build();

            clusterService.submitStateUpdateTask("cql-schema-mapping-update", new ClusterStateUpdateTask() {
                @Override
                public ClusterState execute(ClusterState currentState) {
                    ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);
                    ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());

                    // update blocks
                    if (newMetadata.settings().getAsBoolean("cluster.blocks.read_only", false))
                        blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);

                    MetaData.Builder metaDataBuilder = MetaData.builder(newMetadata);
                    for(ObjectCursor<IndexMetaData> imdCursor : newMetadata.indices().values()) {
                        IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(imdCursor.value);

                        // add old mappings
                        IndexMetaData oldIndexMetaData = currentState.metaData().index(imdCursor.value.getIndex());
                        if (oldIndexMetaData != null) {
                            for(ObjectCursor<MappingMetaData> mmdCursor : oldIndexMetaData.getMappings().values())
                                indexMetaDataBuilder.putMapping(mmdCursor.value);
                        }
                        // add new mappings
                        if (newMappings != null)
                            newMappings.get(imdCursor.value.getIndex().getName()).forEach(mmd ->  indexMetaDataBuilder.putMapping( mmd ));

                        metaDataBuilder.put(indexMetaDataBuilder);
                    }

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

            mappingsBuilder = null;
            metaData = null;
        }
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
                    String indexName = clusterService.getIndexNameFromExtensionName(e.getKey());
                    MappingMetaData mappingMd = clusterService.getTypeExtension(e.getValue());
                    mappingsBuilder.put(indexName, mappingMd);

                    if (hasSecondaryIndex) {
                        SchemaManager.typeToCfName(ksm.name, mappingMd.type());
                    }
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