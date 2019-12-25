package cn.carhouse.imoocproject.utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cn.carhouse.audio.app.AudioHelper;
import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.audio.bean.AudioEventBean;
import cn.carhouse.audio.core.AudioController;
import cn.carhouse.audio.state.MediaStatus;

public class MusicService extends Service implements NotificationInitListener {


    private static String DATA_AUDIOS = "AUDIOS";
    // actions
    private static String ACTION_START = "ACTION_START";

    private List<AudioBean> mAudioBeans;

    private NotificationReceiver mReceiver;

    /**
     * 外部直接service方法
     */
    public static void startMusicService(ArrayList<AudioBean> audioBeans) {
        if (audioBeans == null || audioBeans.size() <= 0) {
            return;
        }
        Intent intent = new Intent(AudioHelper.getContext(), MusicService.class);
        intent.setAction(ACTION_START);
        //还需要传list数据进来
        intent.putExtra(DATA_AUDIOS, audioBeans);
        AudioHelper.getContext().startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        registerBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAudioBeans = (ArrayList<AudioBean>) intent.getSerializableExtra(DATA_AUDIOS);
        if (ACTION_START.equals(intent.getAction())) {
            AudioController.getInstance().setQueue(mAudioBeans);
            // 初始化前台Notification
            MusicNotifyManager.getInstance().setListener(this);
            MusicNotifyManager.getInstance().init(this);
            // 开始播放
            playMusic();

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void playMusic() {
        AudioController.getInstance().play();
    }

    private void registerBroadcastReceiver() {
        if (mReceiver == null) {
            mReceiver = new NotificationReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(NotificationReceiver.ACTION_STATUS_BAR);
            registerReceiver(mReceiver, filter);
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onNotificationInit() {
        // service与Notification绑定
        startForeground(MusicNotifyManager.NOTIFICATION_ID,
                MusicNotifyManager.getInstance().getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unRegisterBroadcastReceiver();
    }

    @Subscribe
    public void onEvent(AudioEventBean bean) {
        MediaStatus status = bean.getMediaStatus();
        switch (status) {
            case IDLE:
                MusicNotifyManager.getInstance().showLoadStatus(bean.getAudioBean());
                break;
            case STARTED:
                if (bean.getEvent() != AudioEventBean.EVENT_UPDATE) {
                    MusicNotifyManager.getInstance().showPlayStatus();
                }
                break;
            case PAUSED:
                if (bean.getEvent() != AudioEventBean.EVENT_UPDATE) {
                    MusicNotifyManager.getInstance().showPauseStatus();
                }
                break;
            case INITIALIZED:
                break;
        }
    }


}
