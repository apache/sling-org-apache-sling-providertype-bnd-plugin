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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import aQute.bnd.build.Classpath.ClassVisitor;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Clazz;
import aQute.bnd.osgi.Descriptors.TypeRef;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.lib.json.Decoder;
import aQute.lib.json.JSONCodec;
import aQute.service.reporter.Reporter;

/** Enforces that no classes implement or extend a type marked as provider. Provider types are retrieved from the resource
 * "META-INF/api-info.json" which is expected to be provided in the class path. */
public class ProviderTypeScanner implements AnalyzerPlugin, Plugin {

    private static final String API_INFO_JSON_RESOURCE_PATH = "META-INF/api-info.json";
    private static final String FIELD_PROVIDER_TYPES = "providerTypes";
    private static final String MESSAGE = "Type \"%s\" %s provider type \"%s\". This is not allowed!";
    private static final String ATTRIBUTE_IGNORED_PROVIDER_TYPES = "ignored";
    private static final String ATTRIBUTE_EVALUATE_ANNOTATIONS_IN_CLASSPATH = "evaluateAnnotations";
    
    private static final List<String> PROVIDER_TYPE_ANNOTATION_FQNS = List.of("org.osgi.annotation.versioning.ProviderType", "aQute.bnd.annotation.ProviderType");

    private Map<String, String> parameters = new HashMap<>();

    @Override
    public void setProperties(Map<String, String> map) throws Exception {
        // https://docs.osgi.org/specification/osgi.core/8.0.0/framework.module.html#framework.common.header.syntax
        parameters.clear();
        parameters.putAll(map);
    }

    @Override
    public void setReporter(Reporter processor) {
        // no need to store it as passed in analyzeJar(...) as well
    }

    @Override
    public boolean analyzeJar(Analyzer analyzer) throws Exception {
        List<Resource> apiInfoJsonResources = analyzer.findResources(s -> s.equals(API_INFO_JSON_RESOURCE_PATH))
                .collect(Collectors.toList());
        final Set<String> providerTypes = new HashSet<>(); // This may contain fully qualified class names or package names
        if (apiInfoJsonResources.isEmpty()) {
            if (Boolean.parseBoolean(parameters.get(ATTRIBUTE_EVALUATE_ANNOTATIONS_IN_CLASSPATH))) {
                analyzer.warning("Retrieving provider type info from annotations found in classpath...");
                providerTypes.addAll(collectProviderTypesFromClasspath(analyzer));
            } else {
                analyzer.warning("Could not find resource \"%s\" in the classpath", API_INFO_JSON_RESOURCE_PATH);
            }
        } else {
            for (Resource apiInfoJsonResource : apiInfoJsonResources) {
                try {
                    Set<String> resourceProviderTypes = collectProviderTypesFromApiInfo(analyzer, apiInfoJsonResource);
                    analyzer.trace("Added provider types from resource \"%s\": %s", apiInfoJsonResource,
                            String.join(",", resourceProviderTypes));
                    providerTypes.addAll(resourceProviderTypes);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not parse JSON from resource " + apiInfoJsonResource, e);
                }
            }
        }
        if (providerTypes.isEmpty()) {
            analyzer.warning("No provider types found, skip checking bundle's classes");
        } else {
            // remove ignored provider types
            Arrays.stream(parameters.getOrDefault(ATTRIBUTE_IGNORED_PROVIDER_TYPES, "").split(",")).filter(s -> !s.isBlank())
                    .forEach(ignored -> {
                        if (providerTypes.remove(ignored)) {
                            analyzer.trace("Ignore extensions of provider type \"%s\" due to plugin configuration", ignored);
                        } else {
                            analyzer.warning(
                                    "Ignored class \"%s\" is not defined as provider type at all, you can safely remove the according plugin parameter",
                                    ignored);
                        }
                    });
            checkIfExtendingType(analyzer, analyzer.getClassspace().values(), providerTypes);
        }
        return false;
    }

    private void checkIfExtendingType(Reporter reporter, Collection<Clazz> clazzes, Set<String> providerTypes) {
        for (Clazz clazz : clazzes) {
            if (clazz.getSuper() != null && (isProvider(clazz.getSuper(), providerTypes))) {
                reporter.error(MESSAGE, clazz.getFQN(), "extends", clazz.getSuper().getFQN()).file(clazz.getSourceFile());
            }
            for (TypeRef interfaceType : clazz.interfaces()) {
                if (isProvider(interfaceType, providerTypes)) {
                    reporter.error(MESSAGE, clazz.getFQN(), "implements", interfaceType.getFQN()).file(clazz.getSourceFile());
                }
            }
        }
    }

    static boolean isProvider(TypeRef type, Set<String> providerTypes) {
        return providerTypes.stream().anyMatch(providerType -> providerType.equals(type.getFQN()) || providerType.equals(type.getPackageRef().getFQN()));
    }

    @SuppressWarnings("unchecked")
    private Set<String> collectProviderTypesFromApiInfo(Reporter reporter, Resource apiInfoResource) throws Exception {
        JSONCodec codec = new JSONCodec();
        // read JSON file
        try (InputStream input = apiInfoResource.openInputStream();
                Decoder decoder = codec.dec().from(input)) {
            Map<?, ?> jsonMap = decoder.get(Map.class);
            Object providerTypes = jsonMap.get(FIELD_PROVIDER_TYPES);
            if (providerTypes == null) {
                reporter.error("Resource \"%s\" does not contain a field named \"%s\"", API_INFO_JSON_RESOURCE_PATH, FIELD_PROVIDER_TYPES);
            } else if (!(providerTypes instanceof Collection)) {
                reporter.error("Field \"%s\" in JSON resource \"%s\" is not containing a string array but a type converted to %s",
                        FIELD_PROVIDER_TYPES, API_INFO_JSON_RESOURCE_PATH, providerTypes.getClass().getName());
            } else {
                return new HashSet<>((Collection<String>) providerTypes);
            }
        }
        return Collections.emptySet();
    }

    private Set<String> collectProviderTypesFromClasspath(Analyzer analyzer) throws Exception {
        // unfortunately bnd does not cache this information in an accessible way
        ProviderTypeCollectorClassVisitor visitor = new ProviderTypeCollectorClassVisitor();
        visit(visitor, analyzer);
        return visitor.getProviderTypes();
    }

    /** Visit each class on the class path (includes package-info.class).
     *
     * @param visitor the visitor */
    private void visit(ClassVisitor visitor, Analyzer analyzer) throws Exception {
        for (Jar classpathJar : analyzer.getClasspath()) {
            for (String path : classpathJar.getResources().keySet()) {
                if (path.endsWith(".class")) {
                    Resource r = classpathJar.getResource(path);
                    Clazz c = new Clazz(analyzer, path, r);
                    c.parseClassFile();
                    visitor.visit(c);
                }
            }
        }
    }

    private static final class ProviderTypeCollectorClassVisitor implements ClassVisitor {

        final Set<String> providerTypes = new HashSet<>();

        @Override
        public boolean visit(Clazz clazz) throws Exception {
            if (clazz.annotations().stream().anyMatch(at -> PROVIDER_TYPE_ANNOTATION_FQNS.contains(at.getFQN()))) {
                providerTypes.add(clazz.getFQN());
            }
            return true;
        }

        public Set<String> getProviderTypes() {
            return providerTypes;
        }
    }
}