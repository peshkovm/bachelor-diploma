package ru.eltech.mapeshkov.batch;

import org.apache.commons.io.input.ReversedLinesFileReader;
import ru.eltech.mapeshkov.mlib.MyFileWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RefactorInFiles {
    public static void main(String[] args) throws Exception {
        generateTestAndPredictFiles("", 5, 20);
    }

    private static void refactorInFileToFolders() throws Exception {
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(new File("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources/in file for prediciton.csv"))) {
            try (MyFileWriter writer = new MyFileWriter(Paths.get("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources/reformat in file for prediction.txt"))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    String company = split[0];
                    String sentiment = split[1];
                    String year = split[2];
                    String month = split[3];
                    String day = split[4];
                    String today_stock = split[5];
                    String future_stock = split[6];

                    String newLine = company + "," + sentiment + "," + year + "-" + month + "-" + day + "," + today_stock + "," + future_stock;
                    writer.println(newLine);
                }
            }
        }
    }

    private static void generateTestDataForPrediction() throws Exception {
        List<String> fileContent = new ArrayList<>();
        int testFileLength = 20;
        int numOfTestFiles = 10;

        for (int testFileNum = 0; testFileNum < numOfTestFiles; testFileNum++) {
            try (BufferedReader reader = new BufferedReader(new FileReader("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\reformat in file for prediction.csv"))) {
                try (MyFileWriter writer = new MyFileWriter(Paths.get("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources/testing batch/testing batch test files for prediction/reformat test file for prediction" + testFileNum + ".txt"))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String[] split = line.split(",");
                        String company = split[0];
                        String sentiment = split[1];
                        String date = split[2];
                        String today_stock = split[3];
                        String future_stock = split[4];

                        String newLine = company + "," + sentiment + "," + date + "," + today_stock + "," + future_stock;
                        fileContent.add(newLine);
                    }

                    for (int i = 0; i < testFileLength; i++) {
                        Random random = new Random(System.currentTimeMillis());
                        int lineNum = random.nextInt(fileContent.size());

                        String randLine = fileContent.get(lineNum);
                        writer.println(randLine);
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                }
            }
        }
    }

    public static void generateTestAndPredictFiles(final String outPath, final int windowWidth, final int numOfTestFiles) throws Exception {
        Files.list(Paths.get("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\testing batch\\allStockDataWithSentimentInitial"))
                .filter(path -> path.toFile().isDirectory())
                .forEach(companyDir -> {
                    List<String> fileContent = null;
                    try {
                        fileContent = Files.list(companyDir)
                                .filter(path -> path.toFile().isFile())
                                .map(file -> {
                                    String line = "";
                                    try {
                                        line = new String(Files.readAllBytes(file)).replaceAll("(\r\n|\n)", "");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    return line;
                                })
                                .collect(Collectors.toList());

                        fileContent.sort((line1, line2) -> {
                            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

                            String[] split1 = line1.split(",");
                            String strDate1 = split1[2];
                            LocalDateTime date1 = LocalDateTime.parse(strDate1, formatter);

                            String[] split2 = line2.split(",");
                            String strDate2 = split2[2];
                            LocalDateTime date2 = LocalDateTime.parse(strDate2, formatter);

                            return date1.isBefore(date2) ? -1 : date1.isEqual(date2) ? 0 : 1;
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int lineNum = fileContent.size() - 20;

                    for (int testFileNum = 0; testFileNum < numOfTestFiles; testFileNum++) {
                        try (MyFileWriter testWriter = new MyFileWriter(Paths.get(outPath + "\\testing batch test files for prediction\\" + companyDir.getFileName() + "\\test file for prediction" + testFileNum + ".txt"))) {
                            //Random random = new Random(System.currentTimeMillis());
                            //int lineNum = random.nextInt(fileContent.size());

                            for (int i = 0; i < windowWidth + 1; i++) {
                                String randLine = fileContent.remove(lineNum - windowWidth);
                                testWriter.println(randLine);
                                TimeUnit.MILLISECONDS.sleep(10);
                            }
                            lineNum -= 30;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    try (MyFileWriter inWriter = new MyFileWriter(Paths.get(outPath + "\\testing batch in files for prediction\\" + companyDir.getFileName() + "\\in file for prediction.csv"))) {
                        fileContent.forEach(inWriter::println);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}