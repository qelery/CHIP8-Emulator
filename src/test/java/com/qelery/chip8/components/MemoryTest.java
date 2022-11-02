package com.qelery.chip8.components;

import com.qelery.chip8.components.Memory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    Memory memory;

    @BeforeEach
    void setup() {
        int typicalMemoryLocationsInChip8 = 4096;
        this.memory = new Memory(typicalMemoryLocationsInChip8);
    }

    @Test
    @DisplayName("Should load data, represented as ints, into a specific memory location")
    void loadData_asInts() {
        int[] data = new int[] {50, 51, 52};
        int offset = 600;

        memory.loadData(data, offset);

        assertEquals(50, memory.readByte(offset));
        assertEquals(51, memory.readByte(offset + 1));
        assertEquals(52, memory.readByte(offset + 2));
    }

    @Test
    @DisplayName("Should load data, represented as unsigned bytes, into a specific memory location")
    void loadData_asUnsignedBytes() {
        byte signedByte = -52;
        int unsignedByteEquivalent = 204;
        byte[] data = new byte[] {50, 51, signedByte};
        int offset = 600;

        memory.loadData(data, offset);

        assertEquals(50, memory.readByte(offset));
        assertEquals(51, memory.readByte(offset + 1));
        assertEquals(unsignedByteEquivalent, memory.readByte(offset + 2));
    }

    @Test
    @DisplayName("Should read an byte, represented as an int, from a specific memory location")
    void readByte() {
        byte expectedValue = 50;
        int address = 600;
        memory.writeByte(expectedValue, address);

        int actualValue = memory.readByte(address);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @DisplayName("Should read a byte, represented as an int, from a specific memory location and ensure it is unsigned")
    void readByte_unsigned() {
        int signedValue = -52;
        int expectedUnsignedValue = 204;
        int address = 600;
        memory.writeByte(signedValue, address);

        int actualValue = memory.readByte(address);

        assertEquals(expectedUnsignedValue, actualValue);
    }

    @Test
    @DisplayName("Should write a byte, represented as an int, to a specific memory location")
    void writeByte() {
        int expectedValue = 50;
        int address = 600;

        memory.writeByte(expectedValue, address);

        assertEquals(expectedValue, memory.readByte(address));
    }

    @Test
    @DisplayName("Should write a byte, represented as an int, to a specific memory location and ensure it is unsigned")
    void writeByte_unsigned() {
        int signedValue = -52;
        int expectedUnsignedValue = 204;
        int address = 600;

        memory.writeByte(signedValue, address);

        assertEquals(expectedUnsignedValue, memory.readByte(address));
    }

    @Test
    @DisplayName("Should load sprites into first 80 bytes of memory when a Memory object is constructed")
    void loadsSpritesOnConstruction() {
        int memoryLocations = 4096;

        Memory memoryObj = new Memory(memoryLocations);

        int expectedEndOfSpritesLocation = 80;
        for (int i = 0; i < expectedEndOfSpritesLocation; i++) {
            assertNotEquals(0x0, memoryObj.readByte(i));
        }
        assertEquals(0x0, memoryObj.readByte(expectedEndOfSpritesLocation));
    }
}