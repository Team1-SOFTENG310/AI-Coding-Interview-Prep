package com.aicodinginterviewprep;

import com.aicodinginterviewprep.errors.AudioStateException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;

/**
 * Captures audio from the system microphone on a background thread and hands
 * back raw 16kHz mono PCM bytes once recording stops, ready for an offline
 * speech recognizer such as Vosk.
 */
public class MicrophoneRecorder {
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000f, 16, 1, true, false);
    private static final int BUFFER_SIZE = 4096;

    private TargetDataLine line;
    private ByteArrayOutputStream capturedAudio;
    private Thread captureThread;
    private volatile boolean recording;

    public void startRecording() throws LineUnavailableException {
        startRecording(openDefaultLine());
    }

    void startRecording(TargetDataLine line) throws LineUnavailableException {
        if (recording) {
            throw new AudioStateException("Already recording.");
        }

        this.line = line;
        line.open(AUDIO_FORMAT);
        line.start();
        capturedAudio = new ByteArrayOutputStream();
        recording = true;

        captureThread = new Thread(this::captureLoop, "microphone-capture");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public byte[] stopRecording() throws InterruptedException {
        if (!recording) {
            throw new AudioStateException("Not currently recording.");
        }

        recording = false;
        line.stop();
        captureThread.join();
        line.close();

        return capturedAudio.toByteArray();
    }

    public boolean isRecording() {
        return recording;
    }

    private void captureLoop() {
        byte[] chunk = new byte[BUFFER_SIZE];
        while (recording) {
            int bytesRead = line.read(chunk, 0, chunk.length);
            if (bytesRead > 0) {
                capturedAudio.write(chunk, 0, bytesRead);
            }
        }
    }

    private static TargetDataLine openDefaultLine() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("No microphone input is available on this system.");
        }
        return (TargetDataLine) AudioSystem.getLine(info);
    }
}
