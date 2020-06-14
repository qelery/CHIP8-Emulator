package chip8;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Sound {

    private boolean isPlaying;
    private SourceDataLine sourceDL;
    private byte[] buffer = new byte[256];

    /**
     * Creates a sound that will be played whenever the sound timer is > 0.
     */
    public Sound() {
        isPlaying = false;

        try {
            createTone();
        } catch (LineUnavailableException e) {
            System.out.println("BEEP!");
        }
    }

    private void createTone() throws LineUnavailableException {

        int volume = 7; // volume > 10 is loud
        float rate = 44100;

        AudioFormat audioF;
        audioF = new AudioFormat(rate, 8, 1, true, false);
        this.sourceDL = AudioSystem.getSourceDataLine(audioF);
        sourceDL.open(audioF);
        sourceDL.start();

        for (int i = 0; i < 256; i++) {
            // tried numbers until I found a sound I liked
            double angle = (i / rate) * 2150;
            buffer[i] = (byte) (Math.sin(angle) * volume);
        }
    }

    public void play() {
        if (isPlaying) {
            return;
        }

        isPlaying = true;
        Thread playThread = new SoundThread();
        playThread.setPriority(Thread.MAX_PRIORITY);
        playThread.start();
    }

    public void stop() {
        isPlaying = false;
    }

    public void closeLine() {
        sourceDL.close();
    }

    class SoundThread extends Thread {

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
