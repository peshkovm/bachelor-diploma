package ru.eltech.Singltones;

import ru.eltech.Schema;
import ru.eltech.Watcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;

public class WatcherSingltone {
    private WatcherSingltone() {
    }

    private static class WatcherHolder {
        private static Watcher helper;

        static {
            try {
                helper = new Watcher(Paths.get("models/"));
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static Watcher getHelper() {
        return WatcherHolder.helper;
    }
}