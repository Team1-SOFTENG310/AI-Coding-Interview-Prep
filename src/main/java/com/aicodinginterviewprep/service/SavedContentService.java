package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.db.CodeSubmission;
import com.aicodinginterviewprep.db.CodeSubmissionRepository;
import com.aicodinginterviewprep.db.SavedQuestion;
import com.aicodinginterviewprep.db.SavedQuestionRepository;
import com.aicodinginterviewprep.db.UserAccount;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.ValidationException;

import java.util.List;

/** Saved questions and code submissions, keyed by the signed-in user's username. */
public class SavedContentService {

    private final UserRepository users;
    private final SavedQuestionRepository questions;
    private final CodeSubmissionRepository submissions;

    public SavedContentService(UserRepository users, SavedQuestionRepository questions,
                               CodeSubmissionRepository submissions) {
        this.users = users;
        this.questions = questions;
        this.submissions = submissions;
    }

    public SavedContentService() {
        this(new UserRepository(), new SavedQuestionRepository(), new CodeSubmissionRepository());
    }

    /** Returns the database id of an existing account. */
    public long resolveUserId(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("You must be signed in to use saved content.");
        }
        return users.findByUsername(username.strip())
                .map(UserAccount::id)
                .orElseThrow(() -> new ValidationException("Account not found. Please sign in again."));
    }

    public SavedQuestion saveQuestion(String username, String questionText, String questionType, String difficulty) {
        requireText(questionText, "Question text cannot be blank.");
        return questions.save(resolveUserId(username), questionText.strip(), questionType, difficulty);
    }

    public boolean isQuestionSaved(String username, String questionText) {
        requireText(questionText, "Question text cannot be blank.");
        return questions.exists(resolveUserId(username), questionText);
    }

    public boolean removeSavedQuestion(String username, long savedQuestionId) {
        return questions.delete(resolveUserId(username), savedQuestionId);
    }

    public List<SavedQuestion> listSavedQuestions(String username) {
        return questions.findByUser(resolveUserId(username));
    }

    /** Stores a submission, linking it to the user's bookmark of the same question when one exists. */
    public CodeSubmission recordSubmission(String username, String questionText, String code,
                                           String language, String feedback, Boolean correct) {
        requireText(questionText, "Question text cannot be blank.");
        requireText(code, "Code cannot be blank.");
        requireText(language, "Language cannot be blank.");
        long userId = resolveUserId(username);
        Long savedQuestionId = questions
                .findByUserAndHash(userId, SavedQuestionRepository.hash(questionText))
                .map(SavedQuestion::id)
                .orElse(null);
        return submissions.save(userId, savedQuestionId, questionText.strip(), code, language, feedback, correct);
    }

    public List<CodeSubmission> listSubmissions(String username) {
        return submissions.findByUser(resolveUserId(username));
    }

    public List<CodeSubmission> listSubmissionsForQuestion(String username, long savedQuestionId) {
        return submissions.findBySavedQuestion(resolveUserId(username), savedQuestionId);
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
}
