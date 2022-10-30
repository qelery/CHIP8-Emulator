package com.qelery.chip8;

import com.qelery.chip8.sound.Sound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * A class to create an emulated cpu for fetching, decoding, and executing
 * instructions from a CHIP-8 ROM.
 * <p>
 * <h2>References</h2>
 * <a href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM">Cowgod's Chip-8 Technical Reference</a><br>
 * <a href="http://www.cs.columbia.edu/~sedwards/classes/2016/4840-spring/designs/Chip8.pdf">Columbia University - Chip-8 Design Specification</a><br>
 * <a href="https://en.wikipedia.org/wiki/CHIP-8">Wikipedia - CHIP-8</a><br>
 */
public class CPU {

    private static final Logger logger = LogManager.getLogger(CPU.class);

    /**
     * The clock speed of the CPU in  hertz.
     */
    private final int clockSpeed;

    /**
     * A decrementing timer. Its value is used to set vRegisters on some
     * instructions.
     */
    private int delayTimer;

    /**
     * A decrementing, non-zero timer which causes CHIP-8 to buzz when its
     * value is non-zero.
     */
    private int soundTimer;

    /**
     * The next opcode that the CPU will execute.
     */
    private Opcode opcode;

    /**
     * Represents 16, 8-bit unsigned registers.
     */
    private final int[] VRegister;

    /**
     * Represents an array of 16, 16-bit unsigned values used to store
     * the address that the interpreter should return to when finished
     * with a subroutine.
     */
    private final int[] stack;

    /**
     * Represents a 16-bit register. This register is generally used to
     * store memory addresses. Only the lowest 12 big are usually used.
     */
    private int IRegister;

    /**
     * Represents an 8-bit point used to point to the top of the stack.
     */
    private int sp;

    /**
     * Represents an 8-bit program counter used to store the currently executing
     * address.
     */
    private int pc;

    /**
     * A flag which is used to determine if CHIP-8 should render the
     * screen.
     */
    private boolean drawFlag;

    private final Memory memory;
    private final Display display;
    private final Sound sound;
    private final Keyboard keyboard;
    private final Random random;

    public CPU(int clockSpeed, Memory memory, Display display, Sound sound, Keyboard keyboard) {
        this.clockSpeed = clockSpeed;
        this.pc = Memory.READ_START_LOCATION;
        this.VRegister = new int[16];
        this.stack = new int[16];
        this.drawFlag = false;
        this.memory = memory;
        this.display = display;
        this.sound = sound;
        this.keyboard = keyboard;
        this.random = new Random();
    }

    public void clearDrawFlag() {
        drawFlag = false;
    }

    public void tickClocks() {
        if (delayTimer > 0) {
            delayTimer--;
        }

        if (soundTimer > 0) {
            sound.play();
            soundTimer--;
            if (soundTimer == 0) {
                sound.stop();
            }
        }
    }

    public void emulateCycle() {
        fetchInstruction();
        incrementPC();
        executeInstruction();
    }

    protected void fetchInstruction() {
        int firstByte = memory.readByte(pc);
        int secondByte = memory.readByte(pc + 1);
        this.opcode = new Opcode(firstByte, secondByte);
    }

    protected void incrementPC() {
        pc += 2;
    }

    protected void executeInstruction() {
        final String UNKNOWN_OPCODE_MESSAGE = "Unknown opcode: {}";

        if (this.opcode.fullValue() == 0x00E0) {
            clearScreen_0x00E0();
            return;
        }

        if (this.opcode.fullValue() == 0x00EE) {
            returnFromSubroutine_0x00EE();
            return;
        }

        switch (opcode.o()) {
            case 0x1 -> op_1NNN_jumpToLocation();
            case 0x2 -> op_2NNN_callSubroutine();
            case 0x3 -> op_3XKK_skipIfRegisterEqualsValue();
            case 0x4 -> op_4XKK_skipIfRegisterNotEqualValue();
            case 0x5 -> op_5XY0_skipIfRegistersEqual();
            case 0x6 -> op_6XKK_setValueToRegister();
            case 0x7 -> op_7XKK_addValueToRegister();
            case 0x8 -> {
                switch (opcode.n()) {
                    case 0x0 -> op_8XY0_setRegistersEqual();
                    case 0x1 -> op_8XY1_bitwiseOr();
                    case 0x2 -> op_8XY2_bitwiseAnd();
                    case 0x3 -> op_8XY3_exclusiveOr();
                    case 0x4 -> op_8XY4_sumRegisters();
                    case 0x5 -> op_8XY5_subtractRegister();
                    case 0x6 -> op_8XY6_rightBitShift();
                    case 0x7 -> op_8XY7_subtractRegister();
                    case 0xE -> op_8XYE_leftBitShift();
                    default -> logger.error(UNKNOWN_OPCODE_MESSAGE, () -> opcode.toHexString());
                }
            }
            case 0x9 -> op_9XY0_skipIfRegistersNotEqual();
            case 0xA -> op_ANNN_setIRegister();
            case 0xB -> op_BNNN_jumpToLocation();
            case 0xC -> op_CXKK_setRegisterToRandom();
            case 0xD -> op_DXYN_drawSprite();
            case 0xE -> {
                switch (opcode.kk()) {
                    case 0x9E -> op_EX9E_skipIfKeyPressed();
                    case 0xA1 -> op_EXA1_skipIfKeyNotPressed();
                    default -> logger.error(UNKNOWN_OPCODE_MESSAGE, () -> opcode.toHexString());
                }
            }
            case 0xF -> {
                switch (opcode.kk()) {
                    case 0x07 -> op_FX07_setRegisterToDelayTimer();
                    case 0x0A -> op_FX0A_waitForKeyPress();
                    case 0x15 -> op_FX15_setDelayTimerToRegister();
                    case 0x18 -> op_FX18_setSoundTimerToValue();
                    case 0x1E -> op_FX1E_addToIRegister();
                    case 0x29 -> op_FX29_loadSprite();
                    case 0x33 -> op_FX33_storeBCD();
                    case 0x55 -> op_FX55_storeRegisterInMemory();
                    case 0x65 -> op_FX65_readRegistersFromMemory();
                    default -> logger.error(UNKNOWN_OPCODE_MESSAGE, () -> opcode.toHexString());
                }
            }
            default -> logger.error(UNKNOWN_OPCODE_MESSAGE, () -> opcode.toHexString());
        }
    }

    /**
     * Clear the display
     */
    private void clearScreen_0x00E0() {
        display.clear();
        drawFlag = true;
    }

    /**
     * Return from a subroutine
     */
    private void returnFromSubroutine_0x00EE() {
        pc = stack[sp];
        sp--;
    }

    /**
     * Jump to location nnn
     */
    private void op_1NNN_jumpToLocation() {
        pc = opcode.nnn();
    }

    /**
     * Call subroutine at nnn
     */
    private void op_2NNN_callSubroutine() {
        sp++;
        stack[sp] = pc;
        pc = opcode.nnn();
    }

    /**
     * Skip next instruction if Vx = kk
     */
    private void op_3XKK_skipIfRegisterEqualsValue() {
        if (VRegister[opcode.x()] == opcode.kk()) {
            incrementPC();
        }
    }

    /**
     * Skip next instruction if Vx != kk
     */
    private void op_4XKK_skipIfRegisterNotEqualValue() {
        if (VRegister[opcode.x()] != opcode.kk()) {
            incrementPC();
        }
    }

    /**
     * Skip next instruction if Vx = Vy and n = 0
     */
    private void op_5XY0_skipIfRegistersEqual() {
        if (VRegister[opcode.x()] == VRegister[opcode.y()] && opcode.n() == 0) {
            incrementPC();
        }
    }

    /**
     * Set Vx = kk
     */
    private void op_6XKK_setValueToRegister() {
        VRegister[opcode.x()] = opcode.kk();
    }

    /**
     * Set Vx = Vx + kk
     */
    private void op_7XKK_addValueToRegister() {
        int result = VRegister[opcode.x()] + opcode.kk();
        if (result >= 256) {
            VRegister[opcode.x()] = result & 0x00FF;
        } else {
            VRegister[opcode.x()] = result;
        }
    }

    /**
     * Set Vx = Vy
     */
    private void op_8XY0_setRegistersEqual() {
        VRegister[opcode.x()] = VRegister[opcode.y()];
    }

    /**
     * Set Vx = Vx OR Vy
     */
    private void op_8XY1_bitwiseOr() {
        VRegister[opcode.x()] |= VRegister[opcode.y()];
    }

    private void op_8XY2_bitwiseAnd() {
        VRegister[opcode.x()] &= VRegister[opcode.y()];
    }

    /**
     * Set Vx = XOR Vy
     */
    private void op_8XY3_exclusiveOr() {
        VRegister[opcode.x()] ^= VRegister[opcode.y()];
    }

    /**
     * Set Vx = Vx + Vy, set VF = carry
     */
    private void op_8XY4_sumRegisters() {
        int sum = VRegister[opcode.x()] + VRegister[opcode.y()];
        int sumsLowestByte = sum & 0xFF;
        VRegister[opcode.x()] = sumsLowestByte;
        VRegister[0xF] = sum > 0xFF ? 1 : 0;
    }

    /**
     * Set Vx = Vx - Vy, set VF = NOT borrow
     */
    private void op_8XY5_subtractRegister() {
        if (VRegister[opcode.x()] > VRegister[opcode.y()]) {
            VRegister[0xF] = 1;
        } else {
            VRegister[0xF] = 0;
        }
        VRegister[opcode.x()] = (VRegister[opcode.x()] - VRegister[opcode.y()]) & 0x00FF;
    }

    /**
     * Set Vx = Vx SHR 1
     */
    private void op_8XY6_rightBitShift() {
        int leastSignificantBit = VRegister[opcode.x()] & 1;
        VRegister[0xF] = leastSignificantBit;
        VRegister[opcode.x()] >>>= 1;
    }

    /**
     * Set Vx = Vy - Vx, set VF = NOT borrow
     */
    private void op_8XY7_subtractRegister() {
        if (VRegister[opcode.y()] > VRegister[opcode.x()]) {
            VRegister[0xF] = 1;
        } else {
            VRegister[0xF] = 0;
        }
        VRegister[opcode.x()] = VRegister[opcode.y()] - VRegister[opcode.x()];
    }

    /**
     * Set Vx = Vx SHL 1
     */
    private void op_8XYE_leftBitShift() {
        int mostSignificantBit = VRegister[opcode.x()] >> 7;
        VRegister[0xF] = mostSignificantBit;
        VRegister[opcode.x()] = (VRegister[opcode.x()] << 1) & 0x00FF;
    }

    /**
     * Skip next instruction if Vx != Vy and n = 0
     */
    private void op_9XY0_skipIfRegistersNotEqual() {
        if (VRegister[opcode.x()] != VRegister[opcode.y()] && opcode.n() == 0) {
            incrementPC();
        }
    }

    /**
     * Set I = nnn
     */
    private void op_ANNN_setIRegister() {
        IRegister = opcode.nnn();
    }

    /**
     * Jump to location nnn + V0
     */
    private void op_BNNN_jumpToLocation() {
        pc = VRegister[0] + opcode.nnn();
    }

    /**
     * Set Vx = random byte AND kk
     */
    private void op_CXKK_setRegisterToRandom() {
        int randomUnsignedByte = random.nextInt(266);
        VRegister[opcode.x()] = randomUnsignedByte & opcode.kk();
    }

    /**
     * Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision
     */
    private void op_DXYN_drawSprite() {
        VRegister[0xF] = 0;

        for (int yLine = 0; yLine < opcode.n(); yLine++) {

            int spriteByte = memory.readByte(IRegister + yLine);
            int yCoord = VRegister[opcode.y()] + yLine;
            yCoord = yCoord % Display.HEIGHT_IN_PIXELS;

            for (int xLine = 0; xLine < 8; xLine++) {

                int xCoord = VRegister[opcode.x()] + xLine;
                xCoord = xCoord % Display.LENGTH_IN_PIXELS;
                int previousPixelVal = display.getPixel(xCoord, yCoord);
                int newPixelVal = previousPixelVal ^ (1 & (spriteByte >> 7 - xLine));
                display.setPixel(xCoord, yCoord, newPixelVal);

                if (previousPixelVal == 1 && newPixelVal == 0) {
                    VRegister[0xF] = 1;
                }
            }
        }
        drawFlag = true;
    }

    /**
     * Skip next instruction if key with the value of Vx is pressed
     */
    private void op_EX9E_skipIfKeyPressed() {
        if (keyboard.isKeyDown(VRegister[opcode.x()])) {
            incrementPC();
        }
    }

    /**
     * Skip next instruction if key with the value of Vx is not pressed
     */
    private void op_EXA1_skipIfKeyNotPressed() {
        if (!keyboard.isKeyDown(VRegister[opcode.x()])) {
            incrementPC();
        }
    }

    /**
     * Set Vx = delay timer value
     */
    private void op_FX07_setRegisterToDelayTimer() {
        VRegister[opcode.x()] = delayTimer;
    }

    /**
     * Wait for a key press, store the value of the key in Vx
     */
    private void op_FX0A_waitForKeyPress() {
        for (int i = 0x0; i < 0xF; i++) {
            if (keyboard.isKeyDown(i)) {
                VRegister[opcode.x()] = i;
                keyboard.forceKeyUp(i);
                return;
            }
        }
        // Rather than suspending the thread, the CPU is simply not advanced
        // to the next instruction
        pc -= 2;
    }

    /**
     * Set the delay timer = Vx
     */
    private void op_FX15_setDelayTimerToRegister() {
        delayTimer = VRegister[opcode.x()];
    }

    /**
     * Set sound timer = Vx
     */
    private void op_FX18_setSoundTimerToValue() {
        soundTimer = VRegister[opcode.x()];
    }

    /**
     * Only the lowest 12 bits of the IRegister are used
     */
    private void op_FX1E_addToIRegister() {
        IRegister = (IRegister + VRegister[opcode.x()]) & 0xFFF;
    }

    /**
     * Set I = location of sprite for digit Vx
     */
    private void op_FX29_loadSprite() {
        IRegister = VRegister[opcode.x()] * 5; // each sprite is 5 byte long
        drawFlag = true;
    }

    /**
     * Store BCD representation of Vx in memory locations I, I+1, I+2
     */
    private void op_FX33_storeBCD() {
        int onesDigitOfVx = VRegister[opcode.x()] % 10;
        int tensDigitOfVx = VRegister[opcode.x()] % 100 / 10;
        int hundredsDigitOfVx = VRegister[opcode.x()] % 1000 / 100;
        memory.writeByte(hundredsDigitOfVx, IRegister);
        memory.writeByte(tensDigitOfVx, IRegister + 1);
        memory.writeByte(onesDigitOfVx, IRegister + 2);
    }

    /**
     * Stores registers V0 through Vx in memory starting at location I
     */
    private void op_FX55_storeRegisterInMemory() {
        for (int i = 0; i < opcode.x() + 1; i++) {
            memory.writeByte(VRegister[i], IRegister + i);
        }
    }

    /**
     * Reads registers V0 through Vx from memory starting at location I
     */
    private void op_FX65_readRegistersFromMemory() {
        for (int i = 0; i < opcode.x() + 1; i++) {
            VRegister[i] = memory.readByte(IRegister + i);
        }
    }

    public int getClockSpeed() {
        return clockSpeed;
    }

    public int getDelayTimer() {
        return delayTimer;
    }

    public int getSoundTimer() {
        return soundTimer;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public int[] getVRegister() {
        return VRegister;
    }

    public int getIRegister() {
        return IRegister;
    }

    public int getSp() {
        return sp;
    }

    public int getPc() {
        return pc;
    }

    public int[] getStack() {
        return stack;
    }

    public boolean isDrawFlagSet() {
        return drawFlag;
    }

    public void setDelayTimer(int delayTimer) {
        this.delayTimer = delayTimer;
    }

    public void setSoundTimer(int soundTimer) {
        this.soundTimer = soundTimer;
    }

    public void setOpcode(int instruction) {
        this.opcode = new Opcode(instruction);
    }

    public void setIRegister(int IRegister) {
        this.IRegister = IRegister;
    }

    public void setSp(int sp) {
        this.sp = sp;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }
}
