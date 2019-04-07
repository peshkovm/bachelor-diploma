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
                writer.println("Google,neutral," + min + "," + rand.nextInt(100));
                min = min.plusMinutes(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}