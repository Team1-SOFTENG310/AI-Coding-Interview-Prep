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
        stage.setWidth(1024);
        stage.setHeight(720);

        SceneManager sceneManager = new SceneManager(stage);
        sceneManager.switchToScene("home");

        stage.show();
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

        VBox center = new VBox(10);
        center.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        center.prefWidthProperty().bind(pane.widthProperty().multiply(0.40));
        center.getChildren().addAll(
            new Label("Code Editor"),
            new TextArea("// Write your code here") {{ setPrefRowCount(20); setWrapText(true); }}
        );

        VBox right = new VBox(10);
        right.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        //right.prefWidthProperty().bind(pane.widthProperty().multiply(0.20));
        right.getChildren().addAll(
            new Label("Answer Submission"),
            new TextField("Enter your solution explanation..."),
            new Button("Submit Answer")
        );

        questionTypeCombo = new ComboBox<>();
        questionTypeCombo.getItems().addAll(QuestionType.values());
        questionTypeCombo.setValue(QuestionType.BEHAVIOURAL);

        generateButton = new Button("Generate New Question");
        generateButton.setOnAction(event -> generateQuestion());

        HBox bottom = new HBox(12);
        bottom.getChildren().addAll(
            questionTypeCombo,
            generateButton,
            new Button("Run AI Evaluation")
        );

        pane.setLeft(left);
        pane.setCenter(center);
        pane.setRight(right);
        pane.setBottom(bottom);
        tab.setContent(pane);
        return tab;
    }

    private Tab createFeedbackTab() {
        Tab tab = new Tab("AI Feedback");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Evaluation Summary"),
            new TextArea("Feedback from AI will appear here.") {{ setPrefRowCount(16); setWrapText(true); setEditable(false); }}
        );

        tab.setContent(content);
        return tab;
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

    public static void main(String[] args) {
        launch();
    }
}
