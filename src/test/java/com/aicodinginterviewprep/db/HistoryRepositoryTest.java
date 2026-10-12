package com.aicodinginterviewprep.db;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
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
                Instant.parse("2026-10-13T00:33:55Z"),
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
    void savePersistsProvidedHistoryDate() {
        ConnectionFactory connections = new TestDatabase().connections();
        long userId = new UserRepository(connections).create("alice", "h").id();
        HistoryRepository history = new HistoryRepository(connections);
        Instant historyDate = Instant.parse("2026-10-13T00:33:55Z");

        history.save(userId, List.of("Arrays"), historyDate,
                new BigDecimal("8"), null, null, null);

        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT history_date FROM history WHERE user_id = ?")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(historyDate, result.getTimestamp("history_date").toInstant());
            }
        } catch (Exception e) {
            fail("Could not verify saved history date", e);
        }
    }

    @Test
    void findByUserReturnsHistoryDatesInDescendingOrder() {
        ConnectionFactory connections = new TestDatabase().connections();
        UserRepository users = new UserRepository(connections);
        long userId = users.create("alice", "h").id();
        long otherUserId = users.create("bob", "h").id();
        HistoryRepository history = new HistoryRepository(connections);

        history.save(userId, List.of("Arrays"), Instant.parse("2026-10-12T00:00:00Z"),
                new BigDecimal("8"), null, null, null);
        history.save(userId, List.of("Graphs"), Instant.parse("2026-10-13T00:00:00Z"),
                new BigDecimal("9"), null, null, null);
        history.save(otherUserId, List.of("Trees"), Instant.parse("2026-10-14T00:00:00Z"),
                new BigDecimal("10"), null, null, null);

        List<History> histories = history.findByUser(userId);

        assertEquals(2, histories.size());
        assertEquals(Instant.parse("2026-10-13T00:00:00Z"), histories.get(0).historyDate());
        assertEquals(List.of("Graphs"), histories.get(0).topics());
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), histories.get(1).historyDate());
    }

    @Test
    void saveRejectsEmptyTopics() {
        ConnectionFactory connections = new TestDatabase().connections();
        long userId = new UserRepository(connections).create("alice", "h").id();
        HistoryRepository history = new HistoryRepository(connections);

        assertThrows(IllegalArgumentException.class,
                () -> history.save(userId, List.of(), Instant.now(), null, null, null, null));
    }
}
