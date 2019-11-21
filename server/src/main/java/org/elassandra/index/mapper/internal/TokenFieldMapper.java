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
package org.elassandra.index.mapper.internal;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.IndexNumericFieldData.NumericType;
import org.elasticsearch.index.fielddata.plain.DocValuesIndexFieldData;
import org.elasticsearch.index.mapper.EnabledAttributeMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.SimpleMappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeBooleanValue;

/**
 *  Mapper for the _token field, the cassandra row token.
 **/
public class TokenFieldMapper extends MetadataFieldMapper {

    public static final String NAME = "_token";
    public static final String CONTENT_TYPE = "_token";

    public static class Defaults {
        public static final String NAME = TokenFieldMapper.NAME;
        public static final TokenFieldType TOKEN_FIELD_TYPE = new TokenFieldType();

        static {
            TOKEN_FIELD_TYPE.setName(NAME);
            TOKEN_FIELD_TYPE.setDocValuesType(DocValuesType.SORTED_NUMERIC);
            TOKEN_FIELD_TYPE.setHasDocValues(true);
            TOKEN_FIELD_TYPE.freeze();
        }
        public static final EnabledAttributeMapper ENABLED_STATE = EnabledAttributeMapper.ENABLED;
        public static final long DEFAULT = -1;
    }

    public static class Builder extends MetadataFieldMapper.Builder<Builder, TokenFieldMapper> {

        private EnabledAttributeMapper enabledState = EnabledAttributeMapper.UNSET_DISABLED;

        public Builder() {
            super(Defaults.NAME, Defaults.TOKEN_FIELD_TYPE, Defaults.TOKEN_FIELD_TYPE);
        }

        public Builder enabled(EnabledAttributeMapper enabled) {
            this.enabledState = enabled;
            return builder;
        }

        @Override
        public TokenFieldMapper build(BuilderContext context) {
            setupFieldType(context);
            return new TokenFieldMapper(fieldType, enabledState, 0, context.indexSettings());
        }
    }

    public static class TypeParser implements MetadataFieldMapper.TypeParser {
        @Override
        public MetadataFieldMapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
            Builder builder = new Builder();
            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                if (fieldName.equals("enabled")) {
                    EnabledAttributeMapper enabledState = nodeBooleanValue(fieldNode, fieldName) ? EnabledAttributeMapper.ENABLED : EnabledAttributeMapper.DISABLED;
                    builder.enabled(enabledState);
                    iterator.remove();
                }
            }
            return builder;
        }

        @Override
        public MetadataFieldMapper getDefault(MappedFieldType fieldType, ParserContext parserContext) {
            final Settings indexSettings = parserContext.mapperService().getIndexSettings().getSettings();
            return new TokenFieldMapper(indexSettings);
        }
    }

    public static final class TokenFieldType extends SimpleMappedFieldType {

        TokenFieldType() {
        }

        protected TokenFieldType(TokenFieldType  ref) {
            super(ref);
        }

        @Override
        public TokenFieldType clone() {
            return new TokenFieldType(this);
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        private long parse(Object value) {
            if (value instanceof Number) {
                double doubleValue = ((Number) value).doubleValue();
                if (doubleValue < Long.MIN_VALUE || doubleValue > Long.MAX_VALUE) {
                    throw new IllegalArgumentException("Value [" + value + "] is out of range for a long");
                }
                if (doubleValue % 1 != 0) {
                    throw new IllegalArgumentException("Value [" + value + "] has a decimal part");
                }
                return ((Number) value).longValue();
            }
            if (value instanceof BytesRef) {
                value = ((BytesRef) value).utf8ToString();
            }
            return Long.parseLong(value.toString());
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            return new DocValuesFieldExistsQuery(name());
        }

        @Override
        public Query termQuery(Object value, @Nullable QueryShardContext context) {
            long v = parse(value);
            return LongPoint.newExactQuery(name(), v);
        }

        @Override
        public Query termsQuery(List<?> values, @Nullable QueryShardContext context) {
            long[] v = new long[values.size()];
            for (int i = 0; i < values.size(); ++i) {
                v[i] = parse(values.get(i));
            }
            return LongPoint.newSetQuery(name(), v);
        }

        @Override
        public Query rangeQuery(Object lowerTerm, Object upperTerm, boolean includeLower,
                                boolean includeUpper, QueryShardContext context) {
            long l = Long.MIN_VALUE;
            long u = Long.MAX_VALUE;
            if (lowerTerm != null) {
                l = parse(lowerTerm);
                if (includeLower == false) {
                    if (l == Long.MAX_VALUE) {
                        return new MatchNoDocsQuery();
                    }
                    ++l;
                }
            }
            if (upperTerm != null) {
                u = parse(upperTerm);
                if (includeUpper == false) {
                    if (u == Long.MIN_VALUE) {
                        return new MatchNoDocsQuery();
                    }
                    --u;
                }
            }
            return LongPoint.newRangeQuery(name(), l, u);
        }

        @Override
        public IndexFieldData.Builder fielddataBuilder(String fullyQualifiedIndexName) {
            failIfNoDocValues();
            return new DocValuesIndexFieldData.Builder().numericType(NumericType.LONG);
        }
    }

    private EnabledAttributeMapper enabledState;

    private TokenFieldMapper(Settings indexSettings) {
        super(NAME, Defaults.TOKEN_FIELD_TYPE, Defaults.TOKEN_FIELD_TYPE, indexSettings);
    }

    private TokenFieldMapper(MappedFieldType fieldType, EnabledAttributeMapper enabled, long defaultToken, Settings indexSettings) {
        super(NAME, fieldType, Defaults.TOKEN_FIELD_TYPE, indexSettings);
        this.enabledState = enabled;
    }

    @Override
    public void preParse(ParseContext context) throws IOException {
    }

    @Override
    public void parse(ParseContext context) throws IOException {
    }

    @Override
    public void postParse(ParseContext context) throws IOException {
        super.parse(context);
    }

    @Override
    public void createField(ParseContext context, Object object, Optional<String> fieldName) throws IOException {
        Long token = (Long) object;
        if (token != null) {
            context.doc().add(new LongPoint(TokenFieldMapper.NAME, token));
            context.doc().add(new SortedNumericDocValuesField(TokenFieldMapper.NAME, token));
        }
    }

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        if (context.sourceToParse().token() != null) {
            Long token = context.sourceToParse().token();
            if (token != null) {
                fields.add(new LongPoint( TokenFieldMapper.NAME, token));
                fields.add(new NumericDocValuesField(TokenFieldMapper.NAME, token));
            }
        }
    }


    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }

    @Override
    protected void doMerge(Mapper mergeWith, boolean updateAllTypes) {
        // nothing to do
    }

}
