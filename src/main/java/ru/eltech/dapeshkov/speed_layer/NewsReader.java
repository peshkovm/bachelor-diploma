package ru.eltech.dapeshkov.speed_layer;

import ru.eltech.dapeshkov.classifier.Processing;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
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
            Connection connection = new Connection(a);
            ex.scheduleAtFixedRate(new Runnable() {
                private LocalDateTime lastpubdate = null;

                @Override
                public void run() {
                    try (connection) {
                        JSONProcessor.News news = JSONProcessor.parse(connection.get(), JSONProcessor.News.class);
                        if (lastpubdate == null || news.getItems()[0].getPublish_date().isAfter(lastpubdate)) {
                            lastpubdate = news.getItems()[0].getPublish_date();
                            //write(news.toString() + " " + Processing.sentiment(news.toString()) + "\n" + "\n", out);
                            //Write to socket
                            write(news.toString() + " " + Processing.sentiment(news.toString()) + "\n" + "\n", finalSinkSocket);
                            write(news.toString() + " " + Processing.sentiment(news.toString()) + "\n" + "\n", out);
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
}