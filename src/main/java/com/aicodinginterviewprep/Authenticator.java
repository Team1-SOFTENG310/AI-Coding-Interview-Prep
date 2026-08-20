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

    public ArrayList<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) { // return false if information is missing
            return false;
        }
        for (UserProfile userProfile : userProfiles) {
            if (userProfile.nameAndPasswordMatch(username, password)) { // If a matching profile is found, set it as the logged in profile
                currentUserProfile = userProfile;
                return true;
            }
        }
        return false;
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
        userProfiles.add(new UserProfile(username, password)); // Add the new account
    }


    public void updateUserScore(boolean correct) {
        currentUserProfile.questionAnswered(correct); // Updates the user score, use true if answer was correct, false if not
    }

    private InputStream openInputStream() throws IOException { // Attempt to open the file as a Path first, if it exists, return its InputStream; otherwise, try to load it as a resource from the classpath
        Path inputPath = Path.of(fileName);
        if (Files.exists(inputPath)) {
            return Files.newInputStream(inputPath);
        }
        return Authenticator.class.getClassLoader().getResourceAsStream(fileName);
    }
}
