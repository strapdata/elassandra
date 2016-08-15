curl -XPUT "http://$NODE:9200/twitter/" -d '{ "settings" : { "index.include_node":true }}'

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

curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=user:vince"

echo "echo \"select * from twitter.tweet;\" | cqlsh $NODE" 
echo "select * from twitter.tweet;" | bin/cqlsh $NODE
echo curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=user:vince"
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=user:vince"

curl -XDELETE "http://$NODE:9200/twitter/tweet/1"
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true&q=user:vince"


curl -XPUT "http://$NODE:9200/twitter3/" -d '{
    "settings": { "keyspace":"twitter" },
    "mappings": { "tweet": { "discover":".*"} }
}'

curl -XGET "http://$NODE:9200/twitter3/_search?pretty=true&q=*:*"