package com.example.budgettracker;
import lombok.Data;

//lombok实现getter和setter
@Data
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
}
