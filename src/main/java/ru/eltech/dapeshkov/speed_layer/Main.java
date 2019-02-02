package ru.eltech.dapeshkov.speed_layer;

import ru.eltech.dapeshkov.speed_layer.NewsReader.URLFilePair;

import java.io.IOException;

/**
 * This is a test class
 */

public class Main {
    public static void main(String[] args) throws IOException {
        NewsReader reader = new NewsReader(new URLFilePair("out1.txt","https://www.rbc.ru/search/ajax/?limit=1&tag=Google"));
        reader.start();
    }
}