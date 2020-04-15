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
doc_count test $((2*$N))
total_hit 1 test $N
total_hit 2 test $N
total_shards 1 test 1 1
total_shards 2 test 1 1

echo "### Repair test2, with RF=2"
alter_rf test2 2
repair 1 test2
repair 2 test2
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

echo "### Repair test, with RF=3"
alter_rf test 3
repair 1 test
repair 2 test
repair 3 test
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
    cleanup 1
    cleanup 2
    cleanup 3
    cleanup 4
    cleanup 5
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
fi

delete_index test
delete_index test2

test_end

destroy_cluster
echo "### TEST successful"
