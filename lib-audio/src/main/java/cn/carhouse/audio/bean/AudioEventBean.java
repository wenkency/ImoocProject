package cn.carhouse.audio.bean;

import org.greenrobot.eventbus.EventBus;

/**
 * 事件
 */
public class AudioEventBean {
    /**
     * 更新进度
     */
    public static final int EVENT_UPDATE = 0x0001;
    /**
     * 销毁事件
     */
    public static final int EVENT_RELEASE = 0x0002;
    /**
     * 事件
     */
    private int event;

    private AudioBean audioBean;
    private int currentPosition;
    private int duration;


    public AudioEventBean(int event) {
        this.event = event;
    }

    public AudioEventBean(int event, int currentPosition, int duration) {
        this.event = event;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }

    public AudioBean getAudioBean() {
        return audioBean;
    }

    public void setAudioBean(AudioBean audioBean) {
        this.audioBean = audioBean;
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

    public static void post(int currentPosition, int duration) {
        EventBus.getDefault().post(new AudioEventBean(EVENT_UPDATE, currentPosition, duration));
    }

    /**
     * 单纯发送事件
     */
    public static void post(int event) {
        EventBus.getDefault().post(new AudioEventBean(event));
    }
}
