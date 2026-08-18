package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneAware;
import com.aicodinginterviewprep.SceneManager;
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

    public void onTryAgain() {
        sceneManager.switchToScene("practice");
    }
}
