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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elassandra.discovery.PendingClusterStatesQueue;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;

public class PendingClusterStateTests extends ESSingleNodeTestCase {
    private static final Logger logger = LogManager.getLogger(PendingClusterStateTests.class);

    class Listener implements PendingClusterStatesQueue.StateProcessedListener {
        int processed = 0;
        int failed = 0;

        @Override
        public void onNewClusterStateProcessed() {
            processed++;
        }

        @Override
        public void onNewClusterStateFailed(Exception e) {
            failed++;
        }

    }

    @Test
    public void processBatchTest() throws Exception {
        PendingClusterStatesQueue queue = new PendingClusterStatesQueue(logger, 25);
        ClusterState cs0 = ClusterState.builder(new ClusterName("cluster")).build();
        ClusterState cs1 = ClusterState.builder(cs0).version(1).build();
        ClusterState cs2 = ClusterState.builder(cs1).version(2).metaData(MetaData.builder().incrementVersion()).build();
        ClusterState cs3 = ClusterState.builder(cs2).version(3).metaData(MetaData.builder(cs2.getMetaData()).incrementVersion()).build();
        ClusterState cs4 = ClusterState.builder(cs3).version(4).metaData(MetaData.builder(cs3.getMetaData()).incrementVersion()).build();
        ClusterState cs5 = ClusterState.builder(cs3).version(5).metaData(MetaData.builder(cs4.getMetaData()).incrementVersion()).build();

        Listener listener = new Listener();
        queue.addPending(cs1, listener);
        queue.addPending(cs2, listener);
        assertThat(queue.getNextClusterStateToProcess().version(), equalTo(2L));
        queue.addPending(cs3, listener);

        ClusterState cs_3 = queue.getNextClusterStateToProcess();
        assertThat(cs_3.version(), equalTo(3L));
        assertThat(cs_3.metaData().version(), equalTo(2L));
        queue.markAsProcessed(cs_3);

        queue.addPending(cs4, listener);
        assertThat(queue.getNextClusterStateToProcess().version(), equalTo(4L));
        assertThat(listener.processed, equalTo(3));
        assertThat(queue.size(), equalTo(1));

        queue.addPending(cs5, listener);
        queue.markAsFailed(cs5, new Exception());
        assertThat(listener.processed, equalTo(3));
        assertThat(listener.failed, equalTo(2));
        assertThat(queue.size(), equalTo(0));
    }

    @Test
    public void queueOverflowTest() throws Exception {
        PendingClusterStatesQueue queue = new PendingClusterStatesQueue(logger, 2);
        ClusterState cs0 = ClusterState.builder(new ClusterName("cluster")).build();
        ClusterState cs1 = ClusterState.builder(cs0).version(1).build();
        ClusterState cs2 = ClusterState.builder(cs1).version(2).metaData(MetaData.builder().incrementVersion()).build();
        ClusterState cs3 = ClusterState.builder(cs2).version(3).metaData(MetaData.builder(cs2.getMetaData()).incrementVersion()).build();
        ClusterState cs4 = ClusterState.builder(cs3).version(4).metaData(MetaData.builder(cs3.getMetaData()).incrementVersion()).build();

        Listener listener = new Listener();
        queue.addPending(cs1, listener);
        queue.addPending(cs2, listener);
        assertThat(listener.failed, equalTo(0));
        queue.addPending(cs3, listener);
        assertThat(listener.failed, equalTo(1));
        assertThat(queue.size(), equalTo(2));

        queue.markAsProcessed(queue.getNextClusterStateToProcess());
        assertThat(queue.size(), equalTo(0));
        assertThat(listener.processed, equalTo(2));
    }
}

