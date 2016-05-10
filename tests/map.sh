curl -XPUT "http://$NODE:9200/twitter/" -d '{ "index.secondary_index_class" : "org.elasticsearch.cassandra.index.ElasticSecondaryIndex" }'

curl -XPUT "http://$NODE:9200/twitter/user/_mapping" -d '{
   "user" : {
        "dynamic_templates": [
            {
                  "attrs": {
                      "match": "attrs",
                      "mapping": {
                          "type":"nested",
                          "include_in_parent" : true
                      }
                  }
             },
             {
                  "nested_attrs": {
                      "path_match": "attrs.*",
                      "mapping": {
                          "type":"string",
                          "index": "not_analyzed"
                      }
                  }
             }
        ]
   }
}'

cqlsh <<EOF
CREATE TABLE twitter.user ( 
name text,
attrs map<text,text>,
primary key (name)
);
insert into twitter.user (name,attrs) VALUES ('alice',{'email':'alice@gmail.com','firstname':'alice'});
insert into twitter.user (name,attrs) VALUES ('bob',{'email':'bob@gmail.com','firstname':'bob'});
EOF

curl -XPUT "http://$NODE:9200/twitter/_mapping/user" -d '
{ "user" : {
        "columns_regexp" : ".*",
        "properties"  : {
             "attrs": { 
                "type":"nested",
                "include_in_parent" : true
             }
        }
    }
}'

curl -XPUT "http://$NODE:9200/toto/" -d '
{
  "mappings": {
    "my_type": {
      "properties": {
        "foo": {
          "type": "nested",
          "include_in_root": true,
          "properties": {
            "bar": {
              "type": "string"
            }
          }
        }
      }
    }
  }
}'

curl -XPUT "http://$NODE:9200/toto/my_type/alain?consistency=one" -d '{
    "foo" : { "bar":"running" }
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

curl -XPUT "http://$NODE:9200/twitter/user/alain?consistency=one" -d '{
    "name":"alain",
    "attrs" : { "cinema":"running" }
}'
