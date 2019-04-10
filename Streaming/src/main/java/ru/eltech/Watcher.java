package ru.eltech;

import java.io.IOException;
import java.nio.file.*;

public class Watcher {

    final private WatchService watchService;
    final private String file;

    public Watcher(Path path) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        file = path.getName(path.getNameCount() - 1).toString();
    }

    public void process(Action action) {
        while (true) {
            final WatchKey wk;
            try {
                wk = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            for (WatchEvent<?> event : wk.pollEvents()) {
                final Path changed = (Path) event.context();
                if (changed.endsWith(file)) {
                    action.run();
                }
            }
            // reset the key
            boolean valid = wk.reset();
            if (!valid) {
                System.err.println("Key has been unregistered");
            }
        }
    }

    public boolean check() {
        final WatchKey wk;

        wk = watchService.poll();

        for (WatchEvent<?> event : wk.pollEvents()) {
            final Path changed = (Path) event.context();
            if (changed.endsWith(file)) {
                return true;
            }
        }
        // reset the key
        boolean valid = wk.reset();
        if (!valid) {
            System.err.println("Key has been unregistered");
        }
        return false;
    }
}