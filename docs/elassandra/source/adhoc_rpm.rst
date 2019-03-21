
.. important:: Cassandra and Elassandra packages conflict. You should remove Cassandra prior to install Elassandra.

The Java runtime 1.8 must be installed in order to run Elassandra. You can install it yourself or let the package manager
pull it automatically as a dependency.

Create a file called ``elassandra.repo`` in the ``/etc/yum.repos.d/`` directory or similar according to your distribution (RedHat, OpenSuSe...)::

  [elassandra]
  name=elassandra
  baseurl=https://nexus.repo.strapdata.com/repository/rpm-releases/
  enabled=1
  gpgcheck=0
  priority=1

And then install elassandra with::

  sudo yum install elassandra

Start Elassandra with Systemd::

  sudo systemctl start cassandra

or SysV::

  sudo service cassandra start

Files locations:

- ``/usr/bin``: startup script, cqlsh, nodetool, elasticsearch-plugin
- ``/etc/cassandra`` and ``/etc/sysconfig/cassandra``: configurations
- ``/var/lib/cassandra``: data
- ``/var/log/cassandra``: logs
- ``/usr/share/cassandra``: plugins, modules, libs...
- ``/usr/share/cassandra/tools``: cassandra-stress, sstabledump...
- ``/usr/lib/python2.7/site-packages/cqlshlib/``: python library for cqlsh
