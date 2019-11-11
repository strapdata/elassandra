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

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for various table options.
 * @author vroyer
 */
public class TableOptionsTests extends ESSingleNodeTestCase {

    @Test
    public void testTWCS() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("b")
                            .field("type", "double")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                .endObject();

        Settings settings = Settings.builder()
                .put("index.table_options",
                        "default_time_to_live = 30 " +
                        "AND compaction = {'compaction_window_size': '1', " +
                        "                  'compaction_window_unit': 'MINUTES', " +
                        "                  'class': 'org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy'}")
                .build();

        assertAcked(client().admin().indices().prepareCreate("ks").setSettings(settings).addMapping("t1", mapping).get());
        ensureGreen("ks");

        System.out.println("schema version="+StorageService.instance.getSchemaVersion());
        Thread.sleep(3000);
        UntypedResultSet.Row row = process(ConsistencyLevel.ONE, "select * from system_schema.tables where keyspace_name='ks';").one();
        assertThat(row.getInt("default_time_to_live"), equalTo(30));
        Map<String, String> compaction = row.getMap("compaction", UTF8Type.instance, UTF8Type.instance);
        assertThat(compaction.get("class"), equalTo("org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy"));
        assertThat(compaction.get("compaction_window_size"), equalTo("1"));
        assertThat(compaction.get("compaction_window_unit"), equalTo("MINUTES"));
    }
    
    @Test
    public void testDropColulmn() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("a").field("type", "keyword").field("cql_collection", "singleton").endObject()
                        .startObject("b").field("type", "keyword").field("cql_collection", "singleton").endObject()
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("my_type", mapping));
        ensureGreen("test");
        
        XContentBuilder mappingWithoutB = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("a").field("type", "keyword").field("cql_collection", "singleton").endObject()
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test2")
                .setSettings(Settings.builder().put("index.keyspace","test")).addMapping("my_type", mappingWithoutB));
        ensureGreen("test2");


        assertThat(client().prepareIndex("test", "my_type", "1")
                .setSource("{\"a\": \"a\",\"b\": \"b\"}", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        SearchResponse resp = client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get();
        assertThat(resp.getHits().getTotalHits(), equalTo(1L));
        
        SearchResponse resp2 = client().prepareSearch().setIndices("test2").setQuery(QueryBuilders.matchAllQuery()).get();
        assertThat(resp2.getHits().getTotalHits(), equalTo(1L));
        
        try {
            process(ConsistencyLevel.ONE,"ALTER TABLE test.my_type DROP b");
            fail("Should throw a InvalidRequestException: Cannot drop column b because it has dependent secondary indexes");
        } catch (org.apache.cassandra.exceptions.InvalidRequestException e) {
        }
        assertAcked(client().admin().indices().prepareDelete("test").get());
        process(ConsistencyLevel.ONE,"ALTER TABLE test.my_type DROP b");
    }

}

