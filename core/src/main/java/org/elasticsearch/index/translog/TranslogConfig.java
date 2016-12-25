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
package org.elasticsearch.index.translog;

import java.nio.file.Path;

import org.elasticsearch.common.inject.internal.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.Translog.TranslogGeneration;
import org.elasticsearch.threadpool.ThreadPool;

public class TranslogConfig {

    public static final String INDEX_TRANSLOG_DURABILITY = "index.translog.durability";
    public static final String INDEX_TRANSLOG_FS_TYPE = "index.translog.fs.type";
    public static final String INDEX_TRANSLOG_BUFFER_SIZE = "index.translog.fs.buffer_size";
    public static final String INDEX_TRANSLOG_SYNC_INTERVAL = "index.translog.sync_interval";
    
    private volatile TranslogGeneration translogGeneration;
    
    Translog.Durabilty durabilty;
    
    public TranslogConfig(ShardId shardId, Path translogPath, Settings indexSettings, Translog.Durabilty durabilty, BigArrays bigArrays, @Nullable ThreadPool threadPool) {
        
    }
    
    /**
     * Returns the current durability mode of this translog.
     */
    public Translog.Durabilty getDurabilty() {
        return durabilty;
    }

    /**
     * Sets the current durability mode for the translog.
     */
    public void setDurabilty(Translog.Durabilty durabilty) {
        this.durabilty = durabilty;
    }

    /**
     * Returns the translog generation to open. If this is <code>null</code> a new translog is created. If non-null
     * the translog tries to open the given translog generation. The generation is treated as the last generation referenced
     * form already committed data. This means all operations that have not yet been committed should be in the translog
     * file referenced by this generation. The translog creation will fail if this generation can't be opened.
     */
    public TranslogGeneration getTranslogGeneration() {
        return translogGeneration;
    }

    /**
     * Set the generation to be opened. Use <code>null</code> to start with a fresh translog.
     * @see #getTranslogGeneration()
     */
    public void setTranslogGeneration(TranslogGeneration translogGeneration) {
        this.translogGeneration = translogGeneration;
    }
}
