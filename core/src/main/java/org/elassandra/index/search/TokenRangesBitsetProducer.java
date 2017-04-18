package org.elassandra.index.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.FilteredDocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BitSet;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.RamUsageEstimator;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 * A {@link BitSetProducer} that wraps a query and caches matching
 * {@link BitSet}s per segment.
 */
public class TokenRangesBitsetProducer implements BitSetProducer, Accountable {
    private static final  ESLogger logger = Loggers.getLogger(TokenRangesBitsetProducer.class);
    
    //memory usage of a simple term query
    static final long QUERY_DEFAULT_RAM_BYTES_USED = 192;

    static final long HASHTABLE_RAM_BYTES_PER_ENTRY =
     2 * RamUsageEstimator.NUM_BYTES_OBJECT_REF // key + value
     * 2; // hash tables need to be oversized to avoid collisions, assume 2x capacity

    static class Value implements Accountable {
        int tombestones;
        BitSet bitset;
        
        Value(int tombestones, BitSet bitset) {
            this.tombestones = tombestones;
            this.bitset = bitset;
        }

        @Override
        public long ramBytesUsed() {
            return HASHTABLE_RAM_BYTES_PER_ENTRY + (bitset==null ? 0 : bitset.ramBytesUsed());
        }
    
        @Override
        public Collection<Accountable> getChildResources() {
            return null;
        }
  }
  
  private final TokenRangesBitsetFilterCache bitsetFilterCache;
  private final Query query;
  private final Map<Object,Value> leafCache;

  
  /** Wraps another query's result and caches it into bitsets.
   * @param query Query to cache results of
   */
  public TokenRangesBitsetProducer(TokenRangesBitsetFilterCache bitsetFilterCache, Query query) {
    this.bitsetFilterCache = bitsetFilterCache;
    this.query = query;
    this.leafCache = Collections.synchronizedMap(new WeakHashMap<Object,Value>());
                    //new ConcurrentReferenceHashMap<Object,Value>(10, 0.9f, 1, ReferenceType.WEAK);
    this.bitsetFilterCache.listener.onCache(this.bitsetFilterCache.shardId, this);
  }

  /**
   * Gets the contained query.
   * @return the contained query.
   */
  public Query getQuery() {
    return query;
  }
  
  public void remove(Object coreCacheKey) {
      if (logger.isTraceEnabled())
          logger.trace("query={} coreCacheKey={} removed", query, coreCacheKey);
      Value value = leafCache.remove(coreCacheKey);
      if (value != null) 
          this.bitsetFilterCache.listener.onRemoval(this.bitsetFilterCache.shardId, value);
  }
  
  public void clear() {
      for(Value value : leafCache.values())
          this.bitsetFilterCache.listener.onRemoval(this.bitsetFilterCache.shardId, value);
      leafCache.clear();
  }
  
  @Override
  public BitSet getBitSet(LeafReaderContext context) throws IOException {
    final LeafReader reader = context.reader();
    final Object key = reader.getCoreCacheKey();

    Value value = leafCache.get(key);
    BitSet bitset = value == null ? null : value.bitset;
    if (value == null || value.tombestones < reader.numDeletedDocs()) {
      final IndexReaderContext topLevelContext = ReaderUtil.getTopLevelContext(context);
      final IndexSearcher searcher = new IndexSearcher(topLevelContext);
      searcher.setQueryCache(null);
      final Weight weight = searcher.createNormalizedWeight(query, false);
      final Scorer s = weight.scorer(context);
      int tombestones = 0;
      if (s != null) {
          final DocIdSetIterator it = s.iterator();
          final Bits liveDocs = reader.getLiveDocs();
          if (liveDocs == null) {
              // visible docs = query result
              bitset = BitSet.of(it, reader.maxDoc());
              if (logger.isTraceEnabled())
                  logger.trace("query={} coreCacheKey={} segment={} cardinality={}", query, key, reader, bitset.cardinality());
          } else  {
              // visible docs = query result AND liveDocs.
              tombestones = reader.numDeletedDocs();
              DocIdSetIterator fit = new FilteredDocIdSetIterator(it) {
                @Override
                protected boolean match(int doc) {
                    return liveDocs.get(doc);
                }
              };
              bitset = BitSet.of(fit, reader.maxDoc());
              if (logger.isTraceEnabled())
                  logger.trace("query={} coreCacheKey={} segment={} cardinality={}", query, key, reader, bitset.cardinality());
          }
      } else {
           bitset = null; // no visible docs.
           if (logger.isTraceEnabled())
              logger.trace("query={} coreCacheKey={} segment={} cardinality=0 ", query, key, reader);
      }
      Value newValue = new Value(tombestones, bitset);
      Value oldValue = leafCache.put(key, newValue);
      if (oldValue != null)
          this.bitsetFilterCache.listener.onRemoval(this.bitsetFilterCache.shardId, oldValue);
      this.bitsetFilterCache.listener.onCache(this.bitsetFilterCache.shardId, newValue);
    }
    return bitset;
  }
  
  @Override
  public String toString() {
    return "TokenRangesBitsetProducer("+query.toString()+":"+leafCache+")";
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TokenRangesBitsetProducer other = (TokenRangesBitsetProducer) o;
    return this.query.equals(other.query);
  }

    @Override
    public int hashCode() {
      return 31 * getClass().hashCode() + query.hashCode();
    }

    
    @Override
    public long ramBytesUsed() {
        return QUERY_DEFAULT_RAM_BYTES_USED + leafCache.values().stream().collect(Collectors.summingLong(m -> m.ramBytesUsed()));
    }
    
    @Override
    public Collection<Accountable> getChildResources() {
        return null;
    }

}

