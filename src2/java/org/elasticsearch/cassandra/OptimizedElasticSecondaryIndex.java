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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.cassandra.config.CFMetaData;
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
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.transport.Server;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.CloseableThreadLocal;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
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
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.lucene.all.AllEntries;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperForType;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesService;

import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * Custom secondary index for CQL3 only, should be created when mapping is applied and local shard started.
 * Index row as document when Elasticsearch clusterState has no write blocks and local shard is started.
 * @author vroyer
 *
 */
public class OptimizedElasticSecondaryIndex extends PerRowSecondaryIndex implements ClusterStateListener {
    private static final ESLogger logger = Loggers.getLogger(OptimizedElasticSecondaryIndex.class);

    public static Set<OptimizedElasticSecondaryIndex> elasticSecondayIndices = new HashSet<OptimizedElasticSecondaryIndex>();
    
    private CloseableThreadLocal<CassandraParseContext> cache = new CloseableThreadLocal<CassandraParseContext>() {
        @Override
        protected CassandraParseContext initialValue() {
            return new CassandraParseContext(indexSettings, docMapper, new ContentPath(0));
        }
    };
    
    public static class CassandraParseContext extends ParseContext {
        private final DocumentMapper docMapper;
        private final ContentPath path;
        
        @Nullable
        private final Settings indexSettings;
        private final MapperService mapperService;
        private final AnalysisService analysisService;
        
        private Document document;
        private List<Document> documents = new ArrayList<>();

        private String id;
        private Field uid, version, token;
        private StringBuilder stringBuilder = new StringBuilder();
        private Map<String, String> ignoredValues = new HashMap<>();
        private AllEntries allEntries = new AllEntries();
        private float docBoost = 1.0f;
        private Mapper dynamicMappingsUpdate = null;
        
        public CassandraParseContext(Settings indexSettings, DocumentMapper docMapper, ContentPath path, MapperService mapperService, AnalysisService analysisService) {
            this.docMapper = docMapper;
            this.path = path;
            this.indexSettings = indexSettings;
            this.mapperService = mapperService;
            this.analysisService = analysisService;
        }
        
        @Override
        public boolean flyweight() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public DocumentMapperParser docMapperParser() {
            return null;
        }

        @Override
        public String index() {
            return null;
        }

        @Override
        public Settings indexSettings() {
            return indexSettings;
        }

        @Override
        public String type() {
            return docMapper.type();
        }

        @Override
        public SourceToParse sourceToParse() {
            return null;
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
            return this.path;
        }

        @Override
        public XContentParser parser() {
            return null;
        }

        @Override
        public Document rootDoc() {
            return null;
        }

        @Override
        public List<Document> docs() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Document doc() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void addDoc(Document doc) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public RootObjectMapper root() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DocumentMapper docMapper() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AnalysisService analysisService() {
            return this.analysisService;
        }

        @Override
        public MapperService mapperService() {
            return this.mapperService;
        }

        @Override
        public String id() {
            // TODO Auto-generated method stub
            return null;
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

        @Override
        public void id(String id) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Field uid() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void uid(Field uid) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Field version() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void version(Field version) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Field token() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void token(Field token) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public AllEntries allEntries() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public float docBoost() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void docBoost(float docBoost) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public StringBuilder stringBuilder() {
            return stringBuilder;
        }

        @Override
        public void addDynamicMappingsUpdate(Mapper update) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Mapper dynamicMappingsUpdate() {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    
    class MappingInfo {
        class IndexInfo  {
            String     name;
            boolean    refresh;
            IndexService indexService;
            Map<String,Object> mapping;
            DocumentMapperForType docMapper;
            
            private final DocumentMapper docMapper;
            private final ContentPath path;
            
            private Document document;
            private List<Document> documents = new ArrayList<>();

            private String id;
            private Field uid, version, token;
            private StringBuilder stringBuilder = new StringBuilder();
            private Map<String, String> ignoredValues = new HashMap<>();
            private AllEntries allEntries = new AllEntries();
            private float docBoost = 1.0f;
            private Mapper dynamicMappingsUpdate = null;
            
            
            public IndexInfo(String name, IndexService indexService, MappingMetaData mappingMetaData) throws IOException {
                this.name = name;
                this.indexService = indexService;
                this.mapping = mappingMetaData.sourceAsMap();
                this.refresh = false;
                this.docMapper = docMapper;
                this.path = path;
            }

        }
        

        final List<IndexInfo> indices = new ArrayList<IndexInfo>();
        final Map<String, Boolean> fields = new HashMap<String, Boolean>();   // map<fieldName, cql_partial_update> for all ES indices.
        final CFMetaData metadata = baseCfs.metadata;
        final List<ColumnDefinition> clusteringColumns = baseCfs.metadata.clusteringColumns();
        final List<ColumnDefinition> partitionKeyColumns = baseCfs.metadata.partitionKeyColumns();
        
        MappingInfo(ClusterState state) {
            if (state.blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                logger.debug("global write blocked");
                return;
            }
            
            for(Iterator<IndexMetaData> indexMetaDataIterator = state.metaData().iterator(); indexMetaDataIterator.hasNext(); ) {
                IndexMetaData indexMetaData = indexMetaDataIterator.next();
                String index = indexMetaData.getIndex();
                MappingMetaData mappingMetaData; 
                ClusterBlockException clusterBlockException = state.blocks().indexBlockedException(ClusterBlockLevel.WRITE, index);
                if (clusterBlockException == null && 
                    state.routingTable().isLocalShardsStarted(index) &&
                    ( OptimizedElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(index) || 
                      OptimizedElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME))) &&
                    ((mappingMetaData = indexMetaData.mapping(OptimizedElasticSecondaryIndex.this.baseCfs.metadata.cfName)) != null)
                   ) {
                    try {
                        IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
                        IndexService indexService = indicesService.indexServiceSafe(index);
                        IndexInfo indexInfo = new IndexInfo(index, indexService, mappingMetaData);
                        indexInfo.docMapper = indexService.mapperService().documentMapperWithAutoCreate(OptimizedElasticSecondaryIndex.this.baseCfs.metadata.cfName);
                        
                        this.indices.add(indexInfo);
                        if (mappingMetaData.getSourceAsMap().get("properties") != null) {
                            Map<String,Object> props = (Map<String,Object>)mappingMetaData.getSourceAsMap().get("properties");
                            for(String fieldName : props.keySet() ) {
                                Map<String,Object> fieldMap = (Map<String,Object>)props.get(fieldName);
                                boolean partialUpdate = (fieldMap.get(TypeParsers.CQL_PARTIAL_UPDATE) == null || (Boolean)fieldMap.get(TypeParsers.CQL_PARTIAL_UPDATE));
                                if (fields.get(fieldName) != null) {
                                    partialUpdate = partialUpdate || fields.get(fieldName);
                                }
                                fields.put(fieldName, partialUpdate);
                            }
                        }
                        if (mappingMetaData.hasParentField()) {
                            this.fields.put(ParentFieldMapper.NAME,true);
                        }
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
                } else {
                    logger.debug("index blocked or not mapped ");
                }
            }
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
        
        
        class DocumentFactory  {
            final ByteBuffer rowKey;
            final ColumnFamily cf;
            final Long token;
            final ArrayNode an;
            final Object[] ptCols;
            final String partitionKey;
            RowDocument doc = null;
            
            public DocumentFactory(final ByteBuffer rowKey, final ColumnFamily cf) throws JsonGenerationException, JsonMappingException, IOException {
                this.rowKey = rowKey;
                this.cf = cf;
                this.token = (Long) partitioner.getToken(rowKey).getTokenValue();   // Cassandra Token value (Murmur3 partitionner only)
                this.an = ClusterService.Utils.jsonMapper.createArrayNode();
                this.ptCols = new Object[partitionKeyColumns.size()];
                
                CType ctype = metadata.getKeyValidatorAsCType();
                Composite composite = ctype.fromByteBuffer(rowKey);
                for(int i=0; i<composite.size(); i++) {
                    ByteBuffer bb = composite.get(i);
                    AbstractType<?> type = ctype.subtype(i);
                    ptCols[i] = type.compose(bb);
                    ClusterService.Utils.addToJsonArray(type, ptCols[i], an);
                }
                this.partitionKey = ClusterService.Utils.writeValueAsString(an);  // JSON string  of the partition key.
            }
            
           
            public void index(Iterator<Cell> cellIterator) throws IOException {
                RowDocument doc = new RowDocument(cellIterator.next());
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    if (metadata.getColumnDefinition(cellName) == null && cellName.clusteringSize() > 0)  {
                        doc.flush();
                        doc = new RowDocument(cell);
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
                            logger.trace("delete rangeTombstone (not implemented) " + getIndexName() + " cf=" + metadata.ksName + "." + metadata.cfName + " min="+rangeTombstone.min+" max="+rangeTombstone.max);
                        }
                    } else {
                        delete();
                    }
                }
            }
            
            public void delete() {
                logger.warn("delete row not implemented");
            }
                
            class RowDocument {
                //final Map<String, Object> docMap = new HashMap<String, Object>(fields.size());
                ParseContext[] contexts;
                String id = null;
                Field uid, version, type;
                Collection<String> tombstoneColumns = null;
                boolean wideRow = false;
                boolean hasStaticUpdate = false;
                boolean docLive = false;
                int     docTtl = Integer.MAX_VALUE;
                
                // init document with clustering columns stored in cellName, or cell value for non-clustered columns (regular with no clustering key or static columns).
                public RowDocument(Cell cell) throws IOException {
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    if (cell.isLive()) this.docLive = true;
                    initParseContexts();
                    for(int i=0; i < ptCols.length; i++) {
                        String colName = partitionKeyColumns.get(i).name.toString();
                        int j=0;
                        for(IndexInfo ii : MappingInfo.this.indices) {
                            Mapper mapper = ii.docMapper.getDocumentMapper().mappers().smartNameFieldMapper(colName);
                            if (mapper != null) {
                                DocumentParser.parseObjectOrField(contexts[j].externalValue(), mapper);
                                documents[j++].add(mapper.);
                            }
                        }
                        if (fields.get(colName) != null) {
                            
                            docMap.put(colName, ptCols[i]);
                        }
                    }
                    if (metadata.getColumnDefinition(cell.name()) == null && cellName.clusteringSize() > 0)  {
                        // add clustering keys to docMap and _id
                        ArrayNode an2 = ClusterService.Utils.jsonMapper.createArrayNode();
                        an2.addAll(an);
                        wideRow = true;
                        for(int i=0; i < clusteringColumns.size() ; i++) {
                            ColumnDefinition ccd = clusteringColumns.get(i);
                            String colName = ccd.name.toString();
                            Object colValue = deserialize(ccd.type, cellName.get(i));
                            if (fields.get(colName) != null) {
                                docMap.put(colName, colValue);
                            }
                            ClusterService.Utils.addToJsonArray(ccd.type, colValue, an2);
                        }
                        id = ClusterService.Utils.writeValueAsString(an2);
                        uid = new Field(UidFieldMapper.NAME, "", UidFieldMapper.Defaults.FIELD_TYPE);
                        type = new Field(TypeFieldMapper.NAME, "", TypeFieldMapper.Defaults.FIELD_TYPE);
                    } else {
                        id = partitionKey;
                        readCellValue(cell);
                    }
                }
               
                public void initParseContexts() {
                    ParseContext[] contexts = new ParseContext[MappingInfo.this.indices.size()];
                    int i=0;
                    for(IndexInfo ii : MappingInfo.this.indices) {
                        final IndexInfo indexInfo = ii;
                        contexts[i++] = new ParseContext() {
                            ContentPath path = new ContentPath();
                            Document rootDoc;
                            Object externalValue = null;
                            
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
                                return MappingInfo.this.metadata.cfName;
                            }

                            @Override
                            public SourceToParse sourceToParse() {
                                return null;
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
                                return this.rootDoc;
                            }

                            @Override
                            public List<Document> docs() {
                                return Collections.singletonList(this.rootDoc);
                            }

                            @Override
                            public Document doc() {
                                return this.rootDoc;
                            }

                            @Override
                            public void addDoc(Document doc) {
                            }

                            @Override
                            public RootObjectMapper root() {
                                return null;
                            }

                            @Override
                            public DocumentMapper docMapper() {
                                // TODO Auto-generated method stub
                                return indexInfo.indexService.mapperService().documentMapper(MappingInfo.this.metadata.cfName);
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

                            @Override
                            public void ignoredValue(String indexName, String value) {
                                // TODO Auto-generated method stub
                                
                            }

                            @Override
                            public String ignoredValue(String indexName) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public void id(String id) {
                            }

                            @Override
                            public Field uid() {
                                return uid;
                            }

                            @Override
                            public void uid(Field uid) {
                            }

                            @Override
                            public Field version() {
                                return version;
                            }

                            @Override
                            public void version(Field version) {
                            }

                            @Override
                            public Field token() {
                                return token;
                            }

                            @Override
                            public void token(Field token) {
                            }

                            @Override
                            public AllEntries allEntries() {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public float docBoost() {
                                // TODO Auto-generated method stub
                                return 0;
                            }

                            @Override
                            public void docBoost(float docBoost) {
                                // TODO Auto-generated method stub
                                
                            }

                            @Override
                            public StringBuilder stringBuilder() {
                                return this.s;
                            }

                            @Override
                            public void addDynamicMappingsUpdate(Mapper update) {
                                // TODO Auto-generated method stub
                                
                            }

                            @Override
                            public Mapper dynamicMappingsUpdate() {
                                // TODO Auto-generated method stub
                                return null;
                            }
                            
                            public boolean externalValueSet() {
                                return true;
                            }

                            public Object externalValue() {
                                return this.externalValue;
                            }
                            
                            public void externalValue(Object o) {
                                this.externalValue = o;
                            }
                        };
                    }
                    this.contexts = contexts;
                }
                public void readCellValue(Cell cell) throws IOException {
                    CellName cellName = cell.name();
                    String cellNameString = cellName.cql3ColumnName(metadata).toString();
                    ColumnDefinition cd = metadata.getColumnDefinition(cell.name());
                    if (cd == null) {
                        //ignore cell, (probably clustered keys in cellnames only) 
                        return;
                    }
                    if (cell.isLive() && fields.get(cellNameString) != null) {
                        docLive = true;
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
                                List l = (List) docMap.get(cellNameString);
                                if (l == null) {
                                    l = new ArrayList();
                                    docMap.put(cellNameString, l);
                                } 
                                l.add(value);
                                break;
                            case SET:
                                value = deserialize(((SetType)cd.type).getElementsType(), cell.value() );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("set name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                                Set s = (Set) docMap.get(cellNameString);
                                if (s == null) {
                                    s = new HashSet();
                                    docMap.put(cellNameString, s);
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
                                    Map m = (Map) docMap.get(cellNameString);
                                    if (m == null) {
                                        m = new HashMap();
                                        docMap.put(cellNameString, m);
                                    } 
                                    m.put(key,value);
                                }
                                break;
                            }
                        } else {
                            Object value = deserialize(cd.type, cell.value() );
                            if (logger.isTraceEnabled()) 
                                logger.trace("name={} kind={} type={} value={}", cellNameString, cd.kind, cd.type.asCQL3Type().toString(), value);
                            docMap.put(cd.name.toString(), value);
                        }
                    } else {
                        // tombstone => black list this column for later document.complete().
                        addTombstoneColumn(cellNameString);
                    }
                }
                
                public void addTombstoneColumn(String cql3name) {
                    if (fields.get(cql3name) != null) {
                        if (tombstoneColumns == null) {
                            tombstoneColumns = new HashSet<String>();
                        }
                        tombstoneColumns.add(cql3name);
                    }
                }
               
                public boolean isTombstone(String cql3name) {
                    if (tombstoneColumns == null) return false;
                    return (tombstoneColumns.contains(cql3name));
                }
                
                public boolean hasMissingClusteringKeys() {
                    return metadata.clusteringColumns().size() > 0 && !wideRow;
                }
                
                public boolean complete() {
                    // add missing or collection columns that should be read before indexing the document.
                    // read missing static columns (with limit 1) or regular columns if  
                    Collection<String> mustReadColumns = null;
                    for(String fieldName: fields.keySet()) {
                        if (metadata.getColumnDefinition(new ColumnIdentifier(fieldName,true)).kind == ColumnDefinition.Kind.STATIC) {
                            if (!this.hasStaticUpdate) {
                                // ignore static columns, we got only regular columns.
                                continue;
                            }
                        } else {
                            if (this.hasMissingClusteringKeys()) {
                                // ignore regular columns, we are updating static one.
                                continue;
                            }
                        }
                        
                        if (fields.get(fieldName)) {
                            Object value = docMap.get(fieldName);
                            if (value == null) {
                                if (!isTombstone(fieldName)) {
                                    if (mustReadColumns == null) mustReadColumns = new ArrayList<String>();
                                    mustReadColumns.add(fieldName);
                                }
                            } else {
                                if (value instanceof Set || value instanceof Map) {
                                    if (mustReadColumns == null) mustReadColumns = new ArrayList<String>();
                                    mustReadColumns.add(fieldName);
                                }
                            }
                        }
                    }
                    if (mustReadColumns != null) {
                        try {
                            // fetch missing fields from the local cassandra row to update Elasticsearch index
                            if (logger.isTraceEnabled()) {
                                logger.trace(" {}.{} id={} read fields={} docMap={}",metadata.ksName, metadata.cfName, id(), mustReadColumns, docMap);
                            }
                            UntypedResultSet results = getClusterService().fetchRowInternal(metadata.ksName, metadata.cfName, mustReadColumns, ptCols, hasStaticUpdate);
                            if (!results.isEmpty()) {
                                int putCount = getClusterService().rowAsMap(metadata.ksName, metadata.cfName, results.one(), docMap);
                                if (putCount > 0) docLive = true;
                                if (logger.isTraceEnabled()) {
                                    logger.trace("{}.{} id={} indexing docMap={}", metadata.ksName, metadata.cfName, id(), docMap);
                                }
                            } else {
                                return false;
                            }
                        } catch (RequestValidationException | IOException e) {
                            logger.error("Failed to fetch columns {}",mustReadColumns,e);
                        }
                    }
                    return true;
                }
                
                
                public String id()  {
                    if (id == null) {
                       try {
                           id = ClusterService.Utils.writeValueAsString(an);
                       } catch (IOException e) {
                           logger.error("Unxepected error",e);
                       }
                    }
                    return id;
                }
                
                public XContentBuilder build(MappingInfo.IndexInfo indexInfo, boolean forStaticDocument) throws IOException {
                    MapperService mapperService = indexInfo.indexService.mapperService();
                    DocumentMapper documentMapper = mapperService.documentMapper(metadata.cfName);
                    return ClusterService.Utils.buildDocument(documentMapper, docMap, false, forStaticDocument);
                }

                public void index() {
                    if (this.hasStaticUpdate) {
                        index(true);
                    }
                    if (!this.hasMissingClusteringKeys()) {
                        index(false);
                    }
                }
                
                public Engine.Index prepareIndex(DocumentMapperForType docMapper, SourceToParse source, long version, VersionType versionType, Engine.Operation.Origin origin, boolean canHaveDuplicates) {
                    long startTime = System.nanoTime();
                    DocumentMapperForType docMapper = 
                    ParsedDocument doc = docMapper.getDocumentMapper().parse(source);
                    
                    if (docMapper.getMapping() != null) {
                        doc.addDynamicMappingsUpdate(docMapper.getMapping());
                    }
                    return new Engine.Index(docMapper.getDocumentMapper().uidMapper().term(doc.uid().stringValue()), doc, version, versionType, origin, startTime, canHaveDuplicates);
                }
                
                public void index(boolean forStaticDocument) {
                    for (MappingInfo.IndexInfo indexInfo : indices) {
                        try {
                            XContentBuilder builder = build(indexInfo, forStaticDocument);
                            if (logger.isTraceEnabled()) {
                                logger.trace("indexing  CF={} target={} id={} token={} source={}",metadata.cfName, indexInfo.name, id(), token, builder.string());
                            }
                            BytesReference source = builder.bytes();
                            SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, source)
                                    .type(metadata.cfName)
                                    .id(id())
                                    .token(DocumentFactory.this.token)
                                    .routing(partitionKey)
                                    .timestamp(Long.toString(System.currentTimeMillis()));
                            if (docMap.get(ParentFieldMapper.NAME) != null) {
                                sourceToParse.parent((String)docMap.get(ParentFieldMapper.NAME));
                            }
                            if (this.docTtl < Integer.MAX_VALUE) {
                                sourceToParse.ttl(this.docTtl);
                            }
                            IndexShard indexShard = indexInfo.indexService.shardSafe(0);
                            Engine.IndexingOperation operation = indexShard.prepareIndex(sourceToParse, Versions.MATCH_ANY, VersionType.INTERNAL, Engine.Operation.Origin.PRIMARY, false);
                            Mapping update = operation.parsedDoc().dynamicMappingsUpdate();
                            if (update != null) { 
                                // could be necessay when cqlStruct=map or if mapping update id not yet propagated.
                                getClusterService().blockingMappingUpdate(indexInfo.indexService, metadata.cfName, new CompressedXContent(update.toString()) );
                            }
                           
                            
                            boolean created = operation.execute(indexShard);
                            long version = operation.version();

                            if (indexInfo.refresh) {
                                try {
                                    indexShard.refresh("refresh_flag_index");
                                } catch (Throwable e) {
                                    // ignore
                                }
                            }
                            
                            if (logger.isDebugEnabled()) {
                                logger.debug("document CF={}.{} index={} type={} id={} version={} created={} ttl={} refresh={} parent={} doc={}", 
                                    metadata.ksName, metadata.cfName,
                                    indexInfo.name, metadata.cfName, 
                                    id(), version, created, sourceToParse.ttl(), indexInfo.refresh, sourceToParse.parent(), builder.string());
                            }
                        } catch (Throwable e1) {
                            logger.error("Failed to index document id=" + id() + " in index.type=" + indexInfo.name + "." + OptimizedElasticSecondaryIndex.this.baseCfs.metadata.cfName, e1);
                        }
                    }
                }
                
                public void delete() {
                    for (MappingInfo.IndexInfo indexInfo : indices) {
                        logger.debug("deleting document from index.type={}.{} id={}", indexInfo.name, metadata.cfName, id());
                        IndexShard indexShard = indexInfo.indexService.shardSafe(0);
                        Engine.Delete delete = indexShard.prepareDelete(metadata.cfName, id(), Versions.MATCH_ANY, VersionType.INTERNAL, Engine.Operation.Origin.PRIMARY);
                        indexShard.delete(delete);
                    }
                }
                
                
                public void flush() throws JsonGenerationException, JsonMappingException, IOException {
                    if (docLive && complete()) {
                        index();
                    } else {
                        delete();
                    }
                }
            }

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
                return IndexInfo.this.indexService.index().name();
            }


            @Override
            public Settings indexSettings() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public String type() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public SourceToParse sourceToParse() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public BytesReference source() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public void source(BytesReference source) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public ContentPath path() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public XContentParser parser() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public Document rootDoc() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public List<Document> docs() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public Document doc() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public void addDoc(Document doc) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public RootObjectMapper root() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public DocumentMapper docMapper() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public AnalysisService analysisService() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public MapperService mapperService() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public String id() {
                // TODO Auto-generated method stub
                return null;
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


            @Override
            public void id(String id) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public Field uid() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public void uid(Field uid) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public Field version() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public void version(Field version) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public Field token() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public void token(Field token) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public AllEntries allEntries() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public float docBoost() {
                // TODO Auto-generated method stub
                return 0;
            }


            @Override
            public void docBoost(float docBoost) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public StringBuilder stringBuilder() {
                // TODO Auto-generated method stub
                return null;
            }


            @Override
            public void addDynamicMappingsUpdate(Mapper update) {
                // TODO Auto-generated method stub
                
            }


            @Override
            public Mapper dynamicMappingsUpdate() {
                // TODO Auto-generated method stub
                return null;
            }
        }
    }

    
    String index_name;
    IPartitioner partitioner = DatabaseDescriptor.getPartitioner();
    
 
    // updated when create/open/close/remove an ES index.
    private AtomicReference<MappingInfo> mappingAtomicReference = new AtomicReference();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private ClusterService clusterService = null;
    
    public OptimizedElasticSecondaryIndex() {
        super();
    }

    public ClusterService getClusterService() {
        if (this.clusterService == null) {
            if (ElassandraDaemon.injector() == null || 
                (this.clusterService=ElassandraDaemon.injector().getInstance(ClusterService.class)) == null ) {
                throw new ElasticsearchException("ClusterService not available");
            }
        }
        return this.clusterService;
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb) {
        if (type instanceof UserType) {
            UserType utype = (UserType) type;
            Map<String, Object> mapValue = new HashMap<String, Object>();
            ByteBuffer[] components = utype.split(bb);
            for (int i = 0; i < components.length; i++) {
                String fieldName = UTF8Type.instance.compose(utype.fieldName(i));
                AbstractType<?> ctype = utype.type(i);
                Object value = (components[i] == null) ? null : deserialize(ctype, components[i]);
                mapValue.put(fieldName, value);
            }
            return mapValue;
        } else if (type instanceof ListType) {
            ListType ltype = (ListType)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            List list = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                list.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, Server.VERSION_3) ));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return list;
        } else if (type instanceof SetType) {
            SetType ltype = (SetType)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            Set set = new HashSet(size);
            for (int i = 0; i < size; i++) {
                set.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, Server.VERSION_3) ));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return set;
        } else if (type instanceof MapType) {
            MapType ltype = (MapType)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            Map map = new LinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                ByteBuffer kbb = CollectionSerializer.readValue(input, Server.VERSION_3);
                ByteBuffer vbb = CollectionSerializer.readValue(input, Server.VERSION_3);
                String key = (String) ltype.getKeysType().compose(kbb);
                map.put(key, deserialize(ltype.getValuesType(), vbb));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return map;
        } else {
             return type.compose(bb);
        }
    }

    
    /**
     * Index a mutation. Set empty field for deleted cells.
     */
    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf)  {
        try {
            MappingInfo mappingInfo = this.mappingAtomicReference.get();
            if (mappingInfo == null || mappingInfo.indices.size() == 0) {
                logger.trace("No Elasticsearch index ready");
                return;
            }

            MappingInfo.DocumentFactory docFactory = mappingInfo.new DocumentFactory(rowKey, cf);
            Iterator<Cell> cellIterator = cf.iterator();
            if (cellIterator.hasNext()) {
                docFactory.index(cellIterator);
            } else {
                docFactory.prune();
            }
        } catch (IOException e) {
            logger.error("error", e);
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

    @Override
    public void init() {
        lock.writeLock().lock();
        try {
            index_name = "elastic_"+this.baseCfs.name;
            elasticSecondayIndices.add(this);
            initMapping();
        } finally {
            lock.writeLock().unlock();
        }
    }

    
    public synchronized void initMapping() {
        if (ElassandraDaemon.injector() != null) {
           getClusterService().addLast(this);
            this.mappingAtomicReference.set(new MappingInfo(getClusterService().state()));
            logger.debug("index=[{}.{}] initialized,  mappingAtomicReference = {}", this.baseCfs.metadata.ksName, index_name, this.mappingAtomicReference.get());
        } else {
            logger.error("Failed to initialize index=[{}.{}], cluster service not available.", this.baseCfs.metadata.ksName, index_name);
        }
    }
    
    
    /**
     * Reload an existing index following a change to its configuration, or that
     * of the indexed column(s). Differs from init() in that we expect expect
     * new resources (such as CFS for a KEYS index) to be created by init() but
     * not here
     */
    @Override
    public void reload() {
    }

    @Override
    public void validateOptions() throws ConfigurationException {
        for (ColumnDefinition cd : getColumnDefs()) {
            for (String optionKey : cd.getIndexOptions().keySet()) {
                if (!(optionKey.equals(CUSTOM_INDEX_OPTION_NAME))) {
                    logger.warn("Ignore elastic secondary index options: " + optionKey);
                }
            }
        }
    }

    
    @Override
    public String getIndexName() {
        return index_name;
    }

    @Override
    protected SecondaryIndexSearcher createSecondaryIndexSearcher(Set<ByteBuffer> columns) {
        return new ElasticSecondaryIndexSearcher(this.baseCfs.indexManager, columns);
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
    public ColumnFamilyStore getIndexCfs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeIndex(ByteBuffer columnName) {
        
    }

    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        //logger.warn("invalidate");
    }

    @Override
    public void truncateBlocking(long truncatedAt) {
        // TODO implements truncate
        logger.warn("truncateBlocking at [{}], not implemented", truncatedAt);
    } 

    /**
     * Returns true if the provided cell name is indexed by this secondary
     * index.
     */
    @Override
    public boolean indexes(CellName name) {
        ColumnDefinition cdef = this.baseCfs.metadata.getColumnDefinition(name);
        if ((cdef != null) && (cdef.getIndexOptions() != null) && (this.getClass().getCanonicalName().equals(cdef.getIndexOptions().get(CUSTOM_INDEX_OPTION_NAME)))) {
            return true;
        }
        /*
         * ..However, some type of COMPACT STORAGE layout do not store the CQL3
         * column name in the cell name and so this part can be null => return
         * true
         */
        ColumnIdentifier cql3Name = name.cql3ColumnName(this.baseCfs.metadata);
        return ((cql3Name != null) && (ByteBufferUtil.EMPTY_BYTE_BUFFER.compareTo(cql3Name.bytes) == 0));
    }
    
    @Override
    public long estimateResultRows() {
        // TODO Auto-generated method stub
        return 0;
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
