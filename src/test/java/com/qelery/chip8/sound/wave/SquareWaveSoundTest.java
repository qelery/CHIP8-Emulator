package com.qelery.chip8.sound.wave;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("This unit test is completely auditory! Run manually to test.")
class SquareWaveSoundTest {

    SquareWaveSound sound;

    final int THREE_SECONDS = 3000;
    static int testsExecuted = 0;

    @BeforeEach
    void setup() {
        double frequency = 300;
        this.sound = new SquareWaveSound(frequency);
    }

    @Test
    @DisplayName("Should play sound then stop.")
    void playThenStop() throws InterruptedException {
        testsExecuted++;
        System.out.println("SquareWaveSoundTest test " + testsExecuted + ". Should hear sound for 3 seconds.");
        sound.play();
        Thread.sleep(THREE_SECONDS);
        sound.stop();
    }

    @Test
    @DisplayName("Should play sound then close the audio line.")
    void playThenCloseLine() throws InterruptedException {
        testsExecuted++;
        System.out.println("SquareWaveSoundTest test " + testsExecuted + ". Should hear sound for 3 seconds.");
        sound.play();
        Thread.sleep(THREE_SECONDS);
        sound.closeLine();
    }
}