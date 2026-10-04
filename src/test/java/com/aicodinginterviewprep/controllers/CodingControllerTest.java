package com.aicodinginterviewprep.controllers;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.CodeSubmissionRepository;
import com.aicodinginterviewprep.db.ConnectionFactory;
import com.aicodinginterviewprep.db.SavedQuestionRepository;
import com.aicodinginterviewprep.db.TestDatabase;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import com.aicodinginterviewprep.service.SavedContentService;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

class CodingControllerTest {

    @BeforeAll
    static void initialiseJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX already started.
        }

    }

    @Test
    void setSceneManager_addsAllDifficultyLevelsAndDefaultsToMedium() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();

            controller.setSceneManager(new FakeSceneManager());

            assertEquals(3, controller.comboDifficulty.getItems().size());
            assertEquals(Difficulty.MEDIUM, controller.comboDifficulty.getValue());
        });
    }

    @Test
    void setSceneManager_addsCodingTopicsAndDefaultsToRandomTopic() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();

            controller.setSceneManager(new FakeSceneManager());

            assertEquals("Random topic", controller.comboTopic.getValue());
            assertTrue(controller.comboTopic.getItems().contains("Graphs"));
        });
    }

    private void runOnFxThreadAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error =
                new java.util.concurrent.atomic.AtomicReference<>();

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

        if (error.get() != null) {
            if (error.get() instanceof AssertionError assertionError) {
                throw assertionError;
            }

            throw new RuntimeException(error.get());
        }
    }

    private void setQuestionService(CodingController controller, OpenAiQuestionService service) {
        try {
            Field field = CodingController.class.getDeclaredField("questionService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private CodingController createController() {
        CodingController controller = new CodingController();

        controller.questionOutput = new TextArea();
        controller.codeEditorContainer = new StackPane();

        
        controller.buttonSubmitAnswer = new Button();
        controller.buttonGenerateQuestion = new Button();
        controller.buttonPractice = new Button();
        controller.labelLoggedInAs = new Label();
        controller.buttonLogOut = new Button();
        controller.comboTopic = new ComboBox<>();
        controller.comboDifficulty = new ComboBox<>();
        controller.buttonSave = new Button();
        controller.labelSaveStatus = new Label();

        return controller;
    }

    private SavedContentService useTestSavedContentService(CodingController controller) {
        ConnectionFactory connections = new TestDatabase().connections();
        new UserRepository(connections).create("alice", "h");
        SavedContentService service = new SavedContentService(new UserRepository(connections),
                new SavedQuestionRepository(connections), new CodeSubmissionRepository(connections));
        try {
            Field field = CodingController.class.getDeclaredField("savedContentService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    @Test
    void saveButtonIsDisabledUntilAQuestionIsGenerated() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            assertTrue(controller.buttonSave.isDisabled());
        });
    }

    @Test
    void generatedQuestionCanBeSavedAsCodingWithSelectedDifficulty() throws Exception {
        CodingController[] holder = new CodingController[1];
        SavedContentService[] serviceHolder = new SavedContentService[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            holder[0] = controller;
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            setQuestionService(controller, new FakeQuestionService("Reverse a list"));
            serviceHolder[0] = useTestSavedContentService(controller);
            controller.comboDifficulty.setValue(Difficulty.EASY);
            controller.questionOutput.textProperty().addListener((o, oldValue, newValue) -> {
                if ("Reverse a list".equals(newValue)) {
                    completed.countDown();
                }
            });
            controller.onGenerateQuestion();
        });
        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertFalse(holder[0].buttonSave.isDisabled());

            holder[0].onSave();

            var saved = serviceHolder[0].listSavedQuestions("alice");
            assertEquals(1, saved.size());
            assertEquals("Reverse a list", saved.get(0).questionText());
            assertEquals("CODING", saved.get(0).questionType());
            assertEquals("EASY", saved.get(0).difficulty());
        });
    }

    @Test
    void saveButtonAlsoStoresTheCodeFromTheEditor() throws Exception {
        CodingController[] holder = new CodingController[1];
        SavedContentService[] serviceHolder = new SavedContentService[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            holder[0] = controller;
            FakeSceneManager sceneManager = new FakeSceneManager();
            sceneManager.setCurrentUsername("alice");
            controller.setSceneManager(sceneManager);
            setQuestionService(controller, new FakeQuestionService("Reverse a list"));
            serviceHolder[0] = useTestSavedContentService(controller);
            controller.questionOutput.textProperty().addListener((o, oldValue, newValue) -> {
                if ("Reverse a list".equals(newValue)) {
                    completed.countDown();
                }
            });
            controller.onGenerateQuestion();
        });
        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            holder[0].codeEditor.replaceText("int x = 1;");

            holder[0].onSave();

            var submissions = serviceHolder[0].listSubmissions("alice");
            assertEquals(1, submissions.size());
            assertEquals("int x = 1;", submissions.get(0).code());
            assertEquals("java", submissions.get(0).language());
        });
    }

    @Test
    void onSavedQuestions_switchesToSavedScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onSavedQuestions();

            assertEquals("saved", sceneManager.lastScene);
        });
    }

    @Test
    void submittingAnAnswerResetsTheSaveButton() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager(new FakeFeedbackController()));
            controller.buttonSave.setDisable(false);

            controller.runEvaluation();

            assertTrue(controller.buttonSave.isDisabled());
        });
    }

    @Test
    void setSceneManager_buildsCodeEditorWithVisiblePlaceholderInitially() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            assertEquals("", controller.codeEditor.getText());
            assertTrue(controller.codePlaceholder.isVisible());
            assertTrue(controller.codeEditorContainer.getChildren().contains(controller.codePlaceholder));
        });
    }

    @Test
    void setSceneManager_submitButtonDisabledWhenCodeIsEmpty() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void typingCodeEnablesSubmitButton() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.codeEditor.replaceText("int x;");

            assertFalse(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void whitespaceOnlyCodeKeepsSubmitButtonDisabled() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.codeEditor.replaceText("   \n  ");

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void clearingCodeDisablesSubmitButtonAgain() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.codeEditor.replaceText("int x;");
            assertFalse(controller.buttonSubmitAnswer.isDisabled());

            controller.codeEditor.clear();

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void typingInCodeEditorHidesPlaceholderAndAppliesHighlighting() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.codeEditor.replaceText("public class Foo {}");

            assertFalse(controller.codePlaceholder.isVisible());
            assertTrue(
                controller.codeEditor.getStyleSpans(0, controller.codeEditor.getLength())
                    .styleStream()
                    .anyMatch(style -> style.contains("code-keyword"))
            );
        });
    }

    @Test
    void clearingCodeEditorShowsPlaceholderAgain() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.codeEditor.replaceText("int x;");
            assertFalse(controller.codePlaceholder.isVisible());

            controller.codeEditor.clear();

            assertTrue(controller.codePlaceholder.isVisible());
        });
    }



    @Test
    void onPractice_switchesToPracticeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onPractice();

            assertEquals("practice", sceneManager.lastScene);
        });
    }

    @Test
    void runEvaluation_switchesToFeedbackScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertEquals("feedback", sceneManager.lastScene);
        });
    }

    @Test
    void runEvaluation_passesControlsAndCodingReturnSceneToFeedbackController() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeFeedbackController feedbackController = new FakeFeedbackController();
            FakeSceneManager sceneManager = new FakeSceneManager(feedbackController);
            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertEquals(controller.questionOutput.getText(), feedbackController.receivedQuestion);
            assertEquals(controller.codeEditor.getText(), feedbackController.receivedCode);
            assertEquals("", feedbackController.receivedExplanation);
            assertEquals("coding", feedbackController.receivedReturnScene);
        });
    }

    @Test
    void runEvaluation_callsFeedbackRunEvaluation() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeFeedbackController feedbackController = new FakeFeedbackController();
            FakeSceneManager sceneManager = new FakeSceneManager(feedbackController);
            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertTrue(feedbackController.evaluationCalled);
        });
    }

    @Test
    void runEvaluation_whenFeedbackControllerMissing_doesNotCrash() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertEquals("feedback", sceneManager.lastScene);
        });
    }

    @Test
    void onSubmitAnswer_startsEvaluationFlow() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeFeedbackController feedbackController = new FakeFeedbackController();
            FakeSceneManager sceneManager = new FakeSceneManager(feedbackController);
            controller.setSceneManager(sceneManager);

            controller.onSubmitAnswer();

            assertEquals("feedback", sceneManager.lastScene);
            assertTrue(feedbackController.evaluationCalled);
        });
    }

    @Test
    void onGenerateQuestion_clearsCodeEditorAndShowsPlaceholderAgain() throws Exception {
        BlockingQuestionService service = new BlockingQuestionService();

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.codeEditor.replaceText("public int[] mySolution() { return null; }");
            assertFalse(controller.codePlaceholder.isVisible());
            assertFalse(controller.buttonSubmitAnswer.isDisabled());

            controller.onGenerateQuestion();

            assertEquals("", controller.codeEditor.getText());
            assertTrue(controller.codePlaceholder.isVisible());
            assertTrue(controller.buttonSubmitAnswer.isDisabled());
        });

        service.release();
    }

    @Test
    void onGenerateQuestion_showsLoadingState() throws Exception {
        BlockingQuestionService service = new BlockingQuestionService();
        CodingController[] holder = new CodingController[1];

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            holder[0] = controller;

            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.onGenerateQuestion();

            assertEquals("Generating question...", controller.questionOutput.getText());
            assertTrue(controller.buttonGenerateQuestion.isDisabled());
        });

        service.release();
    }

    @Test
    void onGenerateQuestion_successDisplaysQuestion() throws Exception {
        FakeQuestionService service = new FakeQuestionService("Title: Two Sum\nDifficulty: Easy");
        CodingController[] holder = new CodingController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            holder[0] = controller;

            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.questionOutput.textProperty().addListener((observable, oldValue, newValue) -> {
                if ("Title: Two Sum\nDifficulty: Easy".equals(newValue)) {
                    completed.countDown();
                }
            });

            controller.onGenerateQuestion();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertEquals("Title: Two Sum\nDifficulty: Easy", holder[0].questionOutput.getText());
            assertFalse(holder[0].buttonGenerateQuestion.isDisabled());
        });
    }

    @Test
    void onGenerateQuestion_passesSelectedOptions() throws Exception {
        RecordingQuestionService service = new RecordingQuestionService();
        CountDownLatch completed = service.completed;

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);
            controller.comboTopic.setValue("Graphs");
            controller.comboDifficulty.setValue(Difficulty.HARD);

            controller.onGenerateQuestion();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));
        assertEquals(QuestionType.CODING, service.receivedType);
        assertEquals("Graphs", service.receivedTopic);
        assertEquals(Difficulty.HARD, service.receivedDifficulty);
    }

    @Test
    void onGenerateQuestion_failureDisplaysError() throws Exception {
        FailingQuestionService service = new FailingQuestionService();
        CodingController[] holder = new CodingController[1];
        CountDownLatch failed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            holder[0] = controller;

            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.questionOutput.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.startsWith("Failed to generate question:")) {
                    failed.countDown();
                }
            });

            controller.onGenerateQuestion();
        });

        assertTrue(failed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertEquals("Failed to generate question: Test API failure", holder[0].questionOutput.getText());
            assertFalse(holder[0].buttonGenerateQuestion.isDisabled());
        });
    }

    @Test
    void onSceneShown_withLoggedInUser_showsUsername() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);
            sceneManager.setCurrentUsername("gabriel");

            controller.onSceneShown();

            assertEquals("Logged in as gabriel", controller.labelLoggedInAs.getText());
        });
    }

    @Test
    void onSceneShown_withNoLoggedInUser_showsEmptyLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.onSceneShown();

            assertEquals("", controller.labelLoggedInAs.getText());
        });
    }

    @Test
    void onLogOut_clearsUsernameAndNavigatesHome() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);
            sceneManager.setCurrentUsername("gabriel");

            controller.onLogOut();

            assertNull(sceneManager.getCurrentUsername());
            assertEquals("home", sceneManager.lastScene);
        });
    }

    private static class FakeSceneManager extends SceneManager {
        String lastScene;
        private final Object feedbackController;

        FakeSceneManager() {
            this(null);
        }

        FakeSceneManager(Object feedbackController) {
            super(new Stage());
            this.feedbackController = feedbackController;
        }

        @Override
        public void switchToScene(String sceneName) {
            lastScene = sceneName;
        }

        @Override
        public Object getController(String sceneName) {
            if ("feedback".equals(sceneName)) {
                return feedbackController;
            }
            return null;
        }
    }

    private static class FakeFeedbackController extends FeedbackController {
        String receivedQuestion;
        String receivedCode;
        String receivedExplanation;
        String receivedReturnScene;

        boolean evaluationCalled = false;

        @Override
        public void setAnswerControls(
                String question,
                String code,
                String explanation,
                String returnScene) {

            this.receivedQuestion = question;
            this.receivedCode = code;
            this.receivedExplanation = explanation;
            this.receivedReturnScene = returnScene;
        }

        @Override
        public void runEvaluation() {
            evaluationCalled = true;
        }
    }

    private static class BlockingQuestionService extends OpenAiQuestionService {
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public String generateQuestion(QuestionType type, Difficulty difficulty, String topic) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return "Generated question";
        }

        void release() {
            latch.countDown();
        }
    }

    private static class FakeQuestionService extends OpenAiQuestionService {
        private final String result;

        FakeQuestionService(String result) {
            this.result = result;
        }

        @Override
        public String generateQuestion(QuestionType type, Difficulty difficulty, String topic) {
            return result;
        }
    }

    private static class RecordingQuestionService extends OpenAiQuestionService {
        QuestionType receivedType;
        Difficulty receivedDifficulty;
        String receivedTopic;
        CountDownLatch completed = new CountDownLatch(1);

        @Override
        public String generateQuestion(QuestionType type, Difficulty difficulty, String topic) {
            receivedType = type;
            receivedDifficulty = difficulty;
            receivedTopic = topic;
            completed.countDown();
            return "Test question";
        }
    }

    private static class FailingQuestionService extends OpenAiQuestionService {
        @Override
        public String generateQuestion(QuestionType type, Difficulty difficulty, String topic) {
            throw new RuntimeException("Test API failure");
        }
    }
}
