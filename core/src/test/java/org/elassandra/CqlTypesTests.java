package org.elassandra;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.TupleType;
import org.elassandra.cluster.InternalCassandraClusterService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;

public class CqlTypesTests extends ESSingleNodeTestCase {
    
    public void testTest() throws Exception {
        createIndex("cmdb");
        ensureGreen("cmdb");
        
        process(ConsistencyLevel.ONE,"CREATE TABLE cmdb.server ( name text, ip inet, netmask int, prod boolean, primary key (name))");
        assertAcked(client().admin().indices().preparePutMapping("cmdb")
                .setType("server")
                .setSource("{ \"server\" : { \"discover\" : \".*\", \"properties\": { \"name\":{ \"type\":\"string\", \"index\":\"not_analyzed\" }}}}")
                .get());
        
        process(ConsistencyLevel.ONE,"insert into cmdb.server (name,ip,netmask,prod) VALUES ('localhost','127.0.0.1',8,true)");
        process(ConsistencyLevel.ONE,"insert into cmdb.server (name,ip,netmask,prod) VALUES ('my-server','123.45.67.78',24,true)");
        
        assertThat(client().prepareGet().setIndex("cmdb").setType("server").setId("my-server").get().isExists(), equalTo(true));
        assertThat(client().prepareGet().setIndex("cmdb").setType("server").setId("localhost").get().isExists(), equalTo(true));
        
        assertThat(client().prepareIndex("cmdb", "server", "bigserver234")
            .setSource("{\"ip\": \"22.22.22.22\", \"netmask\":32, \"prod\" : true, \"description\": \"my big server\" }")
            .get().isCreated(), equalTo(true));
        
        assertThat(client().prepareSearch().setIndices("cmdb").setTypes("server").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(3L));
    }

    @Test
    public void testAllTypesTest() throws Exception {
        createIndex("ks1");
        ensureGreen("ks1");
        
        process(ConsistencyLevel.ONE,
                "CREATE TABLE ks1.natives (c1 text primary key, c2 text, c3 timestamp, c4 int, c5 bigint, c6 double, c7 float, c8 boolean, c9 blob, c10 uuid, c11 timeuuid)");
        assertAcked(client().admin().indices()
                .preparePutMapping("ks1")
                .setType("natives")
                .setSource("{ \"natives\" : { \"discover\" : \".*\", \"properties\": { \"c2\":{ \"type\":\"string\", \"index\":\"not_analyzed\" }}}}")
                .get());
        
        // {"c2": "toto", "c3" : "2016-10-10", "c4": 1, "c5":44, "c6":1.0, "c7":2.22, "c8": true, "c9":"U29tZSBiaW5hcnkgYmxvYg==" }
        assertThat(client().prepareIndex("ks1", "natives", "1")
                .setSource("{\"c2\": \"toto\", \"c3\" : \"2016-10-10\", \"c4\": 1, \"c5\":44, \"c6\":1.0, \"c7\":2.22, \"c8\": true, \"c9\":\"U29tZSBiaW5hcnkgYmxvYg==\", \"c10\":\"ae8c9260-dd02-11e6-b9d5-bbfb41c263ba\",\"c11\":\"ae8c9260-dd02-11e6-b9d5-bbfb41c263ba\"  }")
                .get().isCreated(), equalTo(true));
        Map<String,Object> fields = client().prepareSearch("ks1").setTypes("natives").setQuery(QueryBuilders.queryStringQuery("c2:toto"))
                .get().getHits().getHits()[0]
                .getSource();
        assertThat(fields.get("c2"),equalTo("toto"));
        assertThat(fields.get("c3").toString(),equalTo("2016-10-10T00:00:00.000Z"));
        assertThat(fields.get("c4"),equalTo(1));
        assertThat(fields.get("c5"),equalTo(44));
        assertThat(fields.get("c6"),equalTo(1.0));
        assertThat(fields.get("c7"),equalTo(2.22));
        assertThat(fields.get("c8"),equalTo(true));
        assertThat(fields.get("c9"),equalTo("U29tZSBiaW5hcnkgYmxvYg=="));
        
        process(ConsistencyLevel.ONE,"insert into ks1.natives (c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11) VALUES ('tutu', 'titi', '2016-11-11', 1, 45, 1.0, 2.23, false,textAsBlob('bdb14fbe076f6b94444c660e36a400151f26fc6f'),ae8c9260-dd02-11e6-b9d5-bbfb41c263ba,ae8c9260-dd02-11e6-b9d5-bbfb41c263ba)");
        assertThat(client().prepareSearch().setIndices("ks1").setTypes("natives").setQuery(QueryBuilders.queryStringQuery("*:*")).get().getHits().getTotalHits(), equalTo(2L));
        
        fields = client().prepareSearch().setIndices("ks1").setTypes("natives").setQuery(QueryBuilders.queryStringQuery("c5:45")).get().getHits().getHits()[0].getSource();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        sdf.setTimeZone(TimeZone.getTimeZone(System.getProperty("tests.timezone")));
        
        assertThat(fields.get("c2"), equalTo("titi"));
        //assertThat(fields.get("c3"), equalTo(new SimpleDateFormat("yyyy-MM-dd").parse("2016-11-11").toLocaleString()));
        assertThat(fields.get("c4"),equalTo(1));
        assertThat(fields.get("c5"),equalTo(45));
        assertThat(fields.get("c6"),equalTo(1.0));
        assertThat(fields.get("c7"),equalTo(2.23));
        assertThat(fields.get("c8"),equalTo(false));
    }

    @Test
    public void testTextGeohashMapping() throws Exception {
        createIndex("test");
        ensureGreen("test");
        
        process(ConsistencyLevel.ONE,"create type test.geo_point (lat double, lon double);");
        process(ConsistencyLevel.ONE,"create table test.geoloc (geohash text, id uuid, coord frozen<geo_point>, comment text, primary key ((geohash),id));");
        assertAcked(client().admin().indices().preparePutMapping("test").setType("geoloc")
                .setSource("{ \"geoloc\" : { \"discover\":\"^((?!geohash).*)\", \"properties\": { \"geohash\": { \"type\": \"geo_point\", \"cql_collection\":\"singleton\",\"cql_partition_key\" : true,\"cql_primary_key_order\" : 0, \"index\" : \"not_analyzed\" } }}}").get());
        GeoPoint geo_point = new GeoPoint(-25.068403, 29.411767);
        ByteBuffer[] elements = new ByteBuffer[] {
                InternalCassandraClusterService.serialize("test", "geoloc", DoubleType.instance, GeoPointFieldMapper.Names.LAT, -25.068403, null),
                InternalCassandraClusterService.serialize("test", "geoloc", DoubleType.instance, GeoPointFieldMapper.Names.LON, 29.411767, null)
        };
        process(ConsistencyLevel.ONE,"INSERT INTO test.geoloc (geohash, id, coord, comment) VALUES (?,?,?,?)",
                geo_point.geohash(), UUID.randomUUID(), TupleType.buildValue(elements), "blabla");
        SearchResponse rsp = client().prepareSearch().setIndices("test").setTypes("geoloc")
                .setQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchAllQuery())
                    .filter(QueryBuilders.geoDistanceQuery("geohash").distance("20km").point(-25.068403, 29.411767)))
                .get();
        assertThat(rsp.getHits().getTotalHits(),equalTo(1L));
    }
    
    // #74 test
    @Test
    public void testUUID() throws Exception {
        createIndex("test");
        ensureGreen("test");
        
        process(ConsistencyLevel.ONE,"create table test.pk_uuid (pk_uuid uuid, column_not_uuid text, primary key(pk_uuid));");
        process(ConsistencyLevel.ONE,"create table test.pk_not_uuid (pk_not_uuid text, column_uuid uuid, primary key(pk_not_uuid));");
        
        assertAcked(client().admin().indices().preparePutMapping("test").setType("pk_uuid").setSource("{ \"pk_uuid\" : { \"discover\" : \".*\"}}").get());
        assertAcked(client().admin().indices().preparePutMapping("test").setType("pk_not_uuid").setSource("{ \"pk_not_uuid\" : { \"discover\" : \".*\"}}").get());
        
        assertThat(client().prepareIndex("test", "pk_uuid", "bacc6c75-91b8-4a86-a408-ff7bafac535d").setSource("{ \"column_not_uuid\": \"a value\" }").get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("test", "pk_uuid", "bacc6c75-91b8-4a86-a408-ff7bafac535d").setSource("{ \"column_not_uuid\": \"a value\", \"pk_uuid\": \"bacc6c75-91b8-4a86-a408-ff7bafac535d\" }").get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("test", "pk_not_uuid", "pk2").setSource("{ \"column_uuid\": \"bacc6c75-91b8-4a86-a408-ff7bafac535d\" }").get().isCreated(), equalTo(true));
        assertThat(client().prepareIndex("test", "pk_not_uuid", "pk2").setSource("{ \"column_uuid\": \"bacc6c75-91b8-4a86-a408-ff7bafac535d\", \"pk_not_uuid\":\"pk2\" }").get().isCreated(), equalTo(true));
    }
}
