package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;

public class AuthController implements SceneAware {
    private static final String ACCOUNTS_FILE = "src/main/resources/authorisation/accounts.json";

    private SceneManager sceneManager;
    private Authenticator authenticator;
    private boolean passwordVisible = false;

    public PasswordField passwordfieldPassword;
    public TextField textfieldPasswordVisible;
    public TextField textfieldUsername;
    public Button buttonLogIn;
    public Button buttonSignUp;
    public Button buttonReturn;
    public Hyperlink linkTogglePassword;
    public Label labelMessage;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.authenticator = new Authenticator(ACCOUNTS_FILE);
        textfieldPasswordVisible.textProperty().bindBidirectional(passwordfieldPassword.textProperty());
        textfieldPasswordVisible.setVisible(false);
        textfieldPasswordVisible.setManaged(false);
        linkTogglePassword.setText("Show");

        textfieldUsername.setOnKeyPressed(this::focusPasswordFieldOnTab);
        passwordfieldPassword.setOnKeyPressed(this::focusTogglePasswordOnTab);
        textfieldPasswordVisible.setOnKeyPressed(this::focusTogglePasswordOnTab);
    }

    private void focusPasswordFieldOnTab(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
            event.consume();
            (passwordVisible ? textfieldPasswordVisible : passwordfieldPassword).requestFocus();
        }
    }

    private void focusTogglePasswordOnTab(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
            event.consume();
            linkTogglePassword.requestFocus();
        }
    }

    public void onTogglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        passwordfieldPassword.setVisible(!passwordVisible);
        passwordfieldPassword.setManaged(!passwordVisible);
        textfieldPasswordVisible.setVisible(passwordVisible);
        textfieldPasswordVisible.setManaged(passwordVisible);

        linkTogglePassword.setText(passwordVisible ? "Hide" : "Show");
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
        textfieldUsername.clear();
        passwordfieldPassword.clear();
        labelMessage.setText("");
        if (passwordVisible) {
            onTogglePasswordVisibility();
        }
        sceneManager.switchToScene("home");
    }
}
