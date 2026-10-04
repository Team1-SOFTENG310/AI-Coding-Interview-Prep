package com.aicodinginterviewprep.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Test doubles for {@link Background}. */
final class TestBackgrounds {

    private TestBackgrounds() {}

    /** Runs work and callbacks inline so tests can assert straight after the call. */
    static final Background IMMEDIATE = new Background() {
        @Override
        public <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
            T result;
            try {
                result = work.call();
            } catch (Exception e) {
                onError.accept(e);
                return;
            }
            onSuccess.accept(result);
        }
    };

    /** Holds work until {@link #runAll()} so tests can observe the in-progress state. */
    static final class Deferred implements Background {
        private final List<Runnable> pending = new ArrayList<>();

        @Override
        public <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
            pending.add(() -> IMMEDIATE.run(work, onSuccess, onError));
        }

        int pendingCount() {
            return pending.size();
        }

        void runAll() {
            List<Runnable> toRun = new ArrayList<>(pending);
            pending.clear();
            toRun.forEach(Runnable::run);
        }
    }
}
