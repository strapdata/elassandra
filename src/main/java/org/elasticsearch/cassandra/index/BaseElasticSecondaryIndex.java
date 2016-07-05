package org.elasticsearch.cassandra.index;

import java.nio.ByteBuffer;
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
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.mapper.internal.VersionFieldMapper;

import com.google.common.collect.Maps;

public abstract class BaseElasticSecondaryIndex extends PerRowSecondaryIndex implements ClusterStateListener {
    ESLogger logger = Loggers.getLogger(BaseElasticSecondaryIndex.class);

    public static final Map<String, BaseElasticSecondaryIndex> elasticSecondayIndices = Maps.newConcurrentMap();
    public static final Field DEFAULT_VERSION = new NumericDocValuesField(VersionFieldMapper.NAME, -1L);
    
    volatile boolean registred = false;
    volatile String index_name;
    volatile ClusterService clusterService = null;
    protected IPartitioner partitioner = DatabaseDescriptor.getPartitioner();
    
    // init write lock
    private ReadWriteLock initLock = new ReentrantReadWriteLock();
    
    
    @Override
    public ColumnFamilyStore getIndexCfs() {
        return null;
    }

    @Override
    public void removeIndex(ByteBuffer columnName) {
        
    }

    @Override
    public void invalidate() {
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
     * not here.
     * 
     * Called when removing a secondary index on a column.
     */
    @Override
    public void reload() {
        logger.trace("Reloading secondary index {}", index_name);
        initMapping();
    }

    
    public abstract void initMapping();

    @Override
    public void init() {
        initLock.writeLock().lock();
        try {
            index_name = "elastic_"+this.baseCfs.name;
            logger = Loggers.getLogger(this.getClass().getName()+"."+this.baseCfs.metadata.ksName+"."+this.baseCfs.name);
            elasticSecondayIndices.put(this.baseCfs.metadata.ksName+"."+this.baseCfs.name,this);
            initMapping();
        } finally {
            initLock.writeLock().unlock();
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
         * However, some type of COMPACT STORAGE layout do not store the CQL3
         * column name in the cell name and so this part can be null => return
         * true
         */
        ColumnIdentifier cql3Name = name.cql3ColumnName(this.baseCfs.metadata);
        return ((cql3Name != null) && (ByteBufferUtil.EMPTY_BYTE_BUFFER.compareTo(cql3Name.bytes) == 0));
    }

    @Override
    public long estimateResultRows() {
        return 0;
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

}
