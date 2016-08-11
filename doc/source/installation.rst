Installation
============

Building from source
--------------------

* Elassandra uses `Maven <http://maven.apache.org>`_ for its build system. Simply run :

1. ``git clone http://github.com/vroyer/elassandra.git``
2. ``git clone http://git-wip-us.apache.org/repos/asf/cassandra.git``
3. ``git checkout cassandra-2.2.x``
4. ``mvn clean package -Dmaven.test.skip=true  -Dcassandra.home=<path/to/cassandra>`` command in the elassandra cloned directory. The distribution will be created under *target/releases*.

Tarball Installation
--------------------

* Install Java version 8 (check version with `java -version`). Version 8 is recommended, see `Installing Oracle JDK on RHEL-based Systems <http://docs.datastax.com/en/cassandra/2.2/cassandra/install/installJdkRHEL.html>`_.
* Apply OS settings for cassandra, see `Recommended production settings for Linux <http://docs.datastax.com/en/cassandra/2.2/cassandra/install/installRecommendSettings.html>`_
* For linux, install jemalloc (yum install jemalloc).
* Download Elassandra tarball from `elassandra repository <https://github.com/vroyer/elassandra/releases>`_ and extract files in your installation directory.
* Install the cassandra driver ``pip install cassandra-driver`` and the cqlsh utility ``python pylib/setup.py install``.
* Configure your cassandra cluster (cluster name, snitch, ip address, seed...), see `cassandra configuration <http://docs.datastax.com/en/cassandra/2.0/cassandra/initialize/initializeMultipleDS.html>`_. Default Elasticsearch configuration is located in ``conf/elasticsearch.yml``, but most of configuration is inherited from the cassandra.yml (cluster name, listen address, paths, etc...). .
* Configure cassandra and elasticsearch logging in conf/logback.xml, see `logback framework <http://logback.qos.ch/>`_.

RPM Installation
----------------

.. include:: install_rpm.rst

DEB Installation
----------------

.. include:: install_deb.rst

Docker Installation
-------------------

.. include:: docker.rst

