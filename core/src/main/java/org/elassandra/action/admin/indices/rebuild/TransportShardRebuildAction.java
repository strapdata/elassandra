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

package org.elassandra.action.admin.indices.rebuild;

import com.carrotsearch.hppc.cursors.ObjectCursor;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.support.replication.TransportReplicationAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TransportShardRebuildAction extends TransportReplicationAction<ShardRebuildRequest, ShardRebuildRequest, ReplicationResponse> {

    public static final String NAME = RebuildAction.NAME + "[s]";

    @Inject
    public TransportShardRebuildAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                       IndicesService indicesService, ThreadPool threadPool,
                                       ActionFilters actionFilters,
                                       IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, NAME, transportService, clusterService, indicesService, threadPool,
                actionFilters, indexNameExpressionResolver, ShardRebuildRequest::new, ShardRebuildRequest::new, ThreadPool.Names.FLUSH);
    }

    @Override
    protected ReplicationResponse newResponseInstance() {
        return new ReplicationResponse();
    }

    @Override
    protected PrimaryResult shardOperationOnPrimary(ShardRebuildRequest shardRequest, IndexShard primary) throws IOException {
        IndexService indexService = indicesService.indexServiceSafe(shardRequest.shardId().getIndex());
        List<String> tables = new ArrayList<String>();
        List<String> indexes = new ArrayList<String>();
        MetaData metaData = clusterService.state().metaData();
        IndexMetaData indexMetaData = metaData.index(shardRequest.shardId().getIndex());
        String secondaryIndexClass = indexMetaData.getSettings().get(IndexMetaData.SETTING_SECONDARY_INDEX_CLASS, 
                metaData.settings().get(org.elassandra.cluster.service.ClusterService.SETTING_CLUSTER_SECONDARY_INDEX_CLASS, org.elassandra.cluster.service.ClusterService.defaultSecondaryIndexClass.getName()));
        for(ObjectCursor<MappingMetaData> it : indexMetaData.getMappings().values()) {
            MappingMetaData mapping = it.value;
            String table = org.elassandra.cluster.service.ClusterService.typeToCfName(mapping.type());
            tables.add(table);
            CFMetaData cfMetadata = org.elassandra.cluster.service.ClusterService.getCFMetaData(indexService.keyspace(), table);
            for(IndexMetadata index : cfMetadata.getIndexes()) {
                if (index.isCustom() && secondaryIndexClass.equals(index.options.get("class_name"))) {
                    indexes.add(index.name);
                    break;
                }
            }
        }
        
        // Cassandra flush and rebuild_index for all mapped tables.
        StorageService.instance.forceKeyspaceFlush(indexService.keyspace(), tables.toArray(new String[tables.size()]));
        for(int i=0; i < tables.size(); i++)
            StorageService.instance.rebuildSecondaryIndex(shardRequest.getRequest().numThreads(), indexService.keyspace(), tables.get(i), indexes.get(i));
        
        logger.trace("index=[{}] rebuild request executed on keyspace=[{}] tables={} with numThreads={}", shardRequest.shardId().getIndex(), indexService.mapperService().keyspace(), tables, shardRequest.getRequest().numThreads());
        return new PrimaryResult(shardRequest, new ReplicationResponse());
    }

    @Override
    protected ReplicaResult shardOperationOnReplica(ShardRebuildRequest request, IndexShard replica) {
        return new ReplicaResult();
    }

    @Override
    protected boolean shouldExecuteReplication(IndexMetaData indexMetaData) {
        return false;
    }
}
