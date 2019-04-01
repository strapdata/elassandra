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

package org.elassandra.painless;


import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalTo;

public class ScriptTests extends ESSingleNodeTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return pluginList(PainlessPlugin.class);
    }

    @Test
    public void testUpdate() throws Exception {
        createIndex("test1");
        ensureGreen("test1");

        assertThat(client().prepareIndex("test1", "mytype", "1")
                .setSource("{\"projects\": [ \"bar1\", \"bar2\" ]}", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));

        UpdateRequest udpate = new UpdateRequest()
                .index("test1").type("mytype").id("1")
                .script(new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG,
                        "if (!ctx._source.projects.contains(params.project)) { ctx._source.projects.add(params.project); }",
                       Collections.singletonMap("project", "bar3")));

        BulkItemResponse[] responses = client().prepareBulk().add(udpate).get().getItems();
        assertNull(responses[0].getFailureMessage());
        assertThat(responses[0].getResponse().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertTrue(client().prepareGet("test1", "mytype", "1").get().getSourceAsString().contains("bar3"));
    }
}
