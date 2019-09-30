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

package org.elassandra;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.elassandra.cli.DecodeSmileCommand;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.elasticsearch.test.discovery.MockCassandraDiscovery;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

public class CassandraDiscoveryTests extends ESSingleNodeTestCase {

    long version = -1;
    int resumit = 0;

    final List<String> MAPPING_COLLECTIONS = Arrays.asList("list","set","singleton");
    final List<String> MAPPING_TYPES = Arrays.asList("keyword",
        "text",
        "date",
        "byte",
        "short",
        "integer",
        "long",
        "double",
        "float",
        "boolean",
        "binary",
        "ip",
        "geo_point",
        "geo_shape");

    @Override
    public void tearDown() throws Exception {
        MockCassandraDiscovery discovery = this.getMockCassandraDiscovery();
        discovery.setPublishFunc(null);
        discovery.setResumitFunc(null);
        super.tearDown();
    }

    @Test
    public void testConcurrentPaxosUpdate() {
        createIndex("test");
        ensureGreen("test");

        MockCassandraDiscovery discovery = this.getMockCassandraDiscovery();
        discovery.setPublishFunc(e -> {
            if (version == -1) {
                // increase CQL version number to simulate PAXOS update while CQL schema not yet received.
                UntypedResultSet rs = process(ConsistencyLevel.ONE, "SELECT version FROM elastic_admin.metadata_log WHERE cluster_name = ? LIMIT 1", DatabaseDescriptor.getClusterName());
                version = rs.one().getLong("version");
                logger.warn("forcing metadata.version to {} in elastic_admin.metadata",  version + 1);

                // "UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, source= ?, ts = dateOf(now()) WHERE cluster_name= ? AND v = ? IF version = ?",
                process(ConsistencyLevel.ONE, "UPDATE elastic_admin.metadata_log SET owner = ?, version = ?, source = ?, ts = dateOf(now()) WHERE cluster_name = ? AND v = ?",
                        UUID.fromString(StorageService.instance.getLocalHostId()),
                        version + 1,
                        "paxos test",
                        DatabaseDescriptor.getClusterName(),
                        version + 1);
            }
        });

        discovery.setResumitFunc(e -> {
            if (resumit == 0) {
                resumit++;
                // publish
                clusterService().submitStateUpdateTask("cql-schema-mapping-update", new ClusterStateUpdateTask() {

                    @Override
                    public ClusterState execute(ClusterState currentState) {
                        ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);
                        MetaData newMetadata = MetaData.builder(currentState.metaData())
                                .persistentSettings(Settings.builder().build())
                                .version(version + 1)
                                .build();
                        ClusterState newClusterState = newStateBuilder.incrementVersion().metaData(newMetadata).build();
                        logger.warn("submit cql-schema-mapping-update metadata.version={}",  newClusterState.metaData().version());
                        return newClusterState;
                    }

                    @Override
                    public void onFailure(String source, Exception t) {
                        logger.error("unexpected failure during [{}]", t, source);
                    }

                });
            }
        });

        assertThat(client().prepareIndex("test", "my_type", "1").setSource("{\"status_code\": \"OK\" }", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertEquals(1, resumit);
        assertEquals(version + 2, this.clusterService().state().metaData().version());
    }



    @Test
    public void indexThousandsOfFields() throws Exception {
        final int NB_FIELDS = 5000;

        XContentBuilder mappingDef = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("id")
                        .field("type", "keyword")
                        .field("cql_collection", "singleton")
                        .field("cql_primary_key_order", 0)
                        .field("cql_partition_key", true)
                    .endObject();

        for (int i = 1; i <= NB_FIELDS; ++i) {
            mappingDef = mappingDef.startObject(String.format("c%05d", i))
                .field("type", randomFrom(MAPPING_TYPES))
                .field("cql_collection", randomFrom(MAPPING_COLLECTIONS))
                .field("cql_partition_key", false)
                .endObject();
        }

        mappingDef.endObject()
                .startObject("_meta")
                    .field("index_static_columns", false)
                    .field("index_insert_only", true)
                .endObject()
            .endObject();

        assertAcked(client().admin().indices().prepareCreate("test1")
            .setSettings(Settings.builder()
                .put("index.keyspace","test")
                .put(MapperService.INDEX_MAPPING_TOTAL_FIELDS_LIMIT_SETTING.getKey(), (NB_FIELDS+1000)).build()) // force the max fields to higher value
            .addMapping("t1", mappingDef));
        ensureGreen("test1");

        // query C* to retrieve the Mapping from schema extension.
        UntypedResultSet urs = process(ConsistencyLevel.ONE, "SELECT extensions from system_schema.tables where keyspace_name = ? and table_name = ?", "test", "t1");
        assertEquals(1, urs.size());
        Map<String, ByteBuffer> extensionsMap = urs.one().getMap("extensions", UTF8Type.instance, BytesType.instance);
        assertNotNull(extensionsMap);
        
        IndexMetaData indexMetaDataTest1 = clusterService().getIndexMetaDataFromExtension(extensionsMap.get("elastic_admin/test1"));
        MappingMetaData mmd = indexMetaDataTest1.getMappings().get("t1");
        assertNotNull(mmd);
        
        Map<String, Object> mapping = mmd.getSourceAsMap();
        assertNotNull(mapping);
        
        Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
        assertNotNull(properties);
        
        assertTrue(properties.containsKey("id"));
        for (int i = 1; i <= NB_FIELDS; ++i) {
            assertTrue(properties.containsKey(String.format("c%05d", i)));
        }
    }
}
