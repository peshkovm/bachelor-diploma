package ru.eltech.mapeshkov;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        PlotHelper plotHelper = new PlotHelper("");

        for (; ; ) {
            TimeUnit.SECONDS.sleep(1);
            plotHelper.refresh();
        }
    }
}