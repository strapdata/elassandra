package org.elassandra.rest.action.admin.indices.reload;

import org.elassandra.action.admin.indices.reload.ReloadRequest;
import org.elassandra.action.admin.indices.reload.ReloadResponse;
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

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.RestActions.buildBroadcastShardsHeader;

public class RestReloadAction extends BaseRestHandler {

    @Inject
    public RestReloadAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/_reload", this);
        controller.registerHandler(POST, "/{index}/_reload", this);

        controller.registerHandler(GET, "/_reload", this);
        controller.registerHandler(GET, "/{index}/_reload", this);
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) {
        ReloadRequest reloadRequest = new ReloadRequest(Strings.splitStringByCommaToArray(request.param("index")));
        reloadRequest.indicesOptions(IndicesOptions.fromRequest(request, reloadRequest.indicesOptions()));
        return channel -> client.admin().indices().reload(reloadRequest, new RestBuilderListener<ReloadResponse>(channel) {
            @Override
            public RestResponse buildResponse(ReloadResponse response, XContentBuilder builder) throws Exception {
                builder.startObject();
                buildBroadcastShardsHeader(builder, request, response);
                builder.endObject();
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}

