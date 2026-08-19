package com.aicodinginterviewprep.controller;

import com.aicodinginterviewprep.AppContext;
import com.aicodinginterviewprep.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthenticationController implements ScreenController {

    @FXML
    private TextField textfieldUsername;
    @FXML
    private PasswordField passwordfieldPassword;

    private AppContext context;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
    }

    @FXML
    private void onReturn() {
        context.showHome();
    }

    @FXML
    private void onUsername() {
        passwordfieldPassword.requestFocus();
    }

    @FXML
    private void onPassword() {
        onLogIn();
    }

    @FXML
    private void onLogIn() {
        // No authentication backend yet; return home once one exists to wire up here.
        context.showHome();
    }

    @FXML
    private void onSignUp() {
        // No authentication backend yet; return home once one exists to wire up here.
        context.showHome();
    }
}
