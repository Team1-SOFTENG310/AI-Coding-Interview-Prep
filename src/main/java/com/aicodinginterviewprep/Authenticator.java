package com.aicodinginterviewprep;

import com.aicodinginterviewprep.db.UserAccount;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.ValidationException;

import java.util.Optional;

public class Authenticator {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;

    private final UserRepository users;
    private UserAccount currentUser;

    public Authenticator(UserRepository users) {
        this.users = users;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) { // return false if information is missing
            return false;
        }
        Optional<UserAccount> account = users.findByUsername(username);
        if (account.isPresent() && PasswordHasher.verify(password, account.get().passwordHash())) {
            currentUser = account.get();
            return true;
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
            throw new ValidationException("Username and password cannot be blank");
        }
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            throw new ValidationException("Username must be between 3 and 30 characters");
        }
        // password length check (8 to 64 characters)
        if (password.length() < 8 || password.length() > 64) {
            throw new ValidationException("Password must be between 8 and 64 characters");
        }
        if (!meetsPasswordComplexityRequirements(password)) {
            throw new ValidationException(
                "Password must contain at least one uppercase letter, one " +
                        "lowercase letter, one number and one special character."
            );
        }
        try {
            currentUser = users.create(username, PasswordHasher.hash(password));
        } catch (UserRepository.DuplicateUsernameException e) {
            throw new ValidationException("Account already exists");
        }
    }

    public Optional<UserAccount> getCurrentUser() {
        return Optional.ofNullable(currentUser);
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
}

