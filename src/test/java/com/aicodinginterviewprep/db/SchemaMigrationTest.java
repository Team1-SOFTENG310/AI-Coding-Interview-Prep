package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class SchemaMigrationTest {

    @Test
    void migrationsCreateExpectedTables() throws Exception {
        TestDatabase db = new TestDatabase();

        try (Connection c = db.connections().open();
             ResultSet rs = c.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            java.util.Set<String> names = new java.util.HashSet<>();
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            assertTrue(names.containsAll(java.util.Set.of("user_account", "saved_question", "code_submission")));
        }
    }

    @Test
    void deletingUserCascadesToSavedContent() throws Exception {
        TestDatabase db = new TestDatabase();
        UserRepository users = new UserRepository(db.connections());
        SavedQuestionRepository questions = new SavedQuestionRepository(db.connections());
        CodeSubmissionRepository submissions = new CodeSubmissionRepository(db.connections());
        long userId = users.create("alice", "h").id();
        SavedQuestion q = questions.save(userId, "Q", "CODING", "EASY");
        submissions.save(userId, q.id(), "Q", "code", "java", null, null);

        try (Connection c = db.connections().open();
             PreparedStatement ps = c.prepareStatement("DELETE FROM user_account WHERE id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }

        assertTrue(questions.findByUser(userId).isEmpty());
        assertTrue(submissions.findByUser(userId).isEmpty());
    }
}
