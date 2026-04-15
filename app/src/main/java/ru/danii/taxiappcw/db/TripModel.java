package ru.danii.taxiappcw.db;

public class TripModel {
    public String from;
    public String to;
    public String tariff;
    public String date;

    public TripModel(String from, String to, String tariff, String date) {
        this.from = from;
        this.to = to;
        this.tariff = tariff;
        this.date = date;
    }
}