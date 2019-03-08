package ru.eltech.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class Main {
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "ru.eltech.spark.C:\\winutils\\");

        SparkConf conf = new SparkConf().setAppName("spark").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        while (true) {
            JavaRDD<String> distFile = sc.textFile("files");
            JavaRDD<Integer> lineLengths = distFile.map(String::length);
            int totalLength = lineLengths.reduce((a, b) -> a + b);

            System.out.println(totalLength);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}