# Elassandra

Elassandra is a fork of [Elasticsearch](https://github.com/elastic/elasticsearch) modified to run on top of [Apache Cassandra](http://cassandra.apache.org) in a scalable and resilient peer-to-peer architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.

![Elassandra architecture](/images/elassandra1.jpg)

Elassandra supports Cassandra vnodes and scale horizontally by adding more nodes. A demo video is available on youtube.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=SHncUmuvH58
" target="_blank"><img src="http://img.youtube.com/vi/SHncUmuvH58/0.jpg" 
alt="Elassandra demo" width="240" height="180" border="10" /></a>

## News

[![Build Status](https://travis-ci.org/strapdata/elassandra.svg?branch=master)](https://travis-ci.org/strapdata/elassandra)

New project documentation available at [doc.elassandra.io](http://doc.elassandra.io).

* **2016-06-21 Release 2.1.1-12 Add support for index partitionning and cross-datacenter mapping replication**
* **2016-05-10 Release 2.1.1-9 Upgrade to cassandra 2.2.5**
* **2016-04-17 Release 2.1.1-8 New feature, index cassandra static columns**
* **2016-03-18 Release 2.1.1-6 Add support for SQL plugin (from [NLPchina](https://github.com/NLPchina/elasticsearch-sql)) and JDBC driver (from [Anchormen](https://github.com/Anchormen/sql4es)).**
* **2016-02-16 Release 2.1.1-2 Remove build dependency to elasticsearch parent project.**
* **2016-02-01 Release 2.1.1-1 Add support for parent-child relationship.**
* **2016-01-28 Release 2.1.1 based on Elasticsearch 2.1.1 and cassandra 2.2.4.**
* **2015-12-20 Release 0.5 Re-index you data from cassandra 2.2.4 with zero downtime.**
* **2015-11-15 Release 0.4 New elassandra tarball ready-to-run.**

## Benefits of Elassandra

Show short intro: <a href="http://www.youtube.com/watch?feature=player_embedded&v=2H6RIBjSwcM
" target="_blank"><img src="http://img.youtube.com/vi/2H6RIBjSwcM/0.jpg" 
alt="Elassandra short intro" width="240" height="180" border="10" /></a>

For cassandra users, elassandra provides elasicsearch features :
* Cassandra update are automatically indexed in Elasticsearch.
* Full-Text and spatial search on your cassandra data.
* Real-time aggregation (does not require Spark or Hadoop to group by)
* Provide search on multiple keyspace and tables in one query.
* Provide automatic schema creation and support nested document using [User Defined Types](https://docs.datastax.com/en/cql/3.1/cql/cql_using/cqlUseUDT.html).
* Provide a read/write JSON REST access to cassandra data (for indexed data)
* There are many elasticsearch plugins to import data in cassandra or to visualize your data, with [Kibana](https://www.elastic.co/guide/en/kibana/current/introduction.html) for exemple.

For Elasticsearch users, elassandra provides usefull features :
* Change the mapping and re-index you data from cassandra with zero downtime, see [Mapping change with zero downtime](#mapping-change-with-zero-downtime).
* Cassandra could be your unique datastore for indexed and non-indexed data, it's easier to manage and secure. Source documents are now stored in Cassandra, reducing disk space if you need a noSql database and elasticsearch.
* In elassandra, Elasticsearch is masterless and split-brain resistant because cluster state is now managed within a [cassandra lightweight transactions](http://www.datastax.com/dev/blog/lightweight-transactions-in-cassandra-2-0).
* Write operations are not more restricted to one primary shards, but distributed on all cassandra nodes in a virtual datacenter. Number of shards does not limit your write throughput, just add some elassandra nodes to increase both read and write throughput.
* Elasticsearch indices can be replicated between many cassandra datacenters, allowing to write to the closest datacenter and search globally.
* The [cassandra driver](http://www.planetcassandra.org/client-drivers-tools/) is Datacenter and Token aware.
* Hadoop Hive, Pig and Spark support with pushdown predicate.
* Cassandra supports partial update and [distributed counters](http://docs.datastax.com/en/cql/3.1/cql/cql_using/use_counter_t.html).

# Known bugs and restrictions
    
* Cassandra 
 * Thrift is not supported, only CQL3.
 * CQL3 truncate has not effect on elasticsearch indices.

* Elasticsearch 
 * tribe, percolate, snapshots and recovery service not tested.
 * Geoshape type not supported.
 * Any Elasticsearch metadata update require the LOCAL_QUORUM (more than half the number of nodes in the elassandra datacenter)
 * Document version is always 1 for all documents (because cassandra index rebuild would increment version many times, document version become meaningless). 
 
# Contribute

Contributors are welcome to test and enhance Elassandra to make it production ready.

# License

```
This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

Copyright 2015, Vincent Royer (vroyer@vroyer.org).

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
