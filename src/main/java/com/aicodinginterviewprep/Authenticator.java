package com.aicodinginterviewprep;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Authenticator {

    private final String fileName;
    private ArrayList<UserProfile> userProfiles;
    private final ObjectMapper objectMapper;
    private UserProfile currentUserProfile;

    public Authenticator(String fileName) {
        this.fileName = fileName;
        this.objectMapper = new ObjectMapper();
        this.userProfiles = new ArrayList<>();
        try {
            readUserProfiles();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read user profiles from " + fileName, exception);
        }
    }

    public void readUserProfiles() throws IOException {
        try (InputStream inputStream = openInputStream()) {
            if (inputStream == null) {
                userProfiles = new ArrayList<>();
                return;
            }
            userProfiles = objectMapper.readValue(
                inputStream,
                new TypeReference<ArrayList<UserProfile>>() {}
            );
        }
    }

    public void writeUserProfiles() throws IOException {
        Path outputPath = Path.of(fileName);
        try (OutputStream outputStream = Files.newOutputStream(
            outputPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, userProfiles);
        }
    }

    public List<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public void login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) { // return false if information is missing
            throw new IllegalStateException("Username and password are required");
        }
        if (currentUserProfile != null && currentUserProfile.nameAndPasswordMatch(username, password)) {
            throw new IllegalStateException("You are already logged in");
        }
        for (UserProfile userProfile : userProfiles) {
            if (userProfile.nameAndPasswordMatch(username, password)) { // If a matching profile is found, set it as the logged in profile
                currentUserProfile = userProfile;
                return;
            }
        }
        throw new IllegalStateException("Invalid username and password");
    }

    public void signUp(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        }
        for (UserProfile userProfile : userProfiles) { // Checks if the account already exists
            if (userProfile.nameAndPasswordMatch(username, password)) {
                throw new IllegalArgumentException("Account already exists");
            }
        }
        UserProfile userProfile = new UserProfile(username, password);
        userProfiles.add(userProfile); // Add the new account

        currentUserProfile = userProfile;
    }

    public String getUsername() {
        if (currentUserProfile == null) {
            return "Not logged in";
        }
        return currentUserProfile.getUsername();
    }

    public void updateUserScore(int rating) {
        if (!isSignedIn()) {
            throw new IllegalStateException("You must be logged in");
        }
        currentUserProfile.addEvaluationScore(rating);
    }

    public double getUserScore() {
        if (!isSignedIn()) {
            return 0.0;
        }
        return currentUserProfile.getAverageScore();
    }

    public boolean isSignedIn() {
        return currentUserProfile != null;
    }

    private InputStream openInputStream() throws IOException { // Attempt to open the file as a Path first, if it exists, return its InputStream; otherwise, try to load it as a resource from the classpath
        Path inputPath = Path.of(fileName);
        if (Files.exists(inputPath)) {
            return Files.newInputStream(inputPath);
        }
        return Authenticator.class.getClassLoader().getResourceAsStream(fileName);
    }
}
