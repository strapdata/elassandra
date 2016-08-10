Operations
==========

Indexing
________

Let's try and index some *twitter* like information (demo from `Elasticsearch <https://github.com/elastic/elasticsearch/blob/master/README.textile>`_)). First, let's create a twitter user, and add some tweets (the *twitter* index will be created automatically):

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


There are many more options to perform search, after all, it's a search product no? All the familiar Lucene queries are available through the JSON query language, or through the query parser.


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

   nodetool rebuild_index <keyspace> <table> elastic_<table>

.. TIP::
   Re-index extisting data rely on the cassandra compaction manager. You can trigger a `cassandra compaction <http://docs.datastax.com/en/cassandra/2.0/cassandra/operations/ops_configure_compaction_t.html>`_ when :
   
   * Creating the first Elasticsearch index on a cassandra table with existing data, 
   * Running a `nodetool rebuild_index <https://docs.datastax.com/en/cassandra/2.1/cassandra/tools/toolsRebuildIndex.html>`_  command,
   * Running a `nodetool repair <https://docs.datastax.com/en/cassandra/2.1/cassandra/tools/toolsRepair.html>`_ on a keyspace having indexed tables (a repair actually creates new SSTables triggering index build).
   
   If the compaction manager is busy, secondary index rebuild is added as a pending task and executed later on. You can check current running compactions with a **nodetool compactionstats** and check pending compaction tasks with a **nodetool tpstats**. 


Open, close, index
__________________

Open and close operations allow to close and open an elasticsearch index. Even if the cassandra secondary index remains in the CQL schema while the index is closed, it has no overhead, it's just a dummy function call.
Obviously, when several elasticsearch indices are associated to the same cassandra table, data are indexed in opened indices, but not in closed ones.

.. code::

      curl -XPOST 'localhost:9200/my_index/_close'
      curl -XPOST 'localhost:9200/my_index/_open'
      

Flush, refresh index
____________________

A refresh makes all index updates performed since the last refresh available for search. By default, refresh is scheduled every second. By design, setting refresh=true on a index operation
has no effect on elassandra, because write operations are converted to CQL queries and documents are indexed later by a custom secondary index. So, the per-index refresh interval should be set carfully according to your needs.

.. code::

      curl -XPOST 'localhost:9200/my_index/_refresh'
      
A flush basically write a lucene index on disk. Because document _source is stored in cassandra table in elassandra, it make sense to execute 
a ``nodetool flush <keyspace> <table>`` to flush both cassandra memtables to SSTables and lucene files for all associated elasticsearch indices. 
Moreover, remember that a ``nodetool snapshot``  also involve a flush before creating the snapshot.

.. code::

      curl -XPOST 'localhost:9200/my_index/_flush'


Backup and restore
__________________

By design, Elassandra sychronously update elasticsearch indices on cassandra write path and flushing a cassandra table invlove a flush of all associated elasticsearch indices. Therefore,
elassandra can backup data by taking a snapshot of cassandra SSTables and Elasticsearch Lucene files on the same time on each node, as follow :

1. ``nodetool snapshot --tag <snapshot_name> <keyspace_name>``
2. For all indices associated to <keyspace_name>
   
   ``cp -al $CASSANDRA_DATA/elasticsearch.data/<cluster_name>/nodes/0/indices/<index_name>/0/index/(_*|segement*) $CASSANDRA_DATA/elasticsearch.data/snapshots/<index_name>/<snapshot_name>/``

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
5. Open all indices associated to the keyspace

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

