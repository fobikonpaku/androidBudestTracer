package com.example.budgettracker;
import lombok.Data;

//lombok实现getter和setter
//@Data
public class Record {
    private long id;
    private String type;
    private double amount;
    private String description;
    private long date;
    private int iconResId;

    public Record(String type, double amount, String description, long date) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public Record(long id, String type, double amount, String description, long date,int iconResId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.iconResId = iconResId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}
