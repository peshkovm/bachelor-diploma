package ru.eltech.spark;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class A {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                int i = 0;
                while (true) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("files/src" + i + ".txt")), true);
                    out.println(new Random().nextInt());
                    Thread.sleep(1000);
                    i++;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
