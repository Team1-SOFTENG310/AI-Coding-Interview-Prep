package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryRepositoryTest {

    @Test
    void savePersistsTopicsAndAveragesApplicableScores() {
        ConnectionFactory connections = new TestDatabase().connections();
        long userId = new UserRepository(connections).create("alice", "h").id();
        HistoryRepository history = new HistoryRepository(connections);

        history.save(
                userId,
                List.of("Arrays", "Graphs"),
                new BigDecimal("8"),
                null,
                new BigDecimal("9"),
                new BigDecimal("7"));

        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT topics, efficiency_score, average_score FROM history WHERE user_id = ?")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertTrue(result.getString("topics").contains("Arrays"));
                assertNull(result.getBigDecimal("efficiency_score"));
                assertEquals(new BigDecimal("8.00"), result.getBigDecimal("average_score"));
            }
        } catch (Exception e) {
            fail("Could not verify saved history", e);
        }
    }

    @Test
    void saveRejectsEmptyTopics() {
        ConnectionFactory connections = new TestDatabase().connections();
        long userId = new UserRepository(connections).create("alice", "h").id();
        HistoryRepository history = new HistoryRepository(connections);

        assertThrows(IllegalArgumentException.class,
                () -> history.save(userId, List.of(), null, null, null, null));
    }
}
