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
package org.elassandra;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra partitioned index tests.
 * @author vroyer
 *
 */
//mvn test -Pdev -pl om.strapdata.elasticsearch:elasticsearch -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.PartitionedIndexTests -Des.logger.level=ERROR -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
public class PartitionedIndexTests extends ESSingleNodeTestCase {

    @Test
    public void basicPartitionFunctionTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 ( name text, age int, primary key (name))");

        for(long i=20; i < 30; i++) {
            createIndex("ks_"+i, Settings.builder().put("index.keyspace","ks")
                    .put("index.partition_function", "byage ks_{0,number,##} age")
                    .build(),"t1", discoverMapping("t1"));
            ensureGreen("ks_"+i);
        }
        for(long i=20; i < 30; i++) {
            for(int j=0; j < i; j++)
                process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "INSERT INTO ks.t1 (name, age) VALUES ('name%d-%d', %d)",i,j,i));
        }

        for(long i=20; i < 30; i++)
            assertThat(client().prepareSearch().setIndices("ks_"+i).setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(i));

        // test index delete #286
        for(long i=25; i < 30; i++) {
            assertAcked(client().admin().indices().prepareDelete("ks_"+i).get());
        }
        UntypedResultSet rs = process(ConsistencyLevel.ONE,"SELECT extensions FROM system_schema.tables WHERE keyspace_name = ? AND table_name = ?", "ks", "t1");
        for(Row row : rs) {
            Map<String, ByteBuffer> extensions = row.getMap("extensions", UTF8Type.instance, BytesType.instance);
            for(long i=20; i < 25; i++)
                assertTrue(extensions.containsKey("elastic_admin/ks_"+i));
            for(long i=25; i < 30; i++) {
                assertFalse(extensions.containsKey("elastic_admin/ks_"+i));
            }
        }
    }

    @Test
    public void basicStringPartitionFunctionTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 ( name text, age int, primary key (name))");

        for(long i=20; i < 30; i++) {
            createIndex("ks_"+i, Settings.builder().put("index.keyspace","ks")
                    .put("index.partition_function", "byage ks_%d age")
                    .put("index.partition_function_class", "StringPartitionFunction")
                    .build(),"t1", discoverMapping("t1"));
            ensureGreen("ks_"+i);
        }
        for(long i=20; i < 30; i++) {
            for(int j=0; j < i; j++)
                process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "INSERT INTO ks.t1 (name, age) VALUES ('name%d-%d', %d)",i,j,i));
        }

        for(long i=20; i < 30; i++)
            assertThat(client().prepareSearch().setIndices("ks_"+i).setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(i));
    }

    @Test
    public void basicStringPartitionFunctionWithDummyIndexTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 (name text, age int, primary key (name))");

        XContentBuilder mappingt1 = XContentFactory.jsonBuilder().startObject()
                    .startObject("properties")
                       .startObject("name").field("type", "keyword").field("cql_collection", "singleton").field("index", false).endObject()
                       .startObject("age").field("type", "integer").field("cql_collection", "singleton").field("index", false).endObject()
                    .endObject()
              .endObject();
        assertAcked(client().admin().indices().prepareCreate("ks").addMapping("t1", mappingt1).get());

        for(long i=20; i < 30; i++) {
            createIndex("ks_"+i, Settings.builder().put("index.keyspace","ks")
                    .put("index.partition_function", "byage ks_%d age")
                    .put("index.partition_function_class", "org.elassandra.index.StringPartitionFunction")
                    .build(),"t1", discoverMapping("t1"));
            ensureGreen("ks_"+i);
        }
        for(long i=20; i < 30; i++) {
            for(int j=0; j < i; j++) {
                String name = String.format(Locale.ROOT, "name%d-%d", i, j);
                XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
                        .field("name", name)
                        .field("age", i)
                        .endObject();
                client().prepareIndex("ks", "t1", name).setSource(doc).get();
            }
        }

        for(long i=20; i < 30; i++)
            assertThat(client().prepareSearch().setIndices("ks_"+i).setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(i));
    }

    @Test
    public void multipleMappingTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE IF NOT EXISTS fb WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE fb.messages ( conversation text, num int, author text, content text, date timestamp, recipients list<text>, PRIMARY KEY (conversation, num))");

        createIndex("fb", Settings.builder().put("index.keyspace","fb").put("index.table","messages").build(),"messages", discoverMapping("messages"));
        ensureGreen("fb");

        XContentBuilder mapping2 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("content").field("type", "text").field("cql_collection","singleton").endObject()
                        .startObject("date").field("type", "date").field("cql_collection","singleton").endObject()
                        .startObject("recipients").field("type", "keyword").field("cql_collection","list").endObject()
                        .startObject("conversation").field("type", "text").field("cql_collection","singleton").endObject()
                        .startObject("author").field("type", "text").field("cql_collection","singleton").endObject()
                     .endObject()
                .endObject();
        createIndex("fb2", Settings.builder().put("index.keyspace","fb").put("index.table","messages").build(),"messages", mapping2);
        ensureGreen("fb2");

        client().prepareIndex("fb", "messages","\"Lisa%20Revol\",201]")
        .setSource("{\"content\": \"ouais\", \"num\": 201, \"conversation\": \"Lisa\", \"author\": \"Barth\", \"date\": 1469968740000, \"recipients\": [\"Lisa\"]}", XContentType.JSON)
        .get();

        assertThat(client().prepareSearch().setIndices("fb").setTypes("messages").get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("fb2").setTypes("messages").get().getHits().getTotalHits(), equalTo(1L));
    }
    
    @Test
    public void basicVirtualIndexTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 ( name text, age int, primary key (name))");

        // create 10 indicies "ks20-30" pointing to index "ks"
        for(long i=20; i < 30; i++) {
            createIndex("ks_"+i, Settings.builder()
                    .put("index.keyspace","ks")
                    .put("index.partition_function", "byage ks_%d age")
                    .put("index.partition_function_class", "StringPartitionFunction")
                    .put("index.virtual_index", "ks")
                    .build(),"t1", discoverMapping("t1"));
            ensureGreen("ks_"+i);
        }
        for(long i=20; i < 30; i++) {
            for(int j=0; j < i; j++)
                process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "INSERT INTO ks.t1 (name, age) VALUES ('name%d-%d', %d)",i,j,i));
        }

        for(long i=20; i < 30; i++)
            assertThat(client().prepareSearch().setIndices("ks_"+i).setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(i));
        
        // add a doc with new field in ks_25
        assertEquals(client().prepareIndex("ks_25", "t1","xx")
        .setSource("{\"age\": 20, \"content\": \"ouais\", \"num\": 201, \"conversation\": \"Lisa\", \"author\": \"Barth\", \"date\": 1469968740000, \"recipients\": [\"Lisa\"]}", XContentType.JSON)
        .get().getResult(), DocWriteResponse.Result.CREATED);
        
        // check others have it in their mapping updated
        for(long i=20; i < 30; i++) {
            Map<String, Object> md = client().admin().indices().prepareGetMappings("ks_"+i).addTypes("t1").get().getMappings().get("ks_"+i).get("t1").getSourceAsMap();
            Map<String, Object> properties = (Map<String, Object>) md.get("properties");
            assertEquals(Boolean.TRUE, properties.containsKey("content"));
        }
        
        // check the doc is inserted in ks_20 and not in the others
        assertThat(client().prepareSearch().setIndices("ks_"+20).setTypes("t1").setQuery(QueryBuilders.termQuery("content", "ouais")).get().getHits().getTotalHits(), equalTo(1L));
        for(long i=21; i < 30; i++)
            assertThat(client().prepareSearch().setIndices("ks_"+i).setTypes("t1").setQuery(QueryBuilders.termQuery("content", "ouais")).get().getHits().getTotalHits(), equalTo(0L));
        
        // check tables extensions indexMetaData has only mappings for the virtual index
        UntypedResultSet rs = process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "SELECT extensions FROM system_schema.tables WHERE keyspace_name='ks' AND table_name='t1'"));
        Row row = rs.one();
        assertNotNull(row);
        Map<String, ByteBuffer> extensions = row.getMap("extensions", UTF8Type.instance, BytesType.instance);
        System.out.println("extensions.keys=" + extensions.keySet());
        
        IndexMetaData ksIndexMetaData = clusterService().getIndexMetaDataFromExtension(extensions.get("elastic_admin/ks"));
        assertEquals(1, ksIndexMetaData.getMappings().size());
        assertEquals(Boolean.TRUE, ksIndexMetaData.getSettings().getAsBoolean(IndexMetaData.INDEX_SETTING_VIRTUAL_SETTING.getKey(), false));
        assertNull(ksIndexMetaData.getSettings().get(IndexMetaData.INDEX_SETTING_VIRTUAL_INDEX_SETTING.getKey()));
        for(long i=20; i < 30; i++) {
            IndexMetaData indexMetaData = clusterService().getIndexMetaDataFromExtension(extensions.get("elastic_admin/ks_"+i));
            assertEquals(0, indexMetaData.getMappings().size() );
            assertEquals(Boolean.FALSE, indexMetaData.getSettings().getAsBoolean(IndexMetaData.INDEX_SETTING_VIRTUAL_SETTING.getKey(), false));
            assertEquals("ks", indexMetaData.getSettings().get(IndexMetaData.INDEX_SETTING_VIRTUAL_INDEX_SETTING.getKey()));
            
            IndexMetaData ksi = this.clusterService().state().metaData().index("ks_"+i);
            MappingMetaData mmd = ksi.getMappings().get("t1");
            Map<String, Object> mapping = mmd.getSourceAsMap();
            Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
            assertTrue(properties.containsKey("content"));
        }
        
        // create a new index with a mapping
        XContentBuilder mapping2 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("content2").field("type", "text").field("cql_collection","singleton").endObject()
                     .endObject()
                .endObject();
        createIndex("ks_"+30, Settings.builder()
                .put("index.keyspace","ks")
                .put("index.partition_function", "byage ks_%d age")
                .put("index.partition_function_class", "StringPartitionFunction")
                .put("index.virtual_index", "ks")
                .build(),"t1", mapping2);
        ensureGreen("ks_"+30);

        rs = process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "SELECT extensions FROM system_schema.tables WHERE keyspace_name='ks' AND table_name='t1'"));
        extensions = rs.one().getMap("extensions", UTF8Type.instance, BytesType.instance);
        for(long i=20; i < 31; i++) {
            Map<String, Object> md = client().admin().indices().prepareGetMappings("ks_"+i).addTypes("t1").get().getMappings().get("ks_"+i).get("t1").getSourceAsMap();
            Map<String, Object> properties = (Map<String, Object>) md.get("properties");
            assertEquals(Boolean.TRUE, properties.containsKey("content2"));
            IndexMetaData indexMetaDataX = clusterService().getIndexMetaDataFromExtension(extensions.get("elastic_admin/ks_"+i));
            assertEquals(0, indexMetaDataX.getMappings().size() );
            assertEquals(Boolean.FALSE, indexMetaDataX.getSettings().getAsBoolean(IndexMetaData.INDEX_SETTING_VIRTUAL_SETTING.getKey(), false));
            assertEquals("ks", indexMetaDataX.getSettings().get(IndexMetaData.INDEX_SETTING_VIRTUAL_INDEX_SETTING.getKey()));
        }
        
        IndexMetaData indexMetaDataKs = clusterService().getIndexMetaDataFromExtension(extensions.get("elastic_admin/ks"));
        MappingMetaData mmd = indexMetaDataKs.getMappings().get("t1");
        Map<String, Object> mapping = mmd.getSourceAsMap();
        Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
        assertTrue(properties.containsKey("content2"));
    }

}
