package cn.carhouse.video;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

/**
 * 视频播放
 */
public class CustomVideoView extends RatioLayout implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, View.OnClickListener {
    public static final String TAG = CustomVideoView.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private MediaPlayer mMediaPlayer;
    private String mUrl;
    private ImageView mIvLoading;
    private ImageView mBtnPlay;
    private ImageView mIvSurface;
    private SurfaceHolder.Callback callback;
    private VideoStatus mStatus = VideoStatus.IDLE;
    // 加载完成后自动播放
    private boolean isAutoPlay;

    public CustomVideoView(Context context) {
        this(context, null);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        inflate(context, R.layout.video_layout_custom, this);
        initViews();
    }


    private void initViews() {
        mIvSurface = findViewById(R.id.iv_surface);
        mBtnPlay = findViewById(R.id.btn_play);
        mIvLoading = findViewById(R.id.iv_loading);
        mSurfaceView = findViewById(R.id.surface_view);
        // 设置回调
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        // 给自己设置点击监听
        this.setOnClickListener(this);
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 创建
        initMediaPlayer();
        // 关联MediaPlayer
        mMediaPlayer.setDisplay(holder);
        // 加载视频
        load();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 改变
        if (callback != null) {
            callback.surfaceChanged(holder, format, width, height);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 销毁
        release();
    }

    private void release() {
        setStatus(VideoStatus.RELEASE);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void load() {
        if (TextUtils.isEmpty(mUrl)) {
            return;
        }
        try {
            setStatus(VideoStatus.IDLE);
            // 1. 显示加载的View
            showLoadingView();
            mMediaPlayer.reset();
            setStatus(VideoStatus.RESET);
            mMediaPlayer.setDataSource(mUrl);
            setStatus(VideoStatus.INITIALIZED);
            mMediaPlayer.prepareAsync();
            // -->onPrepared
        } catch (Throwable e) {
            e.printStackTrace();
            setStatus(VideoStatus.ERROR);
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        setStatus(VideoStatus.PREPARED);
        showPlayView();
        if (isAutoPlay) {
            // 播放
            playOrPause();
        }
    }

    /**
     * 播放或者暂停
     */
    public void playOrPause() {
        if (isPlaying()) {
            pause();
        } else if (isPause() || isPrepared()) {
            start();
        } else if (isCompleted()) {
            load();
        }
    }


    private void showLoadingView() {
        mIvLoading.setVisibility(VISIBLE);
        mIvSurface.setVisibility(VISIBLE);
        mBtnPlay.setVisibility(GONE);
        AnimationDrawable anim = (AnimationDrawable) mIvLoading.getBackground();
        anim.start();
        // 获取缩略图
        VideoThumbUtils.getInstance().getVideoThumb(mIvSurface, mUrl);
    }

    private void showPlayView() {
        mIvLoading.clearAnimation();
        mIvLoading.setVisibility(GONE);
        mBtnPlay.setVisibility(VISIBLE);
        mIvSurface.setVisibility(VISIBLE);
    }


    @Override
    public void onClick(View v) {
        if (v == this) {
            playOrPause();
        }
    }


    private void pause() {
        setStatus(VideoStatus.PAUSED);
        mMediaPlayer.pause();
        showPlayView();
    }

    private boolean isPause() {
        return mStatus == VideoStatus.PAUSED;
    }

    private boolean isPlaying() {
        return mStatus == VideoStatus.STARTED;
    }

    private boolean isCompleted() {
        return mStatus == VideoStatus.COMPLETED
                || mStatus == VideoStatus.ERROR;
    }

    private boolean isPrepared() {
        return mStatus == VideoStatus.PREPARED;
    }

    private void start() {
        mMediaPlayer.start();
        setStatus(VideoStatus.STARTED);
        mBtnPlay.setVisibility(GONE);
        mIvSurface.setVisibility(GONE);
    }

    private void showPauseView() {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setStatus(VideoStatus.COMPLETED);
        showPlayView();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        setStatus(VideoStatus.ERROR);
        showPlayView();
        return true;
    }


    /**
     * 视频视频播放地址
     *
     * @param url
     */
    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setCallback(SurfaceHolder.Callback callback) {
        this.callback = callback;
    }

    public void setStatus(VideoStatus status) {
        this.mStatus = status;
        Log.e(TAG, mStatus.toString());
    }

    public void setAutoPlay(boolean autoPlay) {
        this.isAutoPlay = autoPlay;
    }
}