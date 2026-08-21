package com.aicodinginterviewprep;

public class TestSceneController implements SceneAware {

    private SceneManager sceneManager;
    private Authenticator authenticator;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
    @Override
    public void setAuthenticator(Authenticator authenticator) {this.authenticator = authenticator;}

    public SceneManager getSceneManager() {
        return sceneManager;
    }

}