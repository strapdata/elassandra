package org.elassandra.action.admin.indices.listsnapshots;

import java.io.IOException;

import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;

public class ShardListSnapshotsRequest extends ReplicationRequest<ShardListSnapshotsRequest> {

    private ListSnapshotsRequest request = new ListSnapshotsRequest();

    public ShardListSnapshotsRequest(ListSnapshotsRequest request, ShardId shardId) {
        super(request, shardId);
        this.request = request;
    }

    public ShardListSnapshotsRequest() {
    }

    ListSnapshotsRequest getRequest() {
        return request;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        request.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        request.writeTo(out);
    }

    @Override
    public String toString() {
        return "listsnapshots {" + super.toString() + "}";
    }
}

