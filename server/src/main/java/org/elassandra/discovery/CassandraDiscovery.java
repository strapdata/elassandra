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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.exceptions.WriteTimeoutException;
import org.apache.cassandra.gms.ApplicationState;
import org.apache.cassandra.gms.EndpointState;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.gms.IEndpointStateChangeSubscriber;
import org.apache.cassandra.gms.VersionedValue;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.Event;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.logging.log4j.Logger;
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
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateTaskConfig;
import org.elasticsearch.cluster.ClusterStateTaskConfig.SchemaUpdate;
import org.elasticsearch.cluster.ClusterStateTaskExecutor;
import org.elasticsearch.cluster.ClusterStateTaskListener;
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
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.discovery.AckClusterStatePublishResponseHandler;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.discovery.DiscoverySettings;
import org.elasticsearch.discovery.DiscoveryStats;
import org.elasticsearch.index.Index;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;

import static org.apache.cassandra.cql3.QueryProcessor.executeInternal;
import static org.elasticsearch.gateway.GatewayService.STATE_NOT_RECOVERED_BLOCK;

/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/modules-discovery-zen.html
 *
 * Discover the cluster topology from cassandra snitch and settings, mappings, blocks from the elastic_admin keyspace.
 * Publishing is just a notification to refresh in memory configuration from the cassandra table.
 * @author vroyer
 *
 */
public class CassandraDiscovery extends AbstractLifecycleComponent implements Discovery, IEndpointStateChangeSubscriber, AppliedClusterStateAction.AppliedClusterStateListener {
    private static final EnumSet CASSANDRA_ROLES = EnumSet.of(Role.MASTER,Role.DATA);
    private final TransportService transportService;

    private final ClusterService clusterService;
    private final ClusterApplier clusterApplier;
    private final AtomicReference<ClusterState> committedState; // last committed cluster state

    private final ClusterName clusterName;
    private final DiscoverySettings discoverySettings;
    private final NamedWriteableRegistry namedWriteableRegistry;

    private final PendingClusterStatesQueue pendingStatesQueue;
    private final AppliedClusterStateAction appliedClusterStateAction;
    private final AtomicReference<AckClusterStatePublishResponseHandler> handlerRef = new AtomicReference<>();
    private final Object stateMutex = new Object();

    private final ClusterGroup clusterGroup;

    private final InetAddress localAddress;
    private final String localDc;

    private final ConcurrentMap<String, ShardRoutingState> localShardStateMap = new ConcurrentHashMap<String, ShardRoutingState>();
    private final ConcurrentMap<UUID, Map<String,ShardRoutingState>> remoteShardRoutingStateMap = new ConcurrentHashMap<UUID, Map<String,ShardRoutingState>>();
    private final RoutingTableUpdateTaskExecutor routingTableUpdateTaskExecutor;
    /**
     * When searchEnabled=true, local shards are visible for routing, otherwise, local shards are seen as UNASSIGNED.
     * This allows to gracefully shutdown or start the node for maintenance like an offline repair or rebuild_index.
     */
    private final AtomicBoolean searchEnabled = new AtomicBoolean(false);

    /**
     * If autoEnableSearch=true, search is automatically enabled when the node becomes ready to operate, otherwise, searchEnabled should be manually set to true.
     */
    private final AtomicBoolean autoEnableSearch = new AtomicBoolean(System.getProperty("es.auto_enable_search") == null || Boolean.getBoolean("es.auto_enable_search"));

    public static final Setting<Integer> MAX_PENDING_CLUSTER_STATES_SETTING =
            Setting.intSetting("discovery.cassandra.publish.max_pending_cluster_states", 1024, 1, Property.NodeScope);

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

        this.committedState = new AtomicReference<>();
        this.clusterService.setDiscovery(this);
        this.clusterService.getMasterService().setClusterStateSupplier(() -> committedState.get());
        this.clusterService.getMasterService().setClusterStatePublisher(this::publish);

        this.localAddress = FBUtilities.getBroadcastAddress();
        this.localDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(FBUtilities.getBroadcastAddress());

        this.clusterGroup = new ClusterGroup();
        this.pendingStatesQueue = new PendingClusterStatesQueue(logger, MAX_PENDING_CLUSTER_STATES_SETTING.get(settings));
        this.appliedClusterStateAction = new AppliedClusterStateAction(settings, transportService, this, discoverySettings);
        this.routingTableUpdateTaskExecutor = new RoutingTableUpdateTaskExecutor();
    }

    public PendingClusterStatesQueue pendingStatesQueue() {
        return this.pendingStatesQueue;
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
                    UntypedResultSet rs = executeInternal("SELECT preferred_ip, rpc_address from system." + SystemKeyspace.PEERS +"  WHERE peer = ?", endpoint);
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

        updateNodesTable("starting-cassandra-discovery");
    }

    public ClusterState initClusterState(DiscoveryNode localNode) {
        ClusterState.Builder builder = clusterApplier.newClusterStateBuilder();
        ClusterState clusterState = builder.nodes(DiscoveryNodes.builder().add(localNode)
                .localNodeId(localNode.getId())
                .masterNodeId(localNode.getId())
                .build())
            .blocks(ClusterBlocks.builder()
                .addGlobalBlock(STATE_NOT_RECOVERED_BLOCK)
                .addGlobalBlock(CassandraGatewayService.NO_CASSANDRA_RING_BLOCK))
            .build();
        setCommittedState(clusterState);
        this.clusterApplier.setInitialState(clusterState);
        return clusterState;
    }

    /**
     * Update the shardState map and trigger a cluster state routing table update if changed.
     * @param remoteNode
     * @param shardsStateMap
     * @param source
     */
    private void updateShardRouting(final UUID remoteNode, final Map<String, ShardRoutingState> shardsStateMap, String source, final ClusterState currentState) {
        this.remoteShardRoutingStateMap.compute(remoteNode, (k, v) -> {
            if (v == null)
                return shardsStateMap;
            MapDifference<String, ShardRoutingState> mapDifference = Maps.difference(v, shardsStateMap);
            if (!mapDifference.entriesDiffering().isEmpty() || !mapDifference.entriesOnlyOnRight().isEmpty()) {
                Set<Index> indices = Stream.concat(mapDifference.entriesDiffering().keySet().stream(), mapDifference.entriesOnlyOnRight().keySet().stream())
                        .map(i -> Optional.ofNullable(currentState.metaData().hasIndex(i) ? currentState.metaData().index(i).getIndex() : null))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());
                logger.trace("Update routing table for indices={}", indices);
                if (!indices.isEmpty())
                    updateRoutingTable(source, indices, false);
            }
            return shardsStateMap;
        });
    }

    private void updateRoutingTable(String source, Set<Index> indices, boolean nodesUpdates) {
        logger.debug("Updating routing table indices source=[{}] indices={} nodesUpdates={}", source, indices, nodesUpdates);
        clusterService.submitStateUpdateTask(source, new RoutingTableUpdateTask(indices, nodesUpdates),
                this.routingTableUpdateTaskExecutor, this.routingTableUpdateTaskExecutor, this.routingTableUpdateTaskExecutor);
    }

    private void updateNodesTable(String source) {
        logger.debug("Updating routing table node source=[{}]", source);
        clusterService.submitStateUpdateTask(source, new RoutingTableUpdateTask(true),
                this.routingTableUpdateTaskExecutor, this.routingTableUpdateTaskExecutor, this.routingTableUpdateTaskExecutor);
    }

    class RoutingTableUpdateTask  {
        final Set<Index> indices;
        final boolean updateNodes;

        RoutingTableUpdateTask(Index index) {
            this(Collections.singleton(index), false);
        }

        RoutingTableUpdateTask(boolean updateNodes) {
            this(Collections.EMPTY_SET, updateNodes);
        }

        RoutingTableUpdateTask(Set<Index> indices, boolean updateNodes) {
            this.indices = indices;
            this.updateNodes = updateNodes;
        }

        public Set<Index> indices()  { return this.indices; }
        public boolean updateNodes() { return this.updateNodes; }
    }

    /**
     * Computation of the routing table for several indices (or for all indices if nodes changed) for batched cluster state updates.
     */
    class RoutingTableUpdateTaskExecutor implements ClusterStateTaskExecutor<RoutingTableUpdateTask>, ClusterStateTaskConfig, ClusterStateTaskListener {

        @Override
        public ClusterTasksResult<RoutingTableUpdateTask> execute(ClusterState currentState, List<RoutingTableUpdateTask> tasks) throws Exception {
            boolean updateNodes = tasks.stream().filter(RoutingTableUpdateTask::updateNodes).count() > 0;
            Set<Index> indices = tasks.stream().map(RoutingTableUpdateTask::indices).flatMap(Set::stream).collect(Collectors.toSet());

            DiscoveryNodes discoverNodes = nodes();
            ClusterState.Builder clusterStateBuilder = ClusterState.builder(currentState);
            clusterStateBuilder.nodes(discoverNodes);

            if (currentState.nodes().getSize() != discoverNodes.getSize() || updateNodes) {

                // update numberOfShards/numberOfReplicas for all indices.
                MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
                for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
                    IndexMetaData indexMetaData = it.next();
                    IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
                    indexMetaDataBuilder.numberOfShards(discoverNodes.getSize());
                    int rf = ClusterService.replicationFactor(indexMetaData.keyspace());
                    indexMetaDataBuilder.numberOfReplicas( Math.max(0, rf - 1) );
                    metaDataBuilder.put(indexMetaDataBuilder.build(), false);
                }
                clusterStateBuilder.metaData(metaDataBuilder.build());
                ClusterState workingClusterState = clusterStateBuilder.build();
                RoutingTable routingTable = RoutingTable.build(clusterService, workingClusterState);
                ClusterState resultingState = ClusterState.builder(workingClusterState).routingTable(routingTable).build();
                return ClusterTasksResult.builder().successes((List)tasks).build(resultingState);
            }

            // only update routing table for some indices
            RoutingTable routingTable = indices.isEmpty() ?
                    RoutingTable.build(clusterService, clusterStateBuilder.build()) :
                    RoutingTable.build(clusterService, clusterStateBuilder.build(), indices);
            ClusterState resultingState = ClusterState.builder(currentState).routingTable(routingTable).build();
            return ClusterTasksResult.builder().successes((List)tasks).build(resultingState);
        }

        @Override
        public TimeValue timeout() {
            return null;
        }

        @Override
        public Priority priority() {
            return Priority.NORMAL;
        }

        @Override
        public void onFailure(String source, Exception e) {
            logger.error("unexpected failure during [{}]", e, source);
        }
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
                            try {
                                Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(x1.value, indexShardStateTypeReference);
                                updateShardRouting(Gossiper.instance.getHostId(endpoint), shardsStateMap, "X1-"+endpoint, clusterState());
                            } catch (IOException e) {
                                logger.error("Failed to parse X1 for node [{}]", hostId);
                            }
                        }
                    }
                }
            }
        }
        updateNodesTable("discovery-refresh");
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
            if (publishPort.indexOf("-") > 0) {
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
                updateNodesTable("update-node-" + NetworkAddress.format(endpoint)+"-"+newStatus.toString());
                return true;
            }
        }
        return false;
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
    public boolean isNormal(DiscoveryNode node) {
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
                    notifyHandler(Gossiper.instance.getEndpointStateForEndpoint(endpoint));
                }
                break;

            case X1:
                try {
                    // update the remoteShardRoutingStateMap to build ES routing table for joined-normal nodes only.
                    if (clusterGroup.contains(epState.getApplicationState(ApplicationState.HOST_ID).value)) {
                        if (logger.isTraceEnabled())
                            logger.trace("Endpoint={} X1={} => updating routing table", endpoint, versionValue);

                        final Map<String, ShardRoutingState> shardsStateMap = jsonMapper.readValue(versionValue.value, indexShardStateTypeReference);
                        final UUID remoteNode = Gossiper.instance.getHostId(endpoint);
                        updateShardRouting(remoteNode, shardsStateMap, "X1-" + endpoint, clusterState());
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
                    publishX2(this.committedState.get(), true);
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

    private void traceEpState(InetAddress endpoint, EndpointState epState) {
        if (logger.isTraceEnabled())
            logger.trace("Endpoint={} isAlive={} STATUS={} HOST_ID={} INTERNAL_IP={} RPC_ADDRESS={} SCHEMA={} X1={} X2={}", endpoint,
                    epState.isAlive(),
                    epState.getStatus(),
                    epState.getApplicationState(ApplicationState.HOST_ID),
                    epState.getApplicationState(ApplicationState.INTERNAL_IP),
                    epState.getApplicationState(ApplicationState.RPC_ADDRESS),
                    epState.getApplicationState(ApplicationState.SCHEMA),
                    epState.getApplicationState(ApplicationState.X1),
                    epState.getApplicationState(ApplicationState.X2));
    }

    @Override
    public void onAlive(InetAddress endpoint, EndpointState epState) {
        if (isMember(endpoint)) {
            traceEpState(endpoint, epState);
            logger.debug("Endpoint={} isAlive={} => update node + connecting", endpoint, epState.isAlive());
            if (isNormal(epState))
                updateNode(endpoint, epState);
        }
    }

    @Override
    public void onDead(InetAddress endpoint, EndpointState epState) {
        if (isMember(endpoint)) {
            traceEpState(endpoint, epState);
            logger.warn("Endpoint={} isAlive={} => update node + disconnecting", endpoint, epState.isAlive());
            notifyHandler(Gossiper.instance.getEndpointStateForEndpoint(endpoint));
            updateNode(endpoint, epState);
        }
    }

    @Override
    public void onRestart(InetAddress endpoint, EndpointState epState) {
        if (isMember(endpoint)) {
            traceEpState(endpoint, epState);
            if (isNormal(epState))
                updateNode(endpoint, epState);
        }
    }

    @Override
    public void onJoin(InetAddress endpoint, EndpointState epState) {
        if (isLocal(endpoint)) {
            traceEpState(endpoint, epState);
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
                notifyHandler(Gossiper.instance.getEndpointStateForEndpoint(endpoint));
                this.clusterGroup.remove(removedNode.getId());
                updateNodesTable("node-removed-"+endpoint);
            }
        }
    }

    /**
     * Release the listener when all attendees have reached the expected version or become down.
     * Called by the cassandra gossiper thread from onChange() or onDead() or onRemove().
     */
    public void notifyHandler(EndpointState endPointState) {
        VersionedValue hostIdValue = endPointState.getApplicationState(ApplicationState.HOST_ID);
        if (hostIdValue == null)
            return; // happen when we are removing a node while updating the mapping

        String hostId = hostIdValue.value;
        if (hostId == null || localNode().getId().equals(hostId))
            return;

        if (!endPointState.isAlive() || !endPointState.getStatus().equals("NORMAL")) {
            // node was removed from the gossiper, down or leaving, acknowledge to avoid locking.
            AckClusterStatePublishResponseHandler handler = handlerRef.get();
            if (handler != null) {
                DiscoveryNode node = nodes().get(hostId);
                if (node != null) {
                    logger.debug("nack node={}", node.getId());
                    handler.onFailure(node, new NoNodeAvailableException("Node "+hostId+" unavailable"));
                }
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
            updateRoutingTable("searchEnabled changed to "+ready, Collections.EMPTY_SET, false);
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
        if (Gossiper.instance.isEnabled() || force) {
            Gossiper.instance.addLocalApplicationState(ELASTIC_META_DATA, StorageService.instance.valueFactory.datacenter(clusterState.metaData().x2()));
            if (logger.isTraceEnabled())
                logger.trace("X2={} published in gossip state", clusterState.metaData().x2());
        }
    }

    @Override
    protected void doClose()  {
        Gossiper.instance.unregister(this);
    }

    public ClusterState clusterState() {
        ClusterState clusterState = committedState.get();
        assert clusterState != null : "accessing cluster state before it is set";
        return clusterState;
    }

    // visible for testing
    void setCommittedState(ClusterState clusterState) {
        synchronized (stateMutex) {
            committedState.set(clusterState);
            publishX2(clusterState);
        }
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
        if (epState == null || !epState.isAlive()) {
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
        publishX2(this.committedState.get());
    }

    /**
     * Publish all the changes to the cluster from the master (can be called just by the master). The publish
     * process should apply this state to the master as well!
     *
     * The {@link AckListener} allows to keep track of the ack received from nodes, and verify whether
     * they updated their own cluster state or not.
     *
     * The method is guaranteed to throw a {@link FailedToCommitClusterStateException} if the change is not committed and should be rejected.
     * Any other exception signals the something wrong happened but the change is committed.
     *
     * Strapdata NOTES:
     * Publish is blocking until change is apply locally, but not while waiting remote nodes.
     * When the last remote node acknowledge a metadata version, this finally acknowledge the calling task.
     * According to the Metadata.clusterUuid in the new clusterState, the node acts as the coordinator or participant.
     */
    @Override
    public void publish(final ClusterChangedEvent clusterChangedEvent, final AckListener ackListener) {
        ClusterState previousClusterState = clusterChangedEvent.previousState();
        ClusterState newClusterState = clusterChangedEvent.state();

        long startTimeNS = System.nanoTime();
        try {
            if (clusterChangedEvent.schemaUpdate().updated()) {
                // update and broadcast the metadata through a CQL schema update + ack from participant nodes
                if (localNode().getId().equals(newClusterState.metaData().clusterUUID())) {
                    publishAsCoordinator(clusterChangedEvent, ackListener);
                } else {
                    publishAsParticipator(clusterChangedEvent, ackListener);
                }
            } else {
                // publish local cluster state update (for blocks, nodes or routing update)
                publishLocalUpdate(clusterChangedEvent, ackListener);
            }
        } catch (Exception e) {
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            StringBuilder sb = new StringBuilder("failed to execute cluster state update in ").append(executionTime)
                    .append(", state:\nversion [")
                    .append(previousClusterState.version()).
                    append("], source [").append(clusterChangedEvent.source()).append("]\n");
            logger.warn(sb.toString(), e);
            throw new ElasticsearchException(e);
        }
    }

    /**
     * Publish the new metadata through a CQL schema update (a blocking schema update unless we update a CQL map as a dynamic nested object),
     * and wait acks (AckClusterStatePublishResponseHandler) from participant nodes with state alive+NORMAL.
     * @param clusterChangedEvent
     * @param ackListener
     * @throws InterruptedException
     * @throws IOException
     */
    void publishAsCoordinator(final ClusterChangedEvent clusterChangedEvent, final AckListener ackListener) throws InterruptedException, IOException {
        logger.debug("Coordinator update source={} metadata={}", clusterChangedEvent.source(), clusterChangedEvent.state().metaData().x2());

        ClusterState previousClusterState = clusterChangedEvent.previousState();
        ClusterState newClusterState = clusterChangedEvent.state();
        DiscoveryNodes nodes = clusterChangedEvent.state().nodes();
        DiscoveryNode localNode = nodes.getLocalNode();

        // increment metadata.version
        newClusterState = ClusterState.builder(newClusterState)
                .metaData(MetaData.builder(newClusterState.metaData()).incrementVersion().build())
                .build();

        Collection<Mutation> mutations = clusterChangedEvent.mutations() == null ? new ArrayList<>() : clusterChangedEvent.mutations();
        Collection<Event.SchemaChange> events = clusterChangedEvent.events() == null ? new ArrayList<>() : clusterChangedEvent.events();
        try {
            // TODO: track change to update CQL schema when really needed
            clusterService.writeMetadataToSchemaMutations(newClusterState.metaData(), mutations, events);
        } catch (ConfigurationException | IOException e1) {
            throw new ElasticsearchException(e1);
        }

        try {
            // PAXOS schema update commit
            clusterService.commitMetaData(previousClusterState.metaData(), newClusterState.metaData(), clusterChangedEvent.source());

            // compute alive node for awaiting applied acknowledgment
            long publishingStartInNanos = System.nanoTime();
            Set<DiscoveryNode> nodesToPublishTo = new HashSet<>(nodes.getSize());
            for (final DiscoveryNode node : nodes) {
                if (node.status() == DiscoveryNodeStatus.ALIVE && isNormal(node))
                    nodesToPublishTo.add(node);
            }
            logger.trace("New coordinator handler for nodes={}", nodesToPublishTo);
            final AckClusterStatePublishResponseHandler handler = new AckClusterStatePublishResponseHandler(nodesToPublishTo, ackListener);
            handlerRef.set(handler);

            // apply new CQL schema
            if (mutations != null && mutations.size() > 0) {
                logger.debug("Applying CQL schema source={} update={} mutations={} ",
                        clusterChangedEvent.source(), clusterChangedEvent.schemaUpdate(), mutations);

                // unless update is UPDATE_ASYNCHRONOUS, block until schema is applied.
                Future<?> future = MigrationManager.announce(mutations, this.clusterService.getSchemaManager().getInhibitedSchemaListeners());
                if (!SchemaUpdate.UPDATE_ASYNCHRONOUS.equals(clusterChangedEvent.schemaUpdate()))
                    FBUtilities.waitOnFuture(future);

                // build routing table when keyspaces are created locally
                newClusterState = ClusterState.builder(newClusterState)
                        .routingTable(RoutingTable.build(this.clusterService, newClusterState))
                        .build();
                logger.info("CQL source={} SchemaChanges={}", clusterChangedEvent.source(), events);
            }

            // add new cluster state into the pending-to-apply cluster states queue, listening ack from remote nodes.
            final AtomicBoolean processedOrFailed = new AtomicBoolean();
            pendingStatesQueue.addPending(newClusterState, new PendingClusterStatesQueue.StateProcessedListener() {
                @Override
                public void onNewClusterStateProcessed() {
                    processedOrFailed.set(true);
                    handler.onResponse(localNode);
                }

                @Override
                public void onNewClusterStateFailed(Exception e) {
                    processedOrFailed.set(true);
                    handler.onFailure(localNode, e);
                    logger.warn((org.apache.logging.log4j.util.Supplier<?>) () -> new ParameterizedMessage(
                            "failed while applying cluster state locally [{}]", clusterChangedEvent.source()), e);
                }
            });

            // apply the next-to-process cluster state.
            synchronized (stateMutex) {
                if (clusterChangedEvent.previousState() != this.committedState.get()) {
                    throw new FailedToCommitClusterStateException("local state was mutated while CS update was published to other nodes");
                }

                boolean sentToApplier = processNextCommittedClusterState("committed source=" + clusterChangedEvent.source() + " metadata=" + newClusterState.metaData().x2());
                if (sentToApplier == false && processedOrFailed.get() == false) {
                    logger.warn("metadata={} has neither been processed nor failed", newClusterState.metaData().x2());
                    assert false : "cluster state published locally neither processed nor failed: " + newClusterState;
                    return;
                }
            }

            // wait all nodes are applied.
            final TimeValue publishTimeout = discoverySettings.getPublishTimeout();
            long timeLeftInNanos = Math.max(0, publishTimeout.nanos() - (System.nanoTime() - publishingStartInNanos));
            if (!handler.awaitAllNodes(TimeValue.timeValueNanos(timeLeftInNanos))) {
                logger.info("commit source={} metadata={} timeout with pending nodes={}",
                        clusterChangedEvent.source(), newClusterState.metaData().x2(), Arrays.toString(handler.pendingNodes()));
            } else {
                logger.debug("commit source={} metadata={} applied succefully on nodes={}",
                        clusterChangedEvent.source(), newClusterState.metaData().x2(), nodesToPublishTo);
            }

        } catch (ConcurrentMetaDataUpdateException e) {
            // should replay the task later when current cluster state will match the expected metadata uuid and version
            logger.warn("PAXOS concurrent update, source={} metadata={}, resubmit task on next metadata change",
                    clusterChangedEvent.source(), newClusterState.metaData().x2());
            resubmitTaskOnNextChange(clusterChangedEvent);
            return;
        } catch(UnavailableException e) {
            logger.error("PAXOS not enough available nodes, source={} metadata={}",
                    clusterChangedEvent.source(), newClusterState.metaData().x2());
            ackListener.onNodeAck(localNode, e);
            throw e;
        } catch(WriteTimeoutException e) {
            logger.error("PAXOS write timeout, source={} metadata={} writeType={}. If that write succeed, this will cause mapping sycnchronization issue.",
                    clusterChangedEvent.source(), newClusterState.metaData().x2(), e.writeType);
            ackListener.onNodeAck(localNode, e);
            throw new PaxosMetaDataUpdateException(e);
        } finally {
            handlerRef.set(null);
        }
    }

    /**
     * Publish the new metadata and notify the coordinator through an appliedClusterStateAction.
     * @param clusterChangedEvent
     * @param ackListener
     */
    void publishAsParticipator(final ClusterChangedEvent clusterChangedEvent, final AckListener ackListener) {
        ClusterState newClusterState = clusterChangedEvent.state();
        String reason = clusterChangedEvent.source();

        final DiscoveryNode coordinatorNode = newClusterState.nodes().get(newClusterState.metaData().clusterUUID());
        logger.debug("Participator update reason={} metadata={} coordinator={}", reason, newClusterState.metaData().x2(), coordinatorNode);

        if (newClusterState.metaData().version() <= clusterState().metaData().version()) {
            logger.warn("Ignore and acknowlegde obsolete update metadata={}", newClusterState.metaData().x2());
            CassandraDiscovery.this.appliedClusterStateAction.sendAppliedToNode(coordinatorNode, newClusterState, null);
            return;
        }

        final AtomicBoolean processedOrFailed = new AtomicBoolean();
        this.pendingStatesQueue.addPending(newClusterState,  new PendingClusterStatesQueue.StateProcessedListener() {
            @Override
            public void onNewClusterStateProcessed() {
                if (coordinatorNode != null) {
                    logger.trace("sending applied state=[{}] to coordinator={} reason={}",
                            newClusterState.metaData().x2(), coordinatorNode, reason);
                    CassandraDiscovery.this.appliedClusterStateAction.sendAppliedToNode(coordinatorNode, newClusterState, null);
                }
            }

            @Override
            public void onNewClusterStateFailed(Exception e) {
                if (coordinatorNode != null) {
                    logger.trace("sending failed state=[{}] to coordinator={} reason={} exception={}",
                            newClusterState.metaData().x2(), coordinatorNode, reason, e.toString());
                    CassandraDiscovery.this.appliedClusterStateAction.sendAppliedToNode(coordinatorNode, newClusterState, e);
                }
            }
        });
        // apply the next-to-process cluster state.
        synchronized (stateMutex) {
            boolean sentToApplier = processNextCommittedClusterState(
                    "committed version [" + newClusterState.metaData().x2() + "] source [" + reason + "]");
            if (sentToApplier == false  && processedOrFailed.get() == false) {
                logger.warn("metadata={} has neither been processed nor failed", newClusterState.metaData().x2());
                assert false : "cluster state published locally neither processed nor failed: " + newClusterState;
                return;
            }
        }
    }

    /**
     * Publish a local cluster state update (no coordination) coming from a CQL schema update.
     * @param clusterChangedEvent
     * @param ackListener
     */
    void publishLocalUpdate(final ClusterChangedEvent clusterChangedEvent, final AckListener ackListener) {
        ClusterState newClusterState = clusterChangedEvent.state();
        logger.debug("Local update source={} metadata={}", clusterChangedEvent.source(), newClusterState.metaData().x2());
        final AtomicBoolean processedOrFailed = new AtomicBoolean();
        pendingStatesQueue.addPending(newClusterState,
            new PendingClusterStatesQueue.StateProcessedListener() {
                @Override
                public void onNewClusterStateProcessed() {
                    processedOrFailed.set(true);
                    // simulate ack from all nodes, elassandra only update the local clusterState here.
                    clusterChangedEvent.state().nodes().forEach(node -> ackListener.onNodeAck(node, null));
                }

                @Override
                public void onNewClusterStateFailed(Exception e) {
                    processedOrFailed.set(true);
                    // simulate nack from all nodes, elassandra only update the local clusterState here.
                    clusterChangedEvent.state().nodes().forEach(node -> ackListener.onNodeAck(node, e));
                    logger.warn((org.apache.logging.log4j.util.Supplier<?>) () -> new ParameterizedMessage(
                            "failed while applying cluster state locally source={}", clusterChangedEvent.source()), e);
                }
            });

        // apply the next-to-process cluster state.
        synchronized (stateMutex) {
            if (clusterChangedEvent.previousState() != this.committedState.get()) {
                throw new FailedToCommitClusterStateException("local state was mutated while CS update was published to other nodes");
            }

            boolean sentToApplier = processNextCommittedClusterState(
                    "committed version [" + newClusterState.metaData().x2() + "] source [" + clusterChangedEvent.source() + "]");
            if (sentToApplier == false && processedOrFailed.get() == false) {
                logger.warn("metadata={} source=[{}] has neither been processed nor failed", newClusterState.metaData().x2(), clusterChangedEvent.source());
                assert false : "cluster state published locally neither processed nor failed: " + newClusterState;
                return;
            }
        }
    }

    protected void resubmitTaskOnNextChange(final ClusterChangedEvent clusterChangedEvent) {
        final long resubmitTimeMillis = System.currentTimeMillis();
        clusterService.addListener(new ClusterStateListener() {
            @Override
            public void clusterChanged(ClusterChangedEvent event) {
                if (event.metaDataChanged()) {
                    final long lostTimeMillis = System.currentTimeMillis() - resubmitTimeMillis;
                    Priority priority = Priority.URGENT;
                    TimeValue timeout = TimeValue.timeValueMillis(30*1000 - lostTimeMillis);
                    Map<Object, ClusterStateTaskListener> map = clusterChangedEvent.taskInputs().updateTasksToMap(priority, lostTimeMillis);
                    logger.warn("metadata={} => resubmit delayed update source={} tasks={} priority={} remaing timeout={}",
                            event.state().metaData().x2(), clusterChangedEvent.source(), clusterChangedEvent.taskInputs().updateTasks, priority, timeout);
                    clusterService.submitStateUpdateTasks(clusterChangedEvent.source(), map, ClusterStateTaskConfig.build(priority, timeout), clusterChangedEvent.taskInputs().executor);
                    clusterService.removeListener(this); // replay only once.
                }
            }
        });
    }

    // receive ack from remote nodes when cluster state applied.
    @Override
    public void onClusterStateApplied(String nodeId, String x2, Exception e, ActionListener<Void> processedListener) {
        logger.trace("received state=[{}] applied from={}", x2, nodeId);
        try {
            AckClusterStatePublishResponseHandler handler = this.handlerRef.get();
            DiscoveryNode node = this.committedState.get().nodes().get(nodeId);
            if (handler != null && node != null) {
                if (e != null) {
                    logger.trace("state=[{}] apply failed from node={}", x2, nodeId);
                    handler.onFailure(node, e);
                } else {
                    logger.trace("state=[{}] apply from node={}", x2, nodeId);
                    handler.onResponse(node);
                }
            }
            processedListener.onResponse(null);
        } catch(Exception ex) {
            processedListener.onFailure(ex);
        }
    }


    // return true if state has been sent to applier
    boolean processNextCommittedClusterState(String reason) {
        assert Thread.holdsLock(stateMutex);

        final ClusterState newClusterState = pendingStatesQueue.getNextClusterStateToProcess();
        final ClusterState currentState = committedState.get();
        // all pending states have been processed
        if (newClusterState == null) {
            return false;
        }

        assert newClusterState.nodes().getMasterNode() != null : "received a cluster state without a master";
        assert !newClusterState.blocks().hasGlobalBlock(discoverySettings.getNoMasterBlock()) : "received a cluster state with a master block";

        try {
            if (shouldIgnoreOrRejectNewClusterState(logger, currentState, newClusterState)) {
                String message = String.format(
                    Locale.ROOT,
                    "rejecting cluster state version [%d] uuid [%s] received from [%s]",
                    newClusterState.version(),
                    newClusterState.stateUUID(),
                    newClusterState.nodes().getMasterNodeId()
                );
                throw new IllegalStateException(message);
            }
        } catch (Exception e) {
            try {
                pendingStatesQueue.markAsFailed(newClusterState, e);
            } catch (Exception inner) {
                inner.addSuppressed(e);
                logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected exception while failing [{}]", reason), inner);
            }
            return false;
        }

        if (currentState.blocks().hasGlobalBlock(discoverySettings.getNoMasterBlock())) {
            // its a fresh update from the master as we transition from a start of not having a master to having one
            logger.debug("got first state from fresh master [{}]", newClusterState.nodes().getMasterNodeId());
        }

        if (currentState == newClusterState) {
            return false;
        }

        committedState.set(newClusterState);

        clusterApplier.onNewClusterState("apply cluster state (from " + newClusterState.metaData().clusterUUID() + "[" + reason + "])",
            this::clusterState,
            new ClusterStateTaskListener() {
                @Override
                public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
                    try {
                        pendingStatesQueue.markAsProcessed(newClusterState);
                    } catch (Exception e) {
                        onFailure(source, e);
                    }
                }

                @Override
                public void onFailure(String source, Exception e) {
                    logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected failure applying [{}]", reason), e);
                    try {
                        // TODO: use cluster state uuid instead of full cluster state so that we don't keep reference to CS around
                        // for too long.
                        pendingStatesQueue.markAsFailed(newClusterState, e);
                    } catch (Exception inner) {
                        inner.addSuppressed(e);
                        logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected exception while failing [{}]", reason), inner);
                    }
                }
            });

        return true;
    }

    public static boolean shouldIgnoreOrRejectNewClusterState(Logger logger, ClusterState currentState, ClusterState newClusterState) {
        if (newClusterState.version() < currentState.version()) {
            logger.debug("received a cluster state that is not newer than the current one, ignoring (received {}, current {})",
                    newClusterState.version(), currentState.version());
            return true;
        }
        if (newClusterState.metaData().version() < currentState.metaData().version()) {
            logger.debug("received a cluster state metadata.verson that is not newer than the current one, ignoring (received {}, current {})",
                    newClusterState.metaData().version(), currentState.metaData().version());
            return true;
        }
        if (!newClusterState.metaData().clusterUUID().equals(currentState.metaData().clusterUUID()) &&
             newClusterState.metaData().version() == currentState.metaData().version() &&
             currentState.metaData().version() > 0)  {
            logger.debug("received a remote cluster state with same metadata.version, ignoring (received {}, current {})", newClusterState.metaData().version(), currentState.metaData().version());
            return true;
        }
        return false;
    }

    /**
     * does simple sanity check of the incoming cluster state. Throws an exception on rejections.
     */
    static void validateIncomingState(Logger logger, ClusterState incomingState, ClusterState lastState) {
        final ClusterName incomingClusterName = incomingState.getClusterName();
        if (!incomingClusterName.equals(lastState.getClusterName())) {
            logger.warn("received cluster state from [{}] which is also master but with a different cluster name [{}]",
                incomingState.nodes().getMasterNode(), incomingClusterName);
            throw new IllegalStateException("received state from a node that is not part of the cluster");
        }
        if (lastState.nodes().getLocalNode().equals(incomingState.nodes().getLocalNode()) == false) {
            logger.warn("received a cluster state from [{}] and not part of the cluster, should not happen",
                incomingState.nodes().getMasterNode());
            throw new IllegalStateException("received state with a local node that does not match the current local node");
        }

        if (shouldIgnoreOrRejectNewClusterState(logger, lastState, incomingState)) {
            String message = String.format(
                Locale.ROOT,
                "rejecting cluster state version [%d] received from [%s]",
                incomingState.metaData().x2(),
                incomingState.nodes().getMasterNodeId()
            );
            logger.warn(message);
            throw new IllegalStateException(message);
        }
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
