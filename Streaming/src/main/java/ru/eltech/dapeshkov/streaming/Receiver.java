package ru.eltech.dapeshkov.streaming;

import org.apache.spark.storage.StorageLevel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.Arrays;
import java.util.List;

/**
 * Receiver to push data to Apache Streaming
 * We need it because we of sliding-window
 */
public class Receiver extends org.apache.spark.streaming.receiver.Receiver<String> {
    private final String directory;
    private final int window;

    /**
     * @param directory the path to directory to read files from
     * @param window size of window (how many messages should be accumulated before pushing to Apache Spark)
     */
    public Receiver(String directory, int window) {
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.directory = directory;
        this.window = window;
    }

    @Override
    public void onStart() {
        new Thread(this::receive).start();
    }

    @Override
    public void onStop() {

    }

    private void receive() {
        List<String> list = Arrays.asList(new String[window]);
        Watcher watcher = null;
        try {
            watcher = new Watcher(Paths.get(directory));
        } catch (IOException e) {
            stop("IOException");
        }
        String userInput;
        int i = 0;
        List<Path> changedFiles;
        while (!isStopped()) {
            changedFiles = watcher.getChangedFiles(StandardWatchEventKinds.ENTRY_MODIFY);
            for (Path path : changedFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                    userInput = reader.readLine();
                    if (userInput != null) {
                        list.set(i, userInput);
                        i = (i + 1) % window;
                    }
                    if (list.get(list.size() - 1) != null) {
                        store(list.iterator());
                    }
                } catch (IOException e) {
                    stop("IOException");
                }
            }
        }
    }
}
