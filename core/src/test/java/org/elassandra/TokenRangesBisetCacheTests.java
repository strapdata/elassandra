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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

import java.util.Collections;

import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.dht.Murmur3Partitioner.LongToken;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

/**
 * @author vroyer
 *
 */
//mvn test -Pdev -pl com.strapdata:elassandra -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.TokenRangeBisetCacheTests -Des.logger.level=ERROR -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
public class TokenRangesBisetCacheTests extends ESSingleNodeTestCase {
    static long N = 10010; // start caching at 10k
    
    @Test
    public void tokenBitsetTest() throws Exception {
        process(ConsistencyLevel.ONE,"CREATE KEYSPACE IF NOT EXISTS test WITH replication={ 'class':'NetworkTopologyStrategy', 'DC1':'1' }");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS test.t1 ( a int,b text, primary key (a) )");
        
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("t1").field("discover", ".*").endObject().endObject();
        createIndex("test", Settings.builder().put("index.token_bitset_cache",true).build(),"t1", mapping);
        ensureGreen("test");
        
        for(int j=0 ; j < N; j++) 
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", j, "x"+j);

        // force caching after 20 requests.
        for(int i=0; i< 21 ; i++)
            assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
        
        long upper = client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery())
        .setTokenRanges(Collections.singleton(new Range<Token>(new LongToken(0), new LongToken(Long.MAX_VALUE))))
        .get().getHits().getTotalHits();
        long lower = client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery())
                .setTokenRanges(Collections.singleton(new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken(0))))
                .get().getHits().getTotalHits();
        assertThat(upper, lessThan(N));
        assertThat(lower, lessThan(N));
        assertThat(lower+upper, equalTo(N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
    }

}
