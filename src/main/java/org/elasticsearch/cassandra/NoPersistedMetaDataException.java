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

import java.util.UUID;

import org.elasticsearch.ElasticsearchException;

public class NoPersistedMetaDataException extends ElasticsearchException {

	
    public NoPersistedMetaDataException() {
        super("No MetaData as comment");
    }

    public NoPersistedMetaDataException(Exception e) {
        super("Failed to read MetaData as comment",e);
    }

} 
