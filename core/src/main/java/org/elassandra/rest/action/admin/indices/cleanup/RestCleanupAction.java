package org.elassandra.rest.action.admin.indices.cleanup;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.support.RestActions.buildBroadcastShardsHeader;

import org.elassandra.action.admin.indices.cleanup.CleanupRequest;
import org.elassandra.action.admin.indices.cleanup.CleanupResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.support.RestBuilderListener;

public class RestCleanupAction extends BaseRestHandler {

    @Inject
    public RestCleanupAction(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);
        controller.registerHandler(POST, "/_cleanup", this);
        controller.registerHandler(POST, "/{index}/_cleanup", this);

        controller.registerHandler(GET, "/_cleanup", this);
        controller.registerHandler(GET, "/{index}/_cleanup", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        CleanupRequest cleanupRequest = new CleanupRequest(Strings.splitStringByCommaToArray(request.param("index")));
        cleanupRequest.indicesOptions(IndicesOptions.fromRequest(request, cleanupRequest.indicesOptions()));
        client.admin().indices().cleanup(cleanupRequest, new RestBuilderListener<CleanupResponse>(channel) {
            @Override
            public RestResponse buildResponse(CleanupResponse response, XContentBuilder builder) throws Exception {
                builder.startObject();
                buildBroadcastShardsHeader(builder, request, response);
                builder.endObject();
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}

