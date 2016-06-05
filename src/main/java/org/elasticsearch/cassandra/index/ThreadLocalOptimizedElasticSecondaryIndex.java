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
package org.elasticsearch.cassandra.index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.DeletionInfo;
import org.apache.cassandra.db.RangeTombstone;
import org.apache.cassandra.db.composites.CType;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.composites.CompoundSparseCellName;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CloseableThreadLocal;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.cassandra.cluster.InternalCassandraClusterService;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.lucene.all.AllEntries;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.MapperUtils;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.TypeFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper.Defaults;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesService;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * Custom secondary index for CQL3 only, should be created when mapping is applied and local shard started.
 * Index row as document when Elasticsearch clusterState has no write blocks and local shard is started.
 * 
 * OptmizedElasticSecondaryIndex directly build lucene fields without building a JSON document parsed by Elasticsearch.
 *  
 * ThreadLocalOptimizedElasticSecondaryIndex allocates contexts to build ES documents in a per thread cache for later reuse.
 * This approch may consume more memory if a keyspace is mapped to many indices (one context per index).
 * 
 * @author vroyer
 *
 */
public class ThreadLocalOptimizedElasticSecondaryIndex extends BaseElasticSecondaryIndex {
    private final static SourceToParse EMPTY_SOURCE_TO_PARSE= SourceToParse.source((XContentParser)null);

    // reusable per thread context pool
    private CloseableThreadLocal<ContextPool> perThreadContextPool = new CloseableThreadLocal<ContextPool>() {
        @Override
        protected ContextPool initialValue() {
            return new ContextPool();
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
    
    public class ContextPool {
        List<Context> contexts = new ArrayList<Context>(1);
        
        ContextPool() {
        }
        
        List<Context> init(MappingInfo mi, Uid uid) {
            int i=0;
            for(; i < Math.min(contexts.size(), mi.indices.size()); i++) {
                contexts.get(i).reset(mi.indices.get(i), uid);
            }
            boolean addContext = i < mi.indices.size();
            while (i < mi.indices.size()) {
                contexts.add( new Context(mi.indices.get(i), uid) );
                i++;
            }
            if (addContext) {
                logger.debug("contexts pool size={}", contexts.size());
            }
            return contexts;
        }
    }
    
    class Context extends ParseContext {
        private MappingInfo.IndexInfo indexInfo;
        private final ContentPath path;
        private DocumentMapper docMapper;
        private Document document;
        private StringBuilder stringBuilder = new StringBuilder();
        private String id;
        private Field version, uid;
        private final List<Document> documents = new ArrayList<Document>();
        private AllEntries allEntries = new AllEntries();
        private float docBoost = 1.0f;
        private Mapper dynamicMappingsUpdate = null;
        
        private boolean hasStaticField = false;
        private boolean finalized = false;
        
        public Context(MappingInfo.IndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.path = new ContentPath(0);
            this.docMapper = ii.indexService.mapperService().documentMapper(baseCfs.metadata.cfName);
            this.document = (baseCfs.metadata.hasStaticColumns()) ? new StaticDocument("",null, uid) : new Document();
            this.documents.add(this.document);
        }
        
        public void reset(MappingInfo.IndexInfo ii, Uid uid) {
            this.indexInfo = ii;
            this.docMapper = ii.indexService.mapperService().documentMapper(baseCfs.metadata.cfName);
            this.document = (baseCfs.metadata.hasStaticColumns()) ? new StaticDocument("",null, uid) : new Document();
            this.documents.clear();
            this.documents.add(this.document);
            this.id = null;
            this.path.reset();
            this.allEntries = new AllEntries();
            this.docBoost = 1.0f;
            this.dynamicMappingsUpdate = null;
        }
    
        // recusivelly add fields
        public void addField(Mapper mapper, Object value) throws IOException {
            if (value == null) return;
            if (value instanceof Collection) {
                // flatten list or set of fields
                for(Object v : (Collection)value) {
                    addField(mapper, v);
                }
                return;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("doc[{}] class={} name={} value={}", this.documents.indexOf(doc()), mapper.getClass().getSimpleName(), mapper.name(), value);
            }
            if (mapper instanceof FieldMapper) {
                ((FieldMapper)mapper).createField(this, value);
            } else if (mapper instanceof ObjectMapper) {
                final ObjectMapper objectMapper = (ObjectMapper)mapper;
                final ObjectMapper.Nested nested = objectMapper.nested();
                //see https://www.elastic.co/guide/en/elasticsearch/guide/current/nested-objects.html
                // code from DocumentParser.parseObject()
                if (nested.isNested()) {
                    beginNestedDocument(objectMapper.fullPath(),new Uid(baseCfs.metadata.cfName, id));
                    final ParseContext.Document nestedDoc = doc();
                    final ParseContext.Document parentDoc = nestedDoc.getParent();
                    // pre add the uid field if possible (id was already provided)
                    IndexableField uidField = parentDoc.getField(UidFieldMapper.NAME);
                    if (uidField != null) {
                        // we don't need to add it as a full uid field in nested docs, since we don't need versioning
                        // we also rely on this for UidField#loadVersion

                        // this is a deeply nested field
                        nestedDoc.add(new Field(UidFieldMapper.NAME, uidField.stringValue(), UidFieldMapper.Defaults.NESTED_FIELD_TYPE));
                    }
                    // the type of the nested doc starts with __, so we can identify that its a nested one in filters
                    // note, we don't prefix it with the type of the doc since it allows us to execute a nested query
                    // across types (for example, with similar nested objects)
                    nestedDoc.add(new Field(TypeFieldMapper.NAME, objectMapper.nestedTypePathAsString(), TypeFieldMapper.Defaults.FIELD_TYPE));
                }

                ContentPath.Type origPathType = path().pathType();
                path().pathType(objectMapper.pathType());

                for(Entry<String,Object> entry : ((Map<String,Object>)value).entrySet()) {
                    Mapper subMapper = objectMapper.getMapper(entry.getKey());
                    if (subMapper != null) {
                        addField(subMapper, entry.getValue());
                    } else {
                        // dynamic field in top level map => update mapping and add the field.
                        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(new ColumnIdentifier(mapper.name(), true));
                        if (cd != null && cd.type.isCollection() && cd.type instanceof MapType) {
                            logger.debug("updating mapping for field={} type={} value={} ", entry.getKey(), cd.type.toString(), value);
                            CollectionType ctype = (CollectionType) cd.type;
                            if (ctype.kind == CollectionType.Kind.MAP && ((MapType)ctype).getKeysType().asCQL3Type().toString().equals("text")) {
                                try {
                                    final String valueType = InternalCassandraClusterService.cqlMapping.get(((MapType)ctype).getValuesType().asCQL3Type().toString());
                                    // build a mapping update
                                    Map<String,Object> objectMapping = (Map<String,Object>) ((Map<String,Object>)indexInfo.mapping.get("properties")).get(mapper.name());
                                    XContentBuilder builder = XContentFactory.jsonBuilder()
                                            .startObject()
                                            .startObject(baseCfs.metadata.cfName)
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
                                    
                                    getClusterService().blockingMappingUpdate(indexInfo.indexService, baseCfs.metadata.cfName, new CompressedXContent(mappingUpdate) );
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
            return null;
        }
    
        @Override
        public void source(BytesReference source) {
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
                MapperUtils.merge(dynamicMappingsUpdate, mapper);
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
            final boolean refresh;
            final IndexService indexService;
            Map<String,Object> mapping;
            
            public IndexInfo(String name, IndexService indexService, MappingMetaData mappingMetaData) throws IOException {
                this.name = name;
                this.indexService = indexService;
                this.mapping = mappingMetaData.sourceAsMap();
                this.refresh = false;
            }

            public int indexOf(String f) {
                return MappingInfo.this.indexOf(f);
            }
            
            public boolean isStaticField(int idx) {
                return MappingInfo.this.fieldsIsStatic.get(idx);
            }
        }

        final List<IndexInfo> indices = new ArrayList<IndexInfo>();
        final String[] fields;
        final BitSet fieldsToRead;
        final BitSet fieldsIsStatic;
        final boolean[] indexedPkColumns;
        
        MappingInfo(final ClusterState state) {
            if (state.blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                logger.debug("global write blocked");
                this.fields = null;
                this.fieldsToRead = null;
                this.fieldsIsStatic = null;
                this.indexedPkColumns = null;
                return;
            }
            
            Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();
            for(Iterator<IndexMetaData> indexMetaDataIterator = state.metaData().iterator(); indexMetaDataIterator.hasNext(); ) {
                IndexMetaData indexMetaData = indexMetaDataIterator.next();
                String index = indexMetaData.getIndex();
                MappingMetaData mappingMetaData; 
                ClusterBlockException clusterBlockException = state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, index);
                if (clusterBlockException != null) {
                    logger.debug("ignore, index=[{}] blocked blocks={}", index, clusterBlockException.blocks());
                    continue;
                }
                if (!state.routingTable().isLocalShardsStarted(index)) {
                    logger.debug("ignore, local shard not started for index=[{}]", index);
                    continue;
                }
                
                if ( (ThreadLocalOptimizedElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(index) || ThreadLocalOptimizedElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.keyspace())) &&
                     ((mappingMetaData = indexMetaData.mapping(ThreadLocalOptimizedElasticSecondaryIndex.this.baseCfs.metadata.cfName)) != null)
                   ) {
                    try {
                        IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
                        IndexService indexService = indicesService.indexServiceSafe(index);
                        IndexInfo indexInfo = new IndexInfo(index, indexService, mappingMetaData);
                        this.indices.add(indexInfo);
                        if (mappingMetaData.getSourceAsMap().get("properties") != null) {
                            Map<String,Object> props = (Map<String,Object>)mappingMetaData.getSourceAsMap().get("properties");
                            for(String fieldName : props.keySet() ) {
                                Map<String,Object> fieldMap = (Map<String,Object>)props.get(fieldName);
                                boolean partialUpdate = (fieldMap.get(TypeParsers.CQL_PARTIAL_UPDATE) == null || (Boolean)fieldMap.get(TypeParsers.CQL_PARTIAL_UPDATE));
                                if (fieldsMap.get(fieldName) != null) {
                                    partialUpdate = partialUpdate || fieldsMap.get(fieldName);
                                }
                                fieldsMap.put(fieldName, partialUpdate);
                            }
                        }
                        if (mappingMetaData.hasParentField()) {
                            fieldsMap.put(ParentFieldMapper.NAME,true);
                        }
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
                }
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
            
            this.fieldsIsStatic = (baseCfs.metadata.hasStaticColumns()) ? new BitSet() : null;
            for(int i=0; i < fields.length; i++) {
                this.fieldsToRead.set(i, fieldsMap.get(fields[i]));
                if (baseCfs.metadata.hasStaticColumns()) {
                    this.fieldsIsStatic.set(i,baseCfs.metadata.getColumnDefinition(new ColumnIdentifier(fields[i],true)).isStatic());
                }
            }
        }
        
        public int indexOf(String f) {
            for(int i=0; i < this.fields.length; i++) {
                if (this.fields[i].equals(f)) return i;
            }
            return -1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(IndexInfo i : indices) {
                if (sb.length() > 0) sb.append(',');
                sb.append(i.name).append('=').append(i.mapping.toString());
            }
            return sb.toString();
        }
        
        class RowcumentFactory  {
            final ByteBuffer rowKey;
            final ColumnFamily cf;
            final Long token;
            final ArrayNode an;
            final Object[] pkCols = new Object[baseCfs.metadata.partitionKeyColumns().size()+baseCfs.metadata.clusteringColumns().size()];
            final String partitionKey;
            final Uid uid;
            final List<Context> contexts;
            
            public RowcumentFactory(final ByteBuffer rowKey, final ColumnFamily cf, ContextPool contextPool) throws JsonGenerationException, JsonMappingException, IOException {
                this.rowKey = rowKey;
                this.cf = cf;
                this.token = (Long) partitioner.getToken(rowKey).getTokenValue();   // Cassandra Token value (Murmur3 partitionner only)
                this.an = ClusterService.Utils.jsonMapper.createArrayNode();
                
                CType ctype = baseCfs.metadata.getKeyValidatorAsCType();
                Composite composite = ctype.fromByteBuffer(rowKey);
                for(int i=0; i<composite.size(); i++) {
                    ByteBuffer bb = composite.get(i);
                    AbstractType<?> type = ctype.subtype(i);
                    pkCols[i] = type.compose(bb);
                    ClusterService.Utils.addToJsonArray(type, pkCols[i], an);
                }
                this.partitionKey = ClusterService.Utils.writeValueAsString(an);  // JSON string  of the partition key.
                this.uid = (baseCfs.metadata.hasStaticColumns()) ? new Uid(baseCfs.metadata.cfName, this.partitionKey) : null;
                this.contexts = contextPool.init(MappingInfo.this, uid);
            }
            
           
            public void index(Iterator<Cell> cellIterator) throws IOException {
                Rowcument doc = new Rowcument(cellIterator.next());
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    if (baseCfs.metadata.getColumnDefinition(cellName) == null && cellName.clusteringSize() > 0)  {
                        doc.flush();
                        // reset contexts fo next rowcument.
                        int i=0;
                        for(MappingInfo.IndexInfo ii : MappingInfo.this.indices) {
                            contexts.get(i).reset(ii, uid);
                            i++;
                        }
                        doc = new Rowcument(cell);
                    } else {
                        doc.readCellValue(cell);
                    }
                }
                doc.flush();
            }
                
            
            /**
             * Delete tombstones in ES index.
             * @throws IOException
             */
            public void prune() throws IOException {
                DeletionInfo deletionInfo = cf.deletionInfo();
                if (!deletionInfo.isLive()) {
                    if (deletionInfo.hasRanges()) {
                        Iterator<RangeTombstone> it = deletionInfo.rangeIterator();
                        while (it.hasNext()) {
                            RangeTombstone rangeTombstone = it.next();
                            if (logger.isTraceEnabled())
                                logger.trace("delete rangeTombstone (not implemented) " + getIndexName() + " cf=" + baseCfs.metadata.ksName + "." + baseCfs.metadata.cfName + " min="+rangeTombstone.min+" max="+rangeTombstone.max);
                        }
                    } else {
                        delete();
                    }
                }
            }
            
            public void delete() {
                logger.warn("delete row not implemented");
            }
            
            

            
            class Rowcument {
                final Object[] values = new Object[fields.length];
                final BitSet fieldsNotNull = new BitSet();
                final BitSet tombstoneColumns = new BitSet();
                String id = null;
                final boolean wideRow;
                final boolean hasMissingClusteringKeys;
                boolean hasStaticUpdate = false;
                int     docTtl = Integer.MAX_VALUE;
                
                // init document with clustering columns stored in cellName, or cell value for non-clustered columns (regular with no clustering key or static columns).
                public Rowcument(Cell cell) throws IOException {
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    
                    for(int i=0; i < MappingInfo.this.indices.size(); i++) {
                        Context context = contexts.get(i);
                        for (MetadataFieldMapper metadataMapper : context.docMapper.mapping().metadataMappers()) {
                            metadataMapper.preCreate(context);
                        }
                        context.docMapper.typeMapper().createField(context, baseCfs.metadata.cfName);
                        context.docMapper.tokenFieldMapper().createField(context, token);
                        context.docMapper.routingFieldMapper().createField(context, partitionKey);
                        context.docMapper.typeMapper().createField(context, baseCfs.metadata.cfName);
                        context.version(DEFAULT_VERSION);
                        context.doc().add(DEFAULT_VERSION);
                    }
                    
                    // copy the indexed columns of partition key in values
                    int x = 0;
                    for(int i=0 ; i < baseCfs.metadata.partitionKeyColumns().size(); i++) {
                        if (indexedPkColumns[i]) {
                            values[x++] = pkCols[i];
                        }
                    }
                    // copy the indexed columns of clustering key in values
                    if (cellName.clusteringSize() > 0 && baseCfs.metadata.getColumnDefinition(cell.name()) == null)  {
                        // add clustering keys to docMap and _id
                        ArrayNode an2 = ClusterService.Utils.jsonMapper.createArrayNode();
                        an2.addAll(an);
                        wideRow = true;
                        int i=0;
                        for(ColumnDefinition ccd : baseCfs.metadata.clusteringColumns()) {
                            Object value = deserialize(ccd.type, cellName.get(i));
                            pkCols[baseCfs.metadata.partitionKeyColumns().size()+i] = value;
                            if (indexedPkColumns[baseCfs.metadata.partitionKeyColumns().size()+i]) {
                                values[x++] = value;
                                ClusterService.Utils.addToJsonArray(ccd.type, value, an2);
                            }
                            i++;
                        }
                        id = ClusterService.Utils.writeValueAsString(an2);
                    } else {
                        wideRow = false;
                        id = partitionKey;
                        readCellValue(cell);
                    }
                    Uid uid = new Uid(baseCfs.metadata.cfName, id);
                    for(int i=0; i < MappingInfo.this.indices.size(); i++) {
                        Context context = contexts.get(i);
                        context.docMapper.idFieldMapper().createField(context, id);
                        context.docMapper.uidMapper().createField(context, uid);
                    }
                    hasMissingClusteringKeys = baseCfs.metadata.clusteringColumns().size() > 0 && !wideRow;
                }
               
                // for each ES index, add top level field if mapped.
                public void addField(int idx) throws IOException {
                    int j = 0;
                    for(int i=0; i < MappingInfo.this.indices.size(); i++) {
                        final Context context = contexts.get(i);
                        Mapper mapper = context.docMapper.mappers().smartNameFieldMapper(fields[idx]);
                        if (mapper == null) {
                            mapper = context.docMapper.objectMappers().get(fields[idx]);
                        }
                        if (mapper != null) {
                            if (fieldsIsStatic != null && fieldsIsStatic.get(idx)) {
                                context.setStaticField(true);
                            }
                            context.addField(mapper, values[idx]);
                        }
                    }
                }

                public void readCellValue(Cell cell) throws IOException {
                    final CellName cellName = cell.name();
                    final String cellNameString = cellName.cql3ColumnName(baseCfs.metadata).toString();
                    int idx  = indexOf(cellNameString);
                    if (idx == - 1) {
                        //ignore cell, (probably clustered keys in cellnames only) 
                        return;
                    }
                    ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(cell.name());
                    if (cell.isLive() && idx >= 0) {
                        docTtl = Math.min(cell.getLocalDeletionTime(), docTtl);
                        
                        if (cd.kind == ColumnDefinition.Kind.STATIC) {
                            hasStaticUpdate = true;
                        }
                        if (cd.type.isCollection()) {
                            CollectionType ctype = (CollectionType) cd.type;
                            Object value = null;
                  
                            switch (ctype.kind) {
                            case LIST: 
                                value = deserialize(((ListType)cd.type).getElementsType(), cell.value() );
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
                                value = deserialize(((SetType)cd.type).getElementsType(), cell.value() );
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
                                value = deserialize(((MapType)cd.type).getValuesType(), cell.value() );
                                Object key = deserialize(((MapType)cd.type).getKeysType(), cellName.get(cellName.size()-1));
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
                            Object value = deserialize(cd.type, cell.value() );
                            if (logger.isTraceEnabled()) 
                                logger.trace("name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                            values[idx] = value;
                            fieldsNotNull.set(idx, value != null);
                        }
                    } else {
                        // tombstone => black list this column for later document.complete().
                        addTombstoneColumn(cellNameString);
                    }
                }
                
                public void addTombstoneColumn(String cql3name) {
                    int idx = indexOf(cql3name);
                    if (idx >= 0) {
                        tombstoneColumns.set(idx);
                    }
                }
                
                // return true if at least one field in one mapping is updated.
                public boolean complete() {
                    // add missing or collection columns that should be read before indexing the document.
                    // read missing static columns (with limit 1) or regular columns if  
                    final BitSet mustUpdateFields = (BitSet)fieldsToRead.clone();
                    mustUpdateFields.andNot(fieldsNotNull);
                    mustUpdateFields.andNot(tombstoneColumns);
                    if (mustUpdateFields.cardinality() > 0) {
                        final String[] mustReadColumns = new String[mustUpdateFields.cardinality()];
                        final int[]    mustReadColumnsPosition = new int[mustUpdateFields.cardinality()];
                        int x = 0;
                        for(int i=0; i < fields.length; i++) {
                            if (fieldsIsStatic != null && fieldsIsStatic.get(i)) {
                                if (!this.hasStaticUpdate) {
                                    // ignore static columns, we got only regular columns.
                                    continue;
                                }
                            } else {
                                if (this.hasMissingClusteringKeys) {
                                    // ignore regular columns, we are updating static one.
                                    continue;
                                }
                            }
                            if (mustUpdateFields.get(i) && !tombstoneColumns.get(i)) {
                                if (values[i] == null || values[i] instanceof Set || values[i] instanceof Map) {
                                    // List must be fully updated, but set or map can be partially updated without having duplicate entry. 
                                    mustReadColumns[x] = fields[i];
                                    mustReadColumnsPosition[x] = i;
                                    x++;
                                }
                            }
                        }
                        if (x > 0)  {
                            final String[] missingColumns = new String[x];
                            System.arraycopy(mustReadColumns, 0, missingColumns, 0, x);
                            Object[] pk = pkCols;
                            if (hasMissingClusteringKeys) {
                                pk = new Object[baseCfs.metadata.partitionKeyColumns().size()];
                                System.arraycopy(pkCols, 0, pk, 0, baseCfs.metadata.partitionKeyColumns().size());
                            }
                            try {
                                // fetch missing fields from the local cassandra row to update Elasticsearch index
                                if (logger.isTraceEnabled()) {
                                    logger.trace(" {}.{} id={} missing columns names={} hasMissingClusteringKeys={}",baseCfs.metadata.ksName, baseCfs.metadata.cfName, id, missingColumns, hasMissingClusteringKeys);
                                }
                                UntypedResultSet results = getClusterService().fetchRowInternal(baseCfs.metadata.ksName, baseCfs.metadata.cfName, missingColumns, pk, hasStaticUpdate);
                                if (!results.isEmpty()) {
                                    Object[] missingValues = getClusterService().rowAsArray(baseCfs.metadata.ksName, baseCfs.metadata.cfName, results.one(), false);
                                    for(int i=0; i < x; i++) {
                                        values[ mustReadColumnsPosition[i] ] = missingValues[i];
                                    }
                                } 
                            } catch (RequestValidationException | IOException e) {
                                logger.error("Failed to fetch columns {}",missingColumns,e);
                            }
                        }
                    }
                    
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}.{} id={} fields={} values={}", baseCfs.metadata.ksName, baseCfs.metadata.cfName, id, Arrays.toString(values));
                    }
                    // add all fields to contexts.
                    for(int i=0; i < values.length; i++) {
                        if (values[i] != null) {
                            try {
                                addField(i);
                            } catch (IOException e) {
                                logger.error("error", e);
                            }
                        }
                    }
                    
                    // postCreate for all metadata fields.
                    for(Context context : contexts) {
                        Mapping mapping = context.docMapper.mapping();
                        for (MetadataFieldMapper metadataMapper : mapping.metadataMappers()) {
                            try {
                                metadataMapper.postCreate(context);
                            } catch (IOException e) {
                               logger.error("error", e);
                            }
                        }
                    }
                    return fieldsNotNull.cardinality() > 0;
                }
                
                
                public void index() {
                    if (!this.hasMissingClusteringKeys) {
                        // index regular row
                        index(false);
                    }
                    if (this.hasStaticUpdate) {
                        // index static document
                        index(true);
                    }
                }
                
                public void index(boolean staticDocumentOnly) {
                    long startTime = System.nanoTime();
                    long ttl = (long)((this.docTtl < Integer.MAX_VALUE) ? this.docTtl : 0);
                    
                    for(int i=0; i < MappingInfo.this.indices.size(); i++) {
                        final Context context = contexts.get(i);
                        if (staticDocumentOnly && !context.hasStaticField()) continue;
                        
                        Field uid = context.uid();
                        if (staticDocumentOnly) {
                            uid = new Field(UidFieldMapper.NAME, Uid.createUid(baseCfs.metadata.cfName, partitionKey), Defaults.FIELD_TYPE);
                            for(Document doc : context.docs()) {
                                if (doc instanceof Context.StaticDocument) {
                                    ((Context.StaticDocument)doc).applyFilter(staticDocumentOnly);
                                }
                            }
                            
                        }
                        context.finalize();
                        final ParsedDocument parsedDoc = new ParsedDocument(
                                uid, 
                                context.version(), 
                                (staticDocumentOnly) ? partitionKey : context.id(), 
                                context.type(), 
                                partitionKey, // routing
                                System.currentTimeMillis(), // timstamp
                                ttl,
                                token.longValue(), 
                                context.docs(), 
                                (BytesReference)null, // source 
                                (Mapping)null); // mappingUpdate
                        
                        
                        // TODO: could be a part of the primary key (not only the partition key)
                        if ( indexOf(ParentFieldMapper.NAME) == -1) {
                            try {
                                context.docMapper.parentFieldMapper().createField(context, partitionKey);
                            } catch (IOException e) {
                                logger.error("Failed to add _parent id",e);
                            }
                        }
                        
                        if (logger.isTraceEnabled()) {
                            logger.trace("parsedDoc id={} type={} uid={} routing={} docs={}", parsedDoc.id(), parsedDoc.type(), parsedDoc.uid(), parsedDoc.routing(), parsedDoc.docs());
                        }
                        final IndexShard indexShard = context.indexInfo.indexService.shardSafe(0);
                        final Engine.Index operation = new Engine.Index(context.docMapper.uidMapper().term(uid.stringValue()), 
                                parsedDoc, 
                                Versions.MATCH_ANY, 
                                VersionType.INTERNAL, 
                                Engine.Operation.Origin.PRIMARY, 
                                startTime, 
                                false);
                        
                        final boolean created = operation.execute(indexShard);
                        final long version = operation.version();

                        if (context.indexInfo.refresh) {
                            try {
                                indexShard.refresh("refresh_flag_index");
                            } catch (Throwable e) {
                                logger.error("error", e);
                            }
                        }
                        
                        if (logger.isDebugEnabled()) {
                            logger.debug("document CF={}.{} index={} type={} id={} version={} created={} ttl={} refresh={} parent={} ", 
                                baseCfs.metadata.ksName, baseCfs.metadata.cfName,
                                context.indexInfo.name, baseCfs.metadata.cfName, 
                                id, version, created, ttl, context.indexInfo.refresh, (context.docMapper.parentFieldMapper().active()) ? partitionKey :"", parsedDoc.toString());
                        }
                    }
                }
                
                public void delete() {
                    for (MappingInfo.IndexInfo indexInfo : indices) {
                        logger.debug("deleting document from index.type={}.{} id={}", indexInfo.name, baseCfs.metadata.cfName, id);
                        IndexShard indexShard = indexInfo.indexService.shardSafe(0);
                        Engine.Delete delete = indexShard.prepareDelete(baseCfs.metadata.cfName, id, Versions.MATCH_ANY, VersionType.INTERNAL, Engine.Operation.Origin.PRIMARY);
                        indexShard.delete(delete);
                    }
                }
                
                
                public void flush() throws JsonGenerationException, JsonMappingException, IOException {
                    if (complete()) {
                        index();
                    } else {
                        delete();
                    }
                }
            }

        }
    }

    

 
    // updated when create/open/close/remove an ES index.
    private AtomicReference<MappingInfo> mappingAtomicReference = new AtomicReference();

    
    public ThreadLocalOptimizedElasticSecondaryIndex() {
        super();
    }

    
    
    
    /**
     * Index a mutation. Set empty field for deleted cells.
     */
    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf)  {
        try {
            final MappingInfo mappingInfo = this.mappingAtomicReference.get();
            if (mappingInfo == null) {
                if (logger.isTraceEnabled())  logger.trace("No Elasticsearch index ready");
                return;
            }
            if (mappingInfo.indices.size() == 0) {
                if (logger.isTraceEnabled())  logger.trace("No Elasticsearch index configured");
                return;
            }

            {
                // block for stack allocation
                final MappingInfo.RowcumentFactory docFactory = mappingInfo.new RowcumentFactory(rowKey, cf, perThreadContextPool.get());
                final Iterator<Cell> cellIterator = cf.iterator();
                if (cellIterator.hasNext()) {
                    docFactory.index(cellIterator);
                } else {
                    docFactory.prune();
                }
            }
        } catch (Throwable e) {
            logger.error("error:", e);
        }
    }

    

    /**
     * cleans up deleted columns from cassandra cleanup compaction
     *
     * @param key
     */
    @Override
    public void delete(DecoratedKey key, Group opGroup) {
        MappingInfo mappingInfo = this.mappingAtomicReference.get();
        if (mappingInfo == null || mappingInfo.indices.size() == 0) {
            // TODO: save the update in a commit log to replay it later....
            logger.warn("Elastic node not ready, cannot delete document");
            return;
        }
        
        Token token = key.getToken();
        Long  token_long = (Long) token.getTokenValue();
        logger.debug("deleting (not imlemented) document with _token = " + token_long);
        
        // TODO: DeleteByQuery or Scan+Bulk Delete.
        /*
        for (Pair<String, String> target : targets.keySet()) {
            logger.debug("xdeleting document from index={} type={} id={}", target.left, target.right);
            // submit a delete request
            XDeleteRequest request = new XDeleteRequest(target.left, target.right, );
            ActionListener<XDeleteResponse> listener = new ActionListener<XDeleteResponse>() {
                @Override
                public void onResponse(XDeleteResponse response) {
                    logger.debug("doc deleted id=" + response.getId());
                }

                @Override
                public void onFailure(Throwable e) {
                    logger.error("failed to delete doc id=" + id, e);
                }
            };
            ElassandraDaemon.client().xdelete(request, listener);
        }
        */
    }

    
    public synchronized void initMapping() {
        if (ElassandraDaemon.injector() != null) {
           getClusterService().addLast(this);
            this.mappingAtomicReference.set(new MappingInfo(getClusterService().state()));
            logger.debug("index=[{}.{}] initialized,  mappingAtomicReference = {}", this.baseCfs.metadata.ksName, index_name, this.mappingAtomicReference.get());
        } else {
            logger.warn("Cannot initialize index=[{}.{}], cluster service not available.", this.baseCfs.metadata.ksName, index_name);
        }
    }
    
    

    /**
     * Cassandra index flush => Elasticsearch flush => lucene commit and disk
     * sync.
     */
    @Override
    public void forceBlockingFlush() {
        lock.writeLock().lock();
        try {
            MappingInfo mappingInfo = this.mappingAtomicReference.get();
            if (mappingInfo == null || mappingInfo.indices.size() == 0) {
                logger.warn("Elasticsearch not ready, cannot flush Elasticsearch index");
                return;
            }
            for(MappingInfo.IndexInfo indexInfo : mappingInfo.indices) {
                try {
                    IndexShard indexShard = indexInfo.indexService.shardSafe(0);
                    logger.debug("Flushing Elasticsearch index=[{}] state=[{}]",indexInfo.name, indexShard.state());
                    if (indexShard.state() == IndexShardState.STARTED)  {
                        indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                        logger.debug("Elasticsearch index=[{}] flushed",indexInfo.name);
                    } else {
                        logger.warn("Cannot flush index=[{}], state=[{}]",indexInfo.name, indexShard.state());
                    }
                } catch (ElasticsearchException e) {
                    logger.error("Unexpected error",e);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    

    
    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        lock.writeLock().lock();
        try {
            boolean updateMapping = false;
            if (event.blocksChanged()) {
                updateMapping = true;
            } else {
                for (ObjectCursor<IndexMetaData> cursor : event.state().metaData().indices().values()) {
                    IndexMetaData indexMetaData = cursor.value;
                    String indexName = indexMetaData.getIndex();
                    if ((indexName.equals(this.baseCfs.metadata.ksName)) || 
                         indexName.equals(indexMetaData.getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME)) &&
                        (event.indexRoutingTableChanged(indexMetaData.getIndex()) || 
                         event.indexMetaDataChanged(indexMetaData))) {
                            updateMapping = true;
                            break;
                        }
                }
            }
            if (updateMapping) {
                if (logger.isTraceEnabled()) logger.trace("state = {}", event.state());
                this.mappingAtomicReference.set(new MappingInfo(event.state()));
                logger.debug("index=[{}.{}] new mappingInfo = {}",this.baseCfs.metadata.ksName, this.index_name, this.mappingAtomicReference.get() );
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    

}
