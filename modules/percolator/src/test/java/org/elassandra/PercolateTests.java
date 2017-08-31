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

import org.elasticsearch.percolator.PercolateResponse;
import org.elasticsearch.percolator.PercolatorPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.hamcrest.Matchers.equalTo;

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

/**
 * Elassandra precolate tests.
 * @author vroyer
 *
 */
public class PercolateTests extends ESSingleNodeTestCase {
    
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(PercolatorPlugin.class);
    }
    
    
    public void percolateTest() throws Exception {
        assertAcked(client().admin().indices().prepareCreate("my_index")
                .addMapping("my_type", "{ \"my_type\" :{ \"properties\": { \"message\": { \"type\": \"string\" },  \"created_at\" : { \"type\": \"date\" } } }")
                .get());
        ensureGreen("my_index");
        
        assertThat(client().prepareIndex("my_index", ".percolator", "1")
                .setSource("{ \"query\" : { \"match\" : { \"message\" : \"bonsai tree\"  } } }")
                .get().getResult().getLowercase(), equalTo("created"));
        
        assertThat(client().prepareIndex("my_index", ".percolator", "2")
                .setSource("{ \"query\" : { \"match\" : { \"message\" : \"bonsai tree\"  } }, \"priority\" : \"high\" }")
                .get().getResult().getLowercase(), equalTo("created"));
        
        assertThat(client().prepareIndex("my_index", ".percolator", "3")
                .setSource("{ \"query\" : { \"range\" : { \"created_at\" : { \"gte\" : \"2010-01-01T00:00:00\", \"lte\" : \"2011-01-01T00:00:00\" } } }, \"type\" : \"tweet\", \"priority\" : \"high\" }")
                .get().getResult().getLowercase(), equalTo("created"));
        
        
        /*
        PercolateResponse rsp1 = client().preparePercolate().setIndices("my_index").setDocumentType("my_type")
                .setSource("{ \"doc\" : { \"message\" : \"A new bonsai tree in the office\" }}").get();
        assertThat(rsp1.getMatches().length, equalTo(2L) );
        
        PercolateResponse rsp2 = client().preparePercolate().setIndices("my_index").setDocumentType("my_type")
                .setSource("{ \"doc\" : { \"message\" : \"A new bonsai tree in the office\" }, \"filter\" : { \"term\" : { \"priority\" : \"high\" } }}").get();
        assertThat(rsp2.getMatches().length, equalTo(1L) );
        */
    
    }
}
