# Analyzing Lastfm dataset with Elassandra

Elassandra cluster


* Create a *lastfm* keyspace replicated on our 2 datacenters.
* Load 1000 records 
* Create 
## Building from source

* Elassandra uses [Maven](http://maven.apache.org) for its build system. Simply run the `mvn clean package -Dmaven.test.skip=true  -Dcassandra.home=<path/to/cassandra>` command in the cloned directory. The distribution will be created under *target/releases*.

## Elassandra Tarball Installation

* Install Java version 8 (check version with `java -version`). Version 8 is recommanded, see [Installing Oracle JDK on RHEL-based Systems](http://docs.datastax.com/en/cassandra/2.2/cassandra/install/installJdkRHEL.html).
* Apply OS settings for cassandra, see [Recommended production settings for Linux](http://docs.datastax.com/en/cassandra/2.2/cassandra/install/installRecommendSettings.html)
* For linux, install jemalloc (yum install jemalloc).
* Download Elassandra tarball from [elassandra repository](https://github.com/vroyer/elassandra/releases) and extract files in your installation directory.
* Install the cassandra driver `pip install cassandra-driver` and the cqlsh utility `python pylib/setup.py install`.
* Configure your cassandra cluster (cluster name, sntich, ip address, seed...), see [cassandra configuration](http://docs.datastax.com/en/cassandra/2.0/cassandra/initialize/initializeMultipleDS.html). Default Elasticsearch configuration is located in `conf/elasticsearch.yml`, but you should NOT use it, everything is inherited from the cassandra.yml (cluster name, listen adress, paths, etc...). 
* Configure cassandra and elasticsearch logging in conf/logback.xml, see [logback framework](http://logback.qos.ch/).





iMac1:elassandra-2.1.1-11 vroyer$ nodetool status
objc[4741]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
Datacenter: europe-west1
========================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns    Host ID                               Rack
UN  10.132.0.4  189.01 KB  32           ?       1784411f-f7e3-48d5-a51a-ed8c20cd67ef  c
UN  10.132.0.2  34.71 MB   32           ?       6fbeeef7-df98-439a-9976-01eae8e18b86  b
Datacenter: us-central1
=======================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns    Host ID                               Rack
UN  10.128.0.2  112.15 KB  32           ?       289ca45a-1b5b-4847-a241-ffbc59c5b32e  b
UN  10.128.0.3  99.61 KB   32           ?       ef03f183-2982-4a1d-b636-d57e811d5a90  c


rvince800@elassandra-eu-01:/opt/cqlinject-0.1/test$ more create-keyspace.sh 
cqlsh <<EOF
CREATE KEYSPACE lastfm WITH replication = {'class': 'NetworkTopologyStrategy', 'europe-west1': '2', 'us-central1':
 '2'}  AND durable_writes = true;
rvince800@elassandra-eu-01:/opt/cqlinject-0.1/test$ cqlsh
Connected to Test Cluster at elassandra-eu-01:9042.
[cqlsh 5.0.1 | Cassandra 2.2.6 | CQL spec 3.3.1 | Native protocol v4]
rvince800@elassandra-eu-01:/opt/cqlinject-0.1/test$ cqlsh
Connected to Test Cluster at elassandra-eu-01:9042.
[cqlsh 5.0.1 | Cassandra 2.2.6 | CQL spec 3.3.1 | Native protocol v4]
Use HELP for help.
cqlsh> desc KEYSPACE lastfm ;
CREATE KEYSPACE lastfm WITH replication = {'class': 'NetworkTopologyStrategy', 'europe-west1': '2', 'us-central1':
 '2'}  AND durable_writes = true;
CREATE TABLE lastfm.playlist (
    user_id text,
    datetime timestamp,
    age int,
    artist_id text,
    artist_name text,
    country text,
    gender text,
    registred timestamp,
    song text,
    song_id text,
    PRIMARY KEY ((user_id, datetime))
    ) WITH bloom_filter_fp_chance = 0.01
    AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
    AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99.0PERCENTILE';
    
    
rvince800@elassandra-eu-01:/opt/cqlinject-0.1/test$ ./load.sh
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Connected to cluster: Test Cluster
Datatacenter: europe-west1; Host: elassandra-eu-01/10.132.0.2; Rack: b
Datatacenter: europe-west1; Host: /10.132.0.4; Rack: c
Datatacenter: us-central1; Host: /10.128.0.2; Rack: b
Datatacenter: us-central1; Host: /10.128.0.3; Rack: c
Loading file userid-profile.tsv
992 profiles loaded.
Reading file userid-timestamp-artid-artname-traid-traname.tsv separator='       '
CREATE TABLE IF NOT EXISTS "lastfm"."playlist" ( user_id text,datetime timestamp,artist_id text,artist_name text,s
ong_id text,song text,gender text,age int,country text,registred timestamp, PRIMARY KEY ((user_id,datetime)))
INSERT INTO "lastfm"."playlist" (user_id,datetime,artist_id,artist_name,song_id,song,gender,age,country,registred)
 VALUES (?,?,?,?,?,?,?,?,?,?)
[user_000001, Mon May 04 23:08:57 UTC 2009, f1b1cf71-bd35-4e99-8624-24a6e15f133a, Deep Dish, , Fuck Me Im Famous (
Pacha Ibiza)-09-28-2007, m, 45, Japan, Thu Feb 23 00:00:00 UTC 2006]
Request timeout.
Request timeout.



iMac1:elassandra-2.1.1-11 vroyer$ indices
health status index       pri rep docs.count docs.deleted store.size pri.store.size 
green  open   lastfm_2013   2   1          0            0       142b           142b 
green  open   lastfm_2012   2   1          0            0       142b           142b 
green  open   lastfm_2011   2   1          0            0       142b           142b 
green  open   lastfm_2007   2   1      71776           11     23.6mb         23.6mb 
green  open   lastfm_2006   2   1      42477           44     10.7mb         10.7mb 
green  open   lastfm_2005   2   1       7642           12      1.6mb          1.6mb 
green  open   lastfm_2009   2   1      28647           17      8.5mb          8.5mb 
green  open   lastfm_2008   2   1      62981           34     16.3mb         16.3mb 
green  open   lastfm_2010   2   1          0            0       142b           142b 


iMac1:elassandra-2.1.1-11 vroyer$ indices
health status index       pri rep docs.count docs.deleted store.size pri.store.size 
green  open   lastfm_2013   2   1          0            0       142b           142b 
green  open   lastfm_2012   2   1          0            0       142b           142b 
green  open   lastfm_2011   2   1          0            0       142b           142b 
green  open   lastfm_2007   2   1    2624229         6062    371.1mb        371.1mb 
green  open   lastfm_2006   2   1    2115492         4620    314.2mb        314.2mb 
green  open   lastfm_2005   2   1     548274          939    106.6mb        106.6mb 
green  open   lastfm_2009   2   1    1077308         2076    212.7mb        212.7mb 
green  open   lastfm_2008   2   1    2713695         6592    380.9mb        380.9mb 
green  open   kibana        2   0          0            0        71b            71b 
green  open   lastfm_2010   2   1          0            0       142b           142b 


iMac1:elassandra-2.1.1-11 vroyer$ nodetool compactionstats
objc[5027]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
pending tasks: 10
                                     id         compaction type   keyspace      table   completed      total    unit   progress
   a6b5bd00-42de-11e6-b23d-4b686713c22c   Secondary index build     lastfm   playlist     3468894   94640715   bytes      3,67%
   a9496030-42de-11e6-b23d-4b686713c22c   Secondary index build     lastfm   playlist      206700   96656079   bytes      0,21%
Active compaction remaining time :   0h00m00s



iMac1:elassandra-2.1.1-11 vroyer$ nodetool status
objc[4979]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
Datacenter: europe-west1
========================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns    Host ID                               Rack
UN  10.132.0.4  1.01 GB    32           ?       1784411f-f7e3-48d5-a51a-ed8c20cd67ef  c
UN  10.132.0.2  763.71 MB  32           ?       6fbeeef7-df98-439a-9976-01eae8e18b86  b
Datacenter: us-central1
=======================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Tokens       Owns    Host ID                               Rack
UN  10.128.0.2  735.83 MB  32           ?       289ca45a-1b5b-4847-a241-ffbc59c5b32e  b
UN  10.128.0.3  735.78 MB  32           ?       ef03f183-2982-4a1d-b636-d57e811d5a90  c




Note: Non-system keyspaces don't have the same replication settings, effective ownership information is meaningless
iMac1:elassandra-2.1.1-11 vroyer$ nodetool compactionstats
objc[5283]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
pending tasks: 19
                                     id         compaction type   keyspace      table   completed      total    unit   progress
   ae025460-42de-11e6-b23d-4b686713c22c   Secondary index build     lastfm   playlist    57888480   97790199   bytes     59,20%
   aacf63f0-42de-11e6-b23d-4b686713c22c   Secondary index build     lastfm   playlist    57719337   97503705   bytes     59,20%
Active compaction remaining time :   0h00m00s
You have new mail in /var/mail/vroyer
iMac1:elassandra-2.1.1-11 vroyer$ 



iMac1:elassandra-2.1.1-11 vroyer$ bin/nodetool -h 104.197.104.224 compactionstats
objc[5394]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java and /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
pending tasks: 17
                                     id         compaction type   keyspace      table    completed        total    unit   progress
   3fdfb020-42ef-11e6-a4f6-33f8af070846              Compaction     lastfm   playlist   4585897305   4612845649   bytes     99,42%
   b8b60b90-42de-11e6-a4f6-33f8af070846   Secondary index build     lastfm   playlist     73053591     98896356   bytes     73,87%
Active compaction remaining time :   0h00m01s


