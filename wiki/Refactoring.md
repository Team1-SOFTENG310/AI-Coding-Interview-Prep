This refactoring improved two cross-cutting areas of the application: scene navigation and error handling. It replaced
scattered responsibilities with shared conventions, making the code easier to extend and reducing the amount of
controller-specific logic needed for common tasks.

For navigation, `SceneManager` became the central place for scene transitions, active-scene tracking, cached scenes, and
push/pop history. `switchScene` handles regular navigation, `pushScene` saves the current scene before opening a
temporary screen, and `popScene` returns to the previous one. `SceneAware` lifecycle callbacks give each screen a clear and
consistent place to react when it is shown, hidden, or disposed. For example, `PracticeController` stops an active
recording when the user leaves:

    @Override
    public void onSceneHidden() {
        cancelRecordingIfActive();
    }

The same controller also navigates to history and configures the screen through the manager:

    @FXML
    public void onHistory() {
        sceneManager.switchToScene("history");
        if (sceneManager.getController("history") instanceof HistoryController historyController) {
            historyController.setSceneManager(sceneManager);
        }
    }

By keeping scene lookup, activation, and lifecycle behavior in a single place, changes such as adding a screen or updating
navigation rules become much easier to apply consistently. The compatibility alias `switchToScene` lets existing callers
continue working while the newer navigation API is adopted, and the manager rejects attempts to unload the active scene
while cleaning up stale scenes and navigation history when needed.

Error handling was centralized in `AppErrorHandler`, which is installed when the application starts. It logs errors with
context and either shows a standard JavaFX alert or supplies a user-facing message to the caller. Typed exceptions, such as
`ValidationException`, distinguish expected validation failures from other errors. `Authenticator.signUp` uses that
pattern for invalid account details:

    if (username == null || password == null) {
        throw new ValidationException("Username and password cannot be null");
    }

Asynchronous failures follow the same shared flow while still allowing the screen to provide task-specific feedback. In
`PracticeController`, a failed question-generation task is handled like this:

    task.setOnFailed(event -> {
        AppErrorHandler.report(task.getException(), "Generating interview question",
                message -> questionOutput.setText("Failed to generate question: " + message));
        buttonGenerateQuestion.setDisable(false);
    });

This keeps the service responsible for raising a meaningful typed exception while the controller focuses on presenting the
result in context, instead of duplicating logging and notification behavior. The same approach also makes error presentation
consistent across the application.

A similar refactoring was applied to prompt management. Prompt definitions had previously been spread across raw txt files
and hard-coded Java strings, which made them difficult to validate, update, and track consistently. Moving them into JSON
files gave each prompt a clear structure, standard metadata, and a more reliable loading path at runtime.

This separation keeps application logic and prompt content distinct. The interview-generation code can focus on
orchestration while the JSON files define the wording and configuration. That reduces duplication, prevents mismatched
prompt content, and makes future updates safer and easier to review.