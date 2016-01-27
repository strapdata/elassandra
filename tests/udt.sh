# Inner document test.
curl -XPUT "http://$NODE:9200/twitter/tweet2/1" -d '{
    "message": "elasticassandra inner objects 1",
    "user" : {  "first" : "John",  "last" :  "Smith"  }
}'

curl -XPUT "http://$NODE:9200/twitter/tweet2/2" -d '{
    "message": "elasticassandra inner objects 2",
    "user" : [ {  "first" : "John",  "last" :  "Smith"  },
               {  "first" : "Alice", "last" :  "White"  } ]
}'

curl -XGET "http://$NODE:9200/twitter/tweet2/1?pretty=true"
curl -XGET "http://$NODE:9200/twitter/tweet2/2?fields=message,user&pretty=true"
curl -XGET "http://$NODE:9200/twitter/tweet2/2/_source"
sleep 1

curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{"query":{"match":{"message":"inner" }}}'
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{"fields" : ["user.first","user.last"],"query":{"match":{"message":"inner" }}}'

# Nested document test
curl -XPUT "http://$NODE:9200/twitter/_mapping/tweet3" -d '{
        "tweet3" : {
            "properties" : {
              "message" : { "type" : "string", "cql_partial_update": true },
              "user" : {
                "type": "nested",
                "cql_partial_update": true,
                "properties" : {
                  "last" : { "type" : "string" },
                  "first" : { "type" : "string" }
                }
              }
            }
        }
}'
curl -XPUT "http://$NODE:9200/twitter/tweet3/1" -d '{
    "message": "elasticassandra nested objects 1",
    "user" : {  "first" : "John",  "last" :  "Smith"  }
}'

curl -XPUT "http://$NODE:9200/twitter/tweet3/2" -d '{
    "message": "elasticassandra nested objects 2",
    "user" : [ {  "first" : "John",  "last" :  "Smith"  },
               {  "first" : "Alice", "last" :  "White"  } ]
}'
curl -XGET "http://$NODE:9200/twitter/tweet3/1?pretty=true"
curl -XGET "http://$NODE:9200/twitter/tweet3/2?fields=message,user.last&pretty=true"
curl -XGET "http://$NODE:9200/twitter/tweet3/2/_source"
sleep 1
curl -XGET "http://$NODE:9200/twitter/tweet3/_search?pretty=true" -d '{"query":{"match":{"message":"nested" }}}'
curl -XGET "http://$NODE:9200/twitter/tweet3/_search?pretty=true" -d '{"fields" : ["user.first","user.last"],"query":{"match":{"message":"nested" }}}'
curl -XGET "http://$NODE:9200/twitter/tweet3/_search?pretty=true" -d '{ "query":{"match_all":{ }}}'


# Nested document test
curl -XPUT "http://$NODE:9200/my_index/" -d '{ "settings" : { "number_of_replicas" : 0 } }'
curl -XPUT "http://$NODE:9200/my_index/_mapping/blogpost" -d '{
        "blogpost" : {
            "properties" : {
              "tags" : {
                "type" : "string"
              },
              "body" : {
                "type" : "string"
              },
              "title" : {
                "type" : "string"
              },
              "comments" : {
                "type": "nested",
                "properties" : {
                  "stars" : {
                    "type" : "long"
                  },
                  "name" : {
                    "type" : "string"
                  },
                  "age" : {
                    "type" : "long"
                  },
                  "date" : {
                    "format" : "dateOptionalTime",
                    "type" : "date"
                  },
                  "comment" : {
                    "type" : "string"
                  }
              }
           }
        }
     }
}'

curl -XPUT "http://$NODE:9200/my_index/blogpost/1" -d '
{
  "title": "Nest eggs",
  "body":  "Making your money work...",
  "tags":  [ "cash", "shares" ],
  "comments": [ 
    {
      "name":    "John Smith",
      "comment": "Great article",
      "age":     28,
      "stars":   4,
      "date":    "2014-09-01"
    },
    {
      "name":    "Alice White",
      "comment": "More like this please",
      "age":     31,
      "stars":   5,
      "date":    "2014-10-22"
    }
  ]
}'
sleep 1
curl -XGET "http://$NODE:9200/my_index/blogpost/_search?pretty=true" -d '{ "query":{"match_all":{ }}}'
curl -XGET "http://$NODE:9200/my_index/blogpost/_search?pretty=true" -d '
{
  "query": {
    "bool": {
      "must": [
        { "match": { "title": "eggs" }}, 
        {
          "nested": {
            "path": "comments", 
            "query": {
              "bool": {
                "must": [ 
                  { "match": { "comments.name": "john" }},
                  { "match": { "comments.age":  28     }}
                ]
        }}}}
      ]
}}}'

curl -XGET "http://$NODE:9200/my_index/blogpost/1/_source?pretty=true"


curl -XDELETE "http://$NODE:9200/my_index"