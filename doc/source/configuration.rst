Configuration
=============

Directory Layout
----------------

Elasticsearch paths overridden follow : 
* path.home = *CASSANDRA_HOME* environnement variable or *cassandra.home* or *path.home* system property, or the current directory.
* path.conf = *CASSANDRA_CONF* environnement variable, or *path.conf* or *path.home*.
* path.data = *cassandra_storagedir*/data/elasticsearch.data or *path.data* system property or *path.home*/data/elasticsearch.data.

Elasticsearch implicit configuration
------------------------------------

Elassandra mainly rely on on the cassandra configuration file 'conf/cassandra.yaml'.

.. cssclass:: table-bordered

    +------------------------+----------------------+-----------------------------------------------------------------------+
    | Cassandra              | Elasticsearch        | Description                                                           |
    +========================+======================+=======================================================================+
    | cluster.name           | cluster_name         | Elasticsearch cluster name is mapped to the cassandra cluster name.   |
    +------------------------+----------------------+-----------------------------------------------------------------------+
    | rpc_address            | network.host         | Elasticsearch listen on the cassandra rpc address                     |
    | rpc_interface          | http.bind_ho         |                                                                       |
    |                        | http.netty.bind_host |                                                                       |
    |                        | http.host            |                                                                       |
    +------------------------+----------------------+-----------------------------------------------------------------------+
   
Logging configuration
---------------------

The cassandra logs in `logs/system.log` includes elasticsearch logs according to the your `conf/logback.conf` settings.


Multi datacenter configuration
------------------------------

By default, all elassandra datacenters share the same cluster name (the cassandra cluster_name) and the same mapping. This mapping is stored in the *elastic_admin* keyspace.

If you want to manage distincts Elasticsearch clusters, you can set a datacenter group name and thus, all elassandra datacenters sharing the same datacenter group name will share the same mapping. 
Those elasticsearch cluster will be named <cassandra cluster_name>/<datacenter group name> and mapping will be managed in the keyspace *elastic_admin_<datacenter.group>*.

All *elastic_admin[_<datacenter.group]* keyspace rely on a DatacenterReplicationStrategy where all nodes of each configured datacenters are replica.