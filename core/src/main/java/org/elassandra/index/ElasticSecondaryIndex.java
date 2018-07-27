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
package org.elassandra.index;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.Operator;
import org.apache.cassandra.cql3.statements.IndexTarget;
import org.apache.cassandra.db.Clustering;
import org.apache.cassandra.db.ClusteringBound;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.DeletionTime;
import org.apache.cassandra.db.PartitionColumns;
import org.apache.cassandra.db.RangeTombstone;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.ReadExecutionController;
import org.apache.cassandra.db.SinglePartitionReadCommand;
import org.apache.cassandra.db.Slice;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.filter.RowFilter;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.partitions.PartitionIterator;
import org.apache.cassandra.db.partitions.PartitionUpdate;
import org.apache.cassandra.db.rows.Cell;
import org.apache.cassandra.db.rows.CellPath;
import org.apache.cassandra.db.rows.Row;
import org.apache.cassandra.db.rows.RowIterator;
import org.apache.cassandra.db.rows.UnfilteredRowIterator;
import org.apache.cassandra.db.rows.UnfilteredRowIterators;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.index.IndexRegistry;
import org.apache.cassandra.index.transactions.IndexTransaction;
import org.apache.cassandra.index.transactions.IndexTransaction.Type;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.cassandra.utils.concurrent.OpOrder;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexOrDocValuesQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CloseableThreadLocal;
import org.elassandra.index.ElasticSecondaryIndex.ImmutableMappingInfo.WideRowcumentIndexer.WideRowcument;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.lucene.all.AllEntries;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.Engine.DeleteByQuery;
import org.elasticsearch.index.engine.Engine.IndexResult;
import org.elasticsearch.index.engine.Engine.Operation;
import org.elasticsearch.index.engine.EngineException;
import org.elasticsearch.index.mapper.BaseGeoPointFieldMapper;
import org.elasticsearch.index.mapper.BooleanFieldMapper;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.DynamicTemplate;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.GeoShapeFieldMapper;
import org.elasticsearch.index.mapper.IdFieldMapper;
import org.elasticsearch.index.mapper.IpFieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.ParentFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.RootObjectMapper;
import org.elasticsearch.index.mapper.RoutingFieldMapper;
import org.elasticsearch.index.mapper.SourceFieldMapper;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.TypeFieldMapper;
import org.elasticsearch.index.mapper.TypeParsers;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.UidFieldMapper;
import org.elasticsearch.index.mapper.VersionFieldMapper;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesService;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Custom secondary index for CQL3 only, should be created when mapping is applied and local shard started.
 * Index rows as documents when Elasticsearch clusterState has no write blocks and local shard is started.
 *
 * @author vroyer
 *
 */
public class ElasticSecondaryIndex implements Index, ClusterStateListener {
    
    public final static String ES_QUERY = "es_query";
    public final static ByteBuffer ES_QUERY_BYTE_BUFFER = ByteBufferUtil.bytes(ES_QUERY);
    public final static String ES_OPTIONS = "es_options";
    public final static ByteBuffer ES_OPTIONS_BYTE_BUFFER = ByteBufferUtil.bytes(ES_OPTIONS);

    private final static Field DEFAULT_INTERNAL_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, -1L);
    private final static Field DEFAULT_EXTERNAL_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, 1L);

    public static final Map<String, ElasticSecondaryIndex> elasticSecondayIndices = Maps.newConcurrentMap();
    public static final Pattern TARGET_REGEX = Pattern.compile("^(keys|entries|values|full)\\((.+)\\)$");
    
    public static boolean runsElassandra = false;
    
    final String index_name;
    final Logger logger;
    ClusterService clusterService;
    
    // updated when create/open/close/remove an ES index.
    protected final ReadWriteLock mappingInfoLock = new ReentrantReadWriteLock();
    protected volatile ImmutableMappingInfo mappingInfo;
    
    protected final ColumnFamilyStore baseCfs;
    protected final IndexMetadata indexMetadata;
    protected String typeName;
    protected TermQuery typeTermQuery;
    int initCounter = 0;
    protected volatile boolean buildSubmit = false;
    protected Object[] readBeforeWriteLocks = new Object[DatabaseDescriptor.getConcurrentWriters() * 128];
    
    ElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        this.baseCfs = baseCfs;
        this.indexMetadata = indexDef;
        this.index_name = baseCfs.keyspace.getName()+"."+baseCfs.name;

        this.typeName = ClusterService.cfNameToType(baseCfs.keyspace.getName(), ElasticSecondaryIndex.this.baseCfs.metadata.cfName);
        this.typeTermQuery = new TermQuery(new Term(TypeFieldMapper.NAME, typeName));

        this.logger = Loggers.getLogger(this.getClass().getName()+"."+baseCfs.keyspace.getName()+"."+baseCfs.name);
        for(int i=0; i < readBeforeWriteLocks.length; i++)
            readBeforeWriteLocks[i] = new Object();
    }
    
    public static ElasticSecondaryIndex newElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        ElasticSecondaryIndex esi = elasticSecondayIndices.computeIfAbsent(baseCfs.keyspace.getName()+"."+baseCfs.name, K -> new ElasticSecondaryIndex(baseCfs, indexDef));
        return esi;
    }
    
    // Public because it's also used to convert index metadata into a thrift-compatible format
    public static Pair<ColumnDefinition, IndexTarget.Type> parseTarget(CFMetaData cfm, IndexMetadata indexDef)
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
    
    public class Context extends ParseContext {
        private ImmutableMappingInfo.ImmutableIndexInfo indexInfo;
        private final ContentPath path = new ContentPath(0);
        private DocumentMapper docMapper;
        private Document document;
        private String id;
        private String parent;
        private Field version, uid;
        private final List<Document> documents = new ArrayList<Document>();
        private AllEntries allEntries = new AllEntries();
        private float docBoost = 1.0f;
        private List<Mapper> dynamicMappers = null;
        
        private boolean hasStaticField = false;
        private boolean finalized = false;
        private BytesReference source;
        private Object externalValue = null;
        
        public Context() {
        }
        
        public Context(ImmutableMappingInfo.ImmutableIndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(uid.type());
            assert this.docMapper != null;
            this.document = ii.indexStaticOnly() ? new StaticDocument("",null, uid) : new Document();
            this.documents.add(this.document);
        }
        
        public void reset(ImmutableMappingInfo.ImmutableIndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(uid.type());
            assert this.docMapper != null;
            this.document = ii.indexStaticOnly() ? new StaticDocument("",null, uid) : new Document();
            this.documents.clear();
            this.documents.add(this.document);
            this.id = uid.id();
            this.uid = new Field(UidFieldMapper.NAME, uid.toBytesRef(), UidFieldMapper.Defaults.FIELD_TYPE);;
            //this.path.reset();
            this.allEntries = (this.docMapper.allFieldMapper().enabled()) ? new AllEntries() : null;
            this.docBoost = 1.0f;
            this.dynamicMappers = null;
            this.parent = null;
            this.externalValue = null;
        }
    
        // recusivelly add fields
        public void addField(ImmutableMappingInfo.ImmutableIndexInfo indexInfo, Mapper mapper, Object value) throws IOException {
            if (logger.isTraceEnabled())
                logger.trace("doc[{}] class={} name={} value={}", this.documents.indexOf(doc()), mapper.getClass().getSimpleName(), mapper.name(), value);
            
            if (value == null && (!(mapper instanceof FieldMapper) || ((FieldMapper)mapper).fieldType().nullValue() == null))
                return;
            
            if (value instanceof Collection) {
                // flatten list or set of fields
                for(Object v : (Collection)value)
                    addField(indexInfo, mapper, v);
                return;
            }
           
            if (mapper instanceof GeoShapeFieldMapper) {
                GeoShapeFieldMapper geoShapeMapper = (GeoShapeFieldMapper) mapper;
                XContentParser parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, (String)value);
                parser.nextToken();
                ShapeBuilder shapeBuilder = ShapeBuilder.parse(parser, geoShapeMapper);
                externalValue = shapeBuilder.build();
                path().add(mapper.name());
                geoShapeMapper.parse(this);
                path().remove();
                externalValue = null;
            } else if (mapper instanceof BaseGeoPointFieldMapper) {
                BaseGeoPointFieldMapper geoPointFieldMapper = (BaseGeoPointFieldMapper) mapper;
                GeoPoint geoPoint;
                if (value instanceof String) {
                    // geo_point stored as geohash text
                    geoPoint = new GeoPoint((String)value);
                    geoPointFieldMapper.parse(this, geoPoint, (String)value);
                } else {
                    // geo_point stored in UDT.
                    Map<String, Double> geo_point =  (Map<String, Double>) value;
                    geoPoint = new GeoPoint(geo_point.get(BaseGeoPointFieldMapper.Names.LAT), geo_point.get(BaseGeoPointFieldMapper.Names.LON));
                    geoPointFieldMapper.parse(this, geoPoint, null);
                }
            }  else if (mapper instanceof FieldMapper) {
                ((FieldMapper)mapper).createField(this, value);
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

                //ContentPath.Type origPathType = path().pathType();
                //path().pathType(objectMapper.pathType());
   
                if (value instanceof Map<?,?>) {   
                    for(Entry<String,Object> entry : ((Map<String,Object>)value).entrySet()) {
                        // see http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html for locking a cache
                        if (mapper.cqlStruct().equals(Mapper.CqlStruct.MAP))
                            indexInfo.dynamicMappingUpdateLock.readLock().lock();
                        Mapper subMapper = objectMapper.getMapper(entry.getKey());
                        
                        if (subMapper == null) {
                            // try from the mapperService that could be updated
                            DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(indexInfo.type);
                            ObjectMapper newObjectMapper = docMapper.objectMappers().get(mapper.name());
                            subMapper = newObjectMapper.getMapper(entry.getKey());
                        }
                        if (subMapper == null) {
                            // dynamic field in top level map => update mapping and add the field.
                            ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(mapper.cqlName());
                            if (subMapper == null && cd != null && cd.type.isCollection() && cd.type instanceof MapType) {
                                logger.debug("Updating mapping for field={} type={} value={} ", entry.getKey(), cd.type.toString(), value);
                                CollectionType ctype = (CollectionType) cd.type;
                                if (ctype.kind == CollectionType.Kind.MAP && ((MapType)ctype).getKeysType().asCQL3Type().toString().equals("text")) {
                                    // upgrade to write lock
                                    indexInfo.dynamicMappingUpdateLock.readLock().unlock();
                                    indexInfo.dynamicMappingUpdateLock.writeLock().lock();
                                    try {
                                        // Recheck objectMapper because another thread might have acquired write lock and changed state before we did.
                                        if ((subMapper = objectMapper.getMapper(entry.getKey())) == null) {
                                            final String valueType = ClusterService.cqlMapping.get(((MapType)ctype).getValuesType().asCQL3Type().toString());
                                            final DynamicTemplate dynamicTemplate = docMapper.root().findTemplate(path, objectMapper.name()+"."+entry.getKey(), null);
                                            
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
                                                    builder.field(entry.getKey(), (dynamicTemplate != null) ? dynamicTemplate.mappingForName(entry.getKey(), valueType) : new HashMap<String,String>() {{ put("type", valueType); }});
                                                    builder.endObject();
                                                    hasProperties = true;
                                                } else {
                                                    builder.field(key, objectMapping.get(key));
                                                }
                                            }
                                            if (!hasProperties) {
                                                builder.startObject("properties");
                                                builder.field(entry.getKey(), (dynamicTemplate != null) ? dynamicTemplate.mappingForName(entry.getKey(), valueType) : new HashMap<String,String>() {{ put("type", valueType); }});
                                                builder.endObject();
                                            }
                                            builder.endObject().endObject().endObject().endObject();
                                            String mappingUpdate = builder.string();
                                            logger.info("updating mapping={}", mappingUpdate);
                                            
                                            ElasticSecondaryIndex.this.clusterService.blockingMappingUpdate(indexInfo.indexService, docMapper.type(), mappingUpdate);
                                            DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(indexInfo.type);
                                            ObjectMapper newObjectMapper = docMapper.objectMappers().get(mapper.name());
                                            subMapper = newObjectMapper.getMapper(entry.getKey());
                                            //assert subMapper != null : "dynamic subMapper not found for nested field ["+entry.getKey()+"]";
                                        }
                                    } catch (Exception e) {
                                        logger.error("error while updating mapping",e);
                                    } finally {
                                        // Downgrade by acquiring read lock before releasing write lock
                                        indexInfo.dynamicMappingUpdateLock.readLock().lock();
                                        indexInfo.dynamicMappingUpdateLock.writeLock().unlock();
                                    }
                                }
                            } else {
                                logger.error("Unexpected subfield={} for field={} column type={}, ignoring value={}",
                                        entry.getKey(), mapper != null ? mapper.name() : null, cd != null && cd.type != null ? cd.type.asCQL3Type().toString() : null, entry.getValue());
                            }
                        }

                        try {
                            if (subMapper != null) {
                                addField(indexInfo, subMapper, entry.getValue());
                            } else {
                                logger.error("submapper not found for nested field [{}]", entry.getKey());
                            }
                        } finally {
                            if (mapper.cqlStruct().equals(Mapper.CqlStruct.MAP))
                                indexInfo.dynamicMappingUpdateLock.readLock().unlock();
                        }    
                    }
                } else {
                    if (docMapper.type().equals(MapperService.PERCOLATOR_LEGACY_TYPE_NAME)) {
                        // store percolator query as source.
                        String sourceQuery = "{\"query\":"+value+"}";
                        if (logger.isDebugEnabled()) 
                            logger.debug("Store percolate query={}", sourceQuery);
                        
                        BytesReference source = new BytesArray(sourceQuery);
                        source( source );
                        BytesRef ref = source.toBytesRef();
                        doc().add(new StoredField(SourceFieldMapper.NAME, ref.bytes, ref.offset, ref.length));
                    }
                }
                
                // restore the enable path flag
                //path().pathType(origPathType);
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
                /*
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
                */
            }
        }
        
        public boolean hasStaticField() {
            return hasStaticField;
        }

        public void setStaticField(boolean hasStaticField) {
            this.hasStaticField = hasStaticField;
        }

        @Override
        public DocumentMapperParser docMapperParser() {
            return null;
        }
    
        public String index() {
            return indexInfo.name;
        }
    
        @Override
        public Settings indexSettings() {
            return indexInfo.indexService.clusterService().getSettings();
        }
    
        @Override
        public String type() {
            return this.docMapper.type();
        }
    
        public SourceToParse sourceToParse() {
            return null;
        }
    

        public BytesReference source() {
            return this.source;
        }
    

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
        public void id(String id) {
            this.id = id;
        }
    
        public String parent() {
            return parent;
        }
        
        public void parent(String parent) {
            this.parent = parent;
        }
        

        public Field uid() {
            return this.uid;
        }
    
        /**
         * Really, just the uid mapper should set this.
         */
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
        public void addDynamicMapper(Mapper mapper) {
            if (this.dynamicMappers == null)
                this.dynamicMappers = new ArrayList<>();
            dynamicMappers.add(mapper);
        }

        @Override
        public List<Mapper> getDynamicMappers() {
            return dynamicMappers;
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

    final class ImmutableMappingInfo {
        
        class ImmutableIndexInfo  {
            final String name;
            final String type;
            final boolean refresh;
            final boolean snapshot;
            final boolean includeNodeId;
            final IndexService indexService;
            final Map<String,Object> mapping;
            final boolean index_static_columns;
            final boolean index_static_only;
            final boolean index_on_compaction;
            final boolean index_static_document;
            final boolean versionLessEngine;
            
            Mapper[] mappers;   // inititalized in the ImmutableMappingInfo constructor.
            ReadWriteLock dynamicMappingUpdateLock;
            volatile boolean updated = false;

            public ImmutableIndexInfo(String name, IndexService indexService, MappingMetaData mappingMetaData, MetaData metadata, boolean versionLessEngine) throws IOException {
                this.name = name;
                this.versionLessEngine = versionLessEngine;
                this.indexService = indexService;
                this.mapping = mappingMetaData.sourceAsMap();
                this.type = mappingMetaData.type();
                
                Map<String,Object> mappingMap = (Map<String,Object>)mappingMetaData.getSourceAsMap();
                Map<String,Object> metaMap = (mappingMap == null) ? null : (Map<String,Object>)mappingMap.get("_meta");
                
                this.refresh = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_SYNCHRONOUS_REFRESH_SETTING);
                this.snapshot = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_SNAPSHOT_WITH_SSTABLE_SETTING);
                this.includeNodeId = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_INCLUDE_NODE_ID_SETTING);
                
                this.index_on_compaction = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_INDEX_ON_COMPACTION_SETTING);
                this.index_static_columns = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_INDEX_STATIC_COLUMNS_SETTING);
                this.index_static_only = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_INDEX_STATIC_ONLY_SETTING);
                this.index_static_document = getMetaSettings(metadata.settings(), indexService.getIndexSettings(), metaMap, IndexMetaData.INDEX_INDEX_STATIC_DOCUMENT_SETTING);
            }

            // get _meta, index, cluster or system settings.
            public boolean getMetaSettings(Settings metadataSettings, IndexSettings indexSettings, Map<String,Object> metaMap, Setting propName) {
                final boolean value;
                if (metaMap != null && metaMap.get(propName.getKey().substring(6)) != null) {
                    // substring(6) = remove "index." from the index settings.
                    value = XContentMapValues.nodeBooleanValue(metaMap.get(propName.getKey().substring(6)));
                } else {
                    value = (Boolean)indexSettings.getValue(propName);
                    //(ClusterService.INDEX_PREFIX+propName, metadataSettings.getAsBoolean(ClusterService.CLUSTER_PREFIX+propName, Boolean.getBoolean(ClusterService.SYSTEM_PREFIX+propName)));
                }
                logger.debug("index.type=[{}.{}] {}=[{}]", name, this.type, propName.getKey(), value);
                return value;
            }
            
            public int indexOf(String f) {
                return ImmutableMappingInfo.this.fieldsToIdx.getOrDefault(f, -1);
            }
            
            public boolean isStaticField(int idx) {
                return (staticColumns == null) ? false : staticColumns.get(idx);
            }
            
            public IndexShard shard() {
                final IndexShard indexShard = indexService.getShardOrNull(0);
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
            
            public void refresh() {
                if (this.refresh) {
                    IndexShard shard = shard();
                    if (shard != null) {
                        try {
                            shard.refresh("synchronous_refresh");
                        } catch (Throwable e) {
                            logger.error("error", e);
                        }
                    }
                }
            }
            
            public void deleteByQuery(final Object pkCols[], RangeTombstone tombstone) {
                IndexShard shard = shard();
                if (shard != null) {
                    Slice slice = tombstone.deletedSlice();
                    ClusteringBound start = slice.start();
                    ClusteringBound end = slice.end();
    
                    DocumentMapper docMapper = indexService.mapperService().documentMapper(typeName);
                    BooleanQuery.Builder builder = new BooleanQuery.Builder();
                    builder.add( typeTermQuery, Occur.FILTER);
                    
                    int partitionKeyLen = baseCfs.metadata.partitionKeyColumns().size();
                    
                    // build the primary key part of the delete by query
                    int i = 0;
                    for(ColumnDefinition cd : baseCfs.metadata.primaryKeyColumns()) {
                        if (i >= (partitionKeyLen + Math.max(start.size(), end.size())))
                            break;
                        if (indexedPkColumns[i]) {
                            FieldMapper mapper = docMapper.mappers().smartNameFieldMapper(cd.name.toString());
                            Query q;
                            if (i < partitionKeyLen) {
                                q = buildQuery( cd, mapper, pkCols[i], pkCols[i], true, true);
                            } else {
                                ByteBuffer startByteBuffer = null, endByteBuffer = null;
                                boolean startIsInclusive = true, endIsInclusive = true;
                                if (i - partitionKeyLen < start.size()) {
                                    startByteBuffer = start.get(i - partitionKeyLen);
                                    startIsInclusive = start.isInclusive();
                                }
                                if (i - partitionKeyLen < end.size()) {
                                    endByteBuffer = end.get(i - partitionKeyLen);
                                    endIsInclusive = end.isInclusive();
                                }
                                q = buildQuery( cd, mapper, startByteBuffer, endByteBuffer, startIsInclusive, endIsInclusive);
                            }
                            builder.add(q , Occur.FILTER);
                        }
                        i++;
                    }
                    
                    Query query = builder.build();
                    if (logger.isTraceEnabled()) {
                        logger.trace("delete rangeTombstone from ks.cf={}.{} query={} in elasticsearch index=[{}]", baseCfs.metadata.ksName, baseCfs.name, query, name);
                    }
                    if (!updated)
                        updated = true;
                    DeleteByQuery deleteByQuery = new DeleteByQuery(query, null, null, null, null, Operation.Origin.PRIMARY, System.currentTimeMillis(), typeName);
                    shard.getEngine().delete(deleteByQuery);
                }
            }
            
            /**
             * Build range query to remove a row slice.
             * @param cd
             * @param mapper
             * @param lower
             * @param upper
             * @param includeLower
             * @param includeUpper
             * @return
             */
            private Query buildQuery(ColumnDefinition cd, FieldMapper mapper, ByteBuffer lower, ByteBuffer upper, boolean includeLower, boolean includeUpper) {
                Object start = lower == null ? null : cd.type.compose(lower);
                Object end = upper == null ? null : cd.type.compose(upper);
                return buildQuery(cd, mapper, start, end, includeLower, includeUpper);
            }
            
            @SuppressForbidden(reason="unchecked")
            private Query buildQuery(ColumnDefinition cd, FieldMapper mapper, Object start, Object end, boolean includeLower, boolean includeUpper) {
                Query query = null;
                if (mapper != null) {
                    CQL3Type cql3Type = cd.type.asCQL3Type();
                    if (cql3Type instanceof CQL3Type.Native) {
                        switch ((CQL3Type.Native) cql3Type) {
                        case ASCII:
                        case TEXT:
                        case VARCHAR:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ? 
                                    new TermQuery(new Term(mapper.name(),  BytesRefs.toBytesRef(start))) :
                                    new TermRangeQuery(mapper.name(),  BytesRefs.toBytesRef(start),  BytesRefs.toBytesRef(end), includeLower, includeUpper);
                            break;
                        case INT:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ? 
                                    NumberFieldMapper.NumberType.INTEGER.termQuery(mapper.name(), (Integer) start) :
                                    NumberFieldMapper.NumberType.INTEGER.rangeQuery(mapper.name(), (Integer) start, (Integer) end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                            break;
                        case SMALLINT:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ? 
                                    NumberFieldMapper.NumberType.SHORT.termQuery(mapper.name(), (Short) start) :
                                    NumberFieldMapper.NumberType.SHORT.rangeQuery(mapper.name(), (Short) start, (Short) end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                            break;
                        case TINYINT:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ?
                                    NumberFieldMapper.NumberType.BYTE.termQuery(mapper.name(), (Byte) start) :
                                    NumberFieldMapper.NumberType.BYTE.rangeQuery(mapper.name(), (Byte) start, (Byte) end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                            break;
                        case INET:
                            IpFieldMapper ipMapper = (IpFieldMapper)mapper;
                            query = start != null && end != null && ((InetAddress)start).equals((InetAddress)end) ?
                                    ipMapper.fieldType().termQuery(start, null) :
                                    ipMapper.fieldType().rangeQuery(start, end, includeLower, includeUpper, null);
                            break;
                        case TIMESTAMP: {
                                DateFieldMapper dateMapper = (DateFieldMapper)mapper;
                                long l, u;
                                if (start == null) {
                                    l = Long.MIN_VALUE;
                                } else {
                                    l = ((Date)start).getTime();
                                    if (includeLower == false) {
                                        ++l;
                                    }
                                }
                                if (end == null) {
                                    u = Long.MAX_VALUE;
                                } else {
                                    u = ((Date)end).getTime();
                                    if (includeUpper == false) {
                                        --u;
                                    }
                                }
                                query = LongPoint.newRangeQuery(mapper.name(), l, u);
                                if (dateMapper.fieldType().hasDocValues()) {
                                    Query dvQuery = SortedNumericDocValuesField.newRangeQuery(mapper.name(), l, u);
                                    query = new IndexOrDocValuesQuery(query, dvQuery);
                                }
                            }
                            break;
                        case BIGINT:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ? 
                                    NumberFieldMapper.NumberType.LONG.termQuery(mapper.name(), (Long) start) :
                                    NumberFieldMapper.NumberType.LONG.rangeQuery(mapper.name(), (Long) start, (Long) end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                            break;
                        case DOUBLE:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ?
                                    NumberFieldMapper.NumberType.DOUBLE.termQuery(mapper.name(), (Double) start) :
                                    NumberFieldMapper.NumberType.DOUBLE.rangeQuery(mapper.name(), (Double) start, (Double) end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                            break;
                        case FLOAT:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ?
                                    NumberFieldMapper.NumberType.FLOAT.termQuery(mapper.name(), (Float) start) :
                                    NumberFieldMapper.NumberType.FLOAT.rangeQuery(mapper.name(), (Float) start, (Float) end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                            break;
                        case TIMEUUID:
                            if (start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0) {
                                query = (mapper instanceof DateFieldMapper) ?
                                         NumberFieldMapper.NumberType.LONG.termQuery(mapper.name(), UUIDGen.unixTimestamp((UUID) start)) :
                                         new TermQuery(new Term(mapper.name(), BytesRefs.toBytesRef(start)));
                            } else {
                                if (mapper instanceof DateFieldMapper) {
                                    long l = (start == null) ? Long.MIN_VALUE : UUIDGen.unixTimestamp((UUID) start);
                                    long u = (end == null) ? Long.MAX_VALUE : UUIDGen.unixTimestamp((UUID) end);
                                    query = NumberFieldMapper.NumberType.LONG.rangeQuery(mapper.name(), l, u,  includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                } else {
                                    query = new TermRangeQuery(mapper.name(), BytesRefs.toBytesRef(start),  BytesRefs.toBytesRef(end), includeLower, includeUpper);
                                }
                            }
                            break;
                        case UUID:
                            query = start != null && end != null && ((Comparable)start).compareTo((Comparable)end) == 0 ?
                                    new TermQuery(new Term(mapper.name(), BytesRefs.toBytesRef(start))) :
                                    new TermRangeQuery(mapper.name(), BytesRefs.toBytesRef(start), BytesRefs.toBytesRef(end), includeLower, includeUpper);
                            break;
                        case BOOLEAN:
                            query = ((BooleanFieldMapper)mapper).fieldType().rangeQuery(start, end, includeLower, includeUpper, null);
                            break;
                        case DECIMAL:
                            throw new UnsupportedOperationException("Unsupported type [decimal] in primary key");
                        case BLOB:
                            throw new UnsupportedOperationException("Unsupported type [blob] in primary key");
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Object type in primary key not supported");
                }
                return query;
            }
        
            public boolean indexStaticOnly() {
                return this.index_static_only;
            }
            
            @Override
            public String toString() {
                return this.name;
            }
        }

        class ImmutablePartitionFunction {
            final String name;
            final String pattern;
            final String[] fields;      // indexed fields used in the partition function
            final int[]    fieldsIdx;   // column position in Rowcument.values
            final Set<String> indices;  // associated indices
            final PartitionFunction partitionFunction;
            
            ImmutablePartitionFunction(String[] args) {
                this(args, new MessageFormatPartitionFunction());
            }
            
            ImmutablePartitionFunction(String[] args, PartitionFunction partitionFunc) {
                this.name = args[0];
                this.pattern = args[1];
                this.fields = new String[args.length-2];
                this.fieldsIdx = new int[args.length-2];
                System.arraycopy(args, 2, this.fields, 0, args.length-2);
                this.indices = new HashSet<String>();
                this.partitionFunction = partitionFunc;
            }
            
            // values = indexed values in the same order as MappingInfo.fields
            String indexName(Object[] values) {
                Object[] args = new Object[fields.length];
                for(int i=0; i < fieldsIdx.length; i++)
                    args[i] = (fieldsIdx[i] < values.length) ? values[fieldsIdx[i]] : null; 
                return partitionFunction.format(pattern, args);
            }
            
            public String toString() {
                return this.name;
            }
        }

        
        final Map<String, ImmutablePartitionFunction> partitionFunctions; 
        final ImmutableIndexInfo[] indices;
        final ObjectIntHashMap<String> indexToIdx;
        final ObjectIntHashMap<String> fieldsToIdx;
        final BitSet fieldsToRead;
        final BitSet staticColumns;
        final boolean hasIndexedMultiCell;
        final boolean indexSomeStaticColumnsOnWideRow; 
        final boolean[] indexedPkColumns;   // bit mask of indexed PK columns.
        final long metadataVersion;
        final String metadataClusterUUID;
        final String nodeId;
        final boolean indexOnCompaction;  // true if at least one index has index_on_compaction=true;
        
        ImmutableMappingInfo(final ClusterState state) {
            this.metadataVersion = state.metaData().version();
            this.metadataClusterUUID = state.metaData().clusterUUID();
            this.nodeId = state.nodes().getLocalNodeId();
            
            if (state.blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                logger.debug("global write blocked");
                this.indices = null;
                this.indexToIdx = null;
                this.fieldsToIdx = null;
                this.fieldsToRead = null;
                this.staticColumns = null;
                this.hasIndexedMultiCell = false;
                this.indexSomeStaticColumnsOnWideRow = false;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                this.indexOnCompaction = false;
                return;
            }
            
            Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();
            Map<String, ImmutablePartitionFunction> partFuncs = null;
            List<ImmutableIndexInfo> indexList = new ArrayList<ImmutableIndexInfo>();
            
            for(IndexMetaData indexMetaData:state.metaData()) {
                if (!ElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.keyspace()))
                   continue;
                
                String index = indexMetaData.getIndex().getName();
                MappingMetaData mappingMetaData = indexMetaData.mapping(typeName);
                
                if (mappingMetaData == null)
                    continue;
                
                if (indexMetaData.getState() != IndexMetaData.State.OPEN) {
                    if (logger.isDebugEnabled())
                        logger.debug("ignore, index=[{}] not OPEN", index);
                    continue;
                }
                
                ClusterBlockException clusterBlockException = state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, index);
                if (clusterBlockException != null) {
                    if (logger.isInfoEnabled())
                        logger.info("ignore, index=[{}] blocked blocks={}", index, clusterBlockException.blocks());
                    continue;
                }
                
                try {
                    Map<String,Object> mappingMap = (Map<String,Object>)mappingMetaData.getSourceAsMap();
                    if (mappingMap.get("properties") != null) {
                        // #181 IndiceService is available when activated and before Node start.
                        IndicesService indicesService = ElasticSecondaryIndex.this.clusterService.getIndicesService();
                        IndexService indexService = indicesService.indexService(indexMetaData.getIndex());
                        if (indexService == null) {
                            logger.error("indexService not available for [{}], ignoring" , index);
                            continue;
                        }
                        ImmutableIndexInfo indexInfo = new ImmutableIndexInfo(index, indexService, mappingMetaData, state.metaData(), IndexMetaData.isIndexUsingVersionLessEngine(indexMetaData.getSettings()));
                        indexList.add(indexInfo);
                        
                        Map<String,Object> props = (Map<String,Object>)mappingMap.get("properties");
                        for(String fieldName : props.keySet() ) {
                            Map<String,Object> fieldMap = (Map<String,Object>)props.get(fieldName);
                            if (fieldMap.get("enabled") == null || XContentMapValues.nodeBooleanValue(fieldMap.get("enabled"))) {
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
                                partFuncs = new HashMap<String, ImmutablePartitionFunction>();
                            ImmutablePartitionFunction func = partFuncs.get(pf[0]);
                            if (func == null) {
                                func = new ImmutablePartitionFunction(pf, indexMetaData.partitionFunctionClass());
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
            
            if (indexList.size() == 0) {
                if (logger.isTraceEnabled())
                    logger.warn("No active elasticsearch index for keyspace.table=[{}.{}] state={}",baseCfs.metadata.ksName, baseCfs.name, state);
                this.indices = null;
                this.indexToIdx = null;
                this.fieldsToIdx = null;
                this.fieldsToRead = null;
                this.staticColumns = null;
                this.hasIndexedMultiCell = false;
                this.indexSomeStaticColumnsOnWideRow = false;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                this.indexOnCompaction = false;
                return;
            }

            // build indices array and indexToIdx map
            this.indices = new ImmutableIndexInfo[indexList.size()];
            this.indexToIdx = new ObjectIntHashMap<String>(indexList.size());
            for(int i = 0; i < indexList.size(); i++) {
                indices[i] = indexList.get(i);
                indexToIdx.put(indexList.get(i).name, i);
            }
            
            // order fields with pk columns first
            final String[] fields = new String[fieldsMap.size()];
            final int pkLength = baseCfs.metadata.partitionKeyColumns().size()+baseCfs.metadata.clusteringColumns().size();
            this.indexedPkColumns = new boolean[pkLength];
            int j=0, l=0;
            for(ColumnDefinition cd : Iterables.concat(baseCfs.metadata.partitionKeyColumns(), baseCfs.metadata.clusteringColumns())) {
                indexedPkColumns[l] = fieldsMap.containsKey(cd.name.toString());
                if (indexedPkColumns[l])
                    fields[j++] = cd.name.toString();
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
            // build a map for fields, as it is O(1) rather than O(n) for an array.
            this.fieldsToIdx = new ObjectIntHashMap<String>(fields.length);
            for(int i=0; i < fields.length; i++)
                this.fieldsToIdx.put(fields[i], i);
            
            this.fieldsToRead = new BitSet(fields.length);
            this.staticColumns = (baseCfs.metadata.hasStaticColumns()) ? new BitSet(fields.length) : null;
            boolean hasMultiCellColumn = false;
            for(int i=0; i < fields.length; i++) {
                ColumnIdentifier colId = new ColumnIdentifier(fields[i], true);
                ColumnDefinition colDef = baseCfs.metadata.getColumnDefinition(colId);
                if (colDef != null) {
                    // colDef may be null when mapping an object with no sub-field (and no underlying column, see #144)
                    this.fieldsToRead.set(i, fieldsMap.get(fields[i]) && !colDef.isPrimaryKeyColumn());
                    hasMultiCellColumn |= colDef.type.isMultiCell();
                    if (staticColumns != null)
                        this.staticColumns.set(i, colDef.isStatic());
                } else {
                    this.fieldsToRead.set(i, false);
                }
            }
            this.hasIndexedMultiCell = hasMultiCellColumn;
            
            if (partFuncs != null && partFuncs.size() > 0) {
                for(ImmutablePartitionFunction func : partFuncs.values()) {
                    int i = 0;
                    for(String field : func.fields)
                        func.fieldsIdx[i++] = this.fieldsToIdx.getOrDefault(field, -1);
                }
                this.partitionFunctions = partFuncs;
            } else {
                this.partitionFunctions = null;
            }
            
            // build InderInfo.mappers arrays.
            for(ImmutableIndexInfo indexInfo : this.indices) {
                indexInfo.mappers = new Mapper[fields.length];
                for(int i=0; i < fields.length; i++) {
                    DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(typeName);
                    Mapper mapper = fields[i].startsWith(ParentFieldMapper.NAME) ?
                            docMapper.parentFieldMapper() : docMapper.mappers().smartNameFieldMapper(fields[i]); // workaround for _parent#<join_type>
                    if (mapper != null) {
                        indexInfo.mappers[i] = mapper;
                    } else {
                        ObjectMapper objectMapper = docMapper.objectMappers().get(fields[i]);
                        if (objectMapper != null && objectMapper.cqlStruct().equals(Mapper.CqlStruct.MAP))
                            indexInfo.dynamicMappingUpdateLock = new ReentrantReadWriteLock();
                        indexInfo.mappers[i] = objectMapper;
                    }
                }
            }
            
            boolean _indexSomeStaticColumns = false;
            boolean _indexOnCompaction = false;
            for(ImmutableIndexInfo indexInfo : this.indices) {
                if (indexInfo.index_static_columns)
                    _indexSomeStaticColumns = true;
                if (indexInfo.index_on_compaction)
                    _indexOnCompaction = true;
                if (_indexSomeStaticColumns && _indexOnCompaction)
                    break;
            }
            this.indexSomeStaticColumnsOnWideRow = _indexSomeStaticColumns;
            this.indexOnCompaction = _indexOnCompaction;
        }
        
        public BitSet targetIndices(final Object[] values) {
            if (this.partitionFunctions == null)
                return null;
            
            BitSet targets = new BitSet(this.indices.length);
            for(ImmutablePartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                int indexIdx = this.indexToIdx.getOrDefault(indexName, -1);
                if (indexIdx >= 0) {
                    targets.set(indexIdx);
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("No target index=[{}] found for partition function name=[{}] pattern=[{}] indices={}", 
                                indexName, func.name, func.pattern, 
                                Arrays.stream(mappingInfo.indices).map(i -> i.name).collect(Collectors.joining()));
                }
            }
            if (logger.isTraceEnabled()) 
                logger.trace("Partition index bitset={} indices={}", targets, this.indices);
            return targets;
        }
        
        public BitSet targetIndicesForDelete(final Object[] values) {
            if (this.partitionFunctions == null)
                return null;
            
            BitSet targets = new BitSet(this.indices.length);
            for(ImmutablePartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                int indexIdx = this.indexToIdx.getOrDefault(indexName, -1);
                if (indexIdx >= 0) {
                    targets.set(indexIdx);
                } else {
                    if (logger.isWarnEnabled())
                        logger.warn("No target index=[{}] found, function name=[{}] pattern=[{}], return all indices={}", 
                                indexName, func.name, func.pattern, 
                                Arrays.stream(mappingInfo.indices).map(i -> i.name).collect(Collectors.joining()));
                    for(String index : func.indices) {
                        int i = this.indexToIdx.getOrDefault(index, -1);
                        if (i >= 0)
                            targets.set(i);
                    }
                }
            }
            return targets;
        }

        class WideRowcumentIndexer extends RowcumentIndexer {
            NavigableSet<Clustering> clusterings = new java.util.TreeSet<Clustering>(baseCfs.metadata.comparator);
            Map<Clustering, WideRowcument> rowcuments = new TreeMap<Clustering, WideRowcument>(baseCfs.metadata.comparator);
            Row inStaticRow, outStaticRow;
            
            public WideRowcumentIndexer(final DecoratedKey key,
                    final PartitionColumns columns,
                    final int nowInSec,
                    final OpOrder.Group opGroup,
                    final IndexTransaction.Type transactionType) {
                super(key, columns, nowInSec, opGroup, transactionType);
            }
            
            public class WideRowcument extends Rowcument {

                public WideRowcument(Row inRow, Row outRow) throws IOException {
                    super(inRow, outRow);
                }
                
                /**
                 * Check for missing fields
                 * @return true if the rowcument needs some fields.
                 */
                public boolean hasMissingFields() {
                    // add static fields before checking for missing fields
                    try {
                        if (inStaticRow != null)
                            readCellValues(inStaticRow, true);
                        if (outStaticRow != null)
                            readCellValues(outStaticRow, false);
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
                    return super.hasMissingFields();
                }
            }
            
            @Override
            public void collect(Row inRow, Row outRow) {
                try {
                    if (logger.isTraceEnabled()) {
                        if (inRow != null)
                            logger.trace("indexer={} inRowData={} clustering={} static={} hasLiveData={}", 
                                    WideRowcumentIndexer.this.hashCode(), inRow.toString(baseCfs.metadata, true, true), inRow.clustering(), 
                                    inRow.isStatic(), inRow.hasLiveData(nowInSec, baseCfs.metadata.enforceStrictLiveness())); 
                        if (outRow != null)
                            logger.trace("indexer={} outRowData={} clustering={} static={} hasLiveData={}", 
                                    WideRowcumentIndexer.this.hashCode(), outRow.toString(baseCfs.metadata, true, true), outRow.clustering(), 
                                    outRow.isStatic(), outRow.hasLiveData(nowInSec, baseCfs.metadata.enforceStrictLiveness())); 
                    }

                    Row row = (inRow != null) ? inRow : outRow;
                    if (row.isStatic()) {
                        inStaticRow = inRow;
                        outStaticRow = outRow;
                    } else {
                        clusterings.add(row.clustering());
                        rowcuments.put(row.clustering(), new WideRowcument(inRow, outRow));
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                }
            }
            
            @Override
            public void flush() {
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} inStaticRow={} outStaticRow={} clustering={}", this.hashCode(), inStaticRow, outStaticRow, this.clusterings);
                
                switch(transactionType) {
                case CLEANUP:
                    for(WideRowcument rowcument : rowcuments.values())
                        rowcument.delete();
                    break;
                case COMPACTION:
                case UPDATE:
                    if (!clusterings.isEmpty()) {
                        boolean hasMissingFields = false;
                        for(WideRowcument rowcument : rowcuments.values()) {
                            if (rowcument.hasMissingFields()) {
                                hasMissingFields = true;
                                break;
                            }
                        }
                        if (hasMissingFields) {
                            if (logger.isTraceEnabled())
                                logger.trace("indexer={} read partition for clusterings={}", this.hashCode(), clusterings);
                            synchronized(getLock()) {
                                SinglePartitionReadCommand command = SinglePartitionReadCommand.create(baseCfs.metadata, nowInSec, key, clusterings);
                                RowIterator rowIt = read(command);
                                if (!rowIt.staticRow().isEmpty())
                                    this.inStaticRow = rowIt.staticRow();
                                for(; rowIt.hasNext(); ) {
                                    try {
                                        Row row = rowIt.next();
                                        WideRowcument rowcument = new WideRowcument(row, null);
                                        if (indexSomeStaticColumnsOnWideRow && inStaticRow != null)
                                            rowcument.readCellValues(inStaticRow, true);
                                        rowcuments.put(row.clustering(), rowcument);
                                    } catch (IOException e) {
                                        logger.error("Unexpected error", e);
                                    }
                                }
                                for(WideRowcument rowcument : rowcuments.values())
                                    rowcument.write();
                            }
                        } else {
                            for(WideRowcument rowcument : rowcuments.values())
                                rowcument.write();
                        }
                    }
                }
                
                // index static document.
                if (this.inStaticRow != null) {
                    try {
                        WideRowcument rowcument = new WideRowcument(inStaticRow, outStaticRow);
                        rowcument.write();
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
                }
            }
            
            /**
             * Notification of a top level partition delete.
             * Deleting a wide-row require a deleteByQuery on doc type + partition key = elasticsearch _routing.
             * @param deletionTime
             */
            @Override
            public void partitionDelete(IndexShard indexShard) throws IOException {
                if (logger.isTraceEnabled())
                    logger.trace("deleting documents where _routing={} from index.type={}.{}", this.partitionKey, indexShard.shardId().getIndexName(), typeName);
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                builder.add(typeTermQuery, Occur.FILTER);
                builder.add(new TermQuery(new Term(RoutingFieldMapper.NAME, this.partitionKey)), Occur.FILTER);
                DeleteByQuery deleteByQuery = new DeleteByQuery(builder.build(), null, null, null, null, Operation.Origin.PRIMARY, System.currentTimeMillis(), typeName);
                indexShard.getEngine().delete(deleteByQuery);
            }
            
            /**
             * Notification of a RangeTombstone.
             * An update of a single partition may contain multiple RangeTombstones,
             * and a notification will be passed for each of them.
             * @param tombstone
             */
            @Override
            public void rangeTombstone(RangeTombstone tombstone) {
                logger.trace("range tombestone row {}: {}", this.transactionType, tombstone.deletedSlice());
                try {
                    BitSet targets = targetIndices(pkCols);
                    if (targets == null) {
                        for(ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices)
                            indexInfo.deleteByQuery(pkCols, tombstone);
                    } else {
                        for(int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i+1))
                            indices[i].deleteByQuery(pkCols, tombstone);
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                }
            }    
        }
        
        class SkinnyRowcumentIndexer extends RowcumentIndexer {
            SkinnyRowcument rowcument;
            
            public SkinnyRowcumentIndexer(final DecoratedKey key,
                    final PartitionColumns columns,
                    final int nowInSec,
                    final OpOrder.Group opGroup,
                    final IndexTransaction.Type transactionType) {
                super(key, columns, nowInSec, opGroup, transactionType);
            }
            
            public class SkinnyRowcument extends Rowcument {
                
                public SkinnyRowcument(Row inRow, Row outRow) throws IOException {
                    super(inRow, outRow);
                }
            }

            @Override
            public void collect(Row inRow, Row outRow) {
                try {
                    this.rowcument = new SkinnyRowcument(inRow, outRow);
                } catch (IOException e) {
                    logger.error("Unexpected error", e);
                }
            }
            
            @Override
            public void flush() {
                if (rowcument != null) {
                    switch(transactionType) {
                    case CLEANUP:
                        this.rowcument.delete();
                        break;
                    case COMPACTION: // remove expired row or reindex a doc when a column has expired, happen only when index_on_compaction=true for at least one elasticsearch index.
                    case UPDATE:
                        if (rowcument.hasMissingFields()) {
                            synchronized(getLock()) {
                                SinglePartitionReadCommand command = SinglePartitionReadCommand.fullPartitionRead(baseCfs.metadata, nowInSec, key);
                                RowIterator rowIt = read(command);
                                if (rowIt.hasNext())
                                    try {
                                        rowcument = new SkinnyRowcument(rowIt.next(), null);
                                    } catch (IOException e) {
                                        logger.error("Unexpected error", e);
                                    }
                                rowcument.write();
                            }
                        } else {
                            rowcument.write();
                        }
                    }
                }
            }
            
            /**
             * Notification of a top level partition delete.
             * @param deletionTime
             */
            @Override
            public void partitionDelete(IndexShard indexShard) throws IOException {
                Term termUid = new Term(UidFieldMapper.NAME, BytesRefs.toBytesRef(Uid.createUid(typeName, this.partitionKey)));
                if (logger.isDebugEnabled())
                    logger.debug("deleting document from index.type={}.{} id={} termUid={}", indexShard.shardId().getIndexName(), typeName, this.partitionKey, termUid.text());
                Engine.Delete delete = new Engine.Delete(typeName, this.partitionKey, termUid);
                indexShard.delete(indexShard.getEngine(), delete);
            }
        }
        
        abstract class RowcumentIndexer implements Index.Indexer {
            final DecoratedKey key;
            final int nowInSec;
            final IndexTransaction.Type transactionType;
            final OpOrder.Group opGroup;
            final Object[] pkCols = new Object[baseCfs.metadata.partitionKeyColumns().size()+baseCfs.metadata.clusteringColumns().size()];
            final String partitionKey;
            BitSet targets = null;
            
            public RowcumentIndexer(final DecoratedKey key,
                    final PartitionColumns columns,
                    final int nowInSec,
                    final OpOrder.Group opGroup,
                    final IndexTransaction.Type transactionType) {
                this.key = key;
                this.nowInSec = nowInSec;
                this.opGroup = opGroup;
                this.transactionType = transactionType;
                
                AbstractType<?> keyValidator = baseCfs.metadata.getKeyValidator();
                int i = 0;
                if (keyValidator instanceof CompositeType) {
                    CompositeType composite = (CompositeType) keyValidator;
                    for(ByteBuffer bb : composite.split(key.getKey())) {
                        AbstractType<?> type = composite.types.get(i);
                        pkCols[i++] = type.compose(bb);
                    }
                } else {
                    pkCols[i++] = keyValidator.compose(key.getKey());
                }
                this.partitionKey = ClusterService.stringify(pkCols, i);
            }

            // return a per partition object for read-before-write locking
            protected Object getLock() {
                return readBeforeWriteLocks[Math.abs(key.hashCode() % readBeforeWriteLocks.length)];
            }

            /**
             * Notification of the start of a partition update.
             * This event always occurs before any other during the update.
             */
            @Override
            public void begin() {
                
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
                logger.trace("insert row {}: {}", this.transactionType, row);
                collect(row, null);
            }

            /**
             * Notification of a modification to a row in the base table's Memtable.
             * This is allow an Index implementation to clean up entries for base data which is
             * never flushed to disk (and so will not be purged during compaction).
             * It's important to note that the old  new rows supplied here may not represent
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
                logger.trace("update row {}: {} to {}", this.transactionType, oldRowData, newRowData);
                collect(newRowData, oldRowData);
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
                logger.trace("remove row {}: {}", this.transactionType, row);
                collect(null, row);
            }

            /**
             * Notification of the end of the partition update.
             * This event always occurs after all others for the particular update.
             */
            @Override
            public void finish() {
                try {
                    flush();
                    if (this.targets == null) {
                        // refresh all associated indices.
                        for(ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices)
                            indexInfo.refresh();
                    } else {
                        // refresh matching partition indices.
                        for(int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i+1))
                            indices[i].refresh();
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                }
            }
            
            public abstract void collect(Row inRow, Row outRow);
            
            public abstract void flush(); 
            
            public RowIterator read(SinglePartitionReadCommand command) {
                try(ReadExecutionController control = command.executionController()) {
                    UnfilteredRowIterator unfilteredRows = command.queryMemtableAndDisk(baseCfs, control);
                    return UnfilteredRowIterators.filter(unfilteredRows, nowInSec);
                }
            }
            
            class Rowcument {
                final String id;
                final Object[] values = new Object[fieldsToIdx.size()];
                final BitSet fieldsNotNull = new BitSet(fieldsToIdx.size());     // regular or static columns only
                final BitSet tombstoneColumns = new BitSet(fieldsToIdx.size());  // regular or static columns only
                int   docTtl = Integer.MAX_VALUE;
                int   inRowDataSize = 0;
                boolean hasLiveData = false;
                boolean hasRowMarker = false;
                final boolean isStatic;
                
                /**
                 * 
                 * @param inRow  = inserted data
                 * @param outRow = removed data
                 * @throws IOException
                 */
                public Rowcument(Row inRow, Row outRow) throws IOException {
                    if (inRow != null) {
                        this.inRowDataSize = inRow.dataSize();
                        this.hasRowMarker = inRow.primaryKeyLivenessInfo().isLive(nowInSec);
                        this.hasLiveData = inRow.hasLiveData(nowInSec, baseCfs.metadata.enforceStrictLiveness());
                    } 
                    Row row = inRow != null ? inRow : outRow;
                    this.isStatic = row.isStatic();
                    
                    // copy the indexed columns of partition key in values
                    int x = 0;
                    for(int i=0 ; i < baseCfs.metadata.partitionKeyColumns().size(); i++) {
                        if (indexedPkColumns[i])
                            values[x++] = pkCols[i];
                    }
                    // copy the indexed columns of clustering key in values
                    if (!row.isStatic() && row.clustering().size() > 0) {
                        int i=0;
                        for(ColumnDefinition ccd : baseCfs.metadata.clusteringColumns()) {
                            Object value = ClusterService.deserialize(ccd.type, row.clustering().get(i));
                            pkCols[baseCfs.metadata.partitionKeyColumns().size()+i] = value;
                            if (indexedPkColumns[baseCfs.metadata.partitionKeyColumns().size()+i])
                                values[x++] = value;
                            i++;
                        }
                        id = ClusterService.stringify(pkCols, pkCols.length);
                    } else {
                        id = partitionKey;
                    }
                    
                    // order is important, remove before insert.
                    if (outRow != null)
                        readCellValues(outRow, false);
                     if (inRow != null)
                         readCellValues(inRow, true);
                }
                
                public boolean hasLiveData() {
                    return hasLiveData;
                }
                
                public boolean isStatic() {
                    return isStatic;
                }
                
                public void readCellValues(Row row, boolean indexOp) throws IOException {
                    for(Cell cell : row.cells())
                        readCellValue(cell, indexOp);
                }
                
                public void readCellValue(Cell cell, boolean indexOp) throws IOException {
                    final String cellNameString = cell.column().name.toString();
                    int idx  = fieldsToIdx.getOrDefault(cellNameString, -1);
                    if (idx == - 1)
                        return; //ignore cell, not indexed.

                    if (cell.isLive(nowInSec) && indexOp) {
                        docTtl = Math.min(cell.localDeletionTime(), docTtl);
                        
                        ColumnDefinition cd = cell.column();
                        if (cd.type.isCollection()) {
                            CollectionType ctype = (CollectionType) cd.type;
                            Object value = null;
                  
                            switch (ctype.kind) {
                            case LIST: 
                                value = ClusterService.deserialize(((ListType)cd.type).getElementsType(), cell.value() );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("list name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                List l = (List) values[idx];
                                if (l == null) {
                                    l = new ArrayList<>(1);
                                    values[idx] = l;
                                } 
                                l.add(value);
                                break;
                            case SET:
                                value = ClusterService.deserialize(((SetType)cd.type).getElementsType(), cell.path().get(0) );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("set name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                Set s = (Set) values[idx];
                                if (s == null) {
                                    s = new HashSet<>();
                                    values[idx] = s;
                                } 
                                s.add(value);
                                break;
                            case MAP:
                                value = ClusterService.deserialize(((MapType)cd.type).getValuesType(), cell.value() );
                                CellPath cellPath = cell.path();
                                Object key = ClusterService.deserialize(((MapType)cd.type).getKeysType(), cellPath.get(cellPath.size()-1));
                                if (logger.isTraceEnabled()) 
                                    logger.trace("map name={} kind={} type={} key={} value={}", 
                                            cellNameString, cd.kind, 
                                            cd.type.asCQL3Type().toString(),
                                            key, 
                                            value);
                                if (key instanceof String) {
                                    Map m = (Map) values[idx];
                                    if (m == null) {
                                        m = new HashMap<>();
                                        values[idx] = m;
                                    } 
                                    m.put(key,value);
                                }
                                break;
                            }
                            fieldsNotNull.set(idx, value != null);
                        } else {
                            Object value = ClusterService.deserialize(cd.type, cell.value() );
                            if (logger.isTraceEnabled()) 
                                logger.trace("name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                            
                            values[idx] = value;
                            fieldsNotNull.set(idx, value != null);
                        }
                    } else {
                        // tombstone => black list this column for later document.read().
                        if (values[idx]==null)
                            tombstoneColumns.set(idx);
                    }
                }

                /**
                 * Check for missing fields
                 * @return true if the rowcument needs some fields.
                 */
                public boolean hasMissingFields() {
                    if (hasIndexedMultiCell)
                        return true;
                    
                    // add missing or collection columns that should be read before indexing the document.
                    // read missing static or regular columns
                    final BitSet mustReadFields = (BitSet)fieldsToRead.clone();
                    boolean completeOnlyStatic = isStatic();
                    if (staticColumns != null) {
                        if (isStatic() || ImmutableMappingInfo.this.indexSomeStaticColumnsOnWideRow) {
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
                    return (mustReadFields.cardinality() > 0);
                }
                
                
                public Context buildContext(ImmutableIndexInfo indexInfo, boolean staticColumnsOnly) throws IOException {
                    Context context = ElasticSecondaryIndex.this.perThreadContext.get();
                    Uid uid = new Uid(typeName,  (staticColumnsOnly) ? partitionKey : id);
                    
                    context.reset(indexInfo, uid);
                    
                    // preCreate for all metadata fields.
                    for (MetadataFieldMapper metadataMapper : context.docMapper.mapping().metadataMappers())
                        metadataMapper.preCreate(context);
                    
                    context.docMapper.idFieldMapper().createField(context, uid.id());
                    context.docMapper.uidMapper().createField(context, uid);
                    context.docMapper.typeMapper().createField(context, typeName);
                    context.docMapper.tokenFieldMapper().createField(context, (Long) key.getToken().getTokenValue());
                    
                    if (indexInfo.includeNodeId)
                        context.docMapper.nodeFieldMapper().createField(context, ImmutableMappingInfo.this.nodeId);
                    
                    if (this instanceof WideRowcument)
                        context.docMapper.routingFieldMapper().createField(context, partitionKey);

                    if (!indexInfo.versionLessEngine) {
                        context.doc().add(DEFAULT_INTERNAL_VERSION);
                    }
                    
                    // add all mapped fields to the current context.
                    for(int i=0; i < values.length; i++) {
                        if (indexInfo.mappers[i] != null)
                            try {
                                context.addField(indexInfo, indexInfo.mappers[i], values[i]);
                            } catch (IOException e) {
                                logger.error("error", e);
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
                    if (parentMapper.active() && fieldsToIdx.getOrDefault(ParentFieldMapper.NAME, -1) == -1) {
                        String parent = null;
                        if (parentMapper.pkColumns() != null) {
                            String[] cols = parentMapper.pkColumns().split(",");
                            if (cols.length == 1) {
                                parent = (String) values[fieldsToIdx.get(cols[0])];
                            } else {
                                Object parentValues[] = new Object[cols.length];
                                for(int i = 0; i < cols.length; i++) 
                                    parentValues[i] = values[fieldsToIdx.get(cols[i])];
                                parent = ClusterService.stringify(parentValues, cols.length);
                            }
                        } else {
                            int parentIdx = fieldsToIdx.getOrDefault(ParentFieldMapper.NAME, -1);
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
                
                public void write() {
                    try {
                        if (hasLiveData() || hasRowMarker) {
                            index();
                        } else {
                            delete();
                        }
                    } catch (Exception e) {
                        logger.error("Unexpected error", e);
                    }
                }
                
                private void index() {
                    long startTime = System.nanoTime();
                    long ttl = (long)((this.docTtl < Integer.MAX_VALUE) ? this.docTtl : 0);
                    
                    targets = ImmutableMappingInfo.this.targetIndices(values);
                    if (targets == null) {
                        // index for associated indices
                        for(ImmutableIndexInfo indexInfo : indices)
                            index(indexInfo, startTime, ttl);
                    } else {
                        // delete for matching target indices.
                        for(int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i+1))
                            index(indices[i], startTime, ttl);
                    }
                }

                private void index(ImmutableIndexInfo indexInfo, long startTime, long ttl) {
                    if (indexInfo.index_on_compaction || transactionType == IndexTransaction.Type.UPDATE) {
                        if (isStatic() && !indexInfo.index_static_document)
                            return; // ignore static document.
                        if (!isStatic() && indexInfo.index_static_only)
                            return; // ignore non-static document.
                            
                        try {
                            Context context = buildContext(indexInfo, isStatic());
                            Field uid = context.uid();
                            if (isStatic()) {
                                uid = new Field(UidFieldMapper.NAME, Uid.createUid(typeName, partitionKey), UidFieldMapper.Defaults.FIELD_TYPE);
                                for(Document doc : context.docs()) {
                                    if (doc instanceof Context.StaticDocument)
                                        ((Context.StaticDocument)doc).applyFilter(isStatic());
                                }
                            }
                            context.finalize();
                            final ParsedDocument parsedDoc = new ParsedDocument(
                                    context.version(),
                                    (isStatic()) ? partitionKey : id,
                                    context.type(),
                                    ClusterService.stringify(pkCols, baseCfs.metadata.partitionKeyColumns().size()), // routing
                                    System.currentTimeMillis(), // timstamp
                                    ttl,
                                    ((Long)key.getToken().getTokenValue()).longValue(), 
                                    context.docs(), 
                                    context.source(), // source 
                                    XContentType.JSON,
                                    (Mapping)null); // mappingUpdate
                            
                            parsedDoc.parent(context.parent());
    
                            if (logger.isTraceEnabled()) {
                                logger.trace("index={} id={} type={} routing={}", context.indexInfo.name, parsedDoc.id(), parsedDoc.type(), parsedDoc.routing());
                                for(int k = 0; k< parsedDoc.docs().size(); k++)
                                    logger.trace("doc[{}]={}", k, parsedDoc.docs().get(k));
                            }
                            
                            final IndexShard indexShard = context.indexInfo.shard();
                            if (indexShard != null) {
                                if (!indexInfo.updated)
                                    indexInfo.updated = true;
                                final Engine.Index operation = new Engine.Index(context.docMapper.uidMapper().term(Uid.createUid(context.docMapper.type(), id)), 
                                        parsedDoc, 
                                        1L, 
                                        VersionType.INTERNAL, 
                                        Engine.Operation.Origin.PRIMARY, 
                                        startTime, 
                                        startTime, false) {
                                    @Override
                                    public int estimatedSizeInBytes() {
                                        return (id.length() + context.docMapper.type().length()) * 2 + inRowDataSize + 12;
                                    }
                                };
                                
                                IndexResult result = indexShard.index(indexShard.getEngine(), operation);
                                
                                if (logger.isDebugEnabled()) {
                                    logger.debug("document CF={}.{} index/type={}/{} id={} version={} created={} static={} ttl={} refresh={} ", 
                                        baseCfs.metadata.ksName, baseCfs.metadata.cfName,
                                        context.indexInfo.name, typeName,
                                        parsedDoc.id(), operation.version(), result.isCreated(), isStatic(), ttl, context.indexInfo.refresh);
                                }
                             }
                        } catch (IOException e) {
                            logger.error("error", e);
                        }
                    }
                }
                
                public void delete() {
                    targets = ImmutableMappingInfo.this.targetIndices(values);
                    if (targets == null) {
                        // delete for associated indices
                        for(ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices)
                            delete(indexInfo);
                    } else {
                        // delete for matching target indices.
                        for(int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i+1))
                            delete(indices[i]);
                    }
                }
                
                private void delete(ImmutableIndexInfo indexInfo) {
                    if (isStatic() && !indexInfo.index_static_document)
                        return; // ignore static document.
                    if (!isStatic() && indexInfo.index_static_only)
                        return; // ignore non-static document.
                    
                    final IndexShard indexShard = indexInfo.shard();
                    if (indexShard != null) {
                        if (logger.isDebugEnabled())
                            logger.debug("deleting document from index.type={}.{} id={}", indexInfo.name, typeName, id);
                        if (!indexInfo.updated)
                            indexInfo.updated = true;
                        Engine.Delete delete = indexShard.prepareDeleteOnPrimary(typeName, id, 
                                indexInfo.versionLessEngine ? 1L : Versions.MATCH_ANY, 
                                indexInfo.versionLessEngine ? VersionType.EXTERNAL : VersionType.INTERNAL);
                        try {
                            indexShard.delete(delete);
                        } catch (IOException e) {
                            logger.error("Document deletion error", e);
                        }
                    }
                }
            }

 
            /**
             * Notification of a top level partition delete.
             * @param deletionTime
             */
            @Override
            public void partitionDelete(DeletionTime deletionTime) {
                logger.trace("Delete partition {}: {}", this.transactionType, deletionTime);
                mappingInfoLock.readLock().lock();
                try {
                    for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices) {
                        IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                        if (indexShard != null) {
                            if (!indexInfo.updated)
                                indexInfo.updated = true;
                            try {
                                partitionDelete(indexShard);
                            } catch (EngineException e) {
                                logger.error("Document deletion error", e);
                            }
                        } else {
                            logger.warn("Shard not available to delete document index.type={}.{} partitionKey={}", indexInfo.name, indexInfo.type, this.partitionKey);
                        }
                    }
                } catch(Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }
            
            public abstract void partitionDelete(IndexShard indexShard) throws IOException;

            /**
             * Notification of a RangeTombstone.
             * An update of a single partition may contain multiple RangeTombstones,
             * and a notification will be passed for each of them.
             * @param tombstone
             */
            @Override
            public void rangeTombstone(RangeTombstone tombstone) {
                logger.warn("Ignoring range tombstone {}", tombstone);
            }
        }
    }

    public boolean isIndexing() {
        if (!runsElassandra) 
            return false;
        
        if (mappingInfo == null) {
            if (logger.isWarnEnabled())  
                logger.warn("No Elasticsearch index ready {}.{}",this.baseCfs.metadata.ksName, this.baseCfs.metadata.cfName);
            return false;
        }
        if (mappingInfo.indices == null || mappingInfo.indices.length == 0) {
            if (logger.isWarnEnabled())  
                logger.warn("No Elasticsearch index configured for {}.{}",this.baseCfs.metadata.ksName, this.baseCfs.metadata.cfName);
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return this.index_name;
    }
    
    public void initMapping(ClusterState clusterState) {
        mappingInfoLock.writeLock().lock();
        try {
           // initilization could occur after reading mapping from CQL and cfNameToType map update.
           this.typeName = ClusterService.cfNameToType(baseCfs.keyspace.getName(), ElasticSecondaryIndex.this.baseCfs.metadata.cfName);
           this.typeTermQuery = new TermQuery(new Term(TypeFieldMapper.NAME, typeName));
           this.mappingInfo = new ImmutableMappingInfo(clusterState);
           logger.info("Secondary index=[{}] initialized, metadata.version={} mappingInfo.indices={} typeName={}", 
               index_name, mappingInfo.metadataVersion, mappingInfo.indices==null ? null : Arrays.stream(mappingInfo.indices).map(i -> i.name).collect(Collectors.joining()), this.typeName);
        } catch(Exception e) {
           logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to update mapping index=[{}]", index_name), e);
        } finally {
           mappingInfoLock.writeLock().unlock();
        }
    }
    
    // TODO: notify 2i only for udated indices (not all)
    @Override
    public void clusterChanged(ClusterChangedEvent event) 
    {
        boolean updateMapping = false;
        if ( mappingInfo==null || !event.state().blocks().isSame(event.previousState().blocks(), 
                mappingInfo.indices == null ? Collections.EMPTY_LIST : Arrays.stream(mappingInfo.indices).map(i -> i.name).collect(Collectors.toList()))) {
            updateMapping = true;
        } else {
            for (ObjectCursor<IndexMetaData> cursor : event.state().metaData().indices().values()) {
                IndexMetaData indexMetaData = cursor.value;
                if (indexMetaData.keyspace().equals(this.baseCfs.metadata.ksName) && 
                    indexMetaData.mapping(this.typeName) != null &&
                   !indexMetaData.equals(event.previousState().metaData().index(indexMetaData.getIndex().getName()))) {
                    updateMapping = true;
                    break;
                }
            }
        }
        if (updateMapping) {
            mappingInfoLock.writeLock().lock();
            try {
                mappingInfo = new ImmutableMappingInfo(event.state());
                logger.debug("secondary index=[{}] metadata.version={} mappingInfo.indices={}",
                        this.index_name, event.state().metaData().version(),
                        mappingInfo.indices == null ? "" : Arrays.stream(mappingInfo.indices).map(i -> i.name).collect(Collectors.joining()));
            } catch(Exception e) {
                logger.error("Failed to update mapping index=["+index_name+"]", e);
            } finally {
                mappingInfoLock.writeLock().unlock();
            }
        }
    }

    /**
     * Return a task to be executed before the node enters NORMAL state and finally joins the ring.
     *
     * @param hadBootstrap If the node had bootstrap before joining.
     * @return task to be executed by the index manager before joining the ring.
     */
    @Override
    public Callable<?> getPreJoinTask(boolean hadBootstrap)
    {
	 return null;
    }
    
    /**
     * Return a task to perform any initialization work when a new index instance is created.
     * This may involve costly operations such as (re)building the index, and is performed asynchronously
     * by SecondaryIndexManager
     * @return a task to perform any necessary initialization work
     */
    @Override
    public Callable<?> getInitializationTask() 
    {
	if (ElassandraDaemon.instance !=null && ElassandraDaemon.instance.node() != null) {
            initialize(ElassandraDaemon.instance.node().injector().getInstance(ClusterService.class));

            if (!baseCfs.isEmpty() && !isBuilt()) {
                logger.info("index building task for [{}.{}]", baseCfs.keyspace.getName(), baseCfs.name);
                return () -> {
                    baseCfs.forceBlockingFlush();
                    baseCfs.indexManager.buildIndexBlocking(this);
                    return null;
                };
            }
        }
        return null;
    }
    
    public void initialize(ClusterService cs) {
        // 2i index can be recycled by cassandra, while ES node restarted during tests, so update clusterService reference.
        clusterService = cs;
        clusterService.addListener(this);

	try {
            ClusterState state = clusterService.state();
            logger.info("Initializing elastic secondary mapping index=[{}] hashCode={} metadata.version={}/{} indices={}",
                    index_name, hashCode(), state.metaData().clusterUUID(), state.metaData().version(), state.metaData().indices());
            initMapping(state);
        } catch (Throwable e) {
            // minor error thrown when bootstrapping because state is not yet available.
            logger.trace("Mapping initialization failed", e);
        }
    }
    
    public boolean initilized() {
        return  this.mappingInfo != null && 
                this.clusterService != null && 
                this.mappingInfo.metadataVersion == this.clusterService.state().metaData().version() &&
                this.mappingInfo.metadataClusterUUID == this.clusterService.state().metaData().clusterUUID();
    }
 
    private boolean isBuilt()
    {
        return SystemKeyspace.isIndexBuilt(baseCfs.keyspace.getName(), this.indexMetadata.name);
    }

    @Override
    public Callable<?> getMetadataReloadTask(IndexMetadata indexMetadata) 
    {
        return null;
    }

    /**
     * Cassandra index flush .. Elasticsearch flush ... lucene commit and disk sync.
     */
    @Override
    public Callable<?> getBlockingFlushTask() 
    {
        return () -> {
             if (isIndexing()) {
                for(ImmutableMappingInfo.ImmutableIndexInfo indexInfo : mappingInfo.indices) {
                    try {
                        IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                        if (indexShard != null && indexInfo.updated) {
                            if (indexShard.state() == IndexShardState.STARTED)  {
                                long start = System.currentTimeMillis();
                                indexInfo.updated = false; // reset updated state
                                indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                                if (logger.isInfoEnabled())
                                    logger.info("Elasticsearch index=[{}] type=[{}] flushed, duration={}ms",indexInfo.name, indexInfo.type, System.currentTimeMillis() - start);
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
     * Cassandra table snapshot, hard links associated elasticsearch lucene files.
     */
    @SuppressForbidden(reason="File used for snapshots")
    @Override
    public Callable<?> getSnapshotWithoutFlushTask(String snapshotName) 
    {
        return () -> {
             if (isIndexing()) {
                for(ImmutableMappingInfo.ImmutableIndexInfo indexInfo : mappingInfo.indices) {
                    IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                    if (indexShard != null && indexInfo.snapshot) {
                        if (indexShard.state() == IndexShardState.STARTED)  {
                            // snapshotPath = data/elasticsearch.data/nodes/0/snapshots
                            Path snapshotPath = indexShard.shardPath().resolveSnapshot();
                            if ((Files.notExists(snapshotPath))) 
                                Files.createDirectory(snapshotPath, snapshotDirPermissions);
                            
                            // snapshotIndex = data/elasticsearch.data/nodes/0/snapshots/<index_uuid>
                            Path snapshotIndex = snapshotPath.resolve(indexShard.shardId().getIndex().getUUID());
                            if ((Files.notExists(snapshotIndex))) 
                                Files.createDirectory(snapshotIndex, snapshotDirPermissions);
                            
                            // snapshotDir = data/elasticsearch.data/nodes/0/snapshots/<index_uuid>/<snapshot_name>
                            Path snapshotDir = Files.createDirectory(snapshotIndex.resolve(snapshotName), snapshotDirPermissions);
                            Path indexPath = indexShard.shardPath().resolveIndex();
                            
                            try (DirectoryStream<Path> stream = Files.newDirectoryStream(indexPath, "{_*.*,segments*}")) {
                                for (Path luceneFile: stream) {
                                    File targetLink = new File(snapshotDir.toFile(), luceneFile.getFileName().toString());
                                    FileUtils.createHardLink(luceneFile.toFile(), targetLink);
                                }
                                if (logger.isDebugEnabled())
                                    logger.debug("Elasticsearch index=[{}/{}], snapshot=[{}], path=[{}]",indexInfo.name, indexInfo.indexService.indexUUID(), snapshotName, snapshotDir.toString());
                            } catch (DirectoryIteratorException ex) {
                                logger.error("Failed to retreive lucene files in {}", ex, indexPath);
                            }
                        } else {
                            if (logger.isDebugEnabled())
                                logger.debug("Cannot snapshot index=[{}/{}], state=[{}], snapshot=[{}]",indexInfo.name, indexInfo.indexService.indexUUID(), indexShard.state(), snapshotName);
                        }
                    }
                }
            }
            return null;
        };
    }

    @Override
    public Callable<?> getInvalidateTask() {
        return () -> {
            if (this.clusterService != null)
                this.clusterService.removeListener(this);
            elasticSecondayIndices.remove(index_name);
            return null;
        };
    }

    @Override
    public Callable<?> getTruncateTask(long truncatedAt) {
        return () -> {
            if (isIndexing()) {
                for(ImmutableMappingInfo.ImmutableIndexInfo indexInfo : mappingInfo.indices) {
                    try {
                        IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                        if (indexShard != null) {
                            DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(typeName);
                            if (logger.isDebugEnabled()) {
                                logger.debug("truncating from ks.cf={}.{} query={} in elasticsearch index=[{}]", baseCfs.metadata.ksName, baseCfs.name, typeTermQuery, indexInfo.name);
                            }
                            if (!indexInfo.updated)
                                indexInfo.updated = true;
                            DeleteByQuery deleteByQuery = new DeleteByQuery(typeTermQuery, null, null, null, null, Operation.Origin.PRIMARY, System.currentTimeMillis(), typeName);
                            indexShard.getEngine().delete(deleteByQuery);
                        }
                    } catch (ElasticsearchException e) {
                        logger.error("Error while truncating index=[{}]", e, indexInfo.name);
                    }
                }
            }
            return null;
        };
    }

    @Override
    public boolean shouldBuildBlocking() {
        return isIndexing();
    }


    public long getEstimatedResultRows() {
        return 0;
    }


    public void validate(PartitionUpdate update) throws InvalidRequestException {
    }

    public boolean dependsOn(ColumnDefinition column) {
        return ES_QUERY_BYTE_BUFFER.equals(column.name.bytes) || 
               ES_OPTIONS_BYTE_BUFFER.equals(column.name.bytes) || 
               (this.mappingInfo != null && this.mappingInfo.fieldsToIdx.containsKey(column.name.toString()));
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
        return operator.equals(Operator.EQ) && (ES_QUERY_BYTE_BUFFER.equals(column.name.bytes) || ES_OPTIONS_BYTE_BUFFER.equals(column.name.bytes));
    }

    @Override
    public AbstractType<?> customExpressionValueType() {
        return UTF8Type.instance;
    }

    @Override
    public RowFilter getPostIndexQueryFilter(RowFilter filter) {
        return null;
    }

    @Override
    public BiFunction<PartitionIterator, ReadCommand, PartitionIterator> postProcessorFor(ReadCommand command) {
        return (partitionIterator, readCommand) -> {
            return partitionIterator;
        };
    }

    @Override
    public Searcher searcherFor(ReadCommand command) {
        return (group) -> {
            throw new IllegalStateException("CQL query not supported.");
        };
    }
    
    public Indexer indexerFor(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup, Type transactionType) {
        if (isIndexing()) {
            if (transactionType == Type.COMPACTION && !this.mappingInfo.indexOnCompaction)
                return null;

            boolean found = (columns.size() == 0);
            if (!found) {
                for(ColumnDefinition cd : columns) {
                    if (this.mappingInfo.fieldsToIdx.containsKey(cd.name.toString())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                try {
                    if (baseCfs.getComparator().size() == 0)
                        return this.mappingInfo.new SkinnyRowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
                    else 
                        return this.mappingInfo.new WideRowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

}
