package com.qelery.chip8;

import com.qelery.chip8.components.CPU;
import com.qelery.chip8.components.Display;
import com.qelery.chip8.components.Keyboard;
import com.qelery.chip8.components.Memory;
import com.qelery.chip8.components.sound.Sound;
import com.qelery.chip8.components.sound.wave.SineWave;
import com.qelery.chip8.util.IOUtils;
import javafx.scene.paint.Color;

/**
 * An emulated CHIP-8 virtual machine.
 */
public class Chip8VM {

    public static final int INTERNAL_TIMERS_HERTZ = CPU.TIMERS_HERTZ;
    private static final String ROMS_DIRECTORY_PATH = "src/main/resources/ROMS/";
    private static final String INSTRUCTIONS_FILE_PATH = "src/main/resources/ROMInstructions.txt";

    private final Display display;
    private final Sound sound;
    private final Keyboard keyboard;
    private final Memory memory;
    private final CPU cpu;
    private final ROMLoader romLoader;


    public Chip8VM(Display display, Sound sound, Keyboard keyboard, Memory memory, CPU cpu,
                   String romsDirectoryPath, String instructionsFilePath) {
        this.display = display;
        this.sound = sound;
        this.keyboard = keyboard;
        this.memory = memory;
        this.cpu = cpu;
        this.romLoader = new ROMLoader(memory, romsDirectoryPath, instructionsFilePath);
    }

    public static Chip8VM defaultBuild() {
        final int displayScale = 12;
        final int soundFrequency = 300;
        final int clockSpeed = 500;
        Display display = new Display(displayScale, Color.WHITE, Color.BLACK);
        Sound sound = new SineWave(soundFrequency);
        Keyboard keyboard = new Keyboard();
        Memory memory = new Memory(Memory.DEFAULT_SIZE);
        CPU cpu = new CPU(clockSpeed, memory, display, sound, keyboard);
        return new Chip8VM(display, sound, keyboard, memory, cpu, ROMS_DIRECTORY_PATH, INSTRUCTIONS_FILE_PATH);
    }

    public void loadROM() {
        romLoader.loadUserSelectedRom();
    }

    public void printInstructions() {
        IOUtils.clearConsole();
        keyboard.printKeyControls();
        romLoader.printLoadedRomInstructions();
    }

    public void stop() {
        sound.closeLine();
    }

    public Display getDisplay() {
        return display;
    }

    public Sound getSound() {
        return sound;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public Memory getMemory() {
        return memory;
    }

    public CPU getCpu() {
        return cpu;
    }
}
