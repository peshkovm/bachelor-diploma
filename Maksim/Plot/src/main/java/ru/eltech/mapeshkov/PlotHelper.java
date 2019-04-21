package ru.eltech.mapeshkov;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PlotHelper {
    final private CombinedPlot plot;
    final private String fileName;
    private long numOfNews = 1;

    public PlotHelper(final String fileName) throws IOException {
        this.fileName = fileName;
        plot = new CombinedPlot("news/stock plot", "real stock", "prediction stock");

        refresh();
    }

    public void refresh() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(this.fileName))) {

            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                double realStock = Double.parseDouble(split[0]);
                double predictionStock = Double.parseDouble(split[1]);

                plot.addPoint(new XYDataItem(numOfNews, realStock), "real stock");
                plot.addPoint(new XYDataItem(++numOfNews, predictionStock), "prediction stock");
            }
        }
    }
}