package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeSubmissionRepositoryTest {

    private UserRepository users;
    private SavedQuestionRepository questions;
    private CodeSubmissionRepository submissions;
    private long aliceId;
    private long bobId;

    @BeforeEach
    void setUp() {
        ConnectionFactory connections = new TestDatabase().connections();
        users = new UserRepository(connections);
        questions = new SavedQuestionRepository(connections);
        submissions = new CodeSubmissionRepository(connections);
        aliceId = users.create("alice", "h").id();
        bobId = users.create("bob", "h").id();
    }

    @Test
    void savePersistsAllFields() {
        SavedQuestion question = questions.save(aliceId, "Two Sum", "CODING", "EASY");

        CodeSubmission saved = submissions.save(aliceId, question.id(), "Two Sum", "int x;", "java", "Good", true);

        assertTrue(saved.id() > 0);
        assertEquals(aliceId, saved.userId());
        assertEquals(question.id(), saved.savedQuestionId());
        assertEquals("Two Sum", saved.questionText());
        assertEquals("int x;", saved.code());
        assertEquals("java", saved.language());
        assertEquals("Good", saved.feedback());
        assertEquals(Boolean.TRUE, saved.correct());
        assertNotNull(saved.submittedAt());
    }

    @Test
    void optionalFieldsMayBeNull() {
        CodeSubmission saved = submissions.save(aliceId, null, "Q", "code", "java", null, null);

        assertNull(saved.savedQuestionId());
        assertNull(saved.feedback());
        assertNull(saved.correct());
    }

    @Test
    void correctFalseIsPreservedDistinctFromNull() {
        CodeSubmission saved = submissions.save(aliceId, null, "Q", "code", "java", "Bad", false);

        assertEquals(Boolean.FALSE, saved.correct());
    }

    @Test
    void findByIdIsScopedToOwner() {
        CodeSubmission saved = submissions.save(aliceId, null, "Q", "code", "java", null, null);

        assertTrue(submissions.findById(aliceId, saved.id()).isPresent());
        assertTrue(submissions.findById(bobId, saved.id()).isEmpty());
        assertTrue(submissions.findById(aliceId, 999).isEmpty());
    }

    @Test
    void findByUserReturnsNewestFirstAndOnlyOwn() {
        submissions.save(aliceId, null, "Q1", "a", "java", null, null);
        submissions.save(aliceId, null, "Q2", "b", "java", null, null);
        submissions.save(bobId, null, "Q3", "c", "java", null, null);

        List<CodeSubmission> result = submissions.findByUser(aliceId);

        assertEquals(List.of("b", "a"), result.stream().map(CodeSubmission::code).toList());
    }

    @Test
    void codeWhitespaceAndLongContentArePreserved() {
        String code = "class A {\n\tint x;\n}\n" + "// pad\n".repeat(20_000);

        CodeSubmission saved = submissions.save(aliceId, null, "Q", code, "java", null, null);

        assertEquals(code, saved.code());
    }

    @Test
    void findBySavedQuestionReturnsNewestFirst() {
        SavedQuestion q = questions.save(aliceId, "Q1", "CODING", "EASY");
        submissions.save(aliceId, q.id(), "Q1", "first", "java", null, null);
        submissions.save(aliceId, q.id(), "Q1", "second", "java", null, null);

        List<CodeSubmission> result = submissions.findBySavedQuestion(aliceId, q.id());

        assertEquals(List.of("second", "first"), result.stream().map(CodeSubmission::code).toList());
    }

    @Test
    void findByUserIsEmptyWhenNothingSubmitted() {
        assertTrue(submissions.findByUser(aliceId).isEmpty());
    }

    @Test
    void findBySavedQuestionFiltersSubmissions() {
        SavedQuestion q1 = questions.save(aliceId, "Q1", "CODING", "EASY");
        SavedQuestion q2 = questions.save(aliceId, "Q2", "CODING", "EASY");
        submissions.save(aliceId, q1.id(), "Q1", "a", "java", null, null);
        submissions.save(aliceId, q1.id(), "Q1", "b", "java", null, null);
        submissions.save(aliceId, q2.id(), "Q2", "c", "java", null, null);

        assertEquals(2, submissions.findBySavedQuestion(aliceId, q1.id()).size());
        assertEquals(1, submissions.findBySavedQuestion(aliceId, q2.id()).size());
        assertTrue(submissions.findBySavedQuestion(bobId, q1.id()).isEmpty());
    }

    @Test
    void deletingBookmarkDeletesItsSubmissionsOnly() {
        SavedQuestion deleted = questions.save(aliceId, "Q1", "CODING", "EASY");
        SavedQuestion kept = questions.save(aliceId, "Q2", "CODING", "EASY");
        submissions.save(aliceId, deleted.id(), "Q1", "a", "java", null, null);
        submissions.save(aliceId, deleted.id(), "Q1", "b", "java", null, null);
        submissions.save(aliceId, kept.id(), "Q2", "c", "java", null, null);
        submissions.save(aliceId, null, "Q3", "d", "java", null, null);

        assertTrue(questions.delete(aliceId, deleted.id()));

        assertEquals(List.of("d", "c"), submissions.findByUser(aliceId).stream().map(CodeSubmission::code).toList());
    }

    @Test
    void deletingAnotherUsersBookmarkKeepsItsSubmissions() {
        SavedQuestion question = questions.save(aliceId, "Q1", "CODING", "EASY");
        submissions.save(aliceId, question.id(), "Q1", "a", "java", null, null);

        assertFalse(questions.delete(bobId, question.id()));

        assertEquals(1, submissions.findBySavedQuestion(aliceId, question.id()).size());
    }

    @Test
    void deleteRemovesOnlyTheGivenSubmission() {
        CodeSubmission first = submissions.save(aliceId, null, "Q", "a", "java", null, null);
        submissions.save(aliceId, null, "Q", "b", "java", null, null);

        assertTrue(submissions.delete(aliceId, first.id()));

        assertEquals(List.of("b"), submissions.findByUser(aliceId).stream().map(CodeSubmission::code).toList());
    }

    @Test
    void deleteCannotRemoveAnotherUsersSubmissionOrAMissingOne() {
        CodeSubmission saved = submissions.save(aliceId, null, "Q", "a", "java", null, null);

        assertFalse(submissions.delete(bobId, saved.id()));
        assertFalse(submissions.delete(aliceId, 999));

        assertTrue(submissions.findById(aliceId, saved.id()).isPresent());
    }

    @Test
    void deleteKeepsTheLinkedBookmark() {
        SavedQuestion question = questions.save(aliceId, "Q1", "CODING", "EASY");
        CodeSubmission saved = submissions.save(aliceId, question.id(), "Q1", "a", "java", null, null);

        submissions.delete(aliceId, saved.id());

        assertTrue(questions.exists(aliceId, "Q1"));
    }

    @Test
    void saveForUnknownUserFails() {
        assertThrows(DataAccessException.class,
                () -> submissions.save(999, null, "Q", "code", "java", null, null));
    }

    @Test
    void dataAccessFailureHasUserFacingMessage() {
        CodeSubmissionRepository broken = new CodeSubmissionRepository(TestDatabase.failing());

        DataAccessException error = assertThrows(DataAccessException.class, () -> broken.findByUser(1));

        assertEquals("Application data could not be loaded or saved.",
                com.aicodinginterviewprep.errors.AppErrorHandler.userMessage(error));
    }

    @Test
    void connectionFailureIsWrapped() {
        CodeSubmissionRepository broken = new CodeSubmissionRepository(TestDatabase.failing());

        assertThrows(DataAccessException.class, () -> broken.save(1, null, "Q", "c", "java", null, null));
        assertThrows(DataAccessException.class, () -> broken.findById(1, 1));
        assertThrows(DataAccessException.class, () -> broken.findByUser(1));
        assertThrows(DataAccessException.class, () -> broken.findBySavedQuestion(1, 1));
        assertThrows(DataAccessException.class, () -> broken.delete(1, 1));
    }
}
