package org.elassandra.rest.action.admin.indices.rebuild;

import org.elassandra.action.admin.indices.rebuild.RebuildRequest;
import org.elassandra.action.admin.indices.rebuild.RebuildResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.RestBuilderListener;

import java.io.IOException;

import static org.elasticsearch.client.Requests.rebuildRequest;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.RestActions.buildBroadcastShardsHeader;

public class RestRebuildAction extends BaseRestHandler {

    @Inject
    public RestRebuildAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/_rebuild", this);
        controller.registerHandler(POST, "/{index}/_rebuild", this);

        controller.registerHandler(GET, "/_rebuild", this);
        controller.registerHandler(GET, "/{index}/_rebuild", this);
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        RebuildRequest rebuildRequest = rebuildRequest( Strings.splitStringByCommaToArray(request.param("index")));
        rebuildRequest.indicesOptions(IndicesOptions.fromRequest(request, rebuildRequest.indicesOptions()));
        return channel -> client.admin().indices().rebuild(rebuildRequest,new RestBuilderListener<RebuildResponse>(channel) {
            @Override
            public RestResponse buildResponse(RebuildResponse response, XContentBuilder builder) throws Exception {
                builder.startObject();
                buildBroadcastShardsHeader(builder, request, response);
                builder.endObject();
                return new BytesRestResponse(OK, builder);
            }
        });
    }

}

