package com.qelery.chip8.util;

public class IOUtils {

    private IOUtils() {
        throw new IllegalArgumentException("Utility class");
    }

    public static void clearConsole() {
        System.out.println(new String(new char[50]).replace("\0", System.lineSeparator()));
    }
}
