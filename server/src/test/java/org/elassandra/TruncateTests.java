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
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.segments.IndexShardSegments;
import org.elasticsearch.action.admin.indices.segments.ShardSegments;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.Segment;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra paque field test.
 * @author vroyer
 *
 */
public class TruncateTests extends ESSingleNodeTestCase {

    @Test
    public void testNestedTruncate() throws Exception {
        XContentBuilder mapping1 = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("user").field("type", "nested").endObject()
                    .endObject()
                .endObject();
        
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("user", mapping1));
        ensureGreen("test");
        
        IndexResponse resp = client().prepareIndex("test", "user", "1").setSource("{ \n"+
                "\"group\" : \"fans\",\n" + 
                "        \"user\" : [\n" + 
                "          {\n" + 
                "            \"first\" : \"John\",\n" + 
                "            \"last\" :  \"Smith\"\n" + 
                "          },\n" + 
                "          {\n" + 
                "            \"first\" : \"Alice\",\n" + 
                "            \"last\" :  \"White\"\n" + 
                "          }\n" + 
                "        ]}", XContentType.JSON).get();
        assertThat(resp.getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertThat(client().prepareSearch().setIndices("test").setTypes("user").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(1L));
        int totalDocs = 0;
        for(IndexShardSegments iss : client().admin().indices().prepareSegments("test").get().getIndices().get("test")) {
            for(ShardSegments ss : iss.getShards()) {
                for(Segment seg : ss.getSegments()) {
                    totalDocs += seg.getNumDocs();
                }
            }
        }
        assertThat(totalDocs, equalTo(3));
        
        process(ConsistencyLevel.ONE,"TRUNCATE test.user");
        assertThat(client().prepareSearch().setIndices("test").setTypes("user").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(0L));
        totalDocs = 0;
        for(IndexShardSegments iss : client().admin().indices().prepareSegments("test").get().getIndices().get("test")) {
            for(ShardSegments ss : iss.getShards()) {
                for(Segment seg : ss.getSegments()) {
                    if (seg.isSearch())
                        totalDocs += seg.getNumDocs();
                }
            }
        }
        assertThat(totalDocs, equalTo(0));
    }

}
