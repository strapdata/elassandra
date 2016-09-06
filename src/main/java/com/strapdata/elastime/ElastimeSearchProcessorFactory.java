package com.strapdata.elastime;

import org.elasticsearch.search.SearchProcessorFactory;

public class ElastimeSearchProcessorFactory implements SearchProcessorFactory {
	
	@Override
	public ElastimeSearchProcessor create() {
		return new ElastimeSearchProcessor();
	}

}
