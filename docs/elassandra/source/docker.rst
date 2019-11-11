
We provide an `image on docker hub <https://hub.docker.com/r/strapdata/elassandra/>`_::

  docker pull strapdata/elassandra

This image is based on the `official Cassandra image <https://hub.docker.com/_/cassandra/>`_ whose the `documentation <https://github.com/docker-library/docs/tree/master/cassandra>`_ is valid as well for Elassandra.

The source code is on github at `strapdata/docker-elassandra <https://github.com/strapdata/docker-elassandra>`_.

Start an Elassandra server instance
...................................

Starting an Elassandra instance is pretty simple:

.. parsed-literal::
  docker run --name node0 -d strapdata/elassandra:|release|

Run nodetool, cqlsh and curl::

  docker exec -it node0 nodetool status
  docker exec -it node0 cqlsh
  docker exec -it node0 curl localhost:9200


Environment Variables
.....................

When you start the Elassandra image, you can adjust the configuration of the Elassandra instance by passing one or more environment variables on the docker run command line.

+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| Variable Name               | Description                                                                                                           |
+=============================+=======================================================================================================================+
| CASSANDRA_LISTEN_ADDRESS    | This variable is used for controlling which IP address to listen to for incoming connections on.                      |
|                             | The default value is auto, which will set the listen_address option in cassandra.yaml                                 |
|                             | to the IP address of the container when it starts. This default should work in most use cases.                        |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_BROADCAST_ADDRESS | This variable is used for controlling which IP address to advertise on other nodes.                                   |
|                             | The default value is the value of CASSANDRA_LISTEN_ADDRESS.                                                           |
|                             | It will set the broadcast_address and broadcast_rpc_address options in cassandra.yaml.                                |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_RPC_ADDRESS       | This variable is used for controlling which address to bind the thrift rpc server to.                                 |
|                             | If you do not specify an address, the wildcard address (0.0.0.0) will be used.                                        |
|                             | It will set the rpc_address option in cassandra.yaml.                                                                 |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_START_RPC         | This variable is used for controlling if the thrift rpc server is started. It will set the start_rpc option in        |
|                             | cassandra.yaml. As Elastic search used this port in Elassandra, it will be set ON by default.                         |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_SEEDS             | This variable is the comma-separated list of IP addresses used by gossip for bootstrapping                            |
|                             | new nodes joining a cluster. It will set the seeds value of the seed_provider option in                               |
|                             | cassandra.yaml. The CASSANDRA_BROADCAST_ADDRESS will be added to the seeds passed on so that                          |
|                             | the sever can also talk to itself.                                                                                    |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_CLUSTER_NAME      | This variable sets the name of the cluster. It must be the same for all nodes in the cluster.                         |
|                             | It will set the cluster_name option of cassandra.yaml.                                                                |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_NUM_TOKENS        | This variable sets the number of tokens for this node.                                                                |
|                             | It will set the num_tokens option of cassandra.yaml.                                                                  |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_DC                | This variable sets the datacenter name of this node.                                                                  |
|                             | It will set the dc option of cassandra-rackdc.properties.                                                             |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_RACK              | This variable sets the rack name of this node. It will set the rack option of cassandra-rackdc.properties.            |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_ENDPOINT_SNITCH   | This variable sets the snitch implementation that will be used by the node. It will set the endpoint_snitch option of |
|                             | cassandra.yml.                                                                                                        |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+
| CASSANDRA_DAEMON            | The Cassandra entry-point class: ``org.apache.cassandra.service.ElassandraDaemon`` to start                           |
|                             | with ElasticSearch enabled (default), ``org.apache.cassandra.service.ElassandraDaemon`` otherwise.                    |
+-----------------------------+-----------------------------------------------------------------------------------------------------------------------+

Files locations
...............

Docker elassandra image is based on the debian package installation:

- ``/etc/cassandra``: elassandra configuration
- ``/usr/share/cassandra``: elassandra installation
- ``/var/lib/cassandra``: data (sstables, lucene segment, commitlogs, ...)
- ``/var/log/cassandra``: logs files.

``/var/lib/cassandra`` is automatically managed as a docker volume. But it's a good target to bind mount from the host filesystem.

Exposed ports
.............

- 7000: intra-node communication
- 7001: TLS intra-node communication
- 7199: JMX
- 9042: CQL
- 9160: thrift service
- 9200: ElasticSearch HTTP
- 9300: ElasticSearch transport

Create a cluster
................

In case there is only one elassandra instance per docker host, the easiest way is to start the container with ``--net=host``.

When using the host network is not an option, you could just map the necessary ports with ``-p 9042:9042``,  ``-p 9200:9200`` and so on... but you should be aware
that docker default network will considerably slow down performances.

.. note:: Creating a cluster from the standalone image is probably fine for testing environments. But if you plan to run long-lived Elassandra clusters on containers, Kubernetes is the way to go.

