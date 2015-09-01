# Elasticassandra

Elasticassandra is a fork of [Elasticsearch](https://github.com/elastic/elasticsearch) version 1.5 modified to run on top of [Apache Cassandra](http://cassandra.apache.org) in a scalable and resilient peer-to-peer architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.

Elasticassandra supports Cassandra vnodes and scale horizontally by adding more nodes.

## Architecture

From an Elasticsearch perspective :
* An Elasticsearch cluster is a Cassandra virtual datacenter.
* Every Elasticassandra node is a master primary data node.
* Each node only index local data and acts as a primary local shard.
* Elasticsearch data is not more stored in lucene indices, but in cassandra tables. 
  * An Elasticsearch index is mapped to a cassandra keyspace, 
  * Elasticsearch document type is mapped to a cassandra table.
  * Elasticsearch document *_id* is a string representation of the cassandra primary key. 
* Elasticsearch discovery now rely on the cassandra [gossip protocol](https://wiki.apache.org/cassandra/ArchitectureGossip). When a node join or leave the cluster, or when a schema change occurs, each nodes update nodes status and its local routing table.
* Elasticsearch [gateway](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-gateway.html) now store metadata in a cassandra table and in the cassandra schema. Metadata updates are played sequentially through a [cassandra lightweight transaction](http://docs.datastax.com/en/cql/3.1/cql/cql_using/use_ltweight_transaction_t.html). Metadata UUID is the cassandra hostId of the last modifier node.
* Elasticsearch REST and java API remain unchanged (version 1.5).
* [River plugins](https://www.elastic.co/guide/en/elasticsearch/rivers/current/index.html) remain fully operational.
* Logging is now based on [logback](http://logback.qos.ch/) as cassandra.

From a Cassandra perspective :
* Columns with an ElasticSecondaryIndex are indexed in ElasticSearch.
* By default, Elasticsearch document fields are multivalued, so every field is backed by a list. Single valued document field can be mapped to a basic types by setting 'single_value: true' in our type mapping.
* Nested documents are stored using cassandra [User Defined Type](http://docs.datastax.com/en/cql/3.1/cql/cql_using/cqlUseUDT.html).
* Elasticsearch provides a JSON-REST API to cassandra.
 
## Getting Started

### Building from Source

Like Elasticsearch, Elasticassandra uses [Maven](http://maven.apache.org) for its build system.

Simply run the `mvn clean package -DskipTests` command in the cloned directory. The distribution will be created under *target/releases*.

### Installation

* Install Apache Cassandra version 2.1.8. 
* Elasticassandra is currently built from cassandra version 2.1.8 with the following minor changes :
  * org.apache.cassandra.cql3.QueryOptions includes a new static constructor forInternalCalls().
  * org.apache.cassandra.service.CassandraDaemon and StorageService to include hooks to start Elasticsearch in the boostrap process.
  * org.apache.cassandra.service.ElastiCassandraDaemon extends CassandraDaemon with Elasticsearch features.
  * To avoid classloading issue, you can remove these 3 modified classes from the cassandra-all.jar (elasticassandra-SNAPSHOT-x.x.jar contains the modified version).
```
zip -d cassandra-all-2.1.8.jar 'org/apache/cassandra/cql3/QueryOptions*'
zip -d cassandra-all-2.1.8.jar 'org/apache/cassandra/service/CassandraDaemon*'
zip -d cassandra-all-2.1.8.jar 'org/apache/cassandra/service/StorageService$*'
zip -d cassandra-all-2.1.8.jar 'org/apache/cassandra/service/StorageService.class'
```
* Add `target/elasticassandra-SNAPSHOT-x.x.jar` and all its dependencies from `target/lib` in your cassandra lib directory.
* Add `target/conf/elasticsearch.yml` in the cassandra conf directory.
* Replace your `bin/cassandra` script by the one provided in `target/bin/cassandra`. The option '-e' to start cassandra with elasticsearch.
* Run `bin/cassandra -e` to start a cassandra node including elasticsearch. 
* The cassandra logs in `logs/system.log` includes elasticsearch logs according to the your `conf/logback.conf` settings.

### Check your cluster state

Check your cassandra node status:
```
nodetool status
Datacenter: DC1
===============
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address    Load       Tokens  Owns    Host ID                               Rack
UN  127.0.0.1  57,61 KB   2       ?       74ae1629-0149-4e65-b790-cd25c7406675  RAC1
```

Check your Elasticsearch cluster state.

```
curl -XGET 'http://localhost:9200/_cluster/state/?pretty=true'
{
  "cluster_name" : "Test Cluster",
  "version" : 1,
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
    "version" : 0,
    "uuid" : "74ae1629-0149-4e65-b790-cd25c7406675",
    "templates" : { },
    "indices" : { }
  },
  "routing_table" : {
    "indices" : { }
  },
  "routing_nodes" : {
    "unassigned" : [ ],
    "nodes" : {
      "74ae1629-0149-4e65-b790-cd25c7406675" : [ ]
    }
  },
  "allocations" : [ ]
}
```

As you can see, Elasticsearch node UUID = cassandra hostId, and node attributes match your cassandra datacenter and rack. 

### Indexing

Let's try and index some *twitter* like information (demo from [Elasticsearch](https://github.com/elastic/elasticsearch/blob/master/README.textile)). First, let's create a twitter user, and add some tweets (the *twitter* index will be created automatically):

```
curl -XPUT 'http://localhost:9200/twitter/user/kimchy' -d '{ "name" : "Shay Banon" }'
curl -XPUT 'http://localhost:9200/twitter/tweet/1' -d '
{
    "user": "kimchy",
    "postDate": "2009-11-15T13:12:00",
    "message": "Trying out Elasticsearch, so far so good?"
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
 _id | message                                       | postDate                     | user
-----+-----------------------------------------------+------------------------------+------------
   2 |        ['Another tweet, will it be indexed?'] | ['2009-11-15 15:12:12+0100'] | ['kimchy']
   1 | ['Trying out Elasticsearch, so far so good?'] | ['2009-11-15 14:12:00+0100'] | ['kimchy']
(2 rows)

cqlsh> describe table twitter.tweet;
CREATE TABLE twitter.tweet (
    "_id" text PRIMARY KEY,
    message list<text>,
    "postDate" list<timestamp>,
    user list<text>
) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = 'Auto-created by ElastiCassandra'
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
CREATE CUSTOM INDEX elastic_tweet_message_idx ON twitter.tweet (message) USING 'org.elasticsearch.cassandra.ElasticSecondaryIndex';
CREATE CUSTOM INDEX elastic_tweet_postDate_idx ON twitter.tweet ("postDate") USING 'org.elasticsearch.cassandra.ElasticSecondaryIndex';
CREATE CUSTOM INDEX elastic_tweet_user_idx ON twitter.tweet (user) USING 'org.elasticsearch.cassandra.ElasticSecondaryIndex';
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

### Searching

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

### Multi Tenant - Indices and Types

Maan, that twitter index might get big (in this case, index size == valuation). Let's see if we can structure our twitter system a bit differently in order to support such large amounts of data.

Elasticsearch supports multiple indices, as well as multiple types per index. In the previous example we used an index called *twitter*, with two types, *user* and *tweet*.

Another way to define our simple twitter system is to have a different index per user (note, though that each index has an overhead). Here is the indexing curl's in this case:

```
curl -XPUT 'http://localhost:9200/kimchy/info/1' -d '{ "name" : "Shay Banon" }'

curl -XPUT 'http://localhost:9200/kimchy/tweet/1' -d '
{
    "user": "kimchy",
    "postDate": "2009-11-15T13:12:00",
    "message": "Trying out Elasticsearch, so far so good?"
}'

curl -XPUT 'http://localhost:9200/kimchy/tweet/2' -d '
{
    "user": "kimchy",
    "postDate": "2009-11-15T14:12:12",
    "message": "Another tweet, will it be indexed?"
}'
```

The above will index information into the @kimchy@ index (or keyspace), with two types (two cassandra tables), *info* and *tweet*. Each user will get his own special index.

### Shards and Replica

Unlike Elasticsearch, sharding depends on the number of nodes in the datacenter, and number of replica is defined by your keyspace [Replication Factor](http://docs.datastax.com/en/cassandra/2.0/cassandra/architecture/architectureDataDistributeReplication_c.html). Elasticsearch *numberOfShards* and *numberOfReplicas* then become meaningless. 
* When adding a new elasticassandra node, the cassandra boostrap process gets some token ranges from the existing ring and pull the corresponding data. Pulled data are automattically indexed and each node update its routing table to distribute search requests according to the ring topology. 
* When updating the Replication Factor, you will need to run a [nodetool repair <keyspace>](http://docs.datastax.com/en/cql/3.0/cql/cql_using/update_ks_rf_t.html) on the new node to effectivelly copy and index the data.
* If a node become unavailable, the routing table is updated on all nodes in order to route search requests on available nodes. The actual default strategy routes search requests on primary token ranges' owner first, then to replica nodes if available. If some token ranges become unreachable, the cluster status is red, otherwise cluster status is yellow.  

After starting a new Elasticassandra node, data and elasticsearch indices are distributed on 2 nodes (with no replication). 
```
nodetool status twitter
Datacenter: DC1
===============
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address    Load       Tokens  Owns (effective)  Host ID                               Rack
UN  127.0.0.1  156,9 KB   2       70,3%             74ae1629-0149-4e65-b790-cd25c7406675  RAC1
UN  127.0.0.2  129,01 KB  2       29,7%             e5df0651-8608-4590-92e1-4e523e4582b9  RAC2
```

The routing table now distributes search request on 2 elasticassandra nodes to cover 100% of ring.

```
curl -XGET 'http://localhost:9200/_cluster/state/?pretty=true'
{
  "cluster_name" : "Test Cluster",
  "version" : 12,
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
    },
    "e5df0651-8608-4590-92e1-4e523e4582b9" : {
      "name" : "127.0.0.2",
      "status" : "ALIVE",
      "transport_address" : "inet[127.0.0.2/127.0.0.2:9300]",
      "attributes" : {
        "data" : "true",
        "rack" : "RAC2",
        "data_center" : "DC1",
        "master" : "true"
      }
    }
  },
  "metadata" : {
    "version" : 1,
    "uuid" : "e5df0651-8608-4590-92e1-4e523e4582b9",
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
              },
              "_token" : {
                "type" : "long"
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
            "token_ranges" : [ "(-8879901672822909480,4094576844402756550]" ],
            "shard" : 0,
            "index" : "twitter"
          } ],
          "1" : [ {
            "state" : "STARTED",
            "primary" : true,
            "node" : "e5df0651-8608-4590-92e1-4e523e4582b9",
            "token_ranges" : [ "(-9223372036854775808,-8879901672822909480]", "(4094576844402756550,9223372036854775807]" ],
            "shard" : 1,
            "index" : "twitter"
          } ]
        }
      }
    }
  },
  "routing_nodes" : {
    "unassigned" : [ ],
    "nodes" : {
      "e5df0651-8608-4590-92e1-4e523e4582b9" : [ {
        "state" : "STARTED",
        "primary" : true,
        "node" : "e5df0651-8608-4590-92e1-4e523e4582b9",
        "token_ranges" : [ "(-9223372036854775808,-8879901672822909480]", "(4094576844402756550,9223372036854775807]" ],
        "shard" : 1,
        "index" : "twitter"
      } ],
      "74ae1629-0149-4e65-b790-cd25c7406675" : [ {
        "state" : "STARTED",
        "primary" : true,
        "node" : "74ae1629-0149-4e65-b790-cd25c7406675",
        "token_ranges" : [ "(-8879901672822909480,4094576844402756550]" ],
        "shard" : 0,
        "index" : "twitter"
      } ]
    }
  },
  "allocations" : [ ]
}
```

Internally, each node broadcasts its local shard status in the gossip application state X1 ( "twitter":3 stands for STARTED as defined in [ShardRoutingState](../../tree/master/src/main/java/org/elasticsearch/cluster/routing/ShardRoutingState.java)) and its current metadata UUID/version in application state X2.

```
nodetool gossipinfo
127.0.0.2/127.0.0.2
  generation:1440659838
  heartbeat:396197
  DC:DC1
  NET_VERSION:8
  SEVERITY:-1.3877787807814457E-17
  X1:{"twitter":3}
  X2:e5df0651-8608-4590-92e1-4e523e4582b9/1
  RELEASE_VERSION:2.1.8
  RACK:RAC2
  STATUS:NORMAL,-8879901672822909480
  SCHEMA:ce6febf4-571d-30d2-afeb-b8db9d578fd1
  INTERNAL_IP:127.0.0.2
  RPC_ADDRESS:127.0.0.2
  LOAD:131314.0
  HOST_ID:e5df0651-8608-4590-92e1-4e523e4582b9
localhost/127.0.0.1
  generation:1440659739
  heartbeat:396550
  DC:DC1
  NET_VERSION:8
  SEVERITY:2.220446049250313E-16
  X1:{"twitter":3}
  X2:e5df0651-8608-4590-92e1-4e523e4582b9/1
  RELEASE_VERSION:2.1.8
  RACK:RAC1
  STATUS:NORMAL,-4318747828927358946
  SCHEMA:ce6febf4-571d-30d2-afeb-b8db9d578fd1
  RPC_ADDRESS:127.0.0.1
  INTERNAL_IP:127.0.0.1
  LOAD:154824.0
  HOST_ID:74ae1629-0149-4e65-b790-cd25c7406675
```

## Contribute

Contributors are welcome to test and enhance Elasticassandra to make it production ready.

## License

```
This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

Copyright 2015, Vincent Royer (vroyer@vroyer.org).

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
