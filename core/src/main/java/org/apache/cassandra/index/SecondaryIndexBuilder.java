/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.index;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.cassandra.concurrent.NamedThreadFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.compaction.CompactionInfo;
import org.apache.cassandra.db.compaction.CompactionInterruptedException;
import org.apache.cassandra.db.compaction.CompactionManager;
import org.apache.cassandra.db.compaction.OperationType;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.io.sstable.ReducingKeyIterator;
import org.apache.cassandra.utils.UUIDGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages building an entire index from column family data. Runs on to compaction manager.
 */
public class SecondaryIndexBuilder extends CompactionInfo.Holder
{
    protected static final Logger logger = LoggerFactory.getLogger(CompactionManager.class);
    
    private final ColumnFamilyStore cfs;
    private final Set<Index> indexers;
    private final ReducingKeyIterator iter;
    private final UUID compactionId;
    
    public static int queue_depth = Integer.parseInt(System.getProperty("rebuild_index_queue_depth","128"));
    
    private final int indexThreads;
    private final boolean isMultithreaded;
    private BlockingQueue<DecoratedKey>[] queues;
    private AtomicBoolean finished;
    
    public SecondaryIndexBuilder(ColumnFamilyStore cfs, Set<Index> indexers, ReducingKeyIterator iter)
    {
        this(1, cfs, indexers, iter);
    }
    
    public SecondaryIndexBuilder(int indexThreads, ColumnFamilyStore cfs, Set<Index> indexers, ReducingKeyIterator iter)
    {
        this.indexThreads = indexThreads;
        this.cfs = cfs;
        this.indexers = indexers;
        this.iter = iter;
        this.compactionId = UUIDGen.getTimeUUID();
        
        isMultithreaded = (DatabaseDescriptor.getPartitioner() instanceof Murmur3Partitioner) && (indexThreads > 1);
        if (isMultithreaded) 
        {
            finished = new AtomicBoolean(false);
            queues = new BlockingQueue[indexThreads];
            for(int i=0; i < indexThreads; i++)
                this.queues[i] = new LinkedBlockingQueue<DecoratedKey>(queue_depth);
        }
    }

    public CompactionInfo getCompactionInfo()
    {
        return new CompactionInfo(cfs.metadata,
                                  OperationType.INDEX_BUILD,
                                  iter.getBytesRead(),
                                  iter.getTotalBytes(),
                                  compactionId);
    }
    
    public void build()
    {
        ExecutorService indexExecutor = null;
        AtomicLong indexedRows = null;
        
        long start = System.currentTimeMillis();
        try
        {
            if (isMultithreaded) 
            {
                indexedRows = new AtomicLong(0L);
                indexExecutor = Executors.newFixedThreadPool(indexThreads, new NamedThreadFactory("IndexBuilder-"+compactionId));
                for(int i=0; i < indexThreads; i++)
                    indexExecutor.execute(new IndexBuilder(this.queues[i], indexedRows));
            }
            
            while (iter.hasNext())
            {
                if (isStopRequested()) 
                {
                    if (isMultithreaded) {
                        logger.debug(compactionId+" stopped.");
                        indexExecutor.shutdownNow();
                    }
                    throw new CompactionInterruptedException(getCompactionInfo());
                }
                DecoratedKey key = iter.next();
                
                if (isMultithreaded) 
                {
                    Long token = (Long) key.getToken().getTokenValue();
                    try {
                        this.queues[ (int) (((token % indexThreads) + indexThreads) % indexThreads) ].put(key);
                    } catch (InterruptedException e) {
                        logger.error(compactionId+" failed to put key "+key);
                    }
                } else {
                    Keyspace.indexPartition(key, cfs, indexers);
                }
            }
        }
        finally
        {
            iter.close();
            
            try {
                if (isMultithreaded) 
                {
                    logger.debug(compactionId+" awating termination of index rebuild on "+cfs.metadata.ksName+"."+cfs.metadata.cfName);
                    finished.set(true);
                    indexExecutor.shutdown();
                    indexExecutor.awaitTermination(60, TimeUnit.SECONDS);
                    long duration = System.currentTimeMillis() - start;
                    logger.debug(compactionId+" index rebuild terminated, "+indexedRows.get()+" partitions, duration = " + (duration/1000) + "s");
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            
        }
    }
    
    public class IndexBuilder implements Runnable {
        BlockingQueue<DecoratedKey> queue;
        AtomicLong indexedRows;
        
        public IndexBuilder(BlockingQueue<DecoratedKey> q, AtomicLong i) 
        {
            this.queue = q;
            this.indexedRows  = i;
        }
        
        @Override
        public void run() 
        {
            int rowCount = 0;
            try 
            {
                DecoratedKey key;
                while (true)
                {
                    key = queue.poll(5, TimeUnit.SECONDS);
                    if (finished.get() && (key == null))
                        break;
                    
                    if (key != null) 
                    {
                        Keyspace.indexPartition(key, cfs, indexers);
                        rowCount++;
                    }
                }
            } catch (Exception e) {
                logger.error("error:",e);
            } 
            indexedRows.addAndGet(rowCount);
            logger.debug(rowCount + " partitions indexed.");
        }
    }
}
