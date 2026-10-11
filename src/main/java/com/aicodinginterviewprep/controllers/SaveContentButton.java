package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Difficulty;
import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.service.SavedContentService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.Supplier;

/** A page's single save button: bookmarks the displayed question and stores the current answer with it. */
final class SaveContentButton {
    static final String SAVED_QUESTION_MESSAGE = "Question saved.";
    static final String SAVED_QUESTION_AND_ANSWER_MESSAGE = "Question and answer saved.";
    static final String ALREADY_SAVED_MESSAGE = "Already saved.";
    static final String SAVING_MESSAGE = "Saving...";

    private final Button button;
    private final Label statusLabel;
    private final Supplier<SavedContentService> service;
    private final Supplier<Background> background;
    private final Supplier<String> username;
    private final Supplier<String> answer;
    private final String language;

    private String questionText;
    private QuestionType type;
    private Difficulty difficulty;
    private boolean saving;
    // Incremented on reset so a save finishing for an earlier question is ignored.
    private int generation;

    SaveContentButton(Button button, Label statusLabel, Supplier<SavedContentService> service,
                      Supplier<Background> background, Supplier<String> username,
                      Supplier<String> answer, String language) {
        this.button = button;
        this.statusLabel = statusLabel;
        this.service = service;
        this.background = background;
        this.username = username;
        this.answer = answer;
        this.language = language;
        reset();
    }

    /** Disables saving until a question is displayed. */
    void reset() {
        generation++;
        questionText = null;
        type = null;
        difficulty = null;
        saving = false;
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
        if (questionText == null || saving) {
            return;
        }
        String answerText = answer.get();
        SavedContentService savedContent = service.get();
        String user = username.get();
        String question = questionText;
        String questionType = type.name();
        String questionDifficulty = difficulty.name();
        int savedGeneration = generation;

        saving = true;
        button.setDisable(true);
        statusLabel.setText(SAVING_MESSAGE);
        background.get().run(() -> savedContent.saveQuestionAndAnswer(
                user, question, questionType, questionDifficulty, answerText, language), outcome -> {
            if (savedGeneration != generation) {
                return;
            }
            saving = false;
            button.setDisable(false);
            statusLabel.setText(switch (outcome) {
                case QUESTION_SAVED -> SAVED_QUESTION_MESSAGE;
                case QUESTION_AND_ANSWER_SAVED -> SAVED_QUESTION_AND_ANSWER_MESSAGE;
                case ALREADY_SAVED -> ALREADY_SAVED_MESSAGE;
            });
        }, error -> {
            if (savedGeneration != generation) {
                return;
            }
            saving = false;
            button.setDisable(false);
            AppErrorHandler.report(error, "Saving question and answer", statusLabel::setText);
        });
    }
}
