package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneManager;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SavedQuestionsControllerTest {

    @BeforeAll
    static void initialiseJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX already started.
        }
    }

    private void runOnFxThreadAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        if (error.get() instanceof AssertionError assertionError) {
            throw assertionError;
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    private static class FakeSceneManager extends SceneManager {
        String lastScene;

        FakeSceneManager() {
            super(new Stage());
        }

        @Override
        public void switchToScene(String sceneName) {
            lastScene = sceneName;
        }
    }

    private static SavedContentService serviceOn(ConnectionFactory connections) {
        return new SavedContentService(new UserRepository(connections),
                new SavedQuestionRepository(connections), new CodeSubmissionRepository(connections));
    }

    private SavedQuestionsController createController(SavedContentService service) {
        return createController(service, TestBackgrounds.IMMEDIATE);
    }

    private SavedQuestionsController createController(SavedContentService service, Background background) {
        SavedQuestionsController controller = new SavedQuestionsController();
        controller.listQuestions = new ListView<>();
        controller.textQuestion = new TextArea();
        controller.listSubmissions = new ListView<>();
        controller.textSubmission = new TextArea();
        controller.buttonDelete = new Button();
        controller.labelStatus = new Label();
        try {
            Field field = SavedQuestionsController.class.getDeclaredField("savedContentService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        setBackground(controller, background);
        return controller;
    }

    private SavedContentService serviceWithAlice() {
        ConnectionFactory connections = new TestDatabase().connections();
        new UserRepository(connections).create("alice", "h");
        new UserRepository(connections).create("bobby", "h");
        return serviceOn(connections);
    }

    @Test
    void describeQuestionShowsTypeDifficultyAndTruncatedPreview() {
        SavedQuestion longQuestion = new SavedQuestion(1, 1, "word ".repeat(40), "CODING", "EASY", Instant.now());
        SavedQuestion shortQuestion = new SavedQuestion(2, 1, "Two\n  Sum", "THEORY", "HARD", Instant.now());

        assertTrue(SavedQuestionsController.describe(longQuestion).startsWith("CODING / EASY: word word"));
        assertTrue(SavedQuestionsController.describe(longQuestion).endsWith("..."));
        assertEquals("THEORY / HARD: Two Sum", SavedQuestionsController.describe(shortQuestion));
    }

    @Test
    void describeSubmissionIncludesLanguage() {
        CodeSubmission submission = new CodeSubmission(1, 1, null, "Q", "code", "java", null, null, Instant.now());

        assertTrue(SavedQuestionsController.describe(submission).endsWith("(java)"));
    }

    @Test
    void detailAppendsFeedbackOnlyWhenPresent() {
        CodeSubmission plain = new CodeSubmission(1, 1, null, "Q", "int x;", "java", null, null, Instant.now());
        CodeSubmission blank = new CodeSubmission(2, 1, null, "Q", "int x;", "java", "  ", null, Instant.now());
        CodeSubmission graded = new CodeSubmission(3, 1, null, "Q", "int x;", "java", "Good", true, Instant.now());

        assertEquals("int x;", SavedQuestionsController.detail(plain));
        assertEquals("int x;", SavedQuestionsController.detail(blank));
        assertEquals("int x;\n\n--- Feedback ---\nGood", SavedQuestionsController.detail(graded));
    }

    @Test
    void showingTheSceneListsOnlyTheSignedInUsersQuestions() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Two Sum", "CODING", "EASY");
        service.saveQuestion("alice", "Explain ACID", "THEORY", "HARD");
        service.saveQuestion("bobby", "Someone else's", "THEORY", "EASY");

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);

            controller.onSceneShown();

            assertEquals(2, controller.listQuestions.getItems().size());
            assertEquals("", controller.labelStatus.getText());
            assertTrue(controller.buttonDelete.isDisabled());
        });
    }

    @Test
    void loadingShowsAMessageUntilTheBackgroundWorkCompletes() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Two Sum", "CODING", "EASY");
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service, deferred);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);

            controller.onSceneShown();
            assertEquals(SavedQuestionsController.LOADING_MESSAGE, controller.labelStatus.getText());
            assertTrue(controller.listQuestions.getItems().isEmpty());

            deferred.runAll();

            assertEquals(1, controller.listQuestions.getItems().size());
            assertEquals("", controller.labelStatus.getText());
        });
    }

    @Test
    void answersLoadedForAnEarlierSelectionAreIgnored() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Q1", "CODING", "EASY");
        service.saveQuestion("alice", "Q2", "CODING", "EASY");
        service.recordSubmission("alice", "Q1", "code for q1", "java", null, null);
        service.recordSubmission("alice", "Q2", "code for q2", "java", null, null);
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            controller.onSceneShown();
            SavedQuestion q1 = controller.listQuestions.getItems().stream()
                    .filter(q -> q.questionText().equals("Q1")).findFirst().orElseThrow();
            SavedQuestion q2 = controller.listQuestions.getItems().stream()
                    .filter(q -> q.questionText().equals("Q2")).findFirst().orElseThrow();
            setBackground(controller, deferred);

            controller.listQuestions.getSelectionModel().select(q1);
            controller.listQuestions.getSelectionModel().select(q2);
            deferred.runAll();

            assertEquals(1, controller.listSubmissions.getItems().size());
            assertEquals("code for q2", controller.listSubmissions.getItems().get(0).code());
        });
    }

    @Test
    void removeDisablesTheButtonUntilTheBackgroundWorkCompletes() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Two Sum", "CODING", "EASY");
        TestBackgrounds.Deferred deferred = new TestBackgrounds.Deferred();

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            controller.onSceneShown();
            controller.listQuestions.getSelectionModel().select(0);
            setBackground(controller, deferred);

            controller.onDelete();

            assertTrue(controller.buttonDelete.isDisabled());
            assertEquals(1, service.listSavedQuestions("alice").size());
        });
    }

    private static void setBackground(SavedQuestionsController controller, Background background) {
        try {
            Field field = SavedQuestionsController.class.getDeclaredField("background");
            field.setAccessible(true);
            field.set(controller, background);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void emptyListShowsMessage() throws Exception {
        SavedContentService service = serviceWithAlice();

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);

            controller.onSceneShown();

            assertTrue(controller.listQuestions.getItems().isEmpty());
            assertEquals(SavedQuestionsController.EMPTY_MESSAGE, controller.labelStatus.getText());
        });
    }

    @Test
    void selectingAQuestionShowsItsTextAndSavedAnswers() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Two Sum", "CODING", "EASY");
        service.saveQuestion("alice", "Other", "CODING", "EASY");
        service.recordSubmission("alice", "Two Sum", "int x;", "java", "Good", true);
        service.recordSubmission("alice", "Other", "unrelated", "java", null, null);

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            controller.onSceneShown();

            controller.listQuestions.getSelectionModel().select(
                    controller.listQuestions.getItems().stream()
                            .filter(q -> q.questionText().equals("Two Sum")).findFirst().orElseThrow());

            assertEquals("Two Sum", controller.textQuestion.getText());
            assertFalse(controller.buttonDelete.isDisabled());
            assertEquals(1, controller.listSubmissions.getItems().size());

            controller.listSubmissions.getSelectionModel().select(0);

            assertEquals("int x;\n\n--- Feedback ---\nGood", controller.textSubmission.getText());
        });
    }

    @Test
    void removeDeletesTheSelectedQuestionAndClearsTheDetails() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Two Sum", "CODING", "EASY");
        service.saveQuestion("alice", "Other", "CODING", "EASY");

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            controller.onSceneShown();
            controller.listQuestions.getSelectionModel().select(0);

            controller.onDelete();

            assertEquals(1, controller.listQuestions.getItems().size());
            assertEquals(1, service.listSavedQuestions("alice").size());
            assertEquals("", controller.textQuestion.getText());
            assertTrue(controller.buttonDelete.isDisabled());
            assertEquals(SavedQuestionsController.REMOVED_MESSAGE, controller.labelStatus.getText());
        });
    }

    @Test
    void removeWithNothingSelectedDoesNothing() throws Exception {
        SavedContentService service = serviceWithAlice();
        service.saveQuestion("alice", "Two Sum", "CODING", "EASY");

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            controller.onSceneShown();

            controller.onDelete();

            assertEquals(1, service.listSavedQuestions("alice").size());
        });
    }

    @Test
    void signedOutUserSeesMessageInsteadOfQuestions() throws Exception {
        SavedContentService service = serviceWithAlice();

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            controller.setSceneManager(new FakeSceneManager());

            controller.onSceneShown();

            assertTrue(controller.listQuestions.getItems().isEmpty());
            assertEquals("You must be signed in to use saved content.", controller.labelStatus.getText());
        });
    }

    @Test
    void databaseFailureShowsUserMessage() throws Exception {
        SavedContentService service = serviceOn(TestDatabase.failing());

        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(service);
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);

            controller.onSceneShown();

            assertEquals("Application data could not be loaded or saved.", controller.labelStatus.getText());
        });
    }

    @Test
    void fxmlLoadsAndInjectsAllControls() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                javafx.fxml.FXMLLoader loader =
                        new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/SavedQuestions.fxml"));
                loader.load();
                SavedQuestionsController controller = loader.getController();

                assertNotNull(controller.listQuestions);
                assertNotNull(controller.textQuestion);
                assertNotNull(controller.listSubmissions);
                assertNotNull(controller.textSubmission);
                assertNotNull(controller.buttonDelete);
                assertNotNull(controller.labelStatus);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void backReturnsToPracticeByDefaultAndToTheConfiguredScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            SavedQuestionsController controller = createController(serviceWithAlice());
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onBack();
            assertEquals("practice", sceneManager.lastScene);

            controller.setReturnScene("coding");
            controller.onBack();
            assertEquals("coding", sceneManager.lastScene);
        });
    }
}
