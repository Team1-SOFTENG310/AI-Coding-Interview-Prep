package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class CodingController implements SceneAware {
    private static final String STARTER_CODE = "// Write your code here";

    private final OpenAiQuestionService questionService = new OpenAiQuestionService();
    private SceneManager sceneManager;

    public BorderPane codingRoot;
    public TextArea questionOutput;
    public TextArea codeEditor;

    @FXML public Button buttonReturn;
    @FXML public Button buttonSubmitAnswer;
    @FXML public Button buttonGenerateQuestion;
    @FXML public Button buttonPractice;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void onGenerateQuestion() {
        buttonGenerateQuestion.setDisable(true);
        questionOutput.setText("Generating question...");
        codeEditor.setText(STARTER_CODE);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return questionService.generateQuestion(QuestionType.CODING);
            }
        };

        task.setOnSucceeded(event -> {
            questionOutput.setText(task.getValue());
            buttonGenerateQuestion.setDisable(false);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Unknown error.";
            questionOutput.setText("Failed to generate question: " + message);
            buttonGenerateQuestion.setDisable(false);
        });

        Thread worker = new Thread(task, "openai-coding-question-generation");
        worker.setDaemon(true);
        worker.start();
    }

    public void onSubmitAnswer() {
        runEvaluation();
    }

    public void onReturn() {
        sceneManager.switchToScene("home");
    }

    public void onPractice() {
        sceneManager.switchToScene("practice");
    }

    public void runEvaluation() {
        sceneManager.switchToScene("feedback");
        Object controller = sceneManager.getController("feedback");
        if (!(controller instanceof FeedbackController feedbackController)) {
            return;
        }
        feedbackController.setAnswerControls(questionOutput, codeEditor, null, "coding");
        feedbackController.runEvaluation();
    }
}
