package com.aicodinginterviewprep.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class EnvConfig {
    private static final Map<String, String> VALUES = load();

    private EnvConfig() {
    }

    public static String get(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return VALUES.get(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static Map<String, String> load() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return new HashMap<>();
        }
        try (Stream<String> lines = Files.lines(envFile)) {
            return KeyValueFile.parse(lines);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file", e);
        }
    }
}
