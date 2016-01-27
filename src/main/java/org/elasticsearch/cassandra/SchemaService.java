/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.cassandra;

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

import org.apache.cassandra.config.ColumnDefinition;
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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper.Names;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.indices.IndicesService;

public interface SchemaService extends ClusterService {

    public static final String ELASTIC_ADMIN_KEYSPACE = "elastic_admin";
    public static final String ELASTIC_ADMIN_METADATA_TABLE_PREFIX = "metadata_";

    public Map<String, GetField> flattenGetField(final String[] fieldFilter, final String path, final Object node, Map<String, GetField> flatFields);
    public Map<String, List<Object>> flattenTree(final Set<String> neededFiedls, final String path, final Object node, Map<String, List<Object>> fields);

    public void createElasticAdminKeyspace() throws Exception;
    public void createIndexKeyspace(String index, int replicationFactor) throws IOException;
    
    public void createSecondaryIndices(String index) throws IOException;
    public void createSecondaryIndex(String ksName, MappingMetaData mapping) throws IOException;
    public void dropSecondaryIndices(String ksName) throws RequestExecutionException;
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException;
    
    public void removeIndexKeyspace(String index) throws IOException;
    
    public String buildUDT(String ksName, String cfName, String name, ObjectMapper objectMapper) throws RequestExecutionException;

    public void updateTableSchema(String index, String type, Set<String> columns, DocumentMapper docMapper) throws IOException;
    
    public List<ColumnDefinition> getPrimaryKeyColumns(String ksName, String cfName) throws ConfigurationException;

    public Collection<String> mappedColumns(final String index, final String type);
    public String[] mappedColumns(final MapperService mapperService, final String type);
    
    public UntypedResultSet fetchRow(String index, String type, Collection<String> requiredColumns,String id) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException;

    public UntypedResultSet fetchRow(String index, String type, Collection<String> requiredColumns, String id, ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException;

    public UntypedResultSet fetchRow(final String index, final String type, final String id) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    public UntypedResultSet fetchRowInternal(String index, String type, Collection<String> requiredColumns, String id) throws ConfigurationException, IOException;
    public UntypedResultSet fetchRowInternal(String ksName, String cfName, Collection<String> requiredColumns, Object[] pkColumns) throws ConfigurationException, IOException;
    
    public Map<String, Object> rowAsMap(final String index, final String type, UntypedResultSet.Row row) throws IOException;
    public int rowAsMap(final String index, final String type, UntypedResultSet.Row row, Map<String, Object> map) throws IOException;

    public void deleteRow(String index, String type, String id, ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    public String insertDocument(IndicesService indicesService, IndexRequest request, ClusterState clusterState, Long writetime, Boolean applied) throws Exception;

    public String insertRow(String index, String type, Map<String, Object> map, String id, boolean ifNotExists, long ttl, ConsistencyLevel cl, Long writetime, Boolean applied)
            throws Exception;

    public void index(String[] indices, Collection<Range<Token>> tokenRanges);

    //public void index(String index, String type, String id, Object[] sourceData);

    public void blockingMappingUpdate(IndexService indexService, String type, CompressedXContent source) throws Exception;
    
    
    public Token getToken(ByteBuffer rowKey, ColumnFamily cf);

    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException;

    public void initializeMetaDataAsComment();

    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException;

    public MetaData readMetaDataAsRow() throws NoPersistedMetaDataException;

    public void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String metaDataString, String source) throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException,
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
    public ShardRoutingState readIndexShardState(InetAddress address, String index, ShardRoutingState defaultState);
    
    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void writeIndexShardSate(String index, ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException;
}
