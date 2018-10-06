#!/bin/bash
#
# Check for elassandra availability.
#
#set -x

if [[ $($CASSANDRA_HOME/bin/nodetool status 2>/dev/null | grep "$POD_IP" | awk '{ print $1 }') == "UN" ]]; then
  if [[ "$CASSANDRA_DAEMON" == "org.apache.cassandra.service.CassandraDaemon" ]] || \
     [[ $(curl -XGET "http://localhost:9200/_cat/nodes?h=ip" 2>/dev/null | grep "$POD_IP" ) == "$POD_IP" ]]; then
     exit 0;
  else 
     if [[ $DEBUG ]]; then
        echo "Elasticsearch not UP";
     fi
     exit 2;
  fi
else
  if [[ $DEBUG ]]; then
    echo "Cassandra not UP";
  fi
  exit 1;
fi
