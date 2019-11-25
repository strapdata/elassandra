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

package org.elassandra.cluster;

import com.google.common.collect.ImmutableList;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.CBuilder;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.SimpleDateSerializer;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.UUIDGen;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elassandra.index.ElasticSecondaryIndex;
import org.elassandra.index.mapper.internal.NodeFieldMapper;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.cluster.service.ClusterService.DocPrimaryKey;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.uid.VersionsAndSeqNoResolver.DocIdAndVersion;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.CqlMapper.CqlCollection;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.IndexShard;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class QueryManager extends AbstractComponent {
    private final ClusterService clusterService;

    public QueryManager(Settings settings, ClusterService clusterService) {
        super();
        this.clusterService = clusterService;
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
                Serializer.toXContent(builder, fieldMapper, field, docMap.get(field));
            } else {
                ObjectMapper objectMapper = documentMapper.objectMappers().get(field);
                if (objectMapper != null) {
                     if (forStaticDocument && !isStaticOrPartitionKey(objectMapper))
                         continue;
                     Serializer.toXContent(builder, objectMapper, field, docMap.get(field));
                } else {
                    Loggers.getLogger(ClusterService.class).error("No mapper found for field "+field);
                    throw new IOException("No mapper found for field "+field);
                }
            }
        }
        builder.endObject();
        return builder;
    }

    public static boolean isStaticOrPartitionKey(CqlMapper mapper) {
        return mapper.cqlStaticColumn() || mapper.cqlPartitionKey();
    }

    public boolean isStaticDocument(final IndexShard indexShard, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        CFMetaData metadata = SchemaManager.getCFMetaData(indexShard.mapperService().keyspace(),
            SchemaManager.typeToCfName(indexShard.mapperService().keyspace(), uid.type()));
        String id = uid.id();
        if (id.startsWith("[") && id.endsWith("]")) {
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(id, Object[].class);
            return metadata.clusteringColumns().size() > 0 && elements.length == metadata.partitionKeyColumns().size();
        } else {
            return metadata.clusteringColumns().size() != 0;
        }
    }

    public Map<String, DocumentField> flattenDocumentField(final String[] fieldFilter, final String path, final Object node, Map<String, DocumentField> flatFields) {
        if ((node instanceof List) || (node instanceof Set)) {
            for(Object o : ((Collection)node)) {
                flattenDocumentField(fieldFilter, path, o, flatFields);
            }
        } else if (node instanceof Map) {
            for (String key : ((Map<String,Object>)node).keySet()) {
                String fullname = (path.length() > 0) ? path + '.' + key : key;
                flattenDocumentField(fieldFilter, fullname, ((Map<String,Object>)node).get(key), flatFields);
            }
        } else {
            if (fieldFilter != null) {
                for (String f : fieldFilter) {
                    if (path.equals(f)) {
                        DocumentField gf = flatFields.get(path);
                        if (gf == null) {
                            gf = new DocumentField(path, ImmutableList.builder().add(node).build());
                        } else {
                            gf = new DocumentField(path, ImmutableList.builder().addAll(gf.getValues()).add(node).build());
                        }
                        flatFields.put(path, gf);
                        break;
                    }
                }
            }
        }
        return flatFields;
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
                List<Object> values = flatMap.get(path);
                if (values == null) {
                    values = new ArrayList<Object>();
                    flatMap.put(path, values);
                }
                values.add(node);
            }
        }
        return flatMap;
    }


    public BytesReference source(final IndexShard indexShard, DocumentMapper docMapper, Map sourceAsMap, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        if (docMapper.sourceMapper().enabled()  || indexShard.mapperService().getIndexMetaData().isOpaqueStorage()) {
            // retreive from _source columns stored as blob in cassandra if available.
            ByteBuffer bb = (ByteBuffer) sourceAsMap.get(SourceFieldMapper.NAME);
            if (bb != null)
               return new BytesArray(bb.array(), bb.position(), bb.limit() - bb.position());
        }
        // rebuild _source from all cassandra columns.
        XContentBuilder builder = buildDocument(docMapper, sourceAsMap, true, isStaticDocument(indexShard, uid));
        builder.humanReadable(true);
        return BytesReference.bytes(builder);
    }


    public BytesReference source(final IndexShard indexShard, DocumentMapper docMapper, Map sourceAsMap, String id) throws JsonParseException, JsonMappingException, IOException {
        return source( indexShard, docMapper, sourceAsMap, new Uid(docMapper.type(), id));
    }

    public Token getToken(final String ksName, final String type, final String routing) throws JsonParseException, JsonMappingException, IOException {
        DocPrimaryKey pk = parseElasticRouting(ksName, type, routing);
        CFMetaData cfm = SchemaManager.getCFMetaData(ksName, type);
        CBuilder builder = CBuilder.create(cfm.getKeyValidatorAsClusteringComparator());
        for (int i = 0; i < cfm.partitionKeyColumns().size(); i++)
            builder.add(pk.values[i]);
        return cfm.partitioner.getToken(CFMetaData.serializePartitionKey(builder.build()));
    }

    public Set<Token> getTokens(final String ksName, final String[] types, final String routing) throws JsonParseException, JsonMappingException, IOException {
        Set<Token> tokens = new HashSet<Token>();
        if (types != null && types.length > 0) {
            for(String type : types)
                tokens.add(getToken(ksName, type, routing));
        }
        return tokens;
    }

    public DocPrimaryKey parseElasticId(final String ksName, final String type, final String id) throws IOException {
        return parseElasticId(ksName, type, id, null);
    }

    /**
     * Parse elastic _id (a value or a JSON array) to build a DocPrimaryKey or populate map.
     */
    public DocPrimaryKey parseElasticId(final String ksName, final String type, final String id, Map<String, Object> map) throws JsonParseException, JsonMappingException, IOException {
        String cfName = SchemaManager.typeToCfName(ksName, type);
        CFMetaData metadata = SchemaManager.getCFMetaData(ksName, cfName);

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
                    values[i] = atype.compose( Serializer.fromString(atype, elements[i].toString()) );
                } else {
                    map.put(cd.name.toString(), atype.compose( Serializer.fromString(atype, elements[i].toString()) ) );
                }
            }
            return (map != null) ? null : new DocPrimaryKey(names, values, (clusteringColumns.size() > 0 && elements.length == partitionColumns.size()) ) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> atype = partitionColumns.get(0).type;
            if (map == null) {
                return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { atype.compose(Serializer.fromString(atype, id)) }, clusteringColumns.size() != 0);
            } else {
                map.put(partitionColumns.get(0).name.toString(), atype.compose( Serializer.fromString(atype, id) ) );
                return null;
            }
        }
    }

    public DocPrimaryKey parseElasticRouting(final String ksName, final String type, final String routing) throws JsonParseException, JsonMappingException, IOException {
        String cfName = SchemaManager.typeToCfName(ksName, type);
        CFMetaData metadata = SchemaManager.getCFMetaData(ksName, cfName);
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
                values[i] = atype.compose( Serializer.fromString(atype, elements[i].toString()) );
                i++;
            }
            return new DocPrimaryKey(names, values) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> atype = partitionColumns.get(0).type;
            return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { atype.compose( Serializer.fromString(atype, routing) ) });
        }
    }

    public boolean rowExists(final IndexShard indexShard, final String type, final DocPrimaryKey docPk)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        return this.clusterService.process(ConsistencyLevel.LOCAL_ONE,
            buildExistsQuery(indexShard.mapperService().documentMapper(type),
                indexShard.mapperService().keyspace(),
                SchemaManager.typeToCfName(indexShard.mapperService().keyspace(),
                    type)), docPk.values).size() > 0;
    }

    /**
     * Fetch from the coordinator node.
     */
    public UntypedResultSet fetchRow(final IndexShard indexShard, final String type, DocPrimaryKey docPk, final String[] columns, Map<String,ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        return fetchRow(indexShard, type, docPk, columns, ConsistencyLevel.LOCAL_ONE, columnDefs);
    }

    public UntypedResultSet fetchRow(final IndexShard indexShard, final String type, final  DocPrimaryKey docPk, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        return this.clusterService.process(cl, buildFetchQuery(indexShard, type, columns, docPk.isStaticDocument, columnDefs), docPk. values);
    }

    public Engine.GetResult fetchSourceInternal(final IndexShard indexShard, String type, String id, Map<String,ColumnDefinition> columnDefs, LongConsumer onRefresh) throws IOException {
        long time = System.nanoTime();
        DocPrimaryKey docPk = parseElasticId(indexShard.mapperService().keyspace(), type, id);
        UntypedResultSet result = fetchRowInternal(indexShard, type, docPk, columnDefs.keySet().toArray(new String[columnDefs.size()]), columnDefs);
        onRefresh.accept(System.nanoTime() - time);
        if (!result.isEmpty()) {
            return new Engine.GetResult(true, 1L, new DocIdAndVersion(0, 1L, 1L, 1L, null, 0), null);
        }
        return Engine.GetResult.NOT_EXISTS;
    }

    public UntypedResultSet fetchRowInternal(final IndexShard indexShard, final String cfName, final  DocPrimaryKey docPk, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException  {
        return fetchRowInternal(indexShard, cfName, columns, docPk.values, docPk.isStaticDocument, columnDefs);
    }

    public UntypedResultSet fetchRowInternal(final IndexShard indexShard, final String cfName, final String[] columns, final Object[] pkColumns, boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException  {
        return QueryProcessor.executeInternal(buildFetchQuery(indexShard, cfName, columns, forStaticDocument, columnDefs), pkColumns);
    }

    private String regularColumn(final IndexShard indexShard, final String type) throws IOException {
        if (indexShard != null) {
            DocumentMapper docMapper = indexShard.mapperService().documentMapper(type);
            if (docMapper != null) {
                for(Mapper fieldMapper : docMapper.mappers()) {
                    if (fieldMapper instanceof MetadataFieldMapper)
                        continue;
                    if (fieldMapper instanceof CqlMapper) {
                        CqlMapper mapper = (CqlMapper) fieldMapper;
                        if (mapper.cqlPrimaryKeyOrder() == -1 && !mapper.cqlStaticColumn() && mapper.cqlCollection() == CqlMapper.CqlCollection.SINGLETON) {
                            return fieldMapper.name();
                        }
                    }
                }
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("no regular columns for index=[{}] type=[{}]", indexShard.shardId().getIndex().getName(), type);
        return null;
    }

    public String buildFetchQuery(final IndexShard indexShard, final String type, final String[] requiredColumns, boolean forStaticDocument, Map<String, ColumnDefinition> columnDefs)
            throws IOException
    {
        DocumentMapper docMapper = indexShard.mapperService().documentMapper(type);
        String cfName = SchemaManager.typeToCfName(indexShard.mapperService().keyspace(), type);
        CFMetaData metadata = SchemaManager.getCFMetaData(indexShard.mapperService().keyspace(), cfName);
        DocumentMapper.CqlFragments cqlFragment = docMapper.getCqlFragments();
        String regularColumn = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        int prefixLength = query.length();

        for (String c : requiredColumns) {
            switch (c) {
                case TokenFieldMapper.NAME:
                    query.append(query.length() > 7 ? ',' : ' ')
                        .append("token(")
                        .append(cqlFragment.ptCols)
                        .append(") as \"_token\"");
                    break;
                case RoutingFieldMapper.NAME:
                    query.append(query.length() > 7 ? ',' : ' ')
                        .append((metadata.partitionKeyColumns().size() > 1) ? "toJsonArray(" : "toString(")
                        .append(cqlFragment.ptCols)
                        .append(") as \"_routing\"");
                    break;
                case "_ttl":
                    if (regularColumn == null)
                        regularColumn = regularColumn(indexShard, cfName);
                    if (regularColumn != null)
                        query.append(query.length() > 7 ? ',' : ' ').append("TTL(").append(regularColumn).append(") as \"_ttl\"");
                    break;
                case SeqNoFieldMapper.NAME:
                case "_timestamp":
                    if (regularColumn == null)
                        regularColumn = regularColumn(indexShard, cfName);
                    if (regularColumn != null)
                        query.append(query.length() > 7 ? ',' : ' ').append("WRITETIME(").append(regularColumn).append(") as \"_timestamp\"");
                    break;
                case ParentFieldMapper.NAME:
                    ParentFieldMapper parentMapper = docMapper.parentFieldMapper();
                    if (parentMapper.active()) {
                        query.append(query.length() > 7 ? ',' : ' ');
                        if (parentMapper.pkColumns() == null) {
                            // default column name for _parent should be string.
                            query.append("\"_parent\"");
                        } else {
                            query.append((parentMapper.pkColumns().indexOf(',') > 0) ? "toJsonArray(" : "toString(")
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
                        query.append(query.length() > prefixLength ? ',' : ' ').append("\"").append(c).append("\"");
                    }
            }
        }
        if (query.length() == prefixLength) {
            // no column match or requiredColumn is empty, add _id to avoid CQL syntax error...
            query.append( (metadata.partitionKeyColumns().size() > 1) ? "toJsonArray(" : "toString(" )
                .append(cqlFragment.ptCols)
                .append(") as \"_id\"");
        }
        query.append(" FROM \"").append(indexShard.mapperService().keyspace()).append("\".\"").append(cfName)
             .append("\" WHERE ").append((forStaticDocument) ? cqlFragment.ptWhere : cqlFragment.pkWhere )
             .append(" LIMIT 1");
        return query.toString();
    }

    public static String buildDeleteQuery(final DocumentMapper docMapper, final String ksName, final String cfName) {
        return "DELETE FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ docMapper.getCqlFragments().pkWhere;
    }

    public static String buildExistsQuery(final DocumentMapper docMapper, final String ksName, final String cfName) {
        return "SELECT "+docMapper.getCqlFragments().pkCols+" FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ docMapper.getCqlFragments().pkWhere;
    }


    public Engine.DeleteResult deleteRow(final IndexShard indexShard, final String type, final String id, final ConsistencyLevel cl) throws IOException {
        try {
            String ksName = indexShard.mapperService().keyspace();
            String cfName = SchemaManager.typeToCfName(indexShard.mapperService().keyspace(), type);
            DocumentMapper docMapper = indexShard.mapperService().documentMapper(type);
            this.clusterService.process(cl, buildDeleteQuery(docMapper, ksName, cfName), parseElasticId(ksName, type, id).values);
            return new Engine.DeleteResult( 1L, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, SequenceNumbers.UNASSIGNED_SEQ_NO, true);
        } catch(RequestExecutionException | RequestValidationException e) {
            return new Engine.DeleteResult(e, 1L, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, SequenceNumbers.UNASSIGNED_SEQ_NO, false);
        }
    }


    public Map<String, Object> rowAsMap(final IndexShard indexShard, final String type, UntypedResultSet.Row row) throws IOException {
        Map<String, Object> mapObject = new HashMap<String, Object>();
        rowAsMap(indexShard, type, row, mapObject);
        return mapObject;
    }


    public int rowAsMap(final IndexShard indexShard, final String type, UntypedResultSet.Row row, Map<String, Object> mapObject) throws IOException {
        Object[] values = rowAsArray(indexShard, type, row);
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


    public Object[] rowAsArray(final IndexShard indexShard, final String type, UntypedResultSet.Row row) throws IOException {
        return rowAsArray(indexShard, type, row, false);
    }

    private Object value(FieldMapper fieldMapper, Object rowValue, boolean valueForSearch) {
        if (fieldMapper != null) {
            final MappedFieldType fieldType = fieldMapper.fieldType();
            return (valueForSearch) ? fieldType.valueForDisplay( rowValue ) : fieldType.cqlValue( rowValue );
        } else {
            return rowValue;
        }
    }

    // TODO: return raw values if no mapper found.
    public Object[] rowAsArray(final IndexShard indexShard, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException {
        final Object values[] = new Object[row.getColumns().size()];
        final DocumentMapper documentMapper = indexShard.mapperService().documentMapper(type);
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
                    if (fieldMapper instanceof DateFieldMapper && fieldMapper.CQL3Type().equals(CQL3Type.Native.TIMESTAMP)) {
                        values[i] = value(fieldMapper, UUIDGen.unixTimestamp(row.getUUID(columnName)), valueForSearch);
                        break;
                    }
                    values[i] = row.getUUID(columnName).toString();
                    break;
                case UUID:
                    values[i] = row.getUUID(columnName).toString();
                    break;
                case TIMESTAMP:
                    values[i] = value(fieldMapper, row.getTimestamp(columnName).getTime(), valueForSearch);
                    break;
                case DATE:
                    values[i] = value(fieldMapper, SimpleDateSerializer.dayToTimeInMillis(row.getInt(columnName)), valueForSearch);
                    break;
                case TIME:
                    values[i] = value(fieldMapper, row.getLong(columnName), valueForSearch);
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
                case DECIMAL:
                    values[i] = value(fieldMapper, row.getDecimal(columnName), valueForSearch);
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
                    values[i] = value(fieldMapper, row.getInetAddress(columnName), valueForSearch);
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
                            list.add(Serializer.deserialize(elementType, bb, objectMapper));
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
                            set.add(Serializer.deserialize(elementType, bb, objectMapper));
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
                            map.put(key, Serializer.deserialize(elementType, lbb.get(key), objectMapper.getMapper(key)));
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
                values[i] = Serializer.deserialize(colSpec.type, bb, documentMapper.objectMappers().get(columnName));
            } else if (cql3Type instanceof CQL3Type.Custom) {
                logger.warn("CQL3.Custum type not supported for column "+columnName);
            }
            i++;
        }
        return values;
    }

    public Engine.IndexResult updateDocument(final IndexShard indexShard, final IndexRequest request, final IndexMetaData indexMetaData) throws IOException {
        return upsertDocument(indexShard, request, indexMetaData, true);
    }

    public Engine.IndexResult insertDocument(final IndexShard indexShard, final IndexRequest request, final IndexMetaData indexMetaData) throws IOException {
        return upsertDocument(indexShard, request, indexMetaData, false);
    }

    private Map<String, Object> updateField(Map<String, Object> node, String fieldName, Object fieldValue) {
        int idx;
        if ((idx = fieldName.indexOf(".")) == -1) {
            node.put(fieldName, fieldValue);
            return node;
        }
        HashMap<String, Object> subNode = (HashMap<String, Object>) node.computeIfAbsent(fieldName.substring(0, idx), k -> new HashMap<String, Object>());
        updateField(subNode, fieldName.substring(idx+1), fieldValue);
        return node;
    }

    /**
     * Convert an IndexRequest to a CQL insert
     */
    private Engine.IndexResult upsertDocument(final IndexShard indexShard, final IndexRequest request, final IndexMetaData indexMetaData, boolean updateOperation) throws IOException {
        final SourceToParse sourceToParse = SourceToParse.source(request.index(), request.type(), request.id(), request.source(), request.getContentType());
        if (request.routing() != null)
            sourceToParse.routing(request.routing());
        if (request.parent() != null)
            sourceToParse.parent(request.parent());

        // get the docMapper after a potential mapping update
        DocumentMapperForType docMapperForType = indexShard.mapperService().documentMapperWithAutoCreate(request.type());
        DocumentMapper docMapper = docMapperForType.getDocumentMapper();

        ParsedDocument doc = docMapper.parse(sourceToParse);
        Mapping mappingUpdate = doc.dynamicMappingsUpdate();

        if (mappingUpdate == null && !indexShard.mapperService().hasMapping(request.type())) {
            mappingUpdate = docMapper.mapping();
        }

        if (mappingUpdate != null) {
            if (logger.isDebugEnabled())
                logger.debug("Document source={} require a blocking mapping update of [{}] mapping={}",
                    request.sourceAsMap(), indexShard.shardId().getIndex().getName(), mappingUpdate);
            // retry done by the caller once the mapping is updated
            return new Engine.IndexResult(mappingUpdate);
        }

        final String keyspaceName = indexMetaData.keyspace();
        final String cfName = SchemaManager.typeToCfName(keyspaceName, request.type());

        // insert document into cassandra keyspace=index, table = type
        final Map<String, Object> sourceMap = new HashMap<>();
        for(Map.Entry<String, Object> entry : request.sourceAsMap().entrySet())
            updateField(sourceMap, entry.getKey(), entry.getValue());  // build a tree from keys #295

        final Map<String, ObjectMapper> objectMappers = docMapper.objectMappers();
        final DocumentFieldMappers fieldMappers = docMapper.mappers();


        if (logger.isTraceEnabled())
            logger.trace("Insert metadata.version={} index=[{}] table=[{}] id=[{}] source={} consistency={}",
                this.clusterService.state().metaData().version(),
                indexShard.shardId().getIndex().getName(), cfName, request.id(), sourceMap,
                request.waitForActiveShards().toCassandraConsistencyLevel());

        final CFMetaData cfm = SchemaManager.getCFMetaData(keyspaceName, cfName);

        String id = request.id();
        Map<String, ByteBuffer> map = new HashMap<String, ByteBuffer>();
        if (indexMetaData.isOpaqueStorage()) {
            map.put(IdFieldMapper.NAME, Serializer.serialize(request.index(), cfName, cfm.getColumnDefinition(docMapper.idFieldMapper().cqlName()).type, IdFieldMapper.NAME, id, docMapper.idFieldMapper()));
            map.put(SourceFieldMapper.NAME, Serializer.serialize(request.index(), cfName, cfm.getColumnDefinition(docMapper.sourceMapper().cqlName()).type, SourceFieldMapper.NAME, request.source(), docMapper.sourceMapper()));
        } else {
            if (request.parent() != null)
                sourceMap.put(ParentFieldMapper.NAME, request.parent());

            // normalize the _id and may find some column value in _id.
            // if the provided columns does not contains all the primary key columns, parse the _id to populate the columns in map.
            final Map<String, Object> idMap = new HashMap<>();
            this.parseElasticId(indexShard.mapperService().keyspace(), cfName, request.id(), idMap);
            sourceMap.putAll(idMap);

            // workaround because ParentFieldMapper.value() and UidFieldMapper.value() create an Uid.
            if (sourceMap.get(ParentFieldMapper.NAME) != null && ((String) sourceMap.get(ParentFieldMapper.NAME)).indexOf(Uid.DELIMITER) < 0) {
                sourceMap.put(ParentFieldMapper.NAME, request.type() + Uid.DELIMITER + sourceMap.get(ParentFieldMapper.NAME));
            }

            if (docMapper.sourceMapper().enabled()) {
                sourceMap.put(SourceFieldMapper.NAME, request.source());
            }

            for (String field : sourceMap.keySet()) {
                Mapper fieldMapper = field.startsWith(ParentFieldMapper.NAME) ? // workaround for _parent#<join_type>
                        docMapper.parentFieldMapper() :
                        fieldMappers.getMapper(field);
                final Mapper mapper = (fieldMapper != null) ? fieldMapper : objectMappers.get(field);
                final ByteBuffer colName = (mapper == null) ? ByteBufferUtil.bytes(field) : mapper.cqlName();    // cached ByteBuffer column name.
                final ColumnDefinition cd = cfm.getColumnDefinition(colName);
                if (cd != null) {
                    // we got a CQL column.
                    Object fieldValue = sourceMap.get(field);
                    try {
                        if (fieldValue == null) {
                            if (cd.type.isCollection()) {
                                switch (((CollectionType<?>) cd.type).kind) {
                                    case LIST:
                                    case SET:
                                        map.put(field, CollectionSerializer.pack(Collections.emptyList(), 0, ProtocolVersion.CURRENT));
                                        break;
                                    case MAP:
                                        break;
                                }
                            } else {
                                map.put(field, null);
                            }
                            continue;
                        }

                        if (fieldMapper instanceof CqlMapper) {
                            CqlMapper cqlMapper = (CqlMapper) fieldMapper;
                            if (cqlMapper != null && cqlMapper.cqlCollection().equals(CqlCollection.SINGLETON) && (fieldValue instanceof Collection)) {
                                throw new MapperParsingException("field " + fieldMapper.name() + " should be a single value");
                            }
                        }

                        // hack to store percolate query as a string while mapper is an object mapper.
                        if (cfm.cfName.equals("_percolator") && field.equals("query")) {
                            if (cd.type.isCollection()) {
                                switch (((CollectionType<?>) cd.type).kind) {
                                    case LIST:
                                        if (((ListType) cd.type).getElementsType().asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                            // opaque list of objects serialized to JSON text
                                            fieldValue = Collections.singletonList(Serializer.stringify(fieldValue));
                                        }
                                        break;
                                    case SET:
                                        if (((SetType) cd.type).getElementsType().asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                            // opaque set of objects serialized to JSON text
                                            fieldValue = Collections.singleton(Serializer.stringify(fieldValue));
                                        }
                                        break;
                                }
                            } else {
                                if (cd.type.asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                    // opaque singleton object serialized to JSON text
                                    fieldValue = Serializer.stringify(fieldValue);
                                }
                            }
                        }

                        map.put(field, Serializer.serialize(request.index(), cfName, cd.type, field, fieldValue, mapper));
                    } catch (Exception e) {
                        logger.error("[{}].[{}] failed to parse field {}={}", e, request.index(), cfName, field, fieldValue);
                        throw e;
                    }
                }
            }
        }

        String query;
        ByteBuffer[] values;
        if (request.opType() == DocWriteRequest.OpType.CREATE) {
            values = new ByteBuffer[map.size()];
            query = buildInsertQuery(keyspaceName, cfName, map, id,
                    true,
                    values, 0);
            final boolean applied = this.clusterService.processWriteConditional(request.waitForActiveShards().toCassandraConsistencyLevel(), ConsistencyLevel.LOCAL_SERIAL, query, (Object[])values);
            if (!applied)
                throw new VersionConflictEngineException(indexShard.shardId(), cfName, request.id(), "PAXOS insert failed, document already exists");
        } else {
            ElasticSecondaryIndex esi = ElasticSecondaryIndex.elasticSecondayIndices.get(keyspaceName+"."+cfName);
            ByteBuffer NULL_VALUE = (esi == null || !esi.isInsertOnly()) ? null : ByteBufferUtil.UNSET_BYTE_BUFFER;
            for(Mapper m : fieldMappers) {
                String fullname = m.name();
                if (map.get(fullname) == null && !fullname.startsWith("_") && fullname.indexOf('.') == -1 && cfm.getColumnDefinition(m.cqlName()) != null)
                    map.put(fullname, NULL_VALUE);
            }
            for(String m : objectMappers.keySet()) {
                if (map.get(m) == null && m.indexOf('.') == -1 && cfm.getColumnDefinition(objectMappers.get(m).cqlName()) != null)
                    map.put(m, NULL_VALUE);
            }
            values = new ByteBuffer[map.size()];
            query = buildInsertQuery(keyspaceName, cfName, map, id, false, values, 0);
            this.clusterService.process(request.waitForActiveShards().toCassandraConsistencyLevel(), query, (Object[])values);
        }

        assert request.versionType().validateVersionForWrites(request.version());
        return new Engine.IndexResult(1L, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, SequenceNumbers.UNASSIGNED_SEQ_NO, true);
    }

    /**
     * Build CQL insert query and populate values from the provided map.
     * TODO: cached prepared statements ?
     */
    public String buildInsertQuery(final String ksName,
            final String cfName,
            final Map<String, ByteBuffer> map,
            final String id,
            final boolean ifNotExists,
            ByteBuffer[] values,
            final int valuesOffset) {
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
        return query.toString();
    }

}
