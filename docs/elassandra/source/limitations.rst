Breaking changes and limitations
================================

Deleting an index does not delete cassandra data
------------------------------------------------

By default, Cassandra is considered as a primary data storage for Elasticsearch, so deleting an Elasticsearch index does not delete Cassandra content, keyspace and tables remain unchanged.
If you want to use Elassandra as Elasticsearch, you can configure your cluster or only some indices with the ``drop_on delete_index`` like this.

.. code::

   $curl -XPUT -H "Content-Type: application/json" "$NODE:9200/twitter/" -d'{ 
      "settings":{ "index":{ "drop_on_delete_index":true } }
   }'

Or to set ``drop_on delete_index`` at cluster level :

.. code::

   $curl -XPUT -H "Content-Type: application/json" "$NODE:9200/_cluster/settings" -d'{ 
      "persistent":{ "cluster.drop_on_delete_index":true }
   }'

Nested or Object types cannot be empty
--------------------------------------

Because Elasticsearch nested and object types are backed by a Cassandra User Defined Type, it requires at least one sub-field in the mapping.

Document version is meaningless
-------------------------------

Elasticsearch's versioning system helps to cope with conflicts, but in a multi-master database like Apache Cassandra, versionning cannot ensure global consistency
of compare-and-set operations.

In Elassandra, Elasticsearch version management is disabled by default, document version is not more indexed in lucene files and **document version is always 1**. This simplification
improves write throughput and reduce the memory footprint by eliminating the in-memory version cache implemented in the Elasticsearch internal lucene engine.

If you want to keep the Elasticsearch internal lucene file format including a version number for each document, you should create your index with ``index.version_less_engine`` set to *false* like this :

.. code::

   $curl -XPUT -H "Content-Type: application/json" "$NODE:9200/twitter/" -d'{ 
      "settings":{ "index.version_less_engine":false } }
   }'

Finally, if you need to avoid conflicts on write operations, you should use Cassandra `lightweight transactions <http://www.datastax.com/dev/blog/lightweight-transactions-in-cassandra-2-0>`_ (or PAXOS transaction).
Such lightweight transactions is also used when updating the Elassandra mapping or when indexing a document with *op_type=create*, but of course, it comes with a network cost.

Primary term and Sequence Number
--------------------------------

As explained `here <https://www.elastic.co/blog/elasticsearch-sequence-ids-6-0>`_, Elasticsearch introduced **_primary_term** and **_seq_no** in order to manage
shard replication consistently and store these fields in lucene documents. But in Elassandra, replication is fully managed by cassandra and all shard are considered as primary. Thus, these two
fields are not more stored in lucene by the default elassandra lucene engine named **VersionLessInternalEngine**. Consequently, all search results comes with *_primary_term = 0* and *_seq_no = 1*.

Index and type names
--------------------

Because cassandra does not support special caraters in keyspace and table names, Elassandra automatically replaces dots (.) and hyphens (-) characters
by underscore (_) in index names, and hyphen (-) characters by underscore (_) in type names to create underlying Cassandra keyspaces and tables.

When such a modification occurs for document type names, Elassandra keeps type names translation in memory to correctly translate back table names to documents types.
Obviously, if you have types names like *xxx-xxx* and *xxx_xxx* in the sames underlying keyspace, bijective translation is not possible and you will get some trouble.

Moreover, Cassandra table names are limited to 48 caraters, so Elasticsearch type names are also limited to 48 characters.

Column names
------------

For Elasticsearch, field mapping is unique in an index. So, two columns having the same name, indexed in an index, should have the same CQL type and share the same Elasticsearch mapping.

Null values
-----------

To be able to search for null values, Elasticsearch can replace null by a default value (see `<https://www.elastic.co/guide/en/elasticsearch/reference/2.4/null-value.html>`_ ).
In Elasticsearch, an empty array is not a null value,  wheras in Cassandra, an empty array is stored as null and replaced by the default null value at index time.

Refresh on write
----------------

Elasticsearch write operations support a ``refresh`` parameter to control when changes made by this request are made visible to search. Possible values are *true*, *false*, or *wait_for* and in this last case, the coordinator node
waits until a refresh happens. But in elassandra, replication is managed by Cassandra and can be asynchronous. As the result managing a refresh on involved shards or waiting for a refresh to happen in not possible.

If we need to search right after a write operation, you can force a refresh before search or, if you have a reasonably low level of updates, set the index settings ``ìndex.synchronous_refresh`` to true.
This provides *Real Time Search* by refreshing shards after each update, but of course, its comes with a cost.

If you have legacy applications using ``refresh=true`` or ``refresh=wait_for``, you can set the system property ``es.synchronous_refresh`` to a regexp of index name to automatically set ``synchronous_refresh`` to **true**.
By default, because Kibana sometimes updates elasticsearch with ``refresh=wait_for``, this system property ``es.synchronous_refresh`` is set by default to (\.kibana.*).

Elasticsearch unsupported features
----------------------------------

* Tribe node allows to query multiple Elasticsearch clusters. This feature is not currently supported by Elassandra.
* Elasticsearch snapshot and restore operations are disabled (See Elassandra backup and restore in operations).
* Elasticsearch percolator, reindex and shrink API are not supported.
* Elasticsearch range fiels are supported in version 6.2
* Parent-Child join is currently supported only in Elassandra version 5.5

Cassandra limitations
---------------------

* Elassandra only supports the murmur3 partitioner.
* The thrift protocol is supported only for read operations.
* Elassandra synchronously indexes rows into Elasticsearch. This may increases the write duration, particulary when indexing complex document like `GeoShape <https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html>`_, so Cassandra ``write_request_timeout_in_ms`` is set to 5 seconds (Cassandra default is 2000ms, see `Cassandra config <https://docs.datastax.com/en/cassandra/2.1/cassandra/configuration/configCassandra_yaml_r.html>`_)
* In order to avoid concurrent mapping or persistent cluster settings updates, Elassandra plays a PAXOS transaction that require QUORUM available nodes for the keyspace *elastic_admin* to succeed. So it is recommended to have at least 3 nodes in 3 distinct racks (A 2 nodes datacenter won't accept any mapping update when a node is unavailable).
* CQL3 **TRUNCATE** on a Cassandra table deletes all associated Elasticsearch documents by playing a delete_by_query where *_type = <table_name>*. Of course, such a delete_by_query comes with a performance cost and won't notify IndexingOperationListeners for preDelete and postDelete events if used in an Elasticsearch plugin.
