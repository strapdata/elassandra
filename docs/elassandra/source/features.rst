Features
========

Elasticsearch supported features
--------------------------------

Document API
____________
+--------------+------------+--------------------------------+
| Feature      | Elassandra | Comments                       |
+==============+============+================================+
| Index API    | supported  | Refresh parmeter has no effect |
+--------------+------------+--------------------------------+
| Reindex API  | changed    | Use nodetool rebuild_index     |
+--------------+------------+--------------------------------+
| Get API      | supported  |                                |
+--------------+------------+--------------------------------+
| Muli-Get API | supported  |                                |
+--------------+------------+--------------------------------+
| Delete API   | supported  |                                |
+--------------+------------+--------------------------------+
| Update API   | supported  |                                |
+--------------+------------+--------------------------------+
| Bulk API     | supported  |                                |
+--------------+------------+--------------------------------+

Elassandra embeds Elasticsearch to run on top of Apache Cassandra in a scalable and resilient masterless architecture. Elasticsearch code is embedded in Cassanda nodes providing advanced search features on Cassandra tables and Cassandra serve as an Elasticsearch data and configuration store.