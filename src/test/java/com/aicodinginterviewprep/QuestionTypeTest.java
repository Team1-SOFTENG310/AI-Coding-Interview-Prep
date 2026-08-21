package com.aicodinginterviewprep;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionTypeTest {

    @Test
    void hasBehaviouralTheoryAndCodingValues() {
        assertEquals(3, QuestionType.values().length);
    }

    @Test
    void toStringReturnsHumanReadableLabel() {
        assertEquals("Behavioural", QuestionType.BEHAVIOURAL.toString());
        assertEquals("Theory", QuestionType.THEORY.toString());
        assertEquals("Coding", QuestionType.CODING.toString());
    }
}
