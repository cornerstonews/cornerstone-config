package com.github.cornerstonews.configuration.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.github.cornerstonews.configuration.ConfigException;

public abstract class ConfigFileParser<T> extends BaseConfigParser<T> {

    private static final Logger log = LogManager.getLogger(ConfigFileParser.class);

    private File filePath;

    public ConfigFileParser(String path, Class<T> klass, ObjectMapper objectMapper) {
        super(klass, objectMapper);
        this.filePath = path == null ? null : new File(path);
    }

    protected abstract String getFormat();

    public abstract Boolean isValidFileType(String path);

    public static String getFileExtension(final String filename) {
        String fName = new File(filename).getName();
        Optional<String> extensionOptional = Optional.ofNullable(fName).filter(f -> f.contains(".")).map(f -> f.substring(fName.lastIndexOf(".") + 1));
        return extensionOptional.orElse(null);
    }

    public static String identifyFileType(final String fileName) {
        String fileType = null;
        try {
            final File file = new File(fileName);
            fileType = Files.probeContentType(file.toPath());
        } catch (IOException ioException) {
            System.out.println("asdf");
            // Do nothing
        }
        return fileType;
    }

    public T build(String path) throws IOException, ConfigException {
        if (this.filePath != null && path != null && !Objects.equals(filePath, new File(path))) {
            throw new ConfigException(path, Arrays.asList("Invalid parser. '" + this.getFormat() + "' can not be used for given path: " + path));
        }
        if (this.filePath == null && path != null && isValidFileType(path)) {
            this.filePath = new File(path);
        }

        if (filePath == null) {
            return super.build();
        }

        try {
            log.info("Loading application configuration from path '{}'", filePath.getAbsolutePath());
            final T config = mapper.readValue(filePath, this.klass);
            return config;
        } catch (UnrecognizedPropertyException e) {
            final List<String> properties = e.getKnownPropertyIds().stream().map(Object::toString).collect(Collectors.toList());
            throw new ConfigException(path, formatError("Unrecognized field", null, e.getPath(), e.getLocation(), properties), e);
        } catch (InvalidFormatException e) {
            final String sourceType = e.getValue().getClass().getSimpleName();
            final String targetType = e.getTargetType().getSimpleName();
            throw new ConfigException(path,
                    formatError("Incorrect type of value", "is of type: " + sourceType + ", expected: " + targetType, e.getPath(), e.getLocation(), null), e);
        } catch (JsonMappingException e) {
            throw new ConfigException(path, formatError("Failed to parse configuration", e.getMessage(), e.getPath(), e.getLocation(), null), e);
        } catch (JsonParseException e) {
            throw new ConfigException(path, formatError("Malformed " + getFormat(), e.getMessage(), null, e.getLocation(), null), e);
        }

    }

}
