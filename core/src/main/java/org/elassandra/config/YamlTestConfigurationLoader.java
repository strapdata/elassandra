/*
 * Copyright (c) 2017 Strapdata (http://www.strapdata.com)
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
package org.elassandra.config;

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.ParameterizedClass;
import org.apache.cassandra.config.YamlConfigurationLoader;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.elasticsearch.common.SuppressForbidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.MissingProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import com.google.common.io.ByteStreams;

/**
 * Elassandra configurator for maven/junit tests.
 * @author vroyer
 *
 */
public class YamlTestConfigurationLoader extends YamlConfigurationLoader
{
    private static final Logger logger = LoggerFactory.getLogger(YamlTestConfigurationLoader.class);

    @SuppressForbidden(reason="unchecked")
    public Config loadConfig(URL url) throws ConfigurationException
    {
        try
        {
            logger.debug("Loading settings from {}", url);
            byte[] configBytes;
            try (InputStream is = url.openStream())
            {
                configBytes = ByteStreams.toByteArray(is);
            }
            catch (IOException e)
            {
                // getStorageConfigURL should have ruled this out
                throw new AssertionError(e);
            }

            org.yaml.snakeyaml.constructor.Constructor constructor = new org.yaml.snakeyaml.constructor.Constructor(Config.class);
            TypeDescription seedDesc = new TypeDescription(ParameterizedClass.class);
            seedDesc.putMapPropertyType("parameters", String.class, String.class);
            constructor.addTypeDescription(seedDesc);
            MissingPropertiesChecker propertiesChecker = new MissingPropertiesChecker();
            constructor.setPropertyUtils(propertiesChecker);
            Yaml yaml = new Yaml(constructor);
            Config result = yaml.loadAs(new ByteArrayInputStream(configBytes), Config.class);
            //result.configHintedHandoff();
            
            SimpleDateFormat sdf =  new SimpleDateFormat("yyyyMMdd-hhmmss", Locale.ROOT);
            String datadir = System.getProperty("cassandra.storagedir", ".") + File.separator + sdf.format(new Date()) + "_" + Integer.getInteger("cassandra.node_ordinal",0);
            result.commitlog_directory = datadir + File.separator + "commitlog";
            result.saved_caches_directory = datadir + File.separator + "saved_caches";
            result.data_file_directories = new String[] { datadir + File.separator + "data" };
            
            // replace the last number of listen+rpc addresses by the value of cassandra.node_ordinal.
            int ordinal = Integer.getInteger("cassandra.node_ordinal", 0); // env var
            result.listen_address = result.listen_address.substring(0, result.listen_address.lastIndexOf('.')) + (ordinal+1);
            if (result.rpc_address != null)
                result.rpc_address = result.rpc_address.substring(0, result.rpc_address.lastIndexOf('.')) + (ordinal+1);

                      
            propertiesChecker.check();
            return result;
        }
        catch (YAMLException e)
        {
            throw new ConfigurationException("Invalid yaml: " + url, e);
        }
    }

    private static class MissingPropertiesChecker extends PropertyUtils
    {
        private final Set<String> missingProperties = new HashSet<>();

        public MissingPropertiesChecker()
        {
            setSkipMissingProperties(true);
        }

        @Override
        public Property getProperty(Class<? extends Object> type, String name) throws IntrospectionException
        {
            Property result = super.getProperty(type, name);
            if (result instanceof MissingProperty)
            {
                missingProperties.add(result.getName());
            }
            return result;
        }

        public void check() throws ConfigurationException
        {
            if (!missingProperties.isEmpty())
            {
                throw new ConfigurationException("Invalid yaml. Please remove properties " + missingProperties + " from your cassandra.yaml");
            }
        }
    }
}
