package ru.eltech;

import ru.eltech.dapeshkov.speed_layer.Item;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ArrayBlockingQueueSinglton {
    private ArrayBlockingQueueSinglton() {
    }

    private static class ArrayBlockingQueueHolder {
        static final ArrayBlockingQueue<Item> arrayBlockingQueue = new ArrayBlockingQueue<Item>(5);
    }

    public static ArrayBlockingQueue<Item> getHelper() {
        return ArrayBlockingQueueHolder.arrayBlockingQueue;
    }
}