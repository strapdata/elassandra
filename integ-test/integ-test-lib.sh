#!/bin/bash
#
# Canary repair/cleanup tests
#

export CCM_MAX_HEAP_SIZE="1200m"
export CCM_HEAP_NEWSIZE="300m"

cluster_name="integ_elassandra"

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
  rm -rf ~/.ccm/$cluster_name
  ccm create -v file:$1 -n $2 --vnodes $cluster_name
}

destroy_cluster() {
  ccm stop
  ccm remove
}

start_node() {
  IDX=$1
  ccm "node$IDX" start

  sleep 60

  until curl --retry 20 --retry-delay 5 "http://127.0.0.${IDX}:9200/_search" 2>/dev/null
  do
    sleep 5
  done
}

scale_up() {
  IDX=$1
  # add a new node with bootstrap to true
  ccm add -b -r "500${IDX}" -t "127.0.0.${IDX}" --binary-itf "127.0.0.${IDX}:9042" -l "127.0.0.${IDX}:7000" -j "710${IDX}" node$IDX
  start_node $IDX
}

stop_node() {
  IDX=$1
  ccm "node$IDX" stop
}

remove_node() {
  IDX=$1

  ccm "node$IDX" decommission

  stop_node $IDX

  ccm "node$IDX" remove
}

function insert_doc() {
  for i in {1..100}; do curl -H 'Content-Type: application/json' -XPOST "http://localhost:9200/$1/doc" -d'{"foo":"bar"}' 2>/dev/null; done
  sleep 2 # refresh wait
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
     echo "### total_hit : unexpected total.hits = $TOTAL_HIT"
     return 1
  fi
}

function total_hit_lessthan() {
  TOTAL_HIT=$(curl "http://localhost:9200/${1}/_search?size=0" | jq '.hits.total')
  if [ $# == 2 ] && ! [ "$TOTAL_HIT" -lt "${2}" ]; then
     echo "### total_hit_lessthan : unexpected total.hits = $TOTAL_HIT"
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
  echo "node${1}: nodetool repair --full $2"
  # ccm node1 nodetool repair does not work with --full
  ~/.ccm/$cluster_name/node$1/bin/nodetool repair --full $2
}

# $1 = elassandra node index [0, N-1]
# $2 = keyspace
function cleanup() {
  echo "node${1}: nodetool cleanup $2"
  ccm node$1 nodetool cleanup $2
}
