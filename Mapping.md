# Elasticsearch document mapping

Like Elasticsearch, Elassandra support schema-less dynamic addition of unmapped fields. Here is the mapping from Elasticsearch field basic types to cassandra colums types :

Elasticearch Types | CQL Types | Comment
--- | --- | ---
string | test |
integer, short, byte | timestamp |
long | bigint | 
double | double | 
float | float | 
boolean | boolean | 
binary | blob | 
ip | inet | Internet address
geo_point | UDT geo_point | Built-In User Defined Type
geo_shape | UDT geo_shape | **Not yet implemented**
object, nested | Custom User Defined Type

Elasticsearch allows multi-value fields, so by default, a document field of type `X` is mapped to a cassandra column of type `list<X>`. Single valued document field can be mapped to a basic cassandra types by setting `single_value: true` in your type mapping.

```
curl -XPUT "http://$NODE:9200/twitter/" -d '{ "settings" : { "number_of_replicas" : 1 } }'
curl -XPUT "http://$NODE:9200/twitter/_mapping/tweet" -d '{
        "tweet" : {
            "properties" : {
                "user"    : { "type" : "string", "single_value" : "true"    },
                "message" : { "type" : "string", "single_value" : "true"    },
                "post_date" : { "type" : "date", "single_value" : "true"    },
                "size" : { "type" : "long", "single_value" : "true" }
             }
        }
}'
```
Elasticsearch mapping.

```
curl -XGET 'http://localhost:9200/_cluster/state/?pretty=true'
{
...
"metadata" : {
    "version" : 6,
    "uuid" : "cd0bbe80-1062-4ad3-b41d-860a3a49e018",
    "templates" : { },
    "indices" : {
      "twitter" : {
        "state" : "open",
        "settings" : {
          "index" : {
            "creation_date" : "1441224771216",
            "uuid" : "ubHUX_3lRSekBQIzti-eUQ",
            "number_of_replicas" : "1",
            "number_of_shards" : "1",
            "version" : {
              "created" : "1050299"
            }
          }
        },
        "mappings" : {
          "tweet" : {
            "properties" : {
              "message" : {
                "single_value" : true,
                "type" : "string"
              },
              "user" : {
                "single_value" : true,
                "type" : "string"
              },
              "size" : {
                "single_value" : true,
                "type" : "long"
              },
              "post_date" : {
                "single_value" : true,
                "format" : "dateOptionalTime",
                "type" : "date"
              }
            }
          }
        },
        "aliases" : [ ]
      }
    }
  },
...
```

The resulting Cassandra table created for index *twitter*, type *tweet*. 

```
cqlsh> describe table twitter.tweet;
CREATE TABLE twitter.tweet (
    "_id" text PRIMARY KEY,
    message text,
    post_date timestamp,
    size bigint,
    user text
)
```

## Object and Nested mapping

Elasticsearch [object or nested types](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-object-type.html) are mapped to dynamically created Cassandra User Defined Types. 

```
curl -XPUT 'http://localhost:9200/twitter/tweet/1' -d '
{
     "user" : {
         "name" : {
             "first_name" : "Vincent",
             "last_name" : "Royer"
         },
         "uid" : "12345"
     },
     "message" : "This is a tweet!"
}'

curl -XGET 'http://localhost:9200/twitter/tweet/1/_source'
{"message":"This is a tweet!","user":{"uid":["12345"],"name":[{"first_name":["Vincent"],"last_name":["Royer"]}]}}
```


The resulting cassandra user defined types and table.

```
cqlsh>describe keyspace twitter;
CREATE TYPE twitter.tweet_user (
    name frozen<list<frozen<tweet_user_name>>>,
    uid frozen<list<text>>
);

CREATE TYPE twitter.tweet_user_name (
    last_name frozen<list<text>>,
    first_name frozen<list<text>>
);

CREATE TABLE twitter.tweet (
    "_id" text PRIMARY KEY,
    message list<text>,
    person list<frozen<tweet_person>>
)

cqlsh> select * from twitter.tweet;
_id  | message              | user
-----+----------------------+-----------------------------------------------------------------------------
   1 | ['This is a tweet!'] | [{name: [{last_name: ['Royer'], first_name: ['Vincent']}], uid: ['12345']}]
```

