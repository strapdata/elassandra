/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.indices.rebuild;

import java.util.Collection;

import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;

/**
 * A request to optimize one or more indices. In order to optimize on all the indices, pass an empty array or
 * <tt>null</tt> for the indices.
 * <p/>
 * <p>{@link #setMaxNumSegments(int)} allows to control the number of segments to optimize down to. By default, will
 * cause the optimize process to optimize down to half the configured number of segments.
 */
public class RebuildRequestBuilder extends BroadcastOperationRequestBuilder<RebuildRequest, RebuildResponse, RebuildRequestBuilder, IndicesAdminClient> {

    public RebuildRequestBuilder(IndicesAdminClient indicesClient) {
        super(indicesClient, new RebuildRequest());
    }

    /**
     * Should flush be performed after the optimization. Defaults to <tt>true</tt>.
     */
    public RebuildRequestBuilder setFlush(boolean flush) {
        request.flush(flush);
        return this;
    }

    /**
     * Should flush be performed after the optimization. Defaults to <tt>true</tt>.
     */
    public RebuildRequestBuilder tokenRanges(Collection<Range<Token>> tokenRanges) {
        request.tokenRanges(tokenRanges);
        return this;
    }  

    @Override
    protected void doExecute(ActionListener<RebuildResponse> listener) {
        client.rebuild(request, listener);
    }
}
