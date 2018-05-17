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
package org.elassandra;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

/**
 * Elassandra composite key tests.
 * @author vroyer
 *
 */
public class CompletionTests extends ESSingleNodeTestCase {
    
    @Test
    public void testCompletionSubfield() throws Exception {
        
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("description")
                        .field("type", "text")
                        .field("cql_collection", "singleton")
                        .field("analyzer", "standard")
                        .startObject("fields")
                            .startObject("keywordstring")
                                .field("type", "text")
                                .field("analyzer", "keyword")
                            .endObject()
                        .endObject()
                    .endObject()
                    .startObject("tags")
                        .field("type", "keyword")
                        .field("cql_collection", "list")
                        .startObject("fields")
                            .startObject("tag_suggest").field("type", "completion").endObject()
                        .endObject() 
                    .endObject()
                    .startObject("title").field("type", "text").field("cql_collection", "singleton").endObject()
                .endObject()
            .endObject();
        assertAcked(client().admin().indices()
                .prepareCreate("products")
                .setSettings(Settings.builder().build())
                .addMapping("software", mapping));
        ensureGreen("products");
        
        assertThat(client().prepareIndex("products", "software", "1")
            .setSource("{\"title\": \"Product1\",\"description\": \"Product1 Description\",\"tags\": ["+
      "\"blog\",\"magazine\",\"responsive\",\"two columns\",\"wordpress\"]}", XContentType.JSON)
            .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        assertThat(client().prepareIndex("products", "software", "2")
                .setSource("{\"title\": \"Product2\",\"description\": \"Product2 Description\",\"tags\": ["+
          "\"blog\",\"paypal\",\"responsive\",\"skrill\",\"wordland\"]}", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        assertThat(client().prepareIndex("products", "software", "3")
                .setSource("{\"title\": \"Product2\",\"description\": \"Product2 Description\",\"tags\": ["+
          "\"blog\",\"paypal\",\"responsive\",\"skrill\",\"word\"] }", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        
        CompletionSuggestionBuilder suggestion = new CompletionSuggestionBuilder("tags.tag_suggest").text("word");
        SuggestBuilder sb = new SuggestBuilder().addSuggestion("product_suggest", suggestion);
        SearchResponse rsp = client().prepareSearch().setIndices("products").setTypes("software")
                .setQuery(QueryBuilders.matchAllQuery())
                .suggest(sb)
                .setSize(0)
                .get();
        
        for(org.elasticsearch.search.suggest.Suggest.Suggestion.Entry<? extends org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option> entry : rsp.getSuggest().getSuggestion("product_suggest").getEntries()) {
            assertThat(entry.getOptions().size(), equalTo(3));
        }
    }
    
    @Test
    public void testCompletionSuggestion() throws Exception {
        
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("description").field("type", "text").field("cql_collection", "singleton").endObject()
                    .startObject("tags").field("type", "keyword").field("cql_collection", "list").endObject()
                    .startObject("title").field("type", "text").field("cql_collection", "singleton").endObject()
                    .startObject("tag_suggest").field("type", "completion").endObject()
                .endObject()
            .endObject();
        assertAcked(client().admin().indices()
                .prepareCreate("products")
                .setSettings(Settings.builder().build())
                .addMapping("software", mapping));
        ensureGreen("products");
        
        assertThat(client().prepareIndex("products", "software", "1")
            .setSource("{\"title\": \"Product1\",\"description\": \"Product1 Description\",\"tags\": ["+
      "\"blog\",\"magazine\",\"responsive\",\"two columns\",\"wordpress\"],"+
      "\"tag_suggest\": {\"input\": [\"blog\", \"magazine\",\"responsive\",\"two columns\",\"wordpress\"]}}", XContentType.JSON)
            .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        assertThat(client().prepareIndex("products", "software", "2")
                .setSource("{\"title\": \"Product2\",\"description\": \"Product2 Description\",\"tags\": ["+
          "\"blog\",\"paypal\",\"responsive\",\"skrill\",\"wordland\"],"+
          "\"tag_suggest\": {\"input\": [\"blog\", \"paypal\",\"responsive\",\"skrill\",\"wordland\"]}}", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        assertThat(client().prepareIndex("products", "software", "3")
                .setSource("{\"title\": \"Product2\",\"description\": \"Product2 Description\",\"tags\": ["+
          "\"blog\",\"paypal\",\"responsive\",\"skrill\",\"wordland\"],"+
          "\"tag_suggest\": ["+
              "{\"input\": [\"blog\", \"paypal\",\"responsive\",\"skrill\",\"wordland\"], \"weight\" : 34}," +
              "{\"input\": [\"article\", \"paypal\",\"responsive\",\"skrill\",\"word\"], \"weight\" : 10 }"  +
              "] }", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        
        CompletionSuggestionBuilder suggestion = new CompletionSuggestionBuilder("tag_suggest").text("word");
        SuggestBuilder sb = new SuggestBuilder().addSuggestion("product_suggest", suggestion);
        SearchResponse rsp = client().prepareSearch().setIndices("products").setTypes("software")
                .setQuery(QueryBuilders.matchAllQuery())
                .suggest(sb)
                .setSize(0)
                .get();
        
        for(org.elasticsearch.search.suggest.Suggest.Suggestion.Entry<? extends org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option> entry : rsp.getSuggest().getSuggestion("product_suggest").getEntries()) {
            assertThat(entry.getOptions().size(), equalTo(3));
        }
    }
    
    @Test
    public void testCompletionSuggestioWithContext() throws Exception {
        
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("description").field("type", "text").field("cql_collection", "singleton").endObject()
                    .startObject("tags").field("type", "keyword").field("cql_collection", "list").endObject()
                    .startObject("title").field("type", "text").field("cql_collection", "singleton").endObject()
                    .startObject("tag_suggest")
                        .field("type", "completion")
                        .startArray("contexts")
                            .startObject()
                                .field("name", "place_type")
                                .field("type", "category")
                                .field("path", "cat")
                            .endObject()
                            .startObject()
                                .field("name", "location")
                                .field("type", "geo")
                                .field("precision", 4)
                                .field("path", "loc")
                            .endObject()
                        .endArray()
                    .endObject()
                .endObject()
            .endObject();
        assertAcked(client().admin().indices()
                .prepareCreate("products")
                .setSettings(Settings.builder().build())
                .addMapping("software", mapping));
        ensureGreen("products");
        
        assertThat(client().prepareIndex("products", "software", "1")
            .setSource("{\"title\": \"Product1\",\"description\": \"Product1 Description\",\"tags\": ["+
      "\"blog\",\"magazine\",\"responsive\",\"two columns\",\"wordpress\"],"+
      "\"tag_suggest\": {\"input\": [\"blog\", \"magazine\",\"responsive\",\"two columns\",\"wordpress\"],"+
                        "\"contexts\": {\"place_type\": [\"cafe\", \"food\"] } }}", XContentType.JSON)
            .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertThat(client().prepareIndex("products", "software", "2")
                .setSource("{\"title\": \"Product2\",\"description\": \"Product2 Description\",\"tags\": ["+
          "\"blog\",\"paypal\",\"responsive\",\"skrill\",\"wordland\"],"+
          "\"tag_suggest\": {\"input\": [\"blog\", \"paypal\",\"responsive\",\"skrill\",\"wordland\"],"+
                            "\"contexts\": {\"place_type\": [\"cafe\", \"shop\"] }}}", XContentType.JSON)
                .get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        Map<String, List<? extends ToXContent>> contextMap = new HashMap<>();
        contextMap.put("place_type", 
                Arrays.asList(
                        CategoryQueryContext.builder().setCategory("cafe").setBoost(2).build(),
                        CategoryQueryContext.builder().setCategory("food").setBoost(4).build()));
        
        CompletionSuggestionBuilder suggestion = new CompletionSuggestionBuilder("tag_suggest")
                .prefix("word")
                .size(10)
                .contexts(contextMap);
        SuggestBuilder sb = new SuggestBuilder().addSuggestion("product_suggest", suggestion);
        SearchResponse rsp = client().prepareSearch()
                .setQuery(QueryBuilders.matchAllQuery())
                .suggest(sb)
                .get();
        
        for(org.elasticsearch.search.suggest.Suggest.Suggestion.Entry<? extends org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option> entry : rsp.getSuggest().getSuggestion("product_suggest").getEntries()) {
            assertThat(entry.getOptions().size(), equalTo(2));
        }
    }
}
