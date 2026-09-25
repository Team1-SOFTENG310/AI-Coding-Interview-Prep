package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAccountMigrationTest extends DatabaseTestBase{

    @Test
    void migrationCreatesUserAccountTable() throws Exception {
        try (Connection c = connection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM user_account")) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        try (Connection c = connection()) {
            insertUser(c, "johan", "hash1");

            assertThrows(SQLIntegrityConstraintViolationException.class,
                    () -> insertUser(c, "johan", "hash2"));
        }
    }

    private void insertUser(Connection c, String username, String hash) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO user_account (username, password_hash) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
        }
    }
}
