/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cluster.metadata;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.cassandra.index.SecondaryIndicesService;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.TimeoutClusterStateUpdateTask;
import org.elasticsearch.cluster.action.index.NodeIndexDeletedAction;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.threadpool.ThreadPool;

/**
 *
 */
public class MetaDataDeleteIndexService extends AbstractComponent {

    private final ThreadPool threadPool;

    private final ClusterService clusterService;

    private final NodeIndexDeletedAction nodeIndexDeletedAction;

    private final MetaDataService metaDataService;
    
    private final SecondaryIndicesService secondaryIndicesService;

    @Inject
    public MetaDataDeleteIndexService(Settings settings, ThreadPool threadPool, ClusterService clusterService,
                                      NodeIndexDeletedAction nodeIndexDeletedAction, MetaDataService metaDataService,
                                      SecondaryIndicesService secondaryIndicesService) {
        super(settings);
        this.threadPool = threadPool;
        this.clusterService = clusterService;
        this.nodeIndexDeletedAction = nodeIndexDeletedAction;
        this.metaDataService = metaDataService;
        this.secondaryIndicesService = secondaryIndicesService;
    }

    public void deleteIndex(final Request request, final Listener userListener) {
        // we lock here, and not within the cluster service callback since we don't want to
        // block the whole cluster state handling
        final Semaphore mdLock = metaDataService.indexMetaDataLock(request.index);

        // quick check to see if we can acquire a lock, otherwise spawn to a thread pool
        if (mdLock.tryAcquire()) {
            deleteIndex(request, userListener, mdLock);
            return;
        }

        threadPool.executor(ThreadPool.Names.MANAGEMENT).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!mdLock.tryAcquire(request.masterTimeout.nanos(), TimeUnit.NANOSECONDS)) {
                        userListener.onFailure(new ProcessClusterEventTimeoutException(request.masterTimeout, "acquire index lock"));
                        return;
                    }
                } catch (InterruptedException e) {
                    userListener.onFailure(e);
                    return;
                }

                deleteIndex(request, userListener, mdLock);
            }
        });
    }

    private void deleteIndex(final Request request, final Listener userListener, Semaphore mdLock) {
        final DeleteIndexListener listener = new DeleteIndexListener(mdLock, userListener);
        clusterService.submitStateUpdateTask("delete-index [" + request.index + "]", Priority.URGENT, new TimeoutClusterStateUpdateTask() {

            @Override
            public TimeValue timeout() {
                return request.masterTimeout;
            }

            @Override
            public void onFailure(String source, Throwable t) {
                listener.onFailure(t);
            }

            @Override
            public boolean doPresistMetaData() {
                return true;
            }
            
            @Override
            public ClusterState execute(final ClusterState currentState) {
                if (!currentState.metaData().hasConcreteIndex(request.index)) {
                    throw new IndexNotFoundException(request.index);
                }

                logger.info("deleting index [{}] ", request.index);

                MetaData newMetaData = MetaData.builder(currentState.metaData()).remove(request.index).build();
                ClusterBlocks newblocks = ClusterBlocks.builder().blocks(currentState.blocks()).removeIndexBlocks(request.index).build();
                ClusterState newCurrentState = ClusterState.builder(currentState).blocks(newblocks).metaData(newMetaData).build();
                
                RoutingTable routingTable = RoutingTable.build(clusterService, newCurrentState);

                newCurrentState = ClusterState.builder(newCurrentState)
                			.incrementVersion()
                			.blocks(newblocks)
                			.metaData(newMetaData)
                			.routingTable(routingTable)	// update routing before deleting the index in the CassandraIndicesClusterStateService.
                			.build();
                if (logger.isTraceEnabled()) 
                	logger.trace("newClusterState = {}", newCurrentState);
                
                final IndexMetaData indexMetaData = currentState.metaData().index(request.index);
                MetaDataDeleteIndexService.this.secondaryIndicesService.addDeleteListener(new SecondaryIndicesService.DeleteListener() {
                    @Override 
                    public IndexMetaData mapping() {
                        return indexMetaData;
                    }
                    @Override
                    public void onIndexDeleted() {
                        listener.onResponse(new Response(true));
                        MetaDataDeleteIndexService.this.secondaryIndicesService.removeDeleteListener(this);
                    }
                });
                
                listener.future = threadPool.schedule(request.timeout, ThreadPool.Names.SAME, new Runnable() {
                    @Override
                    public void run() {
                        listener.onResponse(new Response(false));
                    }
                });
                
                return newCurrentState;
            }

            @Override
            public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                logger.debug("index [{}] deleted from cluster state", request.index);
            }
        });
    }

    class DeleteIndexListener implements Listener {

        private final AtomicBoolean notified = new AtomicBoolean();
        private final Semaphore mdLock;
        private final Listener listener;
        volatile ScheduledFuture<?> future;

        private DeleteIndexListener(Semaphore mdLock, Listener listener) {
            this.mdLock = mdLock;
            this.listener = listener;
        }

        @Override
        public void onResponse(final Response response) {
            if (notified.compareAndSet(false, true)) {
                mdLock.release();
                FutureUtils.cancel(future);
                listener.onResponse(response);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (notified.compareAndSet(false, true)) {
                mdLock.release();
                FutureUtils.cancel(future);
                listener.onFailure(t);
            }
        }
    }


    public static interface Listener {

        void onResponse(Response response);

        void onFailure(Throwable t);
    }

    public static class Request {

        final String index;

        TimeValue timeout = TimeValue.timeValueSeconds(10);
        TimeValue masterTimeout = MasterNodeRequest.DEFAULT_MASTER_NODE_TIMEOUT;

        public Request(String index) {
            this.index = index;
        }

        public Request timeout(TimeValue timeout) {
            this.timeout = timeout;
            return this;
        }

        public Request masterTimeout(TimeValue masterTimeout) {
            this.masterTimeout = masterTimeout;
            return this;
        }
    }

    public static class Response {
        private final boolean acknowledged;

        public Response(boolean acknowledged) {
            this.acknowledged = acknowledged;
        }

        public boolean acknowledged() {
            return acknowledged;
        }
    }
}
