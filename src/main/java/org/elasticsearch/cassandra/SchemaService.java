/*
 * Copyright (c) 2015 Vincent Royer.
 * Contains some code from Elasticsearch (http://www.elastic.co)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.cassandra;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.cql3.UntypedResultSet;
import org.apache.cassandra.cql3.UntypedResultSet.Row;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.CassandraClusterState;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.index.fieldvisitor.FieldsVisitor;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.indices.IndicesService;

import com.google.common.collect.ImmutableList;

public interface SchemaService {

	public  Map<String,GetField> flattenGetField(final String[] fieldFilter, final String path, final Map<String,Object> map, Map<String,GetField> fields);
	public  Map<String,List<Object>> flattenObject(final Set<String> neededFiedls, final String path, final Map<String,Object> map, Map<String,List<Object>> fields);

	
	/**
	 * @param index
	 * @param replicationFactor
	 * @throws IOException
	 */
	public void createIndexKeyspace(String index, int replicationFactor)
			throws IOException;

	public String updateUDT(String ksName, String cfName, String name,
			ObjectMapper objectMapper) throws RequestExecutionException;

	/**
	 * @param index
	 * @param type
	 * @param typesMap
	 * @throws IOException
	 */
	public void updateTableSchema(String index, String type,
			Set<String> columns, DocumentMapper docMapper) throws IOException;

	public List<ColumnDefinition> getPrimaryKeyColumns(String ksName,
			String cfName) throws ConfigurationException;

	public List<String> getPrimaryKeyColumnsName(String ksName,
			String cfName) throws ConfigurationException;

	public List<String> cassandraMappedColumns(String ksName,
			String cfName);

	public String[] cassandraColumns(MapperService mapperService,
			String type);

	/**
	 * Fetch the row from the matching keyspace.table
	 * @param index
	 * @param type
	 * @param requiredColumns
	 * @param id
	 * @return
	 * @throws InvalidRequestException
	 * @throws RequestExecutionException
	 * @throws RequestValidationException
	 * @throws IOException
	 */
	public UntypedResultSet fetchRow(String index, String type,
			String id, List<String> requiredColumns)
			throws InvalidRequestException, RequestExecutionException,
			RequestValidationException, IOException;

	public  UntypedResultSet fetchRow(String index, String type,
			String id, List<String> requiredColumns, ConsistencyLevel cl)
			throws InvalidRequestException, RequestExecutionException,
			RequestValidationException, IOException;

	public Map<String, Object> rowAsMap(UntypedResultSet.Row row,
			FieldsVisitor fieldVisitor, MapperService mapperService,
			String[] types);

	public void deleteRow(String index, String type, String id,
			ConsistencyLevel cl) throws InvalidRequestException,
			RequestExecutionException, RequestValidationException, IOException;

	public String insertDocument(IndicesService indicesService,
			IndexRequest request, CassandraClusterState clusterState,
			Long writetime, Boolean applied) throws Exception;

	public String insertRow(String index, String type,
			String[] columns, Object[] values, String id, boolean ifNotExists,
			long ttl, ConsistencyLevel cl, Long writetime, Boolean applied)
			throws Exception;

	public void index(String[] indices,
			Collection<Range<Token>> tokenRanges);

	public void indexColumnFamilly(String ksName, String cfName,
			String index, String type, Collection<Range<Token>> tokenRanges);

	public void index(String index, String type, String id,
			Object[] sourceData);

	public Token getToken(ByteBuffer rowKey, ColumnFamily cf);

	public void createElasticAdminKeyspace();

	public void writeMetaDataAsComment(MetaData metadata) throws ConfigurationException, IOException;

	public void initializeMetaDataAsComment();
	
	public MetaData readMetaDataAsComment() throws NoPersistedMetaDataException;
	public MetaData readMetaDataAsRow() throws NoPersistedMetaDataException;
	
	public void persistMetaData(MetaData currentMetadData, MetaData newMetaData, String source) throws ConfigurationException, IOException, InvalidRequestException, RequestExecutionException, RequestValidationException;

}

