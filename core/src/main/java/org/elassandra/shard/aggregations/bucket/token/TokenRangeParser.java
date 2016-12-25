/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
 * Contains some code from Elasticsearch (http://www.elastic.co)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elassandra.shard.aggregations.bucket.token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.SearchParseException;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceParser;
import org.elasticsearch.search.internal.SearchContext;

/**
 *
 */
public class TokenRangeParser implements Aggregator.Parser {

    @Override
    public String type() {
        return InternalTokenRange.TYPE.name();
    }

    @Override
    public AggregatorFactory parse(String aggregationName, XContentParser parser, SearchContext context) throws IOException {

        ValuesSourceParser<ValuesSource.Numeric> vsParser = ValuesSourceParser.numeric(aggregationName, InternalTokenRange.TYPE, context)
                .targetValueType(ValueType.LONG)
                .formattable(false)
                .build();

        List<RangeAggregator.Range> ranges = null;
        boolean keyed = false;

        XContentParser.Token token;
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (vsParser.token(currentFieldName, token, parser)) {
                continue;
            } else if (token == XContentParser.Token.START_ARRAY) {
                if ("ranges".equals(currentFieldName)) {
                    ranges = new ArrayList<>();
                    while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                        long from = Long.MIN_VALUE;
                        long to = Long.MAX_VALUE;
                        String key = null;
                        String toOrFromOrKey = null;
                        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                            if (token == XContentParser.Token.FIELD_NAME) {
                                toOrFromOrKey = parser.currentName();
                            } else if (token == XContentParser.Token.VALUE_NUMBER) {
                                if ("from".equals(toOrFromOrKey)) {
                                    from = parser.longValue();
                                } else if ("to".equals(toOrFromOrKey)) {
                                    to = parser.longValue();
                                }
                            } else if (token == XContentParser.Token.VALUE_STRING) {
                                if ("key".equals(toOrFromOrKey)) {
                                    key = parser.text();
                                }
                            }
                        }
                        RangeAggregator.Range range = new RangeAggregator.Range(key, from, Long.toString(from), to, Long.toString(to));
                        ranges.add(range);
                    }
                } else {
                    throw new SearchParseException(context, "Unknown key for a " + token + " in [" + aggregationName + "]: ["
                            + currentFieldName + "].", parser.getTokenLocation());
                }
            } else if (token == XContentParser.Token.VALUE_BOOLEAN) {
                if ("keyed".equals(currentFieldName)) {
                    keyed = parser.booleanValue();
                } else {
                    throw new SearchParseException(context, "Unknown key for a " + token + " in [" + aggregationName + "]: ["
                            + currentFieldName + "].", parser.getTokenLocation());
                }
            } else {
                throw new SearchParseException(context, "Unexpected token " + token + " in [" + aggregationName + "].",
                        parser.getTokenLocation());
            }
        }

        if (ranges == null) {
            // add all ranges of the routing table.
            ranges = new ArrayList<>();
            for(Iterator<IndexShardRoutingTable> it = context.getClusterState().routingTable().index(context.mapperService().index().getName()).shards().valuesIt(); it.hasNext(); ) {
                for (ShardRouting shard : it.next().shards()) {
                    for(Range<Token> tokenRange : shard.tokenRanges()) {
                        Long left = (Long) tokenRange.left.getToken().getTokenValue() + 1;
                        Long right = (Long) tokenRange.right.getToken().getTokenValue();
                        RangeAggregator.Range range = new RangeAggregator.Range(null, left, left.toString(), right, right.toString());
                        ranges.add(range);
                    }
                }
            }
        }

        return new RangeAggregator.Factory(aggregationName, vsParser.config(), InternalTokenRange.FACTORY, ranges, keyed);
    }

}
