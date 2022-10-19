package com.qelery.chip8;

import java.util.Random;

/**
 * A class to create an emulated cpu for fetching, decoding, and executing
 * instructions from a CHIP-8 ROM.<p>
 *
 * <h2>References</h2>
 * <a href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM">Cowgod's Chip-8 Technical Reference</a><br>
 * <a href="http://www.cs.columbia.edu/~sedwards/classes/2016/4840-spring/designs/Chip8.pdf">Columbia University - Chip-8 Design Specification</a><br>
 * <a href="https://en.wikipedia.org/wiki/CHIP-8">Wikipedia - CHIP-8</a><br>
 */
public class CPU {

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
     * The next instruction that the CPU will execute.
     */
    private int instruction;

    /**
     * Represents 16, 8-bit unsigned registers.<p>
     * <p>
     * Uses an integer type for simplicity since Java does not have
     * unsigned number types.
     */
    private final int[] VRegister;

    /**
     * Represents an array of 16, 16-bit unsigned values used to store
     * the address that the interpreter should return to when finished
     * with a subroutine.<p>
     * <p>
     * Uses an integer type for simplicity since Java does not have
     * unsigned number types.
     */
    private final int[] stack;

    /**
     * Represents a 16-bit register. This register is generally used to
     * store memory addresses. Only the lowest 12 big are usually used.<p>
     * <p>
     * Uses an integer type for simplicity since Java does not have
     * unsigned number types.
     */
    private int IRegister;

    /**
     * Represents an 8-bit point used to point to the top of the stack.<p>
     * <p>
     * Uses an integer type for simplicity since Java does not have
     * unsigned number types.
     */
    private int sp;

    /**
     * Represents an 8-bit point used to store the currently executing
     * address.<p>
     * <p>
     * Uses an integer type for simplicity since Java does not have
     * unsigned number types.
     */
    private int pc;

    /**
     * A flag which is used to determine if CHIP-8 should render the
     * screen.
     */
    private boolean drawFlag;

    private final Memory memory;
    private final Screen screen;
    private final Sound sound;
    private final Keyboard keyboard;
    private final Random random;

    public CPU(int clockSpeed, Memory memory, Screen screen, Sound sound, Keyboard keyboard) {
        this.clockSpeed = clockSpeed;
        this.pc = Memory.READ_START_LOCATION;
        this.VRegister = new int[16];
        this.stack = new int[16];
        this.drawFlag = false;
        this.memory = memory;
        this.screen = screen;
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
        decodeAndExecuteInstruction();
    }


    protected void fetchInstruction() {
        int firstByte = memory.readByte(pc);
        int secondByte = memory.readByte(pc + 1);
        instruction = ((firstByte << 8) & 0xFF00) | (secondByte & 0x00FF); // ensure unsigned
    }

    protected void incrementPC() {
        pc += 2;
    }

    /**
     * Decodes and executes a 2-byte instruction. Notation for the decoded CPU instructions:<p>
     *
     * <h2>CPU instruction notation</h2>
     * Adapted from: <a href="http://devernay.free.fr/hacks/chip8/C8TECH10.HTM">Cowgod's Chip-8 Technical Reference</a><p>
     * <p>
     * ■ ■ ■ ■ - 4-nibble hexadecimal representation of the 2-byte instruction<p>
     * opcode - represents the highest nibble of the instruction (o ■ ■ ■)<br>
     * nnn - represents the lowest 3 nibbles of the instruction (■ n n n)<br>
     * n - represents the lowest nibble of the instruction (■ ■ ■ n)<br>
     * x - represents the lower nibble of the higher byte of the instruction (■ x ■ ■)<br>
     * y - represents the higher nibble of the lower byte of the instruction (■ ■ y ■)<br>
     * kk - represents the lowest 2 nibbles of the instruction (■ ■ k k)<br>
     * <p>
     */
    protected void decodeAndExecuteInstruction() {
        int opcode = instruction >> 12 & 0x00F;
        int x = instruction >> 8 & 0x00F;
        int y = instruction >> 4 & 0x00F;
        int n = instruction & 0x00F;
        int nnn = instruction & 0xFFF;
        int kk = instruction & 0xFF;

        if (instruction == 0x00E0) {
            // 00E0
            // Clear the display
            screen.clear();
            drawFlag = true;
            return;
        }

        if (instruction == 0x00EE) {
            // 00EE
            // Return from a subroutine
            pc = stack[sp];
            sp--;
            return;
        }
        switch (opcode) {

            case 0x1:
                // 1nnn
                // Jump to location nnn
                pc = nnn;
                return;

            case 0x2:
                // 2nnn
                // call subroutine at address
                sp++;
                stack[sp] = pc;
                pc = nnn;
                return;

            case 0x3:
                // 3xkk
                // Skip next instruction if Vx = kk
                if (VRegister[x] == kk) {
                    incrementPC();
                }
                return;

            case 0x4:
                // 4xkk
                // Skip next instruction if Vx != kk
                if (VRegister[x] != kk) {
                    incrementPC();
                }
                return;

            case 0x5:
                // 5xy0
                // Skip next instruction if Vx = Vy and n = 0
                if (n == 0) {
                    if (VRegister[x] == VRegister[y]) {
                        incrementPC();
                    }
                    return;
                }

            case 0x6:
                // 6xkk
                // Set Vx = kk
                VRegister[x] = kk;
                return;

            case 0x7:
                // 7xkk
                // Set Vx = Vx + kk
                int result = VRegister[x] + kk;
                // resolve overflow - original V registers where 8-bit unsigned
                if (result >= 256) {
                    VRegister[x] = result & 0x00FF;
                } else {
                    VRegister[x] = result;
                }
                return;


            case 0x8:

                switch (n) {

                    case 0:
                        // 8xy0
                        // Set Vx = Vy
                        VRegister[x] = VRegister[y];
                        return;

                    case 1:
                        // 8xy1
                        // Set Vx = Vx OR Vy
                        VRegister[x] |= VRegister[y];
                        return;

                    case 2:
                        // 8xy2
                        // Set Vx = Vx AND Vy
                        VRegister[x] &= VRegister[y];
                        return;

                    case 3:
                        // 8xy3
                        // Set Vx = XOR Vy
                        VRegister[x] ^= VRegister[y];
                        return;

                    case 4:
                        // 8xy4
                        // Set Vx = Vx + Vy, set VF = carry
                        int sum = VRegister[x] + VRegister[y];
                        int sumsLowestByte = sum & 0xFF;
                        VRegister[x] = sumsLowestByte;
                        VRegister[0xF] = sum > 0xFF ? 1 : 0;
                        return;


                    case 5:
                        // 8xy5
                        // set Vx = Vx - Vy, set VF = NOT borrow
                        if (VRegister[x] > VRegister[y]) {
                            VRegister[0xF] = 1;
                        } else {
                            VRegister[0xF] = 0;
                        }
                        VRegister[x] = (VRegister[x] - VRegister[y]) & 0x00FF;
                        return;

                    case 6:
                        // 8xy6
                        // Set Vx = Vx SHR 1
                        int leastSignificantBit = VRegister[x] & 1;
                        VRegister[0xF] = leastSignificantBit;
                        VRegister[x] >>>= 1;
                        return;

                    case 7:
                        // 8xy7
                        // Set Vx = Vy - Vx, set VF = NOT borrow
                        if (VRegister[y] > VRegister[x]) {
                            VRegister[0xF] = 1;
                        } else {
                            VRegister[0xF] = 0;
                        }
                        VRegister[x] = VRegister[y] - VRegister[x];
                        return;

                    case 0xE:
                        // 8xyE
                        // Set Vx = Vx SHL 1
                        int mostSignificantBit = VRegister[x] >> 7;
                        VRegister[0xF] = mostSignificantBit;
                        VRegister[x] = (VRegister[x] << 1) & 0x00FF;
                        return;
                }


            case 0x9:
                // 9xy0
                // Skip next instruction if Vx != Vy and n = 0
                if (n == 0) {
                    if (VRegister[x] != VRegister[y]) {
                        incrementPC();
                    }
                    return;
                }

            case 0xA:
                // Annn
                // Set I = nnn
                IRegister = nnn;
                return;

            case 0xB:
                // Bnnn
                // Jump to location nnn + V0
                pc = VRegister[0] + nnn;
                return;
            case 0xC:
                // Cxkk
                // Set Vx = random byte AND kk
                int randomUnsignedByte = random.nextInt(266);
                VRegister[x] = randomUnsignedByte & kk;
                return;

            case 0xD:
                // Dxyn
                // Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision
                VRegister[0xF] = 0;

                for (int yLine = 0; yLine < n; yLine++) {

                    int spriteByte = memory.readByte(IRegister + yLine);
                    int yCoord = VRegister[y] + yLine;
                    yCoord = yCoord % Screen.NUM_PIXEL_ROWS;

                    for (int xLine = 0; xLine < 8; xLine++) {

                        int xCoord = VRegister[x] + xLine;
                        xCoord = xCoord % Screen.NUM_PIXEL_COLUMNS;
                        int previousPixelVal = screen.getPixel(xCoord, yCoord);
                        int newPixelVal = previousPixelVal ^ (1 & (spriteByte >> 7 - xLine));
                        screen.setPixel(xCoord, yCoord, newPixelVal);

                        if (previousPixelVal == 1 && newPixelVal == 0) {
                            VRegister[0xF] = 1;
                        }
                    }
                }
                drawFlag = true;
                return;

            case 0xE:

                switch (kk) {

                    case 0x9E:
                        //Ex9E
                        // Skip next instruction if key with the value of Vx is pressed
                        if (keyboard.isKeyPressed(VRegister[x])) {
                            incrementPC();
                        }
                        return;

                    case 0xA1:
                        // ExA1
                        // Skip next instruction if key with the value of Vx is not pressed
                        if (!keyboard.isKeyPressed(VRegister[x])) {
                            incrementPC();
                        }
                        return;
                }

            case 0xF:

                switch (kk) {

                    case 0x07:
                        // Fx07
                        // Set Vx = delay timer value
                        VRegister[x] = delayTimer;
                        return;


                    case 0x0A:
                        // Fx0A
                        // Wait for a key press, store the value of the key in Vx
//                        int pressedKey = keyboard.getPressedKey();
//                        while (pressedKey == 0) {
//                            try {
//                                Thread.sleep(300);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            pressedKey = keyboard.getPressedKey();
//                        }
//                        keyboard.forceKeyRelease(pressedKey);
//                        VRegister[x] = pressedKey;
                        for (int i = 0x0; i < 0xF; i++) {
                            if (keyboard.isKeyPressed(i)) {
                                VRegister[x] = i;
                                keyboard.forceKeyRelease(i);
                                return;
                            }
                        }
                        pc -= 2;
                        return;


                    case 0x15:
                        // Fx15
                        // Set the delay timer = Vx
                        delayTimer = VRegister[x];
                        return;

                    case 0x18:
                        // Fx18
                        // Set sound timer = Vx
                        soundTimer = VRegister[x];
                        return;

                    case 0x1E:
                        // Fx1E
                        // Set I = I + Vx
                        // Only the lowest 12 bits of the IRegister are used
                        IRegister = (IRegister + VRegister[x]) & 0xFFF;
                        return;

                    case 0x29:
                        // Fx29
                        // Set I = location of sprite for digit Vx
                        IRegister = VRegister[x] * 5; // each sprite is 5 byte long
                        drawFlag = true;
                        return;

                    case 0x33:
                        // Fx33
                        // Store BCD representation of Vx in memory locations I, I+1, I+2
                        int onesDigitOfVx = VRegister[x] % 10;
                        int tensDigitOfVx = VRegister[x] % 100 / 10;
                        int hundredsDigitOfVx = VRegister[x] % 1000 / 100;
                        memory.writeByte(IRegister, hundredsDigitOfVx);
                        memory.writeByte(IRegister + 1, tensDigitOfVx);
                        memory.writeByte(IRegister + 2, onesDigitOfVx);
                        return;

                    case 0x55:
                        // Fx55
                        // Stores registers V0 through Vx in memory starting at location I
                        for (int i = 0; i < x + 1; i++) {
                            memory.writeByte(IRegister + i, VRegister[i]);
                        }
                        return;

                    case 0x65:
                        // Fx65
                        // Reads registers V0 through Vx from memory starting at location I
                        for (int i = 0; i < x + 1; i++) {
                            VRegister[i] = memory.readByte(IRegister + i);
                        }
                        return;
                }

            default:
                System.out.println(String.format("Unknown opcode: 0x%x\n", instruction));
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

    public int getInstruction() {
        return instruction;
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

    public void setInstruction(int instruction) {
        this.instruction = instruction;
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
