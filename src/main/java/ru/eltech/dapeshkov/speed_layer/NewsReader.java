package ru.eltech.dapeshkov.speed_layer;

import ru.eltech.dapeshkov.classifier.Processing;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    /**
     * Initialize the instance of {@code NewsReader}.
     *
     * @param url the array of {@link String}
     * @param out the output file {@link String}
     */

    public NewsReader(String out, String... url) {
        this.url = url;
        this.out = out;
        Processing.train(2);
        System.out.println("Ready");
    }

    synchronized private void write(String str, String out) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(out, true))) {
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized private void write(String str, Socket sink) {
        try {
            PrintWriter writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(sink.getOutputStream())), true);
            writer.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method requests all given sites and outputs the contents to the given files.
     * Most of the time this method should be invoked only once.
     * Method works as a service running all the time with 3 second interval
     */

    public void start() {
        /////////////////////////////////
        int port = 5555;
        Socket sinkSocket = null;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Wait for socket accept");
            sinkSocket = serverSocket.accept();
            System.out.println("Socket accepted");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Socket finalSinkSocket = sinkSocket;
        /////////////////////////////////

        for (String a : url) {
            Connection connection = new Connection("https://www.rbc.ru/search/ajax/?limit=1&tag=" + a);
            ex.scheduleAtFixedRate(new Runnable() {
                private LocalDateTime lastpubdate = null;

                @Override
                public void run() {
                    try (connection) {
                        JSONProcessor.News news = JSONProcessor.parse(connection.get(), JSONProcessor.News.class);
                        if (lastpubdate == null || news.getItems()[0].getPublish_date().isAfter(lastpubdate)) {
                            lastpubdate = news.getItems()[0].getPublish_date();
                            Item item = new Item(news.getItems()[0].toString(), Processing.sentiment(news.getItems()[0].toString()), a);
                            write(JSONProcessor.write(item) + "/n", Objects.requireNonNull(finalSinkSocket));
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 3, TimeUnit.SECONDS);
        }

/*        try {
            sinkSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static class Item {
        private String news;
        private String sentiment;
        private String company_name;

        @Override
        public String toString() {
            return "Item{" +
                    "news='" + news + '\'' +
                    ", sentiment='" + sentiment + '\'' +
                    ", company_name='" + company_name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return Objects.equals(news, item.news) &&
                    Objects.equals(sentiment, item.sentiment) &&
                    Objects.equals(company_name, item.company_name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(news, sentiment, company_name);
        }

        public Item(String news, String sentiment, String company_name) {
            this.news = news;
            this.sentiment = sentiment;
            this.company_name = company_name;
        }

        public void setNews(String news) {
            this.news = news;
        }

        public void setSentiment(String sentiment) {
            this.sentiment = sentiment;
        }

        public void setCompany_name(String company_name) {
            this.company_name = company_name;
        }

        public String getNews() {
            return news;
        }

        public String getSentiment() {
            return sentiment;
        }

        public String getCompany_name() {
            return company_name;
        }
    }
}