package com.aicodinginterviewprep.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class SpeechToTextServiceTest {

    private static final byte[] SOME_AUDIO = {1, 2, 3, 4};

    @Test
    @EnabledOnOs(OS.MAC)
    void bundledNativeLibraryLoadsOnMac() {
        assertDoesNotThrow(() -> LibVosk.setLogLevel(LogLevel.WARNINGS));
    }

    @Test
    void transcribeReturnsTrimmedTextFromRecognizerResult() throws Exception {
        try (var mockedRecognizer = mockConstruction(Recognizer.class, (recognizer, context) -> {
            when(recognizer.acceptWaveForm(any(byte[].class), anyInt())).thenReturn(true);
            when(recognizer.getFinalResult()).thenReturn("{\"text\": \"  what is a hash map  \"}");
        })) {
            SpeechToTextService service = new SpeechToTextService(mock(Model.class));

            assertEquals("what is a hash map", service.transcribe(SOME_AUDIO));
        }
    }

    @Test
    void transcribeThrowsWhenAudioIsNull() {
        SpeechToTextService service = new SpeechToTextService(mock(Model.class));

        assertThrows(IllegalStateException.class, () -> service.transcribe(null));
    }

    @Test
    void transcribeThrowsWhenAudioIsEmpty() {
        SpeechToTextService service = new SpeechToTextService(mock(Model.class));

        assertThrows(IllegalStateException.class, () -> service.transcribe(new byte[0]));
    }

    @Test
    void transcribeThrowsWhenModelDirectoryIsMissing() {
        SpeechToTextService service = new SpeechToTextService(Path.of("does", "not", "exist"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> service.transcribe(SOME_AUDIO));
        assertTrue(exception.getMessage().contains("Offline speech model not found"));
    }

    @Test
    void transcribeWithSilentAudioReturnsEmptyStringWithoutLoadingModel() throws Exception {
        SpeechToTextService service = new SpeechToTextService(Path.of("does", "not", "exist"));
        byte[] silentAudio = new byte[3200];

        assertEquals("", service.transcribe(silentAudio));
    }

    @Test
    void isSilentReturnsTrueForZeroAmplitudeAudio() {
        assertTrue(SpeechToTextService.isSilent(new byte[2000]));
    }

    @Test
    void isSilentReturnsTrueForEmptyAudio() {
        assertTrue(SpeechToTextService.isSilent(new byte[0]));
    }

    @Test
    void isSilentReturnsFalseForLoudAudio() {
        byte[] loud = new byte[2000];
        for (int i = 0; i < loud.length; i += 2) {
            loud[i] = (byte) 0xFF;
            loud[i + 1] = (byte) 0x7F;
        }

        assertFalse(SpeechToTextService.isSilent(loud));
    }

    @Test
    void transcribeCreatesANewRecognizerPerCall() throws Exception {
        try (var mockedRecognizer = mockConstruction(Recognizer.class, (recognizer, context) -> {
            when(recognizer.acceptWaveForm(any(byte[].class), anyInt())).thenReturn(true);
            when(recognizer.getFinalResult()).thenReturn("{\"text\": \"hi\"}");
        })) {
            SpeechToTextService service = new SpeechToTextService(mock(Model.class));

            service.transcribe(SOME_AUDIO);
            service.transcribe(SOME_AUDIO);

            assertEquals(2, mockedRecognizer.constructed().size());
        }
    }
}
