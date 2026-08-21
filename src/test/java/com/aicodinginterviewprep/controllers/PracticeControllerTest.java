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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

class PracticeControllerTest {

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

    private void setQuestionService(
        PracticeController controller,
        OpenAiQuestionService service) {

        try {
            Field field =
                    PracticeController.class.getDeclaredField("questionService");

            field.setAccessible(true);
            field.set(controller, service);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void setSceneManager_addsAllQuestionTypes() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);

            assertEquals(
                    2,
                    controller.comboQuestionType.getItems().size()
            );
            assertTrue(controller.comboQuestionType.getItems().contains(QuestionType.BEHAVIOURAL));
            assertTrue(controller.comboQuestionType.getItems().contains(QuestionType.THEORY));
        });
    }

    @Test
    void setSceneManager_setsBehaviouralAsDefault() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);

            assertEquals(
                    QuestionType.BEHAVIOURAL,
                    controller.comboQuestionType.getValue()
            );
        });
    }

    @Test
    void onReturn_switchesToHomeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.onReturn();

            assertEquals("home", sceneManager.lastScene);
        });
    }

    @Test
    void onCodingPractice_switchesToCodingScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.onCodingPractice();

            assertEquals("coding", sceneManager.lastScene);
        });
    }

    @Test
    void runEvaluation_switchesToFeedbackScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.runEvaluation();

            assertEquals("feedback", sceneManager.lastScene);
        });
    }

    @Test
    void runEvaluation_passesControlsToFeedbackController() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();

            FakeFeedbackController feedbackController =
                    new FakeFeedbackController();

            FakeSceneManager sceneManager =
                    new FakeSceneManager(feedbackController);

            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertEquals(
                    controller.questionOutput,
                    feedbackController.receivedQuestionOutput
            );

            assertNull(feedbackController.receivedCodeEditor);

            assertEquals(
                    controller.answerInput,
                    feedbackController.receivedAnswerInput
            );

            assertEquals("practice", feedbackController.receivedReturnScene);
        });
    }

    @Test
    void runEvaluation_callsFeedbackRunEvaluation() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();

            FakeFeedbackController feedbackController =
                    new FakeFeedbackController();

            FakeSceneManager sceneManager =
                    new FakeSceneManager(feedbackController);

            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertTrue(feedbackController.evaluationCalled);
        });
    }

    @Test
    void runEvaluation_whenFeedbackControllerMissing_doesNotCrash() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertEquals("feedback", sceneManager.lastScene);
        });
    }

    @Test
    void runEvaluation_whenControllerIsWrongType_doesNotCrash() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();

            FakeSceneManager sceneManager =
                    new FakeSceneManager(new Object());

            controller.setSceneManager(sceneManager);

            controller.runEvaluation();

            assertEquals("feedback", sceneManager.lastScene);
        });
    }

    private PracticeController createController() {
        PracticeController controller = new PracticeController();

        controller.comboQuestionType = new ComboBox<>();
        controller.questionOutput = new TextArea();
        controller.answerInput = new TextArea();

        controller.buttonReturn = new Button();
        controller.buttonSubmitAnswer = new Button();
        controller.buttonGenerateQuestion = new Button();
        controller.buttonCodingPractice = new Button();

        return controller;
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

    @Test
    void onSubmitAnswer_startsEvaluationFlow() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();

            FakeFeedbackController feedbackController =
                    new FakeFeedbackController();

            FakeSceneManager sceneManager =
                    new FakeSceneManager(feedbackController);

            controller.setSceneManager(sceneManager);

            controller.onSubmitAnswer();

            assertEquals("feedback", sceneManager.lastScene);
            assertTrue(feedbackController.evaluationCalled);
        });
    }

    @Test
    void onGenerateQuestion_clearsPreviousAnswer() throws Exception {
        BlockingQuestionService service = new BlockingQuestionService();

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);
            setQuestionService(controller, service);

            controller.answerInput.setText("My old answer from the previous question");

            controller.onGenerateQuestion();

            assertEquals("", controller.answerInput.getText());
        });

        service.release();
    }

    @Test
    void onGenerateQuestion_showsLoadingState() throws Exception {

        BlockingQuestionService service =
                new BlockingQuestionService();

        PracticeController[] holder =
                new PracticeController[1];

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;

            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            setQuestionService(controller, service);

            controller.comboQuestionType.setValue(
                    QuestionType.BEHAVIOURAL
            );

            controller.onGenerateQuestion();

            assertEquals(
                    "Generating question...",
                    controller.questionOutput.getText()
            );

            assertTrue(
                    controller.buttonGenerateQuestion.isDisabled()
            );
        });

        service.release();
    }

    @Test
    void onGenerateQuestion_successDisplaysQuestion() throws Exception {

        FakeQuestionService service =
                new FakeQuestionService(
                        "Tell me about a difficult problem you solved."
                );

        PracticeController[] holder =
                new PracticeController[1];

        CountDownLatch completed =
                new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;

            controller.setSceneManager(
                    new FakeSceneManager()
            );

            setQuestionService(controller, service);

            controller.questionOutput.textProperty()
                    .addListener((observable, oldValue, newValue) -> {

                        if ("Tell me about a difficult problem you solved."
                                .equals(newValue)) {

                            completed.countDown();
                        }
                    });

            controller.onGenerateQuestion();
        });

        assertTrue(
                completed.await(5, TimeUnit.SECONDS)
        );

        runOnFxThreadAndWait(() -> {

            assertEquals(
                    "Tell me about a difficult problem you solved.",
                    holder[0].questionOutput.getText()
            );

            assertFalse(
                    holder[0].buttonGenerateQuestion.isDisabled()
            );
        });
    }

    @Test
    void onGenerateQuestion_passesSelectedQuestionType()
            throws Exception {

        RecordingQuestionService service =
                new RecordingQuestionService();

        CountDownLatch completed =
                service.completed;

        runOnFxThreadAndWait(() -> {
            PracticeController controller =
                    createController();

            controller.setSceneManager(
                    new FakeSceneManager()
            );

            setQuestionService(controller, service);

            controller.comboQuestionType.setValue(
                    QuestionType.BEHAVIOURAL
            );

            controller.onGenerateQuestion();
        });

        assertTrue(
                completed.await(5, TimeUnit.SECONDS)
        );

        assertEquals(
                QuestionType.BEHAVIOURAL,
                service.receivedType
        );
    }

    @Test
    void onGenerateQuestion_failureDisplaysError()
            throws Exception {

        FailingQuestionService service =
                new FailingQuestionService();

        PracticeController[] holder =
                new PracticeController[1];

        CountDownLatch failed =
                new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {

            PracticeController controller =
                    createController();

            holder[0] = controller;

            controller.setSceneManager(
                    new FakeSceneManager()
            );

            setQuestionService(controller, service);

            controller.questionOutput.textProperty()
                    .addListener((observable, oldValue, newValue) -> {

                        if (newValue.startsWith(
                                "Failed to generate question:")) {

                            failed.countDown();
                        }
                    });

            controller.onGenerateQuestion();
        });

        assertTrue(
                failed.await(5, TimeUnit.SECONDS)
        );

        runOnFxThreadAndWait(() -> {

            assertEquals(
                    "Failed to generate question: Test API failure",
                    holder[0].questionOutput.getText()
            );

            assertFalse(
                    holder[0].buttonGenerateQuestion.isDisabled()
            );
        });
    }

    private static class FakeFeedbackController
        extends FeedbackController {

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

    private static class BlockingQuestionService
        extends OpenAiQuestionService {

        private final CountDownLatch latch =
                new CountDownLatch(1);

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

    private static class FakeQuestionService
        extends OpenAiQuestionService {

        private final String result;

        FakeQuestionService(String result) {
            this.result = result;
        }

        @Override
        public String generateQuestion(QuestionType type) {
            return result;
        }
    }

    private static class RecordingQuestionService
        extends OpenAiQuestionService {

        QuestionType receivedType;

        CountDownLatch completed =
                new CountDownLatch(1);

        @Override
        public String generateQuestion(QuestionType type) {

            receivedType = type;
            completed.countDown();

            return "Test question";
        }
    }

    private static class FailingQuestionService
        extends OpenAiQuestionService {

        @Override
        public String generateQuestion(QuestionType type) {
            throw new RuntimeException(
                    "Test API failure"
            );
        }
    }
}