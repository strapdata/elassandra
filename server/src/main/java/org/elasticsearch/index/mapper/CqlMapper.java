package org.elasticsearch.index.mapper;

import org.apache.cassandra.cql3.CQL3Type;

public interface CqlMapper {

    public static enum CqlCollection {
        LIST, SET, SINGLETON, NONE
    }

    public static enum CqlStruct {
        UDT, MAP, OPAQUE_MAP, TUPLE
    }

    public CqlCollection cqlCollection();

    public String cqlCollectionTag();

    public CqlStruct cqlStruct();

    public boolean cqlPartialUpdate();

    public boolean cqlPartitionKey();

    public boolean cqlStaticColumn();

    public int cqlPrimaryKeyOrder();

    public boolean cqlClusteringKeyDesc();

    public CQL3Type CQL3Type();

    public default CQL3Type.Raw collection(CQL3Type.Raw rawType) {
        switch(cqlCollection()) {
            case LIST:
                return CQL3Type.Raw.list( rawType );
            case SET:
                return CQL3Type.Raw.set( rawType );
            default:
                return rawType;
        }
    }
}
