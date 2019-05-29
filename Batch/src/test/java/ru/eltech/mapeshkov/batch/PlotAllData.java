package ru.eltech.mapeshkov.batch;

import org.jfree.data.xy.XYDataItem;
import ru.eltech.mapeshkov.plot.CombinedChart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlotAllData {
    public static void main(String[] args) throws IOException {
        String dirName = "C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\testing batch\\allStockDataWithSentimentInitial";


        Files.list(Paths.get(dirName))
                .filter(path -> path.toFile().isDirectory())
                .forEach(companyDir -> {
                    final CombinedChart chart = new CombinedChart("Plot of all data. " + companyDir.getFileName(), "time", "stock price", "stock");
                    final int[] fileNum = {0};
                    try {
                        Files.list(companyDir)
                                .filter(path -> path.toFile().isFile())
                                .sorted((file1, file2) -> {
                                    int fileNum1 = Integer.parseInt(file1.getFileName().toString().replaceAll(".txt", ""));
                                    int fileNum2 = Integer.parseInt(file2.getFileName().toString().replaceAll(".txt", ""));

                                    return fileNum2 - fileNum1;
                                })
                                .forEach(file -> {
                                    try {
                                        String str = new String(Files.readAllBytes(file)).replaceAll("(\r\n|\n)", "");

                                        String[] split = str.split(",");
                                        String company = split[0];
                                        String sentiment = split[1];
                                        String date = split[2];
                                        String stock = split[3];

                                        int fileNum1 = Integer.parseInt(file.getFileName().toString().replaceAll(".txt", ""));
                                        double dStock = Double.parseDouble(stock);
                                        int dSentiment = 0;
                                        if (sentiment.equals("positive"))
                                            dSentiment = 1;
                                        else if (sentiment.equals("negative"))
                                            dSentiment = 0;

                                        double lrX = -0.2768920190391070 * dSentiment - 0.0081697151793756 * dStock + 0.2053354157219950
                                                * dSentiment + 0.0068675164531169 * dStock - 0.2282455117947890 * dSentiment + 1.0012732499680500
                                                * dStock + 0.2383632781770730;

                                        chart.addPoint(new XYDataItem(fileNum[0]++, Double.parseDouble(stock)), "stock");
                                        //chart.addPoint(new XYDataItem(fileNum[0]++, lrX), "lr");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    chart.saveChartAsJPEG(Paths.get("C:\\Users\\Денис\\Desktop\\ВКР\\images\\allData_" + companyDir.getFileName().toString() + "_Chart.jpg"), 700, 500);
                });
    }
}