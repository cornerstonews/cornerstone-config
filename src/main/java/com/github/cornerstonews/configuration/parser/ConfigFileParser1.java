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
package com.github.cornerstonews.configuration.parser;

import java.io.IOException;

import com.github.cornerstonews.configuration.ConfigException;

public interface ConfigParser<T> {

    /**
     * Loads, parses, binds, and validates a configuration object from a file.
     *
     * @param path the path of the configuration file
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigException if there is an error parsing or validating the file
     */
    T build(String path) throws IOException, ConfigException;
    
    /**
     * Loads, parses, binds, and validates a configuration object from an empty document.
     *
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigException if there is an error parsing or validating the file
     */
    T build() throws IOException, ConfigException;

    /**
     * Return true if file at given file is the right format and this parser can parse it,
     * else return false
     *
     * @return true if file can be parsed else false
     */
    Boolean isValidFileType(String path);

}
