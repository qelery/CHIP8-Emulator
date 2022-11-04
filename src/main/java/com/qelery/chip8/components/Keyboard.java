package com.qelery.chip8.components;

import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A virtual keyboard for handling CHIP-8 input events.
 * <p>
 * The machines that originally ran CHIP-8 used a 16-key hexadecimal
 * keypad with the following layout:
 * <p>
 * -----------------<br>
 * | 1 | 2 | 3 | C |<br>
 * -----------------<br>
 * | 4 | 5 | 6 | D |<br>
 * ------------------<br>
 * | 7 | 8 | 9 | E |<br>
 * ------------------<br>
 * | A | 0 | B | F |<br>
 * ------------------<br>
 * <p>
 * Those keys have been mapped to the following layout on a standard
 * keyboard:
 * <p>
 * -----------------<br>
 * | 1 | 2 | 3 | 4 |<br>
 * -----------------<br>
 * | Q | W | E | R |<br>
 * ------------------<br>
 * | A | S | D | F |<br>
 * ------------------<br>
 * | Z | X | C | V |<br>
 * ------------------<br>
 */
public class Keyboard {

    private static final Logger logger = LogManager.getLogger(Keyboard.class);

    private final boolean[] keys;

    /**
     * Constructs a virtual CHIP-8 keyboard.
     */
    public Keyboard() {
        this.keys = new boolean[16];
    }

    public void keyDown(KeyCode key) {
        switch (key) {
            case DIGIT1 -> keys[0x1] = true;
            case DIGIT2 -> keys[0x2] = true;
            case DIGIT3 -> keys[0x3] = true;
            case DIGIT4 -> keys[0xC] = true;
            case Q -> keys[0x4] = true;
            case W -> keys[0x5] = true;
            case E -> keys[0x6] = true;
            case R -> keys[0xD] = true;
            case A -> keys[0x7] = true;
            case S -> keys[0x8] = true;
            case D -> keys[0x9] = true;
            case F -> keys[0xE] = true;
            case Z -> keys[0xA] = true;
            case X -> keys[0x0] = true;
            case C -> keys[0xB] = true;
            case V -> keys[0xF] = true;
            default -> logger.warn("Key down event for unmapped key: {}", key);
        }
    }

    public void keyUp(KeyCode key) {
        switch (key) {
            case DIGIT1 -> keys[0x1] = false;
            case DIGIT2 -> keys[0x2] = false;
            case DIGIT3 -> keys[0x3] = false;
            case DIGIT4 -> keys[0xC] = false;
            case Q -> keys[0x4] = false;
            case W -> keys[0x5] = false;
            case E -> keys[0x6] = false;
            case R -> keys[0xD] = false;
            case A -> keys[0x7] = false;
            case S -> keys[0x8] = false;
            case D -> keys[0x9] = false;
            case F -> keys[0xE] = false;
            case Z -> keys[0xA] = false;
            case X -> keys[0x0] = false;
            case C -> keys[0xB] = false;
            case V -> keys[0xF] = false;
            default -> logger.warn("Key up event for unmapped key: {}", key);
        }
    }

    public int getDownedKeyValue() {
        int minKeyValue = 0x1;
        int maxKeyValue = 0xF;
        for (int i = minKeyValue; i <= maxKeyValue; i++) {
            if (isKeyDown(i)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isKeyDown(int keyVal) {
        return keys[keyVal];
    }

    public void forceKeyUp(int keyVal) {
        keys[keyVal] = false;
    }

    public void printKeyControls() {
        String keyMappingsLayout = """
                YOUR KEYBOARD CONTROLS:        ORIGINAL CHIP-8 LAYOUT:
                      1  2  3  4                    1  2  3  C
                      Q  W  E  R                    4  5  6  D
                      A  S  D  F                    7  8  9  E
                      Z  X  C  V                    A  0  B  F
                """;
        System.out.println(keyMappingsLayout);
    }

    public boolean[] getKeys() {
        return keys;
    }
}
