package com.aicodinginterviewprep.errors;

public class NetworkException extends IllegalStateException implements DomainError {
    private static final String USER_MESSAGE =
        "The remote service is temporarily unavailable. Please try again.";

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String userMessage() {
        return USER_MESSAGE;
    }
}
