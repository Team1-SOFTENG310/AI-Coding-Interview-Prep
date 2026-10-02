package com.aicodinginterviewprep;

import com.aicodinginterviewprep.errors.PersistenceException;
import com.aicodinginterviewprep.errors.ValidationException;
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

//

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
            throw new PersistenceException("Unable to read user profiles from " + fileName, exception);
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
            throw new ValidationException("Username and password cannot be null");
        }
        // strip blank spaces around username and password
        username = username.strip();
        password = password.strip();
        // reject blank username and password after stripping
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username and password cannot be blank");
        }
        // password length check (8 to 64 characters)
        if (password.length() < 8 || password.length() > 64) {
            throw new IllegalArgumentException("Password must be between 8 and 64 characters");
        }
        if (!meetsPasswordComplexityRequirements(password)) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter, one " +
                        "lowercase letter, one number and one special character."
            );
        }
        for (UserProfile userProfile : userProfiles) { // Checks if the account already exists
            if (userProfile.getUsername().equals(username)) {
                throw new ValidationException("Account already exists");
            }
        }
        UserProfile userProfile = new UserProfile(username, password);
        userProfiles.add(userProfile); // Add the new account
        currentUserProfile = userProfile;
    }

    private static boolean meetsPasswordComplexityRequirements(String password) {
        boolean hasUppercaseLetter = password.chars().anyMatch(character -> character >= 'A' && character <= 'Z');
        boolean hasLowercaseLetter = password.chars().anyMatch(character -> character >= 'a' && character <= 'z');
        boolean hasNumber = password.chars().anyMatch(character -> character >= '0' && character <= '9');
        boolean hasSymbol = password.chars().anyMatch(
            character -> !Character.isLetterOrDigit(character) && !Character.isWhitespace(character)
        );

        return hasUppercaseLetter && hasLowercaseLetter && hasNumber && hasSymbol;
    }


    public void updateUserScore(boolean correct) {
        currentUserProfile.questionAnswered(correct); // Updates the user score, use true if answer was correct, false if not
    }

    public int getUserScore() {
        return currentUserProfile.getQuestionsCorrect();
    }

    private InputStream openInputStream() throws IOException { // Attempt to open the file as a Path first, if it exists, return its InputStream; otherwise, try to load it as a resource from the classpath
        Path inputPath = Path.of(fileName);
        if (Files.exists(inputPath)) {
            return Files.newInputStream(inputPath);
        }
        return Authenticator.class.getClassLoader().getResourceAsStream(fileName);
    }
}
