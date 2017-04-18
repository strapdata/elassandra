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

import java.io.Closeable;
import java.io.IOException;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.elasticsearch.index.shard.AbstractIndexShardComponent;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * Dummy Translog Service
 * @author vroyer
 *
 */
public class TranslogService extends AbstractIndexShardComponent implements Closeable {
    
    public static final String INDEX_TRANSLOG_FLUSH_INTERVAL = "index.translog.interval";
    public static final String INDEX_TRANSLOG_FLUSH_THRESHOLD_OPS = "index.translog.flush_threshold_ops";
    public static final String INDEX_TRANSLOG_FLUSH_THRESHOLD_SIZE = "index.translog.flush_threshold_size";
    public static final String INDEX_TRANSLOG_FLUSH_THRESHOLD_PERIOD =  "index.translog.flush_threshold_period";
    public static final String INDEX_TRANSLOG_DISABLE_FLUSH = "index.translog.disable_flush";
   
    @Inject
    public TranslogService(ShardId shardId, IndexSettingsService indexSettingsService, ThreadPool threadPool, IndexShard indexShard) {
        super(shardId, indexSettingsService.getSettings());
    }

    @Override
    public void close() throws IOException {
    }


}
