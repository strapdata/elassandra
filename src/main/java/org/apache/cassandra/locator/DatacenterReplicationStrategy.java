package org.apache.cassandra.locator;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.EndpointState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.gms.IEndpointStateChangeSubscriber;
import org.apache.cassandra.gms.VersionedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Replicates data on all nodes of datacenters having RF > 0.
 * Extends NetworkTopoloyStrategy to remains compatible with standard apache cassandra.
 * 
 * @author vroyer
 **/
public class DatacenterReplicationStrategy extends NetworkTopologyStrategy implements IEndpointStateChangeSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(DatacenterReplicationStrategy.class);

    private final IEndpointSnitch snitch;
    private final Set<String> datacenters = new HashSet<String>();
    private final HashMultimap<String, InetAddress> datacenterEndpoints = HashMultimap.create();

    public DatacenterReplicationStrategy(String keyspaceName, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions) {
        super(keyspaceName, tokenMetadata, snitch, configOptions);
        this.snitch = snitch;
        
        if (configOptions != null)
        {
            for (Entry<String, String> entry : configOptions.entrySet())
            {
                String dc = entry.getKey();
                Integer replicas = Integer.valueOf(entry.getValue());
                if (replicas > 0) 
                    datacenters.add(dc);
            }
        }
        
        datacenterEndpoints.put(this.snitch.getDatacenter(DatabaseDescriptor.getListenAddress()), DatabaseDescriptor.getListenAddress());

        Multimap<String, InetAddress> allDatacenter = tokenMetadata.cachedOnlyTokenMap().getTopology().getDatacenterEndpoints();
        for (String dc : datacenters) {
            datacenterEndpoints.putAll(dc, allDatacenter.get(dc));
        }

        Gossiper.instance.register(this);
        logger.debug("Initial DatacenterReplicationStrategy for keyspace={} datacenters={} endpoints={}", keyspaceName, datacenters, datacenterEndpoints);
    }

    @Override
    public List<InetAddress> calculateNaturalEndpoints(Token token, TokenMetadata tokenMetadata) {
        return new Vector<InetAddress>(datacenterEndpoints.values());
    }

    @Override
    public int getReplicationFactor() {
        return datacenterEndpoints.values().size();
    }

    public int getReplicationFactor(String dc) {
        Collection<InetAddress> replicas = datacenterEndpoints.get(dc);
        return replicas == null ? 0 : replicas.size();
    }

    public Set<String> getDatacenters() {
        return datacenterEndpoints.keySet();
    }

    public Collection<InetAddress> getEndpoints() {
        return this.datacenterEndpoints.values();
    }
    
    public Collection<InetAddress> getAliveEndpoints() {
        return Sets.intersection(Gossiper.instance.getLiveTokenOwners(), Sets.newHashSet(this.datacenterEndpoints.values()) );
    }

    public void addEndpoint(InetAddress addr) {
        String dc = this.snitch.getDatacenter(addr);
        if (datacenters.contains(dc) && !datacenterEndpoints.containsValue(addr)) {
            this.datacenterEndpoints.put(dc, addr);
            logger.debug("DatacenterReplicationStrategy endpoint added keyspace={} datacenters={} endpoints={}", keyspaceName, datacenters, datacenterEndpoints);
        }
    }

    public void removeEndpoint(InetAddress addr) {
        String dc = this.snitch.getDatacenter(addr);
        if (datacenters.contains(dc)) {
            this.datacenterEndpoints.remove(dc, addr);
            logger.debug("DatacenterReplicationStrategy endpoint removed keyspace={} datacenters={} endpoints={}", 
                    keyspaceName, datacenters, datacenterEndpoints);
        }
    }

    @Override
    public void beforeChange(InetAddress arg0, EndpointState arg1, ApplicationState arg2, VersionedValue arg3) {
    }

    @Override
    public void onChange(InetAddress arg0, ApplicationState arg1, VersionedValue arg2) {
    }

    @Override
    public void onAlive(InetAddress arg0, EndpointState arg1) {
        if (logger.isDebugEnabled())
            logger.debug("onAlive addr=" + arg0 + " EndpointState=" + arg1);
        addEndpoint(arg0);
    }
    
    @Override
    public void onDead(InetAddress arg0, EndpointState arg1) {
        if (logger.isDebugEnabled())
            logger.debug("onDead addr=" + arg0 + " EndpointState=" + arg1);
    }

    @Override
    public void onJoin(InetAddress arg0, EndpointState arg1) {
        if (logger.isDebugEnabled())
            logger.debug("Jonning addr=" + arg0 + " EndpointState=" + arg1);
        addEndpoint(arg0);
    }

    @Override
    public void onRemove(InetAddress arg0) {
        if (logger.isDebugEnabled())
            logger.debug("Removing addr=" + arg0);
        removeEndpoint(arg0);
    }

    @Override
    public void onRestart(InetAddress arg0, EndpointState arg1) {
        if (logger.isDebugEnabled())
            logger.debug("onRestart addr=" + arg0);
        addEndpoint(arg0);
    }
}
