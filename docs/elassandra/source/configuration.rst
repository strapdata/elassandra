Configuration
=============

Directory Layout
----------------

Elassandra merge the cassandra and elasticsearch directories as follow :

* ``conf`` : Cassandra configuration directory + elasticsearch.yml default configuration file.
* ``bin`` : Cassandra scripts + elasticsearch plugin script.
* ``lib`` : Cassandra and elasticsearch jar dependency    
* ``pylib`` : Cqlsh python library.  
* ``tools`` : Cassandra tools.
* ``plugins`` : Elasticsearch plugins installation directory.
* ``modules`` : Elasticsearch modules directory.
* ``work`` : Elasticsearch working directory.

Elasticsearch paths are set according to the following environement variables and system properties :

* ``path.home`` : **CASSANDRA_HOME** environement variable, cassandra.home or path.home system property, the current directory.
* ``path.conf`` : **CASSANDRA_CONF** environement variable, path.conf or path.home.
* ``path.data`` : **cassandra_storagedir**/data/elasticsearch.data, path.data system property or path.home/data/elasticsearch.data


.. _elassandra_configuration:

Configuration
-------------

Elasticsearch configuration rely on cassandra configuration file **conf/cassandra.yaml** for the following parameters. 

.. cssclass:: table-bordered

+---------------------------+----------------------------+---------------------------------------------------------------------+
| Cassandra                 | Elasticsearch              | Description                                                         |
+===========================+============================+=====================================================================+
| ``cluster.name``          | ``cluster_name``           | Elasticsearch cluster name is mapped to the cassandra cluster name. |
+---------------------------+----------------------------+---------------------------------------------------------------------+
| ``rpc_address``           | ``network.host``           | Elasticsearch network and transport bind addresses                  |
|                           | ``transport.host``         | are set to the cassandra rpc listen addresses.                      |
+---------------------------+----------------------------+---------------------------------------------------------------------+
| ``broadcast_rpc_address`` | ``network.publish_host``   | Elasticsearch network and transport publish addresses               |
|                           | ``transport.publish_host`` | is set to the cassandra broadcast rpc address.                      |
+---------------------------+----------------------------+---------------------------------------------------------------------+

Node role (master, primary, data) is automatically set by elassandra, standard configuration should only set **cluster_name**, **rpc_address** in the ``conf/cassandra.yaml``.

.. CAUTION::
   If you use the `GossipPropertyFile <https://docs.datastax.com/en/cassandra/2.0/cassandra/architecture/architectureSnitchGossipPF_c.html>'_  Snitch to configure your cassandra datacenter and rack properties in **conf/cassandra-rackdc.properties**, keep
   in mind this snitch falls back to the PropertyFileSnitch when gossip is not enabled. So, when re-starting the first node, dead nodes can appear in the default DC and rack configured in **conf/cassandra-topology.properties**. This also
   breaks the replica placement strategy and the computation of the Elasticsearch routing tables. So it is strongly recommended to set the same default rack and datacenter in both the **conf/cassandra-topology.properties** and **conf/cassandra-rackdc.properties**.


Logging configuration
---------------------

The cassandra logs in ``logs/system.log`` includes elasticsearch logs according to the your ``conf/logback.conf`` settings.
See `cassandra logging configuration <https://docs.datastax.com/en/cassandra/2.1/cassandra/configuration/configLoggingLevels_r.html>`_.

Per keyspace (or per table) logging level can be configured using the logger name ``org.elassandra.index.ExtendedElasticSecondaryIndex.<keyspace>.<table>``.


Multi datacenter configuration
------------------------------

By default, all elassandra datacenters share the same Elasticsearch cluster name and mapping. This mapping is stored in the ``elastic_admin`` keyspace.

.. image:: images/elassandra-datacenter-replication.jpg

|

If you want to manage distinct Elasticsearch clusters inside a cassandra cluster (when indexing differents tables in different datacenter), you can set a ``datacenter.group`` in **conf/elasticsearch.yml** and thus, all elassandra datacenters sharing the same datacenter group name will share the same mapping.
Those elasticsearch clusters will be named <cluster_name>@<datacenter.group> and mapping will be stored in a dedicated keyspace.table ``elastic_admin_<datacenter.group>.metadata``.

All ``elastic_admin[_<datacenter.group>]`` keyspaces are configured with **NetworkReplicationStrategy** (see `data replication <https://docs.datastax.com/en/cassandra/2.0/cassandra/architecture/architectureDataDistributeReplication_c.html>`_).
where the replication factor is automatically set to the number of nodes in each datacenter. This ensure maximum availibility for the elaticsearch metadata. When removing a node from an elassandra datacenter, you should manually decrease the ``elastic_admin[_<datacenter.group>]`` replication factor to the number of nodes.

When a mapping change occurs, Elassandra updates Elasticsearch metadata in `elastic_admin[_<datacenter.group>].metadata` within a `lightweight transaction <https://docs.datastax.com/en/cassandra/2.1/cassandra/dml/dml_ltwt_transaction_c.html>`_ to avoid conflit with concurrent updates.
This transaction requires QUORUM available nodes, that is more than half the nodes of one or more datacenters regarding your ``datacenter.group`` configuration.
It also involve cross-datacenter network latency for each mapping update.


.. TIP::
   Cassandra cross-datacenter writes are not sent directly to each replica; instead, they are sent to a single replica with a parameter telling that replica to forward to the other replicas in that datacenter; those replicas will respond diectly to the original coordinator. This reduces network trafic between datacenters when having many replica.


Elassandra specific parameters
------------------------------

Cluster settings
................

* ``cluster.mapping_update_timeout`` : Set the mapping update timeout. Default is **30 secondes**.

Default index configuration :

* ``cluster.default_secondary_index_class`` : Set the default cassandra secondary index implementation class. Default is the **org.elasticsearch.cassandra.index.ExtendedElasticSecondaryIndex**.
* ``cluster.default_search_strategy_class`` : Set the default search strategy class. Default is the **org.elasticsearch.cassandra.cluster.routing.PrimaryFirstSearchStrategy**.
* ``cluster.default_include_node_id`` : If true, indexes the cassandra hostId in the _node field. Default is **false**.
* ``cluster.default_synchronous_refresh`` : If true, synchrounously refreshes the elasticsearch index on each index update. Default is **false**.
* ``cluster.default_drop_on_delete_index`` : If true, drop underlying cassandra tables and keyspace when deleting an index. Default is **false**.

Index settings
..............

* ``index.keyspace`` : Set the underlying cassandra keyspace. Default is the index name.
* ``index.partition_function`` : Set the index partition function.
* ``index.secondary_index_class`` : Set the cassandra secondary index implementation class overriding the cluster default secondary class.
* ``index.search_strategy_class`` : Set the search strategy class overriding the default cluster search strategy.
* ``index.include_node_id`` : If true, indexes the cassandra hostId in the _node field. Default is **false**.
* ``index.synchronous_refresh`` : If true, synchrounously refreshes the elasticsearch index on each index update. Default is **false**.
* ``index.drop_on_delete_index`` : If true, drop underlying cassandra tables and keyspace when deleting an index (Keyspace is deleted only if all its tables are deleted by removing the index). Default is **false**.


Sizing and tunning
------------------

Basically, Elassandra requires much CPU than standelone Cassandra or Elasticsearch and Elassandra write throughput should be half the cassandra write throughput if you index all columns. If you only index a subset of columns, performance would be better. 

Recommended production setting for Apache cassandra and Elasticsearch can be applied to Elassandra :

* Configure less than half the total memory of your server and up to 30.5Gb. Minimum recommended DRAM for production deployments is 32Gb. If you are not aggregating on analyzed string fields, you can probably use less memory to improve file system cache used by Doc Values (See this `excelent blog <https://www.elastic.co/fr/blog/support-in-the-wild-my-biggest-elasticsearch-problem-at-scale>`_ post by Chris Earle).
* Increase number of Elassandra node or use partitioned index to keep shards size below 50Gb.
* Avoid huge wide rows, write-lock on a wide row can dramatically affect write performance.
* During indexing, if you don't need search, disable **index.refresh** (default is every second). 
* Configure off_heap memory for cassandra memtables (elassandra default configuration).
* Choose the right compaction strategy to fit your workload (See this `blog <https://www.instaclustr.com/blog/2016/01/27/apache-cassandra-compaction/>`_ post by Justin Cameron)



