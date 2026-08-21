package com.aicodinginterviewprep;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticatorTest {

    @Test
    void readsAndWritesUserProfiles(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        UserProfile profile = new UserProfile("alice", "secret");
        profile.addEvaluationScore(1);
        authenticator.getUserProfiles().add(profile);

        authenticator.writeUserProfiles();

        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertEquals(1, reloaded.getUserProfiles().size());
        UserProfile reloadedProfile = reloaded.getUserProfiles().get(0);
        assertEquals("alice", reloadedProfile.getUsername());
        assertEquals("secret", reloadedProfile.getPassword());
        assertEquals(1, reloadedProfile.getQuestionsAnswered());
        assertEquals(1, reloadedProfile.getTotalScore());

    }

    @Test
    void readsAndWritesUserProfilesFail(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        UserProfile profile = new UserProfile("alice", "smith");
        profile.addEvaluationScore(1);
        authenticator.getUserProfiles().add(profile);

        authenticator.writeUserProfiles();

        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertEquals(1, reloaded.getUserProfiles().size());
        UserProfile reloadedProfile = reloaded.getUserProfiles().get(0);
        assertNotEquals("jane", reloadedProfile.getUsername());
        assertEquals("smith", reloadedProfile.getPassword());
        assertEquals(1, reloadedProfile.getQuestionsAnswered());
        assertEquals(1, reloadedProfile.getTotalScore());

    }

    @Test
    void loginToValidProfile(@TempDir Path tempDir) throws Exception {
        assertDoesNotThrow(() -> {
            Path profileFile = tempDir.resolve("testaccounts.json");
            Authenticator authenticator = new Authenticator(profileFile.toString());
            UserProfile profile = new UserProfile("alice", "smith");
            profile.addEvaluationScore(1);
            authenticator.getUserProfiles().add(profile);

            authenticator.writeUserProfiles();

            Authenticator reloaded = new Authenticator(profileFile.toString());
            reloaded.login("alice", "smith");
        });
    }

    @Test
    void loginToInvalidProfileFails(@TempDir Path tempDir) throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            Path profileFile = tempDir.resolve("testaccounts.json");
            Authenticator authenticator = new Authenticator(profileFile.toString());
            UserProfile profile = new UserProfile("alice", "smith");
            profile.addEvaluationScore(3);
            authenticator.getUserProfiles().add(profile);

            authenticator.writeUserProfiles();

            Authenticator reloaded = new Authenticator(profileFile.toString());
            reloaded.login("jane", "smith");
        });
    }

    @Test
    void loginWithNullUsernameFails(@TempDir Path tempDir) throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
            authenticator.login(null, "smith");
        });
    }

    @Test
    void loginWithNullPasswordFails(@TempDir Path tempDir) throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
            authenticator.login("alice", null);
        });
    }


    @Test
    void signUpWithExistingAccountThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "hunter2");

        assertThrows(IllegalArgumentException.class, () -> authenticator.signUp("bob", "hunter2"));
    }

    @Test
    void signUpWithDifferentExistingAccountSucceeds(@TempDir Path tempDir) throws Exception {
        assertDoesNotThrow(() -> {
            Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
            authenticator.signUp("carol", "letmein");

            authenticator.signUp("bob", "hunter2");

            assertEquals(2, authenticator.getUserProfiles().size());
            authenticator.login("carol", "letmein");
        });
    }

    @Test
    void signUpWithNullUsernameThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        assertThrows(IllegalArgumentException.class, () -> authenticator.signUp(null, "hunter2"));
    }

    @Test
    void signUpWithNullPasswordThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        assertThrows(IllegalArgumentException.class, () -> authenticator.signUp("bob", null));
    }

    @Test
    void updateUserScoreUpdatesLoggedInProfile(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "hunter2");

        authenticator.updateUserScore(6);
        authenticator.updateUserScore(4);

        UserProfile profile = authenticator.getUserProfiles().get(0);
        assertEquals(2, profile.getQuestionsAnswered());
        assertEquals(5, profile.getAverageScore());
    }

    @Test
    void signUp(@TempDir Path tempDir) throws Exception {
        assertDoesNotThrow(() -> {
            Path profileFile = tempDir.resolve("testaccounts.json");
            Authenticator authenticator = new Authenticator(profileFile.toString());
            authenticator.signUp("alice", "smith");
            authenticator.writeUserProfiles();
            Authenticator reloaded = new Authenticator(profileFile.toString());
            reloaded.login("alice", "smith");
        });
    }

    @Test
    void signUpFails(@TempDir Path tempDir) throws Exception {
        assertThrows(IllegalStateException.class, () -> {
            Path profileFile = tempDir.resolve("testaccounts.json");
            Authenticator authenticator = new Authenticator(profileFile.toString());
            authenticator.signUp("john", "doe");
            authenticator.writeUserProfiles();
            Authenticator reloaded = new Authenticator(profileFile.toString());
            reloaded.login("alice", "smith");
        });
    }

    @Test
    void updateUserScore(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        authenticator.signUp("alice", "smith");
        authenticator.updateUserScore(5);
        authenticator.writeUserProfiles();
        Authenticator reloaded = new Authenticator(profileFile.toString());
        reloaded.login("alice", "smith");
        assertEquals(5.0, reloaded.getUserScore());
    }
}

