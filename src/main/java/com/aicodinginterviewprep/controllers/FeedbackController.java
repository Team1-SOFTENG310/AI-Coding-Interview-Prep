package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.EvaluatorService;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.HistoryRepository;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.openai.EvaluationResult;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.time.Instant;

public class FeedbackController implements SceneAware {
    private SceneManager sceneManager;
    private final EvaluatorService evaluatorService = new EvaluatorService();
    private final HistoryRepository historyRepository = new HistoryRepository();
    private final UserRepository userRepository = new UserRepository();

    public TextArea correctnessTextArea;
    public TextArea efficiencyTextArea;
    public TextArea communicationTextArea;
    public TextArea codeQualityTextArea;

    public Button buttonTryAgain;
    public Button buttonQuit;

    // Snapshot of the originating tab's answer at the moment evaluation was requested
    private String question = "";
    private String code = "";
    private String explanation = "";
    private String topic;
    private String returnScene = "practice";

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setAnswerControls(String question, String code, String explanation) {
        setAnswerControls(question, code, explanation, "practice");
    }

    public void setAnswerControls(String question, String code, String explanation, String returnScene) {
        setAnswerControls(question, code, explanation, returnScene, null);
    }

    public void setAnswerControls(
            String question, String code, String explanation, String returnScene, String topic) {
        this.question = question == null ? "" : question;
        this.code = code == null ? "" : code;
        this.explanation = explanation == null ? "" : explanation;
        this.returnScene = returnScene;
        this.topic = topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void onTryAgain() {
        sceneManager.switchToScene(returnScene);
    }

    public void onQuit() {
        saveEvaluationRecords();
        sceneManager.switchToScene("home");
    }
    //saves summarised evaluation records to history database table for logged-in user
    public void saveEvaluationRecords() {
        String username = sceneManager.getCurrentUsername();
        if (username == null || username.isBlank()) {
            return;
        }

        if (evaluatorService.getEvaluationRecords().isEmpty()) {
            return;
        }

        long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user was not found: " + username))
                .id();
        EvaluatorService.EvaluationSummary summary = evaluatorService.summarizeEvaluationRecords();
        historyRepository.save(
                userId,
                summary.topics(),
                Instant.now(),
                summary.correctnessScore(),
                summary.efficiencyScore(),
                summary.communicationScore(),
                summary.codeQualityScore());
        evaluatorService.clearEvaluationRecords();
    }

    public void runEvaluation() {
        String validQuestion = extractValidQuestion();
        if (validQuestion == null) {
            displayTextAreaError("Please generate a question first before running an evaluation.");
            return;
        }

        String userAnswer = buildUserAnswer();
        if (userAnswer.isEmpty()) {
            displayTextAreaError("Please provide an answer explanation or code solution before submitting for evaluation.");
            return;
        }

        setEvaluationInProgress(true);
        displayTextAreaError("Evaluating your response with AI, please wait...");

        evaluatorService.evaluateAnswerAsync(validQuestion, userAnswer)
                .thenAccept(result -> Platform.runLater(() -> handleEvaluationSuccess(result)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> handleEvaluationError(ex));
                    return null;
                });
    }

    private String extractValidQuestion() {
        String trimmed = question.trim();
        if (trimmed.isEmpty() || "Question will appear here.".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private String buildUserAnswer() {
        String trimmedExplanation = explanation.trim();
        String trimmedCode = code.trim();

        StringBuilder answerBuilder = new StringBuilder();
        if (!trimmedExplanation.isEmpty()) {
            answerBuilder.append(trimmedExplanation);
        }
        if (!trimmedCode.isEmpty()) {
            if (!answerBuilder.isEmpty()) {
                answerBuilder.append("\n\nCode:\n");
            }
            answerBuilder.append(trimmedCode);
        }
        return answerBuilder.toString().trim();
    }

    private void showCategoryFeedback(
        TextArea textArea,
        Integer rating,
        String feedback) {

    if (textArea == null) {
        return;
    }

    if (rating == null) {
        textArea.setText(feedback);
        return;
    }

    textArea.setText(
            String.format(
                    "Rating: %d/10%n%n%s",
                    rating,
                    feedback
            )
    );
}

    private void setEvaluationInProgress(boolean inProgress) {
        if (buttonTryAgain != null) {
            buttonTryAgain.setDisable(inProgress);
        }
        if (buttonQuit != null) {
            buttonQuit.setDisable(inProgress);
        }
    }

    private void handleEvaluationSuccess(EvaluationResult result) {
    evaluatorService.storeEvaluation(topic, result);

    showCategoryFeedback(
            correctnessTextArea,
            result.getCorrectnessRating(),
            result.getCorrectnessEvaluation()
    );

    showCategoryFeedback(
            efficiencyTextArea,
            result.getEfficiencyRating(),
            result.getEfficiencyEvaluation()
    );

    showCategoryFeedback(
            communicationTextArea,
            result.getCommunicationRating(),
            result.getCommunicationEvaluation()
    );

    showCategoryFeedback(
            codeQualityTextArea,
            result.getCodeQualityRating(),
            result.getCodeQualityEvaluation()
    );

    setEvaluationInProgress(false);
}

    private void handleEvaluationError(Throwable ex) {
        AppErrorHandler.report(ex, "Evaluating interview answer",
                message -> displayTextAreaError("Evaluation failed: " + message));
        setEvaluationInProgress(false);
    }

    private void displayTextAreaError(String errorMessage){
        if (correctnessTextArea != null) {
            correctnessTextArea.setText(errorMessage);
        }
        if (efficiencyTextArea != null) {
            efficiencyTextArea.setText(errorMessage);
        }
        if (communicationTextArea != null) {
            communicationTextArea.setText(errorMessage);
        }
        if (codeQualityTextArea != null) {
            codeQualityTextArea.setText(errorMessage);
        }
    }
}
