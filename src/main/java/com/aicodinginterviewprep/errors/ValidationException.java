package com.aicodinginterviewprep.errors;

public class ValidationException extends IllegalArgumentException implements DomainError {
    public ValidationException(String message) {
        super(message);
    }

    @Override
    public String userMessage() {
        return getMessage();
    }
}
