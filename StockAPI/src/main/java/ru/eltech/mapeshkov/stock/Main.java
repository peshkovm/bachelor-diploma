package ru.eltech.mapeshkov.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.eltech.mapeshkov.stock.beans.StockInfo;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            StockInfo stockInfo = ApiUtils.AlphaVantageParser.getLatestStock("apple");
            System.out.println(stockInfo);
        }
    }
}