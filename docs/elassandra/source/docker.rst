.. note:: 

This image is an adptation of Cassandra Docker Image, with ElasticSearch Elassandra integration. 
This documentation is based on official `Cassandra Docker doc`_, with some adpatation needed for ElasticSearch integration.

.. _`Cassandra Docker Doc`: https://hub.docker.com/_/cassandra/

.. note:: 

   Tested on Docker version 1.11.2.

Get a Docker image
__________________

- Docker Hub 
   To get the lastest image and if you havec access to docker hub, you can find it on docker hub :

   .. code:: bash

      $ docker search elassandra
      NAME                     DESCRIPTION                               STARS     OFFICIAL   AUTOMATED
      xawcourmont/elassandra   Elassandra = ElasticSearch + Cassandra    0

- Download
   Download docker image on https://packages.elassandra.io/docker and import it :

   .. code:: bash

      docker import /path/to/elassandra-X.X.X-XX.tgz
      [root@docker1 ~]# docker import elassandra-2.1.1-15.1.tar.gz
      sha256:3990a7e38b17b27c171833255e4f96e1b816ebbbadf4bd47e0089ab173f7bb58
      [root@docker1 ~]# docker images
      REPOSITORY                         TAG                 IMAGE ID            CREATED             SIZE
      <none>                             <none>              3990a7e38b17        15 seconds ago      686.3 MB
      docker.io/xawcourmont/elassandra   2.1.1-10.1          353345dcb9f7        4 weeks ago         732.4 MB
      docker.io/debian                   latest              1b088884749b        5 weeks ago         125.1 MB
      [root@docker1 ~]# docker tag 3990a7e38b17 elassandra:latest
      [root@docker1 ~]# docker images
      REPOSITORY                         TAG                 IMAGE ID            CREATED             SIZE
      elassandra                         latest              3990a7e38b17        4 minutes ago       686.3 MB
      docker.io/xawcourmont/elassandra   2.1.1-10.1          353345dcb9f7        4 weeks ago         732.4 MB
      docker.io/debian                   latest              1b088884749b        5 weeks ago         125.1 MB


Start an elassandra server instance
___________________________________

Witch dockerhub access, starting an Elassandra instance is simple:

.. code:: bash

    docker run --name my-elassandra-node1 -d -P xawcourmont/elassandra:tag


Where my-elassandra-node1 is the name you want to assign to your container and tag is the docker image version (default is the latest).

.. note:: By default, elassandra stored data in the container's "space", which may be appropriated if you have a large amount of data. Instead, you may want to mount a data volume in your container. Elassandra exposes the volume /var/lib/cassandra.

.. note:: "-P" is use to expose elassandra needed tcp ports. Doing so, docker will use "dynamic" port mapping. You can set wanted host ports values with

   .. code:: bash

      -p %{host_port}:%{cassandra_port}.


You can view the status of your cassandra node with :

.. code:: bash

   docker exec my-elassandra-node1 nodetool status

You can view the status of your ElasticSearch node with :

.. code:: bash

   curl -XGET 'http://$(hostname):%{host_port}/_cluster/state/?pretty=true'

%{host_port} is the port linked to the 9200 port in the container. if you use "dynamic" mapping, you can find it with :

.. code:: bash

   docker inspect --format='{{ (index (index .NetworkSettings.Ports "9200/tcp") 0).HostPort }}' my-elassandra-node1


Stop your elassandra container with the docker stop command

.. code:: bash

   docker stop my-elassandra-node

Connecting to an Eassandra cluster
__________________________________

**Connect to Elassandra from an application in another Docker container**

This image exposes the standard elassandra ports, so container linking makes the Elassandra instance available to other application containers. 
Start your application container like this in order to link it to the Elassandra container:

.. code:: bash

  docker run --name my-app-container --link my-elassandra-node1:elassandra -d image-which-use-elassandra

Using the environment variables documented below, there are two cluster scenarios :
   
For instances on the same machine
   Start the first instance as described above. To start other instances, just tell each new node where the first is.
   
   .. code:: bash
   
      docker run -d --name my-elassandra-node1 -e CASSANDRA_RACK=MYRACK1 -e CASSANDRA_CLUSTER_NAME="MYCLUSTER"  -P   xawcourmont/elassandra:tag
      docker run -d --name my-elassandra-node2 -e CASSANDRA_RACK=MYRACK2 -e CASSANDRA_SEEDS="$(docker inspect --format='{{.NetworkSettings.IPAddress }}' my-elassandra-node1)"  -P   xawcourmont/elassandra:tag

   where my-elassandra-node1 is the name of your original elassandra Server container, taking advantage of docker inspect to get the IP address of the other container.

   Or you may use the docker run --link option to tell the new node where the first is:

   .. code:: bash
   
      docker run --name my-elassandra-node2 -d --link my-elassandra-node1:elassandra elassandra:tag
   
   .. note::  Due to how Cassandra NODE_ID is calculated, you may need to "change something" in your second container. That is why we set CASSANDRA_RACK

For instances running on separate machines
   The easiest way to run across multiple Docker hosts is with --net=host. This tells Docker to leave the container's networking in the host's namespace.
   
   .. code:: bash
   
      docker run --name my-elassandra-node1 -d --net=host  xawcourmont/elassandra:tag
   
   Then start a Elassandra container on the second machine, with the exposed gossip port and seed pointing to the first machine:
   
   .. code:: bash
   
      docker run --name my-elassandra-node2 -d  --net=host -e ELASSANDRA_SEEDS=%{ip_of_first_docker_host} elassandra:tag
   
   .. note:: If you use Firewall, you must allow traffic between each containers in the cluster as shown below :
   
      .. code:: bash
      
         firewall-cmd  --new-service=elassandra
         firewall-cmd  --permanent --new-service=elassandra
         firewall-cmd  --permanent --service=elassandra --add-port=900/tcp
         firewall-cmd --get-default-zone
         firewall-cmd --zone=FedoraServer --add-service=elassandra
         firewall-cmd --permanent --zone=FedoraServer --add-service=elassandra


**Connect to Cassandra from cqlsh**


The following command starts another ELassandra container instance and runs cqlsh (Cassandra Query Language Shell) against your original Elassandra container, allowing you to execute CQL statements against your database instance:

.. code:: bash

   docker run -it --link my-elassandra-instance:elassandra --rm elassandra sh -c 'exec cqlsh "$ELASSANDRA_PORT_9042_TCP_ADDR"'

 or (simplified to take advantage of the /etc/hosts entry Docker adds for linked containers):

.. code:: bash

   docker run -it --link some-cassandra:cassandra --rm cassandra cqlsh cassandra

where some-cassandra is the name of your original Cassandra Server container. More information about the CQL can be found in the Cassandra documentation.


**Container shell access and viewing Cassandra logs**


The docker exec command allows you to run commands inside a Docker container. The following command line will give you a bash shell inside your cassandra container:

.. code:: bash

   docker exec -it some-cassandra bash

The Cassandra Server latest logs is available through Docker's container log:

.. code:: bash

   docker logs some-cassandra


Environment Variables
_____________________

When you start the Elassandra image, you can adjust the configuration of the Elassandra instance by passing one or more environment variables on the docker run command line. We already have seen some of them.

.. cssclass:: table-bordered

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


Where to Store Data
___________________

There are several ways to store data used by applications that run in Docker containers. You should familiarize yourselves with the options available, including:


- Let Docker manage the storage of your database data by writing the database files to disk on the host system using its own internal volume management.This is the default and is easy and fairly transparent to the user. The downside is that the files may be hard to locate for tools and applications that run directly on the host system, i.e. outside containers.

- Create a data directory on the host system (outside the container) and mount this to a directory visible from inside the container. This places the database files in a known location on the host system, and makes it easy for tools and applications on the host system to access the files. The downside is that the user needs to make sure that the directory exists, and that e.g. directory permissions and other security mechanisms on the host system are set up correctly.


The Docker documentation is a good starting point for understanding the different storage options and variations, and there are multiple blogs and forum postings that discuss and give advice in this area. We will simply show the basic procedure here for the latter option above:
Create a data directory on a suitable volume on your host system, e.g. /my/own/datadir.

.. note:: Users on host systems with SELinux enabled may see issues with it. One solution is to append :z or :Z to the volume. This will trigger a relabellization of the host directory, with container cgroup id.
   Add mapping for your volume when you start your elassandra container with:

   .. code:: bash

      -v /my/own/datadir:/opt/elassandra/data;Z

   The -v /my/own/datadir:/var/lib/cassandra part of the command mounts the /my/own/datadir directory from the underlying host system as /opt/elassandra inside the container, where Elassandra by default will write its data files.


If there is no database initialized when the container starts, then a default database will be created. While this is the expected behavior, this means that it will not accept incoming connections until such initialization completes. This may cause issues when using automation tools, such as docker-compose, which start several containers simultaneously. Likewise, when starting clustered docker instances, you should wait until the first one accept connections, before starting another one, specifying seeds.
