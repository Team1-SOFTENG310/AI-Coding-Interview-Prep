package com.aicodinginterviewprep.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class FeedbackView {

    private Tab tab;

    public FeedbackView(){
        setupTab();
    }

    private void setupTab(){
        tab = new Tab("AI Feedback");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
                new Label("Evaluation Summary"),
                new TextArea("Feedback from AI will appear here.") {{ setPrefRowCount(16); setWrapText(true); setEditable(false); }}
        );

        tab.setContent(content);
    }

    public Tab getTab() {
        return tab;
    }
}
