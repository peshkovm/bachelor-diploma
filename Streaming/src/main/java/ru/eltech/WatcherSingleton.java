package ru.eltech;

import java.io.IOException;
import java.nio.file.Path;

public final class WatcherSingleton {

    private static volatile Watcher watcher;

    private WatcherSingleton() {

    }

    public static Watcher getWatcher(Path path) {
        if (watcher == null) {
            synchronized (WatcherSingleton.class) {
                if (watcher == null) {
                    try {
                        watcher = new Watcher(path);
                    } catch (IOException e) {
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }
        return watcher;
    }
}