package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
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

    public void onPassword() {
        // Password field logic
    }

    public void onUsername() {
        // Username field logic
    }

    public void onLogIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onSignUp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onReturn() {
        sceneManager.switchToScene("home");
    }
}
