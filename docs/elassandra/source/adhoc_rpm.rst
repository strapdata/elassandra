
Download the last rpm package from `github relases page <https://github.com/strapdata/elassandra/releases>`_

.. parsed-literal::

  wget -O elassandra.rpm |rpm_url|

Install it with yum::

  sudo yum install elassandra.rpm



To start elassandra, just run::

  sudo systemctl start cassandra

or::

  sudo service cassandra start

Files locations:

- ``/usr/bin``: startup script, cqlsh, nodetool, elasticsearch-plugin
- ``/etc/cassandra`` and ``/etc/sysconfig/cassandra``: configurations
- ``/var/lib/cassandra``: data
- ``/var/log/cassandra``: logs
- ``/usr/share/cassandra``: plugins, modules, libs, ...
- ``/usr/lib/python2.7/site-packages/cqlshlib/``: python library for cqlsh
