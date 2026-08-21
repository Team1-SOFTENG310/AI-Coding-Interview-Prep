package com.aicodinginterviewprep;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Basic JavaFX scaffold for the project setup
        stage.setTitle("AI Coding Interview Prep");
        stage.setWidth(1024);
        stage.setHeight(720);

        Authenticator authenticator =
                new Authenticator("src/main/resources/authorisation/accounts.json");
        SceneManager sceneManager = new SceneManager(stage, authenticator);
        sceneManager.switchToScene("home");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
