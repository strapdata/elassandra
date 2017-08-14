Breaking changes and limitations
================================

Deleting an index does not delete cassandra data
------------------------------------------------

By default, Cassandra is considered as a primary data storage for Elasticsearch, so deleting an Elasticsearch index does not delete Cassandra content, keyspace and tables remain unchanged.
If you want to use Elassandra as Elasticsearch, you can configure your cluster or only some indices with the ``drop_on delete_index`` like this.

.. code::

   $curl -XPUT "$NODE:9200/twitter/" -d'{ 
      "settings":{ "index":{ "drop_on_delete_index":true } }
   }'

Or to set ``drop_on delete_index`` at cluster level :

.. code::

   $curl -XPUT "$NODE:9200/_cluster/settings" -d'{ 
      "persistent":{ "cluster.drop_on_delete_index":true }
   }'

Cannot index document with empty mapping
----------------------------------------

Elassandra cannot index any document for a type having no mapped properties and no underlying clustering key because Cassandra cannot create a secondary index
on the partition key and there is no other indexed columns. Example :

.. code::

   $curl -XPUT "$NODE:9200/foo/bar/1?pretty" -d'{}'
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

The underlying cassandra table *foo.bar* has only a primary key column with no secondary index. So, search operations won't return any result.

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

To get the same behavior as Elasticsearch, just add a dummy field in your mapping.

Nested or Object types cannot be empty
--------------------------------------

Because Elasticsearch nested and object types are backed by a Cassandra User Defined Type, it requires at least one sub-field.

Document version is meaningless
-------------------------------

Elasticsearch's versioning system helps to cope with conflicts, but in a multi-master database like Apache Cassandra, versionning cannot ensure global consistency
of compare-and-set operations.

In Elassandra, Elasticsearch version management is disabled by default, document version is not more indexed in lucene files and **document version is always 1**. This simplification
improves write throughput and reduce the memory footprint by eliminating the in-memory version cache implemented in the Elasticsearch internal lucene engine.

If you want to keep the Elasticsearch internal lucene file format including a version number for each document, you should create your index with ``index.version_less_engine`` set to *false* like this :

.. code::

   $curl -XPUT "$NODE:9200/twitter/" -d'{ 
      "settings":{ "index.version_less_engine":false } }
   }'

Finally, if you need to avoid conflicts on write operations, you should use Cassandra `lightweight transactions <http://www.datastax.com/dev/blog/lightweight-transactions-in-cassandra-2-0>`_ (or PAXOS transaction).
Such lightweight transactions is also used when updating the Elassandra mapping or when indexing a document with *op_type=create*, but of course, it comes with a network cost.

Index and type names
--------------------

Because cassandra does not support special caraters in keyspace and table names, Elassandra automatically replaces dot (.) and dash (-) characters
by underscore (_) in index and type names to create underlying Cassandra keyspaces and tables.
When such a modification occurs, Elassandra keeps this change in memory to correctly convert keyspace/table to index/type.

Moreover, Cassandra table names are limited to 48 caraters, so Elasticsearch type names are also limited to 48 characters.

Column names
------------

For Elasticsearch, field mapping is unique in an index. So, two columns having the same name, indexed in an index, should have the same CQL type and share the same Elasticsearch mapping.

Null values
-----------

To be able to search for null values, Elasticsearch can replace null by a default value (see `<https://www.elastic.co/guide/en/elasticsearch/reference/2.4/null-value.html>`_ ).
In Elasticsearch, an empty array is not a null value,  wheras in Cassandra, an empty array is stored as null and replaced by the default null value at index time.

Elasticsearch unsupported feature
---------------------------------

* Tribe node allows to query multiple Elasticsearch clusters. This feature is not currently supported by Elassandra.
* Elasticsearch snapshot and restore operations are disabled (See Elassandra backup and restore in operations).
* Elasticsearch Ingest node is not supported (Use the cassandra driver to safely ingest your data).

Cassandra limitations
---------------------

* Elassandra only supports the murmur3 partitioner.
* The thrift protocol is supported only for read operations.
* Elassandra synchronously indexes rows into Elasticsearch. This may increases the write duration, particulary when indexing complex document like `GeoShape <https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html>`_, so Cassandra ``write_request_timeout_in_ms`` is set to 5 seconds (Cassandra default is 2000ms, see `Cassandra config <https://docs.datastax.com/en/cassandra/2.1/cassandra/configuration/configCassandra_yaml_r.html>`_)
* In order to avoid concurrent mapping or persistent cluster settings updates, Elassandra plays a PAXOS transaction that require QUORUM available nodes for the keyspace *elastic_admin* to succeed. So it is recommended to have at least 3 nodes in 3 distinct racks (A 2 nodes datacenter won't accept any mapping update when a node is unavailable).
* CQL3 **TRUNCATE** on a Cassandra table deletes all associated Elasticsearch documents by playing a delete_by_query where *_type = <table_name>*. Of course, such a delete_by_query comes with a performance cost.

