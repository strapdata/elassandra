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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.IndexOptions;
import org.elasticsearch.Version;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.analysis.NumericLongAnalyzer;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.LongFieldMapper;

/**
 *  Mapper for the _token field, the cassandra row token.
 **/
public class TokenFieldMapper extends MetadataFieldMapper {

    public static final String NAME = "_token";
    public static final String CONTENT_TYPE = "_token";

    public static class Defaults extends LongFieldMapper.Defaults {
        public static final String NAME = TokenFieldMapper.NAME;
        public static final TokenFieldType TOKEN_FIELD_TYPE = new TokenFieldType();

        static {
            TOKEN_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            TOKEN_FIELD_TYPE.setStored(false);
            TOKEN_FIELD_TYPE.setTokenized(false);
            TOKEN_FIELD_TYPE.setNumericPrecisionStep(Defaults.PRECISION_STEP_64_BIT);
            TOKEN_FIELD_TYPE.setIndexAnalyzer(NumericLongAnalyzer.buildNamedAnalyzer(Defaults.PRECISION_STEP_64_BIT));
            TOKEN_FIELD_TYPE.setSearchAnalyzer(NumericLongAnalyzer.buildNamedAnalyzer(Integer.MAX_VALUE));
            TOKEN_FIELD_TYPE.setNames(new MappedFieldType.Names(NAME));
            TOKEN_FIELD_TYPE.freeze();
        }
    }

    public static class Builder extends MetadataFieldMapper.Builder<Builder, TokenFieldMapper> {

        public Builder() {
            super(Defaults.NAME, Defaults.TOKEN_FIELD_TYPE, Defaults.FIELD_TYPE);
        }

        @Override
        public TokenFieldMapper build(BuilderContext context) {
            return new TokenFieldMapper(context.indexSettings());
        }
    }

    public static class TypeParser implements MetadataFieldMapper.TypeParser {
        @Override
        public MetadataFieldMapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
            Builder builder = new Builder();
            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = Strings.toUnderscoreCase(entry.getKey());
                if (fieldName.equals("doc_values_format") && parserContext.indexVersionCreated().before(Version.V_2_0_0_beta1)) {
                    // ignore in 1.x, reject in 2.x
                    iterator.remove();
                }
            }
            return builder;
        }

        @Override
        public MetadataFieldMapper getDefault(Settings indexSettings, MappedFieldType fieldType, String typeName) {
            return new TokenFieldMapper(indexSettings);
        }
    }

    static final class TokenFieldType extends LongFieldMapper.LongFieldType {

        public TokenFieldType() {
            super();
        }

        protected TokenFieldType(TokenFieldType  ref) {
            super(ref);
        }

        @Override
        public TokenFieldType clone() {
            return new TokenFieldType(this);
        }

    }

    private TokenFieldMapper(Settings indexSettings) {
        super(NAME, Defaults.TOKEN_FIELD_TYPE, Defaults.TOKEN_FIELD_TYPE, indexSettings);
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
            context.doc().add(new LongFieldMapper.CustomLongNumericField(token, fieldType()));
            context.doc().add(new SortedNumericDocValuesField(fieldType().names().indexName(), token));
        }
    }
    
    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
        if (context.sourceToParse().token() != null) {
            Long token = context.sourceToParse().token();
            if (token != null) {
                fields.add(new LongFieldMapper.CustomLongNumericField(token, fieldType()));
                fields.add(new SortedNumericDocValuesField(fieldType().names().indexName(), token));
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
