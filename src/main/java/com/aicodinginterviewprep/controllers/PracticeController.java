package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PracticeController implements SceneAware {
    private SceneManager sceneManager;

    @FXML public Button buttonReturn;
    @FXML public Button buttonSubmitAnswer;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void onSubmitAnswer() {
        sceneManager.switchToScene("feedback");
    }

    public void onReturn() {
        sceneManager.switchToScene("home");
    }
}
