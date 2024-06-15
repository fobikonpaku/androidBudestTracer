package com.example.budgettracker;
import lombok.Data;

//lombok实现getter和setter
//@Data
public class Record {
    private String type;
    private double amount;
    private String description;
    private long date;

    public Record(String type, double amount, String description, long date) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Getter and Setter for amount
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
