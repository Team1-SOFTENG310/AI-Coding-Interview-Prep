package com.aicodinginterviewprep;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfile {
    private String username;
    private String password;
    private int questionsAnswered;
    private int totalScore;

    public UserProfile() {
    }

    public UserProfile(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(int questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void addEvaluationScore(int rating) { // Update the profiles score
        questionsAnswered++;
        totalScore += rating;
    }

    @JsonIgnore
    public double getAverageScore() {
        if (questionsAnswered == 0) {
            return 0.0;
        }
        return (double) totalScore /questionsAnswered;
    }

    public boolean nameAndPasswordMatch(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
