package com.example.budgettracker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private TessBaseAPI tessBaseAPI;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private int width;
    private int height;
    private int density;
    private Handler backgroundHandler;


    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        // 配置无障碍服务信息
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // 指定服务要监听的无障碍事件类型,窗口内容发生变化,窗口状态发生变化时
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        setServiceInfo(info);

        // 创建TessBaseAPI实例（这内部会创建原生的Tesseract实例）
        tessBaseAPI = new TessBaseAPI();

        // 给定的路径必须包含子目录`tessdata`，其中包含`.traineddata`语言文件
        // 该路径必须直接由应用可读
        // 愚蠢的路径，使我的app旋转
        String dataPath = getApplicationContext().getFilesDir().getAbsolutePath();
        dataPath = ensureTrailingSlash(dataPath);

//        Log.d("init Tessract", dataPath);
        // 初始化指定语言的API
        // （可以在Tesseract生命周期内多次调用）
        if (!tessBaseAPI.init(dataPath, "chi_sim")) {
            // 初始化Tesseract出错（错误的数据路径或不存在的语言文件）
            Log.d("init Tessract", "wrong");
            // 释放原生Tesseract实例
            tessBaseAPI.recycle();
            return;
        }

//        // 发送广播通知 Activity
//        Intent intent = new Intent("ACCESSIBILITY_SERVICE_ENABLED");
//        sendBroadcast(intent);
    }
    // 确保路径以斜杠结尾的方法
    String ensureTrailingSlash(String path) {
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra("data");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(); //创建通知渠道
            // 开始媒体投影相关代码
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);

            if (mediaProjection != null) {
                mediaProjection.registerCallback(new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        // 在MediaProjection停止时释放资源
                        if (virtualDisplay != null) {
                            virtualDisplay.release();
                            virtualDisplay = null;
                        }
                        if (imageReader != null) {
                            imageReader.close();
                            imageReader = null;
                        }
                        mediaProjection = null;
                    }
                }, new Handler());
                setupVirtualDisplay(mediaProjection);
            }
        }

//        setupVirtualDisplay();

        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    private void setupVirtualDisplay(MediaProjection mediaProjection) {
        // 获取屏幕参数
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        density = metrics.densityDpi;

        // 创建 ImageReader 实例
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                null
        );

        startForeground(1, getNotification());

        // 启动后台线程处理图像
        HandlerThread handlerThread = new HandlerThread("ImageReaderThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                // 处理可用的图像
                Image image = reader.acquireLatestImage();
                if (image != null) {
                    processImage(image);
                    image.close();
                }
            }
        }, backgroundHandler);
    }

    private void processImage(Image image) {
        // 处理图像数据
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();
        if (text.contains("收款") || text.contains("存入") || text.contains("支付") || text.contains("付款")) {
            saveRecord(text);
        }
    }

    private void saveRecord(String text) {
        // 解析 text 并保存记录
        String type = "";
        double amount = 0;
        String description = text;
        long date = System.currentTimeMillis();

        if (text.contains("已收款")) {
            type = "收入";
        } else if (text.contains("已存入")) {
            type = "收入";
        } else if (text.contains("已支付")) {
            type = "支出";
        }

        // 示例: 从文本中提取金额
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains("￥: ")) {
                String amountStr = line.replace("￥: ", "").trim();
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

//        Record record = new Record(type, amount, description, date);
//        // 这里可以将记录保存到共享偏好或数据库中
//        saveRecordToDatabase(record);
    }

//    private void saveRecordToDatabase(Record record) {
//        // 将记录保存到数据库的示例代码
//        // 这里可以实现数据库操作
//    }

    private Notification getNotification() {
        // 创建一个通知对象 Notification
        // 创建一个PendingIntent，当用户点击通知时打开MainActivity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "record_channel_id");
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle("正在录制屏幕")
                .setContentText("您的屏幕正在被录制")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        return builder.build();
    }


    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器

//        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
//                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
//                .setContentText("is running......") // 设置上下文内容
//                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            NotificationChannel channel = new NotificationChannel(
//                    "record_channel_id",
//                    "Screen Recording",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            notificationManager.createNotificationChannel(channel);
//        }

    }

    //辅助功能服务的一个回调方法，当系统检测到与辅助功能相关的事件，会调用这个方法。
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 处理无障碍事件
        AccessibilityNodeInfo source = event.getSource();
        if (source != null) {
            checkNode(source);
        }
    }

    private void checkNode(AccessibilityNodeInfo node) {
        // 递归检查节点内容
        if (node.getChildCount() == 0) {
            CharSequence text = node.getText();
            if (text != null && (text.toString().contains("收款") || text.toString().contains("已存入") || text.toString().contains("已支付"))) {
                captureAndProcessScreenshot();
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    checkNode(node.getChild(i));
                }
            }
        }
    }

    private void captureAndProcessScreenshot() {
        // 这里我们不需要显式调用，因为 imageReader 会自动处理图像
        //因为imageReader中新建了线程？
    }

    @Override
    public void onInterrupt() {
        // 处理中断
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tessBaseAPI != null) {
            tessBaseAPI.recycle();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (imageReader != null) {
            imageReader.close();
        }
        if (backgroundHandler != null) {
            backgroundHandler.getLooper().quitSafely();
        }
    }
}
