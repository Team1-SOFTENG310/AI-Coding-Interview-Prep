package com.aicodinginterviewprep;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

class SceneManagerTest {

    private SceneManager sceneManager;

    @BeforeAll
    static void initialiseJavaFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // JavaFX already started
        }
    }

    @BeforeEach
    void setUp() {
        sceneManager = new SceneManager(null);
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
    }

    @Test
    void getCurrentScene_beforeAnySceneLoaded_returnsNull() {
        assertNull(sceneManager.getCurrentScene());
    }

    @Test
    void getController_beforeSceneLoaded_returnsNull() {
        assertNull(sceneManager.getController("home"));
    }

    @Test
    void getController_unknownScene_returnsNull() {
        assertNull(sceneManager.getController("does-not-exist"));
    }

    @Test
    void switchToScene_invalidScene_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> sceneManager.switchToScene("does-not-exist")
        );
    }

    @Test
    void switchToScene_invalidScene_exceptionContainsSceneName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sceneManager.switchToScene("wrong-scene")
        );

        assertEquals(
                "Scene not found: wrong-scene",
                exception.getMessage()
        );
    }

    @Test
    void registerScene_customScene_canBeLoaded() {
        runOnFxThread(() -> {

            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene(
                    "test",
                    "/fxml/TestScene.fxml"
            );

            manager.switchToScene("test");

            assertNotNull(manager.getCurrentScene());

            stage.close();
        });
    }

    @Test
    void switchToScene_sameSceneTwice_reusesExistingScene() {
        runOnFxThread(() -> {

            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene(
                    "test",
                    "/fxml/TestScene.fxml"
            );

            manager.switchToScene("test");

            Scene firstScene = manager.getCurrentScene();

            manager.switchToScene("test");

            Scene secondScene = manager.getCurrentScene();

            assertSame(firstScene, secondScene);

            stage.close();
        });
    }

    @Test
    void switchToScene_differentScenes_changesCurrentScene() {
        runOnFxThread(() -> {

            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene(
                    "first",
                    "/fxml/TestScene.fxml"
            );

            manager.registerScene(
                    "second",
                    "/fxml/TestScene2.fxml"
            );

            manager.switchToScene("first");
            Scene firstScene = manager.getCurrentScene();

            manager.switchToScene("second");
            Scene secondScene = manager.getCurrentScene();

            assertNotSame(firstScene, secondScene);

            stage.close();
        });
    }

    @Test
    void switchToScene_switchAwayAndBack_reusesOriginalScene() {
        runOnFxThread(() -> {

            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene(
                    "first",
                    "/fxml/TestScene.fxml"
            );

            manager.registerScene(
                    "second",
                    "/fxml/TestScene2.fxml"
            );

            manager.switchToScene("first");
            Scene originalFirstScene = manager.getCurrentScene();

            manager.switchToScene("second");

            manager.switchToScene("first");
            Scene returnedFirstScene = manager.getCurrentScene();

            assertSame(
                    originalFirstScene,
                    returnedFirstScene
            );

            stage.close();
        });
    }

    @Test
    void switchToScene_sceneAwareController_receivesSceneManager() {
        runOnFxThread(() -> {

            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene(
                    "aware",
                    "/fxml/TestSceneAware.fxml"
            );

            manager.switchToScene("aware");

            Object controllerObject =
                    manager.getController("aware");

            assertNotNull(controllerObject);

            TestSceneController controller =
                    (TestSceneController) controllerObject;

            assertSame(
                    manager,
                    controller.getSceneManager()
            );

            stage.close();
        });
    }

    @Test
    void switchToScene_brokenFXML_throwsRuntimeException() {
        runOnFxThread(() -> {

            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene(
                    "broken",
                    "/fxml/BrokenScene.fxml"
            );

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> manager.switchToScene("broken")
            );

            assertEquals(
                    "Failed to load scene: broken",
                    exception.getMessage()
            );

            assertNotNull(exception.getCause());

            assertTrue(
                    exception.getCause() instanceof IOException
            );

            stage.close();
        });
    }
}