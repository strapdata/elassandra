
Our packages are hosted on `packagecloud.io <https://packagecloud.io/elassandra>`_.
Elassandra can be downloaded using an APT repository.

.. note:: Elassandra requires Java 8 to be installed.

Import the GPG Key
..................

Download and install the public signing key::

  curl -L https://packagecloud.io/elassandra/stable/gpgkey | sudo apt-key add -

Install Elassandra from the APT repository
..............................................

Ensure apt is able to use https::

  sudo apt-get install apt-transport-https

Add the Elassandra repository to your source list::

  echo "deb https://packagecloud.io/elassandra/stable/debian jessie main" | sudo tee -a /etc/apt/sources.list.d/elassandra.list

Update apt cache and install Elassandra::

  sudo apt-get update
  sudo apt-get install elassandra


.. warning:: You should uninstall Cassandra prior to install Elassandra cause the two packages conflict.

Install extra tools
...................

Also install Python, pip, and cassandra-driver::

    sudo apt-get update && sudo apt-get install python python-pip
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
