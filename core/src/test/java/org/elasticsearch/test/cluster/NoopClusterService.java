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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionWriteResponse.ShardInfo;
import org.elasticsearch.action.index.IndexRequest;
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
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.common.component.LifecycleListener;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.DummyTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
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

public class NoopClusterService implements ClusterService {

    final ClusterState state;

    public NoopClusterService() {
        this(ClusterState.builder(new ClusterName("noop")).build());
    }

    public NoopClusterService(ClusterState state) {
        if (state.getNodes().size() == 0) {
            state = ClusterState.builder(state).nodes(
                    DiscoveryNodes.builder()
                            .put(new DiscoveryNode("noop_id", DummyTransportAddress.INSTANCE, Version.CURRENT))
                            .localNodeId("noop_id")).build();
        }

        assert state.getNodes().localNode() != null;
        this.state = state;

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

    }

    @Override
    public void removeInitialStateBlock(ClusterBlock block) throws IllegalStateException {

    }

    @Override
    public OperationRouting operationRouting() {
        return null;
    }

    @Override
    public void addFirst(ClusterStateListener listener) {

    }

    @Override
    public void addLast(ClusterStateListener listener) {

    }

    @Override
    public void add(ClusterStateListener listener) {

    }

    @Override
    public void remove(ClusterStateListener listener) {

    }

    @Override
    public void add(LocalNodeMasterListener listener) {

    }

    @Override
    public void remove(LocalNodeMasterListener listener) {

    }

    @Override
    public void add(TimeValue timeout, TimeoutClusterStateListener listener) {

    }

    @Override
    public void submitStateUpdateTask(String source, ClusterStateUpdateTask updateTask) {

    }

    @Override
    public <T> void submitStateUpdateTask(String source, T task, ClusterStateTaskConfig config, ClusterStateTaskExecutor<T> executor, ClusterStateTaskListener listener) {

    }

    @Override
    public List<PendingClusterTask> pendingTasks() {
        return null;
    }

    @Override
    public int numberOfPendingTasks() {
        return 0;
    }

    @Override
    public TimeValue getMaxTaskWaitTime() {
        return TimeValue.timeValueMillis(0);
    }

    @Override
    public TaskManager getTaskManager() {
        return null;
    }

    @Override
    public Lifecycle.State lifecycleState() {
        return null;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public ClusterService start() {
        return null;
    }

    @Override
    public ClusterService stop() {
        return null;
    }

    @Override
    public void close() {

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
    public Map<BytesRef, Query> loadQueries(IndexService indexService, PercolatorQueriesRegistry percolator) {
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
    public void index(String[] indices, Collection<Range<Token>> tokenRanges) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void blockingMappingUpdate(IndexService indexService, String type, String source) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges,
            Collection<Range<Token>> requestTokenRange) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean tokenRangesContains(Collection<Range<Token>> shardTokenRanges, Token token) {
        // TODO Auto-generated method stub
        return false;
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
    public MetaData checkForNewMetaData(Long version) throws NoPersistedMetaDataException {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dropIndexKeyspace(String ksName) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AbstractSearchStrategy searchStrategy(IndexMetaData indexMetaData, ClusterState state) {
        // TODO Auto-generated method stub
        return null;
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
    public String getElasticAdminKeyspaceName() {
        // TODO Auto-generated method stub
        return null;
    }
}
