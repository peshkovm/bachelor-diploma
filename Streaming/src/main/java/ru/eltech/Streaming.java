package ru.eltech;

import org.apache.spark.SparkConf;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Streaming {

    public static void main(String[] args) {
        startStructured();
    }

    public static void startRDD() {

        System.setProperty("hadoop.home.dir", "C:\\winutils\\");

        SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        JavaDStream<String> stringJavaDStream = jssc.textFileStream("files/");

        JavaDStream<String> window = stringJavaDStream.window(Durations.milliseconds(1000));

        stringJavaDStream.foreachRDD(rdd -> { //driver
            SparkSession spark = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();

            rdd.foreach(str -> {
                Map<String, ArrayBlockingQueue<Schema>> map = HashMapSinglton.getHelper();
                String[] split = str.split(",");
                Schema record = new Schema();
                record.setCompany(split[0]);
                record.setSentiment(split[1]);
                record.setYear(Integer.valueOf(split[2]));
                record.setMonth(Integer.valueOf(split[3]));
                record.setDay(Integer.valueOf(split[4]));
                record.setToday_stock(Double.valueOf(split[5]));

                map.compute(record.getCompany(), (k, v) -> {
                    ArrayBlockingQueue<Schema> temp = null;
                    try {
                        temp = (v == null ? new ArrayBlockingQueue<Schema>(5) : v);
                        temp.put(record);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return temp;
                });

                map.entrySet().stream().filter((e) -> e.getValue().size() == 5).forEach(Streaming::getModel);
            });

            //Dataset<Row> dataFrame = spark.createDataFrame(rowRDD, Schema.class);
        });

        jssc.start();

        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void startStructured() {

        System.setProperty("hadoop.home.dir", "C:\\winutils\\");


        SparkSession spark = SparkSession.builder().master("local[2]").getOrCreate();

        StructType schema = new StructType(new StructField[]{
                new StructField("company", DataTypes.StringType, false, Metadata.empty()),
                new StructField("sentiment", DataTypes.StringType, false, Metadata.empty()),
                new StructField("year", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("month", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("day", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("today_stock", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("id", DataTypes.TimestampType, false, Metadata.empty()),
        });

        Dataset<Row> rowDataset = spark.readStream()
                .schema(schema)
                .option("delimiter", ",")
                .option("charset", "UTF-8")
                .option("includeTimestamp", true)
                .csv("files/")
                .toDF("company", "sentiment", "year", "month", "day", "today_stock", "id");

        Dataset<Row> windowedDataset = rowDataset.groupBy("today_stock").count();

        StreamingQuery query = windowedDataset.writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            query.awaitTermination();
        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }
    }

    static void getModel(Map.Entry<String, ArrayBlockingQueue<Schema>> entry) {
        Watcher watcher = null;
        try {
            watcher = WatcherFactory.get(Paths.get(entry.getKey()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PipelineModel model = null;
        if (watcher.check()) {
            model = PipelineModel.load("model/model");
        }

        try {
            entry.getValue().take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}