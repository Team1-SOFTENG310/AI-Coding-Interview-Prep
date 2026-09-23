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

import com.aicodinginterviewprep.MicrophoneRecorder;
import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import com.aicodinginterviewprep.service.SpeechToTextService;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;

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
    void setSceneManager_addsAllDifficultyLevelsAndDefaultsToMedium() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();

            controller.setSceneManager(new FakeSceneManager());

            assertEquals(3, controller.comboDifficulty.getItems().size());
            assertEquals(Difficulty.MEDIUM, controller.comboDifficulty.getValue());
        });
    }

    @Test
    void setSceneManager_submitButtonDisabledWhenAnswerIsEmpty() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();

            controller.setSceneManager(sceneManager);

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void typingAnAnswerEnablesSubmitButton() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.answerInput.setText("My explanation");

            assertFalse(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void whitespaceOnlyAnswerKeepsSubmitButtonDisabled() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.answerInput.setText("   ");

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
        });
    }

    @Test
    void clearingTheAnswerDisablesSubmitButtonAgain() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.answerInput.setText("My explanation");
            assertFalse(controller.buttonSubmitAnswer.isDisabled());

            controller.answerInput.clear();

            assertTrue(controller.buttonSubmitAnswer.isDisabled());
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
                    controller.questionOutput.getText(),
                    feedbackController.receivedQuestion
            );

            assertEquals("", feedbackController.receivedCode);

            assertEquals(
                    controller.answerInput.getText(),
                    feedbackController.receivedExplanation
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
        controller.comboDifficulty = new ComboBox<>();
        controller.questionOutput = new TextArea();
        controller.answerInput = new TextArea();

        controller.buttonReturn = new Button();
        controller.buttonSubmitAnswer = new Button();
        controller.buttonGenerateQuestion = new Button();
        controller.buttonCodingPractice = new Button();
        controller.buttonVoiceInput = new Button();
        controller.labelVoiceStatus = new Label();
        controller.labelLoggedInAs = new Label();
        controller.buttonLogOut = new Button();

        return controller;
    }

    private void setMicrophoneRecorder(PracticeController controller, MicrophoneRecorder recorder) {
        try {
            Field field = PracticeController.class.getDeclaredField("microphoneRecorder");
            field.setAccessible(true);
            field.set(controller, recorder);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setSpeechToTextService(PracticeController controller, SpeechToTextService service) {
        try {
            Field field = PracticeController.class.getDeclaredField("speechToTextService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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
            assertFalse(controller.buttonSubmitAnswer.isDisabled());

            controller.onGenerateQuestion();

            assertEquals("", controller.answerInput.getText());
            assertTrue(controller.buttonSubmitAnswer.isDisabled());
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

    @Test
    void setSceneManager_voiceButtonDisabledInitially() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();

            controller.setSceneManager(new FakeSceneManager());

            assertTrue(controller.buttonVoiceInput.isDisabled());
        });
    }

    @Test
    void onGenerateQuestion_successEnablesVoiceButton() throws Exception {
        FakeQuestionService service = new FakeQuestionService("A question");
        PracticeController[] holder = new PracticeController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;

            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);

            controller.questionOutput.textProperty().addListener((observable, oldValue, newValue) -> {
                if ("A question".equals(newValue)) {
                    completed.countDown();
                }
            });

            controller.onGenerateQuestion();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> assertFalse(holder[0].buttonVoiceInput.isDisabled()));
    }

    @Test
    void onVoiceInput_startsRecording_whenNotRecording() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
            setMicrophoneRecorder(controller, recorder);

            controller.onVoiceInput();

            assertTrue(recorder.isRecording());
            assertEquals("Stop Recording", controller.buttonVoiceInput.getText());
            assertTrue(controller.buttonVoiceInput.getStyleClass().contains("recording"));
        });
    }

    @Test
    void onVoiceInput_micUnavailable_showsErrorAndStaysIdle() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
            recorder.throwOnStart = true;
            setMicrophoneRecorder(controller, recorder);

            controller.onVoiceInput();

            assertFalse(recorder.isRecording());
            assertTrue(controller.labelVoiceStatus.getText().contains("Microphone unavailable"));
        });
    }

    @Test
    void onVoiceInput_whenRecording_stopsAndTranscribesIntoAnswer() throws Exception {
        FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
        FakeSpeechToTextService speechService = new FakeSpeechToTextService("Hello from voice");

        PracticeController[] holder = new PracticeController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;
            controller.setSceneManager(new FakeSceneManager());
            controller.answerInput.setDisable(false);
            setMicrophoneRecorder(controller, recorder);
            setSpeechToTextService(controller, speechService);

            controller.answerInput.textProperty().addListener((observable, oldValue, newValue) -> {
                if ("Hello from voice".equals(newValue)) {
                    completed.countDown();
                }
            });

            controller.onVoiceInput();
            controller.onVoiceInput();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertEquals("Hello from voice", holder[0].answerInput.getText());
            assertEquals("Record Answer", holder[0].buttonVoiceInput.getText());
            assertFalse(holder[0].buttonVoiceInput.isDisabled());
        });
    }

    @Test
    void onVoiceInput_appendsToExistingAnswerText() throws Exception {
        FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
        FakeSpeechToTextService speechService = new FakeSpeechToTextService("second part");

        PracticeController[] holder = new PracticeController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;
            controller.setSceneManager(new FakeSceneManager());
            setMicrophoneRecorder(controller, recorder);
            setSpeechToTextService(controller, speechService);
            controller.answerInput.setText("first part");

            controller.answerInput.textProperty().addListener((observable, oldValue, newValue) -> {
                if ("first part second part".equals(newValue)) {
                    completed.countDown();
                }
            });

            controller.onVoiceInput();
            controller.onVoiceInput();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() ->
                assertEquals("first part second part", holder[0].answerInput.getText()));
    }

    @Test
    void onVoiceInput_transcriptionFailure_showsErrorAndResetsButton() throws Exception {
        FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
        FailingSpeechToTextService speechService = new FailingSpeechToTextService("Network error");

        PracticeController[] holder = new PracticeController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;
            controller.setSceneManager(new FakeSceneManager());
            controller.answerInput.setDisable(false);
            setMicrophoneRecorder(controller, recorder);
            setSpeechToTextService(controller, speechService);

            controller.labelVoiceStatus.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.startsWith("Transcription failed")) {
                    completed.countDown();
                }
            });

            controller.onVoiceInput();
            controller.onVoiceInput();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertTrue(holder[0].labelVoiceStatus.getText().contains("Network error"));
            assertEquals("Record Answer", holder[0].buttonVoiceInput.getText());
            assertFalse(holder[0].buttonVoiceInput.isDisabled());
        });
    }

    @Test
    void onVoiceInput_emptyTranscription_showsDidntCatchMessageAndDoesNotTouchAnswer() throws Exception {
        FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
        FakeSpeechToTextService speechService = new FakeSpeechToTextService("");

        PracticeController[] holder = new PracticeController[1];
        CountDownLatch completed = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            holder[0] = controller;
            controller.setSceneManager(new FakeSceneManager());
            controller.answerInput.setDisable(false);
            setMicrophoneRecorder(controller, recorder);
            setSpeechToTextService(controller, speechService);

            controller.labelVoiceStatus.textProperty().addListener((observable, oldValue, newValue) -> {
                if ("Didn't catch that - try again.".equals(newValue)) {
                    completed.countDown();
                }
            });

            controller.onVoiceInput();
            controller.onVoiceInput();
        });

        assertTrue(completed.await(5, TimeUnit.SECONDS));

        runOnFxThreadAndWait(() -> {
            assertEquals("", holder[0].answerInput.getText());
            assertEquals("Record Answer", holder[0].buttonVoiceInput.getText());
            assertFalse(holder[0].buttonVoiceInput.isDisabled());
        });
    }

    @Test
    void onSceneShown_withLoggedInUser_showsUsername() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
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
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());

            controller.onSceneShown();

            assertEquals("", controller.labelLoggedInAs.getText());
        });
    }

    @Test
    void onLogOut_clearsUsernameAndNavigatesHome() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);
            sceneManager.setCurrentUsername("gabriel");

            controller.onLogOut();

            assertNull(sceneManager.getCurrentUsername());
            assertEquals("home", sceneManager.lastScene);
        });
    }

    @Test
    void onLogOut_stopsActiveRecording() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
            setMicrophoneRecorder(controller, recorder);

            controller.onVoiceInput();
            assertTrue(recorder.isRecording());

            controller.onLogOut();

            assertFalse(recorder.isRecording());
        });
    }

    @Test
    void onReturn_stopsActiveRecording() throws Exception {
        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);
            FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
            setMicrophoneRecorder(controller, recorder);

            controller.onVoiceInput();
            assertTrue(recorder.isRecording());

            controller.onReturn();

            assertFalse(recorder.isRecording());
            assertEquals("home", sceneManager.lastScene);
        });
    }

    @Test
    void onGenerateQuestion_stopsActiveRecording() throws Exception {
        BlockingQuestionService service = new BlockingQuestionService();

        runOnFxThreadAndWait(() -> {
            PracticeController controller = createController();
            controller.setSceneManager(new FakeSceneManager());
            setQuestionService(controller, service);
            FakeMicrophoneRecorder recorder = new FakeMicrophoneRecorder();
            setMicrophoneRecorder(controller, recorder);

            controller.onVoiceInput();
            assertTrue(recorder.isRecording());

            controller.onGenerateQuestion();

            assertFalse(recorder.isRecording());
        });

        service.release();
    }

    private static class FakeFeedbackController
        extends FeedbackController {

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

    private static class FakeMicrophoneRecorder extends MicrophoneRecorder {
        boolean throwOnStart;
        byte[] audioToReturn = new byte[] {1, 2, 3};
        private boolean recording;

        @Override
        public void startRecording() throws LineUnavailableException {
            if (throwOnStart) {
                throw new LineUnavailableException("No microphone found");
            }
            recording = true;
        }

        @Override
        public byte[] stopRecording() {
            recording = false;
            return audioToReturn;
        }

        @Override
        public boolean isRecording() {
            return recording;
        }
    }

    private static class FakeSpeechToTextService extends SpeechToTextService {
        private final String result;
        byte[] receivedAudio;

        FakeSpeechToTextService(String result) {
            this.result = result;
        }

        @Override
        public String transcribe(byte[] wavAudio) {
            receivedAudio = wavAudio;
            return result;
        }
    }

    private static class FailingSpeechToTextService extends SpeechToTextService {
        private final String message;

        FailingSpeechToTextService(String message) {
            this.message = message;
        }

        @Override
        public String transcribe(byte[] wavAudio) {
            throw new RuntimeException(message);
        }
    }
}