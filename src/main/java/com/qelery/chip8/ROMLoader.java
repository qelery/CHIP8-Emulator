package com.qelery.chip8;

import com.qelery.chip8.components.Memory;
import com.qelery.chip8.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;


record ROM(String name, String instructions, Path path) {
}

public class ROMLoader {

    private static final Logger logger = LogManager.getLogger(ROMLoader.class);
    private final String romsDirectoryPath;
    private final String instructionsFilePath;
    private final Memory memory;

    private List<ROM> availableRoms;
    private ROM loadedRom;

    public ROMLoader(Memory memory, String romsDirectoryPath, String instructionsFilePath) {
        this.memory = memory;
        this.romsDirectoryPath = romsDirectoryPath;
        this.instructionsFilePath = instructionsFilePath;
        this.availableRoms = new ArrayList<>();
    }

    public void loadUserSelectedRom() {
        findRoms();
        if (this.availableRoms.isEmpty()) {
            logger.error("No ROMs were found in directory {}", Paths.get(romsDirectoryPath).toAbsolutePath());
            System.exit(2);
        }
        printAvailableRoms();
        ROM selectedRom = promptUserForRomSelection();
        writeRomToMemory(selectedRom);
    }

    private void findRoms() {
        Path romsPath = Paths.get(romsDirectoryPath);
        try (Stream<Path> paths = Files.walk(romsPath)) {
            this.availableRoms = paths.filter(Files::isRegularFile)
                    .sorted()
                    .map(path -> {
                        String romName = path.getFileName().toString();
                        return new ROM(romName, findInstructions(romName), path);
                    })
                    .toList();
        } catch (IOException e) {
            logger.error("Could not find ROM directory at {}", romsPath.toAbsolutePath());
            System.exit(1);
        }
    }

    private String findInstructions(String romName) {
        Path fileLocation = Paths.get(instructionsFilePath);
        String instructions = null;
        try (Scanner reader = new Scanner(fileLocation)) {
            while (reader.hasNextLine() && instructions == null) {
                String row = reader.nextLine();
                if (!row.contains(":")) {
                    continue;
                }
                String gameName = row.split(":")[0];
                if (gameName.equalsIgnoreCase(romName)) {
                    instructions = row.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            logger.error("Could not find game instructions file at path: {}", fileLocation.toAbsolutePath());
        }
        if (instructions == null) {
            logger.info("No instructions were found for {}", romName);
        }
        return instructions;
    }

    private void printAvailableRoms() {
        IOUtils.clearConsole();
        List<String> romNames = availableRoms.stream().map(ROM::name).toList();
        int filenamesPerPrintedRow = 3;
        int filenamePrintedWidth = 18;
        String format = "%-" + filenamePrintedWidth + "s";
        for (int i = 0; i < romNames.size(); i++) {
            String filename = romNames.get(i);
            System.out.printf(format, filename);
            if ((i + 1) % filenamesPerPrintedRow == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }

    private ROM promptUserForRomSelection() {
        System.out.println("Game to load from the ROMS directory? ");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine().trim();
            Optional<ROM> matchingRom = this.availableRoms.stream()
                    .filter(rom -> input.equalsIgnoreCase(rom.name()))
                    .findFirst();
            if (matchingRom.isPresent()) {
                scanner.close();
                return matchingRom.get();
            } else {
                System.out.println("Could not find that ROM. Please try again.");
            }
        }
    }

    public void writeRomToMemory(ROM rom) {
        try {
            byte[] byteArray = Files.readAllBytes(rom.path());
            memory.loadData(byteArray, Memory.READ_WRITE_START_LOCATION);
            this.loadedRom = rom;
        } catch (IOException e) {
            logger.error("Could not load ROM at path: {}", rom.path().toAbsolutePath());
            System.exit(3);
        }
    }

    public void printLoadedRomInstructions() {
        if (loadedRom == null) {
            logger.info("Cannot print ROM instructions. No ROM loaded.");
        } else if (loadedRom.instructions() == null || loadedRom.instructions().isEmpty()) {
            logger.info("No instructions were found for {}", loadedRom::name);
        } else {
            System.out.printf("%n<%s> INSTRUCTIONS:%n", loadedRom.name());
            for (String line : loadedRom.instructions().split("<br>")) {
                System.out.println(line.trim());
            }
        }
    }

    public List<ROM> getAvailableRoms() {
        return availableRoms;
    }

    public void setAvailableRoms(List<ROM> availableRoms) {
        this.availableRoms = availableRoms;
    }

    public ROM getLoadedRom() {
        return loadedRom;
    }
}
