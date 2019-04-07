package ru.eltech.dapeshkov.speed_layer;

import ru.eltech.dapeshkov.classifier.Processing;
import ru.eltech.mapeshkov.not_spark.ApiUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class reads content from given URLs and outputs the parsed content in the files.
 * After invocation of method {@link #start() start()} it outputs parsed content of all sites to the files, then it will output only newly added items when Connection are updated.
 * This program requests sites contests every 3 seconds.
 *
 * @author Peshkov Denis.
 */

public class NewsReader {
    private final ScheduledExecutorService ex = Executors.newScheduledThreadPool(4); // ExecutorService that runs the tasks
    private final String[] url;
    private final String out;
    private static final AtomicInteger i = new AtomicInteger(0);

    /**
     * Initialize the instance of {@code NewsReader}.
     *
     * @param url the array of {@link String}
     * @param out the output file {@link String}
     */

    public NewsReader(final String out, final String... url) {
        this.url = url;
        this.out = out;
        Processing.train(2);
        System.out.println("Ready");
    }

    synchronized void write(final String str, final OutputStream out) {
        try (final PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(out)), true)) {
            writer.println(str);
        }
    }

    /**
     * This method requests all given sites and outputs the contents to the given files.
     * Most of the time this method should be invoked only once.
     * Method works as a service running all the time with 3 second interval
     */

    public void start() {
        for (final String a : url) {
            final Connection connection = new Connection("https://www.rbc.ru/search/ajax/?limit=1&tag=" + a);
            ex.scheduleAtFixedRate(new Runnable() {
                private LocalDateTime lastpubdate = null;

                @Override
                public void run() {
                    try (final Connection con = connection) {
                        final JSONProcessor.News news = JSONProcessor.parse(con.get(), JSONProcessor.News.class);
                        if (news != null && (lastpubdate == null || news.getItems()[0].getPublish_date().isAfter(lastpubdate))) {
                            lastpubdate = news.getItems()[0].getPublish_date();
                            final Item item = new Item(a, Processing.sentiment(news.getItems()[0].toString()), lastpubdate, ApiUtils.AlphaVantageParser.getLatestStock(a).getChange());
                            write(item.toString(), new FileOutputStream(out + a + "/" + i.incrementAndGet() + ".txt"));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 3, TimeUnit.SECONDS);
        }
    }

    public static class Item {
        private String sentiment;
        private String company_name;
        private double stock;
        LocalDateTime dateTime;

        public Item(String company_name, String sentiment, LocalDateTime dateTime, double stock) {
            this.sentiment = sentiment;
            this.company_name = company_name;
            this.stock = stock;
            this.dateTime = dateTime;
        }

        @Override
        public String toString() {
            return getCompany_name() + "," + getSentiment() + "," + getDateTime() + "," + getStock();
        }

        public String getSentiment() {
            return sentiment;
        }

        public void setSentiment(String sentiment) {
            this.sentiment = sentiment;
        }

        public String getCompany_name() {
            return company_name;
        }

        public void setCompany_name(String company_name) {
            this.company_name = company_name;
        }

        public double getStock() {
            return stock;
        }

        public void setStock(double stock) {
            this.stock = stock;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }
}