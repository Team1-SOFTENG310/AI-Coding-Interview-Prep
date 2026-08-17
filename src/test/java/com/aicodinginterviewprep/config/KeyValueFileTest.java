package com.aicodinginterviewprep.config;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyValueFileTest {

    @Test
    void parsesSimpleKeyValuePairs() {
        Map<String, String> values = KeyValueFile.parse(Stream.of("FOO=bar", "BAZ=qux"));

        assertEquals("bar", values.get("FOO"));
        assertEquals("qux", values.get("BAZ"));
    }

    @Test
    void trimsWhitespaceAroundKeysAndValues() {
        Map<String, String> values = KeyValueFile.parse(Stream.of("  FOO  =  bar  "));

        assertEquals("bar", values.get("FOO"));
    }

    @Test
    void skipsBlankLinesAndComments() {
        Map<String, String> values = KeyValueFile.parse(Stream.of("", "   ", "# a comment", "FOO=bar"));

        assertEquals(1, values.size());
        assertEquals("bar", values.get("FOO"));
    }

    @Test
    void skipsLinesWithoutAnEqualsSign() {
        Map<String, String> values = KeyValueFile.parse(Stream.of("NOT_A_PAIR", "FOO=bar"));

        assertEquals(1, values.size());
        assertTrue(values.containsKey("FOO"));
    }

    @Test
    void skipsLinesWhereEqualsIsTheFirstCharacter() {
        Map<String, String> values = KeyValueFile.parse(Stream.of("=novalue", "FOO=bar"));

        assertEquals(1, values.size());
    }

    @Test
    void valueCanContainAnEqualsSign() {
        Map<String, String> values = KeyValueFile.parse(Stream.of("FOO=bar=baz"));

        assertEquals("bar=baz", values.get("FOO"));
    }

    @Test
    void emptyStreamProducesEmptyMap() {
        Map<String, String> values = KeyValueFile.parse(Stream.empty());

        assertTrue(values.isEmpty());
    }
}
