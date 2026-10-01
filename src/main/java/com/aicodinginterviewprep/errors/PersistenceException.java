package com.aicodinginterviewprep.errors;

public class PersistenceException extends IllegalStateException implements DomainError {
    private static final String USER_MESSAGE = "Application data could not be loaded or saved.";

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String userMessage() {
        return USER_MESSAGE;
    }
}
