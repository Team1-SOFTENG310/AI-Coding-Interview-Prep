package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.errors.PersistenceException;
import com.aicodinginterviewprep.errors.ValidationException;
import com.aicodinginterviewprep.service.SavedContentService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.Supplier;

/** A page's single save button: bookmarks the displayed question and stores the current answer with it. */
final class SaveContentButton {
    static final String SAVED_QUESTION_MESSAGE = "Question saved.";
    static final String SAVED_QUESTION_AND_ANSWER_MESSAGE = "Question and answer saved.";
    static final String ALREADY_SAVED_MESSAGE = "Already saved.";

    private final Button button;
    private final Label statusLabel;
    private final Supplier<SavedContentService> service;
    private final Supplier<String> username;
    private final Supplier<String> answer;
    private final String language;

    private String questionText;
    private QuestionType type;
    private Difficulty difficulty;
    private boolean questionSaved;
    private String lastSavedAnswer;

    SaveContentButton(Button button, Label statusLabel, Supplier<SavedContentService> service,
                      Supplier<String> username, Supplier<String> answer, String language) {
        this.button = button;
        this.statusLabel = statusLabel;
        this.service = service;
        this.username = username;
        this.answer = answer;
        this.language = language;
        reset();
    }

    /** Disables saving until a question is displayed. */
    void reset() {
        questionText = null;
        type = null;
        difficulty = null;
        questionSaved = false;
        lastSavedAnswer = null;
        button.setDisable(true);
        statusLabel.setText("");
    }

    void offer(String question, QuestionType questionType, Difficulty questionDifficulty) {
        reset();
        questionText = question;
        type = questionType;
        difficulty = questionDifficulty;
        button.setDisable(false);
    }

    void save() {
        if (questionText == null) {
            return;
        }
        String answerText = answer.get();
        boolean hasAnswer = answerText != null && !answerText.isBlank();
        if (questionSaved && (!hasAnswer || answerText.equals(lastSavedAnswer))) {
            statusLabel.setText(ALREADY_SAVED_MESSAGE);
            return;
        }
        try {
            String user = username.get();
            service.get().saveQuestion(user, questionText, type.name(), difficulty.name());
            questionSaved = true;
            if (hasAnswer) {
                service.get().recordSubmission(user, questionText, answerText, language, null, null);
                lastSavedAnswer = answerText;
                statusLabel.setText(SAVED_QUESTION_AND_ANSWER_MESSAGE);
            } else {
                statusLabel.setText(SAVED_QUESTION_MESSAGE);
            }
        } catch (ValidationException | PersistenceException e) {
            AppErrorHandler.report(e, "Saving question and answer", statusLabel::setText);
        }
    }
}
