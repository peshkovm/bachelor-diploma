package ru.eltech.dapeshkov.classifier;

import ru.eltech.dapeshkov.speed_layer.JSONProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Processing {

    private static Map<Pair, Double> likelihood = Collections.synchronizedMap(new HashMap<Pair, Double>());
    private static Map<String, Double> prior_probability = Collections.synchronizedMap(new HashMap<String, Double>());
    private static final HashSet<String> hash = new HashSet<>();
    private static int count = 0;
    private static int n;
    static final private String[] category = {"positive", "negative", "neutral"};

    static {
        try {
            Files.lines(Paths.get("stopwatch.txt")).forEach(hash::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        String[] res = str.toLowerCase().split("[^\\p{L}]+");
        res = Arrays.stream(res).filter(t -> !hash.contains(t)).distinct().toArray(String[]::new);

        res = ngram(res, n);

        return res;
    }

    static String[] ngram(String[] arr, int n) {
        String[] res = new String[arr.length - n + 1];
        for (int i = 0; i < arr.length - n + 1; i++) {
            StringBuilder str = new StringBuilder();
            for (int j = 0; j < n; j++) {
                str.append(arr[i + j]).append(" ");
            }
            res[i] = str.toString();
        }
        return res;
    }

    static public void train(int n) {
        Processing.n = n;

        JSONProcessor.Train[] arr = null;

        try (FileInputStream in = new FileInputStream("train.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        count = arr.length;

        Arrays.stream(arr).parallel().unordered().forEach(i -> {
            String[] strings = parse(i.getText(), Processing.n);
            Arrays.stream(strings).unordered().forEach((str) -> likelihood.compute(new Pair(str, i.getSentiment()), (k, v) -> (v == null) ? 1 : v + 1));
            prior_probability.compute(i.getSentiment(), (k, v) -> (v == null) ? 1 : v + 1);
        });

        likelihood.replaceAll((t, u) -> (u + 1) / (prior_probability.get(t.getCategory()) + likelihood.size()));
        prior_probability.replaceAll((t, u) -> u);
    }

    static Double classify_cat(String str, String[] arr) {
        return Math.log(prior_probability.get(str) / count) +
                Arrays.stream(arr)
                        .mapToDouble(value -> likelihood.getOrDefault(new Pair(value, str), 1 / (prior_probability.get(str) / count + likelihood.size())))
                        .reduce(0, (left, right) -> left + Math.log(right));
    }

    static String sentiment(String str) {
        String[] arr = parse(str, Processing.n);


        return Arrays.stream(category).unordered()
                .max(Comparator.comparingDouble(o -> classify_cat(o, arr)))
                .get();
    }

    public static void main(String[] args) {
        train(2);
    }
}