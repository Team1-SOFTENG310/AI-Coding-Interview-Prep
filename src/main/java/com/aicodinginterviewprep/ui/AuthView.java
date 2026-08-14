package com.aicodinginterviewprep.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AuthView {

    private Tab authTab;

    public AuthView() {
        setupTab();
    }

    private void setupTab() {
        authTab = new Tab("Authentication");
        authTab.setClosable(false);

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

        authTab.setContent(authGrid);
    }

    public Tab getTab() {
        return authTab;
    }
}
