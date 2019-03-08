package ru.eltech.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class B {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "ru.eltech.spark.C:\\winutils\\");

        SparkConf conf = new SparkConf().setMaster("local").setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
        JavaDStream<String> lines = jssc.textFileStream("files");

        lines.foreachRDD(length -> {
            if (!length.isEmpty())
                System.out.println(length.first().length());
            else
                System.out.println("empty");
        });

        //lineLengths.print();

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}