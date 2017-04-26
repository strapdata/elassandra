Our packages are hosted on `packagecloud.io <https://packagecloud.io/elassandra>`_.
Elassandra can be downloaded using a RPM repository.

.. note:: Elassandra requires Java 8 to be installed.

Setup the RPM repository
..........................................

Create a file called ``elassandra.repo`` in the directory ``/etc/yum.repos.d/`` (redhat) or ``/etc/zypp/repos.d/`` (opensuse), containing::

  [elassandra_stable]
  name=Elassandra repository
  baseurl=https://packagecloud.io/elassandra/stable/el/7/$basearch
  type=rpm-md
  repo_gpgcheck=1
  gpgcheck=0
  enabled=1
  gpgkey=https://packagecloud.io/elassandra/stable/gpgkey
  autorefresh=1
  sslverify=1
  sslcacert=/etc/pki/tls/certs/ca-bundle.crt

Install Elassandra
..................

Using yum::

  yum install elassandra

.. warning:: You should uninstall Cassandra prior to install Elassandra cause the two packages conflict.

Install extra tools
...................

Also install Python, pip, and cassandra-driver::

    sudo yum install python python-pip
    sudo pip install cassandra-driver

Usage
.....

This package installs a systemd service named cassandra, but do not start nor enable it.
For those who don't have systemd, a init.d script is also provided.

To start elassandra using systemd, run::

  sudo systemctl start elassandra

Files locations:

- ``/etc/cassandra`` : configurations
- ``/var/lib/cassandra``: database storage
- ``/var/log/cassandra``: logs
- ``/usr/share/cassandra``: plugins, modules, ``cassandra.in.sh``, lib...
