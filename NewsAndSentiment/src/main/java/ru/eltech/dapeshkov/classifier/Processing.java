package ru.eltech.dapeshkov.classifier;

import ru.eltech.dapeshkov.news.JSONProcessor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.newBufferedWriter;

/**
 * class for sentiment analysis
 */

public class Processing<T, K> {

    //mapping (word,sentiment) to count, each word (or several words) gets mapped for later use in sentiment method
    private final HashMap<Pair, Integer> likelihood = new HashMap<>(); //concurency not needed final field safe published
    //mapping sentiment to count with given sentiment
    private final HashMap<T, Integer> prior_probability = new HashMap<>(); //concurency not needed final field safe published
    //stopwords
    private static final Set<String> hash = new HashSet<>();
    //ngram count of words in feature vector
    private static int n;
    //sentiment
    final private Set<T> category = new HashSet<>();
    private int countOfDocuments = 0;
    final private Set<K> vocabulary = new HashSet<>();
    final private Map<T, Integer> counts = new HashMap<>();

    //stopwords into hash
    static {
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(Processing.class.getResourceAsStream("/stopwatch.txt"))).lines()) {
            lines.forEach(hash::add);
        }
    }

    private Processing() {

    }

    //(word,sentiment)
    private class Pair {
        final K word;
        final T category;

        public Pair(K word, T category) {
            this.word = word;
            this.category = category;
        }

        public K getWord() {
            return word;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return word.equals(pair.word) &&
                    category.equals(pair.category);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, category);
        }

        public T getCategory() {
            return category;
        }

        @Override
        public String toString() {
            return word + " " + category;
        }
    }

    /**
     * converts {@link String} to lower case, removes all words present in stoplist, removes duplicates, collects to feature vector
     *
     * @param str {@link String} to parse
     * @param n   number of words in feature vector element
     * @return the {@link String[]} representing feature vector
     */
    static String[] parse(final String str, final int n) {
        String[] res = str.toLowerCase().split("[^\\p{L}]+");
        if (res.length < n) return null;
        res = Arrays.stream(res).filter(t -> !hash.contains(t)).toArray(String[]::new);
        res = ngram(res, n);

        return res;
    }

    /**
     * converts array of {@link String} into feature vector (bag of words)
     *
     * @param arr array of words to convert to feature vector
     * @param n   number of words in feature vector element
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
     *
     * @param n number of words in feature vector element
     */
    public void train(T category, Collection<K> vector) {
        this.category.add(category);
        //number of documents
        countOfDocuments++;

        //trains the model
        vector.stream().unordered().forEach(i -> {
            likelihood.compute(new Pair(i, category), (k, v) -> (v == null) ? 1 : v + 1);
            vocabulary.add(i);
            counts.compute(category, (k, v) -> (v == null) ? 1 : v + 1);
        });
        prior_probability.compute(category, (k, v) -> (v == null) ? 1 : v + 1);
    }

    //method to colculate the likelihood of the text to givven sentiment
    private Double classify_cat(final T category, final Collection<K> vector) {
        //log is used to not multiply small close to 0 numbers, instead sum is used
        //laplacian smooth is used
        //multinomial
        Double s = Math.log(prior_probability.get(category) / (double) countOfDocuments) +
                vector.stream().unordered().mapToDouble(value -> Math.log((likelihood.getOrDefault(new Pair(value, category), 0) + 1) / (double) (counts.get(category) + vocabulary.size())))
                        .sum();
        return s;
    }


    /**
     * computes teh sentiment for text
     *
     * @param str text
     * @return sentiment
     */
    public T sentiment(Collection<K> vector) {
        Map<T, Double> collect = this.category.stream().unordered().collect(Collectors.toMap((T s) -> s, (T o) -> classify_cat(o, vector)));
        System.out.println(collect);
        return collect.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
    }

    public static void main(final String[] args) throws IOException {
        JSONProcessor.Train[] arr = null;
        Processing<String, String> processing = new Processing<>();

        try (InputStream in = Processing.class.getResourceAsStream("/train1.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (JSONProcessor.Train a : arr) {
            processing.train(a.getSentiment(), Arrays.asList(Processing.parse(a.getText(), 1)));
        }

        try (InputStream in = Processing.class.getResourceAsStream("/train2.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;

        for (JSONProcessor.Train a : arr) {
            String sentiment = processing.sentiment(Arrays.asList(Processing.parse(a.getText(), 1)));
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