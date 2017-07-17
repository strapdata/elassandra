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

import com.carrotsearch.randomizedtesting.RandomizedContext;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.test.discovery.TestZenDiscovery;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.xml.bind.DatatypeConverter;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * A test that keep a singleton node started for all tests that can be used to get
 * references to Guice injectors in unit tests.
 */
public abstract class ESSingleNodeTestCase extends ESTestCase {

    public interface ActionRequestBuilderHelper {
        public void addHeader(ActionRequestBuilder builder);
    }
    
    private static final Semaphore available = new Semaphore(1);
    
    protected static volatile Node NODE;
    protected static Settings settings;
    public    static ActionRequestBuilderHelper actionRequestHelper = null;
    
    static void initNode(Settings testSettings, Collection<Class<? extends Plugin>> classpathPlugins)  {
        System.out.println("working.dir="+System.getProperty("user.dir"));
        System.out.println("cassandra.home="+System.getProperty("cassandra.home"));
        System.out.println("cassandra.config.loader="+System.getProperty("cassandra.config.loader"));
        System.out.println("cassandra.config="+System.getProperty("cassandra.config"));
        System.out.println("cassandra.config.dir="+System.getProperty("cassandra.config.dir"));
        System.out.println("cassandra-rackdc.properties="+System.getProperty("cassandra-rackdc.properties"));
        System.out.println("cassandra.storagedir="+System.getProperty("cassandra.storagedir"));
        System.out.println("logback.configurationFile="+System.getProperty("logback.configurationFile"));

        System.out.println("settings="+testSettings.getAsMap());
        System.out.println("plugins="+classpathPlugins);
        DatabaseDescriptor.daemonInitialization();
        DatabaseDescriptor.createAllDirectories();

        settings = Settings.builder()
                .put("path.home", System.getProperty("cassandra.home"))
                .put("path.conf", System.getProperty("cassandra.config.dir"))
                .put("path.data", DatabaseDescriptor.getAllDataFileLocations()[0])
                
                // TODO: use a consistent data path for custom paths
                // This needs to tie into the ESIntegTestCase#indexSettings() method
                .put("path.shared_data", DatabaseDescriptor.getAllDataFileLocations()[0])
                .put("node.name", nodeName())
                //.put("script.inline", "on")
                //.put("script.indexed", "on")
                //.put(EsExecutors.PROCESSORS, 1) // limit the number of threads created
                .put(NetworkModule.TRANSPORT_TYPE_KEY, NetworkModule.LOCAL_TRANSPORT)
                .put(NetworkModule.HTTP_ENABLED.getKey(), false)
                .put("discovery.type", "cassandra")
                .put("client.type", "node")
                //.put(InternalSettingsPreparer.IGNORE_SYSTEM_PROPERTIES_SETTING, true)
                
                .put(testSettings)
                .build();
        
        ElassandraDaemon.instance.activate(false,  settings, new Environment(settings), classpathPlugins);
        try {
            Thread.sleep(1000*15);
        } catch (InterruptedException e) {
        }
        //assertThat(DiscoveryNode.getLocalNode(settings), is(false));
        
        NODE = ElassandraDaemon.instance.node();

        ClusterAdminClient clusterAdminClient = client().admin().cluster();
        ClusterHealthRequestBuilder builder = clusterAdminClient.prepareHealth();
        expand(builder);
        ClusterHealthResponse clusterHealthResponse = builder.setWaitForGreenStatus().get();
        assertFalse(clusterHealthResponse.isTimedOut());
    }
    
    public ESSingleNodeTestCase() {
        super();
    }
    
    // override this to initialize the single node cluster.
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.EMPTY;
    }
    
    static void reset() {
        
    }
    
    static void cleanup(boolean resetNode) {
        if (NODE != null) {
            DeleteIndexRequestBuilder builder = client().admin().indices().prepareDelete("*");
            expand(builder);
            assertAcked(builder.get());
            
            if (resetNode) {
                reset();
            }
        }
    }
    public static String encodeBasicHeader(final String username, final String password) {
        return new String(DatatypeConverter.printBase64Binary((username + ":" + Objects.requireNonNull(password)).getBytes(StandardCharsets.UTF_8)));
    }

    @Before
    public synchronized void nodeSetup() throws Exception {
        available.acquireUninterruptibly();
        if (NODE == null) {
            initNode(nodeSettings(1), getPlugins());
            // register NodeEnvironment to remove node.lock
            closeAfterTest(NODE.getNodeEnvironment());
        }
        logger.info("[{}#{}]: setup test {}", getTestClass().getSimpleName(), getTestName());
    }
    
    @After
    public synchronized void nodeTearDown() throws Exception {
        logger.info("[{}#{}]: cleaning up after test", getTestClass().getSimpleName(), getTestName());
        cleanup(resetNodeAfterTest());
        super.tearDown();
        available.release();
        logger.info("[{}#{}]: semaphore released={}", getTestClass().getSimpleName(), getTestName(), available.getQueueLength());
    }

    @BeforeClass
    public static synchronized void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() {
    }


    public static void expand(ActionRequestBuilder builder) {
        if (actionRequestHelper != null)
            actionRequestHelper.addHeader(builder);
    }
    
    /*
    protected void startNode(long seed) throws Exception {
        assert NODE == null;
        NODE = RandomizedContext.current().runWithPrivateRandomness(seed, this::newNode);
        // we must wait for the node to actually be up and running. otherwise the node might have started,
        // elected itself master but might not yet have removed the
        // SERVICE_UNAVAILABLE/1/state not recovered / initialized block
        ClusterHealthResponse clusterHealthResponse = client().admin().cluster().prepareHealth().setWaitForGreenStatus().get();
        assertFalse(clusterHealthResponse.isTimedOut());
        client().admin().indices()
            .preparePutTemplate("one_shard_index_template")
            .setTemplate("*")
            .setOrder(0)
            .setSettings(Settings.builder().put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1)
            .put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0)).get();
    }

    private static void stopNode() throws IOException {
        Node node = NODE;
        NODE = null;
        IOUtils.close(node);
    }
    */
    
    /*
    @Override
    public void setUp() throws Exception {
        super.setUp();
        //the seed has to be created regardless of whether it will be used or not, for repeatability
        long seed = random().nextLong();
        // Create the node lazily, on the first test. This is ok because we do not randomize any settings,
        // only the cluster name. This allows us to have overridden properties for plugins and the version to use.
        if (NODE == null) {
            startNode(seed);
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        logger.info("[{}#{}]: cleaning up after test", getTestClass().getSimpleName(), getTestName());
        super.tearDown();
        assertAcked(client().admin().indices().prepareDelete("*").get());
        MetaData metaData = client().admin().cluster().prepareState().get().getState().getMetaData();
        assertThat("test leaves persistent cluster metadata behind: " + metaData.persistentSettings().getAsMap(),
                metaData.persistentSettings().size(), equalTo(0));
        assertThat("test leaves transient cluster metadata behind: " + metaData.transientSettings().getAsMap(),
                metaData.transientSettings().size(), equalTo(0));
        if (resetNodeAfterTest()) {
            assert NODE != null;
            stopNode();
            //the seed can be created within this if as it will either be executed before every test method or will never be.
            startNode(random().nextLong());
        }
    }
    */
    
    /**
     * This method returns <code>true</code> if the node that is used in the background should be reset
     * after each test. This is useful if the test changes the cluster state metadata etc. The default is
     * <code>false</code>.
     */
    protected boolean resetNodeAfterTest() {
        return true;
    }

    /** The plugin classes that should be added to the node. */
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.emptyList();
    }

    /** Helper method to create list of plugins without specifying generic types. */
    @SafeVarargs
    @SuppressWarnings("varargs") // due to type erasure, the varargs type is non-reifiable, which causes this warning
    protected final Collection<Class<? extends Plugin>> pluginList(Class<? extends Plugin>... plugins) {
        return Arrays.asList(plugins);
    }

    /** Additional settings to add when creating the node. Also allows overriding the default settings. */
    protected Settings nodeSettings() {
        return Settings.EMPTY;
    }

    /*
    private Node newNode() {
        final Path tempDir = createTempDir();
        Settings settings = Settings.builder()
            .put(ClusterName.CLUSTER_NAME_SETTING.getKey(), InternalTestCluster.clusterName("single-node-cluster", random().nextLong()))
            .put(Environment.PATH_HOME_SETTING.getKey(), tempDir)
            .put(Environment.PATH_REPO_SETTING.getKey(), tempDir.resolve("repo"))
            // TODO: use a consistent data path for custom paths
            // This needs to tie into the ESIntegTestCase#indexSettings() method
            .put(Environment.PATH_SHARED_DATA_SETTING.getKey(), createTempDir().getParent())
            .put("node.name", "node_s_0")
            .put(ScriptService.SCRIPT_MAX_COMPILATIONS_PER_MINUTE.getKey(), 1000)
            .put(EsExecutors.PROCESSORS_SETTING.getKey(), 1) // limit the number of threads created
            .put(NetworkModule.HTTP_ENABLED.getKey(), false)
            .put("transport.type", "local")
            .put(Node.NODE_DATA_SETTING.getKey(), true)
            .put(NodeEnvironment.NODE_ID_SEED_SETTING.getKey(), random().nextLong())
            .put(nodeSettings()) // allow test cases to provide their own settings or override these
            .build();
        Collection<Class<? extends Plugin>> plugins = getPlugins();
        if (plugins.contains(TestZenDiscovery.TestPlugin.class) == false) {
            plugins = new ArrayList<>(plugins);
            plugins.add(TestZenDiscovery.TestPlugin.class);
        }
        Node build = new Node(settings, plugins);
        try {
            build.start();
        } catch (NodeValidationException e) {
            throw new RuntimeException(e);
        }
        return build;
    }
    */
    
    /**
     * Returns a client to the single-node cluster.
     */
    public static Client client() {
        return NODE.client();
    }

    /**
     * Return a reference to the singleton node.
     */
    protected Node node() {
        return NODE;
    }
    
    /**
     * Returns the single test nodes name.
     */
    public static String nodeName() {
        return "node_s_0";
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
     * Get an instance for a particular class using the injector of the singleton node.
     */
    protected <T> T getInstanceFromNode(Class<T> clazz) {
        return NODE.injector().getInstance(clazz);
    }

    /**
     * Create a new index on the singleton node with empty index settings.
     */
    protected IndexService createIndex(String index) {
        return createIndex(index, Settings.EMPTY);
    }

    /**
     * Create a new index on the singleton node with the provided index settings.
     */
    protected IndexService createIndex(String index, Settings settings) {
        return createIndex(index, settings, null, (XContentBuilder) null);
    }

    /**
     * Create a new index on the singleton node with the provided index settings.
     */
    protected IndexService createIndex(String index, Settings settings, String type, XContentBuilder mappings) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client().admin().indices().prepareCreate(index).setSettings(settings);
        if (type != null && mappings != null) {
            createIndexRequestBuilder.addMapping(type, mappings);
        }
        return createIndex(index, createIndexRequestBuilder);
    }

    /**
     * Create a new index on the singleton node with the provided index settings.
     */
    protected IndexService createIndex(String index, Settings settings, String type, Object... mappings) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client().admin().indices().prepareCreate(index).setSettings(settings);
        if (type != null && mappings != null) {
            createIndexRequestBuilder.addMapping(type, mappings);
        }
        return createIndex(index, createIndexRequestBuilder);
    }

    protected IndexService createIndex(String index, CreateIndexRequestBuilder createIndexRequestBuilder) {
        expand(createIndexRequestBuilder);
        assertAcked(createIndexRequestBuilder.get());
        // Wait for the index to be allocated so that cluster state updates don't override
        // changes that would have been done locally
        ClusterHealthRequestBuilder builder = client().admin().cluster().prepareHealth(index);
        expand(builder);
        builder.setWaitForYellowStatus()
            .setWaitForEvents(Priority.LANGUID)
            .setWaitForNoRelocatingShards(true);
        ClusterHealthResponse health = builder.get();
        
        assertThat(health.getStatus(), lessThanOrEqualTo(ClusterHealthStatus.YELLOW));
        assertThat("Cluster must be a single node cluster", health.getNumberOfDataNodes(), equalTo(1));
        IndicesService instanceFromNode = getInstanceFromNode(IndicesService.class);
        return instanceFromNode.indexServiceSafe(resolveIndex(index));
    }

    protected static org.elasticsearch.index.engine.Engine engine(IndexService service) {
        return service.getShard(0).getEngine();
    }
    
    public Index resolveIndex(String index) {
        GetIndexResponse getIndexResponse = client().admin().indices().prepareGetIndex().setIndices(index).get();
        assertTrue("index " + index + " not found", getIndexResponse.getSettings().containsKey(index));
        String uuid = getIndexResponse.getSettings().get(index).get(IndexMetaData.SETTING_INDEX_UUID);
        return new Index(index, uuid);
    }

    /**
     * Create a new search context.
     */
    protected SearchContext createSearchContext(IndexService indexService) {
        BigArrays bigArrays = indexService.getBigArrays();
        ThreadPool threadPool = indexService.getThreadPool();
        return new TestSearchContext(threadPool, bigArrays, indexService);
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
        ClusterHealthRequestBuilder builder = client().admin().cluster().prepareHealth(indices);
        expand(builder);
        builder.setTimeout(timeout)
            .setWaitForGreenStatus()
            .setWaitForEvents(Priority.LANGUID)
            .setWaitForNoRelocatingShards(true);
        ClusterHealthResponse actionGet = builder.get();
        if (actionGet.isTimedOut()) {
            logger.info("ensureGreen timed out, cluster state:\n{}\n{}", client().admin().cluster().prepareState().get().getState(), client().admin().cluster().preparePendingClusterTasks().get());
            assertThat("timed out waiting for green state", actionGet.isTimedOut(), equalTo(false));
        }
        assertThat(actionGet.getStatus(), equalTo(ClusterHealthStatus.GREEN));
        logger.debug("indices {} are green", indices.length == 0 ? "[_all]" : indices);
        return actionGet.getStatus();
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        return getInstanceFromNode(NamedXContentRegistry.class);
    }
}
