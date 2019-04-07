package ru.eltech;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ArrayBlockingQueueSinglton {
    private ArrayBlockingQueueSinglton() {
    }

    private static class ArrayBlockingQueueHolder {
        static final ArrayBlockingQueue<Schema> arrayBlockingQueue = new ArrayBlockingQueue<Schema>(5);
    }

    public static ArrayBlockingQueue<Schema> getHelper() {
        return ArrayBlockingQueueHolder.arrayBlockingQueue;
    }
}