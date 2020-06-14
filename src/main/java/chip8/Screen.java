package chip8;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Screen extends Canvas {

    public static final int NUM_PIXEL_COLUMNS = 64;
    public static final int NUM_PIXEL_ROWS = 32;
    public static final int[][] pixelArr = new int[NUM_PIXEL_COLUMNS][NUM_PIXEL_ROWS];
    private int scale;
    private GraphicsContext gc;

    /**
     * Creates a Screen object that displays the graphical information of
     * the CHIP-8 program.
     *
     * The original CHIP-8 display resolution was 64x32 pixels, monochrome.
     * The constructor takes in a scale which will be applied to each CHIP-8
     * pixel to make them larger and therefore easier to see on a modern display.
     *
     * @param scale
     *              how many computer screen pixels wide and tall each CHIP-8 pixel will be
     */
    public Screen(int scale) {
        super(NUM_PIXEL_COLUMNS * scale, NUM_PIXEL_ROWS * scale);
        this.scale = scale;
        this.gc = this.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, NUM_PIXEL_COLUMNS * scale, NUM_PIXEL_ROWS * scale);
        clear();
    }

    public void clear() {
        for (int x = 0; x < NUM_PIXEL_COLUMNS; x++) {
            for (int y = 0; y < NUM_PIXEL_ROWS; y++) {
                pixelArr[x][y] = 0;
            }
        }
    }

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

    public int getPixel(int x, int y) {
        return pixelArr[x][y];
    }

    public void setPixel(int x, int y, int val) {
        pixelArr[x][y] = val;
    }

}
