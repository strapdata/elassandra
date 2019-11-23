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

package org.elasticsearch.test.discovery;

import org.apache.logging.log4j.Logger;
import org.elassandra.discovery.CassandraDiscovery;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterApplier;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.cluster.service.MasterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.zen.UnicastHostsProvider;
import org.elasticsearch.plugins.DiscoveryPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MockCassandraDiscovery extends CassandraDiscovery {

    public static final String MOCK_CASSANDRA = "mock-cassandra";

    protected final Logger logger = Loggers.getLogger(MockCassandraDiscovery.class);

    Consumer<ClusterChangedEvent> publishFunc;
    Consumer<ClusterChangedEvent> resumitFunc;

    private MockCassandraDiscovery(Settings settings, TransportService transportService, MasterService masterService, ClusterService clusterService,
            ClusterApplier clusterApplier, ClusterSettings clusterSettings, NamedWriteableRegistry namedWriteableRegistry) {
        super(settings, transportService, masterService, clusterService, clusterApplier, clusterSettings, namedWriteableRegistry);
    }

    public void setPublishFunc(Consumer<ClusterChangedEvent> publishFunc) {
        this.publishFunc = publishFunc;
    }

    public void setResumitFunc(Consumer<ClusterChangedEvent> resumitFunc) {
        this.resumitFunc = resumitFunc;
    }

    @Override
    public void publish(final ClusterChangedEvent clusterChangedEvent, final AckListener ackListener) {
        if (this.publishFunc != null)
            this.publishFunc.accept(clusterChangedEvent);
        super.publish(clusterChangedEvent, ackListener);
    }

    @Override
    protected void resubmitTaskOnNextChange(final ClusterChangedEvent clusterChangedEvent) {
        if (resumitFunc != null)
            this.resumitFunc.accept(clusterChangedEvent);
        super.resubmitTaskOnNextChange(clusterChangedEvent);
    }

    /** A plugin which installs mock discovery and configures it to be used. */
    public static class TestPlugin extends Plugin implements DiscoveryPlugin {
        protected final Settings settings;

        public TestPlugin(Settings settings) {
            this.settings = settings;
        }

        @Override
        public Map<String, Supplier<Discovery>> getDiscoveryTypes(ThreadPool threadPool, TransportService transportService,
                NamedWriteableRegistry namedWriteableRegistry,
                MasterService masterService,
                ClusterApplier clusterApplier,
                ClusterSettings clusterSettings,
                UnicastHostsProvider hostsProvider,
                AllocationService allocationService) {
            Map<String, Supplier<Discovery>> discoveryTypes = new HashMap<>();
            discoveryTypes.put(MOCK_CASSANDRA,
                    () -> new MockCassandraDiscovery(Settings.builder().build(), transportService, masterService,
                        masterService.getClusterService(), clusterApplier, clusterSettings, namedWriteableRegistry));
            return discoveryTypes;
        }
    }
}
