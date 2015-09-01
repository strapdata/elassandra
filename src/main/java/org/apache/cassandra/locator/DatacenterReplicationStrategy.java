package org.apache.cassandra.locator;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Replicates data on all nodes in specified datacenters.
 * 
 * @author vroyer
 *
 */
public class DatacenterReplicationStrategy extends AbstractReplicationStrategy implements IEndpointStateChangeSubscriber {

    private static final String DATACENTERS = "datacenters";
    private static final Logger logger = LoggerFactory.getLogger(DatacenterReplicationStrategy.class);

    private final Set<String> datacenters = new HashSet<String>();
    private final Multimap<String, InetAddress> datacenterEndpoints = ArrayListMultimap.create();

    public DatacenterReplicationStrategy(String keyspaceName, TokenMetadata tokenMetadata, IEndpointSnitch snitch, Map<String, String> configOptions) {
        super(keyspaceName, tokenMetadata, snitch, configOptions);

        for (String dc : this.configOptions.get(DATACENTERS).split(",")) {
            datacenters.add(dc);

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
    public Collection<String> recognizedOptions() {
        return Collections.<String> singleton(DATACENTERS);
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

    @Override
    public void validateOptions() throws ConfigurationException {
        if (this.configOptions.get(DATACENTERS) == null)
            throw new ConfigurationException(DATACENTERS + " option is required for DatacenterReplicationStrategy");

        /*
         * Multimap<String, InetAddress> allDatacenter =
         * tokenMetadata.cachedOnlyTokenMap
         * ().getTopology().getDatacenterEndpoints(); for(String dc :
         * this.configOptions.get(DATACENTERS).split(",") ) { if
         * (!allDatacenter.containsKey(dc)) { throw new
         * ConfigurationException("Unknown datacenter "+dc); } }
         */
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
            logger.debug("DatacenterReplicationStrategy endpoint removed keyspace={} datacenters={} endpoints={}", keyspaceName, datacenters,
                    datacenterEndpoints);
        }
    }

    @Override
    public void beforeChange(InetAddress arg0, EndpointState arg1, ApplicationState arg2, VersionedValue arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAlive(InetAddress arg0, EndpointState arg1) {
        logger.debug("onAlive addr=" + arg0 + " EndpointState=" + arg1);
        addEndpoint(arg0);
    }

    @Override
    public void onChange(InetAddress arg0, ApplicationState arg1, VersionedValue arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDead(InetAddress arg0, EndpointState arg1) {
        logger.debug("onDead addr=" + arg0 + " EndpointState=" + arg1);
        removeEndpoint(arg0);
    }

    @Override
    public void onJoin(InetAddress arg0, EndpointState arg1) {
        logger.debug("Jonning addr=" + arg0 + " EndpointState=" + arg1);
        addEndpoint(arg0);
    }

    @Override
    public void onRemove(InetAddress arg0) {
        logger.debug("Removing addr=" + arg0);
        removeEndpoint(arg0);
    }

    @Override
    public void onRestart(InetAddress arg0, EndpointState arg1) {
        logger.debug("onRestart addr=" + arg0);
        addEndpoint(arg0);
    }
}
