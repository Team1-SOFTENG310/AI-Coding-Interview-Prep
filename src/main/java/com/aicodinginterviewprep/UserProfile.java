package com.aicodinginterviewprep;

public class UserProfile {
    private String username;
    private String password;
    private int questionsAnswered;
    private int questionsCorrect;

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

    public int getQuestionsCorrect() {
        return questionsCorrect;
    }

    public void setQuestionsCorrect(int questionsCorrect) {
        this.questionsCorrect = questionsCorrect;
    }

    public void questionAnswered(boolean answeredCorrect) { // Update the profiles score
        questionsAnswered++;
        if (answeredCorrect) {
            questionsCorrect++;
        }
    }

    public boolean nameAndPasswordMatch(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
