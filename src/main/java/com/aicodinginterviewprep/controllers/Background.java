package com.aicodinginterviewprep.controllers;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Runs blocking work off the JavaFX thread and delivers the outcome back on it. */
@FunctionalInterface
interface Background {

    <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError);

    Background THREAD = new Background() {
        @Override
        public <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
            Task<T> task = new Task<>() {
                @Override
                protected T call() throws Exception {
                    return work.call();
                }
            };
            task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
            task.setOnFailed(event -> onError.accept(task.getException()));
            Thread worker = new Thread(task, "saved-content");
            worker.setDaemon(true);
            worker.start();
        }
    };
}
