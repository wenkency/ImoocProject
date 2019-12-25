package cn.carhouse.imoocproject.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import cn.carhouse.audio.app.AudioHelper;
import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.imageloader.ImageLoaderFactory;
import cn.carhouse.imoocproject.R;

/**
 * 音乐通知栏管理类
 */

public class MusicNotifyManager {
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    public static int NOTIFICATION_ID = 0x111;
    private Context mContext;
    private static final String channelId = "channel_id_audio";
    private static final String channelName = MusicNotifyManager.class.getSimpleName();
    private static final int importance = NotificationManager.IMPORTANCE_HIGH;
    private Notification mNotification;
    // 通知栏布局
    private RemoteViews mRemoteViews;
    private RemoteViews mSmallRemoteViews;
    private String packageName;
    //当前要播的歌曲Bean
    private AudioBean mAudioBean;
    private NotificationInitListener listener;

    private static MusicNotifyManager instance;

    public static MusicNotifyManager getInstance() {
        if (instance == null) {
            synchronized (MusicNotifyManager.class) {
                if (instance == null) {
                    instance = new MusicNotifyManager();
                }
            }
        }
        return instance;
    }

    private MusicNotifyManager() {

    }

    public MusicNotifyManager setListener(NotificationInitListener listener) {
        this.listener = listener;
        return this;
    }

    public void init(Context context) {
        mContext = context;
        packageName = context.getPackageName();
        if (mNotification == null) {
            // 1. 初始化RemoteView
            initRemoteViews();
            initNotifyManager();
            initNotify();
            if (listener != null) {
                listener.onNotificationInit();
            }
        }
    }


    public Notification getNotification() {
        return mNotification;
    }

    /*
     * 创建Notification的布局,默认布局为Loading状态
     */
    private void initRemoteViews() {
        mRemoteViews = new RemoteViews(packageName, R.layout.music_remote_big_content);
        mSmallRemoteViews = new RemoteViews(packageName, R.layout.music_remote_small_content);
        // 点击播放按钮广播
        Intent playIntent = new Intent(NotificationReceiver.ACTION_STATUS_BAR);
        playIntent.putExtra(NotificationReceiver.EXTRA,
                NotificationReceiver.EXTRA_PLAY);
        PendingIntent playPendingIntent =
                PendingIntent.getBroadcast(AudioHelper.getContext(), 1, playIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.play_view, playPendingIntent);
        mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.play_view, playPendingIntent);
        mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);

        // 点击上一首按钮广播
        Intent previousIntent = new Intent(NotificationReceiver.ACTION_STATUS_BAR);
        previousIntent.putExtra(NotificationReceiver.EXTRA,
                NotificationReceiver.EXTRA_PRE);
        PendingIntent previousPendingIntent =
                PendingIntent.getBroadcast(AudioHelper.getContext(), 2, previousIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.previous_view, previousPendingIntent);
        mRemoteViews.setImageViewResource(R.id.previous_view, R.mipmap.note_btn_pre_white);

        // 点击下一首按钮广播
        Intent nextIntent = new Intent(NotificationReceiver.ACTION_STATUS_BAR);
        nextIntent.putExtra(NotificationReceiver.EXTRA,
                NotificationReceiver.EXTRA_NEXT);
        PendingIntent nextPendingIntent =
                PendingIntent.getBroadcast(AudioHelper.getContext(), 3, nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.next_view, nextPendingIntent);
        mRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);
        mSmallRemoteViews.setOnClickPendingIntent(R.id.next_view, nextPendingIntent);
        mSmallRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

        //点击收藏按钮广播
        Intent favouriteIntent = new Intent(NotificationReceiver.ACTION_STATUS_BAR);
        favouriteIntent.putExtra(NotificationReceiver.EXTRA,
                NotificationReceiver.EXTRA_FAV);
        PendingIntent favouritePendingIntent =
                PendingIntent.getBroadcast(AudioHelper.getContext(), 4, favouriteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.favourite_view, favouritePendingIntent);
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
        mBuilder.setContentIntent(pendingintent)
                // 通常是用来表示一个后台任务
                .setOngoing(false)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setSmallIcon(mContext.getApplicationInfo().icon)
                .setCustomBigContentView(mRemoteViews)
                .setContent(mSmallRemoteViews); // 正常布局，两个布局可以切换;

        // 解决5.0系统通知栏白色Icon的问题
        Drawable appIcon = getAppIcon(mContext);
        Bitmap drawableToBitmap = null;
        if (appIcon != null) {
            drawableToBitmap = drawableToBitmap(appIcon);
        }
        if (drawableToBitmap != null) {
            mBuilder.setLargeIcon(drawableToBitmap);
        }
        mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_NO_CLEAR;
    }


    /**
     * 显示Notification的加载状态
     */
    public void showLoadStatus(AudioBean bean) {
        // 防止空指针crash
        mAudioBean = bean;
        if (mRemoteViews != null && mAudioBean != null) {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mRemoteViews.setTextViewText(R.id.title_view, mAudioBean.getName());
            mRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.getAlbum());

            ImageLoaderFactory.getInstance()
                    .displayNotificationImage(mContext, mNotification, mRemoteViews,
                            R.id.image_view, NOTIFICATION_ID, mAudioBean.getAlbumPic());

            //小布局也要更新
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mSmallRemoteViews.setTextViewText(R.id.title_view, mAudioBean.getName());
            mSmallRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.getAlbum());

            ImageLoaderFactory.getInstance()
                    .displayNotificationImage(mContext, mNotification, mSmallRemoteViews,
                            R.id.image_view, NOTIFICATION_ID, mAudioBean.getAlbumPic());


            mNotifyManager.notify(NOTIFICATION_ID, mNotification);
        }
    }

    public void showPlayStatus() {
        if (mRemoteViews != null) {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
            mNotifyManager.notify(NOTIFICATION_ID, mNotification);
        }
    }

    public void showPauseStatus() {
        if (mRemoteViews != null) {
            mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
            mNotifyManager.notify(NOTIFICATION_ID, mNotification);
        }
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

