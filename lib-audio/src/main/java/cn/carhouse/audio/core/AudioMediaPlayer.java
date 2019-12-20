package cn.carhouse.audio.core;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import cn.carhouse.audio.state.MediaStatus;

/**
 * 带状态的MediaPlayer
 */
public class AudioMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener {
    public static final String TAG = AudioController.class.getSimpleName();
    /**
     * 播放状态
     */
    private MediaStatus mStatus;
    private OnCompletionListener mOnCompletionListener;

    public AudioMediaPlayer() {
        super();
        mStatus = MediaStatus.IDLE;
        setLooping(false);
        // 完成监听
        this.setOnCompletionListener(this);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        mStatus = MediaStatus.STARTED;
        Log.e(TAG,"start");
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        mStatus = MediaStatus.PAUSED;
        Log.e(TAG,"pause");
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        mStatus = MediaStatus.STOPPED;
        Log.e(TAG,"stop");
    }

    @Override
    public void release() {
        super.release();
        mStatus = MediaStatus.IDLE;
        Log.e(TAG,"release");
    }

    @Override
    public void reset() {
        super.reset();
        mStatus = MediaStatus.IDLE;
        Log.e(TAG,"reset");
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(path);
        mStatus = MediaStatus.INITIALIZED;
        Log.e(TAG,"setDataSource");
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri);
        mStatus = MediaStatus.INITIALIZED;
        Log.e(TAG,"setDataSource");
    }


    @Override
    public void seekTo(int time) throws IllegalStateException {
        super.seekTo(time);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mStatus = MediaStatus.COMPLETED;
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
        Log.e(TAG,"onCompletion");
    }

    /**
     * 获取当前状态
     */
    public MediaStatus getStatus() {
        return mStatus;
    }

    public boolean isCompleted() {
        return mStatus == MediaStatus.COMPLETED;
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }
}
