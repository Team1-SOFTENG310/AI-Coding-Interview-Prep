package com.aicodinginterviewprep.controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.aicodinginterviewprep.EvaluatorService;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.openai.EvaluationResult;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

class FeedbackControllerTest {

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

        if (error.get() != null) {
            if (error.get() instanceof AssertionError assertionError) {
                throw assertionError;
            }
            throw new RuntimeException(error.get());
        }
    }

    @Test
    void setSceneManager_storesSceneManager() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.onTryAgain();

            assertEquals("practice", sceneManager.lastScene);
        });
    }

    @Test
    void setAnswerControls_storesValues() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();

            controller.setAnswerControls("What is Java?", "int x;", "Explanation");

            assertEquals("What is Java?", getPrivateField(controller, "question"));
            assertEquals("int x;", getPrivateField(controller, "code"));
            assertEquals("Explanation", getPrivateField(controller, "explanation"));
        });
    }

    @Test
    void onTryAgain_switchesToPracticeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.onTryAgain();

            assertEquals("practice", sceneManager.lastScene);
        });
    }

    @Test
    void onQuit_switchesToHomeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.onQuit();

            assertEquals("home", sceneManager.lastScene);
        });
    }

    @Test
    void onQuit_afterCodingEvaluation_stillSwitchesToHomeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.setAnswerControls("", "", "", "coding");
            controller.onQuit();

            assertEquals("home", sceneManager.lastScene);
        });
    }

    @Test
    void setAnswerControls_withReturnScene_storesValuesAndReturnScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();

            controller.setAnswerControls("What is Java?", "int x;", "Explanation", "coding");

            assertEquals("What is Java?", getPrivateField(controller, "question"));
            assertEquals("int x;", getPrivateField(controller, "code"));
            assertEquals("Explanation", getPrivateField(controller, "explanation"));
            assertEquals("coding", getPrivateField(controller, "returnScene"));
        });
    }

    @Test
    void onTryAgain_afterCodingEvaluation_switchesBackToCodingScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);
            controller.setAnswerControls("", "", "", "coding");
            controller.onTryAgain();

            assertEquals("coding", sceneManager.lastScene);
        });
    }

    @Test
    void runEvaluation_whenQuestionIsEmpty_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            controller.setAnswerControls("", "", "");

            controller.runEvaluation();

            assertTrue(controller.correctnessTextArea.getText()
                    .contains("Please generate a question first"));
        });
    }

    @Test
    void runEvaluation_whenQuestionIsDefault_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();

            controller.setAnswerControls("Question will appear here.", "", "");
            controller.runEvaluation();

            assertTrue(controller.correctnessTextArea.getText()
                    .contains("Please generate a question first"));
        });
    }

    @Test
    void runEvaluation_whenAnswerIsEmpty_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();

            controller.setAnswerControls("What is Java?", "", "");
            controller.runEvaluation();

            assertTrue(controller.correctnessTextArea.getText()
                    .contains("Please provide an answer explanation or code solution"));
        });
    }

    @Test
    void runEvaluation_showsLoadingMessage() throws Exception {
        BlockingEvaluatorService service = new BlockingEvaluatorService();

        FeedbackController[] holder = new FeedbackController[1];

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("What is Java?", "", "Java is a programming language");
            setEvaluatorService(controller, service);

            controller.runEvaluation();

            assertTrue(controller.correctnessTextArea.getText()
                    .contains("Evaluating your response with AI"));
        });

        service.release();
    }

    @Test
    void runEvaluation_disablesButtonsDuringEvaluation() throws Exception {
        BlockingEvaluatorService service = new BlockingEvaluatorService();

        FeedbackController[] holder = new FeedbackController[1];

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("What is Java?", "", "Java is a programming language");
            setEvaluatorService(controller, service);

            controller.runEvaluation();

            assertTrue(controller.buttonTryAgain.isDisabled());
            assertTrue(controller.buttonQuit.isDisabled());
        });

        service.release();
    }

    @Test
    void runEvaluation_successDisplaysResultWithRating() throws Exception {
        FakeEvaluatorService service = new FakeEvaluatorService(
                new EvaluationResult(1,2, 3, 4, "correctness", "efficiency", "communication", "code_quality")
        );

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("What is Java?", "", "Java is a programming language");
            setEvaluatorService(controller, service);

            controller.correctnessTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 1")) {
                            completed.countDown();
                        }
                    });
            controller.efficiencyTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 2")) {
                            completed.countDown();
                        }
                    });
            controller.communicationTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 3")) {
                            completed.countDown();
                        }
                    });
            controller.codeQualityTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 4")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertTrue(holder[0].correctnessTextArea.getText().contains("Rating: 1"));
            assertTrue(holder[0].correctnessTextArea.getText().contains("correctness"));
            assertTrue(holder[0].efficiencyTextArea.getText().contains("Rating: 2"));
            assertTrue(holder[0].efficiencyTextArea.getText().contains("efficiency"));
            assertTrue(holder[0].communicationTextArea.getText().contains("Rating: 3"));
            assertTrue(holder[0].communicationTextArea.getText().contains("communication"));
            assertTrue(holder[0].codeQualityTextArea.getText().contains("Rating: 4"));
            assertTrue(holder[0].codeQualityTextArea.getText().contains("code_quality"));

        });
    }

    @Test
    void runEvaluation_successEnablesButtons() throws Exception {
        FakeEvaluatorService service = new FakeEvaluatorService(
                new EvaluationResult(7, 0, 0, 0, "Good effort", "", "", "")
        );

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("What is Java?", "", "Java is a programming language");
            setEvaluatorService(controller, service);

            controller.correctnessTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 7")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertFalse(holder[0].buttonTryAgain.isDisabled());
            assertFalse(holder[0].buttonQuit.isDisabled());
        });
    }

    @Test
    void runEvaluation_failureDisplaysErrorMessage() throws Exception {
        FailingEvaluatorService service = new FailingEvaluatorService(
                "Network connection failed"
        );

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("What is Java?", "", "Java is a programming language");
            setEvaluatorService(controller, service);

            controller.correctnessTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Evaluation failed")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertTrue(holder[0].correctnessTextArea.getText()
                    .contains("Evaluation failed: Network connection failed"));
        });
    }

    @Test
    void runEvaluation_failureEnablesButtons() throws Exception {
        FailingEvaluatorService service = new FailingEvaluatorService(
                "API error"
        );

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("What is Java?", "", "Java is a programming language");
            setEvaluatorService(controller, service);

            controller.correctnessTextArea.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Evaluation failed")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertFalse(holder[0].buttonTryAgain.isDisabled());
            assertFalse(holder[0].buttonQuit.isDisabled());
        });
    }

    @Test
    void runEvaluation_withBothExplanationAndCode_combinesToAnswer() throws Exception {
        RecordingEvaluatorService service = new RecordingEvaluatorService();

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls(
                "Write a function",
                "public int add(int a, int b) { return a + b; }",
                "This function adds two numbers"
            );
            setEvaluatorService(controller, service);

            service.completed.addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() >= 1) {
                    completed.countDown();
                }
            });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        assertTrue(service.receivedAnswer.contains("This function adds two numbers"));
        assertTrue(service.receivedAnswer.contains("public int add(int a, int b)"));
        assertTrue(service.receivedAnswer.contains("Code:"));
    }

    @Test
    void runEvaluation_withOnlyExplanation_sendsOnlyExplanation() throws Exception {
        RecordingEvaluatorService service = new RecordingEvaluatorService();

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("Explain OOP", "", "OOP is about objects and classes");
            setEvaluatorService(controller, service);

            service.completed.addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() >= 1) {
                    completed.countDown();
                }
            });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        assertEquals("OOP is about objects and classes", service.receivedAnswer);
    }

    @Test
    void runEvaluation_withOnlyCode_sendsOnlyCode() throws Exception {
        RecordingEvaluatorService service = new RecordingEvaluatorService();

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            controller.setAnswerControls("Write a loop", "for (int i = 0; i < 10; i++) {}", "");
            setEvaluatorService(controller, service);

            service.completed.addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() >= 1) {
                    completed.countDown();
                }
            });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        assertEquals("for (int i = 0; i < 10; i++) {}", service.receivedAnswer);
    }

    @Test
    void runEvaluation_whenTextareaEvaluationIsNull_doesNotCrash() throws Exception {
        FakeEvaluatorService service = new FakeEvaluatorService(
                new EvaluationResult(5 ,0 ,0, 0, "Test", "", "", "")
        );

        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            controller.correctnessTextArea = null;

            controller.setAnswerControls("What is Java?", "", "Java is a language");
            setEvaluatorService(controller, service);

            service.completionLatch.addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() >= 1) {
                    completed.countDown();
                }
            });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));
    }

    private FeedbackController createController() {
        FeedbackController controller = new FeedbackController();
        controller.correctnessTextArea = new TextArea();
        controller.efficiencyTextArea = new TextArea();
        controller.communicationTextArea = new TextArea();
        controller.codeQualityTextArea = new TextArea();
        controller.buttonTryAgain = new Button();
        controller.buttonQuit = new Button();
        return controller;
    }

    private void setEvaluatorService(FeedbackController controller, EvaluatorService service) {
        try {
            java.lang.reflect.Field field = FeedbackController.class.getDeclaredField("evaluatorService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getPrivateField(FeedbackController controller, String fieldName) {
        try {
            java.lang.reflect.Field field = FeedbackController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
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

    private static class BlockingEvaluatorService extends EvaluatorService {
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public CompletableFuture<EvaluationResult> evaluateAnswerAsync(String question, String answer) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return new EvaluationResult(5, 0, 0, 0, "Test", "", "", "");
            });
        }

        void release() {
            latch.countDown();
        }
    }

    private static class FakeEvaluatorService extends EvaluatorService {
        private final EvaluationResult result;
        final javafx.beans.property.IntegerProperty completionLatch = new javafx.beans.property.SimpleIntegerProperty(0);

        FakeEvaluatorService(EvaluationResult result) {
            this.result = result;
        }

        @Override
        public CompletableFuture<EvaluationResult> evaluateAnswerAsync(String question, String answer) {
            return CompletableFuture.supplyAsync(() -> {
                completionLatch.setValue(completionLatch.getValue() + 1);
                return result;
            });
        }
    }

    private static class FailingEvaluatorService extends EvaluatorService {
        private final String errorMessage;

        FailingEvaluatorService(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public CompletableFuture<EvaluationResult> evaluateAnswerAsync(String question, String answer) {
            return CompletableFuture.failedFuture(new RuntimeException(errorMessage));
        }
    }

    private static class RecordingEvaluatorService extends EvaluatorService {
        String receivedQuestion;
        String receivedAnswer;
        final javafx.beans.property.IntegerProperty completed = new javafx.beans.property.SimpleIntegerProperty(0);

        @Override
        public CompletableFuture<EvaluationResult> evaluateAnswerAsync(String question, String answer) {
            receivedQuestion = question;
            receivedAnswer = answer;
            completed.setValue(completed.getValue() + 1);
            return CompletableFuture.completedFuture(new EvaluationResult(5, 0, 0, 0, "Test", "", "", ""));
        }
    }
}
