package com.aicodinginterviewprep.db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

public class SavedQuestionRepository {

    private final ConnectionFactory connections;

    public SavedQuestionRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    public SavedQuestionRepository() {
        this(Database::open);
    }

    /** Bookmarks a question; saving the same text twice returns the existing bookmark. */
    public SavedQuestion save(long userId, String questionText, String questionType, String difficulty) {
        String hash = hash(questionText);
        String sql = "INSERT IGNORE INTO saved_question "
                + "(user_id, question_text, question_hash, question_type, difficulty) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, questionText);
            statement.setString(3, hash);
            statement.setString(4, questionType);
            statement.setString(5, difficulty);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save question", e);
        }
        return findByUserAndHash(userId, hash).orElseThrow();
    }

    public List<SavedQuestion> findByUser(long userId) {
        String sql = "SELECT id, user_id, question_text, question_type, difficulty, created_at "
                + "FROM saved_question WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<SavedQuestion> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load saved questions", e);
        }
    }

    public Optional<SavedQuestion> findByUserAndHash(long userId, String questionHash) {
        String sql = "SELECT id, user_id, question_text, question_type, difficulty, created_at "
                + "FROM saved_question WHERE user_id = ? AND question_hash = ?";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, questionHash);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up saved question", e);
        }
    }

    public boolean exists(long userId, String questionText) {
        return findByUserAndHash(userId, hash(questionText)).isPresent();
    }

    /** Deletes only if the bookmark belongs to the given user. */
    public boolean delete(long userId, long savedQuestionId) {
        String sql = "DELETE FROM saved_question WHERE id = ? AND user_id = ?";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, savedQuestionId);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete saved question", e);
        }
    }

    public static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(text.strip().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static SavedQuestion map(ResultSet rs) throws SQLException {
        return new SavedQuestion(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("question_text"),
                rs.getString("question_type"),
                rs.getString("difficulty"),
                rs.getTimestamp("created_at").toInstant());
    }
}
