/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
 * Contains some code from Elasticsearch (http://www.elastic.co)
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
package org.elasticsearch.cassandra.shard;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.shard.IndexShardState;
import org.elasticsearch.indices.IndicesLifecycle;

public class CassandraShardStateObserver extends IndicesLifecycle.Listener {
	ESLogger logger = Loggers.getLogger(CassandraShardStateObserver.class);
	
	final CountDownLatch latch = new CountDownLatch(1);
    final IndicesLifecycle indicesLifecycle;
	final CopyOnWriteArraySet<String> localShardsToStart = new CopyOnWriteArraySet<>();
    
	public CassandraShardStateObserver(IndicesLifecycle indicesLifecycle, String[] conceretIndices) {
		this.indicesLifecycle = indicesLifecycle;
		List<String> indices = Arrays.asList(conceretIndices);
		logger.debug("Local shards to start {}", indices);
		localShardsToStart.addAll(indices);
		indicesLifecycle.addListener(this);
	}

    
    /**
     * Called after the index shard has been started.
     */
	@Override
    public void afterIndexShardStarted(IndexShard indexShard) {
		if (indexShard.state() == IndexShardState.STARTED) {
			localShardsToStart.remove(indexShard.shardId().index().getName());
			logger.debug("Local shard for index [{}] started, remains {}", indexShard.shardId().index().getName(), localShardsToStart);
		} else {
			logger.debug("Local shard for index [{}] state={}",indexShard.state());
		}
    	if (localShardsToStart.size() == 0)
    		latch.countDown();
    }
    
    /**
     * Block until all local shards are started.
     */
    public void waitLocalShardsStarted() {
    	try {
            if (latch.await(600, TimeUnit.SECONDS))
            	logger.debug("All local shards started.");
            else 
            	logger.error("Some local shards did not start {}", localShardsToStart);
        } catch (InterruptedException e) {
            logger.error("Interrupred before all local shards started", e);
        }
    	indicesLifecycle.removeListener(this);
    }
    
    
}
