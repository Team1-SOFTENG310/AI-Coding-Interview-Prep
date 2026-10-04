package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SavedQuestionRepositoryTest {

    private UserRepository users;
    private SavedQuestionRepository questions;
    private long aliceId;
    private long bobId;

    @BeforeEach
    void setUp() {
        ConnectionFactory connections = new TestDatabase().connections();
        users = new UserRepository(connections);
        questions = new SavedQuestionRepository(connections);
        aliceId = users.create("alice", "h").id();
        bobId = users.create("bob", "h").id();
    }

    @Test
    void hashIsStableAndIgnoresSurroundingWhitespace() {
        assertEquals(SavedQuestionRepository.hash("Two Sum"), SavedQuestionRepository.hash("  Two Sum\n"));
        assertNotEquals(SavedQuestionRepository.hash("Two Sum"), SavedQuestionRepository.hash("Three Sum"));
        assertEquals(64, SavedQuestionRepository.hash("x").length());
    }

    @Test
    void savePersistsQuestion() {
        SavedQuestion saved = questions.save(aliceId, "Two Sum", "CODING", "EASY");

        assertTrue(saved.id() > 0);
        assertEquals(aliceId, saved.userId());
        assertEquals("Two Sum", saved.questionText());
        assertEquals("CODING", saved.questionType());
        assertEquals("EASY", saved.difficulty());
        assertNotNull(saved.createdAt());
    }

    @Test
    void savingSameQuestionTwiceReturnsExistingBookmark() {
        SavedQuestion first = questions.save(aliceId, "Two Sum", "CODING", "EASY");
        SavedQuestion second = questions.save(aliceId, "Two Sum", "CODING", "EASY");

        assertEquals(first.id(), second.id());
        assertEquals(1, questions.findByUser(aliceId).size());
    }

    @Test
    void whitespaceVariantsOfSameQuestionAreTreatedAsDuplicates() {
        SavedQuestion first = questions.save(aliceId, "Two Sum", "CODING", "EASY");
        SavedQuestion second = questions.save(aliceId, "  Two Sum\n", "CODING", "EASY");

        assertEquals(first.id(), second.id());
        assertTrue(questions.exists(aliceId, "Two Sum  "));
    }

    @Test
    void duplicateSaveKeepsOriginalMetadata() {
        questions.save(aliceId, "Two Sum", "CODING", "EASY");

        SavedQuestion again = questions.save(aliceId, "Two Sum", "BEHAVIOURAL", "HARD");

        assertEquals("CODING", again.questionType());
        assertEquals("EASY", again.difficulty());
    }

    @Test
    void findByUserAndHashIsScopedToUser() {
        questions.save(aliceId, "Two Sum", "CODING", "EASY");
        String hash = SavedQuestionRepository.hash("Two Sum");

        assertTrue(questions.findByUserAndHash(aliceId, hash).isPresent());
        assertTrue(questions.findByUserAndHash(bobId, hash).isEmpty());
        assertTrue(questions.findByUserAndHash(aliceId, "nope").isEmpty());
    }

    @Test
    void longQuestionTextIsPersisted() {
        String text = "x".repeat(100_000);

        SavedQuestion saved = questions.save(aliceId, text, "CODING", "EASY");

        assertEquals(text, saved.questionText());
    }

    @Test
    void sameQuestionCanBeSavedByDifferentUsers() {
        questions.save(aliceId, "Two Sum", "CODING", "EASY");
        questions.save(bobId, "Two Sum", "CODING", "EASY");

        assertEquals(1, questions.findByUser(aliceId).size());
        assertEquals(1, questions.findByUser(bobId).size());
    }

    @Test
    void findByUserReturnsNewestFirstAndOnlyOwnQuestions() {
        questions.save(aliceId, "Q1", "CODING", "EASY");
        questions.save(aliceId, "Q2", "CODING", "HARD");
        questions.save(bobId, "Q3", "CODING", "EASY");

        List<SavedQuestion> result = questions.findByUser(aliceId);

        assertEquals(List.of("Q2", "Q1"), result.stream().map(SavedQuestion::questionText).toList());
    }

    @Test
    void findByUserIsEmptyWhenNothingSaved() {
        assertTrue(questions.findByUser(aliceId).isEmpty());
    }

    @Test
    void existsReflectsBookmarkState() {
        assertFalse(questions.exists(aliceId, "Two Sum"));
        questions.save(aliceId, "Two Sum", "CODING", "EASY");

        assertTrue(questions.exists(aliceId, "Two Sum"));
        assertFalse(questions.exists(bobId, "Two Sum"));
    }

    @Test
    void deleteRemovesOwnBookmark() {
        SavedQuestion saved = questions.save(aliceId, "Two Sum", "CODING", "EASY");

        assertTrue(questions.delete(aliceId, saved.id()));
        assertFalse(questions.exists(aliceId, "Two Sum"));
    }

    @Test
    void deleteCannotRemoveAnotherUsersBookmark() {
        SavedQuestion saved = questions.save(aliceId, "Two Sum", "CODING", "EASY");

        assertFalse(questions.delete(bobId, saved.id()));
        assertTrue(questions.exists(aliceId, "Two Sum"));
    }

    @Test
    void deleteReturnsFalseWhenMissing() {
        assertFalse(questions.delete(aliceId, 999));
    }

    @Test
    void saveForUnknownUserFails() {
        assertThrows(DataAccessException.class, () -> questions.save(999, "Q", "CODING", "EASY"));
    }

    @Test
    void connectionFailureIsWrapped() {
        SavedQuestionRepository broken = new SavedQuestionRepository(TestDatabase.failing());

        assertThrows(DataAccessException.class, () -> broken.save(1, "Q", "CODING", "EASY"));
        assertThrows(DataAccessException.class, () -> broken.findByUser(1));
        assertThrows(DataAccessException.class, () -> broken.exists(1, "Q"));
        assertThrows(DataAccessException.class, () -> broken.delete(1, 1));
    }
}
