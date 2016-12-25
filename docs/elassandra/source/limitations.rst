Breaking changes and limitations
================================

Cannot index document with empty mapping
----------------------------------------

Elassandra cannot index any document for a type having no mapped properties and no underlying clustering key because Cassandra connot create a secondary index 
on the partition key and there is no other indexed columns. Exemple :

.. code::

   $ curl -XPUT "$NODE:9200/foo/bar/1?pretty" -d'{}'
   {
     "_index" : "foo",
     "_type" : "bar",
     "_id" : "1",
     "_version" : 1,
     "_shards" : {
       "total" : 1,
       "successful" : 1,
       "failed" : 0
     },
     "created" : true
   }

The underlying cassandra table foo.bar has only a primary key column with no secondary index. So, search operations won't return any result.

.. code::

   cqlsh> desc KEYSPACE foo ;
   
   CREATE KEYSPACE foo WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1': '1'}  AND durable_writes = true;
   
   CREATE TABLE foo.bar (
       "_id" text PRIMARY KEY
   ) WITH bloom_filter_fp_chance = 0.01
       AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
       AND comment = 'Auto-created by Elassandra'
       AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
       AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
       AND dclocal_read_repair_chance = 0.1
       AND default_time_to_live = 0
       AND gc_grace_seconds = 864000
       AND max_index_interval = 2048
       AND memtable_flush_period_in_ms = 0
       AND min_index_interval = 128
       AND read_repair_chance = 0.0
       AND speculative_retry = '99.0PERCENTILE';

   cqlsh> SELECT * FROM foo.bar ;
   
    _id
   -----
      1
   
   (1 rows)

Document version is meaningless
-------------------------------

Internal ``_version`` become meaningless because the same data can be reindexed many times when a Cassandra compaction occurs. 

Index and type names
--------------------

Because cassandra does not support special caraters in keyspace and table names, Elassandra automatically replaces dot (.) and dash (-) caraters 
by underscore (_) in index and type names to create underlying Cassandra keyspace and table.
When such a modification occurs, Elassandra keeps this change in memory to correctly convert keyspace/table to index/type.

Morever, Cassandra table names are limited to 48 caraters, so type names is also limted to 48 caraters.

Elasticsearch unsupported feature
---------------------------------

* Tribe node allows to query multiple Elasticsearch clusters. This feature is not currently supported by Elassandra.
* Snapshot and Restore operations are diabled (See backup and restore in operations). 

Cassandra limitations
---------------------

 * Thrift protocol is not supported by Elassandra, only CQL3.
 * CQL3 *TRUNCATE* has not effect on elasticsearch indices.
 * Elassandra synchronously indexes rows into Elasticsearch. This may increases the write duration, particulary when indexing complex document like `GeoShape <https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html>`_, so Cassandra ``write_request_timeout_in_ms`` is set to 5 seconds (Cassandra default is 2000 ms, see `Cassandra config <https://docs.datastax.com/en/cassandra/2.1/cassandra/configuration/configCassandra_yaml_r.html>`_)
 * In order to avoid concurrent mapping updates, Elassandra plays a PAXOS transaction that require QUORUM available nodes for the keyspace *elastic_admin* to succeed. So it is recommanded to have at least 3 nodes in 3 distincts racks (2 nodes datacenter won't accept any mapping update when a node is unavailable).




