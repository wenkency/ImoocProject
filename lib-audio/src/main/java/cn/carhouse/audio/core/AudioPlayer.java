package cn.carhouse.audio.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.audio.bean.AudioEventBean;
import cn.carhouse.audio.state.MediaStatus;

/**
 * 1. 播放音频
 * 2. 发送各种事件
 */
public class AudioPlayer implements MediaPlayer.OnPreparedListener, AudioFocusManager.AudioFocusListener {
    // 更新事件
    private static final int TIME_MSG = 0x100;
    // 更新缓迟时间
    private static final int TIME_DELAY = 500;
    // 音乐播放器
    private AudioMediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    // 焦点监听
    private AudioFocusManager mAudioFocusManager;
    private Context mContext;
    private boolean isPaused = false;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // UI类型处理事件
            int currentPosition = getCurrentPosition();
            int duration = getDuration();
            switch (msg.what) {
                case TIME_MSG:
                    // 更新进度
                    // 暂停也要更新进度，防止UI不同步，只不过进度一直一样
                    if (isStart() || isPause()) {
                        // 发送更新事件
                        AudioEventBean.post(currentPosition, duration);
                        // 发送事件再更新
                        sendEmptyMessageDelayed(TIME_MSG, TIME_DELAY);
                    }
                    break;
            }
        }
    };


    public AudioPlayer(Context context) {
        mContext = context;

        mMediaPlayer = new AudioMediaPlayer();
        mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, AudioPlayer.class.getSimpleName());

        mAudioFocusManager = new AudioFocusManager(mContext, this);
    }


    /**
     * 加载播放音乐的方法
     */
    public void load(AudioBean bean) {
        try {
            mMediaPlayer.setStatus(MediaStatus.IDLE);
            // 先移除事件。不然会凉
            mHandler.removeMessages(TIME_MSG);
            // 开始准备
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(bean.getUrl());
            mMediaPlayer.prepareAsync();
            // 准备后会去--》onPrepared 或者 onError
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        // 准备好了
        start();
    }


    /**
     * prepare以后自动调用start方法,外部不能调用
     */
    private void start() {
        // 获取音频焦点,保证我们的播放器顺利播放
        if (!mAudioFocusManager.requestAudioFocus()) {
        }
        // 先移除事件。不然会凉
        mHandler.removeMessages(TIME_MSG);

        mMediaPlayer.start();
        // 启用wifi锁
        mWifiLock.acquire();
        // 更新进度
        mHandler.sendEmptyMessageDelayed(TIME_MSG, TIME_DELAY);
    }

    /**
     * 暂停
     */
    public void pause() {
        if (isStart()) {
            mMediaPlayer.pause();
            // 关闭wifi锁
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
            // 取消音频焦点
            if (mAudioFocusManager != null) {
                mAudioFocusManager.abandonAudioFocus();
            }
        }
    }

    /**
     * 对外提供的播放方法
     */
    public void resume() {
        if (isPause()) {
            start();
        }
    }

    public boolean isPause() {
        return getStatus() == MediaStatus.PAUSED;
    }

    public boolean isStart() {
        return getStatus() == MediaStatus.STARTED;
    }

    /**
     * 销毁唯一mediaplayer实例,只有在退出app时使用
     */
    public void release() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
        // 取消音频焦点
        if (mAudioFocusManager != null) {
            mAudioFocusManager.abandonAudioFocus();
        }
        // 关闭wifi锁
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        mWifiLock = null;
        mAudioFocusManager = null;
        mHandler.removeMessages(TIME_MSG);
        // 发送销毁播放器事件,清除通知等
        AudioEventBean.post(AudioEventBean.EVENT_RELEASE);
    }

    @Override
    public void audioFocusGrant() {
        // 获得焦点回调处理
        setVolume(1.0f, 1.0f);
        if (isPaused) {
            resume();
        }
        isPaused = false;
    }

    @Override
    public void audioFocusLoss() {
        //永久失去焦点，暂停
        if (mMediaPlayer != null) {
            pause();
        }
    }

    @Override
    public void audioFocusLossTransient() {
        // 短暂失去焦点，暂停
        if (mMediaPlayer != null) {
            pause();
        }
        isPaused = true;
    }

    @Override
    public void audioFocusLossDuck() {
        // 瞬间失去焦点回调
        setVolume(0.3f, 0.3f);
    }


    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
        }
    }


    /**
     * 获取当前音乐总时长,更新进度用
     */
    public int getDuration() {
        if (isStart() || isPause()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 获取当前音乐播放时长
     */
    public int getCurrentPosition() {
        if (isStart() || isPause()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public MediaStatus getStatus() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getStatus();
        }
        return MediaStatus.IDLE;
    }

    public void setStatus(MediaStatus status) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setStatus(status);
        }
    }

    /**
     * 设置音量
     */
    public void setVolume(float left, float right) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(left, right);
        }
    }


    /**
     * @param looping false走onCompletion
     */
    public void setLooping(boolean looping) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
    }

}
