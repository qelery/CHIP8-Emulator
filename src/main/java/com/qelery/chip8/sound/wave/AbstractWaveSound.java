package com.qelery.chip8.sound.wave;

import com.qelery.chip8.sound.Sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public abstract class AbstractWaveSound implements Sound {
    public static final int SAMPLE_RATE = 16 * 1024;

    protected final double frequency;
    protected boolean isPlaying;
    protected SourceDataLine sourceDL;
    protected byte[] toneBuffer;

    protected AbstractWaveSound(double frequency) {
        this.frequency = frequency;
        this.isPlaying = false;

        try {
            openSourceDataLine();
            this.toneBuffer = createToneBuffer();
        } catch (LineUnavailableException e) {
            System.out.println("Error created sound.");
        }
    }

    public void openSourceDataLine() throws LineUnavailableException {
        AudioFormat audioF = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        this.sourceDL = AudioSystem.getSourceDataLine(audioF);
        this.sourceDL.open(audioF);
        this.sourceDL.start();
    }

    abstract byte[] createToneBuffer();

    public void play() {
        if (isPlaying) {
            return;
        }
        if (sourceDL != null) {
            isPlaying = true;
            Thread playThread = new SineWaveSound.SoundThread();
            playThread.setPriority(Thread.MAX_PRIORITY);
            playThread.start();
        } else {
            System.out.println("Beep!");
        }
    }

    public void stop() {
        isPlaying = false;
    }

    public void closeLine() {
        if (sourceDL != null) {
            sourceDL.close();
        }
    }

    class SoundThread extends Thread {

        @Override
        public void run() {
            sourceDL.start();
            do {
                sourceDL.write(toneBuffer, 0, toneBuffer.length);
            } while (isPlaying);
            sourceDL.stop();
            sourceDL.flush();
        }
    }
}
