package ru.eltech.dapeshkov.speed_layer;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Item implements Serializable {
    public static final long serialVersionUID = 0L;
    private String sentiment;
    private String company_name;
    private double stock;
    private Timestamp dateTime;

    public Item(String company_name, String sentiment, Timestamp dateTime, double stock) {
        this.sentiment = sentiment;
        this.company_name = company_name;
        this.stock = stock;
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return getCompany_name() + "," + getSentiment() + "," + getDateTime() + "," + getStock();
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }
}