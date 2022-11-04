package com.qelery.chip8.util;

public class IOUtils {

    public static void clearConsole() {
        System.out.println(new String(new char[50]).replace("\0", System.lineSeparator()));
    }
}
