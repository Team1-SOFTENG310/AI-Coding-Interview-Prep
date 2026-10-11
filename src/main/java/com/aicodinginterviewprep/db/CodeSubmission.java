package com.aicodinginterviewprep.db;

import java.time.Instant;

public record CodeSubmission(long id, long userId, Long savedQuestionId, String questionText,
                             String code, String language, String feedback, Boolean correct,
                             Instant submittedAt) {
}
