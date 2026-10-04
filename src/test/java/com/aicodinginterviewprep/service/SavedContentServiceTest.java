package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.db.CodeSubmission;
import com.aicodinginterviewprep.db.CodeSubmissionRepository;
import com.aicodinginterviewprep.db.ConnectionFactory;
import com.aicodinginterviewprep.db.DataAccessException;
import com.aicodinginterviewprep.db.SavedQuestion;
import com.aicodinginterviewprep.db.SavedQuestionRepository;
import com.aicodinginterviewprep.db.TestDatabase;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SavedContentServiceTest {

    private UserRepository users;
    private SavedContentService service;

    @BeforeEach
    void setUp() {
        ConnectionFactory connections = new TestDatabase().connections();
        users = new UserRepository(connections);
        service = new SavedContentService(users, new SavedQuestionRepository(connections),
                new CodeSubmissionRepository(connections));
        users.create("alice", "h");
        users.create("bobby", "h");
    }

    @Test
    void resolveUserIdFindsExistingAccount() {
        long id = users.findByUsername("alice").orElseThrow().id();

        assertEquals(id, service.resolveUserId("  alice "));
    }

    @Test
    void differentUsernamesGetDifferentIds() {
        assertNotEquals(service.resolveUserId("alice"), service.resolveUserId("bobby"));
    }

    @Test
    void blankOrNullUsernameIsRejected() {
        assertThrows(ValidationException.class, () -> service.resolveUserId(null));
        assertThrows(ValidationException.class, () -> service.resolveUserId("  "));
        assertThrows(ValidationException.class, () -> service.listSavedQuestions(null));
    }

    @Test
    void unknownUsernameIsRejectedWithoutCreatingAnAccount() {
        assertThrows(ValidationException.class, () -> service.resolveUserId("ghost"));
        assertTrue(users.findByUsername("ghost").isEmpty());
    }

    @Test
    void saveListAndRemoveQuestion() {
        SavedQuestion saved = service.saveQuestion("alice", "  Two Sum  ", "CODING", "EASY");

        assertEquals("Two Sum", saved.questionText());
        assertTrue(service.isQuestionSaved("alice", "Two Sum"));
        assertEquals(1, service.listSavedQuestions("alice").size());
        assertTrue(service.listSavedQuestions("bobby").isEmpty());

        assertTrue(service.removeSavedQuestion("alice", saved.id()));
        assertFalse(service.isQuestionSaved("alice", "Two Sum"));
    }

    @Test
    void cannotRemoveAnotherUsersQuestion() {
        SavedQuestion saved = service.saveQuestion("alice", "Two Sum", "CODING", "EASY");

        assertFalse(service.removeSavedQuestion("bobby", saved.id()));
        assertTrue(service.isQuestionSaved("alice", "Two Sum"));
    }

    @Test
    void blankQuestionTextIsRejected() {
        assertThrows(ValidationException.class, () -> service.saveQuestion("alice", " ", "CODING", "EASY"));
        assertThrows(ValidationException.class, () -> service.saveQuestion("alice", null, "CODING", "EASY"));
        assertThrows(ValidationException.class, () -> service.isQuestionSaved("alice", ""));
    }

    @Test
    void submissionLinksToBookmarkedQuestion() {
        SavedQuestion saved = service.saveQuestion("alice", "Two Sum", "CODING", "EASY");

        CodeSubmission submission = service.recordSubmission("alice", "Two Sum", "int x;", "java", "Good", true);

        assertEquals(saved.id(), submission.savedQuestionId());
        assertEquals(1, service.listSubmissionsForQuestion("alice", saved.id()).size());
    }

    @Test
    void submissionWithoutBookmarkHasNoLink() {
        CodeSubmission submission = service.recordSubmission("alice", "Two Sum", "int x;", "java", null, null);

        assertNull(submission.savedQuestionId());
        assertEquals(1, service.listSubmissions("alice").size());
        assertTrue(service.listSubmissions("bobby").isEmpty());
    }

    @Test
    void submissionRequiresQuestionCodeAndLanguage() {
        assertThrows(ValidationException.class, () -> service.recordSubmission("alice", "", "c", "java", null, null));
        assertThrows(ValidationException.class, () -> service.recordSubmission("alice", "Q", " ", "java", null, null));
        assertThrows(ValidationException.class, () -> service.recordSubmission("alice", "Q", "c", null, null, null));
    }

    @Test
    void databaseFailuresSurfaceAsDataAccessException() {
        ConnectionFactory broken = TestDatabase.failing();
        SavedContentService failing = new SavedContentService(new UserRepository(broken),
                new SavedQuestionRepository(broken), new CodeSubmissionRepository(broken));

        assertThrows(DataAccessException.class, () -> failing.listSavedQuestions("alice"));
    }
}
