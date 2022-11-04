package com.qelery.chip8;

import com.qelery.chip8.components.CPU;
import com.qelery.chip8.components.Display;
import com.qelery.chip8.components.Keyboard;
import com.qelery.chip8.components.Memory;
import com.qelery.chip8.components.sound.Sound;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class Chip8VMTest {

    @Test
    @DisplayName("Should print key controls and game instructions")
    void printInstructions() throws NoSuchFieldException, IllegalAccessException {
        Display display = Mockito.mock(Display.class);
        Sound sound = Mockito.mock(Sound.class);
        Keyboard keyboard = Mockito.mock(Keyboard.class);
        Memory memory = Mockito.mock(Memory.class);
        CPU cpu = Mockito.mock(CPU.class);
        Chip8VM chip8 = new Chip8VM(display, sound, keyboard, memory, cpu, "", "");
        ROMLoader romLoader = setMockRomLoader(chip8);

        chip8.printInstructions();

        Mockito.verify(keyboard).printKeyControls();
        Mockito.verify(romLoader).printLoadedRomInstructions();
    }

    @Test
    @DisplayName("Should call ROMLoader to load rom")
    void loadROM() throws NoSuchFieldException, IllegalAccessException {
        Display display = Mockito.mock(Display.class);
        Sound sound = Mockito.mock(Sound.class);
        Keyboard keyboard = Mockito.mock(Keyboard.class);
        Memory memory = Mockito.mock(Memory.class);
        CPU cpu = Mockito.mock(CPU.class);
        Chip8VM chip8 = new Chip8VM(display, sound, keyboard, memory, cpu, "", "");
        ROMLoader romLoader = setMockRomLoader(chip8);

        chip8.loadROM();

        Mockito.verify(romLoader).loadUserSelectedRom();
    }

    @Test
    @DisplayName("Should close the sound line when stopping")
    void stop() {
        Display display = Mockito.mock(Display.class);
        Sound sound = Mockito.mock(Sound.class);
        Keyboard keyboard = Mockito.mock(Keyboard.class);
        Memory memory = Mockito.mock(Memory.class);
        CPU cpu = Mockito.mock(CPU.class);
        Chip8VM chip8 = new Chip8VM(display, sound, keyboard, memory, cpu, "", "");

        chip8.stop();

        Mockito.verify(sound).closeLine();
    }

    private ROMLoader setMockRomLoader(Chip8VM chip8) throws NoSuchFieldException, IllegalAccessException {
        Field field = chip8.getClass().getDeclaredField("romLoader");
        field.setAccessible(true);
        ROMLoader mockRomLoader = Mockito.mock(ROMLoader.class);
        field.set(chip8, mockRomLoader);
        return mockRomLoader;
    }

    @Nested
    @DisplayName("defaultBuild")
    class DefaultBuild {

        @Test
        @DisplayName("Should build a Chip8 machine with a display, sound, keyboard, memory, and cpu components")
        void shouldHaveNonNullComponents() {
            Chip8VM chip8 = Chip8VM.defaultBuild();

            assertNotNull(chip8.getDisplay());
            assertNotNull(chip8.getSound());
            assertNotNull(chip8.getKeyboard());
            assertNotNull(chip8.getMemory());
            assertNotNull(chip8.getCpu());
        }

        @Test
        @DisplayName("Should have a CPU clock speed of 500")
        void shouldHaveClockSpeed500() {
            int expectedClockSpeed = 500;

            Chip8VM chip8 = Chip8VM.defaultBuild();

            assertEquals(expectedClockSpeed, chip8.getCpu().getClockSpeed());
        }


        @Test
        @DisplayName("Should have a screen with primary color white and secondary color black")
        void shouldHaveBlackAndWhiteScreen() {
            Color expectedPrimaryColor = Color.WHITE;
            Color expectedSecondaryColor = Color.BLACK;

            Chip8VM chip8 = Chip8VM.defaultBuild();

            assertEquals(expectedPrimaryColor, chip8.getDisplay().getPrimaryColor());
            assertEquals(expectedSecondaryColor, chip8.getDisplay().getSecondaryColor());
        }

        @Test
        @DisplayName("Should have a memory size of 4096")
        void shouldHaveMemorySize4096() {
            int expectedMemorySize = 4096;

            Chip8VM chip8 = Chip8VM.defaultBuild();

            assertMemorySize(expectedMemorySize, chip8.getMemory());
        }

        private void assertMemorySize(int expectedMemorySize, Memory memory) {
            // Has at least x memory blocks
            assertDoesNotThrow(() -> memory.readByte(0));
            assertDoesNotThrow(() -> memory.readByte(expectedMemorySize - 1));
            // Has no more that x memory blocks
            assertThrows(Exception.class, () -> memory.readByte(expectedMemorySize));
        }
    }
}