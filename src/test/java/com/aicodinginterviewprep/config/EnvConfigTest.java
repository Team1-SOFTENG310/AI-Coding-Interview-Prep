package com.aicodinginterviewprep.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvConfigTest {

    private static final String UNSET_KEY = "SOFTENG310_TEST_KEY_THAT_DOES_NOT_EXIST";

    @Test
    void getReturnsNullWhenKeyIsNotSetAnywhere() {
        assertNull(EnvConfig.get(UNSET_KEY));
    }

    @Test
    void getWithDefaultReturnsDefaultWhenKeyIsNotSetAnywhere() {
        assertEquals("fallback", EnvConfig.get(UNSET_KEY, "fallback"));
    }
}
