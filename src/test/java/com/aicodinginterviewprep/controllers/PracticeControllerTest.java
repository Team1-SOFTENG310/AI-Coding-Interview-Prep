package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.QuestionType;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.service.OpenAiQuestionService;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PracticeControllerTest {
    private PracticeController controller;
    private SceneManager mockSceneManager;

    @BeforeAll
    static void initJavaFX() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() {
        controller = new PracticeController();
        controller.practiceRoot = new BorderPane();
        controller.questionOutput = new TextArea();
        controller.codeEditor = new TextArea();
        controller.answerInput = new TextField();
        controller.buttonReturn = new Button();
        controller.buttonSubmitAnswer = new Button();
        controller.buttonRunEvaluation = new Button();
        controller.buttonGenerateQuestion = new Button();
        controller.comboQuestionType = new ComboBox<>();

        mockSceneManager = mock(SceneManager.class);
    }

    @Test
    void setSceneManagerInitializesComboBox() {
        controller.setSceneManager(mockSceneManager);

        assertEquals(QuestionType.BEHAVIOURAL, controller.comboQuestionType.getValue());
        assertTrue(controller.comboQuestionType.getItems().containsAll(java.util.Arrays.asList(QuestionType.values())));
    }

    @Test
    void onGenerateQuestionDisablesButton() {
        controller.setSceneManager(mockSceneManager);

        controller.onGenerateQuestion();

        assertTrue(controller.buttonGenerateQuestion.isDisabled());
    }

    @Test
    void onGenerateQuestionSetsLoadingText() {
        controller.setSceneManager(mockSceneManager);

        controller.onGenerateQuestion();

        assertEquals("Generating question...", controller.questionOutput.getText());
    }

    @Test
    void onGenerateQuestionStartsAsyncTask() {
        controller.setSceneManager(mockSceneManager);

        controller.onGenerateQuestion();

        assertTrue(controller.buttonGenerateQuestion.isDisabled());
    }

    @Test
    void onGenerateQuestionWithBehaviouralType() {
        controller.setSceneManager(mockSceneManager);
        controller.comboQuestionType.setValue(QuestionType.BEHAVIOURAL);

        controller.onGenerateQuestion();

        assertEquals(QuestionType.BEHAVIOURAL, controller.comboQuestionType.getValue());
    }

    @Test
    void onGenerateQuestionWithTheoryType() {
        controller.setSceneManager(mockSceneManager);
        controller.comboQuestionType.setValue(QuestionType.THEORY);

        controller.onGenerateQuestion();

        assertEquals(QuestionType.THEORY, controller.comboQuestionType.getValue());
    }

    @Test
    void onSubmitAnswerSwitchesToFeedback() {
        controller.setSceneManager(mockSceneManager);
        FeedbackController feedbackController = mock(FeedbackController.class);
        when(mockSceneManager.getController("feedback")).thenReturn(feedbackController);

        controller.onSubmitAnswer();

        verify(mockSceneManager).switchToScene("feedback");
    }

    @Test
    void onSubmitAnswerPassesControlsToFeedback() {
        controller.setSceneManager(mockSceneManager);
        FeedbackController feedbackController = mock(FeedbackController.class);
        when(mockSceneManager.getController("feedback")).thenReturn(feedbackController);

        controller.onSubmitAnswer();

        verify(feedbackController).setAnswerControls(
            controller.questionOutput,
            controller.codeEditor,
            controller.answerInput
        );
    }

    @Test
    void onSubmitAnswerRunsEvaluation() {
        controller.setSceneManager(mockSceneManager);
        FeedbackController feedbackController = mock(FeedbackController.class);
        when(mockSceneManager.getController("feedback")).thenReturn(feedbackController);

        controller.onSubmitAnswer();

        verify(feedbackController).runEvaluation();
    }

    @Test
    void onSubmitAnswerHandlesNonFeedbackController() {
        controller.setSceneManager(mockSceneManager);
        when(mockSceneManager.getController("feedback")).thenReturn("not a controller");

        assertDoesNotThrow(() -> controller.onSubmitAnswer());
    }

    @Test
    void onSubmitAnswerHandlesNullController() {
        controller.setSceneManager(mockSceneManager);
        when(mockSceneManager.getController("feedback")).thenReturn(null);

        assertDoesNotThrow(() -> controller.onSubmitAnswer());
    }

    @Test
    void onReturnSwitchesToHome() {
        controller.setSceneManager(mockSceneManager);

        controller.onReturn();

        verify(mockSceneManager).switchToScene("home");
    }
}