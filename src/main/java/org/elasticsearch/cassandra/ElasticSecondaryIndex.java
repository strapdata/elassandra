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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.ListSerializer;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.transport.Server;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.WriteFailureException;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.delete.XDeleteRequest;
import org.elasticsearch.action.delete.XDeleteResponse;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.Bytes;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class ElasticSecondaryIndex extends PerRowSecondaryIndex {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSecondaryIndex.class);

    public static final String ELASTIC_TOKEN = "_token";

    // targets='<index>.<type>,<index>.<type>...'
    public static final String ELASTIC_OPTION_TARGETS = "targets";

    protected static final ConcurrentMap<Pair<String, String>, Pair<String, String>> mapping = Maps.newConcurrentMap();

    String index_name;
    List<Pair<String, String>> targets = new ArrayList<Pair<String, String>>();

    public ElasticSecondaryIndex() {
        super();
    }

    public static Object deserialize(AbstractType type, ByteBuffer bb) {
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
            for(int i=0; i < size; i++) {
                list.add( deserialize(ltype.getElementsType(), ltype.getSerializer().getElement(bb, i)));
            }
            return list;
        } else {
            return type.compose(bb);
        }
    }

    
    public String getElasticId(ByteBuffer rowKey, ColumnFamily cf) {
        if (cf.metadata().getKeyValidatorAsCType().isCompound()) {
            return Bytes.toRawHexString(rowKey);
        }
        return cf.metadata().getKeyValidator().getString(rowKey);
    }

    /**
     * Index a mutation. Set empty field for deleted cells.
     */
    @Override
    public void index(ByteBuffer rowKey, ColumnFamily cf) {
        if (ElassandraDaemon.client() == null) {
            // TODO: save the update in a commit log to replay it later....
            logger.warn("Elasticsearch  not ready, cannot index");
            return;
        }

        // build the elastic id from row key
        // TODO: implements composite key.
        // TODO: synchronous refresh for near real time search ?
        final String id = getElasticId(rowKey, cf);
        logger.debug("indexing " + getIndexName() + " cf=" + cf.metadata().ksName + "." + cf.metadata().cfName + " rowKey=" + id + " cf=" + cf.toString());

        if (cf.hasOnlyTombstones(new Date().getTime())) {
            // update is a row delete
            deleteDoc(id);
        } else {
            // if pk is one column => do not index pk as a field.
            // if pk is composite => do index pk columns.

            Vector<Object> sourceData = new Vector<Object>((cf.getColumnCount() + 1) * 2);
            for (Cell cell : cf) {
                ColumnDefinition cd = cf.metadata().getColumnDefinition(cell.name());
                if ((cell.value() != null) && (cd != null) && (this.columnDefs.contains(cd))) {
                    Object value;
                    AbstractType<?> ctype = cd.type;
                    if (ctype instanceof ListType) {
                        ctype = ((ListType) ctype).getElementsType();
                    }
                    value = deserialize(ctype, cell.value());
                    /*
                    if (ctype instanceof UserType) {
                        value = deserializeUDT((UserType) ctype, cell.value());
                    } else {
                        value = ctype.compose(cell.value());
                    }
                    */
                    sourceData.add(cd.name.toString());
                    sourceData.add(value);
                    logger.debug("indexing row id={} column={} type={} value={}", id, cd.name, ctype, value);
                }
            }
            // add partition token to elastic index to filter on search.
            // TODO: Add support for RandomPartitionner
            IPartitioner partitioner = DatabaseDescriptor.getPartitioner();
            sourceData.add(ELASTIC_TOKEN);
            sourceData.add(partitioner.getToken(rowKey).getTokenValue());

            // submit an index request for all target index.types in options.
            for (Pair<String, String> target : targets) {
                try {
                    logger.debug("indexing in target={} source={}", target, sourceData);
                    synchronousIndex(target.left, target.right, id, sourceData.toArray(), cf.metadata().getDefaultTimeToLive(), false);
                } catch (Throwable e1) {
                    logger.error("Failed to index document id=" + id + " in index.type=" + target.left + "." + target.right, e1);
                }
            }

        }
    }

    /**
     * Rebuild a JSON document in order to generate a mapping !
     * 
     * @param source
     * @return
     */
    public BytesReference source(Object... source) {
        if (source.length % 2 != 0) {
            throw new IllegalArgumentException("The number of object passed must be even but was [" + source.length + "]");
        }
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
            builder.startObject();
            for (int i = 0; i < source.length; i++) {
                builder.field(source[i++].toString(), source[i]);
            }
            builder.endObject();
            return builder.bytes();
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate", e);
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
    private void synchronousIndex(String indexName, String type, String id, Object[] sourceData, long ttl, boolean refresh) throws Throwable {
        ClusterService clusterService = ElassandraDaemon.injector().getInstance(ClusterService.class);

        // validate, if routing is required, that we got routing
        IndexMetaData indexMetaData = clusterService.state().metaData().index(indexName);
        MappingMetaData mappingMd = indexMetaData.mappingOrDefault(type);

        IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
        IndexService indexService = indicesService.indexServiceSafe(indexName);
        IndexShard indexShard = indexService.shardSafe(0);
        SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, source(sourceData)).type(type).id(id)
        // .timestamp(request.timestamp())
                .ttl(ttl);
        long version;
        boolean created;
        try {
            Engine.IndexingOperation op;
            Engine.Index index = indexShard.prepareIndex(sourceToParse, Versions.MATCH_ANY, VersionType.INTERNAL, Engine.Operation.Origin.PRIMARY, false);

            /*
             * if (index.parsedDoc().mappingsModified()) { // mapping is done
             * via putMapping or when indexing a document (see index action) }
             */

            indexShard.index(index);
            version = index.version();
            op = index;
            created = index.created();

            if (refresh) {
                try {
                    indexShard.refresh("refresh_flag_index");
                } catch (Throwable e) {
                    // ignore
                }
            }

            logger.debug("document index.type={}.{} id={} version={} created={} ttl={} refresh={}", indexName, type, id, version, created, ttl, refresh);

        } catch (WriteFailureException e) {
            if (e.getMappingTypeToUpdate() != null) {
                DocumentMapper docMapper = indexService.mapperService().documentMapper(e.getMappingTypeToUpdate());
                if (docMapper != null) {
                    // mappingUpdatedAction.updateMappingOnMaster(indexService.index().name(),
                    // docMapper, indexService.indexUUID());
                }
            }
            throw e.getCause();
        }
    }

    /**
     * cleans up deleted columns from cassandra cleanup compaction
     *
     * @param key
     */
    @Override
    public void delete(DecoratedKey key, Group opGroup) {
        if (ElassandraDaemon.client() == null) {
            // TODO: save the update in a commit log to replay it later....
            logger.warn("Elastic node not yet started, cannot delete document");
            return;
        }
        String id = this.baseCfs.metadata.getKeyValidator().getString(key.getKey());
        deleteDoc(id);
    }

    public void deleteDoc(final String id) {
        logger.debug("deleting document from index " + getIndexName() + " id=" + id);

        for (Pair<String, String> target : targets) {
            logger.debug("xdeleting document from index={} type={} id={}", target.left, target.right, id);
            // submit a delete request
            XDeleteRequest request = new XDeleteRequest(target.left, target.right, id);
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

    @Override
    public void init() {
        ColumnDefinition cdef = getColumnDefs().iterator().next();
        for (String target : cdef.getIndexOptions().get(ELASTIC_OPTION_TARGETS).split(",")) {
            String[] indexAndType = target.split("\\.");
            if ((indexAndType != null) && (indexAndType.length == 2)) {
                Pair<String, String> targetPair = Pair.<String, String> create(indexAndType[0], indexAndType[1]);
                targets.add(targetPair);
                if (!this.baseCfs.keyspace.getName().equals(targetPair.left) || !this.baseCfs.name.equals(targetPair.right)) {
                    // specific maping from Elastic index.type to Cassandra
                    // keyspace.table
                    mapping.put(targetPair, Pair.create(this.baseCfs.keyspace.getName(), this.baseCfs.name));
                }
            } else {
                logger.warn("Ignoring invalid targets entry {}", indexAndType);
            }
        }
        index_name = this.baseCfs.name + "_" + cdef.name.toString() + "_idx";
        logger.debug("init " + getIndexName() + " targets=" + targets);

    }

    /**
     * Reload an existing index following a change to its configuration, or that
     * of the indexed column(s). Differs from init() in that we expect expect
     * new resources (such as CFS for a KEYS index) to be created by init() but
     * not here
     */
    @Override
    public void reload() {
        logger.debug("reload nothing...");
    }

    @Override
    public void validateOptions() throws ConfigurationException {
        for (ColumnDefinition cd : getColumnDefs()) {
            for (String optionKey : cd.getIndexOptions().keySet()) {
                if (!(optionKey.equals(ELASTIC_OPTION_TARGETS) || optionKey.equals(CUSTOM_INDEX_OPTION_NAME))) {
                    throw new ConfigurationException("Unknown elastic secondary index options: " + optionKey);
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
        if (ElassandraDaemon.injector() != null) {
            IndicesService indicesService = ElassandraDaemon.injector().getInstance(IndicesService.class);
            if (indicesService != null) {
                for (Pair<String, String> target : targets) {
                    IndexShard indexShard = indicesService.indexServiceSafe(target.left).shardSafe(0);
                    indexShard.flush(new FlushRequest().force(false).waitIfOngoing(true));
                    logger.debug("Elasticsearch index {} flushed");
                }
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
            logger.debug("removeIndex {}", ByteBufferUtil.string(columnName));
        } catch (CharacterCodingException e) {
            logger.warn("removeIndex error", e);
        }
    }

    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        logger.debug("invalidate");
    }

    @Override
    public void truncateBlocking(long truncatedAt) {
        // TODO implements truncate
        logger.warn("truncateBlocking at {}, not implemented in ElasticSearch", truncatedAt);
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

}
