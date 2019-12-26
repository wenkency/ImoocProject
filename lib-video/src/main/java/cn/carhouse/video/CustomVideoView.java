package cn.carhouse.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.annotation.NonNull;

/**
 * 视频播放
 */
public class CustomVideoView extends RatioFrameLayout implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, View.OnClickListener {
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
    // 是不是加载错误了
    private boolean isError;
    // 屏幕解锁
    private boolean isRealPause;
    private boolean isInit;
    // 重试次数
    private int reloadCount = 1;
    // 重试
    private int reload = 0;
    private SurfaceHolder mHolder;
    private ScreenEventReceiver mScreenReceiver;
    private int currentPosition;
    private boolean isClick;


    public CustomVideoView(Context context) {
        this(context, null);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.video_layout_custom, this);
        initViews();
    }

    private void initViews() {
        mIvSurface = findViewById(R.id.iv_surface);
        mBtnPlay = findViewById(R.id.btn_play);
        mIvLoading = findViewById(R.id.iv_loading);
        mSurfaceView = findViewById(R.id.surface_view);
        // 设置回调
        mHolder = mSurfaceView.getHolder();
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.setZOrderMediaOverlay(true);
        mHolder.addCallback(this);
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
        Log.e(TAG, "surfaceCreated");
        // 1. 创建MediaPlayer
        initMediaPlayer();
        // 2. 关联MediaPlayer
        mMediaPlayer.setDisplay(holder);
        if (!isInit) {
            isInit = true;
            // 3. 加载视频
            load();
        }
        if (isRealPause) {
            start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        // 销毁
        // release();
        if (isInit) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                isRealPause = true;
                pause();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        Log.e(TAG, "onVisibilityChanged:" + (visibility == View.VISIBLE));
        // 处理没图片问题
        if (isInit && (visibility == View.VISIBLE) && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(currentPosition);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerBroadcastReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unRegisterBroadcastReceiver();
        release();
    }

    private void release() {
        setStatus(VideoStatus.RELEASE);
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        isInit = false;
    }

    /**
     * 加载视频的方法
     */
    private void load() {
        if (TextUtils.isEmpty(mUrl) || mMediaPlayer == null) {
            return;
        }
        try {
            currentPosition = 0;
            setStatus(VideoStatus.IDLE);
            // 1. 显示加载的View
            showLoadingView();
            // 2. 重置
            mMediaPlayer.reset();
            setStatus(VideoStatus.RESET);
            // 3. 设置播放资源
            mMediaPlayer.setDataSource(mUrl);
            setStatus(VideoStatus.INITIALIZED);
            // 4. 异步准备
            mMediaPlayer.prepareAsync();
            // --> 5. onPrepared(MediaPlayer mp)
        } catch (Throwable e) {
            e.printStackTrace();
            // 5. 错误
            setStatus(VideoStatus.ERROR);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        reload = 0;
        setStatus(VideoStatus.PREPARED);
        showPlayView();
        // 如果设置了加载完成自动播放
        if (isAutoPlay || isError) {
            isError = false;
            // 播放
            playOrPause();
        }
    }

    private void start() {
        if (!mMediaPlayer.isPlaying()) {
            isRealPause = false;
            if (!isClick) {
                mMediaPlayer.seekTo(currentPosition);
            }
            isClick = false;
            mMediaPlayer.start();
            setStatus(VideoStatus.STARTED);
            mBtnPlay.setVisibility(GONE);
            mIvSurface.setVisibility(GONE);
        }
    }

    private void pause() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            setStatus(VideoStatus.PAUSED);
            currentPosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause();
            showPlayView();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setStatus(VideoStatus.COMPLETED);
        if (mMediaPlayer != null) {
            currentPosition = 0;
            mMediaPlayer.seekTo(currentPosition);
            pause();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        setStatus(VideoStatus.ERROR);
        isError = true;
        currentPosition = 0;
        showPlayView();
        // 错误加载后，重试
        if (reload < reloadCount) {
            reload++;
            playOrPause();
        }
        return true;
    }

    /**
     * 播放或者暂停
     */
    public void playOrPause() {
        if (mMediaPlayer == null) {
            return;
        }
        if (isPlaying()) {
            pause();
        } else if (isPause() || isPrepared() || isCompleted()) {
            start();
        } else if (isError()) {
            load();
        }
    }


    private void showLoadingView() {
        mIvLoading.setVisibility(VISIBLE);
        mBtnPlay.setVisibility(GONE);
        AnimationDrawable anim = (AnimationDrawable) mIvLoading.getBackground();
        anim.start();
        showThumbView();
    }

    private void showThumbView() {
        // 获取缩略图
        mIvSurface.setVisibility(VISIBLE);
        VideoThumbUtils.getInstance().getVideoThumb(mIvSurface, mUrl);
    }

    private void showPlayView() {
        mIvLoading.clearAnimation();
        mIvLoading.setVisibility(GONE);
        mBtnPlay.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            isClick = true;
            playOrPause();
        }
    }

    private boolean isPause() {
        return mStatus == VideoStatus.PAUSED;
    }

    private boolean isPlaying() {
        return mStatus == VideoStatus.STARTED;
    }

    private boolean isCompleted() {
        return mStatus == VideoStatus.COMPLETED;
    }

    private boolean isError() {
        return mStatus == VideoStatus.ERROR;
    }

    private boolean isPrepared() {
        return mStatus == VideoStatus.PREPARED;
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

    /**
     * 设置加载完成自动播放
     */
    public void setAutoPlay(boolean autoPlay) {
        this.isAutoPlay = autoPlay;
    }

    /**
     * 设置加载错误重新加载次数
     */
    public void setReloadCount(int reloadCount) {
        this.reloadCount = reloadCount;
    }


    private void registerBroadcastReceiver() {
        if (mScreenReceiver == null) {
            mScreenReceiver = new ScreenEventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            getContext().registerReceiver(mScreenReceiver, filter);
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (mScreenReceiver != null) {
            getContext().unregisterReceiver(mScreenReceiver);
        }
    }

    /**
     * 监听锁屏事件的广播接收器
     */
    private class ScreenEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //主动锁屏时 pause, 主动解锁屏幕时，resume
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (isPlaying()) {
                        isRealPause = true;
                        pause();
                    }
                    break;
            }
        }
    }

}
