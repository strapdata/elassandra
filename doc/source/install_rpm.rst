======================================
Install with RPM (Fedora,RHEL, CentOS)
======================================

.. note:: Tested on :

   - CentOS 7.2
   - Fedora 24 (without SELinux)

.. warning:: You should pay attention to this bug Bug1_

.. warning:: If you had  install elassandra before, with a version < 2.1.1-14, you should uninstall it prior to  install this version...

.. _Bug1: https://docs.datastax.com/en/cassandra/2.1/cassandra/troubleshooting/trblshootFetuxWaitBug.html[ Nodes appear unresponsive due to a Linux futex_wait() kernel bug]

.. contents:: Table of contents
    :depth: 2

Installation
============

Enable EPEL repository
----------------------

.. code:: bash

   yum -y install epel-release

Install Cassandra Driver
------------------------

.. code:: bash

   yum -y install python-pip python-cassandra-driver wget
   pip install --upgrade pip
   pip install --upgrade setuptools
   pip install --upgrade cassandra-driver

Get and Install Oracle Java
---------------------------
.. note:: Recommended JAVA is Oracle_

.. _Oracle: http://www.oracle.com/technetwork/java/javase/downloads/index.html[Oracle JRE Download page^]

.. code:: bash

   rpm -ivh /path/to/downloaded/file

Set Alternative
------------------

.. code:: bash

   alternatives --config java
   There are 2 programs which provide 'java'.
     Selection    Command
   + 1           /usr/java/jre1.8.0_91/bin/java
   *  2           /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.91-  0.b14.el7_2.x86_64/jre/bin/java
   Enter to keep the current selection[+], or type selection number:

Declare Elassandra repository
-----------------------------

.. code:: bash

   cat << _EOF_ > /etc/yum.repos.d/elassandra.repo
   [Elassandra]
   name=Elassandra
   baseurl=https://packages.elassandra.io/rpm/elassandra/
   gpgcheck=1
   enabled=1
   _EOF_

Import Elassandra GPG public key
--------------------------------

.. code:: bash

   [root@cos7-1]# wget -O- -q http://packages.elassandra.io/pub/RPM-GPG-KEY-Elassandra > /tmp/RPM-GPG-KEY-Elassandra
   [root@cos7-1]# rpm --import /tmp/RPM-GPG-KEY-Elassandra
   [root@cos7-1]# rm -f /tmp/RPM-GPG-KEY-Elassandra

Verify
------

.. code:: bash

   [root@rpmbld1 ~]# rpm -qa gpg-pubkey --qf "%{version}-%{release} %{summary}\n"
   f1d18d84-5724b296 gpg(Elassandra <build@elassandra.org>)
   34ec9cba-54e38751 gpg(Fedora (23) <fedora-23-primary@fedoraproject.org>)

Install Elassandra
------------------

.. code:: bash

   yum clean all --disablerepo=* --enablerepo=Elassandra
   yum install  elassandra

Configuration
=============

Switch to elassandra administrator user
----------------------------------------

.. note:: Elassandra administrator user has been change to cassandra to be compliant with cassandra tools

.. code:: bash

   su - cassandra

Configuration for cluster mode
------------------------------

.. code:: bash

   CLUSTER="MON_CLUSTER" # replace with whatever you want
   sed -i -e "s/cluster_name: 'Test Cluster'/cluster_name: '${CLUSTER}'/g" ${CASSANDRA_CONF}/cassandra.yaml

Setting seed adress
-------------------

If you want to run an elassandra cluster, you must set seeds, with at least one members address, preferably two :

.. code:: bash

   SEED_IPs="IP_HOST[1],IP_HOST[2]"  # replace with your own values
   sed -i -e "s/- seeds: \"127.0.0.1\"/- seeds: \"${SEED_IPs}\"/g" ${CASSANDRA_CONF}/cassandra.yaml

.. note:: If you want to start a standalone node, you can jump directly to `Manage Elassandra`_.

Configure listen address or interface
-------------------------------------

Installation should have set rpc_interface and listen_interface to the NIC where hostname --ip-address is set in /opt/elassandra/conf/cassandra.yaml.

If you prefer you can use listen_address and rpc_address.

   
Manage Elassandra
=================

.. note:: You will need to be root, or use sudo to run these commands

Start Elassandra
----------------

.. code:: bash

   [root@cos7-2 logs]# systemctl start elassandra
   [root@cos7-2 logs]# systemctl status elassandra
   ● elassandra.service - Elassandra (Cassandra with ElasticSearch integration)  service
      Loaded: loaded (/usr/lib/systemd/system/elassandra.service; disabled; vendor preset: disabled)
      Active: active (running) since dim. 2016-05-22 03:19:44 CEST; 3s ago
      Docs: https://github.com/vroyer/elassandra
      Main PID: 4499 (elassandra)
      CGroup: /system.slice/elassandra.service
           └─4499 /bin/bash /opt/elassandra/bin/elassandra start
   mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Started Elassandra (Cassandra  with ElasticSearch integration) service.
   mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Starting Elassandra (Cassandra with ElasticSearch integration) service...
   mai 22 03:19:44 cos7-2.xcourmont.org su[4500]: (to esandra) root on none

Stop elassandra
---------------

.. code:: bash

   [root@cos7-2 logs]# systemctl stop elassandra
   [root@cos7-2 logs]# systemctl status elassandra
   ● elassandra.service - Elassandra (Cassandra with ElasticSearch integration)  service
      Loaded: loaded (/usr/lib/systemd/system/elassandra.service; disabled; vendor        preset: disabled)
      Active: inactive (dead)
      Docs: https://github.com/vroyer/elassandra
      mai 22 03:18:17 cos7-2.xcourmont.org elassandra[4216]: [34B blob data]
      mai 22 03:18:36 cos7-2.xcourmont.org systemd[1]: Stopping Elassandra    (Cassandra with ElasticSearch integration) service...
      mai 22 03:18:38 cos7-2.xcourmont.org systemd[1]: Stopped Elassandra (Cassandra with ElasticSearch integration) service.
      mai 22 03:18:52 cos7-2.xcourmont.org systemd[1]: Stopped Elassandra (Cassandra with ElasticSearch integration) service.
      mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Started Elassandra (Cassandra with ElasticSearch integration) service.
      mai 22 03:19:44 cos7-2.xcourmont.org systemd[1]: Starting Elassandra (Cassandra with ElasticSearch integration) service...
      mai 22 03:19:44 cos7-2.xcourmont.org su[4500]: (to esandra) root on none
      mai 22 03:19:50 cos7-2.xcourmont.org elassandra[4499]: [34B blob data]
      mai 22 03:20:13 cos7-2.xcourmont.org systemd[1]: Stopping Elassandra (Cassandra with ElasticSearch integration) service...
      mai 22 03:20:15 cos7-2.xcourmont.org systemd[1]: Stopped Elassandra (Cassandra with ElasticSearch integration) service.

Enable elassandra at boot time
------------------------------

.. code:: bash

   [root@cos7-1 ~]# systemctl enable elassandra
   Created symlink from /etc/systemd/system/multi-     user.target.wants/elassandra.service to    /usr/lib/systemd/system/elassandra.service.

