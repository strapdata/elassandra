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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Locale;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author vroyer
 *
 */
//gradle :server:test -Dtests.seed=65E2CF27F286CC89 -Dtests.class=org.elassandra.ExplainTests -Dtests.security.manager=false -Dtests.locale=en-PH -Dtests.timezone=America/Coral_Harbour
public class ExplainTests extends ESSingleNodeTestCase {

    @Test
    public void testExplain() throws Exception {
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

        long N = 10;
        for(int i=0; i < N; i++)
            process(ConsistencyLevel.ONE, String.format(Locale.ROOT, "INSERT INTO test.t1 (id, f1) VALUES ('%d',%d)", i,i));
        assertThat(client().prepareSearch().setIndices("test").setQuery(QueryBuilders.termQuery("f1", 1)).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareExplain("test", "t1", "1").setQuery(QueryBuilders.termQuery("f1", 1)).get().hasExplanation(), equalTo(true));
    }
}

