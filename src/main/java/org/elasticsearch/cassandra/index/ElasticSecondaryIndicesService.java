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
import java.util.Iterator;
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
import org.elasticsearch.index.IndexService;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.indices.IndicesService;


/**
 * Create cassandra secondary indices when all shards are started and metadata applied on all nodes.
 * Remove cassandra secondary indices 
 * @author vroyer
 *
 */
public class ElasticSecondaryIndicesService extends AbstractLifecycleComponent<SecondaryIndicesService> implements SecondaryIndicesService {
    
    public static String TASK_THREAD_NAME = "secondaryIndiceService#taskExecutor";
    
    private volatile PrioritizedEsThreadPoolExecutor tasksExecutor;
    private final CopyOnWriteArraySet<String> toUpdateIndices = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArrayList<SecondaryIndicesService.DeleteListener> deleteListeners = new CopyOnWriteArrayList<SecondaryIndicesService.DeleteListener>();
    
    private final IndicesService indicesService;
    private final IndicesLifecycle indicesLifecycle;
    private final ClusterService clusterService;
    
    @Inject
    public ElasticSecondaryIndicesService(Settings settings,  ClusterService clusterService,
            IndicesService indicesService, IndicesLifecycle indicesLifecycle) {
        super(settings);
        this.clusterService = clusterService;
        this.indicesService = indicesService;
        this.indicesLifecycle = indicesLifecycle;
    }
    
    public void addDeleteListener(DeleteListener listener) {
        deleteListeners.add(listener);
    }
    
    public void removeDeleteListener(DeleteListener listener) {
        deleteListeners.remove(listener);
    }
    
    abstract class Task extends PrioritizedRunnable {
        private final long creationTime;
        final String index;
        final String ksName;
        
       
        public Task(final String index, final String ksName) {
            super(Priority.NORMAL);
            this.index = index;
            this.ksName = ksName;
            this.creationTime = System.currentTimeMillis();
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
        public CreateSecondaryIndexTask(final String index, final String ksName) {
            super(index, ksName);
        }
        @Override
        public void execute() {
            try {
                logger.debug("Creating secondary indices for keyspace [{}]", ksName);
                clusterService.createSecondaryIndices(ksName);
            } catch (IOException e) {
                logger.error("Failed to create secondary indices on [{}]", e, ksName);
            }
        }
    }
    
    class DropSecondaryIndexTask extends Task {
        public DropSecondaryIndexTask(final String index, final String ksName) {
            super(index, ksName);
        }
        @Override
        public void execute() {
            boolean isLastRemainingIndex = true;
            for(Iterator<IndexService> it = indicesService.iterator(); it.hasNext(); ) {
                IndexService indexService2 = it.next();
                if (ksName.equals(indexService2.index().name())) {
                    continue;
                }
                if (ksName.equals(indexService2.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, indexService2.index().name()))) {
                    isLastRemainingIndex = false;
                }
            }
            
            if (isLastRemainingIndex) {
                try {
                    logger.debug("Dropping secondary indices for keyspace [{}]", ksName);
                    clusterService.dropSecondaryIndices(ksName);
                } catch (RequestExecutionException e) {
                    logger.error("Failed to create secondary indices on {}", ksName);
                }
            }
           
        }
    }
    
    public void submitTask(Task task) {
        if (!lifecycle.started()) {
            return;
        }
        logger.debug("submit new task task class={} ", task.getClass().getName());
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
        // TODO Auto-generated method stub
        this.tasksExecutor = EsExecutors.newSinglePrioritizing("SecondaryIndicesService",daemonThreadFactory(settings, TASK_THREAD_NAME));
        logger.debug("{} started.",TASK_THREAD_NAME);
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        // TODO Auto-generated method stub
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

    /**
     * Asynchronously remove secondary indices.
     */
    public void dropSecondaryIndices(String index) {
        IndexService indexService = this.indicesService.indexService(index);
        String ksName = indexService.indexSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, indexService.index().name() );
        submitTask(new DropSecondaryIndexTask(index, ksName));
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        boolean toUpdateIndicesChanged = false;
        if (event.metaDataChanged()) {
            for(Iterator<String> it = event.state().metaData().indices().keysIt(); it.hasNext(); ) {
                String index = it.next();
                if (event.indexMetaDataChanged(event.state().metaData().index(index)) && event.state().metaData().index(index).getMappings().size() > 0) {
                    this.toUpdateIndices.add(index);
                    toUpdateIndicesChanged = true;
                }
            }
        }
        
        if (event.routingTableChanged() || toUpdateIndicesChanged) {
            for(Iterator<String> it = this.toUpdateIndices.iterator(); it.hasNext(); ) {
                String index = it.next();
                IndexRoutingTable indexRoutingTable = event.state().routingTable().index(index);
                if (indexRoutingTable == null) {
                    logger.warn("index [{}] not in routing table, keyspace may be deleted.",index);
                    continue;
                }
                if (indexRoutingTable.allPrimaryShardsActive()) {
                    logger.debug("index=[{}] shards Active/Unassigned={}/{} => asynchronous creates secondary index", 
                            index, indexRoutingTable.primaryShardsActive(), indexRoutingTable.primaryShardsUnassigned());
                    IndexMetaData indexMetaData = event.state().metaData().index(index);
                    submitTask(new CreateSecondaryIndexTask(index, indexMetaData.getSettings().get(IndexMetaData.SETTING_KEYSPACE_NAME, index)));
                    this.toUpdateIndices.remove(index);
                } else {
                    logger.debug("index=[{}] shards Active/Unassigned={}/{} => waiting next cluster state to create secondary indices", 
                            index, indexRoutingTable.primaryShardsActive(), indexRoutingTable.primaryShardsUnassigned());
                }
            }
        }
        
        // notify listeners that all shards are deleted.
        for(DeleteListener deleteListener : this.deleteListeners) {
            if (!event.state().routingTable().hasIndex(deleteListener.index())) {
                deleteListener.onIndexDeleted();
            }
        }
    }

}
