package com.qelery.chip8.sound.piano;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("This unit test is completely auditory! Run manually to test.")
class PianoSoundTest {

    PianoSound sound;

    final int FIVE_SECONDS = 5000;
    static int testsExecuted = 0;

    @BeforeEach
    void setup() {
        this.sound = new PianoSound(PianoNote.C);
    }

    @Test
    @DisplayName("Should play sound then stop.")
    void playThenStop() throws InterruptedException {
        testsExecuted++;
        System.out.println("PianoNoteTest test " + testsExecuted + ". Should hear sound for 3 seconds.");
        sound.play();
        Thread.sleep(FIVE_SECONDS);
        sound.stop();
    }

    @Test
    @DisplayName("Should play sound then close the audio line.")
    void playThenCloseLine() throws InterruptedException {
        testsExecuted++;
        System.out.println("PianoNoteTest test " + testsExecuted + ". Should hear sound for 3 seconds.");
        sound.play();
        Thread.sleep(FIVE_SECONDS);
        sound.closeLine();
    }
}