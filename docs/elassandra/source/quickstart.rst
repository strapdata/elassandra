Quick Start
===========

Start a single node docker-based Elassandra cluster:

.. code::

   docker pull docker.io/strapdata/elassandra:latest
   docker run --name some-elassandra --rm -p 9042:9042 -p 9200:9200 -e JVM_OPTS="-Dcassandra.custom_query_handler_class=org.elassandra.index.ElasticQueryHandler" -dti docker.io/strapdata/elassandra:latest


Check the cassandra cluster status:

.. code::
   
   docker exec -i some-elassandra nodetool status
   Datacenter: DC1
   ===============
   Status=Up/Down
   |/ State=Normal/Leaving/Joining/Moving
   --  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
   UN  172.17.0.2  187.36 KiB  8            100.0%            25457162-c5ef-44fa-a46b-a96434aae319  r1


Create an Elasticsearch index from a Cassandra table
----------------------------------------------------

Use the cassandra CQLSH to create a cassandra Keyspace, a User Defined Type, a Table and add two rows:

.. code::
   
   docker exec -i some-elassandra cqlsh <<EOF
   CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1': 1};
   CREATE TYPE IF NOT EXISTS test.user_type (first text, last text);
   CREATE TABLE IF NOT EXISTS test.docs (uid int, username frozen<user_type>, login text, PRIMARY KEY (uid));
   INSERT INTO test.docs (uid, username, login) VALUES (1, {first:'vince',last:'royer'}, 'vroyer');
   INSERT INTO test.docs (uid, username, login) VALUES (2, {first:'barthelemy',last:'delemotte'}, 'barth');
   EOF


Create an Elasticsearch index from the Cassandra table schema by discovering the CQL schema:

.. code::
   
   curl -XPUT -H 'Content-Type: application/json' http://localhost:9200/test -d'{"mappings":{"docs":{"discover":".*"}}}'
   {"acknowledged":true,"shards_acknowledged":true,"index":"test"}

This command discovers all column matching the provided regular expression, and creates the Eslasticsearch index.


Create an Elasticsearch index from scratch
------------------------------------------

Elassandra automatically generates the underlying CQL schema when creating an index or updating the mapping with a new field.

.. code::
   
   curl -XPUT -H 'Content-Type: application/json' http://localhost:9200/test2 -d'{
      "mappings":{
         "docs":{
            "properties": {
               "first": {
                  "type":"text"
               },
               "last": {
                  "type":"text",
                  "cql_collection":"singleton"
               }
            }
         }
      }
   }'

Generated CQL schema:

.. code::

   cqlsh> desc KEYSPACE test2;
   
   CREATE KEYSPACE test2 WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1': '1'}  AND durable_writes = true;
   
   CREATE TABLE test2.docs (
       "_id" text PRIMARY KEY,
       first list<text>,
       last text
   ) WITH bloom_filter_fp_chance = 0.01
       AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
       AND comment = ''
       AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
       AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
       AND crc_check_chance = 1.0
       AND dclocal_read_repair_chance = 0.1
       AND default_time_to_live = 0
       AND gc_grace_seconds = 864000
       AND max_index_interval = 2048
       AND memtable_flush_period_in_ms = 0
       AND min_index_interval = 128
       AND read_repair_chance = 0.0
       AND speculative_retry = '99PERCENTILE';
   CREATE CUSTOM INDEX elastic_docs_idx ON test2.docs () USING 'org.elassandra.index.ExtendedElasticSecondaryIndex';



Search for a document
---------------------

Search for a document through the Elasticsearch API:

.. code::
   
   curl "http://localhost:9200/test/_search?pretty"
   {
     "took" : 10,
     "timed_out" : false,
     "_shards" : {
       "total" : 1,
       "successful" : 1,
       "skipped" : 0,
       "failed" : 0
     },
     "hits" : {
       "total" : 2,
       "max_score" : 1.0,
       "hits" : [
         {
           "_index" : "test",
           "_type" : "docs",
           "_id" : "1",
           "_score" : 1.0,
           "_source" : {
             "uid" : 1,
             "login" : "vroyer",
             "username" : {
               "last" : "royer",
               "first" : "vince"
             }
           }
         },
         {
           "_index" : "test",
           "_type" : "docs",
           "_id" : "2",
           "_score" : 1.0,
           "_source" : {
             "uid" : 2,
             "login" : "barth",
             "username" : {
               "last" : "delemotte",
               "first" : "barthelemy"
             }
           }
         }
       ]
     }
   }


In order to search a document through the CQL driver, add the following two dummy columns in your table schema. Then, 
execute an Elasticsearch nested query. The dummy columns allow you to specify the targeted index
when index name does not match the keyspace name.

.. code::
   
   docker exec -i some-elassandra cqlsh <<EOF
   ALTER TABLE test.docs ADD es_query text;
   ALTER TABLE test.docs ADD es_options text;
   cqlsh> SELECT uid, login, username FROM test.docs WHERE es_query='{ "query":{"nested":{"path":"username","query":{"term":{"username.first":"barthelemy"}}}}}' AND es_options='indices=test' ALLOW FILTERING;
   uid | login | username
   ----+-------+------------------------------------------
     2 | barth | {first: 'barthelemy', last: 'delemotte'}
      
   (1 rows)


Manage Elasticsearch indices
----------------------------

Get the Elasticsearch cluster state:

.. code::

   curl "http://localhost:9200/_cluster/state?pretty"
   {
     "cluster_name" : "Test Cluster",
     "compressed_size_in_bytes" : 579,
     "version" : 8,
     "state_uuid" : "mrE5raXOQO2SVA8AROJqwQ",
     "master_node" : "25457162-c5ef-44fa-a46b-a96434aae319",
     "blocks" : { },
     "nodes" : {
       "25457162-c5ef-44fa-a46b-a96434aae319" : {
         "name" : "172.17.0.2",
         "status" : "ALIVE",
         "ephemeral_id" : "25457162-c5ef-44fa-a46b-a96434aae319",
         "transport_address" : "172.17.0.2:9300",
         "attributes" : {
           "rack" : "r1",
           "dc" : "DC1"
         }
       }
     },
     "metadata" : {
       "version" : 1,
       "cluster_uuid" : "25457162-c5ef-44fa-a46b-a96434aae319",
       "templates" : { },
       "indices" : {
         "test" : {
           "state" : "open",
           "settings" : {
             "index" : {
               "creation_date" : "1553512833429",
               "number_of_shards" : "1",
               "number_of_replicas" : "0",
               "uuid" : "BOolxI89SqmrcbK7KM4sIA",
               "version" : {
                 "created" : "6020399"
               },
               "provided_name" : "test"
             }
           },
           "mappings" : {
             "docs" : {
               "properties" : {
                 "uid" : {
                   "cql_partition_key" : true,
                   "cql_primary_key_order" : 0,
                   "type" : "integer",
                   "cql_collection" : "singleton"
                 },
                 "login" : {
                   "type" : "keyword",
                   "cql_collection" : "singleton"
                 },
                 "username" : {
                   "cql_udt_name" : "user_type",
                   "type" : "nested",
                   "properties" : {
                     "last" : {
                       "type" : "keyword",
                       "cql_collection" : "singleton"
                     },
                     "first" : {
                       "type" : "keyword",
                       "cql_collection" : "singleton"
                     }
                   },
                   "cql_collection" : "singleton"
                 }
               }
             }
           },
           "aliases" : [ ],
           "primary_terms" : {
             "0" : 0
           },
           "in_sync_allocations" : {
             "0" : [ ]
           }
         }
       },
       "index-graveyard" : {
         "tombstones" : [ ]
       }
     },
     "routing_table" : {
       "indices" : {
         "test" : {
           "shards" : {
             "0" : [
               {
                 "state" : "STARTED",
                 "primary" : true,
                 "node" : "25457162-c5ef-44fa-a46b-a96434aae319",
                 "relocating_node" : null,
                 "shard" : 0,
                 "index" : "test",
                 "token_ranges" : [
                   "(-9223372036854775808,9223372036854775807]"
                 ],
                 "allocation_id" : {
                   "id" : "dummy_alloc_id"
                 }
               }
             ]
           }
         }
       }
     },
     "routing_nodes" : {
       "unassigned" : [ ],
       "nodes" : {
         "25457162-c5ef-44fa-a46b-a96434aae319" : [
           {
             "state" : "STARTED",
             "primary" : true,
             "node" : "25457162-c5ef-44fa-a46b-a96434aae319",
             "relocating_node" : null,
             "shard" : 0,
             "index" : "test",
             "token_ranges" : [
               "(-9223372036854775808,9223372036854775807]"
             ],
             "allocation_id" : {
               "id" : "dummy_alloc_id"
             }
           }
         ]
       }
     },
     "snapshots" : {
       "snapshots" : [ ]
     },
     "restore" : {
       "snapshots" : [ ]
     },
     "snapshot_deletions" : {
       "snapshot_deletions" : [ ]
     }
   }


Get Elasticsearch index information:

.. code::
   
   curl "http://localhost:9200/_cat/indices?v"
   health status index uuid                   pri rep docs.count docs.deleted store.size pri.store.size
   green  open   test  BOolxI89SqmrcbK7KM4sIA   1   0          4            0      4.1kb          4.1kb


Delete the Elasticserach index (does not delete the underlying Cassandra table by default) :

.. code::
   
   curl -XDELETE http://localhost:9200/test
   {"acknowledged":true}
