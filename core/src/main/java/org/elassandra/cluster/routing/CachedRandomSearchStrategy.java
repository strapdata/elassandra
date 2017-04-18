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
package org.elassandra.cluster.routing;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.transport.TransportAddress;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * For each newRoute(), returns all local ranges and randomly pickup ranges from available nodes (may be unbalanced).
 * 
 * @author vroyer
 *
 */
public abstract class CachedRandomSearchStrategy extends RandomSearchStrategy {
    String spec = "maximumSize=8192,expireAfterAccess=10m";
    
    public CachedRandomSearchStrategy() {
                    
    }
    
    public class CachedRandomRouter extends RandomRouter {
        Cache<Key, Route> lruCache;
        
        public class Key implements Callable<Route> {
            final TransportAddress src;
            final String preference;
            
            public Key(TransportAddress src, @Nullable String preference) {
                this.src = src;
                this.preference = preference;
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Key that = (Key) o;

                if (preference != null ? !preference.equals(that.preference) : that.preference != null) return false;
                if (src != null ? !src.sameHost(that.src) : that.src != null) return false;
                return true;
            }
            
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((src == null) ? 0 : src.hashCode());
                result = prime * result + ((preference == null) ? 0 : preference.hashCode());
                return result;
            }

            @Override
            public Route call() throws Exception {
                return newRoute(preference, src);
            }
        }
        
        public CachedRandomRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
            super(index, ksName, shardStates, clusterState);
            lruCache = CacheBuilder.from(CachedRandomSearchStrategy.this.spec).build();
        }
        
        @Override
        public Route newRoute(@Nullable String preference, TransportAddress src) {
            try {
                Key key = newKey(src, preference);
                return lruCache.get(key, key);
            } catch(ExecutionException e) {
                logger.warn("Unexpeceted error", e);
            }
            return super.newRoute(preference, src);
        }
    
        public Key newKey(TransportAddress src, @Nullable String preference) {
            return new Key(src, preference);
        }
        
    }
    
    

    @Override
    public Router newRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
        return new CachedRandomRouter(index, ksName, shardStates, clusterState);
    }
    
    
}
