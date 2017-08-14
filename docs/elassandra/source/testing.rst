Testing
=======

Elasticsearch comes with a testing framework based on `JUNIT <http://junit.org/junit4/>`_ and `RandomizedRunner <http://labs.carrotsearch.com/randomizedtesting.html>`_ provided by the randomized-testing project.
Most of these tests work with Elassandra to ensure compatibility between Elasticsearch and Elassandra.

Testing environnement
---------------------

By default, JUnit creates one instance of each test class and executes each *@Test* method in parallel in many threads. Because Cassandra use many static variables,
concurrent testing is not possible, so each test is executed sequentially (using a semaphore to serialize tests) on a single node Elassandra cluster listening on localhost, see ![ESSingleNodeTestCase](https://github.com/strapdata/elassandra/blob/master/core/src/test/java/org/elasticsearch/test/ESSingleNodeTestCase.java) .
Test configuration is located in **src/test/resources/conf**, data and logs are generated in **target/tests/**.

Between each test, all indices (and underlying keyspaces and tables) are removed to have idempotent testings and avoid conflicts on index names.
System settings ``es.synchronous_refresh``  and ``es.drop_on_delete_index`` are set to *true* in the parent *pom.xml*.

Finally, the testing framework randomizes the locale settings representing a specific geographical, political, or cultural region, but Apache Cassandra does not
support such setting because string manipulation are implemented with the default locale settings (see CASSANDRA-12334).
For exemple, *String.format("SELECT %s FROM ...",...)* is computed as *String.format(Local.getDefault(),"SELECT %s FROM ...",...)*, involving errors for some Locale setting.
As a workaround, a javassit byte-code manipulation in the Ant build step adds a *Locale.ROOT* argument to weak method calls in all Cassandra classes.

Elassandra unit test
--------------------

Elassandra unit test allows to use both the Elasticsearch API and CQL requests as shown in the following sample.

.. code::
   
   public class ParentChildTests extends ESSingleNodeTestCase {
   
       @Test
       public void testCQLParentChildTest() throws Exception {
           process(ConsistencyLevel.ONE,"CREATE KEYSPACE IF NOT EXISTS company3 WITH replication={ 'class':'NetworkTopologyStrategy', 'DC1':'1' }");
           process(ConsistencyLevel.ONE,"CREATE TABLE company3.employee (branch text,\"_id\" text, name text, dob timestamp, hobby text, primary key ((branch),\"_id\"))");
           
           assertAcked(client().admin().indices().prepareCreate("company3")
                   .addMapping("branch", "{ \"branch\": {}}")
                   .addMapping("employee", "{ \"employee\" : { \"discover\" : \".*\", \"_parent\" : { \"type\": \"branch\", \"cql_parent_pk\":\"branch\" } }}")
                   .get());
           ensureGreen("company3");
           
           assertThat(client().prepareIndex("company3", "branch", "london")
                   .setSource("{ \"district\": \"London Westminster\", \"city\": \"London\", \"country\": \"UK\" }")
                   .get().isCreated(), equalTo(true));
           assertThat(client().prepareIndex("company3", "branch", "liverpool")
                   .setSource("{ \"district\": \"Liverpool Central\", \"city\": \"Liverpool\", \"country\": \"UK\" }")
                   .get().isCreated(), equalTo(true));
           assertThat(client().prepareIndex("company3", "branch", "paris")
                   .setSource("{ \"district\": \"Champs Élysées\", \"city\": \"Paris\", \"country\": \"France\" }")
                   .get().isCreated(), equalTo(true));
        
           process(ConsistencyLevel.ONE,"INSERT INTO company3.employee (branch,\"_id\",name,dob,hobby) VALUES ('london','1','Alice Smith','1970-10-24','hiking')");
           process(ConsistencyLevel.ONE,"INSERT INTO company3.employee (branch,\"_id\",name,dob,hobby) VALUES ('london','2','Bob Robert','1970-10-24','hiking')");
           
           assertThat(client().prepareSearch().setIndices("company3").setTypes("branch")
                   .setQuery(QueryBuilders.hasChildQuery("employee", QueryBuilders.rangeQuery("dob").gte("1970-01-01"))).get().getHits().getTotalHits(), equalTo(1L));
           assertThat(client().prepareSearch().setIndices("company3").setTypes("employee")
                   .setQuery(QueryBuilders.hasParentQuery("branch", QueryBuilders.matchQuery("country","UK"))).get().getHits().getTotalHits(), equalTo(2L));
       }
   }

To run this specific test :

.. code::

   $mvn test -Pdev -pl com.strapdata.elasticsearch:elasticsearch -Dtests.seed=56E318ABFCECC61 -Dtests.class=org.elassandra.ParentChildTests 
      -Des.logger.level=DEEBUG -Dtests.assertion.disabled=false -Dtests.security.manager=false -Dtests.heap.size=1024m -Dtests.locale=de-GR -Dtests.timezone=Etc/UTC

To run all unit tests :

.. code::

   $mvn test

