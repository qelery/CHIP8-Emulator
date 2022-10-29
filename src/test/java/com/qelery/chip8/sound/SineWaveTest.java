package com.qelery.chip8.sound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("This unit test is completely auditory! Run manually to test.")
class SineWaveTest {

    SineWave sound;

    final int FIVE_SECONDS = 5000;
    static int testsExecuted = 0;


    @BeforeEach
    void setup() {
        this.sound = new SineWave();
    }


    @Test
    @DisplayName("Should play tone for 5 seconds then stop.")
    void playThenStop() throws InterruptedException {
        testsExecuted++;
        System.out.println("SineWaveTest test " + testsExecuted + ". Should hear tone for 5 seconds.");
        sound.play();
        Thread.sleep(FIVE_SECONDS);
        sound.stop();
    }

    @Test
    @DisplayName("Should play tone for 5 seconds then close the audio line.")
    void playThenCloseLine() throws InterruptedException {
        testsExecuted++;
        System.out.println("SineWaveTest test " + testsExecuted + ". Should hear tone for 5 seconds.");
        sound.play();
        Thread.sleep(FIVE_SECONDS);
        sound.closeLine();
    }
}