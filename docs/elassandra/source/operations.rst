Operations
==========

Indexing
________

Let's try and index some *twitter* like information (demo from `Elasticsearch <https://github.com/elastic/elasticsearch/blob/master/README.textile>`_)).
First, let's create a twitter user, and add some tweets (the *twitter* index will be created automatically, see automatic index and mapping creation in Elasticsearch documentation):

.. code::

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


You now have two rows in the Cassandra **twitter.tweet** table.

.. code::

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
   

Apache Cassandra is a column store that only support upsert operation. This means that deleting a cell or a row invovles the creation of a tombestone (insert a null) kept until
the compaction later removes both the obsolete data and the tombstone (See this blog about `Cassandra tombstone <http://thelastpickle.com/blog/2016/07/27/about-deletes-and-tombstones.html>`_).

By default, when using the Elasticsearch API to replace a document by a new one,
Elassandra insert a row corresponding to the new document including null for unset fields.
Without these null (cell tombstones), old fields not present in the new document would be kept at the Cassandra level as zombie cells.

Moreover, indexing with ``op_type=create`` (See `Elasticsearch indexing '<https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html#operation-type>`_ ) require a Cassandra PAXOS transaction
to check if the document exists in the underlying datacenter. This comes with useless performance cost if you use automatic generated
document ID (See `Automatic ID generation <https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html#_automatic_id_generation>`_.
), as this ID will be the Cassandra primary key.

Depending on *op_type* and document ID, CQL requests are issued as follow when indexing with the Elasticsearch API :

.. cssclass:: table-bordered

+------------+-----------------------------+-------------------------------------------------+--------------------------------------------+
| *op_type*  | Generated ID                | Provided ID                                     | Comment                                    |
+============+=============================+=================================================+============================================+
| ``create`` | INSERT INTO ... VALUES(...) | INSERT INTO ... VALUES(...) IF NOT EXISTS *(1)* | Index a new document.                      |
+------------+-----------------------------+-------------------------------------------------+--------------------------------------------+
| ``index``  | INSERT INTO ... VALUES(...) | DELETE FROM ... WHERE ...                       | Replace a document that may already exists |
|            |                             | INSERT INTO ... VALUES(...)                     |                                            |
+------------+-----------------------------+-------------------------------------------------+--------------------------------------------+

(1) The *IF NOT EXISTS* comes with the cost of the PAXOS transaction. If you don't need to check the uniqueness of the provided ID,
add parameter ``check_unique_id=false``.


GETing
______

Now, let's see if the information was added by GETting it:

.. code::

   curl -XGET 'http://localhost:9200/twitter/user/kimchy?pretty=true'
   curl -XGET 'http://localhost:9200/twitter/tweet/1?pretty=true'
   curl -XGET 'http://localhost:9200/twitter/tweet/2?pretty=true'

Elasticsearch state now show reflect the new twitter index. Because we are currently running on one node, the **token_ranges** routing
attribute match 100% of the ring from Long.MIN_VALUE to Long.MAX_VALUE.

.. code::

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

Updates
_______

In Cassandra, an update is an upsert operation (if the row does not exists, it's an insert).
As Elasticsearch, Elassandra issue a GET operation before any update.
Then, to keep the same semantic as Elasticsearch, update operations are converted to upsert with the ALL consistency level. Thus, later get operations are consistent.
(You should consider `CQL UPDATE <https://docs.datastax.com/en/cql/3.3/cql/cql_reference/update_r.html>`_ operation to avoid this performance cost)

Scripted updates, upsert (scripted_upsert and doc_as_upsert) are also supported.

Searching
_________

Let's find all the tweets that *kimchy* posted:

.. code::

   curl -XGET 'http://localhost:9200/twitter/tweet/_search?q=user:kimchy&pretty=true'

We can also use the JSON query language Elasticsearch provides instead of a query string:

.. code::

   curl -XGET 'http://localhost:9200/twitter/tweet/_search?pretty=true' -d '
   {
       "query" : {
           "match" : { "user": "kimchy" }
       }
   }'

To avoid duplicates results when the Cassandra replication factor is greater than one, Elassandra adds a token_ranges filter to every queries distributed to all nodes. Because every document contains
a _token fields computed at index-time, this ensure that a node only retrieves documents for the requested token ranges.
The ``token_ranges`` parameter is a conjunction of Lucene `NumericRangeQuery <https://lucene.apache.org/core/5_2_1/core/org/apache/lucene/search/NumericRangeQuery.html>`_ build from the Elasticsearch routing tables to cover the entire Cassandra ring.
.. code::

   curl -XGET 'http://localhost:9200/twitter/tweet/_search?pretty=true&token_ranges=(0,9223372036854775807)' -d '
   {
       "query" : {
           "match" : { "user": "kimchy" }
       }
   }'

Of course, if the token range filter cover all ranges (Long.MIN_VALUE to Long.MAX_VALUE), Elassandra automatically remove the useless filter.

Finally, you can restrict a query to the coordinator node with *preference=_only_local* parameter, for all token_ranges as shown below :

.. code::

   curl -XGET 'http://localhost:9200/twitter/tweet/_search?pretty=true&preference=_only_local&token_ranges=' -d '
   {
       "query" : {
           "match" : { "user": "kimchy" }
       }
   }'

Optimizing search requests
--------------------------

The search strategy
...................

Elassandra supports various search strategies to distribute a search request over the Elasticsearch cluster. A search strategy is configured at index-level with the ``index.search_strategy_class`` parameter.

+-----------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| Strategy                                                                    | Description                                                                                                                        |
+=============================================================================+====================================================================================================================================+
| ``org.elassandra.cluster.routing.PrimaryFirstSearchStrategy`` (**Default**) | Search on all alive nodes in the datacenter. All alive nodes responds for their primary token ranges, and for replica token ranges |
|                                                                             | when there is some unavailable nodes. This strategy is always used to build the routing table in the cluster state.                |
+-----------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| ``org.elassandra.cluster.routing.RandomSearchStrategy``                     | For each query, randomly distribute a search request to a minimum of nodes to reduce the network traffic.                          |
|                                                                             | For example, if your underlying keyspace replication factor is N, a search only invloves 1/N of the nodes.                         |
+-----------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+

You can create an index with the ``RandomSearchStrategy`` as shown below.

.. code::

   curl -XPUT "http://localhost:9200/twitter/" -d '{ 
      "settings" : { 
         "index.search_strategy_class":"RandomSearchStrategy" 
      }
   }'

.. TIP::
   When changing a keyspace replication factor, you can force an Elasticsearch routing table update by closing and re-opening all associated elasticsearch indices.
   To troubleshoot search request routing, set the logging level to **DEBUG** for **class org.elassandra.cluster.routing** in the **conf/logback.xml** file.  

Caching features
----------------

Compared to Elasticsearch, Elassandra introduces a search overhead by adding to each query a token ranges filter and by fetching fields through a CQL request at the Cassandra layer. These overheads can
be both mitigated by using caching features.

Token Ranges Query Cache
........................

Token ranges filter depends on the node or vnodes configuration, are quite stable and shared for all keyspaces having the same replication factor. These filters only change when the datacenter topology changes, for example when a node is temporary down or when a node is added to the datacenter.
So, Elassandra use a cache to keep these queries, a conjunction of Lucene `NumericRangeQuery <https://lucene.apache.org/core/5_2_1/core/org/apache/lucene/search/NumericRangeQuery.html>`_ often reused for every search requests.

As a classic caching strategy, the ``token_ranges_query_expire`` controls the expiration time of useless token ranges filter queries into memory. The default is 5 minutes.

Token Ranges Bitset Cache
.........................

When enabled, the token ranges bitset cache keeps into memory the results of the token range filter for each Lucene segment. This in-memory bitset, acting as the liveDocs Lucene thumbstones mechanism, is then reused for subsequent Lucene search queries.
For each Lucene segment, this document bitset is updated when the Lucene thumbstones count increase (it's a bitwise AND between the actual Lucene thumbstones and the token range filter result), or removed if the corresponding token ranges query is removed because unused from the token range query cache.

You can enable the token range bitset cache at index level by setting ``index.token_ranges_bitset_cache`` to *true* (Default is *false*), or configure the its default value for newly created indices at cluster or system levels.

You can also bypass this cache by adding *token_ranges_bitset_cache=false* in your search request :

.. code::

   curl -XPUT "http://localhost:9200/twitter/_search?token_ranges_bitset_cache=false&q=*:*"

Finally, you can check the in-memory size of the token ranges bitset cache with the Elasticsearch stats API, and clear it when clearing the Elasticsearch query_cache :

.. code::

   curl -XGET "http://localhost:9200/_stats?pretty=true"
   ...
   "segments" : {
          "count" : 3,
          "memory_in_bytes" : 26711,
          "terms_memory_in_bytes" : 23563,
          "stored_fields_memory_in_bytes" : 1032,
          "term_vectors_memory_in_bytes" : 0,
          "norms_memory_in_bytes" : 384,
          "doc_values_memory_in_bytes" : 1732,
          "index_writer_memory_in_bytes" : 0,
          "index_writer_max_memory_in_bytes" : 421108121,
          "version_map_memory_in_bytes" : 0,
          "fixed_bit_set_memory_in_bytes" : 0,
          "token_ranges_bit_set_memory_in_bytes" : 240
        },
    ...

Cassandra Key and Row Cache
...........................

To improve CQL fetch requests response time, Cassandra provides key and row caching features configured for each Cassandra table as follow :

.. code::

   ALTER TABLE ... WITH caching = {'keys': 'ALL', 'rows_per_partition': '1'};

To enable Cassandra row caching, set the ``row_cache_size_in_mb`` parameter in your **conf/cassandra.yaml**, and set ``row_cache_class_name: org.apache.cassandra.cache.OHCProvider`` to use off-heap memory.

.. TIP::
   Elasticsearch also provides a Lucene query cache, used for segments having more than 10k documents, and for some frequent queries (queries done more than 5 or 20 times depending of the nature of the query). The shard request cache, can also be enable if the token range bitset cache is disabled. 

Create, delete and rebuild index
________________________________

In order to create an Elasticsearch index from an existing Cassandra table, you can specify the underlying keyspace. In the following example, all columns but *message* is automatically mapped
with the default mapping, and the *message* is explicitly mapped with a custom mapping.

.. code::

   curl -XGET 'http://localhost:9200/twitter_index' -d '{
       "settings": { "keyspace":"twitter" }
       "mappings": { 
           "tweet" : {
               "discover":"^(?!message).*",
               "properties" : {
                  "message" : { "type":"string", "index":"analyzed", "cql_collection":"singleton" }
               }
               
           }
       }
   }'

Deleting an Elasticsearch index does not remove any Cassandra data, it keeps the underlying Cassandra tables but remove Elasticsearch index files.

.. code::

   curl -XDELETE 'http://localhost:9200/twitter_index'

To re-index your existing data, for example after a mapping change to index a new column, run a **nodetool rebuild_index** as follow :

.. code::

   nodetool rebuild_index [--threads <N>] <keyspace> <table> elastic_<table>_idx

.. TIP::
   By default, rebuild index runs on a single thread. In order to improve re-indexing performance, Elassandra comes with a multi-threaded rebuild_index implementation. The **--threads** parameter allows to specify the number of threads dedicated to re-index a Cassandra table.
   Number of indexing threads should be tuned carefully to avoid CPU exhaustion. Moreover, indexing throughput is limited by locking at the lucene level, but this limit can be exceeded by using a partitioned index invloving many independant shards. 

Alternatively, you can use the built-in rebuild action to rebuild index on **all** your Elasticsearch cluster at the same time. The *num_thread* parameter is optional, default is one, but you should care about the load of your cluster in a production environnement.

.. code::

   curl -XGET 'http://localhost:9200/twitter_index/_rebuild?num_threads=4'

Re-index existing data rely on the Cassandra compaction manager. You can trigger a `Cassandra compaction <http://docs.datastax.com/en/cassandra/2.0/cassandra/operations/ops_configure_compaction_t.html>`_ when :

* Creating the first Elasticsearch index on a Cassandra table with existing data,
* Running a `nodetool rebuild_index <https://docs.datastax.com/en/cassandra/2.1/cassandra/tools/toolsRebuildIndex.html>`_  command,
* Running a `nodetool repair <https://docs.datastax.com/en/cassandra/2.1/cassandra/tools/toolsRepair.html>`_ on a keyspace having indexed tables (a repair actually creates new SSTables triggering index build).

If the compaction manager is busy, secondary index rebuild is added as a pending task and executed later on. You can check current running compactions with a **nodetool compactionstats** and check pending compaction tasks with a **nodetool tpstats**.

.. code::

   nodetool -h 52.43.156.196 compactionstats
   pending tasks: 1
                                     id         compaction type   keyspace      table   completed       total    unit   progress
   052c70f0-8690-11e6-aa56-674c194215f6   Secondary index build     lastfm   playlist    66347424   330228366   bytes     20,09%
   Active compaction remaining time :   0h00m00s

To stop a compaction task (including a rebuild index task), you can either use a **nodetool stop** or use the JMX management operation  **stopCompactionById**.

Open, close, index
__________________

Open and close operations allow to close and open an Elasticsearch index. Even if the Cassandra secondary index remains in the CQL schema while the index is closed, it has no overhead, it's just a dummy function call.
Obviously, when several Elasticsearch indices are associated to the same Cassandra table, data are indexed in opened indices, but not in closed ones.

.. code::

      curl -XPOST 'localhost:9200/my_index/_close'
      curl -XPOST 'localhost:9200/my_index/_open'
      

.. warning::

   Elasticsearch `translog <https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules-translog.html>`_ is disabled in Elassandra, 
   so you might loose some indexed documents when closing an index if ``index.flush_on_close`` is *false*.

Flush, refresh index
____________________

A refresh makes all index updates performed since the last refresh available for search. By default, refresh is scheduled every second. By design, setting refresh=true on a index operation
has no effect with Elassandra, because write operations are converted to CQL queries and documents are indexed later by a custom secondary index. So, the per-index refresh interval should be set carfully according to your needs.

.. code::

      curl -XPOST 'localhost:9200/my_index/_refresh'
      
A flush basically write a lucene index on disk. Because document **_source** is stored in Cassandra table in elassandra, it make sense to execute
a ``nodetool flush <keyspace> <table>`` to flush both Cassandra Memtables to SSTables and lucene files for all associated Elasticsearch indices.
Moreover, remember that a ``nodetool snapshot``  also involve a flush before creating a snapshot.

.. code::

      curl -XPOST 'localhost:9200/my_index/_flush'

Percolator
__________

Elassandra supports distributed percolator by storing percolation queries in a dedicated Cassandra table ``_percolator``.
As for documents, token ranges filtering applies to avoid duplicate query matching.

.. code::

   curl -XPUT "localhost:9200/my_index" -d '{
     "mappings": {
       "my_type": {
         "properties": {
           "message": { "type": "string" },
           "created_at" : { "type": "date" }
         }
       }
     }
   }'
   
   curl -XPUT "localhost:9200/my_index/.percolator/1" -d '{
       "query" : {
           "match" : {
               "message" : "bonsai tree"
           }
       }
   }'
   
   curl -XPUT "localhost:9200/my_index/.percolator/2" -d '{
       "query" : {
           "match" : {
               "message" : "bonsai tree"
           }
       },
       "priority" : "high"
   }'
   
   curl -XPUT "localhost:9200/my_index/.percolator/3" -d '{
           "query" : {
                   "range" : {
                           "created_at" : {
                                   "gte" : "2010-01-01T00:00:00",
                                   "lte" : "2011-01-01T00:00:00"
                           }
                   }
           },
           "type" : "tweet",
           "priority" : "high"
   }'

Then search for matching queries.

.. code::

   curl -XGET 'localhost:9200/my_index/my_type/_percolate?pretty=true' -d '{
       "doc" : {
           "message" : "A new bonsai tree in the office"
       }
   }'
   {
     "took" : 4,
     "_shards" : {
       "total" : 2,
       "successful" : 2,
       "failed" : 0
     },
     "total" : 2,
     "matches" : [ {
       "_index" : "my_index",
       "_id" : "2"
     }, {
       "_index" : "my_index",
       "_id" : "1"
     } ]
   }


.. code::

   curl -XGET 'localhost:9200/my_index/my_type/_percolate?pretty=true' -d '{
       "doc" : {
           "message" : "A new bonsai tree in the office"
       },
       "filter" : {
           "term" : {
               "priority" : "high"
           }
       }
   }'
   {
     "took" : 4,
     "_shards" : {
       "total" : 2,
       "successful" : 2,
       "failed" : 0
     },
     "total" : 1,
     "matches" : [ {
       "_index" : "my_index",
       "_id" : "2"
     } ]
   }

Managing Elassandra nodes
_________________________

You can add, remove or replace an Elassandra node by using the same procedure as for Cassandra (see `Adding nodes to an existing cluster <http://docs.datastax.com/en/cassandra/3.0/cassandra/operations/opsAddNodeToCluster.html?hl=vnode>`_).
Even if it's technically possible, you should never boostrap more than one node at a time,

During the bootstrap process, pulled data from existing nodes are automatically indexed by Elasticsearch on the new node, involving a kind of an automatic Elasticsearch resharding.
You can monitor and resume the Cassandra boostrap process with the `nodetool bootstrap <https://docs.datastax.com/en/cassandra/3.0/cassandra/tools/toolsBootstrap.html>`_ command.

After boostrap successfully ends, you should cleanup nodes to throw out any data that is no longer owned by that node, with a `nodetool cleanup <http://docs.datastax.com/en/archived/cassandra/2.0/cassandra/tools/toolsCleanup.html>`_.
Because cleanup involves by a Delete-by-query in Elasticsearch indices, it is recommended to smoothly schedule cleanups one at a time in you datacenter.

Backup and restore
__________________

By design, Elassandra synchronously update Elasticsearch indices on Cassandra write path and flushing a Cassandra table invlove a flush of all associated elasticsearch indices. Therefore,
elassandra can backup data by taking a snapshot of Cassandra SSTables and Elasticsearch Lucene files on the same time on each node, as follow :

1. ``nodetool snapshot --tag <snapshot_name> <keyspace_name>``
2. For all indices associated to <keyspace_name>

   ``cp -al $CASSANDRA_DATA/elasticsearch.data/<cluster_name>/nodes/0/indices/<index_name>/0/index/(_*|segment*) $CASSANDRA_DATA/elasticsearch.data/snapshots/<index_name>/<snapshot_name>/``

Of course, rebuilding Elasticsearch indices after a Cassandra restore is another option.

Restoring a snapshot
--------------------

Restoring Cassandra SSTable and Elasticsearch Lucene files allow to recover a keyspace and its associated Elasticsearch indices without stopping any node.
(but it is not intended to duplicate data to another virtual datacenter or cluster)

To perform a hot restore of Cassandra keyspace and its Elasticsearch indices :

1. Close all Elasticsearch indices associated to the keyspace
2. Trunacte all Cassandra tables of the keyspace (because of delete operation later than the snapshot)
3. Restore the Cassandra table with your snapshot on each node
4. Restore Elasticsearch snapshot on each nodes (if ES index is open during nodetool refresh, this cause Elasticsearch index rebuild by the compaction manager, usually 2 threads).
5. Load restored SSTables with a ``nodetool refresh``
6. Open all indices associated to the keyspace

Point in time recovery
----------------------

Point-in-time recovery is intended to recover the data at any time. This require a restore of the last available Cassandra and Elasticsearch snapshot before your recovery point and then apply
the commitlogs from this restore point to the recovery point. In this case, replaying commitlogs on startup also re-index data in Elasticsearch indices, ensuring consistency at the recovery point.

Of course, when stopping a production cluster is not possible, you should restore on a temporary cluster, make a full snapshot, and restore it on your production cluster as describe by the hot restore procedure.

To perform a point-in-time-recovery of a Cassandra keyspace and its Elasticsearch indices, for all nodes in the same time :

1. Stop all the datacenter nodes.
2. Restore the last Cassandra snapshot before the restore point and commitlogs from that point to the restore point
3. Restore the last Elasticsearch snapshot before the restore point.
4. Restart your nodes

Restoring to a different cluster
--------------------------------

It is possible to restore a Cassandra keyspace and its associated Elasticsearch indices to another cluster.

1. On the target cluster, create the same Cassandra schema without any custom secondary indices
2. From the source cluster, extract the mapping of your associated indices and apply it to your destination cluster. Your keyspace and indices should be open and empty at this step.

If you are restoring into a new cluster having the same number of nodes, configure it with the same token ranges
(see https://docs.datastax.com/en/Cassandra/2.1/cassandra/operations/ops_snapshot_restore_new_cluster.html). In this case,
you can restore from Cassandra and Elasticsearch snapshots as describe in step 1, 3 and 4 of the snapshot restore procedure.

Otherwise, when the number of node and the token ranges from the source and desination cluster does not match, use the sstableloader to restore your Cassandra snapshots
(see https://docs.datastax.com/en/cassandra/2.0/cassandra/tools/toolsBulkloader_t.html ). This approach is much time-and-io-consuming because all rows
are read from the sstables and injected into the Cassandra cluster, causing an full Elasticsearch index rebuild.

How to change the elassandra cluster name
_________________________________________

Because the cluster name is a part of the Elasticsearch directory structure, managing snapshots with shell scripts could be a nightmare when cluster name contains space caracters.
Therfore, it is recommanded to avoid space caraters in your elassandra cluster name.

On all nodes:

1. In a cqlsh, **UPDATE system.local SET cluster_name = '<new_cluster_name>' where key='local'**;
2. Update the cluster_name parameter with the same value in your conf/cassandra.yaml
3. Run a ``nodetool flush system`` (this flush your system keyspace on disk)

Then:

4. On one node only, change the primary key of your cluster metadata in the elastic_admin.metadata table, using cqlsh :

   - **COPY elastic_admin.metadata (cluster_name, metadata, owner, version) TO 'metadata.csv'**;
   - Update the cluster name in the file metadata.csv (first field in the JSON document).
   - **COPY elastic_admin.metadata (cluster_name, metadata, owner, version) FROM 'metadata.csv'**;
   - **DELETE FROM elastic_admin.metadata WHERE cluster_name='<old_cluster_name>'**;

5. Stop all nodes in the cluster
6. On all nodes, in you Cassandra data directory, move elasticsearch.data/<old_cluster_name> to elasticsearch.data/<new_cluster_name>
7. Restart all nodes
8. Check the cluster name in the Elasticsearch cluster state and that you can update the mapping.

