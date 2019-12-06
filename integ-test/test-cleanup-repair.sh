#!/bin/bash
#
# Canary repair/cleanup tests
#
# Usage: $1 = elassandra tarball filename

source $(dirname $0)/integ-test-lib.sh

if [ "$#" -ne 1 ]; then
 echo "Expecting elassandra tarball filename as first argument."
 exit 1
fi

TARBALL_FILE=$1

echo "EXECUTE test-cleanup-repair"
test_start

create_cluster $TARBALL_FILE 1
start_node 1

delete_index test

set -e
insert_doc test

doc_count test 100
total_hit test 100

echo "### Scale up 2 nodes"
scale_up 2
sleep 10
total_hit test 100

alter_rf test 2
repair 1 test
doc_count test 200
total_hit test 100

echo "### Scale up 3 nodes"
scale_up 3
sleep 10
total_hit test 100

alter_rf test 3
repair 1 test
doc_count test 300
total_hit test 100

echo "### Stopping elassandra node 2"
stop_node 2
total_hit test 100
start_node 2
echo "### Node 2 restarted"
sleep 10
total_hit test 100

alter_rf test 2
cleanup 1
cleanup 2
cleanup 3

doc_count test 200
total_hit test 100

echo "### Stopping elassandra node 2"
stop_node 2
total_hit test 100
insert_doc test
total_hit test 200
start_node 2
echo "### Node 2 restarted"
sleep 10
total_hit test 200
echo "### Resuming hint handoff"
sleep 30
doc_count test 400

delete_index test

test_end

destroy_cluster
echo "### TEST successful"
