/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
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
package org.elassandra;

import com.carrotsearch.randomizedtesting.generators.RandomPicks;
import com.google.common.net.InetAddresses;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.TupleType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.serializers.SimpleDateSerializer;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.lucene.document.InetAddressPoint;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.LuceneTestCase.ThrowingRunnable;
import org.elassandra.cluster.Serializer;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.mapper.RangeFieldMapper;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.elasticsearch.test.InternalSettingsPlugin;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.RangeQueryBuilder.GTE_FIELD;
import static org.elasticsearch.index.query.RangeQueryBuilder.GT_FIELD;
import static org.elasticsearch.index.query.RangeQueryBuilder.LTE_FIELD;
import static org.elasticsearch.index.query.RangeQueryBuilder.LT_FIELD;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra CQL types mapping tests.
 * @author vroyer
 *
 */
public class RangeFieldTests extends ESSingleNodeTestCase {

    private static String FROM_DATE = "2016-10-31";
    private static String TO_DATE = "2016-11-02 23:00:00";
    private static String FROM_IP = "::ffff:c0a8:107";
    private static String TO_IP = "2001:db8::";
    private static int FROM = 5;
    private static String FROM_STR = FROM + "";
    private static int TO = 10;
    private static String TO_STR = TO + "";
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis";


    protected Set<String> TYPES = new HashSet<>(Arrays.asList("date_range", "ip_range", "float_range", "double_range", "integer_range", "long_range"));


    private Object getFrom(String type) {
        if (type.equals("date_range")) {
            return FROM_DATE;
        } else if (type.equals("ip_range")) {
            return FROM_IP;
        }
        return random().nextBoolean() ? FROM : FROM_STR;
    }

    private String getFromField() {
        return random().nextBoolean() ? GT_FIELD.getPreferredName() : GTE_FIELD.getPreferredName();
    }

    private String getToField() {
        return random().nextBoolean() ? LT_FIELD.getPreferredName() : LTE_FIELD.getPreferredName();
    }

    private Object getTo(String type) {
        if (type.equals("date_range")) {
            return TO_DATE;
        } else if (type.equals("ip_range")) {
            return TO_IP;
        }
        return random().nextBoolean() ? TO : TO_STR;
    }

    private Object getMax(String type) {
        if (type.equals("date_range") || type.equals("long_range")) {
            return Long.MAX_VALUE;
        } else if (type.equals("ip_range")) {
            return InetAddressPoint.MAX_VALUE;
        } else if (type.equals("integer_range")) {
            return Integer.MAX_VALUE;
        } else if (type.equals("float_range")) {
            return Float.POSITIVE_INFINITY;
        }
        return Double.POSITIVE_INFINITY;
    }

    private QueryBuilder getMiddleQuery(String type) throws ParseException {
        if (type.equals("date_range")) {
            return QueryBuilders.termQuery("field", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-11-01 10:00:00").getTime());
        } else if (type.equals("long_range")) {
            return QueryBuilders.termQuery("field", 6L);
        } else if (type.equals("ip_range")) {
            return QueryBuilders.termQuery("field", "::ffff:c0a8:108");
        } else if (type.equals("integer_range")) {
            return QueryBuilders.termQuery("field", 6);
        } else if (type.equals("float_range")) {
            return QueryBuilders.termQuery("field", (float)6);
        }
        return  QueryBuilders.termQuery("field", (double)6);
    }

    @Test
    public void testRangeTypes() throws Exception {
        for(String type : TYPES) {
            doTestRangeQuery(type);
        }
    }

    public void doTestRangeQuery(String type) throws Exception {

        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("my_type")
            .startObject("properties").startObject("field").field("type", type);
        if (type.equals("date_range")) {
            mapping = mapping.field("format", DATE_FORMAT);
        }
        mapping = mapping.endObject().endObject().endObject().endObject();

        String indexName = "test_"+type;

        assertAcked(client().admin().indices().prepareCreate(indexName).addMapping("my_type", mapping));
        ensureGreen(indexName);

        String doc = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("field")
                .field(getFromField(), getFrom(type))
                .field(getToField(), getTo(type))
                .endObject()
                .endObject().string();
        System.out.println("insert "+indexName+" type="+ type + " doc="+doc+" query="+getMiddleQuery(type));

        assertThat(client().prepareIndex(indexName, "my_type", "1")
                .setSource(doc, XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));

        assertThat(client().prepareSearch().setIndices(indexName).setTypes("my_type")
                .setQuery(getMiddleQuery(type)).get().getHits().getTotalHits(), equalTo(1L));
    }

}

