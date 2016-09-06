package com.strapdata.elastime;

import java.util.Collection;
import java.util.Collections;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;

public class ElastimePlugin extends Plugin {
	
	@Inject
    public ElastimePlugin() {
    }

    @Override
    public String name() {
        return "elastime";
    }

    @Override
    public String description() {
        return "Manage timeseries with elassandra";
    }
    
    /**
     * Per index modules.
     */
    public Collection<Module> indexModules(Settings indexSettings) {
        return Collections.singleton(new ElastimeModule(indexSettings));
    }
    

}
