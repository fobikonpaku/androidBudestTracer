package com.example.budgettracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordDetailActivity extends AppCompatActivity {
    private static final int TAKE_PHOTO = 1;
    private String currentPhotoPath;
    private ImageView imageView;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickPhotoLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        ImageView iconImageView = findViewById(R.id.iconImageView);
        TextView typeTextView = findViewById(R.id.typeTextView);
        TextView amountTextView = findViewById(R.id.amountTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
//        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
//        Button btnChoosePhoto = findViewById(R.id.btnChoosePhoto);

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imageView.setImageURI(imageUri);
                    }
                });

        pickPhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            imageView.setImageURI(selectedImageUri);
                        }
                    }
                });

        // 获取传递的数据
        int id = getIntent().getIntExtra("id", 0);
        int iconResId = getIntent().getIntExtra("iconResId", 0);
        String type = getIntent().getStringExtra("type");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        String description = getIntent().getStringExtra("description");
        long date = getIntent().getLongExtra("date", 0);

        // 设置数据到视图
        iconImageView.setImageResource(iconResId);
        String typetoCh = null;
        if(type.equals("Expense")) typetoCh = "支出";
        else if(type.equals("Income")) typetoCh = "收入";
        typeTextView.setText("类型: " + typetoCh);
        amountTextView.setText(String.format(Locale.getDefault(), "金额：￥%.2f", amount));
        descriptionTextView.setText("备注：" + description);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateTextView.setText(sdf.format(new Date(date)));

        //btnTakePhoto.setOnClickListener(v -> takePhoto());
        //btnChoosePhoto.setOnClickListener(v -> choosePhoto());
    }

//    private void takePhoto() {
//        File outputImage = new File(getExternalCacheDir(),"output_image" + "%id" + ".jpg" );
//        try {
//            if(outputImage.exists()){
//                outputImage.delete();
//            }
//            outputImage.createNewFile();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        if(Build.VERSION.SDK_INT >= 24) {
//            imageUri = FileProvider.getUriForFile(this,"com.example.budgettracker.fileprovider",outputImage);
//        }else{
//            imageUri = Uri.fromFile(outputImage);
//        }
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
//        startActivityForResult(intent,TAKE_PHOTO);
//    }

}
