
Download the last deb package from `github relases page <https://github.com/strapdata/elassandra/releases>`_

.. parsed-literal::

  wget -O elassandra.deb |deb_url|

Install it with dpkg tool::

  sudo dpkg -i elassandra.deb



To start elassandra, just run::

  sudo systemctl start cassandra

or::

  sudo service cassandra start

Files locations:

- ``/usr/bin``: startup script, cqlsh, nodetool, elasticsearch-plugin
- ``/etc/cassandra`` and ``/etc/default/cassandra``: configurations
- ``/var/lib/cassandra``: data
- ``/var/log/cassandra``: logs
- ``/usr/share/cassandra``: plugins, modules, libs, ...
- ``/usr/lib/python2.7/dist-packages/cqlshlib/``: python library for cqlsh
