package ru.eltech.dapeshkov.streaming;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Watcher directory for changes
 */
public class Watcher {

    final private WatchService watchService;
    private Path path;

    /**
     * creates new {@link Watcher} instance
     * @param path path to directory to watch for changes
     * @throws IOException
     */
    public Watcher(Path path) throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        this.path = path;
    }

    /**
     * gets all changed files in directory
     * @param watchEvent what kind of event to watch
     * @return all changed files
     */
    public List<Path> getChangedFiles(WatchEvent.Kind<Path> watchEvent) {
        WatchKey wk = null;
        try {
            wk = watchService.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

/*        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        wk.pollEvents().stream().distinct().filter(event -> event.kind() == watchEvent).forEach(s -> System.out.println(s.context()));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");*/

        List<Path> collect = wk.pollEvents().stream().distinct().filter(event -> event.kind() == watchEvent).map(event -> path.resolve((Path) event.context())).collect(Collectors.toList());
        // reset the key
        boolean valid = wk.reset();
        if (!valid) {
            System.err.println("Key has been unregistered");
        }

        return collect;
    }

    /**
     * checks if file has changed
     * @param watchEvent what kind of event to watch
     * @param file name of file to watch for changes
     * @return if file has changed
     */
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