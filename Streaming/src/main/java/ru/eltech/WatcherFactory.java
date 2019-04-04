package ru.eltech;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WatcherFactory {

    private WatcherFactory() {

    }

    private static final Map<Path, Watcher> store = new HashMap<Path, Watcher>();

    public static Watcher get(Path path) throws IOException {
        synchronized (WatcherFactory.class) {
            Watcher result = store.get(path);
            if (result == null) {
                result = new Watcher(path);
                store.put(path, result);
            }

            return result;
        }
    }
}