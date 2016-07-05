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

package org.elasticsearch.cluster.service;

import static org.elasticsearch.common.util.concurrent.EsExecutors.daemonThreadFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.Version;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cassandra.ConcurrentMetaDataUpdateException;
import org.elasticsearch.cassandra.NoPersistedMetaDataException;
import org.elasticsearch.cassandra.cluster.routing.AbstractSearchStrategy;
import org.elasticsearch.cassandra.gateway.CassandraGatewayService;
import org.elasticsearch.cassandra.index.SecondaryIndicesService;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.LocalNodeMasterListener;
import org.elasticsearch.cluster.ProcessedClusterStateUpdateTask;
import org.elasticsearch.cluster.TimeoutClusterStateListener;
import org.elasticsearch.cluster.TimeoutClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.ProcessClusterEventTimeoutException;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.node.DiscoveryNodeService;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.StringText;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.common.util.concurrent.CountDown;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.common.util.concurrent.PrioritizedEsThreadPoolExecutor;
import org.elasticsearch.common.util.concurrent.PrioritizedRunnable;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.settings.NodeSettingsService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.google.common.collect.Iterables;

/**
 *
 */
public abstract class InternalClusterService extends AbstractLifecycleComponent<ClusterService> implements ClusterService {

    public static final String SETTING_CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD = "cluster.service.slow_task_logging_threshold";
    public static final String SETTING_CLUSTER_SERVICE_RECONNECT_INTERVAL = "cluster.service.reconnect_interval";

    public static final String UPDATE_THREAD_NAME = "clusterService#updateTask";
    private final ThreadPool threadPool;

    private final DiscoveryService discoveryService;

    private final OperationRouting operationRouting;

    private final TransportService transportService;

    private final NodeSettingsService nodeSettingsService;
    private final DiscoveryNodeService discoveryNodeService;
    private final SecondaryIndicesService secondaryIndicesService;
    private final IndicesService indicesService;
    
    private final Version version;

    private final TimeValue reconnectInterval;

    private TimeValue slowTaskLoggingThreshold;

    private volatile PrioritizedEsThreadPoolExecutor updateTasksExecutor;

    /**
     * Those 3 state listeners are changing infrequently - CopyOnWriteArrayList is just fine
     */
    private final Collection<ClusterStateListener> priorityClusterStateListeners = new CopyOnWriteArrayList<>();
    private final Collection<ClusterStateListener> clusterStateListeners = new CopyOnWriteArrayList<>();
    private final Collection<ClusterStateListener> lastClusterStateListeners = new CopyOnWriteArrayList<>();
    // TODO this is rather frequently changing I guess a Synced Set would be better here and a dedicated remove API
    private final Collection<ClusterStateListener> postAppliedListeners = new CopyOnWriteArrayList<>();
    private final Iterable<ClusterStateListener> preAppliedListeners = Iterables.concat(
            priorityClusterStateListeners,
            clusterStateListeners,
            lastClusterStateListeners);

    private final LocalNodeMasterListeners localNodeMasterListeners;

    private final Queue<NotifyTimeout> onGoingTimeouts = ConcurrentCollections.newQueue();

    private volatile ClusterState clusterState;

    private final ClusterBlocks.Builder initialBlocks;

    private volatile ScheduledFuture reconnectToNodes;

    @Inject
    public InternalClusterService(Settings settings, DiscoveryService discoveryService, OperationRouting operationRouting, TransportService transportService,
                                  NodeSettingsService nodeSettingsService, ThreadPool threadPool, ClusterName clusterName, DiscoveryNodeService discoveryNodeService,
                                  Version version, SecondaryIndicesService secondaryIndicesService, IndicesService indicesService) {
        super(settings);
        this.operationRouting = operationRouting;
        this.transportService = transportService;
        this.discoveryService = discoveryService;
        this.threadPool = threadPool;
        this.nodeSettingsService = nodeSettingsService;
        this.discoveryNodeService = discoveryNodeService;
        this.secondaryIndicesService = secondaryIndicesService;
        this.indicesService = indicesService;
        this.version = version;

        // will be replaced on doStart.
        this.clusterState = ClusterState.builder(clusterName).build();

        this.nodeSettingsService.setClusterService(this);
        this.nodeSettingsService.addListener(new ApplySettings());

        this.reconnectInterval = this.settings.getAsTime(SETTING_CLUSTER_SERVICE_RECONNECT_INTERVAL, TimeValue.timeValueSeconds(10));

        this.slowTaskLoggingThreshold = this.settings.getAsTime(SETTING_CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD, TimeValue.timeValueSeconds(30));

        localNodeMasterListeners = new LocalNodeMasterListeners(threadPool);

        //initialBlocks = ClusterBlocks.builder().addGlobalBlock(discoveryService.getNoMasterBlock());
        // No more master election
        //initialBlocks = ClusterBlocks.builder().addGlobalBlock(discoveryService.getNoMasterBlock());
        // Add NO_CASSANDRA_RING_BLOCK to avoid to save metadata while cassandra ring not ready.
        initialBlocks = ClusterBlocks.builder().addGlobalBlock(CassandraGatewayService.NO_CASSANDRA_RING_BLOCK);
    }

    public NodeSettingsService settingsService() {
        return this.nodeSettingsService;
    }

    @Override
    public IndexService indexService(String index) {
        return this.indicesService.indexService(index);
    }
    
    @Override
    public void addInitialStateBlock(ClusterBlock block) throws IllegalStateException {
        if (lifecycle.started()) {
            throw new IllegalStateException("can't set initial block when started");
        }
        initialBlocks.addGlobalBlock(block);
    }

    @Override
    public void removeInitialStateBlock(ClusterBlock block) throws IllegalStateException {
        if (lifecycle.started()) {
            throw new IllegalStateException("can't set initial block when started");
        }
        initialBlocks.removeGlobalBlock(block);
    }

    @Override
    protected void doStart() {
        add(localNodeMasterListeners);
        this.clusterState = ClusterState.builder(clusterState).blocks(initialBlocks).build();
        this.updateTasksExecutor = EsExecutors.newSinglePrioritizing(UPDATE_THREAD_NAME, daemonThreadFactory(settings, UPDATE_THREAD_NAME));
        this.reconnectToNodes = threadPool.schedule(reconnectInterval, ThreadPool.Names.GENERIC, new ReconnectToNodes());
        
        Map<String, String> nodeAttributes = discoveryNodeService.buildAttributes();
        // note, we rely on the fact that its a new id each time we start, see FD and "kill -9" handling
        final String nodeId = DiscoveryService.generateNodeId(settings);
        final TransportAddress publishAddress = transportService.boundAddress().publishAddress();
        DiscoveryNode localNode = new DiscoveryNode(settings.get("name"), nodeId, publishAddress, nodeAttributes, version);
        DiscoveryNodes.Builder nodeBuilder = DiscoveryNodes.builder().put(localNode).localNodeId(localNode.id());
        this.clusterState = ClusterState.builder(clusterState).nodes(nodeBuilder).blocks(initialBlocks).build();
        this.transportService.setLocalNode(localNode);
        
        try {
            updateElasticAdminKeyspace();
        } catch (Exception e) {
            logger.error("Failed to update elastic_admin keyspace", e);
        }
        
        // addPost because 2i shoukd be created/deleted after that cassandra indices have taken the new mapping.
        addLast(this.secondaryIndicesService);
    }
    
    @Override
    protected void doStop() {
        FutureUtils.cancel(this.reconnectToNodes);
        for (NotifyTimeout onGoingTimeout : onGoingTimeouts) {
            onGoingTimeout.cancel();
            onGoingTimeout.listener.onClose();
        }
        ThreadPool.terminate(updateTasksExecutor, 10, TimeUnit.SECONDS);
        remove(localNodeMasterListeners);
    }

    @Override
    protected void doClose() {
    }

    @Override
    public DiscoveryNode localNode() {
        return clusterState.getNodes().localNode();
    }

    @Override
    public OperationRouting operationRouting() {
        return operationRouting;
    }

    @Override
    public ClusterState state() {
        return this.clusterState;
    }

    @Override
    public void addFirst(ClusterStateListener listener) {
        priorityClusterStateListeners.add(listener);
    }

    @Override
    public void addLast(ClusterStateListener listener) {
        lastClusterStateListeners.add(listener);
    }

    @Override
    public void add(ClusterStateListener listener) {
        clusterStateListeners.add(listener);
    }
    
    @Override
    public void remove(ClusterStateListener listener) {
        clusterStateListeners.remove(listener);
        priorityClusterStateListeners.remove(listener);
        lastClusterStateListeners.remove(listener);
        postAppliedListeners.remove(listener);
        for (Iterator<NotifyTimeout> it = onGoingTimeouts.iterator(); it.hasNext(); ) {
            NotifyTimeout timeout = it.next();
            if (timeout.listener.equals(listener)) {
                timeout.cancel();
                it.remove();
            }
        }
    }

    @Override
    public void add(LocalNodeMasterListener listener) {
        localNodeMasterListeners.add(listener);
    }

    @Override
    public void remove(LocalNodeMasterListener listener) {
        localNodeMasterListeners.remove(listener);
    }

    @Override
    public void add(@Nullable final TimeValue timeout, final TimeoutClusterStateListener listener) {
        if (lifecycle.stoppedOrClosed()) {
            listener.onClose();
            return;
        }
        // call the post added notification on the same event thread
        try {
            updateTasksExecutor.execute(new SourcePrioritizedRunnable(Priority.HIGH, "_add_listener_") {
                @Override
                public void run() {
                    if (timeout != null) {
                        NotifyTimeout notifyTimeout = new NotifyTimeout(listener, timeout);
                        notifyTimeout.future = threadPool.schedule(timeout, ThreadPool.Names.GENERIC, notifyTimeout);
                        onGoingTimeouts.add(notifyTimeout);
                    }
                    postAppliedListeners.add(listener);
                    listener.postAdded();
                }
            });
        } catch (EsRejectedExecutionException e) {
            if (lifecycle.stoppedOrClosed()) {
                listener.onClose();
            } else {
                throw e;
            }
        }
    }

    @Override
    public void submitStateUpdateTask(final String source, final ClusterStateUpdateTask updateTask) {
        submitStateUpdateTask(source, Priority.NORMAL, updateTask);
    }

    @Override
    public void submitStateUpdateTask(final String source, Priority priority, final ClusterStateUpdateTask updateTask) {
        if (!lifecycle.started()) {
            return;
        }
        try {
            final UpdateTask task = new UpdateTask(source, priority, updateTask);
            if (updateTask instanceof TimeoutClusterStateUpdateTask) {
                final TimeoutClusterStateUpdateTask timeoutUpdateTask = (TimeoutClusterStateUpdateTask) updateTask;
                updateTasksExecutor.execute(task, threadPool.scheduler(), timeoutUpdateTask.timeout(), new Runnable() {
                    @Override
                    public void run() {
                        threadPool.generic().execute(new Runnable() {
                            @Override
                            public void run() {
                                timeoutUpdateTask.onFailure(task.source(), new ProcessClusterEventTimeoutException(timeoutUpdateTask.timeout(), task.source()));
                            }
                        });
                    }
                });
            } else {
                updateTasksExecutor.execute(task);
            }
        } catch (EsRejectedExecutionException e) {
            // ignore cases where we are shutting down..., there is really nothing interesting
            // to be done here...
            if (!lifecycle.stoppedOrClosed()) {
                throw e;
            }
        }
    }

    @Override
    public List<PendingClusterTask> pendingTasks() {
        PrioritizedEsThreadPoolExecutor.Pending[] pendings = updateTasksExecutor.getPending();
        List<PendingClusterTask> pendingClusterTasks = new ArrayList<>(pendings.length);
        for (PrioritizedEsThreadPoolExecutor.Pending pending : pendings) {
            final String source;
            final long timeInQueue;
            // we have to capture the task as it will be nulled after execution and we don't want to change while we check things here.
            final Object task = pending.task;
            if (task == null) {
                continue;
            } else if (task instanceof SourcePrioritizedRunnable) {
                SourcePrioritizedRunnable runnable = (SourcePrioritizedRunnable) task;
                source = runnable.source();
                timeInQueue = runnable.getAgeInMillis();
            } else {
                assert false : "expected SourcePrioritizedRunnable got " + task.getClass();
                source = "unknown [" + task.getClass() + "]";
                timeInQueue = 0;
            }

            pendingClusterTasks.add(new PendingClusterTask(pending.insertionOrder, pending.priority, new StringText(source), timeInQueue, pending.executing));
        }
        return pendingClusterTasks;
    }

    @Override
    public int numberOfPendingTasks() {
        return updateTasksExecutor.getNumberOfPendingTasks();
    }

    @Override
    public TimeValue getMaxTaskWaitTime() {
        return updateTasksExecutor.getMaxTaskWaitTime();
    }


    /** asserts that the current thread is the cluster state update thread */
    public boolean assertClusterStateThread() {
        assert Thread.currentThread().getName().contains(InternalClusterService.UPDATE_THREAD_NAME) : "not called from the cluster state update thread";
        return true;
    }

    static abstract class SourcePrioritizedRunnable extends PrioritizedRunnable {
        protected final String source;

        public SourcePrioritizedRunnable(Priority priority, String source) {
            super(priority);
            this.source = source;
        }

        public String source() {
            return source;
        }
    }

    class UpdateTask extends SourcePrioritizedRunnable {

        public final ClusterStateUpdateTask updateTask;

        UpdateTask(String source, Priority priority, ClusterStateUpdateTask updateTask) {
            super(priority, source);
            this.updateTask = updateTask;
        }

        @Override
        public void run() {
            if (!lifecycle.started()) {
                logger.debug("processing [{}]: ignoring, cluster_service not started", source);
                return;
            }
            if (logger.isDebugEnabled())
                logger.debug("processing [{}]: execute", source);
            ClusterState previousClusterState = clusterState;
            ClusterState newClusterState;
            long startTimeNS = System.nanoTime();
            try {
                newClusterState = updateTask.execute(previousClusterState);
                String newClusterStateMetaDataString = MetaData.Builder.toXContent(newClusterState.metaData(), ClusterService.persistedParams);
                String previousClusterStateMetaDataString = MetaData.Builder.toXContent(previousClusterState.metaData(), ClusterService.persistedParams);
                if (!newClusterStateMetaDataString.equals(previousClusterStateMetaDataString) && !newClusterState.blocks().disableStatePersistence() && updateTask.doPresistMetaData()) {
                    // update MeteData.version+cluster_uuid
                    newClusterState = ClusterState.builder(newClusterState)
                                        .metaData(MetaData.builder(newClusterState.metaData()).incrementVersion().build())
                                        .incrementVersion()
                                        .build();
                    // try to persist new metadata in cassandra.
                    try {
                        persistMetaData(previousClusterState.metaData(), newClusterState.metaData(), source);
                    } catch (ConcurrentMetaDataUpdateException e) {
                        // should replay the task later when current cluster state will match the expected metadata uuid and version
                        logger.debug("Cannot overwrite persistent metadata, will resubmit after next metadata update");
                        InternalClusterService.this.addFirst(new ClusterStateListener() {
                            @Override
                            public void clusterChanged(ClusterChangedEvent event) {
                                if (event.metaDataChanged()) {
                                    logger.debug("resubmit task source={} after metadata update", source);
                                    InternalClusterService.this.submitStateUpdateTask(source, Priority.URGENT, updateTask);
                                    InternalClusterService.this.remove(this); // replay only once.
                                }
                            }
                        });
                        return;
                    }
                }
                
            } catch (Throwable e) {
                TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
                if (logger.isTraceEnabled()) {
                    StringBuilder sb = new StringBuilder("failed to execute cluster state update in ").append(executionTime).append(", state:\nversion [").append(previousClusterState.version()).append("], source [").append(source).append("]\n");
                    sb.append(previousClusterState.nodes().prettyPrint());
                    sb.append(previousClusterState.routingTable().prettyPrint());
                    sb.append(previousClusterState.getRoutingNodes().prettyPrint());
                    logger.trace(sb.toString(), e);
                }
                warnAboutSlowTaskIfNeeded(executionTime, source);
                updateTask.onFailure(source, e);
                return;
            }

            if (previousClusterState == newClusterState) {
                if (updateTask instanceof AckedClusterStateUpdateTask) {
                    //no need to wait for ack if nothing changed, the update can be counted as acknowledged
                    ((AckedClusterStateUpdateTask) updateTask).onAllNodesAcked(null);
                }
                if (updateTask instanceof ProcessedClusterStateUpdateTask) {
                    ((ProcessedClusterStateUpdateTask) updateTask).clusterStateProcessed(source, previousClusterState, newClusterState);
                }
                TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
                if (logger.isDebugEnabled()) 
                    logger.debug("processing [{}]: took {} no change in cluster_state", source, executionTime);
                warnAboutSlowTaskIfNeeded(executionTime, source);
                return;
            }

            try {
                newClusterState.status(ClusterState.ClusterStateStatus.BEING_APPLIED);

                if (logger.isTraceEnabled()) {
                    StringBuilder sb = new StringBuilder("cluster state updated, source [").append(source).append("]\n");
                    sb.append(newClusterState.prettyPrint());
                    logger.trace(sb.toString());
                } else if (logger.isDebugEnabled()) {
                    logger.debug("cluster state updated, version [{}], source [{}]", newClusterState.version(), source);
                }

                ClusterChangedEvent clusterChangedEvent = new ClusterChangedEvent(source, newClusterState, previousClusterState);
                // new cluster state, notify all listeners
                final DiscoveryNodes.Delta nodesDelta = clusterChangedEvent.nodesDelta();
                if (nodesDelta.hasChanges() && logger.isInfoEnabled()) {
                    String summary = nodesDelta.shortSummary();
                    if (summary.length() > 0) {
                        logger.info("{}, reason: {}", summary, source);
                    }
                }

                // TODO, do this in parallel (and wait)
                for (DiscoveryNode node : nodesDelta.addedNodes()) {
                    if (!nodeRequiresConnection(node)) {
                        continue;
                    }
                    try {
                        transportService.connectToNode(node);
                    } catch (Throwable e) {
                        // the fault detection will detect it as failed as well
                        logger.warn("failed to connect to node [" + node + "]", e);
                    }
                }

                // update the current cluster state
                clusterState = newClusterState;
                if (logger.isTraceEnabled())
                    logger.trace("set local clusterState version={} metadata.version={}", newClusterState.version(), newClusterState.metaData().version());
                
                // publish in gossip state the applied metadata.uuid and version
                discoveryService.publish(newClusterState);
             
                // wait for acknowledgment
                if (updateTask instanceof AckedClusterStateUpdateTask) {
                    final AckedClusterStateUpdateTask ackedUpdateTask = (AckedClusterStateUpdateTask) updateTask;
                    if (ackedUpdateTask.mustApplyMetaData() && newClusterState.nodes().size() > 1) {
                        try {
                            if (logger.isInfoEnabled())
                                logger.info("Waiting MetaData.version = {} for all other alive nodes", newClusterState.metaData().version() );
                            if (!discoveryService.awaitMetaDataVersion(newClusterState.metaData().version(), ackedUpdateTask.ackTimeout())) {
                                logger.warn("Timeout waiting metadata version = {}", newClusterState.metaData().version());
                            }
                            ackedUpdateTask.onAllNodesAcked(null);
                        } catch (InterruptedException e) {
                            logger.warn("Interruped while waiting MetaData.version = {}",e, newClusterState.metaData().version() );
                            ackedUpdateTask.onAllNodesAcked(e);
                        }
                    } else {
                        ackedUpdateTask.onAllNodesAcked(null);
                    }
                }

                for (ClusterStateListener listener : preAppliedListeners) {
                    try {
                        listener.clusterChanged(clusterChangedEvent);
                    } catch (Exception ex) {
                        logger.warn("failed to notify ClusterStateListener", ex);
                    }
                }

                for (DiscoveryNode node : nodesDelta.removedNodes()) {
                    try {
                        transportService.disconnectFromNode(node);
                    } catch (Throwable e) {
                        logger.warn("failed to disconnect to node [" + node + "]", e);
                    }
                }

                newClusterState.status(ClusterState.ClusterStateStatus.APPLIED);

                for (ClusterStateListener listener : postAppliedListeners) {
                    try {
                        listener.clusterChanged(clusterChangedEvent);
                    } catch (Exception ex) {
                        logger.warn("failed to notify ClusterStateListener", ex);
                    }
                }


                if (updateTask instanceof ProcessedClusterStateUpdateTask) {
                    ((ProcessedClusterStateUpdateTask) updateTask).clusterStateProcessed(source, previousClusterState, newClusterState);
                }

                TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
                if (logger.isDebugEnabled()) 
                    logger.debug("processing [{}]: took {} done applying updated cluster_state (version: {}, uuid: {})", source, executionTime, newClusterState.version(), newClusterState.stateUUID());
                warnAboutSlowTaskIfNeeded(executionTime, source);
            } catch (Throwable t) {
                TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
                StringBuilder sb = new StringBuilder("failed to apply updated cluster state in ").append(executionTime).append(":\nversion [").append(newClusterState.version()).append("], uuid [").append(newClusterState.stateUUID()).append("], source [").append(source).append("]\n");
                sb.append(newClusterState.nodes().prettyPrint());
                sb.append(newClusterState.routingTable().prettyPrint());
                sb.append(newClusterState.getRoutingNodes().prettyPrint());
                logger.warn(sb.toString(), t);
                // TODO: do we want to call updateTask.onFailure here?
            }
        }
    }

    private void warnAboutSlowTaskIfNeeded(TimeValue executionTime, String source) {
        if (executionTime.getMillis() > slowTaskLoggingThreshold.getMillis()) {
            logger.warn("cluster state update task [{}] took {} above the warn threshold of {}", source, executionTime, slowTaskLoggingThreshold);
        }
    }

    class NotifyTimeout implements Runnable {
        final TimeoutClusterStateListener listener;
        final TimeValue timeout;
        volatile ScheduledFuture future;

        NotifyTimeout(TimeoutClusterStateListener listener, TimeValue timeout) {
            this.listener = listener;
            this.timeout = timeout;
        }

        public void cancel() {
            FutureUtils.cancel(future);
        }

        @Override
        public void run() {
            if (future != null && future.isCancelled()) {
                return;
            }
            if (lifecycle.stoppedOrClosed()) {
                listener.onClose();
            } else {
                listener.onTimeout(this.timeout);
            }
            // note, we rely on the listener to remove itself in case of timeout if needed
        }
    }

    private class ReconnectToNodes implements Runnable {

        private ConcurrentMap<DiscoveryNode, Integer> failureCount = ConcurrentCollections.newConcurrentMap();

        @Override
        public void run() {
            // master node will check against all nodes if its alive with certain discoveries implementations,
            // but we can't rely on that, so we check on it as well
            for (DiscoveryNode node : clusterState.nodes()) {
                if (lifecycle.stoppedOrClosed()) {
                    return;
                }
                if (!nodeRequiresConnection(node)) {
                    continue;
                }
                if (clusterState.nodes().nodeExists(node.id())) { // we double check existence of node since connectToNode might take time...
                    if (!transportService.nodeConnected(node)) {
                        try {
                            transportService.connectToNode(node);
                        } catch (Exception e) {
                            if (lifecycle.stoppedOrClosed()) {
                                return;
                            }
                            if (clusterState.nodes().nodeExists(node.id())) { // double check here as well, maybe its gone?
                                Integer nodeFailureCount = failureCount.get(node);
                                if (nodeFailureCount == null) {
                                    nodeFailureCount = 1;
                                } else {
                                    nodeFailureCount = nodeFailureCount + 1;
                                }
                                // log every 6th failure
                                if ((nodeFailureCount % 6) == 0) {
                                    // reset the failure count...
                                    nodeFailureCount = 0;
                                    logger.warn("failed to reconnect to node {}", e, node);
                                }
                                failureCount.put(node, nodeFailureCount);
                            }
                        }
                    }
                }
            }
            // go over and remove failed nodes that have been removed
            DiscoveryNodes nodes = clusterState.nodes();
            for (Iterator<DiscoveryNode> failedNodesIt = failureCount.keySet().iterator(); failedNodesIt.hasNext(); ) {
                DiscoveryNode failedNode = failedNodesIt.next();
                if (!nodes.nodeExists(failedNode.id())) {
                    failedNodesIt.remove();
                }
            }
            if (lifecycle.started()) {
                reconnectToNodes = threadPool.schedule(reconnectInterval, ThreadPool.Names.GENERIC, this);
            }
        }
    }

    private boolean nodeRequiresConnection(DiscoveryNode node) {
        return localNode().shouldConnectTo(node) && node.status().equals(DiscoveryNodeStatus.ALIVE);
    }

    private static class LocalNodeMasterListeners implements ClusterStateListener {

        private final List<LocalNodeMasterListener> listeners = new CopyOnWriteArrayList<>();
        private final ThreadPool threadPool;
        private volatile boolean master = false;

        private LocalNodeMasterListeners(ThreadPool threadPool) {
            this.threadPool = threadPool;
        }

        @Override
        public void clusterChanged(ClusterChangedEvent event) {
            if (!master && event.localNodeMaster()) {
                master = true;
                for (LocalNodeMasterListener listener : listeners) {
                    Executor executor = threadPool.executor(listener.executorName());
                    executor.execute(new OnMasterRunnable(listener));
                }
                return;
            }

            if (master && !event.localNodeMaster()) {
                master = false;
                for (LocalNodeMasterListener listener : listeners) {
                    Executor executor = threadPool.executor(listener.executorName());
                    executor.execute(new OffMasterRunnable(listener));
                }
            }
        }

        private void add(LocalNodeMasterListener listener) {
            listeners.add(listener);
        }

        private void remove(LocalNodeMasterListener listener) {
            listeners.remove(listener);
        }

        private void clear() {
            listeners.clear();
        }
    }

    private static class OnMasterRunnable implements Runnable {

        private final LocalNodeMasterListener listener;

        private OnMasterRunnable(LocalNodeMasterListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            listener.onMaster();
        }
    }

    private static class OffMasterRunnable implements Runnable {

        private final LocalNodeMasterListener listener;

        private OffMasterRunnable(LocalNodeMasterListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            listener.offMaster();
        }
    }

    private static class NoOpAckListener implements Discovery.AckListener {
        @Override
        public void onNodeAck(DiscoveryNode node, @Nullable Throwable t) {
        }

        @Override
        public void onTimeout() {
        }
    }

    private static class AckCountDownListener implements Discovery.AckListener {

        private static final ESLogger logger = Loggers.getLogger(AckCountDownListener.class);

        private final AckedClusterStateUpdateTask ackedUpdateTask;
        private final CountDown countDown;
        private final DiscoveryNodes nodes;
        private final long clusterStateVersion;
        private final Future<?> ackTimeoutCallback;
        private Throwable lastFailure;

        AckCountDownListener(AckedClusterStateUpdateTask ackedUpdateTask, long clusterStateVersion, DiscoveryNodes nodes, ThreadPool threadPool) {
            this.ackedUpdateTask = ackedUpdateTask;
            this.clusterStateVersion = clusterStateVersion;
            this.nodes = nodes;
            int countDown = 0;
            for (DiscoveryNode node : nodes) {
                if (ackedUpdateTask.mustAck(node)) {
                    countDown++;
                }
            }
            //we always wait for at least 1 node (the master)
            countDown = Math.max(1, countDown);
            logger.trace("expecting {} acknowledgements for cluster_state update (version: {})", countDown, clusterStateVersion);
            this.countDown = new CountDown(countDown);
            this.ackTimeoutCallback = threadPool.schedule(ackedUpdateTask.ackTimeout(), ThreadPool.Names.GENERIC, new Runnable() {
                @Override
                public void run() {
                    onTimeout();
                }
            });
        }

        @Override
        public void onNodeAck(DiscoveryNode node, @Nullable Throwable t) {
            if (!ackedUpdateTask.mustAck(node)) {
                //we always wait for the master ack anyway
                if (!node.equals(nodes.masterNode())) {
                    return;
                }
            }
            if (t == null) {
                logger.trace("ack received from node [{}], cluster_state update (version: {})", node, clusterStateVersion);
            } else {
                this.lastFailure = t;
                logger.debug("ack received from node [{}], cluster_state update (version: {})", t, node, clusterStateVersion);
            }

            if (countDown.countDown()) {
                logger.trace("all expected nodes acknowledged cluster_state update (version: {})", clusterStateVersion);
                FutureUtils.cancel(ackTimeoutCallback);
                ackedUpdateTask.onAllNodesAcked(lastFailure);
            }
        }

        @Override
        public void onTimeout() {
            if (countDown.fastForward()) {
                logger.trace("timeout waiting for acknowledgement for cluster_state update (version: {})", clusterStateVersion);
                ackedUpdateTask.onAckTimeout();
            }
        }
    }

    class ApplySettings implements NodeSettingsService.Listener {
        @Override
        public void onRefreshSettings(Settings settings) {
            final TimeValue slowTaskLoggingThreshold = settings.getAsTime(SETTING_CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD, InternalClusterService.this.slowTaskLoggingThreshold);
            InternalClusterService.this.slowTaskLoggingThreshold = slowTaskLoggingThreshold;
        }
    }

    @Override
    public abstract AbstractSearchStrategy.Result searchStrategy(IndexMetaData indexMetaData, Collection<InetAddress> startedShards);
    
    @Override
    public abstract Map<String, GetField> flattenGetField(String[] fieldFilter, String path, Object node, Map<String, GetField> flatFields);

    @Override
    public abstract Map<String, List<Object>> flattenTree(Set<String> neededFiedls, String path, Object node, Map<String, List<Object>> fields);

    @Override
    public abstract void createElasticAdminKeyspace() throws Exception;

    @Override
    public abstract void updateElasticAdminKeyspace() throws IOException;
    
    @Override
    public abstract void createIndexKeyspace(String index, int replicationFactor) throws IOException;
    
    @Override
    public abstract void createSecondaryIndices(final IndexMetaData indexMetaData) throws IOException;

    @Override
    public abstract void dropSecondaryIndices(final IndexMetaData indexMetaData) throws RequestExecutionException;
    
    
    @Override
    public abstract DocPrimaryKey parseElasticId(final String index, final String cfName, final String id) throws JsonParseException, JsonMappingException, IOException;
    
    @Override
    public abstract ClusterState updateNumberOfShards(ClusterState currentState);
    
    @Override
    public abstract void submitNumberOfShardsUpdate();
    
    @Override
    public abstract void updateTableSchema(String index, String type, Set<String> columns, DocumentMapper docMapper) throws IOException;

    @Override
    public abstract String[] mappedColumns(String index, String type, boolean isStaticDocument);

    @Override
    public abstract String[] mappedColumns(final String index, final String type, final boolean forStaticDocument, final boolean includeMeta);
    
    @Override
    public abstract String[] mappedColumns(MapperService mapperService, String type, boolean isStaticDocument);

    @Override
    public abstract String[] mappedColumns(String index, Uid uid) throws JsonParseException, JsonMappingException, IOException;

    @Override
    public abstract boolean rowExists(String ksName, String table, String id) throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    @Override
    public abstract UntypedResultSet fetchRow(final String ksName, final String index, final String type, final String id, final String[] columns) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    @Override
    public abstract UntypedResultSet fetchRow(final String ksName, final String index, final String cfName, final String id, final String[] columns, final ConsistencyLevel cl) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    @Override
    public abstract UntypedResultSet fetchRow(final String ksName, final String index, final String cfName, final  DocPrimaryKey docPk, final String[] columns, final ConsistencyLevel cl) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;
    
    @Override
    public abstract Engine.GetResult fetchSourceInternal(String ksName, String index, String type, String id) throws IOException;
    
    @Override
    public abstract String buildFetchQuery(final String ksName, final String index, final String cfName, final String[] requiredColumns, boolean forStaticDocument) throws ConfigurationException, IndexNotFoundException, IOException;
    
    @Override
    public abstract UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final String id, final String[] columns) throws ConfigurationException, IOException;
    
    @Override
    public abstract UntypedResultSet fetchRowInternal(final String ksName, final String index, final String cfName, final  DocPrimaryKey docPk, final String[] columns) throws ConfigurationException, IOException;
    
    @Override
    public abstract UntypedResultSet fetchRowInternal(final String ksName, final String index, String cfName, String[] requiredColumns, Object[] pkColumns, boolean forStaticDocument) throws ConfigurationException, IOException;

    @Override
    public abstract Map<String, Object> rowAsMap(String index, String type, Row row) throws IOException;

    @Override
    public abstract Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row) throws IOException;
    
    @Override
    public abstract Object[] rowAsArray(final String index, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException;
    
    @Override
    public abstract int rowAsMap(String index, String type, Row row, Map<String, Object> map) throws IOException;

    @Override
    public abstract void deleteRow(String index, String type, String id, ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException;

    @Override
    public abstract void insertDocument(IndicesService indicesService, IndexRequest request, ClusterState clusterState, String timestampString) throws Exception;

    @Override
    public abstract void index(String[] indices, Collection<Range<Token>> tokenRanges);

    @Override
    public abstract void blockingMappingUpdate(IndexService indexService, String type, CompressedXContent source) throws Exception;

    @Override
    public abstract Token getToken(ByteBuffer rowKey, ColumnFamily cf);
    
    @Override
    public abstract void writeMetaDataAsComment(String metadataString) throws ConfigurationException, IOException;
    
    @Override
    public abstract MetaData readMetaDataAsComment() throws NoPersistedMetaDataException;

    @Override
    public abstract MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException;

    @Override
    public abstract MetaData checkForNewMetaData(Long version) throws NoPersistedMetaDataException;
    
    @Override
    public abstract void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String source) throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException,
            RequestValidationException;

    @Override
    public abstract Map<String, Object> discoverTableMapping(String ksName, Map<String, Object> mapping) throws IOException, SyntaxException, ConfigurationException;

    @Override
    public abstract Map<String, Object> discoverTableMapping(String ksName, String cfName, Map<String, Object> mapping) throws IOException, SyntaxException, ConfigurationException;

    @Override
    public abstract void waitShardsStarted();

    @Override
    public abstract void publishAllShardsState();

    @Override
    public abstract ShardRoutingState getShardRoutingState(InetAddress address, String index, ShardRoutingState defaultState);

    @Override
    public abstract void putShardRoutingState(String index, ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException;

    
    @Override
    public abstract boolean isStaticDocument(String index, Uid uid) throws JsonParseException, JsonMappingException, IOException;



}
