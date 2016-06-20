package org.elasticsearch.cassandra.index;

import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.component.LifecycleComponent;


public interface SecondaryIndicesService extends LifecycleComponent<SecondaryIndicesService>, ClusterStateListener {
    
    public void addDeleteListener(DeleteListener listener);
    public void removeDeleteListener(DeleteListener listener);
    
    public interface DeleteListener {
        public IndexMetaData mapping();
        public void onIndexDeleted();
    }
    
}
