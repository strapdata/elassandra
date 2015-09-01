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

package org.elasticsearch.action.admin.indices.rebuild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.Version;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

/**
 * A request to re-index from cassandra data. In order to rebuild on all the
 * indices, pass an empty array or <tt>null</tt> for the indices.
 * <p/>
 *
 * @see org.elasticsearch.client.Requests#rebuildRequest(String...)
 * @see org.elasticsearch.client.IndicesAdminClient#rebuild(RebuildRequest)
 * @see RebuildResponse
 */
public class RebuildRequest extends BroadcastOperationRequest<RebuildRequest> {

    public static final class Defaults {
        public static final boolean FLUSH = true;
    }

    boolean flush = Defaults.FLUSH;

    Collection<Range<Token>> tokenRanges;

    /**
     * Constructs an optimization request over one or more indices.
     *
     * @param indices
     *            The indices to optimize, no indices passed means all indices
     *            will be optimized.
     */
    public RebuildRequest(String... indices) {
        super(indices);
    }

    public RebuildRequest() {

    }

    /**
     * Should flush be performed after the optimization. Defaults to
     * <tt>true</tt>.
     */
    public boolean flush() {
        return flush;
    }

    /**
     * Should flush be performed after the optimization. Defaults to
     * <tt>true</tt>.
     */
    public RebuildRequest flush(boolean flush) {
        this.flush = flush;
        return this;
    }

    public Collection<Range<Token>> tokenRanges() {
        return tokenRanges;
    }

    public RebuildRequest tokenRanges(Collection<Range<Token>> tokenRanges) {
        this.tokenRanges = tokenRanges;
        return this;
    }

    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        if (in.getVersion().before(Version.V_1_5_0)) {
            in.readBoolean(); // backcompat for waitForMerges, no longer exists
        }
        flush = in.readBoolean();

        Object[] tokens = (Object[]) in.readGenericValue();
        tokenRanges = new ArrayList<Range<Token>>(tokens.length / 2);
        for (int i = 0; i < tokens.length;) {
            Range<Token> range = new Range<Token>((Token) tokens[i++], (Token) tokens[i++]);
            tokenRanges.add(range);
        }
    }

    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        if (out.getVersion().before(Version.V_1_5_0)) {
            out.writeBoolean(true); // previously waitForMerges
        }
        out.writeBoolean(flush);

        Token[] tokens = new Token[tokenRanges.size() * 2];
        int i = 0;
        for (Range<Token> range : tokenRanges) {
            tokens[i++] = range.left;
            tokens[i++] = range.right;
        }
        out.writeGenericValue(tokens);
    }

    @Override
    public String toString() {
        return "RebuildRequest{" + "tokenRanges=" + tokenRanges + ", flush=" + flush + '}';
    }
}
