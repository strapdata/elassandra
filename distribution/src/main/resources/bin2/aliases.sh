# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Elassandra usefull aliases
#

if [ "x$CASSANDRA_HOME" = "x" ]; then
    export CASSANDRA_HOME="`dirname "$0"`/.."
fi

#
# Please set the following variable in your environnement (multihoming support)
export NODE="127.0.0.1"
export NODETOOL_JMX_PORT="7199"

echo "CASSANDRA_HOME=$CASSANDRA_HOME"
echo "NODE=$NODE"
echo "NODETOOL_JMX_PORT=$NODETOOL_JMX_PORT"

# for debug purpose only
export JVM_DEBUG_PORT="4242"
export JVM_DEBUG_WAIT="n"

export CASSANDRA_CONF=$CASSANDRA_HOME/conf
export CASSANDRA_DATA=$CASSANDRA_HOME/data
export CASSANDRA_LOGS=$CASSANDRA_HOME/logs

# Kill cassandra process
function ckill() {
   kill `ps ax | grep java | grep $CASSANDRA_HOME | awk '{ print $1}'`
}

function cstatus() {
   ps ax | grep java | grep $CASSANDRA_HOME
}

function cleanall() {
   read -r -p "Do you really want to remove data and logs ? [y/n]" response
   response=${response} # tolower
   if [[ $response =~ ^(yes|y| ) ]]; then
      rm -rf $CASSANDRA_DATA/*
      rm -rf $CASSANDRA_LOGS/*
      echo "Done."
   fi
}

function cleanlogs() {
	rm -rf $CASSANDRA_HOME/logs/*.log*
}

function get() {
   curl -XGET "http://$NODE:9200/$1" $2 $2 $4 $5
}
function put() {
   curl -XPUT "http://$NODE:9200/$1" $2 $2 $4 $5
}
function post() {
   curl -XPOST "http://$NODE:9200/$1" $2 $2 $4 $5
}
function delete() {
   curl -XDELETE "http://$NODE:9200/$1" $2 $2 $4 $5
}

function close() {
   curl -XPOST "http://$NODE:9200/$1/_close"
}

function open() {
   curl -XPOST "http://$NODE:9200/$1/_open"
}

function forcemerge() {
   if [ "x$2" == "x" ]; then
      curl -XPOST "http://$NODE:9200/$1/_forcemerge"
   else
      curl -XPOST "http://$NODE:9200/$1/_forcemerge?max_num_segments=$2"
   fi
}

function rebuild() {
   if [ "x$2" == "x" ]; then
      curl -XPOST "http://$NODE:9200/$1/_rebuild"
   else
      curl -XPOST "http://$NODE:9200/$1/_rebuild?num_threads=$2"
   fi
}

function clearcache() {
   curl -XPOST "http://$NODE:9200/$1/_cache/clear"
}

function flush() {
   curl -XPOST "http://$NODE:9200/$1/_flush"
}

function refresh() {
   curl -XPOST "http://$NODE:9200/$1/_refresh"
}

# Cassandra aliases
alias cstart='$CASSANDRA_HOME/bin/cassandra'
alias cdebug='$CASSANDRA_HOME/bin/cassandra -d'
alias cstop='$CASSANDRA_HOME/bin/nodetool -p $NODETOOL_JMX_PORT -h $NODE stopdaemon'
alias clog='less $CASSANDRA_LOGS/system.log'
alias cqlsh='$CASSANDRA_HOME/bin/cqlsh $NODE'
alias nodetool='$CASSANDRA_HOME/bin/nodetool -p $NODETOOL_JMX_PORT'

# Start Elassandra
alias elstart='$CASSANDRA_HOME/bin/cassandra -e'
alias eldebug='$CASSANDRA_HOME/bin/cassandra -d -e'

# Elasticsearch aliases
alias health='curl -XGET http://$NODE:9200/_cluster/health/?pretty=true'
alias state='curl -XGET http://$NODE:9200/_cluster/state/?pretty=true'
alias blocks='curl -XGET http://$NODE:9200/_cluster/state/blocks/?pretty=true'
alias metadata='curl -XGET http://$NODE:9200/_cluster/state/metadata/?pretty=true'
alias stats='curl -XGET http://$NODE:9200/_stats?pretty=true'
alias shards='curl -s -XGET http://$NODE:9200/_cat/shards?v | sort'
alias indices='curl -s -XGET http://$NODE:9200/_cat/indices?v | sort'
alias fielddata='curl -XGET http://$NODE:9200/_cat/fielddata/body,text?v'
alias threads='curl -XGET http://$NODE:9200/_cat/thread_pool?v'
alias pending_tasks='curl -XGET http://$NODE:9200/_cat/pending_tasks?v'
alias segments='curl -XGET http://$NODE:9200/_cat/segments?v'
alias allocation='curl -XGET http://$NODE:9200/_cat/allocation?v'
alias nodec='curl -XGET http://$NODE:9200/_cat/nodes?h=id,ip,heapPercent,ramPercent,fileDescriptorPercent,segmentsCount,segmentsMemory'
alias nodes='curl -XGET "http://$NODE:9200/_nodes?clear&all&pretty"'
alias settings='curl -XGET http://$NODE:9200/_cluster/settings?pretty'
alias plugins='curl "http://$NODE:9200/_nodes?plugin=true&pretty"'
alias tasks='curl http://$NODE:9200/_tasks?pretty'

alias open='open'
alias close='close'
alias forcemerge='forcemerge'
alias clearcache='clearcache'
alias flush='flush'
alias refresh='refresh'
alias rebuild='rebuild'

alias get='get'
alias delete='delete'
