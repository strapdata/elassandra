
.. note:: This procedure has been tested on :

   - Debian 8
   - Ubuntu 16.04

Currently we do not have a repository. So first, download the Elassandra debian package:

.. parsed-literal::
    wget \https://github.com/strapdata/elassandra/releases/download/|release|/elassandra-|version|\_all.deb


You need java 8 installed, for instance::

  sudo apt-get update && sudo apt-get install default-jre


Also install Python, pip, and cassandra-driver::

  sudo apt-get update && sudo apt-get install python python-pip
  sudo pip install cassandra-driver

Now install Elassandra:

.. parsed-literal::
    sudo dpkg -i elasandra-|version|\_all.deb

This should automatically start the systemd or sysV service, let's check::

  # with systemd
  sudo systemctl status cassandra

  # with sysV
  sudo service cassandra status

If you want elassandra to be started on boot, run::

  # with systemd
  sudo systemctl enable cassandra

  # with sysV
  sudo service cassandra enable

Use commands ``start``, ``stop`` and ``restart`` to control the Elassandra daemon.

Configurations are located in ``/etc/cassandra``, data storage is in ``/var/lib/cassandra``,
logs are in ``/var/log/cassandra/system.log``, while modules, plugins, libraries, and ``cassandra.in.sh``
are located in ``/usr/share/cassandra``.
