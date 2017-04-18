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

package org.elasticsearch.cluster.metadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Schema;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.FutureUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.threadpool.ThreadPool;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 *
 */
public class MetaDataDeleteIndexService extends AbstractComponent {

    private final ThreadPool threadPool;

    private final ClusterService clusterService;

    
    @Inject
    public MetaDataDeleteIndexService(Settings settings, ThreadPool threadPool, ClusterService clusterService) {
        super(settings);
        this.threadPool = threadPool;
        this.clusterService = clusterService;
        
    }

    public void deleteIndices(final Request request, final Listener userListener) {
        final Collection<String> indices = Arrays.asList(request.indices);
        clusterService.submitStateUpdateTask("delete-index " + indices, new ClusterStateUpdateTask(Priority.URGENT) {
            @Override
            public TimeValue timeout() {
                return request.masterTimeout;
            }

            @Override
            public boolean doPresistMetaData() {
                return true;
            }
            
            @Override
            public void onFailure(String source, Throwable t) {
                userListener.onFailure(t);
            }

            @Override
            public ClusterState execute(final ClusterState currentState) throws Exception {
                MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
                ClusterBlocks.Builder clusterBlocksBuilder = ClusterBlocks.builder().blocks(currentState.blocks());

                Multimap<IndexMetaData,String> unindexedTables = HashMultimap.create();
                for (final String index: indices) {
                    if (!currentState.metaData().hasConcreteIndex(index)) {
                        throw new IndexNotFoundException(index);
                    }

                    logger.debug("[{}] deleting index", index);

                    clusterBlocksBuilder.removeIndexBlocks(index);
                    metaDataBuilder.remove(index);
                    
                    final IndexMetaData indexMetaData = currentState.metaData().index(index);
                    // record keyspace.table having useless 2i 
                    for(ObjectCursor<MappingMetaData> type:indexMetaData.getMappings().values()) 
                        if (!MapperService.DEFAULT_MAPPING.equals(type.value.type()))
                            unindexedTables.put(indexMetaData, InternalCassandraClusterService.typeToCfName(type.value.type()));
                }
               
                MetaData newMetaData = metaDataBuilder.build();
                
                // remove keyspace.table still having ES indices from the unindexedTables
                for(ObjectCursor<IndexMetaData> index:newMetaData.indices().values()) {
                    for(ObjectCursor<MappingMetaData> type:index.value.getMappings().values()) 
                        unindexedTables.remove(index.value.keyspace(), type.value);
                }
                
                logger.debug("unindexed tables={}", unindexedTables);
                
                boolean clusterDropOnDelete = currentState.metaData().settings().getAsBoolean(InternalCassandraClusterService.SETTING_CLUSTER_DROP_ON_DELETE_INDEX, Boolean.getBoolean("es.drop_on_delete_index"));
                for(IndexMetaData imd : unindexedTables.keySet()) {
                    if (Schema.instance.getKeyspaceInstance(imd.keyspace()) != null) {
                        // keyspace still exists.
                        if (imd.getSettings().getAsBoolean(IndexMetaData.SETTING_DROP_ON_DELETE_INDEX, clusterDropOnDelete)) {
                            int tableCount = 0;
                            for(CFMetaData tableOrView : Schema.instance.getKeyspaceInstance(imd.keyspace()).getMetadata().tablesAndViews()) {
                                if (tableOrView.isCQLTable())
                                    tableCount++;
                            }
                            if (tableCount == unindexedTables.get(imd).size()) {
                                // drop keyspace instead of droping all tables.
                                MetaDataDeleteIndexService.this.clusterService.dropIndexKeyspace(imd.keyspace());
                            } else {
                                // drop tables
                                for(String table : unindexedTables.get(imd))
                                    MetaDataDeleteIndexService.this.clusterService.dropTable(imd.keyspace(), table);
                            }
                        } else {
                            // drop secondary indices
                            for(String table : unindexedTables.get(imd))
                                MetaDataDeleteIndexService.this.clusterService.dropSecondaryIndex(imd.keyspace(), table);
                        }
                    }
                }
                
                ClusterBlocks blocks = clusterBlocksBuilder.build();
                return ClusterState.builder(currentState).metaData(newMetaData).blocks(blocks).build();
            }

            @Override
            public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                userListener.onResponse(new Response(true));
            }
        });
    }

    class DeleteIndexListener implements Listener {

        private final AtomicBoolean notified = new AtomicBoolean();
        private final Listener listener;
        volatile ScheduledFuture<?> future;

        private DeleteIndexListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void onResponse(final Response response) {
            if (notified.compareAndSet(false, true)) {
                FutureUtils.cancel(future);
                listener.onResponse(response);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (notified.compareAndSet(false, true)) {
                FutureUtils.cancel(future);
                listener.onFailure(t);
            }
        }
    }

    public interface Listener {

        void onResponse(Response response);

        void onFailure(Throwable t);
    }

    public static class Request {

        final String[] indices;

        TimeValue timeout = TimeValue.timeValueSeconds(10);
        TimeValue masterTimeout = MasterNodeRequest.DEFAULT_MASTER_NODE_TIMEOUT;

        public Request(String[] indices) {
            this.indices = indices;
        }

        public Request timeout(TimeValue timeout) {
            this.timeout = timeout;
            return this;
        }

        public Request masterTimeout(TimeValue masterTimeout) {
            this.masterTimeout = masterTimeout;
            return this;
        }
    }

    public static class Response {
        private final boolean acknowledged;

        public Response(boolean acknowledged) {
            this.acknowledged = acknowledged;
        }

        public boolean acknowledged() {
            return acknowledged;
        }
    }
}
