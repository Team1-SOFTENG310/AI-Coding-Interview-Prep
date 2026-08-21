package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.SceneManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    @BeforeAll
    static void initialiseJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX already started.
        }
    }

    private void runOnFxThreadAndWait(Runnable action) throws Exception {
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

        latch.await();

        if (error.get() != null) {
            if (error.get() instanceof AssertionError assertionError) {
                throw assertionError;
            }
            throw new RuntimeException(error.get());
        }
    }

    @Test
    void onGetStartedSwitchesToAuthenticationScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            HomeController controller = new HomeController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onGetStarted();

            assertEquals("authentication", sceneManager.lastScene);
        });
    }

    private static class FakeSceneManager extends SceneManager {
        String lastScene;

        FakeSceneManager() {
            super(new Stage());
        }

        @Override
        public void switchToScene(String sceneName) {
            lastScene = sceneName;
        }
    }
}
