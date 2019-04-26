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
import org.apache.cassandra.db.Slices;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.compaction.CompactionManager;
import org.apache.cassandra.db.filter.ClusteringIndexNamesFilter;
import org.apache.cassandra.db.filter.ClusteringIndexSliceFilter;
import org.apache.cassandra.db.filter.ColumnFilter;
import org.apache.cassandra.db.filter.DataLimits;
import org.apache.cassandra.db.filter.RowFilter;
import org.apache.cassandra.db.lifecycle.SSTableSet;
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
import org.apache.cassandra.db.view.View;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.index.IndexRegistry;
import org.apache.cassandra.index.SecondaryIndexBuilder;
import org.apache.cassandra.index.internal.CollatedViewIndexBuilder;
import org.apache.cassandra.index.transactions.IndexTransaction;
import org.apache.cassandra.index.transactions.IndexTransaction.Type;
import org.apache.cassandra.io.sstable.ReducingKeyIterator;
import org.apache.cassandra.io.sstable.format.SSTableReader;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.serializers.SimpleDateSerializer;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.cassandra.utils.concurrent.OpOrder;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.apache.cassandra.utils.concurrent.Refs;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexOrDocValuesQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CloseableThreadLocal;
import org.elassandra.cluster.SchemaManager;
import org.elassandra.cluster.Serializer;
import org.elassandra.index.ElasticSecondaryIndex.ImmutableMappingInfo.WideRowcumentIndexer.WideRowcument;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateTaskConfig.SchemaUpdate;
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
import org.elasticsearch.common.geo.parsers.ShapeParser;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.lucene.all.AllEntries;
import org.elasticsearch.common.lucene.search.Queries;
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
import org.elasticsearch.index.mapper.BooleanFieldMapper;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.DocumentParser;
import org.elasticsearch.index.mapper.DynamicTemplate;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.GeoPointFieldMapper;
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
import org.elasticsearch.index.mapper.SeqNoFieldMapper;
import org.elasticsearch.index.mapper.SeqNoFieldMapper.SequenceIDFields;
import org.elasticsearch.index.mapper.SourceFieldMapper;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.TypeParsers;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.UidFieldMapper;
import org.elasticsearch.index.mapper.VersionFieldMapper;
import org.elasticsearch.index.seqno.SequenceNumbers;
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
import java.util.LinkedList;
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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
 */
public class ElasticSecondaryIndex implements Index {

    public final static String ES_QUERY = "es_query";
    public final static ByteBuffer ES_QUERY_BYTE_BUFFER = ByteBufferUtil.bytes(ES_QUERY);
    public final static String ES_OPTIONS = "es_options";
    public final static ByteBuffer ES_OPTIONS_BYTE_BUFFER = ByteBufferUtil.bytes(ES_OPTIONS);

    private final static Field DEFAULT_INTERNAL_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, -1L);
    private final static Field DEFAULT_EXTERNAL_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, 1L);

    public static final Map<String, ElasticSecondaryIndex> elasticSecondayIndices = Maps.newConcurrentMap();
    public static final Pattern TARGET_REGEX = Pattern.compile("^(keys|entries|values|full)\\((.+)\\)$");
    private static final ClusteringIndexSliceFilter SKINNY_FILTER = new ClusteringIndexSliceFilter(Slices.ALL, false);

    public static boolean runsElassandra = false;

    final String index_name;
    final Logger logger;

    // updated when create/open/close/remove an ES index.
    protected final ReadWriteLock mappingInfoLock = new ReentrantReadWriteLock();
    protected final AtomicReference<ImmutableMappingInfo> mappingInfoRef;
    protected final ClusterService clusterService;

    protected final ColumnFamilyStore baseCfs;
    protected final IndexMetadata indexMetadata;
    protected String typeName;
    protected Object[] readBeforeWriteLocks;
    protected AtomicBoolean needBuild;

    ElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        this.baseCfs = baseCfs;
        this.indexMetadata = indexDef;
        this.index_name = baseCfs.keyspace.getName() + "." + baseCfs.name;
        this.typeName = SchemaManager.cfNameToType(baseCfs.keyspace.getName(), ElasticSecondaryIndex.this.baseCfs.metadata.cfName);
        this.logger = Loggers.getLogger(this.getClass().getName() + "." + baseCfs.keyspace.getName() + "." + baseCfs.name);

        this.clusterService = ElassandraDaemon.instance.node().injector().getInstance(ClusterService.class);
        ClusterState state = null;
        try {
            state = (clusterService == null) ? null : clusterService.state();
        } catch(java.lang.AssertionError e) {
        }
        this.mappingInfoRef = new AtomicReference<>( state != null ? new ImmutableMappingInfo(state) : null);
        this.needBuild = new AtomicBoolean(!isBuilt());
    }

    public static ElasticSecondaryIndex newElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        ElasticSecondaryIndex esi = elasticSecondayIndices.computeIfAbsent(baseCfs.keyspace.getName() + "." + baseCfs.name, K -> new ElasticSecondaryIndex(baseCfs, indexDef));
        return esi;
    }

    // Public because it's also used to convert index metadata into a thrift-compatible format
    public static Pair<ColumnDefinition, IndexTarget.Type> parseTarget(CFMetaData cfm, IndexMetadata indexDef) {
        String target = indexDef.options.get("target");
        assert target != null : String.format(Locale.ROOT, "No target definition found for index %s", indexDef.name);

        // if the regex matches then the target is in the form "keys(foo)", "entries(bar)" etc
        // if not, then it must be a simple column name and implictly its type is VALUES
        Matcher matcher = TARGET_REGEX.matcher(target);
        String columnName;
        IndexTarget.Type targetType;
        if (matcher.matches()) {
            targetType = IndexTarget.Type.fromString(matcher.group(1));
            columnName = matcher.group(2);
        } else {
            columnName = target;
            targetType = IndexTarget.Type.VALUES;
        }

        // in the case of a quoted column name the name in the target string
        // will be enclosed in quotes, which we need to unwrap. It may also
        // include quote characters internally, escaped like so:
        //      abc"def -> abc""def.
        // Because the target string is stored in a CQL compatible form, we
        // need to un-escape any such quotes to get the actual column name
        if (columnName.startsWith("\"")) {
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

        throw new RuntimeException(String.format(Locale.ROOT, "Unable to parse targets for index %s (%s)", indexDef.name, target));
    }

    public boolean isInsertOnly() {
        ImmutableMappingInfo imf =  mappingInfoRef.get();
        return imf != null && imf.indexInsertOnly;
    }

    // reusable per thread context
    private CloseableThreadLocal<IndexingContext> perThreadContext = new CloseableThreadLocal<IndexingContext>() {
        @Override
        protected IndexingContext initialValue() {
            return new IndexingContext();
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

    public void addField(ParseContext ctx, ImmutableMappingInfo.ImmutableIndexInfo indexInfo, Mapper mapper, Object value) throws IOException {
        ParseContext context = ctx;
        if (logger.isTraceEnabled())
            logger.trace("doc[{}] class={} name={} value={}", context.docs().indexOf(context.doc()), mapper.getClass().getSimpleName(), mapper.name(), value);

        if (value == null && (!(mapper instanceof FieldMapper) || ((FieldMapper) mapper).fieldType().nullValue() == null))
            return;

        if (value instanceof Collection) {
            // flatten list or set of fields
            for (Object v : (Collection) value)
                ElasticSecondaryIndex.this.addField(context, indexInfo, mapper, v);
            return;
        }

        if (mapper instanceof GeoShapeFieldMapper) {
            GeoShapeFieldMapper geoShapeMapper = (GeoShapeFieldMapper) mapper;
            XContentParser parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, (String) value);
            parser.nextToken();
            ShapeBuilder shapeBuilder = ShapeParser.parse(parser);
            context.path().add(mapper.name());
            geoShapeMapper.parse(context.createExternalValueContext(shapeBuilder.build()));
            context.path().remove();
        } else if (mapper instanceof GeoPointFieldMapper) {
            GeoPointFieldMapper geoPointFieldMapper = (GeoPointFieldMapper) mapper;
            GeoPoint geoPoint;
            if (value instanceof String) {
                // geo_point stored as geohash text
                geoPoint = new GeoPoint((String) value);
                geoPointFieldMapper.parse(context, geoPoint);
            } else {
                // geo_point stored in UDT.
                Map<String, Double> geo_point = (Map<String, Double>) value;
                geoPoint = new GeoPoint(geo_point.get(org.elasticsearch.common.geo.GeoUtils.LATITUDE), geo_point.get(org.elasticsearch.common.geo.GeoUtils.LONGITUDE));
                geoPointFieldMapper.parse(context, geoPoint);
            }
        } else if (mapper instanceof FieldMapper) {
            ((FieldMapper) mapper).createField(context, value);
            DocumentParser.createCopyFields(context, ((FieldMapper)mapper).copyTo().copyToFields(), value);
        } else if (mapper instanceof ObjectMapper) {
            final ObjectMapper objectMapper = (ObjectMapper) mapper;
            final ObjectMapper.Nested nested = objectMapper.nested();
            // see https://www.elastic.co/guide/en/elasticsearch/guide/current/nested-objects.html
            // code from DocumentParser.parseObject()
            if (nested.isNested()) {
                context = DocumentParser.nestedContext(context, objectMapper);
            }

            //ContentPath.Type origPathType = path().pathType();
            //path().pathType(objectMapper.pathType());

            if (value instanceof Map<?, ?>) {
                for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
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
                        if (subMapper == null && cd != null && cd.type.isCollection() && cd.type instanceof MapType ) {
                            CollectionType ctype = (CollectionType) cd.type;
                            if (ctype.kind == CollectionType.Kind.MAP &&
                                ((MapType) ctype).getKeysType().asCQL3Type().toString().equals("text") &&
                                (DocumentParser.dynamicOrDefault(objectMapper, ctx) == ObjectMapper.Dynamic.TRUE)) {
                                logger.debug("Updating mapping for field={} type={} value={} ", entry.getKey(), cd.type.toString(), value);
                                // upgrade to write lock
                                indexInfo.dynamicMappingUpdateLock.readLock().unlock();
                                indexInfo.dynamicMappingUpdateLock.writeLock().lock();
                                try {
                                    // Recheck objectMapper because another thread might have acquired write lock and changed state before we did.
                                    if ((subMapper = objectMapper.getMapper(entry.getKey())) == null) {
                                        final String esType = ((MapType) ctype).getValuesType().asCQL3Type().toString();
                                        final Map<String, Object> esMapping = new HashMap<>();
                                        indexInfo.indexService.mapperService().buildNativeOrUdtMapping(esMapping, ((MapType) ctype).getValuesType());

                                        final DynamicTemplate dynamicTemplate = context.docMapper().root().findTemplate(context.path(), objectMapper.name() + "." + entry.getKey(), null);

                                        // build a mapping update
                                        Map<String, Object> objectMapping = (Map<String, Object>) ((Map<String, Object>) indexInfo.mapping.get("properties")).get(mapper.name());
                                        XContentBuilder builder = XContentFactory.jsonBuilder()
                                            .startObject()
                                            .startObject(context.docMapper().type())
                                            .startObject("properties")
                                            .startObject(mapper.name());
                                        boolean hasProperties = false;
                                        for (String key : objectMapping.keySet()) {
                                            if (key.equals("properties")) {
                                                Map<String, Object> props = (Map<String, Object>) objectMapping.get(key);
                                                builder.startObject("properties");
                                                for (String key2 : props.keySet()) {
                                                    builder.field(key2, props.get(key2));
                                                }
                                                builder.field(entry.getKey(), (dynamicTemplate != null) ? dynamicTemplate.mappingForName(entry.getKey(), esType) : esMapping);
                                                builder.endObject();
                                                hasProperties = true;
                                            } else {
                                                builder.field(key, objectMapping.get(key));
                                            }
                                        }
                                        if (!hasProperties) {
                                            builder.startObject("properties");
                                            builder.field(entry.getKey(), (dynamicTemplate != null) ? dynamicTemplate.mappingForName(entry.getKey(), esType) : esMapping);
                                            builder.endObject();
                                        }
                                        builder.endObject().endObject().endObject().endObject();
                                        String mappingUpdate = builder.string();
                                        logger.info("updating mapping={}", mappingUpdate);

                                        clusterService.blockingMappingUpdate(indexInfo.indexService, context.docMapper().type(), mappingUpdate, SchemaUpdate.UPDATE_ASYNCHRONOUS);
                                        DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(indexInfo.type);
                                        ObjectMapper newObjectMapper = docMapper.objectMappers().get(mapper.name());
                                        subMapper = newObjectMapper.getMapper(entry.getKey());
                                        //assert subMapper != null : "dynamic subMapper not found for nested field ["+entry.getKey()+"]";
                                    }
                                } catch (Exception e) {
                                    logger.error("error while updating mapping", e);
                                } finally {
                                    // Downgrade by acquiring read lock before releasing write lock
                                    indexInfo.dynamicMappingUpdateLock.readLock().lock();
                                    indexInfo.dynamicMappingUpdateLock.writeLock().unlock();
                                }
                            }
                        } else {
                            logger.error("metadata.version={} index={} unknown subfield={} of field={} column type={}, ignoring value={}",  indexInfo.getImmutableMappingInfo().metadataVersion, indexInfo.name,
                                entry.getKey(), mapper != null ? mapper.name() : null, cd != null && cd.type != null ? cd.type.asCQL3Type().toString() : null, entry.getValue());
                        }
                    }

                    try {
                        if (subMapper != null) {
                            ElasticSecondaryIndex.this.addField(context, indexInfo, subMapper, entry.getValue());
                        } else {
                            logger.error("submapper not found for nested field [{}] in index [{}]", entry.getKey(), indexInfo.name);
                        }
                    } finally {
                        if (mapper.cqlStruct().equals(Mapper.CqlStruct.MAP))
                            indexInfo.dynamicMappingUpdateLock.readLock().unlock();
                    }
                }
            } else {
                if (context.docMapper().type().equals("percolator")) {
                    // store percolator query as source.
                    String sourceQuery = "{\"query\":" + value + "}";
                    if (logger.isDebugEnabled())
                        logger.debug("Store percolate query={}", sourceQuery);

                    BytesReference source = new BytesArray(sourceQuery);
                    context.source(source);
                    BytesRef ref = source.toBytesRef();
                    context.doc().add(new StoredField(SourceFieldMapper.NAME, ref.bytes, ref.offset, ref.length));
                }
            }

            // restore the enable path flag
            if (nested.isNested()) {
                DocumentParser.nested(context, nested);
            }
        }
    }

    public class IndexingContext extends ParseContext {
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

        public IndexingContext() {
        }

        public IndexingContext(ImmutableMappingInfo.ImmutableIndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(uid.type());
            assert this.docMapper != null;
            this.document = ii.indexStaticOnly() ? new StaticDocument("", null, uid) : new Document();
            this.documents.add(this.document);
        }

        public void reset(ImmutableMappingInfo.ImmutableIndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(uid.type());
            assert this.docMapper != null;
            this.document = ii.indexStaticOnly() ? new StaticDocument("", null, uid) : new Document();
            this.documents.clear();
            this.documents.add(this.document);
            this.id = uid.id();
            this.uid = new Field(UidFieldMapper.NAME, uid.toBytesRef(), UidFieldMapper.Defaults.FIELD_TYPE);
            this.allEntries = (this.docMapper.allFieldMapper().enabled()) ? new AllEntries() : null;
            this.docBoost = 1.0f;
            this.dynamicMappers = null;
            this.parent = null;
            this.externalValue = null;
        }

        @Override
        public ParseContext createNestedContext(String fullPath) {
            final Document doc = (baseCfs.metadata.hasStaticColumns()) ? new StaticDocument(fullPath, doc(), new Uid(docMapper.type(), id)) : new Document(fullPath, doc());
            addDoc(doc);
            return switchDoc(doc);
        }

        public void endNestedDocument() {
            this.document = doc().getParent();
        }

        @Override
        public boolean externalValueSet() {
            return (externalValue != null);
        }

        @Override
        public Object externalValue() {
            if (externalValue == null)
                throw new IllegalStateException("External value is not set");
            return externalValue;
        }

        @Override
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

        @Override
        public SourceToParse sourceToParse() {
            return null;
        }


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
            return this.documents;
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


        @Override
        public SequenceIDFields seqID() {
            return SeqNoFieldMapper.SequenceIDFields.emptySeqID();
        }

        @Override
        public void seqID(SequenceIDFields seqID) {
        }

        class StaticDocument extends FilterableDocument {
            Uid uid;

            public StaticDocument(String path, Document parent, Uid uid) {
                super(path, parent);
                this.uid = uid;
            }

            @Override
            public boolean apply(IndexableField input) {
                if (MapperService.isMetadataField(input.name())) {
                    return true;
                }
                int x = input.name().indexOf('.');
                String colName = (x > 0) ? input.name().substring(0, x) : input.name();
                int idx = indexInfo.indexOf(colName);
                return idx < baseCfs.metadata.partitionKeyColumns().size() || indexInfo.isStaticField(idx);
            }
        }

    }

    static Pattern synchronousRefreshPattern = Pattern.compile(System.getProperty(ClusterService.SETTING_SYSTEM_SYNCHRONOUS_REFRESH, "(\\.kibana.*)"));

    final class ImmutableMappingInfo {

        class ImmutableIndexInfo {
            final String name;
            final String type;
            final boolean shardStarted;
            final boolean refresh;
            final boolean snapshot;
            final boolean includeNodeId;
            final IndexService indexService;
            final Map<String, Object> mapping;
            final boolean index_static_columns;
            final boolean index_static_only;
            final boolean index_on_compaction;
            final boolean index_static_document;
            final boolean versionLessEngine;
            final boolean insert_only;
            final boolean opaque_storage;
            final long version;

            Mapper[] mappers;   // inititalized in the ImmutableMappingInfo constructor.
            ReadWriteLock dynamicMappingUpdateLock;
            volatile boolean updated = false;

            public ImmutableIndexInfo(String name, IndexService indexService, MappingMetaData mappingMetaData, MetaData metadata, boolean versionLessEngine) throws IOException {
                this.name = name;
                this.versionLessEngine = versionLessEngine;
                this.indexService = indexService;
                this.mapping = mappingMetaData.sourceAsMap();
                this.type = mappingMetaData.type();
                this.shardStarted = (shard() != null);

                Map<String, Object> mappingMap = mappingMetaData.getSourceAsMap();
                Map<String, Object> metaMap = (mappingMap == null) ? null : (Map<String, Object>) mappingMap.get("_meta");

                this.version = indexService.getMetaData().getVersion();
                this.refresh = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_SYNCHRONOUS_REFRESH_SETTING) || synchronousRefreshPattern.matcher(name).matches();
                logger.debug("index.type=[{}.{}] {}=[{}]", name, this.type, IndexMetaData.INDEX_SYNCHRONOUS_REFRESH_SETTING.getKey(), refresh);

                this.snapshot = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_SNAPSHOT_WITH_SSTABLE_SETTING);
                this.includeNodeId = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INCLUDE_NODE_ID_SETTING);

                this.index_on_compaction = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INDEX_ON_COMPACTION_SETTING);
                this.index_static_columns = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INDEX_STATIC_COLUMNS_SETTING);
                this.index_static_only = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INDEX_STATIC_ONLY_SETTING);
                this.index_static_document = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INDEX_STATIC_DOCUMENT_SETTING);
                this.insert_only = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INDEX_INSERT_ONLY_SETTING);
                this.opaque_storage = getMetaSettings(metadata.settings(), metaMap, IndexMetaData.INDEX_INDEX_OPAQUE_STORAGE_SETTING);

                // lazy lock array initialization if needed
                if (!this.insert_only && readBeforeWriteLocks == null) {
                    synchronized (ElasticSecondaryIndex.this) {
                        if (readBeforeWriteLocks == null) {
                            readBeforeWriteLocks = new Object[DatabaseDescriptor.getConcurrentWriters() * 128];
                            for (int i = 0; i < readBeforeWriteLocks.length; i++)
                                readBeforeWriteLocks[i] = new Object();
                        }
                    }
                }
            }

            public ImmutableMappingInfo getImmutableMappingInfo() {
                return ImmutableMappingInfo.this;
            }

            // get _meta, index, cluster or system settings.
            public boolean getMetaSettings(Settings metadataSettings, Map<String, Object> metaMap, Setting indexSetting) {
                boolean value = false;
                IndexSettings indexSettings = indexService.getIndexSettings();
                String key = indexSetting.getKey().substring(IndexMetaData.INDEX_SETTING_PREFIX.length());
                if (metaMap != null && metaMap.get(key) != null) {
                    value = XContentMapValues.nodeBooleanValue(metaMap.get(key));
                    logger.debug("index.type=[{}.{}] {}=[{}] in _meta settings", name, this.type, key, value);
                } else if (indexSettings.getValue(indexSetting) != null) {
                    value = (Boolean) indexSettings.getValue(indexSetting);
                    logger.debug("index.type=[{}.{}] {}=[{}] in index setting", name, this.type, key, value);
                }
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

                    int partitionKeyLen = baseCfs.metadata.partitionKeyColumns().size();

                    // build the primary key part of the delete by query
                    int i = 0;
                    for (ColumnDefinition cd : baseCfs.metadata.primaryKeyColumns()) {
                        if (i >= (partitionKeyLen + Math.max(start.size(), end.size())))
                            break;

                        if (indexedPkColumns[i]) {
                            FieldMapper mapper = docMapper.mappers().smartNameFieldMapper(cd.name.toString());
                            Query q;
                            if (i < partitionKeyLen) {
                                q = buildQuery(cd, mapper, pkCols[i], pkCols[i], true, true);
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
                                q = buildQuery(cd, mapper, startByteBuffer, endByteBuffer, startIsInclusive, endIsInclusive);
                            }
                            builder.add(q, Occur.FILTER);
                        }
                        i++;
                    }

                    Query query = builder.build();
                    if (logger.isDebugEnabled()) {
                        logger.debug("delete rangeTombstone={} from ks.cf={}.{} query={} in elasticsearch index=[{}]",
                            tombstone.deletedSlice().toString(baseCfs.metadata), baseCfs.metadata.ksName, baseCfs.name, query, name);
                    }
                    if (!updated)
                        updated = true;
                    DeleteByQuery deleteByQuery = buildDeleteByQuery(shard.indexService(), query);
                    shard.getEngine().delete(deleteByQuery);
                }
            }

            /**
             * Build range query to remove a row slice.
             *
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

            @SuppressForbidden(reason = "unchecked")
            private Query buildQuery(ColumnDefinition cd, FieldMapper mapper, Object start, Object end, boolean includeLower, boolean includeUpper) {
                Query query = null;
                if (mapper != null) {
                    CQL3Type cql3Type = cd.type.asCQL3Type();
                    if (cql3Type instanceof CQL3Type.Native) {
                        switch ((CQL3Type.Native) cql3Type) {
                            case ASCII:
                            case TEXT:
                            case VARCHAR:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    new TermQuery(new Term(mapper.name(), BytesRefs.toBytesRef(start))) :
                                    new TermRangeQuery(mapper.name(), BytesRefs.toBytesRef(start), BytesRefs.toBytesRef(end), includeLower, includeUpper);
                                break;
                            case INT:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    NumberFieldMapper.NumberType.INTEGER.termQuery(mapper.name(), start) :
                                    NumberFieldMapper.NumberType.INTEGER.rangeQuery(mapper.name(), start, end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                break;
                            case SMALLINT:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    NumberFieldMapper.NumberType.SHORT.termQuery(mapper.name(), start) :
                                    NumberFieldMapper.NumberType.SHORT.rangeQuery(mapper.name(), start, end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                break;
                            case TINYINT:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    NumberFieldMapper.NumberType.BYTE.termQuery(mapper.name(), start) :
                                    NumberFieldMapper.NumberType.BYTE.rangeQuery(mapper.name(), start, end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                break;
                            case INET:
                                IpFieldMapper ipMapper = (IpFieldMapper) mapper;
                                query = start != null && end != null && ((InetAddress) start).equals(end) ?
                                    ipMapper.fieldType().termQuery(start, null) :
                                    ipMapper.fieldType().rangeQuery(start, end, includeLower, includeUpper, null);
                                break;
                            case DATE: {
                                DateFieldMapper dateMapper = (DateFieldMapper) mapper;
                                int l, u;
                                if (start == null) {
                                    l = Integer.MIN_VALUE;
                                } else {
                                    l = (Integer) start;
                                    if (includeLower == false) {
                                        ++l;
                                    }
                                }
                                if (end == null) {
                                    u = Integer.MAX_VALUE;
                                } else {
                                    u = (Integer) end;
                                    if (includeUpper == false) {
                                        --u;
                                    }
                                }
                                query = LongPoint.newRangeQuery(dateMapper.name(), SimpleDateSerializer.dayToTimeInMillis(l), SimpleDateSerializer.dayToTimeInMillis(u));
                                if (dateMapper.fieldType().hasDocValues()) {
                                    Query dvQuery = SortedNumericDocValuesField.newSlowRangeQuery(dateMapper.name(), l, u);
                                    query = new IndexOrDocValuesQuery(query, dvQuery);
                                }
                            }
                            break;
                            case TIMESTAMP: {
                                DateFieldMapper dateMapper = (DateFieldMapper) mapper;
                                long l, u;
                                if (start == null) {
                                    l = Long.MIN_VALUE;
                                } else {
                                    l = ((Date) start).getTime();
                                    if (includeLower == false) {
                                        ++l;
                                    }
                                }
                                if (end == null) {
                                    u = Long.MAX_VALUE;
                                } else {
                                    u = ((Date) end).getTime();
                                    if (includeUpper == false) {
                                        --u;
                                    }
                                }
                                query = LongPoint.newRangeQuery(dateMapper.name(), l, u);
                                if (dateMapper.fieldType().hasDocValues()) {
                                    Query dvQuery = SortedNumericDocValuesField.newSlowRangeQuery(dateMapper.name(), l, u);
                                    query = new IndexOrDocValuesQuery(query, dvQuery);
                                }
                            }
                            break;
                            case BIGINT:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    NumberFieldMapper.NumberType.LONG.termQuery(mapper.name(), start) :
                                    NumberFieldMapper.NumberType.LONG.rangeQuery(mapper.name(), start, end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                break;
                            case DOUBLE:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    NumberFieldMapper.NumberType.DOUBLE.termQuery(mapper.name(), start) :
                                    NumberFieldMapper.NumberType.DOUBLE.rangeQuery(mapper.name(), start, end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                break;
                            case FLOAT:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    NumberFieldMapper.NumberType.FLOAT.termQuery(mapper.name(), start) :
                                    NumberFieldMapper.NumberType.FLOAT.rangeQuery(mapper.name(), start, end, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                break;
                            case TIMEUUID:
                                if (start != null && end != null && ((Comparable) start).compareTo(end) == 0) {
                                    query = (mapper instanceof DateFieldMapper) ?
                                        NumberFieldMapper.NumberType.LONG.termQuery(mapper.name(), UUIDGen.unixTimestamp((UUID) start)) :
                                        new TermQuery(new Term(mapper.name(), BytesRefs.toBytesRef(start)));
                                } else {
                                    if (mapper instanceof DateFieldMapper) {
                                        long l = (start == null) ? Long.MIN_VALUE : UUIDGen.unixTimestamp((UUID) start);
                                        long u = (end == null) ? Long.MAX_VALUE : UUIDGen.unixTimestamp((UUID) end);
                                        query = NumberFieldMapper.NumberType.LONG.rangeQuery(mapper.name(), l, u, includeLower, includeUpper, mapper.fieldType().hasDocValues());
                                    } else {
                                        query = new TermRangeQuery(mapper.name(), BytesRefs.toBytesRef(start), BytesRefs.toBytesRef(end), includeLower, includeUpper);
                                    }
                                }
                                break;
                            case UUID:
                                query = start != null && end != null && ((Comparable) start).compareTo(end) == 0 ?
                                    new TermQuery(new Term(mapper.name(), BytesRefs.toBytesRef(start))) :
                                    new TermRangeQuery(mapper.name(), BytesRefs.toBytesRef(start), BytesRefs.toBytesRef(end), includeLower, includeUpper);
                                break;
                            case BOOLEAN:
                                query = ((BooleanFieldMapper) mapper).fieldType().rangeQuery(start, end, includeLower, includeUpper, null);
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
            final int[] fieldsIdx;   // column position in Rowcument.values
            final Set<String> indices;  // associated indices
            final PartitionFunction partitionFunction;

            ImmutablePartitionFunction(String[] args) {
                this(args, new MessageFormatPartitionFunction());
            }

            ImmutablePartitionFunction(String[] args, PartitionFunction partitionFunc) {
                this.name = args[0];
                this.pattern = args[1];
                this.fields = new String[args.length - 2];
                this.fieldsIdx = new int[args.length - 2];
                System.arraycopy(args, 2, this.fields, 0, args.length - 2);
                this.indices = new HashSet<String>();
                this.partitionFunction = partitionFunc;
            }

            // values = indexed values in the same order as MappingInfo.fields
            String indexName(Object[] values) {
                Object[] args = new Object[fields.length];
                for (int i = 0; i < fieldsIdx.length; i++)
                    args[i] = (fieldsIdx[i] < values.length) ? values[fieldsIdx[i]] : null;
                return partitionFunction.format(pattern, args);
            }

            @Override
            public String toString() {
                return this.name;
            }
        }


        final Map<String, ImmutablePartitionFunction> partitionFunctions;
        final ImmutableIndexInfo[] indices;
        final ObjectIntHashMap<String> indexToIdx;
        final ObjectIntHashMap<String> fieldsToIdx;
        final ColumnFilter columnFilter;
        final BitSet staticColumns;
        final boolean indexSomeStaticColumnsOnWideRow;
        final boolean[] indexedPkColumns;   // bit mask of indexed PK columns.
        final long metadataVersion;
        final String metadataClusterUUID;
        final String nodeId;
        final boolean indexOnCompaction;  // true if at least one index has index_on_compaction=true;
        final boolean indexInsertOnly;    // true if all indices have index_append_only=true
        final boolean indexOpaqueStorage; // true if one index have index_opaque_storage=true (

        ImmutableMappingInfo(final ClusterState state) {
            this.metadataVersion = state.metaData().version();
            this.metadataClusterUUID = state.metaData().clusterUUID();
            this.nodeId = state.nodes().getLocalNodeId();

            if (state.blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                logger.debug("global write blocked");
                this.indices = null;
                this.indexToIdx = null;
                this.fieldsToIdx = null;
                this.columnFilter = null;
                this.staticColumns = null;
                this.indexSomeStaticColumnsOnWideRow = false;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                this.indexOnCompaction = false;
                this.indexInsertOnly = false;
                this.indexOpaqueStorage = false;
                return;
            }

            Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();
            Map<String, ImmutablePartitionFunction> partFuncs = null;
            List<ImmutableIndexInfo> indexList = new ArrayList<ImmutableIndexInfo>();

            for (IndexMetaData indexMetaData : state.metaData()) {
                if (!ElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.keyspace()))
                    continue;

                String index = indexMetaData.getIndex().getName();
                MappingMetaData mappingMetaData = indexMetaData.mapping(typeName);

                if (mappingMetaData == null) {
                    if (logger.isDebugEnabled())
                        logger.info("ignore, index=[{}] no mapping for type=[{}]", index, typeName);
                    continue;
                }


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
                    Map<String, Object> mappingMap = mappingMetaData.getSourceAsMap();
                    // #181 IndiceService is available when activated and before Node start.
                    IndicesService indicesService = clusterService.getIndicesService();
                    IndexService indexService = indicesService.indexService(indexMetaData.getIndex());
                    if (indexService == null) {
                        logger.error("indexService not available for [{}], ignoring", index);
                        continue;
                    }
                    ImmutableIndexInfo indexInfo = new ImmutableIndexInfo(index, indexService, mappingMetaData, state.metaData(), IndexMetaData.isIndexUsingVersionLessEngine(indexMetaData.getSettings()));
                    indexList.add(indexInfo);

                    Map<String, Object> props = (Map<String, Object>) mappingMap.computeIfAbsent("properties", s -> new HashMap<>());
                    for (String fieldName : props.keySet()) {
                        Map<String, Object> fieldMap = (Map<String, Object>) props.get(fieldName);
                        if (fieldMap.get("enabled") == null || XContentMapValues.nodeBooleanValue(fieldMap.get("enabled"))) {
                            boolean mandartory = (fieldMap.get(TypeParsers.CQL_MANDATORY) == null || XContentMapValues.nodeBooleanValue(fieldMap.get(TypeParsers.CQL_MANDATORY)));
                            if (fieldsMap.get(fieldName) != null) {
                                mandartory = mandartory || fieldsMap.get(fieldName);
                            }
                            fieldsMap.put(fieldName, mandartory);
                        }
                    }
                    if (mappingMetaData.hasParentField()) {
                        Map<String, Object> parentsProps = (Map<String, Object>) mappingMap.get(ParentFieldMapper.NAME);
                        String pkColumns = (String) parentsProps.get(ParentFieldMapper.CQL_PARENT_PK);
                        if (pkColumns == null) {
                            fieldsMap.put(ParentFieldMapper.NAME, true);
                        } else {
                            for (String colName : pkColumns.split(","))
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
                } catch (IOException e) {
                    logger.error("Unexpected error index=[{}]", e, index);
                }
            }

            if (indexList.size() == 0) {
                if (logger.isTraceEnabled())
                    logger.trace("No active elasticsearch index for keyspace.table=[{}.{}] state={}", baseCfs.metadata.ksName, baseCfs.name, state);
                this.indices = null;
                this.indexToIdx = null;
                this.fieldsToIdx = null;
                this.columnFilter = null;
                this.staticColumns = null;
                this.indexSomeStaticColumnsOnWideRow = false;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                this.indexOnCompaction = false;
                this.indexInsertOnly = false;
                this.indexOpaqueStorage = false;
                return;
            }

            // build indices array and indexToIdx map
            this.indices = new ImmutableIndexInfo[indexList.size()];
            this.indexToIdx = new ObjectIntHashMap<String>(indexList.size());
            for (int i = 0; i < indexList.size(); i++) {
                indices[i] = indexList.get(i);
                indexToIdx.put(indexList.get(i).name, i);
            }
            boolean _indexSomeStaticColumns = false;
            boolean _indexOnCompaction = false;
            boolean _indexInsertOnly = true;
            boolean _indexOpaqueStorage = false;
            for (ImmutableIndexInfo indexInfo : this.indices) {
                if (indexInfo.index_static_columns)
                    _indexSomeStaticColumns = true;
                if (indexInfo.index_on_compaction)
                    _indexOnCompaction = true;
                if (!indexInfo.insert_only)
                    _indexInsertOnly = false;
                if (indexInfo.opaque_storage)
                    _indexOpaqueStorage = true;
            }
            this.indexSomeStaticColumnsOnWideRow = _indexSomeStaticColumns;
            this.indexOnCompaction = _indexOnCompaction;
            this.indexInsertOnly = _indexInsertOnly;
            this.indexOpaqueStorage = _indexOpaqueStorage;

            if (indexOpaqueStorage) {
                fieldsMap.put(SourceFieldMapper.NAME, true);
            }

            // order fields with pk columns first
            final String[] fields = new String[fieldsMap.size()];
            final int pkLength = baseCfs.metadata.partitionKeyColumns().size() + baseCfs.metadata.clusteringColumns().size();
            this.indexedPkColumns = new boolean[pkLength];
            int j = 0, l = 0;
            for (ColumnDefinition cd : Iterables.concat(baseCfs.metadata.partitionKeyColumns(), baseCfs.metadata.clusteringColumns())) {
                indexedPkColumns[l] = fieldsMap.containsKey(cd.name.toString());
                if (indexedPkColumns[l])
                    fields[j++] = cd.name.toString();
                l++;
            }
            for (String f : fieldsMap.keySet()) {
                boolean alreadyInFields = false;
                for (int k = 0; k < j; k++) {
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
            for (int i = 0; i < fields.length; i++)
                this.fieldsToIdx.put(fields[i], i);

            this.staticColumns = (baseCfs.metadata.hasStaticColumns()) ? new BitSet(fields.length) : null;
            ColumnFilter.Builder cfb = ColumnFilter.selectionBuilder();
            for (int i = 0; i < fields.length; i++) {
                ColumnIdentifier colId = new ColumnIdentifier(fields[i], true);
                ColumnDefinition colDef = baseCfs.metadata.getColumnDefinition(colId);
                if (colDef != null) {
                    // colDef may be null when mapping an object with no sub-field (and no underlying column, see #144)
                    if (staticColumns != null)
                        this.staticColumns.set(i, colDef.isStatic());
                    if (colDef.isRegular() || colDef.isStatic())
                        cfb.add(colDef);
                }
            }
            this.columnFilter = cfb.build();

            if (partFuncs != null && partFuncs.size() > 0) {
                for (ImmutablePartitionFunction func : partFuncs.values()) {
                    int i = 0;
                    for (String field : func.fields)
                        func.fieldsIdx[i++] = this.fieldsToIdx.getOrDefault(field, -1);
                }
                this.partitionFunctions = partFuncs;
            } else {
                this.partitionFunctions = null;
            }

            // build InderInfo.mappers arrays.
            for (ImmutableIndexInfo indexInfo : this.indices) {
                indexInfo.mappers = new Mapper[fields.length];
                for (int i = 0; i < fields.length; i++) {
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

            if (logger.isDebugEnabled()) {
                logger.debug("New ImmutableMappingInfo indices={}, fields={} staticColumns={} columnFilter=[{}] ",
                        Arrays.toString(indices), fields, staticColumns, columnFilter);
            }
        }

        public long startedShardCount() {
            return indices == null ? 0 : Arrays.stream(indices).filter(i -> { return i.shard() != null; }).count();
        }

        public boolean hasAllShardStarted() {
            return indices == null ? false : startedShardCount() == indices.length;
        }

        public BitSet targetIndices(final Object[] values) {
            if (this.partitionFunctions == null)
                return null;

            BitSet targets = new BitSet(this.indices.length);
            for (ImmutablePartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                int indexIdx = this.indexToIdx.getOrDefault(indexName, -1);
                if (indexIdx >= 0) {
                    targets.set(indexIdx);
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("No target index=[{}] found for partition function name=[{}] pattern=[{}] indices={}",
                            indexName, func.name, func.pattern, Arrays.toString(this.indices));
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
            for (ImmutablePartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                int indexIdx = this.indexToIdx.getOrDefault(indexName, -1);
                if (indexIdx >= 0) {
                    targets.set(indexIdx);
                } else {
                    if (logger.isWarnEnabled())
                        logger.warn("No target index=[{}] found, function name=[{}] pattern=[{}], return all indices={}",
                            indexName, func.name, func.pattern, Arrays.toString(this.indices));
                    for (String index : func.indices) {
                        int i = this.indexToIdx.getOrDefault(index, -1);
                        if (i >= 0)
                            targets.set(i);
                    }
                }
            }
            return targets;
        }

        public DeleteByQuery buildDeleteByQuery(IndexService indexService, Query query) {
            BitSetProducer parentFilter = null;
            if (indexService.mapperService().hasNested()) {
                parentFilter = new BitSetProducer() {
                    @Override
                    public org.apache.lucene.util.BitSet getBitSet(LeafReaderContext context) throws IOException {
                        final IndexReaderContext topLevelContext = ReaderUtil.getTopLevelContext(context);
                        final IndexSearcher searcher = new IndexSearcher(topLevelContext);
                        searcher.setQueryCache(null);
                        final Weight weight = searcher.createNormalizedWeight(new MatchAllDocsQuery(), false);
                        Scorer s = weight.scorer(context);
                        return (s == null) ? null : org.apache.lucene.util.BitSet.of(s.iterator(), context.reader().maxDoc());
                    }
                };
            }
            return new DeleteByQuery(query, null, null, null, parentFilter, Operation.Origin.PRIMARY, System.currentTimeMillis(), typeName);
        }

        class WideRowcumentIndexer extends RowcumentIndexer {
            final NavigableSet<Clustering> clusterings = new java.util.TreeSet<Clustering>(baseCfs.metadata.comparator);
            final Map<Clustering, WideRowcument> rowcuments = new TreeMap<Clustering, WideRowcument>(baseCfs.metadata.comparator);
            List<RangeTombstone> rangeTombstones = null;
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
                    if (inRow != null && inRow.isStatic() && !inRow.isEmpty()) {
                        inStaticRow = inRow;
                    } else if (outRow != null && outRow.isStatic()) {
                        outStaticRow = inRow;
                    }
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

                    if (inRow != null && inRow.isStatic())
                        this.inStaticRow = inRow;
                    if (outRow != null && outRow.isStatic())
                        this.outStaticRow = outRow;

                    Row row = (inRow == null) ? outRow : inRow;
                    if (!row.isStatic()) {
                        clusterings.add(row.clustering());
                        if (outRow != null || ImmutableMappingInfo.this.indexInsertOnly)
                            rowcuments.put(row.clustering(), new WideRowcument(inRow, outRow));
                    }
                } catch (Throwable t) {
                    logger.error("Unexpected error", t);
                }
            }

            private void readBeforeWrite(SinglePartitionReadCommand command) {
                RowIterator rowIt = read(command);
                if (!rowIt.staticRow().isEmpty()) {
                    this.inStaticRow = rowIt.staticRow();
                    this.outStaticRow = null;
                }
                for (; rowIt.hasNext(); ) {
                    try {
                        Row row = rowIt.next();
                        WideRowcument rowcument = new WideRowcument(row, null);
                        if (indexSomeStaticColumnsOnWideRow && inStaticRow != null)
                            rowcument.readCellValues(inStaticRow); // add static fields
                        rowcument.write(); // index live doc or remove tombestone
                        rowcuments.remove(row.clustering());
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
                }
            }

            /**
             * read-before-write is mandatory to filter out-of-time-order inserted rows.
             * We also need to delete rows removed from memtable, not found in the read-before-write to keep ES index sync.
             */
            @Override
            public void update() {
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} inStaticRow={} outStaticRow={} clustering={} rangeTombstones={}",
                        this.hashCode(), inStaticRow, outStaticRow, this.clusterings, this.rangeTombstones);

                // A partition delete before an insert indexed after that insert could trigger a wrong delete, so we need to read-before-write when indexInsertOnly=false, ...
                if (delTime != null && delTime.deletes(this.nowInSec)) {
                    deletePartition();
                    if (ImmutableMappingInfo.this.indexInsertOnly)
                        return;
                }

                if (rangeTombstones != null) {
                    for (RangeTombstone tombstone : rangeTombstones) {
                        try {
                            BitSet targets = targetIndices(pkCols);
                            if (targets == null) {
                                for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices)
                                    indexInfo.deleteByQuery(pkCols, tombstone);
                            } else {
                                for (int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i + 1))
                                    indices[i].deleteByQuery(pkCols, tombstone);
                            }
                        } catch (Throwable t) {
                            logger.error("Unexpected error", t);
                        }
                    }
                    // read tombstone ranges in case of delete played out-of-time-order (if time matters)
                    if (!ImmutableMappingInfo.this.indexInsertOnly) {
                        Slices.Builder slices = new Slices.Builder(baseCfs.metadata.comparator, rangeTombstones.size());
                        for (RangeTombstone tombstone : rangeTombstones) {
                            if (!tombstone.deletedSlice().isEmpty(baseCfs.metadata.comparator)) {
                                if (logger.isTraceEnabled())
                                    logger.trace("indexer={} read partition range={}", this.hashCode(), tombstone.deletedSlice().toString(baseCfs.metadata.comparator));
                                slices.add(tombstone.deletedSlice());
                            }
                        }
                        ClusteringIndexSliceFilter filter = new ClusteringIndexSliceFilter(slices.build(), false);
                        SinglePartitionReadCommand command = SinglePartitionReadCommand.create(baseCfs.metadata, nowInSec, columnFilter, RowFilter.NONE, DataLimits.NONE, key, filter);
                        readBeforeWrite(command);
                    }
                }

                if (ImmutableMappingInfo.this.indexInsertOnly) {
                    for (WideRowcument rowcument : rowcuments.values()) {
                        if (indexSomeStaticColumnsOnWideRow && inStaticRow != null) {
                            try {
                                rowcument.readCellValues(inStaticRow); // add static fields
                            } catch (IOException e) {
                                logger.error("Unexpected error", e);
                            }
                        }
                        rowcument.write();
                    }
                } else {
                    if (!this.clusterings.isEmpty()) {
                        // read-before-write for consistency
                        if (logger.isTraceEnabled())
                            logger.trace("indexer={} read partition for clusterings={}", this.hashCode(), clusterings);
                        ClusteringIndexNamesFilter filter = new ClusteringIndexNamesFilter(clusterings, false);
                        SinglePartitionReadCommand command = SinglePartitionReadCommand.create(baseCfs.metadata, nowInSec, columnFilter, RowFilter.NONE, DataLimits.NONE, key, filter);
                        //SinglePartitionReadCommand command = SinglePartitionReadCommand.create(baseCfs.metadata, nowInSec, key, clusterings);
                        readBeforeWrite(command);
                    }

                    // remove remaining tombestones from ES (row removed from memtable and not found in the read-before-write).
                    for (WideRowcument rowcument : rowcuments.values()) {
                        rowcument.delete();
                    }
                }

                // manage static row
                if (this.inStaticRow != null && inStaticRow.hasLiveData(nowInSec, baseCfs.metadata.enforceStrictLiveness())) {
                    try {
                        // index live static document to ES
                        WideRowcument rowcument = new WideRowcument(inStaticRow, null);
                        rowcument.isStatic = true;
                        rowcument.index();
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
                } else {
                    if (this.outStaticRow != null) {
                        try {
                            // remove tombestone static document from ES
                            WideRowcument rowcument = new WideRowcument(null, outStaticRow);
                            rowcument.isStatic = true;
                            rowcument.delete();
                        } catch (IOException e) {
                            logger.error("Unexpected error", e);
                        }
                    }
                }
            }

            /**
             * Notification of a RangeTombstone.
             * An update of a single partition may contain multiple RangeTombstones,
             * and a notification will be passed for each of them.
             *
             * @param tombstone
             */
            @Override
            public void rangeTombstone(RangeTombstone tombstone) {
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} range tombestone row {}: {}", this.hashCode(), this.transactionType, tombstone.deletedSlice());
                if (this.rangeTombstones == null)
                    this.rangeTombstones = new LinkedList();
                this.rangeTombstones.add(tombstone);
            }

            @Override
            public void deletePartition(ImmutableMappingInfo.ImmutableIndexInfo indexInfo, IndexShard indexShard) throws IOException {
                if (logger.isTraceEnabled())
                    logger.trace("deleting documents where _routing={} from index.type={}.{}", this.partitionKey, indexShard.shardId().getIndexName(), typeName);

                Query termQuery;
                if ( baseCfs.metadata.partitionKeyColumns().size() == 1 ) {
                    String ptName = baseCfs.metadata.partitionKeyColumns().get(0).name.toString();
                    int mapperIdx = indexInfo.indexOf(ptName);
                    FieldMapper ptMapper = (FieldMapper) indexInfo.mappers[mapperIdx];
                    termQuery = ptMapper.fieldType().termQuery(this.pkCols[0], null);
                } else {
                    termQuery = new TermQuery(new Term(RoutingFieldMapper.NAME, this.partitionKey));
                }
                DeleteByQuery deleteByQuery = buildDeleteByQuery(indexShard.indexService(), termQuery);
                indexShard.getEngine().delete(deleteByQuery);
            }
        }

        class CleanupWideRowcumentIndexer extends WideRowcumentIndexer {
            public CleanupWideRowcumentIndexer(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup, Type transactionType) {
                super(key, columns, nowInSec, opGroup, transactionType);
            }

            @Override
            public void update() {
                for (WideRowcument rowcument : rowcuments.values())
                    rowcument.delete();
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
                    if (outRow != null || ImmutableMappingInfo.this.indexInsertOnly)
                        this.rowcument = new SkinnyRowcument(inRow, outRow);
                } catch (Throwable t) {
                    logger.error("Unexpected error", t);
                }
            }

            /**
             * read-before-write is mandatory to filter out-of-time-order inserted rows.
             * We also need to delete a row removed from memtable to keep ES index sync.
             */
            @Override
            public void update() {
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} key={}", this.hashCode(), key);

                // A partition delete before an insert indexed after that insert could trigger a wrong delete, so we need to read-before-write when indexInsertOnly=false, ...
                if (delTime != null && delTime.deletes(this.nowInSec)) {
                    deletePartition();
                    if (ImmutableMappingInfo.this.indexInsertOnly)
                        return;
                }

                if (ImmutableMappingInfo.this.indexInsertOnly) {
                    if (rowcument != null)
                        rowcument.write();
                } else {
                    SinglePartitionReadCommand command = SinglePartitionReadCommand.create(baseCfs.metadata, nowInSec, columnFilter, RowFilter.NONE, DataLimits.NONE, key, SKINNY_FILTER);
                    RowIterator rowIt = read(command);
                    if (rowIt.hasNext()) {
                        try {
                            rowcument = new SkinnyRowcument(rowIt.next(), null);
                            rowcument.write(); // update live row in ES.
                        } catch (IOException e) {
                            logger.error("Unexpected error", e);
                        }
                    } else {
                        // remove tombestone from ES
                        if (rowcument != null && !rowcument.hasLiveData())
                            rowcument.delete();
                    }
                }
            }

            @Override
            public void deletePartition(ImmutableMappingInfo.ImmutableIndexInfo indexInfo, IndexShard indexShard) throws IOException {
                Term termUid = termUid(indexShard.indexService(), this.partitionKey);
                if (logger.isDebugEnabled())
                    logger.debug("indexer={} deleting document from index.type={}.{} id={} termUid={}",
                        this.hashCode(), indexShard.shardId().getIndexName(), typeName, this.partitionKey, termUid.text());
                Engine.Delete delete = new Engine.Delete(typeName, this.partitionKey, termUid);
                indexShard.delete(indexShard.getEngine(), delete);
            }
        }

        class CleanupSkinnyRowcumentIndexer extends SkinnyRowcumentIndexer {
            public CleanupSkinnyRowcumentIndexer(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup, Type transactionType) {
                super(key, columns, nowInSec, opGroup, transactionType);
            }

            @Override
            public void update() {
                if (rowcument != null)
                    rowcument.delete();
            }
        }

        abstract class RowcumentIndexer implements Index.Indexer {
            final DecoratedKey key;
            final int nowInSec;
            final IndexTransaction.Type transactionType;
            final OpOrder.Group opGroup;
            final Object[] pkCols = new Object[baseCfs.metadata.partitionKeyColumns().size() + baseCfs.metadata.clusteringColumns().size()];
            final String partitionKey;
            BitSet targets = null;
            DeletionTime delTime = null;

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
                    for (ByteBuffer bb : composite.split(key.getKey())) {
                        AbstractType<?> type = composite.types.get(i);
                        pkCols[i++] = type.compose(bb);
                    }
                } else {
                    pkCols[i++] = keyValidator.compose(key.getKey());
                }
                this.partitionKey = Serializer.stringify(pkCols, i);
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
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} Insert row {}: {}", this.hashCode(), this.transactionType, row);
                if (row.hasLiveData(nowInSec, baseCfs.metadata.enforceStrictLiveness()))
                    collect(row, null);
                else
                    collect(null, row);
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
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} Update row {}: {} to {}", this.hashCode(), this.transactionType, oldRowData, newRowData);
                collect(newRowData, oldRowData);
            }

            /**
             * Notification that a row was removed from the partition.
             * Note that this is only called as part of either a compaction or a cleanup.
             * This context is indicated by the TransactionType supplied to the indexerFor method.
             * <p>
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
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} Remove row {}: {}", this.hashCode(), this.transactionType, row);
                collect(null, row);
            }

            /**
             * Notification of a top level partition delete.
             *
             * @param deletionTime
             */
            @Override
            public void partitionDelete(DeletionTime deletionTime) {
                if (logger.isTraceEnabled())
                    logger.trace("indexer={} Delete partition {}: {}", this.hashCode(), this.transactionType, deletionTime);
                this.delTime = deletionTime;
            }

            /**
             * Notification of a RangeTombstone.
             * An update of a single partition may contain multiple RangeTombstones,
             * and a notification will be passed for each of them.
             *
             * @param tombstone
             */
            @Override
            public void rangeTombstone(RangeTombstone tombstone) {
            }

            /**
             * Notification of the end of the partition update.
             * This event always occurs after all others for the particular update.
             */
            @Override
            public void finish() {
                try {
                    if (ImmutableMappingInfo.this.indexInsertOnly) {
                        update();
                    } else {
                        // lock to protect concurrent updates on the same partition
                        synchronized (getLock()) {
                            update();
                        }
                    }
                    if (this.targets == null) {
                        // refresh all associated indices.
                        for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices)
                            indexInfo.refresh();
                    } else {
                        // only refresh updated partitionned indices.
                        for (int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i + 1))
                            indices[i].refresh();
                    }
                } catch (Throwable t) {
                    logger.error("Unexpected error", t);
                }
            }

            /**
             * Collect incoming and outgoing rows in the partition.
             *
             * @param inRow
             * @param outRow
             */
            public abstract void collect(Row inRow, Row outRow);

            /**
             * Update elasticsearch indices
             */
            public abstract void update();

            public void deletePartition() {
                mappingInfoLock.readLock().lock();
                try {
                    for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices) {
                        IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                        if (indexShard != null) {
                            if (!indexInfo.updated)
                                indexInfo.updated = true;
                            try {
                                deletePartition(indexInfo, indexShard);
                            } catch (EngineException e) {
                                logger.error("Document deletion error", e);
                            }
                        } else {
                            logger.warn("indexer={} Shard not available to delete document index.type={}.{} partitionKey={}",
                                this.hashCode(), indexInfo.name, indexInfo.type, this.partitionKey);
                        }
                    }
                } catch (Throwable t) {
                    logger.error("Unexpected error", t);
                } finally {
                    mappingInfoLock.readLock().unlock();
                }
            }

            public abstract void deletePartition(ImmutableMappingInfo.ImmutableIndexInfo indexInfo, IndexShard indexShard) throws IOException;

            public RowIterator read(SinglePartitionReadCommand command) {
                try (ReadExecutionController control = command.executionController()) {
                    UnfilteredRowIterator unfilteredRows = command.queryMemtableAndDisk(baseCfs, control);
                    return UnfilteredRowIterators.filter(unfilteredRows, nowInSec);
                }
            }

            public Term termUid(IndexService indexService, String id) {
                Term termUid;
                if (indexService.getIndexSettings().getIndexVersionCreated().onOrAfter(Version.V_6_0_0_beta1)) {
                    termUid = new Term(IdFieldMapper.NAME, Uid.encodeId(id));
                } else if (indexService.mapperService().documentMapper(typeName).idFieldMapper().fieldType().indexOptions() != IndexOptions.NONE) {
                    termUid = new Term(IdFieldMapper.NAME, id);
                } else {
                    termUid = new Term(UidFieldMapper.NAME, Uid.createUidAsBytes(typeName, id));
                }
                return termUid;
            }

            class Rowcument {
                final String id;
                final Object[] values = new Object[fieldsToIdx.size()];
                int docTtl = Integer.MAX_VALUE;
                int inRowDataSize = 0;
                boolean hasLiveData = false;
                boolean hasRowMarker = false;
                boolean isStatic;

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

                    // copy the indexed columns of partition key in values
                    int x = 0;
                    for (int i = 0; i < baseCfs.metadata.partitionKeyColumns().size(); i++) {
                        if (indexedPkColumns[i])
                            values[x++] = pkCols[i];
                    }
                    // copy the indexed columns of clustering key in values
                    if (!row.isStatic() && row.clustering().size() > 0) {
                        int i = 0;
                        for (ColumnDefinition ccd : baseCfs.metadata.clusteringColumns()) {
                            Object value = Serializer.deserialize(ccd.type, row.clustering().get(i));
                            pkCols[baseCfs.metadata.partitionKeyColumns().size() + i] = value;
                            if (indexedPkColumns[baseCfs.metadata.partitionKeyColumns().size() + i])
                                values[x++] = value;
                            i++;
                        }
                        id = Serializer.stringify(pkCols, pkCols.length);
                    } else {
                        id = partitionKey;
                    }

                    if (inRow != null)
                        readCellValues(inRow);
                }

                public boolean hasLiveData() {
                    return hasLiveData;
                }

                public boolean isStatic() {
                    return isStatic;
                }

                public void readCellValues(Row row) throws IOException {
                    for (Cell cell : row.cells())
                        readCellValue(cell);
                }

                public void readCellValue(Cell cell) throws IOException {
                    final String cellNameString = cell.column().name.toString();
                    int idx = fieldsToIdx.getOrDefault(cellNameString, -1);
                    if (idx == -1)
                        return; //ignore cell, not indexed.

                    if (cell.isLive(nowInSec)) {
                        docTtl = Math.min(cell.localDeletionTime(), docTtl);

                        ColumnDefinition cd = cell.column();
                        if (cd.type.isCollection()) {
                            CollectionType ctype = (CollectionType) cd.type;
                            Object value = null;

                            switch (ctype.kind) {
                                case LIST:
                                    value = Serializer.deserialize(((ListType) cd.type).getElementsType(), cell.value());
                                    if (logger.isTraceEnabled())
                                        logger.trace("indexer={} list name={} kind={} type={} value={}",
                                            RowcumentIndexer.this.hashCode(), cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                    List l = (List) values[idx];
                                    if (l == null) {
                                        l = new ArrayList<>(1);
                                        values[idx] = l;
                                    }
                                    l.add(value);
                                    break;
                                case SET:
                                    value = Serializer.deserialize(((SetType) cd.type).getElementsType(), cell.path().get(0));
                                    if (logger.isTraceEnabled())
                                        logger.trace("indexer={} set name={} kind={} type={} value={}",
                                            RowcumentIndexer.this.hashCode(), cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                    Set s = (Set) values[idx];
                                    if (s == null) {
                                        s = new HashSet<>();
                                        values[idx] = s;
                                    }
                                    s.add(value);
                                    break;
                                case MAP:
                                    value = Serializer.deserialize(((MapType) cd.type).getValuesType(), cell.value());
                                    CellPath cellPath = cell.path();
                                    Object key = Serializer.deserialize(((MapType) cd.type).getKeysType(), cellPath.get(cellPath.size() - 1));
                                    if (logger.isTraceEnabled())
                                        logger.trace("indexer={} map name={} kind={} type={} key={} value={}",
                                            RowcumentIndexer.this.hashCode(), cellNameString, cd.kind, cd.type.asCQL3Type().toString(), key, value);
                                    if (key instanceof String) {
                                        Map m = (Map) values[idx];
                                        if (m == null) {
                                            m = new HashMap<>();
                                            values[idx] = m;
                                        }
                                        m.put(key, value);
                                    }
                                    break;
                            }
                        } else {
                            Object value = Serializer.deserialize(cd.type, cell.value());
                            if (logger.isTraceEnabled())
                                logger.trace("indexer={} name={} kind={} type={} value={}",
                                    RowcumentIndexer.this.hashCode(), cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);

                            values[idx] = value;
                        }
                    }
                }

                public IndexingContext buildContext(ImmutableIndexInfo indexInfo, boolean staticColumnsOnly) throws IOException {
                    IndexingContext context = ElasticSecondaryIndex.this.perThreadContext.get();
                    Uid uid = new Uid(typeName, (staticColumnsOnly) ? partitionKey : id);
                    context.reset(indexInfo, uid);

                    // preCreate for all metadata fields.
                    for (MetadataFieldMapper metadataMapper : context.docMapper.mapping().metadataMappers())
                        metadataMapper.preCreate(context);

                    context.docMapper.idFieldMapper().createField(context, uid.id());
                    context.docMapper.uidMapper().createField(context, uid);
                    context.docMapper.typeMapper().createField(context, typeName);
                    context.docMapper.tokenFieldMapper().createField(context, key.getToken().getTokenValue());
                    context.docMapper.seqNoFieldMapper().createField(context, null); // add zero _seq_no

                    if (indexInfo.includeNodeId)
                        context.docMapper.nodeFieldMapper().createField(context, ImmutableMappingInfo.this.nodeId);

                    if (this instanceof WideRowcument && baseCfs.metadata.partitionKeyColumns().size() > 1)
                        context.docMapper.routingFieldMapper().createField(context, partitionKey);

                    if (!indexInfo.versionLessEngine) {
                        context.doc().add(DEFAULT_INTERNAL_VERSION);
                    }

                    // add all mapped fields to the current context.
                    for (int i = 0; i < values.length; i++) {
                        if (indexInfo.mappers[i] != null && (indexInfo.index_static_columns || indexInfo.index_static_document || !indexInfo.isStaticField(i)))
                            try {
                                ElasticSecondaryIndex.this.addField(context, indexInfo, indexInfo.mappers[i], values[i]);
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
                                for (int i = 0; i < cols.length; i++)
                                    parentValues[i] = values[fieldsToIdx.get(cols[i])];
                                parent = Serializer.stringify(parentValues, cols.length);
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

                public void index() {
                    long startTime = System.nanoTime();
                    long ttl = (this.docTtl < Integer.MAX_VALUE) ? this.docTtl : 0;

                    targets = ImmutableMappingInfo.this.targetIndices(values);
                    if (targets == null) {
                        // index for associated indices
                        for (ImmutableIndexInfo indexInfo : indices)
                            index(indexInfo, startTime, ttl);
                    } else {
                        // delete for matching target indices.
                        for (int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i + 1))
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
                            if (indexInfo.opaque_storage) {
                                final DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(typeName);
                                final ByteBuffer bb = (ByteBuffer) values[indexInfo.indexOf(SourceFieldMapper.NAME)];
                                final BytesReference source = new BytesArray(bb.array(), bb.position(), bb.limit() - bb.position());

                                final SourceToParse sourceToParse = SourceToParse.source(indexInfo.name, typeName, id, source, XContentType.JSON);
                                sourceToParse.token((Long)key.getToken().getTokenValue());
                                if (this instanceof WideRowcument && baseCfs.metadata.partitionKeyColumns().size() > 1)
                                    sourceToParse.routing(partitionKey);

                                final ParsedDocument parsedDoc = docMapper.parse(sourceToParse);
                                indexParsedDocument(indexInfo, docMapper, parsedDoc, startTime, ttl);
                            } else {
                                IndexingContext context = buildContext(indexInfo, isStatic());
                                if (isStatic()) {
                                    for (Document doc : context.docs()) {
                                        if (doc instanceof IndexingContext.StaticDocument)
                                            ((IndexingContext.StaticDocument) doc).applyFilter(isStatic());
                                    }
                                }
                                context.finalize();

                                final ParsedDocument parsedDoc = new ParsedDocument(
                                    context.version(),
                                    SeqNoFieldMapper.SequenceIDFields.emptySeqID(),
                                    (isStatic()) ? partitionKey : id,
                                    context.type(),
                                    Serializer.stringify(pkCols, baseCfs.metadata.partitionKeyColumns().size()), // routing
                                    ((Long) key.getToken().getTokenValue()).longValue(),
                                    context.docs(),
                                    context.source(), // source
                                    XContentType.JSON,
                                    (Mapping) null); // mappingUpdate

                                parsedDoc.parent(context.parent());
                                indexParsedDocument(indexInfo, context.docMapper, parsedDoc, startTime, ttl);
                            }
                        } catch (IOException e) {
                            logger.error("error", e);
                        }
                    }
                }

                private void indexParsedDocument(ImmutableIndexInfo indexInfo, DocumentMapper docMapper, ParsedDocument parsedDoc, long startTime, long ttl) throws IOException {
                    if (logger.isTraceEnabled()) {
                        logger.trace("indexer={} index={} id={} type={} routing={}",
                            RowcumentIndexer.this.hashCode(), indexInfo.name, parsedDoc.id(), parsedDoc.type(), parsedDoc.routing());
                        for(int k = 0; k< parsedDoc.docs().size(); k++)
                            logger.trace("indexer={} doc[{}]={}", RowcumentIndexer.this.hashCode(), k, parsedDoc.docs().get(k));
                    }

                    final IndexShard indexShard = indexInfo.shard();
                    if (indexShard != null) {
                        if (!indexInfo.updated)
                            indexInfo.updated = true;

                        final Engine.Index operation = new Engine.Index(
                            termUid(indexInfo.indexService, id),
                            parsedDoc,
                            SequenceNumbers.UNASSIGNED_SEQ_NO,
                            0L,
                            1L,
                            VersionType.INTERNAL,
                            Engine.Operation.Origin.PRIMARY,
                            startTime,
                            startTime, false) {
                            @Override
                            public int estimatedSizeInBytes() {
                                return (id.length() + docMapper.type().length()) * 2 + inRowDataSize + 12;
                            }
                        };

                        IndexResult result = indexShard.index(indexShard.getEngine(), operation);

                        if (result.hasFailure() && logger.isErrorEnabled()) {
                            logger.error((Supplier<?>) () ->
                                    new ParameterizedMessage("document CF={}.{} index/type={}/{} id={} version={} created={} static={} ttl={} refresh={}",
                                        baseCfs.metadata.ksName, baseCfs.metadata.cfName,
                                        indexInfo.name, typeName,
                                        parsedDoc.id(), operation.version(), result.isCreated(), isStatic(), ttl, indexInfo.refresh),
                                result.getFailure());
                        } else if (logger.isDebugEnabled()) {
                            logger.debug("document CF={}.{} index/type={}/{} id={} version={} created={} static={} ttl={} refresh={}",
                                baseCfs.metadata.ksName, baseCfs.metadata.cfName,
                                indexInfo.name, typeName,
                                parsedDoc.id(), operation.version(), result.isCreated(), isStatic(), ttl, indexInfo.refresh);
                        }
                    }
                }

                public void delete() {
                    targets = ImmutableMappingInfo.this.targetIndices(values);
                    if (targets == null) {
                        // delete for associated indices
                        for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : indices)
                            delete(indexInfo);
                    } else {
                        // delete for matching target indices.
                        for (int i = targets.nextSetBit(0); i >= 0 && i < indices.length; i = targets.nextSetBit(i + 1))
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
                        if (!indexInfo.updated)
                            indexInfo.updated = true;

                        try {
                            Term termUid = termUid(indexShard.indexService(), id);
                            if (logger.isDebugEnabled())
                                logger.debug("deleting document from index.type={}.{} id={} termUid={}", indexInfo.name, typeName, id, termUid.text());
                            Engine.Delete delete = new Engine.Delete(typeName, id, termUid);
                            indexShard.delete(indexShard.getEngine(), delete);
                        } catch (IOException e) {
                            logger.error("Document deletion error", e);
                        }
                    }
                }

            }

        }
    }

    public boolean isIndexing() {
        if (!runsElassandra)
            return false;

        ImmutableMappingInfo mappingInfo = mappingInfoRef.get();
        if (mappingInfo == null) {
            if (logger.isWarnEnabled())
                logger.warn("No Elasticsearch index ready on table {}.{}", this.baseCfs.metadata.ksName, this.baseCfs.metadata.cfName);
            return false;
        }
        if (mappingInfo.indices == null || mappingInfo.indices.length == 0) {
            if (logger.isWarnEnabled())
                logger.warn("No Elasticsearch index configured for {}.{}", this.baseCfs.metadata.ksName, this.baseCfs.metadata.cfName);
            return false;
        }

        long startedShards = mappingInfo.startedShardCount();
        if (logger.isTraceEnabled()) {
            logger.trace("indices={} shards {}/{} started for table {}.{}",
                Arrays.toString(mappingInfo.indices), startedShards, mappingInfo.indices.length,
                this.baseCfs.metadata.ksName, this.baseCfs.metadata.cfName);
        }
        return startedShards > 0;
    }

    @Override
    public String toString() {
        return this.index_name;
    }

    public void updateMappingInfo(ClusterState clusterState) {
        mappingInfoLock.writeLock().lock();
        try {
            mappingInfoRef.set(new ImmutableMappingInfo(clusterState));

            ImmutableMappingInfo newMappingInfo = mappingInfoRef.get();
            if (logger.isDebugEnabled())
                logger.debug("secondary index=[{}] metadata.version={} mappingInfo.indices={} started shards={}/{}",
                    this.index_name, clusterState.metaData().version(),
                    Arrays.toString(newMappingInfo.indices),
                    newMappingInfo.startedShardCount(),
                    newMappingInfo.indices == null ? 0 : newMappingInfo.indices.length);

            startRebuildIfNeeded(); // trigger delayed index rebuild
        } catch(Exception e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to update mapping index=[{}]", index_name), e);
         } finally {
            mappingInfoLock.writeLock().unlock();
         }
    }

    // TODO: notify 2i only for udated indices (not all)
    public void clusterChanged(ClusterChangedEvent event) {
        ImmutableMappingInfo mappingInfo = mappingInfoRef.get();
        boolean updateMapping = false;
        if (mappingInfo == null || !event.state().blocks().isSame(event.previousState().blocks(),
            mappingInfo.indices == null ? Collections.EMPTY_LIST : Arrays.stream(mappingInfo.indices).map(i -> i.name).collect(Collectors.toList()))) {
            updateMapping = true;
        } else {
            for (ObjectCursor<IndexMetaData> cursor : event.state().metaData().indices().values()) {
                IndexMetaData indexMetaData = cursor.value;
                if (!indexMetaData.keyspace().equals(this.baseCfs.metadata.ksName) || indexMetaData.mapping(this.typeName) == null)
                    continue;

                if (mappingInfo.indexToIdx == null ||
                    !mappingInfo.indexToIdx.containsKey(indexMetaData.getIndex().getName()) ||
                     mappingInfo.indices[mappingInfo.indexToIdx.get(indexMetaData.getIndex().getName())].version != indexMetaData.getVersion()) {
                    updateMapping = true;
                    break;
                }
            }
        }
        if (updateMapping) {
            updateMappingInfo(event.state());
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
     * If index is not ready, delay index initial build.
     * @return
     */
    @Override
    public boolean delayInitializationTask()
    {
        ImmutableMappingInfo mappingInfo = mappingInfoRef.get();
        return mappingInfo == null || !mappingInfo.hasAllShardStarted();
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
        return isBuilt() || isBuilding() || baseCfs.isEmpty() ? null : getBuildIndexTask();
    }

    private Callable<?> getBuildIndexTask()
    {
        needBuild.set(false);
        return () -> {
            this.baseCfs.indexManager.buildIndex(this);
            return null;
        };
    }

    /**
     * Start 2i index rebuild once all associated shards are started.
     * Rebuild is tried only once (closing+opening index should not trigger many rebuild).
     * @param indexShard
     */
    public void onShardStarted(IndexShard indexShard)
    {
        startRebuildIfNeeded();
    }

    /**
     * Rebuild 2i index if needed.
     */
    private void startRebuildIfNeeded() {
        ImmutableMappingInfo mappingInfo = mappingInfoRef.get();
        if (!isBuilt() &&
            !isBuilding() &&
            mappingInfo != null &&
            mappingInfo.hasAllShardStarted() &&
            needBuild.compareAndSet(true, false))
        {
            logger.info("start building secondary {}.{}.{}", baseCfs.keyspace.getName(), baseCfs.metadata.cfName, indexMetadata.name);
            baseCfs.indexManager.buildIndexBlocking(this);
        }
    }

    private boolean isBuilt() {
        return SystemKeyspace.isIndexBuilt(baseCfs.keyspace.getName(), this.indexMetadata.name);
    }

    private boolean isBuilding() {
        return baseCfs.indexManager.isIndexBuilding(this.indexMetadata.name);
    }

    @Override
    public Callable<?> getMetadataReloadTask(IndexMetadata indexMetadata) {
        return null;
    }

    /**
     * Cassandra index flush .. Elasticsearch flush ... lucene commit and disk sync.
     */
    @Override
    public Callable<?> getBlockingFlushTask() {
        return () -> {
            if (isIndexing()) {
                for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : mappingInfoRef.get().indices) {
                    try {
                        IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                        if (indexShard != null && indexInfo.updated) {
                            if (indexShard.state() == IndexShardState.STARTED) {
                                long start = System.currentTimeMillis();
                                indexInfo.updated = false; // reset updated state
                                indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                                if (logger.isInfoEnabled())
                                    logger.info("Elasticsearch index=[{}] type=[{}] flushed, duration={}ms", indexInfo.name, indexInfo.type, System.currentTimeMillis() - start);
                            } else {
                                if (logger.isDebugEnabled())
                                    logger.debug("Cannot flush index=[{}], state=[{}]", indexInfo.name, indexShard.state());
                            }
                        }
                    } catch (ElasticsearchException e) {
                        logger.error("Error while flushing index=[{}]", e, indexInfo.name);
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
    @SuppressForbidden(reason = "File used for snapshots")
    @Override
    public Callable<?> getSnapshotWithoutFlushTask(String snapshotName) {
        return () -> {
            if (isIndexing()) {
                for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : mappingInfoRef.get().indices) {
                    IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                    if (indexShard != null && indexInfo.snapshot) {
                        if (indexShard.state() == IndexShardState.STARTED) {
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
                                for (Path luceneFile : stream) {
                                    File targetLink = new File(snapshotDir.toFile(), luceneFile.getFileName().toString());
                                    FileUtils.createHardLink(luceneFile.toFile(), targetLink);
                                }
                                if (logger.isDebugEnabled())
                                    logger.debug("Elasticsearch index=[{}/{}], snapshot=[{}], path=[{}]", indexInfo.name, indexInfo.indexService.indexUUID(), snapshotName, snapshotDir.toString());
                            } catch (DirectoryIteratorException ex) {
                                logger.error("Failed to retreive lucene files in {}", ex, indexPath);
                            }
                        } else {
                            if (logger.isDebugEnabled())
                                logger.debug("Cannot snapshot index=[{}/{}], state=[{}], snapshot=[{}]", indexInfo.name, indexInfo.indexService.indexUUID(), indexShard.state(), snapshotName);
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
            elasticSecondayIndices.remove(index_name);
            return null;
        };
    }

    @Override
    public Callable<?> getTruncateTask(long truncatedAt) {
        return () -> {
            if (isIndexing()) {
                for (ImmutableMappingInfo.ImmutableIndexInfo indexInfo : mappingInfoRef.get().indices) {
                    try {
                        IndexShard indexShard = indexInfo.indexService.getShardOrNull(0);
                        if (indexShard != null) {
                            DocumentMapper docMapper = indexInfo.indexService.mapperService().documentMapper(typeName);
                            if (logger.isDebugEnabled()) {
                                logger.debug("truncating from ks.cf={}.{} in elasticsearch index=[{}]", baseCfs.metadata.ksName, baseCfs.name, indexInfo.name);
                            }
                            if (!indexInfo.updated)
                                indexInfo.updated = true;
                            DeleteByQuery deleteByQuery = mappingInfoRef.get().buildDeleteByQuery(indexInfo.indexService, Queries.newMatchAllQuery());
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


    @Override
    public long getEstimatedResultRows() {
        return 0;
    }


    @Override
    public void validate(PartitionUpdate update) throws InvalidRequestException {
    }

    @Override
    public boolean dependsOn(ColumnDefinition column) {
        ImmutableMappingInfo mappingInfo = mappingInfoRef.get();
        return ES_QUERY_BYTE_BUFFER.equals(column.name.bytes) ||
            ES_OPTIONS_BYTE_BUFFER.equals(column.name.bytes) ||
            (mappingInfo != null && mappingInfo.fieldsToIdx.containsKey(column.name.toString()));
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

    @Override
    public Indexer indexerFor(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup, Type transactionType)
    {
        ImmutableMappingInfo mappingInfo = this.mappingInfoRef.get();
        if (isIndexing()) {
            if (transactionType == Type.COMPACTION && !mappingInfo.indexOnCompaction)
                return null;

            boolean found = (columns.size() == 0);
            if (!found) {
                for (ColumnDefinition cd : columns) {
                    if (mappingInfo.fieldsToIdx.containsKey(cd.name.toString())) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                try {
                    if (baseCfs.getComparator().size() == 0) {
                        switch (transactionType) {
                            case CLEANUP:
                                return mappingInfo.new CleanupSkinnyRowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
                            default:
                                return mappingInfo.new SkinnyRowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
                        }
                    } else {
                        switch (transactionType) {
                            case CLEANUP:
                                return mappingInfo.new CleanupWideRowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
                            default:
                                return mappingInfo.new WideRowcumentIndexer(key, columns, nowInSec, opGroup, transactionType);
                        }
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

}
