Operations
==========

Indexing
________

Let's try and index some *twitter* like information (demo from `Elasticsearch <https://github.com/elastic/elasticsearch/blob/master/README.textile>`_)).
First, let's create a twitter user, and add some tweets (the *twitter* index will be created automatically, see automatic index and mapping creation in elasticsearch documentation):

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


You now have two rows in the cassandra **twitter.tweet** table.

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
the compaction later removes both the obsolete data and the tombstone (See this blog about `cassandra tombstone <http://thelastpickle.com/blog/2016/07/27/about-deletes-and-tombstones.html>`_).

By default, when using the Elasticsearch API to replace a document by a new one, 
Elassandra insert a row corresponding to the new document including null for unset fields. 
Without these null (cell tombstones), old fields not present in the new document would be kept at the cassandra level as zombie cells.

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
Then, to keep the same semantic as Elasticsearch, update operations are converted to upsert with the ALL consistency level. Thus, later get operationsare consistent. 
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

Just for kicks, let's get all the documents stored (we should see the user as well):

.. code::

   curl -XGET 'http://localhost:9200/twitter/_search?pretty=true' -d '
   {
       "query" : {
           "matchAll" : {}
       }
   }'


We can also do range search (the 'postDate' was automatically identified as date)

.. code::

   curl -XGET 'http://localhost:9200/twitter/_search?pretty=true' -d '
   {
       "query" : {
           "range" : {
               "postDate" : { "from" : "2009-11-15T13:00:00", "to" : "2009-11-15T14:00:00" }
           }
       }
   }'


There are many more options to perform search, after all, it's a search product no ? All the familiar Lucene queries are available through the JSON query language, or through the query parser.

Optimized search routing
------------------------

Elassandra supports various search strategies to distrbute a search request over the Elasticsearch cluster. A search strategy is configured at index-level with the ``index.search_strategy_class`` parameter.

+-----------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| Strategy                                                                    | Description                                                                                                                        |
+=============================================================================+====================================================================================================================================+
| ``org.elassandra.cluster.routing.PrimaryFirstSearchStrategy`` (**Default**) | Search on all alive nodes in the datacenter. All alive nodes responds for their primary token ranges, and for replica token ranges |
|                                                                             | when there is some unavailable nodes. This strategy is always used to build the routing table in the cluster state.                |
+-----------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| ``org.elassandra.cluster.routing.RandomSearchStrategy``                     | For each query, randomly distribute a search request to a minimum of nodes to reduce the network traffic.                          |
|                                                                             | For exemple, if your underlying keyspace replication factor is N, a search only invloves 1/N of the nodes.                         |
+-----------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+

You can create an index with the ``RandomSearchStrategy`` as shown below.

.. code::

   curl -XPUT "http://localhost:9200/twitter/" -d '{ 
      "settings" : { 
         "index.search_strategy_class":"org.elasticsearch.cassandra.cluster.routing.RandomSearchStrategy" 
      }
   }'

.. TIP::
   When changing a keyspace replication factor, you can force an elasticsearch routing table update by closing and re-opening all associated elasticsearch indices.
   To troubleshoot search request routing, set the logging level to **DEBUG** for **class org.elasticsearch.cassandra.cluster.routing** in the **conf/logback.xml** file.  

Create, delete and rebuild index
________________________________

In order to create an Elasticsearch index from an existing cassandra table, you can specify the underlying keyspace. In the following exemple, all columns but *message* is automatically mapped
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

Deleting an Elasticsearch index does not remove any cassandra data, it keeps the underlying cassandra tables but remove elasticsearch index files.

.. code::

   curl -XDELETE 'http://localhost:9200/twitter_index'

To re-index your existing data, for exemple after a mapping change to index a new column, run a **nodetool rebuild_index** as follow :

.. code::

   nodetool rebuild_index [--threads <N>] <keyspace> <table> elastic_<table>

.. TIP::
   By default, rebuild index runs on a single thread. In order to improve re-indexing performance, Elassandra comes with a multi-threaded rebuild_index implementation. The **--threads** parameter allows to specify the number of threads dedicated to re-index a cassandra table.
   Number of indexing threads should be tuned carefully to avoid CPU exhaustion. Moreover, indexing throughput is limited by locking at the lucene level, but this limit can be exceeded by using a partitioned index invloving many independant shards. 
   
Re-index extisting data rely on the cassandra compaction manager. You can trigger a `cassandra compaction <http://docs.datastax.com/en/cassandra/2.0/cassandra/operations/ops_configure_compaction_t.html>`_ when :

* Creating the first Elasticsearch index on a cassandra table with existing data, 
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

Open and close operations allow to close and open an elasticsearch index. Even if the cassandra secondary index remains in the CQL schema while the index is closed, it has no overhead, it's just a dummy function call.
Obviously, when several elasticsearch indices are associated to the same cassandra table, data are indexed in opened indices, but not in closed ones.

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
      
A flush basically write a lucene index on disk. Because document **_source** is stored in cassandra table in elassandra, it make sense to execute 
a ``nodetool flush <keyspace> <table>`` to flush both cassandra Memtables to SSTables and lucene files for all associated elasticsearch indices. 
Moreover, remember that a ``nodetool snapshot``  also involve a flush before creating a snapshot.

.. code::

      curl -XPOST 'localhost:9200/my_index/_flush'

Percolator
__________

Elassandra supports distributed percolator by storing percolation queries in a dedicated cassandra table ``_percolator``.
As for documents, token ranges filtering applies to avoid dupliquate query matching.

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


Backup and restore
__________________

By design, Elassandra sychronously update elasticsearch indices on cassandra write path and flushing a cassandra table invlove a flush of all associated elasticsearch indices. Therefore,
elassandra can backup data by taking a snapshot of cassandra SSTables and Elasticsearch Lucene files on the same time on each node, as follow :

1. ``nodetool snapshot --tag <snapshot_name> <keyspace_name>``
2. For all indices associated to <keyspace_name>
   
   ``cp -al $CASSANDRA_DATA/elasticsearch.data/<cluster_name>/nodes/0/indices/<index_name>/0/index/(_*|segment*) $CASSANDRA_DATA/elasticsearch.data/snapshots/<index_name>/<snapshot_name>/``

Of course, rebuilding elasticsearch indices after a cassandra restore is another option.

Restoring a snapshot
--------------------

Restoring cassandra SSTable and elasticsearch lucene files allow to recover a keyspace and its associated elasticsearch indices without stopping any node.
(but it is not intended to duplicate data to another virtual datacenter or cluster)

To perform a hot restore of cassandra keyspace and its elasticsearch indices :

1. Close all elasticsearch indices associated to the keyspace
2. Trunacte all cassandra tables of the keyspace (because of delete operation later than the snapshot)
3. Restore the cassandra table with your snapshot on each node
4. Restore elasticsearch snapshot on each nodes (if ES index is open during nodetool refresh, this cause elasticsearch index rebuild by the compaction manager, usually 2 threads).
5. Load restored SSTables with a ``nodetool refresh``
6. Open all indices associated to the keyspace

Point in time recovery
----------------------

Point-in-time recovery is intended to recover the data at any time. This require a restore of the last available cassandra and elasticsearch snapshot before your recovery point and then apply 
the commitlogs from this restore point to the recovery point. In this case, replaying commitlogs on startup also re-index data in elasticsearch indices, ensuring consistency at the recovery point.

Of course, when stopping a production cluster is not possible, you should restore on a temporary cluster, make a full snapshot, and restore it on your production cluster as describe by the hot restore procedure.

To perform a point-in-time-recovery of a cassandra keyspace and its elasticsearch indices, for all nodes in the same time :

1. Stop all the datacenter nodes.
2. Restore the last cassandra snapshot before the restore point and commitlogs from that point to the restore point
3. Restore the last elasticsearch snapshot before the restore point.
4. Restart your nodes

Restoring to a different cluster
--------------------------------

It is possible to restore a cassandra keyspace and its associated elasticsearch indices to another cluster.

1. On the target cluster, create the same cassandra schema without any custom secondary indices
2. From the source cluster, extract the mapping of your associated indices and apply it to your destination cluster. Your keyspace and indices should be open and empty at this step.

If you are restoring into a new cluster having the same number of nodes, configure it with the same token ranges 
(see https://docs.datastax.com/en/cassandra/2.1/cassandra/operations/ops_snapshot_restore_new_cluster.html). In this case, 
you can restore from cassandra and elasticsearch snapshots as describe in step 1, 3 and 4 of the snapshot restore procedure. 

Otherwise, when the number of node and the token ranges from the source and desination cluster does not match, use the sstableloader to restore your cassandra snapshots 
(see https://docs.datastax.com/en/cassandra/2.0/cassandra/tools/toolsBulkloader_t.html ). This approach is much time-and-io-consuming because all rows
are read from the sstables and injected into the cassandra cluster, causing an full elasticsearch index rebuild.

How to change the elassandra cluster name
_________________________________________

Because the cluster name is a part of the elasticsearch directory structure, managing snapshots with shell scripts could be a nightmare when cluster name contains space caracters. 
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
6. On all nodes, in you cassandra data directory, move elasticsearch.data/<old_cluster_name> to elasticsearch.data/<new_cluster_name>
7. Restart all nodes
8. Check the cluster name in the elasticsearch cluster state and that you can update the mapping.

