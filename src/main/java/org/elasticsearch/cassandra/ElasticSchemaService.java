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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.Pair;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.action.index.XIndexRequest;
import org.elasticsearch.action.index.XIndexResponse;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.Bytes;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.fieldvisitor.FieldsVisitor;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentFieldMappers;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.FieldMappers;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;

public class ElasticSchemaService extends AbstractComponent implements SchemaService {

    public static String ELASTIC_ID_COLUMN_NAME = "_id";
    public static String MAPPING_UPDATE_TIMEOUT = "cassandra.mapping_update.timeout";

    private final ClusterService clusterService;
    private final MappingUpdatedAction mappingUpdatedAction;
    private final TimeValue mappingUpdateTimeout;

    // ElasticSearch to Cassandra mapping
    public Map<String, String> typeMapping = new java.util.HashMap<String, String>() {
        {
            put("string", "text");
            put("date", "timestamp");
            put("integer", "int");
            put("double", "double");
            put("float", "float");
            put("long", "bigint");
            put("short", "int");
            put("byte", "int");
            put("boolean", "boolean");
            put("binary", "blob");
            put("ip", "inet");
            put("geo_point", "geo_point");
            put("geo_shape", "geo_shape");
        }
    };

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
    public ElasticSchemaService(Settings settings, MappingUpdatedAction mappingUpdatedAction, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
        this.mappingUpdatedAction = mappingUpdatedAction;
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
                    DatabaseDescriptor.getLocalDataCenter(), replicationFactor), ConsistencyLevel.LOCAL_QUORUM);
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
            QueryProcessor.process(String.format("DROP KEYSPACE \"%s\";", index), ConsistencyLevel.LOCAL_QUORUM);
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
    public Object buildUDTValue(final String ksName, final String cfName, final String fieldName, final Object source, final Mapper mapper) throws SyntaxException, ConfigurationException,
            MapperParsingException {

        if (source instanceof Map) {
            // elasticsearch object type = cassandra UDT
            ByteBuffer bb = buildUDTValueForMap(ksName, cfName, fieldName, (Map<String, Object>) source, mapper);
            if (mapper.isSingleValue()) {
                return bb;
            } else {
                return ImmutableList.of(bb);
            }
        } else if (source instanceof List) {
            // elasticsearch object array = cassandra list<UDT>
            if (mapper.isSingleValue()) {
                throw new MapperParsingException("field " + mapper.name() + " should be a single value");
            }
            List<ByteBuffer> components = new ArrayList<ByteBuffer>();
            for (Object sourceObject : (List) source) {
                ByteBuffer bb = buildUDTValueForMap(ksName, cfName, fieldName, (Map<String, Object>) sourceObject, mapper);
                components.add(bb);
            }
            return components;
        }
        throw new MapperParsingException("unexpected object=" + source + " mapper=" + mapper.name());
    }

    public ByteBuffer buildUDTValueForMap(final String ksName, final String cfName, final String fieldName, final Map<String, Object> source, final Mapper mapper) throws SyntaxException,
            ConfigurationException {
        List<ByteBuffer> components = new ArrayList<ByteBuffer>();
        Pair<List<String>, List<String>> udtInfo = getUDTInfo(ksName, cfName + '_' + fieldName);
        for (int i = 0; i < udtInfo.left.size(); i++) {
            String fname = udtInfo.left.get(i);
            AbstractType type = TypeParser.parse(udtInfo.right.get(i));
            Object field_value = source.get(fname);
            ObjectMapper objectMapper = (ObjectMapper) mapper;
            components.add(buildTypeValue(ksName, cfName, type, fieldName+'_'+fname, field_value, objectMapper.mappers().get(fname)));
        }
        return TupleType.buildValue(components.toArray(new ByteBuffer[components.size()]));
    }

    public Object buildFieldValue(FieldMapper mapper, Object value) {
        if (mapper instanceof DateFieldMapper) {
            // workaround because elasticsearch manage Date as Long
            return new Date(((DateFieldMapper) mapper).value(value));
        } else if (mapper instanceof GeoPointFieldMapper) {
            GeoPoint point = ((GeoPointFieldMapper) mapper).value(value.toString());
            List<ByteBuffer> components = new ArrayList<ByteBuffer>();
            components.add(DoubleType.instance.decompose(point.lat()));
            components.add(DoubleType.instance.decompose(point.lon()));
            return TupleType.buildValue(components.toArray(new ByteBuffer[components.size()]));
        } else {
            return mapper.value(value);
        }
    }

    public ByteBuffer buildTypeValue(final String ksName, final String cfName, final AbstractType type, final String name, final Object value, final Mapper mapper) throws SyntaxException,
    ConfigurationException {
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
                components.add(buildTypeValue(ksName, cfName, subType, subName, subValue, subMapper));
            }
            return TupleType.buildValue(components.toArray(new ByteBuffer[components.size()]));
        } else if (type instanceof ListType) {
            if (!(value instanceof List)) {
                if (value instanceof Map) {
                    // build a singleton list of UDT 
                    return type.decompose(ImmutableList.of( buildUDTValueForMap(ksName, cfName, name, (Map<String, Object>) value, mapper) ) );
                } else {
                    return type.decompose(ImmutableList.of( mappedValue((FieldMapper) mapper, value) ));
                }
            } else {
                return type.decompose(value);
            }
        } else {
            // Native cassandra type,
            return type.decompose( mappedValue((FieldMapper) mapper, value) );
        }
    }

    public Object mappedValue(FieldMapper fieldMapper, Object value) {
        if (fieldMapper instanceof DateFieldMapper) {
            return new Date(((DateFieldMapper) fieldMapper).value(value));
        } 
        return fieldMapper.value(value);
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
    public String updateUDT(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {
        String typeName = cfName + "_" + objectMapper.fullPath().replace('.', '_');

        // create sub-type first
        for (Entry<String, Mapper> entry : objectMapper.mappers().entrySet()) {
            Mapper mapper = entry.getValue();
            if (mapper instanceof ObjectMapper) {
                updateUDT(ksName, cfName, entry.getKey(), (ObjectMapper) mapper);
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
                create.append(mapper.name()).append(" ");
                if (mapper instanceof ObjectMapper) {
                    if (!mapper.isSingleValue()) create.append("list<");
                    create.append("frozen<").append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_')).append(">");
                    if (!mapper.isSingleValue()) create.append(">");
                } else {
                    String cqlType = typeMapping.get(mapper.contentType());
                    if (mapper.isSingleValue()) {
                        create.append(cqlType);
                    } else {
                        create.append("list<");
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
            QueryProcessor.process(create.toString(), ConsistencyLevel.LOCAL_QUORUM);
        } else {
            // update existing UDT.
        }
        return typeName;
    }

    public void buildGeoPointType(String ksName) throws RequestExecutionException {
        String query = String.format("CREATE TYPE IF NOT EXISTS \"%s\".\"geo_point\" ( lat double, lon double )", ksName);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_QUORUM);
    }

    public void buildGeoShapeType(String ksName) {
        // TODO: Implements geo_shap.
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
            // TODO: use setting default replication factor
            createIndexKeyspace(index, 1);

            Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
            // Do not update schema when mapping is not the default one
            // (index.type == keyspace.table)
            if (targetPair == null) {

                CFMetaData cfm = Schema.instance.getCFMetaData(index, type);
                boolean newTable = (cfm == null);

                logger.debug("Inferring CQL3 schema {}.{} with columns={}", index, type, columns);
                StringBuilder columnsList = new StringBuilder();
                for (String column : columns) {
                    columnsList.append(',');
                    if (column.equals("_token")) {
                        continue; // ignore pseudo column known by Elasticsearch
                    }
                    String cqlType;
                    FieldMapper<?> fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                    if (fieldMapper != null) {
                        if (fieldMapper instanceof DateFieldMapper) {
                            // workaround because elasticsearch maps date to
                            // long.
                            cqlType = typeMapping.get("date");
                        } else {
                            cqlType = typeMapping.get(fieldMapper.contentType());
                        }
                        // create build-in geo types.
                        if (cqlType.equals("geo_point"))
                            buildGeoPointType(index);
                        else if (cqlType.equals("geo_shape"))
                            buildGeoShapeType(index);

                        if (!fieldMapper.isSingleValue()) {
                            if (isNativeCql3Type(cqlType)) {
                                cqlType = "list<" + cqlType + ">";
                            } else {
                                cqlType = "list<frozen<" + cqlType + ">>";
                            }
                        }
                    } else {
                        ObjectMapper objectMapper = docMapper.objectMappers().get(column);
                        if (objectMapper != null) {
                            cqlType = "frozen<" + updateUDT(index, type, column, objectMapper) + ">";
                            if (!objectMapper.isSingleValue()) {
                                cqlType = "list<" + cqlType + ">";
                            }
                        } else {
                            logger.warn("Cannot infer CQL type mapping for field {}", column);
                            continue;
                        }
                    }

                    if (cqlType != null) {
                        if (newTable) {
                            columnsList.append("\"").append(column).append("\" ").append(cqlType);
                        } else {
                            ColumnDefinition cdef = cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                            if (cdef == null) {
                                try {
                                    String query = String.format("ALTER TABLE \"%s\".\"%s\" ADD \"%s\" %s", index, type, column, cqlType);
                                    logger.debug(query);
                                    QueryProcessor.process(query, ConsistencyLevel.LOCAL_QUORUM);
                                } catch (Exception e) {
                                    logger.warn("Cannot alter table {}.{} column {} with type {}", e, index, type, column, cqlType);
                                }
                            }
                            if ((cdef == null) || (cdef.getIndexType() == null)) {
                                QueryProcessor.process(String.format("CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" (\"%s\") USING '%s' WITH OPTIONS = {'%s': '%s.%s'}",
                                        buildIndexName(type, column), index, type, column, org.elasticsearch.cassandra.ElasticSecondaryIndex.class.getName(),
                                        ElasticSecondaryIndex.ELASTIC_OPTION_TARGETS, index, type), ConsistencyLevel.LOCAL_QUORUM);
                            }
                        }
                    } else {
                        logger.error("table {}.{}, unkown CQL type for column={} type={}", index, type, column, fieldMapper.contentType());
                    }
                }

                if (newTable) {
                    String query = String.format("CREATE TABLE IF NOT EXISTS \"%s\".\"%s\" ( \"%s\" text PRIMARY KEY %s ) WITH COMMENT='Auto-created by Elassandra'", index, type,
                            ELASTIC_ID_COLUMN_NAME, columnsList);
                    logger.debug(query);
                    QueryProcessor.process(query, ConsistencyLevel.LOCAL_QUORUM);
                    for (String column : columns) {
                        if (column.equals("_token")) {
                            continue; // ignore pseudo column known by
                                      // Elasticsearch
                        }
                        QueryProcessor.process(String.format("CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" (\"%s\") USING '%s' WITH OPTIONS = {'%s': '%s.%s'}",
                                buildIndexName(type, column), index, type, column, org.elasticsearch.cassandra.ElasticSecondaryIndex.class.getName(), ElasticSecondaryIndex.ELASTIC_OPTION_TARGETS,
                                index, type), ConsistencyLevel.LOCAL_QUORUM);
                    }
                }
            }
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public String buildIndexName(final String cfName, final String colName) {
        return new StringBuilder("elastic_").append(cfName).append('_').append(colName.replace('.', '_')).append("_idx").toString();
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
            List<ColumnDefinition> clusterKeys = metadata.clusteringColumns();
            ImmutableList.Builder builder = new ImmutableList.Builder().addAll(metadata.partitionKeyColumns());
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

    public Map<String, GetField> flattenGetField(final String[] fieldFilter, final String path, final Map<String, Object> map, Map<String, GetField> fields) {
        for (Entry<String, Object> entry : map.entrySet()) {
            String fullname = (path.length() > 0) ? path + '.' + entry.getKey() : entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenGetField(fieldFilter, fullname, (Map<String, Object>) entry.getValue(), fields);
            } else {
                // leaf node
                if (fieldFilter != null) {
                    boolean found = false;
                    for (String f : fieldFilter) {
                        if (fullname.equals(f)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        continue;
                }

                GetField value = fields.get(fullname);
                if (value == null) {
                    value = new GetField(fullname, (entry.getValue() instanceof List) ? (List) entry.getValue() : ImmutableList.of(entry.getValue()));
                    fields.put(fullname, value);
                } else {
                    value = new GetField(fullname, ImmutableList.builder().addAll(value.getValues()).add(entry.getValue()).build());
                }
            }
        }
        return fields;
    }

    public Map<String, List<Object>> flattenObject(final Set<String> neededFiedls, final String path, final Map<String, Object> map, Map<String, List<Object>> fields) {
        for (Entry<String, Object> entry : map.entrySet()) {
            String fullname = (path.length() > 0) ? path + '.' + entry.getKey() : entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenObject(neededFiedls, fullname, (Map<String, Object>) entry.getValue(), fields);
            } else {
                // leaf node
                if ((neededFiedls == null) || (neededFiedls.contains(fullname))) {
                    List<Object> values = (List<Object>) fields.get(fullname);
                    if (values == null) {
                        values = new ArrayList<Object>();
                        fields.put(fullname, values);
                    }
                    values.add(entry.getValue());
                }
            }
        }
        return fields;
    }

    /**
     * Return list of columns having an ElasticSecondaryIndex.
     */
    @Override
    public List<String> cassandraMappedColumns(String ksName, String cfName) {
        ArrayList<String> columns = new ArrayList<String>();
        try {
            UntypedResultSet result = QueryProcessor.executeInternal("SELECT  column_name, index_type FROM system.schema_columns WHERE keyspace_name=? and columnfamily_name=?", new Object[] { ksName,
                    cfName });
            for (Row row : result) {
                if (row.has("index_type") && "CUSTOM".equals(row.getString("index_type"))) {
                    columns.add(row.getString("column_name"));
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to retreive column names from " + ksName + "." + cfName, e);
        }
        return columns;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#cassandraColumns(org
     * .elasticsearch.index.mapper.MapperService, java.lang.String)
     */
    @Override
    public String[] cassandraColumns(MapperService mapperService, String type) {
        List<String> cols = cassandraMappedColumns(mapperService.index().getName(), type);
        return cols.toArray(new String[cols.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#fetchRow(java.lang.String
     * , java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public UntypedResultSet fetchRow(final String index, final String type, final String id, final List<String> requiredColumns) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException {
        return fetchRow(index, type, id, requiredColumns, ConsistencyLevel.LOCAL_ONE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#fetchRow(java.lang.String
     * , java.lang.String, java.lang.String, java.util.List,
     * org.apache.cassandra.db.ConsistencyLevel)
     */
    @Override
    public UntypedResultSet fetchRow(final String index, final String type, final String id, final List<String> requiredColumns, final ConsistencyLevel cl) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
        String ksName = (targetPair == null) ? index : targetPair.left;
        String cfName = (targetPair == null) ? type : targetPair.right;

        StringBuilder pkCols = new StringBuilder();
        StringBuilder pkWhere = new StringBuilder();
        buildPrimaryKeyFragment(ksName, cfName, pkCols, pkWhere);

        StringBuilder columns = new StringBuilder();
        for (String c : requiredColumns) {
            if (columns.length() > 0)
                columns.append(',');
            if (c.equals("_token")) {
                columns.append("token(").append(pkCols).append(") as \"_token\"");
            } else {
                columns.append("\"").append(c).append("\"");
            }
        }
        String query = String.format("SELECT %s FROM \"%s\".\"%s\" WHERE %s", new Object[] { columns, ksName, cfName, pkWhere });
        return process(cl, query, elasticIdToPrimaryKeyObject(ksName, cfName, id));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#rowAsMap(org.apache.
     * cassandra.cql3.UntypedResultSet.Row,
     * org.elasticsearch.index.fieldvisitor.FieldsVisitor,
     * org.elasticsearch.index.mapper.MapperService, java.lang.String[])
     */
    @Override
    public Map<String, Object> rowAsMap(UntypedResultSet.Row row, FieldsVisitor fieldVisitor, MapperService mapperService, String[] types) {
        Map<String, Object> mapObject = new HashMap<String, Object>();
        List<ColumnSpecification> columnSpecs = row.getColumns();

        for (int columnIndex = 0; columnIndex < columnSpecs.size(); columnIndex++) {
            ColumnSpecification colSpec = columnSpecs.get(columnIndex);
            String columnName = colSpec.name.toString();
            CQL3Type cql3Type = colSpec.type.asCQL3Type();
            FieldMappers mappers = mapperService.smartNameFieldMappers(columnName, types);

            if (!row.has(colSpec.name.toString()))
                continue;

            if (cql3Type instanceof CQL3Type.Native) {
                switch ((CQL3Type.Native) cql3Type) {
                case ASCII:
                case TEXT:
                case VARCHAR:
                    mapObject.put(columnName, row.getString(colSpec.name.toString()));
                    break;
                case TIMEUUID:
                case UUID:
                    mapObject.put(columnName, row.getUUID(colSpec.name.toString()));
                    break;
                case TIMESTAMP:
                    // Timestamp+Date are stored as Long in ElasticSearch
                    // DateFieldMapper dateMapper =
                    // (DateFieldMapper)mapperService.smartNameFieldMappers(columnName).mapper();
                    // mapObject.put(columnName,
                    // dateMapper.dateTimeFormatter().printer().print(row.getTimestamp(colSpec.name.toString()).getTime()));
                    mapObject.put(columnName, row.getTimestamp(colSpec.name.toString()).getTime());
                    break;
                case INT:
                    mapObject.put(columnName, row.getInt(colSpec.name.toString()));
                    break;
                case BIGINT:
                    mapObject.put(columnName, row.getLong(colSpec.name.toString()));
                    break;
                case FLOAT:
                    break;
                case DECIMAL:
                case DOUBLE:
                    mapObject.put(columnName, row.getDouble(colSpec.name.toString()));
                    break;
                case BLOB:
                    mapObject.put(columnName, row.getBytes(colSpec.name.toString()));
                    break;
                case BOOLEAN:
                    mapObject.put(columnName, row.getBoolean(colSpec.name.toString()));
                    break;
                case COUNTER:
                    break;
                case INET:
                    mapObject.put(columnName, row.getInetAddress(colSpec.name.toString()));
                    break;
                default:
                    logger.warn("Ignoring unsupported type {}", cql3Type);
                }
            } else if (cql3Type.isCollection()) {
                switch (((CollectionType<?>) colSpec.type).kind) {
                case LIST:
                    List list;
                    AbstractType<?> elementType = ((ListType<?>) colSpec.type).getElementsType();
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
                        List dateList = new ArrayList<Long>(list.size());
                        for (Object date : list) {
                            dateList.add(((Date) date).getTime());
                        }
                        list = dateList;
                    }
                    if (list.size() == 1) {
                        mapObject.put(columnName, list.get(0));
                    } else if (list.size() > 1) {
                        mapObject.put(columnName, list);
                    }
                    break;
                }
            } else if (colSpec.type instanceof UserType) {
                ByteBuffer bb = row.getBytes(colSpec.name.toString());
                mapObject.put(columnName, ElasticSecondaryIndex.deserialize(colSpec.type, bb));
            } else if (cql3Type instanceof CQL3Type.Custom) {

            }
        }
        return mapObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.elasticsearch.cassandra.ElasticSchemaService#deleteRow(java.lang.
     * String, java.lang.String, java.lang.String,
     * org.apache.cassandra.db.ConsistencyLevel)
     */
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
        process(cl, query, elasticIdToPrimaryKeyObject(ksName, cfName, id));
    }

    private static class MappingUpdateListener implements MappingUpdatedAction.MappingUpdateListener {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Throwable error = null;

        @Override
        public void onMappingUpdate() {
            latch.countDown();
        }

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
            // blocking Elasticsearch mapping update (required to update
            // cassandra schema, this is the cost of dynamic mapping)
            MappingUpdateListener mappingUpdateListener = new MappingUpdateListener();
            mappingUpdatedAction.updateMappingOnMaster(request.index(), index.docMapper(), indexService.indexUUID(), mappingUpdateListener);
            mappingUpdateListener.waitForMappingUpdate(mappingUpdateTimeout);
            // check CQL3 table schema before insert
            // updateTableSchema(request.index(), request.type(),
            // sourceMap.keySet() ,index.docMapper());
        }

        // insert document into cassandra keyspace=index, table = type
        Map<String, Object> sourceMap = request.sourceAsMap();
        Map<String, ObjectMapper> objectMappers = index.docMapper().objectMappers();
        DocumentFieldMappers fieldMappers = index.docMapper().mappers();

        logger.debug("Insert id={} source={} mapping_updated={} fieldMappers={} objectMappers={} consistency={} ttl={}", request.id(), sourceMap, mappingUpdated,
                Arrays.toString(fieldMappers.toArray()), objectMappers, request.consistencyLevel().toCassandraConsistencyLevel(), request.ttl());

        List<String> columns = new ArrayList<String>(sourceMap.size());
        List<Object> values = new ArrayList<Object>(sourceMap.size());
        for (String field : sourceMap.keySet()) {
            Object fieldValue = sourceMap.get(field);
            ObjectMapper objectMapper = objectMappers.get(field);
            FieldMapper<?> fieldMapper = fieldMappers.smartNameFieldMapper(field);
            if (fieldValue == null) {
                // Can insert null value only when mapping is known
                if (fieldMapper != null) {
                    columns.add(field);
                    values.add((fieldMapper.isSingleValue()) ? null : ImmutableList.of());
                } else if (objectMapper != null) {
                    columns.add(field);
                    values.add((objectMapper.isSingleValue()) ? null : ImmutableList.of());
                }
                continue;
            }

            if (objectMapper != null) {
                columns.add(field);
                values.add(buildUDTValue(request.index(), request.type(), field, fieldValue, objectMapper));
            } else if (fieldMapper != null) {
                columns.add(field);
                if (fieldMapper.isSingleValue() && (fieldValue instanceof List)) {
                    throw new MapperParsingException("field " + fieldMapper.name() + " should be a single value");
                }
                ;
                if (fieldMapper.isSingleValue()) {
                    values.add(buildFieldValue(fieldMapper, fieldMapper.value(fieldValue)));
                } else {
                    List<Object> fieldValueList2 = new ArrayList();
                    if (fieldValue instanceof List) {
                        List<Object> fieldValueList = (List) fieldValue;
                        for (Object o : fieldValueList) {
                            fieldValueList2.add(buildFieldValue(fieldMapper, fieldMapper.value(o)));
                        }
                    } else {
                        fieldValueList2.add(buildFieldValue(fieldMapper, fieldValue));
                    }
                    values.add(fieldValueList2);
                }
            } else {
                logger.debug("Ignoring unmapped field {} = {} ", field, fieldMapper.value(fieldValue));
            }
        }

        return insertRow(request.index(), request.type(), columns.toArray(new String[columns.size()]), values.toArray(new Object[values.size()]), request.id(),
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
    public String insertRow(final String index, final String type, final String[] columns, Object[] values, final String id, final boolean ifNotExists, final long ttl, final ConsistencyLevel cl,
            Long writetime, Boolean applied) throws Exception {
        Pair<String, String> targetPair = ElasticSecondaryIndex.mapping.get(Pair.<String, String> create(index, type));
        String ksName = (targetPair == null) ? index : targetPair.left;
        String cfName = (targetPair == null) ? type : targetPair.right;

        List<String> pkColumns = getPrimaryKeyColumnsName(ksName, cfName);
        List<Object> allValues = new ArrayList<Object>(values.length + pkColumns.size());
        StringBuilder sbCols = new StringBuilder();
        StringBuilder questionsMarks = new StringBuilder();
        String generatedId = id;
        if (pkColumns.size() == 1) {
            // Auto-generated id supported for type PK text only
            if (id == null) {
                generatedId = UUID.randomUUID().toString();
            }
            String pkColumn = pkColumns.get(0);
            boolean pkIncluded = false;
            int i = 0;
            for (String col : columns) {
                if (col.equals(pkColumn))
                    pkIncluded = true;
                if (sbCols.length() > 0) {
                    sbCols.append(',');
                    questionsMarks.append(',');
                }
                sbCols.append("\"").append(col).append("\"");
                questionsMarks.append('?');
                allValues.add(values[i]);
                i++;
            }
            if (!pkIncluded) {
                if (sbCols.length() > 0) {
                    sbCols.append(',');
                    questionsMarks.append(',');
                }
                sbCols.append("\"").append(pkColumn).append("\"");
                questionsMarks.append('?');
                allValues.add(generatedId);
            }
            values = allValues.toArray(new Object[allValues.size()]);
        } else {
            // for composite key, primary columns should be included in provided
            // columns (or may be base64 encoded in id).
            // PK columns should be provided, build the encoded id.
            if (id == null) {
                Object[] pkValues = new Object[pkColumns.size()];
                int j = 0;
                for (String pkColumn : pkColumns) {
                    for (int i = 0; i < columns.length; i++) {
                        if (columns[i].equals(pkColumn)) {
                            pkValues[j++] = values[i];
                            break;
                        }
                    }
                }
                if (j != pkColumns.size()) {
                    logger.error("Missing primary a key column to build unique id {}", Arrays.toString(columns));
                    throw new ElasticsearchIllegalArgumentException("Missing primary key column to build unique id " + Arrays.toString(columns));
                }
                generatedId = getElasticId(ksName, cfName, pkValues);
            }
            for (String col : columns) {
                if (sbCols.length() > 0) {
                    sbCols.append(',');
                    questionsMarks.append(',');
                }
                sbCols.append("\"").append(col).append("\"");
                questionsMarks.append('?');
            }
        }
        String query = String.format("INSERT INTO \"%s\".\"%s\" (%s) VALUES (%s) %s %s", ksName, cfName, sbCols.toString(), questionsMarks.toString(), (ifNotExists) ? "IF NOT EXISTS" : "",
                (ttl > 0) ? "USING TTL " + Long.toString(ttl) : "");
        try {
            UntypedResultSet result = process(cl, query, values);
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
            return generatedId;
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
                        ArrayList sourceData = new ArrayList();
                        Object[] pkValues = new Object[pkColums.size()];
                        if (cql3Type instanceof CQL3Type.Native) {
                            int pkIdx = pkColums.indexOf(columnName);

                            switch ((CQL3Type.Native) cql3Type) {
                            case ASCII:
                            case TEXT:
                            case VARCHAR:
                                sourceData.add(columnName);
                                sourceData.add(row.getString(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getString(columnName);
                                break;
                            case TIMEUUID:
                            case UUID:
                                sourceData.add(columnName);
                                sourceData.add(row.getUUID(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getUUID(columnName);
                                break;
                            case TIMESTAMP:
                                sourceData.add(columnName);
                                sourceData.add(row.getTimestamp(columnName).getTime());
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getTimestamp(columnName).getTime();
                                break;
                            case VARINT:
                            case INT:
                            case BIGINT:
                                sourceData.add(columnName);
                                sourceData.add(row.getLong(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getLong(columnName);
                                break;
                            case FLOAT:
                                break;
                            case DECIMAL:
                            case DOUBLE:
                                sourceData.add(columnName);
                                sourceData.add(row.getDouble(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getDouble(columnName);
                                break;
                            case BLOB:
                                sourceData.add(columnName);
                                sourceData.add(row.getBytes(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getBytes(columnName);
                                break;
                            case BOOLEAN:
                                sourceData.add(columnName);
                                sourceData.add(row.getBoolean(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getBoolean(columnName);
                                break;
                            case COUNTER:
                                break;
                            case INET:
                                sourceData.add(columnName);
                                sourceData.add(row.getInetAddress(columnName));
                                if (pkIdx >= 0)
                                    pkValues[pkIdx] = row.getInetAddress(columnName);
                                break;
                            }
                        } else if (cql3Type.isCollection()) {
                            // TODO: mapping from list to elastic arrays.
                        } else if (cql3Type instanceof CQL3Type.Custom) {

                        }
                        index(index, type, getElasticId(ksName, cfName, pkValues), sourceData.toArray());
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

    public Object[] elasticIdToPrimaryKeyObject(String ksName, String cfName, String id) throws IOException {
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        CType validator = metadata.getKeyValidatorAsCType();
        if (validator.isCompound()) {
            ByteBuffer bb = Bytes.fromHexString(id);
            Composite composite = validator.fromByteBuffer(bb);
            Object[] bbArray = new ByteBuffer[composite.size()];
            for (int i = 0; i < composite.size(); i++) {
                bbArray[i] = validator.subtype(i).compose(composite.get(i));
            }
            return bbArray;
        }
        return new Object[] { id };
    }

    // for composite PK, _id = hex encoded PK
    // for non-composite PK, _id = pk.toString()
    public String getElasticId(String ksName, String cfName, Object[] colValues) {
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        CType validator = metadata.getKeyValidatorAsCType();
        Composite key = metadata.getKeyValidatorAsCType().make(colValues);
        return Bytes.toRawHexString(key.toByteBuffer());
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
    public void createElasticAdminKeyspace() {
        try {
            QueryProcessor.process(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };", ELASTIC_ADMIN_KEYSPACE),
                    ConsistencyLevel.LOCAL_QUORUM);
            QueryProcessor.process(String.format("CREATE TABLE IF NOT EXISTS %s.\"%s\" ( dc text PRIMARY KEY, owner uuid, version bigint, metadata text);", ELASTIC_ADMIN_KEYSPACE, metaDataTableName),
                    ConsistencyLevel.LOCAL_QUORUM);

        } catch (Throwable e) {
            logger.error("Failed to create " + ELASTIC_ADMIN_KEYSPACE + " keyspace or table " + metaDataTableName, e);
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