package com.qelery.chip8;


import java.util.Objects;

/**
 * A class that represents an unsigned 2-byte opcode.
 * <p>
 * <h2>CPU opcode notation</h2>
 * Adapted from: <a href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM">Cowgod's Chip-8 Technical Reference</a>
 * <p>
 * ■ ■ ■ ■ - 4-nibble representation of the 2-byte opcode
 * <p>
 * o &#9; 1st nibble of opcode &#9; ( o ■ ■ ■ )<br>
 * x &#9; 2nd nibble of opcode &#9; ( ■ x ■ ■ )<br>
 * y &#9; 3rd nibble of opcode &#9; ( ■ ■ y ■ )<br>
 * n &#9; 4th nibble of opcode &#9; ( ■ ■ ■ n )<br>
 * kk &#9; last 2 nibbles &#9; ( ■ ■ k k )<br>
 * nnn &#9; last 3 nibbles &#9; ( ■ n n n )<br>
 */
public class Opcode {
    private final int[] opcode;
    private final int instruction;

    public Opcode(int firstByte, int secondByte) {
        this(((firstByte << 8) & 0xFF00) | (secondByte & 0x00FF));
    }

    public Opcode(int instruction) {
        this.instruction = instruction;
        this.opcode = new int[4];
        this.opcode[0] = instruction >> 12 & 0x00F;
        this.opcode[1] = instruction >> 8 & 0x00F;
        this.opcode[2] = instruction >> 4 & 0x00F;
        this.opcode[3] = instruction & 0x00F;
    }

    /**
     * Returns the i-th nibble of the opcode
     *
     * @param index index of the nibble to return.
     * @return the i-th nibble of the opcode
     */
    public int getNibble(int index) {
        Objects.checkIndex(index, opcode.length);
        return opcode[index];
    }

    /**
     * @return the first nibble of the opcode
     */
    public int o() {
        return getNibble(0);
    }

    /**
     * @return the second nibble of the opcode
     */
    public int x() {
        return getNibble(1);
    }

    /**
     * @return the third nibble of the opcode
     */
    public int y() {
        return getNibble(2);
    }

    /**
     * @return the fourth nibble of the opcode
     */
    public int n() {
        return getNibble(3);
    }

    /**
     * @return the last two nibbles of the opcode
     */
    public int kk() {
        return instruction & 0xFF;
    }

    /**
     * @return the last three nibbles of the opcode
     */
    public int nnn() {
        return instruction & 0xFFF;
    }

    /**
     * @return the integer equivalent of the opcode
     */
    public int fullValue() {
        return instruction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Opcode opcode = (Opcode) o;

        return instruction == opcode.instruction;
    }

    @Override
    public int hashCode() {
        return instruction;
    }
}
