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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cornerstonews.configuration.ConfigException;

public class BaseConfigParser<T> {
    private static final Logger log = LogManager.getLogger(BaseConfigParser.class);

    protected final Class<T> klass;
    protected ObjectMapper mapper;

    public BaseConfigParser(Class<T> klass, ObjectMapper objectMapper) {
        this.klass = klass;
        this.mapper = objectMapper;
    }

    public T build() throws ConfigException {
        try {
            log.info("Loading default application configuration");
            final T config = mapper.readValue("{}", this.klass);
            return config;
        } catch (JsonParseException e) {
            throw new ConfigException(null, formatError("Malformed default config", e.getMessage(), null, e.getLocation(), null), e);
        } catch (JsonProcessingException e) {
            throw new ConfigException(null, formatError("Failed to parse configuration", e.getMessage(), null, e.getLocation(), null), e);
        }
    }

    public boolean isValid(T config) throws ConfigException {
        final Set<ConstraintViolation<T>> violations = getValidator().validate(config);
        if (!violations.isEmpty()) {
            final Set<String> errors = new HashSet<>(violations.size());
            for (ConstraintViolation<?> v : violations) {
                errors.add(String.format("%s %s", v.getPropertyPath(), v.getMessage()));
            }
            throw new ConfigException(errors);
        }

        return true;
    }

    private Validator getValidator() {
        return Validation.byProvider(HibernateValidator.class).configure()
                // If needed add ValueExtractor(s) here for variables that can not be directly
                // validated
                .buildValidatorFactory().getValidator();
    }

    protected List<String> formatError(String summary, String detail, List<JsonMappingException.Reference> fieldPath, JsonLocation location,
            Collection<String> suggestions) {
        final StringBuilder sb = new StringBuilder(summary);
        if (fieldPath != null && !fieldPath.isEmpty()) {
            sb.append(" at: ").append(buildPath(fieldPath));
        } else if (location != null) {
            sb.append(" at line: ").append(location.getLineNr() + 1).append(", column: ").append(location.getColumnNr() + 1);
        }

        if (detail != null) {
            sb.append("; ").append(detail);
        }

        if (suggestions != null && !suggestions.isEmpty()) {
            sb.append(String.format("%n")).append("    Did you mean?:").append(String.format("%n"));
            final Iterator<String> it = suggestions.iterator();
            int i = 0;
            while (it.hasNext() && i < 10) {
                sb.append("      - ").append(it.next());
                i++;
                if (it.hasNext()) {
                    sb.append(String.format("%n"));
                }
            }

            final int total = suggestions.size();
            if (i < total) {
                sb.append("        [").append(total - i).append(" more]");
            }
        }

        List<String> errors = new ArrayList<>();
        errors.add(sb.toString());
        return errors;
    }

    private String buildPath(Iterable<JsonMappingException.Reference> path) {
        final StringBuilder sb = new StringBuilder();
        if (path != null) {
            final Iterator<JsonMappingException.Reference> it = path.iterator();
            while (it.hasNext()) {
                final JsonMappingException.Reference reference = it.next();
                final String name = reference.getFieldName();

                // append either the field name or list index
                if (name == null) {
                    sb.append('[').append(reference.getIndex()).append(']');
                } else {
                    sb.append(name);
                }

                if (it.hasNext()) {
                    sb.append('.');
                }
            }
        }
        return sb.toString();
    }
}
