package com.aicodinginterviewprep;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("AI Coding Interview Prep");
        stage.setWidth(1024);
        stage.setHeight(720);

        SceneManager sceneManager = new SceneManager(stage);
        sceneManager.switchToScene("home");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
