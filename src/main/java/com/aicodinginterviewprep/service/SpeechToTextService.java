package com.aicodinginterviewprep.service;

import com.aicodinginterviewprep.errors.SpeechRecognitionException;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Transcribes recorded audio to text entirely offline using Vosk, so this
 * feature works regardless of what a shared/restricted OpenAI API key allows.
 * The speech model is loaded lazily on first use since it takes a couple of
 * seconds to load and would otherwise delay app startup for every user,
 * even ones who never touch voice input.
 */
public class SpeechToTextService {
    private static final float SAMPLE_RATE = 16000f;
    private static final Path DEFAULT_MODEL_PATH = Path.of("models", "vosk-model-en-us-0.22-lgraph");

    // Vosk has no built-in silence rejection - a quiet room's background noise
    // still produces some low-confidence guess instead of an empty result. This
    // is a basic RMS-energy gate: audio quieter than this (16-bit PCM, so the
    // scale is 0-32767) is treated as "nothing said" before it ever reaches the
    // recognizer, rather than letting it guess at noise.
    private static final double SILENCE_RMS_THRESHOLD = 400.0;

    private final Path modelPath;
    private Model model;

    public SpeechToTextService() {
        this(DEFAULT_MODEL_PATH);
    }

    SpeechToTextService(Path modelPath) {
        this.modelPath = modelPath;
    }

    SpeechToTextService(Model model) {
        this.modelPath = null;
        this.model = model;
    }

    public synchronized String transcribe(byte[] pcmAudio) throws IOException {
        if (pcmAudio == null || pcmAudio.length == 0) {
            throw new SpeechRecognitionException("No audio was recorded.");
        }
        if (isSilent(pcmAudio)) {
            return "";
        }

        ensureModelLoaded();

        try (Recognizer recognizer = new Recognizer(model, SAMPLE_RATE)) {
            recognizer.acceptWaveForm(pcmAudio, pcmAudio.length);
            return new JSONObject(recognizer.getFinalResult()).getString("text").trim();
        }
    }

    static boolean isSilent(byte[] pcmAudio) {
        int sampleCount = pcmAudio.length / 2;
        if (sampleCount == 0) {
            return true;
        }

        long sumSquares = 0;
        for (int i = 0; i + 1 < pcmAudio.length; i += 2) {
            short sample = (short) ((pcmAudio[i + 1] << 8) | (pcmAudio[i] & 0xFF));
            sumSquares += (long) sample * sample;
        }

        double rms = Math.sqrt((double) sumSquares / sampleCount);
        return rms < SILENCE_RMS_THRESHOLD;
    }

    private void ensureModelLoaded() throws IOException {
        if (model != null) {
            return;
        }
        if (!Files.isDirectory(modelPath)) {
            throw new SpeechRecognitionException(
                    "Offline speech model not found at " + modelPath.toAbsolutePath()
                            + ". See README for setup instructions.");
        }

        LibVosk.setLogLevel(LogLevel.WARNINGS);
        model = new Model(modelPath.toAbsolutePath().toString());
    }
}
