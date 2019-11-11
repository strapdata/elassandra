package org.elassandra.index;

import java.nio.ByteBuffer;
import java.util.Map;

public class ElasticIncomingPayload {
    public final long hitTotal;
    public final float hitMaxScore;
    public final int shardTotal;
    public final int shardSuccessful;
    public final int shardSkipped;
    public final int shardFailed;

    public ElasticIncomingPayload(Map<String, ByteBuffer> payload) {
        hitTotal = payload.containsKey("hits.total") ? payload.get("hits.total").getLong() : -1;
        hitMaxScore = payload.containsKey("hits.max_score") ? payload.get("hits.max_score").getFloat() : -1;
        shardTotal = payload.containsKey("_shards.total") ? payload.get("_shards.total").getInt() : -1;
        shardSuccessful = payload.containsKey("_shards.successful") ? payload.get("_shards.successful").getInt() : -1;
        shardSkipped = payload.containsKey("_shards.skipped") ? payload.get("_shards.skipped").getInt() : -1;
        shardFailed = payload.containsKey("_shards.failed") ? payload.get("_shards.failed").getInt() : -1;
    }

    @Override
    public String toString() {
        return "{ hitTotal="+hitTotal+
            ", hitMaxScore="+hitMaxScore+
            ", shardTotal="+shardTotal+
            ", shardSuccessful="+shardSuccessful+
            ", shardSkipped="+shardSkipped+
            ", shardFailed="+shardFailed+" }";
    }
}
