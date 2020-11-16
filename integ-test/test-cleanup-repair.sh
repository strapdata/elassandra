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

# prevent issues
unset CASSANDRA_DATA
unset CASSANDRA_CONF
unset CASSANDRA_HOME
unset CASSANDRA_LOGS

# show sys config
echo "SHOW sysconfig"
free
ifconfig -a

TARBALL_FILE=$1
N=1000

echo "EXECUTE test-cleanup-repair"
test_start

create_cluster $TARBALL_FILE 1
start_node 1

ccm node1 status
ccm node1 ring

delete_index test || true
delete_index test2  || true

create_index_with_random_search test
create_index_with_rack_search test2

insert_doc test $N
insert_doc test2 $N

doc_count test $N
doc_count test2 $N
total_hit 1 test $N
total_hit 1 test2 $N

echo "### Scale up 2 nodes, with RF=1"
scale_up 2 DC1 r2
ccm node1 status
ccm node1 ring
total_hit 1 test $N
total_hit 2 test $N
total_shards 1 test 2 2
total_shards 2 test 2 2

total_hit 1 test2 $N
total_hit 2 test2 $N
total_shards 1 test2 2 2
total_shards 2 test2 2 2

echo "### Repair test, with RF=2"
alter_rf test 2
repair 1 test
repair 2 test

curl -XPOST "http://127.0.0.1:9200/test/_refresh"

doc_count test $((2*$N))
total_hit 1 test $N
total_hit 2 test $N
total_shards 1 test 1 1
total_shards 2 test 1 1

echo "### Repair test2, with RF=2"
alter_rf test2 2
repair 1 test2
repair 2 test2
curl -XPOST "http://127.0.0.1:9200/test2/_refresh"

doc_count test2 $((2*$N))
total_hit 1 test2 $N
total_hit 2 test2 $N
total_shards 1 test2 1 1
total_shards 2 test2 1 1

echo "### Scale up 3 nodes, with RF=2"
scale_up 3 DC1 r3
ccm node1 status
ccm node1 ring
total_hit 1 test $N
total_hit 2 test $N
total_hit 3 test $N
total_shards 1 test 2 2
total_shards 2 test 2 2
total_shards 3 test 2 2

total_hit 1 test2 $N
total_hit 2 test2 $N
total_hit 3 test2 $N
total_shards 1 test2 2 2
total_shards 2 test2 2 2
total_shards 3 test2 2 2

# test refresh
curl -H 'Content-Type: application/json' -XPUT "http://127.0.0.1:9200/testrefresh" -d'{"settings":{"index.refresh_interval":-1}}' 2>/dev/null
curl -H 'Content-Type: application/json' -XPOST "http://127.0.0.1:9200/testrefresh/doc?wait_for_active_shards=all" -d'{"foo":"bar"}' 2>/dev/null
curl -XPOST "http://127.0.0.2:9200/testrefresh/_refresh"
total_hit 1 testrefresh 1

# test close/open
curl -XPOST "http://127.0.0.1:9200/testrefresh/_close"
curl -XPOST "http://127.0.0.2:9200/testrefresh/_open"
sleep 2
total_hit 1 testrefresh 1

echo "### Repair test, with RF=3"
alter_rf test 3
repair 1 test
repair 2 test
repair 3 test
curl -XPOST "http://127.0.0.1:9200/test/_refresh"

doc_count test $((3*$N))
total_hit 1 test $N
total_hit 2 test $N
total_hit 3 test $N
total_shards 1 test 1 1
total_shards 2 test 1 1
total_shards 3 test 1 1

echo "### Stopping elassandra node 2"
stop_node 2
total_hit 1 test $N
total_hit 3 test $N
total_shards 1 test 1 1
total_shards 3 test 1 1
start_node 2
echo "### Node 2 restarted"
total_hit 1 test $N
total_hit 2 test $N
total_hit 3 test $N

alter_rf test 2
cleanup 1
cleanup 2
cleanup 3
curl -XPOST "http://127.0.0.1:9200/test/_refresh"

doc_count test $((2*$N))
total_hit 1 test $N
total_hit 2 test $N
total_hit 3 test $N

echo "### Stopping elassandra node 2"
stop_node 2
total_hit 1 test $N
total_hit 3 test $N
insert_doc test $N
total_hit 1 test $((2*$N))
total_hit 3 test $((2*$N))
start_node 2
echo "### Node 2 restarted"
total_hit 1 test $((2*$N))
total_hit 2 test $((2*$N))
total_hit 3 test $((2*$N))
echo "### Resuming hint handoff"
sleep 40
doc_count test $((4*$N))

echo "### Scale up 4 nodes, with RF=2"
scale_up 4 DC1 r1
ccm node1 status
ccm node1 ring
total_hit 1 test $((2*$N))
total_hit 2 test $((2*$N))
total_hit 3 test $((2*$N))
total_hit 4 test $((2*$N))

# Travis cannot run 5 nodes test
if [ -z "$TRAVIS" ]; then
    echo "### Scale up 5 nodes, with RF=2"
    scale_up 5 DC1 r2

    ccm node1 status
    ccm node1 ring

    total_hit 1 test $((2*$N))
    total_hit 2 test $((2*$N))
    total_hit 3 test $((2*$N))
    total_hit 4 test $((2*$N))
    total_hit 5 test $((2*$N))


    total_shards 1 test 3 5
    total_shards 2 test 3 5
    total_shards 3 test 3 5
    total_shards 4 test 3 5
    total_shards 5 test 3 5

    # using the rack-aware search streategy is worse when RF < number of rack
    total_shards 1 test2 3 5
    total_shards 2 test2 3 5
    total_shards 3 test2 3 5
    total_shards 4 test2 3 5
    total_shards 5 test2 3 5

    echo "### Repair test, with RF=3, Random search"
    alter_rf test 3
    repair 1 test
    repair 2 test
    repair 3 test
    repair 4 test
    repair 5 test
    sleep 2

    cleanup 1
    cleanup 2
    cleanup 3
    cleanup 4
    cleanup 5
    sleep 2
    doc_count test $((6*$N))

    total_hit 1 test $((2*$N))
    total_hit 2 test $((2*$N))
    total_hit 3 test $((2*$N))
    total_hit 4 test $((2*$N))
    total_hit 5 test $((2*$N))
    total_shards 1 test 1 5
    total_shards 2 test 1 5
    total_shards 3 test 1 1
    total_shards 4 test 1 5
    total_shards 5 test 1 5

    echo "### Repair test2, with RF=3, RackAware search"
    alter_rf test2 3
    repair 1 test2
    repair 2 test2
    repair 3 test2
    repair 4 test2
    repair 5 test2
    sleep 2

    total_hit 1 test2 $N
    total_hit 2 test2 $N
    total_hit 3 test2 $N
    total_hit 4 test2 $N
    total_hit 5 test2 $N
    total_shards 1 test2 2 2
    total_shards 2 test2 2 2
    total_shards 3 test2 1 1
    total_shards 4 test2 2 2
    total_shards 5 test2 2 2

    echo "### Stop node 2, check RackAware search"
    stop_node 2
    total_hit 1 test2 $N
    total_hit 3 test2 $N
    total_hit 4 test2 $N
    total_hit 5 test2 $N
    total_shards 1 test2 2 2
    total_shards 3 test2 1 1
    total_shards 4 test2 2 2
    total_shards 5 test2 2 5

    # restart node 2 to avoid schema mismatch
    start_node 2

    # remove node5
    ccm node5 decommission
fi

# test node decommission
ccm node4 decommission
check_cluster_status 1 "green"
check_cluster_status 2 "green"
check_cluster_status 3 "green"
total_hit 1 test2 $N
total_hit 2 test2 $N
total_hit 3 test2 $N

# test node removenode
HOST_ID3=$(ccm node1 nodetool status | awk '/127.0.0.3/ { print $7 }')
ccm node3 stop
ccm node1 nodetool removenode $HOST_ID3
check_cluster_status 1 "green"
check_cluster_status 2 "green"
total_hit 1 test2 $N
total_hit 2 test2 $N

# restart a new node3 reusing the same IP address

ccm node3 remove
scale_up 3 DC1 r3
check_cluster_status 3 "green"
total_hit 1 test2 $N
total_hit 2 test2 $N
total_hit 3 test2 $N

delete_index test
delete_index test2

test_end

destroy_cluster
echo "### TEST successful"
