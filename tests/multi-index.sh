curl -XPUT "http://$NODE:9200/twitter/" -d '{ "number_of_replicas" : 0 } }'
curl -XPUT "http://$NODE:9200/twitter/tweet/1" -d '{
    "user" : "bob",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "look at Elassandra !!",
    "size": 50
}'
curl -XPUT "http://$NODE:9200/twitter/tweet/2" -d '{
    "user" : "alice",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "look at Elassandra !!",
    "size" : 200
}'
curl -XPUT "http://$NODE:9200/twitter/tweet/3" -d '{
    "user" : "bob",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elassandra !!",
    "size" : 150
}'
curl -XPUT "http://$NODE:9200/twitter/tweet/4" -d '{
    "user" : "dave",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "look at Elassandra !!",
    "size": 100
}'

sleep 2
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=message:Elassandra"
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&preference=_only_local" -d '{"fields" : ["message","_token","post_date","user"],"query":{"match":{"message":"Elassandra" }}}'

curl -XPOST "http://$NODE:9200/twitter/_flush"


curl -XPUT "http://$NODE:9200/twitter2/" -d '{ "settings" : { "keyspace" : "twitter" } }'
curl -XPUT "http://$NODE:9200/twitter2/_mapping/tweet" -d '
{ 
    "tweet" : {
            "properties" : {
              "message" : {
                "type" : "string",
                "index" : "not_analyzed"
              },
              "user" : {
                "type" : "string",
                "index" : "not_analyzed"
              },
              "size" : {
                "type" : "long"
              },
              "post_date" : {
                "format": "yyyy-MM-dd",
                "type" : "date"
              }
            }
         }
}'


curl -XGET "http://$NODE:9200/twitter2/_search?pretty=true&q=user:bob"
curl -XGET "http://$NODE:9200/twitter2/_search?pretty=true" -d '{"fields" : ["message","_token","post_date","user"],"query":{"match":{"user":"bob" }}}'

curl -XPUT "http://$NODE:9200/twitter2/tweet/5" -d '{
    "user" : "robert",
    "post_date" : "2009-11-15",
    "message" : "look at Elassandra !!",
    "size": 100
}'

curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=user:robert"

curl -XPOST "http://$NODE:9200/_aliases" -d '{ "actions" : [ { "add" : { "index" : "twitter2", "alias" : "twitter" } } ] }'
curl -XDELETE "http://$NODE:9200/twitter"



