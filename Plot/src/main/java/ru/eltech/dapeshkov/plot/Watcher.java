package ru.eltech.dapeshkov.plot;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class Watcher {

    final private WatchService watchService;

    public Watcher(Path path) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
    }

    public void take(WatchEvent.Kind<Path> watchEvent, String file) {
        WatchKey wk = null;

        for (; ; ) {
            try {
                wk = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            for (WatchEvent<?> event : wk.pollEvents()) {
                if (event.kind() == watchEvent) {
                    final Path changed = (Path) event.context();
                    try {
                        if (Files.isSameFile(changed, Paths.get(file))) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // reset the key
            boolean valid = wk.reset();
            if (!valid) {
                System.err.println("Key has been unregistered");
            }
        }
    }
}