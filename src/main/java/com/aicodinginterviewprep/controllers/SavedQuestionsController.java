package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.CodeSubmission;
import com.aicodinginterviewprep.db.SavedQuestion;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.service.SavedContentService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SavedQuestionsController implements SceneAware {
    static final String EMPTY_MESSAGE = "No saved questions yet.";
    static final String REMOVED_MESSAGE = "Removed from saved questions.";
    static final String LOADING_MESSAGE = "Loading saved questions...";
    private static final int PREVIEW_LENGTH = 60;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private SavedContentService savedContentService = new SavedContentService();
    private Background background = Background.THREAD;
    private SceneManager sceneManager;
    private String returnScene = "practice";
    // Bumped on each load so results arriving for an earlier selection or refresh are ignored.
    private int questionsLoad;
    private int submissionsLoad;

    @FXML public ListView<SavedQuestion> listQuestions;
    @FXML public TextArea textQuestion;
    @FXML public ListView<CodeSubmission> listSubmissions;
    @FXML public TextArea textSubmission;
    @FXML public Button buttonDelete;
    @FXML public Label labelStatus;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        listQuestions.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(SavedQuestion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describe(item));
            }
        });
        listSubmissions.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(CodeSubmission item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : describe(item));
            }
        });
        listQuestions.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldQuestion, newQuestion) -> showQuestion(newQuestion));
        listSubmissions.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldSubmission, newSubmission) ->
                        textSubmission.setText(newSubmission == null ? "" : detail(newSubmission)));
        buttonDelete.setDisable(true);
    }

    @Override
    public void onSceneShown() {
        refresh(null);
    }

    /** Sets the scene that the Back button returns to. */
    public void setReturnScene(String sceneName) {
        this.returnScene = sceneName;
    }

    @FXML
    public void onBack() {
        sceneManager.switchToScene(returnScene);
    }

    @FXML
    public void onDelete() {
        SavedQuestion selected = listQuestions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        SavedContentService savedContent = savedContentService;
        String user = sceneManager.getCurrentUsername();
        buttonDelete.setDisable(true);
        background.run(() -> savedContent.removeSavedQuestion(user, selected.id()),
                removed -> refresh(REMOVED_MESSAGE),
                error -> {
                    buttonDelete.setDisable(listQuestions.getSelectionModel().getSelectedItem() == null);
                    AppErrorHandler.report(error, "Removing saved question", labelStatus::setText);
                });
    }

    private void refresh(String statusWhenLoaded) {
        int load = ++questionsLoad;
        listQuestions.getItems().clear();
        showQuestion(null);
        labelStatus.setText(statusWhenLoaded == null ? LOADING_MESSAGE : statusWhenLoaded);
        SavedContentService savedContent = savedContentService;
        String user = sceneManager.getCurrentUsername();
        background.run(() -> savedContent.listSavedQuestions(user), questions -> {
            if (load != questionsLoad) {
                return;
            }
            listQuestions.getItems().setAll(questions);
            labelStatus.setText(questions.isEmpty() ? EMPTY_MESSAGE : statusWhenLoaded == null ? "" : statusWhenLoaded);
        }, error -> {
            if (load == questionsLoad) {
                AppErrorHandler.report(error, "Loading saved questions", labelStatus::setText);
            }
        });
    }

    private void showQuestion(SavedQuestion question) {
        int load = ++submissionsLoad;
        buttonDelete.setDisable(question == null);
        textSubmission.clear();
        listSubmissions.getItems().clear();
        if (question == null) {
            textQuestion.clear();
            return;
        }
        textQuestion.setText(question.questionText());
        SavedContentService savedContent = savedContentService;
        String user = sceneManager.getCurrentUsername();
        background.run(() -> savedContent.listSubmissionsForQuestion(user, question.id()), submissions -> {
            if (load == submissionsLoad) {
                listSubmissions.getItems().setAll(submissions);
            }
        }, error -> {
            if (load == submissionsLoad) {
                AppErrorHandler.report(error, "Loading saved answers", labelStatus::setText);
            }
        });
    }

    static String describe(SavedQuestion question) {
        String flattened = question.questionText().replaceAll("\\s+", " ").strip();
        String preview = flattened.length() > PREVIEW_LENGTH
                ? flattened.substring(0, PREVIEW_LENGTH) + "..."
                : flattened;
        return question.questionType() + " / " + question.difficulty() + ": " + preview;
    }

    static String describe(CodeSubmission submission) {
        return TIME_FORMAT.format(submission.submittedAt()) + " (" + submission.language() + ")";
    }

    static String detail(CodeSubmission submission) {
        String feedback = submission.feedback();
        return feedback == null || feedback.isBlank()
                ? submission.code()
                : submission.code() + "\n\n--- Feedback ---\n" + feedback;
    }
}
