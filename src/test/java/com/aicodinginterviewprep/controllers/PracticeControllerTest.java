package com.aicodinginterviewprep.controllers;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.SceneManager;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
    }

    @Test
    void setSceneManager_addsAllQuestionTypes() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);

            assertEquals(
                    QuestionType.values().length,
                    controller.comboQuestionType.getItems().size()
            );
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

            assertEquals(
                    controller.codeEditor,
                    feedbackController.receivedCodeEditor
            );

            assertEquals(
                    controller.answerInput,
                    feedbackController.receivedAnswerInput
            );
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

            assertEquals(true, feedbackController.evaluationCalled);
        });
    }

    private PracticeController createController() {
        PracticeController controller = new PracticeController();

        controller.comboQuestionType = new ComboBox<>();
        controller.questionOutput = new TextArea();
        controller.codeEditor = new TextArea();
        controller.answerInput = new TextField();

        controller.buttonReturn = new Button();
        controller.buttonSubmitAnswer = new Button();
        controller.buttonRunEvaluation = new Button();
        controller.buttonGenerateQuestion = new Button();

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

    private static class FakeFeedbackController
        extends FeedbackController {

        TextArea receivedQuestionOutput;
        TextArea receivedCodeEditor;
        TextField receivedAnswerInput;

        boolean evaluationCalled = false;

        @Override
        public void setAnswerControls(
                TextArea questionOutput,
                TextArea codeEditor,
                javafx.scene.control.TextInputControl answerInput) {

            this.receivedQuestionOutput = questionOutput;
            this.receivedCodeEditor = codeEditor;

            if (answerInput instanceof TextField textField) {
                this.receivedAnswerInput = textField;
            }
        }

        @Override
        public void runEvaluation() {
            evaluationCalled = true;
        }
    }
}