
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

1. Enable EPEL repository

   .. code:: bash
   
      yum -y install epel-release

2. Install Cassandra Driver

   .. code:: bash
   
      yum -y install python-pip python-cassandra-driver wget
      pip install --upgrade pip
      pip install --upgrade setuptools
      pip install --upgrade cassandra-driver

3. Get and Install Oracle Java

   .. note:: Recommended JAVA is Oracle_
   
   .. _Oracle: http://www.oracle.com/technetwork/java/javase/downloads/index.html[Oracle JRE Download page]
   
   .. code:: bash
   
      rpm -ivh /path/to/downloaded/file

4. Set Alternative

   .. code:: bash
   
      alternatives --config java
      There are 2 programs which provide 'java'.
        Selection    Command
      + 1           /usr/java/jre1.8.0_91/bin/java
      *  2           /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.91-  0.b14.el7_2.x86_64/jre/bin/java
      Enter to keep the current selection[+], or type selection number:

5. Declare Elassandra repository or download. 

   If your server don't have access to internet, you can download the packages on `http://packages.elassandra.io/rpm/elassandra`_.

   .. code:: bash
   
      cat << _EOF_ > /etc/yum.repos.d/elassandra.repo
      [Elassandra]
      name=Elassandra
      baseurl=https://packages.elassandra.io/rpm/elassandra/
      gpgcheck=1
      enabled=1
      _EOF_

6. Import Elassandra GPG public key and verify it

   .. code:: bash
   
      # wget -O- -q http://packages.elassandra.io/pub/RPM-GPG-KEY-Elassandra > /tmp/RPM-GPG-KEY-Elassandra
      # rpm --import /tmp/RPM-GPG-KEY-Elassandra
      # rm -f /tmp/RPM-GPG-KEY-Elassandra
      # rpm -qa gpg-pubkey --qf "%{version}-%{release} %{summary}\n"
      f1d18d84-5724b296 gpg(Elassandra <build@elassandra.org>)
      34ec9cba-54e38751 gpg(Fedora (23) <fedora-23-primary@fedoraproject.org>)

7. Install JNA for Redhat and CentOS

   .. code:: bash
   
      sudo yum install jna

      
8. Install Elassandra

   .. code:: bash
   
      yum clean all --disablerepo=* --enablerepo=Elassandra
      yum install elassandra

See Configuration chapter to configure elassandra before starting.


9. Start, Stop, and check Status of Elassandra (for systemd enabled systems, RHEL and CentOS > 7)

   .. code:: bash
   
      systemctl start elassandra
      systemctl status elassandra
      systemctl stop elassandra

   To enable elassandra at boot time

   .. code:: bash
   
      systemctl enable elassandra

