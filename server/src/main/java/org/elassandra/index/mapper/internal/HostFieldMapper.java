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

import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.mapper.EnabledAttributeMapper;
import org.elasticsearch.index.mapper.KeywordFieldMapper;
import org.elasticsearch.index.mapper.KeywordFieldMapper.KeywordFieldType;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeBooleanValue;

/**
 *  Mapper for the _node field, the cassandra host id.
 **/
public class HostFieldMapper extends MetadataFieldMapper {

    public static final String NAME = "_host";
    public static final String CONTENT_TYPE = "_host";

    public static class Defaults extends KeywordFieldMapper.Defaults {
        public static final String NAME = HostFieldMapper.NAME;
        public static final MappedFieldType HOST_FIELD_TYPE = new NodeFieldType();

        static {
            HOST_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            HOST_FIELD_TYPE.setStored(false);
            HOST_FIELD_TYPE.setOmitNorms(true);
            HOST_FIELD_TYPE.setIndexAnalyzer(Lucene.KEYWORD_ANALYZER);
            HOST_FIELD_TYPE.setSearchAnalyzer(Lucene.KEYWORD_ANALYZER);
            HOST_FIELD_TYPE.freeze();
        }
    }
    
    public static class Builder extends MetadataFieldMapper.Builder<Builder, HostFieldMapper> {

        private EnabledAttributeMapper enabledState = EnabledAttributeMapper.UNSET_DISABLED;

        public Builder() {
            super(Defaults.NAME, Defaults.HOST_FIELD_TYPE, Defaults.FIELD_TYPE);
        }

        public Builder enabled(EnabledAttributeMapper enabled) {
            this.enabledState = enabled;
            return builder;
        }

        @Override
        public HostFieldMapper build(BuilderContext context) {
            setupFieldType(context);
            return new HostFieldMapper(fieldType, enabledState, 0, context.indexSettings());
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
            return new HostFieldMapper(indexSettings);
        }
    }

    static final class NodeFieldType extends KeywordFieldType {

        public NodeFieldType() {
        }

        protected NodeFieldType(NodeFieldType  ref) {
            super(ref);
        }

        @Override
        public NodeFieldType clone() {
            return new NodeFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        @Override
        public String name() {
            return NAME;
        }

    }
    
    private EnabledAttributeMapper enabledState = EnabledAttributeMapper.UNSET_DISABLED;

    private HostFieldMapper(Settings indexSettings) {
        super(NAME, Defaults.HOST_FIELD_TYPE, Defaults.HOST_FIELD_TYPE, indexSettings);
    }

    private HostFieldMapper(MappedFieldType fieldType, EnabledAttributeMapper enabled, long defaultToken, Settings indexSettings) {
        super(NAME, fieldType, Defaults.HOST_FIELD_TYPE, indexSettings);
        this.enabledState = enabled;
    }
    
    @Override
    public void preParse(ParseContext context) throws IOException {
    }

    @Override
    public void parse(ParseContext context) throws IOException {
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

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
    }

}
