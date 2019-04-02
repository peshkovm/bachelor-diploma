package ru.eltech.dapeshkov.speed_layer;

import java.io.*;
import java.util.Random;

/**
 * This is a test class
 */

public class Main {
    public static void main(final String[] args) {
        //final NewsReader reader = new NewsReader("files/", "Google", "Google");
        //reader.start();
        int i = 0;
        Random rand = new Random();
        while (true) {
            try (final PrintWriter writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream("files/" + i++ + ".txt"))), true)) {
                writer.println("Google,neutral,2019,3," + i + "," + rand.nextInt(100));
                Thread.sleep(1000);
            } catch (FileNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}