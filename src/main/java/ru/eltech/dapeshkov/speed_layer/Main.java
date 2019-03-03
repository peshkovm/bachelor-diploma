package ru.eltech.dapeshkov.speed_layer;

/**
 * This is a test class
 */

public class Main {
    public static void main(final String[] args) {
        final NewsReader reader = new NewsReader("out.txt", "Google", "Google");
        reader.start();
    }
}