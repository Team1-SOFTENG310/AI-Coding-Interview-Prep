package com.aicodinginterviewprep.controller;

import com.aicodinginterviewprep.AppContext;
import com.aicodinginterviewprep.ScreenController;
import com.aicodinginterviewprep.openai.EvaluationResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class FeedbackController implements ScreenController {

    @FXML
    private TextArea textareaEvaluation;
    @FXML
    private Button buttonTryAgain;

    private AppContext context;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        runEvaluation();
    }

    @FXML
    private void onTryAgain() {
        context.showPractice();
    }

    private void runEvaluation() {
        buttonTryAgain.setDisable(true);
        textareaEvaluation.setText("Evaluating your response with AI, please wait...");

        context.getEvaluatorService()
                .evaluateAnswerAsync(context.getCurrentQuestion(), context.getCurrentAnswer())
                .thenAccept(result -> Platform.runLater(() -> handleSuccess(result)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> handleError(ex));
                    return null;
                });
    }

    private void handleSuccess(EvaluationResult result) {
        textareaEvaluation.setText(String.format("Rating: %d/10%n%nEvaluation:%n%s",
                result.getRating(), result.getEvaluation()));
        buttonTryAgain.setDisable(false);
    }

    private void handleError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String message = cause.getMessage() != null ? cause.getMessage() : "Unknown error.";
        textareaEvaluation.setText("Evaluation failed: " + message);
        buttonTryAgain.setDisable(false);
    }
}
