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

import org.apache.cassandra.utils.UUIDGen;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.util.LocaleUtils;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.script.TemplateScript;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Generates a TimeUUID as document _id, eventually depending on an existing date field like (@timestamp), and optionally removes the timestamp.
 * field = th source timestamp field (or now)
 * target_field = target field name for the timeuuid (default is _id)
 */
public class TimeuuidProcessor extends AbstractProcessor {

    public static final String TYPE = "timeuuid";
    static final String DEFAULT_TARGET_FIELD = "@timestamp";

    private final TemplateScript.Factory timezone;
    private final TemplateScript.Factory locale;
    private final String field; // optional source timestamp
    private final String targetField; // optional destination field
    private final String bucketField; // optional bucket field
    private final Long bucketTime; // optional bucket time to round timestamp
    private final List<String> formats;
    private final List<Function<Map<String, Object>, Function<String, DateTime>>> dateParsers;

    TimeuuidProcessor(String tag, @Nullable TemplateScript.Factory timezone, @Nullable TemplateScript.Factory locale,
            String field, List<String> formats, String targetField, String bucketField, Long bucketTime) {
        super(tag);
        this.timezone = timezone;
        this.locale = locale;
        this.field = field;
        this.targetField = targetField;
        this.bucketField = bucketField;
        this.bucketTime = bucketTime;
        this.formats = formats;
        this.dateParsers = new ArrayList<>(this.formats.size());
        for (String format : formats) {
            org.elasticsearch.ingest.common.DateFormat dateFormat = org.elasticsearch.ingest.common.DateFormat.fromString(format);
            dateParsers.add((params) -> dateFormat.getFunction(format, newDateTimeZone(params), newLocale(params)));
        }
    }

    private DateTimeZone newDateTimeZone(Map<String, Object> params) {
        return timezone == null ? DateTimeZone.UTC : DateTimeZone.forID(timezone.newInstance(params).execute());
    }

    private Locale newLocale(Map<String, Object> params) {
        return (locale == null) ? Locale.ROOT : LocaleUtils.parse(locale.newInstance(params).execute());
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) {
        Object obj = ingestDocument.getFieldValue(field, Object.class);
        String value = null;
        if (obj != null) {
            // Not use Objects.toString(...) here, because null gets changed to "null" which may confuse some date parsers
            value = obj.toString();
        }

        final UUID timeuuid;
        if (value != null) {
            DateTime dateTime = null;
            Exception lastException = null;
            for (Function<Map<String, Object>, Function<String, DateTime>> dateParser : dateParsers) {
                try {
                    dateTime = dateParser.apply(ingestDocument.getSourceAndMetadata()).apply(value);
                } catch (Exception e) {
                    //try the next parser and keep track of the exceptions
                    lastException = ExceptionsHelper.useOrSuppress(lastException, e);
                }
            }
            if (dateTime == null) {
                throw new IllegalArgumentException("unable to parse date [" + value + "]", lastException);
            }
            // randomized UUID from the provided timestamp
            timeuuid = UUIDGen.getRandomTimeUUIDFromMicros(dateTime.getMillis() * 1000);
        } else {
            // randomized UUID from now
            timeuuid = UUIDGen.getRandomTimeUUIDFromMicros(System.currentTimeMillis() * 1000);
        }
        ingestDocument.setFieldValue( targetField, timeuuid.toString());

        if (bucketField != null && bucketTime != null) {
            long timestamp = UUIDGen.unixTimestamp(timeuuid);
            ingestDocument.setFieldValue( bucketField, Long.toString(timestamp / bucketTime));
        }
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    TemplateScript.Factory getTimezone() {
        return timezone;
    }

    TemplateScript.Factory getLocale() {
        return locale;
    }

    String getField() {
        return this.field;
    }

    String getTargetField() {
        return this.targetField;
    }

    String getBucketField() {
        return this.bucketField;
    }

    Long getBucketTime() {
        return this.bucketTime;
    }

    List<String> getFormats() {
        return formats;
    }

    public static final class Factory implements Processor.Factory {

        private final ScriptService scriptService;

        public Factory(ScriptService scriptService) {
            this.scriptService = scriptService;
        }

        @Override
        public TimeuuidProcessor create(Map<String, Processor.Factory> registry, String processorTag,
                                        Map<String, Object> config) throws Exception {
            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field");
            String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field", DEFAULT_TARGET_FIELD);
            String bucketField = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "bucket_field");
            String bucketTime = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "bucket_time");
            String timezoneString = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "timezone");
            TemplateScript.Factory compiledTimezoneTemplate = null;
            if (timezoneString != null) {
                compiledTimezoneTemplate = ConfigurationUtils.compileTemplate(TYPE, processorTag,
                    "timezone", timezoneString, scriptService);
            }
            String localeString = ConfigurationUtils.readOptionalStringProperty(TYPE, processorTag, config, "locale");
            TemplateScript.Factory compiledLocaleTemplate = null;
            if (localeString != null) {
                compiledLocaleTemplate = ConfigurationUtils.compileTemplate(TYPE, processorTag,
                    "locale", localeString, scriptService);
            }
            List<String> formats = ConfigurationUtils.readList(TYPE, processorTag, config, "formats");
            return new TimeuuidProcessor(processorTag, compiledTimezoneTemplate, compiledLocaleTemplate, field, formats, targetField, bucketField, bucketTime == null ? null : Long.parseLong(bucketTime));
        }
    }
}
