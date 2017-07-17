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
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

/**
 * Elassandra paque field test.
 * @author vroyer
 *
 */
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
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        SearchHits hits = client().prepareSearch().setIndices("my_index").setTypes("session")
                .setQuery(QueryBuilders.queryStringQuery("user_id:kimchy"))
                .get().getHits();
        
        assertThat(hits.getTotalHits(), equalTo(1L));
        assertThat(XContentFactory.jsonBuilder().map((Map<String,Object>)hits.getHits()[0].getSource().get("session_data")).string(), equalTo("{\"arbitrary_object\":{\"some_array\":[\"foo\",\"bar\",{\"baz\":2}]}}"));
        
        UntypedResultSet results = process(ConsistencyLevel.ONE,"SELECT session_data FROM my_index.session WHERE \"_id\"='session_1';");
        assertThat(results.size(),equalTo(1));
        assertThat(results.one().getString("session_data"),equalTo("{\"arbitrary_object\":{\"some_array\":[\"foo\",\"bar\",{\"baz\":2}]}}"));
    }

}
