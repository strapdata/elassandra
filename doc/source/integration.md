# Integration

## Kibana + Elassandra

[Kibana](https://www.elastic.co/guide/en/kibana/4.3/introduction.html) version 4.3 can run on Elassandra, providing a visualization tool for cassandra and elasticsearch data. Here is a demo video.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=yKT96wtjJNg
" target="_blank"><img src="http://img.youtube.com/vi/yKT96wtjJNg/0.jpg" 
alt="Elassandra demo" width="240" height="180" border="10" /></a>

Because cassandra keyspace, type and table can only contain alphanumeric and underscore characters (see [cassandra documentation](http://docs.datastax.com/en/cql/3.1/cql/cql_reference/ref-lexical-valid-chars.html)), the same restriction applies to index and type names. 

* Replace the index name **.kibana** by **kibana** in *config/kibana.yaml*.
* Replace **'index-pattern'** by **'index_pattern'** in the source code with the following sed command:
```
q=\'
# for Kibana 4.1.x
sed -i .bak -e "s/type: ${q}index-pattern${q}/type: ${q}index_pattern${q}/g" -e "s/type = ${q}index-pattern${q}/type = ${q}index_pattern${q}/g" index.js
# for Kibana 4.3.x (for Elassandra v2.1.1+)
sed -i .bak -e "s/type: ${q}index-pattern${q}/type: ${q}index_pattern${q}/g" -e "s/type = ${q}index-pattern${q}/type = ${q}index_pattern${q}/g" -e "s%${q}index-pattern${q}: ${q}/settings/objects/savedSearches/${q}%${q}index_pattern${q}: ${q}/settings/objects/savedSearches/${q}%g" optimize/bundles/kibana.bundle.js src/ui/public/index_patterns/*.js
```
* If you want to load sample data from [Kibana Getting started](https://www.elastic.co/guide/en/kibana/current/getting-started.html), apply the following changes to logstash.jsonl with a sed command. 

```
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
```

## JDBC Driver sql4es + Elassandra

 