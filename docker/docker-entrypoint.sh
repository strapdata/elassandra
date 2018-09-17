#!/bin/bash

# Set memlock limit to unlimited (before set -e)
ulimit -l unlimited

set -e

# first arg is `-f` or `--some-option`
if [ "${1:0:1}" = '-' ]; then
    set -- bin/cassandra
fi

# allow the container to be started with `--user`
if [ "$1" = 'bin/cassandra' -a "$(id -u)" = '0' ]; then
    chown -R cassandra /var/lib/cassandra /var/log/cassandra "$CASSANDRA_CONF"
    exec gosu cassandra "$BASH_SOURCE" "$@"
fi

if [ "$1" = 'bin/cassandra' ]; then

  cd $CASSANDRA_HOME
  ARGS="-f -p $CASSANDRA_PIDFILE"

  : ${CASSANDRA_DAEMON:='org.apache.cassandra.service.ElassandraDaemon'}
  export CASSANDRA_DAEMON

    : ${CASSANDRA_RPC_ADDRESS='0.0.0.0'}

    : ${CASSANDRA_LISTEN_ADDRESS='auto'}
    if [ "$CASSANDRA_LISTEN_ADDRESS" = 'auto' ]; then
        CASSANDRA_LISTEN_ADDRESS="$(hostname --ip-address)"
    fi

    : ${CASSANDRA_BROADCAST_ADDRESS="$CASSANDRA_LISTEN_ADDRESS"}

    if [ "$CASSANDRA_BROADCAST_ADDRESS" = 'auto' ]; then
        CASSANDRA_BROADCAST_ADDRESS="$(hostname --ip-address)"
    fi
    : ${CASSANDRA_BROADCAST_RPC_ADDRESS:=$CASSANDRA_BROADCAST_ADDRESS}

    if [ -n "${CASSANDRA_NAME:+1}" ]; then
        : ${CASSANDRA_SEEDS:="cassandra"}
    fi
    : ${CASSANDRA_SEEDS:="$CASSANDRA_BROADCAST_ADDRESS"}

    sed -ri 's/(- seeds:).*/\1 "'"$CASSANDRA_SEEDS"'"/' "$CASSANDRA_CONF/cassandra.yaml"

    for yaml in \
        broadcast_address \
        broadcast_rpc_address \
        cluster_name \
        endpoint_snitch \
        listen_address \
        num_tokens \
        rpc_address \
        start_rpc \
    ; do
        var="CASSANDRA_${yaml^^}"
        val="${!var}"
        if [ "$val" ]; then
            sed -ri 's/^(# )?('"$yaml"':).*/\2 '"$val"'/' "$CASSANDRA_CONF/cassandra.yaml"
        fi
    done

    for rackdc in dc rack; do
        var="CASSANDRA_${rackdc^^}"
        val="${!var}"
        if [ "$val" ]; then
            sed -ri 's/^('"$rackdc"'=).*/\1 '"$val"'/' "$CASSANDRA_CONF/cassandra-rackdc.properties"
        fi
    done

    # Additional cassandra.yaml variable substitution for env var CASSANDRA__*, substitute __ by .
    set -x
    for v in ${!CASSANDRA__*}; do
       val="${!v}"
       if [ "$val" ]; then
          var=$(echo ${v:9}|sed 's/__/\./g')
          case ${val} in
          true)  filter=$(echo "${var}=true");;
          false) filter=$(echo "${var}=false");;
          *)     filter=$(echo "${var}=\"${val}\"");;
          esac
          yq --yaml-output ${filter} $CASSANDRA_CONF/cassandra.yaml > $CASSANDRA_CONF/cassandra2.yaml
          mv $CASSANDRA_CONF/cassandra2.yaml $CASSANDRA_CONF/cassandra.yaml
       fi
    done

    # Additional elasticsearch.yml variable substitution for env var ELASTICSEARCH__*, substitute __ by .
    for v in ${!ELASTICSEARCH__*}; do
       val="${!v}"
       if [ "$val" ]; then
          var=$(echo ${v:13}|sed 's/__/\./g')
          case ${val} in
          true)  filter=$(echo "${var}=true");;
          false) filter=$(echo "${var}=false");;
          *)     filter=$(echo "${var}=\"${val}\"");;
          esac
          yq --yaml-output ${filter} $CASSANDRA_CONF/elasticsearch.yml > $CASSANDRA_CONF/elasticsearch2.yaml
          mv $CASSANDRA_CONF/elasticsearch2.yaml $CASSANDRA_CONF/elasticsearch.yaml
       fi
    done

    # init script and cql
    for f in docker-entrypoint-init.d/*; do
    case "$f" in
        *.sh)     echo "$0: running $f"; . "$f" ;;
        *.cql)    echo "$0: running $f" && until cqlsh -f "$f"; do >&2 echo "Cassandra is unavailable - sleeping"; sleep 2; done & ;;
        *)        echo "$0: ignoring $f" ;;
    esac
    echo
done

fi

exec $@ $ARGS
