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
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class Base64ProcessorTests extends ESTestCase {

    public void testBinary() throws Exception {
        Map<String, Object> document = new HashMap<>();
        String hexa = "ed953b2e446ec412fc45f68356cfb0f9b3a065c19a1fdb748bcc58ba95c289fe";
        document.put("docker_id", hexa);
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        Processor processor = new Base64Processor(randomAlphaOfLength(10), "docker_id", null);

        processor.execute(ingestDocument);
        assertThat(ingestDocument.getFieldValue("docker_id", String.class), notNullValue());

        String base64 = ingestDocument.getFieldValue("docker_id", String.class);
        String hexa2 = Hex.bytesToHex(Base64.getDecoder().decode(base64));
        assertThat(hexa, equalTo(hexa2));
    }
}
