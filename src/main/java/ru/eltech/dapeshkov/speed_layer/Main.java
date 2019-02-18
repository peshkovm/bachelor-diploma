package ru.eltech.dapeshkov.speed_layer;

/**
 * This is a test class
 */

public class Main {
    public static void main(String[] args) {
        NewsReader reader = new NewsReader("out.txt", "https://www.rbc.ru/search/ajax/?limit=1&tag=Google", "https://www.rbc.ru/search/ajax/?limit=1&tag=Google");
        reader.start();
    }
}
