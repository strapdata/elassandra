curl -XPUT "http://$NODE:9200/twitter/" -d '{ "index.secondary_index_class" : "org.elasticsearch.cassandra.index.ThreadLocalOptimizedElasticSecondaryIndex" }'

curl -XPUT "http://$NODE:9200/twitter/tweet/1?consistency=one" -d '{
    "user" : "vince",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "look at Elassandra !!",
    "size": 50
}'
curl -XPUT "http://$NODE:9200/twitter/tweet/2?consistency=one" -d '{
    "user" : "vince",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "look at Elassandra !!",
    "size" : 200
}'
curl -XPUT "http://$NODE:9200/twitter/tweet/3" -d '{
    "user" : "silas",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elassandra !!",
    "size" : 150
}'
curl -XPUT "http://$NODE:9200/twitter/tweet/4" -d '{
    "user" : "emile",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "look at Elassandra !!",
    "size": 100
}'
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=message:elassandra"

echo "echo \"select * from twitter.tweet;\" | cqlsh $NODE" 
echo "select * from twitter.tweet;" | bin/cqlsh $NODE
echo curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=message:elassandra"
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=message:elassandra"

curl -XDELETE "http://$NODE:9200/twitter/tweet/1"
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=message:elassandra"


curl -XGET "http://$NODE:9200/pat1_metadata/_search?pretty=true&q=*:*"