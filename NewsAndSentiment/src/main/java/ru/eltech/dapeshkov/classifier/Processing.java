package ru.eltech.dapeshkov.classifier;

import ru.eltech.dapeshkov.news.JSONProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class for sentiment analysis
 */

public class Processing {

    //mapping (word,sentiment) to count, each word (or several words) gets mapped for later use in sentiment method
    private static final HashMap<Pair, Double> likelihood = new HashMap<>(); //concurency not needed final field safe published
    //mapping sentiment to ount of documents with given sentiment
    private static final HashMap<String, Double> prior_probability = new HashMap<>(); //concurency not needed final field safe published
    //stopwords
    private static final Set<String> hash = new HashSet<>();
    //count of documents
    private static int count = 0;
    //ngram count of words in feature vector
    private static int n;
    //sentiment
    static final private String[] category = {"positive", "negative", "neutral" };

    //stopwords into hash
    static {
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(Processing.class.getResourceAsStream("/stopwatch.txt"))).lines()) {
            lines.forEach(hash::add);
        }
    }

    private Processing() {

    }

    //(word,sentiment)
    private static class Pair {
        final String word;
        final String category;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Pair pair = (Pair) o;
            return Objects.equals(getWord(), pair.getWord()) &&
                    Objects.equals(getCategory(), pair.getCategory());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getWord(), getCategory());
        }

        Pair(final String word, final String category) {
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

    /**
     * converts {@link String} to lower case, removes all words present in stoplist, removes duplicates, collects to feature vector
     *
     * @param str {@link String} to parse
     * @param n number of words in feature vector element
     * @return the {@link String[]} representing feature vector
     */
    private static String[] parse(final String str, final int n) {
        String[] res = str.toLowerCase().split("[^\\p{L}]+");
        if (res.length < n) return null;
        res = Arrays.stream(res).filter(t -> !hash.contains(t)).distinct().toArray(String[]::new);
        res = ngram(res, n);

        return res;
    }

    /**
     * converts array of {@link String} into feature vector (bag of words)
     * @param arr array of words to convert to feature vector
     * @param n number of words in feature vector element
     * @return {@link String[]} representing feature vector
     */
    private static String[] ngram(final String[] arr, final int n) {
        String[] res = new String[arr.length - n + 1];
        for (int i = 0; i < arr.length - n + 1; i++) {
            final StringBuilder str = new StringBuilder();
            for (int j = 0; j < n; j++) {
                str.append(arr[i + j]).append(" ");
            }
            res[i] = str.toString();
        }
        return res;
    }

    /**
     * trains the model
     * @param n number of words in feature vector element
     */
    static public void train(final int n) {

        Processing.n = n;

        JSONProcessor.Train[] arr = null;

        //reads train data
        try (InputStream in = Processing.class.getResourceAsStream("/final/train1_1.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //number of documents
        count = Objects.requireNonNull(arr).length;

        //trains the model
        Arrays.stream(arr).unordered().forEach(i -> {
            final String[] strings = parse(i.getText(), Processing.n);
            if (strings != null) {
                Arrays.stream(strings).unordered().forEach((str) -> likelihood.compute(new Pair(str, i.getSentiment()), (k, v) -> (v == null) ? 1 : v + 1));
                prior_probability.compute(i.getSentiment(), (k, v) -> (v == null) ? 1 : v + 1);
            }
        });

        //TODO train accuracy
        /*Comparator<Map.Entry<Pair, Double>> entryComparator = Comparator.comparing(Map.Entry::getValue);
        entryComparator = entryComparator.reversed();

        Map<Pair, Double> negative = likelihood.entrySet().stream().filter(e -> e.getKey().category.equals("negative")).sorted(entryComparator).limit(4000).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Pair, Double> neutral = likelihood.entrySet().stream().filter(e -> e.getKey().category.equals("neutral")).sorted(entryComparator).limit(4000).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<Pair, Double> positive = likelihood.entrySet().stream().filter(e -> e.getKey().category.equals("positive")).sorted(entryComparator).limit(4000).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        likelihood.clear();
        likelihood.putAll(negative);
        likelihood.putAll(neutral);
        likelihood.putAll(positive);*/
    }

    //method to colculate the likelihood of the text to givven sentiment
    private static Double classify_cat(final String str, final String... arr) {
        //log is used to not multiply small close to 0 numbers, instead sum is used
        //laplacian smooth is used
        return Math.log(prior_probability.get(str) / count) +
                Arrays.stream(arr).unordered()
                        .mapToDouble(value -> (likelihood.getOrDefault(new Pair(value, str), 0d) + 1) / (prior_probability.get(str) + likelihood.size()))
                        .reduce(0, (left, right) -> left + Math.log(right));
    }


    /**
     * computes teh sentiment for text
     * @param str text
     * @return sentiment
     */
    static public String sentiment(final String str) {
        final String[] arr = parse(str, Processing.n);
        if (arr == null) {
            return null;
        }

        return Arrays.stream(category).unordered()
                .max(Comparator.comparingDouble(o -> classify_cat(o, arr)))
                .get();
    }

    public static void main(final String[] args) {
        train(1);
        JSONProcessor.Train[] arr = null;

        try (InputStream in = Processing.class.getResourceAsStream("/final/test1_2.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i = 0;
        for (JSONProcessor.Train a : arr) {
            String sentiment = sentiment(a.getText());
            if (sentiment == null) {
                continue;
            }
            String sentiment1 = a.getSentiment();
            if (sentiment.equals(sentiment1)) {
                i++;
            }
        }
        System.out.println((i / (double) arr.length) * 100);
    }
}