package com.aicodinginterviewprep.controllers;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        FakeSceneManager() {
            super(new Stage());
        }

        @Override
        public void switchToScene(String sceneName) {
            lastScene = sceneName;
        }

        @Override
        public Object getController(String sceneName) {
            return null;
        }
    }
}