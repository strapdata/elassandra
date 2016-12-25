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

package org.elasticsearch.cluster;

import org.elasticsearch.action.admin.indices.create.CreateIndexClusterStateUpdateRequest;
import org.elasticsearch.cluster.metadata.IndexTemplateFilter;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.routing.allocation.decider.AllocationDecider;
import org.elasticsearch.cluster.routing.allocation.decider.EnableAllocationDecider;
import org.elasticsearch.cluster.settings.ClusterDynamicSettings;
import org.elasticsearch.cluster.settings.DynamicSettings;
import org.elasticsearch.cluster.settings.Validator;
import org.elasticsearch.common.inject.ModuleTestCase;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.settings.IndexDynamicSettings;

import com.google.common.base.Predicate;

public class ClusterModuleTests extends ModuleTestCase {

    public static class FakeAllocationDecider extends AllocationDecider {
        protected FakeAllocationDecider(Settings settings) {
            super(settings);
        }
    }

    
    static class FakeIndexTemplateFilter implements IndexTemplateFilter {
        @Override
        public boolean apply(CreateIndexClusterStateUpdateRequest request, IndexTemplateMetaData template) {
            return false;
        }
    }

    public void testRegisterClusterDynamicSettingDuplicate() {
        ClusterModule module = new ClusterModule(Settings.EMPTY);
        try {
            module.registerClusterDynamicSetting(EnableAllocationDecider.CLUSTER_ROUTING_ALLOCATION_ENABLE, Validator.EMPTY);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Cannot register setting [" + EnableAllocationDecider.CLUSTER_ROUTING_ALLOCATION_ENABLE + "] twice");
        }
    }

    public void testRegisterClusterDynamicSetting() {
        ClusterModule module = new ClusterModule(Settings.EMPTY);
        module.registerClusterDynamicSetting("foo.bar", Validator.EMPTY);
        assertInstanceBindingWithAnnotation(module, DynamicSettings.class, new Predicate<DynamicSettings>() {
            @Override
            public boolean apply(DynamicSettings dynamicSettings) {
                return dynamicSettings.hasDynamicSetting("foo.bar");
            }
        }, ClusterDynamicSettings.class);
    }

    public void testRegisterIndexDynamicSettingDuplicate() {
        ClusterModule module = new ClusterModule(Settings.EMPTY);
        try {
            module.registerIndexDynamicSetting(EnableAllocationDecider.INDEX_ROUTING_ALLOCATION_ENABLE, Validator.EMPTY);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Cannot register setting [" + EnableAllocationDecider.INDEX_ROUTING_ALLOCATION_ENABLE + "] twice");
        }
    }

    public void testRegisterIndexDynamicSetting() {
        ClusterModule module = new ClusterModule(Settings.EMPTY);
        module.registerIndexDynamicSetting("foo.bar", Validator.EMPTY);
        assertInstanceBindingWithAnnotation(module, DynamicSettings.class, new Predicate<DynamicSettings>() {
            @Override
            public boolean apply(DynamicSettings dynamicSettings) {
                return dynamicSettings.hasDynamicSetting("foo.bar");
            }
        }, IndexDynamicSettings.class);
    }

    /*
    public void testUnknownShardsAllocator() {
        Settings settings = Settings.builder().put(ClusterModule.SHARDS_ALLOCATOR_TYPE_KEY, "dne").build();
        ClusterModule module = new ClusterModule(settings);
        assertBindingFailure(module, "Unknown [shards_allocator]");
    }

    public void testEvenShardsAllocatorBackcompat() {
        Settings settings = Settings.builder()
            .put(ClusterModule.SHARDS_ALLOCATOR_TYPE_KEY, ClusterModule.EVEN_SHARD_COUNT_ALLOCATOR).build();
        ClusterModule module = new ClusterModule(settings);
        assertBinding(module, ShardsAllocator.class, BalancedShardsAllocator.class);
    }
    */
    
    public void testRegisterIndexTemplateFilterDuplicate() {
        ClusterModule module = new ClusterModule(Settings.EMPTY);
        try {
            module.registerIndexTemplateFilter(FakeIndexTemplateFilter.class);
            module.registerIndexTemplateFilter(FakeIndexTemplateFilter.class);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Can't register the same [index_template_filter] more than once for [" + FakeIndexTemplateFilter.class.getName() + "]");
        }
    }

    public void testRegisterIndexTemplateFilter() {
        ClusterModule module = new ClusterModule(Settings.EMPTY);
        module.registerIndexTemplateFilter(FakeIndexTemplateFilter.class);
        assertSetMultiBinding(module, IndexTemplateFilter.class, FakeIndexTemplateFilter.class);
    }
}
