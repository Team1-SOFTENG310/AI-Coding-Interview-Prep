package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.CodeSubmission;
import com.aicodinginterviewprep.db.SavedQuestion;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.errors.PersistenceException;
import com.aicodinginterviewprep.errors.ValidationException;
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
    private static final int PREVIEW_LENGTH = 60;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private SavedContentService savedContentService = new SavedContentService();
    private SceneManager sceneManager;
    private String returnScene = "practice";

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
        refresh();
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
        try {
            savedContentService.removeSavedQuestion(sceneManager.getCurrentUsername(), selected.id());
        } catch (ValidationException | PersistenceException e) {
            AppErrorHandler.report(e, "Removing saved question", labelStatus::setText);
            return;
        }
        refresh();
        labelStatus.setText(REMOVED_MESSAGE);
    }

    private void refresh() {
        listQuestions.getItems().clear();
        try {
            listQuestions.getItems().setAll(savedContentService.listSavedQuestions(sceneManager.getCurrentUsername()));
            labelStatus.setText(listQuestions.getItems().isEmpty() ? EMPTY_MESSAGE : "");
        } catch (ValidationException | PersistenceException e) {
            AppErrorHandler.report(e, "Loading saved questions", labelStatus::setText);
        }
        showQuestion(null);
    }

    private void showQuestion(SavedQuestion question) {
        buttonDelete.setDisable(question == null);
        textSubmission.clear();
        if (question == null) {
            textQuestion.clear();
            listSubmissions.getItems().clear();
            return;
        }
        textQuestion.setText(question.questionText());
        try {
            listSubmissions.getItems().setAll(
                    savedContentService.listSubmissionsForQuestion(sceneManager.getCurrentUsername(), question.id()));
        } catch (ValidationException | PersistenceException e) {
            listSubmissions.getItems().clear();
            AppErrorHandler.report(e, "Loading saved answers", labelStatus::setText);
        }
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
