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
N=10

echo "EXECUTE test-cleanup-repair"
test_start

create_cluster $TARBALL_FILE 1
add_datacenter_tags 1 DC1
start_node 1

ccm node1 status
ccm node1 ring

delete_index test || true
delete_index test2  || true

create_index_with_random_search test
curl -H 'Content-Type: application/json' -XPUT "http://127.0.0.1:9200/log-dc1?wait_for_active_shards=1" -d'{"settings":{"index.datacenter_tag":"DC1"}}' 2>/dev/null

insert_doc test $N
insert_doc log-dc1 $N

curl -XPOST "http://127.0.0.1:9200/test/_refresh"
curl -XPOST "http://127.0.0.1:9200/log-dc1/_refresh"
total_hit 1 test $N
total_hit 1 log-dc1 $N

echo "### Scale up 2 nodes, with RF=1"
ccm node1 cqlsh -e "ALTER KEYSPACE test WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1':'1','DC2':'1' }"
ccm node1 cqlsh -e "ALTER KEYSPACE \"elastic_admin\" WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1':'1','DC2':'1' }"
ccm add -b -r "5002" --data-center DC2 --rack r1 -t "127.0.0.2" --binary-itf "127.0.0.2:9042" -l "127.0.0.2:7000" -j "7102" node2
add_datacenter_tags 2 "DC2,DC23"
start_node 2

curl -H 'Content-Type: application/json' -XPUT "http://127.0.0.2:9200/log-dc2?wait_for_active_shards=1" -d'{"settings":{"index.datacenter_tag":"DC2"}}' 2>/dev/null
curl -H 'Content-Type: application/json' -XPOST "http://127.0.0.2:9200/log-dc2/doc" -d'{"foo":"bar"}' 2>/dev/null

check_cluster_status 1 "green"
check_cluster_status 2 "green"
curl -XPOST "http://127.0.0.2:9200/test/_refresh"

total_hit 1 test $N
total_hit 2 test $N

total_hit 1 log-dc1 $N

curl -XPOST "http://127.0.0.2:9200/log-dc2/_refresh"
total_hit 2 log-dc2 1

echo "### Scale up 3 nodes, with RF=1"
ccm node1 cqlsh -e "ALTER KEYSPACE test WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1':'1','DC2':'1','DC3':'1' }"
ccm node1 cqlsh -e "ALTER KEYSPACE \"elastic_admin\" WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1':'1','DC2':'1','DC3':'1' }"
ccm add -b -r "5003" --data-center DC3 --rack r1 -t "127.0.0.3" --binary-itf "127.0.0.3:9042" -l "127.0.0.3:7000" -j "7103" node3
add_datacenter_tags 3 DC23
start_node 3

check_cluster_status 1 "green"
check_cluster_status 2 "green"
check_cluster_status 3 "green"

total_hit 1 test $N
total_hit 2 test $N
total_hit 3 test $N

total_hit 1 log-dc1 $N
total_hit 2 log-dc2 1

curl -H 'Content-Type: application/json' -XPUT "http://127.0.0.3:9200/log-dc3?wait_for_active_shards=1" -d'{"settings":{"index.datacenter_tag":"DC23","index.replication":"DC2:1,DC3:1"}}' 2>/dev/null
sleep 2

curl -H 'Content-Type: application/json' -XPOST "http://127.0.0.2:9200/log-dc3/doc?wait_for_active_shards=all" -d'{"foo":"bar"}' 2>/dev/null
sleep 2 # wait for asynchronous DC replication

curl -H 'Content-Type: application/json' -XPOST "http://127.0.0.3:9200/log-dc3/doc?wait_for_active_shards=all" -d'{"foo":"bar"}' 2>/dev/null
curl -H 'Content-Type: application/json' -XPOST "http://127.0.0.3:9200/log-dc3/doc?wait_for_active_shards=all" -d'{"foo":"bar"}' 2>/dev/null
curl -XPOST "http://127.0.0.3:9200/log-dc3/_refresh"
total_hit 3 log-dc3 3

sleep 2 # wait for asynchronous DC replication
curl -XPOST "http://127.0.0.2:9200/log-dc3/_refresh"
total_hit 2 log-dc3 3

delete_index test
test_end
destroy_cluster
echo "### TEST successful"
