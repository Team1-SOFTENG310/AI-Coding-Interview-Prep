package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController implements SceneAware {
    private SceneManager sceneManager;

    public PasswordField passwordfieldPassword;
    public TextField textfieldUsername;
    public Button buttonLogIn;
    public Button buttonSignUp;
    public Button buttonReturn;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void onPassword(ActionEvent actionEvent) {
        // Password field logic
    }

    public void onUsername(ActionEvent actionEvent) {
        // Username field logic
    }

    public void onLogIn(ActionEvent actionEvent) {
        // TODO: Implement login logic
    }

    public void onSignUp(ActionEvent actionEvent) {
        // TODO: Implement sign-up logic
    }

    public void onReturn(ActionEvent actionEvent) {
        sceneManager.switchToScene("home");
    }
}
