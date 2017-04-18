# Elassandra

Elassandra is a fork of [Elasticsearch](https://github.com/elastic/elasticsearch) modified to run like a plugin for [Apache Cassandra](http://cassandra.apache.org) in a scalable and resilient peer-to-peer architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.

![Elassandra architecture](/docs/elassandra/source/images/elassandra1.jpg)

Elassandra supports Cassandra vnodes and scale horizontally by adding more nodes.

## News

[![Build Status](https://travis-ci.org/strapdata/elassandra.svg)](https://travis-ci.org/strapdata/elassandra) [![Doc Status](https://readthedocs.org/projects/elassandra/badge/?version=latest)](http://doc.elassandra.io)

Project documentation is available at [doc.elassandra.io](http://doc.elassandra.io).

* **2017-04-18 Release 2.4.2-13 Project refactoring (see How to contribute) and search performance improvement**
* **2017-02-25 Release 2.4.2-9 Significant write performance improvement**
* **2017-01-15 Release 2.4.2-4 Upgrade to Cassandra 3.0.10**
* **2016-12-25 Release 2.4.2-2 Upgrade to Elasticsearch 2.4.2 + pass 3000 unit tests from Elasticsearch**
* **2016-10-24 Release 2.1.1-18 Add multi-threaded index rebuild and optimized search routing (see RandomSearchStrategy)**
* **2016-09-05 Release 2.1.1-17 Add spark support with a modified version of [elasticsearch-hadoop-2.2](https://github.com/vroyer/elasticsearch-hadoop)**
* **2016-08-12 Release 2.1.1-16 Upgrade to cassandra 2.2.7**
* **2016-07-10 Release 2.1.1-15 Bug fix**
* **2016-06-21 Release 2.1.1-12 Add support for index partitioning and cross-datacenter mapping replication**
* **2016-05-10 Release 2.1.1-9 Upgrade to cassandra 2.2.5**
* **2016-04-17 Release 2.1.1-8 New feature, index cassandra static columns**
* **2016-03-18 Release 2.1.1-6 Add support for SQL plugin (from [NLPchina](https://github.com/NLPchina/elasticsearch-sql)) and JDBC driver (from [Anchormen](https://github.com/Anchormen/sql4es)).**
* **2016-02-16 Release 2.1.1-2 Remove build dependency to elasticsearch parent project.**
* **2016-02-01 Release 2.1.1-1 Add support for parent-child relationship.**
* **2016-01-28 Release 2.1.1 based on Elasticsearch 2.1.1 and cassandra 2.2.4.**
* **2015-12-20 Release 0.5 Re-index you data from cassandra 2.2.4 with zero downtime.**
* **2015-11-15 Release 0.4 New elassandra tarball ready-to-run.**

## Benefits of Elassandra

For cassandra users, elassandra provides elasticsearch features :
* Cassandra update are indexed in Elasticsearch.
* Full-Text and spatial search on your cassandra data.
* Real-time aggregation (does not require Spark or Hadoop to GROUP BY)
* Provide search on multiple keyspaces and tables in one query.
* Provide automatic schema creation and support nested document using [User Defined Types](https://docs.datastax.com/en/cql/3.1/cql/cql_using/cqlUseUDT.html).
* Provide a read/write JSON REST access to cassandra data.
* There are many elasticsearch plugins to import data in cassandra or to visualize your data, like [Kibana](https://www.elastic.co/guide/en/kibana/current/introduction.html) for example.

For Elasticsearch users, elassandra provides useful features :
* Change the mapping and re-index your data from cassandra with zero downtime.
* Cassandra could be your unique datastore for indexed and non-indexed data, it's easier to manage and secure. Source documents are now stored in Cassandra, reducing disk space if you need a noSql database and elasticsearch.
* In elassandra, Elasticsearch is masterless and split-brain resistant because cluster state is now managed within a [cassandra lightweight transactions](http://www.datastax.com/dev/blog/lightweight-transactions-in-cassandra-2-0).
* Write operations are not more restricted to one primary shards, but distributed on all cassandra nodes in a virtual datacenter. Number of shards does not limit your write throughput, just add some elassandra nodes to increase both read and write throughput.
* Elasticsearch indices can be replicated between many cassandra datacenters, allowing to write to the closest datacenter and search globally.
* The [cassandra driver](http://www.planetcassandra.org/client-drivers-tools/) is Datacenter and Token aware.
* Cassandra supports partial update and [distributed counters](http://docs.datastax.com/en/cql/3.1/cql/cql_using/use_counter_t.html).

# Quick start

* Ensure your JAVA_HOME points to your JDK 8 installation.
* Extract the distribution tarball in your install directory.
* Define the CASSANDRA_HOME environment variable: **export CASSANDRA_HOME=&lt;elassandra_install_dir&gt;**
* Load useful aliases : **source $CASSANDRA_HOME/bin/aliases.sh**
* Start a node: **$CASSANDRA_HOME/bin/cassandra -e** (or **elstart** alias)
* Check the Cassandra status: **$CASSANDRA_HOME/bin/nodetool status**
* Check the Elasticsearch cluster state: **curl -XGET localhost:9200/_cluster/state** (or **state** alias)

# Support

 * Support available via [elassandra google groups](https://groups.google.com/forum/#!forum/elassandra).
 * Post feature requests and bugs on https://github.com/strapdata/elassandra/issues
 
# How to contribute

Elassandra is based on a fork of Elasticsearch acting as a plugin for Apache Cassandra :
* The *ElassandraDaemon* class override the *CassandraDaemon* class in order to manage Elasticsearch internal services.
* The *ElasticSecondaryIndex* class implements the Cassandra *Index* interface to write in Elasticsearch indices.

![Elassandra class inheritance](/docs/elassandra/source/images/elassandra-inheritance.png)

To achieve these operations, both Cassandra and Elasticsearch requires some modifications located in two forks:
* ![Strapdata-Cassandra](https://github.com/strapdata/cassandra), a fork of ![Apache Cassandra](http://git-wip-us.apache.org/repos/asf/cassandra.git) including slight modifications :
 * Adds function hooks to start Elasticsearch (![CASSANDRA-13270](https://issues.apache.org/jira/browse/CASSANDRA-13270)).
 * Adds support for generic CQL functions (![CASSANDRA-13267](https://issues.apache.org/jira/browse/CASSANDRA-13267)).
 * Adds snapshot support for custom secondary indices (![CASSANDRA-13269](https://issues.apache.org/jira/browse/CASSANDRA-13269)).
 * Provides a multi-threaded **nodetool rebuild_index** command ([CASSANDRA-12837](https://issues.apache.org/jira/browse/CASSANDRA-12837)).
 * Reduce lock contention on instance factories ([CASSANDRA-13271](https://issues.apache.org/jira/browse/CASSANDRA-13271))
 * Fix implicit default *java.util.Locale* in String function calls (byte-code correction within an Ant task based on this [javassit-maven](https://github.com/strapdata/maven-javassist) project).
 * Minor upgrade some java libraries.
 * Don't overwrite the DefaultUncaughtExceptionHandler when testing
* Elassandra, a fork of Elasticsearch (aka Strapdata-Elasticsearch, branch *${version}-strapdata*) including modifications in :
 * Cluster state management ([org.elassandra.cluster.InternalCassandraClusterService](https://github.com/strapdata/elassandra/blob/master/core/src/main/java/org/elassandra/cluster/InternalCassandraClusterService.java) override a modified [org.elasticsearch.cluster.service.InternalClusterService](https://github.com/strapdata/elassandra/blob/master/core/src/main/java/org/elasticsearch/cluster/service/InternalClusterService.java))
 * Gateway to retrieve Elasticsearch metadata on startup (see [org.elassandra.gateway](https://github.com/strapdata/elassandra/blob/master/core/src/main/java/org/elassandra/gateway/CassandraGatewayService.java))
 * Discovery to manage alive cluster members (see [org.elassandra.discovery.CassandraDiscovery](https://github.com/strapdata/elassandra/blob/master/core/src/main/java/org/elassandra/discovery/CassandraDiscovery.java))
 * Fields mappers to manage CQL mapping and Lucene field factory (see [org.elasticsearch.index.mapper.core](https://github.com/strapdata/elassandra/tree/master/core/src/main/java/org/elasticsearch/index/mapper/core))
 * Search requests routing (see [org.elassandra.cluster.routing](https://github.com/strapdata/elassandra/tree/master/core/src/main/java/org/elassandra/cluster/routing))

As shown below, forked Cassandra and Elasticsearch projects can change independently and changes can be rebased periodically into Strapdata-Cassandra or Elassandra (aka Strapdata-Elasticsearch).

![Elassandra developpement process](/docs/elassandra/source/images/elassandra-devprocess.png)

Elassandra contains 2 references to the [strapdata-cassandra](https://github.com/strapdata/cassandra) :
* The Elasticsearch *core/pom.xml* include a maven dependency on the strapdata-cassandra project :
        ```
        <dependency>
          <groupId>com.strapdata.cassandra</groupId>
          <artifactId>cassandra-all</artifactId>
          <version>3.0.x</version>
        </dependency>
        ```
* In order to build the elassandra tarball and packages, the elassandra project includes a reference to the [strapdata-cassandra](https://github.com/strapdata/cassandra) as git submodule located in *core/cassandra*.

Elassandra is an opensource project, contributors are welcome to rise issues or pull requests on both [strapdata-cassandra](https://github.com/strapdata/cassandra) or [elassandra](https://github.com/strapdata/elassandra) github repositories. 

## Bug reports

When submitting an issue, please make sure that :

* You are testing against the latest version of Elassandra.
* You're not in the case of a known Elassandra limitation, see http://doc.elassandra.io/en/latest/limitations.html.
* Elassandra behavior is abnormally different from the standard Cassandra or Elasticsearch. For example, like Elasticsearch, Elassandra does not display default mappings unless requested, but this is the expected behavior.

It is very helpful if you can provide a test case to reproduce the bug and the associated error logs or stacktrace. See your *conf/logback.xml* to increase logging level in the *logs/system.log* file, and run *nodetool setlogginglevel* to dynamically update your logging level.

## Feature requests

Your're welcome to rise an issue on https://github.com/strapdata/elassandra for new feature, describing why and how it should work.

## Contributing code and documentation changes

Contributors can clone repositories and follow guidelines from Elasticsearch and Cassandra :
* [Contributing to the elasticsearch codebase](https://github.com/elastic/elasticsearch/blob/2.4/CONTRIBUTING.md#contributing-to-the-elasticsearch-codebase)
* [Cassandra How To Contribute](https://wiki.apache.org/cassandra/HowToContribute)

When cloning Elassandra, use a use **git clone --recurse-submodules https://github.com/strapdata/elassandra** to clone the strapdata-cassandra submodule and check that your are using the same strapdata-cassandra version in your *core/pom.xm* and in this submodule. Alternatively, this submodule can point to your own cassandra branch, assuming this branch include mandatory modifications to support Elassandra, see [strapdata-cassandra](https://github.com/strapdata/cassandra) for details.

Elassandra documentation is based on [sphinx](http://www.sphinx-doc.org/en/stable/rest.html) and published on [readthedoc.org](https://readthedocs.org/). Source RestructuredText files are located at [Elassandra source documentation](https://github.com/strapdata/elassandra/tree/master/docs/elassandra). To build the documentation, just run **make html** from the *${project.dir}/docs/elassandra*.

### Submitting your changes

1. Test you changes

You can build Elassandra single-node unit tests mixing Elasticsearch and Cassandra CQL/nodetool requests. See [Elassandra Testing](http://doc.elassandra.io/en/latest/testing.html) documentation and existing [Elassandra unit tests](https://github.com/strapdata/elassandra/tree/master/core/src/test/java/org/elassandra). For multi-nodes testing, you can use [ecm](https://github.com/strapdata/ecm), a fork of [ccm](https://github.com/pcmanus/ccm) running Elassandra.

2. Rebase your changes

Like with Elasticsearch, update your local repository with the most recent code from the main Elassandra repository, and rebase your branch on top
of the latest master branch. We prefer your initial changes to be squashed into a single commit. Later, if we ask you to make changes, add them as separate commits. This makes them easier to review. As a final step before merging we will either ask you to squash all commits yourself or we'll do it for you.

3. Submit a pull request

Finally, push your local changes to your forked copy of the elassandra repository and [submit a pull request](https://help.github.com/articles/using-pull-requests). In the pull request, choose a title which sums up the changes that you have made, including the issue number (ex: #91 null_value support), and provide details about your changes.

As usual, you should never force push to a publicly shared branch, but add incremental commits.

# License

```
This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

Copyright 2015-2017, Strapdata (contact@strapdata.com).

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

## Acknowledgments

* Elasticsearch and Kibana are trademarks of Elasticsearch BV, registered in the U.S. and in other countries.
* Apache Cassandra, Apache Lucene, Apache, Lucene and Cassandra are trademarks of the Apache Software Foundation.
