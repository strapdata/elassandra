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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.apache.cassandra.db.IndexExpression;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.filter.ExtendedFilter;
import org.apache.cassandra.db.index.SecondaryIndexManager;
import org.apache.cassandra.db.index.SecondaryIndexSearcher;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class ElasticSecondaryIndexSearcher extends SecondaryIndexSearcher {

	private static final Logger logger = LoggerFactory.getLogger(ElasticSecondaryIndexSearcher.class);

	public ElasticSecondaryIndexSearcher(SecondaryIndexManager indexManager, Set<ByteBuffer> columns) {
		super(indexManager, columns);
		logger.debug("new ElasticSecondaryIndexSearcher columns = {}",columns);
		// TODO Auto-generated constructor stub
	}

	/**
     * Validates the specified {@link IndexExpression}. It will throw an {@link org.apache.cassandra.exceptions.InvalidRequestException}
     * if the provided clause is not valid for the index implementation.
     *
     * @param indexExpression An {@link IndexExpression} to be validated
     * @throws org.apache.cassandra.exceptions.InvalidRequestException in case of validation errors
     */
	@Override
    public void validate(IndexExpression indexExpression) throws InvalidRequestException
    {
    	logger.debug("indexExpression = {}",indexExpression);
    	throw new InvalidRequestException("Search through Elastic secondary index is not implemented. Please use the elasticsearch API.");
    }
    
	@Override
	public List<Row> search(ExtendedFilter paramExtendedFilter) {
		logger.debug("search paramExtendedFilter = {}",paramExtendedFilter);
		return null;
	}
	

}
