Operations
==========

Indexing
________

Let's try and index some *twitter* like information (demo from `Elasticsearch <https://github.com/elastic/elasticsearch/blob/master/README.textile>`_)). First, let's create a twitter user, and add some tweets (the *twitter* index will be created automatically):

```
curl -XPUT 'http://localhost:9200/twitter/user/kimchy' -d '{ "name" : "Shay Banon" }'
curl -XPUT 'http://localhost:9200/twitter/tweet/1' -d '
{
    "user": "kimchy",
    "postDate": "2009-11-15T13:12:00",
    "message": "Trying out Elassandra, so far so good?"
}'
curl -XPUT 'http://localhost:9200/twitter/tweet/2' -d '
{
    "user": "kimchy",
    "postDate": "2009-11-15T14:12:12",
    "message": "Another tweet, will it be indexed?"
}'
```

You now have two rows in the cassandra *twitter.tweet* table.
```
cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 2.1.8 | CQL spec 3.2.0 | Native protocol v3]
Use HELP for help.
cqlsh> select * from twitter.tweet;
 _id | message                                    | postDate                     | user
-----+--------------------------------------------+------------------------------+------------
   2 |     ['Another tweet, will it be indexed?'] | ['2009-11-15 15:12:12+0100'] | ['kimchy']
   1 | ['Trying out Elassandra, so far so good?'] | ['2009-11-15 14:12:00+0100'] | ['kimchy']
(2 rows)

cqlsh> describe table twitter.tweet;
CREATE TABLE twitter.tweet (
    "_id" text PRIMARY KEY,
    message list<text>,
    "postDate" list<timestamp>,
    user list<text>
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Auto-created by Elassandra'
    AND compaction = {'min_threshold': '4', 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
CREATE CUSTOM INDEX elastic_tweet_message_idx ON twitter.tweet (message) USING 'org.elasticsearch.cassandra.index.ElasticSecondaryIndex';
CREATE CUSTOM INDEX elastic_tweet_postDate_idx ON twitter.tweet ("postDate") USING 'org.elasticsearch.cassandra.index.ElasticSecondaryIndex';
CREATE CUSTOM INDEX elastic_tweet_user_idx ON twitter.tweet (user) USING 'org.elasticsearch.cassandra.index.ElasticSecondaryIndex';
```

Now, let's see if the information was added by GETting it:
```
curl -XGET 'http://localhost:9200/twitter/user/kimchy?pretty=true'
curl -XGET 'http://localhost:9200/twitter/tweet/1?pretty=true'
curl -XGET 'http://localhost:9200/twitter/tweet/2?pretty=true'
```

Elasticsearch state now show reflect the new twitter index. Because we are currently running on one node, the *token_ranges* routing 
attribute match 100% of the ring Long.MIN_VALUE to Long.MAX_VALUE.
```
curl -XGET 'http://localhost:9200/_cluster/state/?pretty=true'
{
  "cluster_name" : "Test Cluster",
  "version" : 5,
  "master_node" : "74ae1629-0149-4e65-b790-cd25c7406675",
  "blocks" : { },
  "nodes" : {
    "74ae1629-0149-4e65-b790-cd25c7406675" : {
      "name" : "localhost",
      "status" : "ALIVE",
      "transport_address" : "inet[localhost/127.0.0.1:9300]",
      "attributes" : {
        "data" : "true",
        "rack" : "RAC1",
        "data_center" : "DC1",
        "master" : "true"
      }
    }
  },
  "metadata" : {
    "version" : 3,
    "uuid" : "74ae1629-0149-4e65-b790-cd25c7406675",
    "templates" : { },
    "indices" : {
      "twitter" : {
        "state" : "open",
        "settings" : {
          "index" : {
            "creation_date" : "1440659762584",
            "uuid" : "fyqNMDfnRgeRE9KgTqxFWw",
            "number_of_replicas" : "1",
            "number_of_shards" : "1",
            "version" : {
              "created" : "1050299"
            }
          }
        },
        "mappings" : {
          "user" : {
            "properties" : {
              "name" : {
                "type" : "string"
              }
            }
          },
          "tweet" : {
            "properties" : {
              "message" : {
                "type" : "string"
              },
              "postDate" : {
                "format" : "dateOptionalTime",
                "type" : "date"
              },
              "user" : {
                "type" : "string"
              }
            }
          }
        },
        "aliases" : [ ]
      }
    }
  },
  "routing_table" : {
    "indices" : {
      "twitter" : {
        "shards" : {
          "0" : [ {
            "state" : "STARTED",
            "primary" : true,
            "node" : "74ae1629-0149-4e65-b790-cd25c7406675",
            "token_ranges" : [ "(-9223372036854775808,9223372036854775807]" ],
            "shard" : 0,
            "index" : "twitter"
          } ]
        }
      }
    }
  },
  "routing_nodes" : {
    "unassigned" : [ ],
    "nodes" : {
      "74ae1629-0149-4e65-b790-cd25c7406675" : [ {
        "state" : "STARTED",
        "primary" : true,
        "node" : "74ae1629-0149-4e65-b790-cd25c7406675",
        "token_ranges" : [ "(-9223372036854775808,9223372036854775807]" ],
        "shard" : 0,
        "index" : "twitter"
      } ]
    }
  },
  "allocations" : [ ]
}
```

Searching
_________

Let's find all the tweets that *kimchy* posted:

```
curl -XGET 'http://localhost:9200/twitter/tweet/_search?q=user:kimchy&pretty=true'
```

We can also use the JSON query language Elasticsearch provides instead of a query string:

```
curl -XGET 'http://localhost:9200/twitter/tweet/_search?pretty=true' -d '
{
    "query" : {
        "match" : { "user": "kimchy" }
    }
}'
```

Just for kicks, let's get all the documents stored (we should see the user as well):

```
curl -XGET 'http://localhost:9200/twitter/_search?pretty=true' -d '
{
    "query" : {
        "matchAll" : {}
    }
}'
```

We can also do range search (the 'postDate' was automatically identified as date)

```
curl -XGET 'http://localhost:9200/twitter/_search?pretty=true' -d '
{
    "query" : {
        "range" : {
            "postDate" : { "from" : "2009-11-15T13:00:00", "to" : "2009-11-15T14:00:00" }
        }
    }
}'
```

There are many more options to perform search, after all, it's a search product no? All the familiar Lucene queries are available through the JSON query language, or through the query parser.
