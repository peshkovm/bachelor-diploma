package ru.eltech;

public class Schema implements java.io.Serializable {
    private String company;
    private String sentiment;
    private
    private double today_stock;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Schema{" +
                "company='" + company + '\'' +
                ", sentiment='" + sentiment + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", today_stock=" + today_stock +
                '}';
    }

    public double getToday_stock() {
        return today_stock;
    }

    public void setToday_stock(double today_stock) {
        this.today_stock = today_stock;
    }
}