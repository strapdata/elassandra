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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.DeletionInfo;
import org.apache.cassandra.db.RangeTombstone;
import org.apache.cassandra.db.composites.CType;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.composites.CompoundSparseCellName;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.DecimalType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.Token;
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
import com.ibm.icu.text.MessageFormat;

/**
 * Custom secondary index for CQL3 only, should be created when mapping is applied and local shard started.
 * Index rows as documents when Elasticsearch clusterState has no write blocks and local shard is started.
 * 
 * ExtendedElasticSecondaryIndex directly build lucene fields without building a JSON document parsed by Elasticsearch.
 * 
 * @author vroyer
 *
 */
public class ExtendedElasticSecondaryIndex extends BaseElasticSecondaryIndex {
    private final static SourceToParse EMPTY_SOURCE_TO_PARSE= SourceToParse.source((XContentParser)null);

    // reusable per thread context pool
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
        
        public Context() {
        }
        
        public Context(MappingInfo.IndexInfo ii, Uid uid) {
            this.indexInfo = ii;
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
            this.parent = null;
            this.version = BaseElasticSecondaryIndex.DEFAULT_VERSION;
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


        @Override
        public void ignoredValue(String indexName, String value) {
            // TODO Auto-generated method stub
        }

        @Override
        public String ignoredValue(String indexName) {
            // TODO Auto-generated method stub
            return null;
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

        class PartitionFunction {
            String name;
            String pattern;
            String[] fields;
            int[]  fieldsIndex;
            
            public PartitionFunction(String[] args) {
                this.name = args[0];
                this.pattern = args[1];
                this.fields = new String[args.length-2];
                this.fieldsIndex = new int[args.length-2];
                System.arraycopy(args, 2, this.fields, 0, args.length-2);
            }
            
            public String indexName(Object[] values) {
                Object[] args = new Object[fields.length];
                int j=0;
                for(int i : fieldsIndex) {
                    args[j++] = values[i];
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
        final BitSet fieldsToRead;
        final BitSet fieldsIsStatic;
        final boolean[] indexedPkColumns;
        final long metadataVersion;
        
        MappingInfo(final ClusterState state) {
            this.metadataVersion = state.metaData().version();
            if (state.blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                logger.debug("global write blocked");
                this.fields = null;
                this.fieldsToRead = null;
                this.fieldsIsStatic = null;
                this.indexedPkColumns = null;
                this.partitionFunctions = null;
                return;
            }
            
            Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();
            Map<String, PartitionFunction> partFuncs = new HashMap<String, PartitionFunction>();
            for(Iterator<IndexMetaData> indexMetaDataIterator = state.metaData().iterator(); indexMetaDataIterator.hasNext(); ) {
                IndexMetaData indexMetaData = indexMetaDataIterator.next();
                String index = indexMetaData.getIndex();
                MappingMetaData mappingMetaData; 
                ClusterBlockException clusterBlockException = state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, index);
                if (clusterBlockException != null) {
                    logger.warn("ignore, index=[{}] blocked blocks={}", index, clusterBlockException.blocks());
                    continue;
                }

                if ( ExtendedElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.keyspace()) &&
                     (mappingMetaData = indexMetaData.mapping(ExtendedElasticSecondaryIndex.this.baseCfs.metadata.cfName)) != null) {
                    try {
                        Map<String,Object> mappingMap = (Map<String,Object>)mappingMetaData.getSourceAsMap();
                        if (mappingMap.get("properties") != null) {
                            IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
                            IndexService indexService = indicesService.indexServiceSafe(index);
                            IndexInfo indexInfo = new IndexInfo(index, indexService, mappingMetaData);
                            this.indices.put(index, indexInfo);
                            
                            Map<String,Object> props = (Map<String,Object>)mappingMap.get("properties");
                            for(String fieldName : props.keySet() ) {
                                Map<String,Object> fieldMap = (Map<String,Object>)props.get(fieldName);
                                boolean mandartory = (fieldMap.get(TypeParsers.CQL_MANDATORY) == null || (Boolean)fieldMap.get(TypeParsers.CQL_MANDATORY));
                                if (fieldsMap.get(fieldName) != null) {
                                    mandartory = mandartory || fieldsMap.get(fieldName);
                                }
                                fieldsMap.put(fieldName, mandartory);
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
                            
                            String[] partitionFunction = indexMetaData.partitionFunction();
                            if (partitionFunction != null) {
                                PartitionFunction func = new PartitionFunction(partitionFunction);
                                partFuncs.put(func.name, func);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Unexpected error index=[{}]", e, index);
                    }
                }
            }
            
            if (indices.size() == 0) {
                if (logger.isTraceEnabled())
                    logger.warn("no active elasticsearch index for keyspace.table {}.{}",baseCfs.metadata.ksName, baseCfs.name);
                this.fields = null;
                this.fieldsToRead = null;
                this.fieldsIsStatic = null;
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
            this.fieldsIsStatic = (baseCfs.metadata.hasStaticColumns()) ? new BitSet() : null;
            for(int i=0; i < fields.length; i++) {
                this.fieldsToRead.set(i, fieldsMap.get(fields[i]));
                if (baseCfs.metadata.hasStaticColumns()) {
                    this.fieldsIsStatic.set(i,baseCfs.metadata.getColumnDefinition(new ColumnIdentifier(fields[i],true)).isStatic());
                }
            }
            
            if (partFuncs.size() > 0) {
                for(PartitionFunction shunt : partFuncs.values()) {
                    int i = 0;
                    for(String field : shunt.fields) {
                        shunt.fieldsIndex[i++] = indexOf(field);
                    }
                }
                this.partitionFunctions = partFuncs;
            } else {
                this.partitionFunctions = null;
            }
        }
        
        public Collection<IndexInfo> targetIndices(Object[] values) {
            if (this.partitionFunctions == null)
                return this.indices.values();
            
            List<IndexInfo> targetIndices = new ArrayList<IndexInfo>(this.partitionFunctions.size());
            for(PartitionFunction func : this.partitionFunctions.values()) {
                String indexName = func.indexName(values);
                if (this.indices.get(indexName) != null) {
                    targetIndices.add( this.indices.get(indexName) );
                } else {
                    if (logger.isWarnEnabled())
                        logger.warn("No target for index=[{}] partition function name=[{}] pattern=[{}] indices={}", indexName, func.name, func.pattern, this.indices);
                }
            }
            if (logger.isTraceEnabled()) 
                logger.trace("Partition target indices={}", targetIndices.stream().map( e -> e.name ).collect( Collectors.toList() ));
            return targetIndices;
        }
        
        public int indexOf(String f) {
            for(int i=0; i < this.fields.length; i++) {
                if (this.fields[i].equals(f)) return i;
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
        
        class RowcumentFactory  {
            final ByteBuffer rowKey;
            final ColumnFamily cf;
            final Long token;
            final ArrayNode an;
            final Object[] pkCols = new Object[baseCfs.metadata.partitionKeyColumns().size()+baseCfs.metadata.clusteringColumns().size()];
            final String partitionKey;
            
            public RowcumentFactory(final ByteBuffer rowKey, final ColumnFamily cf) throws JsonGenerationException, JsonMappingException, IOException {
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
            }
            
           
            public void index(Iterator<Cell> cellIterator) throws IOException {
                Rowcument doc = new Rowcument(cellIterator.next());
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    if (baseCfs.metadata.getColumnDefinition(cellName) == null && cellName.clusteringSize() > 0)  {
                        doc.flush();
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
                Uid uid;
                
                // init document with clustering columns stored in cellName, or cell value for non-clustered columns (regular with no clustering key or static columns).
                public Rowcument(Cell cell) throws IOException {
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    
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
                            Object value = InternalCassandraClusterService.deserialize(ccd.type, cellName.get(i));
                            pkCols[baseCfs.metadata.partitionKeyColumns().size()+i] = value;
                            if (indexedPkColumns[baseCfs.metadata.partitionKeyColumns().size()+i]) {
                                values[x++] = value;
                            }
                            ClusterService.Utils.addToJsonArray(ccd.type, value, an2);
                            i++;
                        }
                        id = ClusterService.Utils.writeValueAsString(an2);
                    } else {
                        wideRow = false;
                        id = partitionKey;
                        readCellValue(cell);
                    }
                    uid = new Uid(baseCfs.metadata.cfName, id);
                    hasMissingClusteringKeys = baseCfs.metadata.clusteringColumns().size() > 0 && !wideRow;
                }
               
                public void readCellValue(Cell cell) throws IOException {
                    final CellName cellName = cell.name();
                    final String cellNameString = cellName.cql3ColumnName(baseCfs.metadata).toString();
                    int idx  = indexOf(cellNameString);
                    if (idx == - 1) {
                        //ignore cell, (probably clustered keys in cellnames only) 
                        return;
                    }
                    if (cell.isLive() && idx >= 0) {
                        docTtl = Math.min(cell.getLocalDeletionTime(), docTtl);
                        
                        ColumnDefinition cd = baseCfs.metadata.getColumnDefinition(cell.name());
                        if (cd.kind == ColumnDefinition.Kind.STATIC) {
                            hasStaticUpdate = true;
                        }
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
                                Object key = InternalCassandraClusterService.deserialize(((MapType)cd.type).getKeysType(), cellName.get(cellName.size()-1));
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
                        addTombstoneColumn(cellNameString);
                    }
                }
                
                public Object[] rowAsArray(UntypedResultSet.Row row) throws IOException {
                    final Object values[] = new Object[row.getColumns().size()];
                    final List<ColumnSpecification> columnSpecs = row.getColumns();
                    
                    for (int idx = 0; idx < columnSpecs.size(); idx++) {
                        if (!row.has(idx) || ByteBufferUtil.EMPTY_BYTE_BUFFER.equals(row.getBlob(idx)) ) {
                            values[idx] = null;
                            continue;
                        }
                        
                        ColumnSpecification colSpec = columnSpecs.get(idx);
                        String columnName = colSpec.name.toString();
                        CQL3Type cql3Type = colSpec.type.asCQL3Type();
                        
                        if (cql3Type instanceof CQL3Type.Native) {
                            switch ((CQL3Type.Native) cql3Type) {
                            case ASCII:
                            case TEXT:
                            case VARCHAR:
                                values[idx] = row.getString(idx);
                                break;
                            case TIMEUUID:
                            case UUID:
                                values[idx] = row.getUUID(idx).toString();
                                break;
                            case TIMESTAMP:
                                values[idx] = row.getTimestamp(idx).getTime();
                                break;
                            case INT:
                                values[idx] = row.getInt(idx);
                                break;
                            case SMALLINT:
                                values[idx] = row.getShort(idx);
                                break;
                            case TINYINT:
                                values[idx] = row.getByte(idx);
                                break;
                            case BIGINT:
                                values[idx] = row.getLong(idx);;
                                break;
                            case DOUBLE:
                                values[idx] = row.getDouble(idx);
                                break;
                            case DECIMAL:
                                values[idx] = DecimalType.instance.compose(row.getBlob(idx));
                                break;
                            case FLOAT:
                                values[idx] = row.getFloat(idx);
                                break;
                            case BLOB:
                                values[idx] = row.getBytes(idx);
                                break;
                            case BOOLEAN:
                                values[idx] = row.getBoolean(idx);
                                break;
                            case INET:
                                values[idx] = row.getInetAddress(idx).getHostAddress();
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
                                    final List<ByteBuffer> lbb = row.getList(idx, BytesType.instance);
                                    list = new ArrayList(lbb.size());
                                    for (ByteBuffer bb : lbb) {
                                        list.add(InternalCassandraClusterService.deserialize(elementType, bb));
                                    }
                                } else {
                                    list = row.getList(idx, elementType);
                                }
                                values[idx] =  (list.size() == 1) ? list.get(0) : list;
                                break;
                            case SET:
                                Set set;
                                elementType = ((SetType<?>) colSpec.type).getElementsType();
                                if (elementType instanceof UserType) {
                                    final Set<ByteBuffer> lbb = row.getSet(idx, BytesType.instance);
                                    set = new HashSet(lbb.size());
                                    for (ByteBuffer bb : lbb) {
                                        set.add(InternalCassandraClusterService.deserialize(elementType, bb));
                                    }
                                } else {
                                    set = row.getSet(idx, elementType);
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
                                    final Map<String, ByteBuffer> lbb = row.getMap(idx, keyType, BytesType.instance);
                                    map = new HashMap<String , Map<String, Object>>(lbb.size());
                                    for(String key : lbb.keySet()) {
                                        map.put(key, InternalCassandraClusterService.deserialize(elementType, lbb.get(key)));
                                    }
                                } else {
                                    Map<String,Object> map2 = (Map<String,Object>) row.getMap(idx, keyType, elementType);
                                    map = new HashMap<String, Object>(map2.size());
                                    for(String key : map2.keySet()) {
                                        map.put(key,  map2.get(key));
                                    }
                                }
                                values[idx] =  map;
                                break;
                            }
                        } else if (colSpec.type instanceof UserType) {
                            ByteBuffer bb = row.getBytes(idx);
                            values[idx] = InternalCassandraClusterService.deserialize(colSpec.type, bb);
                        } else if (cql3Type instanceof CQL3Type.Custom) {
                            logger.error("CQL3.Custom type not supported for column "+columnName);
                        }
                    }
                    return values;
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
                                UntypedResultSet results = getClusterService().fetchRowInternal(baseCfs.metadata.ksName, null, baseCfs.metadata.cfName, missingColumns, pk, hasStaticUpdate);
                                if (!results.isEmpty()) {
                                    Object[] missingValues = rowAsArray(results.one());
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
                    return fieldsNotNull.cardinality() > 0;
                }
                
                public Context buildContext(IndexInfo indexInfo) throws IOException {
                    Context context = ExtendedElasticSecondaryIndex.this.perThreadContext.get();
                    context.reset(indexInfo, uid);
                    
                    // preCreate for all metadata fields.
                    for (MetadataFieldMapper metadataMapper : context.docMapper.mapping().metadataMappers()) {
                        metadataMapper.preCreate(context);
                    }
                    
                    context.docMapper.idFieldMapper().createField(context, id);
                    context.docMapper.uidMapper().createField(context, uid);
                    context.docMapper.typeMapper().createField(context, baseCfs.metadata.cfName);
                    context.docMapper.tokenFieldMapper().createField(context, token);
                    context.docMapper.routingFieldMapper().createField(context, partitionKey);
                    context.version(DEFAULT_VERSION);
                    context.doc().add(DEFAULT_VERSION);
                    
                    // add all fields to context.
                    for(int i=0; i < values.length; i++) {
                        if (values[i] != null) {
                            try {
                                Mapper mapper = context.docMapper.mappers().smartNameFieldMapper(fields[i]);
                                mapper = (mapper != null) ? mapper : context.docMapper.objectMappers().get(fields[i]);
                                if (mapper != null) {
                                    if (fieldsIsStatic != null && fieldsIsStatic.get(i)) {
                                        context.setStaticField(true);
                                    }
                                    context.addField(mapper, values[i]);
                                }
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
                                ArrayNode an = ClusterService.Utils.jsonMapper.createArrayNode();
                                for(String c: cols) 
                                    ClusterService.Utils.addToJsonArray( values[indexOf(c)], an );
                                parent = ClusterService.Utils.writeValueAsString(an);
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
                    
                    for(IndexInfo ii : MappingInfo.this.targetIndices(values)) {
                        try {
                            Context context = buildContext(ii);
                            if (staticDocumentOnly && !context.hasStaticField()) 
                                continue;
                            
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
                            
                            parsedDoc.parent(context.parent());

                            if (logger.isTraceEnabled()) {
                                logger.trace("index={} id={} type={} uid={} routing={} docs={}", context.indexInfo.name, parsedDoc.id(), parsedDoc.type(), parsedDoc.uid(), parsedDoc.routing(), parsedDoc.docs());
                            }
                            final IndexShard indexShard = context.indexInfo.indexService.shard(0);
                            if (indexShard == null) {
                                logger.warn("No such shard {}.0", context.indexInfo.name);
                            } else if (indexShard.state() != IndexShardState.STARTED) {
                                logger.warn("Shard {}.0 not started", context.indexInfo.name);
                            } else {
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
                                    logger.debug("document CF={}.{} index={} type={} id={} version={} created={} ttl={} refresh={} ", 
                                        baseCfs.metadata.ksName, baseCfs.metadata.cfName,
                                        context.indexInfo.name, baseCfs.metadata.cfName,
                                        id, version, created, ttl, context.indexInfo.refresh);
                                }
                             }
                        } catch (IOException e) {
                            logger.error("error", e);
                        }
                    }
                }
                
                public void delete() {
                    for (MappingInfo.IndexInfo indexInfo : targetIndices(values)) {
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
    protected ReadWriteLock mappingInfoLock = new ReentrantReadWriteLock();
    private volatile MappingInfo mappingInfo;

    public ExtendedElasticSecondaryIndex() {
        super();
    }

    /**
     * Index a mutation. Set empty field for deleted cells.
     */
    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf)  {
        try {
            if (mappingInfo == null) {
                if (logger.isInfoEnabled())  
                    logger.warn("No Elasticsearch index ready");
                return;
            }
            if (mappingInfo.indices.size() == 0) {
                if (logger.isTraceEnabled())  
                    logger.warn("No Elasticsearch index configured");
                return;
            }
            
            if (logger.isTraceEnabled())
                logger.trace("mappingInfo.metadataVersion={} indices={}", mappingInfo.metadataVersion, mappingInfo.indices.keySet());
            
            mappingInfoLock.readLock().lock();
            try {
                // block for stack allocation
                final MappingInfo.RowcumentFactory docFactory = mappingInfo.new RowcumentFactory(rowKey, cf);
                final Iterator<Cell> cellIterator = cf.iterator();
                if (cellIterator.hasNext()) {
                    docFactory.index(cellIterator);
                } else {
                    docFactory.prune();
                }
            } finally {
                mappingInfoLock.readLock().unlock();
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

    
    public void initMapping() {
        mappingInfoLock.writeLock().lock();
        try {
            if (ElassandraDaemon.injector() != null) {
               if (!registred) {
                   getClusterService().addLast(this);
                   registred = true;
               }
               mappingInfo = new MappingInfo(getClusterService().state());
               logger.debug("Secondary index=[{}.{}] initialized, metadata.version={} mappingInfo.indices={}", 
                       this.baseCfs.metadata.ksName, index_name, mappingInfo.metadataVersion,  mappingInfo.indices.keySet());
            } else {
                logger.warn("Cannot initialize index=[{}.{}], cluster service not available.", this.baseCfs.metadata.ksName, index_name);
            }
        } finally {
            mappingInfoLock.writeLock().unlock();
        }
    }
    
    

    /**
     * Cassandra index flush => Elasticsearch flush => lucene commit and disk
     * sync.
     */
    @Override
    public void forceBlockingFlush() {
        if (mappingInfo == null || mappingInfo.indices.size() == 0) {
            logger.trace("Elasticsearch not ready, cannot flush Elasticsearch index");
            return;
        }
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

    

    
    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        boolean updateMapping = false;
        if (event.blocksChanged()) {
            updateMapping = true;
        } else {
            for (ObjectCursor<IndexMetaData> cursor : event.state().metaData().indices().values()) {
                IndexMetaData indexMetaData = cursor.value;
                if (indexMetaData.keyspace().equals(this.baseCfs.metadata.ksName) && 
                    indexMetaData.mapping(this.baseCfs.name) != null &&
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
                logger.debug("index=[{}.{}] metadata.version={} mappingInfo.indices={}",
                        this.baseCfs.metadata.ksName, this.index_name, event.state().metaData().version(), mappingInfo.indices.keySet() );
            } finally {
                mappingInfoLock.writeLock().unlock();
            }
        }
    }
    

}
