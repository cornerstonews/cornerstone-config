/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cornerstonews.configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cornerstonews.configuration.parser.BaseConfigParser;
import com.github.cornerstonews.configuration.parser.ConfigFileParser;
import com.github.cornerstonews.configuration.parser.HashMapConfigParser;
import com.github.cornerstonews.configuration.parser.JsonConfigParser;
import com.github.cornerstonews.configuration.parser.YamlConfigParser;

public final class ConfigFactory {

    private static Logger log = LogManager.getLogger(ConfigFactory.class);

    private ConfigFactory() {
    }

    public final static <T> BaseConfigParser<T> getDefaultParser(Class<T> clazz) throws ConfigException {
        return new BaseConfigParser<T>(clazz, new ObjectMapper());
    }

    public final static <T> ConfigFileParser<T> getParser(String path, Class<T> clazz) {

        ConfigFileParser<T> configurationParser = new YamlConfigParser<>(path, clazz);

        if (path != null && "json".equalsIgnoreCase(ConfigFileParser.getFileExtension(path)) && ConfigFileParser.identifyFileType(path).contains("json")) {
            configurationParser = new JsonConfigParser<>(path, clazz);
        }

        return configurationParser;
    }

    public final static String getConfigPath(String propertyKey) throws ConfigException {
        String path = null;
        Path filePath = null;

        // Read property key from command line
        if (propertyKey != null) {
            path = System.getProperty(propertyKey);
            if (path != null) {
                filePath = Paths.get(path);
            }
        }

        if (filePath != null && Files.isReadable(filePath) && Files.isRegularFile(filePath)) {
            return path;
        }

        return getConfigPath();
    }

    public final static String getConfigPath() throws ConfigException {
        String path = null;

        // Load config file from classpath
        URL pathUrl = Thread.currentThread().getContextClassLoader().getResource("application.yaml");
        if (pathUrl == null) {
            pathUrl = Thread.currentThread().getContextClassLoader().getResource("application.yml");
        }

        if (pathUrl == null) {
            pathUrl = Thread.currentThread().getContextClassLoader().getResource("application.json");
        }

        if (pathUrl != null && !pathUrl.getFile().isEmpty()) {
            path = pathUrl.getFile();
        }

        // override config path if 'APPCONFIG' environment variable is set
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");
            String envPath = (String) env.lookup("APPCONFIG");
            if (envPath != null && !envPath.isEmpty()) {
                path = envPath;
            }
        } catch (NameNotFoundException | NoInitialContextException e) {
            log.info("'APPCONFIG' property not found.");
            // Ignore error if 'APPCONFIG' variable is not found.
        } catch (NamingException ex) {
            ArrayList<String> errors = new ArrayList<>();
            errors.add(ex.getMessage());
            throw new ConfigException("Environment variable 'APPCONFIG'", errors, ex);
        }

        return path;
    }

    public final static <T> T loadConfig(Class<T> clazz) throws ConfigException, IOException {
        return getDefaultParser(clazz).build();
    }

    public final static <T> T loadConfig(String path, Class<T> clazz) throws ConfigException, IOException {
        if (path == null) {
            return loadConfig(clazz);
        }

        return getParser(path, clazz).build(path);
    }

    public final static <T> T loadConfig(Map<String, ?> map, Class<T> clazz) throws ConfigException, IOException {
        return loadConfig(map, clazz, false);
    }

    public final static <T> T loadConfig(Map<String, ?> map, Class<T> clazz, Boolean failOnUnknown) throws ConfigException, IOException {
        if (map.isEmpty()) {
            return loadConfig(clazz);
        }
        
        return new HashMapConfigParser<T>(clazz, failOnUnknown).build(map);
    }

    public final static <T> T loadConfig(Map<String, ?> map, Class<T> clazz, T instance) throws ConfigException {
        return loadConfig(map, clazz, instance, false);
    }

    public final static <T> T loadConfig(Map<String, ?> map, Class<T> clazz, T instance, Boolean failOnUnknown) throws ConfigException {
        if (map.isEmpty()) {
            return instance;
        }
        
        return new HashMapConfigParser<T>(clazz, failOnUnknown).build(map, instance);
    }

    public final static <T> T mergeConfig(Map<String, ?> map, Class<T> clazz, T instance) throws ConfigException {
        return mergeConfig(map, clazz, instance, false);
    }

    public final static <T> T mergeConfig(Map<String, ?> map, Class<T> clazz, T instance, Boolean failOnUnknown) throws ConfigException {
        if (map.isEmpty()) {
            return instance;
        }
        
        return new HashMapConfigParser<T>(clazz, failOnUnknown).merge(map, instance);
    }
    
    public static <T> boolean isValid(T configuration) throws ConfigException {
        return getDefaultParser(null).isValid(configuration);
    }

}
