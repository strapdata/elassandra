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

package org.elasticsearch.search.geo;

import com.spatial4j.core.shape.Rectangle;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.EnvelopeBuilder;
import org.elasticsearch.common.geo.builders.GeometryCollectionBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.elasticsearch.test.geo.RandomShapeGenerator;

import java.io.IOException;
import java.util.Locale;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.geoIntersectionQuery;
import static org.elasticsearch.index.query.QueryBuilders.geoShapeQuery;
import static org.elasticsearch.test.geo.RandomShapeGenerator.createGeometryCollectionWithin;
import static org.elasticsearch.test.geo.RandomShapeGenerator.xRandomPoint;
import static org.elasticsearch.test.geo.RandomShapeGenerator.xRandomRectangle;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertSearchResponse;
import static org.hamcrest.Matchers.*;

public class GeoShapeQueryTests extends ESSingleNodeTestCase {
    public void testNullShape() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type1")
                .startObject("properties").startObject("location")
                .field("type", "geo_shape")
                .endObject().endObject()
                .endObject().endObject().string();
        client().admin().indices().prepareCreate("test").addMapping("type1", mapping).execute().actionGet();
        ensureGreen();

        client().prepareIndex("test", "type1", "aNullshape").setSource("{\"location\": null}").setRefresh(true)
                .execute().actionGet();
        GetResponse result = client().prepareGet("test", "type1", "aNullshape").execute().actionGet();
        assertThat(result.getField("location"), nullValue());
    }

    public void testIndexPointsFilterRectangle() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type1")
                .startObject("properties").startObject("location")
                .field("type", "geo_shape")
                .field("tree", "quadtree")
                .endObject().endObject()
                .endObject().endObject().string();
        client().admin().indices().prepareCreate("test").addMapping("type1", mapping).execute().actionGet();
        ensureGreen();

        client().prepareIndex("test", "type1", "1").setSource(jsonBuilder().startObject()
                .field("name", "Document 1")
                .startObject("location")
                .field("type", "point")
                .startArray("coordinates").value(-30).value(-30).endArray()
                .endObject()
                .endObject()).setRefresh(true).execute().actionGet();

        client().prepareIndex("test", "type1", "2").setSource(jsonBuilder().startObject()
                .field("name", "Document 2")
                .startObject("location")
                .field("type", "point")
                .startArray("coordinates").value(-45).value(-50).endArray()
                .endObject()
                .endObject()).setRefresh(true).execute().actionGet();

        ShapeBuilder shape = ShapeBuilder.newEnvelope().topLeft(-45, 45).bottomRight(45, -45);

        SearchResponse searchResponse = client().prepareSearch("test").setTypes("type1")
                .setQuery(geoIntersectionQuery("location", shape))
                .execute().actionGet();

        assertSearchResponse(searchResponse);
        assertThat(searchResponse.getHits().getTotalHits(), equalTo(1l));
        assertThat(searchResponse.getHits().hits().length, equalTo(1));
        assertThat(searchResponse.getHits().getAt(0).id(), equalTo("1"));

        searchResponse = client().prepareSearch("test").setTypes("type1")
                .setQuery(geoShapeQuery("location", shape))
                .execute().actionGet();

        assertSearchResponse(searchResponse);
        assertThat(searchResponse.getHits().getTotalHits(), equalTo(1l));
        assertThat(searchResponse.getHits().hits().length, equalTo(1));
        assertThat(searchResponse.getHits().getAt(0).id(), equalTo("1"));
    }

    public void testEdgeCases() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type1")
                .startObject("properties").startObject("location")
                .field("type", "geo_shape")
                .field("tree", "quadtree")
                .endObject().endObject()
                .endObject().endObject().string();
        client().admin().indices().prepareCreate("test").addMapping("type1", mapping).execute().actionGet();
        ensureGreen();

        client().prepareIndex("test", "type1", "blakely").setSource(jsonBuilder().startObject()
                .field("name", "Blakely Island")
                .startObject("location")
                .field("type", "polygon")
                .startArray("coordinates").startArray()
                .startArray().value(-122.83).value(48.57).endArray()
                .startArray().value(-122.77).value(48.56).endArray()
                .startArray().value(-122.79).value(48.53).endArray()
                .startArray().value(-122.83).value(48.57).endArray() // close the polygon
                .endArray().endArray()
                .endObject()
                .endObject()).setRefresh(true).execute().actionGet();

        ShapeBuilder query = ShapeBuilder.newEnvelope().topLeft(-122.88, 48.62).bottomRight(-122.82, 48.54);

        // This search would fail if both geoshape indexing and geoshape filtering
        // used the bottom-level optimization in SpatialPrefixTree#recursiveGetNodes.
        SearchResponse searchResponse = client().prepareSearch("test").setTypes("type1")
                .setQuery(geoIntersectionQuery("location", query)).execute().actionGet();

        assertSearchResponse(searchResponse);
        assertThat(searchResponse.getHits().getTotalHits(), equalTo(1l));
        assertThat(searchResponse.getHits().hits().length, equalTo(1));
        assertThat(searchResponse.getHits().getAt(0).id(), equalTo("blakely"));
    }

    public void testIndexedShapeReference() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type1")
                .startObject("properties").startObject("location")
                .field("type", "geo_shape")
                .field("tree", "quadtree")
                .endObject().endObject()
                .endObject().endObject().string();
        client().admin().indices().prepareCreate("test").addMapping("type1", mapping).execute().actionGet();
        client().admin().indices().prepareCreate("shapes").addMapping("shape_type", "_source", "enabled=true").execute().actionGet();
        ensureGreen();

        ShapeBuilder shape = ShapeBuilder.newEnvelope().topLeft(-45, 45).bottomRight(45, -45);

        client().prepareIndex("shapes", "shape_type", "Big_Rectangle").setSource(jsonBuilder().startObject()
                .field("shape", shape).endObject()).setRefresh(true).execute().actionGet();
        client().prepareIndex("test", "type1", "1").setSource(jsonBuilder().startObject()
                .field("name", "Document 1")
                .startObject("location")
                .field("type", "point")
                .startArray("coordinates").value(-30).value(-30).endArray()
                .endObject()
                .endObject()).setRefresh(true).execute().actionGet();

        SearchResponse searchResponse = client().prepareSearch("test").setTypes("type1")
                .setQuery(geoIntersectionQuery("location", "Big_Rectangle", "shape_type"))
                .execute().actionGet();

        assertSearchResponse(searchResponse);
        assertThat(searchResponse.getHits().getTotalHits(), equalTo(1l));
        assertThat(searchResponse.getHits().hits().length, equalTo(1));
        assertThat(searchResponse.getHits().getAt(0).id(), equalTo("1"));

        searchResponse = client().prepareSearch("test")
                .setQuery(geoShapeQuery("location", "Big_Rectangle", "shape_type"))
                .execute().actionGet();

        assertSearchResponse(searchResponse);
        assertThat(searchResponse.getHits().getTotalHits(), equalTo(1l));
        assertThat(searchResponse.getHits().hits().length, equalTo(1));
        assertThat(searchResponse.getHits().getAt(0).id(), equalTo("1"));
    }

    public void testReusableBuilder() throws IOException {
        ShapeBuilder polygon = ShapeBuilder.newPolygon()
                .point(170, -10).point(190, -10).point(190, 10).point(170, 10)
                .hole().point(175, -5).point(185, -5).point(185, 5).point(175, 5).close()
                .close();
        assertUnmodified(polygon);

        ShapeBuilder linestring = ShapeBuilder.newLineString()
                .point(170, -10).point(190, -10).point(190, 10).point(170, 10);
        assertUnmodified(linestring);
    }

    private void assertUnmodified(ShapeBuilder builder) throws IOException {
        String before = jsonBuilder().startObject().field("area", builder).endObject().string();
        builder.build();
        String after = jsonBuilder().startObject().field("area", builder).endObject().string();
        assertThat(before, equalTo(after));
    }
/*
curl -XPUT "$NODE:9200/shapes2/" -d'{ "mappings" : { "type" : { "_source": { "enabled":true } } } }'
curl -XPUT "$NODE:9200/shapes2/type/1" -d'{ 
    "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]}, 
    "a": { "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]} ,  
        "b" : { "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]},   
            "c" : { "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]} } 
         }
     }
}'


curl -XPUT "$NODE:9200/shapes/"
curl -XPUT "$NODE:9200/shapes/type/1" -d'{ 
    "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]}, 
    "1": { "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]} ,  
        "2" : { "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]},   
            "3" : { "location" : {"type":"polygon", "coordinates":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]} } 
         }
     }
}'

curl -XPUT "$NODE:9200/test/" -d'{ "mappings":{ "type":{"properties":{"location":{"type":"geo_shape"}}}}}'
curl -XPUT "$NODE:9200/test/type/1" -d'{ "location" : {"type":"polygon", "coordinates":[[[-20,-20],[20,-20],[20,20],[-20,20],[-20,-20]]]} }'
curl -XGET "$NODE:9200/test/_search" -d'{ 
    "query":{"match_all":{}},
    "post_filter":{
        "geo_shape":{
            "location":{"indexed_shape":{"id":"1","type":"type","index":"shapes","path":"location"},"relation":"intersects"}
            }
        }
}'

curl -XGET "$NODE:9200/test/_search?pretty" -d'{ 
    "query":{"match_all":{}},
    "post_filter":{
        "geo_shape":{
            "location":{"indexed_shape":{"id":"1","type":"type","index":"shapes2","path":"location"},"relation":"intersects"}
            }
        }
}'
 */
    public void testShapeFetchingPath() throws Exception {
        client().admin().indices().prepareCreate("shapes").addMapping("type", "_source", "enabled=true").execute().actionGet();

        client().admin().indices().prepareCreate("test").addMapping("type", "location", "type=geo_shape").execute().actionGet();

        String location = "\"location\" : {\"type\":\"polygon\", \"coordinates\":[[[-10,-10],[10,-10],[10,10],[-10,10],[-10,-10]]]}";

        client().prepareIndex("shapes", "type", "1")
                .setSource(
                        String.format(
                                Locale.ROOT, "{ %s, \"1\" : { %s, \"2\" : { %s, \"3\" : { %s } }} }", location, location, location, location
                        )
                ).setRefresh(true).execute().actionGet();
        client().prepareIndex("test", "type", "1")
                .setSource(jsonBuilder().startObject().startObject("location")
                        .field("type", "polygon")
                        .startArray("coordinates").startArray()
                        .startArray().value(-20).value(-20).endArray()
                        .startArray().value(20).value(-20).endArray()
                        .startArray().value(20).value(20).endArray()
                        .startArray().value(-20).value(20).endArray()
                        .startArray().value(-20).value(-20).endArray()
                        .endArray().endArray()
                        .endObject().endObject()).setRefresh(true).execute().actionGet();

        GeoShapeQueryBuilder filter = QueryBuilders.geoShapeQuery("location", "1", "type").relation(ShapeRelation.INTERSECTS)
                .indexedShapeIndex("shapes")
                .indexedShapePath("location");
        SearchResponse result = client().prepareSearch("test").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        filter = QueryBuilders.geoShapeQuery("location", "1", "type").relation(ShapeRelation.INTERSECTS)
                .indexedShapeIndex("shapes")
                .indexedShapePath("1.location");
        result = client().prepareSearch("test").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        filter = QueryBuilders.geoShapeQuery("location", "1", "type").relation(ShapeRelation.INTERSECTS)
                .indexedShapeIndex("shapes")
                .indexedShapePath("1.2.location");
        result = client().prepareSearch("test").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        filter = QueryBuilders.geoShapeQuery("location", "1", "type").relation(ShapeRelation.INTERSECTS)
                .indexedShapeIndex("shapes")
                .indexedShapePath("1.2.3.location");
        result = client().prepareSearch("test").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);

        // now test the query variant
        GeoShapeQueryBuilder query = QueryBuilders.geoShapeQuery("location", "1", "type")
                .indexedShapeIndex("shapes")
                .indexedShapePath("location");
        result = client().prepareSearch("test").setQuery(query).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        query = QueryBuilders.geoShapeQuery("location", "1", "type")
                .indexedShapeIndex("shapes")
                .indexedShapePath("1.location");
        result = client().prepareSearch("test").setQuery(query).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        query = QueryBuilders.geoShapeQuery("location", "1", "type")
                .indexedShapeIndex("shapes")
                .indexedShapePath("1.2.location");
        result = client().prepareSearch("test").setQuery(query).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        query = QueryBuilders.geoShapeQuery("location", "1", "type")
                .indexedShapeIndex("shapes")
                .indexedShapePath("1.2.3.location");
        result = client().prepareSearch("test").setQuery(query).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
    }
/*
 
curl -XPUT "$NODE:9200/test" -d'{"mappings" : {
          "type" : {
            "properties" : {
              "location" : {
                "tree" : "quadtree",
                "type" : "geo_shape"
              }
            },
            "_source": {
                "enabled": false
              }
          }
        }
}'

curl -XPUT "$NODE:9200/test/type/1" -d'{
 "location":[{"type":"geometrycollection","geometries":[{"type":"linestring","coordinates":[[145.02679295390183,-57.712599860735416],[142.3440620925176,-59.73546527078979],[144.23328242799516,-59.44316761674551],[142.06877684049223,-60.30577286271477],[141.99535336629168,-58.46303380714702]]},{"type":"multilinestring","coordinates":[[[144.36580200972492,-58.030239036136145],[144.69851742490562,-57.84135573024714],[140.7694586511613,-57.77517078865563],[143.88577272773648,-60.02394415618774],[144.20351239252074,-58.5871354093785]],[[140.89488937533162,-58.97329284496266],[141.87643375675754,-58.87082938290032],[143.1405263900838,-59.82065056815453],[143.2082953541793,-60.55983309147369],[141.8399858851942,-58.07003611112626],[141.56791212512013,-58.24349218137289],[143.5428586651673,-60.0106996844064]],[[144.27355687015407,-58.47412930443875],[144.62298495726444,-57.94215993113896],[141.486861263964,-59.26514919743441],[143.81875853615492,-59.033363576933056]],[[140.76706407632227,-60.479412758239135],[141.6657178205829,-58.67731821319659],[143.99735633304277,-58.62582548757713],[145.6982319711383,-59.52534060958157]],[[141.3353271823162,-60.443454029436026],[143.9143574523239,-57.8623520704006],[144.57826198137698,-59.79758922222256],[140.94031452840662,-57.962646464477345],[140.9461499423373,-57.76342726358246],[145.15092219920578,-58.12452730821416],[144.23337245960792,-57.927083089560305],[142.29599886895173,-57.540459398102975],[145.06442720669915,-58.22919912009535],[143.06934838384367,-59.97318170602256]]]}]}]
}'

 */
    public void testShapeFilterWithRandomGeoCollection() throws Exception {
        // Create a random geometry collection.
        GeometryCollectionBuilder gcb = RandomShapeGenerator.createGeometryCollection(getRandom());

        logger.info("Created Random GeometryCollection containing " + gcb.numShapes() + " shapes");

        client().admin().indices().prepareCreate("test").addMapping("type", "location", "type=geo_shape,tree=quadtree")
                .execute().actionGet();

        XContentBuilder docSource = gcb.toXContent(jsonBuilder().startObject().field("location"), null).endObject();
        client().prepareIndex("test", "type", "1").setSource(docSource).setRefresh(true).execute().actionGet();

        ShapeBuilder filterShape = (gcb.getShapeAt(randomIntBetween(0, gcb.numShapes() - 1)));

        GeoShapeQueryBuilder filter = QueryBuilders.geoShapeQuery("location", filterShape);
        filter.relation(ShapeRelation.INTERSECTS);
        SearchResponse result = client().prepareSearch("test").setTypes("type").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
    }

    public void testContainsShapeQuery() throws Exception {
        // Create a random geometry collection.
        Rectangle mbr = xRandomRectangle(getRandom(), xRandomPoint(getRandom()), true);
        GeometryCollectionBuilder gcb = createGeometryCollectionWithin(getRandom(), mbr);

        client().admin().indices().prepareCreate("test").addMapping("type", "location", "type=geo_shape,tree=quadtree" )
                .execute().actionGet();

        XContentBuilder docSource = gcb.toXContent(jsonBuilder().startObject().field("location"), null).endObject();
        client().prepareIndex("test", "type", "1").setSource(docSource).setRefresh(true).execute().actionGet();

        // index the mbr of the collection
        EnvelopeBuilder env = new EnvelopeBuilder().topLeft(mbr.getMinX(), mbr.getMaxY()).bottomRight(mbr.getMaxX(), mbr.getMinY());
        docSource = env.toXContent(jsonBuilder().startObject().field("location"), null).endObject();
        client().prepareIndex("test", "type", "2").setSource(docSource).setRefresh(true).execute().actionGet();

        ShapeBuilder filterShape = (gcb.getShapeAt(randomIntBetween(0, gcb.numShapes() - 1)));
        GeoShapeQueryBuilder filter = QueryBuilders.geoShapeQuery("location", filterShape)
                .relation(ShapeRelation.CONTAINS);
        SearchResponse response = client().prepareSearch("test").setTypes("type").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(response);

        assertThat(response.getHits().totalHits(), greaterThan(0L));
    }

    public void testShapeFilterWithDefinedGeoCollection() throws Exception {
        client().admin().indices().prepareCreate("shapes").addMapping("type", "_source", "enabled=true").execute().actionGet();
        client().admin().indices().prepareCreate("test").addMapping("type", "location", "type=geo_shape,tree=quadtree")
                .execute().actionGet();

        XContentBuilder docSource = jsonBuilder().startObject().startObject("location")
                .field("type", "geometrycollection")
                .startArray("geometries")
                .startObject()
                .field("type", "point")
                .startArray("coordinates")
                .value(100.0).value(0.0)
                .endArray()
                .endObject()
                .startObject()
                .field("type", "linestring")
                .startArray("coordinates")
                .startArray()
                .value(101.0).value(0.0)
                .endArray()
                .startArray()
                .value(102.0).value(1.0)
                .endArray()
                .endArray()
                .endObject()
                .endArray()
                .endObject().endObject();
        client().prepareIndex("test", "type", "1")
                .setSource(docSource).setRefresh(true).execute().actionGet();

        GeoShapeQueryBuilder filter = QueryBuilders.geoShapeQuery(
                "location",
                ShapeBuilder.newGeometryCollection()
                        .polygon(
                                ShapeBuilder.newPolygon().point(99.0, -1.0).point(99.0, 3.0).point(103.0, 3.0).point(103.0, -1.0)
                                        .point(99.0, -1.0))).relation(ShapeRelation.INTERSECTS);
        SearchResponse result = client().prepareSearch("test").setTypes("type").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
        filter = QueryBuilders.geoShapeQuery(
                "location",
                ShapeBuilder.newGeometryCollection().polygon(
                        ShapeBuilder.newPolygon().point(199.0, -11.0).point(199.0, 13.0).point(193.0, 13.0).point(193.0, -11.0)
                                .point(199.0, -11.0))).relation(ShapeRelation.INTERSECTS);
        result = client().prepareSearch("test").setTypes("type").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 0);
        filter = QueryBuilders.geoShapeQuery("location", ShapeBuilder.newGeometryCollection()
                .polygon(ShapeBuilder.newPolygon().point(99.0, -1.0).point(99.0, 3.0).point(103.0, 3.0).point(103.0, -1.0).point(99.0, -1.0))
                        .polygon(
                                ShapeBuilder.newPolygon().point(199.0, -11.0).point(199.0, 13.0).point(193.0, 13.0).point(193.0, -11.0)
                                        .point(199.0, -11.0))).relation(ShapeRelation.INTERSECTS);
        result = client().prepareSearch("test").setTypes("type").setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(filter).get();
        assertSearchResponse(result);
        assertHitCount(result, 1);
    }

    public void testPointsOnly() throws Exception {
        String mapping = XContentFactory.jsonBuilder().startObject().startObject("type1")
                .startObject("properties").startObject("location")
                .field("type", "geo_shape")
                .field("tree", randomBoolean() ? "quadtree" : "geohash")
                .field("tree_levels", "6")
                .field("distance_error_pct", "0.01")
                .field("points_only", true)
                .endObject().endObject()
                .endObject().endObject().string();

        client().admin().indices().prepareCreate("geo_points_only").addMapping("type1", mapping).execute().actionGet();
        ensureGreen();

        ShapeBuilder shape = RandomShapeGenerator.createShape(random());
        try {
            client().prepareIndex("geo_points_only", "type1", "1")
                    .setSource(jsonBuilder().startObject().field("location", shape).endObject())
                    .setRefresh(true).execute().actionGet();
        } catch (MapperParsingException e) {
            // RandomShapeGenerator created something other than a POINT type, verify the correct exception is thrown
            assertThat(e.getCause().getMessage(), containsString("is configured for points only"));
            return;
        }

        // test that point was inserted
        SearchResponse response = client().prepareSearch("geo_points_only").setTypes("type1")
                .setQuery(geoIntersectionQuery("location", shape))
                .execute().actionGet();

        assertEquals(1, response.getHits().getTotalHits());
    }
}
