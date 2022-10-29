package com.qelery.chip8.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SineWave implements Sound {

    private SourceDataLine sourceDL;
    private boolean isPlaying;
    private final byte[] buffer = new byte[256];

    /**
     * Creates a sound that will be played whenever the sound timer is > 0.
     */
    public SineWave() {
        isPlaying = false;

        try {
            createTone();
        } catch (LineUnavailableException e) {
            System.out.println("Error created sound.");
        }
    }

    private void createTone() throws LineUnavailableException {
        float rate = 44100;
        AudioFormat audioF = new AudioFormat(rate, 8, 1, true, false);
        this.sourceDL = AudioSystem.getSourceDataLine(audioF);
        sourceDL.open(audioF);
        sourceDL.start();

        int volume = 7; // volume > 10 is loud
        for (int i = 0; i < 256; i++) {
            // These numbers are arbitrary. I played around with them until I
            // found a sound I liked.
            double angle = (i / rate) * 2150;
            buffer[i] = (byte) (Math.sin(angle) * volume);
        }
    }

    public void play() {
        if (isPlaying) {
            return;
        }
        if (sourceDL != null) {
            isPlaying = true;
            Thread playThread = new SoundThread();
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
                sourceDL.write(buffer, 0, buffer.length);
            } while (isPlaying);
            sourceDL.drain();
            sourceDL.stop();
            sourceDL.flush();
        }
    }
}
