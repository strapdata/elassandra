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
package org.elassandra.cluster;

import static org.apache.cassandra.cql3.QueryProcessor.executeInternal;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.cql3.statements.IndexTarget;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.TupleType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.schema.ReplicationParams;
import org.apache.cassandra.serializers.AsciiSerializer;
import org.apache.cassandra.serializers.BooleanSerializer;
import org.apache.cassandra.serializers.BytesSerializer;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.DecimalSerializer;
import org.apache.cassandra.serializers.DoubleSerializer;
import org.apache.cassandra.serializers.EmptySerializer;
import org.apache.cassandra.serializers.FloatSerializer;
import org.apache.cassandra.serializers.Int32Serializer;
import org.apache.cassandra.serializers.IntegerSerializer;
import org.apache.cassandra.serializers.ListSerializer;
import org.apache.cassandra.serializers.LongSerializer;
import org.apache.cassandra.serializers.MapSerializer;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.serializers.SetSerializer;
import org.apache.cassandra.serializers.TimeUUIDSerializer;
import org.apache.cassandra.serializers.TimestampSerializer;
import org.apache.cassandra.serializers.TypeSerializer;
import org.apache.cassandra.serializers.UTF8Serializer;
import org.apache.cassandra.serializers.UUIDSerializer;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.Server;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elassandra.ConcurrentMetaDataUpdateException;
import org.elassandra.NoPersistedMetaDataException;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.cluster.routing.PrimaryFirstSearchStrategy;
import org.elassandra.index.ExtendedElasticSecondaryIndex;
import org.elassandra.index.mapper.internal.NodeFieldMapper;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elassandra.shard.CassandraShardStartedBarrier;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionWriteResponse.ShardInfo;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingClusterStateUpdateRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateNonMasterUpdateTask;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataMappingService;
import org.elasticsearch.cluster.node.DiscoveryNodeService;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.InternalClusterService;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.DocumentAlreadyExistsException;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentFieldMappers;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.CqlCollection;
import org.elasticsearch.index.mapper.Mapper.CqlStruct;
import org.elasticsearch.index.mapper.MapperException;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.core.BinaryFieldMapper;
import org.elasticsearch.index.mapper.core.BooleanFieldMapper;
import org.elasticsearch.index.mapper.core.ByteFieldMapper;
import org.elasticsearch.index.mapper.core.CompletionFieldMapper;
import org.elasticsearch.index.mapper.core.CompletionFieldMapper.Fields;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.DoubleFieldMapper;
import org.elasticsearch.index.mapper.core.FloatFieldMapper;
import org.elasticsearch.index.mapper.core.IntegerFieldMapper;
import org.elasticsearch.index.mapper.core.LongFieldMapper;
import org.elasticsearch.index.mapper.core.ShortFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapperLegacy;
import org.elasticsearch.index.mapper.geo.GeoShapeFieldMapper;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.RoutingFieldMapper;
import org.elasticsearch.index.mapper.internal.SourceFieldMapper;
import org.elasticsearch.index.mapper.internal.TTLFieldMapper;
import org.elasticsearch.index.mapper.internal.TimestampFieldMapper;
import org.elasticsearch.index.mapper.ip.IpFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.percolator.PercolatorQueriesRegistry;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.translog.Translog;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.settings.NodeSettingsService;
import org.elasticsearch.percolator.PercolatorService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;



/**
 *
 */
public class InternalCassandraClusterService extends InternalClusterService {

    public static final String ELASTIC_ID_COLUMN_NAME = "_id";
    public static final String ELASTIC_ADMIN_KEYSPACE = "elastic_admin";
    public static final String ELASTIC_ADMIN_METADATA_TABLE = "metadata";

    public static String SETTING_CLUSTER_DATACENTER_GROUP = "datacenter.group";
    
    // elassandra dynamic cluster settings
    public static String SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT = "cluster.mapping_update_timeout";
    public static String SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS = "cluster.default_secondary_index_class";
    public static String SETTING_CLUSTER_DEFAULT_SEARCH_STRATEGY_CLASS = "cluster.default_search_strategy_class";
    public static String SETTING_CLUSTER_DEFAULT_INCLUDE_NODE_ID = "cluster.default_include_node_id";
    public static String SETTING_CLUSTER_DEFAULT_SYNCHRONOUS_REFRESH = "cluster.default_synchronous_refresh";
    public static String SETTING_CLUSTER_DEFAULT_DROP_ON_DELETE_INDEX = "cluster.default_drop_on_delete_index";
    public static String SETTING_CLUSTER_DEFAULT_SNAPSHOT_WITH_SSTABLE = "cluster.default_snapshot_with_sstable";
    
    public static Map<String, String> cqlMapping = new ImmutableMap.Builder<String,String>()
            .put("text", "string")
            .put("varchar", "string")
            .put("timestamp", "date")
            .put("int", "integer")
            .put("double", "double")
            .put("float", "float")
            .put("bigint", "long")
            .put("boolean", "boolean")
            .put("blob", "binary")
            .put("inet", "ip" )
            .put("uuid", "string" )
            .put("timeuuid", "string" )
            .build();
    

    public static Map<Class, String> mapperToCql = new java.util.HashMap<Class, String>() {
        {
            put(StringFieldMapper.class, "text");
            put(DateFieldMapper.class, "timestamp");
            put(IntegerFieldMapper.class, "int");
            put(DoubleFieldMapper.class, "double");
            put(FloatFieldMapper.class, "float");
            put(LongFieldMapper.class, "bigint");
            put(ShortFieldMapper.class, "int");
            put(ByteFieldMapper.class, "int");
            put(BooleanFieldMapper.class, "boolean");
            put(BinaryFieldMapper.class, "blob");
            put(IpFieldMapper.class, "inet");
            put(TimestampFieldMapper.class, "timestamp");
            
            put(GeoShapeFieldMapper.class,"text");
            put(SourceFieldMapper.class,"blob");
        }
    };

    public static Map<String,Class> cqlToMapperBuilder = new java.util.HashMap<String, Class>() {
        {
            put("text", StringFieldMapper.Builder.class);
            put("ascii", StringFieldMapper.Builder.class);
            put("timestamp", DateFieldMapper.Builder.class);
            put("double", DoubleFieldMapper.Builder.class);
            put("float" , FloatFieldMapper.Builder.class);
            put("int", IntegerFieldMapper.Builder.class);
            put("bigint", LongFieldMapper.Builder.class);
            put("smallint", ShortFieldMapper.Builder.class);
            put("boolean", BooleanFieldMapper.Builder.class);
            put("blob", BinaryFieldMapper.Builder.class);
            put("inet", IpFieldMapper.Builder.class);
        }
    };
    
    private final TimeValue mappingUpdateTimeout;
    
    private final DiscoveryService discoveryService;
    protected final MappingUpdatedAction mappingUpdatedAction;
    
    public final static Class defaultSecondaryIndexClass = ExtendedElasticSecondaryIndex.class;
    
    protected final PrimaryFirstSearchStrategy primaryFirstSearchStrategy = new PrimaryFirstSearchStrategy();
    protected final Map<String, AbstractSearchStrategy> strategies = new ConcurrentHashMap<String, AbstractSearchStrategy>();
    protected final Map<String, AbstractSearchStrategy.Router> routers = new ConcurrentHashMap<String, AbstractSearchStrategy.Router>();
     
    private final ConsistencyLevel metadataWriteCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.write.cl","QUORUM"));
    private final ConsistencyLevel metadataReadCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.read.cl","QUORUM"));
    private final ConsistencyLevel metadataSerialCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.serial.cl","SERIAL"));
    
    private final String elasticAdminKeyspaceName;
    private final String selectMetadataQuery;
    private final String insertMetadataQuery;
    private final String updateMetaDataQuery;
    
    private CassandraShardStartedBarrier shardStateObserver = null;
    
    @Inject
    public InternalCassandraClusterService(Settings settings, DiscoveryService discoveryService, OperationRouting operationRouting, 
            TransportService transportService, NodeSettingsService nodeSettingsService,
            ThreadPool threadPool, ClusterName clusterName, DiscoveryNodeService discoveryNodeService, Version version, 
            IndicesService indicesService, IndicesLifecycle indicesLifecycle, MappingUpdatedAction mappingUpdatedAction
            ) {
        super(settings, discoveryService, operationRouting, transportService, nodeSettingsService, threadPool, clusterName, discoveryNodeService, version, indicesService, indicesLifecycle);
        this.mappingUpdateTimeout = settings.getAsTime(SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT, TimeValue.timeValueSeconds(30));
        this.mappingUpdatedAction = mappingUpdatedAction;
        this.discoveryService = discoveryService;
        
        String datacenterGroup = settings.get(SETTING_CLUSTER_DATACENTER_GROUP);
        if (datacenterGroup != null && datacenterGroup.length() > 0) {
            logger.info("Starting with datacenter.group=[{}]", datacenterGroup.trim().toLowerCase(Locale.ROOT));
            elasticAdminKeyspaceName = String.format(Locale.ROOT, "%s_%s", ELASTIC_ADMIN_KEYSPACE,datacenterGroup.trim().toLowerCase(Locale.ROOT));
        } else {
            elasticAdminKeyspaceName = ELASTIC_ADMIN_KEYSPACE;
        }
        selectMetadataQuery = String.format(Locale.ROOT, "SELECT metadata,version,owner FROM \"%s\".\"%s\" WHERE cluster_name = ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        insertMetadataQuery = String.format(Locale.ROOT, "INSERT INTO \"%s\".\"%s\" (cluster_name,owner,version,metadata) VALUES (?,?,?,?) IF NOT EXISTS", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        updateMetaDataQuery = String.format(Locale.ROOT, "UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, metadata = ? WHERE cluster_name = ? IF version < ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
    }
    
    public String getElasticAdminKeyspaceName() {
        return this.elasticAdminKeyspaceName;
    }
    
    public boolean isNativeCql3Type(String cqlType) {
        return cqlMapping.keySet().contains(cqlType) && !cqlType.startsWith("geo_");
    }
    
    public Class<AbstractSearchStrategy> searchStrategyClass(IndexMetaData indexMetaData, ClusterState state) {
        return AbstractSearchStrategy.getSearchStrategyClass( 
                indexMetaData.getSettings().get(IndexMetaData.SETTING_SEARCH_STRATEGY_CLASS, 
                state.metaData().settings().get(InternalCassandraClusterService.SETTING_CLUSTER_DEFAULT_SEARCH_STRATEGY_CLASS,PrimaryFirstSearchStrategy.class.getName()))
                );
    }
    
    private AbstractSearchStrategy searchStrategyInstance(Class<AbstractSearchStrategy> clazz) {
        AbstractSearchStrategy searchStrategy = strategies.get(clazz.getName());
        if (searchStrategy == null) {
            try {
                searchStrategy = clazz.newInstance();
            } catch (Exception e) {
                logger.error("Cannot instanciate search strategy {}", e, clazz.getName());
                searchStrategy = new PrimaryFirstSearchStrategy();
            }
            strategies.putIfAbsent(clazz.getName(), searchStrategy);
        }
        return searchStrategy;
    }
    
    @Override
    public PrimaryFirstSearchStrategy.PrimaryFirstRouter updateRouter(IndexMetaData indexMetaData, ClusterState state) {
        // update and returns a PrimaryFirstRouter for the build table.
        PrimaryFirstSearchStrategy.PrimaryFirstRouter router = (PrimaryFirstSearchStrategy.PrimaryFirstRouter)this.primaryFirstSearchStrategy.newRouter(indexMetaData.getIndex(), indexMetaData.keyspace(), getShardRoutingStates(indexMetaData.getIndex()), state);
        
        // update the router cache with the effective router
        AbstractSearchStrategy effectiveSearchStrategy = searchStrategyInstance(searchStrategyClass(indexMetaData, state));
        if (! effectiveSearchStrategy.equals(PrimaryFirstSearchStrategy.class) ) {
            AbstractSearchStrategy.Router router2 = effectiveSearchStrategy.newRouter(indexMetaData.getIndex(), indexMetaData.keyspace(), getShardRoutingStates(indexMetaData.getIndex()), state);
            this.routers.put(indexMetaData.getIndex(), router2);
        } else {
            this.routers.put(indexMetaData.getIndex(), router);
        }
        
        return router;
    }
    
    public AbstractSearchStrategy.Router getRouter(IndexMetaData indexMetaData, ClusterState state) {
        AbstractSearchStrategy.Router router = this.routers.get(indexMetaData.getIndex());
        return router;
    }
    
    @Override
    public UntypedResultSet process(final ConsistencyLevel cl, final ClientState clientState, final String query) 
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, clientState, query, new Long(0), new Object[] {});
    }

    @Override
    public UntypedResultSet process(final ConsistencyLevel cl, final ClientState clientState, final String query, Object... values) 
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, clientState, query, new Long(0), values);
    }
   
    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final String query, final Object... values) 
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, serialConsistencyLevel, ClientState.forInternalCalls(), query, new Long(0), values);
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final String query, Long writetime, final Object... values) {
        return process(cl, serialConsistencyLevel, ClientState.forInternalCalls(), query, writetime, values);
    }
    
    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final ClientState clientState, final String query, Long writetime, final Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        if (logger.isDebugEnabled()) 
            logger.debug("processing CL={} SERIAL_CL={} query={}", cl, serialConsistencyLevel, query);
        
        ParsedStatement.Prepared prepared = QueryProcessor.getStatement(query, clientState);
        List<ByteBuffer> boundValues = new ArrayList<ByteBuffer>(values.length);
        for (int i = 0; i < values.length; i++) {
            Object v = values[i];
            AbstractType type = prepared.boundNames.get(i).type;
            boundValues.add(v instanceof ByteBuffer || v == null ? (ByteBuffer) v : type.decompose(v));
        }
        QueryState queryState = new QueryState(clientState);
        QueryOptions queryOptions = QueryOptions.forInternalCalls(cl, serialConsistencyLevel, boundValues);
        ResultMessage result = QueryProcessor.instance.process(query, queryState, queryOptions);
        writetime = queryState.getTimestamp();
        if (result instanceof ResultMessage.Rows)
            return UntypedResultSet.create(((ResultMessage.Rows) result).result);
        else
            return null;
    }

   
    public boolean processConditional(final ConsistencyLevel cl, final ConsistencyLevel serialCl, final String query, Object... values) 
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        try {
            UntypedResultSet result = process(cl, serialCl, query, values);
            if (serialCl != null) {
                if (!result.isEmpty()) {
                    Row row = result.one();
                    if (row.has("[applied]")) {
                         return row.getBoolean("[applied]");
                    }
                }
                return false;
            } 
            return true;
        } catch (Exception e) {
            logger.error("Failed to process query=" + query + " values=" + Arrays.toString(values), e);
            throw e;
        }
    }

    
    /**
     * Don't use QueryProcessor.executeInternal, we need to propagate this on all nodes.
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#createIndexKeyspace(java.lang.String, int)
     **/
    @Override
    public void createIndexKeyspace(final String ksname, final int replicationFactor) throws IOException {
        try {
            Keyspace ks = Keyspace.open(ksname);
            if (ks != null && !(ks.getReplicationStrategy() instanceof NetworkTopologyStrategy)) {
                throw new IOException("Cannot create index, underlying keyspace requires the NetworkTopologyStrategy.");
            }
        } catch(AssertionError | NullPointerException e) {
        }

        try {
            process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), 
                    String.format(Locale.ROOT, "CREATE KEYSPACE IF NOT EXISTS \"%s\" WITH replication = {'class':'NetworkTopologyStrategy', '%s':'%d' };", 
                    ksname, DatabaseDescriptor.getLocalDataCenter(), replicationFactor));
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    @Override
    public void dropIndexKeyspace(final String ksname) throws IOException {
        try {
            String query = String.format(Locale.ROOT, "DROP KEYSPACE IF EXISTS \"%s\"", ksname);
            logger.debug(query);
            QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static Pair<List<String>, List<String>> getUDTInfo(final String ksName, final String typeName) {
        try {
            UntypedResultSet result = QueryProcessor.executeOnceInternal("SELECT field_names, field_types FROM system_schema.types WHERE keyspace_name = ? AND type_name = ?", 
                    new Object[] { ksName, typeName });
            Row row = result.one();
            if ((row != null) && row.has("field_names")) {
                List<String> field_names = row.getList("field_names", UTF8Type.instance);
                List<String> field_types = row.getList("field_types", UTF8Type.instance);
                return Pair.<List<String>, List<String>> create(field_names, field_types);
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public static MapType<?,?> getMapType(final String ksName, final String cfName, final String colName) {
        try {
            UntypedResultSet result = QueryProcessor.executeOnceInternal("SELECT validator FROM system.schema_columns WHERE keyspace_name = ? AND columnfamily_name = ? AND column_name = ?", 
                    new Object[] { ksName, cfName, colName });
            Row row = result.one();
            if ((row != null) && row.has("validator")) {
                AbstractType<?> type = TypeParser.parse(row.getString("validator"));
                if (type instanceof MapType) {
                    return (MapType<?,?>)type;
                }
            }
        } catch (Exception e) {
            
        }
        return null;
    }
    
    // see https://docs.datastax.com/en/cql/3.0/cql/cql_reference/keywords_r.html
    public static final Pattern keywordsPattern = Pattern.compile("(ADD|ALLOW|ALTER|AND|ANY|APPLY|ASC|AUTHORIZE|BATCH|BEGIN|BY|COLUMNFAMILY|CREATE|DELETE|DESC|DROP|EACH_QUORUM|GRANT|IN|INDEX|INET|INSERT|INTO|KEYSPACE|KEYSPACES|LIMIT|LOCAL_ONE|LOCAL_QUORUM|MODIFY|NOT|NORECURSIVE|OF|ON|ONE|ORDER|PASSWORD|PRIMARY|QUORUM|RENAME|REVOKE|SCHEMA|SELECT|SET|TABLE|TO|TOKEN|THREE|TRUNCATE|TWO|UNLOGGED|UPDATE|USE|USING|WHERE|WITH)"); 
    
    public static boolean isReservedKeyword(String identifier) {
        return keywordsPattern.matcher(identifier.toUpperCase(Locale.ROOT)).matches();
    }
    
    public static String toJsonValue(Object o) {
        if (o instanceof Date)
            return Long.toString( ((Date)o).getTime() );
        if (o instanceof Integer)
            return Integer.toString((Integer)o);
        if (o instanceof Long)
            return Long.toString((Long)o);
        if (o instanceof Double)
            return Double.toString((Double)o);
        if (o instanceof Float)
            return Float.toString((Float)o);
        return o.toString();
    }
    
    private static String stringify(Object o) {
        if (o instanceof String || o instanceof UUID)
            return "\""+o+"\"";
        return toJsonValue(o);
    }
    
    public static String stringify(Object[] cols, int length) {
        if (cols.length == 1)
            return toJsonValue(cols[0]);
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(stringify(cols[i]));
        }
        return sb.append("]").toString();
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
               Object subValue = map.get(subField);
               if (subValue == null)
                   continue;
               
               if (subValue instanceof Collection && ((Collection)subValue).size() == 1)
                   subValue = ((Collection)subValue).iterator().next();

               if (mapper != null) {
                   if (mapper instanceof ObjectMapper) {
                       toXContent(builder, ((ObjectMapper)mapper).getMapper(subField), subField, subValue);
                   } else if (mapper instanceof GeoPointFieldMapper) {
                       GeoPointFieldMapper geoMapper = (GeoPointFieldMapper)mapper;
                       if (geoMapper.fieldType().isLatLonEnabled()) {
                           Iterator<Mapper> it = geoMapper.iterator();
                           switch(subField) {
                           case org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LAT:
                               toXContent(builder, it.next(), org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LAT, map.get(org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LAT));
                               break;
                           case org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LON: 
                               it.next();
                               toXContent(builder, it.next(), org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LON, map.get(org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LON));
                               break;
                           }
                       } else {
                           // No mapper known
                           // TODO: support geohashing
                           builder.field(subField, subValue);
                       }
                   } else {
                       builder.field(subField, subValue);
                   }
               } else {
                   builder.field(subField, subValue);
               }
           }
           builder.endObject();
        } else {
           if (mapper instanceof FieldMapper) {
               FieldMapper fieldMapper = (FieldMapper)mapper;
               if (!(fieldMapper instanceof MetadataFieldMapper)) {
                   if (field != null) {
                       builder.field(field, fieldMapper.fieldType().valueForSearch(value));
                   } else {
                       builder.value((fieldMapper==null) ? value : fieldMapper.fieldType().valueForSearch(value));
                   }
               }
           } else if (mapper instanceof ObjectMapper) {
               ObjectMapper objectMapper = (ObjectMapper)mapper;
               if (!objectMapper.isEnabled()) {
                   builder.field(field, value);
               } else {
                   throw new IOException("Unexpected object value ["+value+"]");
               }
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
            if (field.equals(ParentFieldMapper.NAME)) continue;
            FieldMapper fieldMapper = documentMapper.mappers().smartNameFieldMapper(field);
            if (fieldMapper != null) {
                if (forStaticDocument && !isStaticOrPartitionKey(fieldMapper)) 
                    continue;
                toXContent(builder, fieldMapper, field, docMap.get(field));
            } else {
                ObjectMapper objectMapper = documentMapper.objectMappers().get(field);
                if (objectMapper != null) {
                     if (forStaticDocument && !isStaticOrPartitionKey(objectMapper)) 
                         continue;
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

    public static final String PERCOLATOR_TABLE = "_percolator";
    
    private static final Map<String, String> cfNameToType = new ConcurrentHashMap<String, String>() {{
       put(PERCOLATOR_TABLE, PercolatorService.TYPE_NAME);
    }};
    
    public static String typeToCfName(String type) {
        if (type.indexOf('-') >= 0) {
            String cfName = type.replaceAll("\\-", "_");
            cfNameToType.putIfAbsent(cfName, type);
            return cfName;
        }
        return type;
    }
   
    public static String cfNameToType(String cfName) {
        if (cfName.indexOf('_') >= 0) {
            String type = cfNameToType.get(cfName);
            if (type != null)
                return type;
        }
        return cfName;
    }
    
    public static String indexToKsName(String index) {
        return index.replaceAll("\\.", "_").replaceAll("\\-", "_");
    }
    
    
    public String buildCql(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {
        if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
            return buildUDT(ksName, cfName, name, objectMapper);
        } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
            if (objectMapper.iterator().hasNext()) {
                Mapper childMapper = objectMapper.iterator().next();
                if (childMapper instanceof FieldMapper) {
                    return "map<text,"+mapperToCql.get(childMapper.getClass())+">";
                } else if (childMapper instanceof ObjectMapper) {
                    return "map<text,frozen<"+buildCql(ksName,cfName,childMapper.simpleName(),(ObjectMapper)childMapper)+">>";
                }
            } else {
                // default map prototype, no mapper to determine the value type.
                return "map<text,text>";
            }
        }
        return null;
    }

    public String buildUDT(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {
        String typeName = (objectMapper.cqlUdtName() == null) ? cfName + "_" + objectMapper.fullPath().replace('.', '_') : objectMapper.cqlUdtName();

        if (!objectMapper.iterator().hasNext()) {
            throw new InvalidRequestException("Cannot create an empty nested type (not supported)");
        }
        
        // create sub-type first
        for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
            Mapper mapper = it.next();
            if (mapper instanceof ObjectMapper) {
                buildCql(ksName, cfName, mapper.simpleName(), (ObjectMapper) mapper);
            } else if (mapper instanceof GeoPointFieldMapper) {
                buildGeoPointType(ksName);
            } 
        }

        Pair<List<String>, List<String>> udt = getUDTInfo(ksName, typeName);
        if (udt == null) {
            // create new UDT.
            StringBuilder create = new StringBuilder(String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( ", ksName, typeName));
            boolean first = true;
            for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
                Mapper mapper = it.next();
                if (first)
                    first = false;
                else
                    create.append(", ");
                
                // Use only the last part of the fullname to build UDT.
                int lastDotIndex = mapper.name().lastIndexOf('.');
                String shortName = (lastDotIndex > 0) ? mapper.name().substring(lastDotIndex+1) :  mapper.name();
                
                if (isReservedKeyword(shortName))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", shortName);
                
                create.append('\"').append(shortName).append("\" ");
                if (mapper instanceof ObjectMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_'))
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON))
                        create.append(">");
                } else if (mapper instanceof BaseGeoPointFieldMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(GEO_POINT_TYPE)
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(">");
                } else if (mapper instanceof GeoShapeFieldMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append("text")
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(">");
                } else {
                    String cqlType = mapperToCql.get(mapper.getClass());
                    if (mapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        create.append(cqlType);
                    } else {
                        create.append(mapper.cqlCollectionTag()).append("<");
                        if (!isNativeCql3Type(cqlType)) create.append("frozen<");
                        create.append(cqlType);
                        if (!isNativeCql3Type(cqlType)) create.append(">");
                        create.append(">");
                    }
                }
            }
            create.append(" )");
            if (logger.isDebugEnabled())
                logger.debug("create UDT:"+ create.toString());
            
            QueryProcessor.process(create.toString(), ConsistencyLevel.LOCAL_ONE);
        } else {
            // update existing UDT
            for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
                Mapper mapper = it.next();
                int lastDotIndex = mapper.name().lastIndexOf('.');
                String shortName = (lastDotIndex > 0) ? mapper.name().substring(lastDotIndex+1) :  mapper.name();
                if (isReservedKeyword(shortName))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", shortName);
                
                StringBuilder update = new StringBuilder(String.format(Locale.ROOT, "ALTER TYPE \"%s\".\"%s\" ADD \"%s\" ", ksName, typeName, shortName));
                if (!udt.left.contains(shortName)) {
                    if (mapper instanceof ObjectMapper) {
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(mapper.cqlCollectionTag()).append("<");
                        update.append("frozen<")
                            .append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_'))
                            .append(">");
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(">");
                    } else if (mapper instanceof GeoPointFieldMapper) {
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(mapper.cqlCollectionTag()).append("<");
                        update.append("frozen<")
                            .append(GEO_POINT_TYPE)
                            .append(">");
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(">");
                    } else {
                        String cqlType = mapperToCql.get(mapper.getClass());
                        if (mapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                            update.append(cqlType);
                        } else {
                            update.append(mapper.cqlCollectionTag()).append("<");
                            if (!isNativeCql3Type(cqlType)) update.append("frozen<");
                            update.append(cqlType);
                            if (!isNativeCql3Type(cqlType)) update.append(">");
                            update.append(">");
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("update UDT: "+update.toString());
                    }
                    QueryProcessor.process(update.toString(), ConsistencyLevel.LOCAL_ONE);
                }
            }
        }
        return typeName;
    }

    private static final String GEO_POINT_TYPE = "geo_point";
    private static final String ATTACHEMENT_TYPE = "attachement";
    private static final String COMPLETION_TYPE = "completion";
    
    private void buildGeoPointType(String ksName) throws RequestExecutionException {
        String query = String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( %s double, %s double)", 
                ksName, GEO_POINT_TYPE,org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LAT,org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper.Names.LON);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }

    private void buildAttachementType(String ksName) throws RequestExecutionException {
        String query = String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" (context text, content_type text, content_length bigint, date timestamp, title text, author text, keywords text, language text)", ksName, ATTACHEMENT_TYPE);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }
    
    private void buildCompletionType(String ksName) throws RequestExecutionException {
        String query = String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" (input list<text>, output text, weight bigint, payload text)", ksName, COMPLETION_TYPE);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }
    

    public ClusterState updateNumberOfShards(ClusterState currentState) {
        int numberOfNodes = currentState.nodes().size();
        assert numberOfNodes > 0;
        MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
        for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
            IndexMetaData indexMetaData = it.next();
            IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
            indexMetaDataBuilder.numberOfShards(numberOfNodes);
            String keyspace = indexMetaData.keyspace();
            if (Schema.instance != null && Schema.instance.getKeyspaceInstance(keyspace) != null) {
                AbstractReplicationStrategy replicationStrategy = Schema.instance.getKeyspaceInstance(keyspace).getReplicationStrategy();
                int rf = replicationStrategy.getReplicationFactor();
                if (replicationStrategy instanceof NetworkTopologyStrategy) {
                    rf = ((NetworkTopologyStrategy)replicationStrategy).getReplicationFactor(DatabaseDescriptor.getLocalDataCenter());
                } 
                indexMetaDataBuilder.numberOfReplicas( Math.max(0, rf - 1) );
            } else {
                indexMetaDataBuilder.numberOfReplicas( 0 );
            }
            metaDataBuilder.put(indexMetaDataBuilder.build(), false);
        }
        return ClusterState.builder(currentState).metaData(metaDataBuilder.build()).build();
    }
    
    /**
     * Submit an updateTask to update numberOfShard and numberOfReplica of all indices in clusterState.
     */
    public void submitNumberOfShardsUpdate() {
        submitStateUpdateTask("Update numberOfShard and numberOfReplica of all indices" , new ClusterStateNonMasterUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                return updateNumberOfShards(currentState);
            }
    
            @Override
            public boolean doPresistMetaData() {
                return false;
            }
    
            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }
        });
    }

    @Override
    public void updateRoutingTable() {
        submitStateUpdateTask("Update-routing-table" , new ClusterStateNonMasterUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                 return ClusterState.builder(currentState).incrementVersion().build();
            }
    
            @Override
            public boolean doPresistMetaData() {
                return false;
            }
    
            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }
        });
    }
    
    @Override
    public void updateTableSchema(final IndexService indexService, final MappingMetaData mappingMd) throws IOException {
        try {
            String ksName = indexService.keyspace();
            String cfName = InternalCassandraClusterService.typeToCfName(mappingMd.type());
            
            createIndexKeyspace(ksName, settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 0) +1);
            
            CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
            boolean newTable = (cfm == null);
            
            DocumentMapper docMapper = indexService.mapperService().documentMapper(mappingMd.type());
            Map<String, Object> mappingMap = mappingMd.sourceAsMap();
            
            Set<String> columns = new HashSet();
            if (docMapper.sourceMapper().enabled()) 
                columns.add(SourceFieldMapper.NAME);
            if (mappingMap.get("properties") != null) 
                columns.addAll( ((Map<String, Object>)mappingMap.get("properties")).keySet() );
            
            logger.debug("Updating CQL3 schema {}.{} columns={}", ksName, cfName, columns);
            StringBuilder columnsList = new StringBuilder();
            Map<String,Boolean> columnsMap = new HashMap<String, Boolean>(columns.size());
            String[] primaryKeyList = new String[(newTable) ? columns.size()+1 : cfm.partitionKeyColumns().size()+cfm.clusteringColumns().size()];
            int primaryKeyLength = 0;
            int partitionKeyLength = 0;
            for (String column : columns) {
                if (isReservedKeyword(column))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", column);
                
                if (column.equals(TokenFieldMapper.NAME))
                    continue; // ignore pseudo column known by Elasticsearch
                
                if (columnsList.length() > 0) 
                    columnsList.append(',');
                
                String cqlType = null;
                boolean isStatic = false;
                FieldMapper fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                if (fieldMapper != null) {
                    if (fieldMapper instanceof GeoPointFieldMapper || fieldMapper instanceof GeoPointFieldMapperLegacy) {
                        ColumnDefinition cdef = (newTable) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                        if (cdef != null && cdef.type instanceof UTF8Type) {
                            // index geohash stored as text in cassandra.
                            cqlType = "text";
                        } else {
                            // create a geo_point UDT to store lat,lon
                            cqlType = GEO_POINT_TYPE;
                            buildGeoPointType(ksName);
                        }
                    } else if (fieldMapper instanceof GeoShapeFieldMapper) {
                        cqlType = "text";
                    } else if (fieldMapper instanceof CompletionFieldMapper) {
                        cqlType = COMPLETION_TYPE;
                        buildCompletionType(ksName);
                    } else if (fieldMapper.getClass().getName().equals("org.elasticsearch.mapper.attachments.AttachmentMapper")) {
                        // attachement is a plugin, so class may not found.
                        cqlType = ATTACHEMENT_TYPE;
                        buildAttachementType(ksName);
                    } else if (fieldMapper instanceof SourceFieldMapper) {
                        cqlType = "blob";
                    } else {
                        cqlType = mapperToCql.get(fieldMapper.getClass());
                        if (cqlType == null) {
                            logger.warn("Ignoring field [{}] type [{}]", column, fieldMapper.name());
                            continue;
                        }
                    }
                    
                    columnsMap.put(column, fieldMapper.cqlPartialUpdate());
                    if (fieldMapper.cqlPrimaryKeyOrder() >= 0) {
                        if (fieldMapper.cqlPrimaryKeyOrder() < primaryKeyList.length && primaryKeyList[fieldMapper.cqlPrimaryKeyOrder()] == null) {
                            primaryKeyList[fieldMapper.cqlPrimaryKeyOrder()] = column;
                            primaryKeyLength = Math.max(primaryKeyLength, fieldMapper.cqlPrimaryKeyOrder()+1);
                            if (fieldMapper.cqlPartitionKey()) {
                                partitionKeyLength++;
                            }
                        } else {
                            throw new Exception("Wrong primary key order for column "+column);
                        }
                    }
                    
                    if (!isNativeCql3Type(cqlType)) {
                        cqlType = "frozen<" + cqlType + ">";
                    }
                    if (!fieldMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        cqlType = fieldMapper.cqlCollectionTag()+"<" + cqlType + ">";
                    }
                    isStatic = fieldMapper.cqlStaticColumn();
                } else {
                    ObjectMapper objectMapper = docMapper.objectMappers().get(column);
                    if (objectMapper == null) {
                       logger.warn("Cannot infer CQL type from object mapping for field [{}]", column);
                       continue;
                    }
                    columnsMap.put(column,  objectMapper.cqlPartialUpdate());
                    if (objectMapper.cqlPrimaryKeyOrder() >= 0) {
                        if (objectMapper.cqlPrimaryKeyOrder() < primaryKeyList.length && primaryKeyList[objectMapper.cqlPrimaryKeyOrder()] == null) {
                            primaryKeyList[objectMapper.cqlPrimaryKeyOrder()] = column;
                            primaryKeyLength = Math.max(primaryKeyLength, objectMapper.cqlPrimaryKeyOrder()+1);
                            if (objectMapper.cqlPartitionKey()) {
                                partitionKeyLength++;
                            }
                        } else {
                            throw new Exception("Wrong primary key order for column "+column);
                        }
                    }
                    if (!objectMapper.isEnabled()) {
                        logger.debug("Object [{}] not enabled stored as text", column);
                        cqlType = "text";
                    } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
                        // TODO: check columnName exists and is map<text,?>
                        cqlType = buildCql(ksName, cfName, column, objectMapper);
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                            cqlType = objectMapper.cqlCollectionTag()+"<"+cqlType+">";
                        }
                        //logger.debug("Expecting column [{}] to be a map<text,?>", column);
                    } else  if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                        // enabled=false => opaque json object
                        cqlType = (objectMapper.isEnabled()) ? "frozen<" + ColumnIdentifier.maybeQuote(buildCql(ksName, cfName, column, objectMapper)) + ">" : "text";
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON) && !(cfName.equals(PERCOLATOR_TABLE) && column.equals("query"))) {
                            cqlType = objectMapper.cqlCollectionTag()+"<"+cqlType+">";
                        }
                    }
                    isStatic = objectMapper.cqlStaticColumn();
                }

                
                if (newTable) {
                    if (cqlType != null) {
                        columnsList.append("\"").append(column).append("\" ").append(cqlType);
                        if (isStatic) columnsList.append(" static");
                    }
                } else {
                    ColumnDefinition cdef = cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                    if (cqlType != null) {
                        if (cdef == null) {
                            for(int i=0; i < primaryKeyLength; i++) {
                                if (primaryKeyList[i] != null && primaryKeyList[i].equals(column))
                                    throw new Exception("Cannot alter primary key of an existing table");
                            }
                            try {
                                String query = String.format(Locale.ROOT, "ALTER TABLE \"%s\".\"%s\" ADD \"%s\" %s %s", ksName, cfName, column, cqlType,(isStatic) ? "static":"");
                                logger.debug(query);
                                QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                            } catch (Exception e) {
                                logger.warn("Failed to alter table {}.{} column [{}] with type [{}]", e, ksName, cfName, column, cqlType);
                            }
                        } else {
                            // check that the existing column matches the provided mapping
                            // TODO: do this check for collection
                            String existingCqlType = cdef.type.asCQL3Type().toString();
                            if (!cdef.type.isCollection()) {
                                // cdef.type.asCQL3Type() does not include frozen, nor quote, so can do this check for collection.
                                if (!existingCqlType.equals(cqlType) && 
                                    !cqlType.equals("frozen<"+existingCqlType+">") &&
                                    !(existingCqlType.endsWith("uuid") && cqlType.equals("text")) ) // #74 uuid is mapped as text
                                    throw new IOException("Existing column "+column+" mismatch type "+cqlType);
                            }
                        }
                    }
                }
              
            }

            // add _parent column if necessary. Parent and child documents should have the same partition key.
            if (docMapper.parentFieldMapper().active() && docMapper.parentFieldMapper().pkColumns() == null) {
                if (newTable) {
                    // _parent is a JSON array representation of the parent PK.
                    if (columnsList.length() > 0) 
                        columnsList.append(", ");
                    columnsList.append("\"_parent\" text");
                } else {
                    try {
                        String query = String.format(Locale.ROOT, "ALTER TABLE \"%s\".\"%s\" ADD \"_parent\" text", ksName, cfName);
                        logger.debug(query);
                        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                    } catch (org.apache.cassandra.exceptions.InvalidRequestException e) {
                        logger.warn("Cannot alter table {}.{} column _parent with type text", e, ksName, cfName);
                    }
                }
            }
            
            if (newTable) {
                if (partitionKeyLength == 0) {
                    // build a default primary key _id text
                    if (columnsList.length() > 0) columnsList.append(',');
                    columnsList.append("\"").append(ELASTIC_ID_COLUMN_NAME).append("\" text");
                    primaryKeyList[0] = ELASTIC_ID_COLUMN_NAME;
                    primaryKeyLength = 1;
                    partitionKeyLength = 1;
                }
                // build the primary key definition
                StringBuilder primaryKey = new StringBuilder();
                primaryKey.append("(");
                for(int i=0; i < primaryKeyLength; i++) {
                    if (primaryKeyList[i] == null) throw new Exception("Incomplet primary key definition at index "+i);
                    primaryKey.append("\"").append( primaryKeyList[i] ).append("\"");
                    if ( i == partitionKeyLength -1) primaryKey.append(")");
                    if (i+1 < primaryKeyLength )   primaryKey.append(",");
                }
                String query = String.format(Locale.ROOT, "CREATE TABLE IF NOT EXISTS \"%s\".\"%s\" ( %s, PRIMARY KEY (%s) ) WITH COMMENT='Auto-created by Elassandra'", 
                        ksName, cfName, columnsList.toString(), primaryKey.toString());
                logger.debug(query);
                QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
            }
            
            updateMapping(indexService.index().name(), mappingMd);
            
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    @Override
    public void createSecondaryIndices(final IndexMetaData indexMetaData) throws IOException {
        String ksName = indexMetaData.keyspace();
        String className = indexMetaData.getSettings().get(IndexMetaData.SETTING_SECONDARY_INDEX_CLASS, this.settings.get(SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS, defaultSecondaryIndexClass.getName()));
        for(ObjectCursor<MappingMetaData> cursor: indexMetaData.getMappings().values()) {
            createSecondaryIndex(ksName, cursor.value, className);
        }
    }
   
    // build secondary indices when shard is started and mapping applied
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException {
        final String cfName = typeToCfName(mapping.type());
        final CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
        if (cfm != null) {
            String query=null;
            try {
                Map<String,Object> mappingProperties = (Map<String,Object>)mapping.sourceAsMap().get("properties");
                if (mappingProperties != null) {
                    for (Map.Entry<String, Object> entry : mappingProperties.entrySet()) {
                        String column = entry.getKey();
                        if (column.startsWith("_")) {
                            continue; // ignore pseudo column known by Elasticsearch
                        }
                        Map<String,Object> props = (Map<String,Object>)entry.getValue();
                        if ( (props.get("index") != null && "no".equals(props.get("index"))) || 
                             (props.get("enabled") != null && !XContentMapValues.nodeBooleanValue(props.get("enabled"))) ) {
                            continue; // ignore field with index:no or enabled=false
                        }
                        ColumnDefinition cdef = (cfm == null) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                        if ((cdef != null) && !(cfm.partitionKeyColumns().size()==1 && cdef.kind == ColumnDefinition.Kind.PARTITION_KEY )) {
                            query = String.format(Locale.ROOT, "CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" (\"%s\") USING '%s' %s",
                                    buildIndexName(cfName, column), ksName, cfName, column, className, cdef.isStatic() ? "WITH options = { 'enforce':'true' }" : "");
                            logger.debug(query);
                            QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                        } 
                    }
                }
            } catch (Throwable e) {
                throw new IOException("Failed to process query=["+query+"]:"+e.getMessage(), e);
            }
        } else {
            logger.warn("Cannot create SECONDARY INDEX, [{}.{}] does not exist",ksName, cfName);
        }
    }
    
    @Override
    public void dropSecondaryIndices(final IndexMetaData indexMetaData) throws RequestExecutionException {
        String ksName = indexMetaData.keyspace();
        for(CFMetaData cfMetaData : Schema.instance.getKSMetaData(ksName).tablesAndViews()) {
            if (cfMetaData.isCQLTable())
                dropSecondaryIndex(cfMetaData);
        }
    }
   
    @Override
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException  {
        dropSecondaryIndex(Schema.instance.getCFMetaData(ksName, cfName));
    }
    
    @Override
    public void dropSecondaryIndex(CFMetaData cfMetaData) throws RequestExecutionException  {
        for(IndexMetadata idx : cfMetaData.getIndexes()) {
            if (idx.isCustom()) {
                String className = idx.options.get(IndexTarget.CUSTOM_INDEX_OPTION_NAME);
                if (className != null && className.endsWith("ElasticSecondaryIndex")) {
                    logger.debug("DROP INDEX IF EXISTS {}.{}", cfMetaData.ksName, idx.name);
                    QueryProcessor.process(String.format(Locale.ROOT, "DROP INDEX IF EXISTS \"%s\".\"%s\"",
                            cfMetaData.ksName, idx.name), 
                            ConsistencyLevel.LOCAL_ONE);
                }
            }
        }
    }
    
    public void dropTables(final IndexMetaData indexMetaData) throws RequestExecutionException {
        String ksName = indexMetaData.keyspace();
        for(ObjectCursor<String> cfName : indexMetaData.getMappings().keys()) {
            dropTable(ksName, cfName.value);
        }
    }
    
    @Override
    public void dropTable(String ksName, String cfName) throws RequestExecutionException  {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
        if (cfm != null) {
            logger.warn("DROP TABLE IF EXISTS {}.{}", ksName, cfName);
            QueryProcessor.process(String.format(Locale.ROOT, "DROP TABLE IF EXISTS \"%s\".\"%s\"", ksName, cfName), ConsistencyLevel.LOCAL_ONE);
        }
    }
    
    
    public static String buildIndexName(final String cfName, final String colName) {
        return new StringBuilder("elastic_")
            .append(cfName).append('_')
            .append(colName.replaceAll("\\W+", "_"))
            .append("_idx").toString();
    }
    
    public static CFMetaData getCFMetaData(final String ksName, final String cfName) throws ActionRequestValidationException {
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        if (metadata == null) {
            ActionRequestValidationException arve = new ActionRequestValidationException();
            arve.addValidationError(ksName+"."+cfName+" table does not exists");;
            throw arve;
        }
        return metadata;
    }
    
    
    public Map<String, GetField> flattenGetField(final String[] fieldFilter, final String path, final Object node, Map<String, GetField> flatFields) {
        if ((node instanceof List) || (node instanceof Set)) {
            for(Object o : ((Collection)node)) {
                flattenGetField(fieldFilter, path, o, flatFields);
            }
        } else if (node instanceof Map) {
            for (String key : ((Map<String,Object>)node).keySet()) {
                String fullname = (path.length() > 0) ? path + '.' + key : key;
                flattenGetField(fieldFilter, fullname, ((Map<String,Object>)node).get(key), flatFields);
            }
        } else {
            if (fieldFilter != null) {
                for (String f : fieldFilter) {
                    if (path.equals(f)) {
                        GetField gf = flatFields.get(path);
                        if (gf == null) {
                            gf = new GetField(path, ImmutableList.builder().add(node).build());
                        } else {
                            gf = new GetField(path, ImmutableList.builder().addAll(gf.getValues()).add(node).build());
                        }
                        flatFields.put(path, gf);
                        break;
                    }
                }
            }
        }
        return flatFields;
    }

    public Map<String, List<Object>> flattenTree(final Set<String> neededFiedls, final String path, final Object node, Map<String, List<Object>> flatMap) {
        if ((node instanceof List) || (node instanceof Set)) {
            for(Object o : ((Collection)node)) {
                flattenTree(neededFiedls, path, o, flatMap);
            }
        } else if (node instanceof Map) {
            for (String key : ((Map<String,Object>)node).keySet()) {
                String fullname = (path.length() > 0) ? path + '.' + key : key;
                flattenTree(neededFiedls, fullname, ((Map<String,Object>)node).get(key), flatMap);
            }
        } else {
            if ((neededFiedls == null) || (neededFiedls.contains(path))) {
                List<Object> values = (List<Object>) flatMap.get(path);
                if (values == null) {
                    values = new ArrayList<Object>();
                    flatMap.put(path, values);
                }
                values.add(node);
            }
        }
        return flatMap;
    }
    
    
    @Override
    public boolean rowExists(final MapperService mapperService, final String type, final String id) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        DocPrimaryKey docPk = parseElasticId(mapperService.index().name(), type, id);
        return process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), buildExistsQuery(mapperService.documentMapper(type), mapperService.keyspace(), typeToCfName(type), id), docPk.values).size() > 0;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#fetchRow(java.lang.String
     * , java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public UntypedResultSet fetchRow(final String ksName, final String index, final String type, final String id, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException {
        return fetchRow(ksName, index, type, id, columns, ConsistencyLevel.LOCAL_ONE, columnDefs);
    }

    /**
     * Fetch from the coordinator node.
     */
    @Override
    public UntypedResultSet fetchRow(final String ksName,final String index, final String type, final String id, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        DocPrimaryKey docPk = parseElasticId(index, type, id);
        return fetchRow(ksName, index, type, docPk, columns, cl, columnDefs);
    }
    
    /**
     * Fetch from the coordinator node.
     */
    @Override
    public UntypedResultSet fetchRow(final String ksName, final String index, final String type, final  DocPrimaryKey docPk, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        return process(cl, ClientState.forInternalCalls(), buildFetchQuery(ksName, index, type, columns, docPk.isStaticDocument, columnDefs), docPk. values);
    }
    
    public Engine.GetResult fetchSourceInternal(final String ksName, String index, String type, String id, Map<String,ColumnDefinition> columnDefs) throws IOException {
        DocPrimaryKey docPk = parseElasticId(index, type, id);
        UntypedResultSet result = fetchRowInternal(ksName, index, type, docPk, columnDefs.keySet().toArray(new String[columnDefs.size()]), columnDefs);
        if (!result.isEmpty()) {
            Map<String, Object> sourceMap = rowAsMap(index, type, result.one());
            BytesReference source = XContentFactory.contentBuilder(XContentType.JSON).map(sourceMap).bytes();
            Long timestamp = 0L;
            if (sourceMap.get(TimestampFieldMapper.NAME) != null) {
                timestamp = (Long) sourceMap.get(TimestampFieldMapper.NAME);
            }
            Long ttl = 0L;
            if (sourceMap.get(TTLFieldMapper.NAME) != null) {
                ttl = (Long) sourceMap.get(TTLFieldMapper.NAME);
            }
            Translog.Source transloSource = new Translog.Source(source, (String) sourceMap.get(RoutingFieldMapper.NAME), (String) sourceMap.get(ParentFieldMapper.NAME), timestamp, ttl);
            return new Engine.GetResult(true, 1L, transloSource);
            
        }
        return new Engine.GetResult(false, -1, null);
    }

    
    @Override
    public UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final String id, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException  {
        DocPrimaryKey docPk = parseElasticId(index, cfName, id);
        return fetchRowInternal(ksName, index, cfName, columns, docPk.values, docPk.isStaticDocument, columnDefs);
    }
    
    @Override
    public UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final  DocPrimaryKey docPk, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException  {
        return fetchRowInternal(ksName, index, cfName, columns, docPk.values, docPk.isStaticDocument, columnDefs);
    }
    
    @Override
    public UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final String[] columns, final Object[] pkColumns, boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException, IndexNotFoundException  {
        return QueryProcessor.executeInternal(buildFetchQuery(ksName, index, cfName, columns, forStaticDocument, columnDefs), pkColumns);
    }
  
    private String regularColumn(final String index, final String type) throws IOException {
        IndexService indexService = indexService(index);
        if (indexService != null) {
            DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
            if (docMapper != null) {
                for(FieldMapper fieldMapper : docMapper.mappers()) {
                    if (fieldMapper instanceof MetadataFieldMapper)
                        continue;
                    if (fieldMapper.cqlPrimaryKeyOrder() == -1 && !fieldMapper.cqlStaticColumn() && fieldMapper.cqlCollection() == Mapper.CqlCollection.SINGLETON) {
                        return fieldMapper.name();
                    }
                }
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("no regular columns for index=[{}] type=[{}]", index, type);
        return null;
    }
    
    public String buildFetchQuery(final String ksName, final String index, final String type, final String[] requiredColumns, boolean forStaticDocument, Map<String, ColumnDefinition> columnDefs) 
            throws IndexNotFoundException, IOException 
    {
        IndexService indexService = indexService(index);
        DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
        String cfName = typeToCfName(type);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        DocumentMapper.CqlFragments cqlFragment = docMapper.getCqlFragments();
        String regularColumn = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        if (requiredColumns.length == 0) {
            query.append("\"_id\"");
        } else {
            for (String c : requiredColumns) {
                switch(c){
                case TokenFieldMapper.NAME: 
                    query.append(query.length() > 7 ? ',':' ')
                        .append("token(")
                        .append(cqlFragment.ptCols)
                        .append(") as \"_token\""); 
                    break;
                case RoutingFieldMapper.NAME:
                    query.append(query.length() > 7 ? ',':' ')
                        .append( (metadata.partitionKeyColumns().size() > 1) ? "toJsonArray(" : "toString(" )
                        .append(cqlFragment.ptCols)
                        .append(") as \"_routing\"");
                    break;
                case TTLFieldMapper.NAME: 
                    if (regularColumn == null) 
                        regularColumn = regularColumn(index, cfName);
                    if (regularColumn != null)
                        query.append(query.length() > 7 ? ',':' ').append("TTL(").append(regularColumn).append(") as \"_ttl\""); 
                    break;
                case TimestampFieldMapper.NAME: 
                    if (regularColumn == null) 
                        regularColumn = regularColumn(index, cfName);
                    if (regularColumn != null)
                        query.append(query.length() > 7 ? ',':' ').append("WRITETIME(").append(regularColumn).append(") as \"_timestamp\"");
                    break;
                case ParentFieldMapper.NAME:
                    ParentFieldMapper parentMapper = docMapper.parentFieldMapper();
                    if (parentMapper.active()) {
                        query.append(query.length() > 7 ? ',':' ');
                        if  (parentMapper.pkColumns() == null) {
                            // default column name for _parent should be string.
                            query.append("\"_parent\"");
                        } else {
                            query.append( (parentMapper.pkColumns().indexOf(',') > 0) ? "toJsonArray(" : "toString(")
                                 .append(parentMapper.pkColumns())
                                 .append(") as \"_parent\"");
                        }
                    }
                    break;
                case NodeFieldMapper.NAME:
                    // nothing to add.
                    break;
                default:
                    ColumnDefinition cd = columnDefs.get(c);
                    if (cd != null && (cd.isPartitionKey() || cd.isStatic() || !forStaticDocument)) {
                       query.append(query.length() > 7 ? ',':' ').append("\"").append(c).append("\"");
                    }
                }
            }
        }
        query.append(" FROM \"").append(ksName).append("\".\"").append(cfName)
             .append("\" WHERE ").append((forStaticDocument) ? cqlFragment.ptWhere : cqlFragment.pkWhere )
             .append(" LIMIT 1");
        return query.toString();
    }
    
    public static String buildDeleteQuery(final DocumentMapper docMapper, final String ksName, final String cfName, final String id) {
        return "DELETE FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ docMapper.getCqlFragments().pkWhere;
    }
    
    public static String buildExistsQuery(final DocumentMapper docMapper, final String ksName, final String cfName, final String id) {
        return "SELECT "+docMapper.getCqlFragments().pkCols+" FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ docMapper.getCqlFragments().pkWhere;
    }

    @Override
    public void deleteRow(final String index, final String type, final String id, final ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException {
        IndexService indexService = this.indexServiceSafe(index);
        String cfName = typeToCfName(type);
        DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
        process(cl, ClientState.forInternalCalls(), buildDeleteQuery(docMapper, indexService.keyspace(), cfName, id), parseElasticId(index, type, id).values);
    }
    
    @Override
    public Map<String, Object> rowAsMap(final String index, final String type, UntypedResultSet.Row row) throws IOException {
        Map<String, Object> mapObject = new HashMap<String, Object>();
        rowAsMap(index, type, row, mapObject);
        return mapObject;
    }
    
    @Override
    public int rowAsMap(final String index, final String type, UntypedResultSet.Row row, Map<String, Object> mapObject) throws IOException {
        Object[] values = rowAsArray(index, type, row);
        int i=0;
        int j=0;
        for(ColumnSpecification colSpec: row.getColumns()) {
            if (values[i] != null && !IdFieldMapper.NAME.equals(colSpec.name.toString())) {
                mapObject.put(colSpec.name.toString(), values[i]);
                j++;
            }
            i++;
        }
        return j;
    }
    
    @Override
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row) throws IOException {
        return rowAsArray(index, type, row, true);
    }
    
    private Object value(FieldMapper fieldMapper, Object rowValue, boolean valueForSearch) {
        if (fieldMapper != null) {
            final MappedFieldType fieldType = fieldMapper.fieldType();
            return (valueForSearch) ? fieldType.valueForSearch( rowValue ) : fieldType.value( rowValue );
        } else {
            return rowValue;
        }
    }
    
    // TODO: return raw values if no mapper found.
    @Override
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException {
        final Object values[] = new Object[row.getColumns().size()];
        final IndexService indexService = indexServiceSafe(index);
        final DocumentMapper documentMapper = indexService.mapperService().documentMapper(type);
        final DocumentFieldMappers docFieldMappers = documentMapper.mappers();
        
        int i = 0;
        for (ColumnSpecification colSpec : row.getColumns()) {
            String columnName = colSpec.name.toString();
            CQL3Type cql3Type = colSpec.type.asCQL3Type();
            
            if (!row.has(columnName) || ByteBufferUtil.EMPTY_BYTE_BUFFER.equals(row.getBlob(columnName)) ) {
                values[i++] = null;
                continue;
            }

            if (cql3Type instanceof CQL3Type.Native) {
                final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                switch ((CQL3Type.Native) cql3Type) {
                case ASCII:
                case TEXT:
                case VARCHAR:
                    values[i] = row.getString(columnName);
                    if (values[i] != null && fieldMapper == null) {
                        ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        if (objectMapper != null && !objectMapper.isEnabled()) {
                            // parse text as JSON Map (not enabled object)
                            values[i] = FBUtilities.fromJsonMap(row.getString(columnName));
                        }
                    }
                    break;
                case TIMEUUID:
                case UUID:
                    values[i] = row.getUUID(columnName).toString();
                    break;
                case TIMESTAMP:
                    values[i] = value(fieldMapper, row.getTimestamp(columnName).getTime(), valueForSearch);
                    break;
                case INT:
                    values[i] = value(fieldMapper, row.getInt(columnName), valueForSearch);
                    break;
                case SMALLINT:
                    values[i] = value(fieldMapper, row.getShort(columnName), valueForSearch);
                    break;
                case TINYINT:
                    values[i] = value(fieldMapper, row.getByte(columnName), valueForSearch);
                    break;
                case BIGINT:
                    values[i] = value(fieldMapper, row.getLong(columnName), valueForSearch);
                    break;
                case DOUBLE:
                    values[i] = value(fieldMapper, row.getDouble(columnName), valueForSearch);
                    break;
                case FLOAT:
                    values[i] = value(fieldMapper, row.getFloat(columnName), valueForSearch);
                    break;
                case BLOB:
                    values[i] = value(fieldMapper, 
                            row.getBlob(columnName), 
                            valueForSearch);
                    break;
                case BOOLEAN:
                    values[i] = value(fieldMapper, row.getBoolean(columnName), valueForSearch);
                    break;
                case COUNTER:
                    logger.warn("Ignoring unsupported counter {} for column {}", cql3Type, columnName);
                    break;
                case INET:
                    values[i] = value(fieldMapper, NetworkAddress.format(row.getInetAddress(columnName)), valueForSearch);
                    break;
                default:
                    logger.error("Ignoring unsupported type {} for column {}", cql3Type, columnName);
                }
            } else if (cql3Type.isCollection()) {
                AbstractType<?> elementType;
                switch (((CollectionType<?>) colSpec.type).kind) {
                case LIST:
                    List list;
                    elementType = ((ListType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        final List<ByteBuffer> lbb = row.getList(columnName, BytesType.instance);
                        list = new ArrayList(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            list.add(deserialize(elementType, bb, objectMapper));
                        }
                    } else {
                        final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                        final List list2 = row.getList(colSpec.name.toString(), elementType);
                        list = new ArrayList(list2.size());
                        for(Object v : list2) {
                            list.add(value(fieldMapper, v, valueForSearch));
                        }
                    }
                    values[i] =  (list.size() == 1) ? list.get(0) : list;
                    break;
                case SET :
                    Set set;
                    elementType = ((SetType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        final Set<ByteBuffer> lbb = row.getSet(columnName, BytesType.instance);
                        set = new HashSet(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            set.add(deserialize(elementType, bb, objectMapper));
                        }
                    } else {
                        final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                        final Set set2 = row.getSet(columnName, elementType);
                        set = new HashSet(set2.size());
                        for(Object v : set2) {
                            set.add( value(fieldMapper, v, valueForSearch) );
                        }
                    }
                    values[i] =  (set.size() == 1) ? set.iterator().next() : set;
                    break;
                case MAP :
                    Map map;
                    if (((MapType<?,?>) colSpec.type).getKeysType().asCQL3Type() != CQL3Type.Native.TEXT) {
                        throw new IOException("Only support map<text,?>, bad type for column "+columnName);
                    }
                    UTF8Type keyType = (UTF8Type) ((MapType<?,?>) colSpec.type).getKeysType();
                    elementType = ((MapType<?,?>) colSpec.type).getValuesType();
                    final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                    if (elementType instanceof UserType) {
                        final Map<String, ByteBuffer> lbb = row.getMap(columnName, keyType, BytesType.instance);
                        map = new HashMap<String , Map<String, Object>>(lbb.size());
                        for(String key : lbb.keySet()) {
                            map.put(key, deserialize(elementType, lbb.get(key), objectMapper.getMapper(key)));
                        }
                    } else {
                        Map<String,Object> map2 = (Map<String,Object>) row.getMap(columnName, keyType, elementType);
                        map = new HashMap<String, Object>(map2.size());
                        for(String key : map2.keySet()) {
                            FieldMapper subMapper = (FieldMapper)objectMapper.getMapper(key);
                            map.put(key,  value(subMapper, map2.get(key), valueForSearch) );
                        }
                    }
                    values[i] =  map;
                    break;
                }
            } else if (colSpec.type instanceof UserType) {
                ByteBuffer bb = row.getBytes(columnName);
                values[i] = deserialize(colSpec.type, bb, documentMapper.objectMappers().get(columnName));
            } else if (cql3Type instanceof CQL3Type.Custom) {
                logger.warn("CQL3.Custum type not supported for column "+columnName);
            }
            i++;
        }
        return values;
    }

    

    public static class BlockingActionListener implements ActionListener<ClusterStateUpdateResponse> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Throwable error = null;
        
        public void waitForUpdate(TimeValue timeValue) throws Exception {
            if (timeValue != null && timeValue.millis() > 0) {
                if (!latch.await(timeValue.millis(), TimeUnit.MILLISECONDS)) {
                    throw new ElasticsearchTimeoutException("blocking update timeout");
                }
            } else {
                latch.await();
            }
            if (error != null)
                throw new RuntimeException(error);
        }

        @Override
        public void onResponse(ClusterStateUpdateResponse response) {
            latch.countDown();
        }

        @Override
        public void onFailure(Throwable e) {
            error = e;
            latch.countDown();
        }
    }

    
    @Override
    public void blockingMappingUpdate(IndexService indexService, String type, String source) throws Exception {
        BlockingActionListener mappingUpdateListener = new BlockingActionListener();
        MetaDataMappingService metaDataMappingService = ElassandraDaemon.injector().getInstance(MetaDataMappingService.class);
        PutMappingClusterStateUpdateRequest putRequest = new PutMappingClusterStateUpdateRequest().indices(new String[] { indexService.index().name() }).type(type).source(source);
        metaDataMappingService.putMapping(putRequest, mappingUpdateListener);
        mappingUpdateListener.waitForUpdate(mappingUpdateTimeout);
    }
    
    @Override
    public void updateDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception {
        upsertDocument(indicesService, request, indexMetaData, true);
    }
    
    @Override
    public void insertDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception {
        upsertDocument(indicesService, request, indexMetaData, false);
    }
    
    public void upsertDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData, boolean updateOperation) throws Exception {
        final IndexService indexService = indicesService.indexServiceSafe(request.index());
        final IndexShard indexShard = indexService.shardSafe(0);
        
        final SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, request.source()).type(request.type()).id(request.id());
        if (request.routing() != null)
            sourceToParse.routing(request.routing());
        if (request.parent() != null)
            sourceToParse.parent(request.parent());
        if (request.timestamp() != null)
            sourceToParse.timestamp(request.timestamp());
        if (request.ttl() != null)
            sourceToParse.ttl(request.ttl());

        final String keyspaceName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE, request.index());
        final String cfName = typeToCfName(request.type());

        final Engine.IndexingOperation operation = indexShard.prepareIndexOnPrimary(sourceToParse, request.version(), request.versionType(), request.canHaveDuplicates());
        final Mapping update = operation.parsedDoc().dynamicMappingsUpdate();
        final boolean dynamicMappingEnable = indexService.indexSettings().getAsBoolean("index.mapper.dynamic", true);
        if (update != null && dynamicMappingEnable) {
            if (logger.isDebugEnabled()) 
                logger.debug("Document source={} require a blocking mapping update of [{}]", request.sourceAsMap(), indexService.index().name());
            // blocking Elasticsearch mapping update (required to update cassandra schema before inserting a row, this is the cost of dynamic mapping)
            blockingMappingUpdate(indexService, request.type(), update.toString());
        }

        // get the docMapper after a potential mapping update
        final DocumentMapper docMapper = indexShard.mapperService().documentMapperWithAutoCreate(request.type()).getDocumentMapper();
        
        // insert document into cassandra keyspace=index, table = type
        final Map<String, Object> sourceMap = request.sourceAsMap();
        final Map<String, ObjectMapper> objectMappers = docMapper.objectMappers();
        final DocumentFieldMappers fieldMappers = docMapper.mappers();

        Long timestamp = null;
        if (docMapper.timestampFieldMapper().enabled() && request.timestamp() != null) {
            timestamp = docMapper.timestampFieldMapper().fieldType().value(request.timestamp());
        }
        
        if (logger.isTraceEnabled()) 
            logger.trace("Insert metadata.version={} index=[{}] table=[{}] id=[{}] source={} consistency={} ttl={}",
                state().metaData().version(),
                indexService.index().name(), cfName, request.id(), sourceMap, 
                request.consistencyLevel().toCassandraConsistencyLevel(), request.ttl());

        final CFMetaData metadata = getCFMetaData(keyspaceName, cfName);
        
        String id = request.id();
        Map<String, ByteBuffer> map = new HashMap<String, ByteBuffer>();
        if (request.parent() != null) 
            sourceMap.put(ParentFieldMapper.NAME, request.parent());
        
        // normalize the _id and may find some column value in _id.
        // if the provided columns does not contains all the primary key columns, parse the _id to populate the columns in map.
        parseElasticId(request.index(), cfName, request.id(), sourceMap);
        
        // workaround because ParentFieldMapper.value() and UidFieldMapper.value() create an Uid.
        if (sourceMap.get(ParentFieldMapper.NAME) != null && ((String)sourceMap.get(ParentFieldMapper.NAME)).indexOf(Uid.DELIMITER) < 0) {
            sourceMap.put(ParentFieldMapper.NAME, request.type() + Uid.DELIMITER + sourceMap.get(ParentFieldMapper.NAME));
        } 
       
        if (docMapper.sourceMapper().enabled()) {
            sourceMap.put(SourceFieldMapper.NAME, request.source());
        }
        
        for (String field : sourceMap.keySet()) {
            FieldMapper fieldMapper = fieldMappers.getMapper(field);
            Mapper mapper = (fieldMapper != null) ? fieldMapper : objectMappers.get(field);
            ByteBuffer colName;
            if (mapper == null) {
                if (dynamicMappingEnable)
                    throw new MapperException("Unmapped field ["+field+"]");
                colName = ByteBufferUtil.bytes(field);
            } else {
                colName = mapper.cqlName();    // cached ByteBuffer column name.
            }
            final ColumnDefinition cd = metadata.getColumnDefinition(colName);
            if (cd != null) {
                // we got a CQL column.
                Object fieldValue = sourceMap.get(field);
                try {
                    if (fieldValue == null) {
                        if (cd.type.isCollection()) {
                            switch (((CollectionType<?>)cd.type).kind) {
                            case LIST :
                            case SET : 
                                map.put(field, CollectionSerializer.pack(Collections.emptyList(), 0, Server.VERSION_3)); 
                                break;
                            case MAP :
                                break;
                            }
                        } else {
                            map.put(field, null); 
                        }
                        continue;
                    }
                    
                    if (mapper != null && mapper.cqlCollection().equals(CqlCollection.SINGLETON) && (fieldValue instanceof Collection)) {
                        throw new MapperParsingException("field " + fieldMapper.name() + " should be a single value");
                    }
                    
                    // hack to store percolate query as a string while mapper is an object mapper.
                    if (metadata.cfName.equals("_percolator") && field.equals("query")) {
                        if (cd.type.isCollection()) {
                            switch (((CollectionType<?>)cd.type).kind) {
                            case LIST :
                                if ( ((ListType)cd.type).getElementsType().asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                    // opaque list of objects serialized to JSON text 
                                    fieldValue = Collections.singletonList( stringify(fieldValue) );
                                }
                                break;
                            case SET :
                                if ( ((SetType)cd.type).getElementsType().asCQL3Type().equals(CQL3Type.Native.TEXT) &&  !(fieldValue instanceof String)) {
                                    // opaque set of objects serialized to JSON text 
                                    fieldValue = Collections.singleton( stringify(fieldValue) );
                                }
                                break;
                            }
                        } else {
                            if (cd.type.asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                // opaque singleton object serialized to JSON text 
                                fieldValue = stringify(fieldValue);
                            }
                        }
                    }
                    
                    
                    map.put(field, serialize(request.index(), cfName, cd.type, field, fieldValue, mapper));
                } catch (Exception e) {
                    logger.error("[{}].[{}] failed to parse field {}={}", e, request.index(), cfName, field, fieldValue );
                    throw e;
                }
            }
        }
        
        String query;
        ByteBuffer[] values;
        if (request.autoGeneratedId() || request.opType() == OpType.CREATE) {
            boolean checkUniqueId = Booleans.parseBoolean(request.checkUniqueId(), (request.autoGeneratedId()) ? false : true);
            values = new ByteBuffer[map.size()];
            query = buildInsertQuery(keyspaceName, cfName, map, id, 
                    checkUniqueId,                
                    (request.ttl() != null) ? request.ttl().getSeconds() : null, // ttl
                    timestamp,
                    values, 0);
            final boolean applied = processConditional(request.consistencyLevel().toCassandraConsistencyLevel(), (checkUniqueId) ? ConsistencyLevel.LOCAL_SERIAL : null, query, values);
            if (!applied) 
                throw new DocumentAlreadyExistsException(indexShard.shardId(), cfName, request.id());
        } else {
            // set empty top-level fields to null to overwrite existing columns.
            for(FieldMapper m : fieldMappers) {
                String fullname = m.name();
                if (map.get(fullname) == null && !fullname.startsWith("_") && fullname.indexOf('.') == -1 && metadata.getColumnDefinition(m.cqlName()) != null) 
                    map.put(fullname, null);
            }
            for(String m : objectMappers.keySet()) {
                if (map.get(m) == null && m.indexOf('.') == -1 && metadata.getColumnDefinition(objectMappers.get(m).cqlName()) != null)
                    map.put(m, null);
            }
            values = new ByteBuffer[map.size()];
            query = buildInsertQuery(keyspaceName, cfName, map, id, 
                    false,      
                    (request.ttl() != null) ? request.ttl().getSeconds() : null,
                    timestamp,
                    values, 0);
            process(request.consistencyLevel().toCassandraConsistencyLevel(), ClientState.forInternalCalls(), query, values);
        }
    }

    public String buildInsertQuery(final String ksName, final String cfName, Map<String, ByteBuffer> map, String id, final boolean ifNotExists, final Long ttl, 
            final Long writetime, ByteBuffer[] values, int valuesOffset) throws Exception {
        final StringBuilder questionsMarks = new StringBuilder();
        final StringBuilder columnNames = new StringBuilder();
        
        int i=0;
        for (Entry<String,ByteBuffer> entry : map.entrySet()) {
            if (entry.getKey().equals(TokenFieldMapper.NAME)) 
                continue;
            
            if (columnNames.length() > 0) {
                columnNames.append(',');
                questionsMarks.append(',');
            }
            columnNames.append("\"").append(entry.getKey()).append("\"");
            questionsMarks.append('?');
            values[valuesOffset + i] = entry.getValue();
            i++;
        }
        
        final StringBuilder query = new StringBuilder();
        query.append("INSERT INTO \"").append(ksName).append("\".\"").append(cfName)
             .append("\" (").append(columnNames.toString()).append(") VALUES (").append(questionsMarks.toString()).append(") ");
        if (ifNotExists) query.append("IF NOT EXISTS ");
        if (ttl != null && ttl > 0 || writetime != null) query.append("USING ");
        if (ttl != null && ttl > 0) query.append("TTL ").append(Long.toString(ttl));
        if (ttl != null &&ttl > 0 && writetime != null) query.append(" AND ");
        if (writetime != null) query.append("TIMESTAMP ").append(Long.toString(writetime*1000));
        
        return query.toString();
    }
    
    @Override
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        BytesReference sourceToBeReturned;
        if (docMapper.sourceMapper().enabled()) {
            // retreive from _source columns stored as blob in cassandra
            ByteBuffer bb = (ByteBuffer) sourceAsMap.get(SourceFieldMapper.NAME);
            sourceToBeReturned = new BytesArray(bb.array(), bb.position(), bb.limit() - bb.position());
        } else {
            // rebuild _source from all cassandra columns.
            XContentBuilder builder = buildDocument(docMapper, sourceAsMap, true, isStaticDocument(index, uid));
            builder.humanReadable(true);
            sourceToBeReturned = builder.bytes();
        }
        return sourceToBeReturned;
    }
    
    @Override
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, String type, String id) throws JsonParseException, JsonMappingException, IOException {
        return source( docMapper, sourceAsMap, index, new Uid(type, id));
    }

    
    public DocPrimaryKey parseElasticId(final String index, final String type, final String id) throws IOException {
        return parseElasticId(index, type, id, null);
    }
    
    /**
     * Parse elastic _id (a value or a JSON array) to build a DocPrimaryKey or populate map.
     * @param ksName
     * @param cfName
     * @param map
     * @param id
     */
    public DocPrimaryKey parseElasticId(final String index, final String type, final String id, Map<String, Object> map) throws JsonParseException, JsonMappingException, IOException {
        IndexService indexService = indexServiceSafe(index);
        String ksName = indexService.keyspace();
        String cfName = typeToCfName(type);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        List<ColumnDefinition> clusteringColumns = metadata.clusteringColumns();
        int ptLen = partitionColumns.size();
        
        if (id.startsWith("[") && id.endsWith("]")) {
            // _id is JSON array of values.
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(id, Object[].class);
            Object[] values = (map != null) ? null : new Object[elements.length];
            String[] names = (map != null) ? null : new String[elements.length];
            if (elements.length > ptLen + clusteringColumns.size()) 
                throw new JsonMappingException("_id="+id+" longer than the primary key size="+(ptLen+clusteringColumns.size()) );
            
            for(int i=0; i < elements.length; i++) {
                ColumnDefinition cd = (i < ptLen) ? partitionColumns.get(i) : clusteringColumns.get(i - ptLen);
                AbstractType<?> atype = cd.type;
                if (map == null) {
                    names[i] = cd.name.toString();
                    values[i] = atype.compose( atype.fromString(elements[i].toString()) );
                } else {
                    map.put(cd.name.toString(), atype.compose( atype.fromString(elements[i].toString()) ) );
                }
            }
            return (map != null) ? null : new DocPrimaryKey(names, values, (clusteringColumns.size() > 0 && elements.length == partitionColumns.size()) ) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> atype = partitionColumns.get(0).type;
            if (map == null) {
                return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { atype.compose( atype.fromString(id) ) }, clusteringColumns.size() != 0);
            } else {
                map.put(partitionColumns.get(0).name.toString(), atype.compose( atype.fromString(id) ) );
                return null;
            }
        }
    }

    public DocPrimaryKey parseElasticRouting(final String index, final String type, final String routing) throws JsonParseException, JsonMappingException, IOException {
        IndexService indexService = indexServiceSafe(index);
        String ksName = indexService.settingsService().getSettings().get(IndexMetaData.SETTING_KEYSPACE,index);
        String cfName = typeToCfName(type);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        int ptLen = partitionColumns.size();
        if (routing.startsWith("[") && routing.endsWith("]")) {
            // _routing is JSON array of values.
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(routing, Object[].class);
            Object[] values = new Object[elements.length];
            String[] names = new String[elements.length];
            if (elements.length != ptLen) 
                throw new JsonMappingException("_routing="+routing+" does not match the partition key size="+ptLen);
            
            for(int i=0; i < elements.length; i++) {
                ColumnDefinition cd = partitionColumns.get(i);
                AbstractType<?> atype = cd.type;
                names[i] = cd.name.toString();
                values[i] = atype.compose( atype.fromString(elements[i].toString()) );
                i++;
            }
            return new DocPrimaryKey(names, values) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> atype = partitionColumns.get(0).type;
            return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { atype.compose( atype.fromString(routing) ) });
        }
    }
    
    @Override
    public boolean isStaticDocument(final String index, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        IndexService indexService = indexServiceSafe(index);
        CFMetaData metadata = getCFMetaData(indexService.keyspace(), typeToCfName(uid.type()));
        String id = uid.id();
        if (id.startsWith("[") && id.endsWith("]")) {
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(id, Object[].class);
            return metadata.clusteringColumns().size() > 0 && elements.length == metadata.partitionKeyColumns().size();
        } else {
            return metadata.clusteringColumns().size() != 0;
        }
    }
    
    public boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges, Collection<Range<Token>> requestTokenRange) {
        for(Range<Token> shardRange : shardTokenRanges) {
            if (shardRange.intersects(requestTokenRange)) 
                return true;
        }
        return false;
    }
    
    public boolean tokenRangesContains(Collection<Range<Token>> shardTokenRanges, Token token) {
        for(Range<Token> shardRange : shardTokenRanges) {
            if (shardRange.contains(token)) 
                return true;
        }
        return false;
    }

    public static ConsistencyLevel consistencyLevelFromString(String value) {
        switch(value.toUpperCase(Locale.ROOT)) {
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
    
    
    @Override
    public boolean isDatacenterGroupMember(InetAddress endpoint) {
        String endpointDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint);
        KeyspaceMetadata  elasticAdminMetadata = Schema.instance.getKSMetaData(this.elasticAdminKeyspaceName);
        if (elasticAdminMetadata != null) {
            ReplicationParams replicationParams = elasticAdminMetadata.params.replication;
            if (replicationParams.klass == NetworkTopologyStrategy.class && replicationParams.options.get(endpointDc) != null) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException {
        CFMetaData cfm = getCFMetaData(elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        cfm.comment(metaDataString);
        MigrationManager.announceColumnFamilyUpdate(cfm, false);
    }

    /**
     * Should only be used after a SCHEMA change.
     * @throws IOException 
     */
    @Override
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        try {
            String query = String.format(Locale.ROOT, "SELECT comment FROM system_schema.tables WHERE keyspace_name='%s' AND table_name='%s'", 
                this.elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
            UntypedResultSet result = QueryProcessor.executeInternal(query);
            if (result.isEmpty())
                throw new NoPersistedMetaDataException("Failed to read comment from "+elasticAdminKeyspaceName+"+"+ELASTIC_ADMIN_METADATA_TABLE);

            String metadataString = result.one().getString("comment");
            logger.debug("Recover metadata from {}.{} = {}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE, metadataString);
            return parseMetaDataString( metadataString );
        } catch (RequestValidationException | RequestExecutionException e) {
            throw new NoPersistedMetaDataException("Failed to read comment from "+elasticAdminKeyspaceName+"+"+ELASTIC_ADMIN_METADATA_TABLE, e);
        }
    }
    
    private MetaData parseMetaDataString(String metadataString) throws NoPersistedMetaDataException {
        if (metadataString != null && metadataString.length() > 0) {
            MetaData metaData;
            try {
                XContentParser xparser = new JsonXContentParser(new JsonFactory().createParser(metadataString));
                metaData = MetaData.Builder.fromXContent(xparser);
            } catch (IOException e) {
                logger.error("Failed to parse metadata={}", e, metadataString);
                throw new NoPersistedMetaDataException("Failed to parse metadata="+metadataString, e);
            }
            return metaData;
        }
        throw new NoPersistedMetaDataException("metadata null or empty");
    }

    /**
     * Try to read fresher metadata from cassandra.
     * @param version extected version
     * @return
     */
    @Override
    public MetaData checkForNewMetaData(Long expectedVersion) throws NoPersistedMetaDataException {
        MetaData localMetaData = readMetaDataAsRow(ConsistencyLevel.ONE);
        if (localMetaData != null && localMetaData.version() >= expectedVersion) {
           return localMetaData;
        }
        if (localMetaData == null) {
            throw new NoPersistedMetaDataException("No cluster metadata in "+this.elasticAdminKeyspaceName);
        }
        MetaData quorumMetaData = readMetaDataAsRow(this.metadataReadCL);
        if (quorumMetaData.version() >= expectedVersion) {
            return quorumMetaData;
        }
        return null;
    }
    
    @Override
    public MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException {
        UntypedResultSet result;
        try {
            result = process(cl, ClientState.forInternalCalls(), selectMetadataQuery, DatabaseDescriptor.getClusterName());
            Row row = result.one();
            if (row != null && row.has("metadata")) {
                return parseMetaDataString(row.getString("metadata"));
            }
        } catch (UnavailableException e) {
            logger.warn("Cannot read metadata with consistency="+cl,e);
            return null;
        } catch (Exception e) {
            throw new NoPersistedMetaDataException("Unexpected error",e);
        }
        throw new NoPersistedMetaDataException("Unexpected error");
    }
    

    private int getLocalDataCenterSize() {
        int count = 1; 
        for (UntypedResultSet.Row row : executeInternal("SELECT data_center from system." + SystemKeyspace.PEERS))
            if (DatabaseDescriptor.getLocalDataCenter().equals(row.getString("data_center")))
                count++;
        logger.info(" datacenter=[{}] size={} from peers", DatabaseDescriptor.getLocalDataCenter(), count);
        return count;
    }
    
    /**
     * Create or update elastic_admin keyspace.
     * @throws IOException 
     */
    @Override
    public void createOrUpdateElasticAdminKeyspace()  {
        UntypedResultSet result = QueryProcessor.executeOnceInternal(String.format(Locale.ROOT, "SELECT replication FROM system_schema.keyspaces WHERE keyspace_name='%s'", elasticAdminKeyspaceName)); 
        logger.info("elasticAdminMetadata exist={}", !result.isEmpty());
        if (result.isEmpty()) {
            MetaData metadata = state().metaData();
            try {
                String metaDataString = MetaData.Builder.toXContent(metadata);
                
                Map<String,String> replication = new HashMap<String,String>();
                replication.put("class", NetworkTopologyStrategy.class.getName());
                replication.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(getLocalDataCenterSize()));
                
                String createKeyspace = String.format(Locale.ROOT, "CREATE KEYSPACE IF NOT EXISTS \"%s\" WITH replication = %s;", 
                        elasticAdminKeyspaceName, FBUtilities.json(replication).replaceAll("\"", "'"));
                logger.info(createKeyspace);
                process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), createKeyspace);
                
                String createTable =  String.format(Locale.ROOT, "CREATE TABLE IF NOT EXISTS \"%s\".%s ( cluster_name text PRIMARY KEY, owner uuid, version bigint, metadata text) WITH comment='%s';", 
                        elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE, MetaData.Builder.toXContent(metadata));
                logger.info(createTable);
                process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), createTable);
                
                // initialize a first row if needed
                process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), insertMetadataQuery,
                        DatabaseDescriptor.getClusterName(), UUID.fromString(StorageService.instance.getLocalHostId()), metadata.version(), metaDataString);
                logger.info("Succefully initialize {}.{} = {}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE,metaDataString);
                writeMetaDataAsComment(metaDataString);
            } catch (Throwable e) {
                logger.error("Failed to initialize table {}.{}",e, elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
            }
        } else {
            Map<String,String> replication = result.one().getFrozenTextMap("replication");
            logger.debug("elasticAdminMetadata replication={}", replication);
            
            if (!NetworkTopologyStrategy.class.getName().equals(replication.get("class")))
                    throw new ConfigurationException("Keyspace ["+this.elasticAdminKeyspaceName+"] should use "+NetworkTopologyStrategy.class.getName()+" replication strategy");
            
            int currentRF = -1;
            if (replication.get(DatabaseDescriptor.getLocalDataCenter()) != null) {
                currentRF = Integer.valueOf(replication.get(DatabaseDescriptor.getLocalDataCenter()).toString());
            }
            int targetRF = getLocalDataCenterSize();
            if (targetRF != currentRF) {
                replication.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(targetRF));
                try {
                    String query = String.format(Locale.ROOT, "ALTER KEYSPACE \"%s\" WITH replication = %s", 
                            elasticAdminKeyspaceName, FBUtilities.json(replication).replaceAll("\"", "'"));
                    logger.info(query);
                    process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), query);
                } catch (Throwable e) {
                    logger.error("Failed to alter keyspace [{}]", e, this.elasticAdminKeyspaceName);
                    throw e;
                }
            } else {
                logger.info("Keep unchanged keyspace={} datacenter={} RF={}", elasticAdminKeyspaceName, DatabaseDescriptor.getLocalDataCenter(), targetRF);
            }
        }
    }
    
    /*
    private boolean checkConsistency(String ksName, ConsistencyLevel cl) {
        Keyspace adminKeypsace = Schema.instance.getKeyspaceInstance(ksName);
        DatacenterReplicationStrategy replicationStrategy = (DatacenterReplicationStrategy)adminKeypsace.getReplicationStrategy();
        Collection<InetAddress> aliveNodes = replicationStrategy.getAliveEndpoints();
        if (!cl.isSufficientLiveNodes(adminKeypsace, aliveNodes)) {
            logger.warn("Only {}/{} live nodes on keyspace [{}], cannot succeed transaction with CL={}", 
                    aliveNodes.size(), replicationStrategy.getEndpoints().size(),  ksName, metadataWriteCL);
            return false;
        }
        return true;
    }
    */
    
    @Override
    public ShardInfo shardInfo(String index, ConsistencyLevel cl) {
        Keyspace keyspace = Schema.instance.getKeyspaceInstance(state().metaData().index(index).keyspace());
        AbstractReplicationStrategy replicationStrategy = keyspace.getReplicationStrategy();
        int rf = replicationStrategy.getReplicationFactor();
        if (replicationStrategy instanceof NetworkTopologyStrategy)
            rf = ((NetworkTopologyStrategy)replicationStrategy).getReplicationFactor(DatabaseDescriptor.getLocalDataCenter());
        return new ShardInfo(rf, cl.blockFor(keyspace));
    }
    
    @Override
    public void persistMetaData(MetaData oldMetaData, MetaData newMetaData, String source) throws IOException, InvalidRequestException, RequestExecutionException, RequestValidationException {
        if (!newMetaData.uuid().equals(localNode().id())) {
            logger.error("should not push metadata updated from another node {}/{}", newMetaData.uuid(), newMetaData.version());
            return;
        }
        if (newMetaData.uuid().equals(state().metaData().uuid()) && newMetaData.version() < state().metaData().version()) {
            logger.warn("don't push obsolete metadata uuid={} version {} < {}", newMetaData.uuid(), newMetaData.version(), state().metaData().version());
            return;
        }

        String metaDataString = MetaData.Builder.toXContent(newMetaData);
        UUID owner = UUID.fromString(localNode().id());
        boolean applied = processConditional(
                this.metadataWriteCL,
                this.metadataSerialCL,
                updateMetaDataQuery,
                new Object[] { owner, newMetaData.version(), metaDataString, DatabaseDescriptor.getClusterName(), newMetaData.version() });
        if (applied) {
            logger.debug("PAXOS Succefully update metadata source={} newMetaData={} in cluster {}", source, metaDataString, DatabaseDescriptor.getClusterName());
            writeMetaDataAsComment(metaDataString);
            return;
        } else {
            logger.warn("PAXOS Failed to update metadata oldMetadata={}/{} currentMetaData={}/{} in cluster {}", 
                    oldMetaData.uuid(), oldMetaData.version(), localNode().id(), newMetaData.version(), DatabaseDescriptor.getClusterName());
            throw new ConcurrentMetaDataUpdateException(owner, newMetaData.version());
        }
    }

    
    
    /**
     * Return a set of started shards according t the gossip state map and the local shard state.
     * @param index
     * @return a set of started shards.
     */
    public Map<UUID, ShardRoutingState> getShardRoutingStates(String index) {
        Map<UUID, ShardRoutingState> shards = this.discoveryService.getShardRoutingStates(index);
        try {
            IndexShard localIndexShard = indexServiceSafe(index).shard(0);
            if (localIndexShard != null && localIndexShard.routingEntry() != null)
                shards.put(this.localNode().uuid(), localIndexShard.routingEntry().state());
        } catch (IndexNotFoundException e) {
        }
        return shards;
    }
    
    
    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void putShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        this.discoveryService.putShardRoutingState(index, shardRoutingState);
    }
    
    public void publishGossipStates() {
        this.discoveryService.publishX2(state());
        this.discoveryService.publishX1(state());
    }
    
    private static Object value(FieldMapper mapper, Object value) throws IOException {
        if (mapper instanceof DateFieldMapper) {
            // workaround because elasticsearch manage Date as Long
            DateFieldMapper.DateFieldType dateFiledType = ((DateFieldMapper) mapper).fieldType();
            return new Date( dateFiledType.parseStringValue( value.toString() ) );
        } else if (mapper instanceof IpFieldMapper) {
            // workaround because elasticsearch manage InetAddress as Long
            Long ip = (Long) ((IpFieldMapper) mapper).fieldType().value(value);
            return  com.google.common.net.InetAddresses.forString(IpFieldMapper.longToIp(ip));
        } else if (mapper instanceof GeoShapeFieldMapper) {
            return XContentFactory.jsonBuilder().map((Map)value).string();
        } else if (mapper instanceof BinaryFieldMapper) {
            BytesReference br = (BytesReference) ((BinaryFieldMapper)mapper).fieldType().value(value);
            return ByteBuffer.wrap(br.array(), br.arrayOffset(), br.length());
        } else if (mapper instanceof SourceFieldMapper) {
            byte[] bytes = (byte[]) ((SourceFieldMapper)mapper).fieldType().value(value);
            return ByteBuffer.wrap(bytes, 0, bytes.length);
        } else if (value instanceof UUID) {
            // workaround for #74
            return value;
        } else {
            Object v = mapper.fieldType().value(value);
            if (v instanceof Uid) {
                // workaround because ParentFieldMapper.value() and UidFieldMapper.value() return an Uid (not a string).
                return ((Uid)v).id();
            } else {
                return v;
            }
        }
    }
    
    public static int defaultPrecisionStep = 8;
    public static Query newTokenRangeQuery(Collection<Range<Token>> tokenRanges) {
        Query tokenRangeQuery = null;
        if (tokenRanges != null) {
            switch(tokenRanges.size()) {
                case 0:
                    break;
                case 1:
                    Range<Token> unique_range = tokenRanges.iterator().next();
                    if (unique_range.left.equals(AbstractSearchStrategy.TOKEN_MIN) && unique_range.right.equals(AbstractSearchStrategy.TOKEN_MAX))
                        // full search range, so don't add any filter.
                        break;
                    
                    NumericRangeQuery<Long> nrq2 = NumericRangeQuery.newLongRange(TokenFieldMapper.NAME, defaultPrecisionStep, (Long) unique_range.left.getTokenValue(), (Long) unique_range.right.getTokenValue(), false, true);
                    tokenRangeQuery = nrq2;
                    break;
                default:
                    BooleanQuery.Builder bq2 = new BooleanQuery.Builder();
                    for (Range<Token> range : tokenRanges) {
                        // TODO: check the best precisionStep (6 by default), see https://lucene.apache.org/core/5_2_1/core/org/apache/lucene/search/NumericRangeQuery.html
                        NumericRangeQuery<Long> nrq = NumericRangeQuery.newLongRange(TokenFieldMapper.NAME, defaultPrecisionStep, (Long) range.left.getTokenValue(), (Long) range.right.getTokenValue(), false, true);
                        bq2.add(nrq, Occur.SHOULD);
                    }
                    tokenRangeQuery = bq2.build();
                    break;
            }
        }
        return tokenRangeQuery;
    }
    
    public static Collection flattenCollection(Collection c) {
        List l = new ArrayList(c.size());
        for(Object o : c) {
            if (o instanceof Collection) {
                l.addAll( flattenCollection((Collection)o) );
            } else {
                l.add(o);
            }
        }
        return l;
    }


    /**
     * Serialize a cassandra typed object.
     * List of list converted to List, see https://www.elastic.co/guide/en/elasticsearch/reference/current/array.html
     * @param ksName
     * @param cfName
     * @param type
     * @param name
     * @param value
     * @param mapper
     * @return
     * @throws SyntaxException
     * @throws ConfigurationException
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    public static ByteBuffer serialize(final String ksName, final String cfName, final AbstractType type, final String name, final Object value, final Mapper mapper) 
            throws SyntaxException, ConfigurationException, JsonGenerationException, JsonMappingException, IOException {
        if (value == null) {
            return null;
        }
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            ByteBuffer[] components = new ByteBuffer[udt.size()];
            int i=0;
            
            if (GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                GeoPoint geoPoint = new GeoPoint();
                if (value instanceof String) {
                    // parse from string lat,lon (ex: "41.12,-71.34") or geohash (ex:"drm3btev3e86")
                    geoPoint.resetFromString((String)value);
                } else {
                    // parse from lat, lon fields as map
                    Map<String, Object> mapValue = (Map<String, Object>) value;
                    geoPoint.reset((Double)mapValue.get(GeoPointFieldMapper.Names.LAT), (Double)mapValue.get(GeoPointFieldMapper.Names.LON));
                }
                components[i++]=serialize(ksName, cfName, udt.fieldType(0), GeoPointFieldMapper.Names.LAT, geoPoint.lat(), null);
                components[i++]=serialize(ksName, cfName, udt.fieldType(1), GeoPointFieldMapper.Names.LON, geoPoint.lon(), null);
            } else if (COMPLETION_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                // input list<text>, output text, weight int, payload text
                Map<String, Object> mapValue = (Map<String, Object>) value;
                components[i++]=(mapValue.get(Fields.CONTENT_FIELD_NAME_INPUT) == null) ? null : serialize(ksName, cfName, udt.fieldType(0), Fields.CONTENT_FIELD_NAME_INPUT, mapValue.get(Fields.CONTENT_FIELD_NAME_INPUT), null);
                components[i++]=(mapValue.get(Fields.CONTENT_FIELD_NAME_OUTPUT) == null) ? null : serialize(ksName, cfName, udt.fieldType(1), Fields.CONTENT_FIELD_NAME_OUTPUT, mapValue.get(Fields.CONTENT_FIELD_NAME_OUTPUT), null);
                components[i++]=(mapValue.get(Fields.CONTENT_FIELD_NAME_WEIGHT) == null) ? null : serialize(ksName, cfName, udt.fieldType(2), Fields.CONTENT_FIELD_NAME_WEIGHT, new Long((Integer) mapValue.get(Fields.CONTENT_FIELD_NAME_WEIGHT)), null);
                components[i++]=(mapValue.get(Fields.CONTENT_FIELD_NAME_PAYLOAD) == null) ? null : serialize(ksName, cfName, udt.fieldType(3), Fields.CONTENT_FIELD_NAME_PAYLOAD, stringify(mapValue.get(Fields.CONTENT_FIELD_NAME_PAYLOAD)), null);
            } else {
                Map<String, Object> mapValue = (Map<String, Object>) value;
                for (int j = 0; j < udt.size(); j++) {
                    String subName = UTF8Type.instance.compose(udt.fieldName(j));
                    AbstractType<?> subType = udt.fieldType(j);
                    Object subValue = mapValue.get(subName);
                    Mapper subMapper = (mapper instanceof ObjectMapper) ? ((ObjectMapper) mapper).getMapper(subName) : null;
                    components[i++]=serialize(ksName, cfName, subType, subName, subValue, subMapper);
                }
            }
            return TupleType.buildValue(components);
        } else if (type instanceof MapType) {
            MapType mapType = InternalCassandraClusterService.getMapType(ksName, cfName, name);
            MapSerializer serializer = mapType.getSerializer();
            Map map = (Map)value;
            List<ByteBuffer> buffers = serializer.serializeValues((Map)value);
            return CollectionSerializer.pack(buffers, map.size(), Server.VERSION_3);
        } else if (type instanceof CollectionType) {
            AbstractType elementType = (type instanceof ListType) ? ((ListType)type).getElementsType() : ((SetType)type).getElementsType();
            
            if (elementType instanceof UserType && InternalCassandraClusterService.GEO_POINT_TYPE.equals(ByteBufferUtil.string(((UserType)elementType).name)) && value instanceof List && ((List)value).get(0) instanceof Double) {
                // geo_point as array of double lon,lat like [1.2, 1.3]
                UserType udt = (UserType)elementType;
                List<Double> values = (List<Double>)value;
                ByteBuffer[] elements = new ByteBuffer[] {
                    serialize(ksName, cfName, udt.fieldType(0), GeoPointFieldMapper.Names.LAT, values.get(1), null),
                    serialize(ksName, cfName, udt.fieldType(1), GeoPointFieldMapper.Names.LON, values.get(0), null)
                };
                ByteBuffer geo_point = TupleType.buildValue(elements);
                return CollectionSerializer.pack(ImmutableList.of(geo_point), 1, Server.VERSION_3);
            } 
            
            if (value instanceof Collection) {
                // list of elementType
                List<ByteBuffer> elements = new ArrayList<ByteBuffer>();
                for(Object v : flattenCollection((Collection) value)) {
                    ByteBuffer bb = serialize(ksName, cfName, elementType, name, v, mapper);
                    elements.add(bb);
                }
                return CollectionSerializer.pack(elements, elements.size(), Server.VERSION_3);
            } else {
                // singleton list
                ByteBuffer bb = serialize(ksName, cfName, elementType, name, value, mapper);
                return CollectionSerializer.pack(ImmutableList.of(bb), 1, Server.VERSION_3);
            } 
        } else if (type.getSerializer() instanceof UUIDSerializer) {
            // #74 workaround
            return type.decompose( (value instanceof UUID) ? value : UUID.fromString((String)value));
        } else {
            // Native cassandra type, encoded with mapper if available.
            if (mapper != null) {
                if (mapper instanceof FieldMapper) {
                    return type.decompose( value((FieldMapper) mapper, value) );
                } else if (mapper instanceof ObjectMapper && !((ObjectMapper)mapper).isEnabled()) {
                    // enabled=false => store field as json text
                    XContentBuilder builder = XContentFactory.jsonBuilder();
                    builder.map( (Map) value);
                    return type.decompose( builder.string() );
                }
            } 
            return type.decompose( value );
        }
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb) throws CharacterCodingException {
        return deserialize(type, bb, null);
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb, Mapper mapper) throws CharacterCodingException {
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            Map<String, Object> mapValue = new HashMap<String, Object>();
            ByteBuffer[] components = udt.split(bb);
            
            if (GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                if (components[0] != null) 
                    mapValue.put(GeoPointFieldMapper.Names.LAT, deserialize(udt.type(0), components[0], null));
                if (components[1] != null) 
                    mapValue.put(GeoPointFieldMapper.Names.LON, deserialize(udt.type(1), components[1], null));
            } else {
                for (int i = 0; i < components.length; i++) {
                    String fieldName = UTF8Type.instance.compose(udt.fieldName(i));
                    AbstractType<?> ctype = udt.type(i);
                    Mapper subMapper = null;
                    if (mapper != null && mapper instanceof ObjectMapper)
                        subMapper = ((ObjectMapper)mapper).getMapper(fieldName);
                    Object value = (components[i] == null) ? null : deserialize(ctype, components[i], subMapper);
                    mapValue.put(fieldName, value);
                }
            }
            return mapValue;
        } else if (type instanceof ListType) {
            ListType<?> ltype = (ListType<?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            List list = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                list.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, Server.VERSION_3), mapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return list;
        } else if (type instanceof SetType) {
            SetType<?> ltype = (SetType<?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            Set set = new HashSet(size);
            for (int i = 0; i < size; i++) {
                set.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, Server.VERSION_3), mapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return set;
        } else if (type instanceof MapType) {
            MapType<?,?> ltype = (MapType<?,?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            Map map = new LinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                ByteBuffer kbb = CollectionSerializer.readValue(input, Server.VERSION_3);
                ByteBuffer vbb = CollectionSerializer.readValue(input, Server.VERSION_3);
                String key = (String) ltype.getKeysType().compose(kbb);
                Mapper subMapper = null;
                if (mapper != null) {
                    assert mapper instanceof ObjectMapper : "Expecting an object mapper for MapType";
                    subMapper = ((ObjectMapper)mapper).getMapper(key);
                }
                map.put(key, deserialize(ltype.getValuesType(), vbb, subMapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return map;
        } else {
             Object value = type.compose(bb);
             if (mapper != null && mapper instanceof FieldMapper) {
                 return ((FieldMapper)mapper).fieldType().valueForSearch(value);
             }
             return value;
        }
    }

}
