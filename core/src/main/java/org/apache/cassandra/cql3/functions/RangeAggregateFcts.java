package org.apache.cassandra.cql3.functions;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.IntegerType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.TimestampType;


public class RangeAggregateFcts {

    /**
     * groupbytime(time, timestamp_col_name, value_col_name)
     * ex: groupbytime(300, date, val).
     */
    public static final AggregateFunction rangeAggregationFunctionForTimestampToDouble =
            new NativeAggregateFunction("groupbytime", MapType.getInstance(TimestampType.instance, DoubleType.instance, true), IntegerType.instance, TimestampType.instance, DoubleType.instance )
            {
                public Aggregate newAggregate()
                {
                    return new Aggregate()
                    {
                        private Map<Date,Double> agg = new HashMap<Date,Double>();

                        private long slot = 0;
                        private double sum = 0;
                        private int count = 0;

                        public void reset()
                        {
                            agg = new HashMap<Date,Double>();
                            slot = 0;
                            sum = 0;
                            count = 0;
                        }

                        public ByteBuffer compute(int protocolVersion)
                        {
                            if (count > 0) {
                                agg.put(new Date(slot), sum/count);
                            }

                            return ((MapType<Date,Double>) returnType()).decompose(agg);
                        }

                        public void addInput(int protocolVersion, List<ByteBuffer> values)
                        {
                            ByteBuffer precision = values.get(0);
                            ByteBuffer time = values.get(1);
                            ByteBuffer value = values.get(2);

                            if ((value == null) || (time == null) || (precision == null))
                                return;

                            long p = ((BigInteger) argTypes().get(0).compose(precision)).longValue();
                            Date t = (Date) argTypes().get(1).compose(time);
                            Double v = (Double) argTypes().get(2).compose(value);
                            
                            long s = Math.floorDiv( t.getTime() , p);
                            if (s == slot) {
                                sum += v.doubleValue();
                                count += 1;
                            } else {
                                // flush the current slot 
                                agg.put(new Date(slot), sum/count);
                                slot = s;
                                sum = v.doubleValue();
                                count = 1;
                            }
                        }
                    };
                }
            };
}
