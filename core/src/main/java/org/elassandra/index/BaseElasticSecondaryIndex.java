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
package org.elassandra.index;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.SystemKeyspace;
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
    public static boolean runsElassandra = false;
    
    volatile boolean registred = false;
    volatile String index_name;
    volatile ClusterService clusterService = null;
    final AtomicBoolean buildLaunched = new AtomicBoolean(false);
    
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
        if (registred) {
            if (logger.isDebugEnabled())
                logger.debug("Unregister from clusterService");
            clusterService().remove(this);
        }
        elasticSecondayIndices.remove(this.baseCfs.metadata.ksName+"."+this.baseCfs.name);
    }

    @Override
    public void truncateBlocking(long truncatedAt) {
        // TODO implements truncate with deleteByQuery
        logger.warn("truncateBlocking [{}] at [{}], not implemented", index_name, truncatedAt);
    } 

    /**
     * Triggered 2i built only once for all columns.
     * @see org.apache.cassandra.db.index.SecondaryIndex#buildIndexAsync()
     **/
    public Future<?> buildIndexAsync() {
        boolean isBuilt = SystemKeyspace.isIndexBuilt(baseCfs.keyspace.getName(), index_name);
        if (isBuilt)
        {
            return null;
        }

        // If the base table is empty we can directly mark the index as built.
        if (baseCfs.isEmpty())
        {
            setIndexBuilt();
            return null;
        }

        // build it asynchronously; addIndex gets called by CFS open and schema update, neither of which
        // we want to block for a long period.  (actual build is serialized on CompactionManager.)
        if (!buildLaunched.getAndSet(true)) {
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    baseCfs.forceBlockingFlush();
                    buildIndexBlocking();
                }
            };
            FutureTask<?> f = new FutureTask<Object>(runnable, null);
            new Thread(f, "Creating index: " + getIndexName()).start();
            return f;
        }
        return null;
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
        logger.trace("Reloading secondary index [{}]", index_name);
        initMapping();
    }

    @Override
    public String getNameForSystemKeyspace(ByteBuffer columnName)
    {
        return index_name;
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

    public abstract boolean isIndexing();
    
    /**
     * Returns true if the provided cell name is indexed by this secondary
     * index.
     */
    @Override
    public boolean indexes(CellName name) {
        if (isIndexing() && name != null) {
            ColumnDefinition cdef = this.baseCfs.metadata.getColumnDefinition(name);
            if (cdef != null)
                return indexes(cdef);
            /*
             * However, some type of COMPACT STORAGE layout do not store the CQL3
             * column name in the cell name and so this part can be null => return
             * true
             */
            ColumnIdentifier cql3Name = name.cql3ColumnName(this.baseCfs.metadata);
            return ((cql3Name != null) && (ByteBufferUtil.EMPTY_BYTE_BUFFER.compareTo(cql3Name.bytes) == 0));
        }
        return false;
    }

    @Override
    public long estimateResultRows() {
        return 0;
    }

    public ClusterService clusterService() {
        if (this.clusterService == null) {
            if (ElassandraDaemon.injector() == null || 
                (this.clusterService=ElassandraDaemon.injector().getInstance(ClusterService.class)) == null ) {
                throw new ElasticsearchException("ClusterService not available");
            }
        }
        return this.clusterService;
    }

}
