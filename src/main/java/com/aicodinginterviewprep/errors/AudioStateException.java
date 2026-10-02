package com.aicodinginterviewprep.errors;

public class AudioStateException extends IllegalStateException implements DomainError {
    public AudioStateException(String message) {
        super(message);
    }

    @Override
    public String userMessage() {
        return getMessage();
    }
}
