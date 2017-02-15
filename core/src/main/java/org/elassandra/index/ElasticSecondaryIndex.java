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
package org.elassandra.index;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.Operator;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.statements.IndexTarget;
import org.apache.cassandra.db.Clustering;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.DeletionTime;
import org.apache.cassandra.db.LivenessInfo;
import org.apache.cassandra.db.PartitionColumns;
import org.apache.cassandra.db.RangeTombstone;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.Slice;
import org.apache.cassandra.db.Slice.Bound;
import org.apache.cassandra.db.filter.RowFilter;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.DecimalType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.db.partitions.PartitionIterator;
import org.apache.cassandra.db.partitions.PartitionUpdate;
import org.apache.cassandra.db.rows.Cell;
import org.apache.cassandra.db.rows.CellPath;
import org.apache.cassandra.db.rows.Row;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.index.IndexRegistry;
import org.apache.cassandra.index.transactions.IndexTransaction;
import org.apache.cassandra.index.transactions.IndexTransaction.Type;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.concurrent.OpOrder;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CloseableThreadLocal;
import org.apache.lucene.util.NumericUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.all.AllEntries;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.Engine.DeleteByQuery;
import org.elasticsearch.index.engine.Engine.Operation;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.geo.BaseGeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapperLegacy;
import org.elasticsearch.index.mapper.geo.GeoShapeFieldMapper;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.SourceFieldMapper;
import org.elasticsearch.index.mapper.internal.TypeFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper.Defaults;
import org.elasticsearch.index.mapper.internal.VersionFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.percolator.PercolatorService;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * Custom secondary index for CQL3 only, should be created when mapping is applied and local shard started.
 * Index rows as documents when Elasticsearch clusterState has no write blocks and local shard is started.
 * 
 * ExtendedElasticSecondaryIndex directly build lucene fields without building a JSON document parsed by Elasticsearch.
 * 
 * @author vroyer
 *
 */
public class ElasticSecondaryIndex implements Index, ClusterStateListener {
    
    private final static SourceToParse EMPTY_SOURCE_TO_PARSE= SourceToParse.source((XContentParser)null);
    private final static Field DEFAULT_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, -1L);

    public static final Map<String, ElasticSecondaryIndex> elasticSecondayIndices = Maps.newConcurrentMap();
    public static final Pattern TARGET_REGEX = Pattern.compile("^(keys|entries|values|full)\\((.+)\\)$");
    
    public static boolean runsElassandra = false;
    public static boolean userKeyspaceInitialized = false;
    
    final String index_name;             // keyspace_name.table_name
    final ESLogger logger;
    final ClusterService clusterService;
    
    // updated when create/open/close/remove an ES index.
    protected ReadWriteLock mappingInfoLock = new ReentrantReadWriteLock();
    protected volatile MappingInfo mappingInfo;
    
    public final ColumnFamilyStore baseCfs;
    protected IndexMetadata indexMetadata;
    protected Set<ColumnDefinition> indexedColumns = Sets.newConcurrentHashSet();
    protected AtomicBoolean initialized = new AtomicBoolean(false);
    
    ElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        this.baseCfs = baseCfs;
        this.indexMetadata = indexDef;
        this.index_name = baseCfs.keyspace.getName()+"."+baseCfs.name;
        this.logger = Loggers.getLogger(this.getClass().getName()+"."+baseCfs.keyspace.getName()+"."+baseCfs.name);
        // clusterService must be started before creating 2i.
        this.clusterService = ElassandraDaemon.injector().getInstance(ClusterService.class);
        this.clusterService.addPost(this);
    }
    
    public static ElasticSecondaryIndex newElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        ElasticSecondaryIndex esi = elasticSecondayIndices.computeIfAbsent(baseCfs.keyspace.getName()+"."+baseCfs.name, K -> new ElasticSecondaryIndex(baseCfs, indexDef));
        esi.indexedColumns.add(parseTarget(baseCfs.metadata, indexDef).left);
        return esi;
    }
    
    // Public because it's also used to convert index metadata into a thrift-compatible format
    public static Pair<ColumnDefinition, IndexTarget.Type> parseTarget(CFMetaData cfm,
                                                                       IndexMetadata indexDef)
    {
        String target = indexDef.options.get("target");
        assert target != null : String.format(Locale.ROOT,"No target definition found for index %s", indexDef.name);

        // if the regex matches then the target is in the form "keys(foo)", "entries(bar)" etc
        // if not, then it must be a simple column name and implictly its type is VALUES
        Matcher matcher = TARGET_REGEX.matcher(target);
        String columnName;
        IndexTarget.Type targetType;
        if (matcher.matches())
        {
            targetType = IndexTarget.Type.fromString(matcher.group(1));
            columnName = matcher.group(2);
        }
        else
        {
            columnName = target;
            targetType = IndexTarget.Type.VALUES;
        }

        // in the case of a quoted column name the name in the target string
        // will be enclosed in quotes, which we need to unwrap. It may also
        // include quote characters internally, escaped like so:
        //      abc"def -> abc""def.
        // Because the target string is stored in a CQL compatible form, we
        // need to un-escape any such quotes to get the actual column name
        if (columnName.startsWith("\""))
        {
            columnName = StringUtils.substring(StringUtils.substring(columnName, 1), 0, -1);
            columnName = columnName.replaceAll("\"\"", "\"");
        }

        // if it's not a CQL table, we can't assume that the column name is utf8, so
        // in that case we have to do a linear scan of the cfm's columns to get the matching one
        if (cfm.isCQLTable())
            return Pair.create(cfm.getColumnDefinition(new ColumnIdentifier(columnName, true)), targetType);
        else
            for (ColumnDefinition column : cfm.allColumns())
                if (column.name.toString().equals(columnName))
                    return Pair.create(column, targetType);

        throw new RuntimeException(String.format(Locale.ROOT,"Unable to parse targets for index %s (%s)", indexDef.name, target));
    }
    
    // reusable per thread context
    private CloseableThreadLocal<Context> perThreadContext = new CloseableThreadLocal<Context>() {
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };
    
    abstract class FilterableDocument extends ParseContext.Document implements Predicate<IndexableField> {
        boolean applyFilter = false; 
        
        public FilterableDocument(String path, Document parent) {
            super(path, parent);
        }
        
        public FilterableDocument() {
            super();
        }
        
        public void applyFilter(boolean apply) {
            applyFilter = apply;
        }
        
        @Override
        abstract public boolean apply(IndexableField input);
        
        @Override
        public Iterator<IndexableField> iterator() {
            if (applyFilter) {
                return Iterators.filter(super.iterator(), this);
            } else {
                return super.iterator();
            }
        }
    }
    
    class Context extends ParseContext {
        private MappingInfo.IndexInfo indexInfo;
        private final ContentPath path = new ContentPath(0);
        private DocumentMapper docMapper;
        private Document document;
        private StringBuilder stringBuilder = new StringBuilder();
        private String id;
        private String parent;
        private Field version, uid;
        private final List<Document> documents = new ArrayList<Document>();
        private AllEntries allEntries = new AllEntries();
        private float docBoost = 1.0f;
        private Mapper dynamicMappingsUpdate = null;
        
        private boolean hasStaticField = false;
        private boolean finalized = false;
        private BytesReference source;
        private Object externalValue = null;
        
        public Context() {
        }
        
        public Context(MappingInfo.IndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(uid.type());
            assert this.docMapper != null;
            this.document = ii.indexStaticOnly() ? new StaticDocument("",null, uid) : new Document();
            this.documents.add(this.document);
        }
        
        public void reset(MappingInfo.IndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(uid.type());
            assert this.docMapper != null;
            this.document = ii.indexStaticOnly() ? new StaticDocument("",null, uid) : new Document();
            this.documents.clear();
            this.documents.add(this.document);
            this.id = null;
            this.path.reset();
            this.allEntries = new AllEntries();
            this.docBoost = 1.0f;
            this.dynamicMappingsUpdate = null;
            this.parent = null;
            this.version = DEFAULT_VERSION;
            this.externalValue = null;
        }
    
        // recusivelly add fields
        public void addField(Mapper mapper, Object value) throws IOException {
            if (value == null) 
                return;
            
            if (value instanceof Collection) {
                // flatten list or set of fields
                for(Object v : (Collection)value)
                    addField(mapper, v);
                return;
            }
            
            if (logger.isTraceEnabled())
                logger.trace("doc[{}] class={} name={} value={}", this.documents.indexOf(doc()), mapper.getClass().getSimpleName(), mapper.name(), value);
            
            if (mapper instanceof GeoShapeFieldMapper) {
                GeoShapeFieldMapper geoShapeMapper = (GeoShapeFieldMapper) mapper;
                XContentParser parser = XContentType.JSON.xContent().createParser((String)value);
                parser.nextToken();
                ShapeBuilder shapeBuilder = ShapeBuilder.parse(parser, geoShapeMapper);
                externalValue = shapeBuilder.build();
                path().add(mapper.name());
                geoShapeMapper.parse(this);
                path().remove();
                externalValue = null;
            } else if (mapper instanceof GeoPointFieldMapper || mapper instanceof GeoPointFieldMapperLegacy) {
                BaseGeoPointFieldMapper geoPointFieldMapper = (BaseGeoPointFieldMapper) mapper;
                GeoPoint geoPoint;
                if (value instanceof String) {
                    // geo_point stored as text
                    geoPoint = new GeoPoint((String)value);
                } else {
                    // geo_point stored in UDT.
                    Map<String, Double> geo_point =  (Map<String, Double>) value;
                    geoPoint = new GeoPoint(geo_point.get(BaseGeoPointFieldMapper.Names.LAT), geo_point.get(BaseGeoPointFieldMapper.Names.LON));
                }
                geoPointFieldMapper.parse(this, geoPoint, null);
            }  else if (mapper instanceof FieldMapper) {
                FieldMapper fieldMapper = (FieldMapper)mapper;
                if (value instanceof UUID)
                    value = value.toString();   // #74 uuid stored as string
                if (fieldMapper.fieldType().indexOptions() != IndexOptions.NONE)
                    fieldMapper.createField(this, value);
            } else if (mapper instanceof ObjectMapper) {
                final ObjectMapper objectMapper = (ObjectMapper)mapper;
                final ObjectMapper.Nested nested = objectMapper.nested();
                // see https://www.elastic.co/guide/en/elasticsearch/guide/current/nested-objects.html
                // code from DocumentParser.parseObject()
                if (nested.isNested()) {
                    beginNestedDocument(objectMapper.fullPath(),new Uid(docMapper.type(), id));
                    final ParseContext.Document nestedDoc = doc();
                    final ParseContext.Document parentDoc = nestedDoc.getParent();
                    // pre add the uid field if possible (id was already provided)
                    IndexableField uidField = parentDoc.getField(UidFieldMapper.NAME);
                    if (uidField != null) {
                        nestedDoc.add(new Field(UidFieldMapper.NAME, uidField.stringValue(), UidFieldMapper.Defaults.NESTED_FIELD_TYPE));
                    }
                    nestedDoc.add(new Field(TypeFieldMapper.NAME, objectMapper.nestedTypePathAsString(), TypeFieldMapper.Defaults.FIELD_TYPE));
                }

                ContentPath.Type origPathType = path().pathType();
                path().pathType(objectMapper.pathType());
   
                if (value instanceof Map<?,?>) {   
                    for(Entry<String,Object> entry : ((Map<String,Object>)value).entrySet()) {
                        Mapper subMapper = objectMapper.getMapper(entry.getKey());
                        if (subMapper != null) {
                            addField(subMapper, entry.getValue());
                        } else {
                            // dynamic field in top level map => update mapping and add the field.
                            ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(mapper.cqlName());
                            if (cd != null && cd.type.isCollection() && cd.type instanceof MapType) {
                                logger.debug("Updating mapping for field={} type={} value={} ", entry.getKey(), cd.type.toString(), value);
                                CollectionType ctype = (CollectionType) cd.type;
                                if (ctype.kind == CollectionType.Kind.MAP && ((MapType)ctype).getKeysType().asCQL3Type().toString().equals("text")) {
                                    try {
                                        final String valueType = InternalCassandraClusterService.cqlMapping.get(((MapType)ctype).getValuesType().asCQL3Type().toString());
                                        // build a mapping update
                                        Map<String,Object> objectMapping = (Map<String,Object>) ((Map<String,Object>)indexInfo.mapping.get("properties")).get(mapper.name());
                                        XContentBuilder builder = XContentFactory.jsonBuilder()
                                                .startObject()
                                                .startObject(docMapper.type())
                                                .startObject("properties")
                                                .startObject(mapper.name());
                                        boolean hasProperties = false;
                                        for(String key : objectMapping.keySet()) {
                                            if (key.equals("properties")) {
                                                Map<String,Object> props = (Map<String,Object>)objectMapping.get(key);
                                                builder.startObject("properties");
                                                for(String key2 : props.keySet()) {
                                                    builder.field(key2, props.get(key2));
                                                }
                                                builder.field(entry.getKey(), new HashMap<String,String>() {{ put("type",valueType); }});
                                                builder.endObject();
                                                hasProperties = true;
                                            } else {
                                                builder.field(key, objectMapping.get(key));
                                            }
                                        }
                                        if (!hasProperties) {
                                            builder.startObject("properties");
                                            builder.field(entry.getKey(), new HashMap<String,String>() {{ put("type",valueType); }});
                                            builder.endObject();
                                        }
                                        builder.endObject().endObject().endObject().endObject();
                                        String mappingUpdate = builder.string();
                                        logger.info("updating mapping={}",mappingUpdate);
                                        
                                        ElasticSecondaryIndex.this.clusterService.blockingMappingUpdate(indexInfo.indexService, docMapper.type(), mappingUpdate );
                                        subMapper = objectMapper.getMapper(entry.getKey());
                                        addField(subMapper, entry.getValue());
                                    } catch (Exception e) {
                                        logger.error("error while updating mapping",e);
                                    }
                                }
                            } else {
                                logger.error("Unexpected subfield={} for field={} column type={}",entry.getKey(), mapper.name(), cd.type.asCQL3Type().toString());
                            }
                        }
                    }
                } else {
                    if (docMapper.type().equals(PercolatorService.TYPE_NAME)) {
                        // store percolator query as source.
                        String sourceQuery = "{\"query\":"+value+"}";
                        if (logger.isDebugEnabled()) 
                            logger.debug("Store percolate query={}", sourceQuery);
                        
                        BytesReference source = new BytesArray(sourceQuery);
                        source( source );
                        Field sourceQueryField = new StoredField(SourceFieldMapper.NAME, source.array(), source.arrayOffset(), source.length());
                        doc().add(sourceQueryField);
                    }
                }
                
                // restore the enable path flag
                path().pathType(origPathType);
                if (nested.isNested()) {
                    final ParseContext.Document nestedDoc = doc();
                    final ParseContext.Document parentDoc = nestedDoc.getParent();
                    if (nested.isIncludeInParent()) {
                        for (IndexableField field : nestedDoc.getFields()) {
                            if (field.name().equals(UidFieldMapper.NAME) || field.name().equals(TypeFieldMapper.NAME)) {
                                continue;
                            } else {
                                parentDoc.add(field);
                            }
                        }
                    }
                    if (nested.isIncludeInRoot()) {
                        final ParseContext.Document rootDoc = rootDoc();
                        // don't add it twice, if its included in parent, and we are handling the master doc...
                        if (!nested.isIncludeInParent() || parentDoc != rootDoc) {
                            for (IndexableField field : nestedDoc.getFields()) {
                                if (field.name().equals(UidFieldMapper.NAME) || field.name().equals(TypeFieldMapper.NAME)) {
                                    continue;
                                } else {
                                    rootDoc.add(field);
                                }
                            }
                        }
                    }
                    endNestedDocument();
                }
            }
        }
        
        public void beginNestedDocument(String fullPath, Uid uid) {
            final Document doc = (baseCfs.metadata.hasStaticColumns()) ? new StaticDocument(fullPath, doc(), uid) : new Document(fullPath, doc());
            addDoc(doc);
            this.document = doc;
        }
        
        public void endNestedDocument() {
            this.document = doc().getParent();
        }
        
        public boolean externalValueSet() {
            return (externalValue != null);
        }

        public Object externalValue() {
            if (externalValue == null)
                throw new IllegalStateException("External value is not set");
            return externalValue;
        }
        
        public void finalize() {
            // reverse the order of docs for nested docs support, parent should be last
            if (!finalized) {
                if (this.documents.size() > 1) {
                    Collections.reverse(this.documents);
                }
                // apply doc boost
                if (docBoost() != 1.0f) {
                    final Set<String> encounteredFields = Sets.newHashSet();
                    for (ParseContext.Document doc : this.documents) {
                        encounteredFields.clear();
                        for (IndexableField field : doc) {
                            if (field.fieldType().indexOptions() != IndexOptions.NONE && !field.fieldType().omitNorms()) {
                                if (!encounteredFields.contains(field.name())) {
                                    ((Field) field).setBoost(docBoost() * field.boost());
                                    encounteredFields.add(field.name());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        public boolean hasStaticField() {
            return hasStaticField;
        }

        public void setStaticField(boolean hasStaticField) {
            this.hasStaticField = hasStaticField;
        }

        /**
         * Return a new context that will be used within a nested document.
         */
        
        
        @Override
        public boolean flyweight() {
            return false;
        }
    
        @Override
        public DocumentMapperParser docMapperParser() {
            return null;
        }
    
        @Override
        public String index() {
            return indexInfo.name;
        }
    
        @Override
        public Settings indexSettings() {
            return indexInfo.indexService.indexSettings();
        }
    
        @Override
        public String type() {
            return this.docMapper.type();
        }
    
        @Override
        public SourceToParse sourceToParse() {
            return EMPTY_SOURCE_TO_PARSE;
        }
    
        @Override
        public BytesReference source() {
            return this.source;
        }
    
        @Override
        public void source(BytesReference source) {
            this.source = source;
        }
    
        @Override
        public ContentPath path() {
            return path;
        }
    
        @Override
        public XContentParser parser() {
            return null;
        }
    
        @Override
        public Document rootDoc() {
            return documents.get(0);
        }
    
        @Override
        public List<Document> docs() {
            return (List<Document>)this.documents;
        }
    
        @Override
        public Document doc() {
            return this.document;
        }
    
        @Override
        public void addDoc(Document doc) {
            this.documents.add(doc);
        }
        
        @Override
        public RootObjectMapper root() {
            return docMapper.root();
        }
    
        @Override
        public DocumentMapper docMapper() {
            return this.docMapper;
        }
    
        @Override
        public AnalysisService analysisService() {
            return indexInfo.indexService.analysisService();
        }
    
        @Override
        public MapperService mapperService() {
            return indexInfo.indexService.mapperService();
        }
    
        @Override
        public String id() {
            return id;
        }
        
        /**
         * Really, just the id mapper should set this.
         */
        @Override
        public void id(String id) {
            this.id = id;
        }
    
        public String parent() {
            return parent;
        }
        
        public void parent(String parent) {
            this.parent = parent;
        }
        
        @Override
        public Field uid() {
            return this.uid;
        }
    
        /**
         * Really, just the uid mapper should set this.
         */
        @Override
        public void uid(Field uid) {
            this.uid = uid;
        }
    
        @Override
        public Field version() {
            return this.version;
        }
    
        @Override
        public void version(Field version) {
            this.version = version;
        }
    
        @Override
        public AllEntries allEntries() {
            return this.allEntries;
        }
    
        @Override
        public float docBoost() {
            return this.docBoost;
        }
    
        @Override
        public void docBoost(float docBoost) {
            this.docBoost = docBoost;
        }
    
        @Override
        public StringBuilder stringBuilder() {
            stringBuilder.setLength(0);
            return this.stringBuilder;
        }
    
        @Override
        public void addDynamicMappingsUpdate(Mapper mapper) {
            assert mapper instanceof RootObjectMapper : mapper;
            if (dynamicMappingsUpdate == null) {
                dynamicMappingsUpdate = mapper;
            } else {
                dynamicMappingsUpdate = dynamicMappingsUpdate.merge(mapper, false);
            }
        }
    
        @Override
        public Mapper dynamicMappingsUpdate() {
            return dynamicMappingsUpdate;
        }

        
        class StaticDocument extends FilterableDocument {
            Uid uid;
            public StaticDocument(String path, Document parent, Uid uid) {
                super(path, parent);
                this.uid = uid;
            }
            
            
            public boolean apply(IndexableField input) {
                // when applying filter for static columns, update _id and _uid....
                if (input.name().equals(IdFieldMapper.NAME)) {
                    ((Field)input).setStringValue(uid.id());
                }
                if (input.name().equals(UidFieldMapper.NAME)) {
                    if (input instanceof BinaryDocValuesField) {
                        ((BinaryDocValuesField)input).setBytesValue(new BytesRef(uid.toString()));
                    } else if (input instanceof Field) {
                        ((Field)input).setStringValue(uid.toString());
                    }
                }
                if (input.name().startsWith("_")) {
                    return true;
                }
                int x = input.name().indexOf('.');
                String colName = (x > 0) ? input.name().substring(0,x) : input.name();
                int idx = indexInfo.indexOf(colName);
                return idx < baseCfs.metadata.partitionKeyColumns().size() || indexInfo.isStaticField(idx) ;
            }
        }

    }

    class MappingInfo {
        class IndexInfo  {
            final String name;
            final String type;
            final boolean refresh;
            final boolean snapshot;
            final boolean includeNodeId;
            final IndexService indexService;
            Map<String,Object> mapping;
            
            public IndexInfo(String name, IndexService indexService, MappingMetaData mappingMetaData, MetaData metadata) throws IOException {
                this.name = name;
                this.indexService = indexService;
                this.mapping = mappingMetaData.sourceAsMap();
                this.type = mappingMetaData.type();
                this.refresh = indexService.indexSettings().getAsBoolean(IndexMetaData.SETTING_SYNCHRONOUS_REFRESH, metadata.settings().getAsBoolean(InternalCassandraClusterService.SETTING_CLUSTER_DEFAULT_SYNCHRONOUS_REFRESH, false));
                this.snapshot = indexService.indexSettings().getAsBoolean(IndexMetaData.SETTING_SNAPSHOT_WITH_SSTABLE, metadata.settings().getAsBoolean(InternalCassandraClusterService.SETTING_CLUSTER_DEFAULT_SNAPSHOT_WITH_SSTABLE, false));
                this.includeNodeId = indexService.indexSettings().getAsBoolean(IndexMetaData.SETTING_INCLUDE_NODE_ID, metadata.settings().getAsBoolean(InternalCassandraClusterService.SETTING_CLUSTER_DEFAULT_INCLUDE_NODE_ID, false));
            }

            public int indexOf(String f) {
                return MappingInfo.this.indexOf(f);
            }
            
            public boolean isStaticField(int idx) {
                return (staticColumns == null) ? false : staticColumns.get(idx);
            }
            
            public IndexShard shard() {
                final IndexShard indexShard = indexService.shard(0);
                if (indexShard == null) {
                    logger.debug("No such shard {}.0", name);
                    return null;
                }
                if (indexShard.state() != IndexShardState.STARTED) {
                    logger.debug("Shard {}.0 not started", name);
                    return null;
                }
                return indexShard;
            }
            
            public boolean indexStaticOnly() {
                return MappingInfo.this.index_static_only;
            }
        }

        class PartitionFunction {
            String name;
            String pattern;
            String[] fields;      // indexed fields used in the partition function
            int[]    fieldsIndices; // column position in Rowcument.values
            Set<String> indices;
            
            // args =  field names used in the partition function
            PartitionFunction(String[] args) {
                this.name = args[0];
                this.pattern = args[1];
                this.fields = new String[args.length-2];
                this.fieldsIndices = new int[args.length-2];
                System.arraycopy(args, 2, this.fields, 0, args.length-2);
                this.indices = new HashSet<String>();
            }
            
            // values = indexed values in the same order as MappingInfo.fields
            @SuppressForbidden(reason="unchecked")
            String indexName(Object[] values) {
                Object[] args = new Object[fields.length];
                for(int i=0; i < fieldsIndices.length; i++) {
                    args[i] = (fieldsIndices[i] < values.length) ? values[fieldsIndices[i]] : null; 
                }
                return MessageFormat.format(pattern, args);
            }
            
            public String toString() {
                return this.name;
            }
        }

        
        final Map<String, PartitionFunction> partitionFunctions; 
        final Map<String, IndexInfo> indices = new HashMap<String, IndexInfo>();
        final String[] fields;
        final Map<String, ColumnDefinition> columnsDefs = new HashMap<String, ColumnDefinition>();
        final BitSet fieldsToRead;
        final BitSet staticColumns;
        final boolean[] indexedPkColumns;   // bit mask of indexed PK columns.
        final long metadataVersion;
        final String nodeId;
        final String typeName = InternalCassandraClusterService.cfNameToType(ElasticSecondaryIndex.this.baseCfs.name);
        boolean index_static_columns = false;
        boolean index_static_only = false;
        
        MappingInfo(final ClusterState state) {
            this.metadataVersion = state.metaData().version();
            this.nodeId = state.nodes().localNodeId();
            
            if (state.blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                logger.debug("global write blocked");
                this.fields = null;
                this.fieldsToRead = null;
                this.staticColumns = null;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                return;
            }
            
            Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();
            Map<String, PartitionFunction> partFuncs = null;
            
            for(Iterator<IndexMetaData> indexMetaDataIterator = state.metaData().iterator(); indexMetaDataIterator.hasNext(); ) {
                IndexMetaData indexMetaData = indexMetaDataIterator.next();
                String index = indexMetaData.getIndex();
                MappingMetaData mappingMetaData; 
                
                if (indexMetaData.getState() != IndexMetaData.State.OPEN)
                    continue;
                
                ClusterBlockException clusterBlockException = state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, index);
                if (clusterBlockException != null) {
                    if (logger.isInfoEnabled())
                        logger.info("ignore, index=[{}] blocked blocks={}", index, clusterBlockException.blocks());
                    continue;
                }

                if ( ElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.keyspace()) &&
                     (mappingMetaData = indexMetaData.mapping(typeName)) != null) {
                    try {
                        Map<String,Object> mappingMap = (Map<String,Object>)mappingMetaData.getSourceAsMap();
                        if (mappingMap.get("_meta") != null) {
                            Map<String,Object> meta = (Map<String,Object>)mappingMap.get("_meta");
                            if (meta.get("index_static_only") != null && XContentMapValues.nodeBooleanValue(meta.get("index_static_only"))) {
                                logger.debug("_meta index_static_only=true for index [{}]" , index);
                                index_static_only = true;
                            }
                            if (meta.get("index_static_columns") != null && XContentMapValues.nodeBooleanValue(meta.get("index_static_columns"))) {
                                logger.debug("_meta index_static_columns=true for index [{}]" , index);
                                index_static_columns = true;
                            }
                        }
                        
                        if (mappingMap.get("properties") != null) {
                            IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
                            IndexService indexService = indicesService.indexService(index);
                            if (indexService == null) {
                                logger.error("indexService not available for [{}], ignoring" , index);
                                continue;
                            }
                            IndexInfo indexInfo = new IndexInfo(index, indexService, mappingMetaData, state.metaData());
                            this.indices.put(index, indexInfo);
                            
                            Map<String,Object> props = (Map<String,Object>)mappingMap.get("properties");
                            for(String fieldName : props.keySet() ) {
                                Map<String,Object> fieldMap = (Map<String,Object>)props.get(fieldName);
                                if (fieldMap.get("enabled")==null || XContentMapValues.nodeBooleanValue(fieldMap.get("enabled"))) {
                                    boolean mandartory = (fieldMap.get(TypeParsers.CQL_MANDATORY) == null || XContentMapValues.nodeBooleanValue(fieldMap.get(TypeParsers.CQL_MANDATORY)));
                                    if (fieldsMap.get(fieldName) != null) {
                                        mandartory = mandartory || fieldsMap.get(fieldName);
                                    }
                                    fieldsMap.put(fieldName, mandartory);
                                }
                            }
                            if (mappingMetaData.hasParentField()) {
                                Map<String,Object> parentsProps = (Map<String,Object>)mappingMap.get(ParentFieldMapper.NAME);
                                String pkColumns = (String)parentsProps.get(ParentFieldMapper.CQL_PARENT_PK);
                                if (pkColumns == null) {
                                    fieldsMap.put(ParentFieldMapper.NAME,true);
                                } else {
                                    for(String colName : pkColumns.split(","))
                                        fieldsMap.put(colName, true);
                                }
                            }
                            
                            String[] pf = indexMetaData.partitionFunction();
                            if (pf != null) {
                                if (partFuncs == null) 
                                    partFuncs = new HashMap<String, PartitionFunction>();
                                
                                PartitionFunction func = partFuncs.get(pf[0]);
                                if (func == null) {
                                    func = new PartitionFunction(pf);
                                    partFuncs.put(func.name, func);
                                }
                                if (!func.pattern.equals(pf[1])) {
                                    logger.error("Partition function [{}] is defined with two different partterns [{}] and [{}]", pf[0], func.pattern, pf[1]);
                                }
                                func.indices.add(index);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Unexpected error index=[{}]", e, index);
                    }
                }
            }
            
            if (indices.size() == 0) {
                if (logger.isTraceEnabled())
                    logger.warn("no active elasticsearch index for keyspace.table=[{}.{}] state={}",baseCfs.metadata.ksName, baseCfs.name, state);
                this.fields = null;
                this.fieldsToRead = null;
                this.staticColumns = null;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                return;
            }

            
            // order fields with pk columns first
            this.fields = new String[fieldsMap.size()];
            int pkLength = baseCfs.metadata.partitionKeyColumns().size()+baseCfs.metadata.clusteringColumns().size();
            this.indexedPkColumns = new boolean[pkLength];
            int j=0, l=0;
            for(ColumnDefinition cd : Iterables.concat(baseCfs.metadata.partitionKeyColumns(), baseCfs.metadata.clusteringColumns())) {
                indexedPkColumns[l] = fieldsMap.containsKey(cd.name.toString());
                if (indexedPkColumns[l]) {
                    fields[j++] = cd.name.toString();
                }
                l++;
            }
            for(String f : fieldsMap.keySet()) {
                boolean alreadyInFields = false;
                for(int k=0; k < j; k++) {
                    if (f.equals(fields[k])) {
                        alreadyInFields = true;
                        break;
                    }
                }
                if (!alreadyInFields) {
                    fields[j++] = f;
                }
            }
            
            this.fieldsToRead = new BitSet(fields.length);
            this.staticColumns = (baseCfs.metadata.hasStaticColumns() || index_static_only) ? new BitSet(fields.length) : null;
            for(int i=0; i < fields.length; i++) {
                ColumnIdentifier colId = new ColumnIdentifier(fields[i],true);
                ColumnDefinition colDef = baseCfs.metadata.getColumnDefinition(colId);
                columnsDefs.put(fields[i], colDef);
                this.fieldsToRead.set(i, fieldsMap.get(fields[i]) && !colDef.isPrimaryKeyColumn());
                if (staticColumns != null)
                    this.staticColumns.set(i,colDef.isStatic());
            }
            
            if (partFuncs != null && partFuncs.size() > 0) {
                for(PartitionFunction func : partFuncs.values()) {
                    int i = 0;
                    for(String field : func.fields) {
                        func.fieldsIndices[i++] = indexOf(field);
                    }
                }
                this.partitionFunctions = partFuncs;
            } else {
                this.partitionFunctions = null;
            }
        }
        
        // values = MappingInfo.values (ordered as MappingInfo.fields)
        public Collection<IndexInfo> targetIndices(final Object[] values) {
            if (this.partitionFunctions == null)
                return this.indices.values();
            
            Set<IndexInfo> targetIndices = new HashSet<IndexInfo>(this.partitionFunctions.size());
            for(PartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                IndexInfo targetIndexInfo = this.indices.get(indexName);
                if (targetIndexInfo != null) {
                    targetIndices.add( targetIndexInfo );
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("No target index=[{}] found for partition function name=[{}] pattern=[{}] indices={}", indexName, func.name, func.pattern, this.indices.keySet());
                }
            }
            if (logger.isTraceEnabled()) 
                logger.trace("Partition target indices={}", targetIndices.stream().map( e -> e.name ).collect( Collectors.toList() ));
            return targetIndices;
        }
        
        public Collection<IndexInfo> targetIndicesForDelete(final Object[] values) {
            if (this.partitionFunctions == null)
                return this.indices.values();
            
            Set<IndexInfo> targetIndices = new HashSet<IndexInfo>(this.partitionFunctions.size());
            for(PartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                IndexInfo targetIndexInfo = this.indices.get(indexName);
                if (targetIndexInfo != null) {
                    targetIndices.add( targetIndexInfo );
                } else {
                    if (logger.isWarnEnabled())
                        logger.warn("No target index=[{}] found, function name=[{}] pattern=[{}], return all indices={}", indexName, func.name, func.pattern, this.indices);
                    for(String index : func.indices) {
                        targetIndices.add( this.indices.get(index) );
                    }
                }
            }
            return targetIndices;
        }
        
        public int indexOf(String field) {
            for(int i=0; i < this.fields.length; i++) {
                if (this.fields[i].equals(field)) return i;
            }
            return -1;
        }

        /*
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(IndexInfo i : indices.values()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(i.name).append('=').append(i.mapping.toString());
            }
            return sb.toString();
        }
        */
        
        class RowcumentIndexer implements Index.Indexer {
            final DecoratedKey key;
            final Long token;
            final ArrayNode an;
            final Object[] pkCols = new Object[baseCfs.metadata.partitionKeyColumns().size()+baseCfs.metadata.clusteringColumns().size()];
            final String partitionKey;
            final int nowInSec;
            
            public RowcumentIndexer(final DecoratedKey key,
                    final PartitionColumns columns,
                    final int nowInSec,
                    final OpOrder.Group opGroup,
                    final IndexTransaction.Type transactionType) throws JsonGenerationException, JsonMappingException, IOException {
                this.key = key;
                this.nowInSec = nowInSec;
                this.token = (Long) key.getToken().getTokenValue();   // Cassandra Token value (Murmur3 partitionner only)
                this.an = InternalCassandraClusterService.jsonMapper.createArrayNode();
                
                AbstractType<?> keyValidator = baseCfs.metadata.getKeyValidator();
                boolean isCompound = keyValidator instanceof CompositeType;
                int i = 0;
                if (isCompound) {
                    CompositeType composite = (CompositeType) keyValidator;
                    for(ByteBuffer bb : composite.split(key.getKey())) {
                        AbstractType<?> type = composite.types.get(i);
                        pkCols[i] = type.compose(bb);
                        InternalCassandraClusterService.addToJsonArray(type, pkCols[i], an);
                        i++;
                    }
                } else {
                    pkCols[i] = keyValidator.compose(key.getKey());
                    InternalCassandraClusterService.addToJsonArray(keyValidator, pkCols[i], an);
                    i++;
                }
                this.partitionKey = InternalCassandraClusterService.writeValueAsString(an);  // JSON string  of the partition key.
            }
                
            public Query buildPartitionKeyQuery(DocumentMapper docMapper, ArrayList<Object> indexedPkColumnsValues) {
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                int j = 0;
                for(int i = 0 ; i < baseCfs.metadata.partitionKeyColumns().size(); i++) {
                    if (indexedPkColumns[i]) {
                        ColumnDefinition cd = baseCfs.metadata.clusteringColumns().get(i);
                        FieldMapper mapper = docMapper.mappers().smartNameFieldMapper(cd.name.toString());
                        if (mapper != null) {
                            Term t = new Term(fields[i], mapper.fieldType().indexedValueForSearch(indexedPkColumnsValues.get(j++)));
                            builder.add(new TermQuery(t), Occur.FILTER);
                        } 
                    }
                }
                return builder.build();
            }
            
            @SuppressForbidden(reason="unchecked")
            private Query buildQuery(ColumnDefinition cd, FieldMapper mapper, ByteBuffer lower, ByteBuffer upper, boolean includeLower, boolean includeUpper) {
                Object start = cd.type.compose(lower);
                Object end = cd.type.compose(upper);
                Query query = null;
                if (mapper != null) {
                    CQL3Type cql3Type = cd.type.asCQL3Type();
                    if (cql3Type instanceof CQL3Type.Native) {
                        switch ((CQL3Type.Native) cql3Type) {
                        case ASCII:
                        case TEXT:
                        case VARCHAR:
                            if (start.equals(end)) {
                                query = new TermQuery(new Term(cd.name.toString(), mapper.fieldType().indexedValueForSearch(start)));
                            } else {
                                query = new TermRangeQuery(cd.name.toString(), mapper.fieldType().indexedValueForSearch(start), mapper.fieldType().indexedValueForSearch(end),includeLower, includeUpper);
                            }
                            break;
                        case INT:
                        case SMALLINT:
                        case TINYINT:
                            query = NumericRangeQuery.newIntRange(cd.name.toString(), (Integer) start, (Integer) end, includeLower, includeUpper);
                            break;
                        case INET:
                        case TIMESTAMP:
                        case BIGINT:
                            query = NumericRangeQuery.newLongRange(cd.name.toString(), (Long) start, (Long) start, includeLower, includeUpper);
                            break;
                        case DOUBLE:
                            query = NumericRangeQuery.newDoubleRange(cd.name.toString(), (Double) start, (Double) start, includeLower, includeUpper);
                            break;
                        case FLOAT:
                            query = NumericRangeQuery.newFloatRange(cd.name.toString(), (Float) start, (Float) start, includeLower, includeUpper);
                            break;
                            
                        case DECIMAL:
                        case TIMEUUID:
                        case UUID:
                        case BLOB:
                        case BOOLEAN:
                            throw new UnsupportedOperationException("Unsupported data type in primary key");
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Object type in primary key not supported");
                }
                return query;
            }
            
            class Rowcument {
                String id = null;
                final Object[] values = new Object[fields.length];
                final BitSet fieldsNotNull = new BitSet(fields.length);     // regular or static columns only
                final BitSet tombstoneColumns = new BitSet(fields.length);  // regular or static columns only
                final boolean isStatic;
                int   docTtl = Integer.MAX_VALUE;
                
                // init document with clustering columns stored in cellName, or cell value for non-clustered columns (regular with no clustering key or static columns).
                public Rowcument(Row row) throws IOException {
                    this.isStatic = row.isStatic();
                    
                    // copy the indexed columns of partition key in values
                    int x = 0;
                    for(int i=0 ; i < baseCfs.metadata.partitionKeyColumns().size(); i++) {
                        if (indexedPkColumns[i]) {
                            values[x++] = pkCols[i];
                        }
                    }
                    // copy the indexed columns of clustering key in values
                    Clustering clustering = row.clustering();
                    if (!isStatic && clustering.size() > 0 && (baseCfs.metadata.clusteringColumns().size() > 0))  {
                        // add clustering keys to docMap and _id
                        ArrayNode an2 = InternalCassandraClusterService.jsonMapper.createArrayNode();
                        an2.addAll(an);
                        int i=0;
                        for(ColumnDefinition ccd : baseCfs.metadata.clusteringColumns()) {
                            Object value = InternalCassandraClusterService.deserialize(ccd.type, clustering.get(i));
                            pkCols[baseCfs.metadata.partitionKeyColumns().size()+i] = value;
                            if (indexedPkColumns[baseCfs.metadata.partitionKeyColumns().size()+i]) {
                                values[x++] = value;
                            }
                            InternalCassandraClusterService.addToJsonArray(ccd.type, value, an2);
                            i++;
                        }
                        id = InternalCassandraClusterService.writeValueAsString(an2);
                    } else {
                        // partiton row update
                        id = partitionKey;
                    }
                    readCellValues(row, true);
                }
               
                public void readCellValues(Row row, boolean indexOp) throws IOException {
                    for(Cell cell : row.cells()) {
                        readCellValue(cell, indexOp);
                    }
                }
                
                public void readCellValue(Cell cell, boolean indexOp) throws IOException {
                    final String cellNameString = cell.column().name.toString();
                    int idx  = indexOf(cellNameString);
                    if (idx == - 1) {
                        //ignore cell, not indexed.
                        return;
                    }
                    if (cell.isLive(nowInSec) && indexOp) {
                        docTtl = Math.min(cell.localDeletionTime(), docTtl);
                        
                        ColumnDefinition cd = cell.column();
                        if (cd.type.isCollection()) {
                            CollectionType ctype = (CollectionType) cd.type;
                            Object value = null;
                  
                            switch (ctype.kind) {
                            case LIST: 
                                value = InternalCassandraClusterService.deserialize(((ListType)cd.type).getElementsType(), cell.value() );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("list name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                List l = (List) values[idx];
                                if (l == null) {
                                    l = new ArrayList();
                                    values[idx] = l;
                                } 
                                l.add(value);
                                break;
                            case SET:
                                value = InternalCassandraClusterService.deserialize(((SetType)cd.type).getElementsType(), cell.value() );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("set name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                Set s = (Set) values[idx];
                                if (s == null) {
                                    s = new HashSet();
                                    values[idx] = s;
                                } 
                                s.add(value);
                                break;
                            case MAP:
                                value = InternalCassandraClusterService.deserialize(((MapType)cd.type).getValuesType(), cell.value() );
                                CellPath cellPath = cell.path();
                                Object key = InternalCassandraClusterService.deserialize(((MapType)cd.type).getKeysType(), cellPath.get(cellPath.size()-1));
                                if (logger.isTraceEnabled()) 
                                    logger.trace("map name={} kind={} type={} key={} value={}", 
                                            cellNameString, cd.kind, 
                                            cd.type.asCQL3Type().toString(),
                                            key, 
                                            value);
                                if (key instanceof String) {
                                    Map m = (Map) values[idx];
                                    if (m == null) {
                                        m = new HashMap();
                                        values[idx] = m;
                                    } 
                                    m.put(key,value);
                                }
                                break;
                            }
                            fieldsNotNull.set(idx, value != null);
                        } else {
                            Object value = InternalCassandraClusterService.deserialize(cd.type, cell.value() );
                            if (logger.isTraceEnabled()) 
                                logger.trace("name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                            
                            values[idx] = value;
                            fieldsNotNull.set(idx, value != null);
                        }
                    } else {
                        // tombstone => black list this column for later document.complete().
                        tombstoneColumns.set(idx);
                    }
                }
                
                public Object[] rowAsArray(UntypedResultSet.Row row) throws IOException {
                    final Object values[] = new Object[row.getColumns().size()];
                    final List<ColumnSpecification> columnSpecs = row.getColumns();
                    
                    for (int idx = 0; idx < columnSpecs.size(); idx++) {
                        ColumnSpecification colSpec = columnSpecs.get(idx);
                        String columnName = colSpec.name.toString();
                        
                        if (!row.has(columnName) || ByteBufferUtil.EMPTY_BYTE_BUFFER.equals(row.getBlob(columnName)) ) {
                            values[idx] = null;
                            continue;
                        }
                        
                        CQL3Type cql3Type = colSpec.type.asCQL3Type();
                        if (cql3Type instanceof CQL3Type.Native) {
                            switch ((CQL3Type.Native) cql3Type) {
                            case ASCII:
                            case TEXT:
                            case VARCHAR:
                                values[idx] = row.getString(columnName);
                                break;
                            case TIMEUUID:
                            case UUID:
                                values[idx] = row.getUUID(columnName).toString();
                                break;
                            case TIMESTAMP:
                                values[idx] = row.getTimestamp(columnName).getTime();
                                break;
                            case INT:
                                values[idx] = row.getInt(columnName);
                                break;
                            case SMALLINT:
                                values[idx] = row.getShort(columnName);
                                break;
                            case TINYINT:
                                values[idx] = row.getByte(columnName);
                                break;
                            case BIGINT:
                                values[idx] = row.getLong(columnName);;
                                break;
                            case DOUBLE:
                                values[idx] = row.getDouble(columnName);
                                break;
                            case DECIMAL:
                                values[idx] = DecimalType.instance.compose(row.getBlob(columnName));
                                break;
                            case FLOAT:
                                values[idx] = row.getFloat(columnName);
                                break;
                            case BLOB:
                                values[idx] = row.getBytes(columnName);
                                break;
                            case BOOLEAN:
                                values[idx] = row.getBoolean(columnName);
                                break;
                            case INET:
                                values[idx] = NetworkAddress.format(row.getInetAddress(columnName));
                                break;
                            case COUNTER:
                                logger.warn("Ignoring unsupported counter for column {}", columnName);
                                break;
                            default:
                                logger.error("Ignoring unsupported type={} for column {}", cql3Type, columnName);
                            }
                        } else if (cql3Type.isCollection()) {
                            AbstractType<?> elementType;
                            CollectionType ctype = (CollectionType) colSpec.type;
                            switch (ctype.kind) {
                            case LIST: 
                                List list;
                                elementType = ((ListType<?>) ctype).getElementsType();
                                if (elementType instanceof UserType) {
                                    final List<ByteBuffer> lbb = row.getList(columnName, BytesType.instance);
                                    list = new ArrayList(lbb.size());
                                    for (ByteBuffer bb : lbb) {
                                        list.add(InternalCassandraClusterService.deserialize(elementType, bb));
                                    }
                                } else {
                                    list = row.getList(columnName, elementType);
                                }
                                values[idx] =  (list.size() == 1) ? list.get(0) : list;
                                break;
                            case SET:
                                Set set;
                                elementType = ((SetType<?>) colSpec.type).getElementsType();
                                if (elementType instanceof UserType) {
                                    final Set<ByteBuffer> lbb = row.getSet(columnName, BytesType.instance);
                                    set = new HashSet<>(lbb.size());
                                    for (ByteBuffer bb : lbb) {
                                        set.add(InternalCassandraClusterService.deserialize(elementType, bb));
                                    }
                                } else {
                                    set = row.getSet(columnName, elementType);
                                }
                                values[idx] =  (set.size() == 1) ? set.iterator().next() : set;
                                break;
                            case MAP:
                                Map map;
                                if (((MapType<?,?>) ctype).getKeysType().asCQL3Type() != CQL3Type.Native.TEXT) {
                                    throw new IOException("Only support map<text,?>, bad type for column "+columnName);
                                }
                                UTF8Type keyType = (UTF8Type) ((MapType<?,?>) colSpec.type).getKeysType();
                                elementType = ((MapType<?,?>) colSpec.type).getValuesType();
                                if (elementType instanceof UserType) {
                                    final Map<String, ByteBuffer> lbb = row.getMap(columnName, keyType, BytesType.instance);
                                    map = new HashMap<String , Map<String, Object>>(lbb.size());
                                    for(String key : lbb.keySet()) {
                                        map.put(key, InternalCassandraClusterService.deserialize(elementType, lbb.get(key)));
                                    }
                                } else {
                                    Map<String,Object> map2 = (Map<String,Object>) row.getMap(columnName, keyType, elementType);
                                    map = new HashMap<String, Object>(map2.size());
                                    for(String key : map2.keySet()) {
                                        map.put(key,  map2.get(key));
                                    }
                                }
                                values[idx] =  map;
                                break;
                            }
                        } else if (colSpec.type instanceof UserType) {
                            ByteBuffer bb = row.getBytes(columnName);
                            values[idx] = InternalCassandraClusterService.deserialize(colSpec.type, bb);
                        } else if (cql3Type instanceof CQL3Type.Custom) {
                            logger.error("CQL3.Custom type not supported for column "+columnName);
                        }
                    }
                    return values;
                }
                
                // return true if at least one field in one mapping is populated.
                public boolean complete() {
                    // add missing or collection columns that should be read before indexing the document.
                    // read missing static columns (with limit 1) or regular columns if  
                    final BitSet mustReadFields = (BitSet)fieldsToRead.clone();
                    boolean completeOnlyStatic = isStatic;
                    if (staticColumns != null) {
                        if (this.isStatic || MappingInfo.this.index_static_columns) {
                            // ignore regular columns, we are updating static ones.
                            int prev_cardinality = mustReadFields.cardinality();
                            mustReadFields.and(staticColumns);
                            // ensure that we don't request for static columns only.
                            if (mustReadFields.cardinality() < prev_cardinality)
                                completeOnlyStatic = true;
                        } else {
                            // ignore static columns, we got only regular columns.
                            mustReadFields.andNot(staticColumns);
                        }
                    }
                    mustReadFields.andNot(fieldsNotNull);
                    mustReadFields.andNot(tombstoneColumns);
                    
                    if (mustReadFields.cardinality() > 0) {
                        final String[] mustReadColumns = new String[mustReadFields.cardinality()];
                        final int[]    mustReadColumnsPosition = new int[mustReadFields.cardinality()];
                        int x = 0;
                        
                        for (int i = mustReadFields.nextSetBit(0); i >= 0 && i < fields.length; i = mustReadFields.nextSetBit(i+1)) {
                            if (values[i] == null || values[i] instanceof Set || values[i] instanceof Map) {
                                // List must be fully updated, but set or map can be partially updated without having duplicate entry. 
                                mustReadColumns[x] = fields[i];
                                mustReadColumnsPosition[x] = i;
                                x++;
                            }
                        }

                        if (x > 0)  {
                            final String[] missingColumns = new String[x];
                            System.arraycopy(mustReadColumns, 0, missingColumns, 0, x);
                            Object[] pk = pkCols;
                            if (completeOnlyStatic) {
                                pk = new Object[baseCfs.metadata.partitionKeyColumns().size()];
                                System.arraycopy(pkCols, 0, pk, 0, baseCfs.metadata.partitionKeyColumns().size());
                            }
                            try {
                                // fetch missing fields from the local cassandra row to update Elasticsearch index
                                if (logger.isTraceEnabled()) {
                                    logger.trace(" {}.{} id={} missing columns names={} completeOnlyStatic={}",baseCfs.metadata.ksName, baseCfs.metadata.cfName, id, missingColumns, completeOnlyStatic);
                                }
                                MappingInfo.IndexInfo indexInfo = MappingInfo.this.indices.values().iterator().next();
                                UntypedResultSet results = clusterService.fetchRowInternal(baseCfs.metadata.ksName, indexInfo.name, indexInfo.type, missingColumns, pk, completeOnlyStatic, MappingInfo.this.columnsDefs);
                                if (!results.isEmpty()) {
                                    Object[] missingValues = rowAsArray(results.one());
                                    for(int i=0; i < x; i++) {
                                        values[ mustReadColumnsPosition[i] ] = missingValues[i];
                                        fieldsNotNull.set( mustReadColumnsPosition[i] );
                                    }
                                }
                            } catch (RequestValidationException | IOException e) {
                                logger.error("Failed to fetch columns {}", e, missingColumns);
                            }
                        }
                    }
                    
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}.{} id={} fields={} values={}", baseCfs.metadata.ksName, typeName, id, Arrays.toString(values));
                    }
                    return fieldsNotNull.cardinality() > 0;
                }
                
                public Context buildContext(IndexInfo indexInfo, boolean staticColumnsOnly) throws IOException {
                    Context context = ElasticSecondaryIndex.this.perThreadContext.get();
                    Uid uid = new Uid(typeName,  (staticColumnsOnly) ? partitionKey : id);
                    
                    context.reset(indexInfo, uid);
                    
                    // preCreate for all metadata fields.
                    for (MetadataFieldMapper metadataMapper : context.docMapper.mapping().metadataMappers())
                        metadataMapper.preCreate(context);
                    
                    context.docMapper.idFieldMapper().createField(context, uid.id());
                    context.docMapper.uidMapper().createField(context, uid);
                    context.docMapper.typeMapper().createField(context, typeName);
                    context.docMapper.tokenFieldMapper().createField(context, token);
                    if (indexInfo.includeNodeId)
                        context.docMapper.nodeFieldMapper().createField(context, MappingInfo.this.nodeId);
                    
                    context.docMapper.routingFieldMapper().createField(context, partitionKey);
                    context.docMapper.allFieldMapper().createField(context, null);
                    context.version(DEFAULT_VERSION);
                    context.doc().add(DEFAULT_VERSION);
                    
                    // add all fields to context.
                    for(int i=0; i < values.length; i++) {
                        if (values[i] != null) {
                            try {
                                Mapper mapper = context.docMapper.mappers().smartNameFieldMapper(fields[i]);
                                mapper = (mapper != null) ? mapper : context.docMapper.objectMappers().get(fields[i]);
                                if (mapper != null) 
                                    context.addField(mapper, values[i]);
                            } catch (IOException e) {
                                logger.error("error", e);
                            }
                        }
                    }
                    
                    // postCreate for all metadata fields.
                    Mapping mapping = context.docMapper.mapping();
                    for (MetadataFieldMapper metadataMapper : mapping.metadataMappers()) {
                        try {
                            metadataMapper.postCreate(context);
                        } catch (IOException e) {
                           logger.error("error", e);
                        }
                    }
                    
                    // add _parent
                    ParentFieldMapper parentMapper = context.docMapper.parentFieldMapper();
                    if (parentMapper.active() && indexOf(ParentFieldMapper.NAME) == -1) {
                        String parent = null;
                        if (parentMapper.pkColumns() != null) {
                            String[] cols = parentMapper.pkColumns().split(",");
                            if (cols.length == 1) {
                                parent = (String) values[indexOf(cols[0])];
                            } else {
                                // build a json array
                                ArrayNode an = InternalCassandraClusterService.jsonMapper.createArrayNode();
                                for(String c: cols) 
                                    InternalCassandraClusterService.addToJsonArray( values[indexOf(c)], an );
                                parent = InternalCassandraClusterService.writeValueAsString(an);
                            }
                        } else {
                            int parentIdx = indexOf(ParentFieldMapper.NAME);
                            if (parentIdx != -1 && values[parentIdx] instanceof String)
                                parent = (String) values[parentIdx];
                        }
                        if (parent != null) {
                            //parent = parentMapper.type() + Uid.DELIMITER + parent;
                            if (logger.isDebugEnabled())
                                logger.debug("add _parent={}", parent);
                            parentMapper.createField(context, parent);
                            context.parent(parent);
                        }
                    }
                    if (!parentMapper.active()) {
                        // need to call this for parent types
                        parentMapper.createField(context, null);
                    }
                    return context;
                }
                
                /*
                public void index() {
                    if (forceStatic) {
                        // force index static document, ignore regular rows.
                        index(true);
                        return;
                    }
                    if (!this.hasMissingClusteringKeys) {
                        // index regular row
                        index(false);
                    }
                    if (this.isStatic) {
                        // index static document
                        index(true);
                    }
                }
                */
                
                public void index() {
                    long startTime = System.nanoTime();
                    long ttl = (long)((this.docTtl < Integer.MAX_VALUE) ? this.docTtl : 0);
                    
                    for(IndexInfo ii : MappingInfo.this.targetIndices(values)) {
                        try {
                            Context context = buildContext(ii, isStatic);
                            /*
                            if (staticDocumentOnly &&  !(forceStatic || context.hasStaticField())) 
                                continue;
                            */
                            
                            Field uid = context.uid();
                            if (isStatic) {
                                uid = new Field(UidFieldMapper.NAME, Uid.createUid(typeName, partitionKey), Defaults.FIELD_TYPE);
                                for(Document doc : context.docs()) {
                                    if (doc instanceof Context.StaticDocument) {
                                        ((Context.StaticDocument)doc).applyFilter(isStatic);
                                    }
                                }
                                
                            }
                            context.finalize();
                            final ParsedDocument parsedDoc = new ParsedDocument(
                                    uid, 
                                    context.version(), 
                                    (isStatic) ? partitionKey : context.id(), 
                                    context.type(), 
                                    partitionKey, // routing
                                    System.currentTimeMillis(), // timstamp
                                    ttl,
                                    token.longValue(), 
                                    context.docs(), 
                                    context.source(), // source 
                                    (Mapping)null); // mappingUpdate
                            
                            parsedDoc.parent(context.parent());

                            if (logger.isTraceEnabled()) {
                                logger.trace("index={} id={} type={} uid={} routing={} docs={}", context.indexInfo.name, parsedDoc.id(), parsedDoc.type(), parsedDoc.uid(), parsedDoc.routing(), parsedDoc.docs());
                            }
                            final IndexShard indexShard = context.indexInfo.shard();
                            if (indexShard != null) {
                                final Engine.Index operation = new Engine.Index(context.docMapper.uidMapper().term(uid.stringValue()), 
                                        parsedDoc, 
                                        Versions.MATCH_ANY, 
                                        VersionType.INTERNAL, 
                                        Engine.Operation.Origin.PRIMARY, 
                                        startTime, 
                                        false);
                                
                                final boolean created = operation.execute(indexShard);
                                final long version = operation.version();

                                if (logger.isDebugEnabled()) {
                                    logger.debug("document CF={}.{} index={} type={} id={} version={} created={} ttl={} refresh={} ", 
                                        baseCfs.metadata.ksName, baseCfs.metadata.cfName,
                                        context.indexInfo.name, typeName,
                                        parsedDoc.id(), version, created, ttl, context.indexInfo.refresh);
                                }
                             }
                        } catch (IOException e) {
                            logger.error("error", e);
                        }
                    }
                }
                
                public void delete() {
                    for (MappingInfo.IndexInfo indexInfo : targetIndices(values)) {
                        final IndexShard indexShard = indexInfo.shard();
                        if (indexShard != null) {
                            if (logger.isDebugEnabled())
                                logger.debug("deleting document from index.type={}.{} id={}", indexInfo.name, typeName, id);
                            Engine.Delete delete = indexShard.prepareDeleteOnPrimary(typeName, id, Versions.MATCH_ANY, VersionType.INTERNAL);
                            indexShard.delete(delete);
                        }
                    }
                }
                
            }

            /**
             * Notification of the start of a partition update.
             * This event always occurs before any other during the update.
             */
            @Override
            public void begin() {
                
            }

            /**
             * Notification of a top level partition delete.
             * @param deletionTime
             */
            @Override
            public void partitionDelete(DeletionTime deletionTime) {
                Long  token_long = (Long) key.getToken().getTokenValue();
                String typeName = InternalCassandraClusterService.cfNameToType(ElasticSecondaryIndex.this.baseCfs.metadata.cfName);
                NumericRangeQuery<Long> tokenRangeQuery = NumericRangeQuery.newLongRange(TokenFieldMapper.NAME, NumericUtils.PRECISION_STEP_DEFAULT, token_long, token_long, true, true);
                
                mappingInfoLock.readLock().lock();
                try {
                    // Delete documents where _token = token_long + _type = typeName
                    for (MappingInfo.IndexInfo indexInfo : mappingInfo.indices.values()) {
                        if (logger.isTraceEnabled())
                            logger.trace("deleting documents where _token={} from index.type={}.{} id={}", token_long, indexInfo.name, typeName);
                        IndexShard indexShard = indexInfo.indexService.shard(0);
                        if (indexShard != null) {
                            BooleanQuery.Builder builder = new BooleanQuery.Builder();
                            builder.add( new TermQuery(new Term(TypeFieldMapper.NAME, indexInfo.indexService.mapperService().documentMapper(typeName).typeMapper().fieldType().indexedValueForSearch(typeName))), Occur.FILTER);
                            builder.add(tokenRangeQuery, Occur.FILTER);
                            DeleteByQuery deleteByQuery = new DeleteByQuery(builder.build(), null, null, null, null, Operation.Origin.PRIMARY, System.currentTimeMillis(), typeName);
                            indexShard.engine().delete(deleteByQuery);
                        }
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }

            /**
             * Notification of a RangeTombstone.
             * An update of a single partition may contain multiple RangeTombstones,
             * and a notification will be passed for each of them.
             * @param tombstone
             */
            @Override
            public void rangeTombstone(RangeTombstone tombstone) {
                Slice slice = tombstone.deletedSlice();
                Bound start = slice.start();
                Bound end = slice.end();
                
                mappingInfoLock.readLock().lock();
                try {
                    for(MappingInfo.IndexInfo indexInfo : targetIndices(pkCols)) {
                        IndexShard indexShard = indexInfo.indexService.shard(0);
                        if (indexShard != null) {
                            DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(typeName);
                            BooleanQuery.Builder builder = new BooleanQuery.Builder();
                            builder.add( new TermQuery(new Term(TypeFieldMapper.NAME, docMapper.typeMapper().fieldType().indexedValueForSearch(typeName))), Occur.FILTER);
                            
                            // build the primary key part of the delete by query
                            int i = 0;
                            for(ColumnDefinition cd : baseCfs.metadata.primaryKeyColumns()) {
                                if (i >= start.size())
                                    break;
                                if (indexedPkColumns[i]) {
                                    FieldMapper mapper = docMapper.mappers().smartNameFieldMapper(cd.name.toString());
                                    builder.add( buildQuery( cd, mapper, start.get(i), end.get(i), start.isInclusive(), end.isInclusive()), Occur.FILTER);
                                }
                                i++;
                            }
                            
                            Query query = builder.build();
                            if (logger.isTraceEnabled()) {
                                logger.trace("delete rangeTombstone from ks.cf={}.{} query={} in elasticsearch index=[{}]", baseCfs.metadata.ksName, baseCfs.name, query, indexInfo.name);
                            }
                            DeleteByQuery deleteByQuery = new DeleteByQuery(query, null, null, null, null, Operation.Origin.PRIMARY, System.currentTimeMillis(), typeName);
                            indexShard.engine().delete(deleteByQuery);
                        }
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }


            /**
             * Notification that a new row was inserted into the Memtable holding the partition.
             * This only implies that the inserted row was not already present in the Memtable,
             * it *does not* guarantee that the row does not exist in an SSTable, potentially with
             * additional column data.
             *
             * @param row the Row being inserted into the base table's Memtable.
             */
            @Override
            public void insertRow(Row row) {
                mappingInfoLock.readLock().lock();
                try {
                    Rowcument rowcument = new Rowcument(row);
                    if (!MappingInfo.this.index_static_only || rowcument.isStatic) {
                        if (rowcument.complete()); 
                            rowcument.index();
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }


            /**
             * Notification of a modification to a row in the base table's Memtable.
             * This is allow an Index implementation to clean up entries for base data which is
             * never flushed to disk (and so will not be purged during compaction).
             * It's important to note that the old & new rows supplied here may not represent
             * the totality of the data for the Row with this particular Clustering. There may be
             * additional column data in SSTables which is not present in either the old or new row,
             * so implementations should be aware of that.
             * The supplied rows contain only column data which has actually been updated.
             * oldRowData contains only the columns which have been removed from the Row's
             * representation in the Memtable, while newRowData includes only new columns
             * which were not previously present. Any column data which is unchanged by
             * the update is not included.
             *
             * @param oldRowData data that was present in existing row and which has been removed from
             *                   the base table's Memtable
             * @param newRowData data that was not present in the existing row and is being inserted
             *                   into the base table's Memtable
             */
            @Override
            public void updateRow(Row oldRowData, Row newRowData) {
                mappingInfoLock.readLock().lock();
                try {
                    Rowcument rowcument = new Rowcument(newRowData);
                    rowcument.readCellValues(oldRowData, false);
                    
                    // delete static  row having all indexed static  columns are null => only pt columns not null
                    // delete regular row having all indexed regular columns are null => only pk columns not null
                    if (!MappingInfo.this.index_static_only || rowcument.isStatic) {
                        if (rowcument.complete())
                            rowcument.index();
                        else
                            rowcument.delete();
                    }
                    
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }


            /**
             * Notification that a row was removed from the partition.
             * Note that this is only called as part of either a compaction or a cleanup.
             * This context is indicated by the TransactionType supplied to the indexerFor method.
             *
             * As with updateRow, it cannot be guaranteed that all data belonging to the Clustering
             * of the supplied Row has been removed (although in the case of a cleanup, that is the
             * ultimate intention).
             * There may be data for the same row in other SSTables, so in this case Indexer implementations
             * should *not* assume that all traces of the row have been removed. In particular,
             * it is not safe to assert that all values associated with the Row's Clustering
             * have been deleted, so implementations which index primary key columns should not
             * purge those entries from their indexes.
             *
             * @param row data being removed from the base table
             */
            @Override
            public void removeRow(Row row) {
                mappingInfoLock.readLock().lock();
                try {
                    Rowcument rowcument = new Rowcument(row);
                    if (!MappingInfo.this.index_static_only || rowcument.isStatic) {
                        rowcument.delete();
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }

            /**
             * Notification of the end of the partition update.
             * This event always occurs after all others for the particular update.
             */
            @Override
            public void finish() {
                mappingInfoLock.readLock().lock();
                try {
                    for (MappingInfo.IndexInfo indexInfo : targetIndices(pkCols)) {
                        IndexShard indexShard = indexInfo.indexService.shard(0);
                        if (indexShard != null) {
                            if (indexInfo.refresh) {
                                try {
                                    indexShard.refresh("refresh_flag_index");
                                } catch (Throwable e) {
                                    logger.error("error", e);
                                }
                            }
                        }
                    }
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }

            private LivenessInfo getPrimaryKeyIndexLiveness(Row row) {
                long timestamp = row.primaryKeyLivenessInfo().timestamp();
                int ttl = row.primaryKeyLivenessInfo().ttl();
                for (Cell cell : row.cells())
                {
                    long cellTimestamp = cell.timestamp();
                    if (cell.isLive(nowInSec))
                    {
                        if (cellTimestamp > timestamp)
                        {
                            timestamp = cellTimestamp;
                            ttl = cell.ttl();
                        }
                    }
                }
                return LivenessInfo.create(baseCfs.metadata, timestamp, ttl, nowInSec);
            }
        }
    }

    

    public boolean isIndexing() {
        if (!runsElassandra) 
            return false;
        
        if (mappingInfo == null) {
            if (logger.isWarnEnabled())  
                logger.warn("No Elasticsearch index ready");
            return false;
        }
        if (mappingInfo.indices.size() == 0) {
            if (logger.isWarnEnabled())  
                logger.warn("No Elasticsearch index configured for {}.{}",this.baseCfs.metadata.ksName, this.baseCfs.metadata.cfName);
            return false;
        }
        return true;
    }
    
    public void initMapping() {
        mappingInfoLock.writeLock().lock();
        try {
           mappingInfo = new MappingInfo(this.clusterService.state());
           logger.debug("Secondary index=[{}] initialized, metadata.version={} mappingInfo.indices={}", 
                   index_name, mappingInfo.metadataVersion,  mappingInfo.indices.keySet());
        } catch(Exception e) {
             logger.error("Failed to update mapping index=[{}]",e ,index_name);
        } finally {
            mappingInfoLock.writeLock().unlock();
        }
    }
    
    // TODO: notify 2i only for udated indices (not all)
    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        boolean updateMapping = false;
        if (event.blocksChanged()) {
            updateMapping = true;
        } else {
            for (ObjectCursor<IndexMetaData> cursor : event.state().metaData().indices().values()) {
                IndexMetaData indexMetaData = cursor.value;
                if (indexMetaData.keyspace().equals(this.baseCfs.metadata.ksName) && 
                    indexMetaData.mapping(InternalCassandraClusterService.cfNameToType(this.baseCfs.name)) != null &&
                   (event.indexRoutingTableChanged(indexMetaData.getIndex()) || event.indexMetaDataChanged(indexMetaData))) {
                    updateMapping = true;
                    break;
                }
            }
        }
        if (updateMapping) {
            mappingInfoLock.writeLock().lock();
            try {
                mappingInfo = new MappingInfo(event.state());
                logger.debug("secondary index=[{}] metadata.version={} mappingInfo.indices={}",
                        this.index_name, event.state().metaData().version(), mappingInfo.indices.keySet() );
            } catch(Exception e) {
                logger.error("Failed to update mapping index=[{}]", e, index_name);
            } finally {
                mappingInfoLock.writeLock().unlock();
            }
        }
    }

    public Callable<?> getInitializationTask() 
    {
        return () -> {
            logger.debug("Initializing elastic secondary index [{}]", index_name);
            initMapping();
            
            // Avoid inter-bocking with Keyspace.open()->rebuild()->flush()->open().
            if (userKeyspaceInitialized)
                baseCfs.indexManager.buildIndexBlocking(this);
            return null;
        };
    }

    public Callable<?> getMetadataReloadTask(IndexMetadata indexMetadata) {
        return null;
    }

    /**
     * Cassandra index flush => Elasticsearch flush => lucene commit and disk sync.
     */
    public Callable<?> getBlockingFlushTask() 
    {
        return () -> {
             if (isIndexing()) {
                for(MappingInfo.IndexInfo indexInfo : mappingInfo.indices.values()) {
                    try {
                        IndexShard indexShard = indexInfo.indexService.shard(0);
                        if (indexShard != null) {
                            if (indexShard.state() == IndexShardState.STARTED)  {
                                indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                                if (logger.isDebugEnabled())
                                    logger.debug("Elasticsearch index=[{}] flushed",indexInfo.name);
                            } else {
                                if (logger.isDebugEnabled())
                                    logger.debug("Cannot flush index=[{}], state=[{}]",indexInfo.name, indexShard.state());
                            }
                        }
                    } catch (ElasticsearchException e) {
                        logger.error("Error while flushing index=[{}]",e,indexInfo.name);
                    }
                }
            }
            return null;
        };
    }
    
    static FileAttribute<?> snapshotDirPermissions = PosixFilePermissions.asFileAttribute(EnumSet.of(
            PosixFilePermission.OWNER_EXECUTE, 
            PosixFilePermission.OWNER_READ, 
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.OTHERS_EXECUTE,
            PosixFilePermission.OTHERS_READ));
    
    /**
     * Cassandra table snapshot => hard links associated elasticsearch lucene files.
     */
    @SuppressForbidden(reason="File used for snapshots")
    public Callable<?> getSnapshotWithoutFlushTask(String snapshotName) 
    {
        return () -> {
             if (isIndexing()) {
                for(MappingInfo.IndexInfo indexInfo : mappingInfo.indices.values()) {
                    IndexShard indexShard = indexInfo.indexService.shard(0);
                    if (indexShard != null && indexInfo.snapshot) {
                        if (indexShard.state() == IndexShardState.STARTED)  {
                            // snapshotPath = data/elasticsearch.data/<cluster_name>/nodes/0/snapshots
                            Path snapshotPath = indexShard.shardPath().resolveSnapshot();
                            if ((Files.notExists(snapshotPath))) 
                                Files.createDirectory(snapshotPath, snapshotDirPermissions);
                            
                            // snapshotIndex = data/elasticsearch.data/<cluster_name>/nodes/0/snapshots/<index_name>
                            Path snapshotIndex = snapshotPath.resolve(indexShard.shardId().getIndex());
                            if ((Files.notExists(snapshotIndex))) 
                                Files.createDirectory(snapshotIndex, snapshotDirPermissions);
                            
                            // snapshotDir = data/elasticsearch.data/<cluster_name>/nodes/0/snapshots/<index_name>/<snapshot_name>
                            Path snapshotDir = Files.createDirectory(snapshotIndex.resolve(snapshotName), snapshotDirPermissions);
                            Path indexPath = indexShard.shardPath().resolveIndex();
                            
                            try (DirectoryStream<Path> stream = Files.newDirectoryStream(indexPath, "{_*.*,segments*}")) {
                                for (Path luceneFile: stream) {
                                    File targetLink = new File(snapshotDir.toFile(), luceneFile.getFileName().toString());
                                    FileUtils.createHardLink(luceneFile.toFile(), targetLink);
                                }
                                if (logger.isDebugEnabled())
                                    logger.debug("Elasticsearch index=[{}], snapshot=[{}], path=[{}]",indexInfo.name, snapshotName, snapshotDir.toString());
                            } catch (DirectoryIteratorException ex) {
                                logger.error("Failed to retreive lucene files in {}", ex, indexPath);
                            }
                        } else {
                            if (logger.isDebugEnabled())
                                logger.debug("Cannot snapshot index=[{}], state=[{}], snapshot=[{}]",indexInfo.name, indexShard.state(), snapshotName);
                        }
                    }
                }
            }
            return null;
        };
    }

    public Callable<?> getInvalidateTask() {
        return () -> {
            this.clusterService.remove(this);
            elasticSecondayIndices.remove(index_name);
            return null;
        };
    }

    public Callable<?> getTruncateTask(long truncatedAt) {
        return () -> {
            if (isIndexing()) {
                for(MappingInfo.IndexInfo indexInfo : mappingInfo.indices.values()) {
                    try {
                        IndexShard indexShard = indexInfo.indexService.shard(0);
                        if (indexShard != null) {
                            DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(mappingInfo.typeName);
                            Query query = new TermQuery(new Term(TypeFieldMapper.NAME, docMapper.typeMapper().fieldType().indexedValueForSearch(mappingInfo.typeName)));
                            if (logger.isDebugEnabled()) {
                                logger.debug("truncating from ks.cf={}.{} query={} in elasticsearch index=[{}]", baseCfs.metadata.ksName, baseCfs.name, query, indexInfo.name);
                            }
                            DeleteByQuery deleteByQuery = new DeleteByQuery(query, null, null, null, null, Operation.Origin.PRIMARY, System.currentTimeMillis(), mappingInfo.typeName);
                            indexShard.engine().delete(deleteByQuery);
                        }
                    } catch (ElasticsearchException e) {
                        logger.error("Error while truncating index=[{}]", e, indexInfo.name);
                    }
                }
            }
            return null;
        };
    }

    public boolean shouldBuildBlocking() {
        return isIndexing();
    }


    public long getEstimatedResultRows() {
        // TODO Auto-generated method stub
        return 0;
    }


    public void validate(PartitionUpdate update) throws InvalidRequestException {
        // TODO Auto-generated method stub
        
    }

    public boolean dependsOn(ColumnDefinition column) {
        return this.indexedColumns.contains(column);
    }

    @Override
    public IndexMetadata getIndexMetadata() {
        return this.indexMetadata;
    }

    @Override
    public void register(IndexRegistry registry) {
        registry.registerIndex(this);
    }

    @Override
    public Optional<ColumnFamilyStore> getBackingTable() {
        return Optional.empty();
    }

    @Override
    public boolean supportsExpression(ColumnDefinition column, Operator operator) {
        return false;
    }

    @Override
    public AbstractType<?> customExpressionValueType() {
        return null;
    }

    @Override
    public RowFilter getPostIndexQueryFilter(RowFilter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BiFunction<PartitionIterator, ReadCommand, PartitionIterator> postProcessorFor(ReadCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Searcher searcherFor(ReadCommand command) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Indexer indexerFor(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup, Type transactionType) {
        if (isIndexing()) {
            try {
                return this.mappingInfo.new RowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
