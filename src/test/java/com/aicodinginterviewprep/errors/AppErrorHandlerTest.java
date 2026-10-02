package com.aicodinginterviewprep.errors;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppErrorHandlerTest {
    @Test
    void userMessage_usesDomainSpecificMessageForWrappedFailure() {
        NetworkException networkError = new NetworkException("HTTP 503: private response body");

        assertEquals(
                "The remote service is temporarily unavailable. Please try again.",
                AppErrorHandler.userMessage(new CompletionException(networkError))
        );
    }

    @Test
    void userMessage_preservesValidationDetails() {
        assertEquals(
                "Account already exists",
                AppErrorHandler.userMessage(new ValidationException("Account already exists"))
        );
    }

    @Test
    void userMessage_usesGenericMessageWhenFailureHasNoDetails() {
        assertEquals(
                "An unexpected error occurred. Please try again.",
                AppErrorHandler.userMessage(new RuntimeException())
        );
    }
}
