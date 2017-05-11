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

package org.elasticsearch.action.percolate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.action.OriginalIndices;
import org.elasticsearch.action.support.broadcast.BroadcastShardRequest;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;

/**
 */
public class PercolateShardRequest extends BroadcastShardRequest {

    private String documentType;
    private BytesReference source;
    private BytesReference docSource;
    private boolean onlyCount;
    private int numberOfShards;
    private long startTime;

    private Collection<Range<Token>> tokenRanges;
    
    public PercolateShardRequest() {
    }

    PercolateShardRequest(ShardRouting shard, int numberOfShards, PercolateRequest request) {
        super(new ShardId(shard.index(), shard.id()), request);
        this.documentType = request.documentType();
        this.source = request.source();
        this.docSource = request.docSource();
        this.onlyCount = request.onlyCount();
        this.numberOfShards = numberOfShards;
        this.startTime = request.startTime;
        
        // Use the user provided token_range of the shardRouting one.
        this.tokenRanges = (request.tokenRanges() != null) ? request.tokenRanges() : shard.tokenRanges();
    }

    PercolateShardRequest(ShardId shardId, OriginalIndices originalIndices) {
        super(shardId, originalIndices);
    }

    PercolateShardRequest(ShardId shardId, PercolateRequest request) {
        super(shardId, request);
        this.documentType = request.documentType();
        this.source = request.source();
        this.docSource = request.docSource();
        this.onlyCount = request.onlyCount();
        this.startTime = request.startTime;
        this.tokenRanges = request.tokenRanges();
    }

    public Collection<Range<Token>> tokenRanges() {
        return tokenRanges;
    }

    public PercolateShardRequest tokenRanges(Collection<Range<Token>> tokenRanges) {
        this.tokenRanges = tokenRanges;
        return this;
    }
    
    public String documentType() {
        return documentType;
    }

    public BytesReference source() {
        return source;
    }

    public BytesReference docSource() {
        return docSource;
    }

    public boolean onlyCount() {
        return onlyCount;
    }

    void documentType(String documentType) {
        this.documentType = documentType;
    }

    void source(BytesReference source) {
        this.source = source;
    }

    void docSource(BytesReference docSource) {
        this.docSource = docSource;
    }

    void onlyCount(boolean onlyCount) {
        this.onlyCount = onlyCount;
    }

    public int getNumberOfShards() {
        return numberOfShards;
    }

    public long getStartTime() {
        return startTime;
    }

    void startTime(long startTime) {
        this.startTime = startTime;
    }

    OriginalIndices originalIndices() {
        return originalIndices;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        documentType = in.readString();
        source = in.readBytesReference();
        docSource = in.readBytesReference();
        onlyCount = in.readBoolean();
        numberOfShards = in.readVInt();
        startTime = in.readLong(); // no vlong, this can be negative!
        
        if (in.available() > 0 && in.readBoolean()) {
            // read tokenRanges
            Object[] tokens = (Object[]) in.readGenericValue();
            this.tokenRanges = new ArrayList<Range<Token>>(tokens.length / 2);
            for (int i = 0; i < tokens.length;) {
                Range<Token> range = new Range<Token>((Token) tokens[i++], (Token) tokens[i++]);
                this.tokenRanges.add(range);
            }
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(documentType);
        out.writeBytesReference(source);
        out.writeBytesReference(docSource);
        out.writeBoolean(onlyCount);
        out.writeVInt(numberOfShards);
        out.writeLong(startTime);
        
        // write tokenRanges
        if (tokenRanges != null) {
            out.writeBoolean(tokenRanges != null);
            Token[] tokens = new Token[tokenRanges.size() * 2];
            int i = 0;
            for (Range<Token> range : tokenRanges) {
                tokens[i++] = range.left;
                tokens[i++] = range.right;
            }
            out.writeGenericValue(tokens);
        }
    }

}
