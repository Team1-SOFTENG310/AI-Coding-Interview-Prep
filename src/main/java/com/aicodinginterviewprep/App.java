package com.aicodinginterviewprep;

import com.aicodinginterviewprep.service.OpenAiQuestionService;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    private final OpenAiQuestionService questionService;
    private final EvaluatorService evaluatorService;

    public App() {
        this(new OpenAiQuestionService(), new EvaluatorService());
    }

    public App(OpenAiQuestionService questionService, EvaluatorService evaluatorService) {
        this.questionService = questionService;
        this.evaluatorService = evaluatorService;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("AI Coding Interview Prep");

        AppContext context = new AppContext(stage, questionService, evaluatorService);
        context.showHome();

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
