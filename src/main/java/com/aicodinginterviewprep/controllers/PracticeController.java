package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.event.ActionEvent;
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

    public void onSubmitAnswer(ActionEvent actionEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onReturn(ActionEvent actionEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
