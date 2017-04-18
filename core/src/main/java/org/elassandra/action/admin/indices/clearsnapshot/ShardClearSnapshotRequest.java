package org.elassandra.action.admin.indices.clearsnapshot;

import java.io.IOException;

import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.shard.ShardId;

public class ShardClearSnapshotRequest extends ReplicationRequest<ShardClearSnapshotRequest> {

    private ClearSnapshotRequest request = new ClearSnapshotRequest();

    public ShardClearSnapshotRequest(ClearSnapshotRequest request, ShardId shardId) {
        super(request, shardId);
        this.request = request;
    }

    public ShardClearSnapshotRequest() {
    }

    ClearSnapshotRequest getRequest() {
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
        return "clearsnapshot {" + super.toString() + "}";
    }
}

