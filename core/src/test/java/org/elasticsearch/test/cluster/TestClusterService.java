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
package org.elasticsearch.test.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elassandra.NoPersistedMetaDataException;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.cluster.routing.AbstractSearchStrategy.Router;
import org.elassandra.cluster.routing.PrimaryFirstSearchStrategy.PrimaryFirstRouter;
import org.elassandra.index.search.TokenRangesService;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionWriteResponse.ShardInfo;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateTaskConfig;
import org.elasticsearch.cluster.ClusterStateTaskExecutor;
import org.elasticsearch.cluster.ClusterStateTaskListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.LocalNodeMasterListener;
import org.elasticsearch.cluster.TimeoutClusterStateListener;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.allocation.decider.AwarenessAllocationDecider;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.common.component.LifecycleListener;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.DummyTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine.GetResult;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.percolator.PercolatorQueriesRegistry;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.tasks.TaskManager;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/** a class that simulate simple cluster service features, like state storage and listeners */
public class TestClusterService implements ClusterService {

    volatile ClusterState state;
    private volatile TaskManager taskManager;
    private final List<ClusterStateListener> listeners = new CopyOnWriteArrayList<>();
    private final Queue<NotifyTimeout> onGoingTimeouts = ConcurrentCollections.newQueue();
    private final ThreadPool threadPool;
    private final ESLogger logger = Loggers.getLogger(getClass(), Settings.EMPTY);
    private final OperationRouting operationRouting = new OperationRouting(Settings.Builder.EMPTY_SETTINGS, new AwarenessAllocationDecider(), this);

    public TestClusterService() {
        this(ClusterState.builder(new ClusterName("test")).build());
    }

    public TestClusterService(ThreadPool threadPool) {
        this(ClusterState.builder(new ClusterName("test")).build(), threadPool);
        taskManager = new TaskManager(Settings.EMPTY);
    }

    public TestClusterService(ThreadPool threadPool, TransportService transportService) {
        this(ClusterState.builder(new ClusterName("test")).build(), threadPool);
        taskManager = transportService.getTaskManager();
    }

    public TestClusterService(ClusterState state) {
        this(state, null);
    }

    public TestClusterService(ClusterState state, @Nullable ThreadPool threadPool) {
        if (state.getNodes().size() == 0) {
            state = ClusterState.builder(state).nodes(
                    DiscoveryNodes.builder()
                            .put(new DiscoveryNode("test_node", DummyTransportAddress.INSTANCE, Version.CURRENT))
                            .localNodeId("test_node")).build();
        }

        assert state.getNodes().localNode() != null;
        this.state = state;
        this.threadPool = threadPool;

    }


    /** set the current state and trigger any registered listeners about the change, mimicking an update task */
    synchronized public ClusterState setState(ClusterState state) {
        assert state.getNodes().localNode() != null;
        // make sure we have a version increment
        state = ClusterState.builder(state).version(this.state.version() + 1).build();
        return setStateAndNotifyListeners(state);
    }

    private ClusterState setStateAndNotifyListeners(ClusterState state) {
        ClusterChangedEvent event = new ClusterChangedEvent("test", state, this.state);
        this.state = state;
        for (ClusterStateListener listener : listeners) {
            listener.clusterChanged(event);
        }
        return state;
    }

    /** set the current state and trigger any registered listeners about the change */
    public ClusterState setState(ClusterState.Builder state) {
        return setState(state.build());
    }

    @Override
    public DiscoveryNode localNode() {
        return state.getNodes().localNode();
    }

    @Override
    public ClusterState state() {
        return state;
    }

    @Override
    public void addInitialStateBlock(ClusterBlock block) throws IllegalStateException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeInitialStateBlock(ClusterBlock block) throws IllegalStateException {
        throw new UnsupportedOperationException();

    }

    @Override
    public OperationRouting operationRouting() {
        return operationRouting;
    }

    @Override
    public void addFirst(ClusterStateListener listener) {
        listeners.add(0, listener);
    }

    @Override
    public void addLast(ClusterStateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void add(ClusterStateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(ClusterStateListener listener) {
        listeners.remove(listener);
        for (Iterator<NotifyTimeout> it = onGoingTimeouts.iterator(); it.hasNext(); ) {
            NotifyTimeout timeout = it.next();
            if (timeout.listener.equals(listener)) {
                timeout.cancel();
                it.remove();
            }
        }
    }

    @Override
    public void add(LocalNodeMasterListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(LocalNodeMasterListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final TimeValue timeout, final TimeoutClusterStateListener listener) {
        if (threadPool == null) {
            throw new UnsupportedOperationException("TestClusterService wasn't initialized with a thread pool");
        }
        NotifyTimeout notifyTimeout = new NotifyTimeout(listener, timeout);
        notifyTimeout.future = threadPool.schedule(timeout, ThreadPool.Names.GENERIC, notifyTimeout);
        onGoingTimeouts.add(notifyTimeout);
        listeners.add(listener);
        listener.postAdded();
    }

    @Override
    public void submitStateUpdateTask(String source, ClusterStateUpdateTask updateTask) {
        submitStateUpdateTask(source, null, updateTask, updateTask, updateTask);
    }

    @Override
    synchronized public <T> void submitStateUpdateTask(final String source, T task, ClusterStateTaskConfig config, ClusterStateTaskExecutor<T> executor, final ClusterStateTaskListener listener) {
        logger.debug("processing [{}]", source);
        if (state().nodes().localNodeMaster() == false && executor.runOnlyOnMaster()) {
            listener.onNoLongerMaster(source);
            logger.debug("failed [{}], no longer master", source);
            return;
        }
        ClusterStateTaskExecutor.BatchResult<T> batchResult;
        ClusterState previousClusterState = state;
        try {
            batchResult = executor.execute(previousClusterState, Arrays.asList(task));
        } catch (Exception e) {
            batchResult = ClusterStateTaskExecutor.BatchResult.<T>builder().failure(task, e).build(previousClusterState, config.doPresistMetaData());
        }

        batchResult.executionResults.get(task).handle(
            new Runnable() {
                @Override
                public void run() {

                }
            },
            new ClusterStateTaskExecutor.TaskResult.FailureConsumer() {
                @Override
                public void accept(Throwable ex) {
                    listener.onFailure(source, new ElasticsearchException("failed to process cluster state update task [" + source + "]", ex));
                }
            }
        );

        setStateAndNotifyListeners(batchResult.resultingState);
        listener.clusterStateProcessed(source, previousClusterState, batchResult.resultingState);
        logger.debug("finished [{}]", source);

    }

    @Override
    public TimeValue getMaxTaskWaitTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public List<PendingClusterTask> pendingTasks() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int numberOfPendingTasks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lifecycle.State lifecycleState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClusterService start() throws ElasticsearchException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClusterService stop() throws ElasticsearchException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws ElasticsearchException {
        throw new UnsupportedOperationException();
    }

    class NotifyTimeout implements Runnable {
        final TimeoutClusterStateListener listener;
        final TimeValue timeout;
        volatile ScheduledFuture future;

        NotifyTimeout(TimeoutClusterStateListener listener, TimeValue timeout) {
            this.listener = listener;
            this.timeout = timeout;
        }

        public void cancel() {
            FutureUtils.cancel(future);
        }

        @Override
        public void run() {
            if (future != null && future.isCancelled()) {
                return;
            }
            listener.onTimeout(this.timeout);
            // note, we rely on the listener to remove itself in case of timeout if needed
        }
    }

    @Override
    public IndexService indexService(String index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexService indexServiceSafe(String index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractSearchStrategy searchStrategy(IndexMetaData indexMetaData, ClusterState clusterState) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Router getRouter(IndexMetaData indexMetaData, ClusterState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrimaryFirstRouter updateRouter(IndexMetaData indexMetaData, ClusterState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, GetField> flattenGetField(String[] fieldFilter, String path, Object node,
            Map<String, GetField> flatFields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, List<Object>> flattenTree(Set<String> neededFiedls, String path, Object node,
            Map<String, List<Object>> fields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createOrUpdateElasticAdminKeyspace() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createIndexKeyspace(String index, int replicationFactor) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createSecondaryIndices(IndexMetaData indexMetaData) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropSecondaryIndices(IndexMetaData indexMetaData) throws RequestExecutionException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public DocPrimaryKey parseElasticId(String index, String cfName, String id)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClusterState updateNumberOfShards(ClusterState currentState) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void submitNumberOfShardsUpdate() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateRoutingTable() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isStaticDocument(String index, Uid uid)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public GetResult fetchSourceInternal(String ksName, String index, String type, String id) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> rowAsMap(String index, String type, Row row) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int rowAsMap(String index, String type, Row row, Map<String, Object> map) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object[] rowAsArray(String index, String type, Row row) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] rowAsArray(String index, String type, Row row, boolean valueForSearch) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteRow(String index, String type, String id, ConsistencyLevel cl)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void blockingMappingUpdate(IndexService indexService, String type, String source) throws Exception {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String source)
            throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException,
            RequestValidationException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MetaData checkForNewMetaData(Long version) throws NoPersistedMetaDataException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Map<UUID, ShardRoutingState> getShardRoutingStates(String index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putShardRoutingState(String index, ShardRoutingState shardRoutingState)
            throws JsonGenerationException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isDatacenterGroupMember(InetAddress endpoint) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ShardInfo shardInfo(String index, ConsistencyLevel cl) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Settings settings() {
        return null;
    }

    @Override
    public void dropIndexKeyspace(String ksName) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropTable(String ksName, String cfName) throws RequestExecutionException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateTableSchema(IndexService indexService, MappingMetaData mappingMd) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void publishGossipStates() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateDocument(IndicesService indicesService, IndexRequest request, IndexMetaData indexMetaData)
            throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void insertDocument(IndicesService indicesService, IndexRequest request, IndexMetaData indexMetaData)
            throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addPost(ClusterStateListener listener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, Uid uid)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, String type, String id)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addShardStartedBarrier() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeShardStartedBarrier() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void blockUntilShardsStarted() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropSecondaryIndex(CFMetaData cfMetaData) throws RequestExecutionException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean rowExists(MapperService mapperService, String type, String id)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UntypedResultSet fetchRow(String ksName, String index, String type, String id, String[] columns,
            Map<String, ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRow(String ksName, String index, String cfName, String id, String[] columns,
            ConsistencyLevel cl, Map<String, ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRow(String ksName, String index, String cfName, DocPrimaryKey docPk, String[] columns,
            ConsistencyLevel cl, Map<String, ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String buildFetchQuery(String ksName, String index, String cfName, String[] requiredColumns,
            boolean forStaticDocument, Map<String, ColumnDefinition> columnDefs)
            throws ConfigurationException, IndexNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRowInternal(String ksName, String index, String cfName, String id, String[] columns,
            Map<String, ColumnDefinition> columnDefs) throws ConfigurationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRowInternal(String ksName, String index, String cfName, DocPrimaryKey docPk,
            String[] columns, Map<String, ColumnDefinition> columnDefs) throws ConfigurationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRowInternal(String ksName, String index, String cfName, String[] columns,
            Object[] pkColumns, boolean forStaticDocument, Map<String, ColumnDefinition> columnDefs)
            throws ConfigurationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query, Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getElasticAdminKeyspaceName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getLocalDataCenterSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public TokenRangesService getTokenRangesService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateMapping(String ksName, MappingMetaData mapping) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void recoverShard(String index) {
        // TODO Auto-generated method stub
        
    }

}
