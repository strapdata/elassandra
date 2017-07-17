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

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

/**
 * Elassandra Naming tests.
 * @author vroyer
 *
 */
public class NamingTests extends ESSingleNodeTestCase {
    
    @Test
    public void testNamedUDT() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("twitter")
                .addMapping("tweet", "{ \"tweet\" :{ \"properties\" : { "+
                                 "\"user\" : {"+
                                     "\"type\" : \"object\","+
                                     "\"cql_udt_name\" : \"mytype\","+
                                     "\"cql_collection\" : \"singleton\","+
                                     "\"properties\" : { " +
                                         "\"user_id\" : {"+
                                             "\"type\" : \"string\","+
                                             "\"index\" : \"not_analyzed\""+
                                         "}," +
                                         "\"user_email\" : {"+
                                             "\"type\" : \"string\","+
                                             "\"index\" : \"not_analyzed\""+
                                         "}" +
                                     "}" +
                                 "}," +
                                 "\"message\" : {"+
                                     "\"type\" : \"string\","+
                                     "\"index\" : \"not_analyzed\""+
                                 "}"+
                            "} }}")
                .get());
        ensureGreen("twitter");
        
        assertThat(client().prepareIndex("twitter", "tweet", "1")
                .setSource("{ \"user\": { \"user_id\": \"500\", \"user_email\":\"user@test.com\" }, \"message\": \"hello\" }")
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        assertThat(client().prepareGet().setIndex("twitter").setType("tweet").setId("1")
                .get().isExists(), equalTo(true));
        
        UntypedResultSet results = process(ConsistencyLevel.ONE,"SELECT field_names, field_types FROM system_schema.types WHERE keyspace_name = 'twitter' AND type_name = 'mytype'");
        assertThat(results.size(),equalTo(1));
    }

    @Test
    public void testIndexNameTranslation() throws Exception {
        assertThat(client().admin().indices().preparePutTemplate("test_template")
                .addMapping("_default_", "{ \"_default_\": { \"properties\": { \"attachment\": { \"type\": \"binary\" }}}}")
                .setTemplate("test_index-*")
                .get().isAcknowledged(), equalTo(true));
        
        assertThat(client().prepareIndex("test_index-2016.12.29", "tweet-info", "1")
                .setSource("{ \"user\": { \"user_id\": \"500\", \"user_email\":\"user@test.com\" }, \"message\": \"hello\" }")
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        ensureGreen("test_index-2016.12.29");

        assertThat(client().prepareGet().setIndex("test_index-2016.12.29").setType("tweet-info").setId("1").get().isExists(), equalTo(true));
        assertThat(client().prepareMultiGet().add("test_index-2016.12.29","tweet-info","1").get().getResponses().length, equalTo(1));
        assertThat(client().prepareSearch("test_index-2016.12.29").setTypes("tweet-info").setQuery(QueryBuilders.queryStringQuery("message:hello")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareDelete().setIndex("test_index-2016.12.29").setType("tweet-info").setId("1").get().getId(), equalTo("1"));
        assertThat(client().admin().indices().prepareDelete("test_index-2016.12.29").get().isAcknowledged(), equalTo(true));
        assertThat(client().admin().indices().prepareDeleteTemplate("test_template").get().isAcknowledged(), equalTo(true));
    }
    
    // ES Auto-Discovery fails when Cassandra table has at least one UDT field (#77)
    @Test
    public void testDiscoverUDT() throws Exception {
        createIndex("test");
        ensureGreen("test");
        
        process(ConsistencyLevel.ONE,"create type test.fullname (firstname text, lastname text);");
        process(ConsistencyLevel.ONE,"create table test.testudt (id uuid, name frozen<fullname>, primary key (id));");
        assertAcked(client().admin().indices().preparePutMapping("test").setType("testudt").setSource("{ \"testudt\" : { \"discover\" : \".*\" }}").get());
    }
}
