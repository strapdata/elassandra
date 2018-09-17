## Elassandra docker image

[Elassandra](https://github.com/strapdata/elassandra) is a fork of [Elasticsearch](https://github.com/elastic/elasticsearch) modified to run on top of [Apache Cassandra](http://cassandra.apache.org/) in a scalable and resilient peer-to-peer architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.

Check-out the [elassandra documentation](http://doc.elassandra.io/en/latest) for detailed instructions.

Commercial support is available from [Strapdata](https://www.strapdata.com).

## Usage

This Elassandra image is available on [docker hub](https://hub.docker.com/r/strapdata/elassandra/)

```bash
docker pull strapdata/elassandra
```

#### Start a single-node cluster

```bash
docker run --name el strapdata/elassandra
```

#### Connect with cqlsh

```bash
docker run -it --link el --rm strapdata/elassandra cqlsh some-elassandra
```

#### Connect to Elasticsearch API with curl

```bash
docker run -it --link el --rm strapdata/elassandra curl some-elassandra:9200
```

#### Exposed ports

* 7000: Intra-node communication
* 7001: TLS intra-node communication
* 7199: JMX
* 9042: CQL
* 9160: thrift service
* 9200: ElasticSearch HTTP
* 9300: ElasticSearch transport

#### Volumes

* /var/lib/cassandra

## More information

This docker image is based on [strapdata/elassandra](https://hub.docker.com/r/strapdata/elassandra/) inspired from [docker-library/cassandra](https://github.com/docker-library/cassandra). For more complicated setups, please refer to the [documentation](https://github.com/docker-library/docs/tree/master/cassandra).


* A **logback.xml** with environment variables allows to manage debug levels from your docker env section. 
* For kubernetes usage, a **ready_probe.sh** script can be use for readiness probe as follow:

    readinessProbe:
        exec:
          command: [ "/bin/bash", "-c", "/ready-probe.sh" ]
        initialDelaySeconds: 15
        timeoutSeconds: 5