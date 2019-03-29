package ru.eltech;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class Streaming {

    public static void main(String[] args) {
        start();
    }

    public static void start() {

        System.setProperty("hadoop.home.dir", "C:\\winutils\\");

        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        JavaDStream<String> stringJavaDStream = jssc.textFileStream("files/");

        stringJavaDStream.foreachRDD((rdd) -> { //driver
            SparkSession spark = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();
            rdd.collect().forEach(System.out::println); //driver
        });

        jssc.start();

        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final StructType schema = new StructType(new StructField[]{
            new StructField("company", DataTypes.StringType, false, Metadata.empty()),
            new StructField("sentiment", DataTypes.StringType, false, Metadata.empty()),
            new StructField("year", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("month", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("day", DataTypes.IntegerType, false, Metadata.empty()),
            new StructField("today_stock", DataTypes.DoubleType, false, Metadata.empty()),
    });
}