package com.aicodinginterviewprep.openai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiApiExceptionTest {

    @Test
    @DisplayName("Single-argument message constructor initializes message with default status code and null response body")
    void testMessageConstructor() {
        OpenAiApiException exception = new OpenAiApiException("Invalid request format");

        assertEquals("Invalid request format", exception.getMessage());
        assertEquals(-1, exception.getStatusCode());
        assertNull(exception.getResponseBody());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Message and cause constructor initializes message, cause with default status code and null response body")
    void testMessageAndCauseConstructor() {
        Throwable cause = new IllegalArgumentException("Root cause error");
        OpenAiApiException exception = new OpenAiApiException("Failed operation", cause);

        assertEquals("Failed operation", exception.getMessage());
        assertSame(cause, exception.getCause());
        assertEquals(-1, exception.getStatusCode());
        assertNull(exception.getResponseBody());
    }

    @Test
    @DisplayName("Status code and response body constructor formats message correctly and sets fields")
    void testStatusCodeAndResponseBodyConstructor() {
        int statusCode = 404;
        String responseBody = "{\"error\": \"Model not found\"}";

        OpenAiApiException exception = new OpenAiApiException(statusCode, responseBody);

        assertEquals("OpenAI API call failed with status 404: {\"error\": \"Model not found\"}", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
        assertEquals(responseBody, exception.getResponseBody());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Message, status code, and response body constructor sets all provided fields")
    void testMessageStatusCodeAndResponseBodyConstructor() {
        int statusCode = 401;
        String responseBody = "{\"error\": \"Unauthorized\"}";
        String message = "Authentication failed";

        OpenAiApiException exception = new OpenAiApiException(message, statusCode, responseBody);

        assertEquals("Authentication failed", exception.getMessage());
        assertEquals(401, exception.getStatusCode());
        assertEquals(responseBody, exception.getResponseBody());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Full constructor sets message, status code, response body, and cause")
    void testFullConstructorWithMessageStatusCodeResponseBodyAndCause() {
        Throwable cause = new IllegalStateException("Transport error");
        int statusCode = 500;
        String responseBody = "{\"error\": \"Internal server error\"}";
        String message = "Server side failure occurred";

        OpenAiApiException exception = new OpenAiApiException(message, statusCode, responseBody, cause);

        assertEquals("Server side failure occurred", exception.getMessage());
        assertSame(cause, exception.getCause());
        assertEquals(500, exception.getStatusCode());
        assertEquals(responseBody, exception.getResponseBody());
    }
}
