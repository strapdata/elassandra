/*
 * Copyright (c) 2016 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.cassandra.cluster;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;

import java.io.IOException;
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
import java.util.List;
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
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.composites.CBuilder;
import org.apache.cassandra.db.composites.CType;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.index.SecondaryIndex;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.TupleType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.locator.DatacenterReplicationStrategy;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.MapSerializer;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.Server;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.cassandra.ConcurrentMetaDataUpdateException;
import org.elasticsearch.cassandra.NoPersistedMetaDataException;
import org.elasticsearch.cassandra.cluster.routing.AbstractSearchStrategy;
import org.elasticsearch.cassandra.cluster.routing.PrimaryFirstSearchStrategy;
import org.elasticsearch.cassandra.index.ElasticSecondaryIndex;
import org.elasticsearch.cassandra.index.SecondaryIndicesService;
import org.elasticsearch.cassandra.index.ThreadLocalOptimizedElasticSecondaryIndex;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateObserver;
import org.elasticsearch.cluster.ClusterStateObserver.EventPredicate;
import org.elasticsearch.cluster.ProcessedClusterStateNonMasterUpdateTask;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataMappingService;
import org.elasticsearch.cluster.node.DiscoveryNodeService;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.InternalClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContent.Params;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.gateway.GatewayService;
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
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.core.BinaryFieldMapper;
import org.elasticsearch.index.mapper.core.BooleanFieldMapper;
import org.elasticsearch.index.mapper.core.ByteFieldMapper;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.DoubleFieldMapper;
import org.elasticsearch.index.mapper.core.FloatFieldMapper;
import org.elasticsearch.index.mapper.core.IntegerFieldMapper;
import org.elasticsearch.index.mapper.core.LongFieldMapper;
import org.elasticsearch.index.mapper.core.ShortFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper.Names;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.TimestampFieldMapper;
import org.elasticsearch.index.mapper.ip.IpFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.settings.NodeSettingsService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 */
public class InternalCassandraClusterService extends InternalClusterService {

    public static String ELASTIC_ID_COLUMN_NAME = "_id";
    public static String MAPPING_UPDATE_TIMEOUT = "cassandra.mapping_update.timeout";
    public static String PERSISTED_METADATA = "cassandra.pertisted.metadata";

    public static String SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS = "cluster.default_secondary_index_class";
    public static String SETTING_CLUSTER_DEFAULT_SEARCH_STRATEGY_CLASS = "cluster.default_search_strategy_class";
    
    public static Map<String, String> cqlMapping = new ImmutableMap.Builder<String,String>()
            .put("text", "string")
            .put("ascii", "string")
            .put("varchar", "string")
            .put("timestamp", "date")
            .put("int", "integer")
            .put("double", "double")
            .put("float", "float")
            .put("bigint", "long")
            .put("boolean", "boolean")
            .put("blob", "binary")
            .put("inet", "ip" )
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
    
    private final IndicesService indicesService;
    private final DiscoveryService discoveryService;
    private final TimeValue mappingUpdateTimeout;
    
    protected final MappingUpdatedAction mappingUpdatedAction;
    
    protected Class defaultSecondaryIndexClass = ThreadLocalOptimizedElasticSecondaryIndex.class;
    protected Class defaultSearchStrategyClass = PrimaryFirstSearchStrategy.class;
    protected Map<String, AbstractSearchStrategy> registry = new ConcurrentHashMap<String, AbstractSearchStrategy>();
    
    private ConsistencyLevel metadataWriteCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.write.cl","QUORUM"));
    private ConsistencyLevel metadataReadCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.read.cl","QUORUM"));
    private ConsistencyLevel metadataSerialCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.serial.cl","SERIAL"));
    
    @Inject
    public InternalCassandraClusterService(Settings settings, DiscoveryService discoveryService, OperationRouting operationRouting, TransportService transportService, NodeSettingsService nodeSettingsService,
            ThreadPool threadPool, ClusterName clusterName, DiscoveryNodeService discoveryNodeService, Version version, 
            SecondaryIndicesService secondaryIndicesService, IndicesService indicesService, MappingUpdatedAction mappingUpdatedAction) {
        super(settings, discoveryService, operationRouting, transportService, nodeSettingsService, threadPool, clusterName, discoveryNodeService, version, secondaryIndicesService, indicesService);
        this.mappingUpdateTimeout = settings.getAsTime(MAPPING_UPDATE_TIMEOUT, TimeValue.timeValueSeconds(30));
        this.indicesService = indicesService;
        this.discoveryService = discoveryService;
        this.mappingUpdatedAction = mappingUpdatedAction;
        
        if (settings.get(SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS) != null) {
            try {
                this.defaultSecondaryIndexClass = Class.forName(settings.get(SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS));
            } catch (ClassNotFoundException e) {
                logger.error("Cannot load " + SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS, e);
            }
        }
        if (settings.get(SETTING_CLUSTER_DEFAULT_SEARCH_STRATEGY_CLASS) != null) {
            try {
                this.defaultSearchStrategyClass = Class.forName(settings.get(SETTING_CLUSTER_DEFAULT_SEARCH_STRATEGY_CLASS));
                registry.put(defaultSearchStrategyClass.getClass().getName(), (AbstractSearchStrategy) defaultSearchStrategyClass.newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.error("Cannot load " + SETTING_CLUSTER_DEFAULT_SEARCH_STRATEGY_CLASS, e);
            }
        }
    }
    
    public boolean isNativeCql3Type(String cqlType) {
        return cqlMapping.keySet().contains(cqlType) && !cqlType.startsWith("geo_");
    }
    
    public AbstractSearchStrategy.Result searchStrategy(IndexMetaData indexMetaData, Collection<InetAddress> startedShards) {
        String searchStrategyClassName = defaultSearchStrategyClass.getName();
        if (indexMetaData.searchStrategyClass() != null) {
            searchStrategyClassName = indexMetaData.searchStrategyClass();
        }
        AbstractSearchStrategy searchStrategy = registry.get(searchStrategyClassName);
        if (searchStrategy == null) {
            try {
                searchStrategy = (AbstractSearchStrategy) Class.forName(searchStrategyClassName).newInstance();
                registry.putIfAbsent(searchStrategyClassName, searchStrategy);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                logger.error("Failed to instanciate search strategy "+searchStrategyClassName, e);
                searchStrategy = registry.get(defaultSearchStrategyClass.getClass().getName());
            }
        }
        return searchStrategy.topology(indexMetaData.keyspace(), startedShards);
    }
    
    /**
     * Binds values with query and executes with consistency level.
     * 
     * @param cl
     * @param query
     * @param values
     * @return
     * @throws RequestExecutionException
     * @throws RequestValidationException
     */
    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final String query, final Object... values) throws RequestExecutionException,
            RequestValidationException, InvalidRequestException {
        return process(cl, serialConsistencyLevel, query, new Long(0), values);
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final String query, Long writetime, final Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        if (logger.isDebugEnabled()) {
            logger.debug("processing CL={} SERIAL_CL={} query=[{}] values={} ", cl, serialConsistencyLevel, query, Arrays.toString(values));
        }
        ParsedStatement.Prepared prepared = QueryProcessor.getStatement(query, ClientState.forInternalCalls());
        List<ByteBuffer> boundValues = new ArrayList<ByteBuffer>(values.length);
        for (int i = 0; i < values.length; i++) {
            Object v = values[i];
            AbstractType type = prepared.boundNames.get(i).type;
            boundValues.add(v instanceof ByteBuffer || v == null ? (ByteBuffer) v : type.decompose(v));
        }
        QueryState queryState = QueryState.forInternalCalls();
        ResultMessage result = QueryProcessor.instance.process(query, queryState, QueryOptions.forInternalCalls(cl, serialConsistencyLevel, boundValues));
        writetime = queryState.getTimestamp();
        if (result instanceof ResultMessage.Rows)
            return UntypedResultSet.create(((ResultMessage.Rows) result).result);
        else
            return null;
    }

    public UntypedResultSet process(ConsistencyLevel cl, String query) throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, query, new Object[] {});
    }

    public UntypedResultSet process(ConsistencyLevel cl, String query, Object... values) throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, query, values);
    }

    
    /**
     * Don't use QueryProcessor.executeInternal, we need to propagate this on
     * all nodes.
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#createIndexKeyspace(java.lang.String,
     *      int)
     **/
    @Override
    public void createIndexKeyspace(final String index, final int replicationFactor) throws IOException {
        try {
            QueryProcessor.process(String.format("CREATE KEYSPACE IF NOT EXISTS \"%s\" WITH replication = {'class':'NetworkTopologyStrategy', '%s':'%d' };", index,
                    DatabaseDescriptor.getLocalDataCenter(), replicationFactor), ConsistencyLevel.LOCAL_ONE);
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    /**
     * Don't use QueryProcessor.executeInternal, we need to propagate this on
     * all nodes.
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#removeIndexKeyspace(java.lang.String,
     *      int)
     **/
    @Override
    public void removeIndexKeyspace(final String index) throws IOException {
        try {
            QueryProcessor.process(String.format("DROP KEYSPACE \"%s\";", index), ConsistencyLevel.LOCAL_ONE);
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public Pair<List<String>, List<String>> getUDTInfo(final String ksName, final String typeName) {
        try {
            UntypedResultSet result = QueryProcessor.executeInternal("SELECT field_names, field_types FROM system.schema_usertypes WHERE keyspace_name = ? AND type_name = ?", new Object[] { ksName,
                    typeName });
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
    
    public MapType<?,?> getMapType(final String ksName, final String cfName, final String colName) {
        try {
            UntypedResultSet result = QueryProcessor.executeInternal("SELECT validator FROM system.schema_columns WHERE keyspace_name = ? AND columnfamily_name = ? AND column_name = ?", 
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
    

    public Object value(FieldMapper mapper, Object value) {
        if (mapper instanceof DateFieldMapper) {
            // workaround because elasticsearch manage Date as Long
            return new Date(((DateFieldMapper) mapper).fieldType().value(value));
        } else if (mapper instanceof IpFieldMapper) {
            // workaround because elasticsearch manage InetAddress as Long
            Long ip = (Long) ((IpFieldMapper) mapper).fieldType().value(value);
            return  com.google.common.net.InetAddresses.forString(IpFieldMapper.longToIp(ip));
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

    /**
     * Serialize a cassandra typed object.
     * @param ksName
     * @param cfName
     * @param type
     * @param name
     * @param value
     * @param mapper
     * @return
     * @throws SyntaxException
     * @throws ConfigurationException
     * @throws CharacterCodingException 
     */
    public ByteBuffer serializeType(final String ksName, final String cfName, final AbstractType type, final String name, final Object value, final Mapper mapper) 
            throws SyntaxException, ConfigurationException, CharacterCodingException {
        if (value == null) {
            return null;
        }
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            ByteBuffer[] components = new ByteBuffer[udt.size()];
            int i=0;
            
            if (GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                logger.trace(" geo_point value=[{}]" , value);
                GeoPoint geoPoint = new GeoPoint();
                if (value instanceof String) {
                    // parse from string lat,lon (ex: "41.12,-71.34") or geohash (ex:"drm3btev3e86")
                    geoPoint.resetFromString((String)value);
                } else {
                    // parse from lat, lon fields as map
                    Map<String, Object> mapValue = (Map<String, Object>) value;
                    geoPoint.reset((Double)mapValue.get(GeoPointFieldMapper.Names.LAT), (Double)mapValue.get(GeoPointFieldMapper.Names.LON));
                }
                components[i++]=serializeType(ksName, cfName, udt.fieldType(0), GeoPointFieldMapper.Names.LAT, geoPoint.lat(), null);
                components[i++]=serializeType(ksName, cfName, udt.fieldType(1), GeoPointFieldMapper.Names.LON, geoPoint.lon(), null);
            } else {
                Map<String, Object> mapValue = (Map<String, Object>) value;
                for (int j = 0; j < udt.size(); j++) {
                    String subName = UTF8Type.instance.compose(udt.fieldName(j));
                    AbstractType<?> subType = udt.fieldType(j);
                    Object subValue = mapValue.get(subName);
                    Mapper subMapper = ((ObjectMapper) mapper).getMapper(subName);
                    components[i++]=serializeType(ksName, cfName, subType, subName, subValue, subMapper);
                }
            }
            return TupleType.buildValue(components);
        } else if (type instanceof MapType) {
            MapType mapType = getMapType(ksName, cfName, name);
            MapSerializer serializer = mapType.getSerializer();
            Map map = (Map)value;
            List<ByteBuffer> buffers = serializer.serializeValues((Map)value);
            return CollectionSerializer.pack(buffers, map.size(), Server.VERSION_3);
        } else if (type instanceof CollectionType) {
            AbstractType elementType = (type instanceof ListType) ? ((ListType)type).getElementsType() : ((SetType)type).getElementsType();
            
            if (elementType instanceof UserType && GEO_POINT_TYPE.equals(ByteBufferUtil.string(((UserType)elementType).name)) && value instanceof List && ((List)value).get(0) instanceof Double) {
                // geo_point as array of double lon,lat like [1.2, 1.3]
                UserType udt = (UserType)elementType;
                List<Double> values = (List<Double>)value;
                ByteBuffer[] elements = new ByteBuffer[] {
                        serializeType(ksName, cfName, udt.fieldType(0), GeoPointFieldMapper.Names.LAT, values.get(1), null),
                        serializeType(ksName, cfName, udt.fieldType(1), GeoPointFieldMapper.Names.LON, values.get(0), null)
                };
                ByteBuffer geo_point = TupleType.buildValue(elements);
                return CollectionSerializer.pack(ImmutableList.of(geo_point), 1, Server.VERSION_3);
            } 
            
            if (!(value instanceof Collection)) {
                // singleton list
                ByteBuffer bb = serializeType(ksName, cfName, elementType, name, value, mapper);
                return CollectionSerializer.pack(ImmutableList.of(bb), 1, Server.VERSION_3);
            }
            // list of elementType
            List<ByteBuffer> elements = new ArrayList<ByteBuffer>();
            for(Object v : (Collection) value) {
                ByteBuffer bb = serializeType(ksName, cfName, elementType, name, v, mapper);
                elements.add(bb);
            }
            return CollectionSerializer.pack(elements, elements.size(), Server.VERSION_3);
        } else {
            // Native cassandra type, encoded with mapper if available.
            if (mapper != null) {
                return type.decompose( value((FieldMapper) mapper, value) );
            } 
            return type.decompose( value );
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#updateUDT(java.lang.
     * String, java.lang.String, java.lang.String,
     * org.elasticsearch.index.mapper.object.ObjectMapper)
     */
    @Override
    public String buildUDT(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {

        String typeName = cfName + "_" + objectMapper.fullPath().replace('.', '_');

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
            StringBuilder create = new StringBuilder(String.format("CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( ", ksName, typeName));
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
                
                create.append('\"').append(shortName).append("\" ");
                if (mapper instanceof ObjectMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_'))
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(">");
                } else if (mapper instanceof GeoPointFieldMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(GEO_POINT_TYPE)
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(">");
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
            if (logger.isDebugEnabled()) {
                logger.debug("create UDT:"+ create.toString());
            }
            QueryProcessor.process(create.toString(), ConsistencyLevel.LOCAL_ONE);
        } else {
            // update existing UDT
            for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
                Mapper mapper = it.next();
                int lastDotIndex = mapper.name().lastIndexOf('.');
                String shortName = (lastDotIndex > 0) ? mapper.name().substring(lastDotIndex+1) :  mapper.name();
                StringBuilder update = new StringBuilder(String.format("ALTER TYPE \"%s\".\"%s\" ADD \"%s\" ", ksName, typeName,shortName));
                if (!udt.left.contains(shortName)) {
                    if (mapper instanceof ObjectMapper) {
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) update.append(mapper.cqlCollectionTag()).append("<");
                        update.append("frozen<")
                            .append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_'))
                            .append(">");
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) update.append(">");
                    } else if (mapper instanceof GeoPointFieldMapper) {
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) update.append(mapper.cqlCollectionTag()).append("<");
                        update.append("frozen<")
                            .append(GEO_POINT_TYPE)
                            .append(">");
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) update.append(">");
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

    public static String GEO_POINT_TYPE = "geo_point";
    public void buildGeoPointType(String ksName) throws RequestExecutionException {
        String query = String.format("CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( %s double, %s double)", ksName, GEO_POINT_TYPE,Names.LAT,Names.LON);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }

    public void buildGeoShapeType(String ksName, String typeName) {
        // TODO: Implements geo_shape.
    }
    
   
    public void buildNativeOrUdtMapping(Map<String, Object> mapping, final AbstractType<?> type) throws IOException {
        CQL3Type cql3type = type.asCQL3Type();
        if (cql3type instanceof CQL3Type.Native) {
            String esType = cqlMapping.get(cql3type.toString());
            if (esType != null) {
                mapping.put("type", esType);
                if (esType.equals("string")) {
                    mapping.put("index","not_analyzed");
                }
            } else {
                logger.error("CQL type "+cql3type.toString()+" not supported");
            }
        } else if (cql3type instanceof CQL3Type.UserDefined) {
            mapping.put("type", ObjectMapper.NESTED_CONTENT_TYPE);
            mapping.put(TypeParsers.CQL_STRUCT, "udt");
            Map<String, Object> properties = Maps.newHashMap();
            TupleType tuple = (TupleType)type;
            for(int i=0; i< tuple.size(); i++) {
                buildCollectionMapping(properties, tuple.type(i));
            }
            mapping.put("properties", properties);
        }
    }
    
    public void buildCollectionMapping(Map<String, Object> mapping, final AbstractType<?> type) throws IOException {
        if (type.isCollection()) {
            if (type instanceof ListType) {
                mapping.put(TypeParsers.CQL_COLLECTION, "list");
                buildNativeOrUdtMapping(mapping, ((ListType<?>)type).getElementsType() );
            } else if (type instanceof SetType) {
                mapping.put(TypeParsers.CQL_COLLECTION, "set");
                buildNativeOrUdtMapping(mapping, ((SetType<?>)type).getElementsType() );
            } else if (type instanceof MapType) {
                MapType<?,?> mtype = (MapType<?,?>)type;
                if (mtype.getKeysType().asCQL3Type() == CQL3Type.Native.TEXT) {
                   mapping.put(TypeParsers.CQL_COLLECTION, "singleton");
                   mapping.put(TypeParsers.CQL_STRUCT, "map");
                   mapping.put(TypeParsers.CQL_PARTIAL_UPDATE, Boolean.TRUE);
                   mapping.put("type", ObjectMapper.NESTED_CONTENT_TYPE);
                } else {
                    throw new IOException("Expecting a map<text,?>");
                }
            }
        } else {
            mapping.put(TypeParsers.CQL_COLLECTION, "singleton");
            buildNativeOrUdtMapping(mapping, type );
        }
    }
    
    
    

    public Map<String, Object> expandTableMapping(final String ksName, Map<String, Object> mapping) throws IOException, SyntaxException, ConfigurationException {
        for(String type : mapping.keySet()) {
            expandTableMapping(ksName, type, (Map<String, Object>)mapping.get(type));
        }
        return mapping;
    }
        
    
    public Map<String, Object> expandTableMapping(final String ksName, final String cfName, Map<String, Object> mapping) throws IOException, SyntaxException, ConfigurationException {
        final String columnRegexp = (String)mapping.get("columns_regexp");
        if (columnRegexp != null) {
            mapping.remove("columns_regexp");
            Pattern pattern =  Pattern.compile(columnRegexp);
            Map<String, Object> properties = (Map)mapping.get("properties");
            if (properties == null) {
                properties = Maps.newHashMap();
                mapping.put("properties", properties);
            }
            try {
                CFMetaData metadata = getCFMetaData(ksName, cfName);
                List<String> pkColNames = new ArrayList<String>(metadata.partitionKeyColumns().size() + metadata.clusteringColumns().size());
                for(ColumnDefinition cd: Iterables.concat(metadata.partitionKeyColumns(), metadata.clusteringColumns())) {
                    pkColNames.add(cd.name.toString());
                }
                
                UntypedResultSet result = QueryProcessor.executeInternal("SELECT column_name, validator FROM system.schema_columns WHERE keyspace_name=? and columnfamily_name=?", 
                        new Object[] { ksName, cfName });
                for (Row row : result) {
                    if (row.has("validator") && pattern.matcher(row.getString("column_name")).matches() && !row.getString("column_name").startsWith("_")) {
                        String columnName = row.getString("column_name");
                        Map<String,Object> props = (Map<String, Object>) properties.get(columnName);
                        if (props == null) {
                            props = Maps.newHashMap();
                            properties.put(columnName, props);
                        }
                        int pkOrder = pkColNames.indexOf(columnName);
                        if (pkOrder >= 0) {
                            props.put(TypeParsers.CQL_PRIMARY_KEY_ORDER, pkOrder);
                            if (pkOrder < metadata.partitionKeyColumns().size()) {
                                props.put(TypeParsers.CQL_PARTITION_KEY, true);
                            }
                        }
                        if (metadata.getColumnDefinition(new ColumnIdentifier(columnName, true)).isStatic()) {
                            props.put(TypeParsers.CQL_STATIC_COLUMN, true);
                        }
                        AbstractType<?> type =  TypeParser.parse(row.getString("validator"));
                        buildCollectionMapping(props, type);
                    }
                }
                if (logger.isDebugEnabled()) 
                    logger.debug("mapping {} : {}", cfName, mapping);
                return mapping;
            } catch (IOException | SyntaxException | ConfigurationException e) {
                logger.warn("Failed to build elasticsearch mapping " + ksName + "." + cfName, e);
                throw e;
            }
        }
        return mapping;
    }
    
    public ClusterState updateNumberOfShards(ClusterState currentState) {
        int numberOfNodes = currentState.nodes().size();
        MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
        for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
            IndexMetaData indexMetaData = it.next();
            IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
            indexMetaDataBuilder.numberOfShards(numberOfNodes);
            String keyspace = indexMetaData.getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,indexMetaData.getIndex());
            if (Schema.instance != null && Schema.instance.getKeyspaceInstance(keyspace) != null) {
                indexMetaDataBuilder.numberOfReplicas( Schema.instance.getKeyspaceInstance(keyspace).getReplicationStrategy().getReplicationFactor() - 1 );
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
        submitStateUpdateTask("Update numberOfShard and numberOfReplica of all indices" , Priority.URGENT, new ProcessedClusterStateNonMasterUpdateTask() {
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
    
            @Override
            public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
            }
        });
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#updateTableSchema(java
     * .lang.String, java.lang.String, java.util.Set,
     * org.elasticsearch.index.mapper.DocumentMapper)
     */
    @Override
    public void updateTableSchema(final String index, final String type, Set<String> columns, DocumentMapper docMapper) throws IOException {
        try {
            IndexService indexService = this.indicesService.indexService(index);
            String ksName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, index);
            createIndexKeyspace(ksName, settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 0) +1);

            CFMetaData cfm = Schema.instance.getCFMetaData(ksName, type);
            boolean newTable = (cfm == null);
            
            logger.debug("Inferring CQL3 schema {}.{} with columns={}", ksName, type, columns);
            StringBuilder columnsList = new StringBuilder();
            Map<String,Boolean> columnsMap = new HashMap<String, Boolean>(columns.size());
            String[] primaryKeyList = new String[(newTable) ? columns.size()+1 : cfm.partitionKeyColumns().size()+cfm.clusteringColumns().size()];
            int primaryKeyLength = 0;
            int partitionKeyLength = 0;
            for (String column : columns) {
                if (columnsList.length() > 0) columnsList.append(',');
                if (column.equals("_token")) {
                    continue; // ignore pseudo column known by Elasticsearch
                }
                String cqlType = null;
                boolean isStatic = false;
                FieldMapper fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                if (fieldMapper != null) {
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
                    if (fieldMapper instanceof GeoPointFieldMapper) {
                        cqlType = GEO_POINT_TYPE;
                        buildGeoPointType(ksName);
                    } else {
                        cqlType = mapperToCql.get(fieldMapper.getClass());
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
                       logger.warn("Cannot infer CQL type mapping for field [{}]", column);
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
                    if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
                        // TODO: check columnName exists and is map<text,?>
                        cqlType = buildCql(ksName, type, column, objectMapper);
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                            cqlType = objectMapper.cqlCollectionTag()+"<"+cqlType+">";
                        }
                        //logger.debug("Expecting column [{}] to be a map<text,?>", column);
                    } else  if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                        // Cassandra 2.1.8 : Non-frozen collections are not allowed inside collections
                        cqlType = "frozen<\"" + buildCql(ksName, type, column, objectMapper) + "\">";
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
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
                                if (primaryKeyList[i].equals(column))
                                    throw new Exception("Cannot alter primary key of an existing table");
                            }
                            try {
                                String query = String.format("ALTER TABLE \"%s\".\"%s\" ADD \"%s\" %s %s", ksName, type, column, cqlType,(isStatic) ? "static":"");
                                logger.debug(query);
                                QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                            } catch (Exception e) {
                                logger.warn("Cannot alter table {}.{} column {} with type {}", e, ksName, type, column, cqlType);
                            }
                        } else {
                            // check that the existing column matches the provided mapping
                            // TODO: do this check for collection
                            if (!cdef.type.isCollection()) {
                                // cdef.type.asCQL3Type() does not include frozen, nor quote, so can do this check for collection.
                                if (!cdef.type.asCQL3Type().toString().equals(cqlType) && !cqlType.equals("frozen<"+cdef.type.asCQL3Type().toString()+">")) {
                                    throw new IOException("Existing column "+column+" mismatch type "+cqlType);
                                }
                            }
                        }
                    }
                }
              
            }

            // add _parent column if necessary. Parent and child documents should have the same partition key.
            if (docMapper.parentFieldMapper().active()) {
                if (newTable) {
                    // _parent is a JSON array representation of the parent PK.
                    columnsList.append(", \"_parent\" text");
                } else {
                    try {
                        String query = String.format("ALTER TABLE \"%s\".\"%s\" ADD \"_parent\" text", ksName, type);
                        logger.debug(query);
                        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                    } catch (Exception e) {
                        logger.warn("Cannot alter table {}.{} column _parent with type text", e, ksName, type);
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
                String query = String.format("CREATE TABLE IF NOT EXISTS \"%s\".\"%s\" ( %s, PRIMARY KEY (%s) ) WITH COMMENT='Auto-created by Elassandra'", 
                        ksName, type, columnsList.toString(), primaryKey.toString());
                logger.debug(query);
                QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
            }

        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    @Override
    public void createSecondaryIndices(String index) throws IOException {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, index);
        String className = indexService.indexSettings().get(IndexMetaData.SETTING_SECONDARY_INDEX_CLASS, this.settings.get(SETTING_CLUSTER_DEFAULT_SECONDARY_INDEX_CLASS, defaultSecondaryIndexClass.getName()));
        IndexMetaData indexMetaData = state().metaData().index(index);
        for(ObjectCursor<MappingMetaData> cursor: indexMetaData.getMappings().values()) {
            createSecondaryIndex(ksName, cursor.value, className);
        }
    }
   
    // build secondary indices when shard is started and mapping applied
    @Override
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksName, mapping.type());
        if (cfm != null && mapping.sourceAsMap().get("properties") != null) {
            try {
                for (String column : ((Map<String,Object>)mapping.sourceAsMap().get("properties")).keySet()) {
                    if (column.equals("_token")) {
                        continue; // ignore pseudo column known by Elasticsearch
                    }
                    ColumnDefinition cdef = (cfm == null) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                    if ((cdef != null) && !cdef.isIndexed() && !(cfm.partitionKeyColumns().size()==1 && cdef.kind == ColumnDefinition.Kind.PARTITION_KEY )) {
                        String query = String.format("CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" (\"%s\") USING '%s'",
                                buildIndexName(mapping.type(), column), ksName, mapping.type(), column, className);
                        logger.debug(query);
                        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                    } 
                }
            } catch (Throwable e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void dropSecondaryIndices(String ksName) throws RequestExecutionException {
        for(String cfName : Schema.instance.getKeyspaceMetaData(ksName).keySet()) {
            dropSecondaryIndex(ksName, cfName);
        }
    }
    
    @Override
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException  {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
        if (cfm != null) {
            for (ColumnDefinition cdef : Iterables.concat(cfm.partitionKeyColumns(), cfm.clusteringColumns(), cfm.regularColumns())) {
                if (cdef.isIndexed() && cdef.getIndexOptions().get(SecondaryIndex.CUSTOM_INDEX_OPTION_NAME).endsWith("ElasticSecondaryIndex")) {
                    logger.debug("DROP INDEX IF EXISTS {}.{}", ksName, buildIndexName(cfName, cdef.name.toString()));
                    QueryProcessor.process(String.format("DROP INDEX IF EXISTS \"%s\".\"%s\"",
                        ksName, buildIndexName(cfName, cdef.name.toString())), 
                        ConsistencyLevel.LOCAL_ONE);
                } 
            }
        }
    }
    
    public static String buildIndexName(final String cfName, final String colName) {
        return new StringBuilder("elastic_")
            .append(cfName).append('_')
            .append(colName.replaceAll("\\W+", "_"))
            .append("_idx").toString();
    }
    
    public CFMetaData getCFMetaData(final String ksName, final String cfName) throws ActionRequestValidationException {
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
    public String[] mappedColumns(final String index, Uid uid) throws JsonParseException, JsonMappingException, IOException  {
        return mappedColumns(index, uid.type(), isStaticDocument(index,uid));
    }
    
    /**
     * Return list of columns having an ElasticSecondaryIndex.
     */
    public String[] mappedColumns(final String index, final String type, final boolean forStaticDocument)  {
        IndexMetaData indexMetaData = state().metaData().index(index);
        if (indexMetaData != null) {
            MappingMetaData mappingMetaData = indexMetaData.mapping(type);
            if (mappingMetaData != null) {
                try {
                    Map<String, Object> mapping = ((Map<String, Object>)mappingMetaData.sourceAsMap().get("properties"));
                    Set<String> cols = new HashSet<String>(mapping.size());
                    for(String s : mapping.keySet()) {
                        int x = s.indexOf('.');
                        String colName = (x > 0) ? s.substring(0,x) : s;
                        if (forStaticDocument) {
                            Map<String, Object> fieldMapping = (Map<String, Object>)mapping.get(colName);
                            if ((fieldMapping.get(TypeParsers.CQL_STATIC_COLUMN) != null && fieldMapping.get(TypeParsers.CQL_STATIC_COLUMN).equals(true)) || 
                                 fieldMapping.get(TypeParsers.CQL_PARTITION_KEY) != null && fieldMapping.get(TypeParsers.CQL_PARTITION_KEY).equals(true)) {
                                cols.add( colName );
                            }
                        } else {
                           cols.add( colName );
                        }
                    }
                    return cols.toArray(new String[cols.size()]);
                } catch (IOException e) {
                }
            }
        }
        return new String[0];
    }
    
    @Override
    public String[] mappedColumns(final MapperService mapperService, final String type, final boolean forStaticDocument) {
        return mappedColumns(mapperService.index().getName(), type, forStaticDocument);
    }
    
    
    @Override
    public UntypedResultSet fetchRow(final String index, final String type, final String id) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        return fetchRow(index, type, mappedColumns(index, new Uid(type, id)), id, ConsistencyLevel.LOCAL_ONE);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#fetchRow(java.lang.String
     * , java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public UntypedResultSet fetchRow(final String index, final String type, final String[] requiredColumns, final String id) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException {
        return fetchRow(index, type, requiredColumns, id, ConsistencyLevel.LOCAL_ONE);
    }

    /**
     * Fetch from the coordinator node.
     */
    @Override
    public UntypedResultSet fetchRow(final String index, final String cfName, final String[] requiredColumns, final String id, final ConsistencyLevel cl) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        DocPrimaryKey docPk = parseElasticId(index, cfName, id);
        return process(cl, buildFetchQuery(index,cfName,requiredColumns, docPk.isStaticDocument), docPk.values);
    }

    /**
     * Fetch row from local node.
     * @param index
     * @param type
     * @param id
     * @param requiredColumns
     * @return
     * @throws ConfigurationException 
     * @throws InvalidRequestException
     * @throws RequestExecutionException
     * @throws RequestValidationException
     * @throws IOException
     */
    @Override
    public UntypedResultSet fetchRowInternal(final String index, final String cfName, final String[] requiredColumns, final String id) throws ConfigurationException, IOException  {
        DocPrimaryKey docPk = parseElasticId(index, cfName, id);
        return fetchRowInternal(index, cfName, requiredColumns, docPk.values, docPk.isStaticDocument);
    }
    
    @Override
    public UntypedResultSet fetchRowInternal(final String index, final String cfName, final String[] requiredColumns, final Object[] pkColumns, boolean forStaticDocument) throws ConfigurationException, IOException, IndexNotFoundException  {
        return QueryProcessor.executeInternal(buildFetchQuery(index, cfName, requiredColumns, forStaticDocument), pkColumns);
    }
    
    public String buildFetchQuery(final String index, final String cfName, final String[] requiredColumns, boolean forStaticDocument) throws ConfigurationException, IndexNotFoundException {
        IndexService indexService = this.indicesService.indexService(index);
        if (indexService == null) {
            throw new IndexNotFoundException(index);
        }
        String ksName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        for (String c : requiredColumns) {
            if (query.length() > 7) 
                query.append(',');
            switch(c){
            case "_token": 
                query.append("token(").append(metadata.getCqlFragments().ptCols).append(") as \"_token\""); 
                break;
            case "_routing":
                if (metadata.partitionKeyColumns().size() > 1) 
                    query.append("toJsonArray(").append(metadata.getCqlFragments().ptCols).append(") as \"_routing\"");
                else 
                    query.append(metadata.getCqlFragments().ptCols).append(" as \"_routing\"");
                break;
            case "_ttl": 
                query.append("TTL(").append(metadata.partitionKeyColumns().get(0)).append(") as \"_ttl\""); 
                break;
            case "_timestamp": 
                query.append("WRITETIME(").append(metadata.partitionKeyColumns().get(0)).append(") as \"_timestamp\"");
                break;
            default: 
                query.append("\"").append(c).append("\"");
            }
        }
        query.append(" FROM \"").append(ksName).append("\".\"").append(cfName).append("\" WHERE ").append((forStaticDocument) ? metadata.getCqlFragments().ptWhere : metadata.getCqlFragments().pkWhere );
        if (forStaticDocument) {
            query.append(" LIMIT 1");
        }
        return query.toString();
    }

    @Override
    public void deleteRow(final String ksName, final String cfName, final String id, final ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException {
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        String query = "DELETE FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ metadata.getCqlFragments().pkWhere;
        process(cl, query, parseElasticId(ksName, cfName, id).values);
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
            if (values[i] != null) {
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
    
    @Override
    public Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException {
        final Object values[] = new Object[row.getColumns().size()];
        final List<ColumnSpecification> columnSpecs = row.getColumns();
        
        final IndexService indexService = indicesService.indexServiceSafe(index);
        final DocumentMapper documentMapper = indexService.mapperService().documentMapper(type);
        final DocumentFieldMappers docFieldMappers = documentMapper.mappers();
        
        for (int columnIndex = 0; columnIndex < columnSpecs.size(); columnIndex++) {
            if (!row.has(columnIndex) || ByteBufferUtil.EMPTY_BYTE_BUFFER.equals(row.getBlob(columnIndex)) )
                continue;

            ColumnSpecification colSpec = columnSpecs.get(columnIndex);
            String columnName = colSpec.name.toString();
            CQL3Type cql3Type = colSpec.type.asCQL3Type();
            
            if (cql3Type instanceof CQL3Type.Native) {
                final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                final MappedFieldType fieldType = fieldMapper.fieldType();
                switch ((CQL3Type.Native) cql3Type) {
                case ASCII:
                case TEXT:
                case VARCHAR:
                    values[columnIndex] = row.getString(columnIndex);
                    break;
                case TIMEUUID:
                case UUID:
                    values[columnIndex] = row.getUUID(columnIndex).toString();
                    break;
                case TIMESTAMP:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getTimestamp(columnIndex).getTime()) : fieldType.value( row.getTimestamp(columnIndex).getTime());
                    break;
                case INT:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getInt(columnIndex)) : fieldType.value( row.getInt(columnIndex));
                    break;
                case BIGINT:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getLong(columnIndex)) : fieldType.value( row.getLong(columnIndex));
                    break;
                case DOUBLE:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getDouble(columnIndex)) : fieldType.value( row.getDouble(columnIndex));
                    break;
                case FLOAT:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getFloat(columnIndex)) : fieldType.value( row.getFloat(columnIndex));
                    break;
                case BLOB:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getBytes(columnIndex)) : fieldType.value( row.getBytes(columnIndex));
                    break;
                case BOOLEAN:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getBoolean(columnIndex)) : fieldType.value( row.getBoolean(columnIndex));
                    break;
                case COUNTER:
                    logger.warn("Ignoring unsupported counter {}", cql3Type);
                    break;
                case INET:
                    values[columnIndex] = (valueForSearch) ? fieldType.valueForSearch( row.getInetAddress(columnIndex).getHostAddress()) : fieldType.value( row.getInetAddress(columnIndex).getHostAddress());
                    break;
                default:
                    logger.error("Ignoring unsupported type {}", cql3Type);
                }
            } else if (cql3Type.isCollection()) {
                AbstractType<?> elementType;
                switch (((CollectionType<?>) colSpec.type).kind) {
                case LIST:
                    List list;
                    elementType = ((ListType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        final List<ByteBuffer> lbb = row.getList(columnIndex, BytesType.instance);
                        list = new ArrayList(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            list.add(ElasticSecondaryIndex.deserialize(elementType, bb, objectMapper));
                        }
                    } else {
                        final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                        final List list2 = row.getList(colSpec.name.toString(), elementType);
                        list = new ArrayList(list2.size());
                        for(Object v : list2) {
                            list.add((valueForSearch) ? fieldMapper.fieldType().valueForSearch(v) : fieldMapper.fieldType().value(v) );
                        }
                    }
                    values[columnIndex] =  (list.size() == 1) ? list.get(0) : list;
                    break;
                case SET :
                    Set set;
                    elementType = ((SetType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        final Set<ByteBuffer> lbb = row.getSet(columnIndex, BytesType.instance);
                        set = new HashSet(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            set.add(ElasticSecondaryIndex.deserialize(elementType, bb, objectMapper));
                        }
                    } else {
                        final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                        final Set set2 = row.getSet(columnIndex, elementType);
                        set = new HashSet(set2.size());
                        for(Object v : set2) {
                            set.add( (valueForSearch) ? fieldMapper.fieldType().valueForSearch(v) : fieldMapper.fieldType().value(v) );
                        }
                    }
                    values[columnIndex] =  (set.size() == 1) ? set.iterator().next() : set;
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
                        final Map<String, ByteBuffer> lbb = row.getMap(columnIndex, keyType, BytesType.instance);
                        map = new HashMap<String , Map<String, Object>>(lbb.size());
                        for(String key : lbb.keySet()) {
                            map.put(key, ElasticSecondaryIndex.deserialize(elementType, lbb.get(key), objectMapper.getMapper(key)));
                        }
                    } else {
                        Map<String,Object> map2 = (Map<String,Object>) row.getMap(columnIndex, keyType, elementType);
                        map = new HashMap<String, Object>(map2.size());
                        for(String key : map2.keySet()) {
                            FieldMapper subMapper = (FieldMapper)objectMapper.getMapper(key);
                            map.put(key,  (valueForSearch) ? subMapper.fieldType().valueForSearch(map2.get(key)) : subMapper.fieldType().value(map2.get(key)) );
                        }
                    }
                    values[columnIndex] =  map;
                    break;
                }
            } else if (colSpec.type instanceof UserType) {
                ByteBuffer bb = row.getBytes(columnIndex);
                values[columnIndex] = ElasticSecondaryIndex.deserialize(colSpec.type, bb, documentMapper.objectMappers().get(columnName));
            } else if (cql3Type instanceof CQL3Type.Custom) {
                logger.warn("CQL3.Custum type not supported for column "+columnName);
            }
        }
        return values;
    }

    

    public static class BlockingActionListener<T> implements ActionListener<T> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Throwable error = null;
        private T response = null;

        @Override
        public void onFailure(Throwable t) {
            error = t;
            latch.countDown();
        }

        @Override
        public void onResponse(T response) {
            this.response = response;
            latch.countDown();
        }
        
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
        
        public T response() {
            return this.response;
        }
    }

    
    @Override
    public void blockingMappingUpdate(IndexService indexService, String type, CompressedXContent source) throws Exception {
        BlockingActionListener<ClusterStateUpdateResponse> mappingUpdateListener = new BlockingActionListener<ClusterStateUpdateResponse>();
        MetaDataMappingService metaDataMappingService = ElassandraDaemon.injector().getInstance(MetaDataMappingService.class);
        metaDataMappingService.updateMapping(indexService.index().name(), indexService.indexUUID(), type, source, localNode().id(), mappingUpdateListener, mappingUpdateTimeout, mappingUpdateTimeout);
        mappingUpdateListener.waitForUpdate(mappingUpdateTimeout);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#insertDocument(org.
     * elasticsearch.indices.IndicesService, 
     * org.elasticsearch.action.index.IndexRequest,
     * org.elasticsearch.cluster.ClusterState, java.lang.Long,
     * java.lang.Boolean)
     */
    @Override
    public void insertDocument(final IndicesService indicesService, final IndexRequest request, final ClusterState clusterState, String timestampString) throws Exception {
        final IndexService indexService = indicesService.indexServiceSafe(request.index());
        final IndexShard indexShard = indexService.shardSafe(0);
        final SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, request.source()).type(request.type()).id(request.id()).routing(request.routing()).parent(request.parent())
                .timestamp(request.timestamp()).ttl(request.ttl());

        final Engine.IndexingOperation operation = indexShard.prepareIndex(sourceToParse, request.version(), request.versionType(), Engine.Operation.Origin.PRIMARY, request.canHaveDuplicates());
        final Mapping update = operation.parsedDoc().dynamicMappingsUpdate();
        if (update != null) {
            if (logger.isDebugEnabled()) 
                logger.debug("Document source={} require a blocking mapping update of [{}]", request.sourceAsMap(), indexService.index().name());
            // blocking Elasticsearch mapping update (required to update cassandra schema before inserting a row, this is the cost of dynamic mapping)
            blockingMappingUpdate(indexService, request.type(), new CompressedXContent(update.toString()) );
        }

        // get the docMapper after a potential mapping update
        final DocumentMapper docMapper = indexShard.mapperService().documentMapperWithAutoCreate(request.type()).getDocumentMapper();
        
        // insert document into cassandra keyspace=index, table = type
        final Map<String, Object> sourceMap = request.sourceAsMap();
        final Map<String, ObjectMapper> objectMappers = docMapper.objectMappers();
        final DocumentFieldMappers fieldMappers = docMapper.mappers();

        Long timestamp = null;
        if (docMapper.timestampFieldMapper().enabled() && timestampString != null) {
            timestamp = docMapper.timestampFieldMapper().fieldType().value(timestampString);
        }
        
        if (logger.isTraceEnabled()) 
            logger.trace("Insert index=[{}] id=[{}] source={} fieldMappers={} objectMappers={} consistency={} ttl={}", 
                indexService.index().name(), request.id(), sourceMap,Lists.newArrayList(fieldMappers.iterator()), objectMappers, 
                request.consistencyLevel().toCassandraConsistencyLevel(), request.ttl());

        final CFMetaData metadata = getCFMetaData(request.index(), request.type());
        
        if (request.parent() != null) {
            sourceMap.put(ParentFieldMapper.NAME, request.parent());
        }
        
        // normalize the _id and may find some column value in _id.
        // if the provided columns does not contains all the primary key columns, parse the _id to populate the columns in map.
        String id = request.id();
        boolean buildId = true;
        ArrayNode array = ClusterService.Utils.jsonMapper.createArrayNode();
        for(ColumnDefinition cd: Iterables.concat(metadata.partitionKeyColumns(), metadata.clusteringColumns())) {
            if (cd.name.toString().equals("_id")) {
                sourceMap.put("_id", request.id());
            }
            Object value = sourceMap.get(cd.name.toString());
            if (value != null) {
                ClusterService.Utils.addToJsonArray(cd.type, value, array);
            } else {
                buildId = false;
                parseElasticId(request.index(), request.type(), request.id(), sourceMap);
            }
        }
        if (buildId) {
            id = ClusterService.Utils.writeValueAsString(array);
        }
        
        // workaround because ParentFieldMapper.value() and UidFieldMapper.value() create an Uid.
        if (sourceMap.get(ParentFieldMapper.NAME) != null && ((String)sourceMap.get(ParentFieldMapper.NAME)).indexOf(Uid.DELIMITER) < 0) {
            sourceMap.put(ParentFieldMapper.NAME, request.type() + Uid.DELIMITER + sourceMap.get(ParentFieldMapper.NAME));
        } 
        
        Map<String, ByteBuffer> map = new HashMap<String, ByteBuffer>();
        for (String field : sourceMap.keySet()) {
            if (field.equals("_token")) continue;
            final ColumnDefinition cd = metadata.getColumnDefinition(new ColumnIdentifier(field,true));
            final Object fieldValue = sourceMap.get(field);
            try {
                if (fieldValue == null) {
                    if (cd.type.isCollection()) {
                        switch (((CollectionType<?>)cd.type).kind) {
                        case LIST :
                        case SET : 
                            map.put(field, CollectionSerializer.pack(Collections.EMPTY_LIST, 0, Server.VERSION_3)); 
                            break;
                        case MAP :
                            break;
                        }
                    } else {
                        map.put(field, null); 
                    }
                    continue;
                }
                
                FieldMapper fieldMapper = fieldMappers.getMapper(field);
                Mapper mapper = (fieldMapper != null) ? fieldMapper : objectMappers.get(field);
                if (mapper == null) {
                    logger.warn("[{}].[{}] ignoring unmapped field [{}] = [{}] ", request.index(), request.type(), field, fieldValue);
                    continue;
                }
                if (mapper.cqlCollection().equals(CqlCollection.SINGLETON) && (fieldValue instanceof Collection)) {
                    throw new MapperParsingException("field " + fieldMapper.name() + " should be a single value");
                }
                map.put(field, serializeType(request.index(), request.type(), cd.type, field, fieldValue, mapper));
            } catch (Exception e) {
                logger.error("[{}].[{}] failed to parse field {}={}", e, request.index(), request.type(), field, fieldValue );
                throw e;
            }
        }

        final String keyspaceName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, request.index());
        final boolean applied = insertRow(keyspaceName, request.type(), map, id,
                (request.opType() == OpType.CREATE), // if not exists
                request.ttl(),                       // ttl
                request.consistencyLevel().toCassandraConsistencyLevel(),   // CL
                (request.opType() == OpType.CREATE) ? null : timestamp); // writetime, should be null for conditional updates
        if (!applied) {
            throw new DocumentAlreadyExistsException(indexShard.shardId(), request.type(), request.id());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#insertRow(java.lang.
     * String, java.lang.String, java.lang.String[], java.lang.Object[],
     * java.lang.String, boolean, long,
     * org.apache.cassandra.db.ConsistencyLevel, java.lang.Long,
     * java.lang.Boolean)
     */
    public boolean insertRow(final String ksName, final String cfName, Map<String, ByteBuffer> map, String id, final boolean ifNotExists, final long ttl, final ConsistencyLevel cl,
            Long writetime) throws Exception {
        final StringBuilder questionsMarks = new StringBuilder();
        final StringBuilder columnNames = new StringBuilder();
        final ByteBuffer[] values = new ByteBuffer[map.size()];
        int i=0;
        for (Entry<String,ByteBuffer> entry : map.entrySet()) {
            if (entry.getKey().equals("_token")) continue;
            if (columnNames.length() > 0) {
                columnNames.append(',');
                questionsMarks.append(',');
            }
            columnNames.append("\"").append(entry.getKey()).append("\"");
            questionsMarks.append('?');
            values[i++] = entry.getValue();
        }
        
        final StringBuilder query = new StringBuilder();
        query.append("INSERT INTO \"").append(ksName).append("\".\"").append(cfName)
             .append("\" (").append(columnNames.toString()).append(") VALUES (").append(questionsMarks.toString()).append(") ");
        if (ifNotExists) query.append("IF NOT EXISTS ");
        if (ttl > 0 || writetime != null) query.append("USING ");
        if (ttl > 0) query.append("TTL ").append(Long.toString(ttl));
        if (ttl > 0 && writetime != null) query.append(" AND ");
        if (writetime != null) query.append("TIMESTAMP ").append(Long.toString(writetime*1000));
        
        try {
            UntypedResultSet result = process(cl, (ifNotExists) ? ConsistencyLevel.LOCAL_SERIAL : null, query.toString(), values);
            if (ifNotExists) {
                if (!result.isEmpty()) {
                    Row row = result.one();
                    if (row.has("[applied]")) {
                         return row.getBoolean("[applied]");
                    }
                }
            } 
            return true;
        } catch (Exception e) {
            logger.error("Failed to process query=" + query + " values=" + Arrays.toString(values), e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#index(java.lang.String
     * [], java.util.Collection)
     */
    @Override
    public void index(String[] indices, Collection<Range<Token>> tokenRanges) {
        /*
        ImmutableBiMap<Pair<String, String>, Pair<String, String>> immutableMapping = ImmutableBiMap.<Pair<String, String>, Pair<String, String>> copyOf(ElasticSecondaryIndex.mapping);
        for (String index : indices) {
            for (Entry<Pair<String, String>, Pair<String, String>> entry : immutableMapping.entrySet()) {
                if (entry.getKey().left.equals(index)) {
                    indexColumnFamilly(entry.getValue().left, entry.getValue().right, index, entry.getKey().right, tokenRanges);
                }
            }
        }
        */
    }

    
    public DocPrimaryKey parseElasticId(final String index, final String cfName, final String id) throws IOException {
        return parseElasticId(index, cfName, id, null);
    }
    
    /**
     * Parse elastic _id (a value or a JSON array) to build a DocPrimaryKey or populate map.
     * @param ksName
     * @param cfName
     * @param map
     * @param id
     */
    public DocPrimaryKey parseElasticId(final String index, final String cfName, final String id, Map<String, Object> map) throws JsonParseException, JsonMappingException, IOException {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.settingsService().getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        List<ColumnDefinition> clusteringColumns = metadata.clusteringColumns();
        if (id.startsWith("[") && id.endsWith("]")) {
            // _id is JSON array of values.
            Object[] elements = ClusterService.Utils.jsonMapper.readValue(id, Object[].class);
            Object[] values = (map != null) ? null : new Object[elements.length];
            String[] names = (map != null) ? null : new String[elements.length];
            int i=0;
            for(ColumnDefinition cd : Iterables.concat(partitionColumns, clusteringColumns)) {
                if (i > elements.length) 
                    throw new JsonMappingException("_id="+id+" does not match the primary key size="+(partitionColumns.size()+clusteringColumns.size()) );
                AbstractType<?> type = cd.type;
                if (map == null) {
                    names[i] = cd.name.toString();
                    values[i] = type.compose( type.fromString(elements[i].toString()) );
                } else {
                    map.put(cd.name.toString(), type.compose( type.fromString(elements[i].toString()) ) );
                }
                i++;
            }
            return (map != null) ? null : new DocPrimaryKey(names, values, (clusteringColumns.size() > 0 && elements.length == partitionColumns.size()) ) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> type = partitionColumns.get(0).type;
            if (map == null) {
                return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { type.compose( type.fromString(id) ) }, clusteringColumns.size() != 0);
            } else {
                map.put(partitionColumns.get(0).name.toString(), type.compose( type.fromString(id) ) );
                return null;
            }
        }
    }

    public DocPrimaryKey parseElasticRouting(final String index, final String cfName, final String routing) throws JsonParseException, JsonMappingException, IOException {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.settingsService().getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        if (routing.startsWith("[") && routing.endsWith("]")) {
            // _routing is JSON array of values.
            Object[] elements = ClusterService.Utils.jsonMapper.readValue(routing, Object[].class);
            Object[] values = new Object[elements.length];
            String[] names = new String[elements.length];
            int i=0;
            for(ColumnDefinition cd : partitionColumns) {
                if (i > elements.length) 
                    throw new JsonMappingException("_routing "+routing+" does not match the partition key size="+partitionColumns.size());
                AbstractType<?> type = cd.type;
                names[i] = cd.name.toString();
                values[i] = type.compose( type.fromString(elements[i].toString()) );
                i++;
            }
            return new DocPrimaryKey(names, values) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> type = partitionColumns.get(0).type;
            return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { type.compose( type.fromString(routing) ) });
        }
    }
    
    @Override
    public boolean isStaticDocument(final String index, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.settingsService().getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        CFMetaData metadata = getCFMetaData(ksName, uid.type());
        String id = uid.id();
        if (id.startsWith("[") && id.endsWith("]")) {
            Object[] elements = ClusterService.Utils.jsonMapper.readValue(id, Object[].class);
            return metadata.clusteringColumns().size() > 0 && elements.length == metadata.partitionKeyColumns().size();
        } else {
            return metadata.clusteringColumns().size() != 0;
        }
    }
    
    @Override
    public Token getToken(ByteBuffer rowKey, ColumnFamily cf) {
        IPartitioner partitioner = StorageService.instance.getPartitioner();
        CType ctype = cf.metadata().getKeyValidatorAsCType();
        if (ctype.isCompound()) {
            Composite composite = ctype.fromByteBuffer(rowKey);
            ByteBuffer[] bb = new ByteBuffer[cf.metadata().partitionKeyColumns().size()];
            for (int i = 0; i < cf.metadata().partitionKeyColumns().size(); i++) {
                bb[i] = composite.get(i);
            }
            return partitioner.getToken(CompositeType.build(bb));
        }
        return partitioner.getToken(rowKey);
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
    
    public Token getToken(String index, String type, String routing) throws JsonParseException, JsonMappingException, IOException {
        DocPrimaryKey pk = parseElasticRouting(index, type, routing);
        IPartitioner partitioner = StorageService.instance.getPartitioner();
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.settingsService().getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        CFMetaData metadata = getCFMetaData(ksName, type);
        CBuilder builder = metadata.getKeyValidatorAsCType().builder();
        for (int i = 0; i < pk.values.length; i++) {
            builder.add(pk.values[i]);
        }
        return partitioner.getToken(builder.build().toByteBuffer());
    }
    

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
     * Create or alter elastic_admin keyspace with the DatacenterReplicationStrategy.
     */
    @Override
    public void createElasticAdminKeyspace()  {
        KSMetaData  elasticAdminKeyspace = Schema.instance.getKSMetaData(ClusterService.ELASTIC_ADMIN_KEYSPACE);
        if (elasticAdminKeyspace == null) {
            try {
                QueryProcessor.process(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : '%s', 'datacenters' : '%s' };", 
                        ELASTIC_ADMIN_KEYSPACE, DatacenterReplicationStrategy.class.getName(), DatabaseDescriptor.getLocalDataCenter()),
                        ConsistencyLevel.LOCAL_ONE);
                QueryProcessor.process(String.format("CREATE TABLE IF NOT EXISTS %s.\"%s\" ( cluster_name text PRIMARY KEY, owner uuid, version bigint, metadata text);", 
                        ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE),
                        ConsistencyLevel.LOCAL_ONE);
            } catch (RequestExecutionException e) {
                logger.error("Failed to create keyspace {}",ClusterService.ELASTIC_ADMIN_KEYSPACE, e);
            }
        } else {
            if (elasticAdminKeyspace.strategyClass != DatacenterReplicationStrategy.class) {
                throw new ConfigurationException(ELASTIC_ADMIN_KEYSPACE+" should use "+DatacenterReplicationStrategy.class.getName()+" replication strategy");
            }
            String datacentersOption = elasticAdminKeyspace.strategyOptions.get(DatacenterReplicationStrategy.DATACENTERS);
            if (datacentersOption == null) {
                throw new ConfigurationException("Missing "+DatacenterReplicationStrategy.DATACENTERS+" option in "+DatacenterReplicationStrategy.class.getName());
            }
            boolean localDcFound = false;
            for(String datacenter: datacentersOption.split(",")) {
                if (DatabaseDescriptor.getLocalDataCenter().equals(datacenter)) {
                    localDcFound = true;
                    break;
                }
            }
            if (!localDcFound) {
                try {
                    QueryProcessor.process(String.format("ALTER KEYSPACE %s WITH replication = { 'class' : '%s', 'datacenters' : '%s,%s' };", 
                            ELASTIC_ADMIN_KEYSPACE, DatacenterReplicationStrategy.class.getName(), datacentersOption, DatabaseDescriptor.getLocalDataCenter()),
                            ConsistencyLevel.LOCAL_ONE);
                    logger.info("Add local datacenter {} in {} for keyspace {}", 
                            DatabaseDescriptor.getLocalDataCenter(),DatacenterReplicationStrategy.class.getName(), ClusterService.ELASTIC_ADMIN_KEYSPACE);
                } catch (RequestExecutionException e) {
                    logger.error("Failed to alter keyspace {}",ClusterService.ELASTIC_ADMIN_KEYSPACE, e);
                    throw e;
                }
            }
        }
    }

    @Override
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException {
        CFMetaData cfm = getCFMetaData(ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE);
        cfm.comment(metaDataString);
        MigrationManager.announceColumnFamilyUpdate(cfm, false, false);
    }

    /**
     * Should only be used after a SCHEMA change.
     */
    @Override
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        try {
            CFMetaData cfm = getCFMetaData(ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE);
            String metadataString = cfm.getComment();
            if (metadataString != null && metadataString.length() > 0) {
                XContentParser xparser = new JsonXContentParser(new JsonFactory().createParser(metadataString));
                MetaData metaData = MetaData.Builder.fromXContent(xparser);
                if (logger.isDebugEnabled()) {
                    logger.debug("recovered metadata from {}.{} = {}", ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE, MetaData.Builder.toXContent(metaData));
                }
                return metaData;
            }
        } catch (Exception e) {
            throw new NoPersistedMetaDataException(e);
        } 
        throw new NoPersistedMetaDataException();
    }

    private MetaData parseMetaDataString(String metadataString) throws NoPersistedMetaDataException {
        if (metadataString != null && metadataString.length() > 0) {
            MetaData metaData;
            try {
                XContentParser xparser = new JsonXContentParser(new JsonFactory().createParser(metadataString));
                metaData = MetaData.Builder.fromXContent(xparser);
                if (logger.isTraceEnabled()) {
                    logger.trace("recovered metadata from {}.{} = {}", ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE, MetaData.Builder.toXContent(metaData));
                }
            } catch (IOException e) {
                throw new NoPersistedMetaDataException(e);
            }
            return metaData;
        }
        throw new NoPersistedMetaDataException();
    }

    private static final String selectMetadataQuery = String.format("SELECT metadata FROM \"%s\".\"%s\" WHERE cluster_name = ?", ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE);
    private static final String insertMetadataQuery = String.format("INSERT INTO \"%s\".\"%s\" (cluster_name,owner,version,metadata) VALUES (?,?,?,?) IF NOT EXISTS", ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE);
    private static final String updateMetaDataQuery = String.format("UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, metadata = ? WHERE cluster_name = ? IF version < ?", ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE);

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
            result = process(cl, selectMetadataQuery, DatabaseDescriptor.getClusterName());
            Row row = result.one();
            if (row != null && row.has("metadata")) {
                return parseMetaDataString(row.getString("metadata"));
            }
        } catch (UnavailableException e) {
            logger.warn("Cannot read metadata with consistency="+cl,e);
            return null;
        } catch (Exception e) {
            throw new NoPersistedMetaDataException(e);
        }
        throw new NoPersistedMetaDataException();
    }

    /**
     * Persisted metadata should not include number_of_shards nor number_of_replica.
     */
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
    
    
    
    @Override
    public void initializeMetaDataAsComment() {
        MetaData metadata = state().metaData();
        try {
            String metaDataString = MetaData.Builder.toXContent(metadata);
            // initialize a first row if needed
            UntypedResultSet result = process(ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.LOCAL_SERIAL, 
                    insertMetadataQuery,
                    DatabaseDescriptor.getClusterName(), UUID.fromString(StorageService.instance.getLocalHostId()), metadata.version(), metaDataString);
            Row row = result.one();
            boolean applied = false;
            if (row.has("[applied]")) {
                applied = row.getBoolean("[applied]");
            }
            if (applied) {
                logger.debug("Succefully initialize metadata metaData={}", metadata);
                writeMetaDataAsComment(metaDataString);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize persisted metadata", e);
        }
    }

    
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
        UntypedResultSet result = process(
                this.metadataWriteCL,
                this.metadataSerialCL,
                updateMetaDataQuery,
                new Object[] { UUID.fromString(localNode().id()), newMetaData.version(), metaDataString, DatabaseDescriptor.getClusterName(), newMetaData.version() });
        Row row = result.one();
        boolean applied = false;
        if (row.has("[applied]")) {
            applied = row.getBoolean("[applied]");
        }
        if (applied) {
            logger.debug("PAXOS Succefully update metadata source={} newMetaData={} in cluster {}", source, metaDataString, DatabaseDescriptor.getClusterName());
            writeMetaDataAsComment(metaDataString);
            return;
        } else {
            logger.warn("PAXOS Failed to update metadata source={} oldMetadata={}/{} currentMetaData={}/{} in cluster {}", 
                    source, oldMetaData.uuid(), oldMetaData.version(), row.getUUID("owner"), row.getLong("version"), DatabaseDescriptor.getClusterName());
            throw new ConcurrentMetaDataUpdateException(row.getUUID("owner"), row.getLong("version"));
        }
    }

    
    
    /**
     * Get indices shard state from gossip endpoints state map.
     * @param address
     * @param index
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public ShardRoutingState getShardRoutingState(final InetAddress address, final String index, ShardRoutingState defaultState) {
        return this.discoveryService.getShardRoutingState(address, index, defaultState);
    }

    /**
     * Return a set of remote started shards according t the gossip state map.
     * @param index
     * @return a set of remote started shards according t the gossip state map.
     */
    public Set<InetAddress> getStartedShard(String index) {
        return this.discoveryService.getStartedShard(index);
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
    
    /**
     * Publish on Gossip.X1 all local shard state.
     */
    public void publishAllShardsState() {
        for (IndexRoutingTable indexRoutingTable : state().routingTable()) {
            IndexShardRoutingTable indexShardRoutingTable = indexRoutingTable.shards().get(0);
            try {
                putShardRoutingState(indexRoutingTable.getIndex(), indexShardRoutingTable.getPrimaryShardRouting().state() );
            } catch (IOException e) {
                logger.error("Failed to publish primary shard state index=[{}]", indexRoutingTable.getIndex(), e);
            }
        }
    }

    
    /**
     * Block until recovery is done and all local shards are STARTED
     */
    public void waitShardsStarted() {
        logger.debug("Waiting until all local primary shards are STARTED");
        final CountDownLatch latch = new CountDownLatch(1);
        final ClusterStateObserver observer = new ClusterStateObserver(this, logger);
        observer.waitForNextChange(new ClusterStateObserver.Listener() {
            @Override
            public void onTimeout(TimeValue timeout) {
            }
            
            @Override
            public void onNewClusterState(ClusterState state) {
                logger.debug("Ok, all local primary shards are STARTED, state={}", state().prettyPrint());
                latch.countDown();
            }
            
            @Override
            public void onClusterServiceClose() {
                logger.warn("Cluster service closed", state().prettyPrint());
                latch.countDown();
            }
        }, new EventPredicate() {
            @Override
            public boolean apply(ClusterChangedEvent changedEvent) {
                logger.debug("Cluster state change isLocalShardsStarted={}",state().routingTable().isLocalShardsStarted());
                return (!changedEvent.state().blocks().hasGlobalBlock(GatewayService.STATE_NOT_RECOVERED_BLOCK) && changedEvent.state().routingTable().isLocalShardsStarted());
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupred before local shards started", e);
        }
    }


}
