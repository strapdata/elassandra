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

import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


/**
 * Test pk-only documents.
 * @author Barth
 */
public class PkOnlyTests extends ESSingleNodeTestCase {
    
    /**
     * Test indexing dynamically an empty document (pk-only), creating the underlying CQL table on the fly.
     */
    @Test
    public void testPkOnlyDocumentNoTable() {
        createIndex("test1");
        ensureGreen("test1");
        
        testSimplePrimaryKey("_id");
    }
    
    @Test
    public void testDynamicMappingPkCustomName() {
        createIndex("test1");
        ensureGreen("test1");
    
        process(ConsistencyLevel.ONE,"CREATE TABLE test1.pk_custom (my_id text PRIMARY KEY, name list<text>)");
        assertThat(client().prepareIndex("test1", "pk_custom", "1").setSource("{\"name\": \"test\"}",
            XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
    }
    
    /**
     * Test indexing dynamically an empty document (pk-only), mapping an existing CQL table.
     */
    @Test
    public void testPkOnlyDocumentExistingTable() {
        createIndex("test1");
        ensureGreen("test1");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE test1.pk_only (id text PRIMARY KEY)");
        testSimplePrimaryKey("id");
    }
    
    /**
     * Test indexing dynamically an empty document (pk-only), mapping an existing CQL table, with clustering keys.
     */
    @Test
    public void testPkOnlyDocumentWithClusteringKey() {
        createIndex("test1");
        ensureGreen("test1");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE test1.pk_only (id text, a text, b text, primary key (id, a, b))");
        testCompositePrimaryKey();
    }
    
    /**
     * Test indexing dynamically an empty document (pk-only), mapping an existing CQL table, with composite Partition key and one clustering key.
     */
    @Test
    public void testPkOnlyDocumentWithCompositePartitionKey() {
        createIndex("test1");
        ensureGreen("test1");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE test1.pk_only (id text, a text, b text, primary key ((id, a), b))");
        testCompositePrimaryKey();
    }
    
    /**
     * Test empty pk-only document with an explicit mapping where pk columns are indexed
     */
    @Test
    public void testPkOnlyDocumentPkColumnsIndexed() throws IOException {
        createIndex("test1");
        ensureGreen("test1");

        // create a table
        process(ConsistencyLevel.ONE,"CREATE TABLE test1.pk_only (id text, a text, b text, primary key (id, a, b))");
    
        // put a mapping
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("b")
                        .field("type", "keyword")
                        .field("cql_collection", "singleton")
                    .endObject()
                .endObject()
            .endObject();
        assertAcked(client().admin().indices().preparePutMapping("test1")
            .setType("pk_only")
            .setSource(mapping)
            .get());
    
        // insert two documents
        assertThat(client().prepareIndex("test1", "pk_only", "[\"1\", \"11\", \"111\"]").setSource("{}", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertThat(client().prepareIndex("test1", "pk_only", "[\"2\", \"22\", \"222\"]").setSource("{}", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        // execute search
        SearchResponse resp = client().prepareSearch("test1").setTypes("pk_only").setQuery(QueryBuilders.matchQuery("b", "222")).get();
        assertThat(resp.getHits().getTotalHits(), equalTo(1L));
        assertThat(resp.getHits().getAt(0).getId(), equalTo("[\"2\",\"22\",\"222\"]"));
        assertThat(resp.getHits().getAt(0).getSourceAsMap(), is(new HashMap<String, String>() {{ put("b","222"); }}));
    }
    
    private void testSimplePrimaryKey(String pkName) {
        UntypedResultSet rs;
        GetResponse resp;
        
        // insert two empty documents, generating a mapping update
        assertThat(client().prepareIndex("test1", "pk_only", "1").setSource("{}", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertThat(client().prepareIndex("test1", "pk_only", "2").setSource("{}", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        
        // get the first record from CQL
        rs = process(ConsistencyLevel.ONE, String.format("SELECT * FROM test1.pk_only WHERE \"%s\" = '1'", pkName));
        
        // assert only one row
        assertEquals(1, rs.size());
        // assert only one column
        assertEquals(1, rs.metadata().size());
        // assert the name is of the column is "_id"
        assertThat(rs.metadata().get(0).name.toString(), equalTo(pkName));
        // ensure the value is correct
        assertThat(rs.one().getString(pkName), equalTo("1"));
        
        
        // ensure elasticsearch can query this records
        assertThat(client().prepareSearch().setIndices("test1").setTypes("pk_only").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2L));
        resp = client().prepareGet().setIndex("test1").setType("pk_only").setId("1").get();
        assertTrue(resp.isExists());
        assertTrue(resp.getSource().isEmpty());
        
        // now add some fields to check it continue to works
        assertThat(client().prepareIndex("test1", "pk_only", "3").setSource("{ \"new_field\": \"test\" }", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));

        // fetch the new inserted row with CQL
        rs = process(ConsistencyLevel.ONE, String.format("SELECT * FROM test1.pk_only WHERE \"%s\" = '3'", pkName));

        // assert only one row
        assertEquals(1, rs.size());
        // assert on more column
        assertEquals(2, rs.metadata().size());
        // assert the name of columns are correct
        assertThat(rs.metadata().get(0).name.toString(), equalTo(pkName));
        assertThat(rs.metadata().get(1).name.toString(), equalTo("new_field"));
        // ensure the values are correct
        assertThat(rs.one().getString(pkName), equalTo("3"));
        assertThat(rs.one().getList("new_field", UTF8Type.instance), is(Collections.singletonList("test")));
    
        // check we got the appropriate extra field in elasticsearch
        assertThat(client().prepareSearch().setIndices("test1").setTypes("pk_only").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3L));
        resp = client().prepareGet().setIndex("test1").setType("pk_only").setId("3").get();
        assertTrue(resp.isExists());
        assertThat(resp.getSource().size(), equalTo(1));
        assertThat(resp.getSource().get("new_field"), equalTo("test"));
    }
    
    private void testCompositePrimaryKey() {
        UntypedResultSet rs;
        GetResponse resp;
        
        // insert two empty documents, generating a mapping update
        assertThat(client().prepareIndex("test1", "pk_only", "[\"1\", \"11\", \"111\"]").setSource("{}", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        assertThat(client().prepareIndex("test1", "pk_only", "[\"2\", \"22\", \"222\"]").setSource("{}", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        
        // get the first record from CQL
        rs = process(ConsistencyLevel.ONE, "SELECT * FROM test1.pk_only WHERE id = '1' AND a = '11' AND b = '111'");
        
        // assert only one row
        assertEquals(1, rs.size());
        // assert N columns
        assertEquals(3, rs.metadata().size());
        // assert the name of the columns
        assertThat(rs.metadata().get(0).name.toString(), equalTo("id"));
        assertThat(rs.metadata().get(1).name.toString(), equalTo("a"));
        assertThat(rs.metadata().get(2).name.toString(), equalTo("b"));
    
        // ensure the values are correct
        assertThat(rs.one().getString("id"), equalTo("1"));
        assertThat(rs.one().getString("a"), equalTo("11"));
        assertThat(rs.one().getString("b"), equalTo("111"));
        
        
        // ensure elasticsearch can query this records
        assertThat(client().prepareSearch().setIndices("test1").setTypes("pk_only").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(2L));
        resp = client().prepareGet().setIndex("test1").setType("pk_only").setId("[\"1\", \"11\", \"111\"]").get();
        assertTrue(resp.isExists());
        assertTrue(resp.getSource().isEmpty());
        
        // now add some fields to check it continue to works
        assertThat(client().prepareIndex("test1", "pk_only", "[\"3\", \"33\", \"333\"]").setSource("{ \"new_field\": \"test\" }", XContentType.JSON).get().getResult(), equalTo(DocWriteResponse.Result.CREATED));
        
        // fetch the new inserted row with CQL
        rs = process(ConsistencyLevel.ONE, "SELECT * FROM test1.pk_only WHERE id = '3' AND a = '33' AND b = '333'");
        
        // assert only one row
        assertEquals(1, rs.size());
        // assert on more column
        assertEquals(4, rs.metadata().size());
        // assert the name of columns are correct
        assertThat(rs.metadata().get(0).name.toString(), equalTo("id"));
        assertThat(rs.metadata().get(1).name.toString(), equalTo("a"));
        assertThat(rs.metadata().get(2).name.toString(), equalTo("b"));
        assertThat(rs.metadata().get(3).name.toString(), equalTo("new_field"));
    
        // ensure the values are correct
        assertThat(rs.one().getString("id"), equalTo("3"));
        assertThat(rs.one().getString("a"), equalTo("33"));
        assertThat(rs.one().getString("b"), equalTo("333"));
        assertThat(rs.one().getList("new_field", UTF8Type.instance), is(Collections.singletonList("test")));
        
        // check we got the appropriate extra field in elasticsearch
        assertThat(client().prepareSearch().setIndices("test1").setTypes("pk_only").setQuery(QueryBuilders.matchAllQuery()).get().getHits().getTotalHits(), equalTo(3L));
        resp = client().prepareGet().setIndex("test1").setType("pk_only").setId("[\"3\", \"33\", \"333\"]").get();
        assertTrue(resp.isExists());
        assertThat(resp.getSource().size(), equalTo(1));
        assertThat(resp.getSource().get("new_field"), equalTo("test"));
    }
    
}

