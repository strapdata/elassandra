#!/bin/bash
#
# Canary repair/cleanup tests
#

export CCM_MAX_HEAP_SIZE="1200M"
export CCM_HEAP_NEWSIZE="120M"

cluster_name="integ_elassandra"

test_start() {
  set -x
  set -e
  set -o pipefail
  trap finish ERR
}

test_end() {
  set +e
  trap - ERR
}

finish() {
  echo "ERROR occurs, test failed"
#  cat ~/.ccm/$cluster_name/node1/logs/system.log
#  cat ~/.ccm/$cluster_name/node2/logs/system.log
#  cat ~/.ccm/$cluster_name/node3/logs/system.log
   cat ~/.ccm/$cluster_name/node4/logs/system.log
   cat ~/.ccm/$cluster_name/node5/logs/system.log
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

  if [ $? -ne 0 ]; then
  	 cat ~/.ccm/$cluster_name/node$IDX/logs/system.log
  fi
 
  sleep 35

  echo "Waiting node$IDX restart"
  until curl --retry 20 --retry-delay 5 "http://127.0.0.${IDX}:9200/_search"
  do
    sleep 5
  done
 
  # wait green node
  STATUS=$(curl "http://127.0.0.${IDX}:9200/_cluster/health?wait_for_status=green&wait_for_active_shards=all&timeout=60s&pretty&local=true" | jq '.status')
  echo "Node node$IDX status=$STATUS"

  curl "http://127.0.0.${IDX}:9200/_cluster/state?pretty"
}

# $1 = node index
# $2 = datacenter
# $3 = rack
scale_up() {
  IDX=$1
  # add a new node with bootstrap to true
  ccm add -b -r "500${IDX}" --data-center $2 --rack $3 -t "127.0.0.${IDX}" --binary-itf "127.0.0.${IDX}:9042" -l "127.0.0.${IDX}:7000" -j "710${IDX}" node$IDX
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

# $1 = index name
function create_index_with_random_search() {
  curl -H 'Content-Type: application/json' -XPUT "http://localhost:9200/$1" -d'{ "settings":{ "index.search_strategy_class":"RandomSearchStrategy"} }'
}

# $1 = index name
function create_index_with_rack_search() {
  curl -H 'Content-Type: application/json' -XPUT "http://localhost:9200/$1" -d'{ "settings":{ "index.search_strategy_class":"RackAwareSearchStrategy"} }'
}

# $1 = index name
# $2 = doc count
# $3 = consistency (1 by default)
function insert_doc() {
  echo "Inserting $2 docs in $1"
  for i in $( seq 1 $2 ); 
  do 
    curl -H 'Content-Type: application/json' -XPOST "http://localhost:9200/$1/doc?wait_for_active_shards=${3:-1}" -d'{"foo":"bar"}' 2>/dev/null
  done
  # wait for async cassandra replication
  sleep 5
  curl -XPOST "http://localhost:9200/$1/_refresh"
}

# $1 = index name
# $2 expected doc count
function doc_count() {
  DOC_COUNT=$(curl "http://localhost:9200/_cat/indices/$1" 2>/dev/null | awk '{ print $7 }')
  echo "doc count $1 = $DOC_COUNT"
  if [ "$DOC_COUNT" != "$2" ]; then
     echo "ERROR: unexpected doc count = $DOC_COUNT"
     return 1
  fi
}

function delete_index() {
   curl -XDELETE "http://localhost:9200/$1"
   ccm node1 cqlsh -e "DROP KEYSPACE IF EXISTS $1"
}

# Run 10 queries and check hits.total depending on search strategy and RF
# $1 node index
# $2 index name
# $3 expected total
function total_hit() {
  for x in $(seq 1 10); do
	  TOTAL_HIT=$(curl "http://127.0.0.${1:-1}:9200/${2}/_search?size=0" 2>/dev/null | jq '.hits.total')
	  if [ "$TOTAL_HIT" != "${3}" ]; then
	     echo "### total_hit : unexpected total.hits = $TOTAL_HIT"
	     return 1
	  fi
  done
}

# Run 10 queries and check _shards.total depending on search strategy and RF
# $1 node index
# $2 index name
# $3 min shard total
# $4 max shard total
function total_shards() {
  for x in $(seq 1 10); do
	  TOTAL_SHARDS=$(curl "http://127.0.0.${1:-1}:9200/${2}/_search?size=0" 2>/dev/null | jq '._shards.total')
	  if [ "$TOTAL_SHARDS" -lt "${3}" ]; then
	     echo "### total_shards = $TOTAL_SHARDS lower than $3 from node $1"
	     return 1
	  fi
	  if [ "$TOTAL_SHARDS" -gt "${4}" ]; then
	     echo "### total_shards = $TOTAL_SHARDS greater than $4 from node $1"
	     return 1
	  fi
  done
}

function total_hit_lessthan() {
  TOTAL_HIT=$(curl "http://localhost:9200/${1}/_search?size=0" 2>/dev/null | jq '.hits.total')
  if [ "$TOTAL_HIT" -gt "${2}" ]; then
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

# Full repair of primary ranges only
# $1 = elassandra node index [0, N-1]
# $2 = keyspace
function repair() {
  echo "node${1}: nodetool repair -pr $2"
  P="710${1}"
  if [ "$1" == "1" ]; then
  	 P="7100"
  fi
  # ccm node1 nodetool repair does not work with --full
  ~/.ccm/$cluster_name/node$1/bin/nodetool -p $P repair -pr --full $2
}

# $1 = elassandra node index [0, N-1]
# $2 = keyspace
function cleanup() {
  echo "node${1}: nodetool cleanup $2"
  ccm node$1 nodetool cleanup $2
}
