/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elassandra;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.elasticsearch.test.discovery.MockCassandraDiscovery;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class CassandraDiscoveryTests extends ESSingleNodeTestCase {

    long version = -1;
    int resumit = 0;

    @Override
    public void tearDown() throws Exception {
        MockCassandraDiscovery discovery = this.getMockCassandraDiscovery();
        discovery.setPublishFunc(null);
        discovery.setResumitFunc(null);
        super.tearDown();
    }

    @Test
    public void testConcurrentPaxosUpdate() {
        createIndex("test");
        ensureGreen("test");

        MockCassandraDiscovery discovery = this.getMockCassandraDiscovery();
        discovery.setPublishFunc(e -> {
            if (version == -1) {
                // increase CQL version number to simulate PAXOS update while CQL schema not yet received.
                UntypedResultSet rs = process(ConsistencyLevel.ONE, "SELECT version FROM elastic_admin.metadata_log WHERE cluster_name = ? LIMIT 1", DatabaseDescriptor.getClusterName());
                version = rs.one().getLong("version");
                logger.warn("forcing metadata.version to {} in elastic_admin.metadata",  version + 1);
                
                // "UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, source= ?, ts = dateOf(now()) WHERE cluster_name= ? AND v = ? IF version = ?",
                process(ConsistencyLevel.ONE, "UPDATE elastic_admin.metadata_log SET owner = ?, version = ?, source = ?, ts = dateOf(now()) WHERE cluster_name = ? AND v = ?", 
                        UUID.fromString(StorageService.instance.getLocalHostId()), 
                        version + 1,
                        "paxos test",
                        DatabaseDescriptor.getClusterName(), 
                        version + 1);
            }
        });

        discovery.setResumitFunc(e -> {
            if (resumit == 0) {
                resumit++;
                // publish
                clusterService().submitStateUpdateTask("cql-schema-mapping-update", new ClusterStateUpdateTask() {

                    @Override
                    public ClusterState execute(ClusterState currentState) {
                        ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);
                        MetaData newMetadata = MetaData.builder(currentState.metaData())
                                .persistentSettings(Settings.builder().build())
                                .version(version + 1)
                                .build();
                        ClusterState newClusterState = newStateBuilder.incrementVersion().metaData(newMetadata).build();
                        logger.warn("submit cql-schema-mapping-update metadata.version={}",  newClusterState.metaData().version());
                        return newClusterState;
                    }

                    @Override
                    public void onFailure(String source, Exception t) {
                        logger.error("unexpected failure during [{}]", t, source);
                    }

                });
            }
        });

        assertThat(client().prepareIndex("test", "my_type", "1").setSource("{\"status_code\": \"OK\" }", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertEquals(1, resumit);
        assertEquals(version + 2, this.clusterService().state().metaData().version());
    }
}
