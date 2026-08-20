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
        profile.questionAnswered(true);
        authenticator.getUserProfiles().add(profile);

        authenticator.writeUserProfiles();

        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertEquals(1, reloaded.getUserProfiles().size());
        UserProfile reloadedProfile = reloaded.getUserProfiles().get(0);
        assertEquals("alice", reloadedProfile.getUsername());
        assertEquals("secret", reloadedProfile.getPassword());
        assertEquals(1, reloadedProfile.getQuestionsAnswered());
        assertEquals(1, reloadedProfile.getQuestionsCorrect());

    }

    @Test
    void readsAndWritesUserProfilesFail(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        UserProfile profile = new UserProfile("alice", "smith");
        profile.questionAnswered(true);
        authenticator.getUserProfiles().add(profile);

        authenticator.writeUserProfiles();

        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertEquals(1, reloaded.getUserProfiles().size());
        UserProfile reloadedProfile = reloaded.getUserProfiles().get(0);
        assertNotEquals("jane", reloadedProfile.getUsername());
        assertEquals("smith", reloadedProfile.getPassword());
        assertEquals(1, reloadedProfile.getQuestionsAnswered());
        assertEquals(1, reloadedProfile.getQuestionsCorrect());

    }

    @Test
    void loginToValidProfile(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        UserProfile profile = new UserProfile("alice", "smith");
        profile.questionAnswered(true);
        authenticator.getUserProfiles().add(profile);

        authenticator.writeUserProfiles();

        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertTrue(reloaded.login("alice", "smith"));
    }

    @Test
    void loginToInvalidProfileFails(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        UserProfile profile = new UserProfile("alice", "smith");
        profile.questionAnswered(true);
        authenticator.getUserProfiles().add(profile);

        authenticator.writeUserProfiles();

        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertFalse(reloaded.login("jane", "smith"));
    }
}

