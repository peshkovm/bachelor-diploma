package ru.eltech.dapeshkov.speed_layer;

/**
 * This is a test class
 */

public class Main {
    public static void main(String[] args) {
        NewsReader reader = new NewsReader("out.txt", "Google", "Google");
        reader.start();
    }
}
