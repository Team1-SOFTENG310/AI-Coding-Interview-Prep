package com.aicodinginterviewprep.controllers;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundTest {

    @BeforeAll
    static void initialiseJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX already started.
        }
    }

    @Test
    void workRunsOffTheFxThreadAndSuccessIsDeliveredOnIt() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Boolean> workOnFx = new AtomicReference<>();
        AtomicReference<Boolean> callbackOnFx = new AtomicReference<>();
        AtomicReference<String> result = new AtomicReference<>();

        Platform.runLater(() -> Background.THREAD.run(() -> {
            workOnFx.set(Platform.isFxApplicationThread());
            return "value";
        }, value -> {
            callbackOnFx.set(Platform.isFxApplicationThread());
            result.set(value);
            done.countDown();
        }, error -> done.countDown()));

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertEquals(Boolean.FALSE, workOnFx.get());
        assertEquals(Boolean.TRUE, callbackOnFx.get());
        assertEquals("value", result.get());
    }

    @Test
    void failureIsDeliveredToTheErrorCallbackOnTheFxThread() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Boolean> callbackOnFx = new AtomicReference<>();

        Platform.runLater(() -> Background.THREAD.<String>run(() -> {
            throw new IllegalStateException("boom");
        }, value -> done.countDown(), failure -> {
            callbackOnFx.set(Platform.isFxApplicationThread());
            error.set(failure);
            done.countDown();
        }));

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertEquals(Boolean.TRUE, callbackOnFx.get());
        assertInstanceOf(IllegalStateException.class, error.get());
        assertEquals("boom", error.get().getMessage());
    }
}
