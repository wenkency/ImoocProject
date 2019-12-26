package cn.carhouse.audio.core;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import cn.carhouse.audio.state.MediaStatus;

/**
 * 带状态的MediaPlayer
 * 1. 向外发送各种播放状态
 * 2. IDLE->INITIALIZED->PREPARED->STARTED
 */
public class AudioMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {
    /**
     * 播放状态
     */
    private MediaStatus mStatus;
    // 完成监听
    private OnCompletionListener mOnCompletionListener;
    // 错误监听
    private OnErrorListener onErrorListener;
    private OnPreparedListener onPreparedListener;
    private OnBufferingUpdateListener onBufferingUpdateListener;

    public AudioMediaPlayer() {
        mStatus = MediaStatus.IDLE;
        // 完成监听
        super.setOnCompletionListener(this);
        super.setOnErrorListener(this);
        super.setOnPreparedListener(this);
        super.setOnBufferingUpdateListener(this);
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        mStatus = MediaStatus.STARTED;
        postStatus();
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        mStatus = MediaStatus.PAUSED;
        postStatus();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        mStatus = MediaStatus.STOPPED;
        postStatus();
    }

    @Override
    public void release() {
        super.release();
        mStatus = MediaStatus.RELEASE;
        postStatus();
    }

    @Override
    public void reset() {
        super.reset();
        mStatus = MediaStatus.RESET;
        postStatus();
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(path);
        mStatus = MediaStatus.INITIALIZED;
        postStatus();
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri) throws IOException,
            IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(context, uri);
        mStatus = MediaStatus.INITIALIZED;
        postStatus();
    }

    /**
     * 准备完成
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        mStatus = MediaStatus.PREPARED;
        postStatus();
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared(mp);
        }
    }

    /**
     * 播放完成
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        mStatus = MediaStatus.COMPLETED;
        postStatus();
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
    }

    /**
     * 错误
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mStatus = MediaStatus.ERRORED;
        postStatus();
        if (onErrorListener != null) {
            onErrorListener.onError(mp, what, extra);
        }
        return true;
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    /**
     * 只发送状态、和业务无关
     */
    public void postStatus() {
        EventBus.getDefault().post(mStatus);
        Log.e("postStatus", mStatus.toString());
    }


    /**
     * 获取当前状态
     */
    public MediaStatus getStatus() {
        return mStatus;
    }

    public void setStatus(MediaStatus mStatus) {
        this.mStatus = mStatus;
        // 状态改变了
        postStatus();
    }

    public boolean isCompleted() {
        return mStatus == MediaStatus.COMPLETED;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener) {
        this.onBufferingUpdateListener = onBufferingUpdateListener;
    }
}
