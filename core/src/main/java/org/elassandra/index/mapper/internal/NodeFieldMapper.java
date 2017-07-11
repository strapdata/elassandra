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
import org.apache.lucene.search.Query;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.StringFieldMapper;
import org.elasticsearch.index.mapper.StringFieldType;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *  Mapper for the _node field, the cassandra host id.
 **/
public class NodeFieldMapper extends MetadataFieldMapper {

    public static final String NAME = "_node";
    public static final String CONTENT_TYPE = "_node";

    public static class Defaults extends StringFieldMapper.Defaults {
        public static final String NAME = NodeFieldMapper.NAME;
        public static final MappedFieldType NODE_FIELD_TYPE = new NodeFieldType();

        static {
            NODE_FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            NODE_FIELD_TYPE.setStored(false);
            NODE_FIELD_TYPE.setOmitNorms(true);
            NODE_FIELD_TYPE.setIndexAnalyzer(Lucene.KEYWORD_ANALYZER);
            NODE_FIELD_TYPE.setSearchAnalyzer(Lucene.KEYWORD_ANALYZER);
            NODE_FIELD_TYPE.freeze();
        }
    }

    public static class Builder extends MetadataFieldMapper.Builder<Builder, NodeFieldMapper> {

        public Builder() {
            super(Defaults.NAME, Defaults.NODE_FIELD_TYPE, Defaults.FIELD_TYPE);
        }

        @Override
        public NodeFieldMapper build(BuilderContext context) {
            return new NodeFieldMapper(context.indexSettings());
        }
    }

    public static class TypeParser implements MetadataFieldMapper.TypeParser {
        @Override
        public MetadataFieldMapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
            Builder builder = new Builder();
            return builder;
        }

        @Override
        public MetadataFieldMapper getDefault(MappedFieldType fieldType, ParserContext parserContext) {
            final Settings indexSettings = parserContext.mapperService().getIndexSettings().getSettings();
            return new NodeFieldMapper(indexSettings);
        }
    }

    static final class NodeFieldType extends StringFieldType {

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
        public Query termQuery(Object value, QueryShardContext context) {
            return null;
        }

    }

    private NodeFieldMapper(Settings indexSettings) {
        super(NAME, Defaults.NODE_FIELD_TYPE, Defaults.NODE_FIELD_TYPE, indexSettings);
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
