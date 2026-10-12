package com.aicodinginterviewprep;

import com.aicodinginterviewprep.db.TestDatabase;
import com.aicodinginterviewprep.db.UserAccount;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticatorTest {

    private UserRepository users;
    private Authenticator authenticator;

    @BeforeEach
    void setUp() {
        users = new UserRepository(new TestDatabase().connections());
        authenticator = new Authenticator(users);
    }

    @Test
    void signUpStoresHashedPasswordAndAllowsLogin() {
        authenticator.signUp("bob", "Hunter2!");

        UserAccount stored = users.findByUsername("bob").orElseThrow();
        assertNotEquals("Hunter2!", stored.passwordHash());
        assertTrue(PasswordHasher.verify("Hunter2!", stored.passwordHash()));
        assertTrue(new Authenticator(users).login("bob", "Hunter2!"));
    }

    @Test
    void signUpSetsCurrentUser() {
        authenticator.signUp("bob", "Hunter2!");

        assertEquals("bob", authenticator.getCurrentUser().orElseThrow().username());
    }

    @Test
    void loginSetsCurrentUser() {
        authenticator.signUp("bob", "Hunter2!");
        Authenticator other = new Authenticator(users);
        assertTrue(other.getCurrentUser().isEmpty());

        assertTrue(other.login("bob", "Hunter2!"));

        assertEquals("bob", other.getCurrentUser().orElseThrow().username());
    }

    @Test
    void loginWithUnknownUserFails() {
        assertFalse(authenticator.login("jane", "smith"));
    }

    @Test
    void loginWithWrongPasswordFails() {
        authenticator.signUp("bob", "Hunter2!");

        assertFalse(new Authenticator(users).login("bob", "Hunter3!"));
    }

    @Test
    void failedLoginDoesNotChangeCurrentUser() {
        authenticator.signUp("bob", "Hunter2!");

        assertFalse(authenticator.login("bob", "wrong"));

        assertEquals("bob", authenticator.getCurrentUser().orElseThrow().username());
    }

    @Test
    void loginWithNullUsernameFails() {
        assertFalse(authenticator.login(null, "smith"));
    }

    @Test
    void loginWithNullPasswordFails() {
        assertFalse(authenticator.login("alice", null));
    }

    @Test
    void signUpWithExistingAccountThrows() {
        authenticator.signUp("bob", "Hunter2!");

        assertRejected("bob", "Hunter2!");
    }

    @Test
    void signUpWithDifferentAccountSucceeds() {
        authenticator.signUp("bob", "Hunter2!");

        authenticator.signUp("carol", "Letmein8!");

        assertTrue(new Authenticator(users).login("carol", "Letmein8!"));
        assertTrue(new Authenticator(users).login("bob", "Hunter2!"));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        authenticator.signUp("bob", "Hunter2!");
        authenticator.signUp("carol", "Hunter2!");

        assertNotEquals(users.findByUsername("bob").orElseThrow().passwordHash(),
                users.findByUsername("carol").orElseThrow().passwordHash());
    }

    @Test
    void signUpWithNullUsernameThrows() {
        assertRejected(null, "Hunter2!");
    }

    @Test
    void signUpWithNullPasswordThrows() {
        assertRejected("bob", null);
    }

    @Test
    void signUpWithBlankUsernameThrows() {
        assertRejected("", "Hunter2!");
        assertRejected(" \t\n ", "Hunter2!");
    }

    @Test
    void signUpWithBlankPasswordThrows() {
        assertRejected("bob", "");
        assertRejected("bob", " \t\n ");
    }

    @Test
    void signUpTrimsUsernameAndPassword() {
        authenticator.signUp(" \t bob \n ", " \t Hunter2! \n ");

        assertTrue(users.findByUsername("bob").isPresent());
        assertTrue(new Authenticator(users).login("bob", "Hunter2!"));
    }

    @Test
    void signUpRejectsDuplicateUsernameAfterTrimming() {
        authenticator.signUp("bob", "Hunter2!");

        assertRejected(" \t bob \n ", "Different1!");
    }

    @Test
    void signUpRejectsTooShortUsername() {
        assertRejected("ab", "Hunter2!");
    }

    @Test
    void signUpRejectsTooLongUsername() {
        assertRejected("a".repeat(31), "Hunter2!");
    }

    @Test
    void signUpAcceptsUsernameLengthBounds() {
        authenticator.signUp("abc", "Hunter2!");
        authenticator.signUp("a".repeat(30), "Hunter2!");

        assertTrue(users.findByUsername("abc").isPresent());
        assertTrue(users.findByUsername("a".repeat(30)).isPresent());
    }

    @Test
    void signUpRejectsSevenCharacterPassword() {
        assertRejected("bob", "Aa1!aaa");
    }

    @Test
    void signUpAcceptsEightCharacterPassword() {
        authenticator.signUp("bob", "Aa1!aaaa");

        assertTrue(new Authenticator(users).login("bob", "Aa1!aaaa"));
    }

    @Test
    void signUpAcceptsSixtyFourCharacterPassword() {
        String password = "Aa1!" + "a".repeat(60);
        authenticator.signUp("bob", password);

        assertTrue(new Authenticator(users).login("bob", password));
    }

    @Test
    void signUpRejectsSixtyFiveCharacterPassword() {
        assertRejected("bob", "Aa1!" + "a".repeat(61));
    }

    @Test
    void signUpRejectsPasswordWithoutUppercaseLetter() {
        assertRejected("bob", "aa1!aaaa");
    }

    @Test
    void signUpRejectsPasswordWithoutLowercaseLetter() {
        assertRejected("bob", "AA1!AAAA");
    }

    @Test
    void signUpRejectsPasswordWithoutNumber() {
        assertRejected("bob", "Aa!aaaaa");
    }

    @Test
    void signUpRejectsPasswordWithoutSymbol() {
        assertRejected("bob", "Aa1aaaaa");
    }

    @Test
    void signUpDoesNotTreatWhitespaceAsSymbol() {
        assertRejected("bob", "Aa1 aaaa");
    }

    @Test
    void rejectedSignUpDoesNotSetCurrentUser() {
        assertRejected("bob", "weak");

        assertTrue(authenticator.getCurrentUser().isEmpty());
    }

    @Test
    void databaseFailuresArePropagatedAsPersistenceErrors() {
        Authenticator broken = new Authenticator(new UserRepository(TestDatabase.failing()));

        assertThrows(PersistenceException.class, () -> broken.login("bob", "Hunter2!"));
        assertThrows(PersistenceException.class, () -> broken.signUp("bob", "Hunter2!"));
    }

    private void assertRejected(String username, String password) {
        assertThrows(IllegalArgumentException.class, () -> authenticator.signUp(username, password));
    }
}