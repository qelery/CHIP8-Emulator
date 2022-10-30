package com.qelery.chip8;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpcodeTest {

    Opcode opcode;
    int instruction;
    int nibble1, nibble2, nibble3, nibble4;

    @BeforeEach
    void setup() {
        this.nibble1 = 0x1;
        this.nibble2 = 0x2;
        this.nibble3 = 0x3;
        this.nibble4 = 0x4;

        this.instruction = (nibble1 << 12) + (nibble2 << 8) + (nibble3 << 4) + nibble4;
        this.opcode = new Opcode(instruction);
    }

    @Test
    @DisplayName("Should get the retrieve the i-th nibble of the opcode")
    void getNibble() {
        assertEquals(nibble1, opcode.getNibble(0));
        assertEquals(nibble2, opcode.getNibble(1));
        assertEquals(nibble3, opcode.getNibble(2));
        assertEquals(nibble4, opcode.getNibble(3));
    }

    @Test
    @DisplayName("Should return the first nibble of the opcode")
    void o() {
        assertEquals(nibble1, opcode.o());
    }

    @Test
    @DisplayName("Should return the second nibble of the opcode")
    void x() {
        assertEquals(nibble2, opcode.x());
    }

    @Test
    @DisplayName("Should return the third nibble of the opcode")
    void y() {
        assertEquals(nibble3, opcode.y());
    }

    @Test
    @DisplayName("Should return the fourth nibble of the opcode")
    void n() {
        assertEquals(nibble4, opcode.n());
    }

    @Test
    @DisplayName("Should return the last two nibbles of the opcode")
    void kk() {
        int expectedKK = (nibble3 << 4) + nibble4;

        assertEquals(expectedKK, opcode.kk());
    }

    @Test
    @DisplayName("Should return the last three nibbles of the opcode")
    void nnn() {
        int expectedNNN = (nibble2 << 8) + (nibble3 << 4) + nibble4;

        assertEquals(expectedNNN, opcode.nnn());
    }

    @Test
    @DisplayName("Should return the integer value of the opcode")
    void intValue() {
        int expectedIntValue = (nibble1 << 12) + (nibble2 << 8) + (nibble3 << 4) + nibble4;

        assertEquals(expectedIntValue, opcode.fullValue());
    }

    @Test
    @DisplayName("Should return a hex string representation of the opcode")
    void toHexString() {
        int instruction = (0xa << 12) + (0x2 << 8) + (0xe << 4) + 0xf;
        Opcode opcode = new Opcode(instruction);
        String expectedHexString = "0xA2EF";

        String actualHexString = opcode.toHexString();

        assertEquals(expectedHexString, actualHexString);
    }

    @Test
    @DisplayName("Should return true if two opcodes have the same integer value")
    void testEquals_returnTrue() {
        int firstByte = 0x0055;
        int secondByte = 0x0022;
        Opcode opcode1 = new Opcode(firstByte, secondByte);
        Opcode opcode2 = new Opcode(firstByte, secondByte);

        assertEquals(opcode1, opcode2);


        int instruction = 0x13FF;
        opcode1 = new Opcode(instruction);
        opcode2 = new Opcode(instruction);

        assertEquals(opcode1, opcode2);
    }

    @Test
    @DisplayName("Should return false if two opcodes have the different integer values")
    void testEquals_returnFalse() {
        int firstByte = 0x0055;
        int secondByte = 0x0022;
        int differentSecondByte = 0x0044;
        Opcode opcode1 = new Opcode(firstByte, secondByte);
        Opcode opcode2 = new Opcode(firstByte, differentSecondByte);

        assertNotEquals(opcode1, opcode2);


        int instruction = 0x13FF;
        int differentInstruction = 0x30AC;
        opcode1 = new Opcode(instruction);
        opcode2 = new Opcode(differentInstruction);
        assertNotEquals(opcode1, opcode2);
    }
}