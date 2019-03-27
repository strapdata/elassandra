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
package org.elasticsearch.cluster.service;


import ch.qos.logback.classic.jmx.JMXConfiguratorMBean;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.KeyspaceNotDefinedException;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.exceptions.WriteTimeoutException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.schema.ReplicationParams;
import org.apache.cassandra.schema.SchemaKeyspace;
import org.apache.cassandra.serializers.UUIDSerializer;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.Event;
import org.apache.cassandra.transport.Event.SchemaChange;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elassandra.ConcurrentMetaDataUpdateException;
import org.elassandra.NoPersistedMetaDataException;
import org.elassandra.cluster.QueryManager;
import org.elassandra.cluster.SchemaManager;
import org.elassandra.cluster.Serializer;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.cluster.routing.PrimaryFirstSearchStrategy;
import org.elassandra.discovery.CassandraDiscovery;
import org.elassandra.index.ExtendedElasticSecondaryIndex;
import org.elassandra.index.search.TokenRangesService;
import org.elassandra.shard.CassandraShardStartedBarrier;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingClusterStateUpdateRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateTaskConfig.SchemaUpdate;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataMappingService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.discovery.Discovery;
import org.elasticsearch.gateway.MetaStateService;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.VersionLessInternalEngine;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static java.util.Collections.emptyList;
import static org.apache.cassandra.cql3.QueryProcessor.executeInternal;
import static org.elasticsearch.common.settings.Setting.listSetting;


public class ClusterService extends BaseClusterService {

    private static final String ELASTIC_ADMIN_KEYSPACE = "elastic_admin";
    public static final String ELASTIC_ADMIN_METADATA_TABLE = "metadata";

    public static final String ELASTIC_EXTENSION_METADATA = "metadata";
    public static final String ELASTIC_EXTENSION_VERSION = "version";
    public static final String ELASTIC_EXTENSION_OWNER = "owner";

    public static final String SETTING_CLUSTER_DATACENTER_GROUP = "datacenter.group";
    public static final Setting<List<String>> SETTING_DATCENTER_GROUP = listSetting(SETTING_CLUSTER_DATACENTER_GROUP, emptyList(), Function.identity(), Property.NodeScope);

    // settings levels : system, cluster, index, table(_meta)
    public static final String SYSTEM_PREFIX = "es.";
    public static final String CLUSTER_PREFIX = "cluster.";
    public static final String INDEX_PREFIX = "index.";
    public static final String TABLE_PREFIX = "";
    private static final int CREATE_ELASTIC_ADMIN_RETRY_ATTEMPTS = Integer.getInteger(SYSTEM_PREFIX + "create_elastic_admin_retry_attempts", 5);

    /**
     * Dynamic mapping update timeout
     */
    public static final String MAPPING_UPDATE_TIMEOUT = "mapping_update_timeout";

    /**
     * Secondary index class
     */
    public static final String SECONDARY_INDEX_CLASS = "secondary_index_class";

    /**
     * Search strategy class
     */
    public static final String SEARCH_STRATEGY_CLASS = "search_strategy_class";

    /**
     * When true, add the cassandra node id to documents (for use with the token aggregation feature)
     */
    public static final String INCLUDE_NODE_ID       = "include_node_id";

    /**
     * When true, re-indexes a row when compacting, usefull to delete expired documents or columns.
     */
    public static final String INDEX_ON_COMPACTION   = "index_on_compaction";

    /**
     * When true, refreshes ES index after each update (used for testing).
     */
    public static final String SYNCHRONOUS_REFRESH   = "synchronous_refresh";

    /**
     * When true, delete kespace/table when removing an index.
     */
    public static final String DROP_ON_DELETE_INDEX  = "drop_on_delete_index";

    /**
     * When true, snapshot lucene files with sstables.
     */
    public static final String SNAPSHOT_WITH_SSTABLE = "snapshot_with_sstable";

    /**
     * When true, use the optimized version less Elasticsearch engine.
     */
    public static final String VERSION_LESS_ENGINE   = "version_less_engine";

    /**
     * Lucene numeric precision to store _token , see http://blog-archive.griddynamics.com/2014/10/numeric-range-queries-in-lucenesolr.html
     */
    public static final String TOKEN_PRECISION_STEP  = "token_precision_step";

    /**
     * Enable the token_ranges bitset cache (cache the token_ranges filter result at the lucene liveDocs level).
     */
    public static final String TOKEN_RANGES_BITSET_CACHE    = "token_ranges_bitset_cache";

    /**
     * Expiration time for unused cached token_ranges queries.
     */
    public static final String TOKEN_RANGES_QUERY_EXPIRE = "token_ranges_query_expire";

    /**
     * Add static columns to indexed documents (default is false).
     */
    public static final String INDEX_STATIC_COLUMNS = "index_static_columns";

    /**
     * Index only static columns (one document per partition row, ex: timeseries tags).
     */
    public static final String INDEX_STATIC_ONLY = "index_static_only";

    /**
     * Index static document (document containing only static columns + partion keys).
     */
    public static final String INDEX_STATIC_DOCUMENT = "index_static_document";

    /**
     * Index in insert-only mode without a read-before-write
     */
    public static final String INDEX_INSERT_ONLY = "index_insert_only";

    /**
     * Basically stores and index _source in a column
     */
    public static final String INDEX_OPAQUE_STORAGE = "index_opaque_storage";

    // system property settings
    public static final String SETTING_SYSTEM_MAPPING_UPDATE_TIMEOUT = SYSTEM_PREFIX+MAPPING_UPDATE_TIMEOUT;
    public static final String SETTING_SYSTEM_SECONDARY_INDEX_CLASS = SYSTEM_PREFIX+SECONDARY_INDEX_CLASS;
    public static final String SETTING_SYSTEM_SEARCH_STRATEGY_CLASS = SYSTEM_PREFIX+SEARCH_STRATEGY_CLASS;
    public static final String SETTING_SYSTEM_INCLUDE_NODE_ID = SYSTEM_PREFIX+INCLUDE_NODE_ID;
    public static final String SETTING_SYSTEM_INDEX_ON_COMPACTION = SYSTEM_PREFIX+INDEX_ON_COMPACTION;
    public static final String SETTING_SYSTEM_SYNCHRONOUS_REFRESH = SYSTEM_PREFIX+SYNCHRONOUS_REFRESH;
    public static final String SETTING_SYSTEM_DROP_ON_DELETE_INDEX = SYSTEM_PREFIX+DROP_ON_DELETE_INDEX;
    public static final String SETTING_SYSTEM_SNAPSHOT_WITH_SSTABLE = SYSTEM_PREFIX+SNAPSHOT_WITH_SSTABLE;
    public static final String SETTING_SYSTEM_VERSION_LESS_ENGINE = SYSTEM_PREFIX+VERSION_LESS_ENGINE;
    public static final String SETTING_SYSTEM_TOKEN_PRECISION_STEP = SYSTEM_PREFIX+TOKEN_PRECISION_STEP;
    public static final String SETTING_SYSTEM_TOKEN_RANGES_BITSET_CACHE = SYSTEM_PREFIX+TOKEN_RANGES_BITSET_CACHE;
    public static final String SETTING_SYSTEM_TOKEN_RANGES_QUERY_EXPIRE = SYSTEM_PREFIX+TOKEN_RANGES_QUERY_EXPIRE;
    public static final String SETTING_SYSTEM_INDEX_INSERT_ONLY = SYSTEM_PREFIX+INDEX_INSERT_ONLY;
    public static final String SETTING_SYSTEM_INDEX_OPAQUE_STORAGE = SYSTEM_PREFIX+INDEX_OPAQUE_STORAGE;

    public static final String SETTING_CLUSTER_INCLUDE_NODE_ID = CLUSTER_PREFIX+INCLUDE_NODE_ID;

    // elassandra cluster settings
    public static final String SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT = CLUSTER_PREFIX+MAPPING_UPDATE_TIMEOUT;
    public static final Setting<Integer> CLUSTER_MAPPING_UPDATE_TIMEOUT_SETTING =
            Setting.intSetting(SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT, Integer.getInteger(SETTING_SYSTEM_MAPPING_UPDATE_TIMEOUT, 30), Property.NodeScope, Property.Dynamic);

    public static final String SETTING_CLUSTER_SECONDARY_INDEX_CLASS = CLUSTER_PREFIX+SECONDARY_INDEX_CLASS;
    public static final Setting<String> CLUSTER_SECONDARY_INDEX_CLASS_SETTING =
            Setting.simpleString(SETTING_CLUSTER_SECONDARY_INDEX_CLASS, System.getProperty(SETTING_SYSTEM_SECONDARY_INDEX_CLASS, ExtendedElasticSecondaryIndex.class.getName()), Property.NodeScope, Property.Dynamic);

    public static final String SETTING_CLUSTER_SEARCH_STRATEGY_CLASS = CLUSTER_PREFIX+SEARCH_STRATEGY_CLASS;
    public static final Setting<String> CLUSTER_SEARCH_STRATEGY_CLASS_SETTING =
            Setting.simpleString(SETTING_CLUSTER_SEARCH_STRATEGY_CLASS, System.getProperty(SETTING_SYSTEM_SEARCH_STRATEGY_CLASS, PrimaryFirstSearchStrategy.class.getName()), Property.NodeScope, Property.Dynamic);

    public static final String SETTING_CLUSTER_DROP_ON_DELETE_INDEX = CLUSTER_PREFIX+DROP_ON_DELETE_INDEX;
    public static final Setting<Boolean> CLUSTER_DROP_ON_DELETE_INDEX_SETTING =
            Setting.boolSetting(SETTING_CLUSTER_DROP_ON_DELETE_INDEX, Boolean.getBoolean(SYSTEM_PREFIX+DROP_ON_DELETE_INDEX), Property.NodeScope, Property.Dynamic);

    public static final String SETTING_CLUSTER_VERSION_LESS_ENGINE = CLUSTER_PREFIX+VERSION_LESS_ENGINE;
    public static final Setting<String> CLUSTER_VERSION_LESS_ENGINE_SETTING =
            Setting.simpleString(SETTING_CLUSTER_VERSION_LESS_ENGINE, System.getProperty(SETTING_SYSTEM_VERSION_LESS_ENGINE, VersionLessInternalEngine.class.getName()), Property.NodeScope, Property.Final);

    public static final String SETTING_CLUSTER_TOKEN_RANGES_BITSET_CACHE = CLUSTER_PREFIX+TOKEN_RANGES_BITSET_CACHE;
    public static final Setting<Boolean> CLUSTER_TOKEN_RANGES_BITSET_CACHE_SETTING =
            Setting.boolSetting(SETTING_CLUSTER_TOKEN_RANGES_BITSET_CACHE, Boolean.getBoolean(SYSTEM_PREFIX+TOKEN_RANGES_BITSET_CACHE), Property.NodeScope, Property.Dynamic);

    public static class DocPrimaryKey {
        public String[] names;
        public Object[] values;
        public boolean isStaticDocument; // pk = partition key and pk has clustering key.

        public DocPrimaryKey(String[] names, Object[] values, boolean isStaticDocument) {
            this.names = names;
            this.values = values;
            this.isStaticDocument = isStaticDocument;
        }

        public DocPrimaryKey(String[] names, Object[] values) {
            this.names = names;
            this.values = values;
            this.isStaticDocument = false;
        }

        public List<ByteBuffer> serialize(ParsedStatement.Prepared prepared) {
            List<ByteBuffer> boundValues = new ArrayList<ByteBuffer>(values.length);
            for (int i = 0; i < values.length; i++) {
                Object v = values[i];
                AbstractType type = prepared.boundNames.get(i).type;
                boundValues.add(v instanceof ByteBuffer || v == null ? (ByteBuffer) v : type.decompose(v));
            }
            return boundValues;
        }

        @Override
        public String toString() {
            return Serializer.stringify(values, values.length);
        }
    }

    private MetaStateService metaStateService;
    private IndicesService indicesService;
    private CassandraDiscovery discovery;

    private final TokenRangesService tokenRangeService;

    protected final MappingUpdatedAction mappingUpdatedAction;

    public final static Class<? extends Index> defaultSecondaryIndexClass = ExtendedElasticSecondaryIndex.class;

    protected final PrimaryFirstSearchStrategy primaryFirstSearchStrategy = new PrimaryFirstSearchStrategy();
    protected final Map<String, AbstractSearchStrategy> strategies = new ConcurrentHashMap<String, AbstractSearchStrategy>();
    protected final Map<String, AbstractSearchStrategy.Router> routers = new ConcurrentHashMap<String, AbstractSearchStrategy.Router>();

    private final ConsistencyLevel metadataWriteCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.write.cl","QUORUM"));
    private final ConsistencyLevel metadataReadCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.read.cl","QUORUM"));
    private final ConsistencyLevel metadataSerialCL = consistencyLevelFromString(System.getProperty("elassandra.metadata.serial.cl","SERIAL"));

    private final String elasticAdminKeyspaceName;
    private final String selectMetadataQuery;
    private final String selectVersionMetadataQuery;
    private final String insertMetadataQuery;
    private final String updateMetaDataQuery;

    private final SchemaManager schemaManager;
    private final QueryManager queryManager;

    private final CassandraShardStartedBarrier cassandraShardStartedBarrier;

    @Inject
    public ClusterService(Settings settings, ClusterSettings clusterSettings, ThreadPool threadPool,
            Map<String, java.util.function.Supplier<ClusterState.Custom>> initialClusterStateCustoms) {
        super(settings, clusterSettings, threadPool, initialClusterStateCustoms);
        this.operationRouting = new OperationRouting(settings, clusterSettings, this);
        this.mappingUpdatedAction = null;
        this.tokenRangeService = new TokenRangesService(settings);
        this.getClusterApplierService().setClusterService(this);
        this.getMasterService().setClusterService(this);
        this.schemaManager = new SchemaManager(settings, this);
        this.queryManager = new QueryManager(settings, this);
        this.cassandraShardStartedBarrier = new CassandraShardStartedBarrier(settings, this);

        String datacenterGroup = settings.get(SETTING_CLUSTER_DATACENTER_GROUP);
        if (datacenterGroup != null && datacenterGroup.length() > 0) {
            logger.info("Starting with datacenter.group=[{}]", datacenterGroup.trim().toLowerCase(Locale.ROOT));
            elasticAdminKeyspaceName = String.format(Locale.ROOT, "%s_%s", ELASTIC_ADMIN_KEYSPACE,datacenterGroup.trim().toLowerCase(Locale.ROOT));
        } else {
            elasticAdminKeyspaceName = ELASTIC_ADMIN_KEYSPACE;
        }
        selectMetadataQuery = String.format(Locale.ROOT, "SELECT metadata,version,owner FROM \"%s\".\"%s\" WHERE cluster_name = ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        selectVersionMetadataQuery = String.format(Locale.ROOT, "SELECT version FROM \"%s\".\"%s\" WHERE cluster_name = ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        insertMetadataQuery = String.format(Locale.ROOT, "INSERT INTO \"%s\".\"%s\" (cluster_name,owner,version) VALUES (?,?,?) IF NOT EXISTS", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        updateMetaDataQuery = String.format(Locale.ROOT, "UPDATE \"%s\".\"%s\" SET owner = ?, version = ? WHERE cluster_name = ? IF version = ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
    }

    @Override
    protected synchronized void doStart() {
        super.doStart();
        MigrationManager.instance.register(schemaManager.getSchemaListener());
    }

    @Override
    protected synchronized void doStop() {
        MigrationManager.instance.unregister(schemaManager.getSchemaListener());
        super.doStop();
    }

    @Override
    public OperationRouting operationRouting() {
        return operationRouting;
    }

    public void setMetaStateService(MetaStateService metaStateService) {
        this.metaStateService = metaStateService;
    }

    public void setIndicesService(IndicesService indicesService) {
        this.indicesService = indicesService;
    }

    public IndicesService getIndicesService() {
        return this.indicesService;
    }

    public QueryManager getQueryManager() {
        return this.queryManager;
    }

    public SchemaManager getSchemaManager() {
        return this.schemaManager;
    }

    public void setDiscovery(Discovery discovery) {
        this.discovery = (CassandraDiscovery)discovery;
    }

    public CassandraDiscovery getCassandraDiscovery() {
        return this.discovery;
    }

    public TokenRangesService tokenRangesService() {
        return this.tokenRangeService;
    }

    public String getElasticAdminKeyspaceName() {
        return this.elasticAdminKeyspaceName;
    }

    public Class<? extends AbstractSearchStrategy> searchStrategyClass(IndexMetaData indexMetaData, ClusterState state) {
        try {
            return AbstractSearchStrategy.getSearchStrategyClass(
                    indexMetaData.getSettings().get(IndexMetaData.SETTING_SEARCH_STRATEGY_CLASS, getClusterSettings().get(CLUSTER_SEARCH_STRATEGY_CLASS_SETTING)));
        } catch(ConfigurationException e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Bad search strategy class, fallback to [{}]", PrimaryFirstSearchStrategy.class.getName()), e);
            return PrimaryFirstSearchStrategy.class;
        }
    }

    private AbstractSearchStrategy searchStrategyInstance(Class<? extends AbstractSearchStrategy> clazz) {
        AbstractSearchStrategy searchStrategy = strategies.get(clazz.getName());
        if (searchStrategy == null) {
            try {
                searchStrategy = clazz.newInstance();
            } catch (Exception e) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("Cannot instanciate search strategy [{}]", clazz.getName()), e);
                searchStrategy = new PrimaryFirstSearchStrategy();
            }
            strategies.putIfAbsent(clazz.getName(), searchStrategy);
        }
        return searchStrategy;
    }


    public PrimaryFirstSearchStrategy.PrimaryFirstRouter updateRouter(IndexMetaData indexMetaData, ClusterState state) {
        // update and returns a PrimaryFirstRouter for the build table.
        PrimaryFirstSearchStrategy.PrimaryFirstRouter router = (PrimaryFirstSearchStrategy.PrimaryFirstRouter)this.primaryFirstSearchStrategy.newRouter(indexMetaData.getIndex(), indexMetaData.keyspace(), this::getShardRoutingStates, state);

        // update the router cache with the effective router
        AbstractSearchStrategy effectiveSearchStrategy = searchStrategyInstance(searchStrategyClass(indexMetaData, state));
        if (! effectiveSearchStrategy.equals(PrimaryFirstSearchStrategy.class) ) {
            AbstractSearchStrategy.Router router2 = effectiveSearchStrategy.newRouter(indexMetaData.getIndex(), indexMetaData.keyspace(), this::getShardRoutingStates, state);
            this.routers.put(indexMetaData.getIndex().getName(), router2);
        } else {
            this.routers.put(indexMetaData.getIndex().getName(), router);
        }

        return router;
    }

    public AbstractSearchStrategy.Router getRouter(IndexMetaData indexMetaData, ClusterState state) {
        AbstractSearchStrategy.Router router = this.routers.get(indexMetaData.getIndex().getName());
        return router;
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final String query)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, query, new Long(0), new Object[] {});
    }

    public UntypedResultSet process(final ConsistencyLevel cl, ClientState clientState, final String query)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, clientState, query, new Long(0), new Object[] {});
    }

    public UntypedResultSet process(final ConsistencyLevel cl, ClientState clientState, final String query, Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, clientState, query, new Long(0), values);
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final String query, Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, null, query, new Long(0), values);
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final String query, final Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        return process(cl, serialConsistencyLevel, query, new Long(0), values);
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, final String query, Long writetime, final Object... values) {
        return process(cl, serialConsistencyLevel, ClientState.forInternalCalls(), query, writetime, values);
    }

    public UntypedResultSet process(final ConsistencyLevel cl, final ConsistencyLevel serialConsistencyLevel, ClientState clientState, final String query, Long writetime, final Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
        if (logger.isDebugEnabled())
            logger.debug("processing CL={} SERIAL_CL={} query={} values={}", cl, serialConsistencyLevel, query, Arrays.asList(values));

        // retreive prepared
        QueryState queryState = new QueryState(clientState);
        ResultMessage.Prepared prepared = ClientState.getCQLQueryHandler().prepare(query, queryState, Collections.EMPTY_MAP);

        // bind
        List<ByteBuffer> boundValues = new ArrayList<ByteBuffer>(values.length);
        for (int i = 0; i < values.length; i++) {
            Object v = values[i];
            AbstractType type = prepared.metadata.names.get(i).type;
            boundValues.add(v instanceof ByteBuffer || v == null ? (ByteBuffer) v : type.decompose(v));
        }

        // execute
        QueryOptions queryOptions = (serialConsistencyLevel == null) ? QueryOptions.forInternalCalls(cl, boundValues) : QueryOptions.forInternalCalls(cl, serialConsistencyLevel, boundValues);
        ResultMessage result = ClientState.getCQLQueryHandler().process(query, queryState, queryOptions, Collections.EMPTY_MAP, System.nanoTime());
        writetime = queryState.getTimestamp();
        return (result instanceof ResultMessage.Rows) ? UntypedResultSet.create(((ResultMessage.Rows) result).result) : null;
    }

    public boolean processWriteConditional(final ConsistencyLevel cl, final ConsistencyLevel serialCl, final String query, Object... values) {
        return processWriteConditional(cl, serialCl, ClientState.forInternalCalls(), query, values);
    }

    public boolean processWriteConditional(final ConsistencyLevel cl, final ConsistencyLevel serialCl, ClientState clientState, final String query, Object... values)
            throws RequestExecutionException, RequestValidationException, InvalidRequestException, WriteTimeoutException, UnavailableException  {
        try {
            UntypedResultSet result = process(cl, serialCl, clientState, query, new Long(0), values);
            if (serialCl == null)
                return true;

             if (!result.isEmpty()) {
                Row row = result.one();
                if (row.has("[applied]")) {
                     return row.getBoolean("[applied]");
                }
            }
            return false;
        } catch (WriteTimeoutException e) {
            logger.warn("PAXOS write timeout query=" + query + " values=" + Arrays.toString(values)+". Check your cluster load or increase the cas_contention_timeout_in_ms.", e);
            throw e;
        } catch (UnavailableException e) {
            logger.warn("PAXOS quorum not reached query=" + query + " values=" + Arrays.toString(values)+". Check for unavailable nodes.", e);
            throw e;
        } catch (Exception e) {
            logger.error("PAXOS failed query=" + query + " values=" + Arrays.toString(values), e);
            throw e;
        }
    }

    public static String typeToCfName(String keyspaceName, String typeName) {
        return SchemaManager.typeToCfName(keyspaceName, typeName);
    }

    public static String buildIndexName(final String cfName) {
        return SchemaManager.buildIndexName(cfName);
    }

    public static String indexToKsName(String index) {
        return index.replaceAll("\\.", "_").replaceAll("\\-", "_");
    }

    public static int replicationFactor(String keyspace) {
        if (Schema.instance != null && Schema.instance.getKeyspaceInstance(keyspace) != null) {
            AbstractReplicationStrategy replicationStrategy = Schema.instance.getKeyspaceInstance(keyspace).getReplicationStrategy();
            int rf = replicationStrategy.getReplicationFactor();
            if (replicationStrategy instanceof NetworkTopologyStrategy) {
                rf = ((NetworkTopologyStrategy)replicationStrategy).getReplicationFactor(DatabaseDescriptor.getLocalDataCenter());
            }
            return rf;
        }
        return 0;
    }

   public ClusterState updateNumberOfShardsAndReplicas(ClusterState currentState, final String ksName) {
        int numberOfNodes = currentState.nodes().getSize();

        if (numberOfNodes == 0)
            return currentState; // for testing purposes.

        MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
        for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
            IndexMetaData indexMetaData = it.next();
            if (ksName == null || ksName.equals(indexMetaData.keyspace())) {
                IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
                indexMetaDataBuilder.numberOfShards(numberOfNodes);
                int rf = replicationFactor(indexMetaData.keyspace());
                indexMetaDataBuilder.numberOfReplicas( Math.max(0, rf - 1) );
                metaDataBuilder.put(indexMetaDataBuilder.build(), false);
            }
        }
        return ClusterState.builder(currentState).metaData(metaDataBuilder.build()).build();
    }

    public void submitNumberOfShardsAndReplicasUpdate(final String source) {
        submitNumberOfShardsAndReplicasUpdate(source, null);
    }

    public void submitNumberOfShardsAndReplicasUpdate(final String source, final String ksName) {
        submitStateUpdateTask(source, new ClusterStateUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                return updateNumberOfShardsAndReplicas(currentState, ksName);
            }

            @Override
            public void onFailure(String source, Exception t) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected failure during [{}]", source), t);
            }
        });
    }

    public static class BlockingActionListener implements ActionListener<ClusterStateUpdateResponse> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile Throwable error = null;

        public void waitForUpdate(TimeValue timeValue) throws Exception {
            if (timeValue != null && timeValue.millis() > 0) {
                if (!latch.await(timeValue.millis(), TimeUnit.MILLISECONDS)) {
                    throw new ElasticsearchTimeoutException("blocking update timeout");
                }
            } else {
                latch.await();
            }
            if (error != null)
                throw new RuntimeException(error);
        }

        @Override
        public void onResponse(ClusterStateUpdateResponse response) {
            latch.countDown();
        }

        @Override
        public void onFailure(Exception e) {
            error = e;
            latch.countDown();
        }
    }

    public void blockUntilShardsStarted() {
        if (cassandraShardStartedBarrier != null)
            cassandraShardStartedBarrier.blockUntilShardsStarted();
    }

    /**
     * CQL schema update must be asynchronous when triggered by a new dynamic field (see #91)
     * @param indexService
     * @param type
     * @param source
     * @throws Exception
     */
    public void blockingMappingUpdate(IndexService indexService, String type, String source) throws Exception {
        blockingMappingUpdate(indexService, type, source, SchemaUpdate.UPDATE);
    }

    public void blockingMappingUpdate(IndexService indexService, String type, String source, SchemaUpdate schemaUpdate) throws Exception {
        TimeValue timeout = settings.getAsTime(SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT, TimeValue.timeValueSeconds(Integer.getInteger(SETTING_SYSTEM_MAPPING_UPDATE_TIMEOUT, 30)));
        BlockingActionListener mappingUpdateListener = new BlockingActionListener();
        MetaDataMappingService metaDataMappingService = ElassandraDaemon.injector().getInstance(MetaDataMappingService.class);
        PutMappingClusterStateUpdateRequest putRequest = new PutMappingClusterStateUpdateRequest()
                .indices(new org.elasticsearch.index.Index[] {indexService.index()})
                .type(type)
                .source(source)
                .ackTimeout(timeout)
                .masterNodeTimeout(timeout)
                .schemaUpdate(schemaUpdate);
        metaDataMappingService.putMapping(putRequest, mappingUpdateListener, schemaUpdate);
        mappingUpdateListener.waitForUpdate(timeout);
        logger.debug("Cluster state successfully updated for index=[{}], type=[{}], source=[{}] metadata.version={}/{}",
                indexService.index().getName(), type, source, state().metaData().clusterUUID(), state().metaData().version());
    }

    @SuppressForbidden(reason = "toUpperCase() for consistency level")
    public static ConsistencyLevel consistencyLevelFromString(String value) {
        switch(value.toUpperCase(Locale.ROOT)) {
        case "ANY": return ConsistencyLevel.ANY;
        case "ONE": return ConsistencyLevel.ONE;
        case "TWO": return ConsistencyLevel.TWO;
        case "THREE": return ConsistencyLevel.THREE;
        case "QUORUM": return ConsistencyLevel.QUORUM;
        case "ALL": return ConsistencyLevel.ALL;
        case "LOCAL_QUORUM": return ConsistencyLevel.LOCAL_QUORUM;
        case "EACH_QUORUM": return ConsistencyLevel.EACH_QUORUM;
        case "SERIAL": return ConsistencyLevel.SERIAL;
        case "LOCAL_SERIAL": return ConsistencyLevel.LOCAL_SERIAL;
        case "LOCAL_ONE": return ConsistencyLevel.LOCAL_ONE;
        default :
            throw new IllegalArgumentException("No write consistency match [" + value + "]");
        }
    }

    public boolean isDatacenterGroupMember(InetAddress endpoint) {
        String endpointDc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(endpoint);
        KeyspaceMetadata  elasticAdminMetadata = Schema.instance.getKSMetaData(this.elasticAdminKeyspaceName);
        if (elasticAdminMetadata != null) {
            ReplicationParams replicationParams = elasticAdminMetadata.params.replication;
            if (replicationParams.klass == NetworkTopologyStrategy.class && replicationParams.options.get(endpointDc) != null) {
                return true;
            }
        }
        return false;
    }

    private void addMetadataMutations(MetaData metadata, Mutation.SimpleBuilder builder) throws ConfigurationException, IOException {
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(elasticAdminKeyspaceName);
        CFMetaData cfm = ksm.getTableOrViewNullable(ELASTIC_ADMIN_METADATA_TABLE);

        Map<String, ByteBuffer> extensions = new HashMap<>();
        if (cfm.params.extensions != null)
            extensions.putAll(cfm.params.extensions);

        // write cluster state metadata without mappings in the table extensions of elastic_admin, key=metadata
        byte[] metadataBytes = MetaData.Builder.toBytes(metadata, MetaData.CQL_FORMAT_PARAMS);
        extensions.put(ELASTIC_EXTENSION_METADATA, ByteBuffer.wrap(metadataBytes) );
        extensions.put(ELASTIC_EXTENSION_VERSION, ByteBufferUtil.bytes(metadata.version()) );
        extensions.put(ELASTIC_EXTENSION_OWNER, ByteBufferUtil.bytes(SystemKeyspace.getLocalHostId()) );

        SchemaKeyspace.addTableExtensionsToSchemaMutation(cfm, extensions, builder);
    }

    public void writeMetadataToSchemaMutations(MetaData metadata, final Collection<Mutation> mutations, final Collection<Event.SchemaChange> events) throws ConfigurationException, IOException {
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(elasticAdminKeyspaceName);
        Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
        addMetadataMutations(metadata, builder);
        mutations.add(builder.build());
        events.add(new Event.SchemaChange(Event.SchemaChange.Change.UPDATED, Event.SchemaChange.Target.TABLE, elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE));
    }

    // try to read metadata from table extension first, and fall back to comment if absent.
    public MetaData loadGlobalState() throws NoPersistedMetaDataException {
        MetaData metadata;
        try {
            metadata = readMetaDataFromTableExtensions(true);
        } catch(NoPersistedMetaDataException e) {
            // for backward compatibility
            logger.info("Failed to read metadata from table extensions, falling back to table comment");
            metadata = readAndMigrateMetadata();
        }
        return metadata;
    }

    // read metadata from elastic_admin.metadata comment and flush it as CQL table extensions
    private AtomicBoolean migrated = new AtomicBoolean(false);
    private MetaData readAndMigrateMetadata() throws NoPersistedMetaDataException {
        MetaData metadata = readMetaDataAsComment();
        if (migrated.compareAndSet(false, true)) {
            logger.warn("Saving elasticsearch metadata from table comment into table extensions (This should happen only once when upgrading to 6.2.3.8+)");
            try {
                Collection<Mutation> mutations = new ArrayList<>();
                Collection<SchemaChange> events = new ArrayList<>();
                writeMetadataToSchemaMutations(metadata, mutations, events);
                for(ObjectCursor<IndexMetaData> imd : metadata.indices().values())
                    this.getSchemaManager().updateTableExtensions(imd.value, mutations, events);

                //do not announce schema migration because gossip not yet ready.
                SchemaKeyspace.mergeSchema(mutations, getSchemaManager().getInhibitedSchemaListeners());
                logger.warn("Elasticsearch metadata migrated from comment to table extensions (This should happen only once when upgradin to 6.2.3.8+)");
            } catch (Exception e) {
                logger.error("Failed to migrate elasticsearch metadata from comment to table extension.", e);
            }
        }
        return metadata;
    }

    public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException {
        try {
            String query = String.format(Locale.ROOT, "SELECT comment FROM system_schema.tables WHERE keyspace_name='%s' AND table_name='%s'",
                this.elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
            UntypedResultSet result = QueryProcessor.executeInternal(query);
            if (result.isEmpty())
                throw new NoPersistedMetaDataException("Failed to read comment from "+elasticAdminKeyspaceName+"+"+ELASTIC_ADMIN_METADATA_TABLE);

            String metadataString = result.one().getString("comment");
            logger.debug("Recover metadata from {}.{} = {}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE, metadataString);
            return parseMetaDataString( metadataString );
        } catch (RequestValidationException | RequestExecutionException e) {
            throw new NoPersistedMetaDataException("Failed to read comment from "+elasticAdminKeyspaceName+"+"+ELASTIC_ADMIN_METADATA_TABLE, e);
        }
    }

    public static String metaDataAsString(ClusterState state) {
        try {
            return MetaData.Builder.toXContent(state.metaData(), MetaData.CASSANDRA_FORMAT_PARAMS);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed covert metadata to JSON", e);
        }
    }

    public Pair<UUID, Long> readUUIDAndVersion() throws NoPersistedMetaDataException {
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(this.elasticAdminKeyspaceName);
        if (ksm == null)
            return null;

        CFMetaData cfm = ksm.getTableOrViewNullable(ELASTIC_ADMIN_METADATA_TABLE);
        if (cfm != null &&
                cfm.params.extensions != null &&
                cfm.params.extensions.get(ELASTIC_EXTENSION_VERSION) != null &&
                cfm.params.extensions.get(ELASTIC_EXTENSION_OWNER) != null) {
            return Pair.create(
                    UUIDSerializer.instance.deserialize(cfm.params.extensions.get(ELASTIC_EXTENSION_OWNER)),
                    ByteBufferUtil.toLong(cfm.params.extensions.get(ELASTIC_EXTENSION_VERSION)));
        }
        throw new NoPersistedMetaDataException("No extension found for table "+this.elasticAdminKeyspaceName+"."+ELASTIC_ADMIN_METADATA_TABLE);
    }

    public MetaData readMetaData(CFMetaData cfm) {
        try {
            byte[] bytes = ByteBufferUtil.getArray(cfm.params.extensions.get(ClusterService.ELASTIC_EXTENSION_METADATA));
            return metaStateService.loadGlobalState(bytes);
        } catch (IOException e) {
            throw new ElasticsearchException("Failed to deserialize metadata", e);
        }
    }

    public boolean isValidTypeExtension(String extensionName) {
        return extensionName != null && extensionName.startsWith(getElasticAdminKeyspaceName() + "/");
    }

    public String getIndexNameFromExtensionName(String extensionName) {
        return (isValidTypeExtension(extensionName)) ?
                extensionName.substring(getElasticAdminKeyspaceName().length() + 1) :
                null;
    }

    public void putIndexMetaDataExtension(IndexMetaData indexMetaData, Map<String, ByteBuffer> extensions) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.SMILE);
            builder.startObject();
            IndexMetaData.Builder.toXContent(indexMetaData, builder, MetaData.CASSANDRA_FORMAT_PARAMS);
            builder.endObject();
            extensions.put(getElasticAdminKeyspaceName() + "/" + indexMetaData.getIndex().getName(), ByteBuffer.wrap( BytesReference.toBytes(builder.bytes()) ));
        } catch (IOException e) {
            throw new ElasticsearchException("Failed to serialize index metadata", e);
        }
    }

    public IndexMetaData getIndexMetaDataFromExtension(ByteBuffer value) {
        try {
            return metaStateService.loadIndexState(ByteBufferUtil.getArray(value));
        } catch (IOException e) {
            throw new ElasticsearchException("Failed to deserialize metadata", e);
        }
    }

    public boolean hasMetaDataTable() {
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(this.elasticAdminKeyspaceName);
        return ksm != null && ksm.getTableOrViewNullable(ELASTIC_ADMIN_METADATA_TABLE) != null;
    }

    /**
     * Scan CQL table extensions to build Elasticsearch metadata:
     * - global metadata is located in elastic_admin.metadata extensions, with key = "metadata".
     *   The global metadata contains indices definition with no mapping (no keep index definition with no mapping).
     * - each indexed table have an extension with a SMILE serialized IndexMetaData having only one mapping.
     * When reloading an index table extension from the schema, mapping is added to the global index entry.
     * If no global index entry exists, the one from the table level is used (when restoring CQL table with index+mapping definition)
     *
     * @return
     * @throws NoPersistedMetaDataException
     */
    public MetaData readMetaDataFromTableExtensions(boolean full) throws NoPersistedMetaDataException {
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(this.elasticAdminKeyspaceName);
        if (ksm == null)
            throw new NoPersistedMetaDataException("Keyspace "+this.elasticAdminKeyspaceName+" metadata not available");

        CFMetaData cfm = ksm.getTableOrViewNullable(ELASTIC_ADMIN_METADATA_TABLE);
        if (cfm != null && cfm.params.extensions != null) {
            MetaData metaData;
            try {
                metaData =  readMetaData(cfm);
                logger.trace("metadata.version={}", metaData.version());

                if (full) {
                    // load table extensions for tables having an elastic 2i index and having a valide table extension.
                    ListMultimap<String, IndexMetaData> indexMetaDataExtensions = ArrayListMultimap.create();
                    for(String keyspace : Schema.instance.getUserKeyspaces()) {
                        KeyspaceMetadata ksmx = Schema.instance.getKSMetaData(keyspace);
                        if (ksmx != null) {
                            logger.trace("ksmx={} indices={}", ksmx.name, ksmx.existingIndexNames(null));
                            for(String indexName : ksmx.existingIndexNames(null)) {
                                Optional<CFMetaData> cfmOption = ksmx.findIndexedTable(indexName);
                                if (indexName.startsWith("elastic_") && cfmOption.isPresent()) {
                                    CFMetaData cfmx = cfmOption.get();
                                    if (cfmx.params.extensions != null) {
                                        logger.trace("ks.cf={} metadata.version={} extensions={}", ksmx.name, cfmx.cfName, metaData.version(), cfmx.params.extensions);
                                        for(Map.Entry<String, ByteBuffer> entry : cfmx.params.extensions.entrySet()) {
                                            if (isValidTypeExtension(entry.getKey())) {
                                                IndexMetaData indexMetaData = getIndexMetaDataFromExtension(entry.getValue());
                                                indexMetaDataExtensions.put(indexMetaData.getIndex().getName(), indexMetaData);

                                                // initialize typeToCfName map for later reverse lookup in ElasticSecondaryIndex
                                                schemaManager.typeToCfName(cfmx, keyspace, false);
                                            }
                                        }
                                    } else {
                                        logger.warn("No extentions for index.type={}.{}", keyspace, cfmx.cfName);
                                    }
                                }
                            }
                        }
                    }
                    if (indexMetaDataExtensions.size() > 0) {
                        MetaData.Builder metaDataBuilder = MetaData.builder(metaData);
                        for(String indexName : indexMetaDataExtensions.keySet()) {
                            // merge all IndexMetadata for single type to a multi-typed IndexMetaData (for baward compatibility with version 5)
                            IndexMetaData indexMetaData = metaDataBuilder.get(indexName);  // reuse first the global IndexMetaData defeintion with no mapping
                            if (indexMetaData == null)
                                indexMetaData = indexMetaDataExtensions.get(indexName).get(0); // fall-back to the table level index definition

                            IndexMetaData.Builder indexMetaDataBuilder = new IndexMetaData.Builder(indexMetaData);
                            for(IndexMetaData imd : indexMetaDataExtensions.get(indexName)) {
                                for(ObjectCursor<MappingMetaData> m : imd.getMappings().values())
                                    indexMetaDataBuilder.putMapping(m.value);
                            }
                            metaDataBuilder.put(indexMetaDataBuilder);
                        }
                        metaData = metaDataBuilder.build();
                    }
                }
            } catch (Exception e) {
                throw new NoPersistedMetaDataException("Failed to parse metadata extentions", e);
            }
            logger.info("Elasticsearch metadata succesfully loaded from CQL table extensions metadata.version={}", metaData.version());
            logger.trace("metadata={}", metaData);
            return metaData;
        }
        throw new NoPersistedMetaDataException("No extension found for table "+this.elasticAdminKeyspaceName+"."+ELASTIC_ADMIN_METADATA_TABLE);
    }

    // merge IndexMetaData from table extensions into the provided MetaData.
    public MetaData.Builder mergeWithTableExtensions(final MetaData.Builder metaDataBuilder)  {
        final ListMultimap<String, IndexMetaData> indexMetaDataExtensions = ArrayListMultimap.create();
        for(String keyspace : Schema.instance.getUserKeyspaces()) {
            KeyspaceMetadata ksmx = Schema.instance.getKSMetaData(keyspace);
            if (ksmx != null) {
                if (logger.isTraceEnabled())
                    logger.trace("ksmx={} indices={}", ksmx.name, ksmx.existingIndexNames(null));
                for(String indexName : ksmx.existingIndexNames(null)) {
                    Optional<CFMetaData> cfmOption = ksmx.findIndexedTable(indexName);
                    if (indexName.startsWith("elastic_") && cfmOption.isPresent()) {
                        CFMetaData cfmx = cfmOption.get();
                        if (cfmx.params.extensions != null) {
                            if (logger.isTraceEnabled())
                                logger.trace("ks.cf={} extensions={}", ksmx.name, cfmx.cfName,cfmx.params.extensions);
                            for(Map.Entry<String, ByteBuffer> entry : cfmx.params.extensions.entrySet()) {
                                if (isValidTypeExtension(entry.getKey())) {
                                    IndexMetaData indexMetaData = getIndexMetaDataFromExtension(entry.getValue());
                                    indexMetaDataExtensions.put(indexMetaData.getIndex().getName(), indexMetaData);

                                    // initialize typeToCfName map for later reverse lookup in ElasticSecondaryIndex
                                    schemaManager.typeToCfName(cfmx, keyspace, false);
                                }
                            }
                        } else {
                            logger.warn("No extentions for index.type={}.{}", keyspace, cfmx.cfName);
                        }
                    }
                }
            }
        }
        if (indexMetaDataExtensions.size() > 0) {
            for(String indexName : indexMetaDataExtensions.keySet()) {
                // merge all IndexMetadata for single type to a multi-typed IndexMetaData (for baward compatibility with version 5)
                mergeIndexMetaData(metaDataBuilder, indexName, indexMetaDataExtensions.get(indexName));
            }
        }
        return metaDataBuilder;
    }

    // merge all mappings into the provided IndexMetadata
    public MetaData.Builder mergeIndexMetaData(final MetaData.Builder metaDataBuilder, final String indexName, final List<IndexMetaData> mappings) {
        if (mappings.size() == 0)
            return metaDataBuilder;

        IndexMetaData base = metaDataBuilder.get(indexName);  // reuse first the global IndexMetaData defeintion with no mapping
        IndexMetaData.Builder indexMetaDataBuilder = (base != null) ? IndexMetaData.builder(base) : IndexMetaData.builder(mappings.get(0));
        for(int i = (base == null) ? 1 : 0; i < mappings.size(); i++) {
            for(ObjectCursor<MappingMetaData> md : mappings.get(i).getMappings().values())
                indexMetaDataBuilder.putMapping(md.value);
        }
        return metaDataBuilder.put(indexMetaDataBuilder);
    }

    private MetaData parseMetaDataString(String metadataString) throws NoPersistedMetaDataException {
        if (metadataString != null && metadataString.length() > 0) {
            MetaData metaData;
            try {
                metaData =  metaStateService.loadGlobalState(metadataString);

                // initialize typeToCfName map for later reverse lookup in ElasticSecondaryIndex
                for(ObjectCursor<IndexMetaData> indexCursor : metaData.indices().values()) {
                    for(ObjectCursor<MappingMetaData> mappingCursor :  indexCursor.value.getMappings().values()) {
                        String cfName = SchemaManager.typeToCfName(indexCursor.value.keyspace(), mappingCursor.value.type());
                        if (logger.isDebugEnabled())
                            logger.debug("keyspace.table={}.{} registred for elasticsearch index.type={}.{}",
                                    indexCursor.value.keyspace(), cfName, indexCursor.value.getIndex().getName(), mappingCursor.value.type());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to parse metadata={}", e, metadataString);
                throw new NoPersistedMetaDataException("Failed to parse metadata="+metadataString, e);
            }
            return metaData;
        }
        throw new NoPersistedMetaDataException("metadata null or empty");
    }

    public MetaData readInternalMetaDataAsRow() throws NoPersistedMetaDataException {
        try {
            UntypedResultSet rs = QueryProcessor.executeInternal(selectMetadataQuery, DatabaseDescriptor.getClusterName());
            if (rs != null && !rs.isEmpty()) {
                Row row = rs.one();
                if (row.has("metadata"))
                    return parseMetaDataString(row.getString("metadata"));
            }
        } catch (Exception e) {
            logger.warn("Cannot read metadata locally",e);
        }
        return null;
    }

    public MetaData readMetaDataAsRow(ConsistencyLevel cl) throws NoPersistedMetaDataException {
        try {
            UntypedResultSet rs = process(cl, ClientState.forInternalCalls(), selectMetadataQuery, DatabaseDescriptor.getClusterName());
            if (rs != null && !rs.isEmpty()) {
                Row row = rs.one();
                if (row.has("metadata"))
                    return parseMetaDataString(row.getString("metadata"));
            }
        } catch (UnavailableException e) {
            logger.warn("Cannot read elasticsearch metadata with consistency="+cl, e);
            return null;
        } catch (KeyspaceNotDefinedException e) {
            logger.warn("Keyspace {} not yet defined", elasticAdminKeyspaceName);
            return null;
        } catch (Exception e) {
            throw new NoPersistedMetaDataException("Unexpected error",e);
        }
        throw new NoPersistedMetaDataException("No elasticsearch metadata available");
    }

    public Long readMetaDataVersion(ConsistencyLevel cl) throws NoPersistedMetaDataException {
        try {
            UntypedResultSet rs = process(cl, ClientState.forInternalCalls(), selectVersionMetadataQuery, DatabaseDescriptor.getClusterName());
            if (rs != null && !rs.isEmpty()) {
                Row row = rs.one();
                if (row.has("version"))
                    return row.getLong("version");
            }
        } catch (Exception e) {
            logger.warn("unexpected error", e);
        }
        return -1L;
    }
    public static String getElasticsearchClusterName(Settings settings) {
        String clusterName = DatabaseDescriptor.getClusterName();
        String datacenterGroup = settings.get(ClusterService.SETTING_CLUSTER_DATACENTER_GROUP);
        if (datacenterGroup != null) {
            clusterName = DatabaseDescriptor.getClusterName() + "@" + datacenterGroup.trim();
        }
        return clusterName;
    }

    public int getLocalDataCenterSize() {
        int count = 1;
        for (UntypedResultSet.Row row : executeInternal("SELECT data_center, rpc_address FROM system." + SystemKeyspace.PEERS))
            if (row.has("rpc_address") && row.has("data_center") && DatabaseDescriptor.getLocalDataCenter().equals(row.getString("data_center")))
                count++;
        logger.info(" datacenter=[{}] size={} from peers", DatabaseDescriptor.getLocalDataCenter(), count);
        return count;
    }


    Void createElasticAdminKeyspace()  {
        try {
            Map<String, String> replication = new HashMap<String, String>();

            replication.put("class", NetworkTopologyStrategy.class.getName());
            replication.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(getLocalDataCenterSize()));

            String createKeyspace = String.format(Locale.ROOT, "CREATE KEYSPACE IF NOT EXISTS \"%s\" WITH replication = %s;",
                elasticAdminKeyspaceName, FBUtilities.json(replication).replaceAll("\"", "'"));
            logger.info(createKeyspace);
            process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), createKeyspace);
        } catch (Exception e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to initialize keyspace {}", elasticAdminKeyspaceName), e);
            throw e;
        }
        return null;
    }


    // Modify keyspace replication
    public void alterKeyspaceReplicationFactor(String keyspaceName, int rf) {
        ReplicationParams replication = Schema.instance.getKSMetaData(keyspaceName).params.replication;

        if (!NetworkTopologyStrategy.class.getName().equals(replication.klass))
            throw new ConfigurationException("Keyspace ["+keyspaceName+"] should use "+NetworkTopologyStrategy.class.getName()+" replication strategy");

        Map<String, String> repMap = replication.asMap();

        if (!repMap.containsKey(DatabaseDescriptor.getLocalDataCenter()) || !Integer.toString(rf).equals(repMap.get(DatabaseDescriptor.getLocalDataCenter()))) {
            repMap.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(rf));
            logger.debug("Updating keyspace={} replication={}", keyspaceName, repMap);
            try {
                String query = String.format(Locale.ROOT, "ALTER KEYSPACE \"%s\" WITH replication = %s",
                        keyspaceName, FBUtilities.json(repMap).replaceAll("\"", "'"));
                logger.info(query);
                process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), query);
            } catch (Throwable e) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to alter keyspace [{}]",keyspaceName), e);
                throw e;
            }
        } else {
            logger.info("Keep unchanged keyspace={} datacenter={} RF={}", keyspaceName, DatabaseDescriptor.getLocalDataCenter(), rf);
        }
    }

    // Create The meta Data Table if needed
    Void createElasticAdminMetaTable() {
        try {
            String createTable = String.format(Locale.ROOT, "CREATE TABLE IF NOT EXISTS \"%s\".%s ( cluster_name text PRIMARY KEY, owner uuid, version bigint);",
                elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
            logger.info(createTable);
            process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), createTable);
        } catch (Exception e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to initialize table {}.{}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE), e);
            throw e;
        }
        return null;
    }

    // initialize a first row if needed
    Void insertFirstMetaRow(final MetaData metadata) {
        try {
            logger.info(insertMetadataQuery);
            process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), insertMetadataQuery,
                DatabaseDescriptor.getClusterName(), UUID.fromString(StorageService.instance.getLocalHostId()), metadata.version());
        } catch (Exception e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed insert first row into table {}.{}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE), e);
            throw e;
        }
        return null;
    }

    void retry (final Supplier<Void> function, final String label) {
        for (int i = 0; ; ++i) {
            try {
                function.get();
                break;
            } catch (final Exception e) {
                if (i >= CREATE_ELASTIC_ADMIN_RETRY_ATTEMPTS) {
                    logger.error("Failed to {} after {} attempts", label, CREATE_ELASTIC_ADMIN_RETRY_ATTEMPTS);
                    throw new NoPersistedMetaDataException("Failed to " + label + " after " + CREATE_ELASTIC_ADMIN_RETRY_ATTEMPTS + " attempts", e);
                } else
                    logger.info("Retrying: {}", label);
            }
        }
    }

    /**
     * Create or update elastic_admin keyspace.
     */

    public void createOrUpdateElasticAdminKeyspace()  {
        UntypedResultSet result = QueryProcessor.executeOnceInternal(String.format(Locale.ROOT, "SELECT replication FROM system_schema.keyspaces WHERE keyspace_name='%s'", elasticAdminKeyspaceName));
        if (result.isEmpty()) {
            MetaData metadata = state().metaData();
            try {
                // create elastic_admin if not exists after joining the ring and before allowing metadata update.
                retry(() -> createElasticAdminKeyspace(), "create elastic admin keyspace");
                retry(() -> createElasticAdminMetaTable(), "create elastic admin metadata table");
                retry(() -> insertFirstMetaRow(metadata), "write first row to metadata table");
                logger.info("Succefully initialize {}.{} = {}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE, metadata.toString());
                try {
                    Collection<Mutation> mutations = new ArrayList<>();
                    Collection<SchemaChange> events = new ArrayList<>();
                    writeMetadataToSchemaMutations(state().metaData(), mutations, events);
                    logger.debug("Applying initial elasticsearch mapping mutations={}", mutations);
                    MigrationManager.announce(mutations, this.getSchemaManager().getInhibitedSchemaListeners());
                } catch (IOException e) {
                    logger.error("Failed to write metadata as table extension", e);
                }
            } catch (Throwable e) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to initialize table {}.{}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE),e);
            }
        } else {
            Map<String,String> replication = result.one().getFrozenTextMap("replication");
            logger.debug("keyspace={} replication={}", elasticAdminKeyspaceName, replication);

            if (!NetworkTopologyStrategy.class.getName().equals(replication.get("class")))
                    throw new ConfigurationException("Keyspace ["+this.elasticAdminKeyspaceName+"] should use "+NetworkTopologyStrategy.class.getName()+" replication strategy");

            int currentRF = -1;
            if (replication.get(DatabaseDescriptor.getLocalDataCenter()) != null) {
                currentRF = Integer.valueOf(replication.get(DatabaseDescriptor.getLocalDataCenter()).toString());
            }
            int targetRF = getLocalDataCenterSize();
            if (targetRF != currentRF) {
                replication.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(targetRF));
                try {
                    String query = String.format(Locale.ROOT, "ALTER KEYSPACE \"%s\" WITH replication = %s",
                            elasticAdminKeyspaceName, FBUtilities.json(replication).replaceAll("\"", "'"));
                    logger.info(query);
                    process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), query);
                } catch (Throwable e) {
                    logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to alter keyspace [{}]", elasticAdminKeyspaceName), e);
                }
            } else {
                logger.info("Keep unchanged keyspace={} datacenter={} RF={}", elasticAdminKeyspaceName, DatabaseDescriptor.getLocalDataCenter(), targetRF);
            }
        }
    }


    public ShardInfo shardInfo(String index, ConsistencyLevel cl) {
        Keyspace keyspace = Schema.instance.getKeyspaceInstance(state().metaData().index(index).keyspace());
        AbstractReplicationStrategy replicationStrategy = keyspace.getReplicationStrategy();
        int rf = replicationStrategy.getReplicationFactor();
        if (replicationStrategy instanceof NetworkTopologyStrategy)
            rf = ((NetworkTopologyStrategy)replicationStrategy).getReplicationFactor(DatabaseDescriptor.getLocalDataCenter());
        return new ShardInfo(rf, cl.blockFor(keyspace));
    }


    public void commitMetaData(MetaData oldMetaData, MetaData newMetaData, String source) throws ConcurrentMetaDataUpdateException, UnavailableException, IOException {
        if (!newMetaData.clusterUUID().equals(localNode().getId())) {
            logger.error("should not push metadata updated from another node {}/{}", newMetaData.clusterUUID(), newMetaData.version());
            return;
        }
        if (newMetaData.clusterUUID().equals(state().metaData().clusterUUID()) && newMetaData.version() < state().metaData().version()) {
            logger.warn("don't push obsolete metadata uuid={} version {} < {}", newMetaData.clusterUUID(), newMetaData.version(), state().metaData().version());
            return;
        }

        String metaDataString = MetaData.Builder.toXContent(newMetaData, MetaData.CASSANDRA_FORMAT_PARAMS);
        UUID owner = UUID.fromString(localNode().getId());
        boolean applied = processWriteConditional(
                this.metadataWriteCL,
                this.metadataSerialCL,
                ClientState.forInternalCalls(),
                updateMetaDataQuery,
                new Object[] { owner, newMetaData.version(), DatabaseDescriptor.getClusterName(), newMetaData.version() - 1 });
        if (applied) {
            logger.debug("PAXOS Succefully update metadata source={} nextMetaData={}", source, newMetaData.x2());
            return;
        } else {
            logger.warn("PAXOS Failed to update metadata source={} prevMetadata={} nextMetaData={}",source, oldMetaData.x2(), newMetaData.x2());
            throw new ConcurrentMetaDataUpdateException(owner, newMetaData.version());
        }
    }

    /**
     * Duplicate code from org.apache.cassandra.service.StorageService.setLoggingLevel, allowing to set log level without StorageService.instance for tests.
     * @param classQualifier
     * @param rawLevel
     */
    public static void setLoggingLevel(String classQualifier, String rawLevel)
    {
        ch.qos.logback.classic.Logger logBackLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(classQualifier);

        // if both classQualifer and rawLevel are empty, reload from configuration
        if (StringUtils.isBlank(classQualifier) && StringUtils.isBlank(rawLevel) )
        {
            try {
                JMXConfiguratorMBean jmxConfiguratorMBean = JMX.newMBeanProxy(ManagementFactory.getPlatformMBeanServer(),
                        new ObjectName("ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator"),
                        JMXConfiguratorMBean.class);
                jmxConfiguratorMBean.reloadDefaultConfiguration();
                return;
            } catch (MalformedObjectNameException | JoranException e) {
                throw new RuntimeException(e);
            }
        }
        // classQualifer is set, but blank level given
        else if (StringUtils.isNotBlank(classQualifier) && StringUtils.isBlank(rawLevel) )
        {
            if (logBackLogger.getLevel() != null || hasAppenders(logBackLogger))
                logBackLogger.setLevel(null);
            return;
        }

        ch.qos.logback.classic.Level level = ch.qos.logback.classic.Level.toLevel(rawLevel);
        logBackLogger.setLevel(level);
    }

    private static boolean hasAppenders(ch.qos.logback.classic.Logger logger)
    {
        Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
        return it.hasNext();
    }

    public IndexService indexService(org.elasticsearch.index.Index index) {
        return this.indicesService.indexService(index);
    }

    public IndexService indexServiceSafe(org.elasticsearch.index.Index index) {
        return this.indicesService.indexServiceSafe(index);
    }

    /**
     * Return a set of started shards according t the gossip state map and the local shard state.
     */
    public ShardRoutingState getShardRoutingStates(org.elasticsearch.index.Index index, UUID nodeUuid) {
        if (nodeUuid.equals(this.localNode().uuid())) {
            if (this.discovery.isSearchEnabled()) {
                try {
                    IndexShard localIndexShard = indexServiceSafe(index).getShardOrNull(0);
                    if (localIndexShard != null && localIndexShard.routingEntry() != null)
                        return localIndexShard.routingEntry().state();

                    // shardRouting not yet created.
                    return ShardRoutingState.INITIALIZING;
                } catch (IndexNotFoundException e) {
                }
            }
            return ShardRoutingState.UNASSIGNED;
        }

        // read-only map.
        Map<String, ShardRoutingState> shards = (this.discovery).getShardRoutingState(nodeUuid);
        if (shards == null) {
            if (logger.isDebugEnabled() && state().nodes().get(nodeUuid.toString()).status().equals(DiscoveryNodeStatus.ALIVE))
                logger.debug("No ShardRoutingState for alive node=[{}]",nodeUuid.toString());
            return ShardRoutingState.UNASSIGNED;
        }
        return shards.get(index.getName());
    }

    /**
     * Set index shard state in the gossip endpoint map (must be synchronized).
     */
    public void publishShardRoutingState(final String index, final ShardRoutingState shardRoutingState) throws JsonGenerationException, JsonMappingException, IOException {
        if (this.discovery != null)
            this.discovery.publishShardRoutingState(index, shardRoutingState);
    }

    /**
     * Publish cluster metadata uuid and version in gossip state.
     */
    public void publishX2(final ClusterState clusterState) {
        if (this.discovery != null)
            this.discovery.publishX2(clusterState);
    }

    @Override
    public DiscoveryNode localNode() {
        return (this.discovery != null) ? this.discovery.localNode() : this.getClusterApplierService().state().nodes().getLocalNode();
    }
}
