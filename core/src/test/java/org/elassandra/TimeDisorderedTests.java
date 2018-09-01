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

import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Because of distribution (and Hinted Handoff) Cassandra may write events not properly time-ordered. 
 * This could lead to index in elasticsearch the last written row and not the up-to-date row.
 * @author vroyer
 *
 */
//gradle :core:test -Dtests.seed=65E2CF27F286CC89 -Dtests.class=org.elassandra.TimeDisorderedTests -Dtests.security.manager=false -Dtests.locale=en-PH -Dtests.timezone=America/Coral_Harbour
public class TimeDisorderedTests extends ESSingleNodeTestCase {

    @Test
    public void testSkinnyTimeDisordered() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id")
                            .field("type", "keyword")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 0)
                            .field("cql_partition_key", true)
                        .endObject()
                        .startObject("f1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("t1", mapping));
        ensureGreen("test");
        
        long now = new Date().getTime();
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '1' ", now));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id, f1) VALUES ('1',1) USING TIMESTAMP %d", now - 1000));
        SearchResponse resp = client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get();
        assertThat(resp.getHits().getTotalHits(), equalTo(0L));
        
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id, f1) VALUES ('2',2) USING TIMESTAMP %d", now));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '2' ", now - 1500));
        SearchResponse resp2 = client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get();
        assertThat(resp2.getHits().getTotalHits(), equalTo(1L));
    }
    
    @Test
    public void testWideTimeDisordered() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id")
                            .field("type", "keyword")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 0)
                            .field("cql_partition_key", true)
                        .endObject()
                        .startObject("c1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 1)
                            .field("cql_partition_key", false)
                        .endObject()
                        .startObject("f1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("t1", mapping));
        ensureGreen("test");
        
        long now = new Date().getTime();
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('1',0,0) USING TIMESTAMP %d", now));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '1' and c1 = 1", now));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('1',1,1) USING TIMESTAMP %d", now - 2000)); // not visible
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(1L));
        
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('2',1,1) USING TIMESTAMP %d", now)); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '2' and c1 = 1", now - 2000));
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2L));
        
        // play range delete before a previous-insert
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '3' and c1 > 0 AND c1 <= 2", now));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('3',0,0) USING TIMESTAMP %d", now - 1000)); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('3',1,1) USING TIMESTAMP %d", now - 1000)); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('3',2,2) USING TIMESTAMP %d", now - 1000)); // not visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('3',3,3) USING TIMESTAMP %d", now - 1000)); // not visible
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.termQuery("id", 3)).get().getHits().getTotalHits(), equalTo(2L));
        
        // play insert before an obsolete delete
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('4',0,0) USING TIMESTAMP %d", now )); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('4',1,1) USING TIMESTAMP %d", now)); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('4',2,2) USING TIMESTAMP %d", now)); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1) VALUES ('4',3,3) USING TIMESTAMP %d", now)); // visible
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '4' and c1 > 0 AND c1 <= 2", now - 1000)); // not visible
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.termQuery("id", 4)).get().getHits().getTotalHits(), equalTo(4L));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 WHERE id = '4' and c1 > 0 AND c1 <= 2")); // visible
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.termQuery("id", 4)).get().getHits().getTotalHits(), equalTo(2L));
    }
    
    @Test
    public void testWideWithStaticTimeDisordered() throws Exception {
        XContentBuilder mappingStaticCol = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id")
                            .field("type", "keyword")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 0)
                            .field("cql_partition_key", true)
                        .endObject()
                        .startObject("c1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 1)
                            .field("cql_partition_key", false)
                        .endObject()
                        .startObject("s1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_static_column", true)
                        .endObject()
                        .startObject("f1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                    .startObject("_meta").field("index_static_columns", true).endObject()
                .endObject();
        
        XContentBuilder mappingStaticDoc = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id")
                            .field("type", "keyword")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 0)
                            .field("cql_partition_key", true)
                        .endObject()
                        .startObject("c1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 1)
                            .field("cql_partition_key", false)
                        .endObject()
                        .startObject("s1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_static_column", true)
                        .endObject()
                        .startObject("f1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                    .startObject("_meta").field("index_static_document", true).endObject()
                .endObject();
        
        XContentBuilder mappingStaticDocAndCol = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id")
                            .field("type", "keyword")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 0)
                            .field("cql_partition_key", true)
                        .endObject()
                        .startObject("c1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 1)
                            .field("cql_partition_key", false)
                        .endObject()
                        .startObject("s1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_static_column", true)
                        .endObject()
                        .startObject("f1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                    .startObject("_meta")
                        .field("index_static_columns",true)
                        .field("index_static_document",true)
                    .endObject()
                .endObject();
        
        XContentBuilder mappingStaticDocOnly= XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id")
                            .field("type", "keyword")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 0)
                            .field("cql_partition_key", true)
                        .endObject()
                        .startObject("c1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_primary_key_order", 1)
                            .field("cql_partition_key", false)
                        .endObject()
                        .startObject("s1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                            .field("cql_static_column", true)
                        .endObject()
                        .startObject("f1")
                            .field("type", "integer")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                    .startObject("_meta")
                        .field("index_static_only", true)
                        .field("index_static_document", true)
                    .endObject()
                .endObject();
        
        assertAcked(client().admin().indices().prepareCreate("test1")
                .setSettings(Settings.builder().put("index.keyspace","test").build())
                .addMapping("t1", mappingStaticCol));
        assertAcked(client().admin().indices().prepareCreate("test2")
                .setSettings(Settings.builder().put("index.keyspace","test").build())
                .addMapping("t1", mappingStaticDoc));
        assertAcked(client().admin().indices().prepareCreate("test3")
                .setSettings(Settings.builder().put("index.keyspace","test").build())
                .addMapping("t1", mappingStaticDocAndCol));
        assertAcked(client().admin().indices().prepareCreate("test4")
                .setSettings(Settings.builder().put("index.keyspace","test").build())
                .addMapping("t1", mappingStaticDocOnly));
        
        ensureGreen("test1");
        ensureGreen("test2");
        ensureGreen("test3");
        ensureGreen("test4");
        
        long now = new Date().getTime();
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1,s1) VALUES ('1',0,0,0) USING TIMESTAMP %d", now));
        
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '1' and c1 = 1", now ));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1,s1) VALUES ('1',1,1,0) USING TIMESTAMP %d", now - 1000)); // not visible
        
        assertThat( client().prepareSearch().setIndices("test1").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat( client().prepareSearch().setIndices("test2").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat( client().prepareSearch().setIndices("test3").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat( client().prepareSearch().setIndices("test4").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(1L));
        
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1,s1) VALUES ('2',1,1,0) USING TIMESTAMP %d", now));
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "DELETE FROM test.t1 USING TIMESTAMP %d WHERE id = '2' and c1 = 1", now - 1500));
        
        assertThat( client().prepareSearch().setIndices("test1").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat( client().prepareSearch().setIndices("test2").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(4L));
        assertThat( client().prepareSearch().setIndices("test3").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(4L));
        assertThat( client().prepareSearch().setIndices("test4").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(2L));
        
        // remove a static doc
        process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id,c1,f1,s1) VALUES ('2',1,1,null)"));
        assertThat( client().prepareSearch().setIndices("test1").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat( client().prepareSearch().setIndices("test2").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat( client().prepareSearch().setIndices("test3").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat( client().prepareSearch().setIndices("test4").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("s1:0")).get().getHits().getTotalHits(), equalTo(1L));
    }
}

