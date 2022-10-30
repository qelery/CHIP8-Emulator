package com.qelery.chip8;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * A class that displays the graphics of the CHIP-8 program.
 * <p>
 * The original CHIP-8 display resolution was 64x32 pixels and
 * monochrome. Graphics are drawn to the display using sprites
 * which are XOR'd with the corresponding pixels.
 * <p>
 * A value of 1 means the pixel is on. A value of 0 means the
 * pixel is off.
 */
public class Display extends Canvas {

    public static final int LENGTH_IN_PIXELS = 64;
    public static final int HEIGHT_IN_PIXELS = 32;

    private final int[][] pixelArr = new int[LENGTH_IN_PIXELS][HEIGHT_IN_PIXELS];
    private final int scale;
    private final GraphicsContext gc;

    /**
     * Creates a Display object for CHIP-8.
     * <p>
     * The original CHIP-8 display resolution was 64x32 pixels and
     * monochrome. The constructor takes in a scale which will be
     * applied to each CHIP-8 pixel to make them larger and therefore
     * easier to see on a modern screen.
     *
     * @param scale how large to upscale the display
     *              (e.g. if scale is 5, then each CHIP-8 'pixel'
     *              will take up a 5x5 block of pixels on your
     *              device's screen)
     */
    public Display(int scale) {
        super(LENGTH_IN_PIXELS * scale, HEIGHT_IN_PIXELS * scale);
        this.scale = scale;
        this.gc = this.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, LENGTH_IN_PIXELS * scale, HEIGHT_IN_PIXELS * scale);
        clear();
    }

    /**
     * Clears the display.
     */
    public void clear() {
        for (int x = 0; x < LENGTH_IN_PIXELS; x++) {
            Arrays.fill(pixelArr[x], 0);
        }
    }

    /**
     * Re-renders the entire display.
     */
    public void render() {
        for (int x = 0; x < pixelArr.length; x++) {
            for (int y = 0; y < pixelArr[y].length; y++) {
                if (pixelArr[x][y] == 1) {
                    gc.setFill(Color.WHITE);
                } else {
                    gc.setFill(Color.BLACK);
                }
                gc.fillRect(x * scale, y * scale, scale, scale);
            }
        }
    }

    /**
     * Gets a pixel's value from the display.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the value of the pixel, 0 being on and 1 being off
     */
    public int getPixel(int x, int y) {
        return pixelArr[x][y];
    }

    /**
     * Set a pixel's value on the display.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param val 0 turns the pixel on, 1 turns the pixel off
     */
    public void setPixel(int x, int y, int val) {
        pixelArr[x][y] = val;
    }

}
