package com.aicodinginterviewprep;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SceneManagerTest {
    private SceneManager sceneManager;
    private Stage mockStage;

    @BeforeEach
    void setUp() {
        mockStage = mock(Stage.class);
        sceneManager = new SceneManager(mockStage);
    }

    private MockedConstruction<FXMLLoader> mockFXMLLoader() {
        return mockConstruction(FXMLLoader.class, (mock, context) -> {
            Parent mockRoot = mock(Parent.class);
            ObservableList<String> styleClass = FXCollections.observableArrayList();
            when(mockRoot.getStyleClass()).thenReturn(styleClass);
            when(mock.load()).thenReturn(mockRoot);
            when(mock.getController()).thenReturn(mock(SceneAware.class));
        });
    }

    @Test
    void constructorInitializesSceneMap() {
        assertNotNull(sceneManager);
        verify(mockStage, never()).setScene(any());
    }

    @Test
    void switchToSceneLoadsHomeScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");

            verify(mockStage).setScene(any(Scene.class));
            assertNotNull(sceneManager.getCurrentScene());
        }
    }

    @Test
    void switchToSceneLoadsAuthenticationScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("authentication");

            verify(mockStage).setScene(any(Scene.class));
            assertNotNull(sceneManager.getCurrentScene());
        }
    }

    @Test
    void switchToSceneLoadsPracticeScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("practice");

            verify(mockStage).setScene(any(Scene.class));
            assertNotNull(sceneManager.getCurrentScene());
        }
    }

    @Test
    void switchToSceneLoadsFeedbackScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("feedback");

            verify(mockStage).setScene(any(Scene.class));
            assertNotNull(sceneManager.getCurrentScene());
        }
    }

    @Test
    void switchToSceneCachesLoadedScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Scene firstLoad = sceneManager.getCurrentScene();

            sceneManager.switchToScene("home");
            Scene secondLoad = sceneManager.getCurrentScene();

            assertEquals(firstLoad, secondLoad);
            verify(mockStage, times(2)).setScene(any(Scene.class));
        }
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

        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Scene scene = sceneManager.getCurrentScene();

            assertNotNull(scene);
        }
    }

    @Test
    void getCurrentSceneReturnsLastSwitchedScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Scene homeScene = sceneManager.getCurrentScene();

            sceneManager.switchToScene("practice");
            Scene practiceScene = sceneManager.getCurrentScene();

            assertNotEquals(homeScene, practiceScene);
            assertEquals(practiceScene, sceneManager.getCurrentScene());
        }
    }

    @Test
    void getControllerRetrievesLoadedController() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Object controller = sceneManager.getController("home");

            assertNotNull(controller);
        }
    }

    @Test
    void getControllerReturnsSceneAwareController() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Object controller = sceneManager.getController("home");

            assertInstanceOf(SceneAware.class, controller);
        }
    }

    @Test
    void getControllerReturnsNullForUnloadedScene() {
        Object controller = sceneManager.getController("home");

        assertNull(controller);
    }

    @Test
    void getControllerReturnsSceneAwareInjection() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Object controller = sceneManager.getController("home");

            assertInstanceOf(SceneAware.class, controller);
        }
    }

    @Test
    void registerSceneAddsNewSceneToMap() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            String testFxmlPath = "/fxml/CustomScene.fxml";
            sceneManager.registerScene("custom", testFxmlPath);

            sceneManager.switchToScene("custom");
            assertNotNull(sceneManager.getCurrentScene());
        }
    }

    @Test
    void switchScenesSetsCurrentSceneCorrectly() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Scene homeScene = sceneManager.getCurrentScene();

            sceneManager.switchToScene("authentication");
            Scene authScene = sceneManager.getCurrentScene();

            sceneManager.switchToScene("home");
            Scene homeSceneAgain = sceneManager.getCurrentScene();

            assertEquals(homeScene, homeSceneAgain);
            assertNotEquals(homeScene, authScene);
        }
    }

    @Test
    void switchSceneUsesStageSetScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");

            verify(mockStage, times(1)).setScene(any(Scene.class));
        }
    }

    @Test
    void multipleSceneSwitchesUseStageSetScene() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            sceneManager.switchToScene("practice");
            sceneManager.switchToScene("authentication");

            verify(mockStage, times(3)).setScene(any(Scene.class));
        }
    }

    @Test
    void switchingToSameSceneTwiceUsesCache() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Scene firstSwitch = sceneManager.getCurrentScene();

            sceneManager.switchToScene("home");
            Scene secondSwitch = sceneManager.getCurrentScene();

            assertSame(firstSwitch, secondSwitch,
                "Same scene should be cached and reused");
        }
    }

    @Test
    void controllerInjectionHappensBeforeSceneSwitch() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("practice");

            Object controller = sceneManager.getController("practice");
            assertNotNull(controller);
            assertInstanceOf(SceneAware.class, controller);
        }
    }

    @Test
    void allDefinedScenesLoadWithoutError() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            String[] scenes = {"home", "authentication", "practice", "feedback"};

            for (String scene : scenes) {
                sceneManager.switchToScene(scene);
                assertNotNull(sceneManager.getCurrentScene(),
                    "Scene " + scene + " should load successfully");
                assertNotNull(sceneManager.getController(scene),
                    "Controller for scene " + scene + " should load successfully");
            }
        }
    }

    @Test
    void getControllerForDifferentLoadedScenes() {
        try (MockedConstruction<FXMLLoader> mocked = mockFXMLLoader()) {
            sceneManager.switchToScene("home");
            Object homeController = sceneManager.getController("home");

            sceneManager.switchToScene("practice");
            Object practiceController = sceneManager.getController("practice");

            assertNotNull(homeController);
            assertNotNull(practiceController);
            assertNotSame(homeController, practiceController);
        }
    }
}