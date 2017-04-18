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

package org.elassandra.action.admin.indices.restore;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class RestoreAction extends Action<RestoreRequest, RestoreResponse, RestoreRequestBuilder> {

    public static final RestoreAction INSTANCE = new RestoreAction();
    public static final String NAME = "indices:admin/restore";

    private RestoreAction() {
        super(NAME);
    }

    @Override
    public RestoreResponse newResponse() {
        return new RestoreResponse();
    }

    @Override
    public RestoreRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new RestoreRequestBuilder(client, this);
    }
}
