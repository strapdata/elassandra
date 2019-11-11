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
import org.apache.cassandra.service.StorageService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra composite key tests.
 * @author vroyer
 *
 */
//gradle :server:test -Dtests.seed=65E2CF27F286CC89 -Dtests.class=org.elassandra.CompositeTests -Dtests.security.manager=false -Dtests.locale=en-PH -Dtests.timezone=America/Coral_Harbour
public class CompositeTests extends ESSingleNodeTestCase {

    @Test
    public void testCompositeWithStaticColumnTest() throws Exception {

        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("id").field("type", "integer").field("cql_collection", "singleton").field("index", false).field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                    .startObject("surname").field("type", "text").field("cql_collection", "singleton").field("cql_primary_key_order", 1).field("cql_partition_key", true).endObject()
                    .startObject("name").field("type", "text").field("cql_collection", "singleton").field("cql_primary_key_order", 2).endObject()
                    .startObject("phonetic_name").field("type", "text").field("cql_collection", "singleton").field("cql_static_column", true).endObject()
                    .startObject("nicks").field("type", "text").field("cql_collection", "list").endObject()
                .endObject()
                .startObject("_meta")
                    .field("index_static_document",true)
                .endObject()
            .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").setSettings(Settings.builder().put("index.token_ranges_bitset_cache",false).build()).addMapping("t2", mapping));
        ensureGreen("test");

        // INSERT INTO test.t2 (id, surname, name, phonetic_name, nicks) VALUES (22, 'Genesis', 'Abraham', 'ai-b-ram', ['the-A', 'ab'])
        process(ConsistencyLevel.ONE,"INSERT INTO test.t2 (id, surname, name, phonetic_name, nicks) VALUES (22, 'Genesis', 'Abraham', 'ai-b-ram', ['the-A', 'ab'])");

        SearchResponse rsp = client().prepareSearch().setQuery(QueryBuilders.matchAllQuery()).get();
        assertThat(rsp.getHits().getTotalHits(), equalTo(2L));
    }

    // mvn test -Pdev -pl com.strapdata.elasticsearch:elasticsearch -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.CompositeTests -Dtests.method="testCompositeTest" -Des.logger.level=ERROR -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
    @Test
    public void testCompositeTest() throws Exception {
        testCompositeTest(false);
    }

    @Test
    public void testCompositeTestWithFlush() throws Exception {
        testCompositeTest(true);
    }

    public void testCompositeTest(boolean flush) throws Exception {
        Settings compositSettings = Settings.builder()
            .put("index.token_ranges_bitset_cache", false)
            .put("index.index_static_document", true)
            .put("index.index_static_columns", true)
            .build();

        for(String s : new String[] { "1","2","3","4","11","12","13"})
            createIndex("composite"+s, compositSettings);
        for(String s : new String[] { "1","2","3","4","11","12","13"})
            ensureGreen("composite"+s);

        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite1.t1 ( a text,b text,c bigint,f float,primary key ((a),b) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite2.t2 ( a text,b text,c bigint,d bigint,primary key ((a),b,c) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite3.t3 ( a text,b text,c bigint,d bigint,primary key ((a,b),c) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite4.t4 ( a text,b text,c bigint,d bigint, e double, primary key ((a,b),c,d) )");

        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite11.t11 ( a text,b text,c bigint,f float, s1 text static, primary key ((a),b) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite12.t12 ( a text,b text,c bigint,d bigint,s1 text static, primary key ((a),b,c) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite13.t13 ( a text,b text,c bigint,d bigint,s1 text static, primary key ((a,b),c) )");

        assertAcked(client().admin().indices().preparePutMapping("composite1").setType("t1").setSource("{ \"t1\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());
        assertAcked(client().admin().indices().preparePutMapping("composite2").setType("t2").setSource("{ \"t2\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());
        assertAcked(client().admin().indices().preparePutMapping("composite3").setType("t3").setSource("{ \"t3\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());
        assertAcked(client().admin().indices().preparePutMapping("composite4").setType("t4").setSource("{ \"t4\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());

        assertAcked(client().admin().indices().preparePutMapping("composite11").setType("t11").setSource("{ \"t11\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());
        assertAcked(client().admin().indices().preparePutMapping("composite12").setType("t12").setSource("{ \"t12\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());
        assertAcked(client().admin().indices().preparePutMapping("composite13").setType("t13").setSource("{ \"t13\" : { \"discover\" : \".*\" }}", XContentType.JSON).get());

        process(ConsistencyLevel.ONE,"insert into composite1.t1 (a,b,c,f) VALUES ('a','b1',1, 1.2)");
        process(ConsistencyLevel.ONE,"insert into composite1.t1 (a,b,c,f) VALUES ('b','b1',2, 5);");

        process(ConsistencyLevel.ONE,"insert into composite2.t2 (a,b,c,d) VALUES ('a','b2',2,1)");
        process(ConsistencyLevel.ONE,"insert into composite2.t2 (a,b,c,d) VALUES ('a','b2',3,1)");

        for(int i=0; i < 10; i++) {
            process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "insert into composite3.t3 (a,b,c,d) VALUES ('a','b3',%s,%s)",i,i));
            process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "insert into composite4.t4 (a,b,c,d,e) VALUES ('a','b3',%s,%s,0.0)",i,i));
        }

        process(ConsistencyLevel.ONE,"insert into composite11.t11 (a,b,c,f,s1) VALUES ('a','b1',1, 1.2, 'a')");
        process(ConsistencyLevel.ONE,"insert into composite11.t11 (a,b,c,f,s1) VALUES ('b','b1',2, 5, 'b');");

        process(ConsistencyLevel.ONE,"insert into composite12.t12 (a,b,c,d,s1) VALUES ('a','b2',2,1, 'a1')");
        process(ConsistencyLevel.ONE,"insert into composite12.t12 (a,b,c,d,s1) VALUES ('a','b2',3,1, 'a2')");

        process(ConsistencyLevel.ONE,"insert into composite13.t13 (a,b,c,d,s1) VALUES ('a','b3',2,3, 'ab1')");
        process(ConsistencyLevel.ONE,"insert into composite13.t13 (a,b,c,d,s1) VALUES ('a','b3',3,3, 'ab2')");
        process(ConsistencyLevel.ONE,"insert into composite13.t13 (a,b,c,d,s1) VALUES ('a','b3',4,4, 'ab3')");
        process(ConsistencyLevel.ONE,"insert into composite13.t13 (a,b,c,d,s1) VALUES ('a','b3',5,5, 'ab4')");
        process(ConsistencyLevel.ONE,"insert into composite13.t13 (a,b,c,d,s1) VALUES ('a','b3',6,6, 'ab5')");

        // flushing change read before write results on delete operations
        if (flush) {
            for(String s : new String[] { "1","2","3","4","11","12","13"})
                StorageService.instance.forceKeyspaceFlush("composite"+s);
        }


        assertThat(client().prepareGet().setIndex("composite1").setType("t1").setId("[\"a\",\"b1\"]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite2").setType("t2").setId("[\"a\",\"b2\",2]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite3").setType("t3").setId("[\"a\",\"b3\",2]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite4").setType("t4").setId("[\"a\",\"b3\",2, 2]").get().isExists(),equalTo(true));

        assertThat(client().prepareGet().setIndex("composite11").setType("t11").setId("[\"a\",\"b1\"]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite12").setType("t12").setId("[\"a\",\"b2\",2]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite13").setType("t13").setId("[\"a\",\"b3\",2]").get().isExists(),equalTo(true));

        assertThat(client().prepareSearch().setIndices("composite1").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("c:1")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("composite2").setTypes("t2").setQuery(QueryBuilders.queryStringQuery("d:1")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("d:3")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("composite4").setTypes("t4").setQuery(QueryBuilders.queryStringQuery("d:4")).get().getHits().getTotalHits(), equalTo(1L));

        assertThat(client().prepareSearch().setIndices("composite11").setTypes("t11").setQuery(QueryBuilders.queryStringQuery("c:1")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("composite12").setTypes("t12").setQuery(QueryBuilders.queryStringQuery("d:1")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat(client().prepareSearch().setIndices("composite13").setTypes("t13").setQuery(QueryBuilders.queryStringQuery("d:3")).get().getHits().getTotalHits(), equalTo(2L));

        assertThat(client().prepareSearch().setIndices("composite11").setTypes("t11").setQuery(QueryBuilders.queryStringQuery("s1:b")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat(client().prepareSearch().setIndices("composite12").setTypes("t12").setQuery(QueryBuilders.queryStringQuery("s1:a2")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat(client().prepareSearch().setIndices("composite13").setTypes("t13").setQuery(QueryBuilders.queryStringQuery("s1:ab5")).get().getHits().getTotalHits(), equalTo(2L));

        assertThat(client().prepareMultiGet().add("composite1", "t1", "[\"a\",\"b1\"]", "[\"b\",\"b1\"]").get().getResponses()[0].getIndex(), equalTo("composite1") );
        assertThat(client().prepareMultiGet().add("composite2", "t2", "[\"a\",\"b2\",2]", "[\"a\",\"b2\",3]").get().getResponses()[0].getIndex(), equalTo("composite2") );
        assertThat(client().prepareMultiGet().add("composite3", "t3", "[\"a\",\"b3\",2]", "[\"a\",\"b3\",3]").get().getResponses()[0].getIndex(), equalTo("composite3")  );
        assertThat(client().prepareMultiGet().add("composite4", "t4", "[\"a\",\"b3\",2,2]", "[\"a\",\"b3\",3,3]").get().getResponses()[0].getIndex(), equalTo("composite4")  );

        assertThat(client().prepareMultiGet().add("composite11", "t11", "[\"a\",\"b1\"]", "[\"b\",\"b1\"]").get().getResponses()[0].getIndex(), equalTo("composite11") );
        assertThat(client().prepareMultiGet().add("composite12", "t12", "[\"a\",\"b2\",2]", "[\"a\",\"b2\",3]").get().getResponses()[0].getIndex(), equalTo("composite12") );
        assertThat(client().prepareMultiGet().add("composite13", "t13", "[\"a\",\"b3\",2]", "[\"a\",\"b3\",3]").get().getResponses()[0].getIndex(), equalTo("composite13")  );

        // delete with partition key
        assertThat(client().prepareSearch().setIndices("composite1").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite1.t1 WHERE a='a'");
        assertThat(client().prepareSearch().setIndices("composite1").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(1L));

        // delete with primary key
        assertThat(client().prepareSearch().setIndices("composite2").setTypes("t2").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite2.t2 WHERE a='a' AND b='b2' AND c=2");
        assertThat(client().prepareSearch().setIndices("composite2").setTypes("t2").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(1L));

        // delete with primary key
        process(ConsistencyLevel.ONE,"DELETE FROM composite3.t3 WHERE a='a' AND b='b3' AND c = 4");
        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(9L));

        // t3 delete a slice with composite partition key
        process(ConsistencyLevel.ONE,"DELETE FROM composite3.t3 WHERE a='a' AND b='b3' AND c >= 3 AND c <= 5");
        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(7L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite3.t3 WHERE a='a' AND b='b3' AND c <= 5");
        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(4L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite3.t3 WHERE a='a' AND b='b3' AND c > 6");
        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(1L));

        // t4 delete a slice with composite partition and a composite clustering keys
        process(ConsistencyLevel.ONE,"DELETE FROM composite4.t4 WHERE a='a' AND b='b3' AND c = 4");
        assertThat(client().prepareSearch().setIndices("composite4").setTypes("t4").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(9L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite4.t4 WHERE a='a' AND b='b3' AND c >= 3 AND c <= 5");
        assertThat(client().prepareSearch().setIndices("composite4").setTypes("t4").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(7L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite4.t4 WHERE a='a' AND b='b3' AND c >= 7");
        assertThat(client().prepareSearch().setIndices("composite4").setTypes("t4").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(4L));
        process(ConsistencyLevel.ONE,"DELETE FROM composite4.t4 WHERE a='a' AND b='b3' AND c <= 3");
        assertThat(client().prepareSearch().setIndices("composite4").setTypes("t4").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(1L));

        // truncate content
        process(ConsistencyLevel.ONE,"TRUNCATE composite3.t3");
        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(0L));
        process(ConsistencyLevel.ONE,"TRUNCATE composite4.t4");
        assertThat(client().prepareSearch().setIndices("composite4").setTypes("t4").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(0L));

        // test rebuild index
        assertAcked(client().admin().indices().prepareClose("composite3").get());
        process(ConsistencyLevel.ONE,"insert into composite3.t3 (a,b,c,d) VALUES ('a','b3',2,3)");
        process(ConsistencyLevel.ONE,"insert into composite3.t3 (a,b,c,d) VALUES ('a','b3',3,3)");
        process(ConsistencyLevel.ONE,"insert into composite3.t3 (a,b,c,d) VALUES ('a','b3',4,4)");
        assertAcked(client().admin().indices().prepareOpen("composite3").get());
        ensureGreen("composite3");

        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(0L));

        StorageService.instance.forceKeyspaceFlush("composite3", "t3");
        StorageService.instance.rebuildSecondaryIndex("composite3", "t3", "elastic_t3_idx");

        assertThat(client().prepareSearch().setIndices("composite3").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(3L));

        // delete index
        assertAcked(client().admin().indices().prepareDelete("composite3").get());
    }


/*

 curl -XPUT "http://localhost:9200/test" -d '{
   "mappings" : {
       "timeseries" : {
         "properties" : {
           "t" : {
             "type" : "date",
             "format" : "strict_date_optional_time||epoch_millis",
             "cql_primary_key_order" : 1,
             "cql_collection" : "singleton"
           },
           "meta" : {
             "type" : "nested",
             "cql_struct" : "map",
             "cql_static_column" : true,
             "cql_collection" : "singleton",
             "include_in_parent" : true,
             "properties" : {
               "region" : {
                 "type" : "string"
               }
             }
           },
           "v" : {
             "type" : "double",
             "cql_collection" : "singleton"
           },
           "m" : {
             "type" : "string",
             "cql_partition_key" : true,
             "cql_primary_key_order" : 0,
             "cql_collection" : "singleton"
           }
         }
       }
  }
}'

INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:30', 10);
INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:31', 20);
INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:32', 15);
INSERT INTO test.timeseries (m, meta) VALUES ('server1-cpu', { 'region':'west' } );

curl -XGET "http://$NODE:9200/test/timeseries/_search?pretty=true&q=v:10&fields=m,t,v,meta.region"
curl -XGET "http://$NODE:9200/test/timeseries/_search?pretty=true&q=meta.region:west&fields=m,t,v,meta.region"

 */

    @Test
    public void testTimeserieWithStaticTest() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("m").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("t").field("type", "date").field("cql_collection", "singleton").field("cql_primary_key_order", 1).endObject()
                        .startObject("v").field("type", "double").field("cql_collection", "singleton").endObject()
                        .startObject("meta").field("type", "nested").field("cql_collection", "singleton").field("cql_struct", "map").field("cql_static_column", true).field("include_in_parent", true)
                            .startObject("properties")
                                .startObject("region").field("type", "text").endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("_meta").field("index_static_document",true).endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("timeseries", mapping));
        ensureGreen("test");

        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:30', 10);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:31', 20);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:32', 15);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, meta) VALUES ('server1-cpu', { 'region':'west' } );");

        SearchResponse rsp = client().prepareSearch().setIndices("test").setTypes("timeseries")
                .setQuery(QueryBuilders.termQuery("v", 20))
                .setFetchSource(new String[] { "m",  "t", "v", "meta.region"}, null)
                .get();
        SearchHits hits = rsp.getHits();
        Map<String, Object> source = hits.getHits()[0].getSourceAsMap();
        assertThat(hits.getTotalHits(), equalTo(1L));
        assertThat(source.get("m"), equalTo("server1-cpu"));
        assertThat(((Map)source.get("meta")).get("region"), equalTo("west"));

        rsp = client().prepareSearch().setIndices("test").setTypes("timeseries")
                .setQuery(QueryBuilders.termQuery("meta.region","west"))
                .setFetchSource(new String[] { "m", "meta.region"}, null)
                .get();
        hits = rsp.getHits();
        assertThat(hits.getTotalHits(), equalTo(1L));
        source = hits.getHits()[0].getSourceAsMap();
        assertThat(source.get("m"), equalTo("server1-cpu"));
        assertThat(((Map)source.get("meta")).get("region"), equalTo("west"));
    }

    @Test
    public void testTimeserieIndexStaticColumnsTest() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("m").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("t").field("type", "date").field("cql_collection", "singleton").field("cql_primary_key_order", 1).endObject()
                        .startObject("v").field("type", "double").field("cql_collection", "singleton").endObject()
                        .startObject("meta").field("type", "nested").field("cql_collection", "singleton").field("cql_struct", "map").field("cql_static_column", true).field("include_in_parent", true)
                            .startObject("properties")
                                .startObject("region").field("type", "text").endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("_meta")
                        .field("index_static_columns", true)
                    .endObject()
                .endObject();
        XContentBuilder mapping2 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("m").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("t").field("type", "date").field("cql_collection", "singleton").field("cql_primary_key_order", 1).endObject()
                        .startObject("v").field("type", "double").field("cql_collection", "singleton").endObject()
                        .startObject("meta").field("type", "nested").field("cql_collection", "singleton").field("cql_struct", "map").field("cql_static_column", true).field("include_in_parent", true)
                            .startObject("properties")
                                .startObject("region").field("type", "text").endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("_meta")
                        .field("index_static_columns",false)
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("timeseries", mapping));
        assertAcked(client().admin().indices().prepareCreate("test2").setSettings(Settings.builder().put("index.keyspace","test").build()).addMapping("timeseries", mapping2));
        ensureGreen("test");
        ensureGreen("test2");

        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, meta) VALUES ('server1-cpu', { 'region':'west' } );");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:30', 10);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:31', 20);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:32', 15);");

        SearchResponse rsp = client().prepareSearch().setIndices("test").setTypes("timeseries")
                .setQuery(QueryBuilders.queryStringQuery("meta.region:west"))
                .setFetchSource(new String[] { "m",  "t", "v", "meta.region"}, null)
                .get();
        SearchHits hits = rsp.getHits();
        Map<String, Object> source = hits.getHits()[0].getSourceAsMap();
        assertThat(hits.getTotalHits(), equalTo(3L));
        assertThat(source.get("m"), equalTo("server1-cpu"));
        assertThat(((Map)source.get("meta")).get("region"), equalTo("west"));

        // check static column is ot indexed when index_static_columns=false
        SearchResponse rsp2 = client().prepareSearch().setIndices("test2").setTypes("timeseries")
                .setQuery(QueryBuilders.queryStringQuery("meta.region:west"))
                .setFetchSource(new String[] { "m",  "t", "v", "meta.region"}, null)
                .get();
        assertThat(rsp2.getHits().getTotalHits(), equalTo(0L));
    }

    @Test
    public void testTimeserieWithIndexedStaticOnlyTest() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("m").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("t").field("type", "date").field("cql_collection", "singleton").field("cql_primary_key_order", 1).endObject()
                        .startObject("v").field("type", "double").field("cql_collection", "singleton").endObject()
                        .startObject("meta").field("type", "nested").field("cql_collection", "singleton").field("cql_struct", "map").field("cql_static_column", true).field("include_in_parent", true)
                            .startObject("properties")
                                .startObject("region").field("type", "keyword").endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("_meta")
                    .field("index_static_columns",true)
                    .field("index_static_document",true)
                    .field("index_static_only",true)
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("timeseries", mapping));
        ensureGreen("test");

        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, meta) VALUES ('server1-cpu', { 'region':'west' } );");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:30', 10);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:31', 20);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:32', 15);");

        assertThat(client().prepareSearch().setIndices("test").setTypes("timeseries").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(1L));

        SearchResponse rsp = client().prepareSearch().setIndices("test").setTypes("timeseries")
                .setQuery(QueryBuilders.nestedQuery("meta", QueryBuilders.matchQuery("meta.region", "west"), ScoreMode.Avg))
                .setFetchSource(new String[] { "m",  "t", "v", "meta.region"}, null)
                .get();
        SearchHits hits = rsp.getHits();
        assertThat(hits.getTotalHits(), equalTo(1L));
        Map<String, Object> source = hits.getHits()[0].getSourceAsMap();
        assertThat(source.get("m"), equalTo("server1-cpu"));
        assertThat(((Map)source.get("meta")).get("region"), equalTo("west"));
    }

    @Test
    public void testTimeserieEmptyTest() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("m").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("t").field("type", "date").field("cql_collection", "singleton").field("cql_primary_key_order", 1).endObject()
                        .startObject("v").field("type", "double").field("cql_collection", "singleton").endObject()
                        .startObject("meta").field("type", "nested").field("cql_collection", "singleton").field("cql_struct", "map").field("cql_static_column", true).field("include_in_parent", true)
                            .startObject("properties")
                                .startObject("region").field("type", "text").endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("_meta")
                        .field("index_static_only",true) // and index_static_document=false by default => index nothing
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("timeseries", mapping));
        ensureGreen("test");

        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:30', 10);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:31', 20);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, t, v) VALUES ('server1-cpu', '2016-04-10 13:32', 15);");
        process(ConsistencyLevel.ONE,"INSERT INTO test.timeseries (m, meta) VALUES ('server1-cpu', { 'region':'west' } );");

        SearchResponse rsp = client().prepareSearch().setIndices("test").setTypes("timeseries")
                .setQuery(QueryBuilders.matchAllQuery())
                .setFetchSource(new String[] { "m",  "t", "v", "meta.region"}, null)
                .get();
        SearchHits hits = rsp.getHits();
        assertThat(hits.getTotalHits(), equalTo(0L));
    }

}
