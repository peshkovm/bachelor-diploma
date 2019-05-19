package ru.eltech.dapeshkov.classifier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.eltech.dapeshkov.news.Connection;
import ru.eltech.dapeshkov.news.Item;
import ru.eltech.dapeshkov.news.JSONProcessor;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.newBufferedWriter;

public class Main {
    static void write(final String str, final OutputStream out) {
        try (final PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(out)), true)) {
            writer.println(str);
        }
    }

    public static InputStream req(String str, String str1) throws IOException {
        URL url = new URL("http://eurekaengine.ru/ru/apiobject/lingvo/");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        Map<String, String> arguments = new HashMap<>();
        arguments.put("text", str);
        arguments.put("ot", str1); // This is a fake password obviously
        arguments.put("otSearchMode", "1");
        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> entry : arguments.entrySet())
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                    + URLEncoder.encode(entry.getValue(), "UTF-8"));
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        InputStream inputStream = http.getInputStream();
        http.disconnect();
        return inputStream;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        String a = "Amazon";
        Connection connection = new Connection("https://www.rbc.ru/search/ajax/?limit=5000&tag=" + a);
        final JSONProcessor.News news = JSONProcessor.parse(connection.get(), JSONProcessor.News.class);

        Map<LocalDate, Double> collect = Files.lines(Paths.get("NewsAndSentiment/src/test/resources/allStockData/allStockData" + "_" + a.toLowerCase() + ".txt")).collect(Collectors.toMap((String s) -> LocalDate.parse(s.split(",")[1]), s -> Double.valueOf(s.split(",")[2])));
        Comparator<Map.Entry<LocalDate, Double>> entryComparator = (Map.Entry<LocalDate, Double> b, Map.Entry<LocalDate, Double> v) -> b.getKey().compareTo(v.getKey());
        entryComparator = entryComparator.reversed();
        int l = 0;
        for (JSONProcessor.Item i : news.getItems()) {
            A parse = JSONProcessor.parse(req(i.getAnons(), a), A.class);
            String s;
            if (parse.tonality.frt.pos > parse.tonality.frt.neg) {
                s = "positive";
            } else if (parse.tonality.frt.neg > parse.tonality.frt.pos) {
                s = "negative";
            } else {
                s = "neutral";
            }
            final Item item = new Item(a, s, Timestamp.valueOf(i.getPublish_date()), collect.entrySet().stream().sorted(entryComparator).filter(x -> !x.getKey().isAfter(i.getPublish_date().toLocalDate())).findFirst().get().getValue());
            write(item.toString(), new FileOutputStream("NewsAndSentiment/src/test/resources/files/" + a.toLowerCase() + "/" + l++ + ".txt"));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class A {
        Tonality tonality;

        public Tonality getTonality() {
            return tonality;
        }

        public void setTonality(Tonality tonality) {
            this.tonality = tonality;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Tonality {
            String text;
            Frt frt;

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public Frt getFrt() {
                return frt;
            }

            public void setFrt(Frt frt) {
                this.frt = frt;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            static class Frt {
                public double getPos() {
                    return pos;
                }

                public void setPos(double pos) {
                    this.pos = pos;
                }

                public double getNeg() {
                    return neg;
                }

                public void setNeg(double neg) {
                    this.neg = neg;
                }

                double pos;
                double neg;
            }
        }
    }
}
