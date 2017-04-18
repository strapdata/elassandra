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

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class ListSnapshotsAction extends Action<ListSnapshotsRequest, ListSnapshotsResponse, ListSnapshotsRequestBuilder> {

    public static final ListSnapshotsAction INSTANCE = new ListSnapshotsAction();
    public static final String NAME = "indices:admin/listsnapshots";

    private ListSnapshotsAction() {
        super(NAME);
    }

    @Override
    public ListSnapshotsResponse newResponse() {
        return new ListSnapshotsResponse();
    }

    @Override
    public ListSnapshotsRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ListSnapshotsRequestBuilder(client, this);
    }
}
