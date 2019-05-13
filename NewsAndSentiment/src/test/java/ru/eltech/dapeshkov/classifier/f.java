package ru.eltech.dapeshkov.classifier;

import ru.eltech.dapeshkov.news.JSONProcessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.newBufferedWriter;

public class f {
    public static void main(String[] args) throws IOException {
        JSONProcessor.Train[] arr = null;

        try (InputStream in = Processing.class.getResourceAsStream("/train (1).json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONProcessor.Train[] train = Arrays.copyOfRange(arr, 0, 4890);
        JSONProcessor.Train[] test = Arrays.copyOfRange(arr, 4890, 7290);
        JSONProcessor.Train[] neg = Arrays.stream(train).filter(s -> s.getSentiment().equals("negative")).limit(800).toArray(JSONProcessor.Train[]::new);
        JSONProcessor.Train[] pos = Arrays.stream(train).filter(s -> s.getSentiment().equals("positive")).limit(800).toArray(JSONProcessor.Train[]::new);
        JSONProcessor.Train[] neu = Arrays.stream(train).filter(s -> s.getSentiment().equals("neutral")).limit(800).toArray(JSONProcessor.Train[]::new);
        JSONProcessor.Train[] trains = Stream.concat(Arrays.stream(neg), Stream.concat(Arrays.stream(pos), Arrays.stream(neu))).toArray(JSONProcessor.Train[]::new);

        String write = JSONProcessor.write(trains);
        BufferedWriter bufferedWriter = newBufferedWriter(Paths.get("train.json"), StandardOpenOption.CREATE);
        bufferedWriter.write(write);
        bufferedWriter.close();
        write = JSONProcessor.write(test);
        bufferedWriter = newBufferedWriter(Paths.get("test.json"), StandardOpenOption.CREATE);
        bufferedWriter.write(write);
        bufferedWriter.close();
    }
}
