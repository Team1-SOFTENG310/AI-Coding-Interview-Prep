package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.db.History;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryControllerTest {

    @Test
    void describesTopicsAndScoresForHistoryEntry() {
        History history = new History(
                1,
                2,
                List.of("Arrays", "Graphs"),
                Instant.parse("2026-10-13T00:00:00Z"),
                new BigDecimal("8.00"),
                null,
                new BigDecimal("9.50"),
                new BigDecimal("7.00"),
                new BigDecimal("8.17"));

        assertEquals("Arrays\nGraphs", HistoryController.describeTopics(history));
        assertEquals(
                "Correctness: 8.00\n"
                        + "Efficiency: N/A\n"
                        + "Communication: 9.50\n"
                        + "Code quality: 7.00\n"
                        + "Average: 8.17",
                HistoryController.describeScores(history));
    }
}
