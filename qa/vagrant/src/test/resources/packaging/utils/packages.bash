#!/bin/bash

# This file contains some utilities to test the elasticsearch scripts with
# the .deb/.rpm packages.

# WARNING: This testing file must be executed as root and can
# dramatically change your system. It should only be executed
# in a throw-away VM like those made by the Vagrantfile at
# the root of the Elasticsearch source code. This should
# cause the script to fail if it is executed any other way:
[ -f /etc/is_vagrant_vm ] || {
  >&2 echo "must be run on a vagrant VM"
  exit 1
}

# Licensed to Elasticsearch under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.


# Export some useful paths.
export_elasticsearch_paths() {
    export ESHOME="/usr/share/cassandra"
    export ESPLUGINS="$ESHOME/plugins"
    export ESMODULES="$ESHOME/modules"
    export ESCONFIG="/etc/cassandra"
    export ESSCRIPTS="$ESCONFIG/scripts"
    export ESDATA="/var/lib/cassandra"
    export ESLOG="/var/log/cassandra"
    export ESPIDDIR="/var/run/cassandra"
    if is_dpkg; then
        export ESENVFILE="/etc/default/cassandra"
    fi
    if is_rpm; then
        export ESENVFILE="/etc/sysconfig/cassandra"
    fi
}

# Install the rpm or deb package.
# -u upgrade rather than install. This only matters for rpm.
# -v the version to upgrade to. Defaults to the version under test.
install_package() {
    local version=$(cat full_elass_version)
    local rpmCommand='-i'
    while getopts ":uv:" opt; do
        case $opt in
            u)
                rpmCommand='-U'
                dpkgCommand='--force-confnew'
                ;;
            v)
                version=$OPTARG
                ;;
            \?)
                echo "Invalid option: -$OPTARG" >&2
                ;;
        esac
    done
    if is_rpm; then
        rpm $rpmCommand elassandra-$version.rpm
    elif is_dpkg; then
        dpkg $dpkgCommand -i elassandra-$version.deb
    else
        skip "Only rpm or deb supported"
    fi
}

# Checks that all directories & files are correctly installed after a deb or
# rpm install.
verify_package_installation() {
    id cassandra

    getent group cassandra

    BINDIR=/usr/bin
    assert_file "$ESHOME" d root root 755
    assert_file "$BINDIR/cassandra" f root root 755
    assert_file "$BINDIR/nodetool" f root root 755
    assert_file "$BINDIR/cqlsh" f root root 755
    assert_file "$BINDIR/cqlsh.py" f root root 755
    assert_file "$BINDIR/elasticsearch-plugin" f root root 755
    assert_file_not_exist "$BINDIR/aliases.sh"
    assert_file_not_exist "$BINDIR/cassandra.in.sh"
    assert_file "$ESHOME/cassandra.in.sh" f root root 755
    #assert_file "$BINDIR/elasticsearch-translog" f root root 755
    assert_file "$ESHOME/lib" d root root 755
    assert_file "$ESCONFIG" d root cassandra 755
    assert_file "$ESCONFIG/elasticsearch.yml" f root cassandra 664
    assert_file "$ESCONFIG/cassandra.yaml" f root cassandra 664
    assert_file "$ESCONFIG/cassandra-env.sh" f root cassandra 664
    assert_file "$ESCONFIG/elasticsearch.yml" f root cassandra 664
    assert_file "$ESCONFIG/jvm.options" f root cassandra 664
    assert_file "$ESCONFIG/logback.xml" f root cassandra 664
    assert_file "$ESCONFIG/logback-tools.xml" f root cassandra 664
    assert_file "$ESCONFIG/cassandra-jaas.config" f root cassandra 664
    assert_file "$ESCONFIG/cassandra-rackdc.properties" f root cassandra 664
    assert_file "$ESCONFIG/cassandra-topology.properties" f root cassandra 664
    assert_file "$ESCONFIG/cqlshrc.sample" f root cassandra 664
    assert_file "$ESCONFIG/metrics-reporter-config-sample.yaml" f root cassandra 664
    assert_file "$ESCONFIG/triggers/" d root cassandra 750

    #assert_file "$ESCONFIG/log4j2.properties" f root cassandra 660
    assert_file "$ESSCRIPTS" d root cassandra 750
    assert_file "$ESDATA" d cassandra cassandra 750
    assert_file "$ESLOG" d cassandra cassandra 750
    assert_file "$ESPLUGINS" d root root 755
    assert_file "$ESMODULES" d root root 755
    assert_file "$ESPIDDIR" d cassandra cassandra 755
    assert_file "$ESHOME/NOTICE.txt" f root root 644
    assert_file "$ESHOME/bin" d root root 755
    #assert_file "$ESHOME/README.textile" f root root 644

    # python cassandra-driver for cqlsh.py
    assert_file $ESHOME/lib/cassandra-driver-internal-only-*.zip f root root 644

    if is_dpkg; then
        # Env file
        assert_file "/etc/default/cassandra" f root root 664

        # Doc files
        assert_file "/usr/share/doc/cassandra" d root root 755
        assert_file "/usr/share/doc/cassandra/copyright" f root root 644

        # Pylib files
        assert_file "/usr/lib/python2.7/dist-packages/cqlshlib" d root root 755
        assert_file "/usr/lib/python2.7/dist-packages/cassandra_pylib-0.0.0.egg-info" f root root 664
        assert_file "/usr/lib/python2.7/dist-packages/cqlshlib/__init__.py" f root root 664
    fi

    if is_rpm; then
        # Env file
        assert_file "/etc/sysconfig/cassandra" f root root 664
        # License file
        assert_file "/usr/share/cassandra/LICENSE.txt" f root root 644

        # Pylib files
        assert_file "/usr/lib/python2.7/site-packages/cqlshlib" d root root 755
        assert_file "/usr/lib/python2.7/site-packages/cassandra_pylib-0.0.0.egg-info" f root root 664
        assert_file "/usr/lib/python2.7/site-packages/cqlshlib/__init__.py" f root root 664
    fi

    if is_systemd; then
        assert_file "/usr/lib/systemd/system/cassandra.service" f root root 644
        assert_file "/usr/lib/tmpfiles.d/cassandra.conf" f root root 644
        assert_file "/usr/lib/sysctl.d/cassandra.conf" f root root 644
        if is_rpm; then
            [[ $(/usr/sbin/sysctl vm.max_map_count) =~ "vm.max_map_count = 1048575" ]]
        else
            [[ $(/sbin/sysctl vm.max_map_count) =~ "vm.max_map_count = 1048575" ]]
        fi
    fi

    if is_sysvinit; then
        assert_file "/etc/init.d/cassandra" f root root 750
    fi

#    run sudo -E -u vagrant LANG="en_US.UTF-8" cat "$ESCONFIG/cassandra.yml"
#    [ $status = 1 ]
#    [[ "$output" == *"Permission denied"* ]] || {
#        echo "Expected permission denied but found $output:"
#        false
#    }
}
