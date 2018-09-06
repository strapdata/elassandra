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
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra index rebuild tests.
 * Index rebuild rely on the compaction manger to read SSTables (Does NOT rebuild in-memory data).
 * @author vroyer
 *
 */
public class IndexBuildTests extends ESSingleNodeTestCase {
    static long N = 10;
    
    // mvn test -Pdev -pl com.strapdata.elasticsearch:elasticsearch -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.IndexBuildTests -Des.logger.level=INFO -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
    @Test
    public void indexRebuildTest() throws Exception {
        indexRebuild(1);
    }
    
    @Test
    public void indexMultithreadRebuildTest() throws Exception {
        indexRebuild(3);
    }
    
    public void indexRebuild(int numThread) throws Exception {
        createIndex("test");
        ensureGreen("test");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS test.t1 ( a int,b text, primary key (a) )");
        
        assertAcked(client().admin().indices().preparePutMapping("test").setType("t1").setSource(discoverMapping("t1")).get());
        int i=0;
        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
        
        // close index
        assertAcked(client().admin().indices().prepareClose("test").get());

        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        UntypedResultSet rs = process(ConsistencyLevel.ONE,"select count(*) from test.t1");
        StorageService.instance.forceKeyspaceFlush("test","t1");
        
        // open index
        assertAcked(client().admin().indices().prepareOpen("test").get());
        ensureGreen("test");
        
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
        
        // rebuild_index
        //assertThat(client().admin().indices().prepareRebuild("test").setNumThreads(2).get().getFailedShards(), equalTo(0));
        StorageService.instance.rebuildSecondaryIndex(2, "test", "t1", "elastic_t1_idx");
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
    }
    
    @Test
    public void indexFirstBuildTest() throws Exception {
        createIndex("test");
        ensureGreen("test");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS test.t1 ( a int,b text, primary key (a) )");
        for(int i=0 ; i < N; i++) {
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        StorageService.instance.forceKeyspaceFlush("test","t1");
        
        assertAcked(client().admin().indices().preparePutMapping("test").setType("t1").setSource(discoverMapping("t1")).get());
        // wait for index rebuild by the compaction manager thread
        while (!SystemKeyspace.isIndexBuilt("test", "elastic_t1_idx"))
            Thread.sleep(500);

        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
    }
    
    @Test
    public void indexWithReplicationMap() throws Exception {
        String indexName = "test_rep";
        createIndex(indexName, Settings.builder().putList(IndexMetaData.SETTING_REPLICATION, "DC1:2").build());
        ensureGreen(indexName);
        UntypedResultSet rs = process(ConsistencyLevel.ONE, "SELECT replication FROM system_schema.keyspaces WHERE keyspace_name = ?", indexName);
        Map<String, String> replication = rs.one().getMap("replication", UTF8Type.instance, UTF8Type.instance);
        System.out.println("replication="+replication);
        assertThat(replication.get("class"), equalTo("org.apache.cassandra.locator.NetworkTopologyStrategy"));
        assertThat(replication.get("DC1"), equalTo("2"));
    }
}
