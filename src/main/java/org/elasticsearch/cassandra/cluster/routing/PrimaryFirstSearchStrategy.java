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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Use all local ranges and pickup a random endpoint for remote ranges (may be
 * unbalanced).
 * 
 * @author vroyer
 *
 */
public class PrimaryFirstSearchStrategy extends AbstractSearchStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSearchStrategy.class);

    @Override
    public AbstractSearchStrategy.Result topology(String ksName, Collection<InetAddress> startedShards) {

        Predicate<InetAddress> isLocalDC = new Predicate<InetAddress>() {
            String localDC = DatabaseDescriptor.getEndpointSnitch().getDatacenter(FBUtilities.getBroadcastAddress());
           
            public boolean apply(InetAddress address) {
                String remoteDC = DatabaseDescriptor.getEndpointSnitch().getDatacenter(address);
                return remoteDC.equals(localDC);
            }
        };
        
        Set<InetAddress> localLiveNodes = Sets.newHashSet(Collections2.filter(Gossiper.instance.getLiveTokenOwners(), isLocalDC));
        Set<InetAddress> localUnreachableNodes = Sets.newHashSet(Collections2.filter(Gossiper.instance.getUnreachableTokenOwners(), isLocalDC));
        Map<Range<Token>, List<InetAddress>> allRanges = StorageService.instance.getRangeToAddressMapInLocalDC(ksName);

        Multimap<InetAddress, Range<Token>> topo = ArrayListMultimap.create();
        Set<Range<Token>> orphanRanges = new HashSet<Range<Token>>();
        boolean consistent = true;

        // get live primary token ranges
        for (InetAddress node : localLiveNodes) {
            if (startedShards.contains(node)) {
                topo.putAll(node, StorageService.instance.getPrimaryRangeForEndpointWithinDC(ksName, node));
            } else {
                localUnreachableNodes.add(node);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("keyspace={} live nodes={}, primary ranges map = {}",ksName, localLiveNodes, topo);
        }

        // pickup random live replica for primary range owned by unreachable or not started nodes.
        if (localUnreachableNodes.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("unreachableNodes = {} ", localUnreachableNodes);
            }
            Random rnd = new Random();
            for (InetAddress node : localUnreachableNodes) {
                for (Range<Token> orphanRange : StorageService.instance.getPrimaryRangeForEndpointWithinDC(ksName, node)) {
                    List<InetAddress> replicaEndPoints = allRanges.get(orphanRange);
                    replicaEndPoints.removeAll(localUnreachableNodes);
                    if (replicaEndPoints.size() == 0) {
                        consistent = false;
                        orphanRanges.add(orphanRange);
                        logger.warn("Inconsistent search for keyspace {}, no alive node for range {}", ksName, orphanRange);
                    } else {
                        InetAddress liveReplica = replicaEndPoints.get(rnd.nextInt(replicaEndPoints.size()));
                        topo.put(liveReplica, orphanRange);
                        logger.debug("orphanRanges {} available on = {} ", orphanRanges, liveReplica);
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("topology for keyspace {} = {}, consistent={} unreachableNodes={} orphanRanges={}", ksName, topo.asMap(), consistent, localUnreachableNodes, orphanRanges);
        }
        return new AbstractSearchStrategy.Result(topo.asMap(), orphanRanges, localUnreachableNodes, allRanges);
    }
}
