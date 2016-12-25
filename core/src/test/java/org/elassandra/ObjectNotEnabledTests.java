package org.elassandra;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

public class ObjectNotEnabledTests extends ESSingleNodeTestCase {
    
    @Test
    public void testNotEnabled() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("my_index")
                .addMapping("session", "{ \"session\" :{ \"properties\" : { "+
                                "\"last_updated\" : {" +
                                    "\"type\" : \"date\","+
                                    "\"format\" : \"strict_date_optional_time||epoch_millis\""+
                                 "},"+
                                 "\"session_data\" : {"+
                                     "\"type\" : \"object\","+
                                     "\"enabled\" : false"+
                                 "},"+
                                 "\"user_id\" : {"+
                                     "\"type\" : \"string\","+
                                     "\"index\" : \"not_analyzed\""+
                                 "}"+
                            "} }}")
                .get());
        ensureGreen("my_index");
        
        assertThat(client().prepareIndex("my_index", "session", "session_1")
                .setSource("{ \"user_id\": \"kimchy\"," +
                             "\"session_data\": { " +
                                 "\"arbitrary_object\": {" +
                                     "\"some_array\": [ \"foo\", \"bar\", { \"baz\": 2 } ]" +
                                 "}" +
                             "}," +
                            "\"last_updated\": \"2015-12-06T18:20:22\" }")
                .get().isCreated(), equalTo(true));
        
        assertThat(client().prepareSearch().setIndices("my_index").setTypes("session")
                .setQuery(QueryBuilders.queryStringQuery("user_id:kimchy"))
                .get().getHits().getTotalHits(), equalTo(1L));
        
        UntypedResultSet results = process(ConsistencyLevel.ONE,"SELECT session_data FROM my_index.session WHERE \"_id\"='session_1';");
        assertThat(results.size(),equalTo(1));
        assertThat(results.one().getString("session_data"),equalTo("{\"arbitrary_object\":{\"some_array\":[\"foo\",\"bar\",{\"baz\":2}]}}"));
    }

}
