package org.elasticsearch.search.aggregations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.cql3.ColumnSpecification;
import org.apache.cassandra.cql3.selection.Selection;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.TimestampType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;

public class AggregationMetaDataBuilder {

    final String keyspace;
    final String table;
    final boolean toJson;
    
    List<ColumnSpecification> columns = new ArrayList<>();
    Map<String,Integer> name2idx = new HashMap<>();
    
    public AggregationMetaDataBuilder(String ks, String table, boolean toJson) {
        this.keyspace = ks;
        this.table = table;
        this.toJson = toJson;
    }
    
    int addColumn(String name, AbstractType<?> type) {
        Integer index = name2idx.get(name);
        if (index == null) {
            index = columns.size();
            columns.add(new ColumnSpecification(keyspace, table, new ColumnIdentifier(name,true), type));
            name2idx.put(name, index);
        }
        return index;
    }
    
    public boolean toJson() {
        return this.toJson;
    }
    
    public void setColumnType(int index, String name, AbstractType<?> type) {
        columns.set(index, new ColumnSpecification(keyspace, table, new ColumnIdentifier(name,true), type));
    }
    
    public List<ColumnSpecification> getColumns() {
        return this.columns;
    }
    
    public int getColumn(String name) {
        return (name2idx.containsKey(name)) ? name2idx.get(name) : -1;
    }
    
    public int size() {
        return columns.size();
    }
    
    public void build(String prefix, AggregatorFactories.Builder aggFactoriesBuilder, Selection selection) {
        
        for(AggregationBuilder agg : aggFactoriesBuilder.getAggregatorFactories()) {
            String type = agg.getType();
            String baseName = prefix+agg.getName()+".";
            if (this.toJson()) {
                addColumn(agg.getName(), UTF8Type.instance);
            } else {
                switch(type) {
                case TermsAggregationBuilder.NAME: 
                     // with term aggregation, term type could be string, long, double, 
                     // so use the provided projection column type.
                     if (selection.isWildcard()) {
                         addColumn(baseName+"key", UTF8Type.instance);
                     } else {
                         ColumnDefinition cd = selection.getColumns().get(columns.size());
                         addColumn(baseName+"key", cd.type);
                     }
                     addColumn(baseName+"count", LongType.instance);
                     break;
                case DateHistogramAggregationBuilder.NAME:
                    addColumn(baseName+"key", TimestampType.instance);
                    addColumn(baseName+"count", LongType.instance);
                    break;
                case HistogramAggregationBuilder.NAME:
                    addColumn(baseName+"key", DoubleType.instance);
                    addColumn(baseName+"count", LongType.instance);
                    break;
                case PercentilesAggregationBuilder.NAME:
                    addColumn(baseName+"value", DoubleType.instance);
                    addColumn(baseName+"percent", DoubleType.instance);
                    break;
                case SumAggregationBuilder.NAME:
                    addColumn(baseName+"sum", DoubleType.instance);
                    break;
                case AvgAggregationBuilder.NAME:
                    addColumn(baseName+"avg", DoubleType.instance);
                    break;
                case MinAggregationBuilder.NAME:
                    addColumn(baseName+"min", DoubleType.instance);
                    break;
                case MaxAggregationBuilder.NAME:
                    addColumn(baseName+"max", DoubleType.instance);
                    break;
                }
                build(baseName, agg.factoriesBuilder, selection);
            }
        }
        
        if (!this.toJson) {
            for(PipelineAggregationBuilder agg : aggFactoriesBuilder.getPipelineAggregatorFactories()) {
                addColumn(prefix+agg.getName(), DoubleType.instance);
            }
        }
    }
}
