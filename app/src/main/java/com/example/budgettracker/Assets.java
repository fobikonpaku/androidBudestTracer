package com.example.budgettracker;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {
//    将 .traineddata 文件从应用的资产目录（assets）中提取到应用的文件目录（filesDir）中
    @NonNull
    public static String getTessDataPath(@NonNull Context context) {
        return new File(context.getFilesDir(), "tessdata").getAbsolutePath();
    }

    public static void extractAssets(@NonNull Context context) {
        AssetManager am = context.getAssets();
        File tessDir = new File(getTessDataPath(context));
        if (!tessDir.exists() && !tessDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + tessDir);
        }
        try {
            for (String assetName : am.list("")) {
                if (assetName.endsWith(".traineddata")) {
                    File outFile = new File(tessDir, assetName);
                    if (!outFile.exists()) {
                        try (InputStream in = am.open(assetName);
                             OutputStream out = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                out.write(buffer, 0, read);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
