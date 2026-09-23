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
import com.aicodinginterviewprep.service.OpenAiQuestionService;

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

        controller.buttonReturn = new Button();
        controller.buttonSubmitAnswer = new Button();
        controller.buttonGenerateQuestion = new Button();
        controller.buttonPractice = new Button();
        controller.labelLoggedInAs = new Label();
        controller.buttonLogOut = new Button();
        controller.comboDifficulty = new ComboBox<>();

        return controller;
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
    void onReturn_switchesToHomeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onReturn();

            assertEquals("home", sceneManager.lastScene);
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
    void onGenerateQuestion_alwaysRequestsCodingQuestionType() throws Exception {
        RecordingQuestionService service = new RecordingQuestionService();
        CountDownLatch completed = service.completed;

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.onGenerateQuestion();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));
        assertEquals(QuestionType.CODING, service.receivedType);
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
        public String generateQuestion(QuestionType type) {
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
        public String generateQuestion(QuestionType type) {
            return result;
        }
    }

    private static class RecordingQuestionService extends OpenAiQuestionService {
        QuestionType receivedType;
        CountDownLatch completed = new CountDownLatch(1);

        @Override
        public String generateQuestion(QuestionType type) {
            receivedType = type;
            completed.countDown();
            return "Test question";
        }
    }

    private static class FailingQuestionService extends OpenAiQuestionService {
        @Override
        public String generateQuestion(QuestionType type) {
            throw new RuntimeException("Test API failure");
        }
    }
}
