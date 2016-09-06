package org.elasticsearch.search;

import org.elasticsearch.search.internal.SearchContext;

public interface SearchProcessor {
	public abstract void preProcess(SearchContext context);
	public abstract void postProcess(SearchContext context);
}
