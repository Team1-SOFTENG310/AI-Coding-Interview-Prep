package com.aicodinginterviewprep;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        // Basic JavaFX scaffold for the project setup
        stage.setTitle("AI Coding Interview Prep");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createHomeTab(),
            createPracticeTab(),
            createAuthTab(),
            createFeedbackTab()
        );

        Scene scene = new Scene(tabPane, 1024, 720);
        stage.setScene(scene);
        stage.show();
    }

    private Tab createHomeTab() {
        Tab tab = new Tab("Home");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Welcome to AI Coding Interview Prep!"),
            new Label("Use the Practice tab to answer coding questions and receive AI feedback."),
            new Label("Login or register in the Authentication tab to save progress in later assignments.")
        );

        tab.setContent(content);
        return tab;
    }

    private Tab createPracticeTab() {
        Tab tab = new Tab("Practice");
        tab.setClosable(false);

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-padding: 16;");

        VBox left = new VBox(10);
        left.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        left.getChildren().addAll(
            new Label("Question Output"),
            new TextArea("Question will appear here.") {{ setPrefRowCount(8); setWrapText(true); setEditable(false); }}
        );

        VBox center = new VBox(10);
        center.getChildren().addAll(
            new Label("Code Editor"),
            new TextArea("// Write your code here") {{ setPrefRowCount(20); setWrapText(true); }}
        );

        VBox right = new VBox(10);
        right.getChildren().addAll(
            new Label("Answer Submission"),
            new TextField("Enter your solution explanation..."),
            new Button("Submit Answer")
        );

        HBox bottom = new HBox(12);
        bottom.getChildren().addAll(
            new Button("Generate New Question"),
            new Button("Run AI Evaluation")
        );

        pane.setLeft(left);
        pane.setCenter(center);
        pane.setRight(right);
        pane.setBottom(bottom);
        tab.setContent(pane);
        return tab;
    }

    private Tab createAuthTab() {
        Tab tab = new Tab("Authentication");
        tab.setClosable(false);

        GridPane authGrid = new GridPane();
        authGrid.setHgap(10);
        authGrid.setVgap(10);
        authGrid.setStyle("-fx-padding: 20;");

        authGrid.add(new Label("Username:"), 0, 0);
        authGrid.add(new TextField(), 1, 0);
        authGrid.add(new Label("Password:"), 0, 1);
        authGrid.add(new TextField(), 1, 1);
        authGrid.add(new Button("Login"), 0, 2);
        authGrid.add(new Button("Register"), 1, 2);

        tab.setContent(authGrid);
        return tab;
    }

    private Tab createFeedbackTab() {
        Tab tab = new Tab("AI Feedback");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
            new Label("Evaluation Summary"),
            new TextArea("Feedback from AI will appear here.") {{ setPrefRowCount(16); setWrapText(true); setEditable(false); }}
        );

        tab.setContent(content);
        return tab;
    }

    public static void main(String[] args) {
        launch();
    }
}
