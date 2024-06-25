package com.example.budgettracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Serializable;






public class MainFragment extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSIONS = 1;
    public static final String TESS_DATA = "/tessdata";
    //private static final String DATA_FILENAME = "eng.traineddata";
    private static final String DATA_FILENAME = "chi_sim.traineddata";
    private EditText tv_result;
    private Button btn_upload;
    private Button btn_tiqu;
    private Button btn_commit;
    private ImageView image;
    private String text;
    private Bitmap selectedBitmap;
    StringBuilder descriptionBuilder = new StringBuilder();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);


        tv_result = findViewById(R.id.tv_result);
        btn_upload = findViewById(R.id.btn_upload);
        btn_tiqu = findViewById(R.id.btn_tiqu);
        btn_commit = findViewById(R.id.btn_commit);
        image = findViewById(R.id.image);

//        Bitmap origin = getBitmapFromAssets(this, "cs.png");


        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 选择图片
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);


            }
        });
        btn_tiqu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedBitmap != null) {
                    image.setImageBitmap(selectedBitmap);
                     // 对 Bitmap 进行预处理
                    selectedBitmap = convertGreyImg(selectedBitmap);
                    selectedBitmap = cropBitmapLeft(selectedBitmap, 210);

                    //提取
                    recognizeTextFromBitmap(selectedBitmap);
                } else {
                    Toast.makeText(MainFragment.this, "请先上传图片", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = tv_result.getText().toString();
                List<Record> records = parseTextToRecords(text);
                // 设置返回数据
                Intent resultIntent = new Intent();
                resultIntent.putExtra("records", (Serializable) records);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

    }

    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private Bitmap cropBitmapLeft(Bitmap bitmap, int cropWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int x = cropWidth;
        int y = 0;
        int newWidth = width - cropWidth;

        return Bitmap.createBitmap(bitmap, x, y, newWidth, height);
    }

    private Bitmap getBitmapFromAssets(Context context, String filename) {
        Bitmap bitmap = null;
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open(filename);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            Log.i("TAG", "图片读取成功。");
            Toast.makeText(getApplicationContext(), "图片读取成功。", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.i("TAG", "图片读取失败。");
            Toast.makeText(getApplicationContext(), "图片读取失败。", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return bitmap;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null) {
                    Uri imageUri = data.getData();
                    try {
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        image.setImageBitmap(selectedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void recognizeTextFromBitmap(Bitmap image) {
        prepareTess();
        TessBaseAPI tess = new TessBaseAPI();
        String dataPath = getExternalFilesDir("/").getPath() + "/";
        if (!tess.init(dataPath, "chi_sim")) {
            tess.recycle();
            return;
        }
        tess.setImage(image);
        text = tess.getUTF8Text();
        tv_result.setText(text);


        tess.recycle();
    }

    private List<Record> parseTextToRecords(String text) {
        List<Record> records = new ArrayList<>();
        StringBuilder descriptionBuilder = new StringBuilder();

        // 按行分割文本
        String[] lines = text.split("\\n");

        for (String line : lines) {
            int lastNegativeIndex = -1;
            for (int i = line.length() - 1; i >= 0; i--) {
                if (line.charAt(i) == '-') {
                    lastNegativeIndex = i;
                    break;
                }
            }

            if (lastNegativeIndex != -1) {
                String description = line.substring(0, lastNegativeIndex).trim();
                String amountStr = line.substring(lastNegativeIndex).trim();
                try {
                    double amount = Double.parseDouble(amountStr);
                    long date = System.currentTimeMillis();

                    // 根据金额的符号决定类型
                    String type = amount < 0 ? "Expense" : "Income";

                    // 创建记录时设置默认图标资源
                    Record record = new Record(0,type, Math.abs(amount), description, date, R.drawable.ic_default_ult);
                    records.add(record);

                    descriptionBuilder.append(description).append("\n");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        String test = descriptionBuilder.toString().trim();
        tv_result.setText(test);

        for (Record record : records) {
            Log.d("RecordInfo", "Type: " + record.getType() + ", Description: " + record.getDescription() + ", Amount: " + record.getAmount());
        }

        return records;
    }


    private void prepareTess() {
        try{
            File dir = getExternalFilesDir(TESS_DATA);
            if(!dir.exists()){
                if (!dir.mkdir()) {
                    Toast.makeText(getApplicationContext(), "目录" + dir.getPath() + "没有创建成功", Toast.LENGTH_SHORT).show();
                }
            }
            String pathToDataFile = dir + "/" + DATA_FILENAME;
            if (!(new File(pathToDataFile)).exists()) {
                InputStream in = getAssets().open(DATA_FILENAME);
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte[] buff = new byte[1024];
                int len;
                while ((len = in.read(buff)) > 0) {
                    out.write(buff, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Toast.makeText(this, "所有权限都需要被授予才能使用此应用程序", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
}
