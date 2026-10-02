package com.aicodinginterviewprep.errors;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(AppErrorHandler.class.getName());
    private static final String GENERIC_USER_MESSAGE = "An unexpected error occurred. Please try again.";

    private AppErrorHandler() {
    }

    public static void install() {
        Thread.UncaughtExceptionHandler handler = (thread, error) ->
            handle(error, "Unexpected error on thread " + thread.getName());
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Thread.currentThread().setUncaughtExceptionHandler(handler);
    }

    public static void report(Throwable error, String context, Consumer<String> userNotifier) {
        Throwable cause = rootError(error);
        log(cause, context);
        userNotifier.accept(userMessage(cause));
    }

    public static void handle(Throwable error, String context) {
        Throwable cause = rootError(error);
        log(cause, context);
        Runnable showError = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Something went wrong");
            alert.setHeaderText(context);
            alert.setContentText(userMessage(cause));
            alert.show();
        };

        if (Platform.isFxApplicationThread()) {
            showError.run();
            return;
        }
        try {
            Platform.runLater(showError);
        } catch (IllegalStateException toolkitUnavailable) {
            LOGGER.log(Level.WARNING, "Unable to display the error notification because JavaFX is unavailable.",
                toolkitUnavailable);
        }
    }

    public static String userMessage(Throwable error) {
        Throwable cause = rootError(error);
        if (cause instanceof DomainError domainError) {
            return domainError.userMessage();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank() ? GENERIC_USER_MESSAGE : message;
    }

    private static Throwable rootError(Throwable error) {
        Throwable cause = Objects.requireNonNullElseGet(error,
            IllegalStateException::new);
        while ((cause instanceof CompletionException || cause instanceof ExecutionException)
            && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static void log(Throwable error, String context) {
        Level level = error instanceof ValidationException ? Level.WARNING : Level.SEVERE;
        LOGGER.log(level, context + ": " + error.getMessage(), error);
    }
}
