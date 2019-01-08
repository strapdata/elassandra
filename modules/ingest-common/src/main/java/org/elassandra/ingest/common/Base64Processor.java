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

import org.apache.cassandra.utils.Hex;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.script.ScriptService;

import java.util.Map;

/**
 * Convert a string representing byte array to a base64 string
 * field = the source binary field
 * target_field = target field name for the base64 encoded string (default is self)
 */
public class Base64Processor extends AbstractProcessor {

    public static final String TYPE = "base64";

    private final String field; // optional source timestamp
    private final String targetField; // optional destination field

    Base64Processor(String tag, String field, String targetField) {
        super(tag);
        this.field = field;
        this.targetField = targetField == null ? field : targetField;
    }

    @Override
    public void execute(IngestDocument ingestDocument) {
        String value = ingestDocument.getFieldValue(field, String.class);
        if (value != null) {
            byte[] bytes = Hex.hexToBytes(value);
            String targetValue = Base64.encodeBase64String(bytes);
            ingestDocument.setFieldValue( targetField, targetValue);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getField() {
        return this.field;
    }

    String getTargetField() {
        return this.targetField;
    }

    public static final class Factory implements Processor.Factory {

        private final ScriptService scriptService;

        public Factory(ScriptService scriptService) {
            this.scriptService = scriptService;
        }

        @Override
        public Base64Processor create(Map<String, Processor.Factory> registry, String processorTag,
                                        Map<String, Object> config) throws Exception {
            String field = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "field");
            String targetField = ConfigurationUtils.readStringProperty(TYPE, processorTag, config, "target_field", field);
            return new Base64Processor(processorTag, field, targetField);
        }
    }
}
