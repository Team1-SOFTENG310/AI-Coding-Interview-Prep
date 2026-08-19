package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.EvaluatorService;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.openai.EvaluationResult;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;

public class FeedbackController implements SceneAware {
    private SceneManager sceneManager;
    private final EvaluatorService evaluatorService = new EvaluatorService();

    public TextArea textareaEvaluation;
    public Button buttonTryAgain;

    // References to Practice tab controls for answer extraction
    private TextArea questionOutput;
    private TextArea codeEditor;
    private TextInputControl answerInput;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void setAnswerControls(TextArea questionOutput, TextArea codeEditor, TextInputControl answerInput) {
        this.questionOutput = questionOutput;
        this.codeEditor = codeEditor;
        this.answerInput = answerInput;
    }

    public void onTryAgain() {
        sceneManager.switchToScene("practice");
    }

    public void runEvaluation() {
        String question = extractValidQuestion();
        if (question == null) {
            showFeedback("Please generate a question first before running an evaluation.");
            return;
        }

        String userAnswer = buildUserAnswer();
        if (userAnswer.isEmpty()) {
            showFeedback("Please provide an answer explanation or code solution before submitting for evaluation.");
            return;
        }

        setEvaluationInProgress(true);
        showFeedback("Evaluating your response with AI, please wait...");

        evaluatorService.evaluateAnswerAsync(question, userAnswer)
            .thenAccept(result -> Platform.runLater(() -> handleEvaluationSuccess(result)))
            .exceptionally(ex -> {
                Platform.runLater(() -> handleEvaluationError(ex));
                return null;
            });
    }

    private String extractValidQuestion() {
        String question = getTextOrEmpty(questionOutput);
        if (question.isEmpty() || "Question will appear here.".equals(question)) {
            return null;
        }
        return question;
    }

    private String buildUserAnswer() {
        String explanation = getTextOrEmpty(answerInput);
        String code = getTextOrEmpty(codeEditor);

        StringBuilder answerBuilder = new StringBuilder();
        if (!explanation.isEmpty() && !"Enter your solution explanation...".equals(explanation)) {
            answerBuilder.append(explanation);
        }
        if (!code.isEmpty() && !"// Write your code here".equals(code)) {
            if (!answerBuilder.isEmpty()) {
                answerBuilder.append("\n\nCode:\n");
            }
            answerBuilder.append(code);
        }
        return answerBuilder.toString().trim();
    }

    private String getTextOrEmpty(TextInputControl control) {
        return control != null && control.getText() != null ? control.getText().trim() : "";
    }

    private void showFeedback(String message) {
        if (textareaEvaluation != null) {
            textareaEvaluation.setText(message);
        }
    }

    private void setEvaluationInProgress(boolean inProgress) {
        if (buttonTryAgain != null) {
            buttonTryAgain.setDisable(inProgress);
        }
    }

    private void handleEvaluationSuccess(EvaluationResult result) {
        showFeedback(String.format("Rating: %d/10%n%nEvaluation:%n%s",
                result.getRating(), result.getEvaluation()));
        setEvaluationInProgress(false);
    }

    private void handleEvaluationError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String errorMessage = cause.getMessage() != null ? cause.getMessage() : "Unknown error.";
        showFeedback("Evaluation failed: " + errorMessage);
        setEvaluationInProgress(false);
    }
}
