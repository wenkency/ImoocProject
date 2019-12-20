package cn.carhouse.audio.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;

import cn.carhouse.audio.app.AudioHelper;
import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.audio.bean.AudioEventBean;
import cn.carhouse.audio.state.MediaStatus;

/**
 * 1. 播放音频
 * 2. 发送各种事件
 */
public class AudioPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, AudioFocusManager.AudioFocusListener {
    public static final String TAG = AudioPlayer.class.getSimpleName();
    private static final int TIME_MSG = 0x100;
    private static final int TIME_DELAY = 500;
    private static final int TIME_CURRENT = -100;
    // 音乐播放器
    private AudioMediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    // 焦点监听
    private AudioFocusManager mAudioFocusManager;
    private Context mContext;
    private boolean isPaused = false;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    // 标识有没有播放完成
    private int mCurrentPosition = TIME_CURRENT;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // UI类型处理事件
            int currentPosition = getCurrentPosition();
            int duration = getDuration();
            // 完成播放了
            if (mCurrentPosition != TIME_CURRENT &&
                    getStatus() == MediaStatus.STARTED &&
                    mCurrentPosition == currentPosition) {
                Log.e(TAG, "mCurrentPosition:" + mCurrentPosition +" currentPosition:" + currentPosition + " duration:" + duration);
                resetCurrent();
                mMediaPlayer.onCompletion(mMediaPlayer);
                return;
            }

            switch (msg.what) {
                case TIME_MSG:
                    // 更新进度
                    //暂停也要更新进度，防止UI不同步，只不过进度一直一样
                    if (getStatus() == MediaStatus.STARTED
                            || getStatus() == MediaStatus.PAUSED) {
                        mCurrentPosition = currentPosition;
                        // 发送更新事件
                        AudioEventBean.post(getStatus(), currentPosition, duration);
                        // 发送事件再更新
                        sendEmptyMessageDelayed(TIME_MSG, TIME_DELAY);
                    }
                    break;
            }
        }
    };


    public MediaStatus getStatus() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getStatus();
        }
        return MediaStatus.IDLE;
    }

    public AudioPlayer() {
        mContext = AudioHelper.getContext();
        init();
    }

    private void init() {
        mMediaPlayer = new AudioMediaPlayer();
        mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, AudioPlayer.class.getSimpleName());

        mAudioFocusManager = new AudioFocusManager(mContext, this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    /**
     * 对外提供调用的方法
     */
    public void play(AudioBean bean) {
        try {
            // 手动设置状态最快
            mMediaPlayer.setStatus(MediaStatus.IDLE);
            Log.e(TAG, "" + bean.toString());
            resetCurrent();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(bean.getUrl());
            mMediaPlayer.prepareAsync();
            // -->onPrepared
            // TODO 发送事件
            AudioEventBean.post(getStatus());
        } catch (Throwable e) {
            e.printStackTrace();
            // TODO 发送事件
            AudioEventBean.post(getStatus(), AudioEventBean.EVENT_ERROR);
        }

    }

    private void resetCurrent() {
        mCurrentPosition = TIME_CURRENT;
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
            Log.e(TAG, "获取音频焦点失败");
        }
        resetCurrent();
        // 先移除事件。不然会凉
        mHandler.removeMessages(TIME_MSG);
        mMediaPlayer.start();
        // 启用wifi锁
        mWifiLock.acquire();
        // 更新进度
        mHandler.sendEmptyMessageDelayed(TIME_MSG, TIME_DELAY);
        // TODO　发送start事件，UI类型处理事件
        AudioEventBean.post(getStatus());
    }

    /**
     * 暂停
     */
    public void pause() {
        if (getStatus() == MediaStatus.STARTED) {
            mMediaPlayer.pause();
            // 关闭wifi锁
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
            // 取消音频焦点
            if (mAudioFocusManager != null) {
                mAudioFocusManager.abandonAudioFocus();
            }
            // TODO 发送暂停事件,UI类型事件
            AudioEventBean.post(getStatus());
        }
    }

    /**
     * 对外提供的播放方法
     */
    public void resume() {
        if (getStatus() == MediaStatus.PAUSED) {
            start();
        }
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
        // TODO 发送销毁播放器事件,清除通知等
        AudioEventBean.post(getStatus(), AudioEventBean.EVENT_RELEASE);
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
        // 播放完成
        AudioEventBean.post(getStatus());
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // 异常处理
        AudioEventBean.post(getStatus(), AudioEventBean.EVENT_ERROR);
        return true;
    }

    /**
     * 获取当前音乐总时长,更新进度用
     */
    public int getDuration() {
        if (getStatus() == MediaStatus.STARTED
                || getStatus() == MediaStatus.PAUSED) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (getStatus() == MediaStatus.STARTED
                || getStatus() == MediaStatus.PAUSED) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

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
