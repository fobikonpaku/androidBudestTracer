package com.example.budgettracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends AppCompatActivity {
    // 添加记录列表类型
    static final Type RECORD_LIST_TYPE = new TypeToken<List<Record>>(){}.getType();
    private List<Record> recordList;
    private RecordAdapter recordAdapter;
    private TextView textTotalIncome, textTotalExpense, textTotalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

//        // 初始化FileManager
//        FileManager fileManager = new FileManager(this);
//        String filePath = fileManager.getFilePathAfterCopy(R.raw.chi_sim, "chi_sim.traineddata", "tessdata", true);
//        String tessDataPath = filePath.substring(0, filePath.length() - "chi_sim.traineddata".length());
//
//        // 初始化Tesseract
//        TessBaseAPI tessBaseAPI = new TessBaseAPI();
//        if (!tessBaseAPI.init(tessDataPath, "chi_sim")) {
//            Log.e("MainActivity", "Could not initialize Tesseract.");
//            tessBaseAPI.recycle();
//        }

//        recordList = new ArrayList<>();
        recordList = loadRecordsFromPreferences();
        recordAdapter = new RecordAdapter(recordList);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recordAdapter);
        // 设置灰白相间的背景色
        recyclerView.addItemDecoration(new backgroundcolor(this));

        textTotalIncome = findViewById(R.id.textTotalIncome);
        textTotalExpense = findViewById(R.id.textTotalExpense);
        textTotalBalance = findViewById(R.id.textTotalBalance);

        Button btnAddIncome = findViewById(R.id.btnAddIncome);
        Button btnAddExpense = findViewById(R.id.btnAddExpense);

        updateTotalValues();

        btnAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRecordDialog("Income");
            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示对话框以添加支出记录
                showAddRecordDialog("Expense");
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAddRecordDialog(final String type) {
        // 创建并显示对话框以输入记录详细信息
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_record, null);
        dialogBuilder.setView(dialogView);

        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);

        dialogBuilder.setTitle("添加记录");
        dialogBuilder.setPositiveButton("添加", (dialog, which) -> {
            String amountStr = editTextAmount.getText().toString();
            String description = editTextDescription.getText().toString();

            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                // 假设使用当前时间作为记录的时间戳
                long timestamp = System.currentTimeMillis();
                Record newRecord = new Record(type, amount, description, timestamp);
                recordList.add(newRecord);
                //根据type类型修改收入支出
                updateTotalValues();
                // 完成输入后添加记录并刷新列表
                recordAdapter.notifyDataSetChanged();
                //保存到本地
                saveRecordsToPreferences();
            } else {
                Toast.makeText(MainActivity.this, "金额不能为0", Toast.LENGTH_SHORT).show();
            }
        });
        dialogBuilder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        // recordList.add(new Record(type, 100.0, "Sample Description", System.currentTimeMillis()));
        // recordAdapter.notifyDataSetChanged();
    }

    private void updateTotalValues() {
        double totalIncome = 0.0;
        double totalExpense = 0.0;

        for (Record record : recordList) {
            if (record.getType().equals("Income")) {
                totalIncome += record.getAmount();
            } else if (record.getType().equals("Expense")) {
                totalExpense += record.getAmount();
            }
        }

        // 更新 TextView 显示
        textTotalIncome.setText(String.format(Locale.getDefault(), "总收入: $%.2f", totalIncome));
        textTotalExpense.setText(String.format(Locale.getDefault(), "总支出: $%.2f", totalExpense));

        double balance = totalIncome - totalExpense;
        textTotalBalance.setText(String.format(Locale.getDefault(), "总计: $%.2f", balance));
    }


    private List<Record> loadRecordsFromPreferences() {
        // 获取名为 "transaction_data" 的 SharedPreferences 对象，使用私有模式
        SharedPreferences prefs = getSharedPreferences("transaction_data", MODE_PRIVATE);
        String json = prefs.getString("records", null);
        if (json == null) {
            return new ArrayList<>();
        } else {
            return new Gson().fromJson(json, RECORD_LIST_TYPE);
        }
    }

    private void saveRecordsToPreferences() {
        SharedPreferences prefs = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(recordList);
        editor.putString("records", json);
        editor.apply();
    }
}