# How to contribute

Elassandra is based on a fork of Elasticsearch acting as a plugin for Apache Cassandra :
* The **ElassandraDaemon** class override the **CassandraDaemon** class in order to manage Elasticsearch internal services.
* The **ElasticSecondaryIndex** class implements the Cassandra **Index** interface to write in Elasticsearch indices.

![Elassandra class inheritance](/docs/elassandra/source/images/elassandra-inheritance.png)

To achieve these operations, both Cassandra and Elasticsearch requires some modifications located in two forks:

A fork of [Apache Cassandra](http://git-wip-us.apache.org/repos/asf/cassandra.git) including slight modifications, see (https://github.com/strapdata/cassandra).

A fork of Elasticsearch 5.5.0 (aka Strapdata-Elasticsearch, branch *${version}-strapdata*) including modifications in :
* Cluster state management ([org.elassandra.cluster.InternalCassandraClusterService](/core/src/main/java/org/elassandra/cluster/InternalCassandraClusterService.java) override a modified [org.elasticsearch.cluster.service.InternalClusterService](/core/src/main/java/org/elasticsearch/cluster/service/InternalClusterService.java))
* Gateway to retrieve Elasticsearch metadata on startup (see [org.elassandra.gateway](/core/src/main/java/org/elassandra/gateway/CassandraGatewayService.java))
* Discovery to manage alive cluster members (see [org.elassandra.discovery.CassandraDiscovery](/core/src/main/java/org/elassandra/discovery/CassandraDiscovery.java))
* Fields mappers to manage CQL mapping and Lucene field factory (see [org.elasticsearch.index.mapper.core](/core/src/main/java/org/elasticsearch/index/mapper/core))
* Search requests routing (see [org.elassandra.cluster.routing](/core/src/main/java/org/elassandra/cluster/routing))

As shown below, forked Cassandra and Elasticsearch projects can change independently and changes can be rebased periodically into Strapdata-Cassandra or Elassandra (aka Strapdata-Elasticsearch).

![Elassandra developpement process](/docs/elassandra/source/images/elassandra-devprocess.png)

Elassandra contains 2 references to the [strapdata-cassandra](https://github.com/strapdata/cassandra) :
* The Elassandra version 5+ **core/pom.xml** include a maven dependency on the strapdata-cassandra project
* The Elassandra version 6+ **buildSrc/version.properties** include a gradle dependency on the strapdata-cassandra project.
* In order to build the elassandra tarball and packages, the elassandra project includes a reference to the [strapdata-cassandra](https://github.com/strapdata/cassandra) as git submodule located in **core/cassandra** for version 5+ or **server/cassandra** for version 6+.

Elassandra is an opensource project, contributors are welcome to rise issues or pull requests on both [strapdata-cassandra](https://github.com/strapdata/cassandra) or [elassandra](https://github.com/strapdata/elassandra) github repositories.

## Bug reports

When submitting an issue, please make sure that :

* You are testing against the latest version of Elassandra.
* You're not in the case of a known Elassandra limitation, see http://doc.elassandra.io/en/latest/limitations.html.
* Elassandra behavior is abnormally different from the standard Cassandra or Elasticsearch. For example, like Elasticsearch, Elassandra does not display default mappings unless requested, but this is the expected behavior.

It is very helpful if you can provide a test case to reproduce the bug and the associated error logs or stacktrace. See your **conf/logback.xml** to increase logging level in the **logs/system.log** file, and run **nodetool setlogginglevel** to dynamically update your logging level.

## Feature requests

Your're welcome to rise an issue on https://github.com/strapdata/elassandra for new feature, describing why and how it should work.

## Contributing code and documentation changes

Contributors can clone repositories and follow guidelines from Elasticsearch and Cassandra :
* [Contributing to the elasticsearch codebase](https://github.com/elastic/elasticsearch/blob/2.4/CONTRIBUTING.md#contributing-to-the-elasticsearch-codebase)
* [Cassandra How To Contribute](https://wiki.apache.org/cassandra/HowToContribute)ls 

When cloning Elassandra, use **git clone --recurse-submodules https://github.com/strapdata/elassandra** to clone the strapdata-cassandra submodule and check that your are using the same strapdata-cassandra version in  *core/pom.xm* for elassandra v5.x (or *buildSrc/version.properties* for v6.x) and in this submodule. Alternatively, this submodule can point to your own cassandra branch, assuming this branch include mandatory modifications to support Elassandra, see [strapdata-cassandra](https://github.com/strapdata/cassandra) for details.

If you forgot the **--recurse-submodules** when cloning, you can also fetch the cassandra submodule with **git submodule update --init** and **git checkout cassandra-3.x-strapdata** to set the strapdata branch.

Then, to build from sources:
* Elassandra v5.x: run **mvn clean packages -DskipTests**.
* Elassandra v6.x: run **gradle clean distribution:deb:assemble -Dbuild.snapshot=false**.

Note: For elassandra v6.X, javadoc task failed due to [https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8194281](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8194281).

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
