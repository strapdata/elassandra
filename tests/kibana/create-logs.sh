curl -XPUT http://$NODE:9200/logstash_20150518 -d '
{
  "mappings" : {
    "log" : {
     "properties" : {
     "geo" : {
       "properties" : {
       "coordinates" : {
         "type" : "geo_point"
        }
       }
      }
    }
    }
  }
 }'
curl -XPUT http://$NODE:9200/logstash_20150519 -d '
{
  "mappings" : {
    "log" : {
     "properties" : {
     "geo" : {
       "properties" : {
       "coordinates" : {
         "type" : "geo_point"
        }
       }
      }
    }
    }
  }
 }'
curl -XPUT http://$NODE:9200/logstash_20150520 -d '
{
  "mappings" : {
    "log" : {
     "properties" : {
     "geo" : {
       "properties" : {
       "coordinates" : {
         "type" : "geo_point"
        }
       }
      }
    }
    }
  }
 }'
curl -XPUT http://$NODE:9200/shakespeare -d '
{
 "mappings" : {
  "_default_" : {
   "properties" : {
    "speaker" : {"type": "string", "index" : "not_analyzed" },
    "play_name" : {"type": "string", "index" : "not_analyzed" },
    "line_id" : { "type" : "integer" },
    "speech_number" : { "type" : "integer" }
   }
  }
 }
}
';

#curl -XPOST "$NODE:9200/accounts/account/_bulk?pretty" --data-binary @accounts.json
#curl -XPOST "$NODE:9200/shakespeare/_bulk?pretty" --data-binary @shakespeare.json
curl -XPOST "$NODE:9200/_bulk?pretty" --data-binary @logs.jsonl10
