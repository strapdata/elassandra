package org.elassandra.index.search;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class TokenRangesService extends AbstractComponent {

    Queue<TokenRangesQueryListener> tokenRangesQueryListeners = new ConcurrentLinkedQueue<TokenRangesQueryListener>();

    @Inject
    public TokenRangesService(Settings settings) {
        super(settings);
    }
    
    public interface TokenRangesQueryListener {
        public void onRemoveQuery(Query query);
    }
    
    public void register(TokenRangesQueryListener listener) {
        tokenRangesQueryListeners.add(listener);
    }
    
    public void unregister(TokenRangesQueryListener listener) {
        tokenRangesQueryListeners.remove(listener);
    }
    
    Cache<Collection<Range<Token>>, Query> tokenRangesQueryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(EsExecutors.boundedNumberOfProcessors(settings))
            .expireAfterAccess(Integer.getInteger(InternalCassandraClusterService.SETTING_SYSTEM_TOKEN_RANGES_QUERY_EXPIRE, 5), TimeUnit.MINUTES)
            .removalListener(new RemovalListener<Collection<Range<Token>>, Query>() {
                @Override
                public void onRemoval(RemovalNotification<Collection<Range<Token>>, Query> notification) {
                    if (logger.isTraceEnabled())
                        logger.trace("remove tokenRangeQuery={}, cause={}", notification, notification.getCause()+"]");
                    for(TokenRangesQueryListener listener : tokenRangesQueryListeners)
                        listener.onRemoveQuery(notification.getValue());
                }
            }).build();
    
    public Query getTokenRangesQuery(Collection<Range<Token>> tokenRanges) {
        Query tokenRangesQuery = null;
        if (tokenRanges != null) {
            switch(tokenRanges.size()) {
                case 0:
                    return null;
                case 1:
                    Range<Token> unique_range = tokenRanges.iterator().next();
                    if (unique_range.left.equals(AbstractSearchStrategy.TOKEN_MIN) && unique_range.right.equals(AbstractSearchStrategy.TOKEN_MAX))
                        // full search range, so don't add any filter.
                        return null;
                    
                    tokenRangesQuery = tokenRangesQueryCache.getIfPresent(tokenRanges);
                    if (tokenRangesQuery == null) {
                        tokenRangesQuery = newNumericRangesQuery(unique_range);
                        tokenRangesQueryCache.put(tokenRanges, tokenRangesQuery);
                    }
                    return tokenRangesQuery;
                default:
                    tokenRangesQuery = tokenRangesQueryCache.getIfPresent(tokenRanges);
                    if (tokenRangesQuery == null) {
                        BooleanQuery.Builder bq2 = new BooleanQuery.Builder();
                        for (Range<Token> range : tokenRanges) {
                            // TODO: check the best precisionStep (6 by default), see https://lucene.apache.org/core/5_2_1/core/org/apache/lucene/search/NumericRangeQuery.html
                            bq2.add(newNumericRangesQuery(range), Occur.SHOULD);
                        }
                        bq2.setMinimumNumberShouldMatch(1);
                        tokenRangesQuery = bq2.build();
                        tokenRangesQueryCache.put(tokenRanges, tokenRangesQuery);
                    }
                    return tokenRangesQuery;
            }
        }
        return null;
    }
    
    NumericRangeQuery<Long> newNumericRangesQuery(Range<Token> range) {
        Long left =  (Long) range.left.getTokenValue();
        Long right = (Long) range.right.getTokenValue();
        return NumericRangeQuery.newLongRange(TokenFieldMapper.NAME, InternalCassandraClusterService.defaultPrecisionStep, 
                left == Long.MIN_VALUE ? null : left+1, 
                right == Long.MAX_VALUE ? null : right, 
                true, true);
    }
    
    public boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges, Collection<Range<Token>> requestTokenRange) {
        for(Range<Token> shardRange : shardTokenRanges) {
            if (shardRange.intersects(requestTokenRange)) 
                return true;
        }
        return false;
    }
    
    public boolean tokenRangesContains(Collection<Range<Token>> shardTokenRanges, Token token) {
        for(Range<Token> shardRange : shardTokenRanges) {
            if (shardRange.contains(token)) 
                return true;
        }
        return false;
    }

    // for tests
    public void remove(Query query) {
        tokenRangesQueryCache.invalidate(query);
    }
}
