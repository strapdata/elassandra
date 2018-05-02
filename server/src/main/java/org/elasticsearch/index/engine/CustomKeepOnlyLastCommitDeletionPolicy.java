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

package org.elasticsearch.index.engine;

import com.carrotsearch.hppc.ObjectIntHashMap;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This {@link IndexDeletionPolicy} implementation that
 * keeps only the most recent commit and immediately removes
 * all prior commits after a new commit is done, unless there is not ongoing snapshot on it.
 */
public class CustomKeepOnlyLastCommitDeletionPolicy extends IndexDeletionPolicy {
    private final Logger logger;
    private final EngineConfig.OpenMode openMode;
    private final ObjectIntHashMap<IndexCommit> snapshottedCommits; // Number of snapshots held against each commit point.
    private volatile IndexCommit lastCommit; // the most recent commit point
    private final IndexCommit startingCommit;
    
    /** Sole constructor. */
    public CustomKeepOnlyLastCommitDeletionPolicy(EngineConfig.OpenMode openMode, Logger logger, IndexCommit startingCommit) {
        this.snapshottedCommits = new ObjectIntHashMap<>();
        this.logger = logger;
        this.startingCommit = startingCommit;
        this.openMode = openMode;
    }

    /**
     * Deletes all commits except the most recent one.
     */
    @Override
    public synchronized void onInit(List<? extends IndexCommit> commits) throws IOException {
        switch (openMode) {
            case CREATE_INDEX_AND_TRANSLOG:
                assert startingCommit == null : "CREATE_INDEX_AND_TRANSLOG must not have starting commit; commit [" + startingCommit + "]";
                break;
            case OPEN_INDEX_CREATE_TRANSLOG:
            case OPEN_INDEX_AND_TRANSLOG:
                assert commits.isEmpty() == false : "index is opened, but we have no commits";
                assert startingCommit != null && commits.contains(startingCommit) : "Starting commit not in the existing commit list; "
                    + "startingCommit [" + startingCommit + "], commit list [" + commits + "]";
                keepOnlyStartingCommitOnInit(commits);
                break;
            default:
                throw new IllegalArgumentException("unknown openMode [" + openMode + "]");
        }
    }

    /**
     * Keeping existing unsafe commits when opening an engine can be problematic because these commits are not safe
     * at the recovering time but they can suddenly become safe in the future.
     * The following issues can happen if unsafe commits are kept oninit.
     * <p>
     * 1. Replica can use unsafe commit in peer-recovery. This happens when a replica with a safe commit c1(max_seqno=1)
     * and an unsafe commit c2(max_seqno=2) recovers from a primary with c1(max_seqno=1). If a new document(seqno=2)
     * is added without flushing, the global checkpoint is advanced to 2; and the replica recovers again, it will use
     * the unsafe commit c2(max_seqno=2 at most gcp=2) as the starting commit for sequenced-based recovery even the
     * commit c2 contains a stale operation and the document(with seqno=2) will not be replicated to the replica.
     * <p>
     * 2. Min translog gen for recovery can go backwards in peer-recovery. This happens when are replica with a safe commit
     * c1(local_checkpoint=1, recovery_translog_gen=1) and an unsafe commit c2(local_checkpoint=2, recovery_translog_gen=2).
     * The replica recovers from a primary, and keeps c2 as the last commit, then sets last_translog_gen to 2. Flushing a new
     * commit on the replica will cause exception as the new last commit c3 will have recovery_translog_gen=1. The recovery
     * translog generation of a commit is calculated based on the current local checkpoint. The local checkpoint of c3 is 1
     * while the local checkpoint of c2 is 2.
     * <p>
     * 3. Commit without translog can be used in recovery. An old index, which was created before multiple-commits is introduced
     * (v6.2), may not have a safe commit. If that index has a snapshotted commit without translog and an unsafe commit,
     * the policy can consider the snapshotted commit as a safe commit for recovery even the commit does not have translog.
     */
    private void keepOnlyStartingCommitOnInit(List<? extends IndexCommit> commits) throws IOException {
        for (IndexCommit commit : commits) {
            if (startingCommit.equals(commit) == false) {
                this.deleteCommit(commit);
            }
        }
        assert startingCommit.isDeleted() == false : "Starting commit must not be deleted";
        lastCommit = startingCommit;
    }

    /**
     * Deletes all commits except the most recent one.
     */
    @Override
    public synchronized void onCommit(List<? extends IndexCommit> commits) throws IOException {
      // Note that commits.size() should normally be 2 (if not
      // called by onInit above):
      int size = commits.size();
      lastCommit = commits.get(size-1);
      for(int i=0;i<size-1;i++) {
        IndexCommit currentCommit = commits.get(i);
        if (snapshottedCommits.containsKey(currentCommit) == false) {
            deleteCommit(currentCommit);
        }
      }
      logger.debug("Keep last commit [{}]", commitDescription(lastCommit));
    }
    
    private void deleteCommit(IndexCommit commit) throws IOException {
        assert commit.isDeleted() == false : "Index commit [" + commitDescription(commit) + "] is deleted twice";
        logger.debug("Delete index commit [{}]", commitDescription(commit));
        commit.delete();
        assert commit.isDeleted() : "Deletion commit [" + commitDescription(commit) + "] was suppressed";
    }
    
    /**
     * Captures the most recent commit point {@link #lastCommit} or the most recent safe commit point {@link #safeCommit}.
     * Index files of the capturing commit point won't be released until the commit reference is closed.
     *
     * @param acquiringSafeCommit captures the most recent safe commit point if true; otherwise captures the most recent commit point.
     */
    synchronized IndexCommit acquireIndexCommit() {
        assert lastCommit != null : "Last commit is not initialized yet";
        snapshottedCommits.addTo(lastCommit, 1); // increase refCount
        return new SnapshotIndexCommit(lastCommit);
    }

    /**
     * Releases an index commit that acquired by {@link #acquireIndexCommit(boolean)}.
     */
    synchronized void releaseCommit(final IndexCommit snapshotCommit) {
        final IndexCommit releasingCommit = ((SnapshotIndexCommit) snapshotCommit).delegate;
        assert snapshottedCommits.containsKey(releasingCommit) : "Release non-snapshotted commit;" +
            "snapshotted commits [" + snapshottedCommits + "], releasing commit [" + releasingCommit + "]";
        final int refCount = snapshottedCommits.addTo(releasingCommit, -1); // release refCount
        assert refCount >= 0 : "Number of snapshots can not be negative [" + refCount + "]";
        if (refCount == 0) {
            snapshottedCommits.remove(releasingCommit);
            if (releasingCommit != this.lastCommit)
                releasingCommit.delete();
        }
    }
    
    /**
     * Returns a description for a given {@link IndexCommit}. This should be only used for logging and debugging.
     */
    public static String commitDescription(IndexCommit commit) throws IOException {
        return String.format(Locale.ROOT, "CommitPoint{segment[%s], userData[%s]}", commit.getSegmentsFileName(), commit.getUserData());
    }

    /**
     * A wrapper of an index commit that prevents it from being deleted.
     */
    private static class SnapshotIndexCommit extends IndexCommit {
        private final IndexCommit delegate;

        SnapshotIndexCommit(IndexCommit delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getSegmentsFileName() {
            return delegate.getSegmentsFileName();
        }

        @Override
        public Collection<String> getFileNames() throws IOException {
            return delegate.getFileNames();
        }

        @Override
        public Directory getDirectory() {
            return delegate.getDirectory();
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException("A snapshot commit does not support deletion");
        }

        @Override
        public boolean isDeleted() {
            return delegate.isDeleted();
        }

        @Override
        public int getSegmentCount() {
            return delegate.getSegmentCount();
        }

        @Override
        public long getGeneration() {
            return delegate.getGeneration();
        }

        @Override
        public Map<String, String> getUserData() throws IOException {
            return delegate.getUserData();
        }

        @Override
        public String toString() {
            return "SnapshotIndexCommit{" + delegate + "}";
        }
    }
}
