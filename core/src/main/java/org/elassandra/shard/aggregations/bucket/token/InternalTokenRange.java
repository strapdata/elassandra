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
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.search.aggregations.AggregationStreams;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.aggregations.bucket.BucketStreamContext;
import org.elasticsearch.search.aggregations.bucket.BucketStreams;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.format.ValueFormatter;

/**
 * Cassandra token range aggregation
 */
public class InternalTokenRange extends InternalRange<InternalTokenRange.Bucket, InternalTokenRange> {

    public final static Type TYPE = new Type("token_range", "tokenrange");

    private final static AggregationStreams.Stream STREAM = new AggregationStreams.Stream() {
        @Override
        public InternalTokenRange readResult(StreamInput in) throws IOException {
            InternalTokenRange range = new InternalTokenRange();
            range.readFrom(in);
            return range;
        }
    };

    private final static BucketStreams.Stream<Bucket> BUCKET_STREAM = new BucketStreams.Stream<Bucket>() {
        @Override
        public Bucket readResult(StreamInput in, BucketStreamContext context) throws IOException {
            Bucket buckets = new Bucket(context.keyed());
            buckets.readFrom(in);
            return buckets;
        }

        @Override
        public BucketStreamContext getBucketStreamContext(Bucket bucket) {
            BucketStreamContext context = new BucketStreamContext();
            context.keyed(bucket.keyed());
            return context;
        }
    };

    public static void registerStream() {
        AggregationStreams.registerStream(STREAM, TYPE.stream());
        BucketStreams.registerStream(BUCKET_STREAM, TYPE.stream());
    }

    public static final Factory FACTORY = new Factory();

    public static class Bucket extends InternalRange.Bucket {

        public Bucket(boolean keyed) {
            super(keyed, ValueFormatter.TOKEN);
        }

        public Bucket(String key, long from, long to, long docCount, List<InternalAggregation> aggregations, boolean keyed) {
            super(key, from, to, docCount, new InternalAggregations(aggregations), keyed, ValueFormatter.TOKEN);
        }

        public Bucket(String key, long from, long to, long docCount, InternalAggregations aggregations, boolean keyed) {
            super(key, from, to, docCount, aggregations, keyed, ValueFormatter.TOKEN);
        }

        @Override
        public String getFromAsString() {
            return ValueFormatter.TOKEN.format(from);
        }

        @Override
        public String getToAsString() {
            return ValueFormatter.TOKEN.format(to);
        }

        @Override
        protected InternalRange.Factory<Bucket, ?> getFactory() {
            return FACTORY;
        }

        boolean keyed() {
            return keyed;
        }
    }

    public static class Factory extends InternalRange.Factory<InternalTokenRange.Bucket, InternalTokenRange> {

        @Override
        public String type() {
            return TYPE.name();
        }

        @Override
        public InternalTokenRange create(String name, List<Bucket> ranges, ValueFormatter formatter, boolean keyed,
                List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
            return new InternalTokenRange(name, ranges, keyed, pipelineAggregators, metaData);
        }

        @Override
        public InternalTokenRange create(List<Bucket> ranges, InternalTokenRange prototype) {
            return new InternalTokenRange(prototype.name, ranges, prototype.keyed, prototype.pipelineAggregators(), prototype.metaData);
        }

        @Override
        public Bucket createBucket(String key, long from, long to, long docCount, InternalAggregations aggregations, boolean keyed,
                ValueFormatter formatter) {
            return new Bucket(key, from, to, docCount, aggregations, keyed);
        }

        @Override
        public Bucket createBucket(InternalAggregations aggregations, Bucket prototype) {
            return new Bucket(prototype.getKey(), ((Number) prototype.getFrom()).longValue(), ((Number) prototype.getTo()).longValue(),
                    prototype.getDocCount(), aggregations, prototype.getKeyed());
        }
    }

    public InternalTokenRange() {} // for serialization

    public InternalTokenRange(String name, List<InternalTokenRange.Bucket> ranges, boolean keyed, List<PipelineAggregator> pipelineAggregators,
            Map<String, Object> metaData) {
        super(name, ranges, ValueFormatter.TOKEN, keyed, pipelineAggregators, metaData);
    }

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public InternalRange.Factory<Bucket, InternalTokenRange> getFactory() {
        return FACTORY;
    }
}
