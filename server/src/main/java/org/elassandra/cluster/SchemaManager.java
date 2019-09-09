/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elassandra.cluster;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.collect.ImmutableMap;

import org.antlr.runtime.RecognitionException;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.ColumnDefinition.Kind;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.config.ViewDefinition;
import org.apache.cassandra.cql3.CFName;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.CQLFragmentParser;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.CqlParser;
import org.apache.cassandra.cql3.FieldIdentifier;
import org.apache.cassandra.cql3.IndexName;
import org.apache.cassandra.cql3.UTName;
import org.apache.cassandra.cql3.statements.AlterTableStatement;
import org.apache.cassandra.cql3.statements.AlterTableStatementColumn;
import org.apache.cassandra.cql3.statements.AlterTypeStatement;
import org.apache.cassandra.cql3.statements.CreateIndexStatement;
import org.apache.cassandra.cql3.statements.CreateKeyspaceStatement;
import org.apache.cassandra.cql3.statements.CreateTableStatement;
import org.apache.cassandra.cql3.statements.CreateTypeStatement;
import org.apache.cassandra.cql3.statements.DropIndexStatement;
import org.apache.cassandra.cql3.statements.DropTableStatement;
import org.apache.cassandra.cql3.statements.IndexPropDefs;
import org.apache.cassandra.cql3.statements.IndexTarget;
import org.apache.cassandra.cql3.statements.KeyspaceAttributes;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.cql3.statements.TableAttributes;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.locator.NetworkTopologyStrategy;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.schema.KeyspaceMetadata;
import org.apache.cassandra.schema.KeyspaceParams;
import org.apache.cassandra.schema.SchemaKeyspace;
import org.apache.cassandra.schema.TableParams;
import org.apache.cassandra.schema.Views;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.MigrationListener;
import org.apache.cassandra.thrift.ThriftValidation;
import org.apache.cassandra.transport.Event;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.elassandra.index.mapper.internal.TokenFieldMapper;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.index.mapper.CompletionFieldMapper;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.GeoShapeFieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapper.CqlCollection;
import org.elasticsearch.index.mapper.Mapper.CqlStruct;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.RangeFieldMapper;
import org.elasticsearch.index.mapper.SourceFieldMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;

public class SchemaManager extends AbstractComponent {
    final ClusterService clusterService;
    final SchemaListener schemaListener;

    /**
     * Inhibited MigrationListener avoid loops when applying new cluster state in CQL schema.
     */
    private final Collection<MigrationListener> inhibitedSchemaListeners;

    public static final String GEO_POINT_TYPE = "geo_point";
    public static final ColumnIdentifier GEO_POINT_NAME = new ColumnIdentifier(GEO_POINT_TYPE, true);
    public static final String ATTACHMENT_TYPE = "attachement";
    public static final ColumnIdentifier ATTACHMENT_NAME = new ColumnIdentifier(ATTACHMENT_TYPE, true);
    public static final String COMPLETION_TYPE = "completion";
    public static final ColumnIdentifier COMPLETION_NAME = new ColumnIdentifier(COMPLETION_TYPE, true);

    static final Map<String, CQL3Type.Raw> GEO_POINT_FIELDS = new ImmutableMap.Builder<String, CQL3Type.Raw>()
            .put(org.elasticsearch.common.geo.GeoUtils.LATITUDE, CQL3Type.Raw.from(CQL3Type.Native.DOUBLE))
            .put(org.elasticsearch.common.geo.GeoUtils.LONGITUDE, CQL3Type.Raw.from(CQL3Type.Native.DOUBLE))
            .build();

    static final Map<String, CQL3Type.Raw> COMPLETION_FIELDS = new ImmutableMap.Builder<String, CQL3Type.Raw>()
            .put("input",  CQL3Type.Raw.list(CQL3Type.Raw.from(CQL3Type.Native.TEXT)))
            .put("contexts", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .put("weight", CQL3Type.Raw.from(CQL3Type.Native.INT))
            .build();

    static final Map<String, CQL3Type.Raw> ATTACHMENT_FIELDS = new ImmutableMap.Builder<String, CQL3Type.Raw>()
            .put("context", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .put("content_type", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .put("content_length", CQL3Type.Raw.from(CQL3Type.Native.BIGINT))
            .put("date", CQL3Type.Raw.from(CQL3Type.Native.TIMESTAMP))
            .put("title", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .put("author", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .put("keywords", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .put("language", CQL3Type.Raw.from(CQL3Type.Native.TEXT))
            .build();

    public static final String PERCOLATOR_TABLE = "_percolator";

    public static final String ELASTIC_ID_COLUMN_NAME = "_id";

    public static Map<String, String> cqlMapping = new ImmutableMap.Builder<String,String>()
            .put("text", "keyword")
            .put("varchar", "keyword")
            .put("timestamp", "date")
            .put("date", "date")
            .put("time", "long")
            .put("smallint", "short")
            .put("tinyint", "byte")
            .put("int", "integer")
            .put("bigint", "long")
            .put("double", "double")
            .put("float", "float")
            .put("boolean", "boolean")
            .put("blob", "binary")
            .put("inet", "ip" )
            .put("uuid", "keyword" )
            .put("timeuuid", "keyword" )
            .put("decimal", "keyword" )
            .build();

    public SchemaManager(Settings settings, ClusterService clusterService) {
        super(settings);
        this.clusterService = clusterService;
        this.schemaListener = new SchemaListener(settings, clusterService);
        this.inhibitedSchemaListeners = Collections.singletonList(this.schemaListener);
    }

    public SchemaListener getSchemaListener() {
        return schemaListener;
    }

    public Collection<MigrationListener> getInhibitedSchemaListeners() {
        return inhibitedSchemaListeners;
    }

    public boolean isNativeCql3Type(String cqlType) {
        return cqlMapping.keySet().contains(cqlType) && !cqlType.startsWith("geo_");
    }

    // Because Cassandra table name does not support dash, convert dash to underscore in elasticsearch type, an keep this information
    // in a map for reverse lookup. Of course, conflict is still possible in a keyspace.
    private static final Map<String, String> cfNameToType = new ConcurrentHashMap<String, String>() {{
       put(PERCOLATOR_TABLE, "percolator");
    }};

    public static String typeToCfName(String keyspaceName, String typeName) {
        return typeToCfName(keyspaceName, typeName, false);
    }

    public static String typeToCfName(String keyspaceName, String typeName, boolean remove) {
        if (typeName.indexOf('-') >= 0) {
            String cfName = typeName.replaceAll("\\-", "_");
            if (remove) {
                cfNameToType.remove(keyspaceName+"."+cfName);
            } else {
                cfNameToType.put(keyspaceName+"."+cfName, typeName);
            }
            return cfName;
        }
        return typeName;
    }

    public String typeToCfName(CFMetaData cfm, String typeName, boolean remove) {
        return SchemaManager.typeToCfName(cfm.ksName, typeName);
    }

    public static String cfNameToType(String keyspaceName, String cfName) {
        if (cfName.indexOf('_') >= 0) {
            String type = cfNameToType.get(keyspaceName+"."+cfName);
            if (type != null)
                return type;
        }
        return cfName;
    }

    public static String buildIndexName(final String cfName) {
        return new StringBuilder("elastic_")
            .append(cfName)
            .append("_idx").toString();
    }

    public static CFMetaData getCFMetaData(final String ksName, final String cfName) throws ActionRequestValidationException {
        CFMetaData metadata = Schema.instance.getCFMetaData(ksName, cfName);
        if (metadata == null) {
            ActionRequestValidationException arve = new ActionRequestValidationException();
            arve.addValidationError(ksName+"."+cfName+" table does not exists");
            throw arve;
        }
        return metadata;
    }

    public static KeyspaceMetadata getKSMetaData(final String ksName) throws ActionRequestValidationException {
        KeyspaceMetadata metadata = Schema.instance.getKSMetaData(ksName);
        if (metadata == null) {
            ActionRequestValidationException arve = new ActionRequestValidationException();
            arve.addValidationError("Keyspace " + ksName + " does not exists");
            throw arve;
        }
        return metadata;
    }

    private Pair<KeyspaceMetadata, UserType> createUserTypeIfNotExists(KeyspaceMetadata ksm, String typeName, Map<String, CQL3Type.Raw> fields,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) throws RequestExecutionException
    {
        ColumnIdentifier ci = new ColumnIdentifier(typeName, true);
        UTName name = new UTName(new ColumnIdentifier(ksm.name, true), ci);
        Optional<UserType> typeOption = getType(ksm, ci);
        if (typeOption.isPresent())
            return Pair.create(ksm, typeOption.get());

        logger.debug("create type keyspace=[{}] name=[{}] fields={}", ksm.name, typeName, fields);
        CreateTypeStatement typeStatement = new CreateTypeStatement(name, true);
        for(Map.Entry<String, CQL3Type.Raw> field : fields.entrySet())
            typeStatement.addDefinition(FieldIdentifier.forInternalString(field.getKey()), field.getValue());
        typeStatement.validate(ClientState.forInternalCalls(), ksm, FBUtilities.timestampMicros());

        UserType userType = typeStatement.createType(ksm);
        CreateTypeStatement.checkForDuplicateNames(userType);

        Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
        SchemaKeyspace.addTypeToSchemaMutation(userType, builder);
        mutations.add(builder.build());

        events.add(new Event.SchemaChange(Event.SchemaChange.Change.CREATED, Event.SchemaChange.Target.TYPE, ksm.name, typeName));
        KeyspaceMetadata ksm2 = ksm.withSwapped(ksm.types.with(userType));
        return Pair.create(ksm2, userType);
    }

    private Pair<KeyspaceMetadata, UserType> createOrUpdateUserType(KeyspaceMetadata ksm, String typeName, Map<String, CQL3Type.Raw> fields,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) {
        ColumnIdentifier ci = new ColumnIdentifier(typeName, true);
        Optional<UserType> userTypeOption = getType(ksm, ci);
        if (!userTypeOption.isPresent()) {
            return createUserTypeIfNotExists(ksm, typeName, fields, mutations, events);
        } else {
            UserType userType = userTypeOption.get();
            UTName name = new UTName(new ColumnIdentifier(ksm.name, true), ci);
            logger.trace("update keyspace.type=[{}].[{}] fields={}", ksm.name, typeName, fields);
            for(Map.Entry<String, CQL3Type.Raw> field : fields.entrySet()) {
                FieldIdentifier fieldIdentifier = FieldIdentifier.forInternalString(field.getKey());
                int i = userType.fieldPosition(fieldIdentifier);
                if (i == -1) {
                    // add missing field
                    logger.trace("add field to keyspace.type=[{}].[{}] field={}", ksm.name, typeName, fieldIdentifier);
                    AlterTypeStatement ats = AlterTypeStatement.addition(name, fieldIdentifier, field.getValue());
                    userType = ats.updateUserType(ksm, mutations, events);
                } else {
                    if (!userType.fieldType(i).asCQL3Type().equals(field.getValue().prepare(ksm)))
                        throw new InvalidRequestException(
                                String.format(Locale.ROOT, "Field \"%s\" with type %s does not match type %s",
                                        field.getKey(), userType.fieldType(i).asCQL3Type(), field.getValue().prepare(ksm)));
                }
            }
            return Pair.create(ksm, userType);
        }
    }

    private Optional<UserType> getType(KeyspaceMetadata ksm, ColumnIdentifier typeName) {
        UTName name = new UTName(new ColumnIdentifier(ksm.name, true), typeName);
        return ksm.types.get(name.getUserTypeName());
    }

    private Pair<KeyspaceMetadata, CQL3Type.Raw> createRawTypeIfNotExists(KeyspaceMetadata ksm,
            String typeName,
            Map<String, CQL3Type.Raw> fields,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) {
        Pair<KeyspaceMetadata, UserType> x = createUserTypeIfNotExists(ksm, typeName, fields, mutations, events);
        UTName ut = new UTName(new  ColumnIdentifier(ksm.name, true), new  ColumnIdentifier(x.right.getNameAsString(), true));
        CQL3Type.Raw type = CQL3Type.Raw.userType(ut);
        type.freeze();
        return Pair.create(x.left, type);
    }

    public KeyspaceMetadata createOrUpdateKeyspace(final String ksName,
            final int replicationFactor,
            final Map<String, Integer> replicationMap,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) {
        KeyspaceMetadata ksm;
        Keyspace ks = null;
        try {
            ks = Keyspace.open(ksName);
            if (ks != null && !(ks.getReplicationStrategy() instanceof NetworkTopologyStrategy)) {
                throw new SettingsException("Cannot create index, underlying keyspace requires the NetworkTopologyStrategy.");
            }
        } catch(AssertionError | NullPointerException e) {
        }
        if (ks != null) {
            // TODO: check replication
            ksm =  ks.getMetadata().copy();
        } else {
            Map<String, String> replication = new HashMap<>();
            replication.put("class", "NetworkTopologyStrategy");
            replication.put(DatabaseDescriptor.getLocalDataCenter(), Integer.toString(replicationFactor));
            for(Map.Entry<String, Integer> entry : replicationMap.entrySet())
                replication.put(entry.getKey(), Integer.toString(entry.getValue()));
            logger.trace("Creating new keyspace [{}] with replication={}", ksName, replication);

            KeyspaceAttributes ksAttrs = new KeyspaceAttributes();
            ksAttrs.addProperty(KeyspaceParams.Option.DURABLE_WRITES.toString(), "true");
            ksAttrs.addProperty(KeyspaceParams.Option.REPLICATION.toString(), replication);

            CreateKeyspaceStatement ksStatement = new CreateKeyspaceStatement(ksName, ksAttrs, true);
            ksStatement.validate(ClientState.forInternalCalls());
            ksm =  KeyspaceMetadata.create(ksStatement.keyspace(), ksAttrs.asNewKeyspaceParams());
            mutations.add(SchemaKeyspace.makeCreateKeyspaceMutation(ksm, FBUtilities.timestampMicros()).build());
            events.add(new Event.SchemaChange(Event.SchemaChange.Change.CREATED, ksName));
        }
        return ksm;
    }

    public KeyspaceMetadata createTable(final KeyspaceMetadata ksm, String cfName, String indexName,
            List<ColumnDescriptor> columns,
            String tableOptions,
            final IndexMetaData indexMetaData,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) throws IOException, RecognitionException {
        CFName cfn = new CFName();
        cfn.setColumnFamily(cfName, true);
        cfn.setKeyspace(ksm.name, true);
        CreateTableStatement.RawStatement cts = new CreateTableStatement.RawStatement(cfn, true);

        logger.debug("columns="+columns);
        List<ColumnIdentifier> pk = new ArrayList<>();
        for(ColumnDescriptor cd : columns) {
            ColumnIdentifier ci = ColumnIdentifier.getInterned(cd.name, true);
            cts.addDefinition(ci, cd.type, cd.kind == Kind.STATIC);
            if (cd.kind == Kind.PARTITION_KEY)
                pk.add(ci);
            if (cd.kind == Kind.CLUSTERING) {
                cts.addColumnAlias(ci);
                cts.properties.setOrdering(ci, cd.desc);
            }
        }
        cts.addKeyAliases(pk);

        if (tableOptions != null && tableOptions.length() > 0) {
            CQLFragmentParser.parseAnyUnhandled(new CQLFragmentParser.CQLParserFunction<TableAttributes>() {
                @Override
                public TableAttributes parse(CqlParser parser) throws RecognitionException {
                    parser.properties(cts.properties.properties);
                    return cts.properties.properties;
                }
            }, tableOptions);
        }

        ParsedStatement.Prepared stmt = cts.prepare(ksm.types);
        CFMetaData cfm = ((CreateTableStatement)stmt.statement).getCFMetaData();

        Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
        SchemaKeyspace.addTableToSchemaMutation(cfm, true, builder);
        addTableExtensionsToMutationBuilder(cfm, indexMetaData, builder);
        mutations.add(builder.build());
        events.add(new Event.SchemaChange(Event.SchemaChange.Change.CREATED, Event.SchemaChange.Target.TABLE, cts.keyspace(), cts.columnFamily()));
        return ksm.withSwapped(ksm.tables.with(cfm));
    }

    // add only new columns and update table extension with mapping metadata
    public KeyspaceMetadata updateTable(final KeyspaceMetadata ksm, String cfName, String indexName,
            List<ColumnDescriptor> columns,
            TableAttributes tableAttrs,
            final IndexMetaData indexMetaData,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) {
        KeyspaceMetadata ksm2 = ksm;
        CFMetaData cfm = ThriftValidation.validateColumnFamily(ksm, cfName);
        CFName cfn = new CFName();
        cfn.setColumnFamily(cfName, true);
        cfn.setKeyspace(ksm.name, true);

        List<AlterTableStatementColumn> colDataList = columns.stream()
                .filter(cd -> !cd.exists())
                .map(cd ->  new AlterTableStatementColumn(ColumnDefinition.Raw.forColumn(cd.createColumnDefinition(ksm, cfName)), cd.type, cd.kind == Kind.STATIC))
                .collect(Collectors.toList());

        boolean changed = false;
        Pair<CFMetaData, List<ViewDefinition>> x = Pair.create(cfm, StreamSupport.stream(ksm.views(cfName).spliterator(), false).collect(Collectors.toList()));
        if (colDataList.size() > 0) {
            logger.debug("table {}.{} add columns={}", ksm.name, cfName, columns);
            AlterTableStatement ats = new AlterTableStatement(cfn, AlterTableStatement.Type.ADD, colDataList, tableAttrs, null, null);
            x = ats.updateTable(ksm, cfm, FBUtilities.timestampMicros());
            mutations.add(SchemaKeyspace.makeUpdateTableMutation(ksm, cfm, x.left, FBUtilities.timestampMicros()).build());
            changed = true;
        }

        logger.debug("table {}.{} set extensions for index {}", ksm.name, cfName, indexName);
        Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
        x = Pair.create(addTableExtensionsToMutationBuilder(x.left, indexMetaData, builder), x.right);
        mutations.add(builder.build());

        ksm2 = ksm.withSwapped(ksm.tables.without(cfm.cfName).with(x.left));
        if (x.right != null && x.right.size() > 0) {
            ksm2 = ksm2.withSwapped(Views.builder().add(x.right).build());
            for(ViewDefinition view : x.right) {
                builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
                mutations.add(SchemaKeyspace.makeUpdateViewMutation(builder, ksm2.views.getNullable(view.viewName), view).build());
            }
        }
        events.add(new Event.SchemaChange(Event.SchemaChange.Change.UPDATED, Event.SchemaChange.Target.TABLE, ksm.name, cfName));
        return ksm2;
    }

    private CFMetaData addTableExtensionsToMutationBuilder(CFMetaData cfm, final IndexMetaData indexMetaData, Mutation.SimpleBuilder builder) {
        Map<String, ByteBuffer> extensions = new LinkedHashMap<String, ByteBuffer>();
        if (cfm.params != null && cfm.params.extensions != null)
            extensions.putAll(cfm.params.extensions);
        clusterService.putIndexMetaDataExtension(indexMetaData, extensions);
        CFMetaData cfm2 = cfm.copy();
        cfm2.extensions(extensions);
        SchemaKeyspace.addTableExtensionsToSchemaMutation(cfm2, extensions, builder);
        return cfm2;
    }

    public CFMetaData removeTableExtensionToMutationBuilder(CFMetaData cfm, final Set<IndexMetaData> indexMetaDataSet, Mutation.SimpleBuilder builder) {
        CFMetaData cfm2 = cfm.copy();
        Map<String, ByteBuffer> extensions = new LinkedHashMap<String, ByteBuffer>();
        if (cfm.params != null && cfm.params.extensions != null) {
            Set<String> toRemoveExtentsions = indexMetaDataSet.stream().map(imd -> clusterService.getExtensionKey(imd)).collect(Collectors.toSet());
            extensions = cfm.params.extensions.entrySet().stream()
                .filter( x -> !toRemoveExtentsions.contains(x.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        cfm2.extensions(extensions);
        SchemaKeyspace.addTableExtensionsToSchemaMutation(cfm2, extensions, builder);
        return cfm2;
    }

    public Pair<KeyspaceMetadata, CQL3Type.Raw> createOrUpdateRawType(final KeyspaceMetadata ksm, final String cfName, final String name, final Mapper mapper,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) {
        KeyspaceMetadata ksm2 = ksm;
        CQL3Type.Raw type;
        if (mapper instanceof ObjectMapper) {
            ColumnIdentifier ksCi = new  ColumnIdentifier(ksm.name, true);
            ObjectMapper objectMapper = (ObjectMapper) mapper;
            Map<String, CQL3Type.Raw> fields = new HashMap<>();
            for (Iterator<Mapper> it = objectMapper.iterator(); it.hasNext(); ) {
                Mapper m = it.next();
                if (m instanceof ObjectMapper && ((ObjectMapper) m).isEnabled() && !m.hasField()) {
                    continue;   // ignore object with no sub-field #146
                }

                // Use only the last part of the fullname to build UDT.
                int lastDotIndex = m.name().lastIndexOf('.');
                String fieldName = (lastDotIndex > 0) ? m.name().substring(lastDotIndex+1) :  m.name();
                Pair<KeyspaceMetadata, CQL3Type.Raw> x = createOrUpdateRawType(ksm2, cfName, fieldName, m, mutations, events);
                ksm2 = x.left;
                fields.put(fieldName, x.right);
            }
            String typeName = (objectMapper.cqlUdtName() == null) ? cfName + "_" + objectMapper.fullPath().replaceAll("(\\.|\\-)", "_") : objectMapper.cqlUdtName();
            Pair<KeyspaceMetadata, UserType> x = createOrUpdateUserType(ksm2, typeName, fields, mutations, events);
            ksm2 = x.left;
            UTName ut = new UTName(ksCi, new  ColumnIdentifier(typeName, true));
            type = CQL3Type.Raw.userType(ut);
            type.freeze();
        } else if (mapper instanceof GeoPointFieldMapper) {
            Pair<KeyspaceMetadata, CQL3Type.Raw> x = createRawTypeIfNotExists(ksm2, GEO_POINT_TYPE, GEO_POINT_FIELDS, mutations, events);
            ksm2 = x.left;
            type = x.right;
        } else if (mapper instanceof RangeFieldMapper) {
            Pair<KeyspaceMetadata, CQL3Type.Raw> x = createRawTypeIfNotExists(ksm2,
                    ((RangeFieldMapper) mapper).fieldType().typeName(),
                    ((RangeFieldMapper) mapper).cqlFieldTypes(),
                    mutations, events);
            ksm2 = x.left;
            type = x.right;
        } else if (mapper instanceof FieldMapper) {
            type = ((FieldMapper)mapper).rawType();
        } else {
            throw new ConfigurationException("Unkown mapper class="+mapper.getClass().getName());
        }
        return Pair.create(ksm2, mapper.collection(type));
    }

    public Pair<KeyspaceMetadata, CQL3Type.Raw> buildObject(final KeyspaceMetadata ksm, final String cfName, final String name, final ObjectMapper objectMapper,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) throws RequestExecutionException {
        switch(objectMapper.cqlStruct()) {
        case UDT:
            if (!objectMapper.iterator().hasNext())
                throw new ConfigurationException("Cannot build UDT for empty object ["+name+"]");
            return createOrUpdateRawType(ksm, cfName, name, objectMapper, mutations, events);
        case MAP:
        case OPAQUE_MAP:
            if (objectMapper.iterator().hasNext()) {
                Mapper childMapper = objectMapper.iterator().next();
                if (childMapper instanceof FieldMapper) {
                    //return "map<text,"+childMapper.cqlType()+">";
                    return Pair.create(ksm, CQL3Type.Raw.map(CQL3Type.Raw.from(CQL3Type.Native.TEXT), ((FieldMapper)childMapper).rawType()));
                } else if (childMapper instanceof ObjectMapper) {
                    //String subType = buildCql(ksName,cfName,childMapper.simpleName(),(ObjectMapper)childMapper, updatedUserTypes, validateOnly);
                    //return (subType==null) ? null : "map<text,frozen<"+subType+">>";
                    Pair<KeyspaceMetadata, CQL3Type.Raw> x = buildObject(ksm, cfName, childMapper.simpleName(), (ObjectMapper)childMapper, mutations, events);
                    return (x.right == null) ? null : Pair.create(x.left, CQL3Type.Raw.map(CQL3Type.Raw.from(CQL3Type.Native.TEXT), CQL3Type.Raw.frozen(x.right)));
                }
            }
            // default map prototype, no mapper to determine the value type.
            return Pair.create(ksm, CQL3Type.Raw.map(CQL3Type.Raw.from(CQL3Type.Native.TEXT), CQL3Type.Raw.from(CQL3Type.Native.TEXT)));
        default:
            throw new ConfigurationException("Object ["+name+"] not supported");
        }
    }

    public void dropIndexKeyspace(final String ksName,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) throws ConfigurationException {
        ThriftValidation.validateKeyspaceNotSystem(ksName);
        KeyspaceMetadata oldKsm = Schema.instance.getKSMetaData(ksName);
        if (oldKsm == null)
            throw new ConfigurationException(String.format("Cannot drop non existing keyspace '%s'.", ksName));

        logger.info("Drop Keyspace '{}'", oldKsm.name);
        mutations.add(SchemaKeyspace.makeDropKeyspaceMutation(oldKsm, FBUtilities.timestampMicros()).build());
        events.add(new Event.SchemaChange(Event.SchemaChange.Change.DROPPED, ksName));
    }

    /**
     * Update table extensions when index settings change. This allow to get index settings+mappings in the CQL backup of the table.
     * @param indexMetaData
     * @param mutations
     * @param events
     */
    public void updateTableExtensions(final IndexMetaData indexMetaData, final Collection<Mutation> mutations, Collection<Event.SchemaChange> events) {
        for(ObjectCursor<MappingMetaData> mappingMd : indexMetaData.getMappings().values()) {
            final String cfName = typeToCfName(indexMetaData.keyspace(), mappingMd.value.type());
            final CFMetaData cfm = Schema.instance.getCFMetaData(indexMetaData.keyspace(), cfName);
            if (cfm == null) {
                logger.debug("table {}.{} does not exist in CQL schema, ignoring", indexMetaData.keyspace(), cfName);
            } else {
                logger.debug("table {}.{} set extensions for index {}", indexMetaData.keyspace(), cfName, indexMetaData.getIndex().getName());
                Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(indexMetaData.keyspace(), FBUtilities.timestampMicros());
                addTableExtensionsToMutationBuilder(cfm, indexMetaData, builder);
                mutations.add(builder.build());
            }
        }
    }

    public void updateTableSchema(final MapperService mapperService, final IndexMetaData indexMetaData, final MappingMetaData mappingMd, final Collection<Mutation> mutations, Collection<Event.SchemaChange> events) throws IOException {
        KeyspaceMetadata ksm = createOrUpdateKeyspace(mapperService.keyspace(), settings().getAsInt(SETTING_NUMBER_OF_REPLICAS, 0) +1, mapperService.getIndexSettings().getIndexMetaData().replication(), mutations, events);
        updateTableSchema(ksm, mapperService, indexMetaData, mappingMd, mutations, events);
    }

    public KeyspaceMetadata updateTableSchema(KeyspaceMetadata ksm, final MapperService mapperService, final IndexMetaData indexMetaData, final MappingMetaData mappingMd,
            final Collection<Mutation> mutations, Collection<Event.SchemaChange> events) {
        String query = null;
        try {
            Set<ColumnIdentifier> addedColumns = new HashSet<>();
            String ksName = mapperService.keyspace();
            String cfName = typeToCfName(ksName, mappingMd.type());
            ColumnIdentifier ksNameColumnIdentifier = new ColumnIdentifier(ksName, true);

            final CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cfName);
            boolean newTable = (cfm == null);

            DocumentMapper docMapper = mapperService.documentMapper(mappingMd.type());
            Map<String, Object> mappingMap = mappingMd.sourceAsMap();

            Set<String> columns = new HashSet();
            if (mapperService.getIndexSettings().getIndexMetaData().isOpaqueStorage()) {
                columns.add(SourceFieldMapper.NAME);
            } else {
                if (docMapper.sourceMapper().enabled())
                    columns.add(SourceFieldMapper.NAME);
                if (mappingMap.get("properties") != null)
                    columns.addAll(((Map<String, Object>) mappingMap.get("properties")).keySet());
            }

            logger.debug("Updating CQL3 schema {}.{} columns={}", ksName, cfName, columns);
            List<ColumnDescriptor> columnsList = new ArrayList<>();
            for (String column : columns) {
                if (isReservedKeyword(column))
                    logger.warn("Allowing a CQL reserved keyword in ES: {}", column);

                if (column.equals(TokenFieldMapper.NAME))
                    continue; // ignore pseudo column known by Elasticsearch

                ColumnDescriptor colDesc = new ColumnDescriptor(column);
                FieldMapper fieldMapper = docMapper.mappers().smartNameFieldMapper(column);
                if (fieldMapper != null) {
                    if (fieldMapper.cqlCollection().equals(CqlCollection.NONE))
                        continue; // ignore field.

                    if (fieldMapper instanceof RangeFieldMapper) {
                        RangeFieldMapper rangeFieldMapper = (RangeFieldMapper) fieldMapper;
                        ColumnDefinition cdef = (newTable) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                        if (cdef != null) {
                            // index range stored as a range UDT in cassandra.
                            if (!(cdef.type instanceof UserType))
                                throw new MapperParsingException("Column ["+column+"] is not a Cassandra User Defined Type to store an Elasticsearch range");
                            colDesc.type = CQL3Type.Raw.from(CQL3Type.UserDefined.create((UserType)cdef.type));
                        } else {
                            // create a range UDT to store range fields
                            Pair<KeyspaceMetadata, CQL3Type.Raw> x = createRawTypeIfNotExists(ksm,
                                    rangeFieldMapper.fieldType().typeName(),
                                    rangeFieldMapper.cqlFieldTypes(),
                                    mutations, events);
                            ksm = x.left;
                            colDesc.type = x.right;
                        }
                    } else if (fieldMapper instanceof GeoPointFieldMapper) {
                        ColumnDefinition cdef = (newTable) ? null : cfm.getColumnDefinition(new ColumnIdentifier(column, true));
                        if (cdef != null && cdef.type instanceof UTF8Type) {
                            // index geohash stored as text in cassandra.
                            colDesc.type = CQL3Type.Raw.from(CQL3Type.Native.TEXT);
                        } else {
                            // create a geo_point UDT to store lat,lon
                            Pair<KeyspaceMetadata, CQL3Type.Raw> x = createRawTypeIfNotExists(ksm, GEO_POINT_TYPE, GEO_POINT_FIELDS, mutations, events);
                            ksm = x.left;
                            colDesc.type = x.right;
                        }
                    } else if (fieldMapper instanceof GeoShapeFieldMapper) {
                        colDesc.type = CQL3Type.Raw.from(CQL3Type.Native.TEXT);
                    } else if (fieldMapper instanceof CompletionFieldMapper) {
                        Pair<KeyspaceMetadata, CQL3Type.Raw> x = createRawTypeIfNotExists(ksm, COMPLETION_TYPE, COMPLETION_FIELDS, mutations, events);
                        ksm = x.left;
                        colDesc.type = x.right;
                    } else if (fieldMapper.getClass().getName().equals("org.elasticsearch.mapper.attachments.AttachmentMapper")) {
                        // attachement is a plugin, so class may not found.
                        Pair<KeyspaceMetadata, CQL3Type.Raw> x = createRawTypeIfNotExists(ksm, ATTACHMENT_TYPE, ATTACHMENT_FIELDS, mutations, events);
                        ksm = x.left;
                        colDesc.type = x.right;
                    } else if (fieldMapper instanceof SourceFieldMapper) {
                        colDesc.type = CQL3Type.Raw.from(CQL3Type.Native.BLOB);
                    } else {
                        colDesc.type = fieldMapper.rawType();
                        if (colDesc.type == null) {
                            logger.warn("Ignoring field [{}] type [{}]", column, fieldMapper.name());
                            continue;
                        }
                    }

                    if (fieldMapper.cqlPrimaryKeyOrder() >= 0) {
                        colDesc.position = fieldMapper.cqlPrimaryKeyOrder();
                        if (fieldMapper.cqlPartitionKey()) {
                            colDesc.kind = Kind.PARTITION_KEY;
                        } else {
                            colDesc.kind = Kind.CLUSTERING;
                            colDesc.desc = fieldMapper.cqlClusteringKeyDesc();
                        }
                    }
                    if (fieldMapper.cqlStaticColumn())
                        colDesc.kind = Kind.STATIC;
                    colDesc.type = fieldMapper.collection(colDesc.type);
                    columnsList.add(colDesc);
                } else {
                    ObjectMapper objectMapper = docMapper.objectMappers().get(column);
                    if (objectMapper == null) {
                       logger.warn("Cannot infer CQL type from object mapping for field [{}], ignoring", column);
                       continue;
                    }
                    if (objectMapper.cqlCollection().equals(CqlCollection.NONE))
                        continue; // ignore field

                    if (!objectMapper.isEnabled()) {
                        logger.debug("Object [{}] not enabled stored as text", column);
                        colDesc.type = CQL3Type.Raw.from(CQL3Type.Native.TEXT);
                    } else if (objectMapper.cqlStruct().equals(CqlStruct.MAP) || (objectMapper.cqlStruct().equals(CqlStruct.OPAQUE_MAP))) {
                        // TODO: check columnName exists and is map<text,?>
                        Pair<KeyspaceMetadata, CQL3Type.Raw> x = buildObject(ksm, cfName, column, objectMapper, mutations, events);
                        colDesc.type = x.right;
                        ksm = x.left;
                        //if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON)) {
                        //    colDesc.type = objectMapper.cqlCollectionTag()+"<"+colDesc.type+">";
                        //}
                        //logger.debug("Expecting column [{}] to be a map<text,?>", column);
                    } else  if (objectMapper.cqlStruct().equals(CqlStruct.UDT)) {
                        if (!objectMapper.hasField()) {
                            logger.debug("Ignoring [{}] has no sub-fields", column); // no sub-field, ignore it #146
                            continue;
                        }
                        Pair<KeyspaceMetadata, CQL3Type.Raw> x = buildObject(ksm, cfName, column, objectMapper, mutations, events);
                        ksm = x.left;
                        colDesc.type = x.right;
                        /*
                        if (!objectMapper.cqlCollection().equals(CqlCollection.SINGLETON) && !(cfName.equals(PERCOLATOR_TABLE) && column.equals("query"))) {
                            colDesc.type = objectMapper.collection(colDesc.type);
                        }
                        */
                    }
                    if (objectMapper.cqlPrimaryKeyOrder() >= 0) {
                        colDesc.position = fieldMapper.cqlPrimaryKeyOrder();
                        if (objectMapper.cqlPartitionKey()) {
                            colDesc.kind = Kind.PARTITION_KEY;
                        } else {
                            colDesc.kind = Kind.CLUSTERING;
                            colDesc.desc = fieldMapper.cqlClusteringKeyDesc();
                        }
                    }
                    if (objectMapper.cqlStaticColumn())
                        colDesc.kind = Kind.STATIC;
                    columnsList.add(colDesc);
                }
            }
            logger.debug("columnsList={}", columnsList);

            // add _parent column if necessary. Parent and child documents should have the same partition key.
            if (docMapper.parentFieldMapper().active() && docMapper.parentFieldMapper().pkColumns() == null)
                columnsList.add(new ColumnDescriptor("_parent", CQL3Type.Raw.from(CQL3Type.Native.TEXT)));

            // table extension contains IndexMetaData for a simple type associated to the table, but the provided IndexMetaData may contains multiple types.
            IndexMetaData.Builder singleTypeIndexMetaDataBuilder = new IndexMetaData.Builder(indexMetaData);
            singleTypeIndexMetaDataBuilder.removeAllMapping();
            singleTypeIndexMetaDataBuilder.putMapping(mappingMd);

            if (newTable) {
                boolean hasPartitionKey = false;
                for(ColumnDescriptor cd : columnsList) {
                    if (cd.kind == Kind.PARTITION_KEY) {
                        hasPartitionKey = true;
                        break;
                    }
                }
                if (!hasPartitionKey)
                    columnsList.add(new ColumnDescriptor(ELASTIC_ID_COLUMN_NAME, CQL3Type.Raw.from(CQL3Type.Native.TEXT), Kind.PARTITION_KEY, 0));
                Collections.sort(columnsList); // sort primary key columns
                ksm = createTable(ksm, cfName, mapperService.index().getName(), columnsList, mapperService.tableOptions(), singleTypeIndexMetaDataBuilder.build(), mutations, events);
            } else {
                // check column properties matches existing ones, or add it to columnsDefinitions
                for(ColumnDescriptor cd : columnsList)
                    cd.validate(ksm, cfm);
                TableAttributes tableAttrs = new TableAttributes();
                ksm = updateTable(ksm, cfName, mapperService.index().getName(), columnsList, tableAttrs, singleTypeIndexMetaDataBuilder.build(), mutations, events);
            }

            ksm = createSecondaryIndexIfNotExists(ksm, mappingMd,
                    mapperService.getIndexSettings().getSettings().get(ClusterService.SETTING_CLUSTER_SECONDARY_INDEX_CLASS,
                            clusterService.state().metaData().settings().get(ClusterService.SETTING_CLUSTER_SECONDARY_INDEX_CLASS,
                                    ClusterService.defaultSecondaryIndexClass.getName())),
                    mutations, events);
            return ksm;
        } catch (AssertionError | RequestValidationException e) {
            throw new MapperParsingException("Failed to execute query:" + query + " : "+e.getMessage(), e);
        } catch (Throwable e) {
            throw new MapperParsingException(e.getMessage(), e);
        }
    }

    // see https://docs.datastax.com/en/cql/3.0/cql/cql_reference/keywords_r.html
    public static final Pattern keywordsPattern = Pattern.compile("(ADD|ALLOW|ALTER|AND|ANY|APPLY|ASC|AUTHORIZE|BATCH|BEGIN|BY|COLUMNFAMILY|CREATE|DELETE|DESC|DROP|EACH_QUORUM|GRANT|IN|INDEX|INET|INSERT|INTO|KEYSPACE|KEYSPACES|LIMIT|LOCAL_ONE|LOCAL_QUORUM|MODIFY|NOT|NORECURSIVE|OF|ON|ONE|ORDER|PASSWORD|PRIMARY|QUORUM|RENAME|REVOKE|SCHEMA|SELECT|SET|TABLE|TO|TOKEN|THREE|TRUNCATE|TWO|UNLOGGED|UPDATE|USE|USING|WHERE|WITH)");

    public static boolean isReservedKeyword(String identifier) {
        return keywordsPattern.matcher(identifier.toUpperCase(Locale.ROOT)).matches();
    }

    public KeyspaceMetadata createSecondaryIndexIfNotExists(final KeyspaceMetadata ksm,
            final MappingMetaData mapping,
            final String className,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) {
    	KeyspaceMetadata ksm2 = ksm;
        String tableName = SchemaManager.typeToCfName(ksm.name, mapping.type());
        CFMetaData cfm0 = ksm.getTableOrViewNullable(tableName);
        assert cfm0 != null : "Table "+tableName+" not found in keyspace metadata";

        IndexPropDefs indexPropDefs = new IndexPropDefs();
        indexPropDefs.isCustom = true;
        indexPropDefs.customClass = className;
        IndexName indexName = new IndexName();
        indexName.setKeyspace(ksm.name, true);
        indexName.setIndex(buildIndexName(tableName), true);

        if (!cfm0.getIndexes().get(indexName.getIdx()).isPresent()) {
            logger.debug("Create secondary index on table {}.{}", ksm.name, tableName);
            CFName cfName = new CFName();
            cfName.setKeyspace(ksm.name, true);
            cfName.setColumnFamily(tableName, true);
            CreateIndexStatement cis = new CreateIndexStatement(cfName, indexName, Collections.EMPTY_LIST, indexPropDefs, true);
            cis.validate(ksm);
            CFMetaData cfm = cis.createIndex(ksm, cfm0);
            ksm2 = ksm.withSwapped(ksm.tables.without(cfm0.cfName).with(cfm));
            IndexMetadata indexMetadata = cfm.getIndexes().get(indexName.getIdx()).get();

            Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
            SchemaKeyspace.addUpdatedIndexToSchemaMutation(cfm, indexMetadata, builder);
            mutations.add(builder.build());

            events.add(new Event.SchemaChange(Event.SchemaChange.Change.UPDATED, Event.SchemaChange.Target.TABLE, ksm.name, tableName));
        }
        return ksm2;
    }

    public KeyspaceMetadata dropSecondaryIndex(final KeyspaceMetadata ksm,
            final CFMetaData cfm0,
            final Collection<Mutation> mutations,
            final Collection<Event.SchemaChange> events) throws RequestExecutionException  {
        KeyspaceMetadata ksm2 = ksm;
        for(IndexMetadata idx : cfm0.getIndexes()) {
            if (idx.isCustom() && idx.name.startsWith("elastic_")) {
                String className = idx.options.get(IndexTarget.CUSTOM_INDEX_OPTION_NAME);
                if (className != null && className.endsWith("ElasticSecondaryIndex")) {
                    IndexName indexName = new IndexName();
                    indexName.setKeyspace(ksm.name, true);
                    indexName.setIndex(idx.name, true);

                    DropIndexStatement dis = new DropIndexStatement(indexName, true);
                    CFMetaData cfm = dis.dropIndex(cfm0);
                    cfm.params  = TableParams.builder(cfm.params).extensions(Collections.EMPTY_MAP).build();
                    ksm2 = ksm.withSwapped(ksm.tables.without(cfm.cfName).with(cfm));

                    logger.info("Drop secondary index '{}'", indexName);
                    Mutation.SimpleBuilder builder = SchemaKeyspace.makeCreateKeyspaceMutation(ksm.name, FBUtilities.timestampMicros());
                    SchemaKeyspace.dropIndexFromSchemaMutation(cfm, idx, builder);
                    SchemaKeyspace.addTableExtensionsToSchemaMutation(cfm, Collections.EMPTY_MAP, builder); // drop table extensions

                    mutations.add(builder.build());
                    events.add(new Event.SchemaChange(Event.SchemaChange.Change.UPDATED, Event.SchemaChange.Target.TABLE, ksm.name, cfm.cfName));
                    break;
                }
            }
        }
        return ksm2;
    }

    public void dropSecondaryIndices(final IndexMetaData indexMetaData, final Collection<Mutation> mutations, final Collection<Event.SchemaChange> events)
            throws RequestExecutionException {
        String ksName = indexMetaData.keyspace();
        KeyspaceMetadata ksm = Schema.instance.getKSMetaData(ksName);
        for(CFMetaData cfm : ksm.tablesAndViews()) {
            if (cfm.isCQLTable())
                dropSecondaryIndex(ksm, cfm, mutations, events);
        }
    }

    public void dropSecondaryIndex(KeyspaceMetadata ksm, String cfName, final Collection<Mutation> mutations, final Collection<Event.SchemaChange> events)
            throws RequestExecutionException {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksm.name, cfName);
        if (cfm != null)
            dropSecondaryIndex(ksm, cfm, mutations, events);
    }

    public KeyspaceMetadata dropTables(KeyspaceMetadata ksm, final IndexMetaData indexMetaData, final Collection<Mutation> mutations, final Collection<Event.SchemaChange> events)
            throws RequestExecutionException {
        String ksName = indexMetaData.keyspace();
        for(ObjectCursor<String> cursor : indexMetaData.getMappings().keys()) {
            CFMetaData cfm = Schema.instance.getCFMetaData(ksName, cursor.value);
            ksm = dropTable(ksm, cfm, mutations, events);
        }
        return ksm;
    }

    public KeyspaceMetadata dropTable(final KeyspaceMetadata ksm, final String table, final Collection<Mutation> mutations, final Collection<Event.SchemaChange> events)
            throws RequestExecutionException  {
        CFMetaData cfm = Schema.instance.getCFMetaData(ksm.name, table);
        return (cfm != null) ? dropTable(ksm, cfm, mutations, events) : ksm;
    }

    public KeyspaceMetadata dropTable(final KeyspaceMetadata ksm, final CFMetaData cfm, final Collection<Mutation> mutations, final Collection<Event.SchemaChange> events)
            throws RequestExecutionException  {
        KeyspaceMetadata ksm2 = ksm;
        if (cfm != null) {
            CFName cfName = new CFName();
            cfName.setKeyspace(ksm.name, true);
            cfName.setColumnFamily(cfm.cfName, true);

            DropTableStatement dts = new DropTableStatement(cfName, true);
            dts.dropTable(ksm);
            ksm2 = ksm.withSwapped(ksm.tables.without(cfm.cfName));

            logger.info("Drop table '{}'", cfName);
            mutations.add(SchemaKeyspace.makeDropTableMutation(ksm, cfm, FBUtilities.timestampMicros()).build());
            events.add(new Event.SchemaChange(Event.SchemaChange.Change.DROPPED, Event.SchemaChange.Target.TABLE, ksm.name, cfm.cfName));
        }
        return ksm2;
    }
}
