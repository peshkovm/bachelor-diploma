package ru.eltech;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapSinglton {
    private HashMapSinglton() {
    }

    private static class HashMapHolder {
        static final Map<String, ArrayBlockingQueue<Schema>> map = new ConcurrentHashMap<>();
    }

    public static Map<String, ArrayBlockingQueue<Schema>> getHelper() {
        return HashMapHolder.map;
    }
}