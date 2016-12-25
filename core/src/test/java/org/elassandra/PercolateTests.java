package org.elassandra;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

import org.apache.cassandra.db.ConsistencyLevel;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.percolate.PercolateSourceBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ESSingleNodeTestCase;

/*
 curl -XPUT "localhost:9200/my_index" -d '{
  "mappings": {
    "my_type": {
      "properties": {
        "message": { "type": "string" },
        "created_at" : { "type": "date" }
      }
    }
  }
}'

curl -XPUT "localhost:9200/my_index/.percolator/1" -d '{
    "query" : {
        "match" : {
            "message" : "bonsai tree"
        }
    }
}'

curl -XPUT "localhost:9200/my_index/.percolator/2" -d '{
    "query" : {
        "match" : {
            "message" : "bonsai tree"
        }
    },
    "priority" : "high"
}'

curl -XPUT "localhost:9200/my_index/.percolator/3" -d '{
        "query" : {
                "range" : {
                        "created_at" : {
                                "gte" : "2010-01-01T00:00:00",
                                "lte" : "2011-01-01T00:00:00"
                        }
                }
        },
        "type" : "tweet",
        "priority" : "high"
}'


curl -XGET 'localhost:9200/my_index/my_type/_percolate?pretty=true' -d '{
    "doc" : {
        "message" : "A new bonsai tree in the office"
    }
}'

curl -XGET 'localhost:9200/my_index/my_type/_percolate?pretty=true' -d '{
    "doc" : {
        "message" : "A new bonsai tree in the office"
    },
    "filter" : {
        "term" : {
            "priority" : "high"
        }
    }
}'
 */
public class PercolateTests extends ESSingleNodeTestCase {
    
    public void percolateTest() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("my_index")
                .addMapping("my_type", "{ \"my_type\" :{ \"properties\": { \"message\": { \"type\": \"string\" },  \"created_at\" : { \"type\": \"date\" } } }")
                .get());
        ensureGreen("my_index");
        
        assertThat(client().prepareIndex("my_index", ".percolator", "1")
                .setSource("{ \"query\" : { \"match\" : { \"message\" : \"bonsai tree\"  } } }")
                .get().isCreated(), equalTo(true));
        
        assertThat(client().prepareIndex("my_index", ".percolator", "2")
                .setSource("{ \"query\" : { \"match\" : { \"message\" : \"bonsai tree\"  } }, \"priority\" : \"high\" }")
                .get().isCreated(), equalTo(true));
        
        assertThat(client().prepareIndex("my_index", ".percolator", "3")
                .setSource("{ \"query\" : { \"range\" : { \"created_at\" : { \"gte\" : \"2010-01-01T00:00:00\", \"lte\" : \"2011-01-01T00:00:00\" } } }, \"type\" : \"tweet\", \"priority\" : \"high\" }")
                .get().isCreated(), equalTo(true));
        
        
        
        PercolateResponse rsp1 = client().preparePercolate().setIndices("my_index").setDocumentType("my_type")
                .setSource("{ \"doc\" : { \"message\" : \"A new bonsai tree in the office\" }}").get();
        assertThat(rsp1.getMatches().length, equalTo(2L) );
        
        PercolateResponse rsp2 = client().preparePercolate().setIndices("my_index").setDocumentType("my_type")
                .setSource("{ \"doc\" : { \"message\" : \"A new bonsai tree in the office\" }, \"filter\" : { \"term\" : { \"priority\" : \"high\" } }}").get();
        assertThat(rsp2.getMatches().length, equalTo(1L) );
    
    }
}
