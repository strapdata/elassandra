package org.elasticsearch.cassandra.index;

import java.util.Set;

import org.apache.cassandra.db.index.PerRowSecondaryIndex;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import com.google.common.collect.Sets;

public abstract class BaseElasticSecondaryIndex extends PerRowSecondaryIndex implements ClusterStateListener {
    static final ESLogger logger = Loggers.getLogger(BaseElasticSecondaryIndex.class);

    public static Set<BaseElasticSecondaryIndex> elasticSecondayIndices = Sets.newConcurrentHashSet();
    
    public abstract void initMapping();
}
