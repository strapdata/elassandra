package org.elassandra;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.action.get.MultiGetShardRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;

public class CompositeTests extends ESSingleNodeTestCase {
    

    public void testCompositeWithStaticColumnTest() throws Exception {
        
        XContentBuilder mapping = XContentFactory.jsonBuilder()
            .startObject()
                .startObject("properties")
                    .startObject("id").field("type", "integer").field("cql_collection", "singleton").field("index", "no").field("cql_primary_key_order", 0).field("cql_partition_key", true).endObject()
                    .startObject("surname").field("type", "string").field("cql_collection", "singleton").field("index", "analyzed").field("cql_primary_key_order", 1).field("cql_partition_key", true).endObject()
                    .startObject("name").field("type", "string").field("cql_collection", "singleton").field("index", "analyzed").field("cql_primary_key_order", 2).endObject()
                    
                    .startObject("phonetic_name").field("type", "string").field("cql_collection", "singleton").field("index", "not_analyzed").field("cql_static_column", true).endObject()
                    .startObject("nicks").field("type", "string").field("cql_collection", "list").field("index", "analyzed").endObject()
                .endObject()
            .endObject();
        assertAcked(client().admin().indices().prepareCreate("test").addMapping("t2", mapping));
        ensureGreen("test");
        
        // INSERT INTO test.t2 (id, surname, name, phonetic_name, nicks) VALUES (22, 'Genesis', 'Abraham', 'ai-b-ram', ['the-A', 'ab'])
        clusterService().process(ConsistencyLevel.ONE,"INSERT INTO test.t2 (id, surname, name, phonetic_name, nicks) VALUES (22, 'Genesis', 'Abraham', 'ai-b-ram', ['the-A', 'ab'])");

        SearchResponse rsp = client().prepareSearch().setQuery("{ \"match_all\" : {} }").get();
        assertThat(rsp.getHits().getTotalHits(), equalTo(2L));
    }

    /*
     
     cqlsh <<EOF
CREATE KEYSPACE IF NOT EXISTS composite WITH replication={ 'class':'NetworkTopologyStrategy', 'dc1':'1' };
CREATE TABLE IF NOT EXISTS composite.t1 ( 
a text,
b text,
c bigint,
f float,
primary key ((a),b)
);
insert into composite.t1 (a,b,c,f) VALUES ('a','b1',1, 1.2);
insert into composite.t1 (a,b,c,f) VALUES ('b','b1',2, 5);

CREATE TABLE IF NOT EXISTS composite.t2 ( 
a text,
b text,
c bigint,
d bigint,
primary key ((a),b,c)
);
insert into composite.t2 (a,b,c,d) VALUES ('a','b2',2,1);
insert into composite.t2 (a,b,c,d) VALUES ('a','b2',3,1);


CREATE TABLE IF NOT EXISTS composite.t3 ( 
a text,
b text,
c bigint,
d bigint,
primary key ((a,b),c)
);
insert into composite.t3 (a,b,c,d) VALUES ('a','b3',2,3);
insert into composite.t3 (a,b,c,d) VALUES ('a','b3',3,3);
EOF

curl -XPUT "http://$NODE:9200/composite/_mapping/t1" -d '{ "t1" : { "discover" : ".*" }}'
curl -XPUT "http://$NODE:9200/composite/_mapping/t2" -d '{ "t2" : { "discover" : ".*" }}'
curl -XPUT "http://$NODE:9200/composite/_mapping/t3" -d '{ "t3" : { "discover" : ".*" }}'

curl -XGET "http://$NODE:9200/composite/t1/\[\"a\",\"b1\"\]?pretty=true" 
curl -XGET "http://$NODE:9200/composite/t2/\[\"a\",\"b2\",2\]?pretty=true"
curl -XGET "http://$NODE:9200/composite/t3/\[\"a\",\"b3\",2\]?pretty=true"


curl -XGET "http://$NODE:9200/composite/t1/_search?pretty=true&q=c:1"
curl -XGET "http://$NODE:9200/composite/t2/_search?pretty=true&q=d:1"
curl -XGET "http://$NODE:9200/composite/t3/_search?pretty=true&q=d:3"

curl "$NODE:9200/composite/t1/_mget?pretty=true" -d '{
    "docs" : [
        { "_id" : "[\"a\",\"b1\"]" },
        { "_id" : "[\"b\",\"b1\"]" }
    ]
}'
curl "$NODE:9200/composite/t2/_mget?pretty=true" -d '{
    "docs" : [
        { "_id" : "[\"a\",\"b2\",2]" },
        { "_id" : "[\"a\",\"b2\",3]" }
    ]
}'
curl "$NODE:9200/composite/t3/_mget?pretty=true" -d '{
    "docs" : [
        { "_id" : "[\"a\",\"b3\",2]" },
        { "_id" : "[\"a\",\"b3\",3]" }
    ]
}'

cqlsh <<EOF
DELETE FROM composite.t1 WHERE a='a';
EOF

curl "$NODE:9200/composite/t1/_mget?pretty=true" -d '{
    "docs" : [
        { "_id" : "[\"a\",\"b1\"]" },
        { "_id" : "[\"b\",\"b1\"]" }
    ]
}'
     
     */
    
    public void testCompositeTest() throws Exception {
        createIndex("composite");
        ensureGreen("composite");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite.t1 ( a text,b text,c bigint,f float,primary key ((a),b) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite.t2 ( a text,b text,c bigint,d bigint,primary key ((a),b,c) )");
        process(ConsistencyLevel.ONE,"CREATE TABLE IF NOT EXISTS composite.t3 ( a text,b text,c bigint,d bigint,primary key ((a,b),c) )");

        assertAcked(client().admin().indices().preparePutMapping("composite").setType("t1").setSource("{ \"t1\" : { \"discover\" : \".*\" }}").get());
        assertAcked(client().admin().indices().preparePutMapping("composite").setType("t2").setSource("{ \"t2\" : { \"discover\" : \".*\" }}").get());
        assertAcked(client().admin().indices().preparePutMapping("composite").setType("t3").setSource("{ \"t3\" : { \"discover\" : \".*\" }}").get());
        
        process(ConsistencyLevel.ONE,"insert into composite.t1 (a,b,c,f) VALUES ('a','b1',1, 1.2)");
        process(ConsistencyLevel.ONE,"insert into composite.t1 (a,b,c,f) VALUES ('b','b1',2, 5);");
        
        process(ConsistencyLevel.ONE,"insert into composite.t2 (a,b,c,d) VALUES ('a','b2',2,1)");
        process(ConsistencyLevel.ONE,"insert into composite.t2 (a,b,c,d) VALUES ('a','b2',3,1)");
        
        process(ConsistencyLevel.ONE,"insert into composite.t3 (a,b,c,d) VALUES ('a','b3',2,3)");
        process(ConsistencyLevel.ONE,"insert into composite.t3 (a,b,c,d) VALUES ('a','b3',3,3)");
        process(ConsistencyLevel.ONE,"insert into composite.t3 (a,b,c,d) VALUES ('a','b3',4,4)");
        process(ConsistencyLevel.ONE,"insert into composite.t3 (a,b,c,d) VALUES ('a','b3',5,5)");
        process(ConsistencyLevel.ONE,"insert into composite.t3 (a,b,c,d) VALUES ('a','b3',6,6)");
        
        assertThat(client().prepareGet().setIndex("composite").setType("t1").setId("[\"a\",\"b1\"]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite").setType("t2").setId("[\"a\",\"b2\",2]").get().isExists(),equalTo(true));
        assertThat(client().prepareGet().setIndex("composite").setType("t3").setId("[\"a\",\"b3\",2]").get().isExists(),equalTo(true));
        
        assertThat(client().prepareSearch().setIndices("composite").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("c:1")).get().getHits().getTotalHits(), equalTo(1L));
        assertThat(client().prepareSearch().setIndices("composite").setTypes("t2").setQuery(QueryBuilders.queryStringQuery("d:1")).get().getHits().getTotalHits(), equalTo(2L));
        assertThat(client().prepareSearch().setIndices("composite").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("d:3")).get().getHits().getTotalHits(), equalTo(2L));
        
        assertThat(client().prepareMultiGet().add("composite", "t1", "[\"a\",\"b1\"]", "[\"b\",\"b1\"]").get().getResponses()[0].getIndex(), equalTo("composite") );
        assertThat(client().prepareMultiGet().add("composite", "t2", "[\"a\",\"b2\",2]", "[\"a\",\"b2\",3]").get().getResponses()[0].getIndex(), equalTo("composite") );
        assertThat(client().prepareMultiGet().add("composite", "t3", "[\"a\",\"b3\",2]", "[\"a\",\"b3\",3]").get().getResponses()[0].getIndex(), equalTo("composite")  );
        
        // delete with partition key
        process(ConsistencyLevel.ONE,"DELETE FROM composite.t1 WHERE a='a'");
        assertThat(client().prepareSearch().setIndices("composite").setTypes("t1").setQuery(QueryBuilders.queryStringQuery("c:*")).get().getHits().getTotalHits(), equalTo(1L));
        
        // delete with primary key
        process(ConsistencyLevel.ONE,"DELETE FROM composite.t2 WHERE a='a' AND b='b2' AND c=2");
        assertThat(client().prepareSearch().setIndices("composite").setTypes("t2").setQuery(QueryBuilders.queryStringQuery("d:1")).get().getHits().getTotalHits(), equalTo(1L));
        
        // delete a range slice
        process(ConsistencyLevel.ONE,"DELETE FROM composite.t3 WHERE a='a' AND b='b3' AND c = 4");
        assertThat(client().prepareSearch().setIndices("composite").setTypes("t3").setQuery(QueryBuilders.queryStringQuery("a:a")).get().getHits().getTotalHits(), equalTo(4L));
    }
}
