package ru.eltech;

import org.apache.spark.ml.PipelineModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WatcherFactory {

    private static volatile Watcher watcher;

    private WatcherFactory() {

    }

    public static Watcher getWatcher(Path path) {
        if (watcher == null) {
            synchronized (WatcherFactory.class) {
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