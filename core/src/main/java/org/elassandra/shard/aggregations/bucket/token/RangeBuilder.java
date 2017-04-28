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

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Builder for the {@link Range} aggregation.
 */
public class RangeBuilder extends AbstractRangeBuilder<RangeBuilder> {

    private String format;

    /**
     * Sole constructor.
     */
    public RangeBuilder(String name) {
        super(name, InternalRange.TYPE.name());
    }

    /**
     * Add a new range to this aggregation.
     *
     * @param key  the key to use for this range in the response
     * @param from the lower bound on the distances, inclusive
     * @parap to   the upper bound on the distances, exclusive
     */
    public RangeBuilder addRange(String key, long from, long to) {
        ranges.add(new Range(key, from, to));
        return this;
    }

    /**
     * Same as {@link #addRange(String, long, long)} but the key will be
     * automatically generated based on <code>from</code> and <code>to</code>.
     */
    public RangeBuilder addRange(long from, long to) {
        return addRange(null, from, to);
    }

    /**
     * Add a new range with no lower bound.
     *
     * @param key the key to use for this range in the response
     * @param to  the upper bound on the distances, exclusive
     */
    public RangeBuilder addUnboundedTo(String key, long to) {
        ranges.add(new Range(key, null, to));
        return this;
    }

    /**
     * Same as {@link #addUnboundedTo(String, long)} but the key will be
     * computed automatically.
     */
    public RangeBuilder addUnboundedTo(long to) {
        return addUnboundedTo(null, to);
    }

    /**
     * Add a new range with no upper bound.
     *
     * @param key  the key to use for this range in the response
     * @param from the lower bound on the distances, inclusive
     */
    public RangeBuilder addUnboundedFrom(String key, long from) {
        ranges.add(new Range(key, from, null));
        return this;
    }

    /**
     * Same as {@link #addUnboundedFrom(String, long)} but the key will be
     * computed automatically.
     */
    public RangeBuilder addUnboundedFrom(long from) {
        return addUnboundedFrom(null, from);
    }

    /**
     * Set the format to use to display values.
     */
    public RangeBuilder format(String format) {
        this.format = format;
        return this;
    }


    @Override
    protected XContentBuilder doInternalXContent(XContentBuilder builder, Params params) throws IOException {
        super.doInternalXContent(builder, params);
        if (format != null) {
            builder.field("format", format);
        }
        return builder;
    }

}
