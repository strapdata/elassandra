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

package org.elasticsearch.index.get;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.lucene.index.Term;
import org.elassandra.cluster.SchemaManager;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.cluster.service.ClusterService.DocPrimaryKey;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.lucene.uid.VersionsAndSeqNoResolver.DocIdAndVersion;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;
import org.elasticsearch.common.util.set.Sets;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.fieldvisitor.CustomFieldsVisitor;
import org.elasticsearch.index.fieldvisitor.FieldsVisitor;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.shard.AbstractIndexShardComponent;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.ParentFieldSubFetchPhase;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_PRIMARY_TERM;
import static org.elasticsearch.index.seqno.SequenceNumbers.UNASSIGNED_SEQ_NO;

public final class ShardGetService extends AbstractIndexShardComponent {
    private final MapperService mapperService;
    public final MeanMetric existsMetric = new MeanMetric();
    public final MeanMetric missingMetric = new MeanMetric();
    public final CounterMetric currentMetric = new CounterMetric();
    private final IndexShard indexShard;

    private final IndexService indexService;
    private final ClusterService clusterService;

    public ShardGetService(IndexSettings indexSettings, IndexShard indexShard,
                           MapperService mapperService,
                           IndexService indexService, ClusterService clusterService) {
        super(indexShard.shardId(), indexSettings);
        this.mapperService = mapperService;
        this.indexShard = indexShard;
        this.clusterService = clusterService;
        this.indexService = indexService;
    }

    public GetStats stats() {
        return new GetStats(existsMetric.count(), TimeUnit.NANOSECONDS.toMillis(existsMetric.sum()),
            missingMetric.count(), TimeUnit.NANOSECONDS.toMillis(missingMetric.sum()), currentMetric.count());
    }

    public GetResult get(String type, String id, String[] gFields, boolean realtime, long version,
                            VersionType versionType, FetchSourceContext fetchSourceContext) {
        return
            get(type, id, gFields, realtime, version, versionType, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, fetchSourceContext, false);
    }

    private GetResult get(String type, String id, String[] gFields, boolean realtime, long version, VersionType versionType,
                          long ifSeqNo, long ifPrimaryTerm, FetchSourceContext fetchSourceContext, boolean readFromTranslog) {
        currentMetric.inc();
        try {
            long now = System.nanoTime();
            GetResult getResult =
                innerGet(type, id, gFields, realtime, version, versionType, ifSeqNo, ifPrimaryTerm, fetchSourceContext, readFromTranslog);

            if (getResult.isExists()) {
                existsMetric.inc(System.nanoTime() - now);
            } else {
                missingMetric.inc(System.nanoTime() - now);
            }
            return getResult;
        } finally {
            currentMetric.dec();
        }
    }

    public GetResult getForUpdate(String type, String id, long version, VersionType versionType, long ifSeqNo, long ifPrimaryTerm) {
        return get(type, id, new String[]{RoutingFieldMapper.NAME, ParentFieldMapper.NAME}, true,
            version, versionType, ifSeqNo, ifPrimaryTerm, FetchSourceContext.FETCH_SOURCE, true);
    }

    /**
     * Returns {@link GetResult} based on the specified {@link org.elasticsearch.index.engine.Engine.GetResult} argument.
     * This method basically loads specified fields for the associated document in the engineGetResult.
     * This method load the fields from the Lucene index and not from transaction log and therefore isn't realtime.
     * <p>
     * Note: Call <b>must</b> release engine searcher associated with engineGetResult!
     */
    public GetResult get(Engine.GetResult engineGetResult, String id, String type,
                            String[] fields, FetchSourceContext fetchSourceContext) {
        if (!engineGetResult.exists()) {
            return new GetResult(shardId.getIndexName(), type, id, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, -1, false, null, null);
        }

        currentMetric.inc();
        try {
            long now = System.nanoTime();
            fetchSourceContext = normalizeFetchSourceContent(fetchSourceContext, fields);
            GetResult getResult = innerGetLoadFromStoredFields(type, id, fields, fetchSourceContext, engineGetResult, mapperService);
            if (getResult.isExists()) {
                existsMetric.inc(System.nanoTime() - now);
            } else {
                missingMetric.inc(System.nanoTime() - now); // This shouldn't happen...
            }
            return getResult;
        } finally {
            currentMetric.dec();
        }
    }

    /**
     * decides what needs to be done based on the request input and always returns a valid non-null FetchSourceContext
     */
    private FetchSourceContext normalizeFetchSourceContent(@Nullable FetchSourceContext context, @Nullable String[] gFields) {
        if (context != null) {
            return context;
        }
        if (gFields == null) {
            return FetchSourceContext.FETCH_SOURCE;
        }
        for (String field : gFields) {
            if (SourceFieldMapper.NAME.equals(field)) {
                return FetchSourceContext.FETCH_SOURCE;
            }
        }
        return FetchSourceContext.DO_NOT_FETCH_SOURCE;
    }

    private GetResult innerGet(String type, String id, String[] gFields, boolean realtime, long version, VersionType versionType,
                               long ifSeqNo, long ifPrimaryTerm, FetchSourceContext fetchSourceContext, boolean readFromTranslog) {
        DocPrimaryKey docPk;
        try {
            docPk = clusterService.getQueryManager().parseElasticId(indexService, type, id);
            id = docPk.toString(); // rewrite the id parameter to its canonical form
        } catch(IOException e) {
            throw new ElasticsearchException("Cannot parse type [" + type + "] and id [" + id + "]", e);
        }

        final Collection<String> types;
        if (type == null || type.equals("_all")) {
            try {
                for (String typeX : mapperService.types() ) {
                    // search for the matching type (table)
                    if (clusterService.getQueryManager().rowExists(indexService, typeX, docPk)) {
                        type = typeX;
                        break;
                    }
                }
            } catch (RequestExecutionException | RequestValidationException | IOException e1) {
                throw new ElasticsearchException("Cannot fetch source type [" + type + "] and id [" + id + "]", e1);
            }
        }
        if (type == null || type.equals("_all")) {
            return new GetResult(shardId.getIndexName(), type, id, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, -1, false, null, null);
        }

        DocumentMapper docMapper = mapperService.documentMapper(type);
        if (docMapper == null) {
            //get.release();
            return new GetResult(shardId.getIndexName(), type, id, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, -1, false, null, null);
        }

        fetchSourceContext = normalizeFetchSourceContent(fetchSourceContext, gFields);
        Set<String> columns = new HashSet<String>();
        if ((gFields != null) && (!fetchSourceContext.fetchSource())) {
            for (String field : gFields) {
                int i = field.indexOf('.');
                String colName = (i > 0) ? field.substring(0, i ) : field;
                if (!columns.contains(colName))
                    columns.add(colName);
            }
        } else {
            columns.addAll( mapperService.documentMapper(type).getColumnDefinitions().keySet() );
        }

        if (docMapper.parentFieldMapper().active()) {
            columns.add(ParentFieldMapper.NAME);
        }
        if (docMapper.sourceMapper().enabled()) {
            columns.add(SourceFieldMapper.NAME);
        }

        Map<String, DocumentField> fields = null;

        // deal with source, but only if it's enabled (we always have it from the translog)
        Map<String, Object> sourceAsMap = null;
        BytesReference sourceToBeReturned = null;
        SourceFieldMapper sourceFieldMapper = docMapper.sourceMapper();

        // In elassandra, Engine does not store the source any more, but fetch it from cassandra.
        try {
            UntypedResultSet result = clusterService.getQueryManager().fetchRow(this.indexService, type, docPk, columns.toArray(new String[columns.size()]),
                    docMapper.getColumnDefinitions());
            if (result.isEmpty()) {
                return new GetResult(shardId.getIndexName(), type, id, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, -1, false, null, null);
            }
            sourceAsMap = clusterService.getQueryManager().rowAsMap(this.indexService, type, result.one());
            if (fetchSourceContext.fetchSource()) {
                sourceToBeReturned = clusterService.getQueryManager().source(this.indexService, docMapper, sourceAsMap, id);
            }
        } catch (RequestExecutionException | RequestValidationException | IOException e1) {
            throw new ElasticsearchException("Cannot fetch source type [" + type + "] and id [" + id + "]", e1);
        }

        if (gFields != null && gFields.length > 0) {
            fields = new HashMap<String, DocumentField>();
            clusterService.getQueryManager().flattenDocumentField(gFields, "", sourceAsMap, fields);
        }

        if (fetchSourceContext.fetchSource() && sourceFieldMapper.enabled()) {
            // Cater for source excludes/includes at the cost of performance
            // We must first apply the field mapper filtering to make sure we get correct results
            // in the case that the fetchSourceContext white lists something that's not included by the field mapper

            boolean sourceFieldFiltering = sourceFieldMapper.includes().length > 0 || sourceFieldMapper.excludes().length > 0;
            boolean sourceFetchFiltering = fetchSourceContext.includes().length > 0 || fetchSourceContext.excludes().length > 0;
            if (sourceFieldFiltering || sourceFetchFiltering) {
                // TODO: The source might parsed and available in the sourceLookup but that one uses unordered maps so different. Do we care?
                XContentType sourceContentType = XContentType.JSON;
                /*
                if (fetchSourceContext.transformSource()) {
                    sourceAsMap = docMapper.transformSourceAsMap(sourceAsMap);
                }
                */
                if (sourceFieldFiltering) {
                    sourceAsMap = XContentMapValues.filter(sourceAsMap, sourceFieldMapper.includes(), sourceFieldMapper.excludes());
                }
                if (sourceFetchFiltering) {
                    sourceAsMap = XContentMapValues.filter(sourceAsMap, fetchSourceContext.includes(), fetchSourceContext.excludes());
                }
                try {
                    sourceToBeReturned = BytesReference.bytes(XContentFactory.contentBuilder(sourceContentType).map(sourceAsMap));
                } catch (IOException e) {
                    throw new ElasticsearchException("Failed to get type [" + type + "] and id [" + id + "] with includes/excludes set", e);
                }
            }
        }

        return new GetResult(shardId.getIndexName(), type, id, UNASSIGNED_SEQ_NO, UNASSIGNED_PRIMARY_TERM, 1L, true, sourceToBeReturned, fields);
    }

    private GetResult innerGetLoadFromStoredFields(String type, String id, String[] gFields, FetchSourceContext fetchSourceContext, Engine.GetResult get, MapperService mapperService) {
        Map<String, DocumentField> fields = null;
        BytesReference source = null;
        DocIdAndVersion docIdAndVersion = get.docIdAndVersion();
        DocumentMapper docMapper = mapperService.documentMapper(type);
        FieldsVisitor fieldVisitor = buildFieldsVisitors(gFields, fetchSourceContext);
        if (fieldVisitor != null) {
            try {
                // fetch source from cassandra
                DocPrimaryKey docPk = clusterService.getQueryManager().parseElasticId(this.indexService, type, id);
                String cfName = SchemaManager.typeToCfName(this.indexService.keyspace(), type);
                Map<String, ColumnDefinition> columnDefs = mapperService.documentMapper(type).getColumnDefinitions();
                UntypedResultSet result = clusterService.getQueryManager().fetchRow(this.indexService,
                        cfName, docPk,
                        columnDefs.keySet().toArray(new String[columnDefs.size()]),
                        ConsistencyLevel.LOCAL_ONE,
                        columnDefs);
                Map<String, Object> sourceMap = clusterService.getQueryManager().rowAsMap(this.indexService, type, result.one());

                source = clusterService.getQueryManager().source(this.indexService, docMapper, sourceMap, fieldVisitor.uid().id());

                fieldVisitor.source( BytesReference.toBytes(source) );
            } catch (IOException | RequestExecutionException | RequestValidationException e) {
                throw new ElasticsearchException("Failed to get type [" + type + "] and id [" + id + "]", e);
            }

            if (!fieldVisitor.fields().isEmpty()) {
                fieldVisitor.postProcess(mapperService);
                fields = new HashMap<>(fieldVisitor.fields().size());
                for (Map.Entry<String, List<Object>> entry : fieldVisitor.fields().entrySet()) {
                    fields.put(entry.getKey(), new DocumentField(entry.getKey(), entry.getValue()));
                }
            }
        }

        if (docMapper.parentFieldMapper().active()) {
            String parentId = ParentFieldSubFetchPhase.getParentId(docMapper.parentFieldMapper(), docIdAndVersion.reader, docIdAndVersion.docId);
            if (fields == null) {
                fields = new HashMap<>(1);
            }
            fields.put(ParentFieldMapper.NAME, new DocumentField(ParentFieldMapper.NAME, Collections.singletonList(parentId)));
        }

        if (gFields != null && gFields.length > 0) {
            for (String field : gFields) {
                Mapper fieldMapper = docMapper.mappers().smartNameFieldMapper(field);
                if (fieldMapper == null) {
                    if (docMapper.objectMappers().get(field) != null) {
                        // Only fail if we know it is a object field, missing paths / fields shouldn't fail.
                        throw new IllegalArgumentException("field [" + field + "] isn't a leaf field");
                    }
                }
            }
        }

        if (!fetchSourceContext.fetchSource()) {
            source = null;
        } else if (fetchSourceContext.includes().length > 0 || fetchSourceContext.excludes().length > 0) {
            Map<String, Object> sourceAsMap;
            XContentType sourceContentType = null;
            // TODO: The source might parsed and available in the sourceLookup but that one uses unordered maps so different. Do we care?
            Tuple<XContentType, Map<String, Object>> typeMapTuple = XContentHelper.convertToMap(source, true);
            sourceContentType = typeMapTuple.v1();
            sourceAsMap = typeMapTuple.v2();
            sourceAsMap = XContentMapValues.filter(sourceAsMap, fetchSourceContext.includes(), fetchSourceContext.excludes());
            try {
                source = BytesReference.bytes(XContentFactory.contentBuilder(sourceContentType).map(sourceAsMap));
            } catch (IOException e) {
                throw new ElasticsearchException("Failed to get type [" + type + "] and id [" + id + "] with includes/excludes set", e);
            }
        }

        return new GetResult(shardId.getIndexName(), type, id, get.docIdAndVersion().seqNo, get.docIdAndVersion().primaryTerm,
            get.version(), get.exists(), source, fields);
    }

    private static FieldsVisitor buildFieldsVisitors(String[] fields, FetchSourceContext fetchSourceContext) {
        if (fields == null || fields.length == 0) {
            return fetchSourceContext.fetchSource() ? new FieldsVisitor(true) : null;
        }

        return new CustomFieldsVisitor(Sets.newHashSet(fields), fetchSourceContext.fetchSource());
    }
}
