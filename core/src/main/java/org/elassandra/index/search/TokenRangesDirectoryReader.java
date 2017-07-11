package org.elassandra.index.search;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FilterDirectoryReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.Query;

public class TokenRangesDirectoryReader extends FilterDirectoryReader {
    final Query query;
    final TokenRangesBitsetFilterCache cache;
    
    public TokenRangesDirectoryReader(DirectoryReader in, Query query, TokenRangesBitsetFilterCache cache) throws IOException {
        super(in, new FilterDirectoryReader.SubReaderWrapper() {
            @Override
            public LeafReader wrap(LeafReader reader) {
                try {
                    return new TokenRangesLeafReader(in, reader, query, cache);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        this.query = query;
        this.cache = cache;
        assert !(in instanceof TokenRangesDirectoryReader);
    }
    
    @Override
    protected DirectoryReader doWrapDirectoryReader(DirectoryReader in) throws IOException {
        return new TokenRangesDirectoryReader(in, query, cache);
    }
    
    public Object getCoreCacheKey() {
        return in.getCoreCacheKey();
    }
}
