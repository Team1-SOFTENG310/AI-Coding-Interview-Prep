package com.aicodinginterviewprep.ui;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PracticeView {

    private Tab tab;

    public PracticeView() {
        setupPracticeTab();
    }

    private void setupPracticeTab() {
        tab = new Tab("Practice");
        tab.setClosable(false);

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-padding: 16;");

        VBox left = new VBox(10);
        left.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        left.prefWidthProperty().bind(pane.widthProperty().multiply(0.40));
        left.getChildren().addAll(
                new Label("Question Output"),
                new TextArea("Question will appear here.") {{ setPrefRowCount(8); setWrapText(true); setEditable(false); }}
        );

        VBox center = new VBox(10);
        center.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
        center.prefWidthProperty().bind(pane.widthProperty().multiply(0.40));
        center.getChildren().addAll(
                new Label("Code Editor"),
                new TextArea("// Write your code here") {{ setPrefRowCount(20); setWrapText(true); }}
        );

        VBox right = new VBox(10);
        right.setStyle("-fx-padding: 8; -fx-border-width: 0 1 0 0; -fx-border-color: #ddd;");
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
    }

    public Tab getTab() {
        return tab;
    }
}
