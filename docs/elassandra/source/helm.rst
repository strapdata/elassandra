
Helm Tiller must be initialised on the target kubernetes cluster.

Add our helm repository::

  helm repo add strapdata https://charts.strapdata.com

Then create a cluster with the following command:

.. parsed-literal::
  helm install -n elassandra --set image.tag=|release| strapdata/elassandra

After installation succeeds, you can get a status of chart::

  helm status elassandra


As show below, the Elassandra chart creates 2 clustered service for elasticsearch and cassandra::

  kubectl get all -o wide -n elassandra
  NAME                          READY     STATUS    RESTARTS   AGE
  pod/elassandra-0              1/1       Running   0          51m
  pod/elassandra-1              1/1       Running   0          50m
  pod/elassandra-2              1/1       Running   0          49m

  NAME                               TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)                                                          AGE
  service/elassandra                 ClusterIP   None           <none>        7199/TCP,7000/TCP,7001/TCP,9300/TCP,9042/TCP,9160/TCP,9200/TCP   51m
  service/elassandra-cassandra       ClusterIP   10.0.174.13    <none>        9042/TCP,9160/TCP                                                51m
  service/elassandra-elasticsearch   ClusterIP   10.0.131.15    <none>        9200/TCP                                                         51m

  NAME                          DESIRED   CURRENT   AGE
  statefulset.apps/elassandra   3         3         51m

More information is available on `github <https://github.com/strapdata/helm-charts/tree/master/charts/elassandra>`_.
