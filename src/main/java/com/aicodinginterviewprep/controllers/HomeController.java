package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HomeController implements SceneAware {
    private SceneManager sceneManager;

    @FXML public Button buttonGetStarted;
    @FXML public Button buttonSignIn;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
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
