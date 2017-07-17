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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.metrics.CassandraMetricsRegistry;
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
 * Elassandra SSTable compactions tests.
 * @author vroyer
 *
 */
public class CompactionTests extends ESSingleNodeTestCase {
    
    // mvn test -Pdev -pl com.strapdata.elasticsearch:elasticsearch -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.CompactionTests -Des.logger.level=INFO -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
    @Test
    public void basicCompactionTest() throws Exception {
        createIndex("test");
        ensureGreen("test");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS test.t1 ( a int,b text, primary key (a) ) WITH "+
                "compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}");
        assertAcked(client().admin().indices().preparePutMapping("test").setType("t1").setSource("{ \"t1\" : { \"discover\" : \".*\" }}").get());
        
        Map<String, Gauge> gauges = CassandraMetricsRegistry.Metrics.getGauges(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.endsWith("t1");
            }
        });
        
        int i=0;
        for(int j=0 ; j < 1000; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(1000L));
        StorageService.instance.forceKeyspaceFlush("test","t1");
        
        for(String s:gauges.keySet())
            System.out.println(s+"="+gauges.get(s).getValue());
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        for(int j=0 ; j < 1000; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2000L));
        StorageService.instance.forceKeyspaceFlush("test","t1");
        Thread.sleep(2000);
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(2));
        
        for(int j=0 ; j < 1000; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3000L));
        StorageService.instance.forceKeyspaceFlush("test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(3));
        
        // force compaction
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3000L));
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        for(String s:gauges.keySet())
            System.out.println(s+"="+gauges.get(s).getValue());
        
        // overwrite 1000 docs
        for(int j=0 ; j < 1000; j++)
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", 1000+j, "y");
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3000L));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(1000L));
        
        StorageService.instance.forceKeyspaceFlush("test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(2));
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3000L));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(1000L));
        
        // remove 1000 docs
        for(int j=0 ; j < 1000; j++)
            process(ConsistencyLevel.ONE,"delete from test.t1 WHERE a = ?", 1000+j);
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2000L));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(0L));

        StorageService.instance.forceKeyspaceFlush("test");
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2000L));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(00L));
    }
    
    @Test
    public void expiredTtlCompactionTest() throws Exception {
        createIndex("test", Settings.builder().put(IndexMetaData.SETTING_INDEX_ON_COMPACTION, true).build());
        ensureGreen("test");
        
        long N = 10;
        
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS test.t1 ( a int,b text, primary key (a) ) WITH "+
                "gc_grace_seconds = 15 " +
                " AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}");
        assertAcked(client().admin().indices().preparePutMapping("test").setType("t1").setSource("{ \"t1\" : { \"discover\" : \".*\", \"_meta\": { \"index_on_compaction\":true } }}").get());
        
        Map<String, Gauge> gauges = CassandraMetricsRegistry.Metrics.getGauges(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.endsWith("t1");
            }
        });
        
        int i=0;
        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
        StorageService.instance.forceKeyspaceFlush("test","t1");
        
        for(String s:gauges.keySet())
            System.out.println(s+"="+gauges.get(s).getValue());
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?) USING TTL 15", i, "y");
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(N));
        StorageService.instance.forceKeyspaceFlush("test","t1");
        Thread.sleep(2000);
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(2));
        
        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?)", i, "x"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(N));
        StorageService.instance.forceKeyspaceFlush("test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(3));
        
        // force compaction
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(N));
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        for(String s:gauges.keySet())
            System.out.println(s+"="+gauges.get(s).getValue());
       
        
        Thread.sleep(15*1000);  // wait TTL expiration
        Thread.sleep(20*1000);  // wait gc_grace_seconds expiration
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(N));

        StorageService.instance.forceKeyspaceFlush("test");
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        UntypedResultSet rs = process(ConsistencyLevel.ONE,"SELECT * FROM test.t1");
        System.out.println("t1.count = "+rs.size());
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:y")).get().getHits().getTotalHits(), equalTo(0L));
    }
    
    @Test
    public void expiredTtlColumnCompactionTest() throws Exception {
        createIndex("test", Settings.builder().put(IndexMetaData.SETTING_INDEX_ON_COMPACTION, true).build());
        ensureGreen("test");
        
        long N = 10;
        
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS test.t1 ( a int,b text, c text, primary key (a) ) WITH "+
                "gc_grace_seconds = 15 " +
                " AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}");
        assertAcked(client().admin().indices().preparePutMapping("test").setType("t1").setSource("{ \"t1\" : { \"discover\" : \".*\" }}").get());
        
        Map<String, Gauge> gauges = CassandraMetricsRegistry.Metrics.getGauges(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.endsWith("t1");
            }
        });
        
        int i=0;
        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b,c) VALUES (?,?,?)", i, "b"+i, "c"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(N));
        StorageService.instance.forceKeyspaceFlush("test","t1");
        
        for(String s:gauges.keySet())
            System.out.println(s+"="+gauges.get(s).getValue());
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        for(int j=0 ; j < N; j++) {
            i++;
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,c) VALUES (?,?) ", i, "c"+i);
            process(ConsistencyLevel.ONE,"insert into test.t1 (a,b) VALUES (?,?) USING TTL 15", i, "b"+i);
        }
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:*")).get().getHits().getTotalHits(), equalTo(2*N));
        StorageService.instance.forceKeyspaceFlush("test","t1");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(2));
        
        // force compaction
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:*")).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
       
        Thread.sleep(15*1000);  // wait TTL expiration
        Thread.sleep(20*1000);  // wait gc_grace_seconds expiration
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("c:*")).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:*")).get().getHits().getTotalHits(), equalTo(2*N));

        StorageService.instance.forceKeyspaceFlush("test");
        StorageService.instance.forceKeyspaceCompaction(true, "test");
        assertThat(gauges.get("org.apache.cassandra.metrics.Table.LiveSSTableCount.test.t1").getValue(), equalTo(1));
        
        UntypedResultSet rs = process(ConsistencyLevel.ONE,"SELECT * FROM test.t1");
        System.out.println("t1.count = "+rs.size());
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("c:*")).get().getHits().getTotalHits(), equalTo(2*N));
        assertThat(client().prepareSearch().setIndices("test").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("b:*")).get().getHits().getTotalHits(), equalTo(N));
    }
}
