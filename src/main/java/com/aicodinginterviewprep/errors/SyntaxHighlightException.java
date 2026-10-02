package com.aicodinginterviewprep.errors;

public class SyntaxHighlightException extends IllegalStateException implements DomainError {
    public SyntaxHighlightException(String message) {
        super(message);
    }

    @Override
    public String userMessage() {
        return "Code syntax highlighting encountered an unsupported token.";
    }
}
