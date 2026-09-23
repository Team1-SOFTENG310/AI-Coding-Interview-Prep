package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class CodingController implements SceneAware {
    private static final String PLACEHOLDER_TEXT = "// Write your code here";
    private static final String GENERATE_FIRST_TEXT = "Generate a question first to start coding.";

    private final OpenAiQuestionService questionService = new OpenAiQuestionService();
    private SceneManager sceneManager;

    public BorderPane codingRoot;
    public TextArea questionOutput;
    public CodeArea codeEditor;
    public Label codePlaceholder;

    @FXML public StackPane codeEditorContainer;
    @FXML public Button buttonReturn;
    @FXML public Button buttonSubmitAnswer;
    @FXML public Button buttonGenerateQuestion;
    @FXML public Button buttonPractice;
    @FXML public Label labelLoggedInAs;
    @FXML public Button buttonLogOut;
    @FXML public ComboBox<Difficulty> comboDifficulty;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        if (comboDifficulty != null) {
            comboDifficulty.getItems().addAll(Difficulty.values());
            comboDifficulty.setValue(Difficulty.MEDIUM);
        }
        setUpCodeEditor();
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
        sceneManager.setCurrentUsername(null);
        sceneManager.switchToScene("home");
    }

    private void setUpCodeEditor() {
        codeEditor = new CodeArea();
        codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
        codeEditor.getStyleClass().add("code-editor");

        codePlaceholder = new Label(GENERATE_FIRST_TEXT);
        codePlaceholder.getStyleClass().add("code-editor-placeholder");
        codePlaceholder.setMouseTransparent(true);
        StackPane.setAlignment(codePlaceholder, Pos.TOP_LEFT);
        StackPane.setMargin(codePlaceholder, new Insets(1, 0, 0, 54));

        codeEditor.textProperty().addListener((observable, oldText, newText) -> {
            updateCodePlaceholderVisibility();
            codeEditor.setStyleSpans(0, JavaSyntaxHighlighter.computeHighlighting(newText));
            buttonSubmitAnswer.setDisable(newText.trim().isEmpty());
        });
        codeEditor.focusedProperty().addListener((observable, oldFocused, newFocused) -> updateCodePlaceholderVisibility());
        buttonSubmitAnswer.setDisable(true);
        codeEditor.setDisable(true);

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeEditor);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        codeEditorContainer.getChildren().setAll(scrollPane, codePlaceholder);
    }

    private void updateCodePlaceholderVisibility() {
        codePlaceholder.setVisible(!codeEditor.isFocused() && codeEditor.getText().isEmpty());
    }

    @FXML
    public void onGenerateQuestion() {
        questionService.setDifficulty(comboDifficulty == null ? Difficulty.MEDIUM : comboDifficulty.getValue());
        buttonGenerateQuestion.setDisable(true);
        questionOutput.setText("Generating question...");
        codeEditor.clear();
        codeEditor.setDisable(true);
        codePlaceholder.setText(GENERATE_FIRST_TEXT);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return questionService.generateQuestion(QuestionType.CODING);
            }
        };

        task.setOnSucceeded(event -> {
            questionOutput.setText(task.getValue());
            buttonGenerateQuestion.setDisable(false);
            codeEditor.setDisable(false);
            codePlaceholder.setText(PLACEHOLDER_TEXT);
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
        feedbackController.setAnswerControls(questionOutput.getText(), codeEditor.getText(), "", "coding");
        feedbackController.runEvaluation();
    }
}
