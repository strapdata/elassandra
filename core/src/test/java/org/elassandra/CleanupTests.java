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
//gradle :core:test -Dtests.seed=65E2CF27F286CC89 -Dtests.class=org.elassandra.CleanupTests -Dtests.security.manager=false -Dtests.locale=en-PH -Dtests.timezone=America/Coral_Harbour
public class CleanupTests extends ESSingleNodeTestCase {

    @Test
    public void testSkinnyCleanup() throws Exception {
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
        
        long N = 100;
        for(int i=0; i < N; i++)
            process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id, f1) VALUES ('%d',%d)", i,i));
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
        
        // flush, set RF=0 and cleanup
        StorageService.instance.forceKeyspaceFlush("test", "t1");
        process(ConsistencyLevel.ONE, "ALTER KEYSPACE test WITH replication = {'class': 'NetworkTopologyStrategy','DC1':'0'};");
        StorageService.instance.forceKeyspaceCleanup("test", "t1");
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(0L));
    }
    
    @Test
    public void testWideCleanup() throws Exception {
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
        
        long N = 100;
        for(int i=0; i < N; i++) {
            for(int j = 0; j < 10; j++)
                process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id, c1, f1) VALUES ('%d',%d,%d)", i,j,j));
        }
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(10*N));
        
        // flush, set RF=0 and cleanup
        StorageService.instance.forceKeyspaceFlush("test", "t1");
        process(ConsistencyLevel.ONE, "ALTER KEYSPACE test WITH replication = {'class': 'NetworkTopologyStrategy','DC1':'0'};");
        StorageService.instance.forceKeyspaceCleanup("test", "t1");
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(0L));
    }
}

