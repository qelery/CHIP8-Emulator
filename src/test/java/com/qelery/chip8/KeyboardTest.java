package com.qelery.chip8;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardTest {

    Keyboard keyboard;

    private final ByteArrayOutputStream mockStdOut = new ByteArrayOutputStream();
    private final PrintStream originalStdOut = System.out;

    @BeforeEach
    void setup() {
        this.keyboard = new Keyboard();
        setUpMockIOStream();
    }

    @AfterEach
    void cleanup() {
        restoreIOStream();
    }

    private void setUpMockIOStream() {
        System.setOut(new PrintStream(mockStdOut));
    }

    private void restoreIOStream() {
        System.setOut(originalStdOut);
    }

    @Test
    @DisplayName("Should set array index of corresponding key to true on key down")
    void keyDown() {
        int indexOfAKey = 7;
        keyboard.getKeys()[indexOfAKey] = false;
        KeyCode aKey = KeyCode.A;

        keyboard.keyDown(aKey);

        assertTrue(keyboard.getKeys()[indexOfAKey]);
    }

    @Test
    @DisplayName("Should log message when trying to key down an unmapped key")
    void keyDownShouldLogUnmappedKey() {
        KeyCode unmappedKey = KeyCode.Y;
        keyboard.keyDown(unmappedKey);

        String stdOutMessage = mockStdOut.toString();

        assertEquals("Key down event for unmapped key: Y\n", stdOutMessage);
    }

    @Test
    @DisplayName("Should set array index of corresponding key to false on key up")
    void keyUp() {
        int indexOfAKey = 7;
        keyboard.getKeys()[indexOfAKey] = true;
        KeyCode aKey = KeyCode.A;

        keyboard.keyUp(aKey);

        assertFalse(keyboard.getKeys()[indexOfAKey]);
    }

    @Test
    @DisplayName("Should log message when trying to key up an unmapped key")
    void keyUpShouldLogUnmappedKey() {
        KeyCode unmappedKey = KeyCode.Y;
        keyboard.keyUp(unmappedKey);

        String stdOutMessage = mockStdOut.toString();

        assertEquals("Key up event for unmapped key: Y\n", stdOutMessage);
    }

    @Test
    @DisplayName("Should return true if a key is pressed down")
    void isKeyDown() {
        int indexOfAKey = 7;
        keyboard.getKeys()[indexOfAKey] = true;

        assertTrue(keyboard.isKeyDown(indexOfAKey));
    }

    @Test
    @DisplayName("Should force a key up")
    void forceKeyUp() {
        int indexOfAKey = 7;
        keyboard.getKeys()[indexOfAKey] = true;

        keyboard.forceKeyUp(indexOfAKey);

        assertFalse(keyboard.getKeys()[indexOfAKey]);
    }

    @Test
    @DisplayName("Should return the value of the downed key")
    void getDownedKeyValue() {
        keyboard.getKeys()[0xF] = true;
        assertEquals(0xF, keyboard.getDownedKeyValue());
    }

    @Test
    @DisplayName("Should return -1 if no keys are down")
    void getDownedKeyValue_noneDown() {
        assertEquals(-1, keyboard.getDownedKeyValue());
    }
}