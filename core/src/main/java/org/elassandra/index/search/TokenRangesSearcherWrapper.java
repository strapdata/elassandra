/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
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
package org.elassandra.index.search;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.EngineConfig;
import org.elasticsearch.index.engine.EngineException;
import org.elasticsearch.index.engine.IndexSearcherWrapper;
import org.elasticsearch.index.shard.AbstractIndexShardComponent;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.IndicesLifecycle;
import org.elasticsearch.search.internal.ShardSearchRequest;

public class TokenRangesSearcherWrapper extends AbstractIndexShardComponent implements IndexSearcherWrapper {

    private static ThreadLocal<ShardSearchRequest> shardSearchRequest = new ThreadLocal<>();

    public static void current(ShardSearchRequest value) {
        shardSearchRequest.set(value);
    }

    public static void removeCurrent() {
        shardSearchRequest.remove();
    }

    public static ShardSearchRequest current() {
        return shardSearchRequest.get();
    }

    private final TokenRangesBitsetFilterCache filterCache;
    private final TokenRangesService tokenRangesService;

    @Inject
    public TokenRangesSearcherWrapper(final ShardId shardId, final Settings indexSettings, final IndicesLifecycle indicesLifecycle, 
            final IndexService indexService, TokenRangesBitsetFilterCache filterCache, TokenRangesService tokenRangeService) {
        super(shardId, indexSettings);
        this.filterCache = filterCache;
        this.tokenRangesService = tokenRangeService;
    }

    @Override
    public final DirectoryReader wrap(final DirectoryReader in) throws IOException {
        ShardSearchRequest request = current();
        if (request != null && !Boolean.FALSE.equals(request.tokenRangesBitsetCache()) && request.tokenRanges() != null) {
            Query tokenRangeQuery = tokenRangesService.getTokenRangesQuery(request.tokenRanges());
            if (tokenRangeQuery != null) {
                BooleanQuery.Builder qb = new BooleanQuery.Builder().add(tokenRangeQuery, Occur.FILTER);
                Query query = query(qb);
                if (logger.isTraceEnabled())
                    logger.trace("wrapping DirectoryReader with TokenRangesSearcherWrapper query={}", query);
                return new TokenRangesDirectoryReader(in, query, this.filterCache);
            }
        }
        return in;
    }

    public Query query(BooleanQuery.Builder qb) {
        //qb.add(Queries.newMatchAllQuery(), Occur.MUST);
        return qb.build();
    }
    
    @Override
    public IndexSearcher wrap(EngineConfig engineConfig, IndexSearcher searcher) throws EngineException {
        return searcher;
    }

}
