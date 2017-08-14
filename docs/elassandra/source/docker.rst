
We provide an `image on docker hub <https://hub.docker.com/r/strapdata/elassandra/>`_::

  docker pull strapdata/elassandra

This image is based on the `official Cassandra image <https://hub.docker.com/_/cassandra/>`_ whose the `documentation <https://github.com/docker-library/docs/tree/master/cassandra>`_ is valid as well for Elassandra.


Start an elassandra server instance
...................................

Starting an Elassandra instance is simple::

  docker run --name some-elassandra -d strapdata/elassandra

...where ``some-cassandra`` is the name you want to assign to your container and ``tag`` is the tag specifying the Elassandra version you want. Default is ``latest``.

Connect to Cassandra from an application in another Docker container
....................................................................

This image exposes the standard Cassandra ports and the HTTP ElasticSearch port (9200),
so container linking makes the Elassandra instance available to other application containers.
Start your application container like this in order to link it to the Elassandra container::

  docker run --name some-app --link some-elassandra:elassandra -d app-that-uses-elassandra

Make a cluster
..............

Using the environment variables documented below, there are two cluster
scenarios: instances on the same machine and instances on separate
machines. For the same machine, start the instance as described above.
To start other instances, just tell each new node where the first is.

.. code:: console

    docker run --name some-elassandra2 -d -e CASSANDRA_SEEDS="$(docker inspect --format='{{ .NetworkSettings.IPAddress }}' some-elassandra)" elassandra

... where ``some-elassandra`` is the name of your original Elassandra container,
taking advantage of ``docker inspect`` to get the IP address of the other container.

Or you may use the ``docker run --link`` option to tell the new node where
the first is::

    docker run --name some-elassandra2 -d --link some-elassandra:elassandra elassandra

For separate machines (ie, two VMs on a cloud provider), you need to
tell Elassandra what IP address to advertise to the other nodes (since
the address of the container is behind the docker bridge).

Assuming the first machine's IP address is ``10.42.42.42`` and the
second's is ``10.43.43.43``, start the first with exposed gossip port::

    docker run --name some-elassandra -d -e CASSANDRA_BROADCAST_ADDRESS=10.42.42.42 -p 7000:7000 elassandra

Then start an Elassandra container on the second machine, with the exposed
gossip port and seed pointing to the first machine::

    docker run --name some-elassandra -d -e CASSANDRA_BROADCAST_ADDRESS=10.43.43.43 -p 7000:7000 -e CASSANDRA_SEEDS=10.42.42.42 elassandra

Container shell access and viewing Cassandra logs
.................................................

The ``docker exec`` command allows you to run commands inside a Docker
container. The following command line will give you a bash shell inside
your ``elassandra`` container:

.. code:: console

    $ docker exec -it some-elassandra bash

The Cassandra Server log is available through Docker's container log:

.. code:: console

    $ docker logs some-elassandra

Environment Variables
.....................

When you start the Elassandra image, you can adjust the configuration of the Elassandra instance by passing one or more environment variables on the docker run command line. We already have seen some of them.


+-----------------------------+------------------------------------------------------------------------------------------------------------+
| Variable Name               | Description                                                                                                |
+=============================+============================================================================================================+
| CASSANDRA_LISTEN_ADDRESS    | This variable is for controlling which IP address to listen for incoming connections on.                   |
|                             | The default value is auto, which will set the listen_address option in cassandra.yaml                      |
|                             | to the IP address of the container as it starts. This default should work in most use cases.               |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_BROADCAST_ADDRESS | This variable is for controlling which IP address to advertise to other nodes.                             |
|                             | The default value is the value of CASSANDRA_LISTEN_ADDRESS.                                                |
|                             | It will set the broadcast_address and broadcast_rpc_address options in cassandra.yaml.                     |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_RPC_ADDRESS       | This variable is for controlling which address to bind the thrift rpc server to.                           |
|                             | If you do not specify an address, the wildcard address (0.0.0.0) will be used.                             |
|                             | It will set the rpc_address option in cassandra.yaml.                                                      |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_START_RPC         | This variable is for controlling if the thrift rpc server is started. It will set the start_rpc option in  |
|                             | cassandra.yaml. As Elastic search used this port in Elassandra, it will be set ON by default.              |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_SEEDS             | This variable is the comma-separated list of IP addresses used by gossip for bootstrapping                 |
|                             | new nodes joining a cluster. It will set the seeds value of the seed_provider option in                    |
|                             | cassandra.yaml. The CASSANDRA_BROADCAST_ADDRESS will be added the the seeds passed in so that              |
|                             | the sever will talk to itself as well.                                                                     |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_CLUSTER_NAME      | This variable sets the name of the cluster and must be the same for all nodes in the cluster.              |
|                             | It will set the cluster_name option of cassandra.yaml.                                                     |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_NUM_TOKENS        | This variable sets number of tokens for this node.                                                         |
|                             | It will set the num_tokens option of cassandra.yaml.                                                       |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_DC                | This variable sets the datacenter name of this node.                                                       |
|                             | It will set the dc option of cassandra-rackdc.properties.                                                  |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_RACK              | This variable sets the rack name of this node. It will set the rack option of cassandra-rackdc.properties. |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_ENDPOINT_SNITCH   | This variable sets the snitch implementation this node will use. It will set the endpoint_snitch option of |
|                             | cassandra.yml.                                                                                             |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
| CASSANDRA_DAEMON            | The Cassandra entry-point class: ``org.apache.cassandra.service.ElassandraDaemon`` to start                |
|                             | with ElasticSearch enabled (default), ``org.apache.cassandra.service.ElassandraDaemon`` otherwise.         |
+-----------------------------+------------------------------------------------------------------------------------------------------------+
