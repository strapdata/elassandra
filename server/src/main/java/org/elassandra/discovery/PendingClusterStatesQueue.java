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

package org.elassandra.discovery;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.discovery.zen.PendingClusterStateStats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A queue that holds all "in-flight" incoming cluster states from the master. Once a master commits a cluster
 * state, it is made available via {@link #getNextClusterStateToProcess()}. The class also takes care of batching
 * cluster states for processing and failures.
 * <p>
 * The queue is bound by {@link #maxQueueSize}. When the queue is at capacity and a new cluster state is inserted
 * the oldest cluster state will be dropped. This is safe because:
 * 1) Under normal operations, master will publish &amp; commit a cluster state before processing
 *    another change (i.e., the queue length is 1)
 * 2) If the master fails to commit a change, it will step down, causing a master election, which will flush the queue.
 * 3) In general it's safe to process the incoming cluster state as a replacement to the cluster state that's dropped.
 * a) If the dropped cluster is from the same master as the incoming one is, it is likely to be superseded by the
 *    incoming state (or another state in the queue).
 * This is only not true in very extreme cases of out of order delivery.
 * b) If the dropping cluster state is not from the same master, it means that:
 * i) we are no longer following the master of the dropped cluster state but follow the incoming one
 * ii) we are no longer following any master, in which case it doesn't matter which cluster state will be processed first.
 * <p>
 * The class is fully thread safe and can be used concurrently.
 */

/** Strapdata NOTE:
 *  When PAXOS commit succeed, elassandra announce the CQL schema including the elasticsearch metadata.
 *
 *  The SchemaListener put the received state in the PendingClusterStatesQueue and try to apply it.
 *  When applied, the attached stateProcessedListener send an AppliedClusterState message to the coordinator node
 *  to acknowledge.
 *
 *  On the coordinator node, a BlockingClusterStatePublishResponseHandler wait for the local and remote notification.
 */
public class PendingClusterStatesQueue {

    public interface StateProcessedListener {

        void onNewClusterStateProcessed();

        void onNewClusterStateFailed(Exception e);
    }


    final ConcurrentSkipListMap<ClusterState, StateProcessedListener> pendingStates = new ConcurrentSkipListMap<>(Comparator.comparingLong(ClusterState::version));
    final Logger logger;
    final int maxQueueSize;

    public PendingClusterStatesQueue(Logger logger, int maxQueueSize) {
        this.logger = logger;
        this.maxQueueSize = maxQueueSize;
    }

    /** Add an incoming, not yet committed cluster state */
    public void addPending(ClusterState state, StateProcessedListener listener) {
        logger.trace("adding [{}] state version={} metadata={}", state.version(), state.metaData().x2());
        StateProcessedListener previousListener = pendingStates.put(state, listener);
        assert previousListener == null : "ClusterState "+state.version()+" already pending";

        if (pendingStates.size() > maxQueueSize) {
            Map.Entry<ClusterState, StateProcessedListener> entry = pendingStates.firstEntry();
            logger.warn("dropping pending state.version=[{}] more than [{}] pending states.", entry.getKey().version(), maxQueueSize);
            pendingStates.remove(entry.getKey()).onNewClusterStateFailed(new ElasticsearchException("too many pending states ([{}] pending)", maxQueueSize));
        }
    }

    public int size() {
        return pendingStates.size();
    }

    /**
     * mark that the processing of the given state has failed. All committed states that are
     * {@link ClusterState#supersedes(ClusterState)}-ed by this failed state, will be failed as well
     */
    public synchronized void markAsFailed(ClusterState state, Exception reason) {
        ConcurrentNavigableMap<ClusterState, StateProcessedListener> processedStates = this.pendingStates.headMap(state, true);

        if (processedStates.isEmpty()) {
            throw new IllegalStateException("can't resolve processed cluster state with uuid [" + state.stateUUID()
                + "], version [" + state.version() + "]");
        }

        logger.debug("failing state.version={} metatdata.version={} batch size={}",
                state.version(), state.metaData().x2(), processedStates.size());

        processedStates.entrySet().forEach( entry -> {
            logger.trace("failing state.version={} metatdata.version={} with state.version={}",
                    entry.getKey().version(), entry.getKey().metaData().x2(), state.version());
            entry.getValue().onNewClusterStateFailed(reason);
            this.pendingStates.remove(entry.getKey());
        });
    }

    /**
     * indicates that a cluster state was successfully processed. Any committed state that is
     * {@link ClusterState#supersedes(ClusterState)}-ed by the processed state will be marked as processed as well.
     * <p>
     * NOTE: successfully processing a state indicates we are following the master it came from. Any committed state
     * from another master will be failed by this method
     */
    public synchronized void markAsProcessed(ClusterState state) {
        if (pendingStates.isEmpty()) {
            throw new IllegalStateException("can't resolve processed cluster state with uuid [" + state.stateUUID()
                + "], version [" + state.version() + "]");
        }

        ConcurrentNavigableMap<ClusterState, StateProcessedListener> processedStates = pendingStates.headMap(state, true);
        logger.debug("processed state.version={} metatdata.version={} batch size={}", state.version(), state.metaData().x2(), processedStates.size());
        processedStates.entrySet().forEach( entry -> {
            logger.trace("processed state.version={} metatdata.version={} with state.version={}",
                    entry.getKey().version(), entry.getKey().metaData().x2(), state.version());
            entry.getValue().onNewClusterStateProcessed();
            this.pendingStates.remove(entry.getKey());
        });
    }

    /** clear the incoming queue. any committed state will be failed
     */
    public synchronized void failAllStatesAndClear(Exception reason) {
        this.pendingStates.values().forEach(listener -> listener.onNewClusterStateFailed(reason));
        pendingStates.clear();
    }

    /**
     * Gets the next committed state to process.
     * <p>
     * The method tries to batch operation by getting the cluster state the highest possible committed states
     * which succeeds the first committed state in queue (i.e., it comes from the same master).
     */
    public synchronized ClusterState getNextClusterStateToProcess() {
        if (pendingStates.isEmpty()) {
            return null;
        }

        Map.Entry<ClusterState, StateProcessedListener> lastEntry = pendingStates.lastEntry();
        logger.trace("peek most recent clusterState.version={} metadata=[{}]", lastEntry.getKey().version(), lastEntry.getKey().metaData().x2());
        return lastEntry.getKey();
    }

    /** returns all pending states, committed or not */
    public synchronized ClusterState[] pendingClusterStates() {
        return this.pendingStates.keySet().stream().toArray(ClusterState[]::new);
    }

    public synchronized PendingClusterStateStats stats() {
        return new PendingClusterStateStats(pendingStates.size(), 0, pendingStates.size());
    }
}
