package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeController implements SceneAware {
    private SceneManager sceneManager;
    private Authenticator authenticator;

    @FXML public Button buttonGetStarted;
    @FXML public Button buttonSignIn;
    @FXML public Label labelIdentity;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void refreshIdentity() {
        if (authenticator.isSignedIn()) {
            labelIdentity.setText("Logged in as: " + authenticator.getUsername());
        }  else {
            labelIdentity.setText("");
        }
    }

    @FXML
    public void onGetStarted() {
        sceneManager.switchToScene("practice");
    }

    @FXML
    public void onSignIn() {
        sceneManager.switchToScene("authentication");
    }
}
