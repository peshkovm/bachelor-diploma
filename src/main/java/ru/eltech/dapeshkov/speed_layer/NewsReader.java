package ru.eltech.dapeshkov.speed_layer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.*;

/**
 * This class reads content from given URLs and outputs the parsed content in the files.
 * After invocation of method {@link #start() start()} it outputs parsed content of all sites to the files, then it will output only newly added items when Connection are updated.
 * This program requests sites contests every 3 seconds.
 *
 * @author Peshkov Denis.
 */

public class NewsReader {
    private final URLFilePair[] array;
    private final ScheduledExecutorService ex = Executors.newScheduledThreadPool(4); // ExecutorService that runs the tasks
    private ZonedDateTime lastpubdate = null;

    /**
     * A pair of two {@link String} that is used to represent a file name and an URL name of the site.
     * This class is used to be a parameter for {@link NewsReader constructor} so the number of resources and output files are the same.
     */

    static public class URLFilePair {
        private final String file;
        private final String url;

        /**
         * Initializes the instance of {@code URLFilePair}.
         *
         * @param file output file name
         * @param url  URL of the site
         */

        public URLFilePair(String file, String url) {
            this.file = file;
            this.url = url;
        }

        public String getFile() {
            return file;
        }

        public String getUrl() {
            return url;
        }
    }

    /**
     * Initialize the instance of {@code NewsReader}.
     *
     * @param mas the array of {@link URLFilePair}
     */

    public NewsReader(URLFilePair... mas) {
        array = mas;
    }

    /**
     * This method requests all given sites and outputs the contents to the given files.
     * Most of the time this method should be invoked only once.
     * Method works as a service running all the time with 3 second interval
     */

    public void start() {
        for (URLFilePair a : array) {
            Connection connection = new Connection(a.getUrl());
            ex.scheduleAtFixedRate(() -> {
                try (connection; BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(a.getFile(), true))) {
                    JSONProcessor.News news = JSONProcessor.parse(connection.get(), JSONProcessor.News.class);
                    if (lastpubdate == null || news.getItems()[0].getPublish_date().isAfter(lastpubdate)) {
                        lastpubdate = news.getItems()[0].getPublish_date();
                        bufferedWriter.write(news.toString() + "\n" + "\n");
                        bufferedWriter.flush();
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.SECONDS);
        }
    }
}