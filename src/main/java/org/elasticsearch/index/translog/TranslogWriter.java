package org.elasticsearch.index.translog;

import java.io.IOException;

import org.elasticsearch.index.translog.TranslogReader;

public class TranslogWriter extends TranslogReader {

    @Override
    public void close() throws IOException {
    }

    @Override
    public int compareTo(TranslogReader o) {
        return 0;
    }

}
