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

import org.apache.lucene.document.InetAddressPoint;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.elasticsearch.index.query.RangeQueryBuilder.GTE_FIELD;
import static org.elasticsearch.index.query.RangeQueryBuilder.GT_FIELD;
import static org.elasticsearch.index.query.RangeQueryBuilder.LTE_FIELD;
import static org.elasticsearch.index.query.RangeQueryBuilder.LT_FIELD;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra CQL types mapping tests.
 * @author vroyer
 *
 */
public class RangeFieldTests extends ESSingleNodeTestCase {

    private static String FROM_DATE = "2016-10-31";
    private static String TO_DATE = "2016-11-05 23:00:00";
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
            return QueryBuilders.termQuery("field", new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2016-11-02").getTime());
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

        String doc = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject()
                .startObject("field")
                .field(getFromField(), getFrom(type))
                .field(getToField(), getTo(type))
                .endObject()
                .endObject()).utf8ToString();
        System.out.println("insert "+indexName+" type="+ type + " doc="+doc+" query="+getMiddleQuery(type));

        assertThat(client().prepareIndex(indexName, "my_type", "1")
                .setSource(doc, XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));

        assertThat(client().prepareSearch().setIndices(indexName).setTypes("my_type")
                .setQuery(getMiddleQuery(type)).get().getHits().getTotalHits(), equalTo(1L));
    }

}

