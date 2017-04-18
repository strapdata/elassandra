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

package org.elassandra.index.search;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.BitSet;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;

/**
 * This is a per-index cache for {@link BitDocIdSet} based filters and is unbounded by size or time.
 * <p>
 * Use this cache with care, only components that require that a filter is to be materialized as a {@link BitDocIdSet}
 * and require that it should always be around should use this cache, otherwise the
 * {@link org.elasticsearch.index.cache.query.QueryCache} should be used instead.
 */
public class TokenRangesBitsetFilterCache extends AbstractIndexComponent implements LeafReader.CoreClosedListener, IndexReader.ReaderClosedListener, TokenRangesService.TokenRangesQueryListener, Closeable {

    /**
     *  A listener interface that is executed for each onCache / onRemoval event
     */
    public interface Listener {
        /**
         * Called for each cached bitset on the cache event.
         * @param shardId the shard id the bitset was cached for. This can be <code>null</code>
         * @param accountable the bitsets ram representation
         */
        void onCache(ShardId shardId, Accountable accountable);
        /**
         * Called for each cached bitset on the removal event.
         * @param shardId the shard id the bitset was cached for. This can be <code>null</code>
         * @param accountable the bitsets ram representation
         */
        void onRemoval(ShardId shardId, Accountable accountable);
    }

    private static final Listener DEFAULT_NOOP_LISTENER =  new Listener() {
        @Override
        public void onCache(ShardId shardId, Accountable accountable) {
        }

        @Override
        public void onRemoval(ShardId shardId, Accountable accountable) {
        }
    };

    private final TokenRangesService tokenRangesService;
    private final Map<Query, TokenRangesBitsetProducer> perQueryBitsetCache = Collections.synchronizedMap(new WeakHashMap<Query,TokenRangesBitsetProducer>());
    protected volatile Listener listener = DEFAULT_NOOP_LISTENER;
    protected final ShardId shardId;

    @Inject
    public TokenRangesBitsetFilterCache(Index index, Settings indexSettings, TokenRangesService tokenRangeManager) {
        super(index, indexSettings);
        this.tokenRangesService = tokenRangeManager;
        this.tokenRangesService.register(this);
        this.shardId = new ShardId(index, 0);
        logger.trace("new TokenRangesBitsetFilterCache");
    }

    public BitSet getBitSet(Query query, LeafReaderContext context) throws ExecutionException, IOException {
        TokenRangesBitsetProducer p = perQueryBitsetCache.computeIfAbsent(query, K -> new TokenRangesBitsetProducer(this, K));
        return p.getBitSet(context);
    }
    
    /**
     * Sets a listener that is invoked for all subsequent cache and removal events.
     * @throws IllegalStateException if the listener is set more than once
     */
    public void setListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        if (this.listener != DEFAULT_NOOP_LISTENER) {
            throw new IllegalStateException("can't set listener more than once");
        }
        this.listener = listener;
    }

    @Override
    public void onClose(Object ownerCoreCacheKey) {
        if (logger.isTraceEnabled())
            logger.trace("closing core={}", ownerCoreCacheKey);
        for(TokenRangesBitsetProducer p : perQueryBitsetCache.values()) {
            p.remove(ownerCoreCacheKey);
        }
    }

    
    @Override
    public void onClose(IndexReader reader) throws IOException {
        if (logger.isTraceEnabled())
            logger.trace("closing reader={}", reader);
        for(TokenRangesBitsetProducer p : perQueryBitsetCache.values()) {
            p.remove(reader);
        }
    }
    
    @Override
    public void onRemoveQuery(Query query) {
        TokenRangesBitsetProducer producer = this.perQueryBitsetCache.remove(query);
        if (producer != null) {
            producer.clear();
            this.listener.onRemoval(shardId, producer);
        }
        if (logger.isTraceEnabled())
            logger.trace("query={} removed, cache size={}", query, perQueryBitsetCache.size());
    }

    
    @Override
    public void close() {
        clear("close");
        this.tokenRangesService.unregister(this);
    }

    public void clear(String reason) {
        logger.debug("clearing all bitsets because [{}]", reason);
        for(TokenRangesBitsetProducer producer : this.perQueryBitsetCache.values())
            this.listener.onRemoval(shardId, producer);
        perQueryBitsetCache.clear();
    }

}
