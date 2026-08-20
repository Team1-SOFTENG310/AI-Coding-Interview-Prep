package com.aicodinginterviewprep;

import com.aicodinginterviewprep.coding.CodingQuestion;
import com.aicodinginterviewprep.coding.CodingQuestionBank;
import com.aicodinginterviewprep.openai.EvaluationResult;
import com.aicodinginterviewprep.service.OpenAiQuestionService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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

        private ListView<CodingQuestion> codingQuestionListView;
    private Label codingTitleLabel;
    private TextArea codingDescriptionArea;
    private Label codingIoLabel;
    private TextArea codingCodeArea;
    private Button codingSubmitButton;
    private CodingQuestion currentCodingQuestion;

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
            createCodingTab(),
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

private Tab createCodingTab() {
    Tab tab = new Tab("Coding");
    tab.setClosable(false);

    BorderPane pane = new BorderPane();
    pane.setStyle("-fx-padding: 16;");

    pane.setLeft(buildCodingQuestionListPane());
    pane.setCenter(buildCodingEditorPane());

    if (!CodingQuestionBank.getAll().isEmpty()) {
        codingQuestionListView.getSelectionModel().select(0);
    }

    tab.setContent(pane);
    return tab;
}

private VBox buildCodingQuestionListPane() {
    VBox box = new VBox(8);
    box.setPadding(new Insets(0, 16, 0, 0));
    box.setPrefWidth(220);

    Label header = new Label("Coding Questions");
    header.setFont(Font.font(14));

    codingQuestionListView = new ListView<>();
    codingQuestionListView.getItems().addAll(CodingQuestionBank.getAll());

    codingQuestionListView.getSelectionModel().selectedItemProperty()
    .addListener((obs, oldQuestion, newQuestion) -> {
        if (newQuestion != null) {
            showCodingQuestion(newQuestion);
        }
    });

    box.getChildren().addAll(header, codingQuestionListView);
    return box;
}

private VBox buildCodingEditorPane() {
    codingTitleLabel = new Label();
    codingTitleLabel.setFont(Font.font(18));

    codingDescriptionArea = new TextArea();
    codingDescriptionArea.setEditable(false);
    codingDescriptionArea.setWrapText(true);
    codingDescriptionArea.setPrefRowCount(6);

    codingIoLabel = new Label();
    codingIoLabel.setWrapText(true);

    VBox descriptionBox = new VBox(6, codingTitleLabel, codingDescriptionArea, codingIoLabel);
    descriptionBox.setPadding(new Insets(0, 0, 10, 10));

    codingCodeArea = new TextArea();
    codingCodeArea.setFont(Font.font("Monospaced", 13));
    codingCodeArea.setWrapText(false);
    codingCodeArea.setPrefRowCount(16);
    codingCodeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        if (event.getCode() == KeyCode.TAB) {
            codingCodeArea.insertText(codingCodeArea.getCaretPosition(), "    ");
            event.consume();
        }
    });

    codingSubmitButton = new Button("Submit Code for AI Evaluation");
    codingSubmitButton.setOnAction(event -> runCodingEvaluation());

    VBox editorBox = new VBox(8, codingCodeArea, codingSubmitButton);
    editorBox.setPadding(new Insets(10, 0, 0, 10));

    SplitPane splitPane = new SplitPane(descriptionBox, editorBox);
    splitPane.setOrientation(Orientation.VERTICAL);
    splitPane.setDividerPositions(0.4);

    return new VBox(splitPane);
}

private void showCodingQuestion(CodingQuestion question) {
    currentCodingQuestion = question;
    codingTitleLabel.setText(question.getListLabel());
    codingDescriptionArea.setText(question.getDescription());
    codingIoLabel.setText("Example input: " + question.getExampleInput()
        + "\nExample output: " + question.getExampleOutput());
    codingCodeArea.setText(question.getStarterCode());
}

private void runCodingEvaluation() {
    if (currentCodingQuestion == null) {
        showFeedback("Please select a coding question first.");
        return;
    }

    String code = getTextOrEmpty(codingCodeArea);
    if (code.isEmpty() || code.equals(currentCodingQuestion.getStarterCode().trim())) {
        showFeedback("Please write a solution before submitting for evaluation.");
        return;
    }

    setEvaluationInProgress(true);
    showFeedback("Evaluating your code with AI, please wait...");

    String questionPrompt = "Coding Question: " + currentCodingQuestion.getListLabel()
        + "\nDescription: " + currentCodingQuestion.getDescription()
        + "\nExample input: " + currentCodingQuestion.getExampleInput()
        + "\nExample output: " + currentCodingQuestion.getExampleOutput();

    evaluatorService.evaluateAnswerAsync(questionPrompt, code)
        .thenAccept(result -> Platform.runLater(() -> handleEvaluationSuccess(result)))
        .exceptionally(ex -> {
            Platform.runLater(() -> handleEvaluationError(ex));
            return null;
        });
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
            if (codingSubmitButton != null) {
        codingSubmitButton.setDisable(inProgress);
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
