package com.aicodinginterviewprep.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserRepository {

    private final ConnectionFactory connections;

    public UserRepository(ConnectionFactory connections) {
        this.connections = connections;
    }

    public UserRepository() {
        this(Database::open);
    }

    /** Inserts a new account; throws {@link DuplicateUsernameException} if the username is taken. */
    public UserAccount create(String username, String passwordHash) {
        String sql = "INSERT INTO user_account (username, password_hash) VALUES (?, ?)";
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return findById(keys.getLong(1)).orElseThrow();
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("uq_user_account_username")) {
                throw new DuplicateUsernameException(username, e);
            }
            throw new DataAccessException("Failed to create user " + username, e);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user " + username, e);
        }
    }

    public Optional<UserAccount> findByUsername(String username) {
        return findOne("SELECT id, username, password_hash, created_at FROM user_account WHERE username = ?",
                statement -> statement.setString(1, username));
    }

    public Optional<UserAccount> findById(long id) {
        return findOne("SELECT id, username, password_hash, created_at FROM user_account WHERE id = ?",
                statement -> statement.setLong(1, id));
    }

    private interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private Optional<UserAccount> findOne(String sql, Binder binder) {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserAccount(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("created_at").toInstant()));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query user_account", e);
        }
    }

    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String username, Throwable cause) {
            super("Username already exists: " + username, cause);
        }
    }
}
