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
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.service.MigrationListener;
import org.apache.cassandra.utils.ByteBufferUtil;
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
import org.elasticsearch.common.compress.CompressedXContent;
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
    final ListMultimap<String, MappingMetaData> mappings = ArrayListMultimap.create(); // indexName -> mappings

    public SchemaListener(Settings settings, ClusterService clusterService) {
        this.clusterService = clusterService;
        this.clusterService.addListener(this);
        this.logger = ServerLoggers.getLogger(getClass(), settings);
    }

    @Override
    public void onBeginTransaction()
    {
        mappings.clear();
        metaData = null;
    }

    /**
     * Rebuild MetaData from per transaction updated extensions and submit a clusterState update task.
     */
    @Override
    public void onEndTransaction()
    {
        if (metaData != null) {
            long versionGap = metaData.version() - clusterService.state().metaData().version();

            final MetaData newMetadata;
            if (versionGap > 1) {
                // read all metadata to bridge the gap between previous metadata
                logger.trace("metadata={} mappings={} versionGap={}", metaData, mappings, versionGap);
                newMetadata = this.clusterService.readMetaDataFromTableExtensions(true);
            } else if (!metaData.clusterUUID().equals(SystemKeyspace.getLocalHostId().toString())) {
                logger.trace("metadata={} mappings={} versionGap={}", metaData, mappings, versionGap);
                // read changed table extensions.
                MetaData.Builder metadataBuilder = MetaData.builder(metaData);
                for(ObjectCursor<IndexMetaData> imdCursor : metaData.indices().values()) {
                    IndexMetaData imd = imdCursor.value;
                    IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(imd);
                    mappings.get(imd.getIndex().getName()).forEach(m -> indexMetaDataBuilder.putMapping(m));
                    metadataBuilder.put(indexMetaDataBuilder, false);
                }
                newMetadata = metadataBuilder.build();
            } else {
                // ignore self schema update
                logger.trace("ignore local update metadata={} mappings={} versionGap={}", metaData, mappings, versionGap);
                newMetadata = null;
            }

            if (newMetadata != null) {
                clusterService.submitStateUpdateTask("cql-schema-mapping-update", new ClusterStateUpdateTask() {
                    @Override
                    public ClusterState execute(ClusterState currentState) {
                        ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);
                        ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());

                        // update blocks
                        if (newMetadata.settings().getAsBoolean("cluster.blocks.read_only", false))
                            blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);

                        newStateBuilder.metaData(newMetadata);

                        // update indices block.
                        for (IndexMetaData indexMetaData : newMetadata)
                            blocks.updateBlocks(indexMetaData);

                        return newStateBuilder.blocks(blocks).build();
                    }

                    @Override
                    public void onFailure(String source, Exception t) {
                        logger.error("unexpected failure during [{}]", t, source);
                    }
                });
            }
            mappings.clear();
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

    boolean hasElasticSecondaryIndex(String ksName, String cfName) {
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(ksName);
        if (ksm != null) {
            CFMetaData cfm = ksm.getTableOrViewNullable(cfName);
            if (cfm != null) {
                return cfm.getIndexes().has(SchemaManager.buildIndexName(cfName));
            }
        }
        return false;
    }

    void updateElasticsearchMapping(KeyspaceMetadata ksm, CFMetaData cfm) {
        boolean hasSecondaryIndex = cfm.getIndexes().has(SchemaManager.buildIndexName(cfm.cfName));
        for(Map.Entry<String, ByteBuffer> e : cfm.params.extensions.entrySet()) {
            if (clusterService.isValidTypeExtension(e.getKey())) {
                try {
                    String indexName = clusterService.getIndexNameFromExtensionName(e.getKey());
                    MappingMetaData mappingMd = clusterService.getTypeExtension(e.getValue());
                    mappings.put(indexName, mappingMd);

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