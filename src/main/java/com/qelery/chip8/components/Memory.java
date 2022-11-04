package com.qelery.chip8.components;

/**
 * A Memory object where the font sprite and ROM data are loaded.
 * <p>
 * CHIP-8 was most commonly implemented on microcomputers that had
 * 4KB RAM.
 * <p>
 * <h2>References</h2>
 * <a href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM">Cowgod's Chip-8 Technical Reference</a><br>
 * <a href="https://en.wikipedia.org/wiki/CHIP-8">Wikipedia - CHIP-8</a><br>
 */
public class Memory {

    /**
     * The first 512 bytes of memory held the interpreter in the
     * original CHIP-8. Loading and reading ROM data starts after
     * that memory location.
     */
    public static final int READ_WRITE_START_LOCATION = 512;
    public static final int DEFAULT_SIZE = 4096;

    private final int[] RAM;

    /**
     * Creates a Memory object that serves as the CHIP-8 RAM.
     */
    public Memory(int size) {
        this.RAM = new int[size];

        int[] fontSprites = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        loadData(fontSprites, 0);
    }

    public void loadData(int[] data, int offset) {
        System.arraycopy(data, 0, RAM, offset, data.length);
    }

    public void loadData(byte[] data, int offset) {
        for (int i = 0; i < data.length; i++) {
            RAM[i + offset] = data[i] & 0xFF; // unsigned representation
        }
    }

    public int readByte(int address) {
        return RAM[address] & 0xFF; // unsigned representation
    }

    public void writeByte(int value, int address) {
        RAM[address] = value;
    }
}


