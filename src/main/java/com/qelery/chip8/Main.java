package com.qelery.chip8;

import com.qelery.chip8.components.CPU;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Chip8VM chip8;
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.chip8 = Chip8VM.defaultBuild();
        initializeStage();
        chip8.loadROM();
        chip8.printInstructions();
    }

    @Override
    public void stop() {
        chip8.stop();
        stage.close();
        System.exit(0);
    }


    private void initializeStage() {
        stage.setTitle("CHIP8 by qelery");
        Group root = new Group();
        root.getChildren().add(chip8.getDisplay());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);

        scene.setOnKeyPressed(e -> chip8.getKeyboard().keyDown(e.getCode()));
        scene.setOnKeyReleased(e -> chip8.getKeyboard().keyUp(e.getCode()));

        AnimationTimer animationTimer = configureAnimationTimer(chip8);
        animationTimer.start();

        stage.show();
    }

    private AnimationTimer configureAnimationTimer(Chip8VM chip8) {
        long frameDurationNanoSec = Math.round((1.0 / Chip8VM.INTERNAL_TIMERS_HERTZ) * 1.0e9);
        int cpuCyclesPerFrame = (int) Math.round(chip8.getCpu().getClockSpeed() * frameDurationNanoSec / 1.0e9);
        return new AnimationTimer() {
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
        };
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
}
