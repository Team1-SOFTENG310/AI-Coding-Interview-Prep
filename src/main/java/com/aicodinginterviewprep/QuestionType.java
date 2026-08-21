package com.aicodinginterviewprep;

public enum QuestionType {
    BEHAVIOURAL("Behavioural"),
    THEORY("Theory"),
    CODING("Coding");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
