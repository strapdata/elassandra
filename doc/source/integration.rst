Integration
===========

Integration with an existing cassandra cluster
----------------------------------------------

Elassandra is currently based on cassandra version 2.2, so you can deploy-it within a existing cassandra cluster running version 2.2.x. 
(Merging differents cassandra major version is not possible due to transport protocol changes).

Setup a new elassandra datacenter
.................................

The overall procedure is similar the cassandra one describe on <https://docs.datastax.com/en/cassandra/2.1/cassandra/operations/ops_add_dc_to_cluster_t.html>_.

* Set ``auto_bootstrap: false`` in your **conf/cassandra.yaml**.
* Start cassandra nodes in your new datacenter and check that all nodes join the cluster.

.. code::
   
   bin/cassandra

* Restart all nodes in your new datacenter with elasticsearch enable. You should see started shards but empty indices.

.. code::
   
   bin/cassandra -e

* Set the replication factor of indexed keyspaces to one or more in your newdatacenter.
* Run **nodetool rebuild ** to pull data from your existaing datacenter. 

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

Upgarding an existing datacenter to Elassandra
..............................................

Rather than creating a new datacenter, you can replace cassandra binaries with elassandra ones. 

* Install elassandra
* Configure paths for *data*, *commitlogs* and *saved_cache* in ``bin/cassandra.yaml``
* Stop cassandra 
* Start elassandra 

.. code::

   bin/cassandra -e

* Create new elasticsearch index or map some existing cassandra tables.

Installing Elasticsearch plugins
--------------------------------

Elasticsearch plugin installation remins unchanged, see elasticsearch `plugin installation <https://www.elastic.co/guide/en/elasticsearch/plugins/2.3/installation.html>`_.

* bin/plugin install <url>


Running Kibana with Elassandra
------------------------------

`Kibana <https://www.elastic.co/guide/en/kibana/4.3/introduction.html>'_ version 4.3 can run with Elassandra, providing a visualization tool for cassandra and elasticsearch data. 

Because cassandra keyspace, type and table can only contain alphanumeric and underscore characters 
(see `cassandra documentation <http://docs.datastax.com/en/cql/3.1/cql/cql_reference/ref-lexical-valid-chars.html>`_), 
the same restriction applies to index and type names.

* Replace the index name **.kibana** by **kibana** in *config/kibana.yaml*.
* Replace **'index-pattern'** by **'index_pattern'** in the source code with the following sed command:

.. code::

   q=\'
   # for Kibana 4.1.x
   sed -i .bak -e "s/type: ${q}index-pattern${q}/type: ${q}index_pattern${q}/g" -e "s/type = ${q}index-pattern${q}/type = ${q}index_pattern${q}/g" index.js
   # for Kibana 4.3.x (for Elassandra v2.1.1+)
   sed -i .bak -e "s/type: ${q}index-pattern${q}/type: ${q}index_pattern${q}/g" -e "s/type = ${q}index-pattern${q}/type = ${q}index_pattern${q}/g" -e "s%${q}index-pattern${q}: ${q}/settings/objects/savedSearches/${q}%${q}index_pattern${q}: ${q}/settings/objects/savedSearches/${q}%g" optimize/bundles/kibana.bundle.js src/ui/public/index_patterns/*.js

* If you want to load sample data from Kibana Getting started](https://www.elastic.co/guide/en/kibana/current/getting-started.html), apply the following changes to logstash.jsonl with a sed command. 

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

The `Elasticsearch JDBC driver <https://github.com/Anchormen/sql4es>`_. can be used with elassandra. Here is a code exemple :

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

 