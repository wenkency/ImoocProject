package cn.carhouse.imoocproject.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * 通知栏管理类
 */

public class AppNotifyManager {
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private static int NOTIFICATION_ID = 0x1001;
    private Service mContext;
    private String mVersionName;
    private int mCurrentProgress = -1;
    private static final String channelId = AppNotifyManager.class.getSimpleName();
    private static final String channelName = "爱车小屋更新";
    private static final int importance = NotificationManager.IMPORTANCE_HIGH;
    private Notification mNotification;

    public AppNotifyManager(Service context, String versionName) {
        mContext = context;
        mVersionName = versionName;
        initNotifyManager();

        initNotify();
    }

    private void initNotifyManager() {
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // 适配Android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            // 这个是不要声音
            channel.setSound(null, null);
            mNotifyManager.createNotificationChannel(channel);
        }
    }

    /**
     * 1. 初始化通知栏
     */
    private void initNotify() {
        mBuilder = new NotificationCompat.Builder(mContext, channelId);
        PendingIntent pendingintent = PendingIntent.getActivity(
                mContext,
                100,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(pendingintent);
        mBuilder.setContentTitle(String.format("%s  %s",
                mContext.getString(mContext.getApplicationInfo().labelRes), "版本:" + mVersionName))
                // 通知首次出现在通知栏，带上升动画效果的
                .setTicker("爱车小屋")
                // 通常是用来表示一个后台任务
                .setOngoing(true)
                .setChannelId(channelId)
                .setPriority(Notification.PRIORITY_HIGH)
                // 通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true);

        // 解决5.0系统通知栏白色Icon的问题
        Drawable appIcon = getAppIcon(mContext);
        Bitmap drawableToBitmap = null;
        if (appIcon != null) {
            drawableToBitmap = drawableToBitmap(appIcon);
        }
        if (drawableToBitmap != null) {
            mBuilder.setSmallIcon(mContext.getApplicationInfo().icon);
            mBuilder.setLargeIcon(drawableToBitmap);
        } else {
            mBuilder.setSmallIcon(mContext.getApplicationInfo().icon);
        }
        mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(channelId, NOTIFICATION_ID, mNotification);
    }


    /**
     * 更新通知栏的进度(下载中)
     *
     * @param progress
     */
    public void updateProgress(int progress) {
        if (mCurrentProgress == progress) {
            return;
        }
        mCurrentProgress = progress;
        mBuilder.setContentText(String.format("正在下载:%1$d%%", progress))
                .setProgress(100, progress, false)
                .setWhen(System.currentTimeMillis());
        mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_NO_CLEAR;
        mNotifyManager.notify(channelId, NOTIFICATION_ID, mNotification);
    }

    /**
     * 合成更新的Icon
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 获取App的Icon
     */
    public Drawable getAppIcon(Context context) {
        try {
            return context.getPackageManager().getApplicationIcon(context.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 移除通知
     */
    public void cancel() {
        // 移除通知
        if (mNotifyManager != null) {
            mNotifyManager.cancel(channelId, NOTIFICATION_ID);
        }
    }
}

