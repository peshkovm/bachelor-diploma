package ru.eltech.mapeshkov.batch;

import org.jfree.data.xy.XYDataItem;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.transform.squarifier.XZSquarifier;
import ru.eltech.mapeshkov.plot.CombinedChart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PlotLinearRegression {
    public static void main(String[] args) throws Exception {
        HashMap<String, List<Record>> records = downloadPlotData();
        records = reformatNotLabeledDataToLabeled(records);

        //AnalysisLauncher.open(new PlotAllData(records));
        AllDataChartLR(records);
    }

    private static void AllDataChartLR(HashMap<String, List<Record>> records) {
        CombinedChart[] charts = new CombinedChart[records.size()];
        final int[] i = {0};
        final int[] num = {0};

        records.entrySet().forEach(entry -> {
            String company = entry.getKey();
            charts[i[0]] = new CombinedChart("allData " + company, "stock", "label", "negative", "neutral", "positive"/*, "lr_0", "lr_2"*/);

            entry.getValue().forEach(record -> {
                String sentiment = "";

                sentiment = record.getSentiment() == 0 ? "negative" : record.getSentiment() == 1 ? "neutral" : "positive";

                if (num[0] >= 19 && num[0] <= 24)
                    charts[i[0]].addPoint(new XYDataItem(record.getToday_stock(), record.getLabel()), "negative");
                num[0]++;
            });
            i[0]++;
        });

        /*for (int j = 0; j < 2; j++) {
            String company = "";

            if (j == 0) company = "apple";
            else if (j == 1) company = "google";

            for (int k = 0; k < 500; k++) {
                if (company.equals("apple")) {
                    charts[j].addPoint(new XYDataItem(k, 0.18106569277017753 - 0.14933534763783274 * 0 + 0.9998650119644793 * k), "lr_0");

                    charts[j].addPoint(new XYDataItem(k, 0.18106569277017753 - 0.14933534763783274 * 1 + 0.9998650119644793 * k), "lr_2");
                } else if (company.equals("google")) {
                    charts[j].addPoint(new XYDataItem(500 + k, 3.205086797295632 + 0.0 * 0 + 0.996737793793539 * (500 + k)), "lr_0");

                    charts[j].addPoint(new XYDataItem(500 + k, 3.205086797295632 + 0.0 * 1 + 0.996737793793539 * (500 + k)), "lr_2");
                }

            }
        }*/

/*        for (CombinedChart chart : charts) {
            chart.saveChartAsJPEG(Paths.get("C:\\Users\\Денис\\Desktop\\ВКР\\images\\lr" + chart.getTitle() + ".jpg"), 700, 500);
        }*/
    }

    private static class SurfaceDemo extends AbstractAnalysis {
        public static void main(String[] args) throws Exception {
            AnalysisLauncher.open(new SurfaceDemo());
        }

        @Override
        public void init() {
            // Define a function to plot
            Mapper mapper = new Mapper() {
                @Override
                public double f(double x, double y) {
                    return 5 * x + 10 * y;
                }
            };

            // Define range and precision for the function to plot
            Range range = new Range(-3, 3);
            int steps = 80;

            // Create the object to represent the function over the given range.
            final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
            surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
            surface.setFaceDisplayed(true);
            surface.setWireframeDisplayed(false);

            // Create a chart
            chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
            chart.getScene().getGraph().add(surface);
        }
    }

    private static class SquarifyDemo extends AbstractAnalysis {
        public static void main(String[] args) throws Exception {
            AnalysisLauncher.open(new SquarifyDemo());
        }

        @Override
        public void init() {
            // Define a function to plot
            Mapper mapper = new Mapper() {
                @Override
                public double f(double x, double y) {
                    return x * Math.sin(x * y) * 10;
                }
            };

            // Define range and precision for the function to plot
            Range range = new Range(-2.5f, 2.5f);
            int steps = 80;
            Range yrange = new Range(-5, 5);

            // Create the object to represent the function over the given range.
            final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, yrange, steps), mapper);
            surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
            surface.setFaceDisplayed(true);
            surface.setWireframeDisplayed(false);

            // Create a chart
            chart = AWTChartComponentFactory.chart(Quality.Intermediate, getCanvasType());

            //This addition keeps the aspect ratio of the X and Y data
            //but makes X and Z square
            chart.getView().setSquarifier(new XZSquarifier());
            chart.getView().setSquared(true);
            chart.getScene().getGraph().add(surface);
        }
    }

    private static class ScatterDemo extends AbstractAnalysis {
        private HashMap<String, List<Record>> records;

        ScatterDemo(HashMap<String, List<Record>> records) {
            this.records = records;
        }

        @Override
        public void init() {

            records.keySet().stream().skip(1).forEach(key -> {
                List<Record> values = this.records.get(key);
                int size = values.size();
                Coord3d[] points = new Coord3d[size];
                Color[] colors = new Color[size];
                String companyName = "apple";
                for (int i = 0; i < size; i++) {
                    Random r = new Random();
                    double x = 0;
                    if (this.records.get(companyName).get(i).getSentiment() == 0) {
                        x = this.records.get(companyName).get(i).getSentiment() + 0;
                        colors[i] = new Color(255, 15, 15, 255);
                    } else if (this.records.get(companyName).get(i).getSentiment() == 1) {
                        x = this.records.get(companyName).get(i).getSentiment() + 100;
                        colors[i] = new Color(7, 222, 45, 255);
                    } else if (this.records.get(companyName).get(i).getSentiment() == 2) {
                        x = this.records.get(companyName).get(i).getSentiment() + 200;
                        colors[i] = new Color(7, 13, 222, 255);
                    }
                    double y = this.records.get(companyName).get(i).getToday_stock();
                    double z = this.records.get(companyName).get(i).getLabel() / 2;
                    points[i] = new Coord3d(x, y, z);
                }

                Scatter scatter = new Scatter(points, colors);
                scatter.setWidth(2);
                chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
                chart.getView().setSquared(false);
                chart.getView().getCamera().setUseSquaredDistance(false);
                chart.getView().setMaximized(false);
                chart.getScene().add(scatter);

                ///////////////////////////////////////
                // Define a function to plot
                Mapper mapper = new Mapper() {
                    @Override
                    public double f(double x, double y) {
                        return 1 * x + 1 * y;
                    }
                };

                // Define range and precision for the function to plot
                Range xRange = new Range(0, 3);
                Range yRange = new Range(0, 1500);
                int steps = 5;

                // Create the object to represent the function over the given range.
                final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, steps, yRange, steps), mapper);
                surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
                surface.setFaceDisplayed(true);
                surface.setWireframeDisplayed(false);

                // Create a chart
                //chart.getScene().add(surface);
            });
        }
    }

    private static HashMap<String, List<Record>> downloadPlotData() throws IOException {
        String dirName = "C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\testing batch\\allStockDataWithSentimentInitial";
        HashMap<String, List<Record>> allDataList = new HashMap<>();

        Files.list(Paths.get(dirName))
                .filter(path -> path.toFile().isDirectory())
                .forEach(companyDir -> {
                    allDataList.put(companyDir.getFileName().toString(), new ArrayList<>());
                    try {
                        Files.list(companyDir)
                                .filter(path -> path.toFile().isFile())
                                .sorted((file1, file2) -> {
                                    int fileNum1 = Integer.parseInt(file1.getFileName().toString().replaceAll(".txt", ""));
                                    int fileNum2 = Integer.parseInt(file2.getFileName().toString().replaceAll(".txt", ""));

                                    return fileNum2 - fileNum1;
                                })
                                //.limit(5)
                                .forEach(file -> {
                                    try {
                                        String str = new String(Files.readAllBytes(file)).replaceAll("(\r\n|\n)", "");

                                        String[] splitStr = str.split(",");
                                        String companyStr = splitStr[0];
                                        String sentimentStr = splitStr[1];
                                        String dateStr = splitStr[2];
                                        String stockStr = splitStr[3];

                                        int fileNum1 = Integer.parseInt(file.getFileName().toString().replaceAll(".txt", ""));

                                        /*double lrX = -0.2768920190391070 * sentiment - 0.0081697151793756 * stock + 0.2053354157219950
                                         * sentiment + 0.0068675164531169 * stock - 0.2282455117947890 * sentiment + 1.0012732499680500
                                         * stock + 0.2383632781770730;*/

                                        allDataList.get(companyDir.getFileName().toString()).add(new Record(companyStr, sentimentStr, dateStr, stockStr, "-1"));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return allDataList;
    }

    private static HashMap<String, List<Record>> reformatNotLabeledDataToLabeled(final HashMap<String, List<Record>> recordsHashMap) {
        final HashMap<String, List<Record>> copyRecordsHashMap = new HashMap<>();

        recordsHashMap.entrySet().forEach(entry -> {
            copyRecordsHashMap.put(entry.getKey(), new ArrayList<>());
            List<Record> values = entry.getValue();

            for (int valueNum = 0; valueNum < values.size() - 1; valueNum++) {
                Record recordToday = values.get(valueNum);
                Record recordTomorrow = values.get(valueNum + 1);

                recordToday.setLabel(String.valueOf(recordTomorrow.getToday_stock()));
                copyRecordsHashMap.get(entry.getKey()).add(recordToday);
            }
        });

        return copyRecordsHashMap;
    }

    private static class Record {
        private String company;
        private int sentiment;
        private String date;
        private double today_stock;
        private double label;

        public Record(String company, String sentiment, String date, String today_stock, String label) {
            setCompany(company);
            setSentiment(sentiment);
            setDate(date);
            setToday_stock(today_stock);
            setLabel(label);
        }

        public String getCompany() {
            return company;
        }

        public int getSentiment() {
            return sentiment;
        }

        public String getDate() {
            return date;
        }

        public double getToday_stock() {
            return today_stock;
        }

        public double getLabel() {
            return label;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public void setSentiment(String sentiment) {
            if (sentiment.equals("negative"))
                this.sentiment = 0;
            else if (sentiment.equals("neutral"))
                this.sentiment = 1;
            else if (sentiment.equals("positive"))
                this.sentiment = 2;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setToday_stock(String today_stock) {
            this.today_stock = Double.parseDouble(today_stock);
        }

        public void setLabel(String label) {
            this.label = Double.parseDouble(label);
        }
    }
}