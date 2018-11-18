Integration
===========

Integration with an existing cassandra cluster
----------------------------------------------

Elassandra includes a modified version of cassandra, available at `strapdata-cassandra repro <https://github.com/strapdata/cassandra>`_, 
so **all nodes of a cluster should run elassandra binaries**. However, you can start a node with or without 
the elasticsearch support.  Obviously, all nodes of a datacenter should run cassandra only or cassandra with 
elasticsearch.

Rolling upgrade to elassandra
.............................

Before starting any elassandra node with elasticsearch enabled, do a rolling replace of the cassandra binaries with the elassandra ones. For each node :

* Install elassandra.
* Replace the elassandra configuration files (cassandra.yml and snitch configuration file) with the ones from your existing cluster 
* Stop your cassandra node.
* Restart cassandra ``elassandra bin/cassandra`` or cassandra with elasticsearch enabled ``elassandra bin/cassandra -e``


Create a new elassandra datacenter
..................................

The overall procedure is similar to the cassandra one described in `Adding a datacenter to a cluster <https://docs.datastax.com/en/cassandra/3.0/cassandra/operations/opsAddDCToCluster.html#opsAddDCToCluster>`_.

For each node in your new datacenter :

* Install elassandra.
* Set ``auto_bootstrap: false`` in your **conf/cassandra.yaml**.
* Start cassandra-only nodes in your new datacenter and check that all nodes join the cluster.

.. code::

   bin/cassandra

* Restart all nodes in your new datacenter with elasticsearch enabled. You should see started shards but empty indices.

.. code::

   bin/cassandra -e

* Set the replication factor of indexed keyspaces to one or more in your new datacenter.
* Pull data from your existing datacenter. 

.. code::
   
   nodetool rebuild <source-datacenter-name>

After rebuilding all of your new nodes, you should see the same number of documents for each index in your new and existing datacenters.

* Set ``auto_bootstrap: true`` (default value) in your **conf/cassandra.yaml**
* Create new elasticsearch index or map some existing cassandra tables.

.. TIP::
   If you need to replay this procedure for a node :
   
   * stop your node
   * nodetool removenode <id-of-node-to-remove>
   * clear data, commitlogs and saved_cache directories.


Installing an Elasticsearch plugins
-----------------------------------

Elasticsearch plugin installation remains unchanged, see elasticsearch `plugin installation <https://www.elastic.co/guide/en/elasticsearch/plugins/5.5/installation.html>`_.

* bin/plugin install <url>


Running Kibana with Elassandra
------------------------------

`Kibana <https://www.elastic.co/guide/en/kibana/5.5/introduction.html>`_ can run with Elassandra, providing a visualization tool for cassandra and elasticsearch data.

* If you want to load sample data from the `Kibana Getting started <https://www.elastic.co/guide/en/kibana/current/getting-started.html>`_, apply the following changes to logstash.jsonl with a sed command.

.. code::

   s/logstash-2015.05.18/logstash_20150518/g
   s/logstash-2015.05.19/logstash_20150519/g
   s/logstash-2015.05.20/logstash_20150520/g
   
   s/article:modified_time/articleModified_time/g
   s/article:published_time/articlePublished_time/g
   s/article:section/articleSection/g
   s/article:tag/articleTag/g
   
   s/og:type/ogType/g
   s/og:title/ogTitle/g
   s/og:description/ogDescription/g
   s/og:site_name/ogSite_name/g
   s/og:url/ogUrl/g
   s/og:image:width/ogImageWidth/g
   s/og:image:height/ogImageHeight/g
   s/og:image/ogImage/g
   
   s/twitter:title/twitterTitle/g
   s/twitter:description/twitterDescription/g
   s/twitter:card/twitterCard/g
   s/twitter:image/twitterImage/g
   s/twitter:site/twitterSite/g

JDBC Driver sql4es + Elassandra
-------------------------------

The `Elasticsearch JDBC driver <https://github.com/Anchormen/sql4es>`_. can be used with elassandra. Here is a code example :

.. code:: java

   Class.forName("nl.anchormen.sql4es.jdbc.ESDriver");
   Connection con = DriverManager.getConnection("jdbc:sql4es://localhost:9300/twitter?cluster.name=Test%20Cluster");
   Statement st = con.createStatement();
   ResultSet rs = st.executeQuery("SELECT user,avg(size),count(*) FROM tweet GROUP BY user");
   ResultSetMetaData rsmd = rs.getMetaData();
   int nrCols = rsmd.getColumnCount();
   while(rs.next()){
       for(int i=1; i<=nrCols; i++){
            System.out.println(rs.getObject(i));
        }
   }
   rs.close();
   con.close();

Running Spark with Elassandra
-----------------------------

For elassandra 5.5, a modified version of the `elasticsearch-hadoop <https://github.com/elastic/elasticsearch-hadoop>`_ connector is available for elassandra on the `strapdata repository <https://github.com/strapdata/elasticsearch-hadoop>`_. 
This connector works with spark as described in the elasticsearch documentation available at `elasticsearch/hadoop <https://www.elastic.co/guide/en/elasticsearch/hadoop/current/index.html>`_.

For example, in order to submit a spark job in client mode:

.. code:: java

   bin/spark-submit --driver-class-path <yourpath>/elasticsearch-spark_2.10-2.2.0.jar  --master spark://<sparkmaster>:7077 --deploy-mode client <application.jar> 



