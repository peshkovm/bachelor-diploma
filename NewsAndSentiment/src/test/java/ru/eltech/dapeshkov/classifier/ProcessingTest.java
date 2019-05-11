package ru.eltech.dapeshkov.classifier;

import ru.eltech.dapeshkov.news.JSONProcessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.newBufferedWriter;

public class ProcessingTest {
    private static final Set<String> hash = new HashSet<>();
    static Map<String, String> list = new HashMap<>();
    static Object[] arr;

    public static void get_news(String[] arr) throws IOException {
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(Processing.class.getResourceAsStream("/stopwatch.txt"))).lines()) {
            lines.forEach(hash::add);
        }
        BufferedWriter bufferedWriter = newBufferedWriter(Paths.get("news.csv"), StandardOpenOption.CREATE);
        Arrays.stream(arr).map(i -> i.toLowerCase().replaceAll("[^\\p{L}]+", " ")).map(s -> {
            String[] s1 = s.split(" ");
            StringBuilder str = new StringBuilder();
            for (String i : s1) {
                if (!hash.contains(i) && i.length() > 3)
                    str.append(i).append(" ");
            }
            return str.toString().trim();
        }).forEach(s -> {
            try {
                System.out.println(s);
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bufferedWriter.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        JSONProcessor.Train[] arr = null;

        try (InputStream in = Processing.class.getResourceAsStream("/train.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONProcessor.Train[] trains = Arrays.copyOfRange(arr, 5000, 8000);
        ProcessingTest.arr = trains;
        String[] str = new String[trains.length];
        int a = 0;
        for (JSONProcessor.Train i : trains) {
            str[a++] = i.getText();
        }
        get_news(str);
        lemmatizer();
        //sentiment();
        json(trains);
    }

    public static void lemmatizer() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("./mystem", "-cld", "news.csv", "news_lem.csv");
        Process start = processBuilder.start();
        start.waitFor();
        BufferedWriter bufferedWriter = newBufferedWriter(Paths.get("news_lem_parsed.csv"), StandardOpenOption.CREATE);
        try (Stream<String> lines = Files.lines(Paths.get("news_lem.csv"))) {
            lines.map(i -> i.replaceAll("(\\{|})", "")).map(i -> i.replaceAll("\\p{L}*\\?+", "")).map(i -> i.trim()).map(i -> i.replaceAll(" +", " ")).filter(i -> i.length() != 0).forEach(i -> {
                try {
                    bufferedWriter.write(i);
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        bufferedWriter.close();
    }

    public static void sentiment() throws IOException {
        JSONProcessor.Train[] items = new JSONProcessor.Train[arr.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = new JSONProcessor.Train();
        }
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(Processing.class.getResourceAsStream("/full word_rating_after_coding.csv"))).lines()) {
            lines.forEach(s -> {
                String[] split = s.split(",");
                list.put(split[0], split[1]);
            });
        }
        final int[] i = {0};
        try (Stream<String> news_lem_parsed = Files.lines(Paths.get("news_lem_parsed.csv"))) {
            news_lem_parsed.forEach(s -> {
                items[i[0]].setText(s);
                String[] s1 = s.split(" ");
                int pos = 0;
                int neg = 0;
                for (String s2 : s1) {
                    String s3 = list.get(s2);
                    if (s3 != null) {
                        if (s3.equals("negative")) {
                            neg++;
                        } else {
                            pos++;
                        }
                    }
                }
                double a = ((double) pos + neg) / (pos - neg);
                items[i[0]++].setSentiment(a > 0 ? "positive" : a < 0 ? "negative" : "neutral");
            });
        }
        String write = JSONProcessor.write(items);
        BufferedWriter bufferedWriter = newBufferedWriter(Paths.get("train.json"), StandardOpenOption.CREATE);
        bufferedWriter.write(write);
        bufferedWriter.close();
    }

    public static void json(JSONProcessor.Train[] arr) throws IOException {
        final int[] i = {0};
        try (Stream<String> news_lem_parsed = Files.lines(Paths.get("news_lem_parsed.csv"))) {
            news_lem_parsed.forEach(s -> {
                arr[i[0]++].setText(s);
            });
        }
        String write = JSONProcessor.write(arr);
        BufferedWriter bufferedWriter = newBufferedWriter(Paths.get("train.json"), StandardOpenOption.CREATE);
        bufferedWriter.write(write);
        bufferedWriter.close();
    }
}