package cn.carhouse.imoocproject.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.audio.bean.AudioEventBean;
import cn.carhouse.audio.core.AudioController;
import cn.carhouse.audio.state.MediaStatus;
import cn.carhouse.imageloader.ImageLoaderFactory;
import cn.carhouse.imoocproject.R;

/**
 * 这个是音乐播放底部View的封装
 */
public class MusicBottomLayout extends ConstraintLayout implements View.OnClickListener {
    private ImageView mIvIcon, mIvPlay, mIvPrevious, mIvNext, mIvShowList;
    private TextView mTvName, mTvAlbum;
    private AudioBean mAudioBean;
    private ObjectAnimator mAnimator;
    private long mCurrentPlayTime;

    public MusicBottomLayout(Context context) {
        this(context, null);
    }

    public MusicBottomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicBottomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 加载布局
        inflate(context, R.layout.layout_music_bottom, this);
        // 初始化View
        initViews();
    }

    private void initViews() {
        mIvIcon = findViewById(R.id.iv_icon);
        mIvPlay = findViewById(R.id.iv_play);
        mIvPrevious = findViewById(R.id.iv_previous);
        mIvNext = findViewById(R.id.iv_next);
        mIvShowList = findViewById(R.id.iv_show_list);
        mTvName = findViewById(R.id.tv_name);
        mTvAlbum = findViewById(R.id.tv_album);
        mIvPrevious.setOnClickListener(this);
        mIvPlay.setOnClickListener(this);
        mIvNext.setOnClickListener(this);

        mAnimator = ObjectAnimator.ofFloat(mIvIcon, View.ROTATION.getName(), 0f, 360);
        mAnimator.setDuration(10000);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setRepeatCount(-1);

        // 开启动画
        AudioBean nowPlaying = AudioController.getInstance().getNowPlaying();
        if (nowPlaying != null) {
            mCurrentPlayTime = AudioController.getInstance().getNowPlayTime();
            showLoadView(nowPlaying);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mIvPlay) {
            // 处理播放暂停事件
            AudioController.getInstance().playOrPause();
        } else if (v == mIvPrevious) {
            AudioController.getInstance().previous();
        } else if (v == mIvNext) {
            AudioController.getInstance().next();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(AudioEventBean bean) {
        MediaStatus status = bean.getMediaStatus();
        switch (status) {
            case IDLE:
                showLoadView(bean.getAudioBean());
                stopAnimation();
                break;
            case STARTED:
                if (bean.getEvent() != AudioEventBean.EVENT_UPDATE) {
                    showPlayView();
                }
                break;
            case PAUSED:
                if (bean.getEvent() != AudioEventBean.EVENT_UPDATE) {
                    showPauseView();
                }
                break;
            case INITIALIZED:
                break;
        }
    }

    private void showLoadView(AudioBean audioBean) {
        //目前loading状态的UI处理与pause逻辑一样，分开为了以后好扩展
        if (audioBean != null) {
            mAudioBean = audioBean;
            ImageLoaderFactory.getInstance().displayCircleImage(mIvIcon, mAudioBean.getAlbumPic());
            mTvName.setText(mAudioBean.getName());
            mTvAlbum.setText(mAudioBean.getAlbum());
            showPlayView();
        }
    }

    private void showPauseView() {
        if (mAudioBean != null) {
            mIvPlay.setImageResource(R.mipmap.note_btn_play_white);
            // 停止动画
            stopAnimation();
        }
    }

    private void showPlayView() {
        if (mAudioBean != null) {
            mIvPlay.setImageResource(R.mipmap.note_btn_pause_white);
            if (AudioController.getInstance().isStartState()) {
                // 开启动画
                startAnimation();
            }
        }
    }

    /**
     * 停止动画
     */
    private void stopAnimation() {
        mCurrentPlayTime = mAnimator.getCurrentPlayTime();
        mAnimator.cancel();
    }
    /**
     * 开始动画
     */
    private void startAnimation() {
        mAnimator.setCurrentPlayTime(mCurrentPlayTime);
        mAnimator.start();
    }
}
