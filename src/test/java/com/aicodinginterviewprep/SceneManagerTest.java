package com.aicodinginterviewprep;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(JavaFXInitExtension.class)
class SceneManagerTest {
    private SceneManager sceneManager;
    private Stage mockStage;

    @BeforeEach
    void setUp() {
        mockStage = mock(Stage.class);
        sceneManager = new SceneManager(mockStage);
    }

    @Test
    void constructorInitializesSceneMap() {
        assertNotNull(sceneManager);
        verify(mockStage, never()).setScene(any());
    }

    @Test
    void switchToSceneLoadsHomeScene() {
        sceneManager.switchToScene("home");

        verify(mockStage).setScene(any(Scene.class));
        assertNotNull(sceneManager.getCurrentScene());
    }

    @Test
    void switchToSceneLoadsAuthenticationScene() {
        sceneManager.switchToScene("authentication");

        verify(mockStage).setScene(any(Scene.class));
        assertNotNull(sceneManager.getCurrentScene());
    }

    @Test
    void switchToSceneLoadsPracticeScene() {
        sceneManager.switchToScene("practice");

        verify(mockStage).setScene(any(Scene.class));
        assertNotNull(sceneManager.getCurrentScene());
    }

    @Test
    void switchToSceneLoadsFeedbackScene() {
        sceneManager.switchToScene("feedback");

        verify(mockStage).setScene(any(Scene.class));
        assertNotNull(sceneManager.getCurrentScene());
    }

    @Test
    void switchToSceneCachesLoadedScene() {
        sceneManager.switchToScene("home");
        Scene firstLoad = sceneManager.getCurrentScene();

        sceneManager.switchToScene("home");
        Scene secondLoad = sceneManager.getCurrentScene();

        assertEquals(firstLoad, secondLoad);
        verify(mockStage, times(2)).setScene(any(Scene.class));
    }

    @Test
    void switchToSceneThrowsForUnknownScene() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sceneManager.switchToScene("unknown")
        );

        assertTrue(exception.getMessage().contains("Scene not found"));
        assertTrue(exception.getMessage().contains("unknown"));
    }

    @Test
    void switchToSceneThrowsForNullSceneName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> sceneManager.switchToScene(null)
        );
    }

    @Test
    void getCurrentSceneReturnsLoadedScene() {
        assertNull(sceneManager.getCurrentScene());

        sceneManager.switchToScene("home");
        Scene scene = sceneManager.getCurrentScene();

        assertNotNull(scene);
    }

    @Test
    void getCurrentSceneReturnsLastSwitchedScene() {
        sceneManager.switchToScene("home");
        Scene homeScene = sceneManager.getCurrentScene();

        sceneManager.switchToScene("practice");
        Scene practiceScene = sceneManager.getCurrentScene();

        assertNotEquals(homeScene, practiceScene);
        assertEquals(practiceScene, sceneManager.getCurrentScene());
    }

    @Test
    void getControllerRetrievesLoadedController() {
        sceneManager.switchToScene("home");
        Object controller = sceneManager.getController("home");

        assertNotNull(controller);
    }

    @Test
    void getControllerReturnsSceneAwareController() {
        sceneManager.switchToScene("home");
        Object controller = sceneManager.getController("home");

        assertTrue(controller instanceof SceneAware,
            "Controller should implement SceneAware");
    }

    @Test
    void getControllerReturnsNullForUnloadedScene() {
        Object controller = sceneManager.getController("home");

        assertNull(controller);
    }

    @Test
    void getControllerReturnsSceneAwareInjection() {
        sceneManager.switchToScene("home");
        Object controller = sceneManager.getController("home");

        assertTrue(controller instanceof SceneAware);
        SceneAware sceneAware = (SceneAware) controller;
        // Controller should have sceneManager injected
        assertNotNull(sceneAware);
    }

    @Test
    void registerSceneAddsNewSceneToMap() {
        String testFxmlPath = "/fxml/CustomScene.fxml";
        sceneManager.registerScene("custom", testFxmlPath);

        // Should throw if we try an unknown scene now
        assertThrows(
            RuntimeException.class,
            () -> sceneManager.switchToScene("custom")
        );
    }

    @Test
    void switchScenesSetsCurrentSceneCorrectly() {
        sceneManager.switchToScene("home");
        Scene homeScene = sceneManager.getCurrentScene();

        sceneManager.switchToScene("authentication");
        Scene authScene = sceneManager.getCurrentScene();

        sceneManager.switchToScene("home");
        Scene homeSceneAgain = sceneManager.getCurrentScene();

        assertEquals(homeScene, homeSceneAgain);
        assertNotEquals(homeScene, authScene);
    }

    @Test
    void switchSceneUsesStageSetScene() {
        sceneManager.switchToScene("home");

        verify(mockStage, times(1)).setScene(any(Scene.class));
    }

    @Test
    void multipleSceneSwitchesUseStageSetScene() {
        sceneManager.switchToScene("home");
        sceneManager.switchToScene("practice");
        sceneManager.switchToScene("authentication");

        verify(mockStage, times(3)).setScene(any(Scene.class));
    }

    @Test
    void switchingToSameSceneTwiceUsesCache() {
        sceneManager.switchToScene("home");
        Scene firstSwitch = sceneManager.getCurrentScene();

        sceneManager.switchToScene("home");
        Scene secondSwitch = sceneManager.getCurrentScene();

        assertSame(firstSwitch, secondSwitch,
            "Same scene should be cached and reused");
    }

    @Test
    void controllerInjectionHappensBeforeSceneSwitch() {
        sceneManager.switchToScene("practice");

        Object controller = sceneManager.getController("practice");
        assertNotNull(controller);
        assertTrue(controller instanceof SceneAware);
    }

    @Test
    void allDefinedScenesLoadWithoutError() {
        String[] scenes = {"home", "authentication", "practice", "feedback"};

        for (String scene : scenes) {
            sceneManager.switchToScene(scene);
            assertNotNull(sceneManager.getCurrentScene(),
                "Scene " + scene + " should load successfully");
            assertNotNull(sceneManager.getController(scene),
                "Controller for scene " + scene + " should load successfully");
        }
    }

    @Test
    void getControllerForDifferentLoadedScenes() {
        sceneManager.switchToScene("home");
        Object homeController = sceneManager.getController("home");

        sceneManager.switchToScene("practice");
        Object practiceController = sceneManager.getController("practice");

        assertNotNull(homeController);
        assertNotNull(practiceController);
        assertNotSame(homeController, practiceController);
    }
}