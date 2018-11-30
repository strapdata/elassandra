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

import com.google.common.net.InetAddresses;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.EndpointState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.gms.IEndpointStateChangeSubscriber;
import org.apache.cassandra.gms.VersionedValue;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.Event;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.elassandra.ConcurrentMetaDataUpdateException;
import org.elassandra.PaxosMetaDataUpdateException;
import org.elassandra.gateway.CassandraGatewayService;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateTaskConfig;
import org.elasticsearch.cluster.ClusterStateTaskConfig.SchemaUpdate;
import org.elasticsearch.cluster.ClusterStateTaskListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.node.DiscoveryNode.Role;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.service.ClusterApplier;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoverySettings;
import org.elasticsearch.discovery.DiscoveryStats;
import org.elasticsearch.node.NodeClosedException;
import org.elasticsearch.transport.TransportService;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.cassandra.cql3.QueryProcessor.executeInternal;
import static org.elasticsearch.gateway.GatewayService.STATE_NOT_RECOVERED_BLOCK;

/**
 * Discover the cluster topology from cassandra snitch and settings, mappings, blocks from the elastic_admin keyspace.
 * Publishing is just a notification to refresh in memory configuration from the cassandra table.
 * @author vroyer
 *
 */
public class CassandraDiscovery extends AbstractLifecycleComponent implements Discovery, IEndpointStateChangeSubscriber {
    private static final EnumSet CASSANDRA_ROLES = EnumSet.of(Role.MASTER,Role.DATA);
    private final TransportService transportService;

    private final ClusterService clusterService;
    private final ClusterApplier clusterApplier;
    private volatile ClusterState clusterState;

    private final ClusterName clusterName;
    private final DiscoverySettings discoverySettings;
    private final NamedWriteableRegistry namedWriteableRegistry;

    private final AtomicReference<MetaDataVersionAckListener> metaDataVersionAckListenerRef = new AtomicReference<>(null);
    private final AtomicLong maxMetaDataVersion = new AtomicLong(-1);

    private final ClusterGroup clusterGroup;

    private final InetAddress localAddress;
    private final String localDc;

    private final ConcurrentMap<String, ShardRoutingState> localShardStateMap = new ConcurrentHashMap<String, ShardRoutingState>();
    private final ConcurrentMap<UUID, Map<String,ShardRoutingState>> remoteShardRoutingStateMap = new ConcurrentHashMap<UUID, Map<String,ShardRoutingState>>();

    /**
     * When searchEnabled=true, local shards are visible for routing, otherwise, local shards are seen as UNASSIGNED.
     * This allows to gracefully shutdown or start the node for maintenance like an offline repair or rebuild_index.
     */
    private final AtomicBoolean searchEnabled = new AtomicBoolean(false);

    /**
     * If autoEnableSearch=true, search is automatically enabled when the node becomes ready to operate, otherwise, searchEnabled should be manually set to true.
     */
    private final AtomicBoolean autoEnableSearch = new AtomicBoolean(System.getProperty("es.auto_enable_search") == null || Boolean.getBoolean("es.auto_enable_search"));


    public CassandraDiscovery(Settings settings,
            TransportService transportService,
            final ClusterService clusterService,
            final ClusterApplier clusterApplier,
            NamedWriteableRegistry namedWriteableRegistry) {
        super(settings);
        this.clusterApplier = clusterApplier;
        this.clusterService = clusterService;
        this.discoverySettings = new DiscoverySettings(settings, clusterService.getClusterSettings());
        this.namedWriteableRegistry = namedWriteableRegistry;
        this.transportService = transportService;
        this.clusterName = clusterService.getClusterName();

        this.clusterService.setDiscovery(this);
        this.clusterService.getMasterService().setClusterStateSupplier(() -> clusterState);
        this.clusterService.getMasterService().setClusterStatePublisher(this::publish);

        this.localAddress = FBUtilities.getBroadcastAddress();
        this.localDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(FBUtilities.getBroadcastAddress());

        this.clusterGroup = new ClusterGroup();
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
        Gossiper.instance.register(this);
        synchronized (clusterGroup) {
            logger.debug("Connected to cluster [{}]", clusterName.value());
            clusterGroup.put(localNode().getId(), localNode());
            logger.info("localNode name={} id={} localAddress={} publish_host={}", localNode().getName(), localNode().getId(), localAddress, localNode().getAddress());

            // initialize cluster from cassandra local token map
            for(InetAddress endpoint : StorageService.instance.getTokenMetadata().getAllEndpoints()) {
                if (!this.localAddress.equals(endpoint) && this.localDc.equals(DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint))) {
                    String hostId = StorageService.instance.getHostId(endpoint).toString();
                    UntypedResultSet rs = executeInternal("SELECT preferred_ip, rpc_address from system." + SystemKeyspace.PEERS+" WHERE peer = ?", endpoint);
                    if (!rs.isEmpty()) {
                        UntypedResultSet.Row row = rs.one();
                        EndpointState epState = Gossiper.instance.getEndpointStateForEndpoint(endpoint);
                       clusterGroup.update(epState, hostId, endpoint,
                                row.has("preferred_ip") ? row.getInetAddress("preferred_ip") : endpoint,
                                row.has("rpc_address") ? row.getInetAddress("rpc_address") : null);
                    }
                }
            }
        }
        updateClusterGroupsFromGossiper();

        // Cassandra is usually in the NORMAL state when discovery start.
        if (isNormal(Gossiper.instance.getEndpointStateForEndpoint(this.localAddress)) && isAutoEnableSearch()) {
            try {
                this.setSearchEnabled(true);
            } catch (IOException e) {
                logger.error("Failed to set searchEnabled",e);
            }
        }

        publishX2(clusterState);
        updateRoutingTable("starting-cassandra-discovery", true);
    }

    public ClusterState initClusterState(DiscoveryNode localNode) {
        ClusterState.Builder builder = clusterApplier.newClusterStateBuilder();
        this.clusterState = builder.nodes(DiscoveryNodes.builder().add(localNode)
                .localNodeId(localNode.getId())
                .masterNodeId(localNode.getId())
                .build())
            .blocks(ClusterBlocks.builder()
                .addGlobalBlock(STATE_NOT_RECOVERED_BLOCK)
                .addGlobalBlock(CassandraGatewayService.NO_CASSANDRA_RING_BLOCK))
            .build();
        this.clusterApplier.setInitialState(this.clusterState);
        return this.clusterState;
    }

    private void updateRoutingTable(String source, boolean nodesUpdate) {
        clusterService.submitStateUpdateTask(source, new ClusterStateUpdateTask() {

            @Override
            public ClusterState execute(ClusterState currentState) {
                ClusterState.Builder clusterStateBuilder = ClusterState.builder(currentState);

                DiscoveryNodes discoverNodes = nodes();
                if (nodesUpdate)
                    clusterStateBuilder.nodes(discoverNodes);

                if (currentState.nodes().getSize() != discoverNodes.getSize()) {
                    // update numberOfShards for all indices.
                    MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
                    for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
                        IndexMetaData indexMetaData = it.next();
                        IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
                        indexMetaDataBuilder.numberOfShards(discoverNodes.getSize());
                        metaDataBuilder.put(indexMetaDataBuilder.build(), false);
                    }
                    clusterStateBuilder.metaData(metaDataBuilder.build());
                }
                return clusterStateBuilder.incrementVersion().build();
            }

            @Override
            public void onFailure(String source, Exception t) {
                logger.error("unexpected failure during [{}]", t, source);
            }

        });
    }


    /**
     * Update cluster group members from cassandra topology (should only be triggered by IEndpointStateChangeSubscriber events).
     * This should trigger re-sharding of index for new nodes (when token distribution change).
     */
    private void updateClusterGroupsFromGossiper() {
        for (Entry<InetAddress, EndpointState> entry : Gossiper.instance.getEndpointStates()) {
            EndpointState epState = entry.getValue();
            InetAddress   endpoint = entry.getKey();

            if (!epState.getStatus().equals(VersionedValue.STATUS_NORMAL) && !epState.getStatus().equals(VersionedValue.SHUTDOWN)) {
                logger.info("Ignoring node state={}", epState);
                continue;
            }

            if (isLocal(endpoint)) {
                VersionedValue vv = epState.getApplicationState(ApplicationState.HOST_ID);
                if (vv != null) {
                    String hostId = vv.value;
                    if (!this.localNode().getId().equals(hostId)) {
                        clusterGroup.update(epState, hostId, endpoint, getInternalIp(epState), getRpcAddress(epState));
                    }

                    // initialize the remoteShardRoutingStateMap from gossip states
                    if (epState.getApplicationState(ApplicationState.X1) != null) {
                        VersionedValue x1 = epState.getApplicationState(ApplicationState.X1);
                        if (!this.localNode().getId().equals(hostId)) {
                            Map<String, ShardRoutingState> shardsStateMap;
                            try {
                                shardsStateMap = jsonMapper.readValue(x1.value, indexShardStateTypeReference);
                                this.remoteShardRoutingStateMap.put(Gossiper.instance.getHostId(endpoint), shardsStateMap);
                            } catch (IOException e) {
                                logger.error("Failed to parse X1 for node [{}]", hostId);
                            }
                        }
                    }
                }
            }
        }
        updateRoutingTable("discovery-refresh", true);
    }

    private long getMetadataVersion(VersionedValue versionValue) {
        int i = versionValue.value.indexOf('/');
        if (i > 0) {
            try {
                return Long.valueOf(versionValue.value.substring(i+1));
            } catch (NumberFormatException e) {
                logger.error("Unexpected gossip.X2 value "+versionValue.value, e);
            }
        }
        return -1;
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

    public boolean updateNode(InetAddress endpoint, EndpointState epState) {
        if (isLocal(endpoint)) {
            UUID hostUuid = (epState.getApplicationState(ApplicationState.HOST_ID) == null) ?
                    StorageService.instance.getHostId(endpoint) :
                    UUID.fromString(epState.getApplicationState(ApplicationState.HOST_ID) .value);

            boolean updatedNode = clusterGroup.update(epState, hostUuid.toString(), endpoint, getInternalIp(epState), getRpcAddress(epState));

            // update remote shard routing view.
            DiscoveryNodeStatus newStatus = discoveryNodeStatus(epState);
            switch(newStatus) {
            case ALIVE:
                VersionedValue x1 = epState.getApplicationState(ApplicationState.X1);
                if (x1 != null) {
                    try {
                        Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(x1.value, indexShardStateTypeReference);
                        this.remoteShardRoutingStateMap.put(hostUuid, shardsStateMap);
                    } catch (IOException e) {
                        logger.error("Failed to parse X1 for node=[{}]", hostUuid);
                    }
                }
                break;
            default:
                this.remoteShardRoutingStateMap.remove(hostUuid);
            }

            if (updatedNode) {
                updateRoutingTable("update-node-" + NetworkAddress.format(endpoint)+"-"+newStatus.toString(), true);
                return true;
            }
        }
        return false;
    }

    public Optional<CassandraDiscovery.MetaDataVersionAckListener> getMetaDataVersionAckListener() {
        return Optional.ofNullable(metaDataVersionAckListenerRef.get());
    }

    public MetaDataVersionAckListener newMetaDataVersionAckListener(long version, Discovery.AckListener ackListener, ClusterState clusterState) {
        MetaDataVersionAckListener prevListener = metaDataVersionAckListenerRef.getAndSet(new MetaDataVersionAckListener(version, ackListener, clusterState));
        assert prevListener == null : "metaDataVersionAckListener should be null";
        return metaDataVersionAckListenerRef.get();
    }

    public class MetaDataVersionAckListener implements Closeable  {
        final long expectedVersion;
        final Discovery.AckListener ackListener;
        final ClusterState clusterState;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final ConcurrentMap<String, DiscoveryNode> attendees;

        private MetaDataVersionAckListener(long version, Discovery.AckListener ackListener, ClusterState clusterState) {
            this.expectedVersion = version;
            this.ackListener = ackListener;
            this.clusterState = clusterState;
            this.attendees = new ConcurrentHashMap<String, DiscoveryNode>(clusterState.nodes().getSize());

            clusterState.nodes().forEach((n) -> {
                if (!localNode().getId().equals(n.getId()) &&
                    n.status() == DiscoveryNodeStatus.ALIVE &&
                    isNormal(n))
                    attendees.put(n.getId(), n);
                });
            logger.debug("new MetaDataVersionAckListener version={} attendees={}", expectedVersion, this.attendees.keySet());
            if (attendees.size() == 0)
                countDownLatch.countDown();
        }

        public long version() {
            return this.expectedVersion;
        }

        public Discovery.AckListener ackListener() {
            return this.ackListener;
        }

        // called by the clusterService thread, block until remote nodes have applied a metadata younger than expectedVersion.
        // unlocked be the C* gossiper thread calling checkMetaDataVersion().
        public boolean await(long timeout, TimeUnit unit) {
            boolean done = false;
            try {
               logger.debug("MetaDataVersionAckListener awaiting metadata={} from {} nodes", this.clusterState.metaData(), countDownLatch.getCount());
               done = countDownLatch.await(timeout, unit);
            } catch (InterruptedException e) {

            }
            logger.debug("MetaDataVersionAckListener version={} {}", expectedVersion, done ? "applied by all alive nodes" : "timeout");
            metaDataVersionAckListenerRef.set(null);
            return done;
        }

        public void close() {
            logger.debug("MetaDataVersionAckListener abort on metadata={}", this.clusterState.metaData());
            metaDataVersionAckListenerRef.set(null);
        }

        /**
         * Release the listener when all attendees have reached the expected version or become down.
         * Called by the cassandra gossiper thread from onChange() or onDead() or onRemove().
         */
        public void notify(EndpointState endPointState) {
            VersionedValue hostIdValue = endPointState.getApplicationState(ApplicationState.HOST_ID);
            if (hostIdValue == null)
                return; // happen when we are removing a node while updating the mapping

            String hostId = hostIdValue.value;
            if (hostId == null || localNode().getId().equals(hostId) || !attendees.containsKey(hostId))
                return;

            if (!endPointState.isAlive() || !endPointState.getStatus().equals("NORMAL")) {
                // node was removed from the gossiper, down or leaving, acknowledge to avoid locking.
                DiscoveryNode node = attendees.remove(hostId);
                if (node != null) {
                    logger.debug("nack node={} version={} remaining attendees={}", node.getId(), version(), attendees.keySet());
                    ackListener.onNodeAck(node, new NodeClosedException(node));
                }
            } else {
                VersionedValue vv = endPointState.getApplicationState(ApplicationState.X2);
                if (vv != null && getMetadataVersion(vv) >= version()) {
                    DiscoveryNode node = attendees.remove(hostId);
                    if (node != null) {
                        // acknowledge node having the right metadata version number.
                        logger.debug("ack node={} version={} remaining attendees={}", node.getId(), version(), attendees.keySet());
                        ackListener.onNodeAck(node, null);
                    }
                }
            }
            if (attendees.size() == 0)
                countDownLatch.countDown();
        }
    }

    private boolean isLocal(InetAddress endpoint) {
        return DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint).equals(localDc);
    }

    private boolean isMember(InetAddress endpoint) {
        return !this.localAddress.equals(endpoint) && DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint).equals(localDc);
    }

    /**
     * #183 lookup EndpointState with the node name = cassandra broadcast address.
     * ES RPC adress can be different from the cassandra broadcast address.
     */
    private boolean isNormal(DiscoveryNode node) {
        // endpoint address = C* broadcast address = Elasticsearch node name (transport may be bound to C* internal or C* RPC broadcast)
        EndpointState state = Gossiper.instance.getEndpointStateForEndpoint(InetAddresses.forString(node.getName()));
        if (state == null) {
            logger.warn("Node endpoint address=[{}] name=[{}] state not found", node.getInetAddress(), node.getName());
            return false;
        }
        return state.isAlive() && state.getStatus().equals(VersionedValue.STATUS_NORMAL);
    }

    private boolean isNormal(EndpointState state) {
        return state != null && state.isAlive() && state.getStatus().equals(VersionedValue.STATUS_NORMAL);
    }

    public static InetAddress getInternalIp(EndpointState epState) {
        return epState.getApplicationState(ApplicationState.INTERNAL_IP) == null ? null :
                InetAddresses.forString(epState.getApplicationState(ApplicationState.INTERNAL_IP).value);
    }

    public static InetAddress getRpcAddress(EndpointState epState) {
        return epState.getApplicationState(ApplicationState.RPC_ADDRESS) == null ? null :
                InetAddresses.forString(epState.getApplicationState(ApplicationState.RPC_ADDRESS).value);
    }

    @Override
    public void beforeChange(InetAddress endpoint, EndpointState state, ApplicationState appState, VersionedValue value) {
        //logger.debug("beforeChange Endpoint={} EndpointState={}  ApplicationState={} value={}", endpoint, state, appState, value);
    }

    @Override
    public void onChange(InetAddress endpoint, ApplicationState state, VersionedValue versionValue) {
        EndpointState epState = Gossiper.instance.getEndpointStateForEndpoint(endpoint);
        if (isMember(endpoint)) {
            if (logger.isTraceEnabled())
                logger.trace("Endpoint={} ApplicationState={} value={}", endpoint, state, versionValue);

            switch (state) {
            case STATUS:
                if (isNormal(epState)) {
                    updateNode(endpoint, epState);
                } else {
                    // node probably down, notify metaDataVersionAckListener..
                    getMetaDataVersionAckListener().ifPresent(listener -> listener.notify(Gossiper.instance.getEndpointStateForEndpoint(endpoint)));
                }
                break;

            case X1:
                try {
                    // update the remoteShardRoutingStateMap to build ES routing table for joined-normal nodes only.
                    if (clusterGroup.contains(epState.getApplicationState(ApplicationState.HOST_ID).value)) {
                        if (logger.isTraceEnabled())
                            logger.trace("Endpoint={} X1={} => updating routing table", endpoint, versionValue);

                        Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(versionValue.value, indexShardStateTypeReference);
                        this.remoteShardRoutingStateMap.put(Gossiper.instance.getHostId(endpoint), shardsStateMap);
                        updateRoutingTable("X1-" + endpoint, false);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse gossip index shard state", e);
                }
                break;

            case X2:
            case INTERNAL_IP: // manage address replacement from a remote node
            case RPC_ADDRESS:
                updateNode(endpoint, epState);
                break;

            }
        }

        // metadata version update from a datacenter sharing our cluster state.
        if (state == ApplicationState.X2 && !this.localAddress.equals(endpoint) && clusterService.isDatacenterGroupMember(endpoint)) {
            getMetaDataVersionAckListener().ifPresent(listener -> listener.notify(epState));
        }

        // self status update.
        if (this.localAddress.equals(endpoint)) {
            switch (state) {
            case STATUS:
                if (logger.isTraceEnabled())
                    logger.trace("Endpoint={} STATUS={} => may update searchEnabled", endpoint, versionValue);


                // update searchEnabled according to the node status and autoEnableSearch.
                if (isNormal(Gossiper.instance.getEndpointStateForEndpoint(endpoint))) {
                    if (!this.searchEnabled.get() && this.autoEnableSearch.get()) {
                        try {
                            setSearchEnabled(true, true);
                        } catch (IOException e) {
                            logger.error("Failed to enable search",e);
                        }
                    }
                    publishX2(this.clusterState, true);
                 } else {
                    // node is leaving or whatever, disabling search.
                    if (this.searchEnabled.get()) {
                        try {
                            setSearchEnabled(false, true);
                        } catch (IOException e) {
                            logger.error("Failed to disable search",e);
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * Warning: IEndpointStateChangeSubscriber.onXXXX should not block (on connection timeout or clusterState update) to avoid gossip issues.
     */

    @Override
    public void onAlive(InetAddress endpoint, EndpointState epState) {
        if (isMember(endpoint)) {
            logger.debug("Endpoint={} ApplicationState={} isAlive={} => update node + connecting", endpoint, epState, epState.isAlive());
            if (isNormal(epState))
                updateNode(endpoint, epState);
        }
    }

    @Override
    public void onDead(InetAddress endpoint, EndpointState epState) {
        if (isMember(endpoint)) {
            logger.debug("Endpoint={}  ApplicationState={} isAlive={} => update node + disconnecting", endpoint, epState, epState.isAlive());
            getMetaDataVersionAckListener().ifPresent(listener -> listener.notify(Gossiper.instance.getEndpointStateForEndpoint(endpoint)));
            updateNode(endpoint, epState);
        }
    }

    @Override
    public void onRestart(InetAddress endpoint, EndpointState epState) {
        if (isMember(endpoint)) {
            if (logger.isTraceEnabled())
                logger.debug("Endpoint={}  ApplicationState={} isAlive={} status={}", endpoint, epState, epState.isAlive(), epState.getStatus());
            if (isNormal(epState))
                updateNode(endpoint, epState);
        }
    }

    @Override
    public void onJoin(InetAddress endpoint, EndpointState epState) {
        if (isLocal(endpoint)) {
            if (logger.isTraceEnabled())
                logger.trace("Endpoint={} ApplicationState={} isAlive={} status={}", endpoint, epState, epState.isAlive(), epState.getStatus());
            if (isNormal(epState))
                updateNode(endpoint, epState);
        }
    }

    @Override
    public void onRemove(InetAddress endpoint) {
        if (this.localAddress.equals(endpoint)) {
            try {
                setSearchEnabled(false);
            } catch (IOException e) {
            }
        } else if (isMember(endpoint)) {
            DiscoveryNode removedNode = this.nodes().findByInetAddress(endpoint);
            if (removedNode != null  && !this.localNode().getId().equals(removedNode.getId())) {
                logger.warn("Removing node ip={} node={}  => disconnecting", endpoint, removedNode);
                getMetaDataVersionAckListener().ifPresent(listener -> listener.notify(Gossiper.instance.getEndpointStateForEndpoint(endpoint)));
                this.clusterGroup.remove(removedNode.getId());
                updateRoutingTable("node-removed-"+endpoint, true);
            }
        }
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        Gossiper.instance.unregister(this);

        synchronized (clusterGroup) {
            clusterGroup.members.clear();
        }
    }

    private static final ApplicationState ELASTIC_SHARDS_STATES = ApplicationState.X1;
    private static final ApplicationState ELASTIC_META_DATA = ApplicationState.X2;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final TypeReference<Map<String, ShardRoutingState>> indexShardStateTypeReference = new TypeReference<Map<String, ShardRoutingState>>() {};

    public Map<String,ShardRoutingState> getShardRoutingState(UUID nodeUuid) {
        return remoteShardRoutingStateMap.get(nodeUuid);
    }

    public void publishShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        final ShardRoutingState prevShardRoutingState;
        if (shardRoutingState == null) {
            prevShardRoutingState = localShardStateMap.remove(index);
        } else {
            prevShardRoutingState = localShardStateMap.put(index, shardRoutingState);
        }
        if (shardRoutingState != prevShardRoutingState)
            publishX1();
    }

    public ShardRoutingState getShardRoutingState(final String index) {
        return this.localShardStateMap.get(index);
    }

    public boolean isSearchEnabled() {
        return this.searchEnabled.get();
    }

    public boolean isAutoEnableSearch() {
        return this.autoEnableSearch.get();
    }

    public void setAutoEnableSearch(boolean newValue) {
       this.autoEnableSearch.set(newValue);
    }

    // warning: called from the gossiper.
    public void setSearchEnabled(boolean ready) throws IOException {
        setSearchEnabled(ready, false);
    }

    public void setSearchEnabled(boolean ready, boolean forcePublishX1) throws IOException {
        if (ready && !isNormal(Gossiper.instance.getEndpointStateForEndpoint(this.localAddress))) {
            throw new IOException("Cassandra not ready for search");
        }
        if (searchEnabled.getAndSet(ready) != ready || forcePublishX1) {
            logger.info("searchEnabled set to [{}]", ready);
            publishX1(forcePublishX1);
            updateRoutingTable("searchEnabled changed to "+ready, false);
        }
    }

    public void publishX1() throws JsonGenerationException, JsonMappingException, IOException {
        publishX1(false);
    }

    // Warning: on nodetool enablegossip, Gossiper.instance.isEnable() may be false while receiving a onChange event !
    private void publishX1(boolean force) throws JsonGenerationException, JsonMappingException, IOException {
        if (Gossiper.instance.isEnabled() || force) {
            if (searchEnabled.get()) {
                String newValue = jsonMapper.writerWithType(indexShardStateTypeReference).writeValueAsString(localShardStateMap);
                Gossiper.instance.addLocalApplicationState(ELASTIC_SHARDS_STATES, StorageService.instance.valueFactory.datacenter(newValue));
            } else {
                // publish an empty map, so other nodes will see local shards UNASSIGNED.
                Gossiper.instance.addLocalApplicationState(ELASTIC_SHARDS_STATES, StorageService.instance.valueFactory.datacenter("{}"));
            }
        }
    }

    public void publishX2(ClusterState clusterState) {
        publishX2(clusterState, false);
    }

    public void publishX2(ClusterState clusterState, boolean force) {
        String clusterStateSting = clusterState.metaData().clusterUUID() + '/' + clusterState.metaData().version();
        if (Gossiper.instance.isEnabled() || force) {
            Gossiper.instance.addLocalApplicationState(ELASTIC_META_DATA, StorageService.instance.valueFactory.datacenter(clusterStateSting));
            if (logger.isTraceEnabled())
                logger.trace("X2={} published in gossip state", clusterStateSting);
        }
    }

    @Override
    protected void doClose()  {
        Gossiper.instance.unregister(this);
    }

    public DiscoveryNode localNode() {
        return this.transportService.getLocalNode();
    }


    public String nodeDescription() {
        return clusterName.value() + "/" + localNode().getId();
    }

    public DiscoveryNodes nodes() {
        return this.clusterGroup.nodes();
    }

    public DiscoveryNodeStatus discoveryNodeStatus(final EndpointState epState) {
        if (epState == null) {
            return DiscoveryNodeStatus.DEAD;
        }
        if (epState.getApplicationState(ApplicationState.X2) == null) {
            return DiscoveryNodeStatus.DISABLED;
        }
        if (VersionedValue.STATUS_NORMAL.equals(epState.getStatus())) {
            return DiscoveryNodeStatus.ALIVE;
        }
        return DiscoveryNodeStatus.DEAD;
    }

    private class ClusterGroup {

        private ConcurrentMap<String, DiscoveryNode> members = new ConcurrentHashMap();

        Map<String, DiscoveryNode> members() {
            return members;
        }

        public DiscoveryNode put(String id, DiscoveryNode node) {
            return members.put(id, node);
        }

        /**
         * Put or update discovery node if needed
         * @param hostId
         * @param endpoint
         * @param internalIp = endpoint address or prefered_ip
         * @param rpcAddress
         * @param status
         * @return true if updated
         */
        public synchronized boolean update(final EndpointState epState, final String hostId, final InetAddress endpoint, final InetAddress internalIp, final InetAddress rpcAddress) {
            if (localNode().getId().equals(hostId)) {
                // ignore GOSSIP update related to our self node.
                logger.debug("Ignoring GOSSIP update for node id={} ip={} because it's mine", hostId, endpoint);
                return false;
            }

            DiscoveryNodeStatus status = discoveryNodeStatus(epState);
            DiscoveryNode dn = clusterGroup.get(hostId);
            if (dn == null) {
                Map<String, String> attrs =  new HashMap<>();
                attrs.put("dc", localDc);
                attrs.put("rack", DatabaseDescriptor.getEndpointSnitch().getRack(endpoint));
                dn = new DiscoveryNode(buildNodeName(endpoint),
                        hostId,
                        new TransportAddress(Boolean.getBoolean("es.use_internal_address") ? internalIp : rpcAddress, publishPort()),
                        attrs,
                        CASSANDRA_ROLES,
                        Version.CURRENT,
                        status);
                members.put(hostId, dn);
                logger.debug("Add node host_id={} endpoint={} internal_ip={}, rpc_address={}, status={}",
                        hostId, NetworkAddress.format(endpoint), NetworkAddress.format(internalIp), NetworkAddress.format(rpcAddress), status);
                return true;
            } else {
                if (!dn.getName().equals(buildNodeName(endpoint)) || !dn.getInetAddress().equals(Boolean.getBoolean("es.use_internal_address") ? internalIp : rpcAddress)) {
                    if (status.equals(DiscoveryNodeStatus.ALIVE)) {
                        DiscoveryNode dn2 = new DiscoveryNode(buildNodeName(endpoint),
                                hostId,
                                new TransportAddress(Boolean.getBoolean("es.use_internal_address") ? internalIp : rpcAddress, publishPort()),
                                dn.getAttributes(),
                                CASSANDRA_ROLES,
                                Version.CURRENT,
                                status);
                        members.replace(hostId, dn, dn2);
                        logger.debug("Update node host_id={} endpoint={} internal_ip={}, rpc_address={}, status={}",
                                hostId, NetworkAddress.format(endpoint), NetworkAddress.format(internalIp), NetworkAddress.format(rpcAddress), status);
                        return true;
                    } else {
                        logger.debug("Ignoring node host_id={} endpoint={} internal_ip={}, rpc_address={}, status={}",
                                hostId, NetworkAddress.format(endpoint), NetworkAddress.format(internalIp), NetworkAddress.format(rpcAddress), status);
                        return false;
                    }
                } else if (!dn.getStatus().equals(status)) {
                    dn.status(status);
                    logger.debug("Update node host_id={} endpoint={} internal_ip={} rpc_address={}, status={}",
                            hostId, NetworkAddress.format(endpoint), NetworkAddress.format(internalIp), NetworkAddress.format(rpcAddress), status);
                    return true;
                }
            }
            return false;
        }

        public DiscoveryNode remove(String id) {
            remoteShardRoutingStateMap.remove(id);
            return members.remove(id);
        }

        public DiscoveryNode get(String id) {
            return members.get(id);
        }

        public boolean contains(String id) {
            return members.containsKey(id);
        }

        public Collection<DiscoveryNode> values() {
            return members.values();
        }

        public DiscoveryNodes nodes() {
            DiscoveryNodes.Builder nodesBuilder = new DiscoveryNodes.Builder();
            nodesBuilder.localNodeId(SystemKeyspace.getLocalHostId().toString()).masterNodeId(SystemKeyspace.getLocalHostId().toString());
            for (DiscoveryNode node : members.values()) {
                nodesBuilder.add(node);
            }
            return nodesBuilder.build();
        }
    }

    @Override
    public void startInitialJoin() {
        // TODO Auto-generated method stub
    }

    @Override
    public synchronized void publish(ClusterChangedEvent clusterChangedEvent, Discovery.AckListener ackListener) {
        ClusterState previousClusterState = clusterChangedEvent.previousState();
        ClusterState newClusterState = clusterChangedEvent.state();

        long startTimeNS = System.nanoTime();
        SchemaUpdate schemaUpdate = SchemaUpdate.NO_UPDATE;
        try {
            String newClusterStateMetaDataString = MetaData.Builder.toXContent(newClusterState.metaData(), MetaData.CASSANDRA_FORMAT_PARAMS);
            String previousClusterStateMetaDataString = MetaData.Builder.toXContent(previousClusterState.metaData(), MetaData.CASSANDRA_FORMAT_PARAMS);
            if (!newClusterStateMetaDataString.equals(previousClusterStateMetaDataString) && !newClusterState.blocks().disableStatePersistence() && clusterChangedEvent.schemaUpdate().update()) {
                // update MeteData.version+cluster_uuid
                schemaUpdate = clusterChangedEvent.schemaUpdate();

                newClusterState = ClusterState.builder(newClusterState)
                                    .metaData(MetaData.builder(newClusterState.metaData()).incrementVersion().build())
                                    .incrementVersion()
                                    .build();

                Collection<Mutation> mutations = clusterChangedEvent.mutations() == null ? new ArrayList<>() : clusterChangedEvent.mutations();
                Collection<Event.SchemaChange> events = clusterChangedEvent.events() == null ? new ArrayList<>() : clusterChangedEvent.events();

                try {
                    // TODO: track change to update CQL schema when really needed
                    clusterService.convertMetadataToSchemaMutations(newClusterState.metaData(), mutations, events);
                } catch (ConfigurationException | IOException e1) {
                    throw new RuntimeException(e1);
                }

                // try to persist new metadata in cassandra.
                if (schemaUpdate.update() && newClusterState.nodes().getSize() > 1) {
                    // register the X2 listener before publishing to avoid dead locks !
                    newMetaDataVersionAckListener(newClusterState.metaData().version(), ackListener, newClusterState);
                }
                try {
                    // PAXOS schema update commit
                    clusterService.commitMetaData(previousClusterState.metaData(), newClusterState.metaData(), clusterChangedEvent.source());

                    // apply new CQL schema
                    if (mutations != null && mutations.size() > 0) {
                        logger.debug("Applying CQL schema mutations={}", mutations);

                        // unless update is UPDATE_ASYNCHRONOUS, block until schema is applied.
                        Future<?> future = MigrationManager.announce(mutations);
                        if (!SchemaUpdate.UPDATE_ASYNCHRONOUS.equals(clusterChangedEvent.schemaUpdate()))
                            FBUtilities.waitOnFuture(future);

                        // build routing table when keyspaces are created locally
                        newClusterState = ClusterState.builder(newClusterState)
                                .routingTable(RoutingTable.build(this.clusterService, newClusterState))
                                .build();
                        logger.info("CQL SchemaChanges={}", events);
                    } else {
                        logger.debug("No CQL mutation to apply");
                    }

                    publishX2(newClusterState);
                } catch (ConcurrentMetaDataUpdateException e) {
                    // should replay the task later when current cluster state will match the expected metadata uuid and version
                    logger.warn("PAXOS schema update failed because schema has changed, will resubmit task when metadata.version > {}", previousClusterState.metaData().version());
                    final long resubmitTimeMillis = System.currentTimeMillis();
                    clusterService.addListener(new ClusterStateListener() {
                        @Override
                        public void clusterChanged(ClusterChangedEvent event) {
                            if (event.metaDataChanged()) {
                                final long lostTimeMillis = System.currentTimeMillis() - resubmitTimeMillis;
                                Priority priority = Priority.URGENT;
                                TimeValue timeout = TimeValue.timeValueSeconds(30*1000 - lostTimeMillis);
                                ClusterStateTaskConfig config;
                                Map<Object, ClusterStateTaskListener> map = clusterChangedEvent.taskInputs().updateTasksToMap(priority, lostTimeMillis);
                                logger.warn("metadata={} => resubmit delayed update source={} tasks={} priority={} remaing timeout={}",
                                        clusterState.metaData(), clusterChangedEvent.source(), clusterChangedEvent.taskInputs().updateTasks, priority, timeout);
                                clusterService.submitStateUpdateTasks(clusterChangedEvent.source(), map, ClusterStateTaskConfig.build(priority, timeout), clusterChangedEvent.taskInputs().executor);
                                clusterService.removeListener(this); // replay only once.
                            }
                        }
                    });
                    return;
                } catch(UnavailableException e) {
                    logger.warn("PAXOS schema update failed:", e);
                    throw new PaxosMetaDataUpdateException(e);
                }

                // wait for X2 acknowledgment on coordinator node only
                if (schemaUpdate.update() && newClusterState.nodes().getSize() > 1) {
                    try {
                        getMetaDataVersionAckListener().ifPresent(listener -> listener.await(30L, TimeUnit.SECONDS));
                    } catch (Throwable e) {
                        final long version = newClusterState.metaData().version();
                        logger.error((Supplier<?>) () -> new ParameterizedMessage("Interruped while waiting MetaData.version = {}", version), e);
                    }
                }
            }
        } catch (PaxosMetaDataUpdateException e) {
            throw e;
        } catch (Exception e) {
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder("failed to execute cluster state update in ").append(executionTime)
                        .append(", state:\nversion [")
                        .append(previousClusterState.version()).
                        append("], source [").append(clusterChangedEvent.source()).append("]\n");
                logger.warn(sb.toString(), e);
                // TODO resubmit task on next cluster state change
            } else {
                logger.error("Cassandra issue:", e);
            }
            throw new ElasticsearchException(e);
            //throw new Discovery.FailedToCommitClusterStateException("Failed to commit metadata", e);
        } finally {
            getMetaDataVersionAckListener().ifPresent(listener -> listener.close());
        }

        // apply new cluster state
        this.clusterState = newClusterState;
        ClusterStateTaskListener listener = new ClusterStateTaskListener() {
            @Override
            public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                ackListener.onNodeAck(transportService.getLocalNode(), null);
            }

            @Override
            public void onFailure(String source, Exception e) {
                ackListener.onNodeAck(transportService.getLocalNode(), e);
                logger.warn(
                    (org.apache.logging.log4j.util.Supplier<?>) () -> new ParameterizedMessage(
                        "failed while applying cluster state locally [{}]",
                        clusterChangedEvent.source()),
                    e);
            }
        };
        clusterApplier.onNewClusterState("apply-locally-on-node[" + clusterChangedEvent.source() + "]", () -> this.clusterState, listener);
    }

    @Override
    public DiscoveryStats stats() {
        return null;
    }

    @Override
    public DiscoverySettings getDiscoverySettings() {
        return this.discoverySettings;
    }

}
