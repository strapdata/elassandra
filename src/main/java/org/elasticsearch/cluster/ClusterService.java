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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.serializers.AsciiSerializer;
import org.apache.cassandra.serializers.BooleanSerializer;
import org.apache.cassandra.serializers.BytesSerializer;
import org.apache.cassandra.serializers.DecimalSerializer;
import org.apache.cassandra.serializers.DoubleSerializer;
import org.apache.cassandra.serializers.EmptySerializer;
import org.apache.cassandra.serializers.FloatSerializer;
import org.apache.cassandra.serializers.Int32Serializer;
import org.apache.cassandra.serializers.IntegerSerializer;
import org.apache.cassandra.serializers.ListSerializer;
import org.apache.cassandra.serializers.LongSerializer;
import org.apache.cassandra.serializers.MapSerializer;
import org.apache.cassandra.serializers.SetSerializer;
import org.apache.cassandra.serializers.TimeUUIDSerializer;
import org.apache.cassandra.serializers.TimestampSerializer;
import org.apache.cassandra.serializers.TypeSerializer;
import org.apache.cassandra.serializers.UTF8Serializer;
import org.apache.cassandra.serializers.UUIDSerializer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cassandra.NoPersistedMetaDataException;
import org.elasticsearch.cassandra.cluster.routing.AbstractSearchStrategy;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper.Names;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.indices.IndicesService;

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

    IndexService indexService(String index);
    
    /**
     * Adds a priority listener for updated cluster states.
     */
    void addFirst(ClusterStateListener listener);

    /**
     * Adds last listener.
     */
    void addLast(ClusterStateListener listener);

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
     * Submits a task that will update the cluster state.
     */
    void submitStateUpdateTask(final String source, Priority priority, final ClusterStateUpdateTask updateTask);

    /**
     * Submits a task that will update the cluster state (the task has a default priority of {@link Priority#NORMAL}).
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
     * @returns A zero time value if the queue is empty, otherwise the time value oldest task waiting in the queue
     */
    TimeValue getMaxTaskWaitTime();

    
    public static final String ELASTIC_ADMIN_KEYSPACE = "elastic_admin";
    public static final String ELASTIC_ADMIN_METADATA_TABLE = "metadata";

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
    
    static class Utils {
        public static final org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
        
        public static ArrayNode addToJsonArray(final AbstractType<?> type, final Object value, ArrayNode an) {
            TypeSerializer<?> typeSerializer = type.getSerializer();
            if (typeSerializer instanceof BooleanSerializer) an.add((Boolean)value); 
            else if ((typeSerializer instanceof IntegerSerializer) || (typeSerializer instanceof Int32Serializer)) an.add((Integer)value);
            else if (typeSerializer instanceof LongSerializer) an.add( (Long) value);
            else if (typeSerializer instanceof DoubleSerializer) an.add( (Double) value);
            else if (typeSerializer instanceof DecimalSerializer) an.add( (BigDecimal) value);
            else if (typeSerializer instanceof FloatSerializer) an.add( (Float) value);
            else if (typeSerializer instanceof TimestampSerializer) an.add( ((Date) value).getTime());
            else an.add(stringify(type, value));
            return an;
        }
        
        public static String writeValueAsString(ArrayNode an) throws JsonGenerationException, JsonMappingException, IOException {
            if (an.size() == 1) {
                return an.get(0).asText();
            }
            return jsonMapper.writeValueAsString(an);
        }
        
        public static String stringify(AbstractType<?> type, ByteBuffer buf) {
            return stringify(type, type.compose(buf));
        }
        
        public static String stringify(AbstractType<?> type, Object value) {
            // Thanks' generics !
            TypeSerializer<?> typeSerializer = type.getSerializer();
            if (typeSerializer instanceof AsciiSerializer) return ((AsciiSerializer)typeSerializer).toString( (String)value);
            if (typeSerializer instanceof UTF8Serializer) return ((UTF8Serializer)typeSerializer).toString( (String)value);
            if (typeSerializer instanceof BooleanSerializer) return ((BooleanSerializer)typeSerializer).toString( (Boolean)value );
            if (typeSerializer instanceof BytesSerializer) return ((BytesSerializer)typeSerializer).toString( (ByteBuffer)value );
            if (typeSerializer instanceof DecimalSerializer) return ((DecimalSerializer)typeSerializer).toString( (BigDecimal)value );
            if (typeSerializer instanceof DoubleSerializer) return ((DoubleSerializer)typeSerializer).toString( (Double)value );
            if (typeSerializer instanceof FloatSerializer) return ((FloatSerializer)typeSerializer).toString( (Float)value );
            if (typeSerializer instanceof LongSerializer) return ((LongSerializer)typeSerializer).toString( (Long)value );
            if (typeSerializer instanceof Int32Serializer) return ((Int32Serializer)typeSerializer).toString( (Integer)value );
            if (typeSerializer instanceof IntegerSerializer) return ((IntegerSerializer)typeSerializer).toString( (BigInteger)value );
            if (typeSerializer instanceof TimestampSerializer) return ((TimestampSerializer)typeSerializer).toString( (Date)value );
            if (typeSerializer instanceof TimeUUIDSerializer) return ((TimeUUIDSerializer)typeSerializer).toString( (UUID)value );
            if (typeSerializer instanceof UUIDSerializer) return ((UUIDSerializer)typeSerializer).toString( (UUID)value );
            if (typeSerializer instanceof ListSerializer) return ((ListSerializer)typeSerializer).toString( (List)value );
            if (typeSerializer instanceof SetSerializer) return ((SetSerializer)typeSerializer).toString( (Set)value );
            if (typeSerializer instanceof MapSerializer) return ((MapSerializer)typeSerializer).toString( (Map)value );
            if (typeSerializer instanceof EmptySerializer) return ((EmptySerializer)typeSerializer).toString( (Void)value );
            return null;
        }
        
        public static void toXContent(XContentBuilder builder, Mapper mapper, String field, Object value) throws IOException {
            if (value instanceof Collection) {
               if (field == null) {
                   builder.startArray(); 
               } else {
                   builder.startArray(field);
               }
               for(Iterator<Object> i = ((Collection)value).iterator(); i.hasNext(); ) {
                   toXContent(builder, mapper, null, i.next());
               } 
               builder.endArray();
            } else if (value instanceof Map) {
               Map<String, Object> map = (Map<String,Object>)value;
               if (field != null) {
                   builder.startObject(field);
               } else {
                   builder.startObject();
               }
               for(String subField : map.keySet()) {
                   if (mapper != null) {
                       if (mapper instanceof ObjectMapper) {
                           toXContent(builder, ((ObjectMapper)mapper).getMapper(subField), subField, map.get(subField));
                       } else if (mapper instanceof GeoPointFieldMapper) {
                           GeoPointFieldMapper geoMapper = (GeoPointFieldMapper)mapper;
                           if (geoMapper.fieldType().isLatLonEnabled()) {
                               Iterator<Mapper> it = geoMapper.iterator();
                               switch(subField) {
                               case Names.LAT:
                                   toXContent(builder, it.next(), Names.LAT, map.get(Names.LAT));
                                   break;
                               case Names.LON: 
                                   it.next();
                                   toXContent(builder, it.next(), Names.LON, map.get(Names.LON));
                                   break;
                               }
                           } else {
                               // No mapper known
                               // TODO: support geohashing
                               builder.field(subField, map.get(subField));
                           }
                       } else {
                           builder.field(subField, map.get(subField));
                       }
                   } else {
                       builder.field(subField, map.get(subField));
                   }
               }
               builder.endObject();
            } else {
               FieldMapper fieldMapper = (FieldMapper)mapper;
               if (field != null) {
                   builder.field(field, (fieldMapper==null) ? value : fieldMapper.fieldType().valueForSearch(value));
               } else {
                   builder.value((fieldMapper==null) ? value : fieldMapper.fieldType().valueForSearch(value));
               }
            }
        }
        
        public static XContentBuilder buildDocument(DocumentMapper documentMapper, Map<String, Object> docMap, boolean humanReadable) throws IOException {
            return buildDocument(documentMapper, docMap, humanReadable, false);
        }
            
        public static XContentBuilder buildDocument(DocumentMapper documentMapper, Map<String, Object> docMap, boolean humanReadable, boolean forStaticDocument) throws IOException {
            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).humanReadable(true);
            builder.startObject();
            for(String field : docMap.keySet()) {
                if (field.equals("_parent")) continue;
                FieldMapper fieldMapper = documentMapper.mappers().smartNameFieldMapper(field);
                if (fieldMapper != null) {
                    if (forStaticDocument) {
                       if (!isStaticOrPartitionKey(fieldMapper)) continue;
                    } 
                    toXContent(builder, fieldMapper, field, docMap.get(field));
                } else {
                    ObjectMapper objectMapper = documentMapper.objectMappers().get(field);
                    if (objectMapper != null) {
                         if (forStaticDocument) {
                            if (!isStaticOrPartitionKey(objectMapper)) continue;
                         } 
                         toXContent(builder, objectMapper, field, docMap.get(field));
                    } else {
                        Loggers.getLogger(ClusterService.class).error("No mapper found for field "+field);
                        throw new IOException("No mapper found for field "+field);
                    }
                }
            }
            builder.endObject();
            return builder;
        }
            
        public static boolean isStaticOrPartitionKey(Mapper mapper) {
            return mapper.cqlStaticColumn() || mapper.cqlPartitionKey();
        }
   
    }
    
    public AbstractSearchStrategy.Result searchStrategy(IndexMetaData indexMetaData, Collection<InetAddress> startedShards);
    
    public Map<String, GetField> flattenGetField(final String[] fieldFilter, final String path, final Object node, Map<String, GetField> flatFields);
    public Map<String, List<Object>> flattenTree(final Set<String> neededFiedls, final String path, final Object node, Map<String, List<Object>> fields);

    public void createElasticAdminKeyspace() throws Exception;
    public void createIndexKeyspace(String index, int replicationFactor) throws IOException;
    
    public void createSecondaryIndices(String index) throws IOException;
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException;
    public void dropSecondaryIndices(String ksName) throws RequestExecutionException;
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException;
    
    public void removeIndexKeyspace(String index) throws IOException;
    
    public void buildCollectionMapping(Map<String, Object> mapping, final AbstractType<?> type) throws IOException;
    public String buildUDT(String ksName, String cfName, String name, ObjectMapper objectMapper) throws RequestExecutionException;

    public String buildFetchQuery(final String index, final String cfName, final String[] requiredColumns, boolean forStaticDocument) throws ConfigurationException, IndexNotFoundException;
    public DocPrimaryKey parseElasticId(final String index, final String cfName, final String id) throws JsonParseException, JsonMappingException, IOException;
    
    public ClusterState updateNumberOfShards(ClusterState currentState);
    public void submitNumberOfShardsUpdate();
    
    public void updateTableSchema(String index, String type, Set<String> columns, DocumentMapper docMapper) throws IOException;
    
    public String[] mappedColumns(final String index, Uid uid) throws JsonParseException, JsonMappingException, IOException;
    public String[] mappedColumns(final String index, final String type,final boolean forStaticDocument);
    public String[] mappedColumns(final MapperService mapperService, final String type, final boolean forStaticDocument);
    
    public boolean isStaticDocument(final String index, Uid uid) throws JsonParseException, JsonMappingException, IOException;
    
    public UntypedResultSet fetchRow(String index, String type, String[] requiredColumns,String id) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException;
    public UntypedResultSet fetchRow(String index, String type, String[] requiredColumns, String id, ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException;

    public UntypedResultSet fetchRow(final String index, final String type, final String id) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    public UntypedResultSet fetchRowInternal(final String index, final String cfName, final String[] requiredColumns, final String id) throws ConfigurationException, IOException;
    public UntypedResultSet fetchRowInternal(final String ksName, final String cfName, final String[] requiredColumns, Object[] pkColumns, boolean forStaticDocument) throws ConfigurationException, IOException;
    
    public Map<String, Object> rowAsMap(final String index, final String type, UntypedResultSet.Row row) throws IOException;
    public int rowAsMap(final String index, final String type, UntypedResultSet.Row row, Map<String, Object> map) throws IOException;
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row) throws IOException;
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException;
    
    public void deleteRow(String index, String type, String id, ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    public void insertDocument(IndicesService indicesService, IndexRequest request, ClusterState clusterState, String timestampString) throws Exception;

    public void index(String[] indices, Collection<Range<Token>> tokenRanges);

    //public void index(String index, String type, String id, Object[] sourceData);

    public void blockingMappingUpdate(IndexService indexService, String type, CompressedXContent source) throws Exception;
    
    public CFMetaData getCFMetaData(final String ksName, final String cfName) throws ActionRequestValidationException;
    
    public Token getToken(ByteBuffer rowKey, ColumnFamily cf);
    public Token getToken(String index, String type, String routing) throws JsonParseException, JsonMappingException, IOException;
    public boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges, Collection<Range<Token>> requestTokenRange);
    public boolean tokenRangesContains(Collection<Range<Token>> shardTokenRanges, Token token);
    
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException;
    public void initializeMetaDataAsComment();
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException;
    public MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException;
    public MetaData checkForNewMetaData(Long version) throws NoPersistedMetaDataException;
    
    public void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String source) throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException,
            RequestValidationException;
    
    public Map<String, Object> expandTableMapping(final String ksName, Map<String, Object> mapping) throws IOException, SyntaxException, ConfigurationException;
    public Map<String, Object> expandTableMapping(final String ksName, final String cfName, Map<String, Object> mapping) throws IOException, SyntaxException, ConfigurationException;


    /**
     * Block until all local shards are STARTED.
     * @param startingLatch
     */
    public void waitShardsStarted();
    
    /**
     * Write local primary shard states in Gossip.X2
     */
    public void publishAllShardsState();
    
    
    /**
     * Get indices shard state from gossip endpoints state map.
     * @param address
     * @param index
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public ShardRoutingState getShardRoutingState(final InetAddress address, final String index, ShardRoutingState defaultState);
    
    
    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void putShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException;
    
    /**
     * Return a set of remote started shards according t the gossip state map.
     * @param index
     * @return a set of remote started shards according t the gossip state map.
     */
    public Set<InetAddress> getStartedShard(String index);
    
}
