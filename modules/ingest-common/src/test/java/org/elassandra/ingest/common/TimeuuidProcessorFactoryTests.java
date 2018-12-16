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

import org.elasticsearch.ingest.TestTemplateService;
import org.elasticsearch.ingest.common.DateProcessor;
import org.elasticsearch.test.ESTestCase;
import org.junit.Before;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

public class TimeuuidProcessorFactoryTests extends ESTestCase {

    private TimeuuidProcessor.Factory factory;

    @Before
    public void init() {
        factory = new TimeuuidProcessor.Factory(TestTemplateService.instance());
    }

    public void testParseLocale() throws Exception {
        Map<String, Object> config = new HashMap<>();
        String sourceField = randomAlphaOfLengthBetween(1, 10);
        config.put("field", sourceField);
        config.put("formats", Collections.singletonList("dd/MM/yyyyy"));
        Locale locale = randomFrom(Locale.GERMANY, Locale.FRENCH, Locale.ROOT);
        config.put("locale", locale.toLanguageTag());

        TimeuuidProcessor processor = factory.create(null, null, config);
        assertThat(processor.getLocale().newInstance(Collections.emptyMap()).execute(), equalTo(locale.toLanguageTag()));
    }

    public void testCreateWithDefaults() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "ts");
        config.put("formats", Collections.singletonList("dd/MM/yyyyy"));
        config.put("locale", Locale.FRENCH.toLanguageTag());
        String processorTag = randomAlphaOfLength(10);

        TimeuuidProcessor processor = factory.create(null, processorTag, config);
        assertThat(processor.getTag(), equalTo(processorTag));
        assertThat(processor.getField(), equalTo("ts"));
        assertThat(processor.getTargetField(), equalTo(TimeuuidProcessor.DEFAULT_TARGET_FIELD));
    }

    public void testCreateWithTarget() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "timestamp");
        config.put("formats", Collections.singletonList("dd/MM/yyyyy"));
        config.put("locale", Locale.FRENCH.toLanguageTag());
        config.put("target_field", "ts");
        String processorTag = randomAlphaOfLength(10);

        TimeuuidProcessor processor = factory.create(null, processorTag, config);
        assertThat(processor.getTag(), equalTo(processorTag));
        assertThat(processor.getField(), equalTo("timestamp"));
        assertThat(processor.getTargetField(), equalTo("ts"));
    }

    public void testCreateWithTargetAndBucket() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("field", "timestamp");
        config.put("formats", Collections.singletonList("dd/MM/yyyyy"));
        config.put("locale", Locale.FRENCH.toLanguageTag());
        config.put("target_field", "ts");
        config.put("bucket_field", "bucket");
        config.put("bucket_time", "86400000");
        String processorTag = randomAlphaOfLength(10);

        TimeuuidProcessor processor = factory.create(null, processorTag, config);
        assertThat(processor.getTag(), equalTo(processorTag));
        assertThat(processor.getField(), equalTo("timestamp"));
        assertThat(processor.getTargetField(), equalTo("ts"));
        assertThat(processor.getBucketField(), equalTo("bucket"));
        assertThat(processor.getBucketTime(), equalTo(86400000L));
    }
}
