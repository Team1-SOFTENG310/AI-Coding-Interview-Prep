package com.aicodinginterviewprep.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class HistoryRepository {

    private final ConnectionFactory connections;
    private final ObjectMapper objectMapper;

    public HistoryRepository(ConnectionFactory connections) {
        this(connections, new ObjectMapper());
    }

    HistoryRepository(ConnectionFactory connections, ObjectMapper objectMapper) {
        this.connections = connections;
        this.objectMapper = objectMapper;
    }

    public HistoryRepository() {
        this(Database::open);
    }
    //saves into the history table
    public void save(long userId, List<String> topics, Instant historyDate,
                     BigDecimal correctnessScore, BigDecimal efficiencyScore,
                     BigDecimal communicationScore, BigDecimal codeQualityScore) {
        if (topics == null || topics.isEmpty()) {
            throw new IllegalArgumentException("History must contain at least one topic");
        }
        Objects.requireNonNull(historyDate, "History date must not be null");

        String topicsJson = serializeTopics(topics);
        BigDecimal averageScore = average(correctnessScore, efficiencyScore,
                communicationScore, codeQualityScore);
        String sql = "INSERT INTO history "
                + "(user_id, topics, history_date, correctness_score, efficiency_score, "
                + "communication_score, code_quality_score, average_score) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, topicsJson);
            statement.setTimestamp(3, java.sql.Timestamp.from(historyDate));
            setNullableDecimal(statement, 4, correctnessScore);
            setNullableDecimal(statement, 5, efficiencyScore);
            setNullableDecimal(statement, 6, communicationScore);
            setNullableDecimal(statement, 7, codeQualityScore);
            setNullableDecimal(statement, 8, averageScore);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save history", e);
        }
    }
    //needed to convert the list of topics into a JSON string to store in the database
    private String serializeTopics(List<String> topics) {
        try {
            return objectMapper.writeValueAsString(topics);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("History topics could not be serialized", e);
        }
    }

    private static BigDecimal average(BigDecimal... scores) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal score : scores) {
            if (score != null) {
                total = total.add(score);
                count++;
            }
        }
        return count == 0
                ? null
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
    //needed to handle potential null values for the scores when saving to the database
    private static void setNullableDecimal(PreparedStatement statement, int index,
                                           BigDecimal value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

}
