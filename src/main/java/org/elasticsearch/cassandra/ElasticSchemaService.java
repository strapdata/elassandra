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

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.cassandra.db.marshal.TimestampType;
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
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.action.index.XIndexRequest;
import org.elasticsearch.action.index.XIndexResponse;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataMappingService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentFieldMappers;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper.CqlCollection;
import org.elasticsearch.index.mapper.object.ObjectMapper.CqlStruct;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ElasticSchemaService extends AbstractComponent implements SchemaService {

    public static String ELASTIC_ID_COLUMN_NAME = "_id";
    public static String MAPPING_UPDATE_TIMEOUT = "cassandra.mapping_update.timeout";

    private final ClusterService clusterService;
    private final TimeValue mappingUpdateTimeout;

    // ElasticSearch to Cassandra mapping
    public Map<String, String> typeMapping = new ImmutableMap.Builder<String,String>()
            .put("string", "text")
            .put("date", "timestamp")
            .put("integer", "int")
            .put("double", "double")
            .put("float", "float")
            .put("long", "bigint")
            .put("short", "int")
            .put("byte", "int")
            .put("boolean", "boolean")
            .put("binary", "blob")
            .put("ip", "inet")
            .build();
    
    
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
    

    public Map<String, String> mapperToCql = new java.util.HashMap<String, String>() {
        {
            put("StringFieldMapper", "text");
            put("DateFieldMapper", "timestamp");
            put("IntegerFieldMapper", "int");
            put("DoubleFieldMapper", "double");
            put("FloatFieldMapper", "float");
            put("LongFieldMapper", "bigint");
            put("ShortFieldMapper", "int");
            put("ByteFieldMapper", "int");
            put("BooleanFieldMapper", "boolean");
            put("BinaryFieldMapper", "blob");
            put("IpFieldMapper", "inet");
            put("TimestampFieldMapper", "timestamp");
        }
    };

    public boolean isNativeCql3Type(String cqlType) {
        return typeMapping.values().contains(cqlType) && !cqlType.startsWith("geo_");
    }

    @Inject
    public ElasticSchemaService(Settings settings, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
        this.mappingUpdateTimeout = settings.getAsTime(MAPPING_UPDATE_TIMEOUT, TimeValue.timeValueSeconds(30));
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
    

    public Object value(FieldMapper<?> mapper, Object value) {
        if (mapper instanceof DateFieldMapper) {
            // workaround because elasticsearch manage Date as Long
            return new Date(((DateFieldMapper) mapper).value(value));
        } else {
            return mapper.value(value);
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
                Mapper subMapper = ((ObjectMapper) mapper).mappers().get(subName);
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
                components.add(serialize(ksName, cfName, type, fieldName+'_'+fname, field_value, ((ObjectMapper) mapper).mappers().get(fname)));
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
            Entry<String,Mapper> entry = objectMapper.mappers().entrySet().iterator().next();
            Mapper childMapper = entry.getValue();
            if (childMapper instanceof FieldMapper) {
                return "map<text,"+typeMapping.get(childMapper.contentType())+">";
            } else if (childMapper instanceof ObjectMapper) {
                return "map<text,frozen<"+buildCql(ksName,cfName,entry.getKey(),(ObjectMapper)childMapper)+">>";
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

        // create sub-type first
        for (Entry<String, Mapper> entry : objectMapper.mappers().entrySet()) {
            Mapper mapper = entry.getValue();
            if (mapper instanceof ObjectMapper) {
                buildCql(ksName, cfName, entry.getKey(), (ObjectMapper) mapper);
            } else if (mapper instanceof GeoPointFieldMapper) {
                buildGeoPointType(ksName, typeName+'_'+entry.getKey());
            } 
        }

        Pair<List<String>, List<String>> udt = getUDTInfo(ksName, objectMapper.fullPath().replace('.', '_'));
        if (udt == null) {
            // create new UDT.
            // TODO: Support dynamic mapping by altering UDT
            StringBuilder create = new StringBuilder(String.format("CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( ", ksName, typeName));
            boolean first = true;
            for (Entry<String, Mapper> entry : objectMapper.mappers().entrySet()) {
                Mapper mapper = entry.getValue();
                if (first)
                    first = false;
                else
                    create.append(", ");
                create.append('\"').append(mapper.name()).append("\" ");
                if (mapper instanceof ObjectMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_'))
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(">");
                } else if (mapper instanceof GeoPointFieldMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(typeName).append('_').append(mapper.name())
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) create.append(">");
                } else {
                    String cqlType = typeMapping.get(mapper.contentType());
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
                logger.debug(create.toString());
            }
            QueryProcessor.process(create.toString(), ConsistencyLevel.LOCAL_ONE);
        } else {
            // update existing UDT.
        }
        return typeName;
    }

    public void buildGeoPointType(String ksName, String typeName) throws RequestExecutionException {
        String query = String.format("CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( lat double, lon double )", ksName, typeName);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }

    public void buildGeoShapeType(String ksName, String typeName) {
        // TODO: Implements geo_shape.
    }
    
   
    public void buildObjectMapping(XContentBuilder builder, AbstractType<?> type) throws IOException {
        CQL3Type cql3type = type.asCQL3Type();
        if (cql3type instanceof CQL3Type.Native) {
            builder.field("type", cqlMapping.get(cql3type.toString()));
        } else if (cql3type instanceof CQL3Type.UserDefined) {
            builder.field("type", "nested");
            builder.field("cql_struct", "udt");
            XContentBuilder propertiesBuilder = builder.startObject("properties");
            TupleType tuple = (TupleType)type;
            for(int i=0; i< tuple.size(); i++) {
                buildTypeMapping(propertiesBuilder, tuple.type(i));
            }
            propertiesBuilder.endObject();
        }
    }
    
    public void buildTypeMapping(XContentBuilder builder, AbstractType<?> type) throws IOException {
        CQL3Type cql3type = type.asCQL3Type();
        if (type.isCollection()) {
            if (type instanceof ListType) {
                builder.field("cql_collection", "list");
                buildObjectMapping(builder, ((SetType<?>)type).getElementsType() );
            } else if (type instanceof SetType) {
                builder.field("cql_collection", "set");
                buildObjectMapping(builder, ((SetType<?>)type).getElementsType() );
            } else if (type instanceof MapType) {
                MapType mtype = (MapType)type;
                if (mtype.getKeysType().asCQL3Type() == CQL3Type.Native.TEXT) {
                   builder.field("cql_collection", "singleton");
                   builder.field("cql_struct", "map");
                   builder.field("cql_partial_update", Boolean.TRUE);
                   builder.field("type", "nested");
                   XContentBuilder propertiesBuilder = builder.startObject("properties");
                   propertiesBuilder.endObject();
                }
            }
        } else {
            builder.field("cql_collection", "singleton");
            buildObjectMapping(builder, type );
        }
    }
    
    public String buildTableMapping(String ksName, String cfName) throws IOException, SyntaxException, ConfigurationException {
        return buildTableMapping(ksName, cfName, ".*");
    }
    
    public String buildTableMapping(String ksName, String cfName, String columnRegexp) throws IOException, SyntaxException, ConfigurationException {
        Pattern pattern =  Pattern.compile(columnRegexp);
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            XContentBuilder propertiesBuilder = builder.startObject();
            UntypedResultSet result = QueryProcessor.executeInternal("SELECT  column_name,validator FROM system.schema_columns WHERE keyspace_name=? and columnfamily_name=?", 
                    new Object[] { ksName, cfName });
            for (Row row : result) {
                if (row.has("validator") && pattern.matcher(row.getString("column_name")).matches()) {
                    XContentBuilder columnTypeBuilder = propertiesBuilder.startObject(row.getString("column_name"));
                    AbstractType<?> type =  TypeParser.parse(row.getString("validator"));
                    buildTypeMapping(columnTypeBuilder, type);
                    columnTypeBuilder.endObject();
                }
            }
            propertiesBuilder.endObject();
            if (logger.isDebugEnabled()) 
                logger.debug("mapping = {}", builder.string());
            return "{properties:"+builder.string()+"}";
        } catch (IOException | SyntaxException | ConfigurationException e) {
            logger.warn("Failed to build elasticsearch mapping " + ksName + "." + cfName, e);
            throw e;
        }
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
            
            createIndexKeyspace(index, settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 0) +1);

            Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
            if (targetPair == null) {

                CFMetaData cfm = Schema.instance.getCFMetaData(index, type);
                boolean newTable = (cfm == null);

                logger.debug("Inferring CQL3 schema {}.{} with columns={}", index, type, columns);
                StringBuilder columnsList = new StringBuilder();
                StringBuilder readOnUpdateColumnList = new StringBuilder();
                Map<String,Boolean> columnsMap = new HashMap<String, Boolean>(columns.size());
                for (String column : columns) {
                    columnsList.append(',');
                    if (column.equals("_token")) {
                        continue; // ignore pseudo column known by Elasticsearch
                    }
                    String cqlType = null;
                    FieldMapper<?> fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                    if (fieldMapper != null) {
                        columnsMap.put(column,  fieldMapper.cqlPartialUpdate());
                        if (fieldMapper instanceof DateFieldMapper) {
                            // workaround because elasticsearch maps date to long
                            cqlType = typeMapping.get("date");
                        } else if (fieldMapper instanceof GeoPointFieldMapper) {
                            cqlType = fieldMapper.name().replace('.', '_');
                        } else {
                            cqlType = typeMapping.get(fieldMapper.contentType());
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
                                // check columnName exists and is map<text,?>
                                logger.debug("Expecting column [{}] to be a map<text,?>", column);
                            } else  if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                                // Cassandra 2.1.8 : Non-frozen collections are not allowed inside collections
                                cqlType = "frozen<\"" + buildCql(index, type, column, objectMapper) + "\">";
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
                                    String query = String.format("ALTER TABLE \"%s\".\"%s\" ADD \"%s\" %s", index, type, column, cqlType);
                                    logger.debug(query);
                                    QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                                } catch (Exception e) {
                                    logger.warn("Cannot alter table {}.{} column {} with type {}", e, index, type, column, cqlType);
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

                if (newTable) {
                    String query = String.format("CREATE TABLE IF NOT EXISTS \"%s\".\"%s\" ( \"%s\" text PRIMARY KEY %s ) WITH COMMENT='Auto-created by Elassandra'", index, type,
                            ELASTIC_ID_COLUMN_NAME, columnsList);
                    logger.debug(query);
                    QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                }
                
                // build secondary indices
                for (String column : columns) {
                    if (column.equals("_token")) {
                        continue; // ignore pseudo column known by Elasticsearch
                    }
                    ColumnDefinition cdef = (cfm == null) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                    if (newTable || (cdef != null) && !cdef.isIndexed() && !(cfm.partitionKeyColumns().size()==1 && cdef.kind == ColumnDefinition.Kind.PARTITION_KEY )) {
                        QueryProcessor.process(String.format("CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" (\"%s\") USING '%s' WITH OPTIONS = {'%s':'%s.%s', '%s':'%s'}",
                            buildIndexName(type, column), index, type, column, org.elasticsearch.cassandra.ElasticSecondaryIndex.class.getName(), 
                            ElasticSecondaryIndex.ELASTIC_OPTION_TARGETS, index, type,
                            ElasticSecondaryIndex.ELASTIC_OPTION_PARTIAL_UPDATE, columnsMap.get(column).toString()), 
                            ConsistencyLevel.LOCAL_ONE);
                    } 
                }
                    
            }
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
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
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        if (metadata != null) {
            ImmutableList.Builder<ColumnDefinition> builder = new ImmutableList.Builder<ColumnDefinition>().addAll(metadata.partitionKeyColumns());
            List<ColumnDefinition> clusterKeys = metadata.clusteringColumns();
            if (clusterKeys != null)
                builder.addAll(metadata.clusteringColumns());
            return builder.build();
        }
        throw new ConfigurationException("Unknown table " + ksName + "." + cfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#getPrimaryKeyColumnsName
     * (java.lang.String, java.lang.String)
     */
    @Override
    public List<String> getPrimaryKeyColumnsName(final String ksName, final String cfName) throws ConfigurationException {
        List<ColumnDefinition> pkColumns = getPrimaryKeyColumns(ksName, cfName);
        ArrayList<String> names = new ArrayList<String>(pkColumns.size());
        for (ColumnDefinition cd : pkColumns) {
            names.add(cd.name.toString());
        }
        return names;
    }

    
    public void buildPrimaryKeyFragment(final String ksName, final String cfName, StringBuilder pkCols, StringBuilder pkWhere) throws ConfigurationException {
        List<ColumnDefinition> pkColumns = getPrimaryKeyColumns(ksName, cfName);
        for (ColumnDefinition cd : pkColumns) {
            if (pkCols.length() > 0) {
                pkCols.append(',');
                pkWhere.append(" AND ");
            }
            pkCols.append('\"').append(cd.name.toString()).append('\"');
            pkWhere.append('\"').append(cd.name.toString()).append("\" = ?");
        }
    }

    
    public void buildPartitionKeyFragment(final String ksName, final String cfName, StringBuilder pkCols) throws ConfigurationException {
        List<ColumnDefinition> pkColumns = Schema.instance.getCFMetaData(ksName, cfName).partitionKeyColumns();
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
        IndexMetaData indexMetaData = clusterService.state().metaData().index(index);
        if (indexMetaData != null) {
            MappingMetaData mappingMetaData = indexMetaData.mapping(type);
            if (mappingMetaData != null) {
                try {
                    Set<String> set = ((Map<String, Object>)mappingMetaData.sourceAsMap().get("properties")).keySet();
                    set.remove(ElasticSecondaryIndex.ELASTIC_TOKEN);
                    Collection<String> cols = new ArrayList<String>(set.size());
                    for(String s : set) {
                        int x = s.indexOf('.');
                        cols.add( (x > 0) ? s.substring(0,x) : s );
                    }
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
    public UntypedResultSet fetchRow(final String index, final String type, final Collection<String> requiredColumns, final String id, final ConsistencyLevel cl) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
        String ksName = (targetPair == null) ? index : targetPair.left;
        String cfName = (targetPair == null) ? type : targetPair.right;

        return process(cl, buildFetchQuery(ksName,cfName,requiredColumns), parseElasticId(ksName, cfName, id));
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
    public UntypedResultSet fetchRowInternal(final String index, final String type, final Collection<String> requiredColumns, final String id) throws ConfigurationException, IOException  {
        Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
        String ksName = (targetPair == null) ? index : targetPair.left;
        String cfName = (targetPair == null) ? type : targetPair.right;
        return fetchRowInternal(index, type, requiredColumns, parseElasticId(ksName, cfName, id));
    }
    
    @Override
    public UntypedResultSet fetchRowInternal(final String ksName, final String cfName, final Collection<String> requiredColumns, final Object[] pkColumns) throws ConfigurationException, IOException  {
        return QueryProcessor.instance.executeInternal(buildFetchQuery(ksName,cfName,requiredColumns), pkColumns);
    }
    
    public String buildFetchQuery(final String ksName, final String cfName, final Collection<String> requiredColumns) throws ConfigurationException {
        StringBuilder columns = new StringBuilder();
        StringBuilder pkColums = new StringBuilder();
        StringBuilder pkWhere = new StringBuilder();
        
        buildPrimaryKeyFragment(ksName, cfName, pkColums, pkWhere);
        for (String c : requiredColumns) {
            if (columns.length() > 0)
                columns.append(',');
            if (c.equals("_token")) {
                columns.append("token(").append(pkColums).append(") as \"_token\"");
            } else {
                columns.append("\"").append(c).append("\"");
            }
        }
        return String.format("SELECT %s FROM \"%s\".\"%s\" WHERE %s", columns, ksName, cfName, pkWhere );
    }

    @Override
    public void deleteRow(final String index, final String type, final String id, final ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException {
        Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
        String ksName = (targetPair == null) ? index : targetPair.left;
        String cfName = (targetPair == null) ? type : targetPair.right;

        StringBuilder pkCols = new StringBuilder();
        StringBuilder pkWhere = new StringBuilder();
        buildPrimaryKeyFragment(ksName, cfName, pkCols, pkWhere);

        String query = String.format("DELETE FROM \"%s\".\"%s\" WHERE %s", new Object[] { ksName, cfName, pkWhere });
        process(cl, query, parseElasticId(ksName, cfName, id));
    }
    
    @Override
    public Map<String, Object> rowAsMap(UntypedResultSet.Row row) throws IOException {
        Map<String, Object> mapObject = new HashMap<String, Object>();
        rowAsMap(row, mapObject);
        return mapObject;
    }

    @Override
    public int rowAsMap(UntypedResultSet.Row row, Map<String, Object> mapObject) throws IOException {
        int putCount = 0;
        List<ColumnSpecification> columnSpecs = row.getColumns();

        for (int columnIndex = 0; columnIndex < columnSpecs.size(); columnIndex++) {
            ColumnSpecification colSpec = columnSpecs.get(columnIndex);
            String columnName = colSpec.name.toString();
            CQL3Type cql3Type = colSpec.type.asCQL3Type();
            
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
                    mapObject.put(columnName, row.getUUID(colSpec.name.toString()));
                    putCount++;
                    break;
                case TIMESTAMP:
                    mapObject.put(columnName, row.getTimestamp(colSpec.name.toString()).getTime());
                    putCount++;
                    break;
                case INT:
                    mapObject.put(columnName, row.getInt(colSpec.name.toString()));
                    putCount++;
                    break;
                case BIGINT:
                    mapObject.put(columnName, row.getLong(colSpec.name.toString()));
                    putCount++;
                    break;
                case DECIMAL:
                case DOUBLE:
                    mapObject.put(columnName, row.getDouble(colSpec.name.toString()));
                    putCount++;
                    break;
                case BLOB:
                    mapObject.put(columnName, row.getBytes(colSpec.name.toString()));
                    putCount++;
                    break;
                case BOOLEAN:
                    mapObject.put(columnName, row.getBoolean(colSpec.name.toString()));
                    putCount++;
                    break;
                case COUNTER:
                    break;
                case INET:
                    mapObject.put(columnName, row.getInetAddress(colSpec.name.toString()));
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
                    if (elementType instanceof TimestampType) {
                        // Timestamp+Date are stored as Long in ElasticSearch
                        List<Long> dateList = new ArrayList<Long>(list.size());
                        for (Object date : list) {
                            dateList.add(((Date) date).getTime());
                        }
                        list = dateList;
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
                    if (elementType instanceof TimestampType) {
                        // Timestamp+Date are stored as Long in ElasticSearch
                        Set<Long> dateSet = new HashSet<Long>(set.size());
                        for (Object date : set) {
                            dateSet.add(((Date) date).getTime());
                        }
                        set = dateSet;
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
                    if (elementType instanceof TimestampType) {
                        Map<String, Long> dateMap = new HashMap<String, Long>(map.size());
                        for(Object key : map.keySet()) {
                            dateMap.put((String)key, ((Date) map.get(key)).getTime());
                        }
                        map = dateMap;
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

    

    private static class MappingUpdateListener implements ActionListener<ClusterStateUpdateResponse> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Throwable error = null;

        @Override
        public void onFailure(Throwable t) {
            error = t;
            latch.countDown();
        }

        public void waitForMappingUpdate(TimeValue timeValue) throws Exception {
            if (timeValue.millis() > 0) {
                latch.await(timeValue.millis(), TimeUnit.MILLISECONDS);
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
    }

    
    public void blockingMappingUpdate(IndexService indexService, DocumentMapper mapper ) throws Exception {
        MappingUpdateListener mappingUpdateListener = new MappingUpdateListener();
        MetaDataMappingService metaDataMappingService = ElassandraDaemon.injector().getInstance(MetaDataMappingService.class);
        metaDataMappingService.updateMapping(indexService.index().name(), indexService.indexUUID(), mapper.type(), mapper.refreshSource(), -1, clusterService.localNode().id(), mappingUpdateListener);
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
    public String insertDocument(final IndicesService indicesService, final IndexRequest request, final ClusterState clusterState, Long writetime, Boolean applied) throws Exception {

        IndexService indexService = indicesService.indexServiceSafe(request.index());
        IndexShard indexShard = indexService.shardSafe(0);
        SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, request.source()).type(request.type()).id(request.id()).routing(request.routing()).parent(request.parent())
                .timestamp(request.timestamp()).ttl(request.ttl());

        Engine.Index index = indexShard.prepareIndex(sourceToParse, request.version(), request.versionType(), Engine.Operation.Origin.PRIMARY, request.canHaveDuplicates());
        boolean mappingUpdated = false;
        if (index.parsedDoc().mappingsModified()) {
            mappingUpdated = true;
            // blocking Elasticsearch mapping update (required to update cassandra schema, this is the cost of dynamic mapping)
            blockingMappingUpdate(indexService, index.docMapper());
        }

        // insert document into cassandra keyspace=index, table = type
        Map<String, Object> sourceMap = request.sourceAsMap();
        Map<String, ObjectMapper> objectMappers = index.docMapper().objectMappers();
        DocumentFieldMappers fieldMappers = index.docMapper().mappers();

        logger.debug("Insert id={} source={} mapping_updated={} fieldMappers={} objectMappers={} consistency={} ttl={}", request.id(), sourceMap, mappingUpdated,
                Arrays.toString(fieldMappers.toArray()), objectMappers, request.consistencyLevel().toCassandraConsistencyLevel(), request.ttl());

        Map<String, Object> map = new HashMap<String, Object>();
        for (String field : sourceMap.keySet()) {
            Object fieldValue = sourceMap.get(field);
            try {
                ObjectMapper objectMapper = objectMappers.get(field);
                FieldMapper<?> fieldMapper = fieldMappers.smartNameFieldMapper(field);
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
                        map.put(field, value(fieldMapper, fieldMapper.value(fieldValue)));
                    } else if (fieldMapper.cqlCollection().equals(CqlCollection.LIST)) {
                        List<Object> fieldValueList2 = new ArrayList();
                        if (fieldValue instanceof List) {
                            List<Object> fieldValueList = (List) fieldValue;
                            for (Object o : fieldValueList) {
                                fieldValueList2.add(value(fieldMapper, fieldMapper.value(o)));
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
                                fieldValueList2.add(value(fieldMapper, fieldMapper.value(o)));
                            }
                        } else {
                            fieldValueList2.add(value(fieldMapper, fieldValue));
                        }
                        map.put(field, fieldValueList2);
                    }
                } else {
                    logger.debug("[{}].[{}] ignoring unmapped field [{}] = [{}] ", request.index(), request.type(), field, fieldValue);
                }
            } catch (Exception e) {
                logger.warn("[{}].[{}] failed to parse field {}={} => ignoring", e, request.index(), request.type(), field, fieldValue );
            }
        }

        return insertRow(request.index(), request.type(), map, request.id(),
                (request.opType() == OpType.CREATE), request.ttl(), request.consistencyLevel().toCassandraConsistencyLevel(), writetime, applied);
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
    public String insertRow(final String index, final String type, Map<String, Object> map, String id, final boolean ifNotExists, final long ttl, final ConsistencyLevel cl,
            Long writetime, Boolean applied) throws Exception {
        Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
        String ksName = (targetPair == null) ? index : targetPair.left;
        String cfName = (targetPair == null) ? type : targetPair.right;

        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        // if the provided columns does not contains all the primary key columns, parse the _id to populate the columns in map.
        boolean buildId = true;
        ArrayNode array = jsonMapper.createArrayNode();
        for(ColumnDefinition cd: Iterables.concat(metadata.partitionKeyColumns(), metadata.clusteringColumns())) {
            if (map.keySet().contains(cd.name.toString())) {
                addToJsonArray(cd.type, map.get(cd.name.toString()), array);
            } else {
                buildId = false;
                parseElasticId(index, type, id, map);
            }
        }
        if (buildId) {
            id = buildElasticId(array);
        }
        
        StringBuilder questionsMarks = new StringBuilder();
        StringBuilder columnNames = new StringBuilder();
        Object[] values = new Object[map.size()];
        int i=0;
        for (Entry<String,Object> entry : map.entrySet()) {
            if (columnNames.length() > 0) {
                columnNames.append(',');
                questionsMarks.append(',');
            }
            columnNames.append("\"").append(entry.getKey()).append("\"");
            questionsMarks.append('?');
            values[i++] = entry.getValue();
        }
        String query = String.format("INSERT INTO \"%s\".\"%s\" (%s) VALUES (%s) %s %s", ksName, cfName, 
                columnNames.toString(), questionsMarks.toString(), (ifNotExists) ? "IF NOT EXISTS" : "",
                (ttl > 0) ? "USING TTL " + Long.toString(ttl) : "");
        try {
            UntypedResultSet result = process(cl, (ifNotExists) ? ConsistencyLevel.LOCAL_SERIAL : null, query, values);
            if (ifNotExists) {
                if (!result.isEmpty()) {
                    Row row = result.one();
                    if (row.has("[applied]")) {
                        applied = row.getBoolean("[applied]");
                    }
                }
            } else {
                applied = true;
            }
            return id;
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
        ImmutableBiMap<Pair<String, String>, Pair<String, String>> immutableMapping = ImmutableBiMap.<Pair<String, String>, Pair<String, String>> copyOf(ElasticSecondaryIndex.mapping);
        for (String index : indices) {
            for (Entry<Pair<String, String>, Pair<String, String>> entry : immutableMapping.entrySet()) {
                if (entry.getKey().left.equals(index)) {
                    indexColumnFamilly(entry.getValue().left, entry.getValue().right, index, entry.getKey().right, tokenRanges);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#indexColumnFamilly(java
     * .lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.util.Collection)
     */
    @Override
    public void indexColumnFamilly(String ksName, String cfName, String index, String type, Collection<Range<Token>> tokenRanges) {
        try {
            List<String> pkColums = getPrimaryKeyColumnsName(ksName, cfName);
            StringBuilder partkeyCols = new StringBuilder();
            buildPartitionKeyFragment(ksName, cfName, partkeyCols);
            logger.info("Re-indexing table {}.{} for tokenRanges={}", ksName, cfName, tokenRanges);
            for (Range<Token> range : tokenRanges) {

                String query = String.format("SELECT * FROM \"%s\".\"%s\" WHERE token(%s) > ? AND token(%s) <= ?", ksName, cfName, partkeyCols, partkeyCols);

                UntypedResultSet result = QueryProcessor.executeInternalWithPaging(query, 1024, new Object[] { range.left.getTokenValue(), range.right.getTokenValue() });
                for (Row row : result) {
                    List<ColumnSpecification> columnSpecs = row.getColumns();

                    for (int columnIndex = 0; columnIndex < columnSpecs.size(); columnIndex++) {
                        ColumnSpecification colSpec = columnSpecs.get(columnIndex);
                        String columnName = colSpec.name.toString();
                        CQL3Type cql3Type = colSpec.type.asCQL3Type();

                        if (!row.has(colSpec.name.toString()))
                            continue;
                        ArrayList<Object> sourceData = new ArrayList<Object>();
                        Map<String,Object> primayKeyMap = new HashMap<String,Object>(pkColums.size());
                        if (cql3Type instanceof CQL3Type.Native) {
                            int pkIdx = pkColums.indexOf(columnName);

                            switch ((CQL3Type.Native) cql3Type) {
                            case ASCII:
                            case TEXT:
                            case VARCHAR:
                                sourceData.add(columnName);
                                sourceData.add(row.getString(columnName));
                                primayKeyMap.put(columnName, row.getString(columnName));
                                break;
                            case TIMEUUID:
                            case UUID:
                                sourceData.add(columnName);
                                sourceData.add(row.getUUID(columnName));
                                primayKeyMap.put(columnName, row.getUUID(columnName));
                                break;
                            case TIMESTAMP:
                                sourceData.add(columnName);
                                sourceData.add(row.getTimestamp(columnName).getTime());
                                primayKeyMap.put(columnName, row.getTimestamp(columnName).getTime());
                                break;
                            case VARINT:
                            case INT:
                            case BIGINT:
                                sourceData.add(columnName);
                                sourceData.add(row.getLong(columnName));
                                primayKeyMap.put(columnName, row.getLong(columnName));
                                break;
                            case FLOAT:
                                break;
                            case DECIMAL:
                            case DOUBLE:
                                sourceData.add(columnName);
                                sourceData.add(row.getDouble(columnName));
                                primayKeyMap.put(columnName, row.getDouble(columnName));
                                break;
                            case BLOB:
                                sourceData.add(columnName);
                                sourceData.add(row.getBytes(columnName));
                                primayKeyMap.put(columnName, row.getBytes(columnName));
                                break;
                            case BOOLEAN:
                                sourceData.add(columnName);
                                sourceData.add(row.getBoolean(columnName));
                                primayKeyMap.put(columnName, row.getBoolean(columnName));
                                break;
                            case COUNTER:
                                break;
                            case INET:
                                sourceData.add(columnName);
                                sourceData.add(row.getInetAddress(columnName));
                                primayKeyMap.put(columnName, row.getInetAddress(columnName));
                                break;
                            }
                        } else if (cql3Type.isCollection()) {
                            // TODO: mapping from list to elastic arrays.
                        } else if (cql3Type instanceof CQL3Type.Custom) {

                        }
                
                        //index(index, type, buildElasticId(ksName, cfName, primayKeyMap), sourceData.toArray());
                    }
                }
            }
        } catch (ConfigurationException e) {
            logger.error("Error:", e);
        }
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#index(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.Object[])
     */
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


    public Object[] parseElasticId(final String ksName, final String cfName, final String id) throws IOException {
        return parseElasticId(ksName, cfName, id, null);
    }
    
    /**
     * Parse _id (something like (xa,b,c)) to build a primary key array or populate map.
     * @param ksName
     * @param cfName
     * @param map
     * @param id
     */
    public Object[] parseElasticId(final String ksName, final String cfName, final String id, Map<String, Object> map) throws IOException {
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        List<ColumnDefinition> clusteringColumns = metadata.clusteringColumns();
        if (partitionColumns.size()+metadata.clusteringColumns().size() > 1) {
            if (!(id.startsWith("[") && id.endsWith("]"))) 
                throw new IOException("Unexpected _id=["+id+"], expecting something like [a,b,c]");
            
            Object[] keys = jsonMapper.readValue(id, Object[].class);
            Object[] objects = (map != null) ? null : new Object[partitionColumns.size()+clusteringColumns.size()];
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
            if (map == null) {
                return new Object[] { id };
            } else {
                map.put(partitionColumns.get(0).name.toString(), id);
                return null;
            }
        }
    }

    public static final org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
    
    public static ArrayNode addToJsonArray(final AbstractType<?> type, final Object value, ArrayNode an) {
        TypeSerializer<?> typeSerializer = type.getSerializer();
        if (typeSerializer instanceof BooleanSerializer) an.add((Boolean)value); 
        else if ((typeSerializer instanceof IntegerSerializer) || (typeSerializer instanceof Int32Serializer)) an.add((Integer)value);
        else if (typeSerializer instanceof LongSerializer) an.add( (Long) value);
        else if (typeSerializer instanceof DoubleSerializer) an.add( (Double) value);
        else if (typeSerializer instanceof DecimalSerializer) an.add( (BigDecimal) value);
        else if (typeSerializer instanceof FloatSerializer) an.add( (Float) value);
        else an.add(stringify(type, value));
        return an;
    }
    
    
    public static String buildElasticId(ArrayNode an) throws JsonGenerationException, JsonMappingException, IOException {
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

    public static final String ELASTIC_ADMIN_KEYSPACE = "elastic_admin";
    public static final String ELASTIC_ADMIN_METADATA_TABLE_PREFIX = "metadata_";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#createElasticAdminKeyspace
     * ()
     */
    @Override
    public void createElasticAdminKeyspace()  {
        if (Schema.instance.getKSMetaData(ElasticSchemaService.ELASTIC_ADMIN_KEYSPACE) == null) {
            try {
                QueryProcessor.process(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };", ELASTIC_ADMIN_KEYSPACE),
                        ConsistencyLevel.LOCAL_ONE);
                QueryProcessor.process(String.format("CREATE TABLE IF NOT EXISTS %s.\"%s\" ( dc text PRIMARY KEY, owner uuid, version bigint, metadata text);", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
                        ConsistencyLevel.LOCAL_ONE);
            } catch (RequestExecutionException e) {
                logger.error("Failed to create keyspace {}",ElasticSchemaService.ELASTIC_ADMIN_KEYSPACE, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#pullMetaData()
     */
    public static volatile String lastPulledMetaDataUuid = null;
    public static volatile long lastPulledMetaDataVersion = -1;

    private static final String metaDataTableName = ELASTIC_ADMIN_METADATA_TABLE_PREFIX.concat(DatabaseDescriptor.getLocalDataCenter()).replace("-", "_");

    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#writeMetaData(org.
     * elasticsearch.cluster.metadata.MetaData)
     */
    @Override
    public void writeMetaDataAsComment(MetaData metadata) throws ConfigurationException, IOException {
        String metadataString = MetaData.builder().toXContent(metadata);
        CFMetaData cfm = Schema.instance.getCFMetaData(ELASTIC_ADMIN_KEYSPACE, metaDataTableName);
        cfm.comment(metadataString);
        MigrationManager.announceColumnFamilyUpdate(cfm, false, false);
        this.lastPulledMetaDataUuid = metadata.uuid();
        this.lastPulledMetaDataVersion = metadata.version();
    }

    /**
     * Should only be used after a SCHEMA change.
     */
    @Override
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        CFMetaData cfm = Schema.instance.getCFMetaData(ELASTIC_ADMIN_KEYSPACE, metaDataTableName);
        if (cfm != null) {
            try {
                String metadataString = cfm.getComment();
                if (metadataString != null && metadataString.length() > 0) {
                    XContentParser xparser = new JsonXContentParser(new JsonFactory().createParser(metadataString));
                    MetaData metaData = MetaData.Builder.fromXContent(xparser);
                    if (logger.isDebugEnabled()) {
                        logger.debug("recovered metadata from {}.{} = {}", ELASTIC_ADMIN_KEYSPACE, metaDataTableName, MetaData.Builder.toXContent(metaData));
                    }
                    synchronized (this) {
                        lastPulledMetaDataUuid = metaData.uuid();
                        lastPulledMetaDataVersion = metaData.version();
                    }
                    return metaData;
                }
            } catch (Exception e) {
                throw new NoPersistedMetaDataException(e);
            }
        }
        throw new NoPersistedMetaDataException();
    }

    private MetaData parseMetaDataString(String metadataString) throws NoPersistedMetaDataException {
        if (metadataString != null && metadataString.length() > 0) {
            MetaData metaData;
            try {
                XContentParser xparser = new JsonXContentParser(new JsonFactory().createParser(metadataString));
                metaData = MetaData.Builder.fromXContent(xparser);
                if (logger.isDebugEnabled()) {
                    logger.debug("recovered metadata from {}.{} = {}", ELASTIC_ADMIN_KEYSPACE, metaDataTableName, MetaData.Builder.toXContent(metaData));
                }
            } catch (IOException e) {
                throw new NoPersistedMetaDataException(e);
            }
            synchronized (this) {
                lastPulledMetaDataUuid = metaData.uuid();
                lastPulledMetaDataVersion = metaData.version();
            }
            return metaData;
        }
        throw new NoPersistedMetaDataException();
    }

    @Override
    public MetaData readMetaDataAsRow() throws NoPersistedMetaDataException {
        UntypedResultSet result;
        try {
            result = process(ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.LOCAL_SERIAL, String.format("SELECT metadata FROM \"%s\".\"%s\" WHERE dc = ?", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
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

    @Override
    public void initializeMetaDataAsComment() {
        MetaData metadata = clusterService.state().metaData();
        try {
            // initialize a first row if needed
            UntypedResultSet result = process(ConsistencyLevel.LOCAL_QUORUM, ConsistencyLevel.LOCAL_SERIAL,
                    String.format("INSERT INTO \"%s\".\"%s\" (dc,owner,version,metadata) VALUES (?,?,?,?) IF NOT EXISTS", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
                    DatabaseDescriptor.getLocalDataCenter(), UUID.fromString(StorageService.instance.getLocalHostId()), metadata.version(), MetaData.builder().toXContent(metadata));

            Row row = result.one();
            boolean applied = false;
            if (row.has("[applied]")) {
                applied = row.getBoolean("[applied]");
            }
            if (applied) {
                logger.debug("Succefully initialize metadata metaData={}", metadata);
                writeMetaDataAsComment(metadata);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize persisted metadata", e);
        }
    }

    private static final String updateMetaDataQuery = String.format("UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, metadata = ? WHERE dc = ? IF owner = ? AND version = ?", new Object[] {
            ELASTIC_ADMIN_KEYSPACE, metaDataTableName });

    /*
     * (non-Javadoc)
     * 
     * @see org.elasticsearch.cassandra.ElasticSchemaService#pushMetaData(org.
     * elasticsearch.cluster.ClusterState,
     * org.elasticsearch.cluster.ClusterState, java.lang.String)
     */
    @Override
    public void persistMetaData(MetaData oldMetaData, MetaData newMetaData, String source) throws IOException, InvalidRequestException, RequestExecutionException, RequestValidationException {
        if (!newMetaData.uuid().equals(clusterService.localNode().id())) {
            logger.error("should not push metadata updated from another node {}/{}", newMetaData.uuid(), newMetaData.version());
            return;
        }
        if (newMetaData.uuid().equals(clusterService.state().metaData().uuid()) && newMetaData.version() < clusterService.state().metaData().version()) {
            logger.warn("don't push obsolete metadata uuid={} version {} < {}", newMetaData.uuid(), newMetaData.version(), clusterService.state().metaData().version());
            return;
        }
        UntypedResultSet result = process(
                ConsistencyLevel.LOCAL_ONE,
                ConsistencyLevel.LOCAL_SERIAL,
                updateMetaDataQuery,
                new Object[] { UUID.fromString(clusterService.localNode().id()), newMetaData.version(), MetaData.builder().toXContent(newMetaData), DatabaseDescriptor.getLocalDataCenter(),
                        UUID.fromString(oldMetaData.uuid()), oldMetaData.version() });
        Row row = result.one();
        boolean applied = false;
        if (row.has("[applied]")) {
            applied = row.getBoolean("[applied]");
        }
        if (applied) {
            logger.debug("Succefully update metadata source={} newMetaData={}", source, MetaData.Builder.toXContent(newMetaData));
            writeMetaDataAsComment(newMetaData);
            return;
        } else {
            logger.warn("Failed to update metadata source={} oldMetadata={}/{} currentMetaData={}/{}", source, oldMetaData.uuid(), oldMetaData.version(), row.getUUID("owner"), row.getLong("version"));
            throw new ConcurrentMetaDataUpdateException(row.getUUID("owner"), row.getLong("version"));
        }
    }
    
    
    
}