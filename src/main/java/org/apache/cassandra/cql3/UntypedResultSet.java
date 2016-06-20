/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cassandra.cql3;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.cassandra.cql3.statements.SelectStatement;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BooleanType;
import org.apache.cassandra.db.marshal.ByteType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.FloatType;
import org.apache.cassandra.db.marshal.InetAddressType;
import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.ShortType;
import org.apache.cassandra.db.marshal.TimestampType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UUIDType;
import org.apache.cassandra.service.pager.QueryPager;

import com.google.common.collect.AbstractIterator;

/** a utility for doing internal cql-based queries */
public abstract class UntypedResultSet implements Iterable<UntypedResultSet.Row>
{
    public static UntypedResultSet create(ResultSet rs)
    {
        return new FromResultSet(rs);
    }

    public static UntypedResultSet create(List<Map<String, ByteBuffer>> results)
    {
        return new FromResultList(results);
    }

    public static UntypedResultSet create(SelectStatement select, QueryPager pager, int pageSize)
    {
        return new FromPager(select, pager, pageSize);
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public abstract int size();
    public abstract Row one();

    // No implemented by all subclasses, but we use it when we know it's there (for tests)
    public abstract List<ColumnSpecification> metadata();

    private static class FromResultSet extends UntypedResultSet
    {
        private final ResultSet cqlRows;

        private FromResultSet(ResultSet cqlRows)
        {
            this.cqlRows = cqlRows;
        }

        public int size()
        {
            return cqlRows.size();
        }

        public Row one()
        {
            if (cqlRows.size() != 1)
                throw new IllegalStateException("One row required, " + cqlRows.size() + " found");
            return new Row(cqlRows.metadata.requestNames(), cqlRows.rows.get(0));
        }

        public Iterator<Row> iterator()
        {
            return new AbstractIterator<Row>()
            {
                Iterator<List<ByteBuffer>> iter = cqlRows.rows.iterator();

                protected Row computeNext()
                {
                    if (!iter.hasNext())
                        return endOfData();
                    return new Row(cqlRows.metadata.requestNames(), iter.next());
                }
            };
        }

        public List<ColumnSpecification> metadata()
        {
            return cqlRows.metadata.requestNames();
        }
    }

    private static class FromResultList extends UntypedResultSet
    {
        private final List<Map<String, ByteBuffer>> cqlRows;

        private FromResultList(List<Map<String, ByteBuffer>> cqlRows)
        {
            this.cqlRows = cqlRows;
        }

        public int size()
        {
            return cqlRows.size();
        }

        public Row one()
        {
            if (cqlRows.size() != 1)
                throw new IllegalStateException("One row required, " + cqlRows.size() + " found");
            return new Row(cqlRows.get(0));
        }

        public Iterator<Row> iterator()
        {
            return new AbstractIterator<Row>()
            {
                Iterator<Map<String, ByteBuffer>> iter = cqlRows.iterator();

                protected Row computeNext()
                {
                    if (!iter.hasNext())
                        return endOfData();
                    return new Row(iter.next());
                }
            };
        }

        public List<ColumnSpecification> metadata()
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class FromPager extends UntypedResultSet
    {
        private final SelectStatement select;
        private final QueryPager pager;
        private final int pageSize;
        private final List<ColumnSpecification> metadata;

        private FromPager(SelectStatement select, QueryPager pager, int pageSize)
        {
            this.select = select;
            this.pager = pager;
            this.pageSize = pageSize;
            this.metadata = select.getResultMetadata().requestNames();
        }

        public int size()
        {
            throw new UnsupportedOperationException();
        }

        public Row one()
        {
            throw new UnsupportedOperationException();
        }

        public Iterator<Row> iterator()
        {
            return new AbstractIterator<Row>()
            {
                private Iterator<List<ByteBuffer>> currentPage;

                protected Row computeNext()
                {
                    while (currentPage == null || !currentPage.hasNext())
                    {
                        if (pager.isExhausted())
                            return endOfData();
                        currentPage = select.process(pager.fetchPage(pageSize)).rows.iterator();
                    }
                    return new Row(metadata, currentPage.next());
                }
            };
        }

        public List<ColumnSpecification> metadata()
        {
            return metadata;
        }
    }

    public static class Row
    {
        final ByteBuffer[] colValues;
        final String[]     colNames;
        final List<ColumnSpecification> colSpec;
        
        public Row(Map<String, ByteBuffer> data)
        {
            this.colNames = data.keySet().toArray(new String[data.size()]);
            this.colValues = new ByteBuffer[data.size()];
            int i=0;
            for(String key : colNames) {
                colValues[i++] = data.get(key);
            }
            this.colSpec = null;
        }

        public Row(List<ColumnSpecification> names, List<ByteBuffer> columns)
        {
            this.colSpec = names;
            this.colValues = columns.toArray(new ByteBuffer[columns.size()]);
            this.colNames = new String[names.size()];
            for (int i = 0; i < names.size(); i++)
                this.colNames[i] = names.get(i).name.toString();
        }

        public ByteBuffer get(String name) {
            for(int i=0; i < colNames.length; i++) {
                if (colNames[i].equals(name)) {
                    return colValues[i];
                }
            }
            return null;
        }
        
        public boolean has(String column)
        {
            // Note that containsKey won't work because we may have null values
            return get(column) != null;
        }
        
        public boolean has(int i)
        {
            // Note that containsKey won't work because we may have null values
            return colValues[i] != null;
        }

        public ByteBuffer getBlob(String column)
        {
            return get(column);
        }
        
        public ByteBuffer getBlob(int i)
        {
            return colValues[i];
        }

        public String getString(String column)
        {
            return UTF8Type.instance.compose(get(column));
        }

        public String getString(int i)
        {
            return UTF8Type.instance.compose(colValues[i]);
        }
        
        public boolean getBoolean(String column)
        {
            return BooleanType.instance.compose(get(column));
        }
        
        public boolean getBoolean(int i)
        {
            return BooleanType.instance.compose(colValues[i]);
        }

        public byte getByte(String column)
        {
            return ByteType.instance.compose(get(column));
        }
        
        public byte getByte(int i)
        {
            return ByteType.instance.compose(colValues[i]);
        }

        public short getShort(String column)
        {
            return ShortType.instance.compose(get(column));
        }
        
        public short getShort(int i)
        {
            return ShortType.instance.compose(colValues[i]);
        }

        public int getInt(String column)
        {
            return Int32Type.instance.compose(get(column));
        }

        public int getInt(int i)
        {
            return Int32Type.instance.compose(colValues[i]);
        }
        
        public double getDouble(String column)
        {
            return DoubleType.instance.compose(get(column));
        }
        
        public double getDouble(int i)
        {
            return DoubleType.instance.compose(colValues[i]);
        }

        public ByteBuffer getBytes(String column)
        {
            return get(column);
        }
        
        public ByteBuffer getBytes(int i)
        {
            return colValues[i];
        }

        public InetAddress getInetAddress(String column)
        {
            return InetAddressType.instance.compose(get(column));
        }
        
        public InetAddress getInetAddress(int i)
        {
            return InetAddressType.instance.compose(colValues[i]);
        }

        public UUID getUUID(String column)
        {
            return UUIDType.instance.compose(get(column));
        }

        public UUID getUUID(int i)
        {
            return UUIDType.instance.compose(colValues[i]);
        }
        
        public Date getTimestamp(String column)
        {
            return TimestampType.instance.compose(get(column));
        }
        
        public Date getTimestamp(int i)
        {
            return TimestampType.instance.compose(colValues[i]);
        }

        public long getLong(String column)
        {
            return LongType.instance.compose(get(column));
        }
        
        public long getLong(int i)
        {
            return LongType.instance.compose(colValues[i]);
        }
        
        // add support for float (2016/05/10 vroyer)
        public float getFloat(String column)
        {
            return FloatType.instance.compose(get(column));
        }
        
        public float getFloat(int i)
        {
            return FloatType.instance.compose(colValues[i]);
        }

        public <T> Set<T> getSet(String column, AbstractType<T> type)
        {
            ByteBuffer raw = get(column);
            return raw == null ? null : SetType.getInstance(type, true).compose(raw);
        }
        
        public <T> Set<T> getSet(int i, AbstractType<T> type)
        {
            return colValues[i] == null ? null : SetType.getInstance(type, true).compose(colValues[i]);
        }

        public <T> List<T> getList(String column, AbstractType<T> type)
        {
            ByteBuffer raw = get(column);
            return raw == null ? null : ListType.getInstance(type, true).compose(raw);
        }
        
        public <T> List<T> getList(int i, AbstractType<T> type)
        {
            return colValues[i] == null ? null : ListType.getInstance(type, true).compose(colValues[i]);
        }

        public <K, V> Map<K, V> getMap(String column, AbstractType<K> keyType, AbstractType<V> valueType)
        {
            ByteBuffer raw = get(column);
            return raw == null ? null : MapType.getInstance(keyType, valueType, true).compose(raw);
        }
        
        public <K, V> Map<K, V> getMap(int i, AbstractType<K> keyType, AbstractType<V> valueType)
        {
            return colValues[i] == null ? null : MapType.getInstance(keyType, valueType, true).compose(colValues[i]);
        }

        public List<ColumnSpecification> getColumns()
        {
            return colSpec;
        }

        @Override
        public String toString()
        {
            return toString();
        }
    }
    
}
