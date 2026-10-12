package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.UserRepository;
import com.aicodinginterviewprep.errors.AppErrorHandler;
import com.aicodinginterviewprep.errors.PersistenceException;
import com.aicodinginterviewprep.errors.ValidationException;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AuthController implements SceneAware {
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
        this.authenticator = new Authenticator(new UserRepository());
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
        (passwordVisible ? textfieldPasswordVisible : passwordfieldPassword).requestFocus();
    }

    public void onLogIn() {
        String username = textfieldUsername.getText();
        String password = passwordfieldPassword.getText();

        // strip whitespaces to match authenticator format
        if (username != null) {
            username = username.strip();
        }
        if (password != null) {
            password = password.strip();
        }

        boolean loggedIn;
        try {
            loggedIn = authenticator.login(username, password);
        } catch (PersistenceException e) {
            AppErrorHandler.report(e, "Logging in",
                    message -> labelMessage.setText("Unable to log in: " + message));
            return;
        }
        if (!loggedIn) {
            labelMessage.setText("Incorrect username or password.");
            return;
        }
        sceneManager.setCurrentUsername(username);
        resetForm();
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
        } catch (ValidationException e) {
            AppErrorHandler.report(e, "Validating account details", labelMessage::setText);
            return;
        } catch (PersistenceException e) {
            AppErrorHandler.report(e, "Saving account",
                    message -> labelMessage.setText("Unable to save account: " + message));
            return;
        }

        // strip whitespaces to match authenticator format
        username = username.strip();

        sceneManager.setCurrentUsername(username);
        resetForm();
        sceneManager.switchToScene("practice");
    }

    public void onReturn() {
        resetForm();
        sceneManager.switchToScene("home");
    }

    private void resetForm() {
        textfieldUsername.clear();
        passwordfieldPassword.clear();
        labelMessage.setText("");
        if (passwordVisible) {
            onTogglePasswordVisibility();
        }
    }
}
