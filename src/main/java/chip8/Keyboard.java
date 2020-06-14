package chip8;

import javafx.scene.input.KeyCode;

/**
 * Creates a keyboard for handling input events.
 *
 * The machines that originally ran CHIP-8 used a 16-key hexadecimal
 * keypad with the following layout:
 *
 *   -----------------
 *   | 1 | 2 | 3 | C |
 *   -----------------
 *   | 4 | 5 | 6 | D |
 *   ------------------
 *   | 7 | 8 | 9 | E |
 *   ------------------
 *   | A | 0 | B | F |
 *   ------------------
 *
 * Which has been mapped to the following layout on a standard keyboard:
 *   -----------------
 *   | 1 | 2 | 3 | 4 |
 *   -----------------
 *   | Q | W | E | R |
 *   ------------------
 *   | A | S | D | F |
 *   ------------------
 *   | Z | X | C | V |
 *   ------------------
 */
public class Keyboard {

    private boolean[] keys;

    public Keyboard() {
        this.keys = new boolean[16];
    }

    public void setKeyDown(KeyCode code) {
        switch (code) {
            case DIGIT1:
                keys[0x1] = true;
                break;
            case DIGIT2:
                keys[0x2] = true;
                break;
            case DIGIT3:
                keys[0x3] = true;
                break;
            case DIGIT4:
                keys[0xC] = true;
                break;
            case Q:
                keys[0x4] = true;
                break;
            case W:
                keys[0x5] = true;
                break;
            case E:
                keys[0x6] = true;
                break;
            case R:
                keys[0xD] = true;
                break;
            case A:
                keys[0x7] = true;
                break;
            case S:
                keys[0x8] = true;
                break;
            case D:
                keys[0x9] = true;
                break;
            case F:
                keys[0xE] = true;
                break;
            case Z:
                keys[0xA] = true;
                break;
            case X:
                keys[0x0] = true;
                break;
            case C:
                keys[0xB] = true;
                break;
            case V:
                keys[0xF] = true;
                break;
        }
    }

    public void releaseKey(KeyCode code) {
        switch (code) {
            case DIGIT1:
                keys[0x1] = false;
                break;
            case DIGIT2:
                keys[0x2] = false;
                break;
            case DIGIT3:
                keys[0x3] = false;
                break;
            case DIGIT4:
                keys[0xC] = false;
                break;
            case Q:
                keys[0x4] = false;
                break;
            case W:
                keys[0x5] = false;
                break;
            case E:
                keys[0x6] = false;
                break;
            case R:
                keys[0xD] = false;
                break;
            case A:
                keys[0x7] = false;
                break;
            case S:
                keys[0x8] = false;
                break;
            case D:
                keys[0x9] = false;
                break;
            case F:
                keys[0xE] = false;
                break;
            case Z:
                keys[0xA] = false;
                break;
            case X:
                keys[0x0] = false;
                break;
            case C:
                keys[0xB] = false;
                break;
            case V:
                keys[0xF] = false;
                break;
        }
    }

    public boolean isKeyPressed(int keyVal) {
        return keys[keyVal];
    }

    public void forceKeyRelease(int keyVal) {
        keys[keyVal] = false;
    }

    public void printControls() {
        System.out.println(
                "YOUR CONTROLS:        ORIGINAL KEYPAD LAYOUT:\n" +
                "  1  2  3  4                1  2  3  C\n" +
                "  Q  W  E  R                4  5  6  D\n" +
                "  A  S  D  F                7  8  9  E\n" +
                "  Z  X  C  V                A  0  B  F");
    }
}
