package com.aicodinginterviewprep.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

public class HomeView {

    private Tab tab;

    public HomeView() {
        setupTab();
    }

    private void setupTab() {
        tab = new Tab("Home");
        tab.setClosable(false);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");
        content.getChildren().addAll(
                new Label("Welcome to AI Coding Interview Prep!"),
                new Label("Use the Practice tab to answer coding questions and receive AI feedback."),
                new Label("Login or register in the Authentication tab to save progress in later assignments.")
        );

        tab.setContent(content);
    }

    public Tab getTab() {
        return tab;
    }
}
