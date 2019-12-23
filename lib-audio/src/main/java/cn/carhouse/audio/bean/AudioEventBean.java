package cn.carhouse.audio.bean;

import org.greenrobot.eventbus.EventBus;

import cn.carhouse.audio.state.MediaStatus;

/**
 * 事件
 */
public class AudioEventBean {
    /**
     * 更新进度
     */
    public static final int EVENT_UPDATE = 0x0001;
    /**
     * 加载错误
     */
    public static final int EVENT_ERROR = 0x0002;
    /**
     * 销毁事件
     */
    public static final int EVENT_RELEASE = 0x0003;
    private  AudioBean audioBean;
    private MediaStatus mediaStatus;
    private int currentPosition;
    private int duration;
    /**
     * 事件
     */
    private int event;

    public AudioEventBean() {
    }

    public AudioEventBean(MediaStatus mediaStatus) {
        this.mediaStatus = mediaStatus;
    }

    public AudioEventBean(MediaStatus mediaStatus, int event) {
        this.mediaStatus = mediaStatus;
        this.event = event;
    }

    public AudioEventBean(MediaStatus mediaStatus, int currentPosition, int duration) {
        this.mediaStatus = mediaStatus;
        this.currentPosition = currentPosition;
        this.duration = duration;
        this.event = EVENT_UPDATE;
    }

    public AudioEventBean(MediaStatus mediaStatus, AudioBean bean) {
        this.mediaStatus=mediaStatus;
        this.audioBean=bean;
    }

    public MediaStatus getMediaStatus() {
        return mediaStatus;
    }

    public void setMediaStatus(MediaStatus mediaStatus) {
        this.mediaStatus = mediaStatus;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public AudioBean getAudioBean() {
        return audioBean;
    }

    public void setAudioBean(AudioBean audioBean) {
        this.audioBean = audioBean;
    }

    public static void post(MediaStatus mediaStatus, int currentPosition, int duration) {
        // 把自己发送出去
        EventBus.getDefault().post(new AudioEventBean(mediaStatus, currentPosition, duration));
    }

    public static void post(MediaStatus mediaStatus) {
        // 把自己发送出去
        EventBus.getDefault().post(new AudioEventBean(mediaStatus));
    }

    public static void post(MediaStatus mediaStatus, AudioBean bean) {
        // 把自己发送出去
        EventBus.getDefault().post(new AudioEventBean(mediaStatus,bean));
    }

    public static void post(MediaStatus mediaStatus, int event) {
        // 把自己发送出去
        EventBus.getDefault().post(new AudioEventBean(mediaStatus, event));
    }
}
