package com.aicodinginterviewprep.controller;

import com.aicodinginterviewprep.AppContext;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.ScreenController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class PracticeController implements ScreenController {

    private static final String WAITING_MESSAGE = "Waiting for question to generate...";
    private static final QuestionType DEFAULT_QUESTION_TYPE = QuestionType.BEHAVIOURAL;

    @FXML
    private Label labelQuestion;
    @FXML
    private TextArea textareaCode;
    @FXML
    private TextArea textareaExplanation;
    @FXML
    private Button buttonSubmitAnswer;
    @FXML
    private Button buttonNewQuestion;
    @FXML
    private ProgressBar progressBarQuestion;

    private AppContext context;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        generateQuestion();
    }

    @FXML
    private void onReturn() {
        context.showHome();
    }

    @FXML
    private void onNewQuestion() {
        generateQuestion();
    }

    @FXML
    private void onSubmitAnswer() {
        submitAnswer();
    }

    private void generateQuestion() {
        buttonNewQuestion.setDisable(true);
        progressBarQuestion.setVisible(true);
        labelQuestion.setText(WAITING_MESSAGE);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return context.getQuestionService().generateQuestion(DEFAULT_QUESTION_TYPE);
            }
        };

        task.setOnSucceeded(event -> {
            String question = task.getValue();
            context.setCurrentQuestion(question);
            labelQuestion.setText(question);
            buttonNewQuestion.setDisable(false);
            progressBarQuestion.setVisible(false);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Unknown error.";
            context.setCurrentQuestion("");
            labelQuestion.setText("Failed to generate question: " + message);
            buttonNewQuestion.setDisable(false);
            progressBarQuestion.setVisible(false);
        });

        Thread worker = new Thread(task, "openai-question-generation");
        worker.setDaemon(true);
        worker.start();
    }

    private void submitAnswer() {
        String question = context.getCurrentQuestion();
        if (question == null || question.isEmpty()) {
            labelQuestion.setText("Please wait for a question to generate before submitting.");
            return;
        }

        String answer = buildAnswer();
        if (answer.isEmpty()) {
            labelQuestion.setText("Please provide an answer explanation or code solution before submitting.");
            return;
        }

        context.setCurrentAnswer(answer);
        context.showFeedback();
    }

    private String buildAnswer() {
        String explanation = textOrEmpty(textareaExplanation);
        String code = textOrEmpty(textareaCode);

        StringBuilder builder = new StringBuilder();
        if (!explanation.isEmpty()) {
            builder.append(explanation);
        }
        if (!code.isEmpty()) {
            if (!builder.isEmpty()) {
                builder.append("\n\nCode:\n");
            }
            builder.append(code);
        }
        return builder.toString().trim();
    }

    private String textOrEmpty(TextArea area) {
        return area != null && area.getText() != null ? area.getText().trim() : "";
    }
}
