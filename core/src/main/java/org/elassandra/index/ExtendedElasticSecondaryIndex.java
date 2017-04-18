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
package org.elassandra.index;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.Operator;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.PartitionColumns;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.db.filter.RowFilter;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.partitions.PartitionIterator;
import org.apache.cassandra.db.partitions.PartitionUpdate;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.index.IndexRegistry;
import org.apache.cassandra.index.transactions.IndexTransaction.Type;
import org.apache.cassandra.schema.IndexMetadata;
import org.apache.cassandra.utils.concurrent.OpOrder.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade custom secondary index using reflection to instantiate the real secondary index.
 * Because this class appears in the CQL schema, it should be deployed on all nodes of your Cassandra cluster even if you don't need Elasticsearch features.
 * On nodes where Elasticsearch is not deployed, it just do nothing.
 * @author vroyer
 */
public class ExtendedElasticSecondaryIndex implements Index {
    private static final Logger logger = LoggerFactory.getLogger(SystemKeyspace.class);
    
    final Index elasticSecondaryIndex;
    final IndexMetadata indexDef;
    
    public ExtendedElasticSecondaryIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        this.indexDef = indexDef;
        this.elasticSecondaryIndex = newIndex(baseCfs, indexDef);
    }
    
    private Index newIndex(ColumnFamilyStore baseCfs, IndexMetadata indexDef) {
        try {
            Class indexClass = Class.forName("org.elassandra.index.ElasticSecondaryIndex");
            Method method = indexClass.getMethod("newElasticSecondaryIndex",ColumnFamilyStore.class, IndexMetadata.class);
            return (Index) method.invoke(null, baseCfs, indexDef);
        } catch (ClassNotFoundException e) {
            logger.warn("Class org.elassandra.index.ElasticSecondaryIndex not found, using a dummy secondary index.");
        } catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Failed to instanciate org.elassandra.index.ElasticSecondaryIndex, using a dummy secondary index.", e);
        }
        return new DummySecondaryIndex();
    }
    
    /**
     * 
     * An optional static method may be provided to validate custom index options (two variants are supported):
     *
     * <pre>{@code public static Map<String, String> validateOptions(Map<String, String> options);</pre>
     *
     * The input is the map of index options supplied in the WITH clause of a CREATE INDEX statement.
     *
     * <pre>{@code public static Map<String, String> validateOptions(Map<String, String> options, CFMetaData cfm);}</pre>
     *
     * In this version, the base table's metadata is also supplied as an argument.
     * If both overloaded methods are provided, only the one including the base table's metadata will be invoked.
     *
     * The validation method should return a map containing any of the supplied options which are not valid for the
     * implementation. If the returned map is not empty, validation is considered failed and an error is raised.
     * Alternatively, the implementation may choose to throw an org.apache.cassandra.exceptions.ConfigurationException
     * if invalid options are encountered.
     * @param options
     * @param cfm
     * @return
     */
    public static Map<String, String> validateOptions(Map<String, String> options, CFMetaData cfm) {
        return Collections.EMPTY_MAP;
    }
    
    @Override
    public Callable<?> getInitializationTask() {
        return elasticSecondaryIndex.getInitializationTask();
    }

    @Override
    public IndexMetadata getIndexMetadata() {
        return this.indexDef;
    }

    @Override
    public Callable<?> getMetadataReloadTask(IndexMetadata indexMetadata) {
        return elasticSecondaryIndex.getMetadataReloadTask(indexMetadata);
    }

    @Override
    public void register(IndexRegistry registry) {
        registry.registerIndex(this);
    }

    @Override
    public Optional<ColumnFamilyStore> getBackingTable() {
        return Optional.empty();
    }

    @Override
    public Callable<?> getBlockingFlushTask() {
        return elasticSecondaryIndex.getBlockingFlushTask();
    }

    @Override
    public Callable<?> getInvalidateTask() {
        return elasticSecondaryIndex.getInvalidateTask();
    }

    @Override
    public Callable<?> getTruncateTask(long truncatedAt) {
        return elasticSecondaryIndex.getTruncateTask(truncatedAt);
    }

    @Override
    public Callable<?> getSnapshotWithoutFlushTask(String snapshotName) {
        return this.elasticSecondaryIndex.getSnapshotWithoutFlushTask(snapshotName);
    }
    
    @Override
    public boolean shouldBuildBlocking() {
        return elasticSecondaryIndex.shouldBuildBlocking();
    }

    @Override
    public boolean dependsOn(ColumnDefinition column) {
        return elasticSecondaryIndex.dependsOn(column);
    }

    @Override
    public boolean supportsExpression(ColumnDefinition column, Operator operator) {
        return false;
    }

    @Override
    public AbstractType<?> customExpressionValueType() {
       return null;
    }

    @Override
    public RowFilter getPostIndexQueryFilter(RowFilter filter) {
        return null;
    }

    @Override
    public long getEstimatedResultRows() {
        return 0;
    }

    @Override
    public void validate(PartitionUpdate update) throws InvalidRequestException {
        elasticSecondaryIndex.validate(update);
    }

    @Override
    public Indexer indexerFor(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup, Type transactionType) {
        return elasticSecondaryIndex.indexerFor(key, columns, nowInSec, opGroup, transactionType);
    }

    
    @Override
    public Searcher searcherFor(ReadCommand command) {
        return null;
    }

    @Override
    public BiFunction<PartitionIterator, ReadCommand, PartitionIterator> postProcessorFor(ReadCommand command) {
        return null;
    }
    
    @Override
    public int hashCode() {
        return this.elasticSecondaryIndex.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof ExtendedElasticSecondaryIndex) {
            return this.elasticSecondaryIndex == ((ExtendedElasticSecondaryIndex)o).elasticSecondaryIndex;
        }
        return false;
    }

    static class DummySecondaryIndex implements Index {

        @Override
        public Callable<?> getInitializationTask() {
            return null;
        }

        @Override
        public IndexMetadata getIndexMetadata() {
            return null;
        }

        @Override
        public Callable<?> getMetadataReloadTask(IndexMetadata indexMetadata) {
            return null;
        }

        @Override
        public void register(IndexRegistry registry) {
            
        }

        @Override
        public Optional<ColumnFamilyStore> getBackingTable() {
            return null;
        }

        @Override
        public Callable<?> getBlockingFlushTask() {
            return null;
        }

        @Override
        public Callable<?> getSnapshotWithoutFlushTask(String snapshotName) {
            return null;
        }

        @Override
        public Callable<?> getInvalidateTask() {
            return null;
        }

        @Override
        public Callable<?> getTruncateTask(long truncatedAt) {
            return null;
        }

        @Override
        public boolean shouldBuildBlocking() {
            return false;
        }

        @Override
        public boolean dependsOn(ColumnDefinition column) {
            return false;
        }

        @Override
        public boolean supportsExpression(ColumnDefinition column, Operator operator) {
            return false;
        }

        @Override
        public AbstractType<?> customExpressionValueType() {
            return null;
        }

        @Override
        public RowFilter getPostIndexQueryFilter(RowFilter filter) {
            return null;
        }

        @Override
        public long getEstimatedResultRows() {
            return 0;
        }

        @Override
        public void validate(PartitionUpdate update) throws InvalidRequestException {
            
        }

        @Override
        public Indexer indexerFor(DecoratedKey key, PartitionColumns columns, int nowInSec, Group opGroup,
                Type transactionType) {
            return null;
        }

        @Override
        public BiFunction<PartitionIterator, ReadCommand, PartitionIterator> postProcessorFor(ReadCommand command) {
            return null;
        }

        @Override
        public Searcher searcherFor(ReadCommand command) {
            return null;
        }
    }
}
