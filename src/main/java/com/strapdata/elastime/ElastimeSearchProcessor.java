package com.strapdata.elastime;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.query.ParsedQuery;
import org.elasticsearch.search.SearchProcessor;
import org.elasticsearch.search.internal.SearchContext;

import com.carrotsearch.hppc.cursors.ObjectCursor;

public class ElastimeSearchProcessor implements SearchProcessor {
	private static final ESLogger logger = Loggers.getLogger(ElastimeSearchProcessor.class);
	
	private Query filterQuery = null;
	
	@Override
	public void preProcess(SearchContext context) {
		logger.debug("input query={}", context.query());

		IndexService indexService = context.indexShard().indexService();
		
        IndexMetaData indexMetaData = context.getClusterState().metaData().index(indexService.index().name());
        for (ObjectCursor<String> type : indexMetaData.getMappings().keys()) {
        		DocumentMapper docMapper = indexService.mapperService().documentMapper(type.value);
        		if (docMapper.meta().get("noindex") != null && context.query() instanceof BooleanQuery) {
        			BooleanQuery bq = (BooleanQuery) context.query();
                	final BooleanQuery.Builder builder = new BooleanQuery.Builder()
                			.setDisableCoord(bq.isCoordDisabled())
                			.setMinimumNumberShouldMatch(bq.getMinimumNumberShouldMatch());
                	for(BooleanClause bc : bq.getClauses()) {
                		switch(bc.getOccur()) {
                		case FILTER: 
                			filterQuery = bc.getQuery();
                			break;
                		default:
                			builder.add(bc);
                		}
                	}
                	if (filterQuery != null) {
                		context.parsedQuery(new ParsedQuery(builder.build(), context.parsedQuery()));
                		if (context.searchType() == SearchType.COUNT)
                			context.searchType(SearchType.QUERY_THEN_FETCH);
                		context.size(10);
                		logger.debug("output query={} searchType={}", context.query(), context.searchType());
                	} 
        		}
        }
        
	}

	@Override
	public void postProcess(SearchContext context) {
		logger.debug("postProcess query={}", context.query());
	}
}
