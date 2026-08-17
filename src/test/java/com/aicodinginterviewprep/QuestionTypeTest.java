package com.aicodinginterviewprep;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionTypeTest {

    @Test
    void hasBehaviouralAndTheoryValues() {
        assertEquals(2, QuestionType.values().length);
    }

    @Test
    void toStringReturnsHumanReadableLabel() {
        assertEquals("Behavioural", QuestionType.BEHAVIOURAL.toString());
        assertEquals("Theory", QuestionType.THEORY.toString());
    }
}
