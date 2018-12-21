Installation
============

There are a number of ways to install Elassandra: from the tarball_, with the `deb`_ or `rpm`_ package, with a `docker image`_, or even from :ref:`source <source>`.

Elassandra is based on Cassandra and ElasticSearch, thus it will be easier if you're already familiar with one on these technologies.

Tarball
-------

Elassandra requires at least Java 8. Oracle JDK is the recommended version, but OpenJDK should also work as well.
You need to check which version is installed on your computer::

    $ java -version
    java version "1.8.0_121"
    Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
    Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

Once java is correctly installed, download the Elassandra tarball:

.. parsed-literal::

    wget |tgz_url|

Then extract its content:

.. parsed-literal::
  tar -xzf elassandra-|release|.tar.gz

Go to the extracted directory:

.. parsed-literal::
  cd elassandra-|release|

Configure ``conf/cassandra.yaml`` if necessary, and then run::

  bin/cassandra -e

This has started cassandra with elasticsearch enabled (according to the ``-e`` option).

Get the node status::

  bin/nodetool status

Now connect to the node with cqlsh::

  bin/cqlsh

You're now able to type CQL commands. See the `CQL reference <https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cqlReferenceTOC.html>`_.

Check the elasticsearch API::

  curl -X GET http://localhost:9200/

You should get something like this:

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

You're done !

On a production environment, we recommand to to modify some system settings such as disabling swap. This `guide <http://docs.datastax.com/en/landing_page/doc/landing_page/recommendedSettings.html>`_ shows you how to do it.
On linux, you should instal `jemalloc <http://jemalloc.net/>`_.


deb
---

.. include:: adhoc_deb.rst

rpm
---

.. include:: adhoc_rpm.rst

Docker image
------------

.. include:: docker.rst

.. _source:

Build from source
-----------------

Requirements:
  - Oracle JDK 1.8 or OpenJDK 8
  - gradle >= 3.5

Clone Elassandra repository and Cassandra sub-module:

.. code::

   git clone --recursive git@github.com:strapdata/elassandra.git
   cd elassandra

Elassandra 5.5+ uses `Gradle <https://gradle.org/>`_ for its build system. Simply run:

.. code::

   gradle assemble

or to build only the tgz tarball for a release version : 

.. code::

   gradle assemble distribution:tar:assemble -Dbuild.snapshot=false

It's going to take a little while so, you may want to go for a cuppa ! If all goes well, tarballs will be built in:

.. parsed-literal::
  distribution/(tar|zip|rpm|deb)/build/distributions/

Then follow the instructions for `tarball`_ installation.
