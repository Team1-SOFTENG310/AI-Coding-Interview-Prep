package com.aicodinginterviewprep;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private final Stage stage;
    private final Map<String, String> sceneMap;
    private Scene currentScene;
    private Map<String, Scene> loadedScenes =  new HashMap<>();
    private Map<String, Object> controllers = new HashMap<>();

    public SceneManager(Stage stage) {
        this.stage = stage;
        this.sceneMap = new HashMap<>();
        initializeSceneMap();
    }

    private void initializeSceneMap() {
        sceneMap.put("home", "/fxml/Home.fxml");
        sceneMap.put("authentication", "/fxml/Authentication.fxml");
        sceneMap.put("practice", "/fxml/Practice.fxml");
        sceneMap.put("feedback", "/fxml/Feedback.fxml");
    }

    public void switchToScene(String sceneName) {
        if (loadedScenes.containsKey(sceneName)) {
            Scene scene = loadedScenes.get(sceneName);
            currentScene = scene;
            stage.setScene(scene);
            return;
        }

        String fxmlPath = sceneMap.get(sceneName);
        if (fxmlPath == null) {
            throw new IllegalArgumentException("Scene not found: " + sceneName);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            controllers.put(sceneName, controller);
            if (controller instanceof SceneAware) {
                ((SceneAware) controller).setSceneManager(this);
            }

            Scene scene = new Scene(root);

            loadedScenes.put(sceneName, scene);
            currentScene = scene;
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + sceneName, e);
        }
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public Object getController(String sceneName) {
        return controllers.get(sceneName);
    }

    public void registerScene(String name, String fxmlPath) {
        sceneMap.put(name, fxmlPath);
    }
}
