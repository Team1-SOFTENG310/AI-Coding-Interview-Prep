package com.aicodinginterviewprep;

import com.aicodinginterviewprep.db.DatabaseMigrator;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        AppErrorHandler.install();
        // Basic JavaFX scaffold for the project setup
        stage.setTitle("AI Coding Interview Prep");
        stage.setWidth(1024);
        stage.setHeight(720);
        stage.setMinWidth(900);
        stage.setMinHeight(650);

        SceneManager sceneManager = new SceneManager(stage);
        sceneManager.switchToScene("home");
        stage.show();
    }

    @Override
    public void init() {
        DatabaseMigrator.migrate();
    }

    public static void main(String[] args) {
        AppErrorHandler.install();
        launch();
    }
}
