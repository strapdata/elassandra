/*
 * Copyright (c) 2015 Vincent Royer (vroyer@vroyer.org).
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
package org.elasticsearch.discovery.cassandra;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.EndpointState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.gms.IEndpointStateChangeSubscriber;
import org.apache.cassandra.gms.VersionedValue;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.cassandra.SchemaService;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ProcessedClusterStateNonMasterUpdateTask;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.node.DiscoveryNodeService;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.internal.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoverySettings;
import org.elasticsearch.discovery.InitialStateDiscoveryListener;
import org.elasticsearch.node.service.NodeService;
import org.elasticsearch.transport.TransportService;

import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.google.common.collect.Maps;

/**
 * Discover the cluster topology from cassandra snitch and settings, mappings, blocks from the elastic_admin keyspace.
 * Publishing is just a nofification to refresh in memory configuration from the cassandra table.
 * @author vroyer
 *
 */
public class CassandraDiscovery extends AbstractLifecycleComponent<Discovery> implements Discovery, IEndpointStateChangeSubscriber {

    private static final DiscoveryNode[] NO_MEMBERS = new DiscoveryNode[0];

    private final TransportService transportService;
    private final ClusterService clusterService;
    private final DiscoveryNodeService discoveryNodeService;
    private final ClusterName clusterName;
    private final SchemaService schemaService;
    private final Version version;
    private final DiscoverySettings discoverySettings;

    //private final PublishClusterStateAction publishClusterStateAction;

    private final AtomicBoolean initialStateSent = new AtomicBoolean();
    private final CopyOnWriteArrayList<InitialStateDiscoveryListener> initialStateListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<ShardStateRemovedListener> shardStateRemovedListeners = new CopyOnWriteArrayList<>();

    private AllocationService allocationService;

    private DiscoveryNode localNode;

    @Nullable
    private NodeService nodeService;

    private volatile boolean master = true;

    private static final ConcurrentMap<ClusterName, ClusterGroup> clusterGroups = ConcurrentCollections.newConcurrentMap();

    private final ClusterGroup clusterGroup;

    private InetAddress localAddress = null;
    private String localDc = null;
    
    @Inject
    public CassandraDiscovery(Settings settings, ClusterName clusterName, TransportService transportService, ClusterService clusterService, DiscoveryNodeService discoveryNodeService, Version version,
            DiscoverySettings discoverySettings, SchemaService schemaService) {
        super(settings);
        this.clusterName = clusterName;
        this.clusterService = clusterService;
        this.transportService = transportService;
        this.discoveryNodeService = discoveryNodeService;
        this.version = version;
        this.discoverySettings = discoverySettings;
        this.schemaService = schemaService;
        
        this.clusterGroup = new ClusterGroup();
        clusterGroups.put(clusterName, clusterGroup);
        
        //this.publishClusterStateAction = new PublishClusterStateAction(settings, transportService, this, new NewClusterStateListener(), discoverySettings, clusterName);
    }

    @Override
    public void setNodeService(@Nullable NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public void setAllocationService(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * TODO: provide customizable node name factory.
     * @param addr
     * @return
     */
    public static String buildNodeName(InetAddress addr) {
        String hostname = addr.getHostName();
        if (hostname != null)
            return hostname;
        return String.format(Locale.getDefault(), "node%03d%03d%03d%03d", 
                (int) (addr.getAddress()[0] & 0xFF), (int) (addr.getAddress()[1] & 0xFF), 
                (int) (addr.getAddress()[2] & 0xFF), (int) (addr.getAddress()[3] & 0xFF));
    }

    @Override
    protected void doStart() throws ElasticsearchException {

        synchronized (clusterGroup) {
            logger.debug("Connected to cluster [{}]", clusterName.value());

            localAddress = FBUtilities.getLocalAddress();
            localDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(localAddress);

            InetSocketTransportAddress elasticAddress = (InetSocketTransportAddress) transportService.boundAddress().publishAddress();
            logger.info("Listening address Cassandra=" + localAddress + " Elastic=" + elasticAddress.toString());

            // get local node from cassandra cluster
            {
                Map<String, String> attrs = Maps.newHashMap();
                attrs.put("data", "true");
                attrs.put("master", "true");
                attrs.put("data_center", localDc);
                attrs.put("rack", DatabaseDescriptor.getEndpointSnitch().getRack(localAddress));

                String hostId = SystemKeyspace.getLocalHostId().toString();
                localNode = new DiscoveryNode(buildNodeName(localAddress), hostId, transportService.boundAddress().publishAddress(), attrs, version);
                localNode.status(DiscoveryNodeStatus.ALIVE);
                master = true;
                clusterGroup.put(this.localNode.getId(), this.localNode);
                logger.info("localNode name={} id={}", this.localNode.getName(), this.localNode.getId());
            }

            // initialize cluster from cassandra system.peers 
            Map<InetAddress, UUID> peers = SystemKeyspace.loadHostIds();
            Map<InetAddress, Map<String, String>> endpointInfo = SystemKeyspace.loadDcRackInfo();
            for (Entry<InetAddress, UUID> entry : peers.entrySet()) {
                if ((!entry.getKey().equals(localAddress)) && (localDc.equals(endpointInfo.get(entry.getKey()).get("data_center")))) {
                    Map<String, String> attrs = Maps.newHashMap();
                    attrs.put("data", "true");
                    attrs.put("master", "true");
                    attrs.putAll(endpointInfo.get(entry.getKey()));
                    DiscoveryNode dn = new DiscoveryNode(buildNodeName(entry.getKey()), entry.getValue().toString(), new InetSocketTransportAddress(entry.getKey(), settings.getAsInt(
                            "transport.tcp.port", 9300)), attrs, version);
                    EndpointState endpointState = Gossiper.instance.getEndpointStateForEndpoint(entry.getKey());
                    dn.status((endpointState.isAlive()) ? DiscoveryNodeStatus.ALIVE : DiscoveryNodeStatus.DEAD);
                    clusterGroup.put(dn.getId(), dn);
                    logger.debug("remanent node addr_ip={} node_name={} host_id={} ", entry.getKey().toString(), dn.getId(), dn.getName());
                }
            }

            Gossiper.instance.register(this);
            updateClusterGroupsFromGossiper();
            updateClusterState("starting-cassandra-discovery", null);
        }
    }

    public interface ShardStateRemovedListener {
        public String index();
        public void   removed();
    }
    
    
    public DiscoveryNodes nodes() {
        return this.clusterGroup.nodes();
    }

    private void updateClusterState(String source, MetaData newMetadata1) {
        final MetaData schemaMetaData = newMetadata1;

        clusterService.submitStateUpdateTask(source, new ProcessedClusterStateNonMasterUpdateTask() {

            @Override
            public ClusterState execute(ClusterState currentState) {
                ClusterState.Builder newStateBuilder = ClusterState.builder(currentState);

                DiscoveryNodes newDiscoveryNodes = nodes();
                newStateBuilder.nodes(newDiscoveryNodes);

                if (schemaMetaData != null) {
                    newStateBuilder.metaData(schemaMetaData);
                }

                ClusterState newClusterState = newStateBuilder.build();
                RoutingTable newRoutingTable = RoutingTable.builder(clusterService, newClusterState).build();
                logger.trace("newRoutingTable={}", newRoutingTable.prettyPrint());

                if (newRoutingTable != currentState.routingTable()) {
                    ClusterState newClusterState2 = ClusterState.builder(newClusterState).routingTable(newRoutingTable).version(currentState.version() + 1).build();
                    logger.debug("submit new cluster state {}", newClusterState2.toString());
                    return newClusterState2;
                }
                logger.debug("ingoring unchanged cluster state update version={}", currentState.version());
                return currentState;
            }

            @Override
            public boolean doPresistMetaData() {
                return false;
            }

            @Override
            public void onFailure(String source, Throwable t) {
                logger.error("unexpected failure during [{}]", t, source);
            }

            @Override
            public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                sendInitialStateEventIfNeeded();
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

                        dn = new DiscoveryNode(buildNodeName(entry.getKey()), hostId.toString(), new InetSocketTransportAddress(entry.getKey(), settings.getAsInt("transport.tcp.port", 9300)), attrs, version);
                        dn.status(status);

                        if (localAddress.equals(entry.getKey())) {
                            logger.debug("Update local node host_id={} status={} timestamp={}", 
                                    entry.getKey().toString(), dn.getId(), dn.getName(), entry.getValue().isAlive(), entry.getValue().getUpdateTimestamp());
                            clusterGroup.remove(this.localNode.id());
                            this.localNode = dn;
                        } else {
                            logger.debug("New node addr_ip={} node_name={} host_id={} status={} timestamp={}", 
                                    entry.getKey().toString(), dn.getId(), dn.getName(), entry.getValue().isAlive(), entry.getValue().getUpdateTimestamp());
                        }
                        clusterGroup.put(dn.getId(), dn);
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

    public MetaData hasNewMetaData() {
        MetaData currentMetaData = clusterService.state().metaData();
        MetaData newMetaData = schemaService.readMetaDataAsComment();
        // TODO: merge metadata ?
        if (!newMetaData.uuid().equals(currentMetaData.uuid()) || (newMetaData.version() != currentMetaData.version())) {
            logger.debug("updating metadata from uid/version={}/{} to {}/{}", currentMetaData.uuid(), currentMetaData.version(), newMetaData.uuid(), newMetaData.version());
            return newMetaData;
        }
        logger.debug("ignoring unchanged metadata uuid/version={}/{}", newMetaData.uuid(), newMetaData.version());
        return null;
    }
    
    public void updateNode(InetAddress addr, EndpointState state) {
        
        DiscoveryNodeStatus status = (state.isAlive()) ? DiscoveryNode.DiscoveryNodeStatus.ALIVE : DiscoveryNode.DiscoveryNodeStatus.DEAD;
        boolean updatedNode = false;
        if (DatabaseDescriptor.getEndpointSnitch().getDatacenter(addr).equals(localDc)) {
            String hostId = state.getApplicationState(ApplicationState.HOST_ID).value;
            DiscoveryNode dn = clusterGroup.get(hostId);
            if (dn == null) {
                Map<String, String> attrs = Maps.newHashMap();
                attrs.put("data", "true");
                attrs.put("master", "true");
                attrs.put("data_center", localDc);
                attrs.put("rack", DatabaseDescriptor.getEndpointSnitch().getRack(addr));

                dn = new DiscoveryNode(buildNodeName(addr), hostId.toString(), new InetSocketTransportAddress(addr, settings.getAsInt("transport.tcp.port", 9300)), attrs, version);
                dn.status(status);
                logger.debug("New node soure=updateNode addr_ip={} node_name={} host_id={} status={} timestamp={}", addr.getHostAddress(), dn.getId(), dn.getName(), status, state.getUpdateTimestamp());
                clusterGroup.members.put(dn.getId(), dn);
                updatedNode = true;
            } else {
                // may update DiscoveryNode status.
                if (!dn.getStatus().equals(status)) {
                    dn.status(status);
                    updatedNode = true;
                }
            }
        }
        if (updatedNode)
            updateClusterState("update-node-" + addr.getHostAddress(), null);
    }

    @Override
    public void beforeChange(InetAddress arg0, EndpointState arg1, ApplicationState arg2, VersionedValue arg3) {
        //logger.warn("beforeChange({},{},{},{})",arg0,arg1,arg2,arg3);
    }

    @Override
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue value) {
        if (!localNode.getInetAddress().equals(endpoint)) {
            switch (state) {
            case SCHEMA: // remote metadata change
            case X1: // remote shards state change
                logger.debug("Endpoint={} ApplicationState={} value={} => update metaData and routingTable", endpoint, state, value.value);
                updateClusterState("onChange-" + endpoint + "-" + state.toString(), hasNewMetaData());
                break;
            }
        }
        
        if (state == ELASTIC_SHARDS_STATES && this.shardStateRemovedListeners.size() > 0) {
            for(Iterator<ShardStateRemovedListener> listenerIterator = this.shardStateRemovedListeners.iterator(); listenerIterator.hasNext(); ) {
                ShardStateRemovedListener listener = listenerIterator.next();
                boolean removed = true;
                for(DiscoveryNode node : nodes()) {
                    EndpointState eps = Gossiper.instance.getEndpointStateForEndpoint(node.getInetAddress());
                    VersionedValue vv = eps.getApplicationState(ELASTIC_SHARDS_STATES);
                    if (vv != null) {
                        try {
                            Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(vv.value, indexShardStateTypeReference);
                            ShardRoutingState shardState = shardsStateMap.get(listener.index());
                            if (shardState != null) {
                                removed = false;
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to parse gossip index shard state", e);
                        }
                    }
                }
                if (removed) {
                    listener.removed();
                    shardStateRemovedListeners.remove(listener);
                };
            }
        }
    }

    @Override
    public void onAlive(InetAddress arg0, EndpointState arg1) {
        logger.debug("onAlive Endpoint={} ApplicationState={} isAlive={} => update node", arg0, arg1, arg1.isAlive());
        updateNode(arg0, arg1);
    }
    
    @Override
    public void onDead(InetAddress arg0, EndpointState arg1) {
        logger.debug("onDead Endpoint={}  ApplicationState={} isAlive={} => update node", arg0, arg1, arg1.isAlive());
        updateNode(arg0, arg1);
    }
    
    @Override
    public void onRestart(InetAddress arg0, EndpointState arg1) {
        logger.debug("onRestart Endpoint={}  ApplicationState={} isAlive={}", arg0, arg1, arg1.isAlive());
    }

    @Override
    public void onJoin(InetAddress arg0, EndpointState arg1) {
        logger.debug("onAlive Endpoint={} ApplicationState={} isAlive={}", arg0, arg1, arg1.isAlive() );
    }
   
    @Override
    public void onRemove(InetAddress arg0) {
        // TODO: support onRemove (hostId unavailable)
        logger.warn("onRemove Endpoint={}  => removing a node not supported", arg0);
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

    /**
     * ELASTIC_INDEX_STATES = Map<IndexUid,ShardRoutingState>
     */
    private static final ApplicationState ELASTIC_SHARDS_STATES = ApplicationState.X1;
    private static final ApplicationState ELASTIC_META_DATA = ApplicationState.X2;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final TypeReference<Map<String, ShardRoutingState>> indexShardStateTypeReference = new TypeReference<Map<String, ShardRoutingState>>() {
    };

    /**
     * read the remote shard state from gossiper the X1 field.
     * TODO: cache all enpoint.X1 state to avoid many JSON parsing.
     */
    @Override
    public ShardRoutingState readIndexShardState(InetAddress address, String index, ShardRoutingState defaultState) {
        EndpointState state = Gossiper.instance.getEndpointStateForEndpoint(address);
        if (state != null) {
            VersionedValue value = state.getApplicationState(ELASTIC_SHARDS_STATES);
            if (value != null) {
                try {
                    Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(value.value, indexShardStateTypeReference);
                    ShardRoutingState shardState = shardsStateMap.get(index);
                    if (shardState != null) {
                        logger.debug("index shard state  addr={} index={} state={}", address, index, shardState);
                        return shardState;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse gossip index shard state", e);
                }
            }
        }
        return defaultState;
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
    public synchronized void writeIndexShardState(String index, ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        if (Gossiper.instance.isEnabled()) {
            Map<String, ShardRoutingState> shardsStateMap = null;
            EndpointState state = Gossiper.instance.getEndpointStateForEndpoint(FBUtilities.getBroadcastAddress());
            if (state != null) {
                VersionedValue value = state.getApplicationState(ELASTIC_SHARDS_STATES);
                if (value != null) {
                    shardsStateMap = (Map<String, ShardRoutingState>) jsonMapper.readValue(value.value, new TypeReference<Map<String, ShardRoutingState>>() {
                    });
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
            Gossiper.instance.addLocalApplicationState(ELASTIC_SHARDS_STATES, StorageService.instance.valueFactory.datacenter(jsonMapper.writeValueAsString(shardsStateMap)));
        }
    }

    
    @Override
    public void publish(ClusterState clusterState) {
        if (Gossiper.instance.isEnabled()) {
            String clusterStateSting = clusterState.metaData().uuid() + '/' + clusterState.metaData().version();
            Gossiper.instance.addLocalApplicationState(ELASTIC_META_DATA, StorageService.instance.valueFactory.datacenter(clusterStateSting));
        }
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }

    @Override
    public DiscoveryNode localNode() {
        return localNode;
    }

    @Override
    public void addListener(InitialStateDiscoveryListener listener) {
        this.initialStateListeners.add(listener);
    }

    @Override
    public void removeListener(InitialStateDiscoveryListener listener) {
        this.initialStateListeners.remove(listener);
    }


    public void addShardStateRemovedListener(ShardStateRemovedListener listener) {
        this.shardStateRemovedListeners.add(listener);
    }


    public void removeShardStateRemovedListener(ShardStateRemovedListener listener) {
        this.shardStateRemovedListeners.remove(listener);
    }
    
    @Override
    public String nodeDescription() {
        return clusterName.value() + "/" + localNode.id();
    }

    /*
    public void publish(ClusterState clusterState, final Discovery.AckListener ackListener) {
        if (!master) {
            throw new ElasticsearchIllegalStateException("Shouldn't publish state when not master");
        }
        // publish new clusterState over the network to cassandra nodes.
        //publishClusterStateAction.publish(clusterState, ackListener);
    }
    private void publish(DiscoveryNode[] members, ClusterState clusterState, final BlockingClusterStatePublishResponseHandler publishResponseHandler) {
        try {
            // we do the marshaling intentionally, to check it works well...
            final byte[] clusterStateBytes = Builder.toBytes(clusterState);
            for (final DiscoveryNode node : members) {
                if (discovery.master) {
                    continue;
                }
                final ClusterState nodeSpecificClusterState = ClusterState.Builder.fromBytes(clusterStateBytes, node, clusterName);
                nodeSpecificClusterState.status(ClusterState.ClusterStateStatus.RECEIVED);
                // ignore cluster state messages that do not include "me", not in the game yet...
                if (nodeSpecificClusterState.nodes().localNode() != null) {
                    assert nodeSpecificClusterState.nodes().masterNode() != null : "received a cluster state without a master";
                    assert !nodeSpecificClusterState.blocks().hasGlobalBlock(discoverySettings.getNoMasterBlock()) : "received a cluster state with a master block";
                    this.clusterService.submitStateUpdateTask("cassandra-disco-receive(from " + this.nodeName() + ")", new ProcessedClusterStateNonMasterUpdateTask() {
                        @Override
                        public ClusterState execute(ClusterState currentState) {
                            if (nodeSpecificClusterState.version() < currentState.version() && Objects.equal(nodeSpecificClusterState.nodes().masterNodeId(), currentState.nodes().masterNodeId())) {
                                return currentState;
                            }
                            if (currentState.blocks().hasGlobalBlock(discoverySettings.getNoMasterBlock())) {
                                // its a fresh update from the master as we transition from a start of not having a master to having one
                                logger.debug("got first state from fresh master [{}]", nodeSpecificClusterState.nodes().masterNodeId());
                                return nodeSpecificClusterState;
                            }
                            ClusterState.Builder builder = ClusterState.builder(nodeSpecificClusterState);
                            // if the routing table did not change, use the original one
                            if (nodeSpecificClusterState.routingTable().version() == currentState.routingTable().version()) {
                                builder.routingTable(currentState.routingTable());
                            }
                            if (nodeSpecificClusterState.metaData().version() == currentState.metaData().version()) {
                                builder.metaData(currentState.metaData());
                            }
                            return builder.build();
                        }
                        @Override
                        public void onFailure(String source, Throwable t) {
                            logger.error("unexpected failure during [{}]", t, source);
                            publishResponseHandler.onFailure(node, t);
                        }
                        @Override
                        public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                            sendInitialStateEventIfNeeded();
                            publishResponseHandler.onResponse(node);
                        }
                    });
                } else {
                    publishResponseHandler.onResponse(node);
                }
            }
            TimeValue publishTimeout = discoverySettings.getPublishTimeout();
            if (publishTimeout.millis() > 0) {
                try {
                    boolean awaited = publishResponseHandler.awaitAllNodes(publishTimeout);
                    if (!awaited) {
                        DiscoveryNode[] pendingNodes = publishResponseHandler.pendingNodes();
                        // everyone may have just responded
                        if (pendingNodes.length > 0) {
                            logger.warn("timed out waiting for all nodes to process published state [{}] (timeout [{}], pending nodes: {})", clusterState.version(), publishTimeout, pendingNodes);
                        }
                    }
                } catch (InterruptedException e) {
                    // ignore & restore interrupt
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // failure to marshal or un-marshal
            throw new ElasticsearchIllegalStateException("Cluster state failed to serialize", e);
        }
    }
    */

    /*
     * 
     */
    private void sendInitialStateEventIfNeeded() {
        if (initialStateSent.compareAndSet(false, true)) {
            for (InitialStateDiscoveryListener listener : initialStateListeners) {
                listener.initialStateProcessed();
            }
        }
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
        
        public DiscoveryNodes nodes() {
            DiscoveryNodes.Builder nodesBuilder = new DiscoveryNodes.Builder();
            nodesBuilder.localNodeId(CassandraDiscovery.this.localNode.id()).masterNodeId(CassandraDiscovery.this.localNode.id());
            for (DiscoveryNode node : members.values()) {
                nodesBuilder.put(node);
            }
            return nodesBuilder.build();
        }
    }

    /*
        private class NewClusterStateListener implements PublishClusterStateAction.NewClusterStateListener {
            @Override
            public void onNewClusterState(ClusterState clusterState, NewStateProcessed newStateProcessed) {
                handleNewClusterStateFromMaster(clusterState, newStateProcessed);
            }
        }
        
        static class ProcessClusterState {
            final ClusterState clusterState;
            final PublishClusterStateAction.NewClusterStateListener.NewStateProcessed newStateProcessed;
            volatile boolean processed;
            ProcessClusterState(ClusterState clusterState, PublishClusterStateAction.NewClusterStateListener.NewStateProcessed newStateProcessed) {
                this.clusterState = clusterState;
                this.newStateProcessed = newStateProcessed;
            }
        }
        
        private final BlockingQueue<ProcessClusterState> processNewClusterStates = ConcurrentCollections.newBlockingQueue();
        void handleNewClusterStateFromMaster(ClusterState newClusterState, final PublishClusterStateAction.NewClusterStateListener.NewStateProcessed newStateProcessed) {
            final ClusterName incomingClusterName = newClusterState.getClusterName();
            // The cluster name can still be null if the state comes from a node that is prev 1.1.1
            if (incomingClusterName != null && !incomingClusterName.equals(this.clusterName)) {
                logger.warn("received cluster state from [{}] which is also master but with a different cluster name [{}]", newClusterState.nodes().masterNode(), incomingClusterName);
                newStateProcessed.onNewClusterStateFailed(new ElasticsearchIllegalStateException("received state from a node that is not part of the cluster"));
                return;
            }
            
            logger.debug("received cluster state from [{}] which is also master with cluster name [{}]", newClusterState.nodes().localNodeId(), incomingClusterName);
            final ClusterState newState = newClusterState;
            final ClusterService clusterService = this.clusterService;
            clusterService.submitStateUpdateTask("cassandra-disco-receive_cluster_state_from_another_node [" + newState.nodes().localNodeId() + "]", Priority.URGENT, new ProcessedClusterStateUpdateTask() {
                @Override
                public ClusterState execute(ClusterState currentState) {
                // TODO: should merge fresher information according to upper versions... 
                ClusterState mergedClusterState = new ClusterState.Builder(currentState)
                .version(newState.version())
                .blocks(newState.blocks())          
                      .metaData(newState.metaData()).build();
                   logger.debug("mergedClusterState={}", mergedClusterState.prettyPrint());
                   
                   // allocate new indices in the routing table.
                   ClusterState newClusterState = new ClusterState.Builder(mergedClusterState)
                        .routingTable(RoutingTable.builder(clusterService, mergedClusterState)).build();
                   
                   logger.debug("newClusterState={}", newClusterState.prettyPrint());   
                    return newClusterState;
                }
                @Override
                public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                    newStateProcessed.onNewClusterStateProcessed();
                }
                @Override
                public void onFailure(String source, Throwable t) {
                    logger.error("unexpected failure during [{}]", t, source);
                    newStateProcessed.onNewClusterStateFailed(t);
                }
                
                @Override
                public boolean doPublish() {
                   // do not propagate cluster state we've received from another node !
                   return false;
                }
            });
            
        }
       */

}
