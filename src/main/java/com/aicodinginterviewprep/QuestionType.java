package com.aicodinginterviewprep;

public enum QuestionType {
    BEHAVIOURAL("Behavioural"),
    THEORY("Theory");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
