package org.elassandra.index.search;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.index.mapper.NumberFieldMapper;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TokenRangesService {
    protected final Logger logger = LogManager.getLogger(TokenRangesService.class);

    Queue<TokenRangesQueryListener> tokenRangesQueryListeners = new ConcurrentLinkedQueue<TokenRangesQueryListener>();
    final Settings settings;
    final Cache<Collection<Range<Token>>, Query> tokenRangesQueryCache;

    @Inject
    public TokenRangesService(Settings settings) {
        this.settings = settings;
        this.tokenRangesQueryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(EsExecutors.numberOfProcessors(TokenRangesService.this.settings))
            .expireAfterAccess(Integer.getInteger(ClusterService.SETTING_SYSTEM_TOKEN_RANGES_QUERY_EXPIRE, 5), TimeUnit.MINUTES)
            .removalListener(new RemovalListener<Collection<Range<Token>>, Query>() {
                @Override
                public void onRemoval(RemovalNotification<Collection<Range<Token>>, Query> notification) {
                    if (logger.isTraceEnabled())
                        logger.trace("remove tokenRangeQuery={}, cause={}", notification, notification.getCause());
                    for(TokenRangesQueryListener listener : tokenRangesQueryListeners)
                        listener.onRemoveQuery(notification.getValue());
                }
            }).build();
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

                    if (unique_range.left.equals(unique_range.right)) {
                        // partition key search, not cached.
                        return NumberFieldMapper.NumberType.LONG.termQuery(TokenFieldMapper.NAME,unique_range.left);
                    }
                    tokenRangesQuery = tokenRangesQueryCache.getIfPresent(tokenRanges);
                    if (tokenRangesQuery == null) {
                        tokenRangesQuery = newNumericRangesQuery(unique_range);
                        tokenRangesQueryCache.put(tokenRanges, tokenRangesQuery);
                    }
                    if (logger.isTraceEnabled())
                        logger.trace("tokenRangeQuery={}", tokenRangesQuery);
                    return tokenRangesQuery;
                default:
                    tokenRangesQuery = tokenRangesQueryCache.getIfPresent(tokenRanges);
                    if (tokenRangesQuery == null) {
                        BooleanQuery.Builder bq2 = new BooleanQuery.Builder();
                        boolean hasSingleton = false;
                        for (Range<Token> range : tokenRanges) {
                            bq2.add(newNumericRangesQuery(range), Occur.SHOULD);
                            if (range.left.equals(range.right))
                                hasSingleton = true;
                        }
                        bq2.setMinimumNumberShouldMatch(1);
                        tokenRangesQuery = bq2.build();
                        if (!hasSingleton)
                            tokenRangesQueryCache.put(tokenRanges, tokenRangesQuery);
                    }
                    if (logger.isTraceEnabled())
                        logger.trace("tokenRangeQuery={}", tokenRangesQuery);
                    return tokenRangesQuery;
            }
        }
        return null;
    }

    Query newNumericRangesQuery(Range<Token> range) {
        Long left =  (Long) range.left.getTokenValue();
        Long right = (Long) range.right.getTokenValue();
        return (left.equals(right)) ?
            NumberFieldMapper.NumberType.LONG.termQuery(TokenFieldMapper.NAME,left) :
            NumberFieldMapper.NumberType.LONG.rangeQuery(TokenFieldMapper.NAME,
                left == Long.MIN_VALUE ? null : left,
                right == Long.MAX_VALUE ? null : right,
                false, true, true);
    }

    public static boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges, Range<Token> requestTokenRange) {
        if (requestTokenRange.left.equals(requestTokenRange.right))
            return tokenRangesContains(shardTokenRanges, requestTokenRange.left);

        for(Range<Token> shardRange : shardTokenRanges) {
            if (shardRange.intersects(requestTokenRange))
                return true;
        }
        return false;
    }

    // requestTokenRanges may contains singleton
    public static boolean tokenRangesIntersec(Collection<Range<Token>> shardTokenRanges, Collection<Range<Token>> requestTokenRanges) {
        for(Range<Token> requestTokenRange : requestTokenRanges) {
            if (tokenRangesIntersec(shardTokenRanges, requestTokenRange))
                return true;
        }
        return false;
    }

    public static boolean tokenRangesContains(Collection<Range<Token>> shardTokenRanges, Token token) {
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
