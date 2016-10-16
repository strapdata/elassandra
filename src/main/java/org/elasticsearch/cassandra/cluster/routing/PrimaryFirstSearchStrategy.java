/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.cassandra.cluster.routing;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.cassandra.dht.Murmur3Partitioner.LongToken;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.transport.TransportAddress;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * return primary ranges of all nodes (and some replica for unreachable nodes).
 * 
 * @author vroyer
 *
 */
public class PrimaryFirstSearchStrategy extends AbstractSearchStrategy {

    public Router newRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
    	return new PrimaryFirstRouter(index, ksName, shardStates, clusterState);
    }
    
    public class PrimaryFirstRouter extends Router {
    	
    	public PrimaryFirstRouter(final String index, final String ksName, final Map<UUID, ShardRoutingState> shardStates, final ClusterState clusterState) {
    		super(index, ksName, shardStates, clusterState);
    		
    		Map<UUID, InetAddress> hostIdToEndpoints = StorageService.instance.getUuidToEndpoint();
    		
    		for(ObjectCursor<DiscoveryNode> entry : clusterState.nodes().getDataNodes().values()) {
    			DiscoveryNode node = entry.value;
    			InetAddress endpoint  = hostIdToEndpoints.get(node.uuid());
    			assert endpoint != null;
    			Collection<Range<Token>> primaryRanges = StorageService.instance.getPrimaryRangeForEndpointWithinDC(ksName, endpoint);
    			Range<Token> wrappedRange = null;		
    			for(Range<Token> range : primaryRanges) {
	    			if (range.isWrapAround()) {
	    				wrappedRange = range;
	    				break;
	    			}
    			}
    			if (wrappedRange != null) {
    				primaryRanges.remove(wrappedRange);
                	primaryRanges.add( new Range<Token>(new LongToken((Long) wrappedRange.left.getTokenValue()), new LongToken(Long.MAX_VALUE)) );
                	primaryRanges.add( new Range<Token>(new LongToken(Long.MIN_VALUE), new LongToken((Long) wrappedRange.right.getTokenValue())));
                }
    			if (ShardRoutingState.STARTED.equals(shardStates.get(node.uuid()))) {
    				// unset all primary range bit on replica nodes.
    				for(Range<Token> range : primaryRanges) {
    					int idx = this.tokens.indexOf(range);
    					for(InetAddress replica : this.rangeToEndpointsMap.get(range)) {
    						UUID uuid = StorageService.instance.getHostId(replica);
    						assert uuid != null;
    						if (!node.uuid().equals(uuid)) {
    							DiscoveryNode n = clusterState.nodes().get( uuid.toString() );
    							if (this.greenShards.get(n) != null)
    								this.greenShards.get(n).set(idx, false);
    						}
    					}
    				}
    			} 
    		}
    	}

    }

    
}
