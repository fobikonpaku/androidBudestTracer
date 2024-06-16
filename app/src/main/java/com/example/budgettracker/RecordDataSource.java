package com.example.budgettracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecordDataSource {

    private SQLiteDatabase database;
    private RecordsDbHelper dbHelper;
    private String[] allColumns = {
            RecordsDbHelper.COLUMN_ID,
            RecordsDbHelper.COLUMN_TYPE,
            RecordsDbHelper.COLUMN_AMOUNT,
            RecordsDbHelper.COLUMN_DESCRIPTION,
            RecordsDbHelper.COLUMN_DATE
    };

    public RecordDataSource(Context context) {
        dbHelper = new RecordsDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Record addRecord(Record record) {
        ContentValues values = new ContentValues();
        values.put(RecordsDbHelper.COLUMN_TYPE, record.getType());
        values.put(RecordsDbHelper.COLUMN_AMOUNT, record.getAmount());
        values.put(RecordsDbHelper.COLUMN_DESCRIPTION, record.getDescription());
        values.put(RecordsDbHelper.COLUMN_DATE, record.getDate());

        long insertId = database.insert(RecordsDbHelper.TABLE_RECORDS, null, values);
        record.setId(insertId);
        return record;
    }

    public void deleteRecord(Record record) {
        long id = record.getId();
        database.delete(RecordsDbHelper.TABLE_RECORDS, RecordsDbHelper.COLUMN_ID + " = " + id, null);
    }

    public List<Record> getAllRecords() {
        List<Record> records = new ArrayList<>();

        Cursor cursor = database.query(RecordsDbHelper.TABLE_RECORDS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Record record = cursorToRecord(cursor);
            records.add(record);
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    private Record cursorToRecord(Cursor cursor) {
        @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(RecordsDbHelper.COLUMN_ID));
        @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(RecordsDbHelper.COLUMN_TYPE));
        @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(RecordsDbHelper.COLUMN_AMOUNT));
        @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(RecordsDbHelper.COLUMN_DESCRIPTION));
        @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(RecordsDbHelper.COLUMN_DATE));

        return new Record(id, type, amount, description, date);

    }
}
