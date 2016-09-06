package com.strapdata.elastime;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchProcessorFactory;

public class ElastimeModule extends AbstractModule {
	
	private final Settings settings;

    public ElastimeModule(final Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(SearchProcessorFactory.class).to(ElastimeSearchProcessorFactory.class).asEagerSingleton();
    }

}
