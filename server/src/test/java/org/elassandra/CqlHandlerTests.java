package org.elassandra;

import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.elassandra.index.ElasticIncomingPayload;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class CqlHandlerTests extends ESSingleNodeTestCase {

    public CqlHandlerTests() {
        super();
    }

    @SuppressForbidden(reason="test")
    @Test
    public void testSearch() throws IOException, InterruptedException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("foo").field("type", "keyword").field("cql_collection", "singleton").endObject()
                        .startObject("es_query").field("type", "keyword").field("cql_collection", "singleton").field("index","false").endObject()
            .endObject()
                .endObject();
        createIndex("test", Settings.EMPTY, "foo", mapping);
        ensureGreen("test");

        for(int i=0; i < 100; i++) {
            assertThat(client().prepareIndex("test", "foo", Integer.toString(i))
                    .setSource("{\"foo\": \"bar\" }", XContentType.JSON).get().getResult().getOp(), equalTo((byte)0));
        }

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder().should(new TermQueryBuilder("foo", "bar"));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(queryBuilder);
        String esQuery = sourceBuilder.toString(ToXContent.EMPTY_PARAMS);

        // default limit is 10
        UntypedResultSet rs = process(ConsistencyLevel.ONE, "SELECT * FROM test.foo WHERE es_query=?",esQuery);
        assertThat(rs.size(), equalTo(10));

        // with function
        Date now = new Date();
        rs = process(ConsistencyLevel.ONE, "SELECT writetime(foo), token(\"_id\") FROM test.foo WHERE es_query=? LIMIT 100",esQuery);
        assertThat(rs.size(), equalTo(100));
        UntypedResultSet.Row row = rs.iterator().next();
        assertThat(row.getColumns().size(), equalTo(2));
        assertThat(row.getTimestamp("writetime(foo)"), greaterThan(now));

        // with limit
        rs = process(ConsistencyLevel.ONE, "SELECT * FROM test.foo WHERE es_query=? LIMIT 50",esQuery);
        assertThat(rs.size(), equalTo(50));

        // with limit over index size
        rs = process(ConsistencyLevel.ONE, "SELECT * FROM test.foo WHERE es_query=? LIMIT 5000",esQuery);
        assertThat(rs.size(), equalTo(100));

        // message payload with protocol v4
        Long writetime = new Long(0);
        ByteBuffer buffer = UTF8Type.instance.decompose(esQuery);
        QueryOptions queryOptions = QueryOptions.create(ConsistencyLevel.ONE, Collections.singletonList(buffer), false, 5000, null, null, ProtocolVersion.V4);
        QueryState queryState = new QueryState( ClientState.forInternalCalls());
        ResultMessage message = ClientState.getCQLQueryHandler().process("SELECT * FROM test.foo WHERE es_query=?", queryState, queryOptions, Collections.EMPTY_MAP, System.nanoTime());
        ElasticIncomingPayload payloadInfo = new ElasticIncomingPayload(message.getCustomPayload());
        assertThat(payloadInfo.hitTotal, equalTo(100L));
        assertThat(payloadInfo.shardSuccessful, equalTo(1));
        assertThat(payloadInfo.shardFailed, equalTo(0));
        assertThat(payloadInfo.shardSkipped, equalTo(0));

        // page size = 75
        queryOptions = QueryOptions.create(ConsistencyLevel.ONE, Collections.singletonList(buffer), false, 75, null, null, ProtocolVersion.V4);
        message = ClientState.getCQLQueryHandler().process("SELECT * FROM test.foo WHERE es_query=? LIMIT 1000", queryState, queryOptions, Collections.EMPTY_MAP, System.nanoTime());
        rs = UntypedResultSet.create(((ResultMessage.Rows) message).result);
        assertThat(rs.size(), equalTo(75));
    }

    @SuppressForbidden(reason="test")
    @Test
    public void testNestedAggregation() throws IOException {
        createIndex("iot");
        ensureGreen("iot");
        process(ConsistencyLevel.ONE,"CREATE TABLE iot.sensor ( name text, ts timestamp, water int, power double, es_query text, es_options text, primary key ((name),ts))");
        assertAcked(client().admin().indices().preparePutMapping("iot")
                .setType("sensor")
                .setSource("{ \"sensor\" : { \"discover\" : \".*\" }}", XContentType.JSON)
                .get());

        // round initial date to a point for stable daily aggregation.
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int N = 10;
        for(long i=0; i < 24*10; i++) {
            Date ts = new Date(cal.getTime().getTime() + i*3600*1000);
            //System.out.println("i="+i+" ts="+ts+" ="+ts.getTime());
            process(ConsistencyLevel.ONE, "INSERT INTO iot.sensor (name,ts,water,power) VALUES ('box1',?,?,?)", ts, (int) i%2, randomDouble());
        }

        SumAggregationBuilder aggPower = AggregationBuilders.sum("agg_power").field("power");
        TermsAggregationBuilder aggWater = AggregationBuilders.terms("agg_water").field("water").subAggregation(aggPower);
        DateHistogramAggregationBuilder daily_agg = AggregationBuilders.dateHistogram("daily_agg")
                .field("ts")
                .dateHistogramInterval(DateHistogramInterval.DAY)
                .minDocCount(0)
                .subAggregation(aggWater);

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder().should(new TermQueryBuilder("name", "box1"));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(queryBuilder).aggregation(daily_agg);
        String esQuery = sourceBuilder.toString(ToXContent.EMPTY_PARAMS);

        // default limit is 10
        UntypedResultSet rs = process(ConsistencyLevel.ONE, "SELECT * FROM iot.sensor WHERE es_query=?", esQuery);
        for(ColumnSpecification cs : rs.metadata()) {
            System.out.print(cs.name.toCQLString()+" ");
        }

        /*
        System.out.println();
        for(Row row : rs) {
            for(ColumnSpecification cs : rs.metadata()) {
                if (row.has(cs.name.toString())) {
                    if (cs.type instanceof TimestampType)
                        System.out.print(new Date(row.getLong(cs.name.toString()))+" ");
                    else if (cs.type instanceof UTF8Type)
                        System.out.print(row.getLong(cs.name.toString())+" ");
                    else if (cs.type instanceof LongType)
                        System.out.print(row.getLong(cs.name.toString())+" ");
                    else if (cs.type instanceof DoubleType)
                        System.out.print(row.getDouble(cs.name.toString())+" ");
                }
            }
            System.out.println();
        }
        */
        assertThat(rs.size(), equalTo(N*2));
    }


}
