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

package org.elassandra.ingest.common;

import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.ingest.TestTemplateService;
import org.elasticsearch.ingest.common.DateProcessor;
import org.elasticsearch.script.TemplateScript;
import org.elasticsearch.test.ESTestCase;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class TimeuuidProcessorTests extends ESTestCase {
    private TemplateScript.Factory templatize(Locale locale) {
        return new TestTemplateService.MockTemplateScript.Factory(locale.getLanguage());
    }

    private TemplateScript.Factory templatize(DateTimeZone timezone) {
        return new TestTemplateService.MockTemplateScript.Factory(timezone.getID());
    }

    public void testWithISO() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("date_as_string", "2018-12-13T14:30:31.268Z");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        Processor processor = new TimeuuidProcessor(randomAlphaOfLength(10),
                templatize(DateTimeZone.forID("Europe/Amsterdam")), templatize(Locale.ENGLISH),
                "date_as_string", Collections.singletonList("ISO8601"), "ts", null, null);

        processor.execute(ingestDocument);
        assertThat(ingestDocument.getFieldValue("ts", String.class), notNullValue());
    }

    public void testWitBucket() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("date_as_string", "2018-12-13T14:30:31.000Z");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        Processor processor = new TimeuuidProcessor(randomAlphaOfLength(10),
                templatize(DateTimeZone.forID("Europe/Amsterdam")), templatize(Locale.ENGLISH),
                "date_as_string", Collections.singletonList("ISO8601"), "ts", "bucket", 86400000L);

        processor.execute(ingestDocument);
        assertThat(ingestDocument.getFieldValue("ts", String.class), notNullValue());
        assertThat(ingestDocument.getFieldValue("bucket", String.class), equalTo("17878"));
    }
}
