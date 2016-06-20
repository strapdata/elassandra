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
