package com.aicodinginterviewprep;

import com.aicodinginterviewprep.ui.AuthView;
import com.aicodinginterviewprep.ui.FeedbackView;
import com.aicodinginterviewprep.ui.HomeView;
import com.aicodinginterviewprep.ui.PracticeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        // Basic JavaFX scaffold for the project setup
        stage.setTitle("AI Coding Interview Prep");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            new HomeView().getTab(),
            new PracticeView().getTab(),
            new AuthView().getTab(),
            new FeedbackView().getTab()
        );

        Scene scene = new Scene(tabPane, 1024, 720);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
