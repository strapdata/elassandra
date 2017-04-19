
# The directory where Cassandra's configs live
[ -z "$CASSANDRA_CONF" ] && CASSANDRA_CONF="${packaging.elassandra.conf.dir}"

[ -z "$CASSANDRA_HOME" ] && CASSANDRA_HOME="${packaging.elassandra.home.dir}"

# Set default CASSANDRA_LOGDIR if not set, then export
[ -z "$CASSANDRA_LOGDIR" ] && CASSANDRA_LOGDIR=${packaging.elassandra.log.dir}

# the default location for commitlogs, sstables, and saved caches
# if not set in cassandra.yaml
cassandra_storagedir=${packaging.elassandra.data.dir}

# The java classpath (required)
if [ -n "$CLASSPATH" ]; then
    CLASSPATH=$CLASSPATH:$CASSANDRA_CONF
else
    CLASSPATH=$CASSANDRA_CONF
fi

for jar in $CASSANDRA_HOME/lib/*.jar; do
    CLASSPATH=$CLASSPATH:$jar
done

[ -n "$EXTRA_CLASSPATH" ] CLASSPATH="$CLASSPATH:$EXTRA_CLASSPATH"

# set JVM javaagent opts to avoid warnings/errors
if [ "$JVM_VENDOR" != "OpenJDK" -o "$JVM_VERSION" \> "1.6.0" ] \
      || [ "$JVM_VERSION" = "1.6.0" -a "$JVM_PATCH_VERSION" -ge 23 ]
then
    JAVA_AGENT="$JAVA_AGENT -javaagent:$CASSANDRA_HOME/lib/jamm-0.3.0.jar"
fi
