package ru.eltech.dapeshkov.streaming;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Watcher {

    final private WatchService watchService;
    private Path path;

    public Watcher(Path path) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        this.path = path;
    }

    public List<Path> getChangedFiles(WatchEvent.Kind<Path> watchEvent) {
        final WatchKey wk;
        try {
            wk = watchService.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        List<Path> collect = wk.pollEvents().stream().filter(event -> event.kind() == watchEvent).map(event -> path.resolve((Path) event.context())).collect(Collectors.toList());
        // reset the key
        boolean valid = wk.reset();
        if (!valid) {
            System.err.println("Key has been unregistered");
        }

        return collect;
    }

    public boolean check(WatchEvent.Kind<Path> watchEvent, String file) {
        final WatchKey wk;
        boolean res = false;

        wk = watchService.poll();

        if (wk != null) {
            for (WatchEvent<?> event : wk.pollEvents()) {
                if (event.kind() == watchEvent) {
                    final Path changed = (Path) event.context();
                    try {
                        if (Files.isSameFile(changed, Paths.get(file))) {
                            res = true;
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

        return res;
    }
}