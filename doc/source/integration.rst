Integration
===========

Integration with an existing cassandra cluster
----------------------------------------------

Elassandra include a modified version of cassandra 2.2, so **all nodes of a cluster should run elassandra binaries** [1] However, you can start a node with or without the elasticsearch support. 
Obviously, all nodes of a datacenter should run cassandra only or cassandra with elasticsearch.

[1] This is mainly because the ``DatacenterReplicationStrategy`` class (a replication strategy replicating to all nodes of a datacenter, whatever the number of nodes) and 
a dummy version of the custom index classes should be deployed on all nodes to be instancied when loading the CQL schema.

Rolling upgrade to elassandra
.............................

Before starting any elassandra node with elasticsearch enable, do a rolling replace of the cassandra binaries by the elassandra ones. For each node :

* Install elassandra.
* Replace the elassandra configuration files by the one from your existing cluster (cassandra.yml and sntich configuration file) 
* Stop you cassandra ndoe.
* Restart cassandra ``elassandra bin/cassandra`` or cassandra with elasticsearch enable ``elassandra bin/cassandra -e``


Create a new elassandra datacenter
..................................

The overall procedure is similar the cassandra one describe on https://docs.datastax.com/en/cassandra/2.1/cassandra/operations/ops_add_dc_to_cluster_t.html.

For earch nodes in your new datacenter :

* Install elassandra.
* Set ``auto_bootstrap: false`` in your **conf/cassandra.yaml**.
* Start cassandra-only nodes in your new datacenter and check that all nodes join the cluster.

.. code::

   bin/cassandra

* Restart all nodes in your new datacenter with elasticsearch enable. You should see started shards but empty indices.

.. code::

   bin/cassandra -e

* Set the replication factor of indexed keyspaces to one or more in your new datacenter.
* Pull data from your existaing datacenter. 

.. code::
   
   nodetool rebuild <source-datacenter-name>

After rebuild on all your new nodes, you should see the same number of document for each indices in your new and existing datacenters.

* Set ``auto_bootstrap: true`` (default value) in your **conf/cassandra.yaml**
* Create new elasticsearch index or map some existing cassandra tables.

.. TIP::
   If you need to replay this procedure for a node :
   
   * stop your node
   * nodetool removenode <id-of-node-to-remove>
   * clear data, commitlogs and saved_cache directory.


Installing an Elasticsearch plugins
-----------------------------------

Elasticsearch plugin installation remains unchanged, see elasticsearch `plugin installation <https://www.elastic.co/guide/en/elasticsearch/plugins/2.3/installation.html>`_.

* bin/plugin install <url>


Running Kibana with Elassandra
------------------------------

`Kibana <https://www.elastic.co/guide/en/kibana/4.3/introduction.html>`_ version 4.3 can run with Elassandra, providing a visualization tool for cassandra and elasticsearch data.

Because cassandra keyspace, type and table can only contain alphanumeric and underscore characters (see `cassandra documentation <http://docs.datastax.com/en/cql/3.1/cql/cql_reference/ref-lexical-valid-chars.html>`_), the same restriction applies to index and type names.

* Replace the index name **.kibana** by **kibana** in *config/kibana.yaml*.
* Replace **'index-pattern'** by **'index_pattern'** in the source code with the following sed command:

.. code::

   q=\'
   # for Kibana 4.1.x
   sed -i .bak -e "s/type: ${q}index-pattern${q}/type: ${q}index_pattern${q}/g" -e "s/type = ${q}index-pattern${q}/type = ${q}index_pattern${q}/g" index.js
   # for Kibana 4.3.x (for Elassandra v2.1.1+)
   sed -i .bak -e "s/type: ${q}index-pattern${q}/type: ${q}index_pattern${q}/g" -e "s/type = ${q}index-pattern${q}/type = ${q}index_pattern${q}/g" -e "s%${q}index-pattern${q}: ${q}/settings/objects/savedSearches/${q}%${q}index_pattern${q}: ${q}/settings/objects/savedSearches/${q}%g" optimize/bundles/kibana.bundle.js src/ui/public/index_patterns/*.js

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

 