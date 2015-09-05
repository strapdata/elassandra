# Elassandra environnement-independant shortcuts
#
# Please set the following variable in your environnement
export NODE=`hostname`
export NODETOOL_JMX_PORT="7199"

# for debug purpose only
export JVM_DEBUG_PORT="4242"
export JVM_DEBUG_WAIT="n"


if [ "x$CASSANDRA_HOME" = "x" ]; then
   echo "Please set CASSANDRA_HOME"
   exit 1
fi
echo "CASSANDRA_HOME=$CASSANDRA_HOME"

# For mac onlly
#export JAVA_HOME=$(/usr/libexec/java_home -v 1.7)


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
   rm -rf $CASSANDRA_DATA/*
   rm -rf $CASSANDRA_LOGS/*
}


function cleanlogs() {
	rm -rf $CASSANDRA_HOME/logs/*.log*
}

# Start Cassandra
alias cstart='$CASSANDRA_HOME/bin/cassandra'
alias cdebug='$CASSANDRA_HOME/bin/cassandra -d'

# Start Elassandra
alias ecstart='$CASSANDRA_HOME/bin/cassandra -e'
alias ecdebug='$CASSANDRA_HOME/bin/cassandra -d -e'

alias clog='less $CASSANDRA_LOGS/system.log'

alias nodetool='$CASSANDRA_HOME/bin/nodetool -p $NODETOOL_JMX_PORT -h $NODE'
alias cstop='$CASSANDRA_HOME/bin/nodetool -p $NODETOOL_JMX_PORT -h $NODE stopdaemon'
alias cqlsh='$CASSANDRA_HOME/bin/cqlsh $NODE'


alias health='curl -XGET http://$NODE:9200/_cluster/health/?pretty=true'
alias state='curl -XGET http://$NODE:9200/_cluster/state/?pretty=true'
alias status='curl -XGET http://$NODE:9200/_status/?pretty=true'
alias stats='curl -XGET http://$NODE:9200/_stats?pretty=true'
alias shards='curl -XGET http://$NODE:9200/_cat/shards'
alias nodes='curl -XGET http://$NODE:9200/_nodes/_all/_all?pretty=true'
alias settings='curl -XGET http://$NODE:9200/_cluster/settings?pretty=true'

