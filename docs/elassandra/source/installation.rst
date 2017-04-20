Installation
============

There are a number of ways to install Elassandra: from the tarball_, with the `deb package`_ or `rpm package`_, with a `docker image`_, or even from :ref:`source <source>`.

Elassandra is based on Cassandra and ElasticSearch, thus it will be easier if you're already familiar with one on these technologies.

Tarball
-------

Elassandra requires at least Java 8. Oracle JDK is the recommended version, but OpenJDK should work as well.
You can check which version is installed on your computer::

    $ java -version
    java version "1.8.0_121"
    Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
    Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

Once java is correctly installed, download the Elassandra tarball:

.. parsed-literal::
    wget \https://github.com/strapdata/elassandra/releases/download/|release|/elassandra-|version|.tar.gz

Then extract its content:

.. parsed-literal::
  tar -xzf elassandra-|version|.tar.gz

Go to the extracted directory:

.. parsed-literal::
  cd elassandra-|version|

If you need, configure ``conf/cassandra.yaml`` (cluster name, listen address, snitch, ...), then start elassandra::

  bin/cassandra -f -e

This starts an Elassandra instance in foreground, with ElasticSearch enabled. Afterwards your node is reachable on localhost on port 9042 (CQL) and 9200 (HTTP). Keep this terminal open and launch a new one.

To use cqlsh, we first need to install the Cassandra driver for python. Ensure python and pip are installed, then::

    sudo pip install cassandra-driver

Now connect to the node with cqlsh::

  bin/cqlsh

Then you must be able to type CQL commands. See the `CQL reference <https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cqlReferenceTOC.html>`_.

Also, we started Elassandra with ElasticSearch enabled (according to the ``-e`` option), so let's request the REST API::

  curl -X GET http://localhost:9200/

You should get something like:

.. parsed-literal::
  {
    "name" : "127.0.0.1",
    "cluster_name" : "Test Cluster",
    "cluster_uuid" : "7cb65cea-09c1-4d6a-a17a-24efb9eb7d2b",
    "version" : {
      "number" : "|version|",
      "build_hash" : "b0b4cb025cb8aa74538124a30a00b137419983a3",
      "build_timestamp" : "2017-04-19T13:11:11Z",
      "build_snapshot" : true,
      "lucene_version" : "5.5.2"
    },
    "tagline" : "You Know, for Search"
  }

You're ready for playing with Elassandra. For instance, try to index a document with the ElasticSearch API, then from cqlsh look for the keyspace/table/row automatically created. Cassandra now benefits from dynamic mapping !

On a production environment, it's better to modify some system settings like disabling swap. This `guide <http://docs.datastax.com/en/landing_page/doc/landing_page/recommendedSettings.html>`_ shows you how to.
On linux, consider installing `jemalloc <http://jemalloc.net/>`_.

DEB package
----------------

.. include:: install_deb.rst

RPM package
-----------
..
  .. include:: install_rpm.rst

The rpm package is coming soon. You'll have to install Elassandra with the `tarball`_.

Docker image
------------

.. include:: docker.rst

.. _source:

Build from source
--------------------

Requirements:
  - Oracle JDK 1.8 or OpenJDK 8
  - maven >= 3.5

Clone Elassandra repository and Cassandra sub-module::

   git clone --recursive git@github.com:strapdata/elassandra.git
   cd elassandra

Elassandra uses `Maven <http://maven.apache.org>`_ for its build system. Simply run::

    mvn clean package -DskipTests

It's gonna take a while, you might go for a cup of tea.

If everything succeed, tarballs will be built in:

.. parsed-literal::
  distribution/tar/target/release/elasandra-|version|-SNAPSHOT.tar.gz
  distribution/zip/target/release/elasandra-|version|-SNAPSHOT.zip

Then follow the instructions for `tarball`_ installation.
