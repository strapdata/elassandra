curl -XGET 'http://localhost:9200/logstash_20150519/_search?ignore_unavailable=true' -d '{"size":500,
 "sort":[{"timestamp":{"order":"desc","unmapped_type":"boolean"}}],
 "query":{"filtered":
		{"query":{"query_string":{"query":"response:200","analyze_wildcard":true}},
	        "filter":{"bool":{"must":[{"range":{"timestamp":{"gte":1443653929020,"lte":1443654829020}}}],"must_not":[]}}}
         },
 "highlight":{
	"pre_tags":["@kibana-highlighted-field@"],
	"post_tags":["@/kibana-highlighted-field@"],
	"fields":{"*":{}},
	"fragment_size":2147483647},
 "aggs":{"2":{"date_histogram":{"field":"timestamp","interval":"30s","pre_zone":"+02:00","pre_zone_adjust_large_interval":true,"min_doc_count":0,"extended_bounds":{"min":1443653929019,"max":1443654829020}}}},
 "fields":["*","_source"],
 "script_fields":{},
 "fielddata_fields":["relatedContent.articleModified_time","relatedContent.articlePublished_time","timestamp","utc_time"]}'
