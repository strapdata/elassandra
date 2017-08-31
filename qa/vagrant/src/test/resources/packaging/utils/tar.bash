#!/bin/bash

# This file contains some utilities to test the elasticsearch
# tar distribution.

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


# Install the tar.gz archive
install_archive() {
    export ESHOME=${1:-/tmp/elassandra}

    echo "Unpacking tarball to $ESHOME"
    rm -rf /tmp/untar
    mkdir -p /tmp/untar
    tar -xzpf elassandra*.tar.gz -C /tmp/untar

    find /tmp/untar -depth -type d -name 'elassandra*' -exec mv {} "$ESHOME" \; > /dev/null

    # ES cannot run as root so create elasticsearch user & group if needed
    if ! getent group "cassandra" > /dev/null 2>&1 ; then
        if is_dpkg; then
            addgroup --system "cassandra"
        else
            groupadd -r "cassandra"
        fi
    fi
    if ! id "cassandra" > /dev/null 2>&1 ; then
        if is_dpkg; then
            adduser --quiet --system --no-create-home --ingroup "cassandra" --disabled-password --shell /bin/false "cassandra"
        else
            useradd --system -M --gid "cassandra" --shell /sbin/nologin --comment "elasticsearch user" "cassandra"
        fi
    fi

    chown -R cassandra:cassandra "$ESHOME"
    export_elasticsearch_paths
}

# Move the unzipped tarball to another location.
move_elasticsearch() {
    local oldhome="$ESHOME"
    export ESHOME="$1"
    rm -rf "$ESHOME"
    mv "$oldhome" "$ESHOME"
    export_elasticsearch_paths
}

# Export some useful paths.
export_elasticsearch_paths() {
    export ESMODULES="$ESHOME/modules"
    export ESPLUGINS="$ESHOME/plugins"
    export ESCONFIG="$ESHOME/conf"
    export ESSCRIPTS="$ESCONFIG/scripts"
    export ESDATA="$ESHOME/data"
    export ESLOG="$ESHOME/logs"

    export CASSANDRA_HOME="$ESHOME"
    export CASSANDRA_CONF="$ESCONFIG"
}

# Checks that all directories & files are correctly installed
# after a archive (tar.gz/zip) install
verify_archive_installation() {
    assert_file "$ESHOME" d cassandra cassandra 755
    assert_file "$ESHOME/bin" d cassandra cassandra 755
    assert_file "$ESHOME/bin/cassandra" f cassandra cassandra 755
    assert_file "$ESHOME/bin/aliases.sh" f cassandra cassandra 755
    assert_file "$ESHOME/bin/elasticsearch-plugin" f cassandra cassandra 755
    assert_file "$ESHOME/bin/nodetool" f cassandra cassandra 755
    assert_file "$ESHOME/bin/cqlsh" f cassandra cassandra 755

    assert_file "$ESCONFIG" d cassandra cassandra 755
    assert_file "$ESCONFIG/elasticsearch.yml" f cassandra cassandra 660
    assert_file "$ESCONFIG/jvm.options" f cassandra cassandra 660
    assert_file "$ESCONFIG/cassandra.yaml" f cassandra cassandra 660
    assert_file "$ESCONFIG/cassandra-env.sh" f cassandra cassandra 660
    #assert_file "$ESCONFIG/log4j2.properties" f cassandra cassandra 660
    assert_file "$ESPLUGINS" d cassandra cassandra 755
    assert_file "$ESHOME/lib" d cassandra cassandra 755
    assert_file "$ESHOME/NOTICE.txt" f cassandra cassandra 644
    assert_file "$ESHOME/LICENSE.txt" f cassandra cassandra 644
    assert_file "$ESHOME/README.textile" f cassandra cassandra 644
}
