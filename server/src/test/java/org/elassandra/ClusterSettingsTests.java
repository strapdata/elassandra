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

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertThrows;

/**
 * Test default cluster settings.
 * @author vroyer
 */
//gradle :server:test -Dtests.seed=65E2CF27F286CC89 -Dtests.class=org.elassandra.ClusterSettingsTests -Dtests.security.manager=false -Dtests.locale=en-PH -Dtests.timezone=America/Coral_Harbour
public class ClusterSettingsTests extends ESSingleNodeTestCase {
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertAcked(client().admin().cluster().prepareUpdateSettings().setPersistentSettings(
                Settings.builder()
                .put(ClusterService.SETTING_CLUSTER_SEARCH_STRATEGY_CLASS, "foo")
        ).get());
    }
    
    @Override
    public void tearDown() throws Exception {
        assertAcked(client().admin().cluster().prepareUpdateSettings().setPersistentSettings(
                Settings.builder()
                .put(ClusterService.SETTING_CLUSTER_SEARCH_STRATEGY_CLASS, (String)null)
        ).get());
        super.tearDown();
    }
    
    @Test
    public void testIndexBadSearchStrategy() {
        CreateIndexRequestBuilder createIndexRequestBuilder = client().admin().indices().prepareCreate("test1");
        assertThrows(createIndexRequestBuilder, org.apache.cassandra.exceptions.ConfigurationException.class);
    }
    
}

