/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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

import static org.elasticsearch.common.util.concurrent.EsExecutors.daemonThreadFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.exceptions.RequestExecutionException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.common.util.concurrent.PrioritizedEsThreadPoolExecutor;
import org.elasticsearch.common.util.concurrent.PrioritizedRunnable;


/**
 * Create cassandra secondary indices when all shards are started and metadata applied on all nodes.
 * Remove cassandra secondary indices 
 * @author vroyer
 *
 */
public class ElasticSecondaryIndicesService extends AbstractLifecycleComponent<SecondaryIndicesService> implements SecondaryIndicesService {
    
    public static String TASK_THREAD_NAME = "secondaryIndiceService#taskExecutor";
    
    private volatile PrioritizedEsThreadPoolExecutor tasksExecutor;
    private final CopyOnWriteArraySet<String> toMonitorIndices = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<String> toUpdateIndices = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArrayList<SecondaryIndicesService.DeleteListener> deleteListeners = new CopyOnWriteArrayList<SecondaryIndicesService.DeleteListener>();
    
    private final ClusterService clusterService;
    
    @Inject
    public ElasticSecondaryIndicesService(Settings settings,  ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
    }
    
    public void addDeleteListener(DeleteListener listener) {
        deleteListeners.add(listener);
    }
    
    public void removeDeleteListener(DeleteListener listener) {
        deleteListeners.remove(listener);
    }
    
    public void monitorIndex(String index) {
        toMonitorIndices.add(index);
    }
    
    abstract class Task extends PrioritizedRunnable {
        final long creationTime;
        final ClusterService clusterService;
        final IndexMetaData indexMetaData;
       
        public Task(final IndexMetaData indexMetaData) {
            super(Priority.NORMAL);
            this.indexMetaData = indexMetaData;
            this.creationTime = System.currentTimeMillis();
            this.clusterService = ElasticSecondaryIndicesService.this.clusterService;
        }

        @Override
        public void run() {
            if (!lifecycle.started()) {
                logger.debug("processing [{}]: ignoring, not started");
                return;
            }
            execute();
        }
        
        public abstract void execute();
    }
    
    class CreateSecondaryIndexTask extends Task {
        public CreateSecondaryIndexTask(final IndexMetaData indexMetaData) {
            super(indexMetaData);
        }
        @Override
        public void execute() {
            try {
                logger.debug("Creating secondary indices for keyspace [{}]", indexMetaData.keyspace());
                this.clusterService.createSecondaryIndices(indexMetaData);
            } catch (IOException e) {
                logger.error("Failed to create secondary indices on [{}]", e, indexMetaData.keyspace());
            }
        }
    }
    
    class DropSecondaryIndexTask extends Task {
        public DropSecondaryIndexTask(final IndexMetaData indexMetaData) {
            super(indexMetaData);
        }
        @Override
        public void execute() {
            try {
                logger.debug("Dropping secondary indices for keyspace [{}]", indexMetaData.keyspace());
                clusterService.dropSecondaryIndices(indexMetaData);
            } catch (RequestExecutionException e) {
                logger.error("Failed to create secondary indices on {}", indexMetaData.keyspace());
            }
        }
    }
    
    public void submitTask(Task task) {
        if (!lifecycle.started()) {
            return;
        }
        logger.debug("submit new task class={} for index={} type={}", task.getClass().getName(), task.indexMetaData.getIndex(), task.indexMetaData.getMappings());
        try {
            tasksExecutor.execute(task);
        } catch (EsRejectedExecutionException e) {
            // ignore cases where we are shutting down..., there is really nothing interesting
            // to be done here...
            if (!lifecycle.stoppedOrClosed()) {
                throw e;
            }
        }
    }
    
    @Override
    protected void doStart() throws ElasticsearchException {
        this.tasksExecutor = EsExecutors.newSinglePrioritizing("SecondaryIndicesService",daemonThreadFactory(settings, TASK_THREAD_NAME));
        logger.debug("{} started.",TASK_THREAD_NAME);
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        tasksExecutor.shutdown();
        try {
            tasksExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        logger.debug("toMonitorIndices={} toUpdateIndices={}",toMonitorIndices, toUpdateIndices);
        
        for(String index: toMonitorIndices) {
            if (event.state().metaData().index(index).getMappings().size() > 0 && event.indexMetaDataChanged(event.state().metaData().index(index))) {
                this.toUpdateIndices.add(index);
            }
        }
        this.toMonitorIndices.removeAll(toUpdateIndices);
        
        if (toUpdateIndices.size() > 0) {
            for(String index : toUpdateIndices) {
                IndexRoutingTable indexRoutingTable = event.state().routingTable().index(index);
                if (indexRoutingTable == null) {
                    logger.warn("index [{}] not in routing table, keyspace may be deleted.",index);
                    continue;
                }
                if (indexRoutingTable.allPrimaryShardsActive()) {
                    logger.debug("index=[{}] shards Active/Unassigned={}/{} => asynchronously creates secondary index", 
                            index, indexRoutingTable.primaryShardsActive(), indexRoutingTable.primaryShardsUnassigned());
                    IndexMetaData indexMetaData = event.state().metaData().index(index);
                    submitTask(new CreateSecondaryIndexTask(indexMetaData));
                    this.toUpdateIndices.remove(index);
                } else {
                    logger.debug("index=[{}] shards Active/Unassigned={}/{} => waiting next cluster state to create secondary indices", 
                            index, indexRoutingTable.primaryShardsActive(), indexRoutingTable.primaryShardsUnassigned());
                }
            }
        }
        
        // notify listeners that all shards are deleted.
        Map<String, IndexMetaData> unindexableKeyspace = null;
        for(DeleteListener deleteListener : this.deleteListeners) {
            if (!event.state().routingTable().hasIndex(deleteListener.mapping().getIndex())) {
                // all shard are inactive => notify listeners
                deleteListener.onIndexDeleted();
                if (unindexableKeyspace == null)
                    unindexableKeyspace = new HashMap<String, IndexMetaData>();
                unindexableKeyspace.put(deleteListener.mapping().keyspace(), deleteListener.mapping());
            }
        }
        
        // check if there is still some indices pointing on the unindexed keyspace
        if (unindexableKeyspace != null) {
            for(Iterator<IndexMetaData> i = event.state().metaData().iterator(); i.hasNext(); ) {
                IndexMetaData indexMetaData = i.next();
                unindexableKeyspace.remove(indexMetaData.keyspace());
            }
            if (logger.isTraceEnabled())
                logger.trace("unindexableKeyspace = {}", unindexableKeyspace);
            for(String keyspace : unindexableKeyspace.keySet()) {
                logger.debug("asynchronously dropping secondary index for keyspace=[{}]", keyspace);
                submitTask(new DropSecondaryIndexTask(unindexableKeyspace.get(keyspace)));
            }
        }
    }

}
