package org.elassandra;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

public class NamedUdtTests extends ESSingleNodeTestCase {
    
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
                .get().isCreated(), equalTo(true));
        
        assertThat(client().prepareGet().setIndex("twitter").setType("tweet").setId("1")
                .get().isExists(), equalTo(true));
        
        UntypedResultSet results = process(ConsistencyLevel.ONE,"SELECT field_names, field_types FROM system.schema_usertypes WHERE keyspace_name = 'twitter' AND type_name = 'mytype'");
        assertThat(results.size(),equalTo(1));
    }

}
