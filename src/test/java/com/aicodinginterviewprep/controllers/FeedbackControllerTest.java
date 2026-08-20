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
import javafx.scene.control.TextField;
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
    void setAnswerControls_storesReferences() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            TextArea questionOutput = new TextArea();
            TextArea codeEditor = new TextArea();
            TextField answerInput = new TextField();

            controller.setAnswerControls(questionOutput, codeEditor, answerInput);

            assertEquals(questionOutput, getPrivateField(controller, "questionOutput"));
            assertEquals(codeEditor, getPrivateField(controller, "codeEditor"));
            assertEquals(answerInput, getPrivateField(controller, "answerInput"));
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
    void runEvaluation_whenQuestionIsEmpty_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            controller.setAnswerControls(new TextArea(), new TextArea(), new TextField());

            controller.runEvaluation();

            assertTrue(controller.textareaEvaluation.getText()
                    .contains("Please generate a question first"));
        });
    }

    @Test
    void runEvaluation_whenQuestionIsDefault_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            TextArea questionOutput = new TextArea();
            questionOutput.setText("Question will appear here.");

            controller.setAnswerControls(questionOutput, new TextArea(), new TextField());
            controller.runEvaluation();

            assertTrue(controller.textareaEvaluation.getText()
                    .contains("Please generate a question first"));
        });
    }

    @Test
    void runEvaluation_whenAnswerIsEmpty_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");

            controller.setAnswerControls(questionOutput, new TextArea(), new TextField());
            controller.runEvaluation();

            assertTrue(controller.textareaEvaluation.getText()
                    .contains("Please provide an answer explanation or code solution"));
        });
    }

    @Test
    void runEvaluation_whenAnswerIsDefaultExplanation_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Enter your solution explanation...");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            controller.runEvaluation();

            assertTrue(controller.textareaEvaluation.getText()
                    .contains("Please provide an answer explanation or code solution"));
        });
    }

    @Test
    void runEvaluation_whenAnswerIsDefaultCode_showsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextArea codeEditor = new TextArea();
            codeEditor.setText("// Write your code here");

            controller.setAnswerControls(questionOutput, codeEditor, new TextField());
            controller.runEvaluation();

            assertTrue(controller.textareaEvaluation.getText()
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a programming language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            setEvaluatorService(controller, service);

            controller.runEvaluation();

            assertTrue(controller.textareaEvaluation.getText()
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a programming language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            setEvaluatorService(controller, service);

            controller.runEvaluation();

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
            assertTrue(controller.buttonTryAgain.isDisabled());
        });

        service.release();
    }

    @Test
    void runEvaluation_successDisplaysResultWithRating() throws Exception {
        FakeEvaluatorService service = new FakeEvaluatorService(
                new EvaluationResult("Your answer was good", 8)
        );

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a programming language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            setEvaluatorService(controller, service);

            controller.textareaEvaluation.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 8")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertTrue(holder[0].textareaEvaluation.getText().contains("Rating: 8"));
            assertTrue(holder[0].textareaEvaluation.getText().contains("Your answer was good"));
        });
    }

    @Test
    void runEvaluation_successEnablesButtons() throws Exception {
        FakeEvaluatorService service = new FakeEvaluatorService(
                new EvaluationResult("Good effort", 7)
        );

        FeedbackController[] holder = new FeedbackController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            holder[0] = controller;

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a programming language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            setEvaluatorService(controller, service);

            controller.textareaEvaluation.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Rating: 7")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertFalse(holder[0].buttonSubmitAnswer.isDisabled());
            assertFalse(holder[0].buttonTryAgain.isDisabled());
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a programming language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            setEvaluatorService(controller, service);

            controller.textareaEvaluation.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Evaluation failed")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertTrue(holder[0].textareaEvaluation.getText()
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a programming language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
            setEvaluatorService(controller, service);

            controller.textareaEvaluation.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue.contains("Evaluation failed")) {
                            completed.countDown();
                        }
                    });

            controller.runEvaluation();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertFalse(holder[0].buttonSubmitAnswer.isDisabled());
            assertFalse(holder[0].buttonTryAgain.isDisabled());
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("Write a function");
            TextArea codeEditor = new TextArea();
            codeEditor.setText("public int add(int a, int b) { return a + b; }");
            TextField answerInput = new TextField();
            answerInput.setText("This function adds two numbers");

            controller.setAnswerControls(questionOutput, codeEditor, answerInput);
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("Explain OOP");
            TextArea codeEditor = new TextArea();
            codeEditor.setText("// Write your code here");
            TextField answerInput = new TextField();
            answerInput.setText("OOP is about objects and classes");

            controller.setAnswerControls(questionOutput, codeEditor, answerInput);
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

            TextArea questionOutput = new TextArea();
            questionOutput.setText("Write a loop");
            TextArea codeEditor = new TextArea();
            codeEditor.setText("for (int i = 0; i < 10; i++) {}");
            TextField answerInput = new TextField();
            answerInput.setText("Enter your solution explanation...");

            controller.setAnswerControls(questionOutput, codeEditor, answerInput);
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
                new EvaluationResult("Test", 5)
        );

        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            FeedbackController controller = createController();
            controller.textareaEvaluation = null;

            TextArea questionOutput = new TextArea();
            questionOutput.setText("What is Java?");
            TextField answerInput = new TextField();
            answerInput.setText("Java is a language");

            controller.setAnswerControls(questionOutput, new TextArea(), answerInput);
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
        controller.textareaEvaluation = new TextArea();
        controller.buttonTryAgain = new Button();
        controller.buttonSubmitAnswer = new Button();
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
                return new EvaluationResult("Test", 5);
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
            return CompletableFuture.completedFuture(new EvaluationResult("Test", 5));
        }
    }
}