package com.github.cornerstonews.configuration.parser;

import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.cornerstonews.configuration.ConfigException;

public class HashMapConfigParser<T> extends BaseConfigParser<T> {

    private ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    public HashMapConfigParser(Class<T> klass, boolean failOnUnknown) {
        super(klass, new ObjectMapper());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknown);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknown);
    }

    public T build(Map<String, ?> map) throws ConfigException {
        try {
            String jsonConfig = mapper.writeValueAsString(map);
            final T config = objectMapper.readValue(jsonConfig, this.klass);
            return config;

        } catch (JsonProcessingException e) {
            throw new ConfigException(null, formatError("Failed to parse configuration", e.getMessage(), null, e.getLocation(), null), e);
        }
    }

    public T build(Map<String, ?> map, T config) throws ConfigException {
        try {
            T parsedConfig = this.build(map);
            return objectMapper.updateValue(config, parsedConfig);
        } catch (JsonProcessingException e) {
            throw new ConfigException(null, formatError("Failed to parse configuration", e.getMessage(), null, e.getLocation(), null), e);
        }
    }
    
    public T merge(Map<String, ?> map, T config) throws ConfigException {
        try {
            ObjectReader objectReader = objectMapper.readerForUpdating(config);
            String jsonConfig = mapper.writeValueAsString(map);
            return objectReader.readValue(jsonConfig);
        } catch (JsonProcessingException e) {
            throw new ConfigException(null, formatError("Failed to parse configuration", e.getMessage(), null, e.getLocation(), null), e);
        }
    }

}
