

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


Run this as root :

1. Enable distribution testing repository


   Add this lines in a file located in /etc/apt/sources.list.d (for example testint.list)
   
   .. code:: bash
   
      cat << _EOF_ > /etc/apt/sources.list.d/testing.list
      deb     http://mirror.steadfast.net/debian/ testing main contrib non-free
      deb     http://ftp.us.debian.org/debian/    testing main contrib non-free
      _EOF_

2. Enable elassandra repository or download the debian package.

   If your server don't have access to internet, you can download the packages on `http://packages.elassandra.io/deb`_.
   
   Put this lines in a file in /etc/apt/sources.list.d (for example elassandra.list)
   
   .. code:: bash
   
      cat << _EOF_ > /etc/apt/sources.list.d/elassandra.list
      deb http://packages.elassandra.io/deb/ ./
      _EOF_

3. Import Elassandra GPG public key

   .. code:: bash
   
      wget -O- -q http://packages.elassandra.io/pub/GPG-KEY-Elassandra > /tmp/GPG-KEY-Elassandra
      apt-key add  /tmp/GPG-KEY-Elassandra

4. Install Cassandra Driver, and cqlsh

   .. code:: bash
   
      apt-get install python-pip python-cassandra wget curl libjemalloc1
      pip install --upgrade pip
      pip install --upgrade cassandra-driver
      pip install cqlsh

5. Get and Install Java

   .. note:: Recommended JAVA is Oracle
   
      You can see the Download_ page on Oracle WebSite and install it.
   
      .. _Download: http://www.oracle.com/technetwork/java/javase/downloads/index.html
   
      Or, you can use our java .deb packages
   
   .. code:: bash
   
      apt-get install oracle-java8-jre
   
   .. note:: You need to approve even if apt-get says it can't verify packages...All the needed configuration on our side has not be done...

6. Update Alternative for JAVA


   .. code:: bash
   
      update-alternatives --auto java

7. Verify


   .. code:: bash
   
      java -version
      java version "1.8.0_91"
      Java(TM) SE Runtime Environment (build 1.8.0_91-b14)
      Java HotSpot(TM) 64-Bit Server VM (build 25.91-b14, mixed mode)

8. Install JNA for Debian and Ubuntu

   .. code:: bash
   
      sudo apt-get install libjna-java
      ln -s /usr/share/java/jna.jar install_location/lib
     

9. Install Elassandra


   .. code:: bash
   
      apt-get clean
      apt-get install elassandra
    
      See configuration_ chapter to configure elassandra before starting.
    

10. Start/Stop/Status Elassandra

   .. note:: You need to be root, or use sudo to run the following commands. These commands work for systemd enabled systems (RHEL and CentOS > 7).
   
   .. code:: bash
   
      systemctl start elassandra
      systemctl stop elassandra
      systemctl status elassandra  


   To enable elassandra at boot time

   .. code:: bash
   
      systemctl enable elassandra


