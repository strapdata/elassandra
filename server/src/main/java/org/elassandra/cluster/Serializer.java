/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elassandra.cluster;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InetAddresses;

import org.apache.cassandra.cql3.UserTypes;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.CollectionType;
import org.apache.cassandra.db.marshal.ListType;
import org.apache.cassandra.db.marshal.MapType;
import org.apache.cassandra.db.marshal.SetType;
import org.apache.cassandra.db.marshal.SimpleDateType;
import org.apache.cassandra.db.marshal.TupleType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.db.marshal.UserType;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.serializers.CollectionSerializer;
import org.apache.cassandra.serializers.MapSerializer;
import org.apache.cassandra.serializers.MarshalException;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.CompletionFieldMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.mapper.RangeFieldMapper;
import org.elasticsearch.index.mapper.RangeFieldMapper.Range;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Serializer {

    private static org.codehaus.jackson.map.ObjectMapper jsonMapper = new org.codehaus.jackson.map.ObjectMapper();

    // wrap string values with quotes
    public static String stringify(Object o) throws IOException {
        Object v = toJsonValue(o);
        try {
            return v instanceof String ? jsonMapper.writeValueAsString(v) : v.toString();
        } catch (IOException e) {
            Loggers.getLogger(ClusterService.class).error("Unexpected json encoding error", e);
            throw new RuntimeException(e);
        }
    }

    public static String stringify(Object[] cols, int length) {
        if (cols.length == 1)
            return toJsonValue(cols[0]).toString();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i = 0; i < length; i++) {
            if (i > 0)
                sb.append(",");
            Object val = toJsonValue(cols[i]);
            if (val instanceof String) {
                try {
                    sb.append(jsonMapper.writeValueAsString(val));
                } catch (IOException e) {
                    Loggers.getLogger(ClusterService.class).error("Unexpected json encoding error", e);
                    throw new RuntimeException(e);
                }
            } else {
                sb.append(val);
            }
        }
        return sb.append("]").toString();
    }

    private static Object toJsonValue(Object o) {
        if (o instanceof UUID)
            return o.toString();
        if (o instanceof Date)
            return ((Date)o).getTime();
        if (o instanceof ByteBuffer) {
            // encode byte[] as Base64 encoded string
            ByteBuffer bb = ByteBufferUtil.clone((ByteBuffer)o);
            return Base64.getEncoder().encodeToString(ByteBufferUtil.getArray((ByteBuffer)o));
        }
        if (o instanceof InetAddress)
            return InetAddresses.toAddrString((InetAddress)o);
        return o;
    }

    public static Collection flattenCollection(Collection c) {
        List l = new ArrayList(c.size());
        for(Object o : c) {
            if (o instanceof Collection) {
                l.addAll( flattenCollection((Collection)o) );
            } else {
                l.add(o);
            }
        }
        return l;
    }

    /**
     * Serialize to a cassandra typed object.
     */
    public static ByteBuffer serialize(final String ksName, final String cfName, final AbstractType type, final String name, final Object value, final Mapper mapper)
            throws SyntaxException, ConfigurationException, JsonGenerationException, JsonMappingException, IOException {
        if (value == null) {
            return null;
        }
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            ByteBuffer[] components = new ByteBuffer[udt.size()];
            int i=0;

            if (mapper instanceof RangeFieldMapper) {
                // parse a range field to serialized C* UDT
                RangeFieldMapper rangeFieldMapper = (RangeFieldMapper)mapper;
                Range range = rangeFieldMapper.parse(value);
                components[i++]=serialize(ksName, cfName, udt.fieldType(0), RangeQueryBuilder.FROM_FIELD.getPreferredName(), rangeFieldMapper.fieldType().cqlValue(range.from), null);
                components[i++]=serialize(ksName, cfName, udt.fieldType(1), RangeQueryBuilder.TO_FIELD.getPreferredName(), rangeFieldMapper.fieldType().cqlValue(range.to), null);
                components[i++]=serialize(ksName, cfName, udt.fieldType(2), "include_lower", range.includeFrom, null);
                components[i++]=serialize(ksName, cfName, udt.fieldType(3), "include_upper", range.includeTo, null);
            } else if (SchemaManager.GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                GeoPoint geoPoint = new GeoPoint();
                if (value instanceof String) {
                    // parse from string lat,lon (ex: "41.12,-71.34") or geohash (ex:"drm3btev3e86")
                    geoPoint.resetFromString((String)value);
                } else {
                    // parse from lat, lon fields as map
                    Map<String, Object> mapValue = (Map<String, Object>) value;
                    geoPoint.reset(convertToDouble(mapValue.get(org.elasticsearch.common.geo.GeoUtils.LATITUDE)),
                                   convertToDouble(mapValue.get(org.elasticsearch.common.geo.GeoUtils.LONGITUDE)));
                }
                components[i++]=serialize(ksName, cfName, udt.fieldType(0), org.elasticsearch.common.geo.GeoUtils.LATITUDE, geoPoint.lat(), null);
                components[i++]=serialize(ksName, cfName, udt.fieldType(1), org.elasticsearch.common.geo.GeoUtils.LONGITUDE, geoPoint.lon(), null);
            } else if (SchemaManager.COMPLETION_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                // input list<text>, output text, weight int, payload text
                Map<String, Object> mapValue = (Map<String, Object>) value;
                components[i++]=(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_INPUT) == null) ? null : serialize(ksName, cfName, udt.fieldType(0), CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_INPUT, mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_INPUT), null);
                components[i++]=(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_CONTEXTS) == null) ? null : serialize(ksName, cfName, udt.fieldType(1), CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_CONTEXTS, jsonMapper.writeValueAsString(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_CONTEXTS)), null);
                components[i++]=(mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_WEIGHT) == null) ? null : serialize(ksName, cfName, udt.fieldType(2), CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_WEIGHT, mapValue.get(CompletionFieldMapper.Fields.CONTENT_FIELD_NAME_WEIGHT), null);
            } else {
                Map<String, Object> mapValue = (Map<String, Object>) value;
                for (int j = 0; j < udt.size(); j++) {
                    String subName = UTF8Type.instance.compose(udt.fieldName(j).bytes);
                    AbstractType<?> subType = udt.fieldType(j);
                    Object subValue = mapValue.get(subName);
                    Mapper subMapper = (mapper instanceof ObjectMapper) ? ((ObjectMapper) mapper).getMapper(subName) : null;
                    components[i++]=serialize(ksName, cfName, subType, subName, subValue, subMapper);
                }
            }
            return TupleType.buildValue(components);
        } else if (type instanceof MapType) {
            MapSerializer<?,?> serializer = (MapSerializer<?, ?>) type.getSerializer();
            List<ByteBuffer> buffers = serializer.serializeValues((Map)value);
            return CollectionSerializer.pack(buffers, ((Map)value).size(), ProtocolVersion.CURRENT);
        } else if (type instanceof CollectionType) {
            AbstractType elementType = (type instanceof ListType) ? ((ListType)type).getElementsType() : ((SetType)type).getElementsType();

            if (elementType instanceof UserType && SchemaManager.GEO_POINT_TYPE.equals(ByteBufferUtil.string(((UserType)elementType).name)) && value instanceof List && ((List)value).get(0) instanceof Double) {
                // geo_point as array of double lon,lat like [1.2, 1.3]
                UserType udt = (UserType)elementType;
                List<Double> values = (List<Double>)value;
                ByteBuffer[] elements = new ByteBuffer[] {
                    serialize(ksName, cfName, udt.fieldType(0), org.elasticsearch.common.geo.GeoUtils.LATITUDE, values.get(1), null),
                    serialize(ksName, cfName, udt.fieldType(1), org.elasticsearch.common.geo.GeoUtils.LONGITUDE, values.get(0), null)
                };
                ByteBuffer geo_point = TupleType.buildValue(elements);
                return CollectionSerializer.pack(ImmutableList.of(geo_point), 1, ProtocolVersion.CURRENT);
            }

            if (value instanceof Collection) {
                // list of elementType
                List<ByteBuffer> elements = new ArrayList<ByteBuffer>();
                for(Object v : flattenCollection((Collection) value)) {
                    ByteBuffer bb = serialize(ksName, cfName, elementType, name, v, mapper);
                    elements.add(bb);
                }
                return CollectionSerializer.pack(elements, elements.size(), ProtocolVersion.CURRENT);
            } else {
                // singleton list
                ByteBuffer bb = serialize(ksName, cfName, elementType, name, value, mapper);
                return CollectionSerializer.pack(ImmutableList.of(bb), 1, ProtocolVersion.CURRENT);
            }
        } else {
            // Native cassandra type, encoded with mapper if available.
            if (mapper != null) {
                if (mapper instanceof FieldMapper) {
                    return type.decompose( ((FieldMapper) mapper).fieldType().cqlValue(value, type) );
                } else if (mapper instanceof ObjectMapper && !((ObjectMapper)mapper).isEnabled()) {
                    // enabled=false => store field as json text
                    if (value instanceof Map) {
                        XContentBuilder builder = XContentFactory.jsonBuilder();
                        builder.map( (Map) value);
                        return type.decompose( builder.string() );
                    }
                    return type.decompose( value );
                }
            }
            return type.decompose( value );
        }
    }

    public static Double convertToDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Double)
            return (Double)value;
        if (value instanceof Integer)
            return ((Integer)value).doubleValue();
        if (value instanceof Long)
            return ((Long)value).doubleValue();
        return Double.parseDouble(value.toString());
    }
    
    public static Object deserialize(AbstractType<?> type, ByteBuffer bb) throws CharacterCodingException {
        return deserialize(type, bb, null);
    }

    public static Object deserialize(AbstractType<?> type, ByteBuffer bb, Mapper mapper) throws CharacterCodingException {
        if (type instanceof UserType) {
            UserType udt = (UserType) type;
            Map<String, Object> mapValue = new HashMap<String, Object>();
            UserTypes.Value udtValue = UserTypes.Value.fromSerialized(bb, udt);
            ByteBuffer[] components = udtValue.elements;

            if (SchemaManager.GEO_POINT_TYPE.equals(ByteBufferUtil.string(udt.name))) {
                if (components[0] != null)
                    mapValue.put(org.elasticsearch.common.geo.GeoUtils.LATITUDE, deserialize(udt.type(0), components[0], null));
                if (components[1] != null)
                    mapValue.put(org.elasticsearch.common.geo.GeoUtils.LONGITUDE, deserialize(udt.type(1), components[1], null));
            } else {
                for (int i = 0; i < components.length; i++) {
                    String fieldName = UTF8Type.instance.compose(udt.fieldName(i).bytes);
                    AbstractType<?> ctype = udt.type(i);
                    Mapper subMapper = null;
                    if (mapper != null && mapper instanceof ObjectMapper)
                        subMapper = ((ObjectMapper)mapper).getMapper(fieldName);
                    Object value = (components[i] == null) ? null : deserialize(ctype, components[i], subMapper);
                    mapValue.put(fieldName, value);
                }
            }
            return mapValue;
        } else if (type instanceof ListType) {
            ListType<?> ltype = (ListType<?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, ProtocolVersion.CURRENT);
            List list = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                list.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, ProtocolVersion.CURRENT), mapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return list;
        } else if (type instanceof SetType) {
            SetType<?> ltype = (SetType<?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, ProtocolVersion.CURRENT);
            Set set = new HashSet(size);
            for (int i = 0; i < size; i++) {
                set.add( deserialize(ltype.getElementsType(), CollectionSerializer.readValue(input, ProtocolVersion.CURRENT), mapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return set;
        } else if (type instanceof MapType) {
            MapType<?,?> ltype = (MapType<?,?>)type;
            ByteBuffer input = bb.duplicate();
            int size = CollectionSerializer.readCollectionSize(input, ProtocolVersion.CURRENT);
            Map map = new LinkedHashMap(size);
            for (int i = 0; i < size; i++) {
                ByteBuffer kbb = CollectionSerializer.readValue(input, ProtocolVersion.CURRENT);
                ByteBuffer vbb = CollectionSerializer.readValue(input, ProtocolVersion.CURRENT);
                String key = (String) ltype.getKeysType().compose(kbb);
                Mapper subMapper = null;
                if (mapper != null) {
                    assert mapper instanceof ObjectMapper : "Expecting an object mapper for MapType";
                    subMapper = ((ObjectMapper)mapper).getMapper(key);
                }
                map.put(key, deserialize(ltype.getValuesType(), vbb, subMapper));
            }
            if (input.hasRemaining())
                throw new MarshalException("Unexpected extraneous bytes after map value");
            return map;
        } else {
             Object value = type.compose(bb);
             return value;
        }
    }

    public static ByteBuffer fromString(AbstractType<?> atype, String v) throws IOException {
        if (atype instanceof BytesType)
            return ByteBuffer.wrap(Base64.getDecoder().decode(v));
        if (atype instanceof SimpleDateType)
            return SimpleDateType.instance.getSerializer().serialize(Integer.parseInt(v));
        return atype.fromString(v);
    }

    public static void toXContent(XContentBuilder builder, Mapper mapper, String field, Object value) throws IOException {
        if (value instanceof Collection) {
           if (field == null) {
               builder.startArray();
           } else {
               builder.startArray(field);
           }
           for(Iterator<Object> i = ((Collection)value).iterator(); i.hasNext(); ) {
               toXContent(builder, mapper, null, i.next());
           }
           builder.endArray();
        } else if (value instanceof Map) {
           Map<String, Object> map = (Map<String,Object>)value;
           if (field != null) {
               builder.startObject(field);
           } else {
               builder.startObject();
           }
           for(String subField : map.keySet()) {
               Object subValue = map.get(subField);
               if (subValue == null)
                   continue;

               if (subValue instanceof Collection && ((Collection)subValue).size() == 1)
                   subValue = ((Collection)subValue).iterator().next();

               if (mapper != null) {
                   if (mapper instanceof ObjectMapper) {
                       toXContent(builder, ((ObjectMapper)mapper).getMapper(subField), subField, subValue);
                   }
                   /*
                   else if (mapper instanceof GeoPointFieldMapper) {
                       BaseGeoPointFieldMapper geoMapper = (BaseGeoPointFieldMapper)mapper;
                       if (geoMapper.fieldType() instanceof LegacyGeoPointFieldType && ((LegacyGeoPointFieldType)geoMapper.fieldType()).isLatLonEnabled()) {
                           Iterator<Mapper> it = geoMapper.iterator();
                           switch(subField) {
                           case org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT:
                               toXContent(builder, it.next(), org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT, map.get(org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LAT));
                               break;
                           case org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON:
                               it.next();
                               toXContent(builder, it.next(), org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON, map.get(org.elasticsearch.index.mapper.BaseGeoPointFieldMapper.Names.LON));
                               break;
                           }
                       } else {
                           // No mapper known
                           // TODO: support geohashing
                           builder.field(subField, subValue);
                       }
                   }
                   */
                   else {
                       builder.field(subField, subValue);
                   }
               } else {
                   builder.field(subField, subValue);
               }
           }
           builder.endObject();
        } else {
           if (mapper instanceof FieldMapper) {
               FieldMapper fieldMapper = (FieldMapper)mapper;
               if (!(fieldMapper instanceof MetadataFieldMapper)) {
                   if (field != null) {
                       builder.field(field, fieldMapper.fieldType().valueForDisplay(value));
                   } else {
                       builder.value(value);
                   }
               }
           } else if (mapper instanceof ObjectMapper) {
               ObjectMapper objectMapper = (ObjectMapper)mapper;
               if (!objectMapper.isEnabled()) {
                   builder.field(field, value);
               } else {
                   throw new IOException("Unexpected object value ["+value+"]");
               }
           }
        }
    }
}
