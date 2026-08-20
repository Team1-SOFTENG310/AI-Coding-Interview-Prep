package com.aicodinginterviewprep.coding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CodingQuestionTest {

    private static final CodingQuestion SAMPLE = new CodingQuestion(
            "coding-test-1",
            "Sample Question",
            "Easy",
            "A sample description.",
            "input = [1, 2, 3]",
            "output = 6",
            "public int solve() {\n    return 0;\n}\n"
    );

    @Test
    void constructorStoresAllFieldsExactly() {
        assertEquals("coding-test-1", SAMPLE.getId());
        assertEquals("Sample Question", SAMPLE.getTitle());
        assertEquals("Easy", SAMPLE.getDifficulty());
        assertEquals("A sample description.", SAMPLE.getDescription());
        assertEquals("input = [1, 2, 3]", SAMPLE.getExampleInput());
        assertEquals("output = 6", SAMPLE.getExampleOutput());
        assertEquals("public int solve() {\n    return 0;\n}\n", SAMPLE.getStarterCode());
    }

    @Test
    void getListLabelCombinesTitleAndDifficulty() {
        assertEquals("Sample Question (Easy)", SAMPLE.getListLabel());
    }

    @Test
    void toStringMatchesGetListLabel() {
        // ListView relies on toString() to render each row, so these must stay in sync.
        assertEquals(SAMPLE.getListLabel(), SAMPLE.toString());
    }

    @Test
    void differentDifficultyProducesDifferentLabel() {
        CodingQuestion hard = new CodingQuestion(
                "coding-test-2", "Sample Question", "Hard",
                "desc", "in", "out", "code"
        );
        assertEquals("Sample Question (Hard)", hard.getListLabel());
    }
}