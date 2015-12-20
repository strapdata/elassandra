
cqlsh <<EOF
CREATE KEYSPACE IF NOT EXISTS twitter WITH replication={ 'class':'NetworkTopologyStrategy', 'DC1':'1' };
CREATE TABLE twitter.user ( 
name text,
attrs map<text,text>,
primary key (name)
);
insert into twitter.user (name,attrs) VALUES ('alice',{'email':'alice@gmail.com','firstname':'alice'});
insert into twitter.user (name,attrs) VALUES ('bob',{'email':'bob@gmail.com','firstname':'bob'});
EOF

curl -XPUT "http://$NODE:9200/twitter/" -d '{ "settings" : { "number_of_shards" : 1, "number_of_replicas" : 0 } }'
curl -XPUT "http://$NODE:9200/twitter/_mapping/user" -d '
{ "user" : {
        "columns_regexp" : ".*"
    }
}'

curl -XGET "http://$NODE:9200/twitter/user/alice?pretty=true" 
curl -XGET "http://$NODE:9200/twitter/user/bob?pretty=true&fields=name,attrs.email&_source=false"

sleep 1

curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{ "query":{"match_all":{ }}}'
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{ "query":{ "match": {"name":"bob" } }}'

        
cqlsh <<EOF
UPDATE twitter.user SET attrs = attrs + { 'city':'paris' } WHERE name = 'bob';
EOF

sleep 1
curl -XGET "http://$NODE:9200/twitter/_search?pretty=true" -d '{
"query":{
    "bool": {
      "must": [
        { "nested":{ 
            "path":"attrs",
            "query":{ "match": {"attrs.city":"paris" } },
            "inner_hits" : {}
             }
        }
       ]
     }
   }
}'

