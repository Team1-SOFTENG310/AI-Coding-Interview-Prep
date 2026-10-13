package com.aicodinginterviewprep;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void getCurrentSceneKey_beforeAnySceneLoaded_returnsNull() {
        assertNull(sceneManager.getCurrentSceneKey());
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
    void switchScene_tracksActiveSceneKey() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("test", "/fxml/TestScene.fxml");

            manager.switchScene("test");

            assertEquals("test", manager.getCurrentSceneKey());
            stage.close();
        });
    }

    @Test
    void pushSceneAndPopScene_returnToPreviousScene() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("first", "/fxml/TestScene.fxml");
            manager.registerScene("second", "/fxml/TestScene2.fxml");

            manager.switchScene("first");
            manager.pushScene("second");

            assertEquals("second", manager.getCurrentSceneKey());
            assertTrue(manager.popScene());
            assertEquals("first", manager.getCurrentSceneKey());
            assertFalse(manager.popScene());
            stage.close();
        });
    }

    @Test
    void switchScene_clearsPushPopHistory() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("home-test", "/fxml/TestScene.fxml");
            manager.registerScene("saved-test", "/fxml/TestScene2.fxml");
            manager.registerScene("practice-test", "/fxml/TestScene.fxml");

            manager.switchScene("home-test");
            manager.pushScene("saved-test");
            manager.switchScene("practice-test");

            assertFalse(manager.popScene());
            assertEquals("practice-test", manager.getCurrentSceneKey());
            stage.close();
        });
    }

    @Test
    void switchScene_updatesActiveStateBeforeLifecycleCallbacks() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("first", "/fxml/TestSceneAware.fxml");
            manager.registerScene("second", "/fxml/TestSceneAware.fxml");
            manager.switchScene("first");
            TestSceneController firstController = (TestSceneController) manager.getController("first");

            manager.switchScene("second");

            TestSceneController secondController = (TestSceneController) manager.getController("second");
            assertEquals("second", firstController.getCurrentSceneKeyWhenHidden());
            assertEquals("second", secondController.getCurrentSceneKeyWhenShown());
            assertSame(manager.getCurrentScene(), stage.getScene());
            stage.close();
        });
    }

    @Test
    void switchScene_navigationRequestedDuringLifecycleCallback_runsAfterCurrentTransition() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("first", "/fxml/TestSceneAware.fxml");
            manager.registerScene("second", "/fxml/TestSceneAware.fxml");
            manager.registerScene("third", "/fxml/TestSceneAware.fxml");
            manager.switchScene("first");
            TestSceneController firstController = (TestSceneController) manager.getController("first");
            firstController.setOnHiddenAction(() -> {
                assertEquals("second", manager.getCurrentSceneKey());
                assertSame(manager.getCurrentScene(), stage.getScene());
                manager.switchScene("third");
            });

            manager.switchScene("second");

            TestSceneController secondController = (TestSceneController) manager.getController("second");
            assertEquals("third", manager.getCurrentSceneKey());
            assertEquals(1, secondController.getSceneShownCount());
            assertSame(manager.getCurrentScene(), stage.getScene());
            stage.close();
        });
    }

    @Test
    void unloadScene_removesCachedSceneAndController() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("first", "/fxml/TestSceneAware.fxml");
            manager.registerScene("second", "/fxml/TestScene2.fxml");
            manager.switchScene("first");
            Scene firstScene = manager.getCurrentScene();
            TestSceneController firstController = (TestSceneController) manager.getController("first");
            manager.switchScene("second");

            assertEquals(1, firstController.getSceneHiddenCount());
            manager.unloadScene("first");
            assertEquals(1, firstController.getSceneDisposedCount());
            manager.switchScene("first");

            assertNotSame(firstScene, manager.getCurrentScene());
            assertNotSame(firstController, manager.getController("first"));
            stage.close();
        });
    }

    @Test
    void unloadScene_activeScene_isRejected() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);
            manager.registerScene("active", "/fxml/TestScene.fxml");
            manager.switchScene("active");

            assertThrows(IllegalStateException.class, () -> manager.unloadScene("active"));
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
    void switchToScene_practiceLoadsTopicSelector() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.switchToScene("practice");

            assertNotNull(manager.getCurrentScene().lookup("#comboTopic"));
            stage.close();
        });
    }

    @Test
    void switchToScene_codingLoadsTopicSelector() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.switchToScene("coding");

            assertNotNull(manager.getCurrentScene().lookup("#comboTopic"));
            stage.close();
        });
    }

    @Test
    void getCurrentUsername_initiallyNull() {
        assertNull(sceneManager.getCurrentUsername());
    }

    @Test
    void setCurrentUsername_thenGetCurrentUsername_returnsIt() {
        sceneManager.setCurrentUsername("gabriel");

        assertEquals("gabriel", sceneManager.getCurrentUsername());
    }

    @Test
    void switchToScene_freshLoad_callsOnSceneShown() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene("aware", "/fxml/TestSceneAware.fxml");
            manager.switchToScene("aware");

            TestSceneController controller = (TestSceneController) manager.getController("aware");

            assertEquals(1, controller.getSceneShownCount());

            stage.close();
        });
    }

    @Test
    void switchToScene_cachedScene_alsoCallsOnSceneShown() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            SceneManager manager = new SceneManager(stage);

            manager.registerScene("aware", "/fxml/TestSceneAware.fxml");
            manager.registerScene("other", "/fxml/TestScene.fxml");

            manager.switchToScene("aware");
            manager.switchToScene("other");
            manager.switchToScene("aware");

            TestSceneController controller = (TestSceneController) manager.getController("aware");

            assertEquals(2, controller.getSceneShownCount());

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
