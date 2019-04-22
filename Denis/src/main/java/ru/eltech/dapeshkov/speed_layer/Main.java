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
        final NewsReader reader = new NewsReader("working_files/files/", "Google");
        reader.start();
    }
}