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

package org.elassandra.action.admin.indices.cleanup;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;

/**
 * Cleanup on all nodes from the underlying Cassandra tables.
 *
 * @see org.elasticsearch.client.Requests#cleanupRequest(String...)
 * @see org.elasticsearch.client.IndicesAdminClient#cleanup(CleanupRequest, ActionListener)
 * @see CleanupResponse
 */
public class CleanupRequest extends BroadcastRequest<CleanupRequest> {

    int jobs = 0;
    
    public CleanupRequest() {
    }

    /**
     * Copy constructor that creates a new refresh request that is a copy of the one provided as an argument.
     * The new request will inherit though headers and context from the original request that caused it.
     */
    public CleanupRequest(ActionRequest originalRequest) {
        super(originalRequest);
    }

    public CleanupRequest(String... indices) {
        super(indices);
    }
    
    public int jobs() {
        return jobs;
    }

    public void jobs(int jobs) {
        this.jobs = jobs;
    }

    
}
