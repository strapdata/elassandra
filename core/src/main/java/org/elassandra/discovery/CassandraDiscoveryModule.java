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
package org.elassandra.discovery;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoveryService;

/**
 * A module for loading classes for node discovery.
 */
public class CassandraDiscoveryModule extends AbstractModule {

    private final Settings settings;

    public static final String DISCOVERY_TYPE_KEY = "discovery.type";

    public CassandraDiscoveryModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(Discovery.class).to(CassandraDiscovery.class).asEagerSingleton();
        bind(DiscoveryService.class).asEagerSingleton();
    }
    
}