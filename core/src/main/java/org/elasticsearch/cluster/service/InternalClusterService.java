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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.service.ClientState;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elassandra.ConcurrentMetaDataUpdateException;
import org.elassandra.NoPersistedMetaDataException;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.cluster.routing.AbstractSearchStrategy.Router;
import org.elassandra.cluster.routing.PrimaryFirstSearchStrategy.PrimaryFirstRouter;
import org.elassandra.gateway.CassandraGatewayService;
import org.elassandra.index.search.TokenRangesService;
import org.elassandra.indices.CassandraSecondaryIndicesListener;
import org.elassandra.shard.CassandraShardStartedBarrier;
import org.elassandra.shard.CassandraShardStateListener;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionWriteResponse.ShardInfo;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.AckedClusterStateTaskListener;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateTaskConfig;
import org.elasticsearch.cluster.ClusterStateTaskExecutor;
import org.elasticsearch.cluster.ClusterStateTaskListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.LocalNodeMasterListener;
import org.elasticsearch.cluster.TimeoutClusterStateListener;
import org.elasticsearch.cluster.block.ClusterBlock;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.ProcessClusterEventTimeoutException;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.node.DiscoveryNodeService;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.common.util.concurrent.PrioritizedEsThreadPoolExecutor;
import org.elasticsearch.common.util.concurrent.PrioritizedRunnable;
import org.elasticsearch.discovery.DiscoveryService;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine.GetResult;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.settings.NodeSettingsService;
import org.elasticsearch.tasks.TaskManager;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import com.google.common.collect.Iterables;

/**
 *
 */
public class InternalClusterService extends AbstractLifecycleComponent<ClusterService> implements ClusterService {

    public static final String SETTING_CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD = "cluster.service.slow_task_logging_threshold";
    public static final String SETTING_CLUSTER_SERVICE_RECONNECT_INTERVAL = "cluster.service.reconnect_interval";

    public static final String UPDATE_THREAD_NAME = "clusterService#updateTask";
    private final ThreadPool threadPool;

    private final DiscoveryService discoveryService;

    private final OperationRouting operationRouting;

    private final TransportService transportService;

    private final NodeSettingsService nodeSettingsService;
    private final DiscoveryNodeService discoveryNodeService;
    private final IndicesService indicesService;
    private final IndicesLifecycle indicesLifecycle;
    private final CassandraSecondaryIndicesListener cassandraSecondaryIndicesService;
    
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
    private final Map<ClusterStateTaskExecutor, List<UpdateTask>> updateTasksPerExecutor = new HashMap<>();
    // TODO this is rather frequently changing I guess a Synced Set would be better here and a dedicated remove API
    private final Collection<ClusterStateListener> postAppliedListeners = new CopyOnWriteArrayList<>();
    private final Iterable<ClusterStateListener> preAppliedListeners = Iterables.concat(
            priorityClusterStateListeners,
            clusterStateListeners,
            lastClusterStateListeners);

    private final LocalNodeMasterListeners localNodeMasterListeners;
    
    private final Queue<NotifyTimeout> onGoingTimeouts = ConcurrentCollections.newQueue();

    private volatile ClusterState clusterState;
    
    private volatile CassandraShardStartedBarrier shardStartedBarrier;
    
    private final ClusterBlocks.Builder initialBlocks;

    private final TaskManager taskManager;

    private volatile ScheduledFuture reconnectToNodes;

    @Inject
    public InternalClusterService(Settings settings, DiscoveryService discoveryService, OperationRouting operationRouting, TransportService transportService,
                                  NodeSettingsService nodeSettingsService, ThreadPool threadPool, ClusterName clusterName, DiscoveryNodeService discoveryNodeService, 
                                  Version version, IndicesService indicesService, IndicesLifecycle indicesLifecycle) {
        super(settings);
        this.operationRouting = operationRouting;
        this.transportService = transportService;
        this.discoveryService = discoveryService;
        this.threadPool = threadPool;
        this.nodeSettingsService = nodeSettingsService;
        this.discoveryNodeService = discoveryNodeService;
        this.indicesService = indicesService;
        this.indicesLifecycle = indicesLifecycle;
        this.cassandraSecondaryIndicesService = new CassandraSecondaryIndicesListener(this, indicesService);
        this.version = version;

        // will be replaced on doStart.
        this.clusterState = ClusterState.builder(clusterName).build();

        this.nodeSettingsService.setClusterService(this);
        this.nodeSettingsService.addListener(new ApplySettings());

        this.reconnectInterval = this.settings.getAsTime(SETTING_CLUSTER_SERVICE_RECONNECT_INTERVAL, TimeValue.timeValueSeconds(10));

        this.slowTaskLoggingThreshold = this.settings.getAsTime(SETTING_CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD, TimeValue.timeValueSeconds(30));

        localNodeMasterListeners = new LocalNodeMasterListeners(threadPool);

        // Add NO_CASSANDRA_RING_BLOCK to avoid to save metadata while cassandra ring not ready.
        initialBlocks = ClusterBlocks.builder().addGlobalBlock(CassandraGatewayService.NO_CASSANDRA_RING_BLOCK);
        
        taskManager = transportService.getTaskManager();
    }

    public NodeSettingsService settingsService() {
        return this.nodeSettingsService;
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
        add(taskManager);
        this.clusterState = ClusterState.builder(clusterState).blocks(initialBlocks).build();
        this.updateTasksExecutor = EsExecutors.newSinglePrioritizing(UPDATE_THREAD_NAME, daemonThreadFactory(settings, UPDATE_THREAD_NAME));
        this.reconnectToNodes = threadPool.schedule(reconnectInterval, ThreadPool.Names.GENERIC, new ReconnectToNodes());
       
        DiscoveryNode localNode = this.discoveryService.localNode();
        DiscoveryNodes.Builder nodeBuilder = DiscoveryNodes.builder().put(localNode).localNodeId(localNode.id());
        this.clusterState = ClusterState.builder(clusterState).nodes(nodeBuilder).blocks(initialBlocks).build();
        
        // add listener to publish shard state in Application.X1
        this.indicesLifecycle.addListener(new CassandraShardStateListener(this, this.discoveryService));
        
        // add post-applied because 2i shoukd be created/deleted after that cassandra indices have taken the new mapping.
        addPost(cassandraSecondaryIndicesService);
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
    public void updateMapping(String ksName, MappingMetaData mapping) {
        cassandraSecondaryIndicesService.updateMapping( ksName, mapping);
    }
    
    @Override
    public void recoverShard(String index) {
        cassandraSecondaryIndicesService.recoverShard(index);
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
    public void addPost(ClusterStateListener listener) {
        this.postAppliedListeners.add(listener);
    }

    @Override
    public void add(ClusterStateListener listener) {
        clusterStateListeners.add(listener);
    }

    @Override
    public void addShardStartedBarrier() {
        this.shardStartedBarrier = new CassandraShardStartedBarrier(this);
    }
    
    @Override
    public void removeShardStartedBarrier() {
        this.shardStartedBarrier = null;
    }
    @Override
    public void blockUntilShardsStarted() {
        if (shardStartedBarrier != null) {
            shardStartedBarrier.blockUntilShardsStarted();
        }
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
        submitStateUpdateTask(source, updateTask, updateTask, updateTask, updateTask);
    }


    @Override
    public <T> void submitStateUpdateTask(final String source, final T task,
                                          final ClusterStateTaskConfig config,
                                          final ClusterStateTaskExecutor<T> executor,
                                          final ClusterStateTaskListener listener
    ) {
        innerSubmitStateUpdateTask(source, task, config, executor, safe(listener, logger));
    }

    private <T> void innerSubmitStateUpdateTask(final String source, final T task,
                                           final ClusterStateTaskConfig config,
                                           final ClusterStateTaskExecutor executor,
                                           final SafeClusterStateTaskListener listener) {
        if (!lifecycle.started()) {
            return;
        }
        try {
            final UpdateTask<T> updateTask = new UpdateTask<>(source, task, config, executor, listener);

            synchronized (updateTasksPerExecutor) {
                List<UpdateTask> updateTasksForExecutor = updateTasksPerExecutor.get(executor);
                if (updateTasksForExecutor == null) {
                    updateTasksPerExecutor.put(executor, new ArrayList<UpdateTask>());
                }
                updateTasksPerExecutor.get(executor).add(updateTask);
            }

            if (config.timeout() != null) {
                updateTasksExecutor.execute(updateTask, threadPool.scheduler(), config.timeout(), new Runnable() {
                    @Override
                    public void run() {
                        threadPool.generic().execute(new Runnable() {
                            @Override
                            public void run() {
                                if (updateTask.processed.getAndSet(true) == false) {
                                    listener.onFailure(source, new ProcessClusterEventTimeoutException(config.timeout(), source));
                                }
                            }
                        });
                    }
                });
            } else {
                updateTasksExecutor.execute(updateTask);
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

            pendingClusterTasks.add(new PendingClusterTask(pending.insertionOrder, pending.priority, new Text(source), timeInQueue, pending.executing));
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

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
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

    <T> void runTasksForExecutor(ClusterStateTaskExecutor<T> executor) {
        final ArrayList<UpdateTask<T>> toExecute = new ArrayList<>();
        final ArrayList<String> sources = new ArrayList<>();
        synchronized (updateTasksPerExecutor) {
            List<UpdateTask> pending = updateTasksPerExecutor.remove(executor);
            if (pending != null) {
                for (UpdateTask<T> task : pending) {
                    if (task.processed.getAndSet(true) == false) {
                        logger.trace("will process [{}]", task.source);
                        toExecute.add(task);
                        sources.add(task.source);
                    } else {
                        logger.trace("skipping [{}], already processed", task.source);
                    }
                }
            }
        }
        if (toExecute.isEmpty()) {
            return;
        }
        final String source = Strings.collectionToCommaDelimitedString(sources);
        if (!lifecycle.started()) {
            logger.debug("processing [{}]: ignoring, cluster_service not started", source);
            return;
        }
        logger.debug("processing [{}]: execute", source);
        ClusterState previousClusterState = clusterState;
        
        ClusterStateTaskExecutor.BatchResult<T> batchResult;
        long startTimeNS = System.nanoTime();
        List<T> inputs = new ArrayList<>();
        for (UpdateTask<T> updateTask : toExecute) {
            inputs.add(updateTask.task);
        }
        try {
            batchResult = executor.execute(previousClusterState, inputs);
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
            batchResult = ClusterStateTaskExecutor.BatchResult.<T>builder().failures(inputs, e).build(previousClusterState, false);
        }

        assert batchResult.executionResults != null;
        assert batchResult.executionResults.size() == toExecute.size()
            : String.format(Locale.ROOT, "expected [%d] task result%s but was [%d]", toExecute.size(), toExecute.size() == 1 ? "" : "s", batchResult.executionResults.size());
        boolean assertsEnabled = false;
        assert (assertsEnabled = true);
        if (assertsEnabled) {
            for (UpdateTask<T> updateTask : toExecute) {
                assert batchResult.executionResults.containsKey(updateTask.task) : "missing task result for [" + updateTask.task + "]";
            }
        }

        ClusterState newClusterState = batchResult.resultingState;
        final ArrayList<UpdateTask<T>> proccessedListeners = new ArrayList<>();
        // fail all tasks that have failed and extract those that are waiting for results
        for (final UpdateTask<T> updateTask : toExecute) {
            assert batchResult.executionResults.containsKey(updateTask.task) : "missing " + updateTask.task.toString();
            final ClusterStateTaskExecutor.TaskResult executionResult =
                    batchResult.executionResults.get(updateTask.task);
            executionResult.handle(
                new Runnable() {
                    @Override
                    public void run() {
                        proccessedListeners.add(updateTask);
                    }
                },
                new ClusterStateTaskExecutor.TaskResult.FailureConsumer() {
                    @Override
                    public void accept(Throwable ex) {
                        logger.debug("cluster state update task [{}] failed", ex, updateTask.source);
                        updateTask.listener.onFailure(updateTask.source, ex);
                    }
                }
            );
        }


        if (previousClusterState == newClusterState) {
            for (UpdateTask<T> task : proccessedListeners) {
                if (task.listener instanceof AckedClusterStateTaskListener) {
                    //no need to wait for ack if nothing changed, the update can be counted as acknowledged
                    ((AckedClusterStateTaskListener) task.listener).onAllNodesAcked(null);
                }
                task.listener.clusterStateProcessed(task.source, previousClusterState, newClusterState);
            }
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            logger.debug("processing [{}]: took {} no change in cluster_state", source, executionTime);
            warnAboutSlowTaskIfNeeded(executionTime, source);
            return;
        }

        // try to update cluster state.
        boolean newPertistedMetadata = false;
        try {
            String newClusterStateMetaDataString = MetaData.Builder.toXContent(newClusterState.metaData(), ClusterService.persistedParams);
            String previousClusterStateMetaDataString = MetaData.Builder.toXContent(previousClusterState.metaData(), ClusterService.persistedParams);
            if (!newClusterStateMetaDataString.equals(previousClusterStateMetaDataString) && !newClusterState.blocks().disableStatePersistence() && batchResult.doPresistMetaData) {
                // update MeteData.version+cluster_uuid
                newPertistedMetadata = true;
                newClusterState = ClusterState.builder(newClusterState)
                                    .metaData(MetaData.builder(newClusterState.metaData()).incrementVersion().build())
                                    .incrementVersion()
                                    .build();
                // try to persist new metadata in cassandra.
                try {
                    persistMetaData(previousClusterState.metaData(), newClusterState.metaData(), source);
                } catch (ConcurrentMetaDataUpdateException e) {
                    // should replay the task later when current cluster state will match the expected metadata uuid and version
                    logger.debug("Cannot overwrite persistent metadata, will resubmit task when metadata.version > {}", previousClusterState.metaData().version());
                    InternalClusterService.this.addFirst(new ClusterStateListener() {
                        @Override
                        public void clusterChanged(ClusterChangedEvent event) {
                            if (event.metaDataChanged()) {
                                logger.debug("metadata.version={} => resubmit delayed update source={} tasks={}",InternalClusterService.this.state().metaData().version(), source, inputs);
                                // TODO: resubmit tasks after the next cluster state update.
                                for (final UpdateTask<T> updateTask : toExecute) {
                                    InternalClusterService.this.submitStateUpdateTask(source, (ClusterStateUpdateTask) updateTask.task);
                                }
                                InternalClusterService.this.remove(this); // replay only once.
                            }
                        }
                    });
                    return;
                }
            }
        } catch (org.apache.cassandra.exceptions.UnavailableException e) {
            // Cassandra issue => ack with failure.
            for (UpdateTask<T> task : proccessedListeners) {
                if (task.listener instanceof AckedClusterStateTaskListener) {
                    //no need to wait for ack if nothing changed, the update can be counted as acknowledged
                    try {
                        ((AckedClusterStateTaskListener) task.listener).onFailure(source, e);
                    } catch (Throwable t) {
                        logger.debug("error while processing ack for master node [{}]", t, newClusterState.nodes().localNode());
                    }
                }
            }
            return;
        } catch (Throwable e) {
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder("failed to execute cluster state update in ").append(executionTime)
                        .append(", state:\nversion [")
                        .append(previousClusterState.version()).
                        append("], source [").append(source).append("]\n");
                logger.warn(sb.toString(), e);
                // TODO resubmit task on next cluster state change
            }
            return;
        }
        
        
        try {
            if (newPertistedMetadata) {
                // publish in gossip state the applied metadata.uuid and version
                discoveryService.publishX2(newClusterState);
            }

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
                if (!nodeRequiresConnection(node) || !DiscoveryNodeStatus.ALIVE.equals(node.status())) {
                    continue;
                }
                try {
                    transportService.connectToNode(node);
                } catch (Throwable e) {
                    // the fault detection will detect it as failed as well
                    logger.warn("failed to connect to node [" + node + "]", e);
                }
            }

            // if we are the master, publish the new state to all nodes
            // we publish here before we send a notification to all the listeners, since if it fails
            // we don't want to notify
            /*
            if (newClusterState.nodes().localNodeMaster()) {
                logger.debug("publishing cluster state version [{}]", newClusterState.version());
                discoveryService.publish(clusterChangedEvent, ackListener);
            }
            */

            // update the current cluster state 
            clusterState = newClusterState;
            if (logger.isTraceEnabled())
                logger.trace("set local clusterState version={} metadata.version={}", newClusterState.version(), newClusterState.metaData().version());
            
            if (logger.isTraceEnabled())
                logger.trace("preAppliedListeners={}",preAppliedListeners);
            for (ClusterStateListener listener : preAppliedListeners) {
                try {
                    listener.clusterChanged(clusterChangedEvent);
                } catch (Exception ex) {
                    logger.warn("failed to notify ClusterStateListener", ex);
                }
            }

            if (!newPertistedMetadata) {
                // For non-coordinator nodes, publish in gossip state the applied metadata.uuid and version.
                discoveryService.publishX2(newClusterState);
            }
            
            Throwable ackFailure = null;
            if (newPertistedMetadata) {
                // for the coordinator node, wait for acknowledgment from all nodes
                if (batchResult.doPresistMetaData && newClusterState.nodes().size() > 1) {
                    try {
                        if (logger.isInfoEnabled())
                            logger.info("Waiting MetaData.version = {} for all other alive nodes", newClusterState.metaData().version() );
                        if (!discoveryService.awaitMetaDataVersion(newClusterState.metaData().version(), new TimeValue(30, TimeUnit.SECONDS))) {
                            logger.warn("Timeout waiting metadata version = {}", newClusterState.metaData().version());
                        }
                    } catch (Throwable e) {
                        ackFailure = e;
                        logger.error("Interruped while waiting MetaData.version = {}",e, newClusterState.metaData().version() );
                    }
                }
            }
            
            newClusterState.status(ClusterState.ClusterStateStatus.APPLIED);
            
            // update cluster state routing table
            // TODO: update the routing table only for updated indices.
            RoutingTable newRoutingTable = RoutingTable.build(this, newClusterState);
            clusterState = ClusterState.builder(newClusterState).routingTable(newRoutingTable).build().status(ClusterState.ClusterStateStatus.APPLIED);

            // notify 2i with the new cluster state, including new local started shard.
            if (logger.isTraceEnabled())
                logger.trace("postAppliedListeners={}",postAppliedListeners);
            for (ClusterStateListener listener : postAppliedListeners) {
                try {
                    listener.clusterChanged(clusterChangedEvent);
                } catch (Exception ex) {
                    logger.warn("failed to notify ClusterStateListener", ex);
                }
            }
            
            
            //manual ack only from the master at the end of the publish
            for (UpdateTask<T> task : proccessedListeners) {
                if (task.listener instanceof AckedClusterStateTaskListener) {
                    //no need to wait for ack if nothing changed, the update can be counted as acknowledged
                    try {
                        ((AckedClusterStateTaskListener) task.listener).onAllNodesAcked(ackFailure);
                    } catch (Throwable t) {
                        logger.debug("error while processing ack for master node [{}]", t, newClusterState.nodes().localNode());
                    }
                }
            }

            for (UpdateTask<T> task : proccessedListeners) {
                task.listener.clusterStateProcessed(task.source, previousClusterState, newClusterState);
            }

            executor.clusterStatePublished(newClusterState);

            if (this.shardStartedBarrier != null) {
                shardStartedBarrier.isReadyToIndex(newClusterState);
            }
            
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
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

    private static SafeClusterStateTaskListener safe(ClusterStateTaskListener listener, ESLogger logger) {
        if (listener instanceof AckedClusterStateTaskListener) {
            return new SafeAckedClusterStateTaskListener((AckedClusterStateTaskListener) listener, logger);
        } else {
            return new SafeClusterStateTaskListener(listener, logger);
        }
    }

    private static class SafeClusterStateTaskListener implements ClusterStateTaskListener {
        private final ClusterStateTaskListener listener;
        private final ESLogger logger;

        public SafeClusterStateTaskListener(ClusterStateTaskListener listener, ESLogger logger) {
            this.listener = listener;
            this.logger = logger;
        }

        @Override
        public void onFailure(String source, Throwable t) {
            try {
                listener.onFailure(source, t);
            } catch (Exception e) {
                logger.error("exception thrown by listener notifying of failure [{}] from [{}]", e, t, source);
            }
        }

        @Override
        public void onNoLongerMaster(String source) {
            try {
                listener.onNoLongerMaster(source);
            } catch (Exception e) {
                logger.error("exception thrown by listener while notifying no longer master from [{}]", e, source);
            }
        }

        @Override
        public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
            try {
                listener.clusterStateProcessed(source, oldState, newState);
            } catch (Exception e) {
                logger.error(
                    "exception thrown by listener while notifying of cluster state processed from [{}], old cluster state:\n{}\nnew cluster state:\n{}",
                    e,
                    source,
                    oldState.prettyPrint(),
                    newState.prettyPrint());
            }
        }
    }

    private static class SafeAckedClusterStateTaskListener extends SafeClusterStateTaskListener implements AckedClusterStateTaskListener {
        private final AckedClusterStateTaskListener listener;
        private final ESLogger logger;

        public SafeAckedClusterStateTaskListener(AckedClusterStateTaskListener listener, ESLogger logger) {
            super(listener, logger);
            this.listener = listener;
            this.logger = logger;
        }

        @Override
        public boolean mustAck(DiscoveryNode discoveryNode) {
            return listener.mustAck(discoveryNode);
        }

        @Override
        public void onAllNodesAcked(@Nullable Throwable t) {
            try {
                listener.onAllNodesAcked(t);
            } catch (Exception e) {
                logger.error("exception thrown by listener while notifying on all nodes acked [{}]", e, t);
            }
        }

        @Override
        public void onAckTimeout() {
            try {
                listener.onAckTimeout();
            } catch (Exception e) {
                logger.error("exception thrown by listener while notifying on ack timeout", e);
            }
        }

        @Override
        public TimeValue ackTimeout() {
            return listener.ackTimeout();
        }
    }

    class UpdateTask<T> extends SourcePrioritizedRunnable {

        public final T task;
        public final ClusterStateTaskConfig config;
        public final ClusterStateTaskExecutor<T> executor;
        public final ClusterStateTaskListener listener;
        public final AtomicBoolean processed = new AtomicBoolean();

        UpdateTask(String source, T task, ClusterStateTaskConfig config, ClusterStateTaskExecutor<T> executor, ClusterStateTaskListener listener) {
            super(config.priority(), source);
            this.task = task;
            this.config = config;
            this.executor = executor;
            this.listener = listener;
        }

        @Override
        public void run() {
            runTasksForExecutor(executor);
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
                if (clusterState.nodes().nodeExists(node.id()) && node.status()==DiscoveryNode.DiscoveryNodeStatus.ALIVE) { // we double check existence of node since connectToNode might take time...
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
        return localNode().shouldConnectTo(node);
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

    /*
    private static class DelegetingAckListener implements Discovery.AckListener {

        final private List<Discovery.AckListener> listeners;

        private DelegetingAckListener(List<Discovery.AckListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onNodeAck(DiscoveryNode node, @Nullable Throwable t) {
            for (Discovery.AckListener listener : listeners) {
                listener.onNodeAck(node, t);
            }
        }

        @Override
        public void onTimeout() {
            throw new UnsupportedOperationException("no timeout delegation");
        }
    }
    */
    
    /*
    private static class AckCountDownListener implements Discovery.AckListener {

        private static final ESLogger logger = Loggers.getLogger(AckCountDownListener.class);

        private final AckedClusterStateTaskListener ackedTaskListener;
        private final CountDown countDown;
        private final DiscoveryNodes nodes;
        private final long clusterStateVersion;
        private final Future<?> ackTimeoutCallback;
        private Throwable lastFailure;

        AckCountDownListener(AckedClusterStateTaskListener ackedTaskListener, long clusterStateVersion, DiscoveryNodes nodes, ThreadPool threadPool) {
            this.ackedTaskListener = ackedTaskListener;
            this.clusterStateVersion = clusterStateVersion;
            this.nodes = nodes;
            int countDown = 0;
            for (DiscoveryNode node : nodes) {
                if (ackedTaskListener.mustAck(node)) {
                    countDown++;
                }
            }
            //we always wait for at least 1 node (the master)
            countDown = Math.max(1, countDown);
            logger.trace("expecting {} acknowledgements for cluster_state update (version: {})", countDown, clusterStateVersion);
            this.countDown = new CountDown(countDown);
            this.ackTimeoutCallback = threadPool.schedule(ackedTaskListener.ackTimeout(), ThreadPool.Names.GENERIC, new Runnable() {
                @Override
                public void run() {
                    onTimeout();
                }
            });
        }

        @Override
        public void onNodeAck(DiscoveryNode node, @Nullable Throwable t) {
            if (!ackedTaskListener.mustAck(node)) {
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
                ackedTaskListener.onAllNodesAcked(lastFailure);
            }
        }

        @Override
        public void onTimeout() {
            if (countDown.fastForward()) {
                logger.trace("timeout waiting for acknowledgement for cluster_state update (version: {})", clusterStateVersion);
                ackedTaskListener.onAckTimeout();
            }
        }
    }
    */
    
    class ApplySettings implements NodeSettingsService.Listener {
        @Override
        public void onRefreshSettings(Settings settings) {
            final TimeValue slowTaskLoggingThreshold = settings.getAsTime(SETTING_CLUSTER_SERVICE_SLOW_TASK_LOGGING_THRESHOLD, InternalClusterService.this.slowTaskLoggingThreshold);
            InternalClusterService.this.slowTaskLoggingThreshold = slowTaskLoggingThreshold;
        }
    }

    @Override
    public void publishGossipStates() {
        
    }
    
    @Override
    public IndexService indexService(String index) {
        return this.indicesService.indexService(index);
    }
    
    @Override
    public IndexService indexServiceSafe(String index) {
        return this.indicesService.indexServiceSafe(index);
    }
    
    @Override
    public TokenRangesService getTokenRangesService() {
        return  null;
    }
    
    @Override
    public AbstractSearchStrategy searchStrategy(IndexMetaData indexMetaData, ClusterState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Router getRouter(IndexMetaData indexMetaData, ClusterState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrimaryFirstRouter updateRouter(IndexMetaData indexMetaData, ClusterState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, GetField> flattenGetField(String[] fieldFilter, String path, Object node,
            Map<String, GetField> flatFields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, List<Object>> flattenTree(Set<String> neededFiedls, String path, Object node,
            Map<String, List<Object>> fields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createOrUpdateElasticAdminKeyspace() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createIndexKeyspace(String index, int replicationFactor) throws IOException {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void dropIndexKeyspace(String ksName) throws IOException {
    }
    
    @Override
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException {
    }

    @Override
    public void createSecondaryIndices(IndexMetaData indexMetaData) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dropSecondaryIndices(IndexMetaData indexMetaData) throws RequestExecutionException {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void dropSecondaryIndex(CFMetaData cfMetaData) throws RequestExecutionException {
        
    }

    @Override
    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException {
        
    }
    
    @Override
    public void dropTable(String ksName, String cfName) throws RequestExecutionException {
        
    }
    
    @Override
    public DocPrimaryKey parseElasticId(String index, String cfName, String id)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClusterState updateNumberOfShards(ClusterState currentState) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void submitNumberOfShardsUpdate() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateRoutingTable() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateTableSchema(final IndexService indexService, final MappingMetaData mappingMd)
            throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isStaticDocument(String index, Uid uid)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean rowExists(final MapperService mapperService, final String type, String id)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public UntypedResultSet fetchRow(String ksName, String index, String type, String id, String[] columns, Map<String,ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRow(String ksName, String index, String cfName, String id, String[] columns,
            ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRow(String ksName, String index, String cfName, DocPrimaryKey docPk, String[] columns,
            ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String buildFetchQuery(String ksName, String index, String cfName, String[] requiredColumns,
            boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws IndexNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRowInternal(String ksName, String index, String cfName, String id, String[] columns,  Map<String,ColumnDefinition> columnDefs) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRowInternal(String ksName, String index, String cfName, DocPrimaryKey docPk, String[] columns,  Map<String,ColumnDefinition> columnDefs) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet fetchRowInternal(String ksName, String index, String cfName, String[] columns,
            Object[] pkColumns, boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GetResult fetchSourceInternal(String ksName, String index, String type, String id) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> rowAsMap(String index, String type, Row row) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int rowAsMap(String index, String type, Row row, Map<String, Object> map) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object[] rowAsArray(String index, String type, Row row) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] rowAsArray(String index, String type, Row row, boolean valueForSearch) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteRow(String index, String type, String id, ConsistencyLevel cl)
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception {
    }
    
    @Override
    public void insertDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void blockingMappingUpdate(IndexService indexService, String type, String source)
            throws Exception {
        // TODO Auto-generated method stub
        
    }
/*
    @Override
    public Token getToken(ByteBuffer rowKey, ColumnFamily cf) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Token getToken(String index, String type, String routing)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }
*/

    
    @Override
    public void writeMetaDataAsComment(String metaDataString) throws ConfigurationException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MetaData checkForNewMetaData(Long version) throws NoPersistedMetaDataException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String source)
            throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException,
            RequestValidationException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<UUID, ShardRoutingState> getShardRoutingStates(String index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putShardRoutingState(String index, ShardRoutingState shardRoutingState)
            throws JsonGenerationException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isDatacenterGroupMember(InetAddress endpoint) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public ShardInfo shardInfo(String index, ConsistencyLevel cl) {
        return null;
    }

    @Override
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UntypedResultSet process(ConsistencyLevel cl, ClientState clientState, String query, Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, String type, String id)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BytesReference source(DocumentMapper docMapper, Map sourceAsMap, String index, Uid uid)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getElasticAdminKeyspaceName() {
        return null;
    }

    @Override
    public int getLocalDataCenterSize() {
        return 0;
    }
}
