package org.elassandra.index.search;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FilterLeafReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BitSet;
import org.apache.lucene.util.Bits;
import org.elasticsearch.common.lucene.index.ElasticsearchDirectoryReader;

/**
 * Per request LeafReader using cached token ranges bitset filter.
 */
public class TokenRangesLeafReader extends FilterLeafReader {

    /**
     *  A {@link BitSet} matching the expected documents on the given
     * segment. This may {@code null} if no documents match
     */
    private volatile int numDocs = -1;
    private final BitSet mask;
    private final boolean hasDeletions;
    private final Bits noBitMatch;
    
    public TokenRangesLeafReader(DirectoryReader directoryReader, LeafReader in, Query query, TokenRangesBitsetFilterCache cache) throws IOException {
        super(in);
        try {
            in.addCoreClosedListener(cache);
            ElasticsearchDirectoryReader.addReaderCloseListener(directoryReader, cache);
            //in.addReaderClosedListener(cache);
            this.mask = cache.getBitSet(query, in.getContext());
            if (mask == null) {
                numDocs = 0;
                hasDeletions = true;
                noBitMatch = new Bits.MatchNoBits(in.maxDoc());
            } else {
                numDocs = mask.cardinality();
                hasDeletions = numDocs < in.numDocs();
                noBitMatch = null;
            }
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    /** Returns the number of documents in this index. */
    @Override
    public int numDocs() {
        return numDocs;
    }
    
    /** Returns true if any documents have been deleted. Implementers should
     *  consider overriding this method if {@link #maxDoc()} or {@link #numDocs()}
     *  are not constant-time operations. */
    @Override
    public boolean hasDeletions() {
        return hasDeletions;
    }
    
    /** Returns the {@link Bits} representing live (not
     *  deleted) docs.  A set bit indicates the doc ID has not
     *  been deleted.  If this method returns null it means
     *  there are no deleted documents (all documents are
     *  live).
     *
     *  The returned instance has been safely published for
     *  use by multiple threads without additional
     *  synchronization.
     */
    @Override
    public Bits getLiveDocs() {
        if (!hasDeletions)
            return null;
        return (mask == null)  ? noBitMatch : mask;
    }
    
    @Override
    public Object getCoreCacheKey() {
        return in.getCoreCacheKey();
    }
    
    @Override
    public String toString() {
      final StringBuilder buffer = new StringBuilder("TokenRangeLeafReader(");
      buffer.append(in);
      buffer.append(')');
      return buffer.toString();
    }

}
