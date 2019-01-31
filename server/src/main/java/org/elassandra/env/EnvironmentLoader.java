/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elassandra.env;

import org.elasticsearch.cli.Terminal;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.InternalSettingsPreparer;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * Elasticsearch file configuration loader interface.
 */
public interface EnvironmentLoader {

    default Environment loadEnvironment(boolean foreground, String homeDir, String configDir) {
        return InternalSettingsPreparer.prepareEnvironment(
            Settings.builder()
                .put("node.name","node0")
                .put("path.home", homeDir)
                .build(),
            foreground ? Terminal.DEFAULT : null,
            Collections.EMPTY_MAP,
            Paths.get(configDir));
    }

}
