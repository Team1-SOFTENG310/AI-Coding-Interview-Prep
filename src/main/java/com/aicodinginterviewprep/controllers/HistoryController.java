package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class HistoryController implements SceneAware {
    private SceneManager sceneManager;

    @FXML
    public ListView<?> listQuestions;
    @FXML
    public ListView<?> listSubmissions1;
    @FXML
    public ListView<?> listSubmissions;
    @FXML
    public Button buttonBack;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    public void onBack() {
        sceneManager.switchToScene("practice");
    }
}
