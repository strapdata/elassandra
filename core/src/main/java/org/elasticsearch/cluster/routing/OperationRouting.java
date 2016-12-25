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

package org.elasticsearch.cluster.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.allocation.decider.AwarenessAllocationDecider;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.math.MathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.shard.ShardNotFoundException;

/**
 *
 */
public class OperationRouting extends AbstractComponent {


    private final ClusterService clusterService;
    private final AwarenessAllocationDecider awarenessAllocationDecider;

    @Inject
    public OperationRouting(Settings settings, AwarenessAllocationDecider awarenessAllocationDecider, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
        this.awarenessAllocationDecider = awarenessAllocationDecider;
    }

    public ShardIterator shard(String index) {
        IndexService indexService = clusterService.indexService(index);
        ShardRouting shardRouting = indexService.shardSafe(0).routingEntry();
        return new PlainShardIterator(new ShardId(index, 0), Collections.singletonList(shardRouting));
    }
    
    public ShardIterator indexShards(ClusterState clusterState, String index, String type, String id, @Nullable String routing) {
        return shard(index);
        //return shards(clusterState, index, type, id, routing).shardsIt();
    }

    public ShardIterator deleteShards(ClusterState clusterState, String index, String type, String id, @Nullable String routing) {
        return shard(index);
        //return shards(clusterState, index, type, id, routing).shardsIt();
    }

    public ShardIterator getShards(ClusterState clusterState, String index, String type, String id, @Nullable String routing, @Nullable String preference) {
        return shard(index);
        //return preferenceActiveShardIterator(shards(clusterState, index, type, id, routing), clusterState.nodes().localNodeId(), clusterState.nodes(), preference);
    }
    
    
    public ShardIterator getShards(ClusterState clusterState, String index, int shardId, @Nullable String preference) {
        return shard(index);
        //return preferenceActiveShardIterator(shards(clusterState, index, shardId), clusterState.nodes().localNodeId(), clusterState.nodes(), preference);
    }

    public GroupShardsIterator broadcastDeleteShards(ClusterState clusterState, String index) throws RoutingValidationException, ConfigurationException, InstantiationException, IllegalAccessException {
         IndexRoutingTable routingTable = new IndexRoutingTable.Builder(index, this.clusterService, clusterState).build();
         if (routingTable == null) {
             throw new ShardNotFoundException(new ShardId(index, 0));
         }
        return routingTable.groupByShardsIt();
    }

    public int searchShardsCount(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing) {
        return searchShardsCount(clusterState, concreteIndices, routing, null, null);
    }
    
    
    public int searchShardsCount(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing, @Nullable String preference) {
        return searchShardsCount(clusterState, concreteIndices, routing, preference, null);
    }
    
    
    
    public int searchShardsCount(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing, @Nullable String preference, TransportAddress src) {
        final Set<IndexShardRoutingTable> shards = computeTargetedShards(clusterState, concreteIndices, routing, preference, src);
        return shards.size();
    }
    
    public GroupShardsIterator searchShards(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing) {
        return searchShards(clusterState, concreteIndices, routing, null, null);
    }
    
    public GroupShardsIterator searchShards(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing, @Nullable String preference) {
        return searchShards(clusterState, concreteIndices, routing, preference, null);
    }
    
    
    
    public GroupShardsIterator searchShards(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing, @Nullable String preference, TransportAddress src) {
        final Set<IndexShardRoutingTable> shards = computeTargetedShards(clusterState, concreteIndices, routing, preference, src);
        final Set<ShardIterator> set = new HashSet<>(shards.size());
        for (IndexShardRoutingTable shard : shards) {
            ShardIterator iterator = preferenceActiveShardIterator(shard, clusterState.nodes().localNodeId(), clusterState.nodes(), preference);
            // Modified here to check that iterator.size() > 0
            if ((iterator != null) && (iterator.size() > 0)) {
                set.add(iterator);
            }
        }
        return new GroupShardsIterator(new ArrayList<>(set));
    }
    

    private static final Map<String, Set<String>> EMPTY_ROUTING = Collections.emptyMap();

    public Set<IndexShardRoutingTable> computeTargetedShards(ClusterState clusterState, String[] concreteIndices, @Nullable Map<String, Set<String>> routing, @Nullable String preference, TransportAddress src) {
        routing = routing == null ? EMPTY_ROUTING : routing; // just use an empty map
        final Set<IndexShardRoutingTable> set = new HashSet<>();
        // we use set here and not list since we might get duplicates
        for (String index : concreteIndices) {
            final IndexRoutingTable indexRouting = new IndexRoutingTable.Builder(index, this.clusterService, clusterState, preference, src).build();
            if (indexRouting == null)
                throw new IndexNotFoundException(index);
            
            for (IndexShardRoutingTable indexShard : indexRouting) {
                set.add(indexShard);
            }
        }
        return set;
    }

    private ShardIterator preferenceActiveShardIterator(IndexShardRoutingTable indexShard, String localNodeId, DiscoveryNodes nodes, @Nullable String preference) {
        if (preference == null || preference.isEmpty()) {
            String[] awarenessAttributes = awarenessAllocationDecider.awarenessAttributes();
            if (awarenessAttributes.length == 0) {
                return indexShard.activeInitializingShardsRandomIt();
            } else {
                return indexShard.preferAttributesActiveInitializingShardsIt(awarenessAttributes, nodes);
            }
        }
        if (preference.charAt(0) == '_') {
            Preference preferenceType = Preference.parse(preference);
            if (preferenceType == Preference.SHARDS) {
                // starts with _shards, so execute on specific ones
                int index = preference.indexOf(';');

                String shards;
                if (index == -1) {
                    shards = preference.substring(Preference.SHARDS.type().length() + 1);
                } else {
                    shards = preference.substring(Preference.SHARDS.type().length() + 1, index);
                }
                String[] ids = Strings.splitStringByCommaToArray(shards);
                boolean found = false;
                for (String id : ids) {
                    if (Integer.parseInt(id) == indexShard.shardId().id()) {
                        found = true;
                        break;
                    }
                }
                /* Always found as shard 0 on elassandra.
                if (!found) {
                    return null;
                }
                */
    
                // no more preference
                if (index == -1 || index == preference.length() - 1) {
                    String[] awarenessAttributes = awarenessAllocationDecider.awarenessAttributes();
                    if (awarenessAttributes.length == 0) {
                        return indexShard.activeInitializingShardsRandomIt();
                    } else {
                        return indexShard.preferAttributesActiveInitializingShardsIt(awarenessAttributes, nodes);
                    }
                } else {
                    // update the preference and continue
                    preference = preference.substring(index + 1);
                }
            }
            preferenceType = Preference.parse(preference);
            switch (preferenceType) {
                case PREFER_NODE:
                    return indexShard.preferNodeActiveInitializingShardsIt(preference.substring(Preference.PREFER_NODE.type().length() + 1));
                case LOCAL:
                    return indexShard.preferNodeActiveInitializingShardsIt(localNodeId);
                case PRIMARY:
                    return indexShard.primaryActiveInitializingShardIt();
                case REPLICA:
                    return indexShard.replicaActiveInitializingShardIt();
                case PRIMARY_FIRST:
                    return indexShard.primaryFirstActiveInitializingShardsIt();
                case REPLICA_FIRST:
                    return indexShard.replicaFirstActiveInitializingShardsIt();
                case ONLY_LOCAL:
                    return indexShard.onlyNodeActiveInitializingShardsIt(localNodeId);
                case ONLY_NODE:
                    String nodeId = preference.substring(Preference.ONLY_NODE.type().length() + 1);
                    ensureNodeIdExists(nodes, nodeId);
                    return indexShard.onlyNodeActiveInitializingShardsIt(nodeId);
                case ONLY_NODES:
                    String nodeAttribute = preference.substring(Preference.ONLY_NODES.type().length() + 1);
                    return indexShard.onlyNodeSelectorActiveInitializingShardsIt(nodeAttribute, nodes);
                default:
                    throw new IllegalArgumentException("unknown preference [" + preferenceType + "]");
            }
        }
        // if not, then use it as the index
        String[] awarenessAttributes = awarenessAllocationDecider.awarenessAttributes();
        if (awarenessAttributes.length == 0) {
            return indexShard.activeInitializingShardsIt(DjbHashFunction.DJB_HASH(preference));
        } else {
            return indexShard.preferAttributesActiveInitializingShardsIt(awarenessAttributes, nodes, DjbHashFunction.DJB_HASH(preference));
        }
    }

    public IndexMetaData indexMetaData(ClusterState clusterState, String index) {
        IndexMetaData indexMetaData = clusterState.metaData().index(index);
        if (indexMetaData == null) {
            throw new IndexNotFoundException(index);
        }
        return indexMetaData;
    }

    

    // either routing is set, or type/id are set

    protected IndexShardRoutingTable shards(ClusterState clusterState, String index, String type, String id, String routing) throws RoutingValidationException, ConfigurationException, InstantiationException, IllegalAccessException {
        return shards(clusterState, index, 0);
    }

    
    protected IndexShardRoutingTable shards(ClusterState clusterState, String index, int shardId) throws RoutingValidationException, ConfigurationException, InstantiationException, IllegalAccessException {
        IndexShardRoutingTable indexShard = new IndexRoutingTable.Builder(index, this.clusterService, clusterState).build().shard(shardId);
        if (indexShard == null) {
            throw new ShardNotFoundException(new ShardId(index, shardId));
        }
        return indexShard;
    }
    
    
    @SuppressForbidden(reason = "Math#abs is trappy")
    public int shardId(ClusterState clusterState, String index, String type, String id, @Nullable String routing) {
        final IndexMetaData indexMetaData = indexMetaData(clusterState, index);
        final Version createdVersion = indexMetaData.getCreationVersion();
        final HashFunction hashFunction = indexMetaData.getRoutingHashFunction();
        final boolean useType = indexMetaData.getRoutingUseType();

        final int hash;
        if (routing == null) {
            if (!useType) {
                hash = hash(hashFunction, id);
            } else {
                hash = hash(hashFunction, type, id);
            }
        } else {
            hash = hash(hashFunction, routing);
        }
        if (createdVersion.onOrAfter(Version.V_2_0_0_beta1)) {
            return MathUtils.mod(hash, indexMetaData.getNumberOfShards());
        } else {
            return Math.abs(hash % indexMetaData.getNumberOfShards());
        }
    }

    protected int hash(HashFunction hashFunction, String routing) {
        return hashFunction.hash(routing);
    }

    @Deprecated
    protected int hash(HashFunction hashFunction, String type, String id) {
        if (type == null || "_all".equals(type)) {
            throw new IllegalArgumentException("Can't route an operation with no type and having type part of the routing (for backward comp)");
        }
        return hashFunction.hash(type, id);
    }

    private void ensureNodeIdExists(DiscoveryNodes nodes, String nodeId) {
        if (!nodes.dataNodes().keys().contains(nodeId)) {
            throw new IllegalArgumentException("No data node with id[" + nodeId + "] found");
        }
    }
    
}
