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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.ColumnIdentifier;
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
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
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
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
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
public class ElasticSecondaryIndex extends BaseElasticSecondaryIndex {
    
    class MappingInfo {
        class IndexInfo {
            String     name;
            boolean    refresh;
            IndexService indexService;
            Map<String,Object> mapping;
            String[] pkColumns;
            
            public IndexInfo(String name, IndexService indexService, MappingMetaData mappingMetaData) throws IOException {
                this.name = name;
                this.indexService = indexService;
                this.mapping = mappingMetaData.sourceAsMap();
                this.refresh = false;
            }
        }
        

        List<IndexInfo> indices = new ArrayList<IndexInfo>();
        Map<String, Boolean> fieldsMap = new HashMap<String, Boolean>();   // map<fieldName, cql_partial_update> for all ES indices.
        
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
                if (clusterBlockException != null) {
                    logger.debug("ignore, index=[{}] blocked blocks={}", index, clusterBlockException.blocks());
                    continue;
                }
                if (!state.routingTable().isLocalShardsStarted(index)) {
                    logger.debug("ignore, local shard not started for index=[{}]", index);
                    continue;
                }
                if ( (ElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(index) || ElasticSecondaryIndex.this.baseCfs.metadata.ksName.equals(indexMetaData.keyspace())) &&
                     ((mappingMetaData = indexMetaData.mapping(ElasticSecondaryIndex.this.baseCfs.metadata.cfName)) != null)
                   ) {
                    try {
                        IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
                        IndexService indexService = indicesService.indexServiceSafe(index);
                        IndexInfo indexInfo = new IndexInfo(index, indexService, mappingMetaData);
                        this.indices.add(indexInfo);
                        
                        Map<String,Object> mappingMap = (Map<String,Object>)mappingMetaData.getSourceAsMap();
                        if (mappingMap.get("properties") != null) {
                            Map<String,Object> props = (Map<String,Object>)mappingMap.get("properties");
                            for(String fieldName : props.keySet() ) {
                                Map<String,Object> fieldMap = (Map<String,Object>)props.get(fieldName);
                                boolean mandatory = (fieldMap.get(TypeParsers.CQL_MANDATORY) == null || (Boolean)fieldMap.get(TypeParsers.CQL_MANDATORY));
                                if (fieldsMap.get(fieldName) != null) {
                                    mandatory = mandatory || fieldsMap.get(fieldName);
                                }
                                fieldsMap.put(fieldName, mandatory);
                            }
                        }
                        if (mappingMetaData.hasParentField()) {
                            Map<String,Object> props = (Map<String,Object>)mappingMap.get(ParentFieldMapper.NAME);
                            if (props.get(ParentFieldMapper.CQL_PARENT_PK) == null) {
                                fieldsMap.put(ParentFieldMapper.NAME,true);
                            } else {
                                indexInfo.pkColumns = ((String)props.get(ParentFieldMapper.CQL_PARENT_PK)).split(",");
                                for(String colName : indexInfo.pkColumns)
                                    fieldsMap.put(colName, true);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Unexpected error", e);
                    }
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
        
        final CFMetaData metadata = baseCfs.metadata;
        final List<ColumnDefinition> clusteringColumns = baseCfs.metadata.clusteringColumns();
        final List<ColumnDefinition> partitionKeyColumns = baseCfs.metadata.partitionKeyColumns();
        
        class DocumentFactory {
            final ByteBuffer rowKey;
            final ColumnFamily cf;
            final Long token;
            final ArrayNode an;
            final Object[] ptCols;
            final String partitionKey;
            Document doc = null;
            
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
                Document doc = new Document(cellIterator.next());
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    if (metadata.getColumnDefinition(cellName) == null && cellName.clusteringSize() > 0)  {
                        doc.flush();
                        doc = new Document(cell);
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
                
            class Document {
                final Map<String, Object> docMap = new HashMap<String, Object>(fieldsMap.size());
                String id = null;
                Collection<String> tombstoneColumns = null;
                boolean wideRow = false;
                boolean hasStaticUpdate = false;
                boolean docLive = false;
                int     docTtl = Integer.MAX_VALUE;
                
                // init document with clustering columns stored in cellName, or cell value for non-clustered columns (regular with no clustering key or static columns).
                public Document(Cell cell) throws IOException {
                    CellName cellName = cell.name();
                    assert cellName instanceof CompoundSparseCellName;
                    if (cell.isLive()) this.docLive = true;
                    
                    for(int i=0; i < ptCols.length; i++) {
                        String colName = partitionKeyColumns.get(i).name.toString();
                        if (fieldsMap.get(colName) != null) {
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
                            Object colValue = InternalCassandraClusterService.deserialize(ccd.type, cellName.get(i));
                            if (fieldsMap.get(colName) != null) {
                                docMap.put(colName, colValue);
                            }
                            ClusterService.Utils.addToJsonArray(ccd.type, colValue, an2);
                        }
                        id = ClusterService.Utils.writeValueAsString(an2);
                    } else {
                        id = partitionKey;
                        readCellValue(cell);
                    }
                }
               
                
                public void readCellValue(Cell cell) throws IOException {
                    CellName cellName = cell.name();
                    String cellNameString = cellName.cql3ColumnName(metadata).toString();
                    ColumnDefinition cd = metadata.getColumnDefinition(cell.name());
                    if (cd == null) {
                        //ignore cell, (probably clustered keys in cellnames only) 
                        return;
                    }
                    if (cell.isLive() && fieldsMap.get(cellNameString) != null) {
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
                                value = InternalCassandraClusterService.deserialize(((ListType)cd.type).getElementsType(), cell.value() );
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
                                value = InternalCassandraClusterService.deserialize(((SetType)cd.type).getElementsType(), cell.value() );
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
                                value = InternalCassandraClusterService.deserialize(((MapType)cd.type).getValuesType(), cell.value() );
                                Object key = InternalCassandraClusterService.deserialize(((MapType)cd.type).getKeysType(), cellName.get(cellName.size()-1));
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
                            Object value = InternalCassandraClusterService.deserialize(cd.type, cell.value() );
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
                    if (fieldsMap.get(cql3name) != null) {
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
                    Set<String> mustReadColumns = null;
                    for(String fieldName: fieldsMap.keySet()) {
                        if (metadata.getColumnDefinition(new ColumnIdentifier(fieldName,true)).kind == ColumnDefinition.Kind.STATIC) {
                            if (!this.hasStaticUpdate) {
                                // ignore static columns, we got only regular columns.
                                continue;
                            }
                        } else {
                            if (this.hasMissingClusteringKeys()) {
                                // ignore regular columns, we are updating a static document.
                                continue;
                            }
                        }
                        
                        if (fieldsMap.get(fieldName)) {
                            Object value = docMap.get(fieldName);
                            if (value == null) {
                                if (!isTombstone(fieldName)) {
                                    if (mustReadColumns == null) mustReadColumns = new HashSet<String>();
                                    mustReadColumns.add(fieldName);
                                }
                            } else {
                                if (value instanceof Set || value instanceof Map) {
                                    if (mustReadColumns == null) mustReadColumns = new HashSet<String>();
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
                            UntypedResultSet results = getClusterService().fetchRowInternal(metadata.ksName, null, metadata.cfName, mustReadColumns.toArray(new String[mustReadColumns.size()]), ptCols, hasStaticUpdate);
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
                            } else {
                                if (indexInfo.pkColumns != null && indexInfo.pkColumns.length > 0) {
                                    ArrayNode an = ClusterService.Utils.jsonMapper.createArrayNode();
                                    for(String f : indexInfo.pkColumns) {
                                        ClusterService.Utils.addToJsonArray(docMap.get(f), an);
                                    }
                                    sourceToParse.parent(ClusterService.Utils.writeValueAsString(an));
                                }
                            }
                            if (this.docTtl < Integer.MAX_VALUE) {
                                sourceToParse.ttl(this.docTtl);
                            }
                            IndexShard indexShard = indexInfo.indexService.shardSafe(0);
                            Engine.IndexingOperation operation = indexShard.prepareIndex(sourceToParse, Versions.MATCH_ANY, VersionType.INTERNAL, Engine.Operation.Origin.PRIMARY, false);
                            if (logger.isTraceEnabled()) {
                                ParsedDocument parsedDoc = operation.parsedDoc();
                                logger.trace("parsedDoc id={} type={} uid={} routing={} docs={}",  operation.parsedDoc().id(), parsedDoc.type(), parsedDoc.uid(), parsedDoc.routing(), parsedDoc.docs());
                            }
                            Mapping update = operation.parsedDoc().dynamicMappingsUpdate();
                            if (update != null) { 
                                // could be necessay when cqlStruct=map or if mapping update id not yet propagated.
                                logger.debug("updating mapping=[{}]", update.toString());
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
                            logger.error("Failed to index document id=" + id() + " in index.type=" + indexInfo.name + "." + ElasticSecondaryIndex.this.baseCfs.metadata.cfName, e1);
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
        }
    }
    
    // updated when create/open/close/remove an ES index.
    private AtomicReference<MappingInfo> mappingAtomicReference = new AtomicReference();
    protected ReadWriteLock mappingInfoLock = new ReentrantReadWriteLock();
    
    public ElasticSecondaryIndex() {
        super();
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
        } catch (Throwable e) {
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



    
    public synchronized void initMapping() {
        if (ElassandraDaemon.injector() != null) {
            if (!registred) {
                getClusterService().addLast(this);
                registred = true;
            }
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
        MappingInfo mappingInfo = this.mappingAtomicReference.get();
        if (mappingInfo == null || mappingInfo.indices.size() == 0) {
            logger.warn("Elasticsearch not ready, cannot flush Elasticsearch index");
            return;
        }
        for(MappingInfo.IndexInfo indexInfo : mappingInfo.indices) {
            try {
                IndexShard indexShard = indexInfo.indexService.shard(0);
                if (indexShard != null) {
                    if (indexShard.state() == IndexShardState.STARTED)  {
                        indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                        if (logger.isDebugEnabled())
                            logger.debug("Elasticsearch index=[{}] flushed",indexInfo.name);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.warn("Cannot flush index=[{}], state=[{}]",indexInfo.name, indexShard.state());
                    }
                }
            } catch (ElasticsearchException e) {
                logger.error("Error while flushing index {}",e,indexInfo.name);
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
                String indexName = indexMetaData.getIndex();
                if ((indexName.equals(this.baseCfs.metadata.ksName)) || 
                     indexName.equals(indexMetaData.getSettings().get(IndexMetaData.SETTING_KEYSPACE)) &&
                    (event.indexRoutingTableChanged(indexMetaData.getIndex()) || 
                     event.indexMetaDataChanged(indexMetaData))) {
                        updateMapping = true;
                        break;
                    }
            }
        }
        if (updateMapping) {
            if (logger.isTraceEnabled()) logger.trace("state = {}", event.state());
            mappingInfoLock.writeLock().lock();
            try {
                this.mappingAtomicReference.set(new MappingInfo(event.state()));
                logger.debug("index=[{}.{}] metadata.version={} new mappingInfo = {}",
                        this.baseCfs.metadata.ksName, this.index_name, event.state().metaData().version(), this.mappingAtomicReference.get() );
            } finally {
                mappingInfoLock.writeLock().unlock();
            }
        }
    }
    

}
