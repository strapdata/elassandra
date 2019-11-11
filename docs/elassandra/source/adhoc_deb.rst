
.. important:: Cassandra and Elassandra packages conflict. You should remove Cassandra prior to install Elassandra.

The Java Runtime 1.8 is required to run Elassandra. On recent distributions it should be resolved automatically as a dependency.
On Debian Jessie it can be installed from backports::

  sudo apt-get install -t jessie-backports openjdk-8-jre-headless

You may need to install ``apt-transport-https`` and other utilities as well::

  sudo apt-get install software-properties-common apt-transport-https gnupg2

Add our repository and gpg key::

  sudo add-apt-repository 'deb [arch=all] https://nexus.repo.strapdata.com/repository/apt-releases/ stretch main'
  sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys B335A4DD

And then install elassandra with::

  sudo apt-get update && sudo apt-get install elassandra

Start Elassandra with Systemd::

  sudo systemctl start cassandra

or SysV::

  sudo service cassandra start

Files locations:

- ``/usr/bin``: startup script, cqlsh, nodetool, elasticsearch-plugin
- ``/etc/cassandra`` and ``/etc/default/cassandra``: configurations
- ``/var/lib/cassandra``: data
- ``/var/log/cassandra``: logs
- ``/usr/share/cassandra``: plugins, modules, libs, ...
- ``/usr/share/cassandra/tools``: cassandra-stress, sstabledump...
- ``/usr/lib/python2.7/dist-packages/cqlshlib/``: python library for cqlsh
