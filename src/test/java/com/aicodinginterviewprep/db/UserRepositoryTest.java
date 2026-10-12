package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository users;

    @BeforeEach
    void setUp() {
        users = new UserRepository(new TestDatabase().connections());
    }

    @Test
    void createReturnsPersistedAccount() {
        UserAccount account = users.create("alice", "hash1");

        assertTrue(account.id() > 0);
        assertEquals("alice", account.username());
        assertEquals("hash1", account.passwordHash());
        assertNotNull(account.createdAt());
    }

    @Test
    void findByUsernameAndIdReturnAccount() {
        UserAccount created = users.create("alice", "hash1");

        assertEquals(created, users.findByUsername("alice").orElseThrow());
        assertEquals(created, users.findById(created.id()).orElseThrow());
    }

    @Test
    void findReturnsEmptyWhenMissing() {
        assertTrue(users.findByUsername("nobody").isEmpty());
        assertTrue(users.findById(999).isEmpty());
    }

    @Test
    void duplicateUsernameIsRejected() {
        users.create("alice", "hash1");

        assertThrows(UserRepository.DuplicateUsernameException.class, () -> users.create("alice", "hash2"));
    }

    @Test
    void invalidUsernameViolatingSchemaChecksIsRejected() {
        assertThrows(DataAccessException.class, () -> users.create("ab", "hash"));
        assertThrows(DataAccessException.class, () -> users.create(" padded ", "hash"));
    }

    @Test
    void connectionFailureIsWrapped() {
        UserRepository broken = new UserRepository(TestDatabase.failing());

        assertThrows(DataAccessException.class, () -> broken.findByUsername("alice"));
        assertThrows(DataAccessException.class, () -> broken.findById(1));
        assertThrows(DataAccessException.class, () -> broken.create("alice", "h"));
    }
}
