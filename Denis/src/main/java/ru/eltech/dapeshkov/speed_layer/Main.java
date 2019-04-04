package ru.eltech.dapeshkov.speed_layer;

import java.io.*;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Random;
import java.sql.Timestamp;

/**
 * This is a test class
 */

public class Main {
    public static void main(final String[] args) {
        //final NewsReader reader = new NewsReader("files/", "Google", "Google");
        //reader.start();
        int i = 0;
        Random rand = new Random();
        LocalDateTime min = LocalDateTime.MIN;
        while (true) {
            try (final PrintWriter writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream("files/" + i++ + ".txt"))), true)) {
                writer.println("Google,neutral,2019,3,1," + rand.nextInt(100) + "," + Timestamp.valueOf(min));
                min = min.plusMinutes(1);
                Thread.sleep(10000);
            } catch (FileNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}