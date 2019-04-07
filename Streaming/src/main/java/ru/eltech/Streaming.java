package ru.eltech;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.VoidFunction2;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.count;

public class Streaming {

    public static void main(String[] args) {
        startRDD();
    }

    public static void startRDD() {

        System.setProperty("hadoop.home.dir", "C:\\winutils\\");

        SparkConf conf = new SparkConf().setMaster("local[4]").setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
        jssc.sparkContext().setLogLevel("ERROR");

        JavaDStream<String> stringJavaDStream = jssc.textFileStream("files/");

        ArrayBlockingQueue<Schema> arrayBlockingQueue = new ArrayBlockingQueue<>(5);

        JavaDStream<Schema> schemaJavaDStream = stringJavaDStream.map(str -> {
            String[] split = str.split(",");
            Schema record = new Schema();
            record.setCompany(split[0]);
            record.setSentiment(split[1]);
            record.setYear(Integer.valueOf(split[2]));
            record.setMonth(Integer.valueOf(split[3]));
            record.setDay(Integer.valueOf(split[4]));
            record.setToday_stock(Double.valueOf(split[5]));
            return record;
        });

        schemaJavaDStream.foreachRDD(rdd -> { //driver
            SparkSession spark = SparkSession.builder().config(rdd.context().getConf()).getOrCreate();

            rdd.sortBy().collect().forEach(str -> { //driver
                System.out.println(str);
                //ArrayBlockingQueue<Schema> arrayBlockingQueue = ArrayBlockingQueueSinglton.getHelper();
                /*String[] split = str.split(",");
                Schema record = new Schema();
                record.setCompany(split[0]);
                record.setSentiment(split[1]);
                record.setYear(Integer.valueOf(split[2]));
                record.setMonth(Integer.valueOf(split[3]));
                record.setDay(Integer.valueOf(split[4]));
                record.setToday_stock(Double.valueOf(split[5]));

                try {
                    arrayBlockingQueue.put(record);
                    if (arrayBlockingQueue.size() == 5) {
                        Dataset<Row> dataFrame = spark.createDataFrame(new ArrayList<Schema>(arrayBlockingQueue), Schema.class);
                        dataFrame.show();
                        arrayBlockingQueue.take();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/
            });
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
        spark.sparkContext().setLogLevel("ERROR");
        spark.sparkContext().conf().set("spark.sql.shuffle.partitions", "1");

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
                .csv("files/")
                .toDF("company", "sentiment", "year", "month", "day", "today_stock", "id");

        Dataset<Row> windowedDataset = rowDataset.withWatermark("id", "0 seconds").groupBy(functions.window(col("id"), "5 minutes", "1 minute"),
                col("company"),
                col("sentiment"),
                col("year"),
                col("month"),
                col("day"),
                col("today_stock"),
                col("id")).count();

        StreamingQuery query = windowedDataset
                .writeStream()
                .foreachBatch((VoidFunction2<Dataset<Row>, Long>) (v1, v2) -> {
                    if (v1.count() == 5) {
                        PipelineModel model = ModelSingleton.getModel("models/");
                    }
                })
                .format("console")
                .start();

        try {
            query.awaitTermination();
        } catch (StreamingQueryException e) {
            e.printStackTrace();
        }
    }
}