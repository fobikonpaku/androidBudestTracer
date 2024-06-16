package com.example.budgettracker;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecordDataSource {
    private static final String PREFS_NAME = "records";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public RecordDataSource(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void addRecord(Record record) {
        Gson gson = new Gson();
        String json = gson.toJson(record);
        editor.putString(String.valueOf(record.getId()), json);
        editor.apply();
    }

    public void deleteRecord(long id) {
        editor.remove(String.valueOf(id));
        editor.apply();
    }

    public List<Record> getAllRecords() {
        List<Record> records = new ArrayList<>();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        Gson gson = new Gson();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String json = entry.getValue().toString();
            Record record = gson.fromJson(json, Record.class);
            records.add(record);
        }
        return records;
    }
}
