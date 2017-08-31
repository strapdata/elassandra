#!/usr/bin/env bats

# This file is used to test the elasticsearch Systemd setup.

# WARNING: This testing file must be executed as root and can
# dramatically change your system. It should only be executed
# in a throw-away VM like those made by the Vagrantfile at
# the root of the Elasticsearch source code. This should
# cause the script to fail if it is executed any other way:
[ -f /etc/is_vagrant_vm ] || {
  >&2 echo "must be run on a vagrant VM"
  exit 1
}

# The test case can be executed with the Bash Automated
# Testing System tool available at https://github.com/sstephenson/bats
# Thanks to Sam Stephenson!

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

# Load test utilities
load $BATS_UTILS/utils.bash
load $BATS_UTILS/packages.bash
load $BATS_UTILS/plugins.bash

# Cleans everything for the 1st execution
setup() {
    skip_not_dpkg_or_rpm
    export_elasticsearch_paths
}

@test "[CASSANDRA] install elassandra" {
    clean_before_test
    install_package
}

@test "[CASSANDRA] test nodetool command while elassandra is stopped" {
    nodetool
    run nodetool status
    [ "$status" -eq 1 ]
    [ "$output" = "nodetool: Failed to connect to '127.0.0.1:7199' - ConnectException: 'Connection refused (Connection refused)'." ]

    # test with non-root user
    sudo -u vagrant nodetool
}

@test "[CASSANDRA] test cqlsh works while elassandra is stopped" {
  run cqlsh
    [ "$status" -eq 1 ]
    [ "$output" = "Connection error: ('Unable to connect to any servers', {'127.0.0.1': error(111, \"Tried connecting to [('127.0.0.1', 9042)]. Last error: Connection refused\")})" ]
}

@test "[CASSANDRA] start elassandra" {
    start_elasticsearch_service
}

@test "[CASSANDRA] test nodetool while elassandra is started" {
    nodetool status | grep 127.0.0.1

    sudo -u vagrant nodetool status | grep 127.0.0.1
}

@test "[CASSANDRA] test cqlsh while elassandra is started" {
    cqlsh -e 'SHOW VERSION'
    cqlsh -e 'SHOW VERSION' | grep Cassandra

    sudo -u vagrant cqlsh -e 'SHOW VERSION'
    sudo -u vagrant cqlsh -e 'SHOW VERSION' | grep Cassandra
}

@test "[CASSANDRA] stop elassandra" {
    stop_elasticsearch_service
}
