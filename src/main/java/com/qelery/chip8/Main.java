package com.qelery.chip8;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An emulator of the CHIP-8 virtual machine.
 */
public class Main extends Application {

    private Chip8VM chip8;
    private Stage stage;
    private String loadedROMName;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.initialize();
    }

    @Override
    public void stop() {
        // Ensures safe closure of SoundThread
        chip8.getSound().closeLine();
        System.exit(0);
    }


    private void initialize() {
        this.chip8 = Chip8VM.defaultBuild();

        stage.setTitle("CHIP8 by qelery");
        Group root = new Group();
        root.getChildren().add(chip8.getDisplay());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);

        scene.setOnKeyPressed(e -> chip8.getKeyboard().keyDown(e.getCode()));

        scene.setOnKeyReleased(e -> chip8.getKeyboard().keyUp(e.getCode()));

        long frameDurationNanoSec = Math.round((1.0 / Chip8VM.FRAME_HERTZ) * 1.0e9);
        int cpuCyclesPerFrame = (int) Math.round(chip8.getCpu().getClockSpeed() * frameDurationNanoSec / 1.0e9);

        new AnimationTimer() {
            public void handle(long frameStartTime) {

                for (int i = 0; i < cpuCyclesPerFrame; i++) {
                    chip8.getCpu().emulateCycle();
                }

                if (chip8.getCpu().isDrawFlagSet()) {
                    chip8.getDisplay().render();
                    chip8.getCpu().clearDrawFlag();
                }

                chip8.getCpu().tickClocks();
                pauseUntilFrameOver(frameStartTime, frameDurationNanoSec);
            }
        }.start();

        loadROM();
        printInstructions();
        stage.show();
    }

    private void pauseUntilFrameOver(long frameStartTime, long frameDurationNanoSec) {

        long elapsedTime = (System.nanoTime() - frameStartTime);
        if (elapsedTime < frameDurationNanoSec) {
            long timeToWait = frameDurationNanoSec - elapsedTime;
            long start = System.nanoTime();
            long end;
            do {
                end = System.nanoTime();
            } while (start + timeToWait >= end);
        }
    }

    private void loadROM() {
        Scanner scanner = new Scanner(System.in);

        try (Stream<Path> paths = Files.walk(Paths.get("src/main/resources/ROMS/"))) {
            clearConsole();
            List<String> availableROMs = paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
            for (int i = 0; i < availableROMs.size(); i++) {
                System.out.printf("%-18s", availableROMs.get(i));
                if ((i + 1) % 3 == 0) {
                    System.out.println();
                }
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println("Could not find ROM directory at " +
                    Paths.get("src/main/resources/ROMS/").toAbsolutePath());
            System.exit(1);
        }

        while (true) {
            System.out.println("Game to load from the ROMS directory? ");
            String fileName = scanner.nextLine();
            Path fileLocation = Paths.get("src/main/resources/ROMS/" + fileName.toUpperCase());
            try {
                byte[] byteArray = Files.readAllBytes(fileLocation);
                chip8.getMemory().loadData(byteArray, 0x200);
                loadedROMName = fileName;
                return;
            } catch (IOException e) {
                System.out.println("Could not find path: " + fileLocation.toAbsolutePath() + "\n");
            }
        }
    }

    public void printInstructions() {
        clearConsole();
        chip8.getKeyboard().printControls();

        Path fileLocation = Paths.get("src/main/resources/GameInstructions.txt");
        try (Scanner reader = new Scanner(fileLocation)) {
            while (reader.hasNextLine()) {
                String row = reader.nextLine();
                if (row.isEmpty()) {
                    continue;
                }
                String gameName = row.split(":")[0];
                String gameInstructions = row.split(":")[1].trim();
                if (gameName.equalsIgnoreCase(loadedROMName)) {
                    System.out.println("\n");
                    System.out.println(String.format("<%s> INSTRUCTIONS:", loadedROMName.toUpperCase()));
                    for (String line : gameInstructions.split("<br>")) {
                        System.out.println(line.trim());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("\n");
            System.out.println("Could not find game instruction file at path: " + fileLocation.toAbsolutePath());
        }
    }

    public void clearConsole() {
        System.out.println(new String(new char[50]).replace("\0", System.lineSeparator()));
    }


}
