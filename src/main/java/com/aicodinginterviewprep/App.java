package com.aicodinginterviewprep;

import com.aicodinginterviewprep.openai.EvaluationResult;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    private final OpenAiQuestionService questionService;
    private final EvaluatorService evaluatorService;

    private TabPane tabPane;
    private Tab feedbackTab;
    private TextArea questionOutput;
    private TextArea codeEditor;
    private TextField answerInput;
    private TextArea feedbackOutput;
    private ComboBox<QuestionType> questionTypeCombo;
    private Button generateButton;
    private Button runEvaluationButton;
    private Button submitAnswerButton;

    public App() {
        this(new OpenAiQuestionService(), new EvaluatorService());
    }

    public App(OpenAiQuestionService questionService, EvaluatorService evaluatorService) {
        this.questionService = questionService;
        this.evaluatorService = evaluatorService;
    }

    @Override
    public void start(Stage stage) {
        // Basic JavaFX scaffold for the project setup
        stage.setTitle("AI Coding Interview Prep");

        tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createHomeTab(),
            createPracticeTab(),
            createAuthTab(),
            createFeedbackTab()
        );

        Scene scene = new Scene(tabPane, 1024, 720);
        stage.setScene(scene);
        stage.show();
    }

    private Tab createHomeTab() {
        Tab tab = new Tab("Home");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Welcome to AI Coding Interview Prep!"),
            new Label("Use the Practice tab to answer coding questions and receive AI feedback."),
            new Label("Login or register in the Authentication tab to save progress in later assignments.")
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createPracticeTab() {
        Tab tab = new Tab("Practice");
        tab.setClosable(false);

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-padding: 16;");

        questionOutput = new TextArea("Question will appear here.");
        questionOutput.setPrefRowCount(8);
        questionOutput.setWrapText(true);
        questionOutput.setEditable(false);

        VBox left = new VBox(10);
        left.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        left.prefWidthProperty().bind(pane.widthProperty().multiply(0.40));
        left.getChildren().addAll(
            new Label("Question Output"),
            questionOutput
        );

        codeEditor = new TextArea("// Write your code here");
        codeEditor.setPrefRowCount(20);
        codeEditor.setWrapText(true);

        VBox center = new VBox(10);
        center.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        center.prefWidthProperty().bind(pane.widthProperty().multiply(0.40));
        center.getChildren().addAll(
            new Label("Code Editor"),
            codeEditor
        );

        answerInput = new TextField("Enter your solution explanation...");
        submitAnswerButton = new Button("Submit Answer");
        submitAnswerButton.setOnAction(event -> runEvaluation());

        VBox right = new VBox(10);
        right.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        right.getChildren().addAll(
            new Label("Answer Submission"),
            answerInput,
            submitAnswerButton
        );

        questionTypeCombo = new ComboBox<>();
        questionTypeCombo.getItems().addAll(QuestionType.values());
        questionTypeCombo.setValue(QuestionType.BEHAVIOURAL);

        generateButton = new Button("Generate New Question");
        generateButton.setOnAction(event -> generateQuestion());

        runEvaluationButton = new Button("Run AI Evaluation");
        runEvaluationButton.setOnAction(event -> runEvaluation());

        HBox bottom = new HBox(12);
        bottom.getChildren().addAll(
            questionTypeCombo,
            generateButton,
            runEvaluationButton
        );

        pane.setLeft(left);
        pane.setCenter(center);
        pane.setRight(right);
        pane.setBottom(bottom);
        tab.setContent(pane);
        return tab;
    }

    private Tab createAuthTab() {
        Tab tab = new Tab("Authentication");
        tab.setClosable(false);

        GridPane authGrid = new GridPane();
        authGrid.setHgap(10);
        authGrid.setVgap(10);
        authGrid.setStyle("-fx-padding: 20;");

        authGrid.add(new Label("Username:"), 0, 0);
        authGrid.add(new TextField(), 1, 0);
        authGrid.add(new Label("Password:"), 0, 1);
        authGrid.add(new TextField(), 1, 1);
        authGrid.add(new Button("Login"), 0, 2);
        authGrid.add(new Button("Register"), 1, 2);

        tab.setContent(authGrid);
        return tab;
    }

    private Tab createFeedbackTab() {
        feedbackTab = new Tab("AI Feedback");
        feedbackTab.setClosable(false);

        feedbackOutput = new TextArea("Feedback from AI will appear here.");
        feedbackOutput.setPrefRowCount(16);
        feedbackOutput.setWrapText(true);
        feedbackOutput.setEditable(false);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Evaluation Summary"),
            feedbackOutput
        );

        feedbackTab.setContent(content);
        return feedbackTab;
    }

    private void generateQuestion() {
        QuestionType type = questionTypeCombo.getValue();
        generateButton.setDisable(true);
        questionOutput.setText("Generating question...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return questionService.generateQuestion(type);
            }
        };

        task.setOnSucceeded(event -> {
            questionOutput.setText(task.getValue());
            generateButton.setDisable(false);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Unknown error.";
            questionOutput.setText("Failed to generate question: " + message);
            generateButton.setDisable(false);
        });

        Thread worker = new Thread(task, "openai-question-generation");
        worker.setDaemon(true);
        worker.start();
    }

    private void runEvaluation() {
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
        if (feedbackOutput != null) {
            feedbackOutput.setText(message);
        }
        if (tabPane != null && feedbackTab != null) {
            tabPane.getSelectionModel().select(feedbackTab);
        }
    }

    private void setEvaluationInProgress(boolean inProgress) {
        if (runEvaluationButton != null) {
            runEvaluationButton.setDisable(inProgress);
        }
        if (submitAnswerButton != null) {
            submitAnswerButton.setDisable(inProgress);
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

    public static void main(String[] args) {
        launch();
    }
}
