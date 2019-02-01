/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
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

package org.elassandra.index;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.CQLStatement;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.ResultSet;
import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.PartitionPosition;
import org.apache.cassandra.db.filter.RowFilter.Expression;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.TimestampType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.pager.PagingState;
import org.apache.cassandra.service.pager.PagingState.RowMark;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.Logger;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationMetaDataBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.pipeline.InternalSimpleValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.CqlFetchPhase;
import org.joda.time.DateTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * CQL query processor executing Elasticsearch query.
 */
public class ElasticQueryHandler extends QueryProcessor  {

    private static final Logger logger = Loggers.getLogger(ElasticQueryHandler.class);

    public static final String SELECTION = "_selection";

    public ElasticQueryHandler() {
        super();
    }

    @Override
    public ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options, long queryStartNanoTime)
            throws RequestExecutionException, RequestValidationException
    {
        ClientState clientState = queryState.getClientState();
        statement.checkAccess(clientState);
        statement.validate(clientState);

        if (statement instanceof SelectStatement) {
            SelectStatement select = (SelectStatement)statement;
            if (!select.getSelection().isAggregate()) {
                String elasticQuery = null;
                String elasticOptions = null;
                for(Expression expr : select.getRowFilter(options).getExpressions()) {
                    if (expr.column().name.bytes.equals(ElasticSecondaryIndex.ES_QUERY_BYTE_BUFFER)) {
                        elasticQuery = UTF8Type.instance.getString(expr.getIndexValue());
                        if (elasticOptions != null)
                            break;
                    } else if (expr.column().name.bytes.equals(ElasticSecondaryIndex.ES_OPTIONS_BYTE_BUFFER)) {
                        elasticOptions = UTF8Type.instance.getString(expr.getIndexValue());
                        if (elasticQuery != null)
                            break;
                    }
                }

                if (elasticQuery != null) {
                    ColumnFamilyStore cfs = Keyspace.open(select.keyspace()).getColumnFamilyStore(select.columnFamily());
                    Index index = cfs.indexManager.getIndexByName(ClusterService.buildIndexName(cfs.name));
                    if (index instanceof ExtendedElasticSecondaryIndex) {
                        Map<String, String> esOptions = null;
                        if (elasticOptions != null) {
                            esOptions = new HashMap<>();
                            for(NameValuePair pair : URLEncodedUtils.parse(elasticOptions, Charset.forName("UTF-8")))
                                esOptions.put(pair.getName(), pair.getValue());
                        }
                        ExtendedElasticSecondaryIndex elasticIndex = (ExtendedElasticSecondaryIndex)index;
                        return executeElasticQuery(select, queryState, options, queryStartNanoTime, (ElasticSecondaryIndex)elasticIndex.elasticSecondaryIndex, elasticQuery, esOptions);
                    }
                }
            }
        }
        ResultMessage result = statement.execute(queryState, options, queryStartNanoTime);
        return result == null ? new ResultMessage.Void() : result;
    }

    void handle(QueryState queryState, Client client) {
    }

    ResultMessage executeElasticQuery(SelectStatement select, QueryState queryState, QueryOptions options, long queryStartNanoTime, ElasticSecondaryIndex index, String query, Map<String, String> esOptions) {

        Client client = ElassandraDaemon.instance.node().client();
        ThreadContext context = client.threadPool().getThreadContext();
        Map<String, Object> extraParams = null;
        try (ThreadContext.StoredContext stashedContext = context.stashContext()) {
            int limit = select.getLimit(options) == Integer.MAX_VALUE ? 10 : select.getLimit(options);
            PagingState paging = options.getPagingState();
            String scrollId = null;
            int remaining = limit;
            if (paging != null) {
                scrollId = ByteBufferUtil.string(paging.partitionKey);
                remaining = paging.remaining;
            }

            if (Tracing.isTracing()) {
                extraParams = new HashMap<>();
                extraParams.put("_cassandra.trace.session", Tracing.instance.getSessionId().toString());
                extraParams.put("_cassandra.trace.coordinator", FBUtilities.getBroadcastAddress().getHostAddress());
                Tracing.instance.begin("Elasticsearch query", FBUtilities.getBroadcastAddress(), Collections.EMPTY_MAP);
            }

            boolean hasAgregation = false;
            SearchResponse resp;
            AggregationMetaDataBuilder aggMetadataBuilder = null;
            if (scrollId == null) {
            	SearchSourceBuilder ssb = null;
            	try {
            		XContentParser parser = JsonXContent.jsonXContent.createParser(ElassandraDaemon.instance.node().getNamedXContentRegistry(), query);
            		ssb = SearchSourceBuilder.fromXContent(parser);
            	} catch(ParsingException e) {
            		throw new SyntaxException(e.getMessage());
            	}
                String indices = (esOptions != null && esOptions.containsKey("indices")) ? esOptions.get("indices") : select.keyspace();
                boolean toJson = select.parameters.isJson || (esOptions != null && esOptions.containsKey("json"));
                SearchRequestBuilder srb = client.prepareSearch(indices)
                    .setSource(ssb)
                    .setTypes(index.typeName);

                AbstractBounds bounds = select.getRestrictions().getPartitionKeyBounds(options);
                if (bounds != null) {
                    Token left = ((PartitionPosition) bounds.left).getToken();
                    Token right = ((PartitionPosition) bounds.right).getToken();
                    // undefined bound is set to minimum.
                    if (!left.isMinimum() || !right.isMinimum()) {
                        Range range = (!left.isMinimum() && right.isMinimum()) ? new Range(left, AbstractSearchStrategy.TOKEN_MAX) : new Range(left, right);
                        srb.setTokenRanges(Collections.singletonList(range));
                        if (logger.isDebugEnabled())
                            logger.debug("tokenRanges={}", range);
                    }
                }
                if (esOptions != null && esOptions.containsKey("preference"))
                    srb.setPreference(esOptions.get("preference"));
                if (esOptions != null && esOptions.containsKey("routing"))
                    srb.setRouting(esOptions.get("routing"));

                hasAgregation = ssb.aggregations() != null;
                if (hasAgregation) {
                    srb.setSize(0);
                    aggMetadataBuilder = new AggregationMetaDataBuilder(select.keyspace(), "aggs", toJson );
                    aggMetadataBuilder.build("", ssb.aggregations(), select.getSelection());
                } else {
                    if (extraParams == null)
                        extraParams = new HashMap<>();
                    context.putTransient(SELECTION, select.getSelection());
                    extraParams.put(CqlFetchPhase.PROJECTION, select.getSelection().toCQLString());
                    if (toJson)
                        extraParams.put("_json", "true");

                    if (options.getPageSize() > 0 && limit > options.getPageSize()) {
                        srb.setScroll(new Scroll(new TimeValue(300, TimeUnit.SECONDS)));
                        srb.setSize(options.getPageSize());
                    } else {
                        srb.setSize(limit);
                    }
                }
                handle(queryState, client);
                if (extraParams != null)
                    srb.setExtraParams(extraParams);
                resp = srb.get();
                scrollId = resp.getScrollId();
            } else {
                SearchScrollRequestBuilder ssrb = client.prepareSearchScroll(scrollId);
                handle(queryState, client);
                if (extraParams != null)
                    ssrb.setExtraParams(extraParams);
                resp = ssrb.get();
            }

            ResultSet.ResultMetadata resultMetadata = null;
            List<List<ByteBuffer>> rows = new LinkedList<>();
            if (hasAgregation) {
                // add aggregation results
                flattenAggregation(aggMetadataBuilder, 0, "", resp.getAggregations(), rows);

                if (select.getSelection().isWildcard()) {
                    resultMetadata = new ResultSet.ResultMetadata(aggMetadataBuilder.getColumns());
                } else {
                    List<ColumnSpecification> columns = aggMetadataBuilder.getColumns();
                    List<ColumnSpecification> projectionColumns = new ArrayList<>(aggMetadataBuilder.getColumns().size());
                    int i = 0;
                    for(ColumnDefinition cd : select.getSelection().getColumns()) {
                        if (i < columns.size() && !cd.type.isValueCompatibleWith(columns.get(i).type)) {
                            logger.warn("Aggregation column ["+columns.get(i).name.toString()+"] of type ["+
                                    columns.get(i).type+"] is not compatible with projection term ["+cd.name.toCQLString()+"] of type ["+cd.type+"]");
                            throw new InvalidRequestException("Aggregation column "+columns.get(i).name.toString()+
                                    " of type "+columns.get(i).type+" is not compatible with projection term "+cd.name.toCQLString());
                        }
                        projectionColumns.add(cd);
                        i++;
                    }
                    resultMetadata = new ResultSet.ResultMetadata(projectionColumns);
                }
            } else {
                // add row results
                for(SearchHit hit : resp.getHits().getHits()) {
                    if (hit.getValues() != null)
                        rows.add(hit.getValues());
                }
                resultMetadata = select.getResultMetadata().copy();
                if (scrollId != null) {
                    // paging management
                    remaining -= rows.size();
                    if ((options.getPageSize() > 0 && rows.size() < options.getPageSize()) || remaining <= 0) {
                        client.prepareClearScroll().addScrollId(scrollId).get();
                        resultMetadata.setHasMorePages(null);
                    } else {
                        resultMetadata.setHasMorePages(new PagingState(
                           ByteBufferUtil.bytes(scrollId, Charset.forName("UTF-8")), (RowMark) null, remaining, remaining));
                    }
                }
            }

            ResultMessage.Rows messageRows = new ResultMessage.Rows(new ResultSet(resultMetadata, rows));
            // see https://docs.datastax.com/en/developer/java-driver/3.2/manual/custom_payloads/
            if (options.getProtocolVersion().isGreaterOrEqualTo(ProtocolVersion.V4)) {
                Map<String,ByteBuffer> customPayload = new HashMap<String,ByteBuffer>();
                customPayload.put("_shards.successful", ByteBufferUtil.bytes(resp.getSuccessfulShards()));
                customPayload.put("_shards.skipped", ByteBufferUtil.bytes(resp.getSkippedShards()));
                customPayload.put("_shards.failed", ByteBufferUtil.bytes(resp.getFailedShards()));
                customPayload.put("_shards.total", ByteBufferUtil.bytes(resp.getTotalShards()));
                customPayload.put("hits.total", ByteBufferUtil.bytes(resp.getHits().getTotalHits()));
                customPayload.put("hits.max_score", ByteBufferUtil.bytes(resp.getHits().getMaxScore()));
                if (logger.isDebugEnabled())
                    logger.debug("Add custom payload, _shards.successful={}, _shards.skipped={}, _shards.failed={}, _shards.total={}, hits.total={}, hits.max_score={}",
                            resp.getSuccessfulShards(),
                            resp.getSkippedShards(),
                            resp.getFailedShards(),
                            resp.getTotalShards(),
                            resp.getHits().getTotalHits(),
                            resp.getHits().getMaxScore());
                messageRows.setCustomPayload(customPayload);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Cannot add payload, ProtocolVersion={}", options.getProtocolVersion());
            }
            if (Tracing.isTracing())
                Tracing.instance.stopSession();
            return messageRows;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Set element at a position in the list
    void setElement(List<ByteBuffer> l, int index, ByteBuffer element) {
        l.set(index, element);
    }


    List<ByteBuffer> getRow(final AggregationMetaDataBuilder amdb, long level, List<List<ByteBuffer>> rows) {
        List<ByteBuffer> row = rows.size() > 0 ? rows.get(rows.size() - 1) : null;
        if (level == 0) {
            row = Arrays.asList(new ByteBuffer[amdb.size()]);
            rows.add(row);
        }
        return row;
    }

    List<ByteBuffer> getRowForBucket(final AggregationMetaDataBuilder amdb, long level, int index, boolean firstBucket, List<List<ByteBuffer>> rows) {
        List<ByteBuffer> row = getRow(amdb, level, rows);
        if (!firstBucket && level > 0) {
            // duplicate left part of the row to fill right part for buckets.
            List<ByteBuffer> row2 = Arrays.asList(new ByteBuffer[amdb.size()]);
            for(int i = 0; i < index; i++) {
                if (row.get(i) != null)
                   row2.set(i, row.get(i).duplicate());
            }
            rows.add(row2);
            row = row2;
        }
        return row;
    }

    // flatten tree results to a table of rows.
    void flattenAggregation(final AggregationMetaDataBuilder amdb, final long level, String prefix, final Aggregations aggregations, List<List<ByteBuffer>> rows) throws IOException {
        List<ByteBuffer> row; // current filled row
        for(Aggregation agg : aggregations) {
            String type = agg.getType();
            String baseName = prefix+agg.getName()+".";

            if (amdb.toJson()) {
                switch(type) {
                case "dterms":
                case "lterms":
                case "sterms":
                    for (Terms.Bucket termBucket : ((Terms)agg).getBuckets()) {
                        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
                        xContentBuilder.startObject();
                        termBucket.toXContent(xContentBuilder, ToXContent.EMPTY_PARAMS);
                        xContentBuilder.endObject();
                        setElement(getRow(amdb, level, rows), amdb.getColumn(agg.getName()), ByteBufferUtil.bytes(xContentBuilder.string()));
                    }
                    break;
                case "date_histogram":
                    for (InternalDateHistogram.Bucket histoBucket : ((InternalDateHistogram)agg).getBuckets()) {
                        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
                        if (histoBucket.getKeyed())
                            xContentBuilder.startObject();
                        histoBucket.toXContent(xContentBuilder, ToXContent.EMPTY_PARAMS);
                        if (histoBucket.getKeyed())
                            xContentBuilder.endObject();
                        setElement(getRow(amdb, level, rows), amdb.getColumn(agg.getName()), ByteBufferUtil.bytes(xContentBuilder.string()));
                    }
                    break;
                default:
                    logger.error("unsupported aggregation type=[{}] name=[{}]", type, agg.getName());
                    throw new IllegalArgumentException("unsupported aggregation type=["+type+"] name=["+agg.getName()+"]");
                }

            } else {
                switch(type) {
                case "sterms": {
                        int keyIdx = amdb.getColumn(baseName+"key");
                        int cntIdx = amdb.getColumn(baseName+"count");
                        boolean fistBucket = true;
                        for (Terms.Bucket termBucket : ((Terms)agg).getBuckets()) {
                            row = getRowForBucket(amdb, level, keyIdx, fistBucket, rows);
                            setElement(row, keyIdx, ByteBufferUtil.bytes(termBucket.getKeyAsString()));
                            setElement(row, cntIdx, ByteBufferUtil.bytes(termBucket.getDocCount()));
                            if (termBucket.getAggregations().iterator().hasNext())
                                flattenAggregation(amdb, level+1, baseName, termBucket.getAggregations(), rows);
                            fistBucket=false;
                        }
                    }
                    break;
                case "lterms": {
                        int keyIdx = amdb.getColumn(baseName+"key");
                        int cntIdx = amdb.getColumn(baseName+"count");
                        amdb.setColumnType(keyIdx, baseName+"key", LongType.instance);
                        boolean fistBucket = true;
                        for (Terms.Bucket termBucket : ((Terms)agg).getBuckets()) {
                            row = getRowForBucket(amdb, level, keyIdx, fistBucket, rows);
                            setElement(row, keyIdx, ByteBufferUtil.bytes((long)termBucket.getKeyAsNumber()));
                            setElement(row, cntIdx, ByteBufferUtil.bytes(termBucket.getDocCount()));
                            if (termBucket.getAggregations().iterator().hasNext())
                                flattenAggregation(amdb,level+1, baseName, termBucket.getAggregations(), rows);
                            fistBucket=false;
                        }
                    }
                    break;
                case "dterms": {
                    int keyIdx = amdb.getColumn(baseName+"key");
                    int cntIdx = amdb.getColumn(baseName+"count");
                    amdb.setColumnType(keyIdx, baseName+"key", DoubleType.instance);
                    boolean fistBucket = true;
                        for (Terms.Bucket termBucket : ((Terms)agg).getBuckets()) {
                            row = getRowForBucket(amdb, level, keyIdx, fistBucket, rows);
                            setElement(row,keyIdx, ByteBufferUtil.bytes((double)termBucket.getKeyAsNumber()));
                            setElement(row,cntIdx, ByteBufferUtil.bytes(termBucket.getDocCount()));
                            if (termBucket.getAggregations().iterator().hasNext())
                                flattenAggregation(amdb,level+1, baseName, termBucket.getAggregations(), rows);
                            fistBucket=false;
                        }
                    }
                    break;
                case "date_histogram": {
                        int keyIdx = amdb.getColumn(baseName+"key");
                        int cntIdx = amdb.getColumn(baseName+"count");
                        boolean fistBucket = true;
                        for (InternalDateHistogram.Bucket histoBucket : ((InternalDateHistogram)agg).getBuckets()) {
                            row = getRowForBucket(amdb, level, keyIdx, fistBucket, rows);
                            setElement(row,keyIdx, TimestampType.instance.getSerializer().serialize(((DateTime)histoBucket.getKey()).toDate()));
                            setElement(row,cntIdx, ByteBufferUtil.bytes(histoBucket.getDocCount()));
                            if (histoBucket.getAggregations().iterator().hasNext())
                                flattenAggregation(amdb,level+1, baseName, histoBucket.getAggregations(), rows);
                            fistBucket=false;
                        }
                    }
                    break;
                case "histogram": {
                    int keyIdx = amdb.getColumn(baseName+"value");
                    int cntIdx = amdb.getColumn(baseName+"count");
                    boolean fistBucket = true;
                    for (InternalHistogram.Bucket histoBucket : ((InternalHistogram)agg).getBuckets()) {
                        row = getRowForBucket(amdb, level, keyIdx, fistBucket, rows);
                        setElement(row,keyIdx, ByteBufferUtil.bytes((double)histoBucket.getKey()));
                        setElement(row,cntIdx, ByteBufferUtil.bytes(histoBucket.getDocCount()));
                        if (histoBucket.getAggregations().iterator().hasNext())
                            flattenAggregation(amdb,level+1, baseName, histoBucket.getAggregations(), rows);
                        fistBucket=false;
                    }
                }
                break;
                case "sum": {
                        Sum sum = (Sum)agg;
                        row = getRow(amdb, level, rows);
                        setElement(row, amdb.getColumn(baseName+"sum"), ByteBufferUtil.bytes((double)sum.getValue()));
                    }
                    break;
                case "avg": {
                        Avg avg = (Avg)agg;
                        row = getRow(amdb, level, rows);
                        setElement(row, amdb.getColumn(baseName+"avg"), ByteBufferUtil.bytes((double)avg.getValue()));
                    }
                    break;
                case "min": {
                        Min min = (Min)agg;
                        row = getRow(amdb, level, rows);
                        setElement(row, amdb.getColumn(baseName+"min"), ByteBufferUtil.bytes((double)min.getValue()));
                    }
                    break;
                case "max": {
                        Max max = (Max)agg;
                        row = getRow(amdb, level, rows);
                        setElement(row, amdb.getColumn(baseName+"max"), ByteBufferUtil.bytes((double)max.getValue()));
                    }
                    break;
                case "percentiles": {
                        Percentile percentile = (Percentile)agg;
                        int valIdx = amdb.getColumn(baseName+"value");
                        int pctIdx = amdb.getColumn(baseName+"percent");
                        row = getRow(amdb, level, rows);
                        setElement(row, valIdx, ByteBufferUtil.bytes((double)percentile.getValue()));
                        setElement(row, pctIdx, ByteBufferUtil.bytes((double)percentile.getPercent()));
                    }
                    break;
                case "simple_value": {
                        InternalSimpleValue simpleValue = (InternalSimpleValue)agg;
                        int valIdx = amdb.getColumn(simpleValue.getName());
                        row = getRow(amdb, level, rows);
                        setElement(row, valIdx, ByteBufferUtil.bytes(simpleValue.getValue()));
                    }
                    break;
                default:
                    logger.error("unsupported aggregation type=[{}] name=[{}]", type, agg.getName());
                    throw new IllegalArgumentException("unsupported aggregation type=["+type+"] name=["+agg.getName()+"]");
                }
            }
        }
    }
}
