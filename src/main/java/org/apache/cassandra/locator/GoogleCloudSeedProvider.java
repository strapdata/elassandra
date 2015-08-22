/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.locator;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.SeedProviderDef;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.io.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

/**
 * Provide seed hostnames from Google Cloud project metadata (property seed-<region_name>)
 * @author vroyer
 *
 */
public class GoogleCloudSeedProvider extends GoogleCloudSnitch implements SeedProvider
{
    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudSeedProvider.class);

    private static String SEED_BY_REGION = "http://metadata.google.internal/computeMetadata/v1/project/attributes/seed-%s";
 
    		
    public GoogleCloudSeedProvider(Map<String, String> args) throws IOException, ConfigurationException {
    	super();
    }

    public List<InetAddress> getSeeds() 
    {
    	try {
			String response = gceApiCall( String.format(SEED_BY_REGION, gceRegion) );
			logger.debug("seed-"+gceRegion+": "+response);
			String hosts[] = response.split(",", -1);
			List<InetAddress> seeds = new ArrayList<InetAddress>(hosts.length);
			for (String host : hosts)
			{
			    try
			    {
			        seeds.add(InetAddress.getByName(host.trim()));
			    }
			    catch (UnknownHostException ex)
			    {
			        // not fatal... DD will bark if there end up being zero seeds.
			        logger.warn("Seed provider couldn't lookup host {}", host);
			    }
			}
			return Collections.unmodifiableList(seeds);
		} catch (ConfigurationException |  IOException e) {
			logger.error("Failed to get cluster seed",e);
			return Collections.EMPTY_LIST;
		} 
    }
}
