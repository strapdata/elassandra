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

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for various table options.
 * @author vroyer
 */
public class TableOptionsTests extends ESSingleNodeTestCase {

    @Test
    public void testTWCS() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("b")
                            .field("type", "double")
                            .field("cql_collection", "singleton")
                        .endObject()
                    .endObject()
                .endObject();

        Settings settings = Settings.builder()
                .put("index.table_options",
                        "default_time_to_live = 30 " +
                        "AND compaction = {'compaction_window_size': '1', " +
                        "                  'compaction_window_unit': 'MINUTES', " +
                        "                  'class': 'org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy'}")
                .build();

        assertAcked(client().admin().indices().prepareCreate("ks").setSettings(settings).addMapping("t1", mapping).get());
        ensureGreen("ks");

        System.out.println("schema version="+StorageService.instance.getSchemaVersion());
        Thread.sleep(3000);
        UntypedResultSet.Row row = process(ConsistencyLevel.ONE, "select * from system_schema.tables where keyspace_name='ks';").one();
        assertThat(row.getInt("default_time_to_live"), equalTo(30));
        Map<String, String> compaction = row.getMap("compaction", UTF8Type.instance, UTF8Type.instance);
        assertThat(compaction.get("class"), equalTo("org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy"));
        assertThat(compaction.get("compaction_window_size"), equalTo("1"));
        assertThat(compaction.get("compaction_window_unit"), equalTo("MINUTES"));
    }

}

