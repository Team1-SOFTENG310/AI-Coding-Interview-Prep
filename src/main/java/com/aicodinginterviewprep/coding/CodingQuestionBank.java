package com.aicodinginterviewprep.coding;

import java.util.List;

public final class CodingQuestionBank {

    private static final List<CodingQuestion> QUESTIONS = List.of(

new CodingQuestion(
    "coding-1",
    "Two Sum",
    "Easy",
    "Given an array of integers nums and an integer target, return the indices "
        + "of the two numbers such that they add up to target. Assume exactly one "
        + "solution exists and you may not use the same element twice.",
    "nums = [2, 7, 11, 15], target = 9",
    "[0, 1]",
    "public int[] twoSum(int[] nums, int target) {\n"
        + "    // TODO: write your solution here\n"
        + "    return new int[]{};\n"
        + "}\n"
)

    );

     private CodingQuestionBank() {
    }

    public static List<CodingQuestion> getAll() {
        return QUESTIONS;
    }

}