package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class FeedbackController implements SceneAware {
    private SceneManager sceneManager;

    public TextArea textareaEvaluation;
    public Button buttonTryAgain;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public void onTryAgain(ActionEvent actionEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
