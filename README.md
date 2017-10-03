# Elassandra [![Build Status](https://travis-ci.org/strapdata/elassandra.svg?branch=v2.4.5-strapdata)](https://travis-ci.org/strapdata/elassandra) [![Doc Status](https://readthedocs.org/projects/elassandra/badge/?version=latest)](http://doc.elassandra.io)

Elassandra is a fork of [Elasticsearch](https://github.com/elastic/elasticsearch) modified to run as a plugin for [Apache Cassandra](http://cassandra.apache.org) in a scalable and resilient peer-to-peer architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.

![Elassandra architecture](/docs/elassandra/source/images/elassandra1.jpg)

Elassandra supports Cassandra vnodes and scales horizontally by adding more nodes.

Project documentation is available at [doc.elassandra.io](http://doc.elassandra.io).

## Benefits of Elassandra

For Cassandra users, elassandra provides Elasticsearch features :
* Cassandra update are indexed in Elasticsearch.
* Full-text and spatial search on your Cassandra data.
* Real-time aggregation (does not require Spark or Hadoop to GROUP BY)
* Provide search on multiple keyspaces and tables in one query.
* Provide automatic schema creation and support nested document using [User Defined Types](https://docs.datastax.com/en/cql/3.1/cql/cql_using/cqlUseUDT.html).
* Provide a read/write JSON REST access to Cassandra data.
* Numerous Elasticsearch plugins and products like [Kibana](https://www.elastic.co/guide/en/kibana/current/introduction.html).

For Elasticsearch users, elassandra provides useful features :
* Change the mapping and re-index your data from Cassandra with zero downtime.
* Cassandra could be your unique datastore for indexed and non-indexed data, it's easier to manage and secure. Source documents are now stored in Cassandra, reducing disk space if you need a NoSQL database and Elasticsearch.
* In elassandra, Elasticsearch is masterless and split-brain resistant because cluster state is now managed within a [cassandra lightweight transactions](http://www.datastax.com/dev/blog/lightweight-transactions-in-cassandra-2-0).
* Write operations are not more restricted to one primary shards, but distributed on all Cassandra nodes in a virtual datacenter. Number of shards does not limit your write throughput, just add some elassandra nodes to increase both read and write throughput.
* Elasticsearch indices can be replicated between many Cassandra datacenters, allowing to write to the closest datacenter and search globally.
* The [cassandra driver](http://www.planetcassandra.org/client-drivers-tools/) is Datacenter and Token aware, providing automatic load-balancing.
* Cassandra supports partial update and [distributed counters](http://docs.datastax.com/en/cql/3.1/cql/cql_using/use_counter_t.html).

## Quick start

#### Requirements

Ensure Java 8 is installed and `JAVA_HOME` points to the correct location

#### Installation

* [Download](https://github.com/strapdata/elassandra/releases) and extract the distribution tarball
* Define the CASSANDRA_HOME environment variable : `export CASSANDRA_HOME=<extracted_directory>`
* Run `bin/cassandra -e`
* Run `bin/nodetool status`
* Run `curl -XGET localhost:9200/_cluster/state`

#### Example

Try indexing a document on a non-existing index:
```bash
curl -XPUT 'http://localhost:9200/twitter/doc/1?pretty' -H 'Content-Type: application/json' -d '
{
    "user": "Poulpy",
    "post_date": "2017-10-4T13:12:00",
    "message": "Elassandra adds dynamic mapping to Cassandra"
}'
```

Then look-up in Cassandra:
```bash
bin/cqlsh -c "SELECT * from twitter.doc"
```
Behind the scene, Elassandra has created a new Keyspace `twitter` and table `doc`.

Now, insert a row with CQL :
```CQL
INSERT INTO twitter.doc ("_id", user, post_date, message)
VALUES ( '2', ['Jimmy'], [dateof(now())],
              ['New data is indexed automatically']);
```

Then search for it with the Elasticsearch API:
```bash
curl "localhost:9200/twitter/_search?q=user:Jimmy&pretty"
```

And here is a sample response :
```JSON
{
  "took" : 1,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "failed" : 0
  },
  "hits" : {
    "total" : 1,
    "max_score" : 0.9808292,
    "hits" : [
      {
        "_index" : "twitter",
        "_type" : "doc",
        "_id" : "2",
        "_score" : 0.9808292,
        "_source" : {
          "post_date" : "2017-10-04T13:20:00",
          "message" : "New data is indexed automatically",
          "user" : "Jimmy"
        }
      }
    ]
  }
}

```

## Support

 * Support available via [elassandra google groups](https://groups.google.com/forum/#!forum/elassandra).
 * Post feature requests and bugs on https://github.com/strapdata/elassandra/issues

## License

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
