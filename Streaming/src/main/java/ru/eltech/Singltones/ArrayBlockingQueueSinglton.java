package ru.eltech.Singltones;

import ru.eltech.Schema;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueSinglton {
    private ArrayBlockingQueueSinglton() {
    }

    private static class QueueHolder {
        static final ArrayBlockingQueue<Schema> helper = new ArrayBlockingQueue<Schema>(5);
    }

    public static ArrayBlockingQueue<Schema> getHelper() {
        return QueueHolder.helper;
    }
}