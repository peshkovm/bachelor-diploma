package ru.eltech.dapeshkov.classifier;

import ru.eltech.dapeshkov.news.Connection;
import ru.eltech.dapeshkov.news.Item;
import ru.eltech.dapeshkov.news.JSONProcessor;
import ru.eltech.mapeshkov.stock.ApiUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class M {
    static void write(final String str, final OutputStream out) {
        try (final PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(out)), true)) {
            writer.println(str);
        }
    }

    public static void main(String[] args) throws IOException {
        Processing<String, String> processing = new Processing<>();
        JSONProcessor.Train[] arr = null;
        try (InputStream in = Processing.class.getResourceAsStream("/train.json")) {
            arr = JSONProcessor.parse(in, JSONProcessor.Train[].class);
            arr = Arrays.copyOfRange(arr, 0, arr.length - 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (JSONProcessor.Train a : arr) {
            String[] str = Processing.parse(a.getText(), 1);
            if (str != null) {
                processing.train(a.getSentiment(), Arrays.asList(str));
            }
        }
        String a = "Sberbank";
        Connection connection = new Connection("https://www.rbc.ru/search/ajax/?limit=5000&tag=" + a);
        final JSONProcessor.News news = JSONProcessor.parse(connection.get(), JSONProcessor.News.class);
        Map<LocalDate, Double> collect = Files.lines(Paths.get("NewsAndSentiment/src/test/resources/allStockData/allStockData" + "_" + a.toLowerCase() + ".txt")).collect(Collectors.toMap((String s) -> LocalDate.parse(s.split(",")[1]), s -> Double.valueOf(s.split(",")[2])));
        Comparator<Map.Entry<LocalDate, Double>> entryComparator = (Map.Entry<LocalDate, Double> b, Map.Entry<LocalDate, Double> v) -> b.getKey().compareTo(v.getKey());
        entryComparator = entryComparator.reversed();
        int l = 0;
        for (JSONProcessor.Item i : news.getItems()) {
            final Item item = new Item(a, processing.sentiment(Arrays.asList(Processing.parse(i.getAnons(), 1))), Timestamp.valueOf(i.getPublish_date()), collect.entrySet().stream().sorted(entryComparator).filter(x -> !x.getKey().isAfter(i.getPublish_date().toLocalDate())).findFirst().get().getValue());
            write(item.toString(), new FileOutputStream("NewsAndSentiment/src/test/resources/files/" + a.toLowerCase() + "/" + l++ + ".txt"));
        }
    }
}
