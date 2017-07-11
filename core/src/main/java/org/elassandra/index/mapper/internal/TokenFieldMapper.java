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

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.elassandra.cluster.service.ClusterService;
import org.elasticsearch.Version;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.mapper.EnabledAttributeMapper;
import org.elasticsearch.index.mapper.LegacyLongFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper;
import org.elasticsearch.index.mapper.NumberFieldMapper.NumberFieldType;
import org.elasticsearch.index.mapper.ParseContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.lenientNodeBooleanValue;

/**
 *  Mapper for the _token field, the cassandra row token.
 **/
public class TokenFieldMapper extends MetadataFieldMapper {

    public static final String NAME = "_token";
    public static final String CONTENT_TYPE = "_token";

    public static class Defaults extends LegacyLongFieldMapper.Defaults {
        public static final String NAME = TokenFieldMapper.NAME;
        public static final TokenFieldType TOKEN_FIELD_TYPE = new TokenFieldType(NumberFieldMapper.NumberType.LONG);

        static {
            TOKEN_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            TOKEN_FIELD_TYPE.setStored(false);
            TOKEN_FIELD_TYPE.setTokenized(false);
            TOKEN_FIELD_TYPE.setOmitNorms(true);
            TOKEN_FIELD_TYPE.setHasDocValues(true);
            TOKEN_FIELD_TYPE.setDocValuesType(DocValuesType.NUMERIC);
            TOKEN_FIELD_TYPE.setNumericPrecisionStep(ClusterService.defaultPrecisionStep);
            TOKEN_FIELD_TYPE.setIndexAnalyzer(Lucene.KEYWORD_ANALYZER);
            TOKEN_FIELD_TYPE.setSearchAnalyzer(Lucene.KEYWORD_ANALYZER);
            TOKEN_FIELD_TYPE.setName(NAME);
            TOKEN_FIELD_TYPE.freeze();
        }
        public static final EnabledAttributeMapper ENABLED_STATE = EnabledAttributeMapper.ENABLED;
        public static final long DEFAULT = -1;
    }

    public static class Builder extends MetadataFieldMapper.Builder<Builder, TokenFieldMapper> {

        private EnabledAttributeMapper enabledState = EnabledAttributeMapper.UNSET_DISABLED;
        private long defaultTTL = Defaults.DEFAULT;

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
                    EnabledAttributeMapper enabledState = lenientNodeBooleanValue(fieldNode, fieldName) ? EnabledAttributeMapper.ENABLED : EnabledAttributeMapper.DISABLED;
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

    public static final class TokenFieldType extends NumberFieldType {

        protected TokenFieldType(TokenFieldType  ref) {
            super(ref);
        }

        protected TokenFieldType(NumberFieldMapper.NumberType  type) {
            super(type);
        }
        
        @Override
        public TokenFieldType clone() {
            return new TokenFieldType(this);
        }
    }

    private EnabledAttributeMapper enabledState;
    
    private TokenFieldMapper(Settings indexSettings) {
        this(Defaults.TOKEN_FIELD_TYPE.clone(), Defaults.ENABLED_STATE, Defaults.DEFAULT, indexSettings);
    }

    private TokenFieldMapper(MappedFieldType fieldType, EnabledAttributeMapper enabled, long defaultToken, Settings indexSettings) {
        super(NAME, fieldType, Defaults.TOKEN_FIELD_TYPE, indexSettings);
        this.enabledState = enabled;
    }
    
    @Override
    public void preParse(ParseContext context) throws IOException {
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        return null;
    }

    @Override
    public void postParse(ParseContext context) throws IOException {
        super.parse(context);
    }

    
    @Override
    public void createField(ParseContext context, Object object) throws IOException {
        Long token = (Long) object;
        if (token != null) {
            context.doc().add(new LegacyLongFieldMapper.CustomLongNumericField(token, fieldType()));
        }
    }
    
    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        if (context.sourceToParse().token() != null) {
            Long token = context.sourceToParse().token();
            if (token != null) {
                fields.add(new LegacyLongFieldMapper.CustomLongNumericField(token, fieldType()));
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
