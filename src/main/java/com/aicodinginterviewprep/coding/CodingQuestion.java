package com.aicodinginterviewprep.coding;

public class CodingQuestion {

    private final String id;
    private final String title;
    private final String difficulty;
    private final String description;
    private final String exampleInput;
    private final String exampleOutput;
    private final String starterCode;

    public CodingQuestion(String id, String title, String difficulty, String description,
                           String exampleInput, String exampleOutput, String starterCode) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.description = description;
        this.exampleInput = exampleInput;
        this.exampleOutput = exampleOutput;
        this.starterCode = starterCode;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDifficulty() { return difficulty; }
    public String getDescription() { return description; }
    public String getExampleInput() { return exampleInput; }
    public String getExampleOutput() { return exampleOutput; }
    public String getStarterCode() { return starterCode; }

    public String getListLabel() {
        return title + " (" + difficulty + ")";
    }

    @Override
    public String toString() {
        return getListLabel();
    }

}