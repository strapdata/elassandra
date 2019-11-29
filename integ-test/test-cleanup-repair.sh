#!/bin/bash
# Canary repair/cleanup tests
#


test_start() {
 	set -x
    set -o pipefail
    trap finish ERR
}

test_end() {
	set +e
	trap - ERR
}

finish() {
	echo "ERROR occurs, test failed"
	exit 1
}

create_cluster() {
  ccm create -v file:$1 -n $2 --vnodes  integ_elassandra
  cp logback.xml ~/.ccm/integ_elassandra/node1/conf/logback.xml
  cp logback.xml ~/.ccm/integ_elassandra/node2/conf/logback.xml
  cp logback.xml ~/.ccm/integ_elassandra/node3/conf/logback.xml
}

destroy_cluster() {
  ccm stop
  ccm remove
}

start_node() {
  IDX=$1
  ccm "node$IDX" start

  sleep 10 # sleep 10 seconds before checking the node status

  until [ $(ccm status | grep "node$IDX" | cut -d' ' -f2) = "UP" ];
  do
    echo "Node starting..."
    sleep 5s
  done

  sleep 10s

  until [ $(ccm node$IDX nodetool status | grep "127.0.0.$IDX" | cut -d' ' -f1) = "UN" ];
  do
    echo "Node bootstraping..."
    sleep 5s
  done

  sleep 10s

  curl "http://127.0.0.$IDX/_search"
  if [ "$?" = "7" ]
  then
    echo "HTTP endpoint not yet ready"
    sleep 10
  fi


}

stop_node() {
  IDX=$1
  ccm "node$IDX" stop
}

function insert_doc() {
	for i in {1..100}; do curl -H 'Content-Type: application/json' -XPOST "http://localhost:9200/$1/doc" -d'{"foo":"bar"}'; done
	sleep 2
}

# $1 = index name
# $2 expected doc count
function doc_count() {
	DOC_COUNT=$(curl "http://localhost:9200/_cat/indices/$1" | awk '{ print $7 }')
	if [ "$DOC_COUNT" != $2 ]; then
		echo "ERROR: unexpected doc count = $DOC_COUNT"
		return 1
  fi
}

function delete_index() {
	curl -XDELETE "http://localhost:9200/$1"
	ccm node1 cqlsh -e "DROP KEYSPACE IF EXISTS $1"
}

# $1 index name
# $2 expected total
function total_hit() {
	TOTAL_HIT=$(curl "http://localhost:9200/${1}/_search?size=0" | jq '.hits.total')
	if [ $# == 2 ] && [ "$TOTAL_HIT" != "${2}" ]; then
		echo "### unexpected total.hits = $TOTAL_HIT"
		return 1
  fi
}

# $1 = keyspace
# $2 = RF
function alter_rf() {
  echo "Alter keyspace ${1} with RF=${2:-1}"
	ccm node1 cqlsh -e "ALTER KEYSPACE \"${1}\" WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1':'${2:-1}'}"
}


# $1 = elassandra node index [0, N-1]
# $2 = keyspace
function repair() {
	echo "node-${1}: nodetool repair --full $2"
	ccm node$1 nodetool repair -- --full $2
}

# $1 = elassandra node index [0, N-1]
# $2 = keyspace
function cleanup() {
	echo "node-${1}: nodetool cleanup  $2"
	ccm node$1 nodetool cleanup -- $2
}

test_start

# TODO check params
TARBALL_FILE=$1
NODES=3

create_cluster $TARBALL_FILE $NODES
start_node 1

delete_index test

set -e
insert_doc test

doc_count test 100
total_hit test 100

start_node 2
echo "### Scale up 2 nodes"
sleep 10
total_hit test 100

alter_rf test 2
repair 1 test
doc_count test 200
total_hit test 100

start_node 3
echo "### Scale up 3 nodes"
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
cleanup 0
cleanup 1
cleanup 2

doc_count test 200
total_hit test 100

echo "Stopping elassandra node 2"
stop_node 2
total_hit test 100
insert_doc test
total_hit test 200
start_node 2
echo "Node 2 restarted"
sleep 10
total_hit test 200
echo "### Resuming hint handoff"
sleep 30
doc_count test 400

delete_index test

test_end

destroy_cluster
echo "### TEST successful"
