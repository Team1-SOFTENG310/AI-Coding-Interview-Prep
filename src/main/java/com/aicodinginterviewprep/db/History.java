package com.aicodinginterviewprep.db;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record History(
        long id,
        long userId,
        List<String> topics,
        Instant historyDate,
        BigDecimal correctnessScore,
        BigDecimal efficiencyScore,
        BigDecimal communicationScore,
        BigDecimal codeQualityScore,
        BigDecimal averageScore) {
}
