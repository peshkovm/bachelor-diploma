package ru.eltech;

import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.spark.SparkConf;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import ru.eltech.Singltones.ArrayBlockingQueueSinglton;
import ru.eltech.Singltones.WatcherSingltone;
import ru.eltech.mapeshkov.spark.PredictionUtils;

import java.util.concurrent.ArrayBlockingQueue;

public class Streaming {

    public static void main(String[] args) {
        startRDD();
    }

    public static void startRDD() {

        System.setProperty("hadoop.home.dir", "C:\\winutils\\");

        SparkConf conf = new SparkConf().setMaster("local[1]").setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        JavaDStream<String> stringJavaDStream = jssc.textFileStream("files/");

        stringJavaDStream.foreachRDD(rdd -> { //driver
            SparkSession spark = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();

            rdd.foreach(str -> {
                ArrayBlockingQueue<Schema> queue = ArrayBlockingQueueSinglton.getHelper();
                String[] split = str.split(",");
                Schema record = new Schema();
                record.setCompany(split[0]);
                record.setSentiment(split[1]);
                record.setYear(Integer.valueOf(split[2]));
                record.setMonth(Integer.valueOf(split[3]));
                record.setDay(Integer.valueOf(split[4]));
                record.setToday_stock(Double.valueOf(split[5]));

                queue.put(record);

                if (queue.size() == 5) {
                    Watcher watcher = WatcherSingltone.getHelper();
                    PipelineModel model = null;
                    if (watcher.check()) {
                        model = PipelineModel.load("model/model");
                    }

                    if (model != null) {
                    }
                    queue.take(); //TODO latest element
                }
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
}