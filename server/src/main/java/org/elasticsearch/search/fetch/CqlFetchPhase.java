package org.elasticsearch.search.fetch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.ResultSet;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.tracing.TraceState;
import org.apache.cassandra.tracing.TraceStateImpl;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.tracing.Tracing.TraceType;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.LeafReaderContext;
import org.elassandra.cluster.SchemaManager;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.fieldvisitor.FieldsVisitor;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.internal.SearchContext;

import com.google.common.net.InetAddresses;

public class CqlFetchPhase extends FetchPhase {

    public static final String PROJECTION = "_projection";


    public CqlFetchPhase(List<FetchSubPhase> fetchSubPhases, ClusterService clusterService) {
        super(fetchSubPhases, clusterService);
    }


    @Override
    protected SearchHit createSearchHit(SearchContext context, FieldsVisitor fieldsVisitor, int docId, int subDocId, Map<String, Set<String>> storedToRequestedFields, LeafReaderContext subReaderContext) {
        SearchHit searchHit = super.createSearchHit(context, fieldsVisitor, docId, subDocId, storedToRequestedFields, subReaderContext);
        if (fieldsVisitor.getValues() != null) {
            searchHit.setValues(fieldsVisitor.getValues());
            searchHit.version(Long.MIN_VALUE);   // flag indicating that searchHit has ByteBuffer value when reading from InputStream
        }
        return searchHit;
    }

    String projection(final SearchContext searchContext) {
        return (searchContext.request().extraParams() != null && searchContext.request().extraParams().containsKey(PROJECTION)) ?
                (String)searchContext.request().extraParams().get(PROJECTION) : null;
    }

    public String buildFetchQuery(final IndexService indexService, final String type, String cqlProjection, boolean forStaticDocument, boolean isJson)
            throws IndexNotFoundException, IOException
    {
        DocumentMapper docMapper = indexService.mapperService().documentMapper(type);
        String cfName = SchemaManager.typeToCfName(indexService.keyspace(), type);
        DocumentMapper.CqlFragments cqlFragment = docMapper.getCqlFragments();
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        if (isJson)
            query.append("JSON ");

        query.append(cqlProjection)
            .append(" FROM \"").append(indexService.keyspace()).append("\".\"").append(cfName)
            .append("\" WHERE ").append((forStaticDocument) ? cqlFragment.ptWhere : cqlFragment.pkWhere )
            .append(" LIMIT 1");
        return query.toString();
    }

    @Override
    protected ParsedStatement.Prepared getCqlPreparedStatement(final SearchContext searchContext, final IndexService indexService, FieldsVisitor fieldVisitor, String typeKey, boolean staticDocument) throws IOException {
        ParsedStatement.Prepared cqlStatement = searchContext.getCqlPreparedStatement( typeKey );
        if (cqlStatement == null) {
            final String projection = projection(searchContext);
            if (projection != null) {
                // fetch from CQL projection
                String query = buildFetchQuery(indexService, fieldVisitor.uid().type(), projection, false,
                        searchContext.request().extraParams().get("_json") != null);
                Logger logger = Loggers.getLogger(FetchPhase.class);
                if (logger.isTraceEnabled())
                    logger.trace("new statement={}", query);

                String session = (String)searchContext.request().extraParams().get("_cassandra.trace.session");
                if (session != null) {
                    String coordinator = (String)searchContext.request().extraParams().get("_cassandra.trace.coordinator");
                    if (coordinator != null) {
                        TraceState state = new TraceStateImpl( InetAddresses.forString(coordinator), UUID.fromString(session), TraceType.QUERY);
                        Tracing.instance.set(state);
                    }
                }
                cqlStatement = QueryProcessor.prepareInternal(query);
                searchContext.putCqlPreparedStatement(typeKey, cqlStatement);
            } else {
                cqlStatement = super.getCqlPreparedStatement(searchContext, indexService, fieldVisitor, typeKey, staticDocument);
            }
        }
        return cqlStatement;
    }

    @Override
    protected void processCqlResultSet(final SearchContext searchContext, final IndexService indexService, FieldsVisitor fieldVisitor, ResultSet resultSet) throws IOException {
        UntypedResultSet rs = UntypedResultSet.create(resultSet);
        if (!rs.isEmpty()) {
            final String projection = projection(searchContext);
            if (projection != null) {
                // binary response for cassandra coordinator.
                fieldVisitor.setValues(resultSet.firstRow());
                String coordinator = (String)searchContext.request().extraParams().get("_cassandra.trace.coordinator");
                if ( coordinator != null && !coordinator.equals(FBUtilities.getBroadcastAddress().getHostAddress())) {
                    Tracing.instance.set((TraceState) null);
                }
            } else {
                super.processCqlResultSet(searchContext, indexService, fieldVisitor, resultSet);
            }
        }
    }
}
