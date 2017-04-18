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

package org.elassandra.action.admin.indices.listsnapshots;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;

/**
 * Snapshot on all nodes from the underlying Cassandra tables.
 *
 * @see org.elasticsearch.client.Requests#cleanupRequest(String...)
 * @see org.elasticsearch.client.IndicesAdminClient#cleanup(ListSnapshotsRequest)
 * @see ClearSnapshotResponse
 */
public class ListSnapshotsRequest extends BroadcastRequest<ListSnapshotsRequest> {

    String tag = Long.toString(System.currentTimeMillis());
    
    public ListSnapshotsRequest() {
    }

    /**
     * Copy constructor that creates a new refresh request that is a copy of the one provided as an argument.
     * The new request will inherit though headers and context from the original request that caused it.
     */
    public ListSnapshotsRequest(ActionRequest originalRequest) {
        super(originalRequest);
    }

    public ListSnapshotsRequest(String... indices) {
        super(indices);
    }
    
    public String tag() {
        return tag;
    }

    public void tag(String tag) {
        this.tag = tag;
    }

}
