package com.github.cornerstonews.configuration.parser;

import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cornerstonews.configuration.ConfigException;

public class HashMapConfigParser<T> extends BaseConfigParser<T> {

    private ObjectMapper jsonObjectMapper = new ObjectMapper(new JsonFactory());

    public HashMapConfigParser(Class<T> klass) {
        super(klass, new ObjectMapper());
    }

    public T build(Map<String, ?> map) throws ConfigException {
        try {
            String jsonConfig = mapper.writeValueAsString(map);
            final T config = jsonObjectMapper.readValue(jsonConfig, this.klass);
            return config;

        } catch (JsonProcessingException e) {
            throw new ConfigException(null, formatError("Failed to parse configuration", e.getMessage(), null, e.getLocation(), null), e);
        }
    }

    public T build(Map<String, ?> map, T config) throws ConfigException {
        try {
            T parsedConfig = this.build(map);
            return jsonObjectMapper.updateValue(config, parsedConfig);
        } catch (JsonProcessingException e) {
            throw new ConfigException(null, formatError("Failed to parse configuration", e.getMessage(), null, e.getLocation(), null), e);
        }
    }

}