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

package org.elassandra.action.admin.indices.rebuild;

import java.io.IOException;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

/**
 * Re-index on all nodes from the underlying Cassandra tables.
 *
 * @see org.elasticsearch.client.Requests#rebuildRequest(String...)
 * @see org.elasticsearch.client.IndicesAdminClient#rebuild(RebuildRequest, ActionListener)
 * @see RebuildResponse
 */
public class RebuildRequest extends BroadcastRequest<RebuildRequest> {

    int numThreads = 1;
    
    public RebuildRequest() {
    }

    /**
     * Copy constructor that creates a new refresh request that is a copy of the one provided as an argument.
     * The new request will inherit though headers and context from the original request that caused it.
     */
    public RebuildRequest(ActionRequest originalRequest) {
        super(originalRequest);
    }

    public RebuildRequest(String... indices) {
        super(indices);
    }
    
    public int numThreads() {
        return numThreads;
    }

    public void numThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeInt(numThreads);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        numThreads = in.readInt();
    }

    @Override
    public String toString() {
        return "RebuildRequest{" +
                "numThreads=" + numThreads + "}";
    }
}
