package com.aicodinginterviewprep.controllers;

import com.aicodinginterviewprep.Authenticator;
import com.aicodinginterviewprep.SceneManager;
import com.aicodinginterviewprep.db.ConnectionFactory;
import com.aicodinginterviewprep.db.TestDatabase;
import com.aicodinginterviewprep.db.UserRepository;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        controller.textfieldPasswordVisible = new TextField();
        controller.buttonLogIn = new Button();
        controller.buttonSignUp = new Button();
        controller.buttonReturn = new Button();
        controller.linkTogglePassword = new Hyperlink();
        controller.labelMessage = new Label();

        return controller;
    }

    private void useTestAuthenticator(AuthController controller) throws Exception {
        setAuthenticator(controller, new TestDatabase().connections());
    }

    private void setAuthenticator(AuthController controller, ConnectionFactory connections) throws Exception {
        Field field = AuthController.class.getDeclaredField("authenticator");
        field.setAccessible(true);
        field.set(controller, new Authenticator(new UserRepository(connections)));
    }

    @Test
    void signUpWithNewAccountNavigatesToPractice() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("alice");
                controller.passwordfieldPassword.setText("Secret123!");
                controller.onSignUp();

                assertEquals("practice", sceneManager.lastScene);
                assertEquals("", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithNewAccount_setsCurrentUsernameOnSceneManager() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("alice");
                controller.passwordfieldPassword.setText("Secret123!");
                controller.onSignUp();

                assertEquals("alice", sceneManager.getCurrentUsername());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithBlankUsernameShowsMessageAndDoesNotNavigate() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("");
                controller.passwordfieldPassword.setText("Secret123!");
                controller.onSignUp();

                assertNull(sceneManager.lastScene);
                assertEquals("Enter a username and password.", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithShortPasswordShowsMessageAndDoesNotNavigate() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("alice");
                controller.passwordfieldPassword.setText("Short7!");
                controller.onSignUp();

                assertNull(sceneManager.lastScene);
                assertEquals(
                    "Password must be between 8 and 64 characters",
                    controller.labelMessage.getText()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithPasswordMissingRequiredCharacterTypeShowsMessageAndDoesNotNavigate() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("alice");
                controller.passwordfieldPassword.setText("lowercase1!");
                controller.onSignUp();

                assertNull(sceneManager.lastScene);
                assertEquals(
                    "Password must contain at least one uppercase letter, one lowercase letter, "
                        + "one number and one special character.",
                    controller.labelMessage.getText()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithSurroundingSpacesUsesTrimmedCredentials() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("  alice  ");
                controller.passwordfieldPassword.setText("  Secret123!  ");
                controller.onSignUp();

                assertEquals("practice", sceneManager.lastScene);
                assertEquals("alice", sceneManager.getCurrentUsername());
                assertEquals("", controller.labelMessage.getText());

                sceneManager.lastScene = null;
                sceneManager.setCurrentUsername(null);
                controller.textfieldUsername.setText("  alice  ");
                controller.passwordfieldPassword.setText("  Secret123!  ");
                controller.onLogIn();

                assertEquals("practice", sceneManager.lastScene);
                assertEquals("alice", sceneManager.getCurrentUsername());
                assertEquals("", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void signUpWithExistingAccountShowsErrorInsteadOfCrashing() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("bob");
                controller.passwordfieldPassword.setText("Hunter2!");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.textfieldUsername.setText("bob");
                controller.passwordfieldPassword.setText("Hunter2!");
                controller.onSignUp();

                assertNull(sceneManager.lastScene, "Duplicate sign up should not navigate away");
                assertTrue(controller.labelMessage.getText().toLowerCase().contains("already exists"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void logInWithCorrectCredentialsNavigatesToPractice() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("carol");
                controller.passwordfieldPassword.setText("Letmein8!");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.textfieldUsername.setText("carol");
                controller.passwordfieldPassword.setText("Letmein8!");
                controller.onLogIn();

                assertEquals("practice", sceneManager.lastScene);
                assertEquals("", controller.labelMessage.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void logInWithCorrectCredentials_setsCurrentUsernameOnSceneManager() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("carol");
                controller.passwordfieldPassword.setText("Letmein8!");
                controller.onSignUp();

                sceneManager.setCurrentUsername(null);
                controller.textfieldUsername.setText("carol");
                controller.passwordfieldPassword.setText("Letmein8!");
                controller.onLogIn();

                assertEquals("carol", sceneManager.getCurrentUsername());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void logInWithWrongPasswordShowsMessageAndDoesNotNavigate() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("dave");
                controller.passwordfieldPassword.setText("Correct1!");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.textfieldUsername.setText("dave");
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
    void onPasswordSubmitsLogIn() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("erin");
                controller.passwordfieldPassword.setText("Passw0rd!");
                controller.onSignUp();

                sceneManager.lastScene = null;
                controller.textfieldUsername.setText("erin");
                controller.passwordfieldPassword.setText("Passw0rd!");
                controller.onPassword();

                assertEquals("practice", sceneManager.lastScene, "Pressing Enter in the password field should submit login");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void onUsernameMovesFocusToPasswordField() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            // Deliberately no Stage/show() here - real window focus needs a window
            // manager, which headless CI runners (Xvfb) don't have, and requesting
            // it hangs indefinitely there. Scene tracks its own focus owner without
            // needing a shown window, so that's enough to verify the redirect.
            javafx.scene.Scene scene = new javafx.scene.Scene(new javafx.scene.layout.VBox(
                controller.textfieldUsername, controller.passwordfieldPassword));

            controller.onUsername();

            assertEquals(controller.passwordfieldPassword, scene.getFocusOwner());
            assertNull(sceneManager.lastScene, "Pressing Enter in the username field should not navigate away");
        });
    }

    @Test
    void signUpWhenWriteFailsShowsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                setAuthenticator(controller, TestDatabase.failing());

                controller.textfieldUsername.setText("frank");
                controller.passwordfieldPassword.setText("Secret123!");
                controller.onSignUp();

                assertNull(sceneManager.lastScene);
                assertTrue(controller.labelMessage.getText().startsWith("Unable to save account"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void logInWhenDatabaseFailsShowsErrorMessage() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                setAuthenticator(controller, TestDatabase.failing());

                controller.textfieldUsername.setText("frank");
                controller.passwordfieldPassword.setText("Secret123!");
                controller.onLogIn();

                assertNull(sceneManager.lastScene);
                assertTrue(controller.labelMessage.getText().startsWith("Unable to log in"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void passwordFieldsStayInSyncViaBidirectionalBinding() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.passwordfieldPassword.setText("secret123");
            assertEquals("secret123", controller.textfieldPasswordVisible.getText());

            controller.textfieldPasswordVisible.setText("changed");
            assertEquals("changed", controller.passwordfieldPassword.getText());
        });
    }

    @Test
    void onTogglePasswordVisibilitySwapsFieldVisibilityAndButtonText() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            assertTrue(controller.passwordfieldPassword.isVisible());
            assertFalse(controller.textfieldPasswordVisible.isVisible());
            assertEquals("Show", controller.linkTogglePassword.getText());

            controller.onTogglePasswordVisibility();

            assertFalse(controller.passwordfieldPassword.isVisible());
            assertTrue(controller.textfieldPasswordVisible.isVisible());
            assertEquals("Hide", controller.linkTogglePassword.getText());

            controller.onTogglePasswordVisibility();

            assertTrue(controller.passwordfieldPassword.isVisible());
            assertFalse(controller.textfieldPasswordVisible.isVisible());
            assertEquals("Show", controller.linkTogglePassword.getText());
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

    @Test
    void onReturnClearsUsernameAndPasswordAndResetsVisibility() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            controller.textfieldUsername.setText("alice");
            controller.passwordfieldPassword.setText("secret123");
            controller.onTogglePasswordVisibility();

            controller.onReturn();

            assertEquals("", controller.textfieldUsername.getText());
            assertEquals("", controller.passwordfieldPassword.getText());
            assertEquals("", controller.textfieldPasswordVisible.getText());
            assertTrue(controller.passwordfieldPassword.isVisible());
            assertFalse(controller.textfieldPasswordVisible.isVisible());
            assertEquals("Show", controller.linkTogglePassword.getText());
        });
    }

    @Test
    void onSignUp_success_clearsUsernameAndPassword() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("grace");
                controller.passwordfieldPassword.setText("Hopper123!");
                controller.onSignUp();

                assertEquals("", controller.textfieldUsername.getText());
                assertEquals("", controller.passwordfieldPassword.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void onLogIn_success_clearsUsernameAndPassword() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                AuthController controller = createController();
                FakeSceneManager sceneManager = new FakeSceneManager();
                controller.setSceneManager(sceneManager);
                useTestAuthenticator(controller);

                controller.textfieldUsername.setText("heidi");
                controller.passwordfieldPassword.setText("Letmein456!");
                controller.onSignUp();

                controller.textfieldUsername.setText("heidi");
                controller.passwordfieldPassword.setText("Letmein456!");
                controller.onLogIn();

                assertEquals("", controller.textfieldUsername.getText());
                assertEquals("", controller.passwordfieldPassword.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void tabFromUsernameSkipsAheadToPasswordField() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            KeyEvent tab = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false);
            controller.textfieldUsername.getOnKeyPressed().handle(tab);

            assertTrue(tab.isConsumed(), "Tab out of username should be handled explicitly");
        });
    }

    @Test
    void tabFromPasswordFieldSkipsAheadToShowLink() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            KeyEvent tab = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false);
            controller.passwordfieldPassword.getOnKeyPressed().handle(tab);

            assertTrue(tab.isConsumed(), "Tab out of the password field should be handled explicitly");
        });
    }

    @Test
    void shiftTabIsLeftToDefaultBackwardTraversal() throws Exception {
        runOnFxThreadAndWait(() -> {
            AuthController controller = createController();
            FakeSceneManager sceneManager = new FakeSceneManager();
            controller.setSceneManager(sceneManager);

            KeyEvent shiftTab = new KeyEvent(
                KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, true, false, false, false);
            controller.passwordfieldPassword.getOnKeyPressed().handle(shiftTab);

            assertFalse(shiftTab.isConsumed(), "Shift+Tab should fall back to default traversal");
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
