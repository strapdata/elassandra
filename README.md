# Elasticassandra

## Introduction

Elasticassandra is a fork of [Elasticsearch](https://github.com/elastic/elasticsearch) version 1.5 modified to run on top of [Apache Cassandra](http://cassandra.apache.org) in a scalable and resilient peer-to-peer architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.

Elasticassandra supports Cassandra vnodes and scale horizontally by adding more nodes. 

## Architecture

From an Elasticsearch perspective :
  - An Elasticsearch cluster is a Cassandra virtual datacenter.
  - Every Elasticassandra node is master primary data node.
  - Each node only index local data, with for each index, a primary local shard.
  - Elasticsearch data is not more stored in lucene indices, but in cassandra tables. An Elasticsearch index is mapped to a cassandra keyspace, and each index type is mapped to a cassandra table.
  - Elasticsearch discovery now rely on the cassandra [gossip](https://wiki.apache.org/cassandra/ArchitectureGossip) protocol. When a node join or leave the cluster, or when a schema change occure, elasticassandra update its state and local routing table.
  - Elasticsearch [gateway](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-gateway.html) now store metadata in a cassandra table and in the cassandra schema.
  - Elasticsearch API remain unchanged.
  - Plugins, REST and java API remain full operational.
  - Logging is now based on [logback](http://logback.qos.ch/) as cassandra.

From a Cassandra perspective :
 - Columns with an ElasticSecondaryIndex are indexed in ElasticSearch.
 - Elasticsearch configuration is stored in the elastic_admin keyspace.


## Contributing

Contributers are welcome to test and enhance Elasticassandra.

## License

Copyright 2015, Vincent Royer.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
