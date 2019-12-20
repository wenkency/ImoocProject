package cn.carhouse.audio.core;

import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.carhouse.audio.bean.AudioBean;
import cn.carhouse.audio.state.MediaStatus;
import cn.carhouse.audio.state.PlayModes;

public class AudioController implements MediaPlayer.OnCompletionListener {


    private static AudioController mInstance;

    public static AudioController getInstance() {
        if (mInstance == null) {
            synchronized (AudioController.class) {
                if (mInstance == null) {
                    mInstance = new AudioController();
                }
            }
        }
        return mInstance;
    }

    // 音乐播放器
    private AudioPlayer mAudioPlayer;
    // 音乐列表
    private List<AudioBean> mQueue = new ArrayList<>();
    // 播放歌曲位置
    private int mQueueIndex = 0;
    // 循环状态
    private PlayModes mPlayMode = PlayModes.LOOP;

    private AudioController() {
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.setOnCompletionListener(this);
    }

    /**
     * 播放方法
     */
    private void play(AudioBean bean) {
        if (bean == null) {
            return;
        }
        mAudioPlayer.play(bean);
    }

    private AudioBean getPlaying(int index) {
        if (mQueue != null && !mQueue.isEmpty() && index >= 0 && index < mQueue.size()) {
            return mQueue.get(index);
        } else {
            // throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        return null;
    }

    /*
     * 获取播放器当前状态
     */
    private MediaStatus getStatus() {
        return mAudioPlayer.getStatus();
    }

    private AudioBean getNextPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + 1) % mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    private AudioBean getPreviousPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + mQueue.size() - 1) % mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    private int queryAudio(AudioBean bean) {
        return mQueue.indexOf(bean);
    }

    private void addCustomAudio(int index, AudioBean bean) {
        if (mQueue == null) {
            // throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueue.add(index, bean);
    }

    /**
     * 设置播放队列
     */
    public void setQueue(ArrayList<AudioBean> queue) {
        setQueue(queue, 0);
    }

    public void setQueue(ArrayList<AudioBean> queue, int queueIndex) {
        mQueue.addAll(queue);
        mQueueIndex = queueIndex;
    }

    /**
     * 队列头添加播放哥曲
     */
    public void addAudio(AudioBean bean) {
        this.addAudio(0, bean);
    }

    public void addAudio(int index, AudioBean bean) {
        if (mQueue == null) {
            // throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        int query = queryAudio(bean);
        if (query <= -1) {
            // 没添加过此id的歌曲，添加且直播番放
            addCustomAudio(index, bean);
            setPlayIndex(index);
        } else {
            AudioBean currentBean = getNowPlaying();
            if (!currentBean.equals(bean)) {
                //添加过且不是当前播放，播，否则什么也不干
                setPlayIndex(query);
            }
        }
    }

    public void setPlayIndex(int index) {
        if (mQueue == null) {
            //  throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueueIndex = index;
        play();
    }

    /**
     * 对外提供是否播放中状态
     */
    public boolean isStartState() {
        return MediaStatus.STARTED == getStatus();
    }

    /**
     * 对外提提供是否暂停状态
     */
    public boolean isPauseState() {
        return MediaStatus.PAUSED == getStatus();
    }

    /**
     * 加载当前index歌曲
     */
    public void play() {
        play(getPlaying(mQueueIndex));
    }

    /**
     * 播放/暂停切换
     */
    public void playOrPause() {
        if (isStartState()) {
            pause();
        } else if (isPauseState()) {
            resume();
        }
    }

    /**
     * 对外提供的获取当前歌曲信息
     */
    public AudioBean getNowPlaying() {
        return getPlaying(mQueueIndex);
    }

    /**
     * 加载next index歌曲
     */
    public void next() {
        AudioBean bean = getNextPlaying();
        play(bean);
    }

    /**
     * 加载previous index歌曲
     */
    public void previous() {
        AudioBean bean = getPreviousPlaying();
        play(bean);
    }

    /**
     * 对外提供获取当前播放时间
     */
    public int getNowPlayTime() {
        return mAudioPlayer.getCurrentPosition();
    }

    /**
     * 对外提供获取总播放时间
     */
    public int getTotalPlayTime() {
        return mAudioPlayer.getDuration();
    }

    public void resume() {
        mAudioPlayer.resume();
    }

    public void pause() {
        mAudioPlayer.pause();
    }

    public void release() {
        mAudioPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mPlayMode == PlayModes.LOOP) {
            next();
        }
    }

    public void setPlayMode(PlayModes mPlayMode) {
        this.mPlayMode = mPlayMode;
        // 单曲播放
        if (mPlayMode == PlayModes.SINGLE) {
            mAudioPlayer.setLooping(true);
        }
    }
}
