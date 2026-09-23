package com.aicodinginterviewprep;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyTest {

    @Test
    void hasEasyMediumAndHardValues() {
        assertEquals(3, Difficulty.values().length);
    }

    @Test
    void toStringReturnsHumanReadableLabel() {
        assertEquals("Easy", Difficulty.EASY.toString());
        assertEquals("Medium", Difficulty.MEDIUM.toString());
        assertEquals("Hard", Difficulty.HARD.toString());
    }
}
