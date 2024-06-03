package com.andcool.sillyLogger;

import static java.lang.String.format;

public class SillyLogger {
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";
    public String name;
    private final boolean colors;

    public SillyLogger(String name, boolean colors) {
        this.name = name;
        this.colors = colors;
    }

    public void log(Level level, String message) {
        String color;
        switch (level) {
            case ERROR -> color = RED;
            case WARN -> color = YELLOW;
            default -> color = "";
        }
        System.out.println((colors? color : "") + format("[%s][%s] %s", this.name, level.name(), message) + (colors? RESET : ""));
    }
}
