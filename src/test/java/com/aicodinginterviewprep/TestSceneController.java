package com.aicodinginterviewprep;

public class TestSceneController implements SceneAware {

    private SceneManager sceneManager;
    private int sceneShownCount = 0;
    private int sceneHiddenCount = 0;
    private int sceneDisposedCount = 0;
    private String currentSceneKeyWhenHidden;
    private String currentSceneKeyWhenShown;
    private Runnable onHiddenAction;

    @Override
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void onSceneShown() {
        sceneShownCount++;
        currentSceneKeyWhenShown = sceneManager.getCurrentSceneKey();
    }

    @Override
    public void onSceneHidden() {
        sceneHiddenCount++;
        currentSceneKeyWhenHidden = sceneManager.getCurrentSceneKey();
        if (onHiddenAction != null) {
            onHiddenAction.run();
        }
    }

    @Override
    public void onSceneDisposed() {
        sceneDisposedCount++;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public int getSceneShownCount() {
        return sceneShownCount;
    }

    public int getSceneHiddenCount() {
        return sceneHiddenCount;
    }

    public int getSceneDisposedCount() {
        return sceneDisposedCount;
    }

    public String getCurrentSceneKeyWhenHidden() {
        return currentSceneKeyWhenHidden;
    }

    public String getCurrentSceneKeyWhenShown() {
        return currentSceneKeyWhenShown;
    }

    public void setOnHiddenAction(Runnable onHiddenAction) {
        this.onHiddenAction = onHiddenAction;
    }
}