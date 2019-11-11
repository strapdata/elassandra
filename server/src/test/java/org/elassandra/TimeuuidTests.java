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

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.utils.UUIDGen;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.UUID;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra Naming tests.
 * @author vroyer
 *
 */
public class TimeuuidTests extends ESSingleNodeTestCase {

    @Test
    public void timeuuidInsertTest() throws Exception {
        XContentBuilder mapping1 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("ts")
                            .field("type", "date")
                            .field("cql_collection", "singleton")
                            .field("cql_type", "timeuuid")
                        .endObject()
                    .endObject()
                .endObject();

        assertAcked(client().admin().indices().prepareCreate("twitter").addMapping("_doc", mapping1).get());
        ensureGreen("twitter");

        UUID timeuuid = UUIDGen.getTimeUUID();
        assertThat(client().prepareIndex("twitter", "_doc", "1")
                .setSource("{ \"ts\": \"" + timeuuid.toString() + "\" }", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));

        assertThat(client().prepareGet().setIndex("twitter").setType("_doc").setId("1").get().isExists(), equalTo(true));

        UntypedResultSet results = process(ConsistencyLevel.ONE,"SELECT ts, dateOf(ts) FROM twitter.\"_doc\"");
        assertThat(results.size(),equalTo(1));
        assertThat(results.one().getUUID("ts"),equalTo(timeuuid));
    }
}
