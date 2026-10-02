package com.aicodinginterviewprep.errors;

public class SpeechRecognitionException extends IllegalStateException implements DomainError {
    public SpeechRecognitionException(String message) {
        super(message);
    }

    @Override
    public String userMessage() {
        return getMessage();
    }
}
