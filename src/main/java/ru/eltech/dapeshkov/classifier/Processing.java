package ru.eltech.dapeshkov.classifier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Processing {

    private static Map<Pair, Double> likelihood = Collections.synchronizedMap(new HashMap<Pair, Double>());
    private static Map<String, Double> prior_probability = Collections.synchronizedMap(new HashMap<String, Double>());
    private static HashSet<String> hash = new HashSet<>();
    static int count = 0;
    static int n;

    private static class Pair {
        String word;
        String category;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(getWord(), pair.getWord()) &&
                    Objects.equals(getCategory(), pair.getCategory());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getWord(), getCategory());
        }

        private Pair(String word, String category) {
            this.word = word;
            this.category = category;
        }

        private String getWord() {
            return word;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "word='" + word + '\'' +
                    ", category='" + category + '\'' +
                    '}';
        }

        private String getCategory() {
            return category;
        }
    }

    static String[] parse(String str, int n) {
        //str = str.toLowerCase().replaceAll("\\p{P}", "");
        //String[] res = Pattern.compile("([^\\s]+)").matcher(str).results().map(MatchResult::group).filter(t -> !hash.contains(t)).distinct().toArray(String[]::new);
        //String[] res = str.split("([^\\s]+)");

        //res = ngram(res, n);

        String[] res={"odfjidof","oijfodjf","iodfjiudf"};

        return res;
    }

    static String[] ngram(String[] arr, int n) {
        if (n == 1) return arr;

        StringBuilder[] res = new StringBuilder[arr.length - n + 1];
        for (int i = 0; i < res.length; ++i) {
            res[i] = new StringBuilder();
        }

        for (int i = 0; i < arr.length - n + 1; i++) {
            for (int j = 0; j < n; j++) {
                res[i] = res[i].append(arr[i + j]).append(" ");
            }
        }
        return Arrays.stream(res).map(StringBuilder::toString).toArray(String[]::new);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1, warmups = 1)
    static public void train() {
        /*try {
            Files.lines(Paths.get("stopwatch.txt")).forEach(hash::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Processing.n = n;*/

        Path path = FileSystems.getDefault().getPath("train.json");
        String str1 = null;
        try {
            str1 = Files.lines(path).collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //JSONProcessor.Train[] arr = JSONProcessor.parse(str1, JSONProcessor.Train[].class);
        //count = arr.length;

        //Arrays.stream(arr).unordered().parallel().forEach(i -> {
            //String[] strings = parse(i.getText(), Processing.n);
            //Arrays.stream(strings).unordered().forEach((str) -> likelihood.compute(new Pair(str, i.getSentiment()), (k, v) -> (v == null) ? 1 : v + 1));
            //prior_probability.compute(i.getSentiment(), (k, v) -> (v == null) ? 1 : v + 1);
        //});

        //likelihood.replaceAll((t, u) -> (u + 1) / (prior_probability.get(t.getCategory()) + likelihood.size()));
        //prior_probability.replaceAll((t, u) -> u);
    }

    static Double classify_cat(String str, String[] arr) {
        return Math.log(prior_probability.get(str) / count) +
                Arrays.stream(arr)
                        .mapToDouble(value -> likelihood.getOrDefault(new Pair(value, str), 1 / (prior_probability.get(str) / count + likelihood.size())))
                        .reduce(0, (left, right) -> left + Math.log(right));
    }

    static String sentiment(String str) {
        String[] category = {"positive", "negative", "neutral"};
        String[] arr = parse(str, Processing.n);


        //Arrays.stream(category).forEach(t -> System.out.println(classify_cat(t, arr)));
        return Arrays.stream(category)
                .max(Comparator.comparingDouble(o -> classify_cat(o, arr)))
                .get();
    }

    /*public static void main(String[] args) {
        *//*long l=System.currentTimeMillis();
        train(2);
        long l2=System.currentTimeMillis();
        System.out.println(l2-l);

        Path path = FileSystems.getDefault().getPath("train.json");
        String str1 = null;

        try {
            str1 = Files.lines(path).collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONProcessor.Train[] arr = JSONProcessor.parse(str1, JSONProcessor.Train[].class);

        int a = 0;
        int b = arr.length;

        for (JSONProcessor.Train i : arr) {
            String str = sentiment(i.getText());
            if (str.equals(i.getSentiment())) {
                a++;
            }
        }

        System.out.println((double) a / b * 100);*//*
    }*/
}