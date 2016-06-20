package org.elasticsearch.index.shard;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.index.aliases.IndexAliasesService;
import org.elasticsearch.index.cache.IndexCache;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.mapper.MapperException;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.query.IndexQueryParserService;
import org.elasticsearch.index.translog.Translog;

/**
 * Dummy TranslogRecoveryPerformer
 * @author vroyer
 *
 */
public class TranslogRecoveryPerformer {
    
    protected TranslogRecoveryPerformer(ShardId shardId, MapperService mapperService, IndexQueryParserService queryParserService,
            IndexAliasesService indexAliasesService, IndexCache indexCache, ESLogger logger) {
    }
    
    /**
     * Returns the recovered types modifying the mapping during the recovery.
     * Useless with cassandra because mapping is persistent.
     */
    public Map<String, Mapping> getRecoveredTypes() {
        return Collections.<String,Mapping>emptyMap();
    }
    
    /**
     * Applies all operations in the iterable to the current engine and returns the number of operations applied.
     * This operation will stop applying operations once an operation failed to apply.
     *
     * Throws a {@link MapperException} to be thrown if a mapping update is encountered.
     */
    int performBatchRecovery(Engine engine, Iterable<Translog.Operation> operations) {
        return 0;
    }
    
    public void performRecoveryOperation(Engine engine, Translog.Operation operation, boolean allowMappingUpdates) {
    }
    
    protected void operationProcessed() {
    }
    
    public static class BatchOperationException extends ElasticsearchException {

        private final int completedOperations;

        public BatchOperationException(ShardId shardId, String msg, int completedOperations, Throwable cause) {
            super(msg, cause);
            setShard(shardId);
            this.completedOperations = completedOperations;
        }

        public BatchOperationException(StreamInput in) throws IOException{
            super(in);
            completedOperations = in.readInt();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeInt(completedOperations);
        }

        /** the number of succesful operations performed before the exception was thrown */
        public int completedOperations() {
            return completedOperations;
        }
    }
}
