Search through CQL
==================

Executing Elasticsearch queries through through the Cassandra CQL driver provides several benefits:

* Eliminates the needs for an HTTP load balancer because the drivers are cluster aware and will load balance for you.
* Simplify the development of your application.
* Get integrated security with Cassandra authentication and TLS encryption.

Configuration
.............

To enable Elasticsearch query over CQL:

* Add the following system property to your cassandra-env.sh and restart your nodes :

.. code::

   JVM_OPTS="$JVM_OPTS -Dcassandra.custom_query_handler_class=org.elassandra.index.ElasticQueryHandler"
   
* Add a dummy column ``es_query`` to your cassandra table.
* Add a dummy column ``es_options`` to your cassandra table if you need to specify some specific options like target index names.

.. code::

   ALTER TABLE twitter.tweet ADD es_query text;
   ALTER TABLE twitter.tweet ADD es_options text;

Search request through CQL
..........................

Then you can query the associated Elasticsearch index directly into a CQL SELECT request such as (document *_type* is the cassandra table name).

.. code::

   cassandra@cqlsh> SELECT "_id",foo FROM twitter.tweet WHERE es_query='{"query":{"query_string":{"query":"bar2*"}}}';
   
    _id | foo
   -----+-------
      2 |  bar2
     20 | bar20
     22 | bar22
     23 | bar23
     24 | bar24
     28 | bar28
     21 | bar21
     25 | bar25
     26 | bar26
     27 | bar27
   
   (10 rows)

By default, an elasticsearch query returns the first 10 results, but you can request more or less results with the LIMIT clause.

.. code::

   cassandra@cqlsh> SELECT "_id",foo FROM twitter.tweet WHERE es_query='{"query":{"query_string":{"query":"bar2*"}}}' LIMIT 3;
   
    _id | foo
   -----+-------
      2 |  bar2
     20 | bar20
     22 | bar22
   
   (3 rows)

If `paging <https://docs.datastax.com/en/developer/java-driver/3.3/manual/paging/>`_ is enabled on your Cassandra driver and you request additional 
results than your page size, Elassandra will use an Elasticsearch scrolled search request to retreive all the results. Default scoll timeout is 60 seconds.

If all partition key columns are set in the where clause, the Elasticsearch query will be directly sent to a node hosting the data (no fan out).

.. code::

   cassandra@cqlsh> SELECT "_id", foo FROM twitter.tweet WHERE es_query='{"query":{"query_string":{"query":"bar2*"}}}' AND "_id"='2';
   
    _id | foo
   -----+------
      2 | bar2
   
   (1 rows)

Cassandra functions and User Defined Functions can be used in the CQL projection clause.

.. code::

   cassandra@cqlsh> SELECT "_id",foo,token("_id"),writetime(foo) FROM twitter.tweet WHERE es_query='{"query":{"query_string":{"query":"bar2*"}}}';
   
    _id | foo   | system.token(_id)    | writetime(foo)
   -----+-------+----------------------+------------------
      2 |  bar2 |  5293579765126103566 | 1509275059354000
     20 | bar20 |  4866192165766252016 | 1509275059572000
     22 | bar22 |  5315788262387249245 | 1509275059591000
     23 | bar23 |  5502885531913083742 | 1509275059600000
     24 | bar24 |  5568379873904613205 | 1509275059614000
     28 | bar28 |  3168262793124788288 | 1509275059663000
     21 | bar21 | -3201810799627846645 | 1509275059580000
     25 | bar25 |  2509205981756244107 | 1509275059625000
     26 | bar26 | -6132418777949225301 | 1509275059633000
     27 | bar27 |  9060526884622895268 | 1509275059645000
   
   (10 rows)

If your target index does not have the same name as the underlying keyspace one, you can specify targeted indices names in ``es_options``.

.. code::

   cassandra@cqlsh> SELECT "_id",foo FROM twitter.tweet WHERE es_query='{"query":{"query_string":{"query":"bar2*"}}}' AND es_options='indices=twitter*';


Elasticsearch aggregations through CQL
......................................

Elassandra supports the elasticsearch aggregation only in **regular CQL statement**. In this case :

* Returned columns are named with aggregations names.
* CQL functions are not supported.
* CQL projection clause, limit and pagination are ignored. It also implies that aggregation results must fit into the available memory.

.. code::

   cassandra@cqlsh> SELECT * FROM twitter2.doc WHERE es_query='{"aggs":{"sales_per_month":{"date_histogram":{"field":"post_date","interval":"day"},"aggs":{"sales":{"sum":{"field":"price"}}}}}}';
   
    sales_per_month.key             | sales_per_month.count | sales_per_month.sales.sum
   ---------------------------------+-----------------------+---------------------------
    2017-10-04 00:00:00.000000+0000 |                     3 |                        30
    2017-10-05 00:00:00.000000+0000 |                     1 |                        10
    2017-10-06 00:00:00.000000+0000 |                     1 |                        10
    2017-10-07 00:00:00.000000+0000 |                     3 |                        30
   
   (4 rows)

When requesting multiple sibling aggregations, the tree result is flattened. 
In the following example, there are two top level aggregations named *sales_per_month* and *sum_monthly_sales*.

.. code::

   cassandra@cqlsh> SELECT * FROM twitter2.doc WHERE es_query='{"size":0,
         "aggs":{"sales_per_month":{"date_histogram":{"field":"post_date","interval":"day"},"aggs":{"sales":{"sum":{"field":"price"}}}},
         "sum_monthly_sales":{"sum_bucket":{"buckets_path":"sales_per_month>sales"}}}}';

    sales_per_month.key             | sales_per_month.count | sales_per_month.sales.sum | sum_monthly_sales.value
   
   ---------------------------------+-----------------------+---------------------------+-------------------------
    2017-10-04 00:00:00.000000+0000 |                     3 |                        30 |                    null
    2017-10-05 00:00:00.000000+0000 |                     1 |                        10 |                    null
    2017-10-06 00:00:00.000000+0000 |                     1 |                        10 |                    null
    2017-10-07 00:00:00.000000+0000 |                     3 |                        30 |                    null
                               null |                  null |                      null |                      80
   
   (5 rows)

Distributed Elasticsearch aggregation with Apach Spark
......................................................

In order to use the Elasticsearch aggregation capabilities from Apache Spark, you must request Elassandra with a projection clause having the same CQL types
as the returned aggregation results. Moreover, do not re-use the same column name more than once, otherwise you will get an **IndexOutOfBoundsException** while Apache Spark parses the result.
In the following example, we used dummy columns count2, dc_power1, dc_power2 and dc_power3 to fit the aggregation results :

.. code::

   import org.apache.spark.{SparkConf, SparkContext}
   import com.datastax.spark.connector._
   import org.apache.spark.sql.cassandra._
   val query = """{
     "query":{
       "bool":{
         "filter": [
           {"term": { "datalogger_name": "mysensor" }},
           {"range" : {
               "ts" : { "gte" : "2017-12-16", "lte" : "2018-01-20"  }
           }}
         ]
       }
     },
     "aggs":{
       "hour_agg":{
         "date_histogram":{"field":"ts","interval":"hour"},
         "aggs": {
           "agg_irradiance": {
             "avg": {
               "field": "irradiance"
             }
           },
           "agg_conso": {
             "avg": {
               "field": "altitude"
             }
           },
          "water1":{
               "terms":{"field":"azimuth"},
               "aggs":{
                 "dc_power_agg":{ "sum":{"field":"dc_power"}}
               }
          }
         }
       }
     }
   }"""
   val t = sc.cassandraTable("iot", "sensors").select("ts","count","dc_power","dc_power1","dc_power2","count2","dc_power3").where("es_query='"+query+"'");
   t.collect.foreach(println)
   
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 305.64675177506786, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 308.4126297573829, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 311.4319809865401, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 314.7328283387269, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 318.34321582364055, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 322.28910238170704, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 326.59122459682067, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 331.2608198139219, count2: 17, dc_power3: 0.0}
   CassandraRow{ts: 2017-12-31 00:00:00+0100, count: 204, dc_power: 0.0, dc_power1: null, dc_power2: 336.2944302705681, count2: 17, dc_power3: 0.0}

Alternatively, you can request an Apache Spark to get the aggregation results as JSON objects by adding the option **json=true** to the query ``es_options`` as follow :

.. code::

   val t = sc.cassandraTable("iot", "sensors").select("es_query").where("es_query='"+query+"' AND es_options='json=true'");
   t.collect.foreach(println)
   
   CassandraRow{es_query: {"key_as_string":"2017-12-30T23:00:00.000Z","key":1514674800000,"doc_count":204,"agg_irradiance":{"value":0.0},"water1":{"doc_count_error_upper_bound":0,"sum_other_doc_count":34,"buckets":[{"key":305.64675177506786,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":308.4126297573829,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":311.4319809865401,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":314.7328283387269,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":318.34321582364055,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":322.28910238170704,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":326.59122459682067,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":331.2608198139219,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":336.2944302705681,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":341.6684918842001,"doc_count":17,"dc_power_agg":{"value":0.0}}]},"agg_conso":{"value":0.0}}}
   CassandraRow{es_query: {"key_as_string":"2017-12-31T00:00:00.000Z","key":1514678400000,"doc_count":204,"agg_irradiance":{"value":0.0},"water1":{"doc_count_error_upper_bound":0,"sum_other_doc_count":34,"buckets":[{"key":5.253033308292965,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":11.17937932261813,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":16.9088341251606,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":22.361824055627704,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":27.483980631203153,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":32.24594386978638,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":36.63970141314307,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":40.673315954868855,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":44.36558478428467,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":47.74149653565296,"doc_count":17,"dc_power_agg":{"value":0.0}}]},"agg_conso":{"value":0.0}}}
   CassandraRow{es_query: {"key_as_string":"2017-12-31T01:00:00.000Z","key":1514682000000,"doc_count":204,"agg_irradiance":{"value":0.0},"water1":{"doc_count_error_upper_bound":0,"sum_other_doc_count":34,"buckets":[{"key":53.65569068831377,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":56.249279017946265,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":58.63483107417463,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":60.835352658997266,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":62.87149505671871,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":64.76161651252164,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":66.52193854036197,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":68.16674119813763,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":69.70857084793244,"doc_count":17,"dc_power_agg":{"value":0.0}},{"key":71.15844512445423,"doc_count":17,"dc_power_agg":{"value":0.0}}]},"agg_conso":{"value":0.0}}}

CQL Driver integration
......................

For better performance, you can use a CQL prepared statement to submit the Elasticsearch queries as shown below in java. 
You can also retrieve the Elasticsearch results summary **hits.total**, **hits.max_score**, **_shards.total**, **_shards.successful**, **_shards.skipped** and **_shards.failed** 
from the result `custom payload <https://docs.datastax.com/en/developer/java-driver/3.2/manual/custom_payloads/>`_.

.. code-block:: java

   public static class IncomingPayload {
        public final long hitTotal;
        public final float hitMaxScore;
        public final int shardTotal;
        public final int shardSuccessful;
        public final int shardSkipped;
        public final int shardFailed;
        public IncomingPayload(Map<String,ByteBuffer> payload) {
            hitTotal = payload.get("hits.total").getLong();
            hitMaxScore = payload.get("hits.max_score").getFloat();
            shardTotal = payload.get("_shards.total").getInt();
            shardSuccessful = payload.get("_shards.successful").getInt();
            shardSkipped = payload.get("_shards.skipped").getInt();
            shardFailed = payload.get("_shards.failed").getInt();
        }
   }
   
   String esQuery = "{\"query\":{\"match_all\":{}}}";
   ResultSet rs = session.execute("SELECT * FROM ks.table WHERE es_query=?", esQuery);
   IncomingPayload payload = new IncomingPayload(rs.getExecutionInfo().getIncomingPayload());
   System.out.println("hits.total="+payload.hitTotal);

.. TIP::

   When sum of **_shards.successful**, **_shards.skipped** and **_shards.failed** is lower than **_shards.total**, it means the search is not consistent because of missing nodes. In such cases, index state is red.

Application UNIT Tests
......................

`Elassandra Unit <https://github.com/strapdata/elassandra-unit>`_ helps you writing isolated JUnit tests in a Test Driven Development style with an embedded Elassandra instance.

.. image:: images/elassandra-unit.png

In order to execute Elasticsearch queries through CQL with Elassandra unit, set the system property ``cassandra.custom_query_handler_class`` to ``org.elassandra.index.ElasticQueryHandler``.
Moreover set the system property ``cassandra.ring_delay_ms`` to 0 to quickly start elassandra.

Maven configuration:

.. code::

   <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M3</version>
        <configuration>
          <systemPropertyVariables>
            <cassandra.custom_query_handler_class>org.elassandra.index.ElasticQueryHandler</cassandra.custom_query_handler_class>
            <cassandra.ring_delay_ms>0</cassandra.ring_delay_ms>
          </systemPropertyVariables>
        </configuration>
      </plugin>
   </plugins>

Elasticsearch Query Builder
...........................

In order to build elasticsearch queries, `query builders and helpers<https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-query-builders.html>`_ from elastic can be used as shown bellow:

.. code::

   String esQuery = new SearchSourceBuilder().query(QueryBuilders.termQuery("user", "vince")).toString(ToXContent.EMPTY_PARAMS);
   ResultSet results = cassandraCQLUnit.session.execute("SELECT * FROM users WHERE es_query = ? ALLOW FILTERING", esQuery);

CQL Tracing
...........

Elasticsearch search request may invlove CQL requests to requested fields from the underlying Cassandra table. When searching through CQL,
you can use `Cassandra tracing <https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cqlshTracing.html>`_ capabilities to troubleshoot the Cassandra performance problems.

.. code::

   cassandra@cqlsh> tracing on;
   Now Tracing is enabled
   cassandra@cqlsh> SELECT * FROM twitter2.doc WHERE es_query='{"query":{"match_all":{}}}';
   
    _id | es_options | es_query | message                                          | post_date                           | price | user
   -----+------------+----------+--------------------------------------------------+-------------------------------------+-------+------------
      2 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-04 14:12:00.000000+0000'] |  [10] | ['Poulpy']
      3 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-04 15:12:00.000000+0000'] |  [10] | ['Poulpy']
      5 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-06 13:12:00.000000+0000'] |  [10] | ['Poulpy']
      8 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-07 18:12:00.000000+0000'] |  [10] | ['Poulpy']
      1 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-04 13:12:00.000000+0000'] |  [10] | ['Poulpy']
      4 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-05 13:12:00.000000+0000'] |  [10] | ['Poulpy']
      6 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-07 13:12:00.000000+0000'] |  [10] | ['Poulpy']
      7 |       null |     null | ['Elassandra adds dynamic mapping to Cassandra'] | ['2017-10-07 15:12:00.000000+0000'] |  [10] | ['Poulpy']
   
   (8 rows)
   
   Tracing session: 817762d0-c6d8-11e7-80c9-cf9ea31c7788
   
    activity                                                                                                           | timestamp                  | source    | source_elapsed | client
   --------------------------------------------------------------------------------------------------------------------+----------------------------+-----------+----------------+-----------
                                                                                                   Elasticsearch query | 2017-11-11 13:04:44.544000 | 127.0.0.1 |              0 | 127.0.0.1
         Parsing SELECT * FROM twitter2.doc WHERE es_query='{"query":{"match_all":{}}}'; [Native-Transport-Requests-1] | 2017-11-11 13:04:44.541000 | 127.0.0.1 |            192 | 127.0.0.1
                                                                     Preparing statement [Native-Transport-Requests-1] | 2017-11-11 13:04:44.541000 | 127.0.0.1 |            382 | 127.0.0.1
                                                               Executing single-partition query on roles [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1048 | 127.0.0.1
                                                                            Acquiring sstable references [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1145 | 127.0.0.1
                               Skipped 0/1 non-slice-intersecting sstables, included 0 due to tombstones [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1327 | 127.0.0.1
                                                                             Key cache hit for sstable 1 [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1475 | 127.0.0.1
                                                               Merged data from memtables and 1 sstables [ReadStage-2] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           1724 | 127.0.0.1
                                                                       Read 1 live and 0 tombstone cells [ReadStage-2] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           1830 | 127.0.0.1
                                                               Executing single-partition query on roles [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2279 | 127.0.0.1
                                                                            Acquiring sstable references [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2360 | 127.0.0.1
                               Skipped 0/1 non-slice-intersecting sstables, included 0 due to tombstones [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2432 | 127.0.0.1
                                                                             Key cache hit for sstable 1 [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2509 | 127.0.0.1
                                                               Merged data from memtables and 1 sstables [ReadStage-4] | 2017-11-11 13:04:44.544000 | 127.0.0.1 |           2736 | 127.0.0.1
                                                                       Read 1 live and 0 tombstone cells [ReadStage-4] | 2017-11-11 13:04:44.544000 | 127.0.0.1 |           2801 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.552000 | 127.0.0.1 |            143 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.552000 | 127.0.0.1 |            311 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.552000 | 127.0.0.1 |            438 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |            553 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |            624 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |            953 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |           1031 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |           1280 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |           1335 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553001 | 127.0.0.1 |           1423 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1515 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1593 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1853 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1921 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           2091 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           2136 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554001 | 127.0.0.1 |           2253 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554001 | 127.0.0.1 |           2346 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554001 | 127.0.0.1 |           2408 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           2654 | 127.0.0.1
                                      Executing single-partition query on doc [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.555000 | 127.0.0.2 |            116 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           2733 | 127.0.0.1
                                                 Acquiring sstable references [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.555000 | 127.0.0.2 |            303 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           2950 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           3002 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           3095 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           3191 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555001 | 127.0.0.1 |           3253 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.556000 | 127.0.0.1 |           3549 | 127.0.0.1
                                                  Key cache hit for sstable 5 [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |            480 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.556000 | 127.0.0.1 |           3656 | 127.0.0.1
                                                  Key cache hit for sstable 6 [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |            650 | 127.0.0.1
    Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |            747 | 127.0.0.1
                                    Merged data from memtables and 2 sstables [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |           1245 | 127.0.0.1
                                            Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |           1362 | 127.0.0.1
                                                                                                      Request complete | 2017-11-11 13:04:44.563745 | 127.0.0.1 |          19745 | 127.0.0.1

You can then retreive tracing information stored into the system_traces keyspace for 24 hours as demonstrated below.

.. code::

   cassandra@cqlsh> select * from system_traces.sessions;

    session_id                           | client    | command | coordinator | duration | parameters                                                                                                                                                                   | request             | started_at
   --------------------------------------+-----------+---------+-------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+---------------------------------
    817762d0-c6d8-11e7-80c9-cf9ea31c7788 | 127.0.0.1 |   QUERY |   127.0.0.1 |    19745 | {'consistency_level': 'ONE', 'page_size': '100', 'query': 'SELECT * FROM twitter2.doc WHERE es_query=''{"query":{"match_all":{}}}'';', 'serial_consistency_level': 'SERIAL'} | Elasticsearch query | 2017-11-11 12:04:44.544000+0000
    7c49dae0-c6d8-11e7-80c9-cf9ea31c7788 | 127.0.0.1 |   QUERY |   127.0.0.1 |    20002 | {'consistency_level': 'ONE', 'page_size': '100', 'query': 'SELECT * FROM twitter2.doc WHERE es_query=''{"query":{"match_all":{}}}'';', 'serial_consistency_level': 'SERIAL'} | Elasticsearch query | 2017-11-11 12:04:35.856000+0000
    6786c2d0-c6d8-11e7-80c9-cf9ea31c7788 | 127.0.0.1 |   QUERY |   127.0.0.1 |    16426 |                                              {'consistency_level': 'ONE', 'page_size': '100', 'query': 'SELECT * FROM twitter2.doc ;', 'serial_consistency_level': 'SERIAL'} |  Execute CQL3 query | 2017-11-11 12:04:01.021000+0000
    6b49e550-c6d8-11e7-80c9-cf9ea31c7788 | 127.0.0.1 |   QUERY |   127.0.0.1 |    14129 |                                               {'consistency_level': 'ONE', 'page_size': '100', 'query': 'SELECT * FROM twitter2.doc;', 'serial_consistency_level': 'SERIAL'} |  Execute CQL3 query | 2017-11-11 12:04:07.333000+0000
   
   (4 rows)
   cassandra@cqlsh> SHOW SESSION 817762d0-c6d8-11e7-80c9-cf9ea31c7788;

   Tracing session: 817762d0-c6d8-11e7-80c9-cf9ea31c7788
   
    activity                                                                                                           | timestamp                  | source    | source_elapsed | client
   --------------------------------------------------------------------------------------------------------------------+----------------------------+-----------+----------------+-----------
                                                                                                   Elasticsearch query | 2017-11-11 13:04:44.544000 | 127.0.0.1 |              0 | 127.0.0.1
         Parsing SELECT * FROM twitter2.doc WHERE es_query='{"query":{"match_all":{}}}'; [Native-Transport-Requests-1] | 2017-11-11 13:04:44.541000 | 127.0.0.1 |            192 | 127.0.0.1
                                                                     Preparing statement [Native-Transport-Requests-1] | 2017-11-11 13:04:44.541000 | 127.0.0.1 |            382 | 127.0.0.1
                                                               Executing single-partition query on roles [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1048 | 127.0.0.1
                                                                            Acquiring sstable references [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1145 | 127.0.0.1
                               Skipped 0/1 non-slice-intersecting sstables, included 0 due to tombstones [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1327 | 127.0.0.1
                                                                             Key cache hit for sstable 1 [ReadStage-2] | 2017-11-11 13:04:44.542000 | 127.0.0.1 |           1475 | 127.0.0.1
                                                               Merged data from memtables and 1 sstables [ReadStage-2] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           1724 | 127.0.0.1
                                                                       Read 1 live and 0 tombstone cells [ReadStage-2] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           1830 | 127.0.0.1
                                                               Executing single-partition query on roles [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2279 | 127.0.0.1
                                                                            Acquiring sstable references [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2360 | 127.0.0.1
                               Skipped 0/1 non-slice-intersecting sstables, included 0 due to tombstones [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2432 | 127.0.0.1
                                                                             Key cache hit for sstable 1 [ReadStage-4] | 2017-11-11 13:04:44.543000 | 127.0.0.1 |           2509 | 127.0.0.1
                                                               Merged data from memtables and 1 sstables [ReadStage-4] | 2017-11-11 13:04:44.544000 | 127.0.0.1 |           2736 | 127.0.0.1
                                                                       Read 1 live and 0 tombstone cells [ReadStage-4] | 2017-11-11 13:04:44.544000 | 127.0.0.1 |           2801 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.552000 | 127.0.0.1 |            143 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.552000 | 127.0.0.1 |            311 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.552000 | 127.0.0.1 |            438 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |            553 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |            624 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |            953 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |           1031 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |           1280 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553000 | 127.0.0.1 |           1335 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.553001 | 127.0.0.1 |           1423 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1515 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1593 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1853 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           1921 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           2091 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554000 | 127.0.0.1 |           2136 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554001 | 127.0.0.1 |           2253 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554001 | 127.0.0.1 |           2346 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.554001 | 127.0.0.1 |           2408 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           2654 | 127.0.0.1
                                      Executing single-partition query on doc [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.555000 | 127.0.0.2 |            116 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           2733 | 127.0.0.1
                                                 Acquiring sstable references [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.555000 | 127.0.0.2 |            303 | 127.0.0.1
                                       Executing single-partition query on doc [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           2950 | 127.0.0.1
                                                  Acquiring sstable references [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           3002 | 127.0.0.1
                                                   Key cache hit for sstable 5 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           3095 | 127.0.0.1
                                                   Key cache hit for sstable 6 [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555000 | 127.0.0.1 |           3191 | 127.0.0.1
     Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.555001 | 127.0.0.1 |           3253 | 127.0.0.1
                                     Merged data from memtables and 2 sstables [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.556000 | 127.0.0.1 |           3549 | 127.0.0.1
                                                  Key cache hit for sstable 5 [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |            480 | 127.0.0.1
                                             Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.1][search][T#2]] | 2017-11-11 13:04:44.556000 | 127.0.0.1 |           3656 | 127.0.0.1
                                                  Key cache hit for sstable 6 [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |            650 | 127.0.0.1
    Skipped 0/2 non-slice-intersecting sstables, included 0 due to tombstones [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |            747 | 127.0.0.1
                                    Merged data from memtables and 2 sstables [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |           1245 | 127.0.0.1
                                            Read 1 live and 0 tombstone cells [elasticsearch[127.0.0.2][search][T#10]] | 2017-11-11 13:04:44.556000 | 127.0.0.2 |           1362 | 127.0.0.1
                                                                                                      Request complete | 2017-11-11 13:04:44.563745 | 127.0.0.1 |          19745 | 127.0.0.1
