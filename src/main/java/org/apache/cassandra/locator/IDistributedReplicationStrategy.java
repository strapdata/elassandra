package org.apache.cassandra.locator;

import java.util.Set;

/**
 * Common interface for distributed replication strategy.
 * @author vroyer
 */
public interface IDistributedReplicationStrategy {
    
    /**
     * 
     * @return List of datacenter hosting some replica.
     */
    public Set<String> getDatacenters();
    
    /**
     * 
     * @return Overall replication factor
     */
    public int getReplicationFactor();
    
    /***
     * 
     * @param dc datacenter name
     * @return Replication factor per datacenter.
     */
    public int getReplicationFactor(String dc);
}
