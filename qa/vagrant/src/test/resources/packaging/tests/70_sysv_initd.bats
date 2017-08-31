#!/usr/bin/env bats

# This file is used to test the elasticsearch init.d scripts.

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
    #skip_not_sysvinit
    SERVICE="sudo service --skip-redirect"
    skip_not_dpkg_or_rpm
    export_elasticsearch_paths
}

@test "[INIT.D] remove any leftover configuration to start elasticsearch on restart" {
    # This configuration can be added with a command like:
    # $ sudo update-rc.d elasticsearch defaults 95 10
    # but we want to test that the RPM and deb _don't_ add it on its own.
    # Note that it'd be incorrect to use:
    # $ sudo update-rc.d elasticsearch disable
    # here because that'd prevent elasticsearch from installing the symlinks
    # that cause it to be started on restart.
    sudo update-rc.d -f cassandra remove || true
    sudo chkconfig cassandra off || true
}

@test "[INIT.D] install elasticsearch" {
    clean_before_test
    install_package
}

@test "[INIT.D] elasticsearch fails if startup script is not executable" {
    local INIT="/etc/init.d/cassandra"
    local DAEMON="/usr/bin/cassandra"

    sudo chmod -x "$DAEMON"
    run "$INIT"
    sudo chmod +x "$DAEMON"

    [ "$status" -eq 1 ]
    echo $output
    [[ "$output" == *"The cassandra startup script does not exists or it is not executable, tried: $DAEMON"* ]]
}

@test "[INIT.D] daemon isn't enabled on restart" {
    # Rather than restart the VM which would be slow we check for the symlinks
    # that init.d uses to restart the application on startup.
    ! find /etc/rc[0123456].d | grep cassandra
    # Note that we don't use -iname above because that'd have to look like:
    # [ $(find /etc/rc[0123456].d -iname "elasticsearch*" | wc -l) -eq 0 ]
    # Which isn't really clearer than what we do use.
}

@test "[INIT.D] start" {
    # Install scripts used to test script filters and search templates before
    # starting Elasticsearch so we don't have to wait for elasticsearch to scan for
    # them.
    install_elasticsearch_test_scripts
    $SERVICE cassandra start
    wait_for_elasticsearch_status
    assert_file_exist "/var/run/cassandra/cassandra.pid"
}

@test "[INIT.D] status (running)" {
    $SERVICE cassandra status
}

##################################
# Check that Elasticsearch is working
##################################
@test "[INIT.D] test elasticsearch" {
    run_elasticsearch_tests
}

@test "[INIT.D] restart" {
    $SERVICE cassandra restart

    wait_for_elasticsearch_status

    $SERVICE cassandra status
}

@test "[INIT.D] stop (running)" {
    $SERVICE cassandra stop
}

@test "[INIT.D] status (stopped)" {
    run $SERVICE cassandra status
    # precise returns 4, trusty 3
    [ "$status" -eq 3 ] || [ "$status" -eq 4 ]
}

# Not used for elassandra
#@test "[INIT.D] don't mkdir when it contains a comma" {
#    # Remove these just in case they exist beforehand
#    rm -rf /tmp/aoeu,/tmp/asdf
#    rm -rf /tmp/aoeu,
#    # set DATA_DIR to DATA_DIR=/tmp/aoeu,/tmp/asdf
#    sed -i 's/DATA_DIR=.*/DATA_DIR=\/tmp\/aoeu,\/tmp\/asdf/' /etc/init.d/elasticsearch
#    cat /etc/init.d/elasticsearch | grep "DATA_DIR"
#    run $SERVICE elasticsearch start
#    if [ "$status" -ne 0 ]; then
#      cat /var/log/elasticsearch/*
#      fail
#    fi
#    wait_for_elasticsearch_status
#    assert_file_not_exist /tmp/aoeu,/tmp/asdf
#    assert_file_not_exist /tmp/aoeu,
#    $SERVICE elasticsearch stop
#    run $SERVICE elasticsearch status
#    # precise returns 4, trusty 3
#    [ "$status" -eq 3 ] || [ "$status" -eq 4 ]
#}

@test "[INIT.D] start Elasticsearch with custom JVM options" {
    assert_file_exist $ESENVFILE
    local es_java_opts=$ES_JAVA_OPTS
    local es_jvm_options=$ES_JVM_OPTIONS
    local temp=`mktemp -d`
    touch "$temp/jvm.options"
    chown -R cassandra:cassandra "$temp"
    echo "-Xms1024m" >> "$temp/jvm.options"
    echo "-Xmx1024m" >> "$temp/jvm.options"
    # we have to disable Log4j from using JMX lest it will hit a security
    # manager exception before we have configured logging; this will fail
    # startup since we detect usages of logging before it is configured
    echo "-Dlog4j2.disable.jmx=true" >> "$temp/jvm.options"
    cp $ESENVFILE "$temp/cassandra"
    cp "/etc/cassandra/jvm.options" "$temp/jvm.options.bak"
    cp "$temp/jvm.options" /etc/cassandra/jvm.options
    echo "ES_JVM_OPTS=\"$temp/jvm.options\"" >> $ESENVFILE
    echo "JVM_OPTS=\"-XX:-UseCompressedOops\"" >> $ESENVFILE
    $SERVICE cassandra start
    wait_for_elasticsearch_status
    curl -s -XGET localhost:9200/_nodes | fgrep '"heap_init_in_bytes":1073741824'
    curl -s -XGET localhost:9200/_nodes | fgrep '"using_compressed_ordinary_object_pointers":"false"'
    $SERVICE cassandra stop
    cp "$temp/cassandra" $ESENVFILE
    cp "$temp/jvm.options.bak" /etc/cassandra/jvm.options
}

## Simulates the behavior of a system restart:
## the PID directory is deleted by the operating system
## but it should not block ES from starting
## see https://github.com/elastic/elasticsearch/issues/11594
@test "[INIT.D] delete PID_DIR and restart" {
    rm -rf /var/run/cassandra

    $SERVICE cassandra start

    wait_for_elasticsearch_status

    assert_file_exist "/var/run/cassandra/cassandra.pid"

    $SERVICE cassandra stop
}
