package com.qelery.chip8.components.sound.wave;

import com.qelery.chip8.components.sound.Sound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public abstract class AbstractWave implements Sound {

    public static final int SAMPLE_RATE = 16 * 1024;
    private static final Logger logger = LogManager.getLogger(AbstractWave.class);
    protected final double frequency;
    protected boolean isPlaying;
    protected SourceDataLine sourceDL;
    protected byte[] toneBuffer;

    protected AbstractWave(double frequency) {
        this.frequency = frequency;
        this.isPlaying = false;

        try {
            openSourceDataLine();
            this.toneBuffer = createToneBuffer();
        } catch (LineUnavailableException e) {
            logger.error("Error created sound. Line unavailable.");
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
            Thread playThread = new SineWave.SoundThread();
            playThread.setPriority(Thread.MAX_PRIORITY);
            playThread.start();
        } else {
            logger.info("Beep! (No sound available)");
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
