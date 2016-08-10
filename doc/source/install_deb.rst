===================================
Install with DEB (Debian,Ubuntu...)
===================================

.. note:: You need to be root, or use sudo for each commands

.. note:: This procedure has been tested on :

   - Debian 8.4

.. warning:: If you had  install elassandra before, with a version < 2.1.1-14, you should uninstall it prior to  install this version.
   Once  the new version of Elassandra has been installed, check that all the files are owned by the user cassandra.

   .. code:: bash

      apt-get remove elassandra
      ...
      apt-get install elassandra
      ...
      find /opt/elassandra \! -user cassandra -exec chown cassandra:cassandra {} \; -ls

.. contents:: :depth: 3

Install Elassandra and needed dependancies
==========================================

Enable distribution testing repository
--------------------------------------

Put this lines in a file in /etc/apt/sources.list.d (for example testint.list)

.. code:: bash

   cat << _EOF_ > /etc/apt/sources.list.d/testing.list
   deb     http://mirror.steadfast.net/debian/ testing main contrib non-free
   deb     http://ftp.us.debian.org/debian/    testing main contrib non-free
   _EOF_

Enable elassandra repository or download.
-----------------------------------------
If your server don't have access to internet, you can download the packages on http://packages.elassandra.io/deb.

Put this lines in a file in /etc/apt/sources.list.d (for example elassandra.list)

.. code:: bash

   cat << _EOF_ > /etc/apt/sources.list.d/elassandra.list
   deb http://packages.elassandra.io/deb/ ./
   _EOF_

Import Elassandra GPG public key
--------------------------------

.. code:: bash

   wget -O- -q http://packages.elassandra.io/pub/GPG-KEY-Elassandra > /tmp/GPG-KEY-Elassandra
   apt-key add  /tmp/GPG-KEY-Elassandra

Install Cassandra Driver, and some useful tools
-----------------------------------------------

.. code:: bash

   apt-get install python-pip python-cassandra wget curl libjemalloc1
   pip install --upgrade pip
   pip install --upgrade cassandra-driver
   pip install cqlsh

Get and Install Java
--------------------
.. note:: Recommended JAVA is Oracle

   You can see the Download_ page on Oracle WebSite and install it.

   .. _Download: http://www.oracle.com/technetwork/java/javase/downloads/index.html

   Or, you can use our java .deb packages

.. code:: bash

   apt-get install oracle-java8-jre

.. note:: You need to approve even if apt-get says it can't verify packages...All the needed configuration on our side has not be done...

Update Alternative for JAVA
---------------------------

.. code:: bash

   update-alternatives --auto java

Verify
------

.. code:: bash

   java -version
   java version "1.8.0_91"
   Java(TM) SE Runtime Environment (build 1.8.0_91-b14)
   Java HotSpot(TM) 64-Bit Server VM (build 25.91-b14, mixed mode)

Install Elassandra
------------------

.. code:: bash

   apt-get clean
   apt-get install elassandra

Configuration
=============

Elassandra administrator user
-----------------------------
.. note:: This version of package uses the user **cassandra** as administrator. If you want to use another user, make sure all files in /opt/elassandra are readable and possibly executable (at least for /opt/elassandra/bin) for your user. If you want to reuse some Cassandra datas, make sure that these data files (your Cassandra database files) are readable and writable.

Switch to elassandra administrator user
---------------------------------------

.. code::

   su - cassandra

Cluster Name
------------

.. code:: bash

   CLUSTER="MON_CLUSTER" # replace with whatever you want
   sed -i -e "s/cluster_name: 'Test Cluster'/cluster_name: '${CLUSTER}'/g" ${CASSANDRA_CONF}/cassandra.yaml

Configure listen address or interface
-------------------------------------

Installation should have set rpc_interface and listen_interface to the NIC where hostname --ip-address is set in /opt/elassandra/conf/cassandra.yaml.

If you prefer you can use listen_adress and rpc_address.

Setting seed address
--------------------

You must set seeds, with at least one members address, preferably two in case of a cluster setup :

.. code:: bash

   SEED_IPs="IP_HOST[1],IP_HOST[2]"  # replace with your own values
   sed -i -e "s/- seeds: \"127.0.0.1\"/- seeds: \"${SEED_IPs}\"/g" ${CASSANDRA_CONF}/cassandra.yaml

Manage Elassandra instances
===========================

.. note:: You need to be root, or use sudo to run the following commands

.. note:: These commands work for systemd enabled systems (RHEL and CentOS > 7).

Start Cassandra
---------------
.. code:: bash

   systemctl start elassandra

Stop elassandra
---------------
.. code:: bash

   systemctl stop elassandra

Review Elassandra status
------------------------
.. code:: bash

   /opt/elassandra/data# systemctl status elassandra

Enable elassandra at boot time
------------------------------

.. code:: bash

   systemctl enable elassandra
