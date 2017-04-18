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

package org.elasticsearch.search.internal;

import static org.elasticsearch.search.Scroll.readScroll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.ContextAndHeaderHolder;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.script.Template;
import org.elasticsearch.search.Scroll;

/**
 * Shard level search request that gets created and consumed on the local node.
 * Used by warmers and by api that need to create a search context within their execution.
 *
 * Source structure:
 * <pre>
 * {
 *  from : 0, size : 20, (optional, can be set on the request)
 *  sort : { "name.first" : {}, "name.last" : { reverse : true } }
 *  fields : [ "name.first", "name.last" ]
 *  query : { ... }
 *  aggs : {
 *      "agg1" : {
 *          terms : { ... }
 *      }
 *  }
 * }
 * </pre>
 */

public class ShardSearchLocalRequest extends ContextAndHeaderHolder implements ShardSearchRequest {

    private String index;
    private int shardId;
    private int numberOfShards;
    private SearchType searchType;
    private Scroll scroll;
    private String[] types = Strings.EMPTY_ARRAY;
    private String[] filteringAliases;
    private BytesReference source;
    private BytesReference extraSource;
    private BytesReference templateSource;
    private Template template;
    private Boolean requestCache;
    private long nowInMillis;

    private boolean profile;
    
    private Boolean tokenRangesBitsetCache;
    private Collection<Range<Token>> tokenRanges = null;

    ShardSearchLocalRequest() {
    }

    ShardSearchLocalRequest(SearchRequest searchRequest, ShardRouting shardRouting, int numberOfShards,
                            String[] filteringAliases, long nowInMillis) {
        this(shardRouting.shardId(), shardRouting, numberOfShards, searchRequest.searchType(),
                searchRequest.source(), searchRequest.types(), searchRequest.requestCache(), searchRequest.tokenRangesBitsetCache());
        this.extraSource = searchRequest.extraSource();
        this.templateSource = searchRequest.templateSource();
        this.template = searchRequest.template();
        this.scroll = searchRequest.scroll();
        this.filteringAliases = filteringAliases;
        this.nowInMillis = nowInMillis;
        copyContextAndHeadersFrom(searchRequest);
        
        // Use the user provided token_range of the shardRouting one.
        this.tokenRanges = (searchRequest.tokenRanges() != null) ? searchRequest.tokenRanges() : shardRouting.tokenRanges();
        this.tokenRangesBitsetCache = searchRequest.tokenRangesBitsetCache();
    }

    public ShardSearchLocalRequest(String[] types, long nowInMillis) {
        this.types = types;
        this.nowInMillis = nowInMillis;
    }

    public ShardSearchLocalRequest(String[] types, long nowInMillis, String[] filteringAliases) {
        this(types, nowInMillis);
        this.filteringAliases = filteringAliases;
    }

    public ShardSearchLocalRequest(ShardId shardId, ShardRouting shardRouting, int numberOfShards, SearchType searchType,
                                   BytesReference source, String[] types, Boolean requestCache, Boolean tokenRangesBitsetCache) {
        this.index = shardId.getIndex();
        this.shardId = shardId.id();
        this.numberOfShards = numberOfShards;
        this.searchType = searchType;
        this.source = source;
        this.types = types;
        this.requestCache = requestCache;
        
        // Use the user provided token_range of the shardRouting one.
        this.tokenRanges = shardRouting.tokenRanges();
        this.tokenRangesBitsetCache = tokenRangesBitsetCache;
    }

    @Override
    public String index() {
        return index;
    }

    @Override
    public int shardId() {
        return shardId;
    }

    @Override
    public String[] types() {
        return types;
    }

    @Override
    public BytesReference source() {
        return source;
    }

    @Override
    public void source(BytesReference source) {
        this.source = source;
    }

    @Override
    public BytesReference extraSource() {
        return extraSource;
    }

    @Override
    public int numberOfShards() {
        return numberOfShards;
    }

    @Override
    public SearchType searchType() {
        return searchType;
    }

    @Override
    public String[] filteringAliases() {
        return filteringAliases;
    }

    @Override
    public long nowInMillis() {
        return nowInMillis;
    }

    @Override
    public Template template() {
        return template;
    }

    @Override
    public BytesReference templateSource() {
        return templateSource;
    }

    @Override
    public Boolean requestCache() {
        return requestCache;
    }

    @Override
    public Scroll scroll() {
        return scroll;
    }

    @Override
    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    @Override
    public boolean isProfile() {
        return profile;
    }

    @SuppressWarnings("unchecked")
    protected void innerReadFrom(StreamInput in) throws IOException {
        index = in.readString();
        shardId = in.readVInt();
        searchType = SearchType.fromId(in.readByte());
        numberOfShards = in.readVInt();
        if (in.readBoolean()) {
            scroll = readScroll(in);
        }

        source = in.readBytesReference();
        extraSource = in.readBytesReference();

        types = in.readStringArray();
        filteringAliases = in.readStringArray();
        nowInMillis = in.readVLong();

        templateSource = in.readBytesReference();
        if (in.readBoolean()) {
            template = Template.readTemplate(in);
        }
        requestCache = in.readOptionalBoolean();
        tokenRangesBitsetCache = in.readOptionalBoolean();
        
        // read tokenRanges
        Object[] tokens = (Object[]) in.readGenericValue();
        this.tokenRanges = new ArrayList<Range<Token>>(tokens.length / 2);
        for (int i = 0; i < tokens.length;) {
            Range<Token> range = new Range<Token>((Token) tokens[i++], (Token) tokens[i++]);
            this.tokenRanges.add(range);
        }
    }

    protected void innerWriteTo(StreamOutput out, boolean asKey) throws IOException {
        out.writeString(index);
        out.writeVInt(shardId);
        out.writeByte(searchType.id());
        if (!asKey) {
            out.writeVInt(numberOfShards);
        }
        if (scroll == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            scroll.writeTo(out);
        }
        out.writeBytesReference(source);
        out.writeBytesReference(extraSource);
        out.writeStringArray(types);
        out.writeStringArrayNullable(filteringAliases);
        if (!asKey) {
            out.writeVLong(nowInMillis);
        }

        out.writeBytesReference(templateSource);
        boolean hasTemplate = template != null;
        out.writeBoolean(hasTemplate);
        if (hasTemplate) {
            template.writeTo(out);
        }
        out.writeOptionalBoolean(requestCache);
        out.writeOptionalBoolean(tokenRangesBitsetCache);
        
        // write tokenRanges
        if (tokenRanges != null) {
            Token[] tokens = new Token[tokenRanges.size() * 2];
            int i = 0;
            for (Range<Token> range : tokenRanges) {
                tokens[i++] = range.left;
                tokens[i++] = range.right;
            }
            out.writeGenericValue(tokens);
        }
    }

    @Override
    public BytesReference cacheKey() throws IOException {
        BytesStreamOutput out = new BytesStreamOutput();
        this.innerWriteTo(out, true);
        // copy it over, most requests are small, we might as well copy to make sure we are not sliced...
        // we could potentially keep it without copying, but then pay the price of extra unused bytes up to a page
        return out.bytes().copyBytesArray();
    }
    

    @Override
    public Collection<Range<Token>> tokenRanges() {
        return this.tokenRanges;
    }

    @Override
    public Boolean tokenRangesBitsetCache() {
        return this.tokenRangesBitsetCache;
    }
}
