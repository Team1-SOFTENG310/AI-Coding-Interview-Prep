package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.db.CodeSubmission;
import com.aicodinginterviewprep.db.CodeSubmissionRepository;
import com.aicodinginterviewprep.db.ConnectionFactory;
import com.aicodinginterviewprep.db.SavedQuestion;
import com.aicodinginterviewprep.db.SavedQuestionRepository;
import com.aicodinginterviewprep.db.TestDatabase;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.service.SavedContentService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveContentButtonTest {

    private Button button;
    private Label status;
    private SavedContentService service;
    private String username;
    private String answer;
    private Background background = TestBackgrounds.IMMEDIATE;
    private SaveContentButton saveContent;

    @BeforeAll
    static void initialiseJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX already started.
        }
    }

    @BeforeEach
    void setUp() {
        ConnectionFactory connections = new TestDatabase().connections();
        UserRepository users = new UserRepository(connections);
        users.create("alice", "h");
        service = new SavedContentService(users, new SavedQuestionRepository(connections),
                new CodeSubmissionRepository(connections));
        username = "alice";
        answer = "";
        button = new Button();
        status = new Label();
        saveContent = new SaveContentButton(button, status, () -> service, () -> background,
                () -> username, () -> answer, "java");
    }

    @Test
    void startsDisabledAndOfferEnablesIt() {
        assertTrue(button.isDisabled());

        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);

        assertFalse(button.isDisabled());
    }

    @Test
    void saveWithoutAnAnswerStoresOnlyTheQuestion() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.HARD);

        saveContent.save();

        List<SavedQuestion> saved = service.listSavedQuestions("alice");
        assertEquals(1, saved.size());
        assertEquals("CODING", saved.get(0).questionType());
        assertEquals("HARD", saved.get(0).difficulty());
        assertTrue(service.listSubmissions("alice").isEmpty());
        assertEquals(SaveContentButton.SAVED_QUESTION_MESSAGE, status.getText());
    }

    @Test
    void saveWithAnAnswerStoresQuestionAndLinkedAnswer() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        answer = "int x;";

        saveContent.save();

        SavedQuestion question = service.listSavedQuestions("alice").get(0);
        List<CodeSubmission> submissions = service.listSubmissionsForQuestion("alice", question.id());
        assertEquals(1, submissions.size());
        assertEquals("int x;", submissions.get(0).code());
        assertEquals("java", submissions.get(0).language());
        assertEquals(SaveContentButton.SAVED_QUESTION_AND_ANSWER_MESSAGE, status.getText());
    }

    @Test
    void whitespaceOnlyAnswerIsTreatedAsNoAnswer() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        answer = "  \n";

        saveContent.save();

        assertTrue(service.listSubmissions("alice").isEmpty());
        assertEquals(SaveContentButton.SAVED_QUESTION_MESSAGE, status.getText());
    }

    @Test
    void savingAgainWithTheSameAnswerDoesNotDuplicate() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        answer = "int x;";
        saveContent.save();

        saveContent.save();

        assertEquals(1, service.listSubmissions("alice").size());
        assertEquals(SaveContentButton.ALREADY_SAVED_MESSAGE, status.getText());
    }

    @Test
    void savingAgainWithoutAnAnswerAfterSavingDoesNotDuplicate() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        saveContent.save();

        saveContent.save();

        assertEquals(1, service.listSavedQuestions("alice").size());
        assertEquals(SaveContentButton.ALREADY_SAVED_MESSAGE, status.getText());
    }

    @Test
    void savingAnAnswerAfterSavingOnlyTheQuestionAddsTheAnswer() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        saveContent.save();
        answer = "int x;";

        saveContent.save();

        assertEquals(1, service.listSavedQuestions("alice").size());
        assertEquals(1, service.listSubmissions("alice").size());
    }

    @Test
    void savingAChangedAnswerStoresANewSubmission() {
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        answer = "v1";
        saveContent.save();
        answer = "v2";

        saveContent.save();

        assertEquals(2, service.listSubmissions("alice").size());
    }

    @Test
    void whileSavingTheButtonIsDisabledAndFurtherClicksAreIgnored() {
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();
        background = deferred;
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);

        saveContent.save();
        saveContent.save();

        assertTrue(button.isDisabled());
        assertEquals(SaveContentButton.SAVING_MESSAGE, status.getText());
        assertEquals(1, deferred.pendingCount());
        assertTrue(service.listSavedQuestions("alice").isEmpty());
    }

    @Test
    void nothingIsWrittenOnTheCallingThreadUntilTheBackgroundRuns() {
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();
        background = deferred;
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);
        answer = "int x;";

        saveContent.save();
        assertTrue(service.listSubmissions("alice").isEmpty());

        deferred.runAll();

        assertEquals(1, service.listSubmissions("alice").size());
        assertFalse(button.isDisabled());
        assertEquals(SaveContentButton.SAVED_QUESTION_AND_ANSWER_MESSAGE, status.getText());
    }

    @Test
    void failureWhileSavingReenablesTheButton() {
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();
        background = deferred;
        ConnectionFactory broken = TestDatabase.failing();
        service = new SavedContentService(new UserRepository(broken), new SavedQuestionRepository(broken),
                new CodeSubmissionRepository(broken));
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);

        saveContent.save();
        deferred.runAll();

        assertFalse(button.isDisabled());
        assertEquals("Application data could not be loaded or saved.", status.getText());
    }

    @Test
    void saveFinishingAfterAResetDoesNotChangeTheNewQuestionsState() {
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();
        background = deferred;
        saveContent.offer("Q1", QuestionType.CODING, Difficulty.EASY);
        saveContent.save();

        saveContent.offer("Q2", QuestionType.CODING, Difficulty.EASY);
        deferred.runAll();

        assertFalse(button.isDisabled());
        assertEquals("", status.getText());
        saveContent.save();
        assertEquals(SaveContentButton.SAVING_MESSAGE, status.getText());
    }

    @Test
    void saveWithoutAQuestionDoesNothing() {
        answer = "int x;";

        saveContent.save();

        assertTrue(service.listSavedQuestions("alice").isEmpty());
        assertEquals("", status.getText());
    }

    @Test
    void resetDisablesAndForgetsTheSavedState() {
        saveContent.offer("Q1", QuestionType.THEORY, Difficulty.EASY);
        saveContent.save();

        saveContent.reset();

        assertTrue(button.isDisabled());
        assertEquals("", status.getText());
        saveContent.save();
        assertEquals(1, service.listSavedQuestions("alice").size());
    }

    @Test
    void signedOutUserSeesMessageAndNothingIsSaved() {
        username = null;
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);

        saveContent.save();

        assertEquals("You must be signed in to use saved content.", status.getText());
    }

    @Test
    void databaseFailureShowsUserMessageAndAllowsRetry() {
        ConnectionFactory broken = TestDatabase.failing();
        SavedContentService failing = new SavedContentService(new UserRepository(broken),
                new SavedQuestionRepository(broken), new CodeSubmissionRepository(broken));
        service = failing;
        saveContent.offer("Two Sum", QuestionType.CODING, Difficulty.EASY);

        saveContent.save();
        assertEquals("Application data could not be loaded or saved.", status.getText());

        ConnectionFactory working = new TestDatabase().connections();
        new UserRepository(working).create("alice", "h");
        service = new SavedContentService(new UserRepository(working), new SavedQuestionRepository(working),
                new CodeSubmissionRepository(working));
        saveContent.save();

        assertEquals(SaveContentButton.SAVED_QUESTION_MESSAGE, status.getText());
    }
}
