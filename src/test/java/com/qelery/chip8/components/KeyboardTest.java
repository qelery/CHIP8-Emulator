package com.qelery.chip8.components;

import com.qelery.chip8.components.Keyboard;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardTest {

    Keyboard keyboard;

    @BeforeEach
    void setup() {
        this.keyboard = new Keyboard();
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
    @DisplayName("Should set array index of corresponding key to false on key up")
    void keyUp() {
        int indexOfAKey = 7;
        keyboard.getKeys()[indexOfAKey] = true;
        KeyCode aKey = KeyCode.A;

        keyboard.keyUp(aKey);

        assertFalse(keyboard.getKeys()[indexOfAKey]);
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