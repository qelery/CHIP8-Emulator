package com.qelery.chip8;

import com.qelery.chip8.components.Memory;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ROMLoaderTest {

    private static final String ROMS_DIRECTORY_PATH = "src/test/resources/ROMS/";
    private static final String INSTRUCTIONS_FILE_PATH = "src/test/resources/TestROMInstructions.txt";

    private ByteArrayOutputStream mockSystemOut;
    private ByteArrayInputStream mockSystemIn;
    private final PrintStream originalSystemOut = System.out;
    private final InputStream originalSystemIn = System.in;

    ROMLoader romLoader;
    Memory memory;

    ROM rom1, rom2, rom3, rom4, rom5, rom6;

    @BeforeEach
    void setUp() {
        this.memory = new Memory(Memory.DEFAULT_SIZE);
        this.romLoader = new ROMLoader(memory, ROMS_DIRECTORY_PATH, INSTRUCTIONS_FILE_PATH);

        this.rom1 = new ROM("TESTROM1", "These are game instructions for TESTROM1.", Paths.get("src/test/resources/ROMS/TESTROM1"));
        this.rom2 = new ROM("TESTROM2", "These are game instructions for TESTROM2.", Paths.get("src/test/resources/ROMS/TESTROM2"));
        this.rom3 = new ROM("TESTROM3", "These are game instructions for TESTROM3.", Paths.get("src/test/resources/ROMS/TESTROM3"));
        this.rom4 = new ROM("TESTROM4", "These are game instructions for TESTROM4.", Paths.get("src/test/resources/ROMS/TESTROM4"));
        this.rom5 = new ROM("TESTROM5", "These are game instructions for TESTROM5.", Paths.get("src/test/resources/ROMS/TESTROM5"));
        this.rom6 = new ROM("TESTROM6", "These are game instructions for TESTROM6.", Paths.get("src/test/resources/ROMS/TESTROM6"));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
        System.setIn(originalSystemIn);
    }

    @Nested
    class LoadUserSelectedRom {

        @Test
        @DisplayName("Should find ROMs")
        void shouldFindRoms() {
            String userInput = "TESTROM4\n";
            mockSystemIn = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
            System.setIn(mockSystemIn);

            romLoader.loadUserSelectedRom();

            List<ROM> expectedRoms = List.of(rom1, rom2, rom3, rom4, rom5, rom6);
            assertEquals(expectedRoms, romLoader.getAvailableRoms());
        }

        @Test
        @DisplayName("Should assign the user selected ROM to the loadedRom field")
        void shouldAssignUserSelectedRomAsLoadedRom() {
            String userInput = "TESTROM4\n";
            mockSystemIn = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
            System.setIn(mockSystemIn);

            romLoader.loadUserSelectedRom();

            assertEquals(rom4, romLoader.getLoadedRom());
        }

        @Test
        @DisplayName("Should write selected ROM to memory")
        void shouldWriteUserSelectedRomToMemory() {
            String userInput = "TESTROM4\n";
            mockSystemIn = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
            System.setIn(mockSystemIn);

            romLoader.loadUserSelectedRom();

            int bytesToRead = 50; // arbitrary number large enough to read all bytes from the ROM
            String rom4BytesAsString = "THIS IS TEST DATA INSIDE TESTROM4";
            assertBytesWrittenToMemoryAsString(bytesToRead, rom4BytesAsString);
        }
    }


    @Test
    @DisplayName("Should write rom to memory and set loadedRom field")
    void writeRomToMemory() {
        romLoader.writeRomToMemory(rom1);

        String rom1BytesAsString = "THIS IS TEST DATA INSIDE TESTROM1";
        int bytesToRead = 50; // arbitrary number large enough to read all bytes from the ROM
        assertBytesWrittenToMemoryAsString(bytesToRead, rom1BytesAsString);
    }

    @Test
    @DisplayName("Should print loaded rom instructions to system out")
    void printLoadedRomInstructions() {
        mockSystemOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(mockSystemOut));
        String expectedSystemOutText = """
                <TESTROM1> INSTRUCTIONS:
                These are game instructions for TESTROM1.""";

        romLoader.writeRomToMemory(rom1);

        romLoader.printLoadedRomInstructions();

        assertEquals(expectedSystemOutText, mockSystemOut.toString().trim());
    }

    private void assertBytesWrittenToMemoryAsString(int bytesToRead, String expectedStringWrittenToMemory) {
        byte[] bytesArray = new byte[bytesToRead];
        for (int i = Memory.READ_WRITE_START_LOCATION; i < Memory.READ_WRITE_START_LOCATION + bytesToRead; i++) {
            bytesArray[i - Memory.READ_WRITE_START_LOCATION] = (byte) memory.readByte(i);
        }
        String actualRom1BytesAsString = new String(bytesArray, StandardCharsets.UTF_8);

        assertTrue(actualRom1BytesAsString.contains(expectedStringWrittenToMemory),
                "The bytes written to memory did not decode to the expected string.");
    }
}