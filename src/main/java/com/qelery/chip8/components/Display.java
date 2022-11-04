package com.qelery.chip8.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * Displays the graphics of the CHIP-8 program.
 * <p>
 * The original CHIP-8 display resolution was 64x32 pixels and
 * monochrome. Graphics are drawn to the display using sprites
 * which are XOR'd with the corresponding pixels.
 * <p>
 * Pixels can only have one of two states. A value of 1 means
 * the pixel is ON and will display the primary color. A value
 * of 0 means the pixel is OFF and will display the secondary
 * color.
 */
public class Display extends Canvas {

    public static final int LENGTH_IN_PIXELS = 64;
    public static final int HEIGHT_IN_PIXELS = 32;
    public static final int PIXEL_ON_VALUE = 1;
    public static final int PIXEL_OFF_VALUE = 0;
    public static final Color DEFAULT_PRIMARY_COLOR = Color.WHITE;
    public static final Color DEFAULT_SECONDARY_COLOR = Color.BLACK;

    private final int scale;
    private Color primaryColor;
    private Color secondaryColor;
    private final GraphicsContext gc;
    private final int[][] pixelArr = new int[LENGTH_IN_PIXELS][HEIGHT_IN_PIXELS];

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
        this.primaryColor = DEFAULT_PRIMARY_COLOR;
        this.secondaryColor = DEFAULT_SECONDARY_COLOR;
        this.gc = this.getGraphicsContext2D();
        initDisplay();
    }

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
     * @param color the color of ON pixels
     */
    public Display(int scale, Color color) {
        super(LENGTH_IN_PIXELS * scale, HEIGHT_IN_PIXELS * scale);
        this.scale = scale;
        this.primaryColor = color;
        this.secondaryColor = DEFAULT_SECONDARY_COLOR;
        if (primaryColor.equals(secondaryColor)) {
            this.secondaryColor = Color.WHITE;
        }
        this.gc = this.getGraphicsContext2D();
        initDisplay();
    }

    /**
     * Creates a Display object for CHIP-8.
     * <p>
     * The original CHIP-8 display resolution was 64x32 pixels and
     * monochrome. The constructor takes in a scale which will be
     * applied to each CHIP-8 pixel to make them larger and therefore
     * easier to see on a modern screen.
     *
     * @param scale          how large to upscale the display
     *                       (e.g. if scale is 5, then each CHIP-8 'pixel'
     *                       will take up a 5x5 block of pixels on your
     *                       device's screen)
     * @param primaryColor   the color of ON pixels
     * @param secondaryColor the color of OFF pixels
     */
    public Display(int scale, Color primaryColor, Color secondaryColor) {
        super(LENGTH_IN_PIXELS * scale, HEIGHT_IN_PIXELS * scale);
        this.scale = scale;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.gc = this.getGraphicsContext2D();
        initDisplay();
    }

    private void initDisplay() {
        gc.setFill(this.secondaryColor);
        gc.fillRect(0, 0, LENGTH_IN_PIXELS * scale, HEIGHT_IN_PIXELS * scale);
        clear();
    }

    /**
     * Clears the display.
     */
    public void clear() {
        for (int x = 0; x < LENGTH_IN_PIXELS; x++) {
            Arrays.fill(pixelArr[x], PIXEL_OFF_VALUE);
        }
    }

    /**
     * Re-renders the entire display.
     */
    public void render() {
        for (int x = 0; x < pixelArr.length; x++) {
            for (int y = 0; y < pixelArr[y].length; y++) {
                if (pixelArr[x][y] == PIXEL_ON_VALUE) {
                    gc.setFill(primaryColor);
                } else {
                    gc.setFill(secondaryColor);
                }
                gc.fillRect(x * scale, y * scale, scale, scale);
            }
        }
    }

    /**
     * Gets a pixel's value from the display.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the value of the pixel, 0 being ON and 1 being OFF
     */
    public int getPixel(int x, int y) {
        return pixelArr[x][y];
    }

    /**
     * Set a pixel's value on the display.
     *
     * @param x   the x coordinate
     * @param y   the y coordinate
     * @param val 0 turns the pixel on, 1 turns the pixel off
     * @throws IllegalArgumentException if {@code val} does not
     *                                  equal 0 or 1
     */
    public void setPixel(int x, int y, int val) {
        if (val != Display.PIXEL_ON_VALUE && val != Display.PIXEL_OFF_VALUE) {
            throw new IllegalArgumentException("Can only assign pixel the 0 (OFF) or 1 (ON). Value: " + val);
        }
        pixelArr[x][y] = val;
    }

    /**
     * @return the ON color of a pixel
     */
    public Color getPrimaryColor() {
        return primaryColor;
    }

    /**
     * @return the OFF color of a pixel
     */
    public Color getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * @param primaryColor the ON color of a pixel
     */
    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * @param secondaryColor the OFF color of a pixel
     */
    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }
}
