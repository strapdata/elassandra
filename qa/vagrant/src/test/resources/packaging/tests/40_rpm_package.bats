#!/usr/bin/env bats

# This file is used to test the installation of a RPM package.

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
    skip_not_rpm
    export_elasticsearch_paths
}

@test "[RPM] package depends on bash" {
    rpm -qpR elassandra-$(cat version).rpm | grep '/bin/bash'
}

##################################
# Install RPM package
##################################
@test "[RPM] rpm command is available" {
    clean_before_test
    rpm --version
}

@test "[RPM] package is available" {
    count=$(ls elassandra-$(cat version).rpm | wc -l)
    [ "$count" -eq 1 ]
}

@test "[RPM] package is not installed" {
    run rpm -qe 'elassandra'
    [ "$status" -eq 1 ]
}

@test "[RPM] install package" {
    rpm -i elassandra-$(cat version).rpm
}

@test "[RPM] package is installed" {
    rpm -qe 'elassandra'
}

@test "[RPM] verify package installation" {
    verify_package_installation
}

@test "[RPM] verify elasticsearch-plugin list runs without any plugins installed" {
    local plugins_list=`/usr/bin/elasticsearch-plugin list`
    [[ -z $plugins_list ]]
}

@test "[RPM] elasticsearch isn't started by package install" {
    # Wait a second to give Elasticsearch a change to start if it is going to.
    # This isn't perfect by any means but its something.
    sleep 1
    ! ps aux | grep cassandra | grep java
}

@test "[RPM] test elasticsearch" {
    # Install scripts used to test script filters and search templates before
    # starting Elasticsearch so we don't have to wait for elasticsearch to scan for
    # them.
    install_elasticsearch_test_scripts
    start_elasticsearch_service
    run_elasticsearch_tests
}

@test "[RPM] verify package installation after start" {
    # Checks that the startup scripts didn't change the permissions
    verify_package_installation
}

@test "[RPM] remove package" {
    # User installed scripts aren't removed so we'll just get them ourselves
    rm -rf $ESSCRIPTS
    rpm -e 'elassandra'
}

@test "[RPM] package has been removed" {
    run rpm -qe 'elassandra'
    [ "$status" -eq 1 ]
}

@test "[RPM] verify package removal" {
    # The removal must stop the service
    count=$(ps | grep [c]assandra | wc -l)
    [ "$count" -eq 0 ]

    # The removal must disable the service
    # see prerm file
    if is_systemd; then
        run systemctl is-enabled elassandra.service
        [ "$status" -eq 1 ]
    fi

    # Those directories are deleted when removing the package
    # see postrm file
    assert_file_not_exist "/var/log/cassandra"
    assert_file_not_exist "/usr/share/cassandra/plugins"
    assert_file_not_exist "/var/run/cassandra"

    # Those directories are removed by the package manager
    #assert_file_not_exist "/usr/share/elasticsearch/bin"
    assert_file_not_exist "/usr/share/cassandra/lib"
    assert_file_not_exist "/usr/share/cassandra/modules"
    assert_file_not_exist "/usr/share/cassandra/tools"
    assert_file_not_exist "/usr/share/cassandra/bin"


    assert_file_not_exist "/etc/cassandra"

    assert_file_not_exist "/etc/init.d/cassandra"
    assert_file_not_exist "/usr/lib/systemd/system/cassandra.service"

    assert_file_not_exist "/etc/sysconfig/cassandra"

    for bin in cassandra nodetool cqlsh cqlsh.py \
      sstableloader sstablescrub sstableupgrade sstableverify \
      stop-server debug-cql elasticsearch-plugin \
      aliases.sh cassandra.in.sh; do
      assert_file_not_exist "/usr/bin/$bin"
    done

    assert_file_not_exist "/usr/share/cassandra/aliases.sh"
    assert_file_not_exist "/usr/share/cassandra/cassandra.in.sh"
}

@test "[RPM] reinstall package" {
    rpm -i elassandra-$(cat version).rpm
}

@test "[RPM] package is installed by reinstall" {
    rpm -qe 'elassandra'
}

@test "[RPM] verify package reinstallation" {
    verify_package_installation
}

@test "[RPM] reremove package" {
    echo "# ping" >> "/etc/cassandra/elasticsearch.yml"
    echo "# ping" >> "/etc/cassandra/jvm.options"
    echo "# ping" >> "/etc/cassandra/logback.xml"
    echo "# ping" >> "/etc/cassandra/scripts/script"
    echo "# ping" >> "/etc/cassandra/triggers/trigger"
    echo "# ping" >> "/etc/cassandra/cassandra.yaml"
    echo "# ping" >> "/etc/cassandra/cassandra-env.sh"

    rpm -e 'elassandra'
}

@test "[RPM] verify preservation" {
    # The removal must disable the service
    # see prerm file
    if is_systemd; then
        run systemctl is-enabled cassandra.service
        [ "$status" -eq 1 ]
    fi

    # Those directories are deleted when removing the package
    # see postrm file
    assert_file_not_exist "/var/log/cassandra"
    assert_file_not_exist "/usr/share/cassandra/plugins"
    assert_file_not_exist "/usr/share/cassandra/modules"
    assert_file_not_exist "/var/run/cassandra"

    assert_file_not_exist "/usr/bin/cassandra"
    assert_file_not_exist "/usr/share/cassandra/lib"
    assert_file_not_exist "/usr/share/cassandra/modules"
    assert_file_not_exist "/usr/share/cassandra/modules/lang-painless"

    assert_file_not_exist "/etc/cassandra/elasticsearch.yml"
    assert_file_exist "/etc/cassandra/elasticsearch.yml.rpmsave"
    assert_file_not_exist "/etc/cassandra/jvm.options"
    assert_file_exist "/etc/cassandra/jvm.options.rpmsave"
    assert_file_not_exist "/etc/cassandra/logback.xml"
    assert_file_exist "/etc/cassandra/logback.xml.rpmsave"
    assert_file_not_exist "/etc/cassandra/cassandra.yaml"
    assert_file_exist "/etc/cassandra/cassandra.yaml.rpmsave"
    assert_file_not_exist "/etc/cassandra/cassandra-env.sh"
    assert_file_exist "/etc/cassandra/cassandra-env.sh.rpmsave"
    
    # older versions of rpm behave differently and preserve the
    # directory but do not append the ".rpmsave" suffix
    test -e "/etc/cassandra/scripts" || test -e "/etc/cassandra/scripts.rpmsave"
    test -e "/etc/cassandra/scripts/script" || test -e "/etc/cassandra/scripts.rpmsave/script"
    test -e "/etc/cassandra/triggers" || test -e "/etc/cassandra/triggers.rpmsave"
    test -e "/etc/cassandra/triggers/trigger" || test -e "/etc/cassandra/triggers.rpmsave/trigger"
    
    assert_file_not_exist "/etc/init.d/cassandra"
    assert_file_not_exist "/usr/lib/systemd/system/cassandra.service"

    assert_file_not_exist "/etc/sysconfig/cassandra"
}


@test "[RPM] finalize package removal" {
    # cleanup
    rm -rf /etc/cassandra
}

@test "[RPM] package has been removed again" {
    run rpm -qe 'elassandra'
    [ "$status" -eq 1 ]
}
