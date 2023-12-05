/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.providertype.bndplugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Jar;
import aQute.bnd.service.Plugin;

public class BndBuilderExtension implements BeforeEachCallback, AfterEachCallback {
    
    private Builder builder;
    private final Object plugin;

    public BndBuilderExtension(Object plugin) {
        this.plugin = plugin;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        builder = new Builder();
        Jar classesDirJar = new Jar("test.jar", new File("target/test-classes"));
        classesDirJar.setManifest(new Manifest());
        builder.setJar(classesDirJar); // jar closed with builder
        builder.setSourcepath(new File[] { new File("src/test/java") } );
        if (plugin instanceof Plugin) {
            Plugin pluginPlugin = (Plugin)plugin;
            pluginPlugin.setReporter(builder);
            pluginPlugin.setProperties(Collections.emptyMap());
        }
        builder.addBasicPlugin(plugin);
    }

    protected Builder getBuilder() {
        return builder;
    }

    protected void setPluginProperties(Map<String, String> pluginProperties) throws Exception{
        if (plugin instanceof Plugin) {
            Plugin pluginPlugin = (Plugin)plugin;
            pluginPlugin.setProperties(pluginProperties);
        } else {
            throw new IllegalStateException("Given plugin does not implement class Plugin but is " + plugin);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws IOException {
        builder.close();
    }
}
