package com.aicodinginterviewprep;

import com.aicodinginterviewprep.errors.SceneLoadException;
import com.aicodinginterviewprep.errors.ValidationException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private final Stage stage;
    private final Map<String, String> sceneMap;
    private Scene currentScene;
    private final Map<String, Scene> loadedScenes = new HashMap<>();
    private final Map<String, Object> controllers = new HashMap<>();
    private final Deque<String> sceneHistory = new ArrayDeque<>();
    private final Deque<String> pendingTransitions = new ArrayDeque<>();
    private String currentSceneKey;
    private String currentUsername;
    private boolean transitioning;

    public SceneManager(Stage stage) {
        this.stage = stage;
        this.sceneMap = new HashMap<>();
        initializeSceneMap();
    }

    private void initializeSceneMap() {
        sceneMap.put("home", "/fxml/Home.fxml");
        sceneMap.put("authentication", "/fxml/Authentication.fxml");
        sceneMap.put("practice", "/fxml/Practice.fxml");
        sceneMap.put("coding", "/fxml/Coding.fxml");
        sceneMap.put("feedback", "/fxml/Feedback.fxml");
        sceneMap.put("saved", "/fxml/SavedQuestions.fxml");
        sceneMap.put("history", "/fxml/History.fxml");
    }

    public void switchScene(String sceneName) {
        getOrLoadScene(sceneName);
        sceneHistory.clear();
        requestTransition(sceneName);
    }

    /** Compatibility alias for callers using the original navigation API. */
    public void switchToScene(String sceneName) {
        switchScene(sceneName);
    }

    public void pushScene(String sceneName) {
        getOrLoadScene(sceneName);
        String previousSceneKey = currentSceneKey;
        if (previousSceneKey != null && !previousSceneKey.equals(sceneName)) {
            sceneHistory.push(previousSceneKey);
        }
        requestTransition(sceneName);
    }

    public boolean popScene() {
        if (sceneHistory.isEmpty()) {
            return false;
        }

        String previousSceneKey = sceneHistory.peek();
        getOrLoadScene(previousSceneKey);
        sceneHistory.pop();
        requestTransition(previousSceneKey);
        return true;
    }

    private void requestTransition(String sceneName) {
        pendingTransitions.addLast(sceneName);
        if (transitioning) {
            return;
        }

        transitioning = true;
        try {
            while (!pendingTransitions.isEmpty()) {
                activateScene(pendingTransitions.removeFirst());
            }
        } catch (RuntimeException | Error e) {
            pendingTransitions.clear();
            sceneHistory.clear();
            throw e;
        } finally {
            transitioning = false;
        }
    }

    private void activateScene(String sceneName) {
        Scene scene = loadedScenes.get(sceneName);
        String previousSceneKey = currentSceneKey;
        if (sceneName.equals(previousSceneKey)) {
            notifySceneShown(sceneName);
            return;
        }

        stage.setScene(scene);
        currentScene = scene;
        currentSceneKey = sceneName;
        notifySceneHidden(previousSceneKey);
        notifySceneShown(sceneName);
    }

    /**
     * Removes a cached, inactive scene and its controller so it can be reclaimed.
     * The scene remains registered and will be loaded again on the next visit.
     */
    public void unloadScene(String sceneName) {
        getScenePath(sceneName);
        if (sceneName.equals(currentSceneKey)) {
            throw new IllegalStateException("Cannot unload the active scene: " + sceneName);
        }

        Object controller = controllers.get(sceneName);
        if (controller instanceof SceneAware sceneAware) {
            sceneAware.onSceneDisposed();
        }
        loadedScenes.remove(sceneName);
        controllers.remove(sceneName);
        sceneHistory.removeIf(sceneName::equals);
    }

    private Scene getOrLoadScene(String sceneName) {
        Scene scene = loadedScenes.get(sceneName);
        if (scene != null) {
            return scene;
        }

        String fxmlPath = getScenePath(sceneName);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof SceneAware sceneAware) {
                sceneAware.setSceneManager(this);
            }

            scene = new Scene(root);
            controllers.put(sceneName, controller);
            loadedScenes.put(sceneName, scene);
            return scene;
        } catch (IOException e) {
            throw new SceneLoadException("Failed to load scene: " + sceneName, e);
        }
    }

    private String getScenePath(String sceneName) {
        String fxmlPath = sceneMap.get(sceneName);
        if (fxmlPath == null) {
            throw new ValidationException("Scene not found: " + sceneName);
        }
        return fxmlPath;
    }

    private void notifySceneShown(String sceneName) {
        if (sceneName == null) {
            return;
        }
        Object controller = controllers.get(sceneName);
        if (controller instanceof SceneAware sceneAware) {
            sceneAware.onSceneShown();
        }
    }

    private void notifySceneHidden(String sceneName) {
        if (sceneName == null) {
            return;
        }
        Object controller = controllers.get(sceneName);
        if (controller instanceof SceneAware sceneAware) {
            sceneAware.onSceneHidden();
        }
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public String getCurrentSceneKey() {
        return currentSceneKey;
    }

    public Object getController(String sceneName) {
        return controllers.get(sceneName);
    }

    public void registerScene(String name, String fxmlPath) {
        sceneMap.put(name, fxmlPath);
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
