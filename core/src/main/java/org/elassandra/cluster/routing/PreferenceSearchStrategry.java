package org.elassandra.cluster.routing;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.transport.TransportAddress;

/**
 * Search strategy 
 * @author vroyer
 *
 */
public class PreferenceSearchStrategry extends CachedRandomSearchStrategy {
    
    public PreferenceSearchStrategry() {
        
    }
    
    public class SearchKeyRouter extends CachedRandomRouter {
        public SearchKeyRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
            super(index, ksName, shardStates, clusterState);
        }
        
        public Key getKey(TransportAddress src, @Nullable String searchKey, @Nullable String preference) {
            return new Key(null, ((preference!=null) && (preference.charAt(0)!='_')) ? preference : null);
        }
    }
    
    @Override
    public Router newRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
        return new SearchKeyRouter(index, ksName, shardStates, clusterState);
    }
}
