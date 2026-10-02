package com.aicodinginterviewprep.errors;

public class SceneLoadException extends RuntimeException implements DomainError {
    private static final String USER_MESSAGE = "The requested screen could not be opened.";

    public SceneLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String userMessage() {
        return USER_MESSAGE;
    }
}
