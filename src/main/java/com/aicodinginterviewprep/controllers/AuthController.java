package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AuthController implements SceneAware {
    private static final String ACCOUNTS_FILE = "src/main/resources/authorisation/accounts.json";

    private SceneManager sceneManager;
    private Authenticator authenticator;

    public PasswordField passwordfieldPassword;
    public TextField textfieldUsername;
    public Button buttonLogIn;
    public Button buttonSignUp;
    public Button buttonReturn;
    public Label labelMessage;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.authenticator = new Authenticator(ACCOUNTS_FILE);
    }

    public void onPassword() {
        onLogIn();
    }

    public void onUsername() {
        // Username field logic
    }

    public void onLogIn() {
        String username = textfieldUsername.getText();
        String password = passwordfieldPassword.getText();

        if (!authenticator.login(username, password)) {
            labelMessage.setText("Incorrect username or password.");
            return;
        }
        labelMessage.setText("");
        sceneManager.switchToScene("practice");
    }

    public void onSignUp() {
        String username = textfieldUsername.getText();
        String password = passwordfieldPassword.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            labelMessage.setText("Enter a username and password.");
            return;
        }

        try {
            authenticator.signUp(username, password);
            authenticator.writeUserProfiles();
        } catch (IllegalArgumentException e) {
            labelMessage.setText(e.getMessage());
            return;
        } catch (IOException e) {
            labelMessage.setText("Unable to save account: " + e.getMessage());
            return;
        }

        authenticator.login(username, password);
        labelMessage.setText("");
        sceneManager.switchToScene("practice");
    }

    public void onReturn() {
        sceneManager.switchToScene("home");
    }
}
