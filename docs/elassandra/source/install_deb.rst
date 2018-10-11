
Debian based packages are available from the github `release page <https://github.com/strapdata/elassandra/releases>`_.

.. warning:: You should uninstall Cassandra prior to install Elassandra cause the two packages conflict.

Usage
.....

This package installs a systemd service named cassandra, but do not start nor enable it.
For those who don't have systemd, a init.d script is also provided.

To start elassandra using systemd, run::

  sudo systemctl start cassandra

Files locations:

- ``/etc/cassandra`` : configurations
- ``/var/lib/cassandra``: database storage
- ``/var/log/cassandra``: logs
- ``/usr/share/cassandra``: plugins, modules, ``cassandra.in.sh``, lib...
