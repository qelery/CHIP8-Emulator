package com.qelery.chip8.components;

import com.qelery.chip8.components.Display;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DisplayTest {


    @Test
    @DisplayName("Should create a Display with default primary and secondary color")
    void constructorSetsDefaultColors() {
        int scale = 10;
        Display display = new Display(scale);

        Color actualPrimaryColor = display.getPrimaryColor();
        Color actualSecondaryColor = display.getSecondaryColor();

        assertEquals(Display.DEFAULT_PRIMARY_COLOR, actualPrimaryColor);
        assertEquals(Display.DEFAULT_SECONDARY_COLOR, actualSecondaryColor);
    }

    @Test
    @DisplayName("Should create a Display with black as the secondary color by default if only a primary color is supplied")
    void constructorSetsDefaultSecondaryColors() {
        int scale = 10;
        Color primaryColor = Color.LIMEGREEN;
        Display display = new Display(scale, primaryColor);

        Color actualPrimaryColor = display.getPrimaryColor();
        Color actualSecondaryColor = display.getSecondaryColor();


        assertEquals(primaryColor, actualPrimaryColor);
        assertEquals(Display.DEFAULT_SECONDARY_COLOR, actualSecondaryColor);
    }

    @Test
    @DisplayName("Should create a Display with passed in primary and secondary colors")
    void constructorWithPassedInColors() {
        int scale = 10;
        Color primaryColor = Color.LIGHTCYAN;
        Color secondaryColor = Color.SEAGREEN;
        Display display = new Display(scale, primaryColor, secondaryColor);

        Color actualPrimaryColor = display.getPrimaryColor();
        Color actualSecondaryColor = display.getSecondaryColor();


        assertEquals(primaryColor, actualPrimaryColor);
        assertEquals(secondaryColor, actualSecondaryColor);
    }

    @Test
    @DisplayName("Should clear the display")
    void clear() {
        int scale = 10;
        Display display = new Display(scale);
        for (int x = 0; x < Display.LENGTH_IN_PIXELS; x++) {
            for (int y = 0; y < Display.HEIGHT_IN_PIXELS; y++) {
                display.setPixel(x, y, Display.PIXEL_ON_VALUE);
            }
        }

        display.clear();

        for (int x = 0; x < Display.LENGTH_IN_PIXELS; x++) {
            for (int y = 0; y < Display.HEIGHT_IN_PIXELS; y++) {
                assertEquals(Display.PIXEL_OFF_VALUE, display.getPixel(x, y));
            }
        }
    }

    @Test
    @DisplayName("Should get a pixel at the x and y coordinates on the display")
    void getPixel() {
        int scale = 10;
        Display display = new Display(scale);
        int x = 5;
        int y = 5;
        display.setPixel(x, y, Display.PIXEL_ON_VALUE);

        int pixelValue = display.getPixel(x, y);

        assertEquals(Display.PIXEL_ON_VALUE, pixelValue);
    }

    @Test
    @DisplayName("Should set a pixel at the x and y coordinates on the display")
    void setPixel() {
        int scale = 10;
        Display display = new Display(scale);

        int x = 5;
        int y = 5;
        display.setPixel(x, y, Display.PIXEL_ON_VALUE);

        assertEquals(Display.PIXEL_ON_VALUE, display.getPixel(x, y));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when trying to set pixel to value other than 0 (OFF) or 1 (ON)")
    void setPixelThrowsIllegalArgumentException() {
        int scale = 10;
        Display display = new Display(scale);

        int x = 5;
        int y = 5;
        int val = 2;

        assertThrows(IllegalArgumentException.class, () -> display.setPixel(x, y, val));
    }
}