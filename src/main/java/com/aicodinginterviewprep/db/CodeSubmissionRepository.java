package com.aicodinginterviewprep.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CodeSubmissionRepository {

    private static final String COLUMNS =
            "id, user_id, saved_question_id, question_text, code, language, feedback, correct, submitted_at";

    private final ConnectionFactory connections;

    public CodeSubmissionRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    public CodeSubmissionRepository() {
        this(Database::open);
    }

    /** {@code savedQuestionId}, {@code feedback} and {@code correct} may be null. */
    public CodeSubmission save(long userId, Long savedQuestionId, String questionText, String code,
                               String language, String feedback, Boolean correct) {
        String sql = "INSERT INTO code_submission "
                + "(user_id, saved_question_id, question_text, code, language, feedback, correct) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            if (savedQuestionId == null) {
                statement.setNull(2, Types.BIGINT);
            } else {
                statement.setLong(2, savedQuestionId);
            }
            statement.setString(3, questionText);
            statement.setString(4, code);
            statement.setString(5, language);
            statement.setString(6, feedback);
            if (correct == null) {
                statement.setNull(7, Types.BOOLEAN);
            } else {
                statement.setBoolean(7, correct);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return findById(userId, keys.getLong(1)).orElseThrow();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save code submission", e);
        }
    }

    public Optional<CodeSubmission> findById(long userId, long id) {
        String sql = "SELECT " + COLUMNS + " FROM code_submission WHERE id = ? AND user_id = ?";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load code submission", e);
        }
    }

    public List<CodeSubmission> findByUser(long userId) {
        return query("SELECT " + COLUMNS + " FROM code_submission WHERE user_id = ? "
                + "ORDER BY submitted_at DESC, id DESC", userId);
    }

    public List<CodeSubmission> findBySavedQuestion(long userId, long savedQuestionId) {
        return query("SELECT " + COLUMNS + " FROM code_submission WHERE user_id = ? AND saved_question_id = ? "
                + "ORDER BY submitted_at DESC, id DESC", userId, savedQuestionId);
    }

    /** Deletes only if the submission belongs to the given user. */
    public boolean delete(long userId, long id) {
        String sql = "DELETE FROM code_submission WHERE id = ? AND user_id = ?";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete code submission", e);
        }
    }

    private List<CodeSubmission> query(String sql, Object... params) {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<CodeSubmission> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load code submissions", e);
        }
    }

    private static CodeSubmission map(ResultSet rs) throws SQLException {
        long savedQuestion = rs.getLong("saved_question_id");
        Long savedQuestionId = rs.wasNull() ? null : savedQuestion;
        boolean correct = rs.getBoolean("correct");
        Boolean correctValue = rs.wasNull() ? null : correct;
        return new CodeSubmission(
                rs.getLong("id"),
                rs.getLong("user_id"),
                savedQuestionId,
                rs.getString("question_text"),
                rs.getString("code"),
                rs.getString("language"),
                rs.getString("feedback"),
                correctValue,
                rs.getTimestamp("submitted_at").toInstant());
    }
}
