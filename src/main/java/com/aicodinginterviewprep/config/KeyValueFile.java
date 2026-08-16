package com.aicodinginterviewprep.config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class KeyValueFile {
    private KeyValueFile() {
    }

    public static Map<String, String> parse(Stream<String> lines) {
        Map<String, String> values = new HashMap<>();
        lines.forEach(line -> {
            String trimmed = line.trim();
            int separator = trimmed.indexOf('=');
            boolean isEntry = !trimmed.isEmpty() && !trimmed.startsWith("#") && separator > 0;
            if (isEntry) {
                values.put(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim());
            }
        });
        return values;
    }
}
