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
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
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
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.delete.XDeleteRequest;
import org.elasticsearch.action.delete.XDeleteResponse;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper.Defaults;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Custom secondary index for CQL3 only, should be created when mapping is applied and local shard started.
 * Index row as document when Elasticsearch clusterState has no write blocks and local shard is started.
 * @author vroyer
 *
 */
public class ElasticSecondaryIndex extends PerRowSecondaryIndex implements ClusterStateListener {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSecondaryIndex.class);

    public static final String ELASTIC_TOKEN = "_token";

    // targets='<index>.<type>,<index>.<type>...'
    public static final String ELASTIC_OPTION_TARGETS = "targets";

    public static final String ELASTIC_OPTION_PARTIAL_UPDATE = "partial_update";
    
    // keyspace.table to elasticsearch index.type mapping used when keyspace.table <> index.type
    protected static final ConcurrentMap<Pair<String, String>, Pair<String, String>> mapping = Maps.newConcurrentMap();
    
 // Indexed columns and if we need to read it to deal with partial updates.
    protected final ConcurrentMap<String, Boolean> readOnUpdateColumnMap = Maps.newConcurrentMap();
    
    String index_name;
    Boolean isOpen = false;
    IPartitioner partitioner = DatabaseDescriptor.getPartitioner();
    
    // elasticsearch index.type -> mappingMetaData
    Set<Pair<String, String>> targets = new HashSet<Pair<String, String>>();
    
    
    
    public ElasticSecondaryIndex() {
        super();
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
                map.put(deserialize(ltype.getKeysType(), kbb), deserialize(ltype.getValuesType(), vbb));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return map;
        } else {
            return type.compose(bb);
        }
    }
    
    
    class Document {
        final CFMetaData metadata;
        
        final ArrayNode an;
        final Object[] pkColumns;
        int pkLength;
        
        boolean docLive = false;
        final Map<String, Object> docMap = new Hashtable<String, Object>();
        
        XContentBuilder builder;
        String id = null;
        Collection<String> tombstoneColumns = null;
        
        // init document with partition keys;
        private Document(final ByteBuffer rowKey, final ColumnFamily cf) throws IOException {
            this.metadata = cf.metadata();
            this.docMap.put(ELASTIC_TOKEN,partitioner.getToken(rowKey).getTokenValue());
             
            this.an = ElasticSchemaService.jsonMapper.createArrayNode();
            this.pkColumns = new Object[metadata.partitionKeyColumns().size()+metadata.clusteringColumns().size()];
            this.pkLength = 0;
            
            CType ctype = metadata.getKeyValidatorAsCType();
            Composite composite = ctype.fromByteBuffer(rowKey);
            for(int i=0; i<composite.size(); i++) {
                ByteBuffer bb = composite.get(i);
                AbstractType<?> type = ctype.subtype(i);
                String name = metadata.partitionKeyColumns().get(i).name.toString();
                Object value = type.compose(bb);
                pkColumns[pkLength++] = value;
                ElasticSchemaService.addToJsonArray(type, value, an);
                if (readOnUpdateColumnMap.get(name) != null) {
                    docMap.put(name, value);
                }
            }
        }
        
        // init document with clustering keys
        public Document(final ByteBuffer rowKey, final ColumnFamily cf, final Cell cell) throws IOException {
            this(rowKey, cf);
            
            List<ColumnDefinition> clusteringColumns = metadata.clusteringColumns();
            CellName cellName = cell.name();
            ColumnDefinition cd = metadata.getColumnDefinition(cellName);
            int nrPartitonColumns = metadata.partitionKeyColumns().size();
            if ((cd != null) && (clusteringColumns.size() > 0)) {
                for(int i=0; i < clusteringColumns.size(); i++) {
                    ColumnDefinition ccd = clusteringColumns.get(i);
                    String name = ccd.name.toString();
                    Object value = deserialize(ccd.type, cellName.get(i));
                    if (logger.isTraceEnabled()) 
                        logger.trace("cell clustering column={} value={}",  name, value);
                    pkColumns[pkLength++] = value;
                    ElasticSchemaService.addToJsonArray(ccd.type, value, an);
                    if (cell.isLive() && (readOnUpdateColumnMap.get(name) != null)) {
                        docLive = true; 
                        docMap.put(name, value);
                    }
                }
            }
        }
        
        
        public void addRegularColumn(final String name, final Object value) throws IOException {
            if (readOnUpdateColumnMap.get(name) != null) {
                docLive = true; 
                docMap.put(name, value);
            }
        }
        
        public void addListColumn(final String name, final Object value) throws IOException {
            if (readOnUpdateColumnMap.get(name) != null) {
                docLive = true; 
                List v = (List) docMap.get(name);
                if (v == null) {
                    v = new ArrayList();
                    docMap.put(name, v);
                } 
                v.add(value);
            }
        }
        
        public void addSetColumn(final String name, final Object value) throws IOException {
            if (readOnUpdateColumnMap.get(name) != null) {
                docLive = true; 
                Set v = (Set) docMap.get(name);
                if (v == null) {
                    v = new HashSet();
                    docMap.put(name, v);
                } 
                v.add(value);
            }
        }
        
        public void addMapColumn(final String name, final Object key, final Object value) throws IOException {
            if (readOnUpdateColumnMap.get(name) != null) {
                docLive = true; 
                Map v = (Map) docMap.get(name);
                if (v == null) {
                    v = new HashMap();
                    docMap.put(name, v);
                } 
                v.put(key,value);
            }
        }
        
        public void addTombstoneColumn(String cql3name) {
            if (readOnUpdateColumnMap.get(cql3name)) {
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
        
        public void complete() throws JsonGenerationException, JsonMappingException, IOException {
            if (builder == null) {
                // add missing or collection columns that should be read before indexing the document.
                Collection<String> mustReadColumns = null;
                for(String fieldName: readOnUpdateColumnMap.keySet()) {
                    if (readOnUpdateColumnMap.get(fieldName)) {
                        Object value = docMap.get(fieldName);
                        if (value == null) {
                            if (!isTombstone(fieldName)) {
                                if (mustReadColumns == null) mustReadColumns = new ArrayList<String>();
                                mustReadColumns.add(fieldName);
                            }
                        } else {
                            if (value instanceof Collection || value instanceof Map) {
                                if (mustReadColumns == null) mustReadColumns = new ArrayList<String>();
                                mustReadColumns.add(fieldName);
                            }
                        }
                    }
                }
                if (mustReadColumns != null) {
                    try {
                        // fetch missing fields from the local cassandra row to update Elasticsearch index
                        logger.debug(" {}.{} id={} read fields={}",metadata.ksName, metadata.cfName, id(), mustReadColumns);
                        SchemaService schemaService = ElassandraDaemon.injector().getInstance(SchemaService.class);
                        Row row = schemaService.fetchRowInternal(metadata.ksName, metadata.cfName, mustReadColumns, pkColumns).one();
                        int putCount = schemaService.rowAsMap(metadata.ksName, metadata.cfName, row, docMap);
                        if (putCount > 0) docLive = true;
                    } catch (RequestValidationException | IOException e) {
                        logger.error("Failed to fetch columns {}",mustReadColumns,e);
                    }
                }
                
                // TODO: build one builder for each target index 
                this.builder = XContentFactory.contentBuilder(XContentType.JSON);
                this.builder.startObject();
                for(Entry<String,Object> entry : docMap.entrySet()) {
                    builder.field(entry.getKey(),entry.getValue());
                }
                this.builder.endObject();
            }
        }
        
        public String id()  {
            if (id == null) {
               try {
                   id = ElasticSchemaService.buildElasticId(an);
               } catch (IOException e) {
                   logger.error("Unxepected error",e);
               }
            }
            return id;
        }
        
        public BytesReference source() throws IOException {
            return builder.bytes();
        }
        
        public void index() throws JsonGenerationException, JsonMappingException, IOException {
            for (Pair<String, String> target : targets) {
                try {
                    if (logger.isTraceEnabled()) {
                        logger.debug("indexing  CF={} target={} id={} source={}",metadata.cfName, target, id(), builder.string());
                    }
                    // TODO: should customize source builder for each target index.
                    synchronousIndex(target.left, target.right, this, this.metadata.getDefaultTimeToLive(), false);
                } catch (Throwable e1) {
                    logger.error("Failed to index document id=" + id() + " in index.type=" + target.left + "." + target.right, e1);
                }
            }
        }
        
        public void delete() {
            logger.debug("deleting document from index " + getIndexName() + " id=" + id());
            for (Pair<String, String> target : targets) {
                logger.debug("xdeleting document from index={} type={} id={}", target.left, target.right, id());
                // submit a delete request
                XDeleteRequest request = new XDeleteRequest(target.left, target.right, id());
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
        }
        
        public void flush() throws JsonGenerationException, JsonMappingException, IOException {
            complete();
            if (docLive) {
                index();
            } else {
                delete();
            }
        }
    }
    
    class DocumentFactory {
        final ByteBuffer rowKey;
        final ColumnFamily cf;
        final CFMetaData metadata;
        final List<ColumnDefinition> clusteringColumns;
        final int nrPartitonColumns;
        
        Document doc = null;
        
        public DocumentFactory(final ByteBuffer rowKey, final ColumnFamily cf) {
            this.rowKey = rowKey;
            this.cf = cf;
            this.metadata = cf.metadata();
            this.clusteringColumns = metadata.clusteringColumns();
            this.nrPartitonColumns = metadata.partitionKeyColumns().size();
        }
        
        public Document nextDocument(final Cell cell) throws IOException {
            if (this.doc == null) {
                this.doc = new Document(rowKey, cf, cell);
                return this.doc;
            }
            
            CellName cellName = cell.name();
            boolean sameRow = true;
            if (metadata.getColumnDefinition(cellName) != null) {
                for(int i=0; i < clusteringColumns.size(); i++) {
                    ColumnDefinition ccd = clusteringColumns.get(i);
                    String name = ccd.name.toString();
                    Object value = deserialize(ccd.type, cellName.get(i));
                    if (!value.equals(doc.pkColumns[nrPartitonColumns+i])) {
                        sameRow = false;
                        break;
                    }
                }
                if (!sameRow) {
                    doc.flush();
                    return new Document(rowKey, cf, cell);
                }
            }
            return doc;
        }
    }

    
    /**
     * Index a mutation. Set empty field for deleted cells.
     */
    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf)  {
        if (!this.isOpen) {
            logger.warn("Elasticsearch not ready, cannot index");
            return;
        }

        CFMetaData metadata = cf.metadata();
        
        if (logger.isTraceEnabled()) {       
            CType ctype = metadata.getKeyValidatorAsCType();
            Composite composite = ctype.fromByteBuffer(rowKey);
            logger.debug("indexing " + getIndexName() + " cf=" + metadata.ksName + "." + metadata.cfName + " composite=" + composite + " cf=" + cf.toString());
        }
        
        try {
            DocumentFactory docFactory = new DocumentFactory(rowKey, cf);
            Document doc = null;
            Cell cell = null;
            CellName cellName = null;
            Iterator<Cell> cellIterator = cf.iterator();
            while (cellIterator.hasNext()) {
                cell = cellIterator.next();
                cellName = cell.name();
                assert cellName instanceof CompoundSparseCellName;
                
                if (cellName.cql3ColumnName(metadata).toString().length()==0) {
                    // ignore fake cell with no CQL3 name.
                    continue;
                }
                doc = docFactory.nextDocument(cell);
                ColumnDefinition cd = metadata.getColumnDefinition(cell.name());
                if (cd != null) {
                    if (cell.isLive()) {
                        if (cd.type.isCollection()) {
                            CollectionType ctype = (CollectionType) cd.type;
                            Object value = null;
                  
                            switch (ctype.kind) {
                            case LIST: 
                                value = deserialize(((ListType)cd.type).getElementsType(), cell.value() );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("list name={} type={} value={}", cellName.cql3ColumnName(metadata), cd.type.asCQL3Type().toString(), value);
                                doc.addListColumn(cd.name.toString(), value);
                                break;
                            case SET:
                                value = deserialize(((SetType)cd.type).getElementsType(), cell.value() );
                                if (logger.isTraceEnabled()) 
                                    logger.trace("set name={} type={} value={}", cellName.cql3ColumnName(metadata), cd.type.asCQL3Type().toString(), value);
                                doc.addSetColumn(cd.name.toString(), value);
                                break;
                            case MAP:
                                value = deserialize(((MapType)cd.type).getValuesType(), cell.value() );
                                String key = (String) deserialize(((MapType)cd.type).getKeysType(), cellName.get(cellName.size()-1));
                                if (logger.isTraceEnabled()) 
                                    logger.trace("map name={} type={} key={} value={}", 
                                            cellName.cql3ColumnName(metadata),
                                            cd.type.asCQL3Type().toString(),
                                            key, 
                                            value);
                                doc.addMapColumn(cd.name.toString(), key, value);
                                break;
                            }

                        } else {
                            Object value = deserialize(cd.type, cell.value() );
                            if (logger.isTraceEnabled()) 
                                logger.trace("name={} type={} value={}", cellName.cql3ColumnName(metadata), cd.type.asCQL3Type().toString(), value);
                            doc.addRegularColumn(cd.name.toString(), value);
                        }
                    } else {
                        // tombstone => black list this column for later document.complete().
                        doc.addTombstoneColumn(cellName.cql3ColumnName(metadata).toString());
                    }
                }
            }
            if (doc != null) {
                doc.flush();
            }
        } catch (IOException e) {
            logger.error("failed to index",e);
        }
    }

    
   
    
    /**
     * Elasticsearch synchronous document index.
     * 
     * @param indexName
     * @param type
     * @param id
     * @param sourceData
     * @param ttl
     * @throws Throwable
     */
    private void synchronousIndex(String indexName, String type, Document doc, long ttl, boolean refresh) throws Throwable {    
        IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
        IndexService indexService = indicesService.indexServiceSafe(indexName);
        IndexShard indexShard = indexService.shardSafe(0);
        SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, doc.source()).type(type).id(doc.id())
                .timestamp(Long.toString(System.currentTimeMillis()))
                .ttl(ttl);
        
        Engine.Index index = indexShard.prepareIndex(sourceToParse, Versions.MATCH_ANY, VersionType.INTERNAL, Engine.Operation.Origin.PRIMARY, false);

        if (index.parsedDoc().mappingsModified()) { 
            // could be necessay when cqlStruct=map or if mapping update id not yet propagated.
            SchemaService schemaService = ElassandraDaemon.injector().getInstance(SchemaService.class);
            schemaService.blockingMappingUpdate(indexService, index.docMapper());
        }

        indexShard.index(index);
        long version = index.version();
        boolean created = index.created();

        if (refresh) {
            try {
                indexShard.refresh("refresh_flag_index");
            } catch (Throwable e) {
                // ignore
            }
        }

        logger.debug("document index.type={}.{} id={} version={} created={} ttl={} refresh={} doc={}", 
                indexName, type, doc.id(), version, created, ttl, refresh, doc.builder.string());
    }

    /**
     * cleans up deleted columns from cassandra cleanup compaction
     *
     * @param key
     */
    @Override
    public void delete(DecoratedKey key, Group opGroup) {
        if (!this.isOpen) {
            // TODO: save the update in a commit log to replay it later....
            logger.warn("Elastic node not ready, cannot delete document");
            return;
        }
        Token token = key.getToken();
        Long  token_long = (Long) token.getTokenValue();
        logger.debug("deleting document with _token = " + token_long);
        
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
        ColumnDefinition cdef = getColumnDefs().iterator().next();
        for (String target : cdef.getIndexOptions().get(ELASTIC_OPTION_TARGETS).split(",")) {
            String[] indexAndType = target.split("\\.");
            if ((indexAndType != null) && (indexAndType.length == 2)) {
                Pair<String, String> targetPair = Pair.<String, String> create(indexAndType[0], indexAndType[1]);
                targets.add(targetPair);
                if (!this.baseCfs.keyspace.getName().equals(targetPair.left) || !this.baseCfs.name.equals(targetPair.right)) {
                    // explicit mapping from Elastic index.type to Cassandra keyspace.table
                    mapping.put(targetPair, Pair.create(this.baseCfs.keyspace.getName(), this.baseCfs.name));
                }
            } else {
                logger.warn("Ignoring invalid targets entry {}", indexAndType);
            }
        }
        index_name = ElasticSchemaService.buildIndexName(this.baseCfs.name, cdef.name.toString());
        updateReadOnUpdateMap();
        logger.debug(" {} targets={} readOnUpdate={}", this.getIndexName(), this.targets, this.readOnUpdateColumnMap);
        updateIndexState();
    }


    /**
     * Reload an existing index following a change to its configuration, or that
     * of the indexed column(s). Differs from init() in that we expect expect
     * new resources (such as CFS for a KEYS index) to be created by init() but
     * not here
     */
    @Override
    public void reload() {
        updateReadOnUpdateMap();
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final TypeReference<Map<String, Boolean>> readOnUpdateColumnTypeReference = new TypeReference<Map<String, Boolean>>() {
    };
    
    @Override
    public void validateOptions() throws ConfigurationException {
        for (ColumnDefinition cd : getColumnDefs()) {
            for (String optionKey : cd.getIndexOptions().keySet()) {
                if (!(optionKey.equals(ELASTIC_OPTION_TARGETS) || optionKey.equals(CUSTOM_INDEX_OPTION_NAME) || optionKey.equals(ELASTIC_OPTION_PARTIAL_UPDATE))) {
                    throw new ConfigurationException("Unknown elastic secondary index options: " + optionKey);
                }
            }
        }
    }
    
    public void updateReadOnUpdateMap() {
        for (ColumnDefinition cd : getColumnDefs()) {
            for (String optionKey : cd.getIndexOptions().keySet()) {
                String partialUpdate = cd.getIndexOptions().get(ELASTIC_OPTION_PARTIAL_UPDATE);
                if (partialUpdate != null) {
                    try {
                        this.readOnUpdateColumnMap.putAll((Map<String,Boolean>)jsonMapper.readValue(partialUpdate, readOnUpdateColumnTypeReference));
                    } catch (IOException e) {
                        logger.error("Failed to parse "+ELASTIC_OPTION_PARTIAL_UPDATE, e);
                    }
                }
            }
        }
        logger.debug(" index [{}] {} = {}", index_name, ELASTIC_OPTION_PARTIAL_UPDATE, readOnUpdateColumnMap);
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
        if (!this.isOpen) {
            logger.warn("Elasticsearch not ready, cannot flush Elasticsearch index");
            return;
        }
        for (Pair<String, String> target : targets) {
            try {
                IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
                IndexShard indexShard = indicesService.indexServiceSafe(target.left).shardSafe(0);
                logger.debug("Flushing Elasticsearch index=[{}] state=[{}]",target.left, indexShard.state());
                if (indexShard.state() == IndexShardState.STARTED)  {
                    indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                    logger.debug("Elasticsearch index=[{}] flushed",target.left);
                } else {
                    logger.warn("Cannot flush index=[{}], state=[{}]",target.left, IndexShardState.STARTED);
                }
            } catch (ElasticsearchException e) {
                logger.error("Unexpected error",e);
            }
        }
    }

    
    @Override
    public ColumnFamilyStore getIndexCfs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeIndex(ByteBuffer columnName) {
        try {
            logger.warn("removeIndex [{}] not implemented", ByteBufferUtil.string(columnName));
            
        } catch (CharacterCodingException e) {
            logger.warn("removeIndex error", e);
        }
    }

    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        logger.warn("invalidate");
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

    public void updateIndexState() {
        ClusterService clusterService = ElassandraDaemon.injector().getInstance(ClusterService.class);
        ClusterState state = clusterService.state();
        for (Pair<String, String> target : targets) { // only one target
            ClusterBlockException clusterBlockException = state.blocks().indexBlockedException(ClusterBlockLevel.METADATA, target.left);
            this.isOpen = (clusterBlockException == null);
            if (this.isOpen) {
                this.isOpen = state.routingTable().localShardsStarted(target.left);
            }
            updateReadOnUpdate(state);
        }
    }
    
    
    
    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        for (Pair<String, String> target : targets) { // only one target
            if (event.blocksChanged()) {
                ClusterBlockException clusterBlockException = event.state().blocks().indexBlockedException(ClusterBlockLevel.METADATA, target.left);
                this.isOpen = (clusterBlockException == null);
            } 
            if (this.isOpen && event.indexRoutingTableChanged(target.left)) {
                this.isOpen = event.state().routingTable().localShardsStarted(target.left);
            }
            if (event.metaDataChanged()) {
                updateReadOnUpdate(event.state());
            }
        }
    }
    
    public void updateReadOnUpdate(ClusterState state) {
        for (Pair<String, String> target : targets) {   // only one target
            IndexMetaData indexMetaData = state.metaData().index(target.left);
            MappingMetaData mappingMetaData = indexMetaData.getMappings().get(target.right);
            if (mappingMetaData != null) {
                try {
                    Map<String, Object> properties = (Map<String, Object>) mappingMetaData.getSourceAsMap().get("properties");
                    for(String field : properties.keySet()) {
                        boolean readOnUpdate = Defaults.CQL_PARTIAL_UPDATE;
                        if (properties.get(TypeParsers.CQL_PARTIAL_UPDATE) != null) {
                            readOnUpdate = Boolean.valueOf((String)properties.get(TypeParsers.CQL_PARTIAL_UPDATE));
                        }
                        this.readOnUpdateColumnMap.put(field, readOnUpdate);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse mapping metadata = {}", e, mappingMetaData);
                }
            }
        }
    }

}
