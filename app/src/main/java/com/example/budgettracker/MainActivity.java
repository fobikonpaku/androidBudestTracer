package com.example.budgettracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends AppCompatActivity {
    // 添加记录列表类型
    static final Type RECORD_LIST_TYPE = new TypeToken<List<Record>>(){}.getType();
    private List<Record> recordList;
    private RecordAdapter recordAdapter;
    private TextView textTotalIncome, textTotalExpense, textTotalBalance;
    private ActivityResultLauncher<Intent> fragmentResultLauncher;
    private RelativeLayout mainLayout;
    private Button btnSwitchMode;
    private boolean isNightMode = false; // 标识当前模式

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recordList = new ArrayList<>();
        recordList = loadRecordsFromPreferences();
        recordAdapter = new RecordAdapter(recordList,this);

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
        Button autoAdd = findViewById(R.id.btnAutoHandleAdd);

        updateTotalValues();

        //手动添加监听
        btnAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRecordDialog("Income");
            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRecordDialog("Expense");
            }
        });
        autoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainFragment();
            }
        });
        fragmentResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        List<Record> records = (List<Record>) result.getData().getSerializableExtra("records");
                        if (records != null) {

                            Toast.makeText(getApplicationContext(), "导入中...", Toast.LENGTH_SHORT).show();
                            recordList.addAll(records);
                            saveRecordsToPreferences();
                            updateTotalValues();
                            recordAdapter.notifyDataSetChanged();
                        }
                    }
                    recordAdapter.notifyDataSetChanged();
                }
        );

    }

    private void startMainFragment() {
        Intent intent = new Intent(MainActivity.this, MainFragment.class);
        fragmentResultLauncher.launch(intent);
    }

    //手动添加逻辑
    @SuppressLint("NotifyDataSetChanged")
    private void showAddRecordDialog(final String type) {
        // 创建并显示对话框以输入记录详细信息
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_record, null);
        dialogBuilder.setView(dialogView);

        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        GridLayout iconGrid = dialogView.findViewById(R.id.iconGrid);

        final int[] selectedIconResId = {0}; // 使用数组来存储选中的图片资源ID


        @SuppressLint("ResourceType") View.OnClickListener iconClickListener = v -> {
            for (int i = 0; i < iconGrid.getChildCount(); i++) {
                iconGrid.getChildAt(i).setBackground(null);
            }
            v.setBackgroundResource(R.drawable.icon_selected_background);
            selectedIconResId[0] = (int) v.getTag();
        };

        int[] iconResIds = {
                //R.drawable.ic_default,
                R.drawable.ic_food,
                R.drawable.ic_fruit,
                R.drawable.ic_shopping
        };

        for (int iconResId : iconResIds) {
            ImageView iconView = new ImageView(this);
            iconView.setImageResource(iconResId);
            iconView.setTag(iconResId); // 将图片资源ID设置为标记
            iconView.setOnClickListener(iconClickListener);
            iconGrid.addView(iconView);
        }

        for (int i = 0; i < iconGrid.getChildCount(); i++) {
            iconGrid.getChildAt(i).setOnClickListener(iconClickListener);
        }

        dialogBuilder.setTitle("添加记录");
        dialogBuilder.setPositiveButton("添加", (dialog, which) -> {
            String amountStr = editTextAmount.getText().toString();
            String description = editTextDescription.getText().toString();
            int pid = recordAdapter.getItemCount()+1;
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                // 假设使用当前时间作为记录的时间戳
                long timestamp = System.currentTimeMillis();
                Record newRecord = new Record(pid, type, amount, description, timestamp, selectedIconResId[0]);
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

    //更新收支收入
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
        textTotalIncome.setText(String.format(Locale.getDefault(), "总收入: ￥%.2f", totalIncome));
        textTotalExpense.setText(String.format(Locale.getDefault(), "总支出: ￥%.2f", totalExpense));

        double balance = totalIncome - totalExpense;
        textTotalBalance.setText(String.format(Locale.getDefault(), "总计: ￥%.2f", balance));
    }

    //数据持久化读取
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

    //数据持久化储存
    private void saveRecordsToPreferences() {
        SharedPreferences prefs = getSharedPreferences("transaction_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(recordList);
        editor.putString("records", json);
        editor.apply();
    }


}