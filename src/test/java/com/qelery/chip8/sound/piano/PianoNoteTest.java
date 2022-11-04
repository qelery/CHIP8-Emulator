package com.qelery.chip8.sound.piano;

import com.qelery.chip8.components.sound.piano.MusicalNote;
import com.qelery.chip8.components.sound.piano.PianoNote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("PianoNoteTest is completely auditory. Run manually to test.")
class PianoNoteTest {

    PianoNote sound;

    final int FIVE_SECONDS = 5000;
    static int testsExecuted = 0;

    @BeforeEach
    void setUp() {
        this.sound = new PianoNote(MusicalNote.C);
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