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

    @Test
    void loginWithNullUsernameFails(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        assertFalse(authenticator.login(null, "smith"));
    }

    @Test
    void loginWithNullPasswordFails(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        assertFalse(authenticator.login("alice", null));
    }

    @Test
    void signUpAddsNewAccountAndAllowsLogin(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "Hunter2!");

        assertEquals(1, authenticator.getUserProfiles().size());
        assertTrue(authenticator.login("bob", "Hunter2!"));
    }

    @Test
    void signUpWithExistingAccountThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "Hunter2!");

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "Hunter2!");
    }

    @Test
    void signUpWithDifferentExistingAccountSucceeds(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "Hunter2!");

        authenticator.signUp("carol", "Letmein8!");

        assertEquals(2, authenticator.getUserProfiles().size());
        assertTrue(authenticator.login("carol", "Letmein8!"));
    }

    @Test
    void signUpWithNullUsernameThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, null, "Hunter2!");
    }

    @Test
    void signUpWithNullPasswordThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", null);
    }

    @Test
    void signUpWithBlankUsernameThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "", "Hunter2!");
        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, " \t\n ", "Hunter2!");
    }

    @Test
    void signUpWithBlankPasswordThrows(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "");
        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", " \t\n ");
    }

    @Test
    void signUpTrimsUsernameAndPassword(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        authenticator.signUp(" \t bob \n ", " \t Hunter2! \n ");

        assertEquals(1, authenticator.getUserProfiles().size());
        UserProfile profile = authenticator.getUserProfiles().get(0);
        assertEquals("bob", profile.getUsername());
        assertEquals("Hunter2!", profile.getPassword());
        assertTrue(authenticator.login("bob", "Hunter2!"));
    }

    @Test
    void signUpRejectsDuplicateUsernameAfterTrimming(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "Hunter2!");

        assertRejectedSignUpDoesNotChangeProfileCount(
            authenticator,
            " \t bob \n ",
            "Different1!"
        );
    }

    @Test
    void signUpRejectsSevenCharacterPassword(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "Aa1!aaa");
    }

    @Test
    void signUpAcceptsEightCharacterPassword(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        String password = "Aa1!aaaa";
        authenticator.signUp("bob", password);

        assertEquals(1, authenticator.getUserProfiles().size());
        assertEquals(password, authenticator.getUserProfiles().get(0).getPassword());
    }

    @Test
    void signUpAcceptsSixtyFourCharacterPassword(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        String password = "Aa1!" + "a".repeat(60);
        authenticator.signUp("bob", password);

        assertEquals(1, authenticator.getUserProfiles().size());
        assertEquals(password, authenticator.getUserProfiles().get(0).getPassword());
    }

    @Test
    void signUpRejectsSixtyFiveCharacterPassword(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(
            authenticator,
            "bob",
            "Aa1!" + "a".repeat(61)
        );
    }

    @Test
    void signUpRejectsPasswordWithoutUppercaseLetter(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "aa1!aaaa");
    }

    @Test
    void signUpRejectsPasswordWithoutLowercaseLetter(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "AA1!AAAA");
    }

    @Test
    void signUpRejectsPasswordWithoutNumber(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "Aa!aaaaa");
    }

    @Test
    void signUpRejectsPasswordWithoutSymbol(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "Aa1aaaaa");
    }

    @Test
    void signUpDoesNotTreatWhitespaceAsSymbol(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());

        assertRejectedSignUpDoesNotChangeProfileCount(authenticator, "bob", "Aa1 aaaa");
    }

    @Test
    void updateUserScoreUpdatesLoggedInProfile(@TempDir Path tempDir) throws Exception {
        Authenticator authenticator = new Authenticator(tempDir.resolve("testaccounts.json").toString());
        authenticator.signUp("bob", "Hunter2!");
        authenticator.login("bob", "Hunter2!");

        authenticator.updateUserScore(true);
        authenticator.updateUserScore(false);

        UserProfile profile = authenticator.getUserProfiles().get(0);
        assertEquals(2, profile.getQuestionsAnswered());
        assertEquals(1, profile.getQuestionsCorrect());
    }

    @Test
    void signUp(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        authenticator.signUp("alice", "Password1!");
        authenticator.writeUserProfiles();
        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertTrue(reloaded.login("alice", "Password1!"));
    }

    @Test
    void signUpFails(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        authenticator.signUp("john", "Password1!");
        authenticator.writeUserProfiles();
        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertFalse(reloaded.login("alice", "smith"));
    }

    @Test
    void updateUserScore(@TempDir Path tempDir) throws Exception {
        Path profileFile = tempDir.resolve("testaccounts.json");
        Authenticator authenticator = new Authenticator(profileFile.toString());
        authenticator.signUp("alice", "Password1!");
        authenticator.updateUserScore(true);
        authenticator.writeUserProfiles();
        Authenticator reloaded = new Authenticator(profileFile.toString());
        assertTrue(reloaded.login("alice", "Password1!"));
        assertEquals(1, reloaded.getUserScore());
    }

    private static void assertRejectedSignUpDoesNotChangeProfileCount(
        Authenticator authenticator,
        String username,
        String password
    ) {
        int profileCount = authenticator.getUserProfiles().size();

        assertThrows(IllegalArgumentException.class, () -> authenticator.signUp(username, password));
        assertEquals(profileCount, authenticator.getUserProfiles().size());
    }
}
