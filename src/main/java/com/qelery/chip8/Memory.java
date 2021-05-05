package com.qelery.chip8;

public class Memory {

    private final int[] RAM;

    /**
     * Creates a Memory object where the font sprite and ROM data are loaded.
     *
     * The original CHIP-8 interpreter was usually implemented on microcomputers
     * that had 4096 memory locations of RAM.
     *
     * @param size
     *              the number of memory locations
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
        return RAM[address] & 0xFF;
    }

    public void writeByte(int address, int value) {
        RAM[address] = value;
    }
}


