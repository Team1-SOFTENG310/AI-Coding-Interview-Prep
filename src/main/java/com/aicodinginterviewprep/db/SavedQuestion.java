package com.aicodinginterviewprep.db;

import java.time.Instant;

public record SavedQuestion(long id, long userId, String questionText, String questionType,
                            String difficulty, Instant createdAt) {
}
