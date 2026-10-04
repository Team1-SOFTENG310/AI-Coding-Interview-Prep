package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import com.aicodinginterviewprep.service.SavedContentService;
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
    private static final String RANDOM_TOPIC = "Random topic";

    private final OpenAiQuestionService questionService = new OpenAiQuestionService();
    private SavedContentService savedContentService = new SavedContentService();
    private SaveContentButton saveContent;
    private SceneManager sceneManager;

    public BorderPane codingRoot;
    public TextArea questionOutput;
    public CodeArea codeEditor;
    public Label codePlaceholder;

    @FXML public StackPane codeEditorContainer;

    @FXML public Button buttonSubmitAnswer;
    @FXML public Button buttonGenerateQuestion;
    @FXML public Button buttonPractice;
    @FXML public Label labelLoggedInAs;
    @FXML public Button buttonLogOut;
    @FXML public Button buttonSave;
    @FXML public Label labelSaveStatus;
    @FXML public ComboBox<String> comboTopic;
    @FXML public ComboBox<Difficulty> comboDifficulty;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        saveContent = new SaveContentButton(buttonSave, labelSaveStatus, () -> savedContentService,
            () -> this.sceneManager.getCurrentUsername(), () -> codeEditor.getText(), "java");
        comboTopic.getItems().setAll(RANDOM_TOPIC);
        comboTopic.getItems().addAll(questionService.getAvailableTopics(QuestionType.CODING));
        comboTopic.setValue(RANDOM_TOPIC);
        if (comboDifficulty != null) {
            comboDifficulty.getItems().setAll(Difficulty.values());
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
        clearQuestionAndCode();
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
        StackPane.setMargin(codePlaceholder, new Insets(0, 0, 0, 30));

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
        Difficulty difficulty = comboDifficulty == null ? Difficulty.MEDIUM : comboDifficulty.getValue();
        String selectedTopic = comboTopic.getValue();
        String topic = RANDOM_TOPIC.equals(selectedTopic) ? null : selectedTopic;
        buttonGenerateQuestion.setDisable(true);
        saveContent.reset();
        questionOutput.setText("Generating question...");
        codeEditor.clear();
        codeEditor.setDisable(true);
        codePlaceholder.setText(GENERATE_FIRST_TEXT);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return questionService.generateQuestion(QuestionType.CODING, difficulty, topic);
            }
        };

        task.setOnSucceeded(event -> {
            questionOutput.setText(task.getValue());
            questionOutput.setMouseTransparent(false);
            buttonGenerateQuestion.setDisable(false);
            codeEditor.setDisable(false);
            codePlaceholder.setText(PLACEHOLDER_TEXT);
            saveContent.offer(task.getValue(), QuestionType.CODING, difficulty);
        });

        task.setOnFailed(event -> {
            AppErrorHandler.report(task.getException(), "Generating coding question",
                    message -> questionOutput.setText("Failed to generate question: " + message));
            buttonGenerateQuestion.setDisable(false);
        });

        Thread worker = new Thread(task, "openai-coding-question-generation");
        worker.setDaemon(true);
        worker.start();
    }

    public void onSubmitAnswer() {
        runEvaluation();
    }

    @FXML
    public void onSave() {
        saveContent.save();
    }


    public void onPractice() {
        sceneManager.switchToScene("practice");
    }

    @FXML
    public void onSavedQuestions() {
        sceneManager.switchToScene("saved");
        if (sceneManager.getController("saved") instanceof SavedQuestionsController savedQuestions) {
            savedQuestions.setReturnScene("coding");
        }
    }

    private void clearQuestionAndCode() {
        questionOutput.clear();
        questionOutput.setMouseTransparent(true);
        codeEditor.clear();
        saveContent.reset();
    }
    public void runEvaluation() {
        sceneManager.switchToScene("feedback");
        Object controller = sceneManager.getController("feedback");
        if (!(controller instanceof FeedbackController feedbackController)) {
            return;
        }
        feedbackController.setAnswerControls(questionOutput.getText(), codeEditor.getText(), "", "coding");
        clearQuestionAndCode();
        feedbackController.runEvaluation();
    }
}
