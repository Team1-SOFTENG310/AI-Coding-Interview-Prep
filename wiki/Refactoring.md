This refactoring improved two cross-cutting areas of the application: scene navigation and error handling. It replaces
scattered responsibilities with shared conventions, making it easier to extend behavior consistently and reducing the
amount of controller-specific code needed for common tasks.
For navigation, `SceneManager` is the central place for scene transitions, active-scene tracking, cached scenes, and
push/pop history. `switchScene` handles regular navigation, `pushScene` saves the current scene before opening a
temporary screen, and `popScene` returns to the previous one. `SceneAware` lifecycle callbacks provide consistent places
to react when a scene is shown, hidden, or disposed. For example, `PracticeController` stops an active recording when
the user leaves:

    @Override
    public void onSceneHidden() {
        cancelRecordingIfActive();
    }

The existing practice controller also navigates to history and then configures its controller through the manager:

    @FXML
    public void onHistory() {
        sceneManager.switchToScene("history");
        if (sceneManager.getController("history") instanceof HistoryController historyController) {
            historyController.setSceneManager(sceneManager);
        }
    }

Keeping scene lookup, activation, and lifecycle behavior in `SceneManager` makes changes such as adding a screen or
adjusting navigation policy easier to apply consistently. The compatibility alias `switchToScene` also lets existing
callers keep working while the newer navigation API is adopted. The manager rejects attempts to unload the active scene
and removes inactive scenes from its cache and navigation history when unloading them, helping prevent stale state.
Error handling is centralized in `AppErrorHandler`, which is installed when the application starts. It logs errors with
context and either displays a standard JavaFX alert or supplies a user-facing message to a caller. Typed exceptions,
such as `ValidationException`, distinguish expected validation failures from other errors. `Authenticator.signUp` uses
that exception for invalid account details:

    if (username == null || password == null) {
        throw new ValidationException("Username and password cannot be null");
    }

Asynchronous failures use the same shared handler while allowing the screen to provide task-specific feedback. In
`PracticeController`, a failed question-generation task is reported as follows:

    task.setOnFailed(event -> {
        AppErrorHandler.report(task.getException(), "Generating interview question",
                message -> questionOutput.setText("Failed to generate question: " + message));
        buttonGenerateQuestion.setDisable(false);
    });

This means a service can raise a meaningful typed exception and a controller can report it with useful context, rather
than implementing separate logging and notification behavior. Changes to error presentation can also be made centrally.
Overall, the refactoring makes navigation, cleanup, and user-facing error reporting more predictable and reduces
duplicated logic. Existing tests cover transition history, lifecycle callbacks, scene unloading, and error-message
handling, helping maintain these conventions as new features are added.