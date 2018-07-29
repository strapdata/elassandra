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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.QueryOptions;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.cql3.statements.IndexTarget;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.cql3.statements.TableAttributes;
import org.apache.cassandra.db.CBuilder;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.KeyspaceNotDefinedException;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.TupleType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.exceptions.WriteTimeoutException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.schema.ReplicationParams;
import org.apache.cassandra.schema.TableParams;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.MapSerializer;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.serializers.SimpleDateSerializer;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.ElassandraDaemon;
import org.apache.cassandra.service.MigrationManager;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elassandra.ConcurrentMetaDataUpdateException;
import org.elassandra.NoPersistedMetaDataException;
import org.elassandra.cluster.routing.AbstractSearchStrategy;
import org.elassandra.cluster.routing.PrimaryFirstSearchStrategy;
import org.elassandra.discovery.CassandraDiscovery;
import org.elassandra.index.ExtendedElasticSecondaryIndex;
import org.elassandra.index.mapper.internal.NodeFieldMapper;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elassandra.index.search.TokenRangesService;
import org.elassandra.indices.CassandraSecondaryIndicesApplier;
import org.elassandra.shard.CassandraShardStartedBarrier;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingClusterStateUpdateRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateApplier;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.ClusterStateTaskConfig;
import org.elasticsearch.cluster.ClusterStateTaskListener;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.ack.ClusterStateUpdateRequest;
import org.elasticsearch.cluster.ack.ClusterStateUpdateResponse;
import org.elasticsearch.cluster.action.index.MappingUpdatedAction;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.metadata.MetaDataMappingService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNode.DiscoveryNodeStatus;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.OperationRouting;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
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
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.BaseGeoPointFieldMapper;
import org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.LegacyGeoPointFieldType;
import org.elasticsearch.index.mapper.CompletionFieldMapper;
import org.elasticsearch.index.mapper.DateFieldMapper;
import org.elasticsearch.index.mapper.DocumentFieldMappers;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.GeoShapeFieldMapper;
import org.elasticsearch.index.mapper.IdFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.CqlCollection;
import org.elasticsearch.index.mapper.Mapper.CqlStruct;
import org.elasticsearch.index.mapper.MapperException;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.ParentFieldMapper;
import org.elasticsearch.index.mapper.RoutingFieldMapper;
import org.elasticsearch.index.mapper.SourceFieldMapper;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.index.mapper.TTLFieldMapper;
import org.elasticsearch.index.mapper.TimestampFieldMapper;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static java.util.Collections.emptyList;
import static org.apache.cassandra.cql3.QueryProcessor.executeInternal;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.common.settings.Setting.listSetting;


public class ClusterService extends org.elasticsearch.cluster.service.BaseClusterService {

    public static final String ELASTIC_ID_COLUMN_NAME = "_id";
    public static final String ELASTIC_ADMIN_KEYSPACE = "elastic_admin";
    public static final String ELASTIC_ADMIN_METADATA_TABLE = "metadata";

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
    
    // elassandra cluster settings
    public static final String SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT = CLUSTER_PREFIX+MAPPING_UPDATE_TIMEOUT;
    public static final String SETTING_CLUSTER_SECONDARY_INDEX_CLASS = CLUSTER_PREFIX+SECONDARY_INDEX_CLASS;
    public static final String SETTING_CLUSTER_SEARCH_STRATEGY_CLASS = CLUSTER_PREFIX+SEARCH_STRATEGY_CLASS;
    public static final String SETTING_CLUSTER_INCLUDE_NODE_ID = CLUSTER_PREFIX+INCLUDE_NODE_ID;
    public static final String SETTING_CLUSTER_INDEX_ON_COMPACTION = CLUSTER_PREFIX+INDEX_ON_COMPACTION;
    public static final String SETTING_CLUSTER_SYNCHRONOUS_REFRESH = CLUSTER_PREFIX+SYNCHRONOUS_REFRESH;
    public static final String SETTING_CLUSTER_DROP_ON_DELETE_INDEX = CLUSTER_PREFIX+DROP_ON_DELETE_INDEX;
    public static final String SETTING_CLUSTER_SNAPSHOT_WITH_SSTABLE = CLUSTER_PREFIX+SNAPSHOT_WITH_SSTABLE;
    public static final String SETTING_CLUSTER_VERSION_LESS_ENGINE = CLUSTER_PREFIX+VERSION_LESS_ENGINE; 
    public static final String SETTING_CLUSTER_TOKEN_PRECISION_STEP = CLUSTER_PREFIX+TOKEN_PRECISION_STEP;
    public static final String SETTING_CLUSTER_TOKEN_RANGES_BITSET_CACHE = CLUSTER_PREFIX+TOKEN_RANGES_BITSET_CACHE;
    
    public static int defaultPrecisionStep = Integer.getInteger(SETTING_SYSTEM_TOKEN_PRECISION_STEP, 6);
    
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
    }
    
    
    public static Map<String, String> cqlMapping = new ImmutableMap.Builder<String,String>()
            .put("text", "keyword")
            .put("varchar", "keyword")
            .put("timestamp", "date")
            .put("date", "date")
            .put("time", "long")
            .put("int", "integer")
            .put("double", "double")
            .put("float", "float")
            .put("bigint", "long")
            .put("smallint", "short")
            .put("tinyint", "byte")
            .put("boolean", "boolean")
            .put("blob", "binary")
            .put("inet", "ip" )
            .put("uuid", "keyword" )
            .put("timeuuid", "keyword" )
            .build();

    private MetaStateService metaStateService;
    private IndicesService indicesService;
    private CassandraDiscovery discovery;
    
    private final TokenRangesService tokenRangeService;
    private final CassandraSecondaryIndicesApplier cassandraSecondaryIndicesApplier;
    
    // manage asynchronous CQL schema update
    protected final AtomicReference<MetadataSchemaUpdate> lastMetadataToSave = new AtomicReference<MetadataSchemaUpdate>(null);
    protected final Semaphore metadataToSaveSemaphore = new Semaphore(0);
    
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
    
    private volatile CassandraShardStartedBarrier shardStartedBarrier;
    private final OperationRouting operationRouting;

    @Inject
    public ClusterService(Settings settings, ClusterSettings clusterSettings, ThreadPool threadPool, Supplier<DiscoveryNode> localNodeSupplier) {
        super(settings, clusterSettings, threadPool, localNodeSupplier);
        this.mappingUpdatedAction = null;
        this.tokenRangeService = new TokenRangesService(settings);
        this.cassandraSecondaryIndicesApplier = new CassandraSecondaryIndicesApplier(settings, this);
        this.operationRouting = new OperationRouting(settings, clusterSettings, this);
        
        String datacenterGroup = settings.get(SETTING_CLUSTER_DATACENTER_GROUP);
        if (datacenterGroup != null && datacenterGroup.length() > 0) {
            logger.info("Starting with datacenter.group=[{}]", datacenterGroup.trim().toLowerCase(Locale.ROOT));
            elasticAdminKeyspaceName = String.format(Locale.ROOT, "%s_%s", ELASTIC_ADMIN_KEYSPACE,datacenterGroup.trim().toLowerCase(Locale.ROOT));
        } else {
            elasticAdminKeyspaceName = ELASTIC_ADMIN_KEYSPACE;
        }
        selectMetadataQuery = String.format(Locale.ROOT, "SELECT metadata,version,owner FROM \"%s\".\"%s\" WHERE cluster_name = ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        selectVersionMetadataQuery = String.format(Locale.ROOT, "SELECT version FROM \"%s\".\"%s\" WHERE cluster_name = ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        insertMetadataQuery = String.format(Locale.ROOT, "INSERT INTO \"%s\".\"%s\" (cluster_name,owner,version,metadata) VALUES (?,?,?,?) IF NOT EXISTS", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
        updateMetaDataQuery = String.format(Locale.ROOT, "UPDATE \"%s\".\"%s\" SET owner = ?, version = ?, metadata = ? WHERE cluster_name = ? IF version < ?", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE);
    }
    
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
    
    public void setDiscovery(Discovery discovery) {
        this.discovery = (CassandraDiscovery)discovery;
    }
    
    
    public TokenRangesService tokenRangesService() {
        return this.tokenRangeService;
    }
    
    public void addShardStartedBarrier() {
        this.shardStartedBarrier = new CassandraShardStartedBarrier(settings, this);
    }
    
    public void removeShardStartedBarrier() {
        this.shardStartedBarrier = null;
    }
    
    public void blockUntilShardsStarted() {
        if (shardStartedBarrier != null) {
            shardStartedBarrier.blockUntilShardsStarted();
        }
    }
    
    public String getElasticAdminKeyspaceName() {
        return this.elasticAdminKeyspaceName;
    }
    
    public boolean isNativeCql3Type(String cqlType) {
        return cqlMapping.keySet().contains(cqlType) && !cqlType.startsWith("geo_");
    }
    
    public Class<? extends AbstractSearchStrategy> searchStrategyClass(IndexMetaData indexMetaData, ClusterState state) {
        try {
            return AbstractSearchStrategy.getSearchStrategyClass( 
                    indexMetaData.getSettings().get(IndexMetaData.SETTING_SEARCH_STRATEGY_CLASS, 
                    state.metaData().settings().get(ClusterService.SETTING_CLUSTER_SEARCH_STRATEGY_CLASS,PrimaryFirstSearchStrategy.class.getName()))
                    );
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
            logger.debug("processing CL={} SERIAL_CL={} query={}", cl, serialConsistencyLevel, query);
        
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
            throws RequestExecutionException, RequestValidationException, InvalidRequestException {
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
            logger.warn("PAXOS phase failed query=" + query + " values=" + Arrays.toString(values), e);
            return false;
        } catch (UnavailableException e) {
            logger.warn("PAXOS commit failed query=" + query + " values=" + Arrays.toString(values), e);
            return false;
        } catch (Exception e) {
            logger.error("Failed to process query=" + query + " values=" + Arrays.toString(values), e);
            throw e;
        }
    }

    
    /**
     * Don't use QueryProcessor.executeInternal, we need to propagate this on all nodes.
     **/
    public void createIndexKeyspace(final String ksname, final int replicationFactor, final Map<String, Integer> replicationMap) throws IOException {
        Keyspace ks = null;
        try {
            ks = Keyspace.open(ksname);
            if (ks != null && !(ks.getReplicationStrategy() instanceof NetworkTopologyStrategy)) {
                throw new IOException("Cannot create index, underlying keyspace requires the NetworkTopologyStrategy.");
            }
        } catch(AssertionError | NullPointerException e) {
        }

        try {
            if (ks == null) {
                Map<String, String> replication = new HashMap<>();
                replication.put("class", "NetworkTopologyStrategy");
                replication.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(replicationFactor));
                for(Map.Entry<String, Integer> entry : replicationMap.entrySet())
                    replication.put(entry.getKey(), Integer.toString(entry.getValue()));
                process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), 
                    String.format(Locale.ROOT, "CREATE KEYSPACE IF NOT EXISTS \"%s\" WITH replication = %s", 
                            ksname,  FBUtilities.json(replication).replaceAll("\"", "'")));
            }
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    
    public void dropIndexKeyspace(final String ksname) throws IOException {
        try {
            String query = String.format(Locale.ROOT, "DROP KEYSPACE IF EXISTS \"%s\"", ksname);
            logger.debug(query);
            QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static Pair<List<String>, List<String>> getUDTInfo(final String ksName, final String typeName) {
        try {
            UntypedResultSet result = QueryProcessor.executeOnceInternal("SELECT field_names, field_types FROM system_schema.types WHERE keyspace_name = ? AND type_name = ?", 
                    new Object[] { ksName, typeName });
            Row row = result.one();
            if ((row != null) && row.has("field_names")) {
                List<String> field_names = row.getList("field_names", UTF8Type.instance);
                List<String> field_types = row.getList("field_types", UTF8Type.instance);
                return Pair.<List<String>, List<String>> create(field_names, field_types);
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public static MapType<?,?> getMapType(final String ksName, final String cfName, final String colName) {
        try {
            UntypedResultSet result = QueryProcessor.executeOnceInternal("SELECT validator FROM system.schema_columns WHERE keyspace_name = ? AND columnfamily_name = ? AND column_name = ?", 
                    new Object[] { ksName, cfName, colName });
            Row row = result.one();
            if ((row != null) && row.has("validator")) {
                AbstractType<?> type = TypeParser.parse(row.getString("validator"));
                if (type instanceof MapType) {
                    return (MapType<?,?>)type;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    // see https://docs.datastax.com/en/cql/3.0/cql/cql_reference/keywords_r.html
    public static final Pattern keywordsPattern = Pattern.compile("(ADD|ALLOW|ALTER|AND|ANY|APPLY|ASC|AUTHORIZE|BATCH|BEGIN|BY|COLUMNFAMILY|CREATE|DELETE|DESC|DROP|EACH_QUORUM|GRANT|IN|INDEX|INET|INSERT|INTO|KEYSPACE|KEYSPACES|LIMIT|LOCAL_ONE|LOCAL_QUORUM|MODIFY|NOT|NORECURSIVE|OF|ON|ONE|ORDER|PASSWORD|PRIMARY|QUORUM|RENAME|REVOKE|SCHEMA|SELECT|SET|TABLE|TO|TOKEN|THREE|TRUNCATE|TWO|UNLOGGED|UPDATE|USE|USING|WHERE|WITH)"); 
    
    public static boolean isReservedKeyword(String identifier) {
        return keywordsPattern.matcher(identifier.toUpperCase(Locale.ROOT)).matches();
    }
    
    private static Object toJsonValue(Object o) {
        if (o instanceof UUID)
            return o.toString();
        if (o instanceof Date)
            return ((Date)o).getTime();
        if (o instanceof ByteBuffer) {
            // encode byte[] as Base64 encoded string
            ByteBuffer bb = ByteBufferUtil.clone((ByteBuffer)o);
            return Base64.getEncoder().encodeToString(ByteBufferUtil.getArray((ByteBuffer)o));
        }
        if (o instanceof InetAddress)
            return InetAddresses.toAddrString((InetAddress)o);
        return o;
    }
    
    private static org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();

    // wrap string values with quotes
    private static String stringify(Object o) throws IOException {
        Object v = toJsonValue(o);
        try {
            return v instanceof String ? jsonMapper.writeValueAsString(v) : v.toString();
        } catch (IOException e) {
            Loggers.getLogger(ClusterService.class).error("Unexpected json encoding error", e);
            throw new RuntimeException(e);
        }
    }
    
    public static String stringify(Object[] cols, int length) {
        if (cols.length == 1)
            return toJsonValue(cols[0]).toString();
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < length; i++) {
            if (i > 0)
                sb.append(",");
            Object val = toJsonValue(cols[i]);
            if (val instanceof String) {
                try {
                    sb.append(jsonMapper.writeValueAsString(val));
                } catch (IOException e) {
                    Loggers.getLogger(ClusterService.class).error("Unexpected json encoding error", e);
                    throw new RuntimeException(e);
                }
            } else {
                sb.append(val);
            }
        }
        return sb.append("]").toString();
    }

    public static ByteBuffer fromString(AbstractType<?> atype, String v) throws IOException {
        if (atype instanceof BytesType)
            return ByteBuffer.wrap(Base64.getDecoder().decode(v));
        return atype.fromString(v);
    }
    
    public static void toXContent(XContentBuilder builder, Mapper mapper, String field, Object value) throws IOException {
        if (value instanceof Collection) {
           if (field == null) {
               builder.startArray(); 
           } else {
               builder.startArray(field);
           }
           for(Iterator<Object> i = ((Collection)value).iterator(); i.hasNext(); ) {
               toXContent(builder, mapper, null, i.next());
           } 
           builder.endArray();
        } else if (value instanceof Map) {
           Map<String, Object> map = (Map<String,Object>)value;
           if (field != null) {
               builder.startObject(field);
           } else {
               builder.startObject();
           }
           for(String subField : map.keySet()) {
               Object subValue = map.get(subField);
               if (subValue == null)
                   continue;
               
               if (subValue instanceof Collection && ((Collection)subValue).size() == 1)
                   subValue = ((Collection)subValue).iterator().next();

               if (mapper != null) {
                   if (mapper instanceof ObjectMapper) {
                       toXContent(builder, ((ObjectMapper)mapper).getMapper(subField), subField, subValue);
                   } else if (mapper instanceof BaseGeoPointFieldMapper) {
                       BaseGeoPointFieldMapper geoMapper = (BaseGeoPointFieldMapper)mapper;
                       if (geoMapper.fieldType() instanceof LegacyGeoPointFieldType && ((LegacyGeoPointFieldType)geoMapper.fieldType()).isLatLonEnabled()) {
                           Iterator<Mapper> it = geoMapper.iterator();
                           switch(subField) {
                           case org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT:
                               toXContent(builder, it.next(), org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT, map.get(org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT));
                               break;
                           case org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON: 
                               it.next();
                               toXContent(builder, it.next(), org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON, map.get(org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON));
                               break;
                           }
                       } else {
                           // No mapper known
                           // TODO: support geohashing
                           builder.field(subField, subValue);
                       }
                   } else {
                       builder.field(subField, subValue);
                   }
               } else {
                   builder.field(subField, subValue);
               }
           }
           builder.endObject();
        } else {
           if (mapper instanceof FieldMapper) {
               FieldMapper fieldMapper = (FieldMapper)mapper;
               if (!(fieldMapper instanceof MetadataFieldMapper)) {
                   if (field != null) {
                       builder.field(field, fieldMapper.fieldType().valueForDisplay(value));
                   } else {
                       builder.value((fieldMapper==null) ? value : fieldMapper.fieldType().valueForDisplay(value));
                   }
               }
           } else if (mapper instanceof ObjectMapper) {
               ObjectMapper objectMapper = (ObjectMapper)mapper;
               if (!objectMapper.isEnabled()) {
                   builder.field(field, value);
               } else {
                   throw new IOException("Unexpected object value ["+value+"]");
               }
           }
        }
    }
    
    public static XContentBuilder buildDocument(DocumentMapper documentMapper, Map<String, Object> docMap, boolean humanReadable) throws IOException {
        return buildDocument(documentMapper, docMap, humanReadable, false);
    }
        
    public static XContentBuilder buildDocument(DocumentMapper documentMapper, Map<String, Object> docMap, boolean humanReadable, boolean forStaticDocument) throws IOException {
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).humanReadable(true);
        builder.startObject();
        for(String field : docMap.keySet()) {
            if (field.equals(ParentFieldMapper.NAME)) continue;
            FieldMapper fieldMapper = documentMapper.mappers().smartNameFieldMapper(field);
            if (fieldMapper != null) {
                if (forStaticDocument && !isStaticOrPartitionKey(fieldMapper)) 
                    continue;
                toXContent(builder, fieldMapper, field, docMap.get(field));
            } else {
                ObjectMapper objectMapper = documentMapper.objectMappers().get(field);
                if (objectMapper != null) {
                     if (forStaticDocument && !isStaticOrPartitionKey(objectMapper)) 
                         continue;
                     toXContent(builder, objectMapper, field, docMap.get(field));
                } else {
                    Loggers.getLogger(ClusterService.class).error("No mapper found for field "+field);
                    throw new IOException("No mapper found for field "+field);
                }
            }
        }
        builder.endObject();
        return builder;
    }

    public static boolean isStaticOrPartitionKey(Mapper mapper) {
        return mapper.cqlStaticColumn() || mapper.cqlPartitionKey();
    }

    public static final String PERCOLATOR_TABLE = "_percolator";
    
    // Because Cassandra table name does not support dash, convert dash to underscore in elasticsearch type, an keep this information
    // in a map for reverse lookup. Of course, conflict is still possible in a keyspace.
    private static final Map<String, String> cfNameToType = new ConcurrentHashMap<String, String>() {{
       put(PERCOLATOR_TABLE, MapperService.PERCOLATOR_LEGACY_TYPE_NAME);
    }};
    
    public static String typeToCfName(String keyspaceName, String typeName) {
        if (typeName.indexOf('-') >= 0) {
            String cfName = typeName.replaceAll("\\-", "_");
            cfNameToType.putIfAbsent(keyspaceName+"."+cfName, typeName);
            return cfName;
        }
        return typeName;
    }
   
    public static String cfNameToType(String keyspaceName, String cfName) {
        if (cfName.indexOf('_') >= 0) {
            String type = cfNameToType.get(keyspaceName+"."+cfName);
            if (type != null)
                return type;
        }
        return cfName;
    }
    
    public static String indexToKsName(String index) {
        return index.replaceAll("\\.", "_").replaceAll("\\-", "_");
    }
    
    public String buildCql(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {
        if (objectMapper.cqlStruct().equals(CqlStruct.UDT) && objectMapper.iterator().hasNext()) {
            return buildUDT(ksName, cfName, name, objectMapper);
        } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP) && objectMapper.iterator().hasNext()) {
            if (objectMapper.iterator().hasNext()) {
                Mapper childMapper = objectMapper.iterator().next();
                if (childMapper instanceof FieldMapper) {
                    return "map<text,"+childMapper.cqlType()+">";
                } else if (childMapper instanceof ObjectMapper) {
                    String subType = buildCql(ksName,cfName,childMapper.simpleName(),(ObjectMapper)childMapper);
                    return (subType==null) ? null : "map<text,frozen<"+subType+">>";
                }
            } else {
                // default map prototype, no mapper to determine the value type.
                return "map<text,text>";
            }
        }
        return null;
    }

    public String buildUDT(final String ksName, final String cfName, final String name, final ObjectMapper objectMapper) throws RequestExecutionException {
        String typeName = (objectMapper.cqlUdtName() == null) ? cfName + "_" + objectMapper.fullPath().replace('.', '_') : objectMapper.cqlUdtName();

        if (!objectMapper.hasField()) {
            // delay UDT creation for object with no sub-field #146
            return null;
        }
        
        // create sub-type first
        for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
            Mapper mapper = it.next();
            if (mapper instanceof ObjectMapper) {
                buildCql(ksName, cfName, mapper.simpleName(), (ObjectMapper) mapper);
            } else if (mapper instanceof BaseGeoPointFieldMapper) {
                buildGeoPointType(ksName);
            } 
        }

        Pair<List<String>, List<String>> udt = getUDTInfo(ksName, typeName);
        if (udt == null) {
            // create new UDT.
            StringBuilder create = new StringBuilder(String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( ", ksName, typeName));
            boolean first = true;
            for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
                Mapper mapper = it.next();
                if (mapper instanceof ObjectMapper && ((ObjectMapper) mapper).isEnabled() && !mapper.hasField()) {
                    continue;   // ignore object with no sub-field #146
                }
                if (first)
                    first = false;
                else
                    create.append(", ");
                
                // Use only the last part of the fullname to build UDT.
                int lastDotIndex = mapper.name().lastIndexOf('.');
                String shortName = (lastDotIndex > 0) ? mapper.name().substring(lastDotIndex+1) :  mapper.name();
                
                if (isReservedKeyword(shortName))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", shortName);
                
                create.append('\"').append(shortName).append("\" ");
                if (mapper instanceof ObjectMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(ColumnIdentifier.maybeQuote(cfName+'_'+((ObjectMapper) mapper).fullPath().replace('.', '_')))
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON))
                        create.append(">");
                } else if (mapper instanceof BaseGeoPointFieldMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append(GEO_POINT_TYPE)
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(">");
                } else if (mapper instanceof GeoShapeFieldMapper) {
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(mapper.cqlCollectionTag()).append("<");
                    create.append("frozen<")
                        .append("text")
                        .append(">");
                    if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                        create.append(">");
                } else {
                    String cqlType = mapper.cqlType();
                    if (mapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        create.append(cqlType);
                    } else {
                        create.append(mapper.cqlCollectionTag()).append("<");
                        if (!isNativeCql3Type(cqlType)) create.append("frozen<");
                        create.append(cqlType);
                        if (!isNativeCql3Type(cqlType)) create.append(">");
                        create.append(">");
                    }
                }
            }
            create.append(" )");
            if (!first) {
                if (logger.isDebugEnabled())
                    logger.debug("create UDT:"+ create.toString());
                QueryProcessor.process(create.toString(), ConsistencyLevel.LOCAL_ONE);
                return typeName;
            } else {
                // UDT not created because it has no sub-fields #146
                return null;
            }
        } else {
            // update existing UDT
            for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
                Mapper mapper = it.next();
                if (mapper instanceof ObjectMapper && ((ObjectMapper) mapper).isEnabled() && !((ObjectMapper) mapper).iterator().hasNext()) {
                    continue;   // ignore object with no sub-field #146
                }
                int lastDotIndex = mapper.name().lastIndexOf('.');
                String shortName = (lastDotIndex > 0) ? mapper.name().substring(lastDotIndex+1) :  mapper.name();
                if (isReservedKeyword(shortName))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", shortName);
                
                StringBuilder update = new StringBuilder(String.format(Locale.ROOT, "ALTER TYPE \"%s\".\"%s\" ADD \"%s\" ", ksName, typeName, shortName));
                if (!udt.left.contains(shortName)) {
                    if (mapper instanceof ObjectMapper) {
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(mapper.cqlCollectionTag()).append("<");
                        update.append("frozen<")
                            .append(cfName).append('_').append(((ObjectMapper) mapper).fullPath().replace('.', '_'))
                            .append(">");
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(">");
                    } else if (mapper instanceof BaseGeoPointFieldMapper) {
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(mapper.cqlCollectionTag()).append("<");
                        update.append("frozen<")
                            .append(GEO_POINT_TYPE)
                            .append(">");
                        if (!mapper.cqlCollection().equals(CqlCollection.SINGLETON)) 
                            update.append(">");
                    } else {
                        String cqlType = mapper.cqlType();
                        if (mapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                            update.append(cqlType);
                        } else {
                            update.append(mapper.cqlCollectionTag()).append("<");
                            if (!isNativeCql3Type(cqlType)) update.append("frozen<");
                            update.append(cqlType);
                            if (!isNativeCql3Type(cqlType)) update.append(">");
                            update.append(">");
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("update UDT: "+update.toString());
                    }
                    QueryProcessor.process(update.toString(), ConsistencyLevel.LOCAL_ONE);
                }
            }
        }
        return typeName;
    }

    public static final String GEO_POINT_TYPE = "geo_point";
    public static final String ATTACHEMENT_TYPE = "attachement";
    public static final String COMPLETION_TYPE = "completion";
    
    private void buildGeoPointType(String ksName) throws RequestExecutionException {
        String query = String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" ( %s double, %s double)", 
                ksName, GEO_POINT_TYPE,org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT,org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }

    private void buildAttachementType(String ksName) throws RequestExecutionException {
        String query = String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" (context text, content_type text, content_length bigint, date timestamp, title text, author text, keywords text, language text)", ksName, ATTACHEMENT_TYPE);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
    }
    
    private void buildCompletionType(String ksName) throws RequestExecutionException {
        String query = String.format(Locale.ROOT, "CREATE TYPE IF NOT EXISTS \"%s\".\"%s\" (input list<text>, contexts text, weight int)", ksName, COMPLETION_TYPE);
        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
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
    
    
    public ClusterState updateNumberOfReplica(ClusterState currentState) {
        MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
        for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
            IndexMetaData indexMetaData = it.next();
            IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
            int rf = replicationFactor(indexMetaData.keyspace());
            indexMetaDataBuilder.numberOfReplicas( Math.max(0, rf - 1) );
            metaDataBuilder.put(indexMetaDataBuilder.build(), false);
        }
        return ClusterState.builder(currentState).metaData(metaDataBuilder.build()).build();
    }
    
    public ClusterState updateNumberOfShardsAndReplicas(ClusterState currentState) {
        int numberOfNodes = currentState.nodes().getSize();
        
        if (numberOfNodes == 0)
            return currentState; // for testing purposes.
        
        MetaData.Builder metaDataBuilder = MetaData.builder(currentState.metaData());
        for(Iterator<IndexMetaData> it = currentState.metaData().iterator(); it.hasNext(); ) {
            IndexMetaData indexMetaData = it.next();
            IndexMetaData.Builder indexMetaDataBuilder = IndexMetaData.builder(indexMetaData);
            indexMetaDataBuilder.numberOfShards(numberOfNodes);
            int rf = replicationFactor(indexMetaData.keyspace());
            indexMetaDataBuilder.numberOfReplicas( Math.max(0, rf - 1) );
            metaDataBuilder.put(indexMetaDataBuilder.build(), false);
        }
        return ClusterState.builder(currentState).metaData(metaDataBuilder.build()).build();
    }
    
    /**
     * Reload cluster metadata from elastic_admin.metadatatable row.
     * Should only be called when user keyspaces are initialized.
     */
    public void submitRefreshMetaData(final MetaData metaData, final String source) {
        submitStateUpdateTask(source, new ClusterStateUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                return ClusterState.builder(currentState).metaData(metaData).build();
            }

            @Override
            public void onFailure(String source, Exception t) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected failure during [{}]", source), t);
            }
        });
        
    }
    
    public void submitNumberOfShardsAndReplicasUpdate(final String source) {
        submitStateUpdateTask(source, new ClusterStateUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                return updateNumberOfShardsAndReplicas(currentState);
            }

            @Override
            public void onFailure(String source, Exception t) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected failure during [{}]", source), t);
            }
        });
    }
    
    public void updateRoutingTable() {
        submitStateUpdateTask("Update-routing-table" , new ClusterStateUpdateTask() {
            @Override
            public ClusterState execute(ClusterState currentState) {
                 return ClusterState.builder(currentState).incrementVersion().build();
            }
    
            @Override
            public void onFailure(String source, Exception t) {
                logger.error((Supplier<?>) () -> new ParameterizedMessage("unexpected failure during [{}]", source), t);
            }
        });
    }
    
    
    public void updateTableSchema(final MapperService mapperService, final MappingMetaData mappingMd) throws IOException {
        try {
            String ksName = mapperService.keyspace();
            String cfName = ClusterService.typeToCfName(ksName, mappingMd.type());
            
            createIndexKeyspace(ksName, settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, 0) +1, mapperService.getIndexSettings().getIndexMetaData().replication());
            
            CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
            boolean newTable = (cfm == null);
            
            DocumentMapper docMapper = mapperService.documentMapper(mappingMd.type());
            Map<String, Object> mappingMap = mappingMd.sourceAsMap();
            
            Set<String> columns = new HashSet();
            if (docMapper.sourceMapper().enabled()) 
                columns.add(SourceFieldMapper.NAME);
            if (mappingMap.get("properties") != null) 
                columns.addAll( ((Map<String, Object>)mappingMap.get("properties")).keySet() );
            
            logger.debug("Updating CQL3 schema {}.{} columns={}", ksName, cfName, columns);
            StringBuilder columnsList = new StringBuilder();
            Map<String,Boolean> columnsMap = new HashMap<String, Boolean>(columns.size());
            String[] primaryKeyList = new String[(newTable) ? columns.size()+1 : cfm.partitionKeyColumns().size()+cfm.clusteringColumns().size()];
            int primaryKeyLength = 0;
            int partitionKeyLength = 0;
            for (String column : columns) {
                if (isReservedKeyword(column))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", column);
                
                if (column.equals(TokenFieldMapper.NAME))
                    continue; // ignore pseudo column known by Elasticsearch
                
                if (columnsList.length() > 0) 
                    columnsList.append(',');
                
                String cqlType = null;
                boolean isStatic = false;
                FieldMapper fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                if (fieldMapper != null) {
                    if (fieldMapper instanceof BaseGeoPointFieldMapper) {
                        ColumnDefinition cdef = (newTable) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                        if (cdef != null && cdef.type instanceof UTF8Type) {
                            // index geohash stored as text in cassandra.
                            cqlType = "text";
                        } else {
                            // create a geo_point UDT to store lat,lon
                            cqlType = GEO_POINT_TYPE;
                            buildGeoPointType(ksName);
                        }
                    } else if (fieldMapper instanceof GeoShapeFieldMapper) {
                        cqlType = "text";
                    } else if (fieldMapper instanceof CompletionFieldMapper) {
                        cqlType = COMPLETION_TYPE;
                        buildCompletionType(ksName);
                    } else if (fieldMapper.getClass().getName().equals("org.elasticsearch.mapper.attachments.AttachmentMapper")) {
                        // attachement is a plugin, so class may not found.
                        cqlType = ATTACHEMENT_TYPE;
                        buildAttachementType(ksName);
                    } else if (fieldMapper instanceof SourceFieldMapper) {
                        cqlType = "blob";
                    } else {
                        cqlType = fieldMapper.cqlType();
                        if (cqlType == null) {
                            logger.warn("Ignoring field [{}] type [{}]", column, fieldMapper.name());
                            continue;
                        }
                    }
                    
                    columnsMap.put(column, fieldMapper.cqlPartialUpdate());
                    if (fieldMapper.cqlPrimaryKeyOrder() >= 0) {
                        if (fieldMapper.cqlPrimaryKeyOrder() < primaryKeyList.length && primaryKeyList[fieldMapper.cqlPrimaryKeyOrder()] == null) {
                            primaryKeyList[fieldMapper.cqlPrimaryKeyOrder()] = column;
                            primaryKeyLength = Math.max(primaryKeyLength, fieldMapper.cqlPrimaryKeyOrder()+1);
                            if (fieldMapper.cqlPartitionKey()) {
                                partitionKeyLength++;
                            }
                        } else {
                            throw new Exception("Wrong primary key order for column "+column);
                        }
                    }
                    
                    if (!isNativeCql3Type(cqlType)) {
                        cqlType = "frozen<" + cqlType + ">";
                    }
                    if (!fieldMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        cqlType = fieldMapper.cqlCollectionTag()+"<" + cqlType + ">";
                    }
                    isStatic = fieldMapper.cqlStaticColumn();
                } else {
                    ObjectMapper objectMapper = docMapper.objectMappers().get(column);
                    if (objectMapper == null) {
                       logger.warn("Cannot infer CQL type from object mapping for field [{}], ignoring", column);
                       continue;
                    }
                    if (objectMapper.isEnabled() && !objectMapper.iterator().hasNext()) {
                        logger.warn("Ignoring enabled object with no sub-fields", column);
                        continue;
                    }
                    columnsMap.put(column,  objectMapper.cqlPartialUpdate());
                    if (objectMapper.cqlPrimaryKeyOrder() >= 0) {
                        if (objectMapper.cqlPrimaryKeyOrder() < primaryKeyList.length && primaryKeyList[objectMapper.cqlPrimaryKeyOrder()] == null) {
                            primaryKeyList[objectMapper.cqlPrimaryKeyOrder()] = column;
                            primaryKeyLength = Math.max(primaryKeyLength, objectMapper.cqlPrimaryKeyOrder()+1);
                            if (objectMapper.cqlPartitionKey()) {
                                partitionKeyLength++;
                            }
                        } else {
                            throw new Exception("Wrong primary key order for column "+column);
                        }
                    }
                    if (!objectMapper.isEnabled()) {
                        logger.debug("Object [{}] not enabled stored as text", column);
                        cqlType = "text";
                    } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP)) {
                        // TODO: check columnName exists and is map<text,?>
                        cqlType = buildCql(ksName, cfName, column, objectMapper);
                        if (cqlType == null) {
                            // no sub-field, ignore it #146
                            continue;
                        }
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                            cqlType = objectMapper.cqlCollectionTag()+"<"+cqlType+">";
                        }
                        //logger.debug("Expecting column [{}] to be a map<text,?>", column);
                    } else  if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                        
                        if (!objectMapper.isEnabled()) {
                            cqlType = "text";   // opaque json object stored as text
                        } else {
                            String subType = buildCql(ksName, cfName, column, objectMapper);
                            if (subType == null) {
                                continue;       // no sub-field, ignore it #146
                            }
                            cqlType = "frozen<" + ColumnIdentifier.maybeQuote(subType) + ">";
                        }
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON) && !(cfName.equals(PERCOLATOR_TABLE) && column.equals("query"))) {
                            cqlType = objectMapper.cqlCollectionTag()+"<"+cqlType+">";
                        }
                    }
                    isStatic = objectMapper.cqlStaticColumn();
                }

                
                if (newTable) {
                    if (cqlType != null) {
                        columnsList.append("\"").append(column).append("\" ").append(cqlType);
                        if (isStatic) columnsList.append(" static");
                    }
                } else {
                    ColumnDefinition cdef = cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                    if (cqlType != null) {
                        if (cdef == null) {
                            for(int i=0; i < primaryKeyLength; i++) {
                                if (primaryKeyList[i] != null && primaryKeyList[i].equals(column))
                                    throw new Exception("Cannot alter primary key of an existing table");
                            }
                            try {
                                String query = String.format(Locale.ROOT, "ALTER TABLE \"%s\".\"%s\" ADD \"%s\" %s %s", ksName, cfName, column, cqlType,(isStatic) ? "static":"");
                                logger.debug(query);
                                QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                            } catch (Exception e) {
                                logger.warn("Failed to alter table {}.{} column [{}] with type [{}]", e, ksName, cfName, column, cqlType);
                            }
                        } else {
                            // check that the existing column matches the provided mapping
                            // TODO: do this check for collection
                            String existingCqlType = cdef.type.asCQL3Type().toString();
                            if (!cdef.type.isCollection()) {
                                if (cqlType.equals("frozen<geo_point>")) {
                                    if (!(existingCqlType.equals("text") || existingCqlType.equals("frozen<geo_point>"))) {
                                        throw new IOException("geo_point cannot be mapped to column ["+column+"] with CQL type ["+cqlType+"]. ");
                                    }
                                } else 
                                    // cdef.type.asCQL3Type() does not include frozen, nor quote, so can do this check for collection.
                                    if (!existingCqlType.equals(cqlType) && 
                                        !cqlType.equals("frozen<"+existingCqlType+">") &&
                                        !(existingCqlType.endsWith("uuid") && cqlType.equals("text")) && // #74 uuid is mapped as keyword
                                        !(existingCqlType.equals("timeuuid") && (cqlType.equals("timestamp") || cqlType.equals("text"))) && 
                                        !(existingCqlType.equals("date") && cqlType.equals("timestamp")) &&
                                        !(existingCqlType.equals("time") && cqlType.equals("bigint"))
                                        ) // timeuuid can be mapped to date
                                    throw new IOException("Existing column ["+column+"] type ["+existingCqlType+"] mismatch with inferred type ["+cqlType+"]");
                            }
                        }
                    }
                }
              
            }

            // add _parent column if necessary. Parent and child documents should have the same partition key.
            if (docMapper.parentFieldMapper().active() && docMapper.parentFieldMapper().pkColumns() == null) {
                if (newTable) {
                    // _parent is a JSON array representation of the parent PK.
                    if (columnsList.length() > 0) 
                        columnsList.append(", ");
                    columnsList.append("\"_parent\" text");
                } else {
                    try {
                        String query = String.format(Locale.ROOT, "ALTER TABLE \"%s\".\"%s\" ADD \"_parent\" text", ksName, cfName);
                        logger.debug(query);
                        QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                    } catch (org.apache.cassandra.exceptions.InvalidRequestException e) {
                        logger.warn("Cannot alter table {}.{} column _parent with type text", e, ksName, cfName);
                    }
                }
            }
            
            if (newTable) {
                if (partitionKeyLength == 0) {
                    // build a default primary key _id text
                    if (columnsList.length() > 0) columnsList.append(',');
                    columnsList.append("\"").append(ELASTIC_ID_COLUMN_NAME).append("\" text");
                    primaryKeyList[0] = ELASTIC_ID_COLUMN_NAME;
                    primaryKeyLength = 1;
                    partitionKeyLength = 1;
                }
                // build the primary key definition
                StringBuilder primaryKey = new StringBuilder();
                primaryKey.append("(");
                for(int i=0; i < primaryKeyLength; i++) {
                    if (primaryKeyList[i] == null) throw new Exception("Incomplet primary key definition at index "+i);
                    primaryKey.append("\"").append( primaryKeyList[i] ).append("\"");
                    if ( i == partitionKeyLength -1) primaryKey.append(")");
                    if (i+1 < primaryKeyLength )   primaryKey.append(",");
                }
                String query = String.format(Locale.ROOT, "CREATE TABLE IF NOT EXISTS \"%s\".\"%s\" ( %s, PRIMARY KEY (%s) ) WITH COMMENT='Auto-created by Elassandra'", 
                        ksName, cfName, columnsList.toString(), primaryKey.toString());
                logger.debug(query);
                QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
            }
            
            updateMapping(mapperService.index().getName(), mappingMd);
            
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }
    
    @Override
    protected void doStart() {
        super.doStart();
        // add post-applied because 2i shoukd be created/deleted after that cassandra indices have taken the new mapping.
        this.addStateApplier(cassandraSecondaryIndicesApplier);
        
        // start a thread for asynchronous CQL schema update, always the last update.
        Runnable task = new Runnable() {
            @Override 
            public void run() { 
                while (true) {
                    try{
                        ClusterService.this.metadataToSaveSemaphore.acquire();
                        MetadataSchemaUpdate metadataSchemaUpdate = ClusterService.this.lastMetadataToSave.getAndSet(null);
                        if (metadataSchemaUpdate != null) {
                            if (metadataSchemaUpdate.version < state().metaData().version()) {
                                logger.trace("Giveup {}.{}.comment obsolete update of metadata.version={} timestamp={}",
                                        ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE,
                                        metadataSchemaUpdate.version, metadataSchemaUpdate.timestamp);
                            } else {
                                logger.trace("Applying {}.{}.comment update with metadata.version={} timestamp={}",
                                        ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE,
                                        metadataSchemaUpdate.version, metadataSchemaUpdate.timestamp);
                                // delayed CQL schema update with timestamp = time of cluster state update
                                CFMetaData cfm = getCFMetaData(ELASTIC_ADMIN_KEYSPACE, ELASTIC_ADMIN_METADATA_TABLE).copy();
                                TableAttributes attrs = new TableAttributes();
                                attrs.addProperty(TableParams.Option.COMMENT.toString(), metadataSchemaUpdate.metaDataString);
                                cfm.params( attrs.asAlteredTableParams(cfm.params) );
                                MigrationManager.announceColumnFamilyUpdate(cfm, null, false, metadataSchemaUpdate.timestamp);
                                /*
                                QueryProcessor.executeOnceInternal(String.format(Locale.ROOT, "ALTER TABLE \"%s\".\"%s\" WITH COMMENT = '%s'", 
                                        elasticAdminKeyspaceName,  ELASTIC_ADMIN_METADATA_TABLE, metadataSchemaUpdate.metaDataString));
                                */
                            }
                        }
                    } catch(Exception e) {
                        logger.warn("Failed to update CQL schema",e);
                    }
                }
            } 
        };
        new Thread(task, "metadataSchemaUpdater").start();
    }
    
    public void updateMapping(String ksName, MappingMetaData mapping) {
        cassandraSecondaryIndicesApplier.updateMapping( ksName, mapping);
    }
    
    public void recoverShard(String index) {
        cassandraSecondaryIndicesApplier.recoverShard(index);
    }
    
    @Override
    protected void publishAndApplyChanges(TaskInputs taskInputs, TaskOutputs taskOutputs) {
        ClusterState previousClusterState = taskOutputs.previousClusterState;
        ClusterState newClusterState = taskOutputs.newClusterState;

        final Discovery.AckListener ackListener = newClusterState.nodes().isLocalNodeElectedMaster() ?
            taskOutputs.createAckListener(threadPool, newClusterState) :
            null;

        // try to update cluster state.
        long startTimeNS = System.nanoTime();
        boolean presistedMetadata = false;
        CassandraDiscovery.MetaDataVersionAckListener metaDataVersionAckListerner = null;
        try {
            String newClusterStateMetaDataString = MetaData.Builder.toXContent(newClusterState.metaData(), MetaData.CASSANDRA_FORMAT_PARAMS);
            String previousClusterStateMetaDataString = MetaData.Builder.toXContent(previousClusterState.metaData(), MetaData.CASSANDRA_FORMAT_PARAMS);
            if (!newClusterStateMetaDataString.equals(previousClusterStateMetaDataString) && !newClusterState.blocks().disableStatePersistence() && taskOutputs.doPersistMetadata) {
                // update MeteData.version+cluster_uuid
                presistedMetadata = true;
                
                newClusterState = ClusterState.builder(newClusterState)
                                    .metaData(MetaData.builder(newClusterState.metaData()).incrementVersion().build())
                                    .incrementVersion()
                                    .build();
                
                // try to persist new metadata in cassandra.
                if (presistedMetadata && newClusterState.nodes().getSize() > 1) {
                    // register the X2 listener before publishing to avoid dead locks !
                    metaDataVersionAckListerner = this.discovery.new MetaDataVersionAckListener(newClusterState.metaData().version(), ackListener, newClusterState);
                }
                try {
                    persistMetaData(previousClusterState.metaData(), newClusterState.metaData(), taskInputs.summary);
                    publishX2(newClusterState);
                } catch (ConcurrentMetaDataUpdateException e) {
                    if (metaDataVersionAckListerner != null)
                        metaDataVersionAckListerner.abort();
                    // should replay the task later when current cluster state will match the expected metadata uuid and version
                    logger.warn("Cannot overwrite persistent metadata, will resubmit task when metadata.version > {}", previousClusterState.metaData().version());
                    final long resubmitTimeMillis = System.currentTimeMillis();
                    ClusterService.this.addListener(new ClusterStateListener() {
                        @Override
                        public void clusterChanged(ClusterChangedEvent event) {
                            if (event.metaDataChanged()) {
                                final long lostTimeMillis = System.currentTimeMillis() - resubmitTimeMillis;
                                Priority priority = Priority.URGENT;
                                TimeValue timeout = TimeValue.timeValueSeconds(30*1000 - lostTimeMillis);
                                ClusterStateTaskConfig config;
                                Map<Object, ClusterStateTaskListener> map = new HashMap<Object, ClusterStateTaskListener>();
                                for (final ClusterServiceTaskBatcher.UpdateTask updateTask : taskInputs.updateTasks) {
                                    map.put( updateTask.task, updateTask.listener);
                                    priority = updateTask.priority();
                                    if (updateTask.task instanceof ClusterStateUpdateTask) {
                                        timeout = TimeValue.timeValueMillis( ((ClusterStateUpdateTask)updateTask.task).timeout().getMillis() - lostTimeMillis);
                                    } else if (updateTask.task instanceof ClusterStateUpdateRequest) {
                                        timeout = TimeValue.timeValueMillis( ((ClusterStateUpdateRequest)updateTask.task).masterNodeTimeout().getMillis() - lostTimeMillis);
                                    }
                                }
                                logger.warn("metadata.version={} => resubmit delayed update source={} tasks={} priority={} remaing timeout={}",
                                        ClusterService.this.state().metaData().version(), taskInputs.summary, taskInputs.updateTasks, priority, timeout);
                                ClusterService.this.submitStateUpdateTasks(taskInputs.summary, map, ClusterStateTaskConfig.build(priority, timeout), taskInputs.executor);
                                ClusterService.this.removeListener(this); // replay only once.
                            }
                        }
                    });
                    long currentVersion = readMetaDataVersion(ConsistencyLevel.LOCAL_QUORUM);
                    if (currentVersion > previousClusterState.metaData().version())
                        // trigger a metadat update from metadata table if not yet triggered by a gossip change.
                        this.discovery.updateMetadata("refresh-metadata", currentVersion);
                    return;
                }
            }
        } catch (Exception e) {
            // Unexpected issue => ack with failure.
            ackListener.onNodeAck(this.localNode(), e);
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder("failed to execute cluster state update in ").append(executionTime)
                        .append(", state:\nversion [")
                        .append(previousClusterState.version()).
                        append("], source [").append(taskInputs.summary).append("]\n");
                logger.warn(sb.toString(), e);
                // TODO resubmit task on next cluster state change
            } else {
                logger.error("Cassandra issue:", e);
            }
            if (metaDataVersionAckListerner != null)
                metaDataVersionAckListerner.abort();
            return;
        }

        try {
            if (logger.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder("cluster state updated, source [").append(taskInputs.summary).append("]\n");
                sb.append(newClusterState.toString());
                logger.trace(sb.toString());
            } else if (logger.isDebugEnabled()) {
                logger.debug("cluster state updated, version [{}], source [{}]", newClusterState.version(), taskInputs.summary);
            }

            // update routing table.
            newClusterState = ClusterState.builder(newClusterState).routingTable(RoutingTable.build(this, newClusterState)).build();
            
            ClusterChangedEvent clusterChangedEvent = new ClusterChangedEvent(taskInputs.summary, newClusterState, previousClusterState);
            // new cluster state, notify all listeners
            final DiscoveryNodes.Delta nodesDelta = clusterChangedEvent.nodesDelta();
            if (nodesDelta.hasChanges() && logger.isInfoEnabled()) {
                String summary = nodesDelta.shortSummary();
                if (summary.length() > 0) {
                    logger.info("{}, reason: {}", summary, taskInputs.summary);
                }
            }

            nodeConnectionsService.connectToNodes(newClusterState.nodes());

            logger.debug("applying cluster state version {}", newClusterState.version());
            try {
                // nothing to do until we actually recover from the gateway or any other block indicates we need to disable persistency
                if (clusterChangedEvent.state().blocks().disableStatePersistence() == false && clusterChangedEvent.metaDataChanged()) {
                    final Settings incomingSettings = clusterChangedEvent.state().metaData().settings();
                    clusterSettings.applySettings(incomingSettings);
                }
            } catch (Exception ex) {
                logger.warn("failed to apply cluster settings", ex);
            }
            
            nodeConnectionsService.disconnectFromNodesExcept(newClusterState.nodes());
            
            // notify highPriorityStateAppliers, including IndicesClusterStateService to start shards and update mapperServices.
            if (logger.isTraceEnabled())
                logger.trace("notfiy highPriorityStateAppliers={}",highPriorityStateAppliers);
            for (ClusterStateApplier applier : this.highPriorityStateAppliers) {
                try {
                    applier.applyClusterState(clusterChangedEvent);
                } catch (Exception ex) {
                    logger.warn("failed to notify ClusterStateListener", ex);
                }
            }

            // update the current cluster state 
            final ClusterState newClusterState2 = newClusterState;
            updateState(css -> newClusterState2);
            if (logger.isTraceEnabled())
                logger.trace("set local clusterState version={} metadata.version={}", newClusterState.version(), newClusterState.metaData().version());
            
            // notifiy listener, including ElasticSecondaryIndex instances.
            Stream.concat(clusterStateListeners.stream(), timeoutClusterStateListeners.stream()).forEach(listener -> {
                try {
                    logger.trace("calling [{}] with change to version [{}] metadata.version=[{}]", listener, newClusterState2.version(), newClusterState2.metaData().version());
                    listener.clusterChanged(clusterChangedEvent);
                } catch (Exception ex) {
                    logger.warn("failed to notify ClusterStateListener", ex);
                }
            });
            
            // update cluster state routing table
            // TODO: update the routing table only for updated indices.
            newClusterState = ClusterState.builder(newClusterState).routingTable(RoutingTable.build(this, newClusterState)).build();
            final ClusterState newClusterState3 = newClusterState;
            updateState(css -> newClusterState3);
            
            // coordinator node
            if (presistedMetadata && metaDataVersionAckListerner != null && newClusterState.nodes().getSize() > 1) {
                try {
                    if (logger.isInfoEnabled())
                        logger.debug("Waiting MetaData.version = {} for all other alive nodes", newClusterState.metaData().version() );
                    if (!metaDataVersionAckListerner.await(30L, TimeUnit.SECONDS)) {
                        logger.warn("Timeout waiting MetaData.version={}", newClusterState.metaData().version());
                    } else {
                        logger.debug("Metadata.version={} applied by all alive nodes", newClusterState.metaData().version());
                    }
                } catch (Throwable e) {
                    final long version = newClusterState.metaData().version();
                    logger.error((Supplier<?>) () -> new ParameterizedMessage("Interruped while waiting MetaData.version = {}", version), e);
                }
            }
            
            // notify normalPriorityStateAppliers with the new cluster state, 
            // including CassandraSecondaryIndicesApplier to create new C* 2i instances when newClusterState is applied by all nodes.
            if (logger.isTraceEnabled())
                logger.trace("notifiy normalPriorityStateAppliers={} before acknowlegdging listeners",normalPriorityStateAppliers);
            for (ClusterStateApplier applier : normalPriorityStateAppliers) {
                try {
                    applier.applyClusterState(clusterChangedEvent);
                } catch (Exception ex) {
                    logger.warn("failed to notify ClusterStateListener", ex);
                }
            }
            
            // non-coordinator node
            if (!presistedMetadata && newClusterState.metaData().version() > previousClusterState.metaData().version()) {
                // For non-coordinator nodes, publish in gossip state the applied metadata.uuid and version.
                // after mapping change have been applied to secondary index in normalPriorityStateAppliers
                publishX2(newClusterState);
            }
            
            //manual ack only from the master at the end of the publish
            if (newClusterState.nodes().isLocalNodeElectedMaster()) {
                try {
                    ackListener.onNodeAck(newClusterState.nodes().getLocalNode(), null);
                } catch (Exception e) {
                    final DiscoveryNode localNode = newClusterState.nodes().getLocalNode();
                    logger.debug(
                        (Supplier<?>) () -> new ParameterizedMessage("error while processing ack for master node [{}]", localNode),
                        e);
                }
            }
            
            if (logger.isTraceEnabled())
                logger.trace("notify lowPriorityStateAppliers={} after acknowlegdging listeners", lowPriorityStateAppliers);
            for (ClusterStateApplier applier : lowPriorityStateAppliers) {
                try {
                    applier.applyClusterState(clusterChangedEvent);
                } catch (Exception ex) {
                    logger.warn("failed to notify ClusterStateListener", ex);
                }
            }

            // notify processed listeners
            taskOutputs.processedDifferentClusterState(previousClusterState, newClusterState);
            
            if (newClusterState.nodes().isLocalNodeElectedMaster()) {
                try {
                    taskOutputs.clusterStatePublished(clusterChangedEvent);
                } catch (Exception e) {
                    logger.error(
                        (Supplier<?>) () -> new ParameterizedMessage(
                            "exception thrown while notifying executor of new cluster state publication [{}]",
                            taskInputs.summary),
                        e);
                }
            }
            
            if (this.shardStartedBarrier != null) {
                shardStartedBarrier.isReadyToIndex(newClusterState);
            }
            
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            logger.debug("processed [{}]: took {} done applying updated cluster_state (version: {}, uuid: {}, metadata.version: {})", taskInputs.summary, executionTime, newClusterState.version(), newClusterState.stateUUID(), newClusterState.metaData().version());
            warnAboutSlowTaskIfNeeded(executionTime, taskInputs.summary);
        } catch (Throwable t) {
            TimeValue executionTime = TimeValue.timeValueMillis(Math.max(0, TimeValue.nsecToMSec(System.nanoTime() - startTimeNS)));
            StringBuilder sb = new StringBuilder("failed to apply updated cluster state in ").append(executionTime).append(":\nversion [").append(newClusterState.version())
                    .append("], uuid [").append(newClusterState.stateUUID()).append("], source [").append(taskInputs.summary).append("]\n");
            sb.append(newClusterState.nodes());
            sb.append(newClusterState.routingTable());
            sb.append(newClusterState.getRoutingNodes());
            logger.warn(sb.toString(), t);
            // TODO: do we want to call updateTask.onFailure here?
        }

    }

    public void createSecondaryIndices(final IndexMetaData indexMetaData) throws IOException {
        String ksName = indexMetaData.keyspace();
        String className = indexMetaData.getSettings().get(IndexMetaData.SETTING_SECONDARY_INDEX_CLASS, this.settings.get(SETTING_CLUSTER_SECONDARY_INDEX_CLASS, defaultSecondaryIndexClass.getName()));
        for(ObjectCursor<MappingMetaData> cursor: indexMetaData.getMappings().values()) {
            createSecondaryIndex(ksName, cursor.value, className);
        }
    }
   
    // build secondary indices when shard is started and mapping applied
    public void createSecondaryIndex(String ksName, MappingMetaData mapping, String className) throws IOException {
        final String cfName = typeToCfName(ksName, mapping.type());
        final CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
        boolean found = false;
        if (cfm != null && cfm.getIndexes() != null) {
            for(IndexMetadata indexMetadata : cfm.getIndexes()) {
                if (indexMetadata.isCustom() && indexMetadata.options.get(IndexTarget.CUSTOM_INDEX_OPTION_NAME).equals(className)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                String indexName = buildIndexName(cfName);
                String query = String.format(Locale.ROOT, "CREATE CUSTOM INDEX IF NOT EXISTS \"%s\" ON \"%s\".\"%s\" () USING '%s'",
                        indexName, ksName, cfName, className);
                logger.debug(query);
                try {
                    QueryProcessor.process(query, ConsistencyLevel.LOCAL_ONE);
                } catch (Throwable e) {
                    throw new IOException("Failed to process query=["+query+"]:"+e.getMessage(), e);
                }
            }
        } else {
            logger.warn("Cannot create SECONDARY INDEX, [{}.{}] does not exist",ksName, cfName);
        }
    }
    
    
    public void dropSecondaryIndices(final IndexMetaData indexMetaData) throws RequestExecutionException {
        String ksName = indexMetaData.keyspace();
        for(CFMetaData cfMetaData : Schema.instance.getKSMetaData(ksName).tablesAndViews()) {
            if (cfMetaData.isCQLTable())
                dropSecondaryIndex(cfMetaData);
        }
    }

    public void dropSecondaryIndex(String ksName, String cfName) throws RequestExecutionException  {
        CFMetaData cfMetaData = Schema.instance.getCFMetaData(ksName, cfName);
        if (cfMetaData != null)
            dropSecondaryIndex(cfMetaData);
    }

    public void dropSecondaryIndex(CFMetaData cfMetaData) throws RequestExecutionException  {
        for(IndexMetadata idx : cfMetaData.getIndexes()) {
            if (idx.isCustom()) {
                String className = idx.options.get(IndexTarget.CUSTOM_INDEX_OPTION_NAME);
                if (className != null && className.endsWith("ElasticSecondaryIndex")) {
                    logger.debug("DROP INDEX IF EXISTS {}.{}", cfMetaData.ksName, idx.name);
                    QueryProcessor.process(String.format(Locale.ROOT, "DROP INDEX IF EXISTS \"%s\".\"%s\"",
                            cfMetaData.ksName, idx.name), 
                            ConsistencyLevel.LOCAL_ONE);
                }
            }
        }
    }
    
    public void dropTables(final IndexMetaData indexMetaData) throws RequestExecutionException {
        String ksName = indexMetaData.keyspace();
        for(ObjectCursor<String> cfName : indexMetaData.getMappings().keys()) {
            dropTable(ksName, cfName.value);
        }
    }

    public void dropTable(String ksName, String cfName) throws RequestExecutionException  {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
        if (cfm != null) {
            logger.warn("DROP TABLE IF EXISTS {}.{}", ksName, cfName);
            QueryProcessor.process(String.format(Locale.ROOT, "DROP TABLE IF EXISTS \"%s\".\"%s\"", ksName, cfName), ConsistencyLevel.LOCAL_ONE);
        }
    }
    
    public static String buildIndexName(final String cfName) {
        return new StringBuilder("elastic_")
            .append(cfName)
            .append("_idx").toString();
    }
    
    public static String buildIndexName(final String cfName, final String colName) {
        return new StringBuilder("elastic_")
            .append(cfName).append('_')
            .append(colName.replaceAll("\\W+", "_"))
            .append("_idx").toString();
    }
    
    public static CFMetaData getCFMetaData(final String ksName, final String cfName) throws ActionRequestValidationException {
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        if (metadata == null) {
            ActionRequestValidationException arve = new ActionRequestValidationException();
            arve.addValidationError(ksName+"."+cfName+" table does not exists");;
            throw arve;
        }
        return metadata;
    }
    
    
    public Map<String, GetField> flattenGetField(final String[] fieldFilter, final String path, final Object node, Map<String, GetField> flatFields) {
        if ((node instanceof List) || (node instanceof Set)) {
            for(Object o : ((Collection)node)) {
                flattenGetField(fieldFilter, path, o, flatFields);
            }
        } else if (node instanceof Map) {
            for (String key : ((Map<String,Object>)node).keySet()) {
                String fullname = (path.length() > 0) ? path + '.' + key : key;
                flattenGetField(fieldFilter, fullname, ((Map<String,Object>)node).get(key), flatFields);
            }
        } else {
            if (fieldFilter != null) {
                for (String f : fieldFilter) {
                    if (path.equals(f)) {
                        GetField gf = flatFields.get(path);
                        if (gf == null) {
                            gf = new GetField(path, ImmutableList.builder().add(node).build());
                        } else {
                            gf = new GetField(path, ImmutableList.builder().addAll(gf.getValues()).add(node).build());
                        }
                        flatFields.put(path, gf);
                        break;
                    }
                }
            }
        }
        return flatFields;
    }

    public Map<String, List<Object>> flattenTree(final Set<String> neededFiedls, final String path, final Object node, Map<String, List<Object>> flatMap) {
        if ((node instanceof List) || (node instanceof Set)) {
            for(Object o : ((Collection)node)) {
                flattenTree(neededFiedls, path, o, flatMap);
            }
        } else if (node instanceof Map) {
            for (String key : ((Map<String,Object>)node).keySet()) {
                String fullname = (path.length() > 0) ? path + '.' + key : key;
                flattenTree(neededFiedls, fullname, ((Map<String,Object>)node).get(key), flatMap);
            }
        } else {
            if ((neededFiedls == null) || (neededFiedls.contains(path))) {
                List<Object> values = (List<Object>) flatMap.get(path);
                if (values == null) {
                    values = new ArrayList<Object>();
                    flatMap.put(path, values);
                }
                values.add(node);
            }
        }
        return flatMap;
    }
    
    
    
    public boolean rowExists(final IndexService indexService, final String type, final String id) 
            throws InvalidRequestException, RequestExecutionException, RequestValidationException, IOException {
        DocPrimaryKey docPk = parseElasticId(indexService, type, id);
        return process(ConsistencyLevel.LOCAL_ONE, buildExistsQuery(indexService.mapperService().documentMapper(type), indexService.keyspace(), typeToCfName(indexService.keyspace(), type), id), docPk.values).size() > 0;
    }
    
    
    public UntypedResultSet fetchRow(final IndexService indexService, final String type, final String id, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws InvalidRequestException, RequestExecutionException,
            RequestValidationException, IOException {
        return fetchRow(indexService, type, id, columns, ConsistencyLevel.LOCAL_ONE, columnDefs);
    }

    /**
     * Fetch from the coordinator node.
     */
    public UntypedResultSet fetchRow(final IndexService indexService, final String type, final String id, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        DocPrimaryKey docPk = parseElasticId(indexService, type, id);
        return fetchRow(indexService, type, docPk, columns, cl, columnDefs);
    }
    
    /**
     * Fetch from the coordinator node.
     */
    public UntypedResultSet fetchRow(final IndexService indexService, final String type, final  DocPrimaryKey docPk, final String[] columns, final ConsistencyLevel cl, Map<String,ColumnDefinition> columnDefs) throws InvalidRequestException,
            RequestExecutionException, RequestValidationException, IOException {
        return process(cl, buildFetchQuery(indexService, type, columns, docPk.isStaticDocument, columnDefs), docPk. values);
    }
    
    public Engine.GetResult fetchSourceInternal(final IndexService indexService, String type, String id, Map<String,ColumnDefinition> columnDefs, LongConsumer onRefresh) throws IOException {
        long time = System.nanoTime();
        DocPrimaryKey docPk = parseElasticId(indexService, type, id);
        UntypedResultSet result = fetchRowInternal(indexService, type, docPk, columnDefs.keySet().toArray(new String[columnDefs.size()]), columnDefs);
        onRefresh.accept(System.nanoTime() - time);
        if (!result.isEmpty()) {
            Map<String, Object> sourceMap = rowAsMap(indexService, type, result.one());
            BytesReference source = XContentFactory.contentBuilder(XContentType.JSON).map(sourceMap).bytes();
            Long timestamp = 0L;
            if (sourceMap.get(TimestampFieldMapper.NAME) != null) {
                timestamp = (Long) sourceMap.get(TimestampFieldMapper.NAME);
            }
            Long ttl = 0L;
            if (sourceMap.get(TTLFieldMapper.NAME) != null) {
                ttl = (Long) sourceMap.get(TTLFieldMapper.NAME);
            }
            // build termVector form the Cassandra doc.
            return new Engine.GetResult(true, 1L, null, null); 
        }
        return Engine.GetResult.NOT_EXISTS;
    }

    public UntypedResultSet fetchRowInternal(final IndexService indexService, final String cfName, final String id, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException  {
        DocPrimaryKey docPk = parseElasticId(indexService, cfName, id);
        return fetchRowInternal(indexService, cfName, columns, docPk.values, docPk.isStaticDocument, columnDefs);
    }

    public UntypedResultSet fetchRowInternal(final IndexService indexService, final String cfName, final  DocPrimaryKey docPk, final String[] columns, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException  {
        return fetchRowInternal(indexService, cfName, columns, docPk.values, docPk.isStaticDocument, columnDefs);
    }

    public UntypedResultSet fetchRowInternal(final IndexService indexService, final String cfName, final String[] columns, final Object[] pkColumns, boolean forStaticDocument, Map<String,ColumnDefinition> columnDefs) throws ConfigurationException, IOException, IndexNotFoundException  {
        return QueryProcessor.executeInternal(buildFetchQuery(indexService, cfName, columns, forStaticDocument, columnDefs), pkColumns);
    }
  
    private String regularColumn(final IndexService indexService, final String type) throws IOException {
        if (indexService != null) {
            DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
            if (docMapper != null) {
                for(FieldMapper fieldMapper : docMapper.mappers()) {
                    if (fieldMapper instanceof MetadataFieldMapper)
                        continue;
                    if (fieldMapper.cqlPrimaryKeyOrder() == -1 && !fieldMapper.cqlStaticColumn() && fieldMapper.cqlCollection() == Mapper.CqlCollection.SINGLETON) {
                        return fieldMapper.name();
                    }
                }
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("no regular columns for index=[{}] type=[{}]", indexService.index().getName(), type);
        return null;
    }
    
    public String buildFetchQuery(final IndexService indexService, final String type, final String[] requiredColumns, boolean forStaticDocument, Map<String, ColumnDefinition> columnDefs) 
            throws IndexNotFoundException, IOException 
    {
        DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
        String cfName = typeToCfName(indexService.keyspace(), type);
        CFMetaData metadata = getCFMetaData(indexService.keyspace(), cfName);
        DocumentMapper.CqlFragments cqlFragment = docMapper.getCqlFragments();
        String regularColumn = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        int prefixLength = query.length();
        
        for (String c : requiredColumns) {
            switch(c){
            case TokenFieldMapper.NAME: 
                query.append(query.length() > 7 ? ',':' ')
                    .append("token(")
                    .append(cqlFragment.ptCols)
                    .append(") as \"_token\""); 
                break;
            case RoutingFieldMapper.NAME:
                query.append(query.length() > 7 ? ',':' ')
                    .append( (metadata.partitionKeyColumns().size() > 1) ? "toJsonArray(" : "toString(" )
                    .append(cqlFragment.ptCols)
                    .append(") as \"_routing\"");
                break;
            case TTLFieldMapper.NAME: 
                if (regularColumn == null) 
                    regularColumn = regularColumn(indexService, cfName);
                if (regularColumn != null)
                    query.append(query.length() > 7 ? ',':' ').append("TTL(").append(regularColumn).append(") as \"_ttl\""); 
                break;
            case TimestampFieldMapper.NAME: 
                if (regularColumn == null) 
                    regularColumn = regularColumn(indexService, cfName);
                if (regularColumn != null)
                    query.append(query.length() > 7 ? ',':' ').append("WRITETIME(").append(regularColumn).append(") as \"_timestamp\"");
                break;
            case ParentFieldMapper.NAME:
                ParentFieldMapper parentMapper = docMapper.parentFieldMapper();
                if (parentMapper.active()) {
                    query.append(query.length() > 7 ? ',':' ');
                    if  (parentMapper.pkColumns() == null) {
                        // default column name for _parent should be string.
                        query.append("\"_parent\"");
                    } else {
                        query.append( (parentMapper.pkColumns().indexOf(',') > 0) ? "toJsonArray(" : "toString(")
                             .append(parentMapper.pkColumns())
                             .append(") as \"_parent\"");
                    }
                }
                break;
            case NodeFieldMapper.NAME:
                // nothing to add.
                break;
            default:
                ColumnDefinition cd = columnDefs.get(c);
                if (cd != null && (cd.isPartitionKey() || cd.isStatic() || !forStaticDocument)) {
                   query.append(query.length() > prefixLength ? ',':' ').append("\"").append(c).append("\"");
                }
            }
        }
        if (query.length() == prefixLength) {
            // no column match or requiredColumn is empty, add _id to avoid CQL syntax error...
            query.append("\"_id\"");
        }
        query.append(" FROM \"").append(indexService.keyspace()).append("\".\"").append(cfName)
             .append("\" WHERE ").append((forStaticDocument) ? cqlFragment.ptWhere : cqlFragment.pkWhere )
             .append(" LIMIT 1");
        return query.toString();
    }
    
    public static String buildDeleteQuery(final DocumentMapper docMapper, final String ksName, final String cfName, final String id) {
        return "DELETE FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ docMapper.getCqlFragments().pkWhere;
    }
    
    public static String buildExistsQuery(final DocumentMapper docMapper, final String ksName, final String cfName, final String id) {
        return "SELECT "+docMapper.getCqlFragments().pkCols+" FROM \""+ksName+"\".\""+cfName+"\" WHERE "+ docMapper.getCqlFragments().pkWhere;
    }

    
    public void deleteRow(final IndexService indexService, final String type, final String id, final ConsistencyLevel cl) throws InvalidRequestException, RequestExecutionException, RequestValidationException,
            IOException {
        String cfName = typeToCfName(indexService.keyspace(), type);
        DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
        process(cl, buildDeleteQuery(docMapper, indexService.keyspace(), cfName, id), parseElasticId(indexService, type, id).values);
    }
    
    
    public Map<String, Object> rowAsMap(final IndexService indexService, final String type, UntypedResultSet.Row row) throws IOException {
        Map<String, Object> mapObject = new HashMap<String, Object>();
        rowAsMap(indexService, type, row, mapObject);
        return mapObject;
    }
    
    
    public int rowAsMap(final IndexService indexService, final String type, UntypedResultSet.Row row, Map<String, Object> mapObject) throws IOException {
        Object[] values = rowAsArray(indexService, type, row);
        int i=0;
        int j=0;
        for(ColumnSpecification colSpec: row.getColumns()) {
            if (values[i] != null && !IdFieldMapper.NAME.equals(colSpec.name.toString())) {
                mapObject.put(colSpec.name.toString(), values[i]);
                j++;
            }
            i++;
        }
        return j;
    }
    
    
    public Object[] rowAsArray(final IndexService indexService, final String type, UntypedResultSet.Row row) throws IOException {
        return rowAsArray(indexService, type, row, false);
    }
    
    private Object value(FieldMapper fieldMapper, Object rowValue, boolean valueForSearch) {
        if (fieldMapper != null) {
            final MappedFieldType fieldType = fieldMapper.fieldType();
            return (valueForSearch) ? fieldType.valueForDisplay( rowValue ) : fieldType.cqlValue( rowValue );
        } else {
            return rowValue;
        }
    }
    
    // TODO: return raw values if no mapper found.
    public Object[] rowAsArray(final IndexService indexService, final String type, UntypedResultSet.Row row, boolean valueForSearch) throws IOException {
        final Object values[] = new Object[row.getColumns().size()];
        final DocumentMapper documentMapper = indexService.mapperService().documentMapper(type);
        final DocumentFieldMappers docFieldMappers = documentMapper.mappers();
        
        int i = 0;
        for (ColumnSpecification colSpec : row.getColumns()) {
            String columnName = colSpec.name.toString();
            CQL3Type cql3Type = colSpec.type.asCQL3Type();
            
            if (!row.has(columnName) || ByteBufferUtil.EMPTY_BYTE_BUFFER.equals(row.getBlob(columnName)) ) {
                values[i++] = null;
                continue;
            }

            if (cql3Type instanceof CQL3Type.Native) {
                final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                switch ((CQL3Type.Native) cql3Type) {
                case ASCII:
                case TEXT:
                case VARCHAR:
                    values[i] = row.getString(columnName);
                    if (values[i] != null && fieldMapper == null) {
                        ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        if (objectMapper != null && !objectMapper.isEnabled()) {
                            // parse text as JSON Map (not enabled object)
                            values[i] = FBUtilities.fromJsonMap(row.getString(columnName));
                        }
                    }
                    break;
                case TIMEUUID:
                    if (fieldMapper instanceof DateFieldMapper) {
                        // Timeuuid can be mapped to date rather than keyword.
                        values[i] = UUIDGen.unixTimestamp(row.getUUID(columnName));
                        break;
                    }
                    values[i] = row.getUUID(columnName).toString();
                    break;
                case UUID:
                    values[i] = row.getUUID(columnName).toString();
                    break;
                case TIMESTAMP:
                    values[i] = value(fieldMapper, row.getTimestamp(columnName).getTime(), valueForSearch);
                    break;
                case DATE:
                    values[i] = value(fieldMapper, SimpleDateSerializer.dayToTimeInMillis(row.getInt(columnName)), valueForSearch);
                    break;
                case TIME:
                    values[i] = value(fieldMapper, row.getLong(columnName), valueForSearch);
                    break;
                case INT:
                    values[i] = value(fieldMapper, row.getInt(columnName), valueForSearch);
                    break;
                case SMALLINT:
                    values[i] = value(fieldMapper, row.getShort(columnName), valueForSearch);
                    break;
                case TINYINT:
                    values[i] = value(fieldMapper, row.getByte(columnName), valueForSearch);
                    break;
                case BIGINT:
                    values[i] = value(fieldMapper, row.getLong(columnName), valueForSearch);
                    break;
                case DOUBLE:
                    values[i] = value(fieldMapper, row.getDouble(columnName), valueForSearch);
                    break;
                case FLOAT:
                    values[i] = value(fieldMapper, row.getFloat(columnName), valueForSearch);
                    break;
                case BLOB:
                    values[i] = value(fieldMapper, 
                            row.getBlob(columnName), 
                            valueForSearch);
                    break;
                case BOOLEAN:
                    values[i] = value(fieldMapper, row.getBoolean(columnName), valueForSearch);
                    break;
                case COUNTER:
                    logger.warn("Ignoring unsupported counter {} for column {}", cql3Type, columnName);
                    break;
                case INET:
                    values[i] = value(fieldMapper, row.getInetAddress(columnName), valueForSearch);
                    break;
                default:
                    logger.error("Ignoring unsupported type {} for column {}", cql3Type, columnName);
                }
            } else if (cql3Type.isCollection()) {
                AbstractType<?> elementType;
                switch (((CollectionType<?>) colSpec.type).kind) {
                case LIST:
                    List list;
                    elementType = ((ListType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        final List<ByteBuffer> lbb = row.getList(columnName, BytesType.instance);
                        list = new ArrayList(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            list.add(deserialize(elementType, bb, objectMapper));
                        }
                    } else {
                        final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                        final List list2 = row.getList(colSpec.name.toString(), elementType);
                        list = new ArrayList(list2.size());
                        for(Object v : list2) {
                            list.add(value(fieldMapper, v, valueForSearch));
                        }
                    }
                    values[i] =  (list.size() == 1) ? list.get(0) : list;
                    break;
                case SET :
                    Set set;
                    elementType = ((SetType<?>) colSpec.type).getElementsType();
                    if (elementType instanceof UserType) {
                        final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                        final Set<ByteBuffer> lbb = row.getSet(columnName, BytesType.instance);
                        set = new HashSet(lbb.size());
                        for (ByteBuffer bb : lbb) {
                            set.add(deserialize(elementType, bb, objectMapper));
                        }
                    } else {
                        final FieldMapper fieldMapper = docFieldMappers.smartNameFieldMapper(columnName);
                        final Set set2 = row.getSet(columnName, elementType);
                        set = new HashSet(set2.size());
                        for(Object v : set2) {
                            set.add( value(fieldMapper, v, valueForSearch) );
                        }
                    }
                    values[i] =  (set.size() == 1) ? set.iterator().next() : set;
                    break;
                case MAP :
                    Map map;
                    if (((MapType<?,?>) colSpec.type).getKeysType().asCQL3Type() != CQL3Type.Native.TEXT) {
                        throw new IOException("Only support map<text,?>, bad type for column "+columnName);
                    }
                    UTF8Type keyType = (UTF8Type) ((MapType<?,?>) colSpec.type).getKeysType();
                    elementType = ((MapType<?,?>) colSpec.type).getValuesType();
                    final ObjectMapper objectMapper = documentMapper.objectMappers().get(columnName);
                    if (elementType instanceof UserType) {
                        final Map<String, ByteBuffer> lbb = row.getMap(columnName, keyType, BytesType.instance);
                        map = new HashMap<String , Map<String, Object>>(lbb.size());
                        for(String key : lbb.keySet()) {
                            map.put(key, deserialize(elementType, lbb.get(key), objectMapper.getMapper(key)));
                        }
                    } else {
                        Map<String,Object> map2 = (Map<String,Object>) row.getMap(columnName, keyType, elementType);
                        map = new HashMap<String, Object>(map2.size());
                        for(String key : map2.keySet()) {
                            FieldMapper subMapper = (FieldMapper)objectMapper.getMapper(key);
                            map.put(key,  value(subMapper, map2.get(key), valueForSearch) );
                        }
                    }
                    values[i] =  map;
                    break;
                }
            } else if (colSpec.type instanceof UserType) {
                ByteBuffer bb = row.getBytes(columnName);
                values[i] = deserialize(colSpec.type, bb, documentMapper.objectMappers().get(columnName));
            } else if (cql3Type instanceof CQL3Type.Custom) {
                logger.warn("CQL3.Custum type not supported for column "+columnName);
            }
            i++;
        }
        return values;
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

    /**
     * CQL schema update must be asynchronous when triggered by a new dynamic field (see #91) 
     * @param indexService
     * @param type
     * @param source
     * @throws Exception
     */
    public void blockingMappingUpdate(IndexService indexService, String type, String source) throws Exception {
        TimeValue timeout = settings.getAsTime(SETTING_CLUSTER_MAPPING_UPDATE_TIMEOUT, TimeValue.timeValueSeconds(Integer.getInteger(SETTING_SYSTEM_MAPPING_UPDATE_TIMEOUT, 30)));
        BlockingActionListener mappingUpdateListener = new BlockingActionListener();
        MetaDataMappingService metaDataMappingService = ElassandraDaemon.injector().getInstance(MetaDataMappingService.class);
        PutMappingClusterStateUpdateRequest putRequest = new PutMappingClusterStateUpdateRequest()
                .indices(new org.elasticsearch.index.Index[] {indexService.index()})
                .type(type)
                .source(source)
                .ackTimeout(timeout)
                .masterNodeTimeout(timeout);
        metaDataMappingService.putMapping(putRequest, mappingUpdateListener);
        mappingUpdateListener.waitForUpdate(timeout);
    }
    
    
    public void updateDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception {
        upsertDocument(indicesService, request, indexMetaData, true);
    }
    
    public void insertDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData) throws Exception {
        upsertDocument(indicesService, request, indexMetaData, false);
    }
    
    private void upsertDocument(final IndicesService indicesService, final IndexRequest request, final IndexMetaData indexMetaData, boolean updateOperation) throws Exception {
        final IndexService indexService = indicesService.indexService(indexMetaData.getIndex());
        final IndexShard indexShard = indexService.getShard(0);
        
        final SourceToParse sourceToParse = SourceToParse.source(SourceToParse.Origin.PRIMARY, request.index(), request.type(), request.id(), request.source(), XContentType.JSON);
        if (request.routing() != null)
            sourceToParse.routing(request.routing());
        if (request.parent() != null)
            sourceToParse.parent(request.parent());
        if (request.timestamp() != null)
            sourceToParse.timestamp(request.timestamp());
        if (request.ttl() != null)
            sourceToParse.ttl(request.ttl());

        final String keyspaceName = indexMetaData.keyspace();
        final String cfName = typeToCfName(keyspaceName, request.type());

        final Engine.Index operation = indexShard.prepareIndexOnPrimary(sourceToParse, request.version(), request.versionType(), request.getAutoGeneratedTimestamp(), false);
        final Mapping update = operation.parsedDoc().dynamicMappingsUpdate();
        final boolean dynamicMappingEnable = indexService.mapperService().dynamic();
        if (update != null && dynamicMappingEnable) {
            if (logger.isDebugEnabled()) 
                logger.debug("Document source={} require a blocking mapping update of [{}]", request.sourceAsMap(), indexService.index().getName());
            // blocking Elasticsearch mapping update (required to update cassandra schema before inserting a row, this is the cost of dynamic mapping)
            blockingMappingUpdate(indexService, request.type(), update.toString());
        }

        // get the docMapper after a potential mapping update
        final DocumentMapper docMapper = indexShard.mapperService().documentMapperWithAutoCreate(request.type()).getDocumentMapper();
        
        // insert document into cassandra keyspace=index, table = type
        final Map<String, Object> sourceMap = request.sourceAsMap();
        final Map<String, ObjectMapper> objectMappers = docMapper.objectMappers();
        final DocumentFieldMappers fieldMappers = docMapper.mappers();

        Long timestamp = null;
        if (docMapper.timestampFieldMapper().enabled() && request.timestamp() != null) {
            timestamp = docMapper.timestampFieldMapper().fieldType().cqlValue(request.timestamp());
        }
        
        if (logger.isTraceEnabled()) 
            logger.trace("Insert metadata.version={} index=[{}] table=[{}] id=[{}] source={} consistency={} ttl={}",
                state().metaData().version(),
                indexService.index().getName(), cfName, request.id(), sourceMap, 
                request.waitForActiveShards().toCassandraConsistencyLevel(), request.ttl());

        final CFMetaData metadata = getCFMetaData(keyspaceName, cfName);
        
        String id = request.id();
        Map<String, ByteBuffer> map = new HashMap<String, ByteBuffer>();
        if (request.parent() != null) 
            sourceMap.put(ParentFieldMapper.NAME, request.parent());
        
        // normalize the _id and may find some column value in _id.
        // if the provided columns does not contains all the primary key columns, parse the _id to populate the columns in map.
        parseElasticId(indexService, cfName, request.id(), sourceMap);
        
        // workaround because ParentFieldMapper.value() and UidFieldMapper.value() create an Uid.
        if (sourceMap.get(ParentFieldMapper.NAME) != null && ((String)sourceMap.get(ParentFieldMapper.NAME)).indexOf(Uid.DELIMITER) < 0) {
            sourceMap.put(ParentFieldMapper.NAME, request.type() + Uid.DELIMITER + sourceMap.get(ParentFieldMapper.NAME));
        } 
       
        if (docMapper.sourceMapper().enabled()) {
            sourceMap.put(SourceFieldMapper.NAME, request.source());
        }
        
        for (String field : sourceMap.keySet()) {
            FieldMapper fieldMapper = field.startsWith(ParentFieldMapper.NAME) ? // workaround for _parent#<join_type>
                    docMapper.parentFieldMapper() : fieldMappers.getMapper( field );
            Mapper mapper = (fieldMapper != null) ? fieldMapper : objectMappers.get(field);
            ByteBuffer colName;
            if (mapper == null) {
                if (dynamicMappingEnable)
                    throw new MapperException("Unmapped field ["+field+"]");
                colName = ByteBufferUtil.bytes(field);
            } else {
                colName = mapper.cqlName();    // cached ByteBuffer column name.
            }
            final ColumnDefinition cd = metadata.getColumnDefinition(colName);
            if (cd != null) {
                // we got a CQL column.
                Object fieldValue = sourceMap.get(field);
                try {
                    if (fieldValue == null) {
                        if (cd.type.isCollection()) {
                            switch (((CollectionType<?>)cd.type).kind) {
                            case LIST :
                            case SET : 
                                map.put(field, CollectionSerializer.pack(Collections.emptyList(), 0, ProtocolVersion.CURRENT)); 
                                break;
                            case MAP :
                                break;
                            }
                        } else {
                            map.put(field, null); 
                        }
                        continue;
                    }
                    
                    if (mapper != null && mapper.cqlCollection().equals(CqlCollection.SINGLETON) && (fieldValue instanceof Collection)) {
                        throw new MapperParsingException("field " + fieldMapper.name() + " should be a single value");
                    }
                    
                    // hack to store percolate query as a string while mapper is an object mapper.
                    if (metadata.cfName.equals("_percolator") && field.equals("query")) {
                        if (cd.type.isCollection()) {
                            switch (((CollectionType<?>)cd.type).kind) {
                            case LIST :
                                if ( ((ListType)cd.type).getElementsType().asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                    // opaque list of objects serialized to JSON text 
                                    fieldValue = Collections.singletonList( stringify(fieldValue) );
                                }
                                break;
                            case SET :
                                if ( ((SetType)cd.type).getElementsType().asCQL3Type().equals(CQL3Type.Native.TEXT) &&  !(fieldValue instanceof String)) {
                                    // opaque set of objects serialized to JSON text 
                                    fieldValue = Collections.singleton( stringify(fieldValue) );
                                }
                                break;
                            }
                        } else {
                            if (cd.type.asCQL3Type().equals(CQL3Type.Native.TEXT) && !(fieldValue instanceof String)) {
                                // opaque singleton object serialized to JSON text 
                                fieldValue = stringify(fieldValue);
                            }
                        }
                    }
                    
                    map.put(field, serialize(request.index(), cfName, cd.type, field, fieldValue, mapper));
                } catch (Exception e) {
                    logger.error("[{}].[{}] failed to parse field {}={}", e, request.index(), cfName, field, fieldValue );
                    throw e;
                }
            }
        }
        
        String query;
        ByteBuffer[] values;
        if (request.opType() == DocWriteRequest.OpType.CREATE) {
            values = new ByteBuffer[map.size()];
            query = buildInsertQuery(keyspaceName, cfName, map, id, 
                    true,                
                    (request.ttl() != null) ? request.ttl().getSeconds() : null, // ttl
                    timestamp,
                    values, 0);
            final boolean applied = processWriteConditional(request.waitForActiveShards().toCassandraConsistencyLevel(), ConsistencyLevel.LOCAL_SERIAL, query, (Object[])values);
            if (!applied)
                throw new VersionConflictEngineException(indexShard.shardId(), cfName, request.id(), "PAXOS insert failed, document already exists");
        } else {
            // set empty top-level fields to null to overwrite existing columns.
            for(FieldMapper m : fieldMappers) {
                String fullname = m.name();
                if (map.get(fullname) == null && !fullname.startsWith("_") && fullname.indexOf('.') == -1 && metadata.getColumnDefinition(m.cqlName()) != null) 
                    map.put(fullname, null);
            }
            for(String m : objectMappers.keySet()) {
                if (map.get(m) == null && m.indexOf('.') == -1 && metadata.getColumnDefinition(objectMappers.get(m).cqlName()) != null)
                    map.put(m, null);
            }
            values = new ByteBuffer[map.size()];
            query = buildInsertQuery(keyspaceName, cfName, map, id, 
                    false,      
                    (request.ttl() != null) ? request.ttl().getSeconds() : null,
                    timestamp,
                    values, 0);
            process(request.waitForActiveShards().toCassandraConsistencyLevel(), query, (Object[])values);
        }
    }

    public String buildInsertQuery(final String ksName, final String cfName, Map<String, ByteBuffer> map, String id, final boolean ifNotExists, final Long ttl, 
            final Long writetime, ByteBuffer[] values, int valuesOffset) throws Exception {
        final StringBuilder questionsMarks = new StringBuilder();
        final StringBuilder columnNames = new StringBuilder();
        
        int i=0;
        for (Entry<String,ByteBuffer> entry : map.entrySet()) {
            if (entry.getKey().equals(TokenFieldMapper.NAME)) 
                continue;
            
            if (columnNames.length() > 0) {
                columnNames.append(',');
                questionsMarks.append(',');
            }
            columnNames.append("\"").append(entry.getKey()).append("\"");
            questionsMarks.append('?');
            values[valuesOffset + i] = entry.getValue();
            i++;
        }
        
        final StringBuilder query = new StringBuilder();
        query.append("INSERT INTO \"").append(ksName).append("\".\"").append(cfName)
             .append("\" (").append(columnNames.toString()).append(") VALUES (").append(questionsMarks.toString()).append(") ");
        if (ifNotExists) query.append("IF NOT EXISTS ");
        if (ttl != null && ttl > 0 || writetime != null) query.append("USING ");
        if (ttl != null && ttl > 0) query.append("TTL ").append(Long.toString(ttl));
        if (ttl != null &&ttl > 0 && writetime != null) query.append(" AND ");
        if (writetime != null) query.append("TIMESTAMP ").append(Long.toString(writetime*1000));
        
        return query.toString();
    }
    
    
    public BytesReference source(IndexService indexService, DocumentMapper docMapper, Map sourceAsMap, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        if (docMapper.sourceMapper().enabled()) {
            // retreive from _source columns stored as blob in cassandra if available.
            ByteBuffer bb = (ByteBuffer) sourceAsMap.get(SourceFieldMapper.NAME);
            if (bb != null)
               return new BytesArray(bb.array(), bb.position(), bb.limit() - bb.position());
        } 
        // rebuild _source from all cassandra columns.
        XContentBuilder builder = buildDocument(docMapper, sourceAsMap, true, isStaticDocument(indexService, uid));
        builder.humanReadable(true);
        return builder.bytes();
    }
    
    
    public BytesReference source(IndexService indexService, DocumentMapper docMapper, Map sourceAsMap, String id) throws JsonParseException, JsonMappingException, IOException {
        return source( indexService, docMapper, sourceAsMap, new Uid(docMapper.type(), id));
    }

    
    public DocPrimaryKey parseElasticId(final IndexService indexService, final String type, final String id) throws IOException {
        return parseElasticId(indexService, type, id, null);
    }
    
    /**
     * Parse elastic _id (a value or a JSON array) to build a DocPrimaryKey or populate map.
     */
    public DocPrimaryKey parseElasticId(final IndexService indexService, final String type, final String id, Map<String, Object> map) throws JsonParseException, JsonMappingException, IOException {
        String ksName = indexService.keyspace();
        String cfName = typeToCfName(ksName, type);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        List<ColumnDefinition> clusteringColumns = metadata.clusteringColumns();
        int ptLen = partitionColumns.size();
        
        if (id.startsWith("[") && id.endsWith("]")) {
            // _id is JSON array of values.
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(id, Object[].class);
            Object[] values = (map != null) ? null : new Object[elements.length];
            String[] names = (map != null) ? null : new String[elements.length];
            if (elements.length > ptLen + clusteringColumns.size()) 
                throw new JsonMappingException("_id="+id+" longer than the primary key size="+(ptLen+clusteringColumns.size()) );
            
            for(int i=0; i < elements.length; i++) {
                ColumnDefinition cd = (i < ptLen) ? partitionColumns.get(i) : clusteringColumns.get(i - ptLen);
                AbstractType<?> atype = cd.type;
                if (map == null) {
                    names[i] = cd.name.toString();
                    values[i] = atype.compose( fromString(atype, elements[i].toString()) );
                } else {
                    map.put(cd.name.toString(), atype.compose( fromString(atype, elements[i].toString()) ) );
                }
            }
            return (map != null) ? null : new DocPrimaryKey(names, values, (clusteringColumns.size() > 0 && elements.length == partitionColumns.size()) ) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> atype = partitionColumns.get(0).type;
            if (map == null) {
                return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { atype.compose(fromString(atype, id)) }, clusteringColumns.size() != 0);
            } else {
                map.put(partitionColumns.get(0).name.toString(), atype.compose( fromString(atype, id) ) );
                return null;
            }
        }
    }
    
    public DocPrimaryKey parseElasticRouting(final IndexService indexService, final String type, final String routing) throws JsonParseException, JsonMappingException, IOException {
        String ksName = indexService.keyspace();
        String cfName = typeToCfName(ksName, type);
        CFMetaData metadata = getCFMetaData(ksName, cfName);
        List<ColumnDefinition> partitionColumns = metadata.partitionKeyColumns();
        int ptLen = partitionColumns.size();
        if (routing.startsWith("[") && routing.endsWith("]")) {
            // _routing is JSON array of values.
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(routing, Object[].class);
            Object[] values = new Object[elements.length];
            String[] names = new String[elements.length];
            if (elements.length != ptLen) 
                throw new JsonMappingException("_routing="+routing+" does not match the partition key size="+ptLen);
            
            for(int i=0; i < elements.length; i++) {
                ColumnDefinition cd = partitionColumns.get(i);
                AbstractType<?> atype = cd.type;
                names[i] = cd.name.toString();
                values[i] = atype.compose( fromString(atype, elements[i].toString()) );
                i++;
            }
            return new DocPrimaryKey(names, values) ;
        } else {
            // _id is a single columns, parse its value.
            AbstractType<?> atype = partitionColumns.get(0).type;
            return new DocPrimaryKey( new String[] { partitionColumns.get(0).name.toString() } , new Object[] { atype.compose( fromString(atype, routing) ) });
        }
    }
    
    public Token getToken(final IndexService indexService, final String type, final String routing) throws JsonParseException, JsonMappingException, IOException {
        DocPrimaryKey pk = parseElasticRouting(indexService, type, routing);
        CFMetaData cfm = getCFMetaData(indexService.keyspace(), type);
        CBuilder builder = CBuilder.create(cfm.getKeyValidatorAsClusteringComparator());
        for (int i = 0; i < cfm.partitionKeyColumns().size(); i++)
            builder.add(pk.values[i]);
        return cfm.partitioner.getToken(CFMetaData.serializePartitionKey(builder.build()));
    }
    
    public Set<Token> getTokens(final IndexService indexService, final String[] types, final String routing) throws JsonParseException, JsonMappingException, IOException {
        Set<Token> tokens = new HashSet<Token>();
        if (types != null && types.length > 0) {
            for(String type : types)
                tokens.add(getToken(indexService, type, routing));
        }
        return tokens;
    }
    
    public boolean isStaticDocument(final IndexService indexService, Uid uid) throws JsonParseException, JsonMappingException, IOException {
        CFMetaData metadata = getCFMetaData(indexService.keyspace(), typeToCfName(indexService.keyspace(), uid.type()));
        String id = uid.id();
        if (id.startsWith("[") && id.endsWith("]")) {
            org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();
            Object[] elements = jsonMapper.readValue(id, Object[].class);
            return metadata.clusteringColumns().size() > 0 && elements.length == metadata.partitionKeyColumns().size();
        } else {
            return metadata.clusteringColumns().size() != 0;
        }
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
    
    
    public class MetadataSchemaUpdate {
        long version;
        long timestamp;
        String metaDataString;
        
        MetadataSchemaUpdate(String metaDataString, long version) {
            this.metaDataString = metaDataString;
            this.version = version;
            this.timestamp = FBUtilities.timestampMicros();
        }
    }
    
    public void writeMetaDataAsComment(MetaData metaData) throws ConfigurationException, IOException {
        writeMetaDataAsComment( MetaData.Builder.toXContent(metaData, MetaData.CASSANDRA_FORMAT_PARAMS), metaData.version());
    }
        
    public void writeMetaDataAsComment(String metaDataString, long version) throws ConfigurationException, IOException {
        // Issue #91, update C* schema asynchronously to avoid inter-locking with map column as nested object.
        logger.trace("Submit asynchronous CQL schema update for metadata={}", metaDataString);
        this.lastMetadataToSave.set(new MetadataSchemaUpdate(metaDataString, version));
        this.metadataToSaveSemaphore.release();
    }

    /**
     * Should only be used after a SCHEMA change.
     */
    
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
    
    private MetaData parseMetaDataString(String metadataString) throws NoPersistedMetaDataException {
        if (metadataString != null && metadataString.length() > 0) {
            MetaData metaData;
            try {
                metaData =  metaStateService.loadGlobalState(metadataString);
                
                // initialize typeToCfName map for later reverse lookup in ElasticSecondaryIndex
                for(ObjectCursor<IndexMetaData> indexCursor : metaData.indices().values()) {
                    for(ObjectCursor<MappingMetaData> mappingCursor :  indexCursor.value.getMappings().values()) {
                        String cfName = typeToCfName(indexCursor.value.keyspace(), mappingCursor.value.type());
                        logger.info("keyspace.table={}.{} registred for elasticsearch index.type={}.{}", 
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

    /**
     * Try to read fresher metadata from cassandra.
     */
    
    public MetaData checkForNewMetaData(Long expectedVersion) throws NoPersistedMetaDataException {
        MetaData localMetaData = readMetaDataAsRow(ConsistencyLevel.ONE);
        if (localMetaData != null && localMetaData.version() >= expectedVersion) {
           return localMetaData;
        }
        if (localMetaData == null) {
            throw new NoPersistedMetaDataException("No cluster metadata in "+this.elasticAdminKeyspaceName);
        }
        MetaData quorumMetaData = readMetaDataAsRow(this.metadataReadCL);
        if (quorumMetaData.version() >= expectedVersion) {
            return quorumMetaData;
        }
        return null;
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
            if (row.has("rpc_address") && DatabaseDescriptor.getLocalDataCenter().equals(row.getString("data_center")))
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
    Void createElasticAdminMetaTable(final String metaDataString) {
        try {
            String createTable = String.format(Locale.ROOT, "CREATE TABLE IF NOT EXISTS \"%s\".%s ( cluster_name text PRIMARY KEY, owner uuid, version bigint, metadata text) WITH comment='%s';",
                elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE, metaDataString);
            logger.info(createTable);
            process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), createTable);
        } catch (Exception e) {
            logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to initialize table {}.{}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE), e);
            throw e;
        }
        return null;
    }

    // initialize a first row if needed
    Void insertFirstMetaRow(final MetaData metadata, final String metaDataString) {
        try {
            logger.info(insertMetadataQuery);
            process(ConsistencyLevel.LOCAL_ONE, ClientState.forInternalCalls(), insertMetadataQuery,
                DatabaseDescriptor.getClusterName(), UUID.fromString(StorageService.instance.getLocalHostId()), metadata.version(), metaDataString);
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
                final String metaDataString;
                try {
                    metaDataString = MetaData.Builder.toXContent(metadata);
                } catch (IOException e) {
                    logger.error("Failed to build metadata", e);
                    throw new NoPersistedMetaDataException("Failed to build metadata", e);
                }
                // create elastic_admin if not exists after joining the ring and before allowing metadata update.
                retry(() -> createElasticAdminKeyspace(), "create elastic admin keyspace");
                retry(() -> createElasticAdminMetaTable(metaDataString), "create elastic admin metadata table");
                retry(() -> insertFirstMetaRow(metadata, metaDataString), "write first row to metadata table");
                logger.info("Succefully initialize {}.{} = {}", elasticAdminKeyspaceName, ELASTIC_ADMIN_METADATA_TABLE, metaDataString);
                try {
                    writeMetaDataAsComment(metaDataString, metadata.version());
                } catch (IOException e) {
                    logger.error("Failed to write metadata as comment", e);
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
                    logger.error((Supplier<?>) () -> new ParameterizedMessage("Failed to alter keyspace [{}]", elasticAdminKeyspaceName),e);
                    throw e;
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
    
    
    public void persistMetaData(MetaData oldMetaData, MetaData newMetaData, String source) throws IOException, InvalidRequestException, RequestExecutionException, RequestValidationException {
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
                new Object[] { owner, newMetaData.version(), metaDataString, DatabaseDescriptor.getClusterName(), newMetaData.version() });
        if (applied) {
            logger.debug("PAXOS Succefully update metadata source={} newMetaData={} in cluster {}", source, metaDataString, DatabaseDescriptor.getClusterName());
            writeMetaDataAsComment(metaDataString, newMetaData.version());
            return;
        } else {
            logger.warn("PAXOS Failed to update metadata oldMetadata={}/{} currentMetaData={}/{} in cluster {}", 
                    oldMetaData.clusterUUID(), oldMetaData.version(), localNode().getId(), newMetaData.version(), DatabaseDescriptor.getClusterName());
            throw new ConcurrentMetaDataUpdateException(owner, newMetaData.version());
        }
    }

    public static Collection flattenCollection(Collection c) {
        List l = new ArrayList(c.size());
        for(Object o : c) {
            if (o instanceof Collection) {
                l.addAll( flattenCollection((Collection)o) );
            } else {
                l.add(o);
            }
        }
        return l;
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
        //logger.info("set log level to {} for classes under '{}' (if the level doesn't look like '{}' then the logger couldn't parse '{}')", level, classQualifier, rawLevel, rawLevel);
    }

    private static boolean hasAppenders(ch.qos.logback.classic.Logger logger)
    {
        Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
        return it.hasNext();
    }
    
    /**
     * Serialize a cassandra typed object.
     */
    public static ByteBuffer serialize(final String ksName, final String cfName, final AbstractType type, final String name, final Object value, final Mapper mapper) 
            throws SyntaxException, ConfigurationException, JsonGenerationException, JsonMappingException, IOException {
        if (value == null) {
            return null;
        }
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            ByteBuffer[] components = new ByteBuffer[udt.size()];
            int i=0;
            
            if (GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                GeoPoint geoPoint = new GeoPoint();
                if (value instanceof String) {
                    // parse from string lat,lon (ex: "41.12,-71.34") or geohash (ex:"drm3btev3e86")
                    geoPoint.resetFromString((String)value);
                } else {
                    // parse from lat, lon fields as map
                    Map<String, Object> mapValue = (Map<String, Object>) value;
                    geoPoint.reset((Double)mapValue.get(GeoPointFieldMapper.Names.LAT), (Double)mapValue.get(GeoPointFieldMapper.Names.LON));
                }
                components[i++]=serialize(ksName, cfName, udt.fieldType(0), GeoPointFieldMapper.Names.LAT, geoPoint.lat(), null);
                components[i++]=serialize(ksName, cfName, udt.fieldType(1), GeoPointFieldMapper.Names.LON, geoPoint.lon(), null);
            } else if (COMPLETION_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                // input list<text>, output text, weight int, payload text
                Map<String, Object> mapValue = (Map<String, Object>) value;
                components[i++]=(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_INPUT) == null) ? null : serialize(ksName, cfName, udt.fieldType(0), CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_INPUT, mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_INPUT), null);
                components[i++]=(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_CONTEXTS) == null) ? null : serialize(ksName, cfName, udt.fieldType(1), CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_CONTEXTS, jsonMapper.writeValueAsString(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_CONTEXTS)), null);
                components[i++]=(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_WEIGHT) == null) ? null : serialize(ksName, cfName, udt.fieldType(2), CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_WEIGHT, (Integer) mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_WEIGHT), null);
            } else {
                Map<String, Object> mapValue = (Map<String, Object>) value;
                for (int j = 0; j < udt.size(); j++) {
                    String subName = UTF8Type.instance.compose(udt.fieldName(j).bytes);
                    AbstractType<?> subType = udt.fieldType(j);
                    Object subValue = mapValue.get(subName);
                    Mapper subMapper = (mapper instanceof ObjectMapper) ? ((ObjectMapper) mapper).getMapper(subName) : null;
                    components[i++]=serialize(ksName, cfName, subType, subName, subValue, subMapper);
                }
            }
            return TupleType.buildValue(components);
        } else if (type instanceof MapType) {
            MapType mapType = ClusterService.getMapType(ksName, cfName, name);
            MapSerializer serializer = mapType.getSerializer();
            Map map = (Map)value;
            List<ByteBuffer> buffers = serializer.serializeValues((Map)value);
            return CollectionSerializer.pack(buffers, map.size(), ProtocolVersion.CURRENT);
        } else if (type instanceof CollectionType) {
            AbstractType elementType = (type instanceof ListType) ? ((ListType)type).getElementsType() : ((SetType)type).getElementsType();
            
            if (elementType instanceof UserType && ClusterService.GEO_POINT_TYPE.equals(ByteBufferUtil.string(((UserType)elementType).name)) && value instanceof List && ((List)value).get(0) instanceof Double) {
                // geo_point as array of double lon,lat like [1.2, 1.3]
                UserType udt = (UserType)elementType;
                List<Double> values = (List<Double>)value;
                ByteBuffer[] elements = new ByteBuffer[] {
                    serialize(ksName, cfName, udt.fieldType(0), GeoPointFieldMapper.Names.LAT, values.get(1), null),
                    serialize(ksName, cfName, udt.fieldType(1), GeoPointFieldMapper.Names.LON, values.get(0), null)
                };
                ByteBuffer geo_point = TupleType.buildValue(elements);
                return CollectionSerializer.pack(ImmutableList.of(geo_point), 1, ProtocolVersion.CURRENT);
            } 
            
            if (value instanceof Collection) {
                // list of elementType
                List<ByteBuffer> elements = new ArrayList<ByteBuffer>();
                for(Object v : flattenCollection((Collection) value)) {
                    ByteBuffer bb = serialize(ksName, cfName, elementType, name, v, mapper);
                    elements.add(bb);
                }
                return CollectionSerializer.pack(elements, elements.size(), ProtocolVersion.CURRENT);
            } else {
                // singleton list
                ByteBuffer bb = serialize(ksName, cfName, elementType, name, value, mapper);
                return CollectionSerializer.pack(ImmutableList.of(bb), 1, ProtocolVersion.CURRENT);
            } 
        } else {
            // Native cassandra type, encoded with mapper if available.
            if (mapper != null) {
                if (mapper instanceof FieldMapper) {
                    return type.decompose( ((FieldMapper) mapper).fieldType().cqlValue(value, type) );
                } else if (mapper instanceof ObjectMapper && !((ObjectMapper)mapper).isEnabled()) {
                    // enabled=false => store field as json text
                    if (value instanceof Map) {
                        XContentBuilder builder = XContentFactory.jsonBuilder();
                        builder.map( (Map) value);
                        return type.decompose( builder.string() );
                    }
                    return type.decompose( value );
                }
            } 
            return type.decompose( value );
        }
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb) throws CharacterCodingException {
        return deserialize(type, bb, null);
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb, Mapper mapper) throws CharacterCodingException {
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            Map<String, Object> mapValue = new HashMap<String, Object>();
            ByteBuffer[] components = udt.split(bb);
            
            if (GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                if (components[0] != null) 
                    mapValue.put(GeoPointFieldMapper.Names.LAT, deserialize(udt.type(0), components[0], null));
                if (components[1] != null) 
                    mapValue.put(GeoPointFieldMapper.Names.LON, deserialize(udt.type(1), components[1], null));
            } else {
                for (int i = 0; i < components.length; i++) {
                    String fieldName = UTF8Type.instance.compose(udt.fieldName(i).bytes);
                    AbstractType<?> ctype = udt.type(i);
                    Mapper subMapper = null;
                    if (mapper != null && mapper instanceof ObjectMapper)
                        subMapper = ((ObjectMapper)mapper).getMapper(fieldName);
                    Object value = (components[i] == null) ? null : deserialize(ctype, components[i], subMapper);
                    mapValue.put(fieldName, value);
                }
            }
            return mapValue;
        } else if (type instanceof ListType) {
            ListType<?> ltype = (ListType<?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, ProtocolVersion.CURRENT);
            List list = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                list.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, ProtocolVersion.CURRENT), mapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return list;
        } else if (type instanceof SetType) {
            SetType<?> ltype = (SetType<?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, ProtocolVersion.CURRENT);
            Set set = new HashSet(size);
            for (int i = 0; i < size; i++) {
                set.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, ProtocolVersion.CURRENT), mapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return set;
        } else if (type instanceof MapType) {
            MapType<?,?> ltype = (MapType<?,?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, ProtocolVersion.CURRENT);
            Map map = new LinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                ByteBuffer kbb = CollectionSerializer.readValue(input, ProtocolVersion.CURRENT);
                ByteBuffer vbb = CollectionSerializer.readValue(input, ProtocolVersion.CURRENT);
                String key = (String) ltype.getKeysType().compose(kbb);
                Mapper subMapper = null;
                if (mapper != null) {
                    assert mapper instanceof ObjectMapper : "Expecting an object mapper for MapType";
                    subMapper = ((ObjectMapper)mapper).getMapper(key);
                }
                map.put(key, deserialize(ltype.getValuesType(), vbb, subMapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return map;
        } else {
             Object value = type.compose(bb);
             if (mapper != null && mapper instanceof FieldMapper) {
                 return ((FieldMapper)mapper).fieldType().valueForDisplay(value);
             }
             return value;
        }
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
    
    /**
     * publish gossip states when setup completed.
     */
    public void publishGossipStates() {
        if (this.discovery != null) {
            this.discovery.publishX2(state());
            try {
                this.discovery.publishX1();
            } catch (IOException e) {
                logger.error("Unexpected error", e);
            }
        }
    }
}
