# Elassandra [![Build Status](https://travis-ci.org/strapdata/elassandra.svg)](https://travis-ci.org/strapdata/elassandra) [![Documentation Status](https://readthedocs.org/projects/elassandra/badge/?version=latest)](https://elassandra.readthedocs.io/en/latest/?badge=latest) [![GitHub release](https://img.shields.io/github/v/release/strapdata/elassandra.svg)](https://github.com/strapdata/elassandra/releases/latest)
[![Twitter](https://img.shields.io/twitter/follow/strapdataio?style=social)](https://twitter.com/strapdataio)

![Elassandra Logo](elassandra-logo.png)

## [http://www.elassandra.io/](http://www.elassandra.io/)

Elassandra is an [Apache Cassandra](http://cassandra.apache.org) distribution including an [Elasticsearch](https://github.com/elastic/elasticsearch) search engine.
Elassandra is a multi-master multi-cloud database and search engine with support for replicating across multiple datacenters in active/active mode.

Elasticsearch code is embedded in Cassandra nodes providing advanced search features on Cassandra tables and Cassandra serves as an Elasticsearch data and configuration store.

![Elassandra architecture](/docs/elassandra/source/images/elassandra1.jpg)

Elassandra supports Cassandra vnodes and scales horizontally by adding more nodes without the need to reshard indices.

Project documentation is available at [doc.elassandra.io](http://doc.elassandra.io).

## Benefits of Elassandra

For Cassandra users, elassandra provides Elasticsearch features :
* Cassandra updates are indexed in Elasticsearch.
* Full-text and spatial search on your Cassandra data.
* Real-time aggregation (does not require Spark or Hadoop to GROUP BY)
* Provide search on multiple keyspaces and tables in one query.
* Provide automatic schema creation and support nested documents using [User Defined Types](https://docs.datastax.com/en/cql/3.1/cql/cql_using/cqlUseUDT.html).
* Provide read/write JSON REST access to Cassandra data.
* Numerous Elasticsearch plugins and products like [Kibana](https://www.elastic.co/guide/en/kibana/current/introduction.html).
* Manage concurrent elasticsearch mappings changes and applies batched atomic CQL schema changes.
* Support [Elasticsearch ingest processors](https://www.elastic.co/guide/en/elasticsearch/reference/master/ingest.html) allowing to transform input data.

For Elasticsearch users, elassandra provides useful features :
* Elassandra is masterless. Cluster state is managed through [cassandra lightweight transactions](http://www.datastax.com/dev/blog/lightweight-transactions-in-cassandra-2-0).
* Elassandra is a sharded multi-master database, where Elasticsearch is sharded master-slave. Thus, Elassandra has no Single Point Of Write, helping to achieve high availability.
* Elassandra inherits Cassandra data repair mechanisms (hinted handoff, read repair and nodetool repair) providing support for **cross datacenter replication**.
* When adding a node to an Elassandra cluster, only data pulled from existing nodes are re-indexed in Elasticsearch.
* Cassandra could be your unique datastore for indexed and non-indexed data. It's easier to manage and secure. Source documents are now stored in Cassandra, reducing disk space if you need a NoSQL database and Elasticsearch.
* Write operations are not restricted to one primary shard, but distributed across all Cassandra nodes in a virtual datacenter. The number of shards does not limit your write throughput. Adding elassandra nodes increases both read and write throughput.
* Elasticsearch indices can be replicated among many Cassandra datacenters, allowing write to the closest datacenter and search globally.
* The [cassandra driver](http://www.planetcassandra.org/client-drivers-tools/) is Datacenter and Token aware, providing automatic load-balancing and failover.
* Elassandra efficiently stores Elasticsearch documents in binary SSTables without any JSON overhead.

## Quick start

* [Quick Start](http://doc.elassandra.io/en/latest/quickstart.html) guide to run a single node Elassandra cluster in docker.
* [Deploy Elassandra by launching a Google Kubernetes Engine](./docs/google-kubernetes-tutorial.md):

  [![Open in Cloud Shell](https://gstatic.com/cloudssh/images/open-btn.png)](https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/strapdata/elassandra-google-k8s-marketplace&tutorial=docs/google-kubernetes-tutorial.md)
  
## Upgrade Instructions


#### Elassandra 6.8.4.2+

<<<<<<< HEAD
Since version 6.8.4.2, the gossip X1 application state can be compressed using a system property. Enabling this settings allows the creation of a lot of virtual indices.
Before enabling this setting, upgrade all the 6.8.4.x nodes to the 6.8.4.2 (or higher). Once all the nodes are in 6.8.4.2, they are able to decompress the application state even if the settings isn't yet configured locally.

#### Elassandra 6.2.3.25+

Elassandra use the Cassandra GOSSIP protocol to manage the Elasticsearch routing table and Elassandra 6.8.4.2+ add support for compression of
the X1 application state to increase the maxmimum number of Elasticsearch indices. For backward compatibility, the compression is disabled by default, 
but once all your nodes are upgraded into version 6.8.4.2+, you should enable the X1 compression by adding **-Des.compress_x1=true** in your **conf/jvm.options** and rolling restart all nodes.
Nodes running version 6.8.4.2+ are able to read compressed and not compressed X1.

#### Elassandra 6.2.3.21+

Before version 6.2.3.21, the Cassandra replication factor for the **elasic_admin** keyspace (and elastic_admin_[datacenter.group]) was automatically adjusted to the 
number of nodes of the datacenter. Since version 6.2.3.21 and because it has a performance impact on large clusters, it's now up to your Elassandra administrator to 
properly adjust the replication factor for this keyspace. Keep in mind that Elasticsearch mapping updates rely on a PAXOS transaction that requires QUORUM nodes to succeed, 
so replication factor should be at least 3 on each datacenter.

#### Elassandra 6.2.3.19+

Elassandra 6.2.3.19 metadata version now relies on the Cassandra table **elastic_admin.metadata_log** (that was **elastic_admin.metadata** from 6.2.3.8 to 6.2.3.18) 
to keep the elasticsearch mapping update history and automatically recover from a possible PAXOS write timeout issue. 

When upgrading the first node of a cluster, Elassandra automatically copy the current **metadata.version** into the new **elastic_admin.metadata_log** table.
To avoid Elasticsearch mapping inconsistency, you must avoid mapping update while the rolling upgrade is in progress. Once all nodes are upgraded,
the **elastic_admin.metadata** is not more used and can be removed. Then, you can get the mapping update history from the new **elastic_admin.metadata_log** and know
which node has updated the mapping, when and for which reason.

#### Elassandra 6.2.3.8+

Elassandra 6.2.3.8+ now fully manages the elasticsearch mapping in the CQL schema through the use of CQL schema extensions (see *system_schema.tables*, column *extensions*). These table extensions and the CQL schema updates resulting of elasticsearch index creation/modification are updated in batched atomic schema updates to ensure consistency when concurrent updates occurs. Moreover, these extensions are stored in binary and support partial updates to be more efficient. As the result, the elasticsearch mapping is not more stored in the *elastic_admin.metadata* table. 

WARNING: During the rolling upgrade, elasticserach mapping changes are not propagated between nodes running the new and the old versions, so don't change your mapping while you're upgrading. Once all your nodes have been upgraded to 6.2.3.8+ and validated, apply the following CQL statements to remove useless elasticsearch metadata:
```bash
ALTER TABLE elastic_admin.metadata DROP metadata;
ALTER TABLE elastic_admin.metadata WITH comment = '';
```

WARNING: Due to CQL table extensions used by Elassandra, some old versions of **cqlsh** may lead to the following error message **"'module' object has no attribute 'viewkeys'."**. This comes from the old python cassandra driver embedded in Cassandra and has been reported in [CASSANDRA-14942](https://issues.apache.org/jira/browse/CASSANDRA-14942). Possible workarounds:
* Use the **cqlsh** embedded with Elassandra
* Install a recent version of the  **cqlsh** utility (*pip install cqlsh*) or run it from a docker image:

```bash
docker run -it --rm strapdata/cqlsh:0.1 node.example.com
```

#### Elassandra 6.x changes

* Elasticsearch now supports only one document type per index backed by one Cassandra table. Unless you specify an elasticsearch type name in your mapping, data is stored in a cassandra table named **"_doc"**. If you want to search many cassandra tables, you now need to create and search many indices.
* Elasticsearch 6.x manages shard consistency through several metadata fields (_primary_term, _seq_no, _version) that are not used in elassandra because replication is fully managed by cassandra.

## Installation

Ensure Java 8 is installed and `JAVA_HOME` points to the correct location.

* [Download](https://github.com/strapdata/elassandra/releases) and extract the distribution tarball
* Define the CASSANDRA_HOME environment variable : `export CASSANDRA_HOME=<extracted_directory>`
* Run `bin/cassandra -e`
* Run `bin/nodetool status`
* Run `curl -XGET localhost:9200/_cluster/state`

#### Example

Try indexing a document on a non-existing index:

```bash
curl -XPUT 'http://localhost:9200/twitter/_doc/1?pretty' -H 'Content-Type: application/json' -d '{
    "user": "Poulpy",
    "post_date": "2017-10-04T13:12:00Z",
    "message": "Elassandra adds dynamic mapping to Cassandra"
}'
```

Then look-up in Cassandra:

```bash
bin/cqlsh -e "SELECT * from twitter.\"_doc\""
```

Behind the scenes, Elassandra has created a new Keyspace `twitter` and table `_doc`.

```CQL
admin@cqlsh>DESC KEYSPACE twitter;

CREATE KEYSPACE twitter WITH replication = {'class': 'NetworkTopologyStrategy', 'DC1': '1'}  AND durable_writes = true;

CREATE TABLE twitter."_doc" (
    "_id" text PRIMARY KEY,
    message list<text>,
    post_date list<timestamp>,
    user list<text>
) WITH bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';
CREATE CUSTOM INDEX elastic__doc_idx ON twitter."_doc" () USING 'org.elassandra.index.ExtendedElasticSecondaryIndex';
```

By default, multi valued Elasticsearch fields are mapped to Cassandra list.
Now, insert a row with CQL :

```CQL
INSERT INTO twitter."_doc" ("_id", user, post_date, message)
VALUES ( '2', ['Jimmy'], [dateof(now())], ['New data is indexed automatically']);
SELECT * FROM twitter."_doc";

 _id | message                                          | post_date                           | user
-----+--------------------------------------------------+-------------------------------------+------------
   2 |            ['New data is indexed automatically'] | ['2019-07-04 06:00:21.893000+0000'] |  ['Jimmy']
   1 | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-04 13:12:00.000000+0000'] | ['Poulpy']

(2 rows)
```

Then search for it with the Elasticsearch API:

```bash
curl "localhost:9200/twitter/_search?q=user:Jimmy&pretty"
```

And here is a sample response :

```JSON
{
  "took" : 3,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 1,
    "max_score" : 0.6931472,
    "hits" : [
      {
        "_index" : "twitter",
        "_type" : "_doc",
        "_id" : "2",
        "_score" : 0.6931472,
        "_source" : {
          "post_date" : "2019-07-04T06:00:21.893Z",
          "message" : "New data is indexed automatically",
          "user" : "Jimmy"
        }
      }
    ]
  }
}
```

## Support

 * Commercial support is available through [Strapdata](http://www.strapdata.com/).
 * Community support available via [elassandra google groups](https://groups.google.com/forum/#!forum/elassandra).
 * Post feature requests and bugs on https://github.com/strapdata/elassandra/issues

## License

```
This software is licensed under the Apache License, version 2 ("ALv2"), quoted below.

Copyright 2015-2018, Strapdata (contact@strapdata.com).

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

* Elasticsearch, Logstash, Beats and Kibana are trademarks of Elasticsearch BV, registered in the U.S. and in other countries.
* Apache Cassandra, Apache Lucene, Apache, Lucene and Cassandra are trademarks of the Apache Software Foundation.
* Elassandra is a trademark of Strapdata SAS.
