package ru.eltech.dapeshkov.speed_layer;

/**
 * This is a test class
 */

public class Main {
    public static void main(String[] args) {
        NewsReader reader = new NewsReader(new NewsReader.URLFilePair("out1.txt", "https://www.rbc.ru/search/ajax/?limit=1&tag=Google"));
        reader.start();
    }
}
