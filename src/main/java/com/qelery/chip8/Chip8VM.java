package com.qelery.chip8;

import com.qelery.chip8.components.CPU;
import com.qelery.chip8.components.Display;
import com.qelery.chip8.components.Keyboard;
import com.qelery.chip8.components.Memory;
import com.qelery.chip8.components.sound.Sound;
import com.qelery.chip8.components.sound.wave.SineWave;
import javafx.scene.paint.Color;

public class Chip8VM {

    public static final int FRAME_HERTZ = 60; // CHIP-8 delay and sound timers always count down at 60 hz

    private final Display display;
    private final Sound sound;
    private final Keyboard keyboard;
    private final Memory memory;
    private final CPU cpu;

    public Chip8VM(Display display, Sound sound, Keyboard keyboard, Memory memory, CPU cpu) {
        this.display = display;
        this.sound = sound;
        this.keyboard = keyboard;
        this.memory = memory;
        this.cpu = cpu;
    }

    public static Chip8VM defaultBuild() {
        final int displayScale = 12;
        final int soundFrequency = 300;
        final int memorySize = 4096;
        final int clockSpeed = 500;
        Display display = new Display(displayScale, Color.WHITE, Color.BLACK);
        Sound sound = new SineWave(soundFrequency);
        Keyboard keyboard = new Keyboard();
        Memory memory = new Memory(memorySize);
        CPU cpu = new CPU(clockSpeed, memory, display, sound, keyboard);
        return new Chip8VM(display, sound, keyboard, memory, cpu);
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
