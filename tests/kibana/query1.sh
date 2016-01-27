curl -XGET 'http://localhost:9200/logstash_20150519/_search?ignore_unavailable=true' -d '{"size":500,
 "sort":[{"timestamp":{"order":"desc","unmapped_type":"boolean"}}],
 "query":{"query_string":{"query":"response:200","analyze_wildcard":true}},
 "fields":["*","_source"],
 "script_fields":{},
 "fielddata_fields":["relatedContent.articleModified_time","relatedContent.articlePublished_time","timestamp","utc_time"]}'
