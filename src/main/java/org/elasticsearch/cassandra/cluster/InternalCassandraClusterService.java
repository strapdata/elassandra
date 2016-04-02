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
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.composites.CType;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.DoubleType;
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
import org.apache.cassandra.serializers.MapSerializer;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.messages.ResultMessage;
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
import org.elasticsearch.cassandra.ElasticSecondaryIndex;
import org.elasticsearch.cassandra.NoPersistedMetaDataException;
import org.elasticsearch.cassandra.SchemaService;
import org.elasticsearch.cassandra.SecondaryIndicesService;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
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
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContent.Params;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.gateway.GatewayService;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.DocumentAlreadyExistsException;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentFieldMappers;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.CqlCollection;
import org.elasticsearch.index.mapper.Mapper.CqlStruct;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.SourceToParse;
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
import com.google.common.collect.ImmutableSet;
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

    private final IndicesService indicesService;
    private final DiscoveryService discoveryService;
    private final TimeValue mappingUpdateTimeout;

    protected final MappingUpdatedAction mappingUpdatedAction;
    
    public Map<String, String> cqlMapping = new ImmutableMap.Builder<String,String>()
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
    

    public Map<Class, String> mapperToCql = new java.util.HashMap<Class, String>() {
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

    public boolean isNativeCql3Type(String cqlType) {
        return cqlMapping.keySet().contains(cqlType) && !cqlType.startsWith("geo_");
    }

    @Inject
    public InternalCassandraClusterService(Settings settings, DiscoveryService discoveryService, OperationRouting operationRouting, TransportService transportService, NodeSettingsService nodeSettingsService,
            ThreadPool threadPool, ClusterName clusterName, DiscoveryNodeService discoveryNodeService, Version version, 
            SecondaryIndicesService secondaryIndicesService, IndicesService indicesService, MappingUpdatedAction mappingUpdatedAction) {
        super(settings, discoveryService, operationRouting, transportService, nodeSettingsService, threadPool, clusterName, discoveryNodeService, version, secondaryIndicesService, indicesService);
        this.mappingUpdateTimeout = settings.getAsTime(MAPPING_UPDATE_TIMEOUT, TimeValue.timeValueSeconds(30));
        this.indicesService = indicesService;
        this.discoveryService = discoveryService;
        this.mappingUpdatedAction = mappingUpdatedAction;
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
            UntypedResultSet result = QueryProcessor.executeInternal("SELECT validator FROM system.schema_columns WHERE columnfamily_name = ? AND column_name = ?", 
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
            return mapper.fieldType().value(value);
        }
    }

    public ByteBuffer serialize(final String ksName, final String cfName, final AbstractType type, final String name, final Object value, final Mapper mapper) 
            throws SyntaxException, ConfigurationException {
        if (value == null)
            return null;

        if (type instanceof UserType) {
            List<ByteBuffer> components = new ArrayList<ByteBuffer>();
            UserType udt = (UserType) type;
            Map<String, Object> mapValue = (Map<String, Object>) value;
            for (int j = 0; j < udt.size(); j++) {
                String subName = UTF8Type.instance.compose(udt.fieldName(j));
                AbstractType<?> subType = udt.fieldType(j);
                Mapper subMapper = ((ObjectMapper) mapper).getMapper(subName);
                Object subValue = mapValue.get(subName);
                components.add(serialize(ksName, cfName, subType, subName, subValue, subMapper));
            }
            return TupleType.buildValue(components.toArray(new ByteBuffer[components.size()]));
        } else if (type instanceof ListType) {
            if (!(value instanceof List)) {
                if (value instanceof Map) {
                    // build a singleton list of UDT 
                    return type.decompose(ImmutableList.of( serializeMapAsUDT(ksName, cfName, name, (Map<String, Object>) value, mapper) ) );
                } else {
                    return type.decompose(ImmutableList.of( value((FieldMapper) mapper, value) ));
                }
            } else {
                return type.decompose(value);
            }
        } else {
            // Native cassandra type,
            return type.decompose( value((FieldMapper) mapper, value) );
        }
    }

    public ByteBuffer serializeMapAsMap(final String ksName, final String cfName, final String fieldName, final Map<String, Object> source, final ObjectMapper mapper) 
            throws SyntaxException, ConfigurationException 
    {
        MapType mapType = getMapType(ksName, cfName, fieldName);
        MapSerializer serializer = mapType.getSerializer();
        List<ByteBuffer> buffers = serializer.serializeValues(source);
        return TupleType.buildValue(buffers.toArray(new ByteBuffer[buffers.size()]));
    }
    
    public ByteBuffer serializeMapAsUDT(final String ksName, final String cfName, final String fieldName, final Map<String, Object> source, final Mapper mapper) 
            throws SyntaxException, ConfigurationException 
    {
        List<ByteBuffer> components = new ArrayList<ByteBuffer>();
        
        if (mapper instanceof GeoPointFieldMapper) {
            components.add( DoubleType.instance.decompose( (Double) source.get("lat") ) );
            components.add( DoubleType.instance.decompose( (Double) source.get("lon") ) );
        } else if (mapper instanceof ObjectMapper) {
            Pair<List<String>, List<String>> udtInfo = getUDTInfo(ksName, cfName + '_' + fieldName);
            if (udtInfo == null) {
                throw new ConfigurationException("User Defined Type not found:"+ksName+"."+cfName + '_' + fieldName);
            }
            for (int i = 0; i < udtInfo.left.size(); i++) {
                String fname = udtInfo.left.get(i);
                AbstractType type = TypeParser.parse(udtInfo.right.get(i));
                Object field_value = source.get(fname);
                components.add(serialize(ksName, cfName, type, fieldName+'_'+fname, field_value, ((ObjectMapper) mapper).getMapper(fname)));
            }  
        }
        return TupleType.buildValue(components.toArray(new ByteBuffer[components.size()]));
    }
    
    /**
     * Recursive build UDT value according to the input map.
     * 
     * @param ksName
     * @param cfName
     * @param fieldName
     * @param valueMap
     * @param objectMapper
     * @return ByteBuffer or List<ByteBuffer>
     * @throws ConfigurationException
     * @throws SyntaxException
     */
    public Object serializeUDT(final String ksName, final String cfName, final String fieldName, final Object source, final Mapper mapper) throws SyntaxException, ConfigurationException,
            MapperParsingException {

        if (source instanceof Map) {
            // elasticsearch object type = cassandra UDT
            ByteBuffer bb = serializeMapAsUDT(ksName, cfName, fieldName, (Map<String, Object>) source, mapper);
            if (mapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                return bb;
            } else if (mapper.cqlCollection().equals(CqlCollection.LIST)) {
                return ImmutableList.of(bb);
            } else if (mapper.cqlCollection().equals(CqlCollection.SET)) {
                return ImmutableSet.of(bb);
            } 
        } else if (source instanceof List) {
            // elasticsearch object array = cassandra list<UDT>
            if (mapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                throw new MapperParsingException("field " + mapper.name() + " should be a single value");
            } else if (mapper.cqlCollection().equals(CqlCollection.LIST)) {
                List<ByteBuffer> components = new ArrayList<ByteBuffer>();
                for (Object sourceObject : (List) source) {
                    ByteBuffer bb = serializeMapAsUDT(ksName, cfName, fieldName, (Map<String, Object>) sourceObject, mapper);
                    components.add(bb);
                }
                return components;
            } else if (mapper.cqlCollection().equals(CqlCollection.SET)) {
                Set<ByteBuffer> components = new HashSet<ByteBuffer>();
                for (Object sourceObject : (List) source) {
                    ByteBuffer bb = serializeMapAsUDT(ksName, cfName, fieldName, (Map<String, Object>) sourceObject, mapper);
                    components.add(bb);
                }
                return components;
            }
        }
        throw new MapperParsingException("unexpected object=" + source + " mapper=" + mapper.name());
    }

    public String buildCql(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {
        if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
            return buildUDT(ksName, cfName, name, objectMapper);
        } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
            Mapper childMapper = objectMapper.iterator().next();
            if (childMapper instanceof FieldMapper) {
                return "map<text,"+mapperToCql.get(childMapper.getClass())+">";
            } else if (childMapper instanceof ObjectMapper) {
                return "map<text,frozen<"+buildCql(ksName,cfName,childMapper.simpleName(),(ObjectMapper)childMapper)+">>";
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
                buildGeoPointType(ksName, typeName+'_'+mapper.simpleName());
            } 
        }

        Pair<List<String>, List<String>> udt = getUDTInfo(ksName, typeName);
        if (udt == null) {
            // create new UDT.
            // TODO: Support dynamic mapping by altering UDT
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
                        .append(typeName).append('_').append(shortName)
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
                            .append(typeName).append('_').append(shortName)
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

    public void buildGeoPointType(String ksName, String typeName) throws RequestExecutionException {
        String query = String.format("CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( %s double, %s double )", ksName, typeName,Names.LAT,Names.LON);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }

    public void buildGeoShapeType(String ksName, String typeName) {
        // TODO: Implements geo_shape.
    }
    
   
    public void buildObjectMapping(Map<String, Object> mapping, final AbstractType<?> type) throws IOException {
        CQL3Type cql3type = type.asCQL3Type();
        if (cql3type instanceof CQL3Type.Native) {
            mapping.put("type", cqlMapping.get(cql3type.toString()));
        } else if (cql3type instanceof CQL3Type.UserDefined) {
            mapping.put("type", ObjectMapper.NESTED_CONTENT_TYPE);
            mapping.put(TypeParsers.CQL_STRUCT, "udt");
            Map<String, Object> properties = Maps.newHashMap();
            TupleType tuple = (TupleType)type;
            for(int i=0; i< tuple.size(); i++) {
                buildTypeMapping(properties, tuple.type(i));
            }
            mapping.put("properties", properties);
        }
    }
    
    public void buildTypeMapping(Map<String, Object> mapping, final AbstractType<?> type) throws IOException {
        CQL3Type cql3type = type.asCQL3Type();
        if (type.isCollection()) {
            if (type instanceof ListType) {
                mapping.put(TypeParsers.CQL_COLLECTION, "list");
                buildObjectMapping(mapping, ((ListType<?>)type).getElementsType() );
            } else if (type instanceof SetType) {
                mapping.put(TypeParsers.CQL_COLLECTION, "set");
                buildObjectMapping(mapping, ((SetType<?>)type).getElementsType() );
            } else if (type instanceof MapType) {
                MapType mtype = (MapType)type;
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
            buildObjectMapping(mapping, type );
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
                UntypedResultSet result = QueryProcessor.executeInternal("SELECT  column_name,validator FROM system.schema_columns WHERE keyspace_name=? and columnfamily_name=?", 
                        new Object[] { ksName, cfName });
                for (Row row : result) {
                    if (row.has("validator") && pattern.matcher(row.getString("column_name")).matches() && !row.getString("column_name").startsWith("_")) {
                        String columnName = row.getString("column_name");
                        Map<String,Object> props = (Map<String, Object>) properties.get(columnName);
                        if (props == null) {
                            props = Maps.newHashMap();
                            properties.put(columnName, props);
                        }
                        AbstractType<?> type =  TypeParser.parse(row.getString("validator"));
                        buildTypeMapping(props, type);
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
        for(Iterator<IndexMetaData> it = currentState.metaData().indices().valuesIt(); it.hasNext(); ) {
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
            StringBuilder readOnUpdateColumnList = new StringBuilder();
            Map<String,Boolean> columnsMap = new HashMap<String, Boolean>(columns.size());
            for (String column : columns) {
                columnsList.append(',');
                if (column.equals("_token")) {
                    continue; // ignore pseudo column known by Elasticsearch
                }
                String cqlType = null;
                FieldMapper fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                if (fieldMapper != null) {
                    columnsMap.put(column,  fieldMapper.cqlPartialUpdate());
                    if (fieldMapper instanceof GeoPointFieldMapper) {
                        cqlType = fieldMapper.name().replace('.', '_');
                    } else {
                        cqlType = mapperToCql.get(fieldMapper.getClass());
                    }
                    
                    if (!fieldMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        if (isNativeCql3Type(cqlType)) {
                            cqlType = fieldMapper.cqlCollectionTag()+"<" + cqlType + ">";
                        } else {
                            cqlType = fieldMapper.cqlCollectionTag()+"<frozen<" + cqlType + ">>";
                        }
                    }
                } else {
                    ObjectMapper objectMapper = docMapper.objectMappers().get(column);
                    columnsMap.put(column,  objectMapper.cqlPartialUpdate());
                    if (objectMapper != null) {
                        if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
                            // TODO: check columnName exists and is map<text,?>
                            logger.debug("Expecting column [{}] to be a map<text,?>", column);
                        } else  if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                            // Cassandra 2.1.8 : Non-frozen collections are not allowed inside collections
                            cqlType = "frozen<\"" + buildCql(ksName, type, column, objectMapper) + "\">";
                            if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                                cqlType = objectMapper.cqlCollectionTag()+"<"+cqlType+">";
                            }
                        }
                    } else {
                        logger.warn("Cannot infer CQL type mapping for field [{}]", column);
                        continue;
                    }
                }

                
                if (newTable) {
                    if (cqlType != null) {
                        columnsList.append("\"").append(column).append("\" ").append(cqlType);
                    }
                } else {
                    ColumnDefinition cdef = cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                    if (cqlType != null) {
                        if (cdef == null) {
                            try {
                                String query = String.format("ALTER TABLE \"%s\".\"%s\" ADD \"%s\" %s", ksName, type, column, cqlType);
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
                                if (!cdef.type.asCQL3Type().toString().equals(cqlType)) {
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
                    // _parent is a JSON arry representation of the parent PK.
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
                String query = String.format("CREATE TABLE IF NOT EXISTS \"%s\".\"%s\" ( \"%s\" text PRIMARY KEY %s ) WITH COMMENT='Auto-created by Elassandra'", ksName, type,
                        ELASTIC_ID_COLUMN_NAME, columnsList);
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
        IndexMetaData indexMetaData = state().metaData().index(index);
        for(ObjectCursor<MappingMetaData> cursor: indexMetaData.getMappings().values()) {
            createSecondaryIndex(ksName, cursor.value);
        }
    }
   
    // build secondary indices when shard is started and mapping applied
    @Override
    public void createSecondaryIndex(String ksName, MappingMetaData mapping) throws IOException {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksName, mapping.type());
        if (cfm != null) {
            try {
                for (String column : ((Map<String,Object>)mapping.sourceAsMap().get("properties")).keySet()) {
                    if (column.equals("_token")) {
                        continue; // ignore pseudo column known by Elasticsearch
                    }
                    ColumnDefinition cdef = (cfm == null) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                    if ((cdef != null) && !cdef.isIndexed() && !(cfm.partitionKeyColumns().size()==1 && cdef.kind == ColumnDefinition.Kind.PARTITION_KEY )) {
                        logger.debug("CREATE CUSTOM INDEX IF NOT EXISTS {} ON {}.{} ({})", buildIndexName(mapping.type(), column), ksName, mapping.type(), column);
                        QueryProcessor.process(String.format("CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" (\"%s\") USING '%s'",
                            buildIndexName(mapping.type(), column), ksName, mapping.type(), column, org.elasticsearch.cassandra.ElasticSecondaryIndex.class.getName()), 
                            ConsistencyLevel.LOCAL_ONE);
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
                if (cdef.isIndexed() && ElasticSecondaryIndex.class.getName().equals(cdef.getIndexOptions().get(ElasticSecondaryIndex.CUSTOM_INDEX_OPTION_NAME))) {
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
    
    

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#getPrimaryKeyColumns
     * (java.lang.String, java.lang.String)
     */
    @Override
    public List<ColumnDefinition> getPrimaryKeyColumns(final String ksName, final String cfName) throws ConfigurationException {
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        ImmutableList.Builder<ColumnDefinition> builder = new ImmutableList.Builder<ColumnDefinition>().addAll(metadata.partitionKeyColumns());
        List<ColumnDefinition> clusterKeys = metadata.clusteringColumns();
        if (clusterKeys != null)
            builder.addAll(metadata.clusteringColumns());
        return builder.build();
    }



    
    public void buildPrimaryKeyFragment(final String ksName, final String cfName, StringBuilder ptCols, StringBuilder pkCols, StringBuilder pkWhere) throws ConfigurationException {
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        boolean first = true;
        for (ColumnDefinition cd : metadata.partitionKeyColumns()) {
            if (ptCols != null && ptCols.length() > 0) {
                ptCols.append(',');
                if (pkCols != null) pkCols.append(',');
                if (pkWhere != null) pkWhere.append(" AND ");
            }
            if (ptCols != null) ptCols.append('\"').append(cd.name.toString()).append('\"');
            if (pkCols != null) pkCols.append('\"').append(cd.name.toString()).append('\"');
            if (pkWhere != null) pkWhere.append('\"').append(cd.name.toString()).append("\" = ?");
        }
        for (ColumnDefinition cd : metadata.clusteringColumns()) {
            if (pkCols != null && pkCols.length() > 0) {
                pkCols.append(',');
                if (pkWhere != null) pkWhere.append(" AND ");
            }
            if (pkCols != null) pkCols.append('\"').append(cd.name.toString()).append('\"');
            if (pkWhere != null) pkWhere.append('\"').append(cd.name.toString()).append("\" = ?");
        }
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

    
    public void buildPartitionKeyFragment(final String ksName, final String cfName, StringBuilder pkCols) throws ConfigurationException {
        List<ColumnDefinition> pkColumns = getCFMetaData(ksName, cfName).partitionKeyColumns();
        for (ColumnDefinition cd : pkColumns) {
            if (pkCols.length() > 0) {
                pkCols.append(',');
            }
            pkCols.append('\"').append(cd.name.toString()).append('\"');
        }
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
    
    /**
     * Return list of columns having an ElasticSecondaryIndex.
     */
    public Collection<String> mappedColumns(final String index, final String type)  {
        IndexMetaData indexMetaData = state().metaData().index(index);
        if (indexMetaData != null) {
            MappingMetaData mappingMetaData = indexMetaData.mapping(type);
            if (mappingMetaData != null) {
                try {
                    Set<String> set = ((Map<String, Object>)mappingMetaData.sourceAsMap().get("properties")).keySet();
                    Collection<String> cols = new ArrayList<String>(set.size()+1);
                    for(String s : set) {
                        int x = s.indexOf('.');
                        cols.add( (x > 0) ? s.substring(0,x) : s );
                    }
                    //cols.add(TokenFieldMapper.NAME);
                    return cols;
                } catch (IOException e) {
                }
            }
        }
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public String[] mappedColumns(MapperService mapperService, String type) {
        Collection<String> cols = mappedColumns(mapperService.index().getName(), type);
        return cols.toArray(new String[cols.size()]);
    }
    
    
    
    
    @Override
    public UntypedResultSet fetchRow(final String index, final String type, final String id) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        return fetchRow(index, type, mappedColumns(index, type), id, ConsistencyLevel.LOCAL_ONE);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#fetchRow(java.lang.String
     * , java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public UntypedResultSet fetchRow(final String index, final String type, final Collection<String> requiredColumns, final String id) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException {
        return fetchRow(index, type, requiredColumns, id, ConsistencyLevel.LOCAL_ONE);
    }

    /**
     * Fetch from the coordinator node.
     */
    @Override
    public UntypedResultSet fetchRow(final String index, final String cfName, final Collection<String> requiredColumns, final String id, final ConsistencyLevel cl) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        return process(cl, buildFetchQuery(index,cfName,requiredColumns), parseElasticId(index, cfName, id));
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
    public UntypedResultSet fetchRowInternal(final String index, final String cfName, final Collection<String> requiredColumns, final String id) throws ConfigurationException, IOException  {
        return fetchRowInternal(index, cfName, requiredColumns, parseElasticId(index, cfName, id));
    }
    
    @Override
    public UntypedResultSet fetchRowInternal(final String index, final String cfName, final Collection<String> requiredColumns, final Object[] pkColumns) throws ConfigurationException, IOException  {
        return QueryProcessor.instance.executeInternal(buildFetchQuery(index,cfName,requiredColumns), pkColumns);
    }
    
    public String buildFetchQuery(final String index, final String cfName, final Collection<String> requiredColumns) throws ConfigurationException {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        StringBuilder ptColums = new StringBuilder();
        StringBuilder pkColums = new StringBuilder();
        StringBuilder pkWhere = new StringBuilder();
        
        buildPrimaryKeyFragment(ksName, cfName, ptColums, pkColums, pkWhere);
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        for (String c : requiredColumns) {
            if (query.length() > 7)
                query.append(',');
            if (c.equals("_token")) {
                query.append("token(").append(ptColums).append(") as \"_token\"");
            } else {
                query.append("\"").append(c).append("\"");
            }
        }
        query.append(" FROM \"").append(ksName).append("\".\"").append(cfName).append("\" WHERE ").append(pkWhere);
        return query.toString();
    }

    @Override
    public void deleteRow(final String ksName, final String cfName, final String id, final ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException {
        StringBuilder pkWhere = new StringBuilder();
        buildPrimaryKeyFragment(ksName, cfName, null, null, pkWhere);

        String query = String.format("DELETE FROM \"%s\".\"%s\" WHERE %s", new Object[] { ksName, cfName, pkWhere });
        process(cl, query, parseElasticId(ksName, cfName, id));
    }
    
    @Override
    public Map<String, Object> rowAsMap(final String index, final String type, UntypedResultSet.Row row) throws IOException {
        Map<String, Object> mapObject = new HashMap<String, Object>();
        rowAsMap(index, type, row, mapObject);
        return mapObject;
    }

    @Override
    public int rowAsMap(final String index, final String type, UntypedResultSet.Row row, Map<String, Object> mapObject) throws IOException {
        int putCount = 0;
        List<ColumnSpecification> columnSpecs = row.getColumns();
        
        IndexService indexService = indicesService.indexServiceSafe(index);
        DocumentMapper documentMapper = indexService.mapperService().documentMapper(type);
        
        for (int columnIndex = 0; columnIndex < columnSpecs.size(); columnIndex++) {
            ColumnSpecification colSpec = columnSpecs.get(columnIndex);
            String columnName = colSpec.name.toString();
            CQL3Type cql3Type = colSpec.type.asCQL3Type();
            
            FieldMapper fieldMapper = documentMapper.mappers().smartNameFieldMapper(columnName);
            if (fieldMapper == null && columnName.equals("_token")) {
                fieldMapper = documentMapper.tokenFieldMapper();
            }
            
            if (!row.has(colSpec.name.toString()))
                continue;

            if (cql3Type instanceof CQL3Type.Native) {
                switch ((CQL3Type.Native) cql3Type) {
                case ASCII:
                case TEXT:
                case VARCHAR:
                    mapObject.put(columnName, row.getString(colSpec.name.toString()));
                    putCount++;
                    break;
                case TIMEUUID:
                case UUID:
                    mapObject.put(columnName, row.getUUID(colSpec.name.toString()).toString() );
                    putCount++;
                    break;
                case TIMESTAMP:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getTimestamp(colSpec.name.toString()).getTime()));
                    putCount++;
                    break;
                case INT:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getInt(colSpec.name.toString())) );
                    putCount++;
                    break;
                case BIGINT:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getLong(colSpec.name.toString())) );
                    putCount++;
                    break;
                case DECIMAL:
                case DOUBLE:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getDouble(colSpec.name.toString())));
                    putCount++;
                    break;
                case BLOB:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getBytes(colSpec.name.toString())));
                    putCount++;
                    break;
                case BOOLEAN:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getBoolean(colSpec.name.toString())));
                    putCount++;
                    break;
                case COUNTER:
                    break;
                case INET:
                    mapObject.put(columnName, fieldMapper.fieldType().valueForSearch( row.getInetAddress(colSpec.name.toString()).getHostAddress()));
                    putCount++;
                    break;
                default:
                    logger.warn("Ignoring unsupported type {}", cql3Type);
                }
            } else if (cql3Type.isCollection()) {
                AbstractType<?> elementType;
                switch (((CollectionType<?>) colSpec.type).kind) {
                case LIST:
                    List list;
                    elementType = ((ListType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        List<ByteBuffer> lbb = row.getList(colSpec.name.toString(), BytesType.instance);
                        list = new ArrayList<Map<String, Object>>(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            list.add(ElasticSecondaryIndex.deserialize(elementType, bb));
                        }
                    } else {
                        list = row.getList(colSpec.name.toString(), elementType);
                    }
                    mapObject.put(columnName, (list.size() == 1) ? list.get(0) : list);
                    putCount++;
                    break;
                case SET :
                    Set set;
                    elementType = ((SetType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        Set<ByteBuffer> lbb = row.getSet(colSpec.name.toString(), BytesType.instance);
                        set = new HashSet<Map<String, Object>>(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            set.add(ElasticSecondaryIndex.deserialize(elementType, bb));
                        }
                    } else {
                        set = row.getSet(colSpec.name.toString(), elementType);
                    }
                    mapObject.put(columnName, (set.size() == 1) ? set.iterator().next() : set);
                    putCount++;
                    break;
                case MAP :
                    Map map;
                    if (((MapType<?,?>) colSpec.type).getKeysType().asCQL3Type() != CQL3Type.Native.TEXT) {
                        throw new IOException("Only support map<text,?>, bad type for column "+columnName);
                    }
                    UTF8Type keyType = (UTF8Type) ((MapType<?,?>) colSpec.type).getKeysType();
                    elementType = ((MapType<?,?>) colSpec.type).getValuesType();
                    if (elementType instanceof UserType) {
                        Map<String, ByteBuffer> lbb = row.getMap(colSpec.name.toString(), keyType, BytesType.instance);
                        map = new HashMap<String , Map<String, Object>>(lbb.size());
                        for(String key : lbb.keySet()) {
                            map.put(key,ElasticSecondaryIndex.deserialize(elementType, lbb.get(key)));
                        }
                    } else {
                        map = row.getMap(colSpec.name.toString(), keyType, elementType);
                    }
                    mapObject.put(columnName, map);
                    putCount++;
                    break;
                }
            } else if (colSpec.type instanceof UserType) {
                ByteBuffer bb = row.getBytes(colSpec.name.toString());
                mapObject.put(columnName, ElasticSecondaryIndex.deserialize(colSpec.type, bb));
                putCount++;
            } else if (cql3Type instanceof CQL3Type.Custom) {
                logger.warn("CQL3.Custum type not supported for column "+columnName);
            }
        }
        return putCount;
    }

    

    private class MappingUpdateListener implements ActionListener<ClusterStateUpdateResponse> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Throwable error = null;

        @Override
        public void onFailure(Throwable t) {
            error = t;
            latch.countDown();
        }

        public void waitForMappingUpdate(TimeValue timeValue) throws Exception {
            if (timeValue.millis() > 0) {
                if (!latch.await(timeValue.millis(), TimeUnit.MILLISECONDS)) {
                    throw new ElasticsearchTimeoutException("blocking mapping update timeout");
                }
            } else {
                latch.await();
            }
            if (error != null)
                throw new RuntimeException(error);
            logger.debug("mapping updated");
        }

        @Override
        public void onResponse(ClusterStateUpdateResponse response) {
            latch.countDown();
        }
    }

    
    @Override
    public void blockingMappingUpdate(IndexService indexService, String type, CompressedXContent source) throws Exception {
        MappingUpdateListener mappingUpdateListener = new MappingUpdateListener();
        MetaDataMappingService metaDataMappingService = ElassandraDaemon.injector().getInstance(MetaDataMappingService.class);
        metaDataMappingService.updateMapping(indexService.index().name(), indexService.indexUUID(), type, source, localNode().id(), mappingUpdateListener, mappingUpdateTimeout, mappingUpdateTimeout);
        mappingUpdateListener.waitForMappingUpdate(mappingUpdateTimeout);
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

        IndexService indexService = indicesService.indexServiceSafe(request.index());
        IndexShard indexShard = indexService.shardSafe(0);
        SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, request.source()).type(request.type()).id(request.id()).routing(request.routing()).parent(request.parent())
                .timestamp(request.timestamp()).ttl(request.ttl());

        Engine.IndexingOperation operation = indexShard.prepareIndex(sourceToParse, request.version(), request.versionType(), Engine.Operation.Origin.PRIMARY, request.canHaveDuplicates());
        Mapping update = operation.parsedDoc().dynamicMappingsUpdate();
        if (update != null) {
            if (logger.isDebugEnabled()) 
                logger.debug("Document source={} require a blocking mapping update of [{}]", request.sourceAsMap(), indexService.index().name());
            // blocking Elasticsearch mapping update (required to update cassandra schema before inserting a row, this is the cost of dynamic mapping)
            blockingMappingUpdate(indexService, request.type(), new CompressedXContent(update.toString()) );
        }

        // get the docMapper after a potential mapping update
        DocumentMapper docMapper = indexShard.mapperService().documentMapperWithAutoCreate(request.type()).getDocumentMapper();
        
        // insert document into cassandra keyspace=index, table = type
        Map<String, Object> sourceMap = request.sourceAsMap();
        Map<String, ObjectMapper> objectMappers = docMapper.objectMappers();
        DocumentFieldMappers fieldMappers = docMapper.mappers();

        Long timestamp = null;
        if (docMapper.timestampFieldMapper().enabled() && timestampString != null) {
            timestamp = docMapper.timestampFieldMapper().fieldType().value(timestampString);
        }
        
        if (logger.isTraceEnabled()) 
            logger.trace("Insert index=[{}] id=[{}] source={} fieldMappers={} objectMappers={} consistency={} ttl={}", 
                indexService.index().name(), request.id(), sourceMap,Lists.newArrayList(fieldMappers.iterator()), objectMappers, 
                request.consistencyLevel().toCassandraConsistencyLevel(), request.ttl());


        Map<String, Object> map = new HashMap<String, Object>();
        if (request.parent() != null) {
            map.put("_parent", request.parent());
        }
        for (String field : sourceMap.keySet()) {
            if (field.equals("_token")) continue;
            Object fieldValue = sourceMap.get(field);
            try {
                ObjectMapper objectMapper = objectMappers.get(field);
                FieldMapper fieldMapper = fieldMappers.smartNameFieldMapper(field);
                if (fieldValue == null) {
                    Mapper mapper = (fieldMapper != null) ? fieldMapper : objectMapper;
                    // Can insert null value only when mapping is known
                    if (mapper != null) {
                        if (fieldMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                            map.put(field, null);
                        } else if (fieldMapper.cqlCollection().equals(CqlCollection.SET)) {
                            map.put(field, ImmutableSet.of());
                        } else {
                            map.put(field, ImmutableList.of());
                        }
                    } 
                    continue;
                }

                if (objectMapper != null) {
                    if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                        map.put(field, serializeUDT(request.index(), request.type(), field, fieldValue, objectMapper));
                    } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
                        map.put(field, serializeMapAsMap(request.index(), request.type(), field, (Map<String,Object>)fieldValue, objectMapper));
                    }
                } else if (fieldMapper != null) {
                    if (fieldMapper.cqlCollection().equals(CqlCollection.SINGLETON) && (fieldValue instanceof Collection)) {
                        throw new MapperParsingException("field " + fieldMapper.name() + " should be a single value");
                    }
                    if (fieldMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        map.put(field, value(fieldMapper, fieldMapper.fieldType().value(fieldValue)));
                    } else if (fieldMapper.cqlCollection().equals(CqlCollection.LIST)) {
                        List<Object> fieldValueList2 = new ArrayList();
                        if (fieldValue instanceof List) {
                            List<Object> fieldValueList = (List) fieldValue;
                            for (Object o : fieldValueList) {
                                fieldValueList2.add(value(fieldMapper, fieldMapper.fieldType().value(o)));
                            }
                        } else {
                            fieldValueList2.add(value(fieldMapper, fieldValue));
                        }
                        map.put(field, fieldValueList2);
                    } else if (fieldMapper.cqlCollection().equals(CqlCollection.SET)) {
                        Set<Object> fieldValueList2 = new HashSet<Object>();
                        if (fieldValue instanceof List) {
                            List<Object> fieldValueList = (List) fieldValue;
                            for (Object o : fieldValueList) {
                                fieldValueList2.add(value(fieldMapper, fieldMapper.fieldType().value(o)));
                            }
                        } else {
                            fieldValueList2.add(value(fieldMapper, fieldValue));
                        }
                        map.put(field, fieldValueList2);
                    }
                } else {
                    logger.warn("[{}].[{}] ignoring unmapped field [{}] = [{}] ", request.index(), request.type(), field, fieldValue);
                }
            } catch (Exception e) {
                logger.warn("[{}].[{}] failed to parse field {}={} => ignoring", e, request.index(), request.type(), field, fieldValue );
            }
        }

        String keyspaceName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, request.index());
        boolean applied = insertRow(keyspaceName, request.type(), map, request.id(),
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
    @Override
    public boolean insertRow(final String ksName, final String cfName, Map<String, Object> map, String id, final boolean ifNotExists, final long ttl, final ConsistencyLevel cl,
            Long writetime) throws Exception {
        
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        // if the provided columns does not contains all the primary key columns, parse the _id to populate the columns in map.
        boolean buildId = true;
        ArrayNode array = SchemaService.Utils.jsonMapper.createArrayNode();
        for(ColumnDefinition cd: Iterables.concat(metadata.partitionKeyColumns(), metadata.clusteringColumns())) {
            if (map.keySet().contains(cd.name.toString())) {
                SchemaService.Utils.addToJsonArray(cd.type, map.get(cd.name.toString()), array);
            } else {
                buildId = false;
                parseElasticId(ksName, cfName, id, map);
            }
        }
        if (buildId) {
            id = SchemaService.Utils.writeValueAsString(array);
        }
        
        StringBuilder questionsMarks = new StringBuilder();
        StringBuilder columnNames = new StringBuilder();
        Object[] values = new Object[map.size()];
        int i=0;
        for (Entry<String,Object> entry : map.entrySet()) {
            if (entry.getKey().equals("_token")) continue;
            if (columnNames.length() > 0) {
                columnNames.append(',');
                questionsMarks.append(',');
            }
            columnNames.append("\"").append(entry.getKey()).append("\"");
            questionsMarks.append('?');
            values[i++] = entry.getValue();
        }
        
        StringBuilder query = new StringBuilder();
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

    

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#index(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.Object[])
     */
    /*
    @Override
    public void index(final String index, final String type, final String id, Object[] sourceData) {
        ActionListener<XIndexResponse> listener = new ActionListener<XIndexResponse>() {
            @Override
            public void onResponse(XIndexResponse response) {
                logger.debug("row indexed id=" + response.getId() + " version=" + response.getVersion() + " created=" + response.isCreated());
            }

            @Override
            public void onFailure(Throwable e) {
                logger.error("failed to index row id=" + id, e);
            }
        };

        // submit an index request for all target index.types in options.
        logger.debug("indexing in {}.{} source={}", index, type, Arrays.toString(sourceData));
        XIndexRequest request = new XIndexRequest(index, type, id);
        request.source(sourceData);
        ElassandraDaemon.client().xindex(request, listener);
    }
    */

    public Object[] parseElasticId(final String index, final String cfName, final String id) throws IOException {
        return parseElasticId(index, cfName, id, null);
    }
    
    /**
     * Parse _id (something like (xa,b,c)) to build a primary key array or populate map.
     * @param ksName
     * @param cfName
     * @param map
     * @param id
     */
    public Object[] parseElasticId(final String index, final String cfName, final String id, Map<String, Object> map) throws IOException {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.settingsService().getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME,index);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        List<ColumnDefinition> clusteringColumns = metadata.clusteringColumns();
        int pkSize = partitionColumns.size()+clusteringColumns.size();
        if (pkSize > 1) {
            if (id.startsWith("[") && id.endsWith("]")) {
                // _id is JSON array
                Object[] keys = SchemaService.Utils.jsonMapper.readValue(id, Object[].class);
                Object[] objects = (map != null) ? null : new Object[pkSize];
                int i=0;
                for(ColumnDefinition cd : Iterables.concat(partitionColumns, clusteringColumns)) {
                    AbstractType<?> type = cd.type;
                    if (map == null) {
                        objects[i] = type.compose( type.fromString(keys[i].toString()) );
                    } else {
                        map.put(cd.name.toString(), type.compose( type.fromString(keys[i].toString()) ) );
                    }
                    i++;
                }
                return objects;
            } else {
               // Expect that id id the last colomn of the primary key, and check that previous colomns are set in the map
               if (map == null) {
                   throw new IOException("Unexpected _id="+id+", expecting a JSON array or a document that contains all primary key columns but the last one.");
               }
               int i=0;
               for(ColumnDefinition cd : Iterables.concat(partitionColumns, clusteringColumns)) {
                   AbstractType<?> type = cd.type;
                   if (i < pkSize-1) {
                      if (map.get(cd.name.toString()) == null) {
                          throw new IOException("Unexpected _id="+id+", expecting a value for "+cd.name.toString());
                      }
                   } else {
                       map.put(cd.name.toString(), type.compose( type.fromString(id) ) );
                   }
                   i++;
               }
               return null;
            }
        } else {
            AbstractType<?> type = partitionColumns.get(0).type;
            if (map == null) {
                return new Object[] { type.compose( type.fromString(id) ) };
            } else {
                map.put(partitionColumns.get(0).name.toString(), type.compose( type.fromString(id) ) );
                return null;
            }
        }
    }

    
    
    
    
    
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#getToken(java.nio.ByteBuffer
     * , org.apache.cassandra.db.ColumnFamily)
     */
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
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#createElasticAdminKeyspace
     * ()
     */
    @Override
    public void createElasticAdminKeyspace()  {
        if (Schema.instance.getKSMetaData(SchemaService.ELASTIC_ADMIN_KEYSPACE) == null) {
            try {
                QueryProcessor.process(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'DatacenterReplicationStrategy', 'datacenters' : '%s' };", 
                        ELASTIC_ADMIN_KEYSPACE, DatabaseDescriptor.getLocalDataCenter()),
                        ConsistencyLevel.LOCAL_ONE);
                QueryProcessor.process(String.format("CREATE TABLE IF NOT EXISTS %s.\"%s\" ( dc text PRIMARY KEY, owner uuid, version bigint, metadata text);", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
                        ConsistencyLevel.LOCAL_ONE);
            } catch (RequestExecutionException e) {
                logger.error("Failed to create keyspace {}",SchemaService.ELASTIC_ADMIN_KEYSPACE, e);
            }
        }
    }

   
    private static final String metaDataTableName = ELASTIC_ADMIN_METADATA_TABLE_PREFIX.concat(DatabaseDescriptor.getLocalDataCenter()).replace("-", "_");

    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#writeMetaData(org.
     * elasticsearch.cluster.metadata.MetaData)
     */
    @Override
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException {
        CFMetaData cfm = getCFMetaData(ELASTIC_ADMIN_KEYSPACE, metaDataTableName);
        cfm.comment(metaDataString);
        MigrationManager.announceColumnFamilyUpdate(cfm, false, false);
    }

    /**
     * Should only be used after a SCHEMA change.
     */
    @Override
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        try {
            CFMetaData cfm = getCFMetaData(ELASTIC_ADMIN_KEYSPACE, metaDataTableName);
            String metadataString = cfm.getComment();
            if (metadataString != null && metadataString.length() > 0) {
                XContentParser xparser = new JsonXContentParser(new JsonFactory().createParser(metadataString));
                MetaData metaData = MetaData.Builder.fromXContent(xparser);
                if (logger.isDebugEnabled()) {
                    logger.debug("recovered metadata from {}.{} = {}", ELASTIC_ADMIN_KEYSPACE, metaDataTableName, MetaData.Builder.toXContent(metaData));
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
                    logger.trace("recovered metadata from {}.{} = {}", ELASTIC_ADMIN_KEYSPACE, metaDataTableName, MetaData.Builder.toXContent(metaData));
                }
            } catch (IOException e) {
                throw new NoPersistedMetaDataException(e);
            }
            return metaData;
        }
        throw new NoPersistedMetaDataException();
    }

    @Override
    public MetaData readMetaDataAsRow() throws NoPersistedMetaDataException {
        UntypedResultSet result;
        try {
            result = process(ConsistencyLevel.LOCAL_QUORUM,
                    String.format("SELECT metadata FROM \"%s\".\"%s\" WHERE dc = ?", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
                    DatabaseDescriptor.getLocalDataCenter());
        } catch (RequestExecutionException | RequestValidationException e) {
            throw new NoPersistedMetaDataException(e);
        }
        Row row = result.one();
        if (row != null && row.has("metadata")) {
            return parseMetaDataString(row.getString("metadata"));
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
            String metaDataString = MetaData.builder().toXContent(metadata);
            // initialize a first row if needed
            UntypedResultSet result = process(ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.LOCAL_SERIAL,
                    String.format("INSERT INTO \"%s\".\"%s\" (dc,owner,version,metadata) VALUES (?,?,?,?) IF NOT EXISTS", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
                    DatabaseDescriptor.getLocalDataCenter(), UUID.fromString(StorageService.instance.getLocalHostId()), metadata.version(), metaDataString);

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

    private static final String updateMetaDataQuery = String.format("UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, metadata = ? WHERE dc = ? IF owner = ? AND version = ?", 
            new Object[] {ELASTIC_ADMIN_KEYSPACE, metaDataTableName });

    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#pushMetaData(org.
     * elasticsearch.cluster.ClusterState,
     * org.elasticsearch.cluster.ClusterState, java.lang.String)
     */
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
        String metaDataString = MetaData.builder().toXContent(newMetaData);
        UntypedResultSet result = process(
                ConsistencyLevel.LOCAL_ONE,
                ConsistencyLevel.LOCAL_SERIAL,
                updateMetaDataQuery,
                new Object[] { UUID.fromString(localNode().id()), newMetaData.version(), metaDataString, DatabaseDescriptor.getLocalDataCenter(),
                        UUID.fromString(oldMetaData.uuid()), oldMetaData.version() });
        Row row = result.one();
        boolean applied = false;
        if (row.has("[applied]")) {
            applied = row.getBoolean("[applied]");
        }
        if (applied) {
            logger.debug("Succefully update metadata source={} newMetaData={}", source, metaDataString);
            writeMetaDataAsComment(metaDataString);
            return;
        } else {
            logger.warn("Failed to update metadata source={} oldMetadata={}/{} currentMetaData={}/{}", source, oldMetaData.uuid(), oldMetaData.version(), row.getUUID("owner"), row.getLong("version"));
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
    public ShardRoutingState readIndexShardState(InetAddress address, String index, ShardRoutingState defaultState) {
        return this.discoveryService.readIndexShardState(address, index, defaultState);
    }

    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void writeIndexShardSate(String index, ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        this.discoveryService.writeIndexShardSate(index, shardRoutingState);
    }
    
    /**
     * Publish on Gossip.X1 all local shard state.
     */
    public void publishAllShardsState() {
        for (IndexRoutingTable indexRoutingTable : state().routingTable()) {
            IndexShardRoutingTable indexShardRoutingTable = indexRoutingTable.shards().get(0);
            try {
                writeIndexShardSate(indexRoutingTable.getIndex(), indexShardRoutingTable.getPrimaryShardRouting().state() );
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
