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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Use all local ranges and pickup a random endpoint for remote ranges (may be
 * unbalanced).
 * 
 * @author vroyer
 *
 */
public class LocalFirstSearchStrategy extends AbstractSearchStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSearchStrategy.class);

    @Override
    public AbstractSearchStrategy.Result topology(String ksName, Collection<InetAddress> staredShard) {
        Keyspace.open(ksName);

        Set<InetAddress> liveNodes = Gossiper.instance.getLiveTokenOwners();
        InetAddress localAddress = FBUtilities.getBroadcastAddress();
        Map<Range<Token>, List<InetAddress>> allRanges = StorageService.instance.getRangeToAddressMapInLocalDC(ksName);

        Multimap<InetAddress, Range<Token>> topo = ArrayListMultimap.create();
        boolean consistent = true;

        Collection<Range<Token>> localRanges = new ArrayList<Range<Token>>();
        for (Entry<Range<Token>, List<InetAddress>> entry : allRanges.entrySet()) {
            List<InetAddress> addrList = entry.getValue();
            if (addrList.contains(localAddress)) {
                localRanges.add(entry.getKey());
                entry.getValue().remove(localAddress);
            }
        }
        logger.debug("{} localRanges for keyspace {} on address {} = {}", localRanges.size(), ksName, FBUtilities.getBroadcastAddress(), localRanges);

        topo.putAll(localAddress, localRanges);

        // remove localRanges from allRanges.
        for (Range<Token> range : localRanges) {
            allRanges.remove(range);
        }

        // remove dead nodes form allRanges values.
        for (Entry<Range<Token>, List<InetAddress>> entry : allRanges.entrySet()) {
            List<InetAddress> addrList = entry.getValue();
            for (Iterator<InetAddress> i = addrList.iterator(); i.hasNext();) {
                InetAddress addr = i.next();
                if (!liveNodes.contains(addr)) {
                    i.remove();
                }
            }
            if (addrList.size() == 0) {
                consistent = false;
                logger.warn("Inconsistent search for keyspace {}, no alive node for range {}", ksName, entry.getKey());
            }
        }

        // pickup a random address for non-local ranges
        Random rnd = new Random();
        for (Entry<Range<Token>, List<InetAddress>> entry : allRanges.entrySet()) {
            List<InetAddress> addrList = entry.getValue();
            InetAddress addr = addrList.get(rnd.nextInt(addrList.size()));
            topo.put(addr, entry.getKey());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("topology for keyspace {} = {}", ksName, topo.asMap());
        }
        return null;
        // return new AbstractSearchStrategy.Result(topo.asMap(), consistent,
        // Gossiper.instance.getUnreachableTokenOwners().size());
    }
}
