/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.gateway.cassandra;

import org.elasticsearch.cluster.routing.allocation.allocator.ShardsAllocatorModule;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.PreProcessModule;
import org.elasticsearch.gateway.Gateway;
import org.elasticsearch.gateway.local.state.meta.LocalAllocateDangledIndices;
import org.elasticsearch.gateway.local.state.meta.LocalGatewayMetaState;
import org.elasticsearch.gateway.local.state.meta.TransportNodesListGatewayMetaState;
import org.elasticsearch.gateway.local.state.shards.LocalGatewayShardsState;
import org.elasticsearch.gateway.local.state.shards.TransportNodesListGatewayStartedShards;

/**
 *
 */
public class CassandraGatewayModule extends AbstractModule implements PreProcessModule {

    @Override
    protected void configure() {
        bind(Gateway.class).to(CassandraGateway.class).asEagerSingleton();
        //bind(LocalGatewayShardsState.class).asEagerSingleton();
        //bind(TransportNodesListGatewayMetaState.class).asEagerSingleton();
        //bind(LocalGatewayMetaState.class).asEagerSingleton();
        //bind(TransportNodesListGatewayStartedShards.class).asEagerSingleton();
        //bind(LocalAllocateDangledIndices.class).asEagerSingleton();
    }

    @Override
    public void processModule(Module module) {
        /*
        if (module instanceof ShardsAllocatorModule) {
            ((ShardsAllocatorModule) module).setGatewayAllocator(CassandraGatewayAllocator.class);
        }
        */
    }
}
