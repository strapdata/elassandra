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
package org.elasticsearch.test;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.commons.compress.utils.Charsets;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.cache.recycler.PageCacheRecycler;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * A test that keep a singleton node started for all tests that can be used to get
 * references to Guice injectors in unit tests.
 * 
 * Cassandra use many static initializer, so NODE is statically initialized once 
 * for all tests and multinode cluster test is not possible.
 */
public abstract class ESSingleNodeTestCase extends ESTestCase {

     
    private static final Semaphore available = new Semaphore(1);
    
    protected static Node NODE;
    protected static Settings settings;
    
    static void initNode(Settings testSettings, Collection<Class<? extends Plugin>> classpathPlugins)  {
        System.out.println("cassandra.home="+System.getProperty("cassandra.home"));
        System.out.println("cassandra.config.loader="+System.getProperty("cassandra.config.loader"));
        System.out.println("cassandra.config="+System.getProperty("cassandra.config"));
        System.out.println("cassandra.config.dir="+System.getProperty("cassandra.config.dir"));
        System.out.println("cassandra-rackdc.properties="+System.getProperty("cassandra-rackdc.properties"));
        System.out.println("cassandra.storagedir="+System.getProperty("cassandra.storagedir"));
        System.out.println("logback.configurationFile="+System.getProperty("logback.configurationFile"));

        System.out.println("settings="+testSettings.getAsMap());
        System.out.println("plugins="+classpathPlugins);
        DatabaseDescriptor.createAllDirectories();

        settings = Settings.builder()
                .put(ClusterName.SETTING, DatabaseDescriptor.getClusterName())
                
                .put("path.home", System.getProperty("cassandra.home"))
                .put("path.conf", System.getProperty("cassandra.config.dir"))
                .put("path.data", DatabaseDescriptor.getAllDataFileLocations()[0])
                
                // TODO: use a consistent data path for custom paths
                // This needs to tie into the ESIntegTestCase#indexSettings() method
                .put("path.shared_data", DatabaseDescriptor.getAllDataFileLocations()[0])
                .put("node.name", nodeName())
                .put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
                .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)
                .put("script.inline", "on")
                .put("script.indexed", "on")
                //.put(EsExecutors.PROCESSORS, 1) // limit the number of threads created
                .put("http.enabled", true)
                .put(InternalSettingsPreparer.IGNORE_SYSTEM_PROPERTIES_SETTING, true)
                .put(testSettings)
                .build();
        
        ElassandraDaemon.instance.activate(false,  settings, new Environment(settings), classpathPlugins);
        try {
            Thread.sleep(1000*15);
        } catch (InterruptedException e) {
        }
        assertThat(DiscoveryNode.localNode(settings), is(false));
        
        NODE = ElassandraDaemon.instance.node();
        
        ClusterAdminClient clusterAdminClient = client().admin().cluster();
        ClusterHealthResponse clusterHealthResponse = clusterAdminClient.prepareHealth().setWaitForGreenStatus().get();
        assertFalse(clusterHealthResponse.isTimedOut());
        
        Settings.Builder settingsBuilder = Settings.builder()
                .put(InternalCassandraClusterService.SETTING_CLUSTER_DEFAULT_SYNCHRONOUS_REFRESH, true)
                .put(InternalCassandraClusterService.SETTING_CLUSTER_DEFAULT_DROP_ON_DELETE_INDEX, true);
        ClusterUpdateSettingsResponse clusterUpdateSettingsResponse = clusterAdminClient.prepareUpdateSettings().setTransientSettings(settingsBuilder).get();
        assertTrue(clusterUpdateSettingsResponse.isAcknowledged());
    }
    
    public ESSingleNodeTestCase() {
        super();
    }
    
    // override this to initialize the single node cluster.
    public Settings settings() {
        return Settings.EMPTY;
    }
    
    static void reset() {
        
    }
    
    static void cleanup(boolean resetNode) {
        if (NODE != null) {
            assertAcked(client().admin().indices().prepareDelete("*").get());
            
            if (resetNode) {
                reset();
            }
            MetaData metaData = client().admin().cluster().prepareState().get().getState().getMetaData();
            assertThat("test leaves persistent cluster metadata behind: " + metaData.persistentSettings().getAsMap(),
                    metaData.persistentSettings().getAsMap().size(), equalTo(0));
            assertThat("test leaves transient cluster metadata behind: " + metaData.transientSettings().getAsMap(),
                    metaData.transientSettings().getAsMap().size(), equalTo(2));
        }
    }

    @Before
    public void nodeSetup() throws Exception {
        try {
            available.acquire();
            if (NODE == null) {
                initNode(settings(), nodePlugins());
                // register NodeEnvironment to remove node.lock
                closeAfterTest(NODE.nodeEnvironment());
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        logger.info("[{}#{}]: setup test {}", getTestClass().getSimpleName(), getTestName());
    }
    
    @After
    public void nodeTearDown() throws Exception {
        logger.info("[{}#{}]: cleaning up after test {}", getTestClass().getSimpleName(), getTestName());
        cleanup(resetNodeAfterTest());
        super.tearDown();
        available.release();
    }

    @BeforeClass
    public static synchronized void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * This method returns <code>true</code> if the node that is used in the background should be reset
     * after each test. This is useful if the test changes the cluster state metadata etc. The default is
     * <code>false</code>.
     */
    protected boolean resetNodeAfterTest() {
        return true;
    }

    /**
     * Returns a collection of plugins that should be loaded on each node.
     */
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.emptyList();
    }

    /**
     * Returns a collection of plugins that should be loaded when creating a transport client.
     */
    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
        return Collections.emptyList();
    }

    /** Helper method to create list of plugins without specifying generic types. */
    protected static Collection<Class<? extends Plugin>> pluginList(Class<? extends Plugin>... plugins) {
        return Arrays.asList(plugins);
    }
    
    
    /**
     * Returns a client to the single-node cluster.
     */
    public static Client client() {
        return NODE.client();
    }

    public ClusterService clusterService() {
        return ElassandraDaemon.instance.node().clusterService();
    }
    
    public UntypedResultSet process(ConsistencyLevel cl, String query) throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return clusterService().process(cl, ClientState.forInternalCalls(), query);
    }
    
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query) throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return clusterService().process(cl, clientState, query);
    }
    
    public UntypedResultSet process(ConsistencyLevel cl, String query, Object... values) throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return clusterService().process(cl, ClientState.forInternalCalls(), query, values);
    }
    
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query, Object... values) throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return clusterService().process(cl, clientState, query, values);
    }
    
    /**
     * Returns the single test nodes name.
     */
    public static String nodeName() {
        return "node_s_0";
    }

    /**
     * Return a reference to the singleton node.
     */
    protected static Node node() {
        return NODE;
    }

    /**
     * Get an instance for a particular class using the injector of the singleton node.
     */
    protected static <T> T getInstanceFromNode(Class<T> clazz) {
        return NODE.injector().getInstance(clazz);
    }

    /**
     * Create a new index on the singleton node with empty index settings.
     */
    protected static IndexService createIndex(String index) {
        return createIndex(index, Settings.EMPTY);
    }

    /**
     * Create a new index on the singleton node with the provided index settings.
     */
    protected static IndexService createIndex(String index, Settings settings) {
        return createIndex(index, settings, null, (XContentBuilder) null);
    }

    /**
     * Create a new index on the singleton node with the provided index settings.
     */
    protected static IndexService createIndex(String index, Settings settings, String type, XContentBuilder mappings) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client().admin().indices().prepareCreate(index).setSettings(settings);
        if (type != null && mappings != null) {
            createIndexRequestBuilder.addMapping(type, mappings);
        }
        return createIndex(index, createIndexRequestBuilder);
    }

    /**
     * Create a new index on the singleton node with the provided index settings.
     */
    protected static IndexService createIndex(String index, Settings settings, String type, Object... mappings) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client().admin().indices().prepareCreate(index).setSettings(settings);
        if (type != null && mappings != null) {
            createIndexRequestBuilder.addMapping(type, mappings);
        }
        return createIndex(index, createIndexRequestBuilder);
    }

    protected static IndexService createIndex(String index, CreateIndexRequestBuilder createIndexRequestBuilder) {
        assertAcked(createIndexRequestBuilder.get());
        // Wait for the index to be allocated so that cluster state updates don't override
        // changes that would have been done locally
        ClusterHealthResponse health = client().admin().cluster()
                .health(Requests.clusterHealthRequest(index).waitForYellowStatus().waitForEvents(Priority.LANGUID).waitForRelocatingShards(0)).actionGet();
        assertThat(health.getStatus(), lessThanOrEqualTo(ClusterHealthStatus.YELLOW));
        assertThat("Cluster must be a single node cluster", health.getNumberOfDataNodes(), equalTo(1));
        IndicesService instanceFromNode = getInstanceFromNode(IndicesService.class);
        return instanceFromNode.indexServiceSafe(index);
    }

    protected static org.elasticsearch.index.engine.Engine engine(IndexService service) {
        return service.shard(0).engine();
    }

    /**
     * Create a new search context.
     */
    protected static SearchContext createSearchContext(IndexService indexService) {
        BigArrays bigArrays = indexService.injector().getInstance(BigArrays.class);
        ThreadPool threadPool = indexService.injector().getInstance(ThreadPool.class);
        PageCacheRecycler pageCacheRecycler = indexService.injector().getInstance(PageCacheRecycler.class);
        return new TestSearchContext(threadPool, pageCacheRecycler, bigArrays, indexService, null);
    }

    /**
     * Ensures the cluster has a green state via the cluster health API. This method will also wait for relocations.
     * It is useful to ensure that all action on the cluster have finished and all shards that were currently relocating
     * are now allocated and started.
     */
    public ClusterHealthStatus ensureGreen(String... indices) {
        return ensureGreen(TimeValue.timeValueSeconds(30), indices);
    }


    /**
     * Ensures the cluster has a green state via the cluster health API. This method will also wait for relocations.
     * It is useful to ensure that all action on the cluster have finished and all shards that were currently relocating
     * are now allocated and started.
     *
     * @param timeout time out value to set on {@link org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest}
     */
    public ClusterHealthStatus ensureGreen(TimeValue timeout, String... indices) {
        ClusterHealthResponse actionGet = client().admin().cluster()
                .health(Requests.clusterHealthRequest(indices).timeout(timeout).waitForGreenStatus().waitForEvents(Priority.LANGUID).waitForRelocatingShards(0)).actionGet();
        if (actionGet.isTimedOut()) {
            logger.info("ensureGreen timed out, cluster state:\n{}\n{}", client().admin().cluster().prepareState().get().getState().prettyPrint(), client().admin().cluster().preparePendingClusterTasks().get().prettyPrint());
            assertThat("timed out waiting for green state", actionGet.isTimedOut(), equalTo(false));
        }
        assertThat(actionGet.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        logger.debug("indices {} are green", indices.length == 0 ? "[_all]" : indices);
        return actionGet.getStatus();
    }

}
