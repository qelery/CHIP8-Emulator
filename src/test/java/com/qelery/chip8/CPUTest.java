package com.qelery.chip8;

import com.qelery.chip8.sound.SineWave;
import com.qelery.chip8.sound.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
class CPUTest {

    CPU cpu;

    @Mock
    Memory memory;
    @Mock
    Screen screen;
    @Mock
    Sound sound;
    @Mock
    Keyboard keyboard;

    @BeforeEach
    void setup() {
        int clockSpeed = 500;
        this.cpu = new CPU(clockSpeed, memory, screen, sound, keyboard);
    }

    @Nested
    @DisplayName("tickClocks")
    class TickClocks {
        @Test
        @DisplayName("Should decrement the delay timer by 1")
        void tickClocks_decrementsDelayTimer() {
            int start = 10;
            cpu.setDelayTimer(start);

            cpu.tickClocks();

            assertEquals(start - 1, cpu.getDelayTimer());
        }

        @Test
        @DisplayName("Should decrement the sound timer by 1")
        void tickClocks_decrementsSoundTimer() {
            int start = 10;
            cpu.setSoundTimer(start);

            cpu.tickClocks();

            assertEquals(start - 1, cpu.getSoundTimer());
        }

        @Test
        @DisplayName("Should not decrement delay timer if it is zero")
        void tickClocks_delayTimerStaysDoesNotDropBelowZero() {
            int start = 0;
            cpu.setDelayTimer(start);

            cpu.tickClocks();

            assertEquals(start, cpu.getDelayTimer());
        }

        @Test
        @DisplayName("Should not decrement sound timer if it is zero")
        void tickClocks_soundTimerStaysDoesNotDropBelowZero() {
            int start = 0;
            cpu.setSoundTimer(start);

            cpu.tickClocks();

            assertEquals(start, cpu.getSoundTimer());
        }

        @Test
        @DisplayName("Should stop the sound when sound timer reaches zero")
        void tickClocks_stopSoundWhenSoundTimerZero() {
            int start = 1;
            cpu.setSoundTimer(start);
            cpu.tickClocks();
            assertEquals(0, cpu.getSoundTimer());
            Mockito.verify(sound).stop();

            Mockito.reset(sound);
            start = 100;
            cpu.setSoundTimer(start);
            cpu.tickClocks();
            assertNotEquals(0, cpu.getSoundTimer());
            Mockito.verify(sound, Mockito.never()).stop();
        }
    }

    @Test
    @DisplayName("Should get two unsigned bytes from memory as the next opcode")
    void fetchInstruction() {
        int firstByte = 0x0012;
        int secondByte = 0x0010;
        Mockito.when(memory.readByte(anyInt())).thenReturn(firstByte, secondByte);

        cpu.fetchInstruction();

        Opcode expectedOpcode = new Opcode(0x1210);
        assertEquals(expectedOpcode, cpu.getOpcode());
        assertEquals(expectedOpcode.fullValue(), cpu.getOpcode().fullValue());
    }

    @Test
    @DisplayName("Should increment the program counter by 2")
    void emulateCycle() {
        int value = 5;
        cpu.setPc(value);

        cpu.incrementPC();

        assertEquals(value + 2, cpu.getPc());
    }

    // Read the docstring of the executeInstruction method to see the
    // instruction nibble notations (e.g. nnn) used in these tests
    @Nested
    @DisplayName("executeInstruction")
    class ExecuteInstruction {

        @Test
        @DisplayName("Instruction 00E0 - should clear screen")
        void executeInstruction_instruction00E0_shouldClearScreen() {
            cpu.setDrawFlag(false);
            int twoByteInstruction = 0x00E0;
            cpu.setOpcode(twoByteInstruction);

            cpu.executeInstruction();

            Mockito.verify(screen).clear();
            assertTrue(cpu.isDrawFlagSet());
        }

        @Test
        @DisplayName("Instruction 00E0 - should return from a subroutine")
        void executeInstruction_instruction00EE_shouldReturnFromSubroutine() {
            int startingSP = 2;
            cpu.setSp(startingSP);
            int stackValue = 5;
            cpu.getStack()[startingSP] = stackValue;
            int instruction = 0x00EE;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(stackValue, cpu.getPc());
            assertEquals(startingSP - 1, cpu.getSp());
        }

        @Test
        @DisplayName("Opcode 1 - should jump to location nnn")
        void executeInstruction_opcode1_shouldJump() {
            int startingPC = 5;
            cpu.setPc(startingPC);
            int opcode = 0x1; // first nibble of instruction
            int nnn = 0x034F; // last 3 nibble of instruction
            int instruction = (opcode << 12) + nnn;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(nnn, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 2 - should call subroutine at address nnn")
        void executeInstruction_opcode2_shouldCallSubroutine() {
            int startingSP = 4;
            cpu.setSp(startingSP);
            int startingPC = 6;
            cpu.setPc(startingPC);
            int opcode = 0x2; // first nibble of instruction
            int nnn = 0x034F; // last 3 nibble of instruction
            int instruction = (opcode << 12) + nnn;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingSP + 1, cpu.getSp());
            assertEquals(startingPC, cpu.getStack()[startingSP + 1]);
            assertEquals(nnn, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 3 - should skip next instruction if VRegister[x] equals kk")
        void executeInstruction_opcode3_shouldConditionallySkipNextInstruction() {
            int opcode = 0x3; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            cpu.getVRegister()[x] = kk; // VRegister[x] equals kk
            int startingPC = 10;
            cpu.setPc(startingPC);
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC + 2, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 3 - should not skip next instruction if VRegister[x] does not equal kk")
        void executeInstruction_opcode3_shouldConditionallyNotSkipNextInstruction() {
            int opcode = 0x3; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            cpu.getVRegister()[x] = 0x0044; // VRegister[x] does not equal kk
            int startingPC = 10;
            cpu.setPc(startingPC);
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 4 - should skip next instruction if VRegister[x] does not equal kk")
        void executeInstruction_opcode4_shouldConditionallySkipNextInstruction() {
            int opcode = 0x4; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            cpu.getVRegister()[x] = 0x0044; // VRegister[x] does not equal kk
            int startingPC = 10;
            cpu.setPc(startingPC);
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC + 2, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 4 - should not skip next instruction if VRegister[x] equals kk")
        void executeInstruction_opcode4_shouldConditionallyNotSkipNextInstruction() {
            int opcode = 0x4; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            cpu.getVRegister()[x] = kk; // VRegister[x] equals kk
            int startingPC = 10;
            cpu.setPc(startingPC);
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 5 - should skip next instruction if n equals 0 and VRegister[x] equals VRegister[y]")
        void executeInstruction_opcode5_shouldConditionallySkipNextInstruction() {
            int opcode = 0x5; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int y = 0x001; // third nibble of instruction
            int n = 0x0000; // last nibble of instruction
            int kk = (y << 4) + n;
            cpu.getVRegister()[x] = 5;
            cpu.getVRegister()[y] = cpu.getVRegister()[x];
            int startingPC = 10;
            cpu.setPc(startingPC);
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC + 2, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 5 - should not skip next instruction if n does not 0 or VRegister[x] does not equal VRegister[y]")
        void executeInstruction_opcode5_shouldConditionallyNotSkipNextInstruction() {
            // n equals 0, VRegister[x] does not equal VRegister[y]
            int opcode = 0x5; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int y = 0x001; // third nibble of instruction
            int n = 0x0000; // last nibble of instruction
            int kk = (y << 4) + n; // last two nibbles of instruction
            cpu.getVRegister()[x] = 5;
            cpu.getVRegister()[y] = 6;
            int startingPC = 10;
            cpu.setPc(startingPC);
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC, cpu.getPc());


            // n does not equal 0, VRegister[x] equals VRegister[y]
            n = 0x0005; // last nibble of instruction
            kk = (y << 4) + n; // last two nibbles of instruction
            cpu.getVRegister()[x] = 5;
            cpu.getVRegister()[y] = cpu.getVRegister()[x];
            cpu.setPc(startingPC);
            instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(startingPC, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 6 - should write value kk to VRegister[x]")
        void executeInstruction_opcode6_shouldWriteValueToVRegister() {
            int opcode = 0x6; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(kk, cpu.getVRegister()[x]);
        }

        @Test
        @DisplayName("Opcode 7 - should write value kk plus VRegister[x] to VRegister[x]")
        void executeInstruction_opcode7_shouldWriteValueToVRegister() {
            int opcode = 0x7; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            int instruction = (opcode << 12) + (x << 8) + kk;
            int initialVRegisterValue = 0x0001;
            cpu.getVRegister()[x] = initialVRegisterValue;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(kk + initialVRegisterValue, cpu.getVRegister()[x]);
        }

        @Test
        @DisplayName("Opcode 7 - should ensure VRegister[x], which represents an unsigned byte, resolves overflow")
        void executeInstruction_opcode7_shouldNotOverflowUnsignedByte() {
            int opcode = 0x7; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x00FF; // last 2 nibbles of instruction
            int instruction = (opcode << 12) + (x << 8) + kk;
            int initialVRegisterValue = 0x00F;
            cpu.getVRegister()[x] = initialVRegisterValue;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(0x00E, cpu.getVRegister()[x]);
        }

        @Nested
        @DisplayName("Opcode - 8")
        class Opcode8 {

            @Test
            @DisplayName("Opcode = 8, n = 0 - should set VRegister[x] to VRegister[y]")
            void executeInstruction_opcode8_n0_shouldWriteValueToVRegister() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0000; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                cpu.getVRegister()[x] = 5;
                cpu.getVRegister()[y] = 10;

                cpu.executeInstruction();

                assertEquals(cpu.getVRegister()[y], cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 1 - should set VRegister[x] to bitwise OR of its value and VRegister[y]")
            void executeInstruction_opcode8_n1_shouldWriteValueToVRegister() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0001; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 5;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 11;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(initialVRegisterXValue | initialVRegisterYValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 2 - should set VRegister[x] to bitwise AND of its value and VRegister[y]")
            void executeInstruction_opcode8_n2_shouldWriteValueToVRegister() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0002; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 5;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 11;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(initialVRegisterXValue & initialVRegisterYValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 3 - should set VRegister[x] to bitwise XOR of its value and VRegister[y]")
            void executeInstruction_opcode8_n3_shouldWriteValueToVRegister() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0003; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 5;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 11;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(initialVRegisterXValue ^ initialVRegisterYValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 4 - should sum VRegister[x] add VRegister[y] " +
                    "and set VRegister[x] to the lowest byte of the sum, resolving unsigned byte overflow")
            void executeInstruction_opcode8_n4_shouldWriteValueToVRegisterX() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0004; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x003F;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x001A;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                int expectedSumLowestByte = (initialVRegisterXValue + initialVRegisterYValue) & 0xFF;
                assertEquals(expectedSumLowestByte, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 4 - should set VRegister[F] to 1 if the lowest byte of the sum of " +
                    "VRegister[x] and VRegister[y] is greater than the value of an unsigned byte")
            void executeInstruction_opcode8_n4_shouldWriteOneToVRegisterF() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0004; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x00C9;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x00AA;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(0x0001, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 4 - should set VRegister[F] to 0 if the lowest byte of the sum of " +
                    "VRegister[x] and VRegister[y] is less than or equal to than the value of an unsigned byte")
            void executeInstruction_opcode8_n4_shouldWriteZeroToVRegisterF() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0004; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x003F;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x001A;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(0x0000, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 5 - should set VRegister[x] to difference of VRegister[x] and VRegister[y]")
            void executeInstruction_opcode8_n5_shouldWriteSubtractVRegisterYFromVRegisterX() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0005; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 5;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 11;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                int expectedVRegisterXValue = (initialVRegisterXValue - initialVRegisterYValue) & 0x00FF;
                assertEquals(expectedVRegisterXValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 5 - should set VRegister[F] to 1 if VRegister[x] greater than VRegister[y]")
            void executeInstruction_opcode8_n5_shouldSetVRegisterFToOne() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0005; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 100;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 1;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(1, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 5 - should set VRegister[F] to 0 if VRegister[x] less than VRegister[y]")
            void executeInstruction_opcode8_n5_shouldSetVRegisterFToZero() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0005; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                cpu.getVRegister()[0xF] = 0x00FF;
                int initialVRegisterXValue = 1;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 100;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(0, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 5 - should set subtract VRegister[y] from VRegister[x], " +
                    "resolving unsigned byte underflow")
            void executeInstruction_opcode8_n5_shouldSetVRegisterX() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0005; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x0022;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x00FF;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                int expectedVRegisterXValue = (initialVRegisterXValue - initialVRegisterYValue) & 0x00FF;
                assertEquals(expectedVRegisterXValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 6 - should do unsigned right bitshift on VRegister[x]")
            void executeInstruction_opcode8_n6_shouldSetVRegisterX() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0006; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x00AC;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(initialVRegisterXValue >>> 1, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 6 - should set VRegister[F] to the least significant bit of VRegister[x]")
            void executeInstruction_opcode8_n6_shouldSetVRegisterF() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0006; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x0001;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(1, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 7 - should set VRegister[x] to difference of VRegister[y] and VRegister[x]")
            void executeInstruction_opcode8_n7_shouldSetVRegisterX() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0007; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x0022;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x0033;
                cpu.getVRegister()[y] = initialVRegisterYValue;


                cpu.executeInstruction();

                assertEquals(initialVRegisterYValue - initialVRegisterXValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 7 - should set VRegister[F] to 1 if VRegister[y] greater than VRegister[x]")
            void executeInstruction_opcode8_n7_shouldSetVRegisterFToOne() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0007; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x0022;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x0033;
                cpu.getVRegister()[y] = initialVRegisterYValue;


                cpu.executeInstruction();

                assertEquals(1, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = 7 - should set VRegister[F] to 0 if VRegister[y] less than or equal to VRegister[x]")
            void executeInstruction_opcode8_n7_shouldSetVRegisterFToZero() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x0007; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                cpu.getVRegister()[0xF] = 0x00FF;
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                int initialVRegisterYValue = 0x0033;
                cpu.getVRegister()[y] = initialVRegisterYValue;


                cpu.executeInstruction();

                assertEquals(0, cpu.getVRegister()[0xF]);


                initialVRegisterXValue = 0x0077;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                initialVRegisterYValue = 0x0077;
                cpu.getVRegister()[y] = initialVRegisterYValue;

                cpu.executeInstruction();

                assertEquals(0, cpu.getVRegister()[0xF]);
            }

            @Test
            @DisplayName("Opcode = 8, n = E - should do unsigned left bitshift on VRegister[x]")
            void executeInstruction_opcode8_nE_shouldSetVRegisterX() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x000E; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x00AC;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(0x0058, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = 8, n = E - should set VRegister[F] to the most significant bit of VRegister[x]")
            void executeInstruction_opcode8_nE_shouldSetVRegisterF() {
                int opcode = 0x8; // first nibble of instruction
                int x = 0x02; // second nibble of instruction
                int y = 0x003; // third nibble of instruction
                int n = 0x000E; // last nibble of instruction
                int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
                cpu.setOpcode(instruction);
                int initialVRegisterXValue = 0x00AC;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(1, cpu.getVRegister()[0xF]);
            }
        }

        @Test
        @DisplayName("Opcode 9 - should skip next instruction if n equals 0 and VRegister[x] does not equal VRegister[y]")
        void executeInstruction_opcode9_shouldConditionallySkipNextInstruction() {
            int startingPC = 5;
            cpu.setPc(startingPC);
            int opcode = 0x9; // first nibble of instruction
            int x = 0x02; // second nibble of instruction
            int y = 0x003; // third nibble of instruction
            int n = 0x0000; // last nibble of instruction
            int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
            cpu.setOpcode(instruction);
            int initialVRegisterXValue = 0x0055;
            cpu.getVRegister()[x] = initialVRegisterXValue;
            int initialVRegisterYValue = 0x0033;
            cpu.getVRegister()[y] = initialVRegisterYValue;

            cpu.executeInstruction();

            assertEquals(startingPC + 2, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode 9 - should not skip next instruction if VRegister[x] equals VRegister[y]")
        void executeInstruction_opcode9_shouldConditionallyNotSkipNextInstruction() {
            int startingPC = 5;
            cpu.setPc(startingPC);
            int opcode = 0x9; // first nibble of instruction
            int x = 0x02; // second nibble of instruction
            int y = 0x003; // third nibble of instruction
            int n = 0x0007; // last nibble of instruction
            int instruction = (opcode << 12) + (x << 8) + (y << 4) + n;
            cpu.setOpcode(instruction);
            int initialVRegisterXValue = 0x0055;
            cpu.getVRegister()[x] = initialVRegisterXValue;
            cpu.getVRegister()[y] = cpu.getVRegister()[x];

            cpu.executeInstruction();

            assertEquals(startingPC, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode A - should set IRegister to nnn")
        void executeInstruction_opcodeA_shouldSetIRegisterToNNN() {
            int opcode = 0xA; // first nibble of instruction
            int nnn = 0x0999; // last 3 nibbles of instruction
            int instruction = (opcode << 12) + nnn;
            cpu.setOpcode(instruction);

            cpu.executeInstruction();

            assertEquals(nnn, cpu.getIRegister());
        }

        @Test
        @DisplayName("Opcode B - should jump to location nnn plus VRegister[0]")
        void executeInstruction_opcodeB_shouldJump() {
            int opcode = 0xB; // first nibble of instruction
            int nnn = 0x0999; // last 3 nibbles of instruction
            int instruction = (opcode << 12) + nnn;
            cpu.setOpcode(instruction);
            int vRegister0Value = 0x0055;
            cpu.getVRegister()[0] = vRegister0Value;

            cpu.executeInstruction();

            assertEquals(nnn + vRegister0Value, cpu.getPc());
        }

        @Test
        @DisplayName("Opcode C - should set VRegister[x] to a random unsigned byte and bitwise AND kk")
        void executeInstruction_opcodeC_shouldSetVRegister() {
            int opcode = 0xC; // first nibble of instruction
            int x = 0x07; // second nibble of instruction
            int kk = 0x0055; // last 2 nibbles of instruction
            int instruction = (opcode << 12) + (x << 8) + kk;
            cpu.setOpcode(instruction);
            // Set VRegister to a signed value so that we will know the test is setting it to a random unsigned value
            int initialVRegisterX = -1;
            cpu.getVRegister()[x] = initialVRegisterX;

            int manyTrials = 1000;
            for (int i = 0; i < manyTrials; i++) {
                cpu.executeInstruction();

                int actualRandomUnsignedByte = cpu.getVRegister()[x];
                assertTrue(actualRandomUnsignedByte >= 0);
                assertTrue(actualRandomUnsignedByte < 266);
            }
        }

        @Test
        @DisplayName("Opcode D")
        void executeInstruction_opcodeD() {
            fail("Unimplemented.");
        }

        @Nested
        @DisplayName("Opcode - E")
        class OpcodeE {

            @Test
            @DisplayName("Opcode = E, kk = 9E - should skip next instruction if key with the value of VRegister[x] is pressed")
            void executeInstruction_opcodeE_kk9E_shouldConditionallySkipNextInstruction() {
                int startingPC = 10;
                cpu.setPc(startingPC);
                int opcode = 0xE; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x009E; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                Mockito.when(keyboard.isKeyDown(cpu.getVRegister()[x])).thenReturn(true);

                cpu.executeInstruction();

                assertEquals(startingPC + 2, cpu.getPc());
            }

            @Test
            @DisplayName("Opcode = E, kk = 9E - should not skip next instruction if key with the value of VRegister[x] is not pressed")
            void executeInstruction_opcodeE_kk9E_shouldConditionallyNotSkipNextInstruction() {
                int startingPC = 10;
                cpu.setPc(startingPC);
                int opcode = 0xE; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x009E; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                Mockito.when(keyboard.isKeyDown(cpu.getVRegister()[x])).thenReturn(false);

                cpu.executeInstruction();

                assertEquals(startingPC, cpu.getPc());
            }

            @Test
            @DisplayName("Opcode = E, kk = A1 - should skip next instruction if key with the value of VRegister[x] is not pressed")
            void executeInstruction_opcodeE_kkA1_shouldConditionallySkipNextInstruction() {
                int startingPC = 10;
                cpu.setPc(startingPC);
                int opcode = 0xE; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x00A1; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                Mockito.when(keyboard.isKeyDown(cpu.getVRegister()[x])).thenReturn(false);

                cpu.executeInstruction();

                assertEquals(startingPC + 2, cpu.getPc());
            }

            @Test
            @DisplayName("Opcode = E, kk = A1 - should not skip next instruction if key with the value of VRegister[x] is pressed")
            void executeInstruction_opcodeE_kkA1_shouldConditionallyNotSkipNextInstruction() {
                int startingPC = 10;
                cpu.setPc(startingPC);
                int opcode = 0xE; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x00A1; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                Mockito.when(keyboard.isKeyDown(cpu.getVRegister()[x])).thenReturn(true);

                cpu.executeInstruction();

                assertEquals(startingPC, cpu.getPc());
            }
        }

        @Nested
        @DisplayName("Opcode - F")
        class OpcodeF {

            @Test
            @DisplayName("Opcode = F, kk = 07 - should set VRegister[x] to delay timer")
            void executeInstruction_opcodeF_kk07_shouldSetVRegisterToDelayTimer() {
                int opcode = 0xF; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x0007; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                int delayTimerValue = 0x0066;
                cpu.setDelayTimer(delayTimerValue);
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(delayTimerValue, cpu.getVRegister()[x]);
            }

            @Test
            @DisplayName("Opcode = F, kk = 0A - should wait for a key press then store the value of the key in VRegister[x]")
            void executeInstruction_opcodeF_kk0A_shouldWaitForKeyPressThenStoreKeyValueInVRegister() {
                fail("Unimplemented.");
            }

            @Test
            @DisplayName("Opcode = F, kk = 18 - should set sound timer to VRegister[x]")
            void executeInstruction_opcodeF_kk18_shouldSetSoundTimerToVRegister() {
                int opcode = 0xF; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x0018; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                int soundTimerValue = 0x0066;
                cpu.setSoundTimer(soundTimerValue);
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(initialVRegisterXValue, cpu.getSoundTimer());
            }

            @Test
            @DisplayName("Opcode = F, kk = 1E - should add VRegister[x] to IRegister")
            void executeInstruction_opcodeF_kk1E_shouldAddVRegisterToIRegister() {
                int opcode = 0xF; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x001E; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                int initialIRegisterValue = 0x0022;
                cpu.setIRegister(initialIRegisterValue);
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                assertEquals(initialIRegisterValue + initialVRegisterXValue, cpu.getIRegister());
            }

            @Test
            @DisplayName("Opcode = F, kk = 29 - should set IRegister to the value VRegister[x] times 5 and set draw flag to true")
            void executeInstruction_opcodeF_kk29_shouldSetIRegisterAndSetDrawFlagToTrue() {
                int opcode = 0xF; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x0029; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                int initialIRegisterValue = 0x0022;
                cpu.setIRegister(initialIRegisterValue);
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                cpu.setDrawFlag(false);

                cpu.executeInstruction();

                assertEquals(initialVRegisterXValue * 5, cpu.getIRegister());
                assertTrue(cpu.isDrawFlagSet());
            }

            @Test
            @DisplayName("Opcode = F, kk = 33 - should store the binary coded decimal representation of VRegister[x] " +
                    "in memory locations IRegister, IRegister + 1, and IRegister + 2")
            void executeInstruction_opcodeF_kk33_shouldStoreBCDRepresentationOfVRegisterInMemory() {
                int opcode = 0xF; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x0033; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                int initialIRegisterValue = 0x0022;
                cpu.setIRegister(initialIRegisterValue);
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;

                cpu.executeInstruction();

                Mockito.verify(memory, Mockito.times(1)).writeByte(initialVRegisterXValue % 1000 / 100, initialIRegisterValue);
                Mockito.verify(memory, Mockito.times(1)).writeByte(initialVRegisterXValue % 100 / 10, initialIRegisterValue + 1);
                Mockito.verify(memory, Mockito.times(1)).writeByte(initialVRegisterXValue % 10, initialIRegisterValue + 2);
            }

            @Test
            @DisplayName("Opcode = F, kk = 65 - should load registers VRegister[0] through VRegister[x] from memory " +
                    "starting at location IRegister")
            void executeInstruction_opcodeF_kk65_shouldLoadVRegistersFromMemory() {
                int opcode = 0xF; // first nibble of instruction
                int x = 0x07; // second nibble of instruction
                int kk = 0x0065; // last 2 nibbles of instruction
                int instruction = (opcode << 12) + (x << 8) + kk;
                cpu.setOpcode(instruction);
                int initialIRegisterValue = 0x0022;
                cpu.setIRegister(initialIRegisterValue);
                int initialVRegisterXValue = 0x0055;
                cpu.getVRegister()[x] = initialVRegisterXValue;
                List<Integer> arbitraryExpectedMemoryValues = List.of(2, 4, 6, 8, 10, 12, 14, 16);
                for (int i = 0; i < x + 1; i++) {
                    Mockito.when(memory.readByte(initialIRegisterValue + i))
                            .thenReturn(arbitraryExpectedMemoryValues.get(i));
                }

                cpu.executeInstruction();

                for (int i = 0; i < x + 1; i++) {
                    assertEquals(arbitraryExpectedMemoryValues.get(i), cpu.getVRegister()[i]);
                }
            }
        }
    }
}