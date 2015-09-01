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

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.Version;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
class ShardRebuildRequest extends BroadcastShardOperationRequest {

    private boolean flush = RebuildRequest.Defaults.FLUSH;
    private Collection<Range<Token>> tokenRanges;

    ShardRebuildRequest() {
    }

    ShardRebuildRequest(ShardId shardId, RebuildRequest request) {
        super(shardId, request);
        flush = request.flush();
        tokenRanges = request.tokenRanges;
    }

    public boolean flush() {
        return flush;
    }

    public Collection<Range<Token>> tokenRanges() {
        return tokenRanges;
    }

    public ShardRebuildRequest tokenRanges(Collection<Range<Token>> tokenRanges) {
        this.tokenRanges = tokenRanges;
        return this;
    }

    @Override
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

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        if (out.getVersion().before(Version.V_1_5_0)) {
            out.writeBoolean(true);
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
}
