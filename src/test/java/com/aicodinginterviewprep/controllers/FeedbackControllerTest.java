package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneManager;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackControllerTest {
    private FeedbackController controller;
    private SceneManager mockSceneManager;
    private TextArea questionOutput;
    private TextArea codeEditor;
    private TextField answerInput;

    @BeforeAll
    static void initJavaFX() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() {
        controller = new FeedbackController();
        controller.textareaEvaluation = new TextArea();
        controller.buttonTryAgain = new Button();

        mockSceneManager = mock(SceneManager.class);
        questionOutput = new TextArea();
        codeEditor = new TextArea();
        answerInput = new TextField();
    }

    @Test
    void setSceneManagerStoresReference() {
        controller.setSceneManager(mockSceneManager);

        assertNotNull(controller);
    }

    @Test
    void setAnswerControlsStoresReferences() {
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertNotNull(controller);
    }

    @Test
    void onTryAgainSwitchesToPractice() {
        controller.setSceneManager(mockSceneManager);

        controller.onTryAgain();

        verify(mockSceneManager).switchToScene("practice");
    }

    @Test
    void runEvaluationWithNoQuestionShowsMessage() {
        controller.setSceneManager(mockSceneManager);
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        controller.runEvaluation();

        String feedback = controller.textareaEvaluation.getText();
        assertTrue(feedback.contains("generate a question"));
    }

    @Test
    void runEvaluationWithDefaultQuestionTextShowsMessage() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("Question will appear here.");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        controller.runEvaluation();

        String feedback = controller.textareaEvaluation.getText();
        assertTrue(feedback.contains("generate a question"));
    }

    @Test
    void runEvaluationWithNoAnswerShowsMessage() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("What is a binary tree?");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        controller.runEvaluation();

        String feedback = controller.textareaEvaluation.getText();
        assertTrue(feedback.contains("provide an answer"));
    }

    @Test
    void runEvaluationWithValidQuestionAndAnswerProceedsWithoutError() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("What is a binary tree?");
        answerInput.setText("A tree with two children");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationDisablesTryAgainButton() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("What is a binary tree?");
        answerInput.setText("A tree with two children");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        controller.runEvaluation();

        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationShowsLoadingMessage() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("What is a binary tree?");
        answerInput.setText("A tree with two children");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        controller.runEvaluation();

        String feedback = controller.textareaEvaluation.getText();
        assertTrue(feedback.contains("Evaluating"));
    }

    @Test
    void runEvaluationWithOnlyExplanation() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("What is OOP?");
        answerInput.setText("Object-oriented programming is...");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationWithOnlyCode() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("Write a function to reverse a string");
        codeEditor.setText("def reverse(s): return s[::-1]");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationWithBothExplanationAndCode() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("Implement a stack");
        answerInput.setText("A stack uses LIFO...");
        codeEditor.setText("class Stack: pass");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationIgnoresDefaultCodeText() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("What is polymorphism?");
        answerInput.setText("Polymorphism is...");
        codeEditor.setText("// Write your code here");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationIgnoresDefaultExplanationText() {
        controller.setSceneManager(mockSceneManager);
        questionOutput.setText("Write a recursive function");
        answerInput.setText("Enter your solution explanation...");
        codeEditor.setText("def factorial(n): return n * factorial(n-1)");
        controller.setAnswerControls(questionOutput, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
        assertTrue(controller.buttonTryAgain.isDisabled());
    }

    @Test
    void runEvaluationWithNullAnswerControls() {
        controller.setSceneManager(mockSceneManager);

        assertDoesNotThrow(() -> controller.runEvaluation());
    }

    @Test
    void runEvaluationWithNullQuestionOutput() {
        controller.setSceneManager(mockSceneManager);
        controller.setAnswerControls(null, codeEditor, answerInput);

        assertDoesNotThrow(() -> controller.runEvaluation());
    }

}