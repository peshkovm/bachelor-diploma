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

    public static void main(final String[] args) throws FileNotFoundException {
        /*final NewsReader reader = new NewsReader("working_files/files/", "Google");
        reader.start();*/
        LocalDateTime localDateTime = LocalDateTime.now();
        int i = 2565;
        Random rand1 = new Random();
        while (true) {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream("working_files/files/Google/" + i++ + ".txt", false), true);
            Timestamp timestamp = Timestamp.valueOf(localDateTime);
            printWriter.println("Google,neutral," + timestamp + "," + rand1.nextInt(100));
            printWriter.close();
            localDateTime = localDateTime.plusMinutes(1);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}