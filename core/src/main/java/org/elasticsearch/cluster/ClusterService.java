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

package org.elasticsearch.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.UntypedResultSet;
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
import org.elassandra.cluster.routing.PrimaryFirstSearchStrategy;
import org.elasticsearch.action.ActionWriteResponse.ShardInfo;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContent.Params;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.percolator.PercolatorQueriesRegistry;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.tasks.TaskManager;

/**
 * The cluster service allowing to both register for cluster state events ({@link ClusterStateListener})
 * and submit state update tasks ({@link ClusterStateUpdateTask}.
 */
public interface ClusterService extends LifecycleComponent<ClusterService> {

    /**
     * The local node.
     */
    DiscoveryNode localNode();

    /**
     * The current state.
     */
    ClusterState state();

    /**
     * The cluster settings
     */
    Settings settings();
    
    /**
     * Adds an initial block to be set on the first cluster state created.
     */
    void addInitialStateBlock(ClusterBlock block) throws IllegalStateException;

    /**
     * Remove an initial block to be set on the first cluster state created.
     */
    void removeInitialStateBlock(ClusterBlock block) throws IllegalStateException;

    /**
     * The operation routing.
     */
    OperationRouting operationRouting();

    /**
     * Adds a priority listener for updated cluster states.
     */
    void addFirst(ClusterStateListener listener);

    /**
     * Adds last listener.
     */
    void addLast(ClusterStateListener listener);

    /**
     * Adds post-applied listener.
     */
    void addPost(ClusterStateListener listener);
    

    public void addShardStartedBarrier();
    public void removeShardStartedBarrier();
    public void blockUntilShardsStarted();
    
    
    /**
     * Adds a listener for updated cluster states.
     */
    void add(ClusterStateListener listener);

    /**
     * Removes a listener for updated cluster states.
     */
    void remove(ClusterStateListener listener);

    /**
     * Add a listener for on/off local node master events
     */
    void add(LocalNodeMasterListener listener);

    /**
     * Remove the given listener for on/off local master events
     */
    void remove(LocalNodeMasterListener listener);

    /**
     * Adds a cluster state listener that will timeout after the provided timeout,
     * and is executed after the clusterstate has been successfully applied ie. is
     * in state {@link org.elasticsearch.cluster.ClusterState.ClusterStateStatus#APPLIED}
     * NOTE: a {@code null} timeout means that the listener will never be removed
     * automatically
     */
    void add(@Nullable TimeValue timeout, TimeoutClusterStateListener listener);

    /**
     * Submits a cluster state update task; submitted updates will be
     * batched across the same instance of executor. The exact batching
     * semantics depend on the underlying implementation but a rough
     * guideline is that if the update task is submitted while there
     * are pending update tasks for the same executor, these update
     * tasks will all be executed on the executor in a single batch
     *
     * @param source   the source of the cluster state update task
     * @param task     the state needed for the cluster state update task
     * @param config   the cluster state update task configuration
     * @param executor the cluster state update task executor; tasks
     *                 that share the same executor will be executed
     *                 batches on this executor
     * @param listener callback after the cluster state update task
     *                 completes
     * @param <T>      the type of the cluster state update task state
     */
    <T> void submitStateUpdateTask(final String source, final T task,
                                   final ClusterStateTaskConfig config,
                                   final ClusterStateTaskExecutor<T> executor,
                                   final ClusterStateTaskListener listener);

    /**
     * Submits a cluster state update task; unlike {@link #submitStateUpdateTask(String, Object, ClusterStateTaskConfig, ClusterStateTaskExecutor, ClusterStateTaskListener)},
     * submitted updates will not be batched.
     *
     * @param source     the source of the cluster state update task
     * @param updateTask the full context for the cluster state update
     *                   task
     */
    void submitStateUpdateTask(final String source, final ClusterStateUpdateTask updateTask);

    /**
     * Returns the tasks that are pending.
     */
    List<PendingClusterTask> pendingTasks();

    /**
     * Returns the number of currently pending tasks.
     */
    int numberOfPendingTasks();

    /**
     * Returns the maximum wait time for tasks in the queue
     *
     * @return A zero time value if the queue is empty, otherwise the time value oldest task waiting in the queue
     */
    TimeValue getMaxTaskWaitTime();

    /**
     * Returns task manager created in the cluster service
     */
    TaskManager getTaskManager();
    
   


    public IndexService indexService(String index);
    public IndexService indexServiceSafe(String index);
    
 
    @SuppressForbidden(reason = "toUpperCase() for consistency level")
    public static ConsistencyLevel consistencyLevelFromString(String value) {
        switch(value.toUpperCase()) {
        case "ANY": return ConsistencyLevel.ANY;
        case "ONE": return ConsistencyLevel.ONE;
        case "TWO": return ConsistencyLevel.TWO;
        case "THREE": return ConsistencyLevel.THREE;
        case "QUORUM": return ConsistencyLevel.QUORUM;
        case "ALL": return ConsistencyLevel.ALL;
        case "LOCAL_QUORUM": return ConsistencyLevel.LOCAL_QUORUM;
        case "EACH_QUORUM": return ConsistencyLevel.EACH_QUORUM;
        case "SERIAL": return ConsistencyLevel.SERIAL;
        case "LOCAL_SERIAL": return ConsistencyLevel.LOCAL_SERIAL;
        case "LOCAL_ONE": return ConsistencyLevel.LOCAL_ONE;
        default :
            throw new IllegalArgumentException("No write consistency match [" + value + "]");
        }
    }
    
    /**
     * Persisted metadata should not include number_of_shards nor number_of_replica.
     */
    public static String PERSISTED_METADATA = "cassandra.pertisted.metadata";
    public static ToXContent.Params persistedParams = new Params() {
        @Override
        public String param(String key) {
            if (PERSISTED_METADATA.equals(key)) return "true";
            return null;
        }

        @Override
        public String param(String key, String defaultValue) {
            if (PERSISTED_METADATA.equals(key)) return "true";
            return defaultValue;
        }

        @Override
        public boolean paramAsBoolean(String key, boolean defaultValue) {
            if (PERSISTED_METADATA.equals(key)) return true;
            return defaultValue;
        }

        @Override
        public Boolean paramAsBoolean(String key, Boolean defaultValue) {
            if (PERSISTED_METADATA.equals(key)) return new Boolean(true);
            return defaultValue;
        }
    };
    
    public static class DocPrimaryKey {
        public String[] names;
        public Object[] values;
        public boolean isStaticDocument; // pk = partition key and pk has clustering key.
        
        public DocPrimaryKey(String[] names, Object[] values, boolean isStaticDocument) {
            this.names = names;
            this.values = values;
            this.isStaticDocument = isStaticDocument;
        }
        
        public DocPrimaryKey(String[] names, Object[] values) {
            this.names = names;
            this.values = values;
            this.isStaticDocument = false;
        }
    }
    
    public void publishGossipStates();
    
    public AbstractSearchStrategy searchStrategy(IndexMetaData indexMetaData, ClusterState state);
    public AbstractSearchStrategy.Router getRouter(IndexMetaData indexMetaData, ClusterState state);
    public PrimaryFirstSearchStrategy.PrimaryFirstRouter updateRouter(IndexMetaData indexMetaData, ClusterState state);
    
    public Map<String, GetField> flattenGetField(final String[] fieldFilter, final String path, final Object node, Map<String, GetField> flatFields);
    public Map<String, List<Object>> flattenTree(final Set<String> neededFiedls, final String path, final Object node, Map<String, List<Object>> fields);

    public void createOrUpdateElasticAdminKeyspace();
    public String getElasticAdminKeyspaceName();
    
    public void createIndexKeyspace(String index, int replicationFactor) throws IOException;
    public void dropIndexKeyspace(String ksName) throws IOException;
    
    public void createSecondaryIndices(final IndexMetaData indexMetaData) throws IOException;
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException;
    public void dropSecondaryIndices(final IndexMetaData indexMetaData) throws RequestExecutionException;
    public void dropSecondaryIndex(CFMetaData cfMetaData) throws RequestExecutionException;
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException;
    public void dropTable(String ksName, String cfName) throws RequestExecutionException;
    
    public DocPrimaryKey parseElasticId(final String index, final String cfName, final String id) throws JsonParseException, JsonMappingException, IOException;
    
    public ClusterState updateNumberOfShards(ClusterState currentState);
    public void submitNumberOfShardsUpdate();
    public void updateRoutingTable();
    
    public void updateTableSchema(final IndexService indexService, final MappingMetaData mappingMd) throws IOException;
    
    public boolean isStaticDocument(final String index, final Uid uid) throws JsonParseException, JsonMappingException, IOException;
    public boolean rowExists(final MapperService mapperService, final String type, final String id) throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    
    public UntypedResultSet fetchRow(final String ksName, final String index, final String type, final String id, final String[] columns, Map<String,ColumnDefinition> columnDefs) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    public UntypedResultSet fetchRow(final String ksName, final String index, final String cfName, final String id, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    public UntypedResultSet fetchRow(final String ksName, final String index, final String cfName, final  DocPrimaryKey docPk, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    public String buildFetchQuery(final String ksName, final String index, final String cfName, final String[] requiredColumns, boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IndexNotFoundException, IOException;
    
    public UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final String id, final String[] columns,  Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException;
    public UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final  DocPrimaryKey docPk, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException;
    public UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final String[] columns, Object[] pkColumns, boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException;
    
    public Engine.GetResult fetchSourceInternal(String ksName, String index, String type, String id) throws IOException;
    
    public Map<BytesRef, Query> loadQueries(final IndexService indexService,  PercolatorQueriesRegistry percolator);
    
    public Map<String, Object> rowAsMap(final String index, final String type, UntypedResultSet.Row row) throws IOException;
    public int rowAsMap(final String index, final String type, UntypedResultSet.Row row, Map<String, Object> map) throws IOException;
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row) throws IOException;
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException;
    
    public void deleteRow(String index, String type, String id, ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    public void updateDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception;
    public void insertDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception;

    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, Uid uid) throws JsonParseException, JsonMappingException, IOException;
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, String type, String id) throws JsonParseException, JsonMappingException, IOException;
     
    public void index(String[] indices, Collection<Range<Token>> tokenRanges);

    //public void index(String index, String type, String id, Object[] sourceData);

    public void blockingMappingUpdate(IndexService indexService, String type, String source) throws Exception;
    
    /*
    public Token getToken(ByteBuffer rowKey, ColumnFamily cf);
    public Token getToken(String index, String type, String routing) throws JsonParseException, JsonMappingException, IOException;
    */
    
    public boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges, Collection<Range<Token>> requestTokenRange);
    public boolean tokenRangesContains(Collection<Range<Token>> shardTokenRanges, Token token);
    
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException;
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException;
    public MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException;
    public MetaData checkForNewMetaData(Long version) throws NoPersistedMetaDataException;
    public void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String source) throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException,
            RequestValidationException;
    
    /**
     * Get indices shard state from gossip endpoints state map.
     * @param address
     * @param index
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public Map<UUID, ShardRoutingState> getShardRoutingStates(String index);
    
    
    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void putShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException;
    

    boolean isDatacenterGroupMember(InetAddress endpoint);
    
    public ShardInfo shardInfo(String index, ConsistencyLevel cl);
    
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query) throws RequestExecutionException, RequestValidationException, InvalidRequestException;
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query, Object... values) throws RequestExecutionException, RequestValidationException, InvalidRequestException;
}
