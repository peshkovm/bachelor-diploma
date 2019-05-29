package ru.eltech.mapeshkov.batch;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.jfree.data.xy.XYDataItem;
import ru.eltech.mapeshkov.mlib.MyEvaluator;
import ru.eltech.mapeshkov.mlib.MyFileWriter;
import ru.eltech.mapeshkov.mlib.PredictionUtils;
import ru.eltech.mapeshkov.mlib.Schemes;
import ru.eltech.mapeshkov.mlib.in_data_refactor_utils.InDataRefactorUtils;
import ru.eltech.mapeshkov.plot.CombinedChart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestBatch {
    private static final int numOfTestFiles = 30;

    public static void main(String[] args) throws Exception {
        int bestWindowWidth = windowWidthPlot();
        //System.in.read();
        withAndWithoutSentimentPlot(bestWindowWidth);
    }

    private static int windowWidthPlot() throws Exception {
        final int[] windowWidthMas = {2, 3, 4};
        final String outDirPath = "C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\testing batch\\windowWidthPlot";

        RefactorInFiles.generateTestAndPredictFiles(outDirPath, windowWidthMas[0], numOfTestFiles);

        final List<Path> companyNameList = Files.list(Paths.get(outDirPath + "\\testing batch in files for prediction"))
                .filter(path -> path.toFile().isDirectory())
                .collect(Collectors.toList());
        final Comparable<?>[] keys = new Comparable<?>[windowWidthMas.length];
        final HashMap<Integer, String> keysMap = new HashMap<>();
        final HashMap<String, CombinedChart> plotMap = new HashMap<>();
        final HashMap<Integer, List<Double>> errorMap = new HashMap<>();

        for (int i = 0; i < windowWidthMas.length; i++) {
            keys[i] = "error with window width=" + windowWidthMas[i];
            keysMap.put(windowWidthMas[i], "error with window width=" + windowWidthMas[i]);
        }
        for (int i = 0; i < companyNameList.size(); i++) {
            plotMap.put(companyNameList.get(i).getFileName().toString(), new CombinedChart("company=" + companyNameList.get(i).getFileName().toString(), "num of test", "error", 400, 300, keys));
        }
        for (int i = 0; i < windowWidthMas.length; i++) {
            errorMap.put(windowWidthMas[i], new ArrayList<>());
        }

/*        plotMap.get(companyNameList.get(0).getFileName().toString()).addPoint(new XYDataItem(5, 5), keysMap.get(3));
        plotMap.get(companyNameList.get(0).getFileName().toString()).addPoint(new XYDataItem(6, 6), keysMap.get(4));
        plotMap.get(companyNameList.get(0).getFileName().toString()).addPoint(new XYDataItem(7, 7), keysMap.get(5));*/
        final MyFileWriter resultsOfTestingLog = new MyFileWriter(Paths.get(outDirPath + "\\logFiles\\resultsOfTesting.txt"));

        for (int i = 0; i < windowWidthMas.length; i++) {
            int windowWidth = windowWidthMas[i];
            Schemes.SCHEMA_WINDOWED.setWindowWidth(windowWidth);
            System.out.println("For window width=" + windowWidth);
            System.out.println("schema window width=" + Schemes.SCHEMA_WINDOWED.getWindowWidth());
            if (i > 0) {
                RefactorInFiles.generateTestAndPredictFiles(outDirPath, windowWidth, numOfTestFiles);
            }

            resultsOfTestingLog.println("window width=" + windowWidth);
            long startExecTime = System.currentTimeMillis();

            Batch.start(outDirPath, "window width=" + windowWidth, true);

            Files.list(Paths.get(outDirPath + "\\testing batch in files for prediction"))
                    .filter(path -> path.toFile().isDirectory())
                    .forEach(companyDirPath -> {
                        MyFileWriter predictionLog = null;
                        try {

                            PipelineModel trainedModel = PipelineModel.load(outDirPath + "\\models\\" + companyDirPath.getFileName() + "\\window width=" + windowWidth);

                            SparkSession spark = SparkSession
                                    .builder()
                                    .appName("Batch layer")
                                    .config("spark.some.config.option", "some-value")
                                    .master("local[*]")
                                    .getOrCreate();

                            for (int testFileNum = 0; testFileNum < numOfTestFiles; testFileNum++) {
                                Dataset<Row> testingDatasetNotLabeled = spark.read()
                                        .schema(Schemes.SCHEMA_NOT_LABELED.getScheme())
                                        //.option("inferSchema", true)
                                        //.option("header", true)л
                                        .option("delimiter", ",")
                                        .option("charset", "UTF-8")
                                        //.csv("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\in files for prediction\\" + companyDirPath.getFileName())
                                        .csv(outDirPath + "\\testing batch test files for prediction\\" + companyDirPath.getFileName() + "\\test file for prediction" + testFileNum + ".txt")
                                        .toDF("company", "sentiment", "date", "today_stock");
                                //.cache();

                                predictionLog = new MyFileWriter(Paths.get(outDirPath + "\\logFiles\\" + companyDirPath.getFileName() + "\\window width=" + windowWidth + "\\predictionLog" + testFileNum + ".txt"));

                                predictionLog.show(testingDatasetNotLabeled);
                                Dataset<Row> testingDataNotLabeledSorted = InDataRefactorUtils.sortByDate(spark, testingDatasetNotLabeled, Schemes.SCHEMA_NOT_LABELED.getScheme());

                                predictionLog.show(testingDataNotLabeledSorted);

                                Dataset<Row> testingDataLabeled = InDataRefactorUtils.reformatNotLabeledDataToLabeled(spark, testingDataNotLabeledSorted, false);

                                predictionLog.show(testingDataLabeled);

                                Dataset<Row> testingDataWindowed = InDataRefactorUtils.reformatInDataToSlidingWindowLayout(spark, testingDataLabeled, windowWidth);

                                predictionLog.show(testingDataWindowed);

                                Dataset<Row> predictions = PredictionUtils.predict(trainedModel, testingDataWindowed, predictionLog);

                                MyEvaluator evaluator = new MyEvaluator();
                                double error = evaluator.evaluate(predictions);
                                errorMap.get(windowWidth).add(error);

                                System.out.println("keysMap.get(windowWidth)=" + keysMap.get(windowWidth));

                                plotMap.get(companyDirPath.getFileName().toString()).addPoint(new XYDataItem(testFileNum, error), keysMap.get(windowWidth));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            predictionLog.close();
                        }
                    });

            long endExecTime = System.currentTimeMillis();

            resultsOfTestingLog.println("execution time=" + (endExecTime - startExecTime) / 1000);
            resultsOfTestingLog.println();
        }

        errorMap.entrySet()
                .forEach(entry -> {
                    final int size = entry.getValue().size();
                    Optional<Double> reduce = entry.getValue().stream().reduce((error1, error2) -> (error1 + error2));
                    entry.setValue(new ArrayList<>(Collections.singletonList(reduce.get() / size)));
                });

        Arrays.stream(windowWidthMas).forEach(windowWidth -> {
            resultsOfTestingLog.println("window width=" + windowWidth);
            resultsOfTestingLog.println("average error=" + errorMap.get(windowWidth).get(0));
            resultsOfTestingLog.println();
        });

        resultsOfTestingLog.close();

        for (Path companyName : companyNameList)
            plotMap.get(companyName.getFileName().toString()).saveChartAsJPEG(Paths.get(outDirPath + "\\plotJPEGs_MSE\\" + companyName.getFileName() + "\\plot.jpg"), 1600, 800);

        Optional<Map.Entry<Integer, List<Double>>> mostAccurateEntry = errorMap.entrySet().stream().min(Comparator.comparingDouble(entry -> entry.getValue().get(0)));

        return mostAccurateEntry.get().getKey();
    }

    private static void withAndWithoutSentimentPlot(int bestWindowWidth) throws Exception {
        final int[] windowWidthMas = {bestWindowWidth};
        final boolean[] isWithSentimentMas = {true, false};
        final String outDirPath = "C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\testing batch\\withAndWithoutSentimentPlot";

        RefactorInFiles.generateTestAndPredictFiles(outDirPath, windowWidthMas[0], numOfTestFiles);

        final List<Path> companyNameList = Files.list(Paths.get(outDirPath + "\\testing batch in files for prediction"))
                .filter(path -> path.toFile().isDirectory())
                .collect(Collectors.toList());
        final Comparable<?>[] keys = new Comparable<?>[2];
        final HashMap<Boolean, String> keysMap = new HashMap<>();
        final HashMap<String, CombinedChart> plotMap = new HashMap<>();
        final HashMap<Boolean, List<Double>> errorMap = new HashMap<>();

        for (int i = 0; i < 2; i++) {
            keys[i] = "with sentiment=" + (i == 0);
            keysMap.put((i == 0), "with sentiment=" + (i == 0));
        }
        for (int i = 0; i < companyNameList.size(); i++) {
            plotMap.put(companyNameList.get(i).getFileName().toString(), new CombinedChart("company=" + companyNameList.get(i).getFileName().toString(), "num of test", "error", 400, 300, keys));
        }
        for (int i = 0; i < 2; i++) {
            errorMap.put((i == 0), new ArrayList<>());
        }


/*        plotMap.get(companyNameList.get(0).getFileName().toString()).addPoint(new XYDataItem(5, 5), keysMap.get(3));
        plotMap.get(companyNameList.get(0).getFileName().toString()).addPoint(new XYDataItem(6, 6), keysMap.get(4));
        plotMap.get(companyNameList.get(0).getFileName().toString()).addPoint(new XYDataItem(7, 7), keysMap.get(5));*/
        final MyFileWriter resultsOfTestingLog = new MyFileWriter(Paths.get(outDirPath + "\\logFiles\\resultsOfTesting.txt"));

        for (int i = 0; i < 2; i++) {
            int windowWidth = windowWidthMas[0];
            Schemes.SCHEMA_WINDOWED.setWindowWidth(windowWidth);
            System.out.println("For window width=" + windowWidth);
            System.out.println("schema window width=" + Schemes.SCHEMA_WINDOWED.getWindowWidth());
            if (i > 0) {
                RefactorInFiles.generateTestAndPredictFiles(outDirPath, windowWidth, numOfTestFiles);
            }

            resultsOfTestingLog.println("window width=" + windowWidth);
            long startExecTime = System.currentTimeMillis();
            final int finalI = i;

            Batch.start(outDirPath, "with sentiment=" + isWithSentimentMas[finalI], i == 0);

            Files.list(Paths.get(outDirPath + "\\testing batch in files for prediction"))
                    .filter(path -> path.toFile().isDirectory())
                    .forEach(companyDirPath -> {
                        MyFileWriter predictionLog = null;
                        try {

                            PipelineModel trainedModel = PipelineModel.load(outDirPath + "\\models\\" + companyDirPath.getFileName() + "\\with sentiment=" + (isWithSentimentMas[finalI]));

                            SparkSession spark = SparkSession
                                    .builder()
                                    .appName("Batch layer")
                                    .config("spark.some.config.option", "some-value")
                                    .master("local[*]")
                                    .getOrCreate();

                            for (int testFileNum = 0; testFileNum < numOfTestFiles; testFileNum++) {
                                Dataset<Row> testingDatasetNotLabeled = spark.read()
                                        .schema(Schemes.SCHEMA_NOT_LABELED.getScheme())
                                        //.option("inferSchema", true)
                                        //.option("header", true)
                                        .option("delimiter", ",")
                                        .option("charset", "UTF-8")
                                        //.csv("C:\\JavaLessons\\bachelor-diploma\\Batch\\src\\test\\resources\\in files for prediction\\" + companyDirPath.getFileName())
                                        .csv(outDirPath + "\\testing batch test files for prediction\\" + companyDirPath.getFileName() + "\\test file for prediction" + testFileNum + ".txt")
                                        .toDF("company", "sentiment", "date", "today_stock");
                                //.cache();

                                predictionLog = new MyFileWriter(Paths.get(outDirPath + "\\logFiles\\" + companyDirPath.getFileName() + "\\with sentiment=" + isWithSentimentMas[finalI] + "\\predictionLog" + testFileNum + ".txt"));

                                predictionLog.show(testingDatasetNotLabeled);
                                Dataset<Row> testingDataNotLabeledSorted = InDataRefactorUtils.sortByDate(spark, testingDatasetNotLabeled, Schemes.SCHEMA_NOT_LABELED.getScheme());

                                predictionLog.show(testingDataNotLabeledSorted);

                                Dataset<Row> testingDataLabeled = InDataRefactorUtils.reformatNotLabeledDataToLabeled(spark, testingDataNotLabeledSorted, false);

                                predictionLog.show(testingDataLabeled);

                                Dataset<Row> testingDataWindowed = InDataRefactorUtils.reformatInDataToSlidingWindowLayout(spark, testingDataLabeled, windowWidth);

                                predictionLog.show(testingDataWindowed);

                                Dataset<Row> predictions = PredictionUtils.predict(trainedModel, testingDataWindowed, predictionLog);

                                MyEvaluator evaluator = new MyEvaluator();
                                double error = evaluator.evaluate(predictions);
                                errorMap.get(isWithSentimentMas[finalI]).add(error);

                                System.out.println("keysMap.get((isWithSentimentMas[finalI])))=" + keysMap.get((isWithSentimentMas[finalI])));

                                plotMap.get(companyDirPath.getFileName().toString()).addPoint(new XYDataItem(testFileNum, error), keysMap.get((isWithSentimentMas[finalI])));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            predictionLog.close();
                        }
                    });

            long endExecTime = System.currentTimeMillis();

            resultsOfTestingLog.println("execution time=" + (endExecTime - startExecTime) / 1000);
            resultsOfTestingLog.println();

        }

        errorMap.entrySet()
                .forEach(entry -> {
                    final int size = entry.getValue().size();
                    Optional<Double> reduce = entry.getValue().stream().reduce((error1, error2) -> (error1 + error2));
                    entry.setValue(new ArrayList<>(Collections.singletonList(reduce.get() / size)));
                });

        Stream.of(true, false)
                .forEach(isWithSentiment -> {
                    resultsOfTestingLog.println("with sentiment=" + isWithSentiment);
                    resultsOfTestingLog.println("average error=" + errorMap.get(isWithSentiment).get(0));
                    resultsOfTestingLog.println();
                });

        resultsOfTestingLog.close();

        for (Path companyName : companyNameList)
            plotMap.get(companyName.getFileName().toString()).saveChartAsJPEG(Paths.get(outDirPath + "\\plotJPEGs_MSE\\" + companyName.getFileName() + "\\plot.jpg"), 1600, 1200);
    }
}