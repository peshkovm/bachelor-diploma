package ru.eltech.dapeshkov.speed_layer;

import ru.eltech.dapeshkov.classifier.Processing;
import ru.eltech.mapeshkov.not_spark.ApiUtils;

import java.io.*;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Random;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * This is a test class
 */

public class Main {
    static synchronized void write(final String str, final OutputStream out) {
        try (final PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(out)), true)) {
            writer.println(str);
        }
    }

    public static void main(final String[] args) {
        //final NewsReader reader = new NewsReader("files/", "Google", "Google");
        //reader.start();
        /*int i = 0;
        Random rand = new Random();
        LocalDateTime min = LocalDateTime.now();
        while (true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            try {
                String inputString = bufferRead.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (final PrintWriter writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream("files/" + i++ + ".txt"))), true)) {
                writer.println("Google,neutral," + Timestamp.valueOf(min) + "," + i);
                min = min.plusMinutes(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        int a = 0;

        try (final Connection con = new Connection("https://www.rbc.ru/search/ajax/?limit=10&tag=Google")) {
            final JSONProcessor.News news = JSONProcessor.parse(con.get(), JSONProcessor.News.class);
            Processing.train(2);
            for (JSONProcessor.Item i : news.getItems()) {
                final Item item = new Item("Google", Processing.sentiment(i.toString()), Timestamp.valueOf(i.getPublish_date()), ApiUtils.AlphaVantageParser.getLatestStock("Google").getChange());
                write(item.toString(), new FileOutputStream("files/Google/" + a++ + ".txt"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}