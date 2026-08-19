package com.aicodinginterviewprep;

import com.aicodinginterviewprep.service.OpenAiQuestionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Shared state and navigation for the screen-based FXML UI: holds the
 * services and in-progress question/answer, and swaps the Stage's Scene
 * root between the Home/Practice/Authentication/Feedback screens.
 */
public class AppContext {

    private final Stage stage;
    private final OpenAiQuestionService questionService;
    private final EvaluatorService evaluatorService;

    private String currentQuestion = "";
    private String currentAnswer = "";

    public AppContext(Stage stage, OpenAiQuestionService questionService, EvaluatorService evaluatorService) {
        this.stage = stage;
        this.questionService = questionService;
        this.evaluatorService = evaluatorService;
    }

    public OpenAiQuestionService getQuestionService() {
        return questionService;
    }

    public EvaluatorService getEvaluatorService() {
        return evaluatorService;
    }

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(String currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public String getCurrentAnswer() {
        return currentAnswer;
    }

    public void setCurrentAnswer(String currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    public void showHome() {
        navigateTo("/fxml/Home.fxml");
    }

    public void showPractice() {
        navigateTo("/fxml/Practice.fxml");
    }

    public void showAuthentication() {
        navigateTo("/fxml/Authentication.fxml");
    }

    public void showFeedback() {
        navigateTo("/fxml/Feedback.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            ScreenController controller = loader.getController();
            controller.setContext(this);

            Scene scene = stage.getScene();
            if (scene == null) {
                stage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load screen: " + fxmlPath, e);
        }
    }
}
