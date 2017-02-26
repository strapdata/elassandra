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
package org.elassandra.discovery;

import static org.apache.cassandra.cql3.QueryProcessor.executeInternal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.EndpointState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.gms.IEndpointStateChangeSubscriber;
import org.apache.cassandra.gms.VersionedValue;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.internal.Nullable;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.node.service.NodeService;
import org.elasticsearch.transport.TransportService;

import com.google.common.collect.Maps;

/**
 * Discover the cluster topology from cassandra snitch and settings, mappings, blocks from the elastic_admin keyspace.
 * Publishing is just a notification to refresh in memory configuration from the cassandra table.
 * @author vroyer
 *
 */
public class CassandraDiscovery extends AbstractLifecycleComponent<Discovery> implements Discovery, IEndpointStateChangeSubscriber {
    private final TransportService transportService;
    private final ClusterService clusterService;
    private final ClusterName clusterName;
    private final Version version;

    private final CopyOnWriteArrayList<MetaDataVersionListener> metaDataVersionListeners = new CopyOnWriteArrayList<>();

    private DiscoveryNode localNode;

    @Nullable
    private NodeService nodeService;

    private static final ConcurrentMap<ClusterName, ClusterGroup> clusterGroups = ConcurrentCollections.newConcurrentMap();

    private final ClusterGroup clusterGroup;

    private InetAddress localAddress;
    private String localDc;
    
    @Inject
    public CassandraDiscovery(Settings settings, ClusterName clusterName, TransportService transportService, ClusterService clusterService, Version version) {
        super(settings);
        this.clusterName = clusterName;
        this.clusterService = clusterService;
        this.transportService = transportService;
        this.version = version;
        
        this.localAddress = FBUtilities.getBroadcastAddress();
        this.localDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(localAddress);

        Map<String, String> attrs = Maps.newHashMap();
        attrs.put("data", "true");
        attrs.put("master", "true");
        attrs.put("data_center", localDc);
        attrs.put("rack", DatabaseDescriptor.getEndpointSnitch().getRack(localAddress));

        String localHostId = SystemKeyspace.getLocalHostId().toString();
        localNode = new DiscoveryNode(buildNodeName(localAddress), localHostId, new InetSocketTransportAddress(FBUtilities.getBroadcastAddress(), publishPort()), attrs, version);
        localNode.status(DiscoveryNodeStatus.ALIVE);
        this.transportService.setLocalNode(localNode);
        
        this.clusterGroup = new ClusterGroup();
        clusterGroups.put(clusterName, clusterGroup);
        clusterGroup.put(this.localNode.getId(), this.localNode);
        
        logger.info("localNode name={} id={} localAddress={} publish_host={}", this.localNode.getName(), this.localNode.getId(), localAddress, this.localNode.address());
    }

    @Override
    public void setNodeService(@Nullable NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public static String buildNodeName() {
        return buildNodeName(FBUtilities.getLocalAddress());
    }
    
    public static String buildNodeName(InetAddress addr) {
        String hostname = NetworkAddress.format(addr);
        if (hostname != null)
            return hostname;
        return String.format(Locale.getDefault(), "node%03d%03d%03d%03d", 
                (int) (addr.getAddress()[0] & 0xFF), (int) (addr.getAddress()[1] & 0xFF), 
                (int) (addr.getAddress()[2] & 0xFF), (int) (addr.getAddress()[3] & 0xFF));
    }
    
    @Override
    protected void doStart()  {
        synchronized (clusterGroup) {
            logger.debug("Connected to cluster [{}]", clusterName.value());
            // initialize cluster from cassandra system.peers 
            // WARNING: system.peers may be incomplete because commitlogs not yet applied
            for (UntypedResultSet.Row row : executeInternal("SELECT peer, data_center, rack, rpc_address, host_id from system." + SystemKeyspace.PEERS)) {
                InetAddress peer = row.getInetAddress("peer");
                InetAddress rpc_address = row.getInetAddress("rpc_address");
                String datacenter = row.getString("data_center");
                if ((!peer.equals(localAddress)) && (localDc.equals(datacenter))) {
                    Map<String, String> attrs = Maps.newHashMap();
                    attrs.put("data", "true");
                    attrs.put("master", "true");
                    attrs.put("data_center", datacenter);
                    attrs.put("rack", row.getString("rack"));
                    
                    DiscoveryNode dn = new DiscoveryNode(buildNodeName(peer), row.getUUID("host_id").toString(), new InetSocketTransportAddress(rpc_address, publishPort()), attrs, version);
                    EndpointState endpointState = Gossiper.instance.getEndpointStateForEndpoint(peer);
                    if (endpointState == null) {
                        dn.status( DiscoveryNodeStatus.UNKNOWN );
                    } else {
                        dn.status((endpointState.isAlive()) ? DiscoveryNodeStatus.ALIVE : DiscoveryNodeStatus.DEAD);
                    }
                    clusterGroup.put(dn.getId(), dn);
                    logger.debug("  node internal_ip={} host_id={} node_name={} ", NetworkAddress.format(peer), dn.getId(), dn.getName());
                }
            }

            Gossiper.instance.register(this);
            updateClusterGroupsFromGossiper();
            updateRoutingTable("starting-cassandra-discovery");
        }
    }

    public DiscoveryNodes nodes() {
        return this.clusterGroup.nodes();
    }
    
    private void updateMetadata(String source, MetaData newMetadata) {
        final MetaData schemaMetaData = newMetadata;
        clusterService.submitStateUpdateTask(source, new ClusterStateUpdateTask() {

            @Override
            public ClusterState execute(ClusterState currentState) {
                ClusterState.Builder newStateBuilder = ClusterState.builder(currentState).nodes(nodes());

                if (schemaMetaData != null) {
                    newStateBuilder.metaData(schemaMetaData);
                }

                ClusterState newClusterState = clusterService.updateNumberOfShards( newStateBuilder.build() );
                return ClusterState.builder(newClusterState).incrementVersion().build();
            }
            
            @Override
            public boolean doPresistMetaData() {
                return false;
            }
            
            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }

        });
    }
    
    private void updateRoutingTable(String source) {
        clusterService.submitStateUpdateTask(source, new ClusterStateUpdateTask() {

            @Override
            public ClusterState execute(ClusterState currentState) {
                return ClusterState.builder(currentState).incrementVersion().nodes(nodes()).build();
            }

            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }

        });
    }

    
    /**
     * Update cluster group members from cassandra topology (should only be triggered by IEndpointStateChangeSubscriber events).
     * This should trigger re-sharding of index for new nodes (when token distribution change).
     */
    public void updateClusterGroupsFromGossiper() {
        for (Entry<InetAddress, EndpointState> entry : Gossiper.instance.getEndpointStates()) {
            DiscoveryNodeStatus status = (entry.getValue().isAlive()) ? DiscoveryNode.DiscoveryNodeStatus.ALIVE : DiscoveryNode.DiscoveryNodeStatus.DEAD;

            if (DatabaseDescriptor.getEndpointSnitch().getDatacenter(entry.getKey()).equals(localDc)) {
                VersionedValue vv = entry.getValue().getApplicationState(ApplicationState.HOST_ID);
                if (vv != null) {
                    String hostId = vv.value;
                    DiscoveryNode dn = clusterGroup.get(hostId);
                    if (dn == null) {
                        Map<String, String> attrs = Maps.newHashMap();
                        attrs.put("data", "true");
                        attrs.put("master", "true");
                        attrs.put("data_center", localDc);
                        attrs.put("rack", DatabaseDescriptor.getEndpointSnitch().getRack(entry.getKey()));

                        InetAddress rpc_address = com.google.common.net.InetAddresses.forString(entry.getValue().getApplicationState(ApplicationState.RPC_ADDRESS).value);
                        dn = new DiscoveryNode(buildNodeName(entry.getKey()), hostId.toString(), 
                                new InetSocketTransportAddress(rpc_address, publishPort()), attrs, version);
                        dn.status(status);

                        if (localAddress.equals(entry.getKey())) {
                            logger.debug("Update local node host_id={} status={} timestamp={}", 
                                    NetworkAddress.format(entry.getKey()), dn.getId(), dn.getName(), entry.getValue().isAlive(), entry.getValue().getUpdateTimestamp());
                            clusterGroup.remove(this.localNode.id());
                            this.localNode = dn;
                        } else {
                            logger.debug("New node addr_ip={} node_name={} host_id={} status={} timestamp={}", 
                                    NetworkAddress.format(entry.getKey()), dn.getId(), dn.getName(), entry.getValue().isAlive(), entry.getValue().getUpdateTimestamp());
                        }
                        clusterGroup.put(dn.getId(), dn);
                        if (ElassandraDaemon.hasWorkloadColumn && (entry.getValue().getApplicationState(ApplicationState.X1) != null || entry.getValue().getApplicationState(ApplicationState.X2) !=null)) {
                            SystemKeyspace.updatePeerInfo(entry.getKey(), "workload", "elasticsearch");
                        }
                    } else {
                        // may update DiscoveryNode status.
                        if (!dn.getStatus().equals(status)) {
                            dn.status(status);
                        }
                    }
                }
            }
        }
    }

    private int publishPort() {
        try {
            return settings.getAsInt("transport.netty.publish_port", settings.getAsInt("transport.publish_port",settings.getAsInt("transport.tcp.port", 9300)));
        } catch (SettingsException | NumberFormatException e) {
            String publishPort = settings.get("transport.netty.publish_port", settings.get("transport.publish_port",settings.get("transport.tcp.port", "9300")));
            if (publishPort.indexOf("-") >0 ) {
                return Integer.parseInt(publishPort.split("-")[0]);
            } else {
                throw e;
            }
        }
    }
    
    public void updateNode(InetAddress addr, EndpointState state) {
        if (DatabaseDescriptor.getEndpointSnitch().getDatacenter(addr).equals(localDc)) {
            DiscoveryNodeStatus status = (state.isAlive()) ? DiscoveryNode.DiscoveryNodeStatus.ALIVE : DiscoveryNode.DiscoveryNodeStatus.DEAD;
            boolean updatedNode = false;
            String hostId = state.getApplicationState(ApplicationState.HOST_ID).value;
            DiscoveryNode dn = clusterGroup.get(hostId);
            if (dn == null) {
                Map<String, String> attrs = Maps.newHashMap();
                attrs.put("data", "true");
                attrs.put("master", "true");
                attrs.put("data_center", localDc);
                attrs.put("rack", DatabaseDescriptor.getEndpointSnitch().getRack(addr));

                InetAddress rpc_address = com.google.common.net.InetAddresses.forString(state.getApplicationState(ApplicationState.RPC_ADDRESS).value);
                dn = new DiscoveryNode(buildNodeName(addr), 
                        hostId.toString(), 
                        new InetSocketTransportAddress(rpc_address, publishPort()), 
                        attrs, version);
                dn.status(status);
                logger.debug("New node soure=updateNode internal_ip={} rpc_address={}, node_name={} host_id={} status={} timestamp={}", 
                        NetworkAddress.format(addr), NetworkAddress.format(rpc_address), dn.getId(), dn.getName(), status, state.getUpdateTimestamp());
                clusterGroup.members.put(dn.getId(), dn);
                if (ElassandraDaemon.hasWorkloadColumn && (state.getApplicationState(ApplicationState.X1) != null || state.getApplicationState(ApplicationState.X2) !=null)) {
                    SystemKeyspace.updatePeerInfo(addr, "workload", "elasticsearch");
                }
                updatedNode = true;
            } else {
                // may update DiscoveryNode status.
                if (!dn.getStatus().equals(status)) {
                    dn.status(status);
                    updatedNode = true;
                    logger.debug("node id={} new state={}",dn.id(), dn.status().toString());
                }
            }
            if (updatedNode)
                updateRoutingTable("update-node-" + NetworkAddress.format(addr));
        }
    }

    public boolean awaitMetaDataVersion(long version, TimeValue timeout) throws InterruptedException {
        MetaDataVersionListener listener = new MetaDataVersionListener(version);
        return listener.await(timeout.millis(), TimeUnit.MILLISECONDS);
    }
    
    /**
     * Release listeners who have reached the expected metadat version.
     */
    public void checkMetaDataVersion() {
        for(Iterator<MetaDataVersionListener> it = this.metaDataVersionListeners.iterator(); it.hasNext(); ) {
            MetaDataVersionListener listener = it.next();
            boolean versionReached = true;
            for(InetAddress addr : Gossiper.instance.getLiveTokenOwners()) {
                if (DatabaseDescriptor.getEndpointSnitch().getDatacenter(addr).equals(localDc)) {
                    EndpointState endPointState = Gossiper.instance.getEndpointStateForEndpoint(addr);
                    VersionedValue vv = endPointState.getApplicationState(ELASTIC_META_DATA);
                    if (vv != null && vv.value.lastIndexOf('/') > 0) { 
                        Long version = Long.valueOf(vv.value.substring(vv.value.lastIndexOf('/')+1));
                        if (version < listener.version()) {
                            versionReached = false;
                            break;
                        }
                    }
                }
            }
            if (versionReached) {
                logger.debug("MetaData.version = {} reached", listener.version());
                listener.release();
                metaDataVersionListeners.remove(listener);
            }
        }
    }
    
    public class MetaDataVersionListener  {
        final long expectedVersion;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        
        public MetaDataVersionListener(long version) {
            expectedVersion = version;
        }

        public long version() {
            return this.expectedVersion;
        }
        
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            CassandraDiscovery.this.metaDataVersionListeners.add(this);
            Set<Entry<InetAddress,EndpointState>> currentStates = Gossiper.instance.getEndpointStates();
            boolean versionReached = true;
            for(Entry<InetAddress,EndpointState> entry : currentStates) {
                if (DatabaseDescriptor.getEndpointSnitch().getDatacenter(entry.getKey()).equals(localDc) && entry.getValue().isAlive()) {
                    VersionedValue vv = entry.getValue().getApplicationState(ELASTIC_META_DATA);
                    if (vv != null && vv.value.lastIndexOf('/') > 0) { 
                        Long version = Long.valueOf(vv.value.substring(vv.value.lastIndexOf('/')+1));
                        if (version < expectedVersion) {
                            versionReached = false;
                            break;
                        }
                    }
                }
            }
            if (!versionReached) {
                return countDownLatch.await(timeout, unit);
            }
           return true;
        }

        public void release() {
            countDownLatch.countDown();
        }
    }
    
    @Override
    public void beforeChange(InetAddress arg0, EndpointState arg1, ApplicationState arg2, VersionedValue arg3) {
        //logger.warn("beforeChange({},{},{},{})",arg0,arg1,arg2,arg3);
    }

    @Override
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue versionValue) {
        EndpointState epState = Gossiper.instance.getEndpointStateForEndpoint(endpoint);
        if (epState == null || Gossiper.instance.isDeadState(epState)) {
            if (logger.isTraceEnabled()) 
                logger.trace("Ignoring state change for dead or unknown endpoint: {}", endpoint);
            return;
        }
        if (!this.localAddress.equals(endpoint)) {
            if (state == ApplicationState.X1 && DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint).equals(localDc)) {
                // X1: update local shard state
                if (logger.isTraceEnabled())
                    logger.trace("Endpoint={} ApplicationState={} value={} => update routingTable", endpoint, state, versionValue.value);
                updateRoutingTable("onChange-" + endpoint + "-" + state.toString()+" X1="+versionValue.value);
                connectToNode(endpoint);
            } else if (state == ApplicationState.X2 && clusterService.isDatacenterGroupMember(endpoint)) {
                // X2 from datacenter.group: update metadata if metadata version is higher than our.
                if (versionValue != null) {
                    int i = versionValue.value.lastIndexOf('/');
                    if (i > 0) {
                        Long version = Long.valueOf(versionValue.value.substring(i+1));
                        if (version > this.clusterService.state().metaData().version()) {
                            MetaData metadata = clusterService.checkForNewMetaData(version);
                            if (metadata != null) {
                                if (logger.isTraceEnabled()) 
                                    logger.trace("Endpoint={} ApplicationState={} value={} => update metaData {}/{}", 
                                        endpoint, state, versionValue.value, metadata.uuid(), metadata.version());
                                updateMetadata("onChange-" + endpoint + "-" + state.toString()+" metadata="+metadata.uuid()+"/"+metadata.version(), metadata);
                            }
                        }
                    }
                }
                if (metaDataVersionListeners.size() > 0 && DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint).equals(localDc)) {
                    checkMetaDataVersion();
                }
            }
        }
    }

    @Override
    public void connectToNodes() {
        for(DiscoveryNode node : clusterGroup.members().values()) {
            if (!localNode().equals(node) && localNode().shouldConnectTo(node))
                transportService.connectToNode(node);
        }
    }
    
    public void connectToNode(InetAddress arg0) {
        DiscoveryNode node = this.nodes().findByInetAddress(arg0);
        if (node != null && localNode().shouldConnectTo(node)) {
            try {
                transportService.connectToNode(node);
            } catch (Throwable e) {
                // the fault detection will detect it as failed as well
                logger.warn("failed to connect to node [" + node + "]", e);
            }
        }
    }
    
    public void disconnectFromNode(InetAddress arg0) {
        DiscoveryNode node = this.nodes().findByInetAddress(arg0);
        if (node != null) {
            try {
                transportService.disconnectFromNode(node);
            } catch (Throwable e) {
                logger.warn("failed to disconnect to node [" + node + "]", e);
            }
        }
    }
    
    @Override
    public void onAlive(InetAddress arg0, EndpointState arg1) {
        logger.debug("onAlive Endpoint={} ApplicationState={} isAlive={} => update node + connecting", arg0, arg1, arg1.isAlive());
        updateNode(arg0, arg1);
        connectToNode(arg0);
    }
    
    @Override
    public void onDead(InetAddress arg0, EndpointState arg1) {
        logger.debug("onDead Endpoint={}  ApplicationState={} isAlive={} => update node + disconnecting", arg0, arg1, arg1.isAlive());
        updateNode(arg0, arg1);
        //disconnectFromNode(arg0);
    }
    
    @Override
    public void onRestart(InetAddress arg0, EndpointState arg1) {
        //logger.debug("onRestart Endpoint={}  ApplicationState={} isAlive={}", arg0, arg1, arg1.isAlive());
    }

    @Override
    public void onJoin(InetAddress arg0, EndpointState arg1) {
        //logger.debug("onAlive Endpoint={} ApplicationState={} isAlive={}", arg0, arg1, arg1.isAlive() );
    }
   
    @Override
    public void onRemove(InetAddress endpoint) {
        DiscoveryNode removedNode = this.nodes().findByInetAddress(endpoint);
        if (removedNode != null) {
            logger.warn("Removing node ip={} node={}  => disconnecting", endpoint, removedNode);
            this.clusterGroup.remove(removedNode.getId());
            updateRoutingTable("node-removed-"+removedNode.getId());
            disconnectFromNode(endpoint);
        }
        
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        Gossiper.instance.unregister(this);
        
        synchronized (clusterGroup) {
            if (clusterGroup == null) {
                logger.warn("Illegal state, should not have an empty cluster group when stopping, I should be there at teh very least...");
                return;
            }
            if (clusterGroup.members().isEmpty()) {
                // no more members, remove and return
                clusterGroups.remove(clusterName);
                return;
            }
        }
    }

    private static final ApplicationState ELASTIC_SHARDS_STATES = ApplicationState.X1;
    private static final ApplicationState ELASTIC_META_DATA = ApplicationState.X2;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final TypeReference<Map<String, ShardRoutingState>> indexShardStateTypeReference = new TypeReference<Map<String, ShardRoutingState>>() {};

    @Override
    public Map<UUID, ShardRoutingState> getShardRoutingStates(String index) {
        Map<UUID, ShardRoutingState> shardsStates = new HashMap<UUID, ShardRoutingState>(this.clusterGroup.members.size());
        for(Entry<InetAddress,EndpointState> entry :  Gossiper.instance.getEndpointStates()) {
            InetAddress endpoint = entry.getKey();
            EndpointState state = entry.getValue();
            if (!endpoint.equals(this.localAddress) && 
                DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint).equals(this.localDc) &&
                state != null && 
                state.isAlive()) {
                VersionedValue value = state.getApplicationState(ELASTIC_SHARDS_STATES);
                if (value != null) {
                    try {
                        Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(value.value, indexShardStateTypeReference);
                        ShardRoutingState shardState = shardsStateMap.get(index);
                        if (shardState != null) {
                            shardsStates.put(Gossiper.instance.getHostId(endpoint), shardState);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse gossip index shard state", e);
                    }
                }
            }
        }
        return shardsStates;
    }
    
    /**
     * add local index shard state to local application state.
     * @param index
     * @param shardRoutingState
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Override
    public synchronized void putShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        if (Gossiper.instance.isEnabled()) {
            Map<String, ShardRoutingState> shardsStateMap = null;
            EndpointState state = Gossiper.instance.getEndpointStateForEndpoint(FBUtilities.getBroadcastAddress());
            if (state != null) {
                VersionedValue value = state.getApplicationState(ELASTIC_SHARDS_STATES);
                if (value != null) {
                    shardsStateMap = (Map<String, ShardRoutingState>) jsonMapper.readValue(value.value, indexShardStateTypeReference);
                }
            }
            if (shardsStateMap == null) {
                shardsStateMap = new HashMap<String, ShardRoutingState>();
            }
            if (shardRoutingState != null) {
                shardsStateMap.put(index, shardRoutingState);
            } else {
                if (shardsStateMap.containsKey(index)) {
                    shardsStateMap.remove(index);
                }
            }
            String newValue = jsonMapper.writerWithType(indexShardStateTypeReference).writeValueAsString(shardsStateMap);
            Gossiper.instance.addLocalApplicationState(ELASTIC_SHARDS_STATES, StorageService.instance.valueFactory.datacenter(newValue));
        } else {
            logger.trace("Cannot put X1 for index={}, gossip not enabled", index);
        }
    }

    @Override
    public void publishX1(ClusterState clusterState) {
        if (Gossiper.instance.isEnabled()) {
            Map<String, ShardRoutingState> shardsStateMap = new HashMap<String, ShardRoutingState>();
            for(IndexRoutingTable irt : clusterState.routingTable()) {
                IndexShardRoutingTable isrt = irt.shard(0);
                if (isrt != null) {
                    ShardRouting shardRouting = isrt.getPrimaryShardRouting();
                    if (shardRouting != null)
                        shardsStateMap.put(irt.getIndex(), shardRouting.state());
                }
            }
            try {
                String newValue = jsonMapper.writerWithType(indexShardStateTypeReference).writeValueAsString(shardsStateMap);
                Gossiper.instance.addLocalApplicationState(ELASTIC_SHARDS_STATES, StorageService.instance.valueFactory.datacenter(newValue));
            } catch (IOException e) {
                logger.error("Unxepected error", e);
            }
        } else {
            logger.trace("Cannot put X1 for cluster state, gossip not enabled");
        }
    }
    
    @Override
    public void publishX2(ClusterState clusterState) {
        if (Gossiper.instance.isEnabled()) {
            String clusterStateSting = clusterState.metaData().uuid() + '/' + clusterState.metaData().version();
            Gossiper.instance.addLocalApplicationState(ELASTIC_META_DATA, StorageService.instance.valueFactory.datacenter(clusterStateSting));
        } else {
            logger.trace("Cannot put X2 for cluster state, gossip not enabled");
        }
    }

    @Override
    protected void doClose()  {
    }

    @Override
    public DiscoveryNode localNode() {
        return localNode;
    }

    
    @Override
    public String nodeDescription() {
        return clusterName.value() + "/" + localNode.id();
    }


    private class ClusterGroup {

        private Map<String, DiscoveryNode> members = ConcurrentCollections.newConcurrentMap();

        Map<String, DiscoveryNode> members() {
            return members;
        }

        public void put(String id, DiscoveryNode node) {
            members.put(id, node);
        }
        
        public void remove(String id) {
            members.remove(id);
        }


        public DiscoveryNode get(String id) {
            return members.get(id);
        }
        
        public Collection<DiscoveryNode> values() {
            return members.values();
        }
        
        public DiscoveryNodes nodes() {
            DiscoveryNodes.Builder nodesBuilder = new DiscoveryNodes.Builder();
            nodesBuilder.localNodeId(CassandraDiscovery.this.localNode.id()).masterNodeId(CassandraDiscovery.this.localNode.id());
            for (DiscoveryNode node : members.values()) {
                nodesBuilder.put(node);
            }
            return nodesBuilder.build();
        }
    }

    @Override
    public void startInitialJoin() {
        // TODO Auto-generated method stub
    }

}
