======================================
Install with RPM (Fedora,RHEL, CentOS)
======================================

.. note:: Tested on :

   - CentOS 7.2
   - Fedora 24 (without SELinux)

.. warning:: You should pay attention to this bug Bug1_

.. warning:: If you had  install elassandra before, with a version < 2.1.1-14, you should uninstall it prior to  install this version.
   Once  the new version of Elassandra has been installed, check that all the files are owned by the user cassandra.

   .. code:: bash

      apt-get remove elassandra
      ...
      apt-get install elassandra
      ...
      find /opt/elassandra \! -user cassandra -exec chown cassandra:cassandra {} \; -ls

.. _Bug1: https://support.datastax.com/hc/en-us/articles/206259833-Nodes-appear-unresponsive-due-to-a-Linux-futex-wait-kernel-bug[ Nodes appear unresponsive due to a Linux futex_wait() kernel bug]

.. contents:: :depth: 3

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

Declare Elassandra repository or download
-----------------------------------------

If your server don't have access to internet, you can download the packages on http://packages.elassandra.io/rpm/elassandra.

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

.. note:: Elassandra administrator user has been changed to cassandra to be compliant with cassandra tools

.. code:: bash

   su - cassandra

Set Cluster Name
----------------

.. code:: bash

   CLUSTER="MON_CLUSTER" # replace with whatever you want
   sed -i -e "s/cluster_name: 'Test Cluster'/cluster_name: '${CLUSTER}'/g" ${CASSANDRA_CONF}/cassandra.yaml

Setting seed adress
-------------------

You must set seeds, with at least one members address, preferably two in case of a cluster setup

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

.. note:: These commands work for systemd enabled systems (RHEL and CentOS > 7).

Start Elassandra
----------------

.. code:: bash

   systemctl start elassandra

Review Elassandra Status
------------------------

.. code:: bash

   systemctl status elassandra

Stop elassandra
---------------

.. code:: bash

   systemctl stop elassandra

Enable elassandra at boot time
------------------------------

.. code:: bash

   systemctl enable elassandra

