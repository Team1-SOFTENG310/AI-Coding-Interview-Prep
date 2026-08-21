package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController implements SceneAware {
    private SceneManager sceneManager;
    private final Authenticator authenticator = new Authenticator("src/main/resources/authorisation/accounts.json");

    public PasswordField passwordFieldPassword;
    public TextField textfieldUsername;
    public Button buttonLogIn;
    public Button buttonSignUp;
    public Button buttonReturn;
    public Label labelAuthenticationMessage;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void onPassword() {
        // Password field logic
        labelAuthenticationMessage.setText("Choose Log In or Sign Up");
    }

    public void onUsername() {
        // Username field logic
        labelAuthenticationMessage.setText("Choose Log In or Sign Up");
    }

    public void onLogIn() {
        try {
            authenticator.login(textfieldUsername.getText().trim(), passwordFieldPassword.getText().trim());
            labelAuthenticationMessage.setText("Logged in as: " + authenticator.getUsername());
        } catch (Exception e) {
            labelAuthenticationMessage.setText(e.getMessage());
        }
    }

    public void onSignUp() {
        System.out.println(textfieldUsername.getText() + passwordFieldPassword.getText());
        try {
            authenticator.signUp(textfieldUsername.getText().trim(), passwordFieldPassword.getText().trim());
            authenticator.writeUserProfiles();
            labelAuthenticationMessage.setText("Signed up as: " + authenticator.getUsername());
        }  catch (Exception e) {
            labelAuthenticationMessage.setText(e.getMessage());
        }
    }

    public void onReturn() {
        sceneManager.switchToScene("home");
    }
}
