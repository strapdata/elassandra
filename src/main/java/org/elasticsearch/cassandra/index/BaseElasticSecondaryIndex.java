package org.elasticsearch.cassandra.index;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.composites.CellName;
import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.transport.Server;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.internal.VersionFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;

import com.google.common.collect.Sets;

public abstract class BaseElasticSecondaryIndex extends PerRowSecondaryIndex implements ClusterStateListener {
    static final ESLogger logger = Loggers.getLogger(BaseElasticSecondaryIndex.class);

    public static final Set<BaseElasticSecondaryIndex> elasticSecondayIndices = Sets.newConcurrentHashSet();
    public static final Field DEFAULT_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, -1L);
    
    protected String index_name;
    protected IPartitioner partitioner = DatabaseDescriptor.getPartitioner();
    protected ReadWriteLock lock = new ReentrantReadWriteLock();
    protected ClusterService clusterService = null;

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
     * Reload an existing index following a change to its configuration, or that
     * of the indexed column(s). Differs from init() in that we expect expect
     * new resources (such as CFS for a KEYS index) to be created by init() but
     * not here
     */
    @Override
    public void reload() {
    }

    
    public abstract void initMapping();

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

    public ClusterService getClusterService() {
        if (this.clusterService == null) {
            lock.writeLock().lock();
            try {
                if (ElassandraDaemon.injector() == null || 
                    (this.clusterService=ElassandraDaemon.injector().getInstance(ClusterService.class)) == null ) {
                    throw new ElasticsearchException("ClusterService not available");
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return this.clusterService;
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb) {
        return deserialize(type, bb, null);
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb, Mapper mapper) {
        if (type instanceof UserType) {
            UserType utype = (UserType) type;
            Map<String, Object> mapValue = new HashMap<String, Object>();
            ByteBuffer[] components = utype.split(bb);
            for (int i = 0; i < components.length; i++) {
                String fieldName = UTF8Type.instance.compose(utype.fieldName(i));
                AbstractType<?> ctype = utype.type(i);
                Mapper subMapper = null;
                if (mapper != null) {
                    assert mapper instanceof ObjectMapper : "expecting an object mapper";
                    subMapper = ((ObjectMapper)mapper).getMapper(fieldName);
                }
                Object value = (components[i] == null) ? null : deserialize(ctype, components[i], subMapper);
                mapValue.put(fieldName, value);
            }
            return mapValue;
        } else if (type instanceof ListType) {
            ListType ltype = (ListType)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, Server.VERSION_3);
            List list = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                list.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, Server.VERSION_3), mapper));
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
                set.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, Server.VERSION_3), mapper));
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
                Mapper subMapper = null;
                if (mapper != null) {
                    assert mapper instanceof ObjectMapper : "expecting an object mapper";
                    subMapper = ((ObjectMapper)mapper).getMapper(key);
                }
                map.put(key, deserialize(ltype.getValuesType(), vbb, subMapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return map;
        } else {
             Object value = type.compose(bb);
             if (mapper != null && mapper instanceof FieldMapper) {
                 return ((FieldMapper)mapper).fieldType().valueForSearch(value);
             }
             return value;
        }
    }
}
