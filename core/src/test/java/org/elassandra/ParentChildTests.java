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

import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;

/**
 * Elassandra parent-child tests.
 * @author vroyer
 *
 */
public class ParentChildTests extends ESSingleNodeTestCase {
    
    public void testESParentChildTest() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("company")
                .addMapping("branch", "{ \"branch\": {}}")
                .addMapping("employee", "{ \"employee\" :{ \"_parent\": { \"type\": \"branch\" } }}")
                .get());
        ensureGreen("company");
        
        assertThat(client().prepareIndex("company", "branch", "london")
                .setSource("{ \"district\": \"London Westminster\", \"city\": \"London\", \"country\": \"UK\" }")
                .get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("company", "branch", "liverpool")
                .setSource("{ \"district\": \"Liverpool Central\", \"city\": \"Liverpool\", \"country\": \"UK\" }")
                .get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("company", "branch", "paris")
                .setSource("{ \"district\": \"Champs Élysées\", \"city\": \"Paris\", \"country\": \"France\" }")
                .get().isCreated(), equalTo(true));
     
        assertThat(client().prepareIndex("company", "employee", "1").setParent("london")
                .setSource("{ \"name\":  \"Alice Smith\", \"dob\":   \"1970-10-24\", \"hobby\": \"hiking\" }")
                .get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("company", "employee", "2").setParent("london")
                .setSource("{ \"name\":  \"Bob Robert\", \"dob\":   \"1970-10-24\",  \"hobby\": \"hiking\" }")
                .get().isCreated(), equalTo(true));
        
        assertThat(client().prepareSearch().setIndices("company").setTypes("branch")
                .setQuery(QueryBuilders.hasChildQuery("employee", QueryBuilders.rangeQuery("dob").gte("1970-01-01"))).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("company").setTypes("employee")
                .setQuery(QueryBuilders.hasParentQuery("branch", QueryBuilders.matchQuery("country","UK"))).get().getHits().getTotalHits(), equalTo(2L));
    }


    public void testCQLParentChildTest() throws Exception {
        process(ConsistencyLevel.ONE,"CREATE KEYSPACE IF NOT EXISTS company3 WITH replication={ 'class':'NetworkTopologyStrategy', 'DC1':'1' }");
        process(ConsistencyLevel.ONE,"CREATE TABLE company3.employee (branch text,\"_id\" text,name text,dob timestamp,hobby text,primary key ((branch),\"_id\"))");
        
        assertAcked(client().admin().indices().prepareCreate("company3")
                .addMapping("branch", "{ \"branch\": {}}")
                .addMapping("employee", "{ \"employee\" : { \"discover\" : \".*\", \"_parent\" : { \"type\": \"branch\", \"cql_parent_pk\":\"branch\" } }}")
                .get());
        ensureGreen("company3");
        
        assertThat(client().prepareIndex("company3", "branch", "london")
                .setSource("{ \"district\": \"London Westminster\", \"city\": \"London\", \"country\": \"UK\" }")
                .get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("company3", "branch", "liverpool")
                .setSource("{ \"district\": \"Liverpool Central\", \"city\": \"Liverpool\", \"country\": \"UK\" }")
                .get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("company3", "branch", "paris")
                .setSource("{ \"district\": \"Champs Élysées\", \"city\": \"Paris\", \"country\": \"France\" }")
                .get().isCreated(), equalTo(true));
     
        process(ConsistencyLevel.ONE,"INSERT INTO company3.employee (branch,\"_id\",name,dob,hobby) VALUES ('london','1','Alice Smith','1970-10-24','hiking')");
        process(ConsistencyLevel.ONE,"INSERT INTO company3.employee (branch,\"_id\",name,dob,hobby) VALUES ('london','2','Bob Robert','1970-10-24','hiking')");
        
        assertThat(client().prepareSearch().setIndices("company3").setTypes("branch")
                .setQuery(QueryBuilders.hasChildQuery("employee", QueryBuilders.rangeQuery("dob").gte("1970-01-01"))).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("company3").setTypes("employee")
                .setQuery(QueryBuilders.hasParentQuery("branch", QueryBuilders.matchQuery("country","UK"))).get().getHits().getTotalHits(), equalTo(2L));
    }

}
