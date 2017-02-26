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
package org.elassandra.gateway;


import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.ExtensionPoint;
import org.elasticsearch.gateway.Gateway;

public class CassandraGatewayModule extends AbstractModule {

    public static final String GATEWAY_TYPE_KEY = "gateway.type";

    private final ExtensionPoint.SelectedType<Gateway> gatewayTypes = new ExtensionPoint.SelectedType<>("gateway", Gateway.class);
    private final Settings settings;

    public CassandraGatewayModule(Settings settings) {
        this.settings = settings;
        registerGatewayType("default", Gateway.class);
    }

    /**
     * Adds a custom Discovery type.
     */
    public void registerGatewayType(String type, Class<? extends Gateway> clazz) {
        gatewayTypes.registerExtension(type, clazz);
    }

    @Override
    protected void configure() {
        //bind(MetaStateService.class).asEagerSingleton();
        //bind(DanglingIndicesState.class).asEagerSingleton();
        bind(CassandraGatewayService.class).asEagerSingleton();
        gatewayTypes.bindType(binder(), settings, GATEWAY_TYPE_KEY, "default");
        //bind(TransportNodesListGatewayMetaState.class).asEagerSingleton();
        //bind(GatewayMetaState.class).asEagerSingleton();
        //bind(TransportNodesListGatewayStartedShards.class).asEagerSingleton();
        //bind(LocalAllocateDangledIndices.class).asEagerSingleton();
    }
}
