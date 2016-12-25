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

import org.elassandra.shard.aggregations.bucket.token.AbstractRangeBuilder;

/**
 * Builder for the {@link DateRange} aggregation.
 */
public class TokenRangeBuilder extends AbstractRangeBuilder<TokenRangeBuilder> {
    /**
     * Sole constructor.
     */
    public TokenRangeBuilder(String name) {
        super(name, InternalTokenRange.TYPE.name());
    }

    /**
     * Add a new range to this aggregation.
     *
     * @param key  the key to use for this range in the response
     * @param from the lower bound on the distances, inclusive
     * @parap to   the upper bound on the distances, exclusive
     */
    public TokenRangeBuilder addRange(String key, Object from, Object to) {
        ranges.add(new Range(key, from, to));
        return this;
    }

    /**
     * Same as {@link #addRange(String, double, double)} but the key will be
     * automatically generated based on <code>from</code> and <code>to</code>.
     */
    public TokenRangeBuilder addRange(Object from, Object to) {
        return addRange(null, from, to);
    }


}
