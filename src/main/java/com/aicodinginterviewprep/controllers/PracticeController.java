package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.MicrophoneRecorder;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import com.aicodinginterviewprep.service.SpeechToTextService;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import javax.sound.sampled.LineUnavailableException;

public class PracticeController implements SceneAware {
    private static final String GENERATE_FIRST_PROMPT = "Generate a question first to start answering.";
    private static final String ANSWER_PROMPT = "Explain your solution";
    private static final String VOICE_IDLE_TEXT = "Record Answer";
    private static final String VOICE_RECORDING_TEXT = "Stop Recording";
    private static final String VOICE_TRANSCRIBING_TEXT = "Transcribing...";
    private static final String RECORDING_STYLE_CLASS = "recording";

    private final OpenAiQuestionService questionService = new OpenAiQuestionService();
    private final MicrophoneRecorder microphoneRecorder = new MicrophoneRecorder();
    private final SpeechToTextService speechToTextService = new SpeechToTextService();
    private SceneManager sceneManager;

    public BorderPane practiceRoot;
    public TextArea questionOutput;
    public TextArea answerInput;

    
    @FXML public Button buttonSubmitAnswer;
    @FXML public Button buttonGenerateQuestion;
    @FXML public Button buttonCodingPractice;
    @FXML public Button buttonVoiceInput;
    @FXML public Label labelVoiceStatus;
    @FXML public Label labelLoggedInAs;
    @FXML public Button buttonLogOut;
    @FXML public ComboBox<QuestionType> comboQuestionType;
    @FXML public ComboBox<Difficulty> comboDifficulty;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.comboQuestionType.getItems().addAll(QuestionType.BEHAVIOURAL, QuestionType.THEORY);
        this.comboQuestionType.setValue(QuestionType.BEHAVIOURAL);
        if (this.comboDifficulty != null) {
            this.comboDifficulty.getItems().addAll(Difficulty.values());
            this.comboDifficulty.setValue(Difficulty.MEDIUM);
        }

        buttonSubmitAnswer.disableProperty().bind(Bindings.createBooleanBinding(
            () -> answerInput.getText() == null || answerInput.getText().trim().isEmpty(),
            answerInput.textProperty()
        ));

        answerInput.setDisable(true);
        answerInput.setPromptText(GENERATE_FIRST_PROMPT);
        buttonVoiceInput.setDisable(true);
    }

    @Override
    public void onSceneShown() {
        updateLoggedInLabel();
    }

    private void updateLoggedInLabel() {
        if (labelLoggedInAs == null) {
            return;
        }
        String username = sceneManager.getCurrentUsername();
        labelLoggedInAs.setText(username == null || username.isBlank() ? "" : "Logged in as " + username);
    }

    public void onLogOut() {
        cancelRecordingIfActive();
        clearQuestionAndAnswer();
        sceneManager.setCurrentUsername(null);
        sceneManager.switchToScene("home");
    }

    @FXML
    public void onGenerateQuestion() {
        cancelRecordingIfActive();
        QuestionType type = comboQuestionType.getValue();
        Difficulty difficulty = comboDifficulty == null ? Difficulty.MEDIUM : comboDifficulty.getValue();
        questionService.setDifficulty(difficulty);
        buttonGenerateQuestion.setDisable(true);
        questionOutput.setText("Generating question...");
        answerInput.clear();
        answerInput.setDisable(true);
        answerInput.setPromptText(GENERATE_FIRST_PROMPT);
        buttonVoiceInput.setDisable(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return questionService.generateQuestion(type);
            }
        };

        task.setOnSucceeded(event -> {
            questionOutput.setText(task.getValue());
            buttonGenerateQuestion.setDisable(false);
            answerInput.setDisable(false);
            answerInput.setPromptText(ANSWER_PROMPT);
            buttonVoiceInput.setDisable(false);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Unknown error.";
            questionOutput.setText("Failed to generate question: " + message);
            buttonGenerateQuestion.setDisable(false);
        });

        Thread worker = new Thread(task, "openai-question-generation");
        worker.setDaemon(true);
        worker.start();
    }

    public void onSubmitAnswer() {
        runEvaluation();
    }



    public void onCodingPractice() {
        sceneManager.switchToScene("coding");
    }

    @FXML
    public void onVoiceInput() {
        if (microphoneRecorder.isRecording()) {
            stopRecordingAndTranscribe();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        try {
            microphoneRecorder.startRecording();
        } catch (LineUnavailableException e) {
            setVoiceStatus("Microphone unavailable: " + e.getMessage());
            return;
        }
        setVoiceStatus("Recording... click again to stop.");
        buttonVoiceInput.setText(VOICE_RECORDING_TEXT);
        if (!buttonVoiceInput.getStyleClass().contains(RECORDING_STYLE_CLASS)) {
            buttonVoiceInput.getStyleClass().add(RECORDING_STYLE_CLASS);
        }
    }

    private void stopRecordingAndTranscribe() {
        buttonVoiceInput.setDisable(true);
        buttonVoiceInput.setText(VOICE_TRANSCRIBING_TEXT);
        buttonVoiceInput.getStyleClass().remove(RECORDING_STYLE_CLASS);
        setVoiceStatus("Transcribing your answer...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                byte[] audio = microphoneRecorder.stopRecording();
                return speechToTextService.transcribe(audio);
            }
        };

        task.setOnSucceeded(event -> {
            String transcript = task.getValue();
            if (transcript == null || transcript.isBlank()) {
                setVoiceStatus("Didn't catch that - try again.");
            } else {
                appendTranscript(transcript);
                setVoiceStatus("");
            }
            resetVoiceButton();
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Unknown error.";
            setVoiceStatus("Transcription failed: " + message);
            resetVoiceButton();
        });

        Thread worker = new Thread(task, "voice-transcription");
        worker.setDaemon(true);
        worker.start();
    }

    private void appendTranscript(String transcript) {
        String existing = answerInput.getText();
        String combined = existing == null || existing.isBlank()
            ? transcript
            : existing.trim() + " " + transcript;
        answerInput.setText(combined);
        answerInput.positionCaret(combined.length());
    }

    private void cancelRecordingIfActive() {
        if (!microphoneRecorder.isRecording()) {
            return;
        }
        try {
            microphoneRecorder.stopRecording();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        setVoiceStatus("");
        resetVoiceButton();
    }

    private void resetVoiceButton() {
        buttonVoiceInput.setDisable(answerInput.isDisabled());
        buttonVoiceInput.setText(VOICE_IDLE_TEXT);
        buttonVoiceInput.getStyleClass().remove(RECORDING_STYLE_CLASS);
    }

    private void setVoiceStatus(String message) {
        if (labelVoiceStatus != null) {
            labelVoiceStatus.setText(message);
        }
    }
    
    private void clearQuestionAndAnswer(){
        questionOutput.clear();
        answerInput.clear();
        

    }

    public void runEvaluation() {
        sceneManager.switchToScene("feedback");
        Object controller = sceneManager.getController("feedback");
        if (!(controller instanceof FeedbackController feedbackController)) {
            return;
        }
        feedbackController.setAnswerControls(questionOutput.getText(), "", answerInput.getText(), "practice");
        clearQuestionAndAnswer();
        feedbackController.runEvaluation();
    }
}
