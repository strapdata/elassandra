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

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

/**
 * Elassandra opaque field test.
 * @author vroyer
 *
 */
public class ObjectNotEnabledTests extends ESSingleNodeTestCase {

    @Test
    public void testNullDynamicField() throws Exception {
        XContentBuilder mapping1 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("foo")
                            .field("type", "keyword")
                        .endObject()
                    .endObject()
                .endObject();

        assertAcked(client().admin().indices().prepareCreate("my_index")
                .addMapping("_doc", mapping1)
                .get());
        ensureGreen("my_index");

        IndexResponse resp = client().prepareIndex("my_index", "_doc", "1").setSource("{\"foo\" : \"bar\"}", XContentType.JSON).get();
        assertThat(resp.getResult(), equalTo(DocWriteResponse.Result.CREATED));

        resp = client().prepareIndex("my_index", "_doc", "2").setSource("{\"foo\" : \"bar\", \"bar\":null }", XContentType.JSON).get();
        assertThat(resp.getResult(), equalTo(DocWriteResponse.Result.CREATED));
    }

    @Test
    public void testNotEnabled() throws Exception {
        XContentBuilder mapping1 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("last_updated")
                            .field("type", "date")
                            .field("format", "strict_date_optional_time||epoch_millis")
                        .endObject()
                        .startObject("session_data")
                            .field("type", "object")
                            .field("enabled", false)
                        .endObject()
                            .startObject("user_id")
                            .field("type", "keyword")
                        .endObject()
                    .endObject()
                .endObject();

        assertAcked(client().admin().indices().prepareCreate("my_index")
                .addMapping("_doc", mapping1)
                .get());
        ensureGreen("my_index");

        assertThat(client().prepareIndex("my_index", "_doc", "session_1")
                .setSource("{ \"user_id\": \"kimchy\"," +
                             "\"session_data\": { " +
                                 "\"arbitrary_object\": {" +
                                     "\"some_array\": [ \"foo\", \"bar\", { \"baz\": 2 } ]" +
                                 "}" +
                             "}," +
                            "\"last_updated\": \"2015-12-06T18:20:22\" }", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));

        SearchHits hits = client().prepareSearch().setIndices("my_index").setTypes("_doc")
                .setQuery(QueryBuilders.queryStringQuery("user_id:kimchy"))
                .get().getHits();

        assertThat(hits.getTotalHits(), equalTo(1L));
        assertThat(BytesReference.bytes(XContentFactory.jsonBuilder().map((Map<String,Object>)hits.getHits()[0].getSourceAsMap().get("session_data"))).utf8ToString(),
            equalTo("{\"arbitrary_object\":{\"some_array\":[\"foo\",\"bar\",{\"baz\":2}]}}"));

        UntypedResultSet results = process(ConsistencyLevel.ONE,"SELECT session_data FROM my_index.\"_doc\" WHERE \"_id\"='session_1';");
        assertThat(results.size(),equalTo(1));
        assertThat(results.one().getString("session_data"),equalTo("{\"arbitrary_object\":{\"some_array\":[\"foo\",\"bar\",{\"baz\":2}]}}"));
    }

    // #146
    @Test
    public void testEmptyEnabledObject() throws Exception {
        XContentBuilder mapping1 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("payload")
                            .field("type", "object")
                        .endObject()
                    .endObject()
                .endObject();
        XContentBuilder mapping2 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id").field("type", "keyword").field("cql_collection", "singleton").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                        .startObject("status")
                            .startObject("properties")
                                .startObject("payload")
                                    .field("type", "object")
                                .endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject();
        assertAcked(client().admin().indices().prepareCreate("test1").addMapping("_doc", mapping1));
        assertAcked(client().admin().indices().prepareCreate("test2").addMapping("_doc", mapping2));
        ensureGreen("test1");
        ensureGreen("test2");

        IndexResponse resp = client().prepareIndex("test1", "_doc", "1").setSource("{ \"payload\":{\"foo\" : \"bar\"}}", XContentType.JSON).get();
        assertThat(resp.getResult(), equalTo(DocWriteResponse.Result.CREATED));

        resp = client().prepareIndex("test2", "_doc", "1").setSource("{ \"status\":{ \"payload\":{\"foo\" : \"bar\"}}}", XContentType.JSON).get();
        assertThat(resp.getResult(), equalTo(DocWriteResponse.Result.CREATED));
    }

}
