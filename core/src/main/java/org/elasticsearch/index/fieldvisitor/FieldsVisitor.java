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
package org.elasticsearch.index.fieldvisitor;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.util.BytesRef;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.RoutingFieldMapper;
import org.elasticsearch.index.mapper.internal.SourceFieldMapper;
import org.elasticsearch.index.mapper.internal.TTLFieldMapper;
import org.elasticsearch.index.mapper.internal.TimestampFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper;
import org.elasticsearch.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Base {@link StoredFieldsVisitor} that retrieves all non-redundant metadata.
 */
public class FieldsVisitor extends StoredFieldVisitor {

    private static final Set<String> BASE_REQUIRED_FIELDS = ImmutableSet.of(
            UidFieldMapper.NAME,        // row primary key
            TimestampFieldMapper.NAME,  // row witetime
            TTLFieldMapper.NAME,        // row TTL
            RoutingFieldMapper.NAME,    // a JSON string representation of the partition key
            ParentFieldMapper.NAME      // part of the primary key
   );

    private final boolean loadSource;
    private final Set<String> requiredFields;
    private Collection<String> filtredFields = null;
    protected BytesReference source;
    protected Uid uid;
    protected Map<String, List<Object>> fieldsValues;
    
    protected NavigableSet<String> requiredColumns = null;
             
    public FieldsVisitor(boolean loadSource) {
        this.loadSource = loadSource;
        requiredFields = new HashSet<>();
        reset();
    }

    @Override
    public Status needsField(FieldInfo fieldInfo) throws IOException {
        if (requiredFields.remove(fieldInfo.name)) {
            return Status.YES;
        }
        // All these fields are single-valued so we can stop when the set is
        // empty
        return requiredFields.isEmpty()
                ? Status.STOP
                : Status.NO;
    }

    public Set<String> requestedFields() {
        return ImmutableSet.of();
    }
    
    // cache the cassandra required columns and return the static+partition columns
    public NavigableSet<String> requiredColumns(ClusterService clusterService, SearchContext searchContext) throws JsonParseException, JsonMappingException, IOException {
        if (this.requiredColumns == null) {
            List<String> requiredColumns =  new ArrayList<String>();
            if (requestedFields() != null) {
                for(String fieldExp : requestedFields()) {
                    for(String field : searchContext.mapperService().simpleMatchToIndexNames(fieldExp)) {
                        int i = field.indexOf('.');
                        String columnName = (i > 0) ? field.substring(0, i) : field;
                        // TODO: eliminate non-existant columns or (non-static or non-partition-key) for static docs.
                        if (this.filtredFields == null || this.filtredFields.contains(columnName))
                            requiredColumns.add(columnName);
                    }
                }
            }
            if (loadSource()) {
                for(String columnName : searchContext.mapperService().documentMapper(uid.type()).getColumnDefinitions().keySet()) 
                    if (this.filtredFields == null || this.filtredFields.contains(columnName))
                        requiredColumns.add( columnName );
            }
            this.requiredColumns = new TreeSet<String>(requiredColumns);
        }
        return this.requiredColumns;
    }
    
    public void filtredColumns(Collection<String> fields) {
        this.filtredFields = fields;
    }
    
    public boolean loadSource() {
        return this.loadSource;
    }

    public void postProcess(MapperService mapperService) {
        if (uid != null) {
            DocumentMapper documentMapper = mapperService.documentMapper(uid.type());
            if (documentMapper != null) {
                // we can derive the exact type for the mapping
                postProcess(documentMapper);
                return;
            }
        }
        // can't derive exact mapping type
        for (Map.Entry<String, List<Object>> entry : fields().entrySet()) {
            MappedFieldType fieldType = mapperService.indexName(entry.getKey());
            if (fieldType == null) {
                continue;
            }
            List<Object> fieldValues = entry.getValue();
            for (int i = 0; i < fieldValues.size(); i++) {
                fieldValues.set(i, fieldType.valueForSearch(fieldValues.get(i)));
            }
        }
    }

    public void postProcess(DocumentMapper documentMapper) {
        for (Map.Entry<String, List<Object>> entry : fields().entrySet()) {
            String indexName = entry.getKey();
            FieldMapper fieldMapper = documentMapper.mappers().getMapper(indexName);
            if (fieldMapper == null) {
                // it's possible index name doesn't match field name (legacy feature)
                for (FieldMapper mapper : documentMapper.mappers()) {
                    if (mapper.fieldType().names().indexName().equals(indexName)) {
                        fieldMapper = mapper;
                        break;
                    }
                }
                if (fieldMapper == null) {
                    // no index name or full name found, so skip
                    continue;
                }
            }
            List<Object> fieldValues = entry.getValue();
            for (int i = 0; i < fieldValues.size(); i++) {
                fieldValues.set(i, fieldMapper.fieldType().valueForSearch(fieldValues.get(i)));
            }
        }
    }

    @Override
    public void binaryField(FieldInfo fieldInfo, byte[] value) throws IOException {
        if (SourceFieldMapper.NAME.equals(fieldInfo.name)) {
            source = new BytesArray(value);
        } else {
            addValue(fieldInfo.name, new BytesRef(value));
        }
    }

    @Override
    public void stringField(FieldInfo fieldInfo, byte[] bytes) throws IOException {
        final String value = new String(bytes, StandardCharsets.UTF_8);
        if (UidFieldMapper.NAME.equals(fieldInfo.name)) {
            uid = Uid.createUid(value);
        } else {
            addValue(fieldInfo.name, value);
        }
    }

    @Override
    public void intField(FieldInfo fieldInfo, int value) throws IOException {
        addValue(fieldInfo.name, value);
    }

    @Override
    public void longField(FieldInfo fieldInfo, long value) throws IOException {
        addValue(fieldInfo.name, value);
    }

    @Override
    public void floatField(FieldInfo fieldInfo, float value) throws IOException {
        addValue(fieldInfo.name, value);
    }

    @Override
    public void doubleField(FieldInfo fieldInfo, double value) throws IOException {
        addValue(fieldInfo.name, value);
    }

    public BytesReference source() {
        return source;
    }
    
    public FieldsVisitor source(byte[] _source) {
        source = new BytesArray(_source);
        return this;
    }
    
    public FieldsVisitor source(BytesReference _source) {
        source = _source;
        return this;
    }

    public Uid uid() {
        return uid;
    }

    public void uid(Uid uid) {
        this.uid = uid;
    }
    
    public String routing() {
        if (fieldsValues == null) {
            return null;
        }
        List<Object> values = fieldsValues.get(RoutingFieldMapper.NAME);
        if (values == null || values.isEmpty()) {
            return null;
        }
        assert values.size() == 1;
        return values.get(0).toString();
    }

    public Map<String, List<Object>> fields() {
        return fieldsValues != null
                ? fieldsValues
                : ImmutableMap.<String, List<Object>>of();
    }

    public void reset() {
        if (fieldsValues != null) fieldsValues.clear();
        source = null;
        uid = null;

        requiredFields.addAll(BASE_REQUIRED_FIELDS);
        if (loadSource) {
            requiredFields.add(SourceFieldMapper.NAME);
        }
    }

    public void addValue(String name, Object value) {
        if (fieldsValues == null) {
            fieldsValues = newHashMap();
        }

        List<Object> values = fieldsValues.get(name);
        if (values == null) {
            values = new ArrayList<>(2);
            fieldsValues.put(name, values);
        }
        values.add(value);
    }
    
    public void setValues(String name, List<Object> values) {
        if (fieldsValues == null) {
            fieldsValues = newHashMap();
        }
        this.fieldsValues.put(name, values);
    }
}
