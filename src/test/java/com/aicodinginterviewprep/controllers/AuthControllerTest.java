package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneManager;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {

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

    private AuthController createController() {
        AuthController controller = new AuthController();

        controller.textfieldUsername = new TextField();
        controller.passwordfieldPassword = new PasswordField();
        controller.buttonLogIn = new Button();
        controller.buttonSignUp = new Button();
        controller.buttonReturn = new Button();
        controller.labelMessage = new Label();

        return controller;
    }

    private void useTempAuthenticator(AuthController controller, Path accountsFile) throws Exception {
        Field field = AuthController.class.getDeclaredField("authenticator");
        field.setAccessible(true);
        field.set(controller, new Authenticator(accountsFile.toString()));
    }

    @Test
    void signUpWithNewAccountNavigatesToPractice(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTempAuthenticator(controller, tempDir.resolve("accounts.json"));

                controller.textfieldUsername.setText("alice");
                controller.passwordfieldPassword.setText("secret123");
                controller.onSignUp();

                assertEquals("practice", sceneManager.lastScene);
                assertEquals("", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithBlankUsernameShowsMessageAndDoesNotNavigate(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTempAuthenticator(controller, tempDir.resolve("accounts.json"));

                controller.textfieldUsername.setText("");
                controller.passwordfieldPassword.setText("secret123");
                controller.onSignUp();

                assertNull(sceneManager.lastScene);
                assertEquals("Enter a username and password.", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithExistingAccountShowsErrorInsteadOfCrashing(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTempAuthenticator(controller, tempDir.resolve("accounts.json"));

                controller.textfieldUsername.setText("bob");
                controller.passwordfieldPassword.setText("hunter2");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.onSignUp();

                assertNull(sceneManager.lastScene, "Duplicate sign up should not navigate away");
                assertTrue(controller.labelMessage.getText().toLowerCase().contains("already exists"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void logInWithCorrectCredentialsNavigatesToPractice(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTempAuthenticator(controller, tempDir.resolve("accounts.json"));

                controller.textfieldUsername.setText("carol");
                controller.passwordfieldPassword.setText("letmein");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.onLogIn();

                assertEquals("practice", sceneManager.lastScene);
                assertEquals("", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void logInWithWrongPasswordShowsMessageAndDoesNotNavigate(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTempAuthenticator(controller, tempDir.resolve("accounts.json"));

                controller.textfieldUsername.setText("dave");
                controller.passwordfieldPassword.setText("correcthorse");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.passwordfieldPassword.setText("wrongpassword");
                controller.onLogIn();

                assertNull(sceneManager.lastScene);
                assertEquals("Incorrect username or password.", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void onPasswordSubmitsLogIn(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTempAuthenticator(controller, tempDir.resolve("accounts.json"));

                controller.textfieldUsername.setText("erin");
                controller.passwordfieldPassword.setText("passw0rd");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.onPassword();

                assertEquals("practice", sceneManager.lastScene, "Pressing Enter in the password field should submit login");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void onUsernameDoesNotThrow() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onUsername();

            assertNull(sceneManager.lastScene);
        });
    }

    @Test
    void signUpWhenWriteFailsShowsErrorMessage(@TempDir Path tempDir) throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                // Parent directory does not exist, so writing the accounts file will fail.
                useTempAuthenticator(controller, tempDir.resolve("missing-dir").resolve("accounts.json"));

                controller.textfieldUsername.setText("frank");
                controller.passwordfieldPassword.setText("secret123");
                controller.onSignUp();

                assertNull(sceneManager.lastScene);
                assertTrue(controller.labelMessage.getText().startsWith("Unable to save account"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void onReturnSwitchesToHomeScene() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.onReturn();

            assertEquals("home", sceneManager.lastScene);
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
