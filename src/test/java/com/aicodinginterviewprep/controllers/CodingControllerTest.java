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
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.service.OpenAiQuestionService;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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
        controller.codeEditor = new TextArea();

        controller.buttonReturn = new Button();
        controller.buttonSubmitAnswer = new Button();
        controller.buttonGenerateQuestion = new Button();
        controller.buttonPractice = new Button();

        return controller;
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

            assertEquals(controller.questionOutput, feedbackController.receivedQuestionOutput);
            assertEquals(controller.codeEditor, feedbackController.receivedCodeEditor);
            assertNull(feedbackController.receivedAnswerInput);
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
    void onGenerateQuestion_resetsCodeEditorToStarterCode() throws Exception {
        BlockingQuestionService service = new BlockingQuestionService();

        runOnFxThreadAndWait(() -> {
            CodingController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.codeEditor.setText("public int[] mySolution() { return null; }");

            controller.onGenerateQuestion();

            assertEquals("// Write your code here", controller.codeEditor.getText());
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
        TextArea receivedQuestionOutput;
        TextArea receivedCodeEditor;
        javafx.scene.control.TextInputControl receivedAnswerInput;
        String receivedReturnScene;

        boolean evaluationCalled = false;

        @Override
        public void setAnswerControls(
                TextArea questionOutput,
                TextArea codeEditor,
                javafx.scene.control.TextInputControl answerInput,
                String returnScene) {

            this.receivedQuestionOutput = questionOutput;
            this.receivedCodeEditor = codeEditor;
            this.receivedAnswerInput = answerInput;
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
