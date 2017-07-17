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
package org.elassandra.indices;

import org.apache.cassandra.utils.Pair;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterStateApplier;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;

/**
 * Post applied cluster state listener.
 */
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Post-applied ClusterState listener to manage
 * - Start INITIALIZING local shards
 * - Cassandra 2i creation when associated local shard is started (on coordinator node only).
 * @author vroyer
 *
 */
public class CassandraSecondaryIndicesApplier extends AbstractComponent implements ClusterStateApplier {
    
    private final ClusterService clusterService;
    
    private final CopyOnWriteArraySet<Pair<String,MappingMetaData>> updatedMapping = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<String> initilizingShards = new CopyOnWriteArraySet<>();
    
    public CassandraSecondaryIndicesApplier(Settings settings, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
    }
    
    // called only by the coordinator of a mapping change on pre-applied phase
    public void updateMapping(String index, MappingMetaData mapping) {
        updatedMapping.add(Pair.create(index, mapping));
    }
    
    public void recoverShard(String index) {
        initilizingShards.add(index);
    }

    // called on post-applied phase (when shards are started on all nodes)
    @Override
    public void applyClusterState(ClusterChangedEvent event) {
        /*
        // recover initializing shards
        DiscoveryNode localNode = event.state().nodes().getLocalNode();
        RoutingTable routingTable = event.state().routingTable();
        for(String index : initilizingShards) {
            IndexMetaData indexMetaData = event.state().metaData().index(index);
            IndexService indexService = this.clusterService.getIndicesService().indexService(indexMetaData.getIndex());
            IndexShard indexShard = indexService.getShard(0);
            ShardRouting shardRouting = indexShard.routingEntry();
            if (indexMetaData.getState() == IndexMetaData.State.OPEN && shardRouting != null) {
                if (logger.isDebugEnabled())
                    logger.debug("[{}][{}] state=[{}], recovering", shardRouting.index(), shardRouting.shardId().getId(), shardRouting.state());
                
                // try to recover if index was existing but has no shards.
                
                indexShard.recoverFromStore();
            }
        }
        if (initilizingShards.size() > 0)
            initilizingShards.clear();
        */
        // create cassandra 2i on coordinator node only.
        for(Pair<String,MappingMetaData> mapping : updatedMapping) {
            String index = mapping.left;
            IndexMetaData indexMetaData = event.state().metaData().index(index);
            if (indexMetaData != null) {
                try {
                    String clazz = indexMetaData.getSettings().get(IndexMetaData.SETTING_SECONDARY_INDEX_CLASS, event.state().metaData().settings().get(ClusterService.SETTING_CLUSTER_SECONDARY_INDEX_CLASS, ClusterService.defaultSecondaryIndexClass.getName()));
                    logger.debug("Creating secondary indices for table={}.{} with class={}", indexMetaData.keyspace(), mapping.right.type(),clazz);
                    this.clusterService.createSecondaryIndex(indexMetaData.keyspace(), mapping.right, clazz);
                } catch (IOException e) {
                    logger.error("Failed to create secondary indices for table={}.{}", e, indexMetaData.keyspace(), mapping.right.type());
                }
            } else {
                logger.warn("Index [{}] not found in new state metadata version={}", index, event.state().metaData().version());
            }
        }
        if (updatedMapping.size() > 0)
            updatedMapping.clear();
    }

    
}
