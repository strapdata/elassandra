curl -XPUT "http://$NODE:9200/twitter/" -d '{ "settings" : { "number_of_replicas" : 0 } }'

cqlsh <<EOF
CREATE TABLE twitter.server ( 
name text,
ip inet,
netmask int,
prod boolean,
primary key (name)
);
insert into twitter.server (name,ip,netmask,prod) VALUES ('localhost','127.0.0.1',8,true);
insert into twitter.server (name,ip,netmask,prod) VALUES ('my-server','123.45.67.78',24,true);
EOF


curl -XPUT "http://$NODE:9200/twitter/_mapping/server" -d '
{ "server" : {
        "columns_regexp" : ".*",
         "properties" : {
                "name" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                }
            }
       }
    }
}'


curl -XGET "http://$NODE:9200/twitter/server/my-server?pretty=true"
curl -XGET "http://$NODE:9200/twitter/server/localhost?pretty=true"

<<<<<<< HEAD

=======
>>>>>>> bbfeba86cc382b1c74d8451d1ef58a716fdfb5a5
curl -XGET "http://$NODE:9200/twitter/server/_search?pretty=true" -d '{ "query":{"match_all":{ }}}'

curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{ "query":{ "match": {"name":"my-server" } }}'
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{ "query":{ "term": {"name":"localhost" } }}'

<<<<<<< HEAD
curl -XPUT "http://$NODE:9200/twitter/server/bigserver" -d '{
    "ip": "22.22.22.22",
    "netmask":32,
    "prod" : true,
    "description234":"my big server"
}'
=======
>>>>>>> bbfeba86cc382b1c74d8451d1ef58a716fdfb5a5
curl -XPUT "http://$NODE:9200/twitter/server/bigserver234" -d '{
    "ip": "22.22.22.22",
    "netmask":32,
    "prod" : true,
    "description234":"my big server"
}'

curl -XGET "http://$NODE:9200/twitter/server/_search?pretty=true" -d '{ "query":{"match_all":{ }}}'

<<<<<<< HEAD
curl -XGET "http://$NODE:9200/twitter/server/bigserver234?pretty=true&fields=description234,netmask"



=======
>>>>>>> bbfeba86cc382b1c74d8451d1ef58a716fdfb5a5
curl -XDELETE "http://$NODE:9200/twitter"

curl -XPUT "http://$NODE:9200/twitter" -d '
{ "mappings": {
    "server" : {
        "columns_regexp" : ".*",
         "properties" : {
                "name" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                }
            }
       }
    }
<<<<<<< HEAD
}'
=======
}'
>>>>>>> bbfeba86cc382b1c74d8451d1ef58a716fdfb5a5
