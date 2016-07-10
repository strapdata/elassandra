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

Enable elassandra repository
----------------------------

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

   root@deb8-1:~# update-alternatives --auto java

Verify
------

.. code:: bash

   root@deb8-1:~# java -version
   java version "1.8.0_91"
   Java(TM) SE Runtime Environment (build 1.8.0_91-b14)
   Java HotSpot(TM) 64-Bit Server VM (build 25.91-b14, mixed mode)

Install Elassandra
------------------

.. code:: bash

   root@deb8-1:~# apt-get install elassandra

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

Start Cassandra
---------------
.. code:: bash

   [root@cos7-2 logs]# systemctl start elassandra
   [root@cos7-2 logs]# systemctl status elassandra
   ● elassandra.service - Elassandra (Cassandra with ElasticSearch integration) service
   Loaded: loaded (/usr/lib/systemd/system/elassandra.service; disabled; vendor preset: disabled)
   Active: active (running) since dim. 2016-05-22 03:19:44 CEST; 3s ago
   Docs: https://github.com/vroyer/elassandra
   Main PID: 4499 (elassandra)
   CGroup: /system.slice/elassandra.service
           └─4499 /bin/bash /opt/elassandra/bin/elassandra start

   mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Started Elassandra   (Cassandra with ElasticSearch integration) service.
   mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Starting Elassandra (Cassandra with ElasticSearch integration) service...
   mai 22 03:19:44 cos7-2.xcourmont.org su[4500]: (to esandra) root on none

Stop elassandra
---------------
.. code:: bash

   [root@cos7-2 logs]# systemctl stop elassandra
   [root@cos7-2 logs]# systemctl status elassandra
   ● elassandra.service - Elassandra (Cassandra with ElasticSearch integration) service
   Loaded: loaded (/usr/lib/systemd/system/elassandra.service; disabled; vendor preset: disabled)
   Active: inactive (dead)
     Docs: https://github.com/vroyer/elassandra
   mai 22 03:18:17 cos7-2.xcourmont.org elassandra[4216]: [34B blob data]
   mai 22 03:18:36 cos7-2.xcourmont.org systemd[1]: Stopping Elassandra (Cassandra with ElasticSearch integration) service...
   mai 22 03:18:38 cos7-2.xcourmont.org systemd[1]: Stopped Elassandra (Cassandra with ElasticSearch integration) service.
   mai 22 03:18:52 cos7-2.xcourmont.org systemd[1]: Stopped Elassandra (Cassandra with ElasticSearch integration) service.
   mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Started Elassandra (Cassandra with ElasticSearch integration) service.
   mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Starting Elassandra (Cassandra with ElasticSearch integration) service...
   mai 22 03:19:44 cos7-2.xcourmont.org su[4500]: (to esandra) root on none
   mai 22 03:19:50 cos7-2.xcourmont.org elassandra[4499]: [34B blob data]
   mai 22 03:20:13 cos7-2.xcourmont.org systemd[1]: Stopping Elassandra (Cassandra with ElasticSearch integration) service...
   mai 22 03:20:15 cos7-2.xcourmont.org systemd[1]: Stopped Elassandra (Cassandra with ElasticSearch integration) service.

Review Elassandra status
------------------------
.. code:: bash

   root@deb8-1:/opt/elassandra/data# systemctl status elassandra
   ● elassandra.service - Elassandra (Cassandra with ElasticSearch integration) service
      Loaded: loaded (/lib/systemd/system/elassandra.service; disabled)
      Active: active (running) since jeu. 2016-07-07 01:54:00 CEST; 2 days ago
      Docs: https://github.com/vroyer/elassandra
   Main PID: 3981 (java)
   CGroup: /system.slice/elassandra.service
           └─3981 java -Djava.library.path=/opt/elassandra/lib/sigar-bin -ea -javaagent:/opt/elassandra/lib/jamm-0.3.0.jar -XX:+CMSClassUnloading...

   juil. 07 01:54:00 deb8-1.xcourmont.org su[3902]: Successful su for cassandra by root
   juil. 07 01:54:00 deb8-1.xcourmont.org su[3902]: + ??? root:cassandra
   juil. 07 01:54:00 deb8-1.xcourmont.org su[3902]: pam_unix(su:session): session opened for user cassandra by (uid=0)
   juil. 07 01:54:00 deb8-1.xcourmont.org elassandra[3901]: Starting Elassandra: CASSANDRA_HOME=/opt/elassandra
   juil. 07 01:54:05 deb8-1.xcourmont.org elassandra[3901]: [13B blob data]

Enable elassandra at boot time
------------------------------

.. code:: bash

   [root@cos7-1 ~]# systemctl enable elassandra
   Created symlink from /etc/systemd/system/multi- user.target.wants/ elassandra.service to       /usr/lib/systemd/system/elassandra.service.
