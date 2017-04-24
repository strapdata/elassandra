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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

/**
 * Elassandra snapshot tests.
 * @author vroyer
 *
 */
//mvn test -Pdev -pl om.strapdata.elasticsearch:elasticsearch -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.SnapshotTests -Des.logger.level=ERROR -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
public class SnapshotTests extends ESSingleNodeTestCase {

    // SSTable snapshotDir = data/data/<keyspace>/<table>/snapshots/<snapshot_name>/
    public void restoreSSTable(String dataLocation, String keyspaceName, String cfName, UUID cfId, String snapshotName) throws IOException {
        String id = cfId.toString().replaceAll("\\-", "");
        Path sourceDir = PathUtils.get(dataLocation+"/"+keyspaceName+"/"+cfName+"-"+id+"/snapshots/"+snapshotName);
        Path targetDir = PathUtils.get(dataLocation+"/"+keyspaceName+"/"+cfName+"-"+id+"/");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "{*.db}")) {
            for (Path dbFile: stream)
                Files.delete(dbFile);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path f: stream)
                Files.copy(f, PathUtils.get(targetDir.toString(), f.getFileName().toString()) , StandardCopyOption.COPY_ATTRIBUTES);
        } 
    }
    
    // lucene snapshotDir = data/<cluster_name>/nodes/0/snapshots/<index_name>/<snapshot_name>
    public void restoreLucenceFiles(String dataLocation, String indexName, String snapshot) throws IOException {
        Path sourceDir = PathUtils.get(dataLocation+"/"+DatabaseDescriptor.getClusterName()+"/nodes/0/snapshots/"+indexName+"/"+snapshot+"/");
        Path targetDir = PathUtils.get(dataLocation+"/"+DatabaseDescriptor.getClusterName()+"/nodes/0/indices/"+indexName+"/0/index/");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "{_*.*,segments*}")) {
            for (Path segmentFile: stream)
                Files.delete(segmentFile);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir, "{_*.*,segments*}")) {
            for (Path f: stream)
                Files.copy(f, PathUtils.get(targetDir.toString(), f.getFileName().toString()) , StandardCopyOption.COPY_ATTRIBUTES);
        } 
    }
    
    @Test
    public void basicSnapshotTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 ( name text, age int, primary key (name))");
        
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("t1").field("discover", ".*").endObject().endObject();
        createIndex("ks", Settings.builder().put("index.snapshot_with_sstable",true).build(),"t1", mapping);
        ensureGreen("ks");

        for(long i=0; i < 1000; i++)
           process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "INSERT INTO ks.t1 (name, age) VALUES ('name%d', %d)",i,i));
        assertThat(client().prepareSearch().setIndices("ks").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(1000L));
        
        // take snaphot
        StorageService.instance.takeSnapshot("snap1", "ks");
        
        // drop all
        process(ConsistencyLevel.ONE,"TRUNCATE ks.t1");
        assertThat(client().prepareSearch().setIndices("ks").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(0L));
        
        // close index and restore SSTable+Lucene files
        assertAcked(client().admin().indices().prepareClose("ks").get());
        
        String dataLocation = DatabaseDescriptor.getAllDataFileLocations()[0];
        restoreSSTable(dataLocation, "ks", "t1", Schema.instance.getCFMetaData("ks", "t1").cfId, "snap1");
        restoreLucenceFiles(dataLocation, "ks", "snap1");
        
        // refresh SSTables and repopen index
        StorageService.instance.loadNewSSTables("ks", "t1");
        assertAcked(client().admin().indices().prepareOpen("ks").get());
        ensureGreen("ks");
        
        assertThat(client().prepareSearch().setIndices("ks").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(1000L));
    }
    
    @Test
    //mvn test -Pdev -pl com.strapdata.elasticsearch:elasticsearch -Dtests.seed=622A2B0618CE4676 -Dtests.class=org.elassandra.SnapshotTests -Dtests.method="onDropSnapshotTest" -Des.logger.level=ERROR -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=ro-RO -Dtests.timezone=America/Toronto
    public void onDropSnapshotTest() throws Exception {
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 ( name text, age int, primary key (name))");
        
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("t1").field("discover", ".*").endObject().endObject();
        createIndex("ks", Settings.builder().put("index.snapshot_with_sstable",true).build(),"t1", mapping);
        ensureGreen("ks");

        for(long i=0; i < 1000; i++)
           process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "INSERT INTO ks.t1 (name, age) VALUES ('name%d', %d)",i,i));
        assertThat(client().prepareSearch().setIndices("ks").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(1000L));
        
        UUID cfId = Schema.instance.getCFMetaData("ks","t1").cfId;
        String id = cfId.toString().replaceAll("\\-", "");
        
        // drop index + keyspace (C* snapshot before drop => flush before snapshot => ES flush before delete)
        assertAcked(client().admin().indices().prepareDelete("ks").get());
        
        // recreate schema and mapping
        process(ConsistencyLevel.ONE,String.format(Locale.ROOT, "CREATE KEYSPACE ks WITH replication = {'class': 'NetworkTopologyStrategy', '%s': '1'}",DatabaseDescriptor.getLocalDataCenter()));
        process(ConsistencyLevel.ONE,"CREATE TABLE ks.t1 ( name text, age int, primary key (name))");
        createIndex("ks", Settings.builder().put("index.snapshot_with_sstable",true).build(),"t1", mapping);
        ensureGreen("ks");
        assertThat(client().prepareSearch().setIndices("ks").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(0L));
        
        // close index and restore SSTable+Lucene files
        assertAcked(client().admin().indices().prepareClose("ks").get());
        
        String dataLocation = DatabaseDescriptor.getAllDataFileLocations()[0];
        DirectoryStream<Path> stream = Files.newDirectoryStream(PathUtils.get(dataLocation+"/ks/t1-"+id+"/snapshots/"));
        Path snapshot = stream.iterator().next();
        String snap = snapshot.getFileName().toString();
        System.out.println("snapshot name="+snap);
        stream.close();
        
        restoreSSTable(dataLocation, "ks", "t1", cfId, snap);
        restoreLucenceFiles(dataLocation, "ks", snap);
        
        // refresh SSTables and repopen index
        StorageService.instance.loadNewSSTables("ks", "t1");
        assertAcked(client().admin().indices().prepareOpen("ks").get());
        ensureGreen("ks");
        
        assertThat(client().prepareSearch().setIndices("ks").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(1000L));
    }
}
