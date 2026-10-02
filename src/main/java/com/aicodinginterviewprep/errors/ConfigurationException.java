package com.aicodinginterviewprep.errors;

public class ConfigurationException extends IllegalStateException implements DomainError {
    private static final String USER_MESSAGE =
        "Application configuration is incomplete. Check your local configuration.";

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String userMessage() {
        return USER_MESSAGE;
    }
}
